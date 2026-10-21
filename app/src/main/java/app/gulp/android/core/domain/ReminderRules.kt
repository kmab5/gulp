package app.gulp.android.core.domain

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** The interval is a behavioral nudge, never a dose or physiological schedule. */
data class ReminderPlan(
    val enabled: Boolean = false,
    val intervalMinutes: Int = 90,
    val awakeStartMinute: Int = 8 * 60,
    val awakeEndMinute: Int = 22 * 60,
    val snoozeUntilMillis: Long = 0L
) {
    val awakeDurationMinutes: Int get() = (awakeEndMinute - awakeStartMinute + 1440) % 1440

    fun valid(): Boolean = intervalMinutes in 60..240 && intervalMinutes % 30 == 0 &&
        awakeStartMinute in 0..1439 && awakeEndMinute in 0..1439 && awakeDurationMinutes in 60..1380
}

object ReminderRules {
    const val RECENT_DRINK_MINUTES = 45L
    const val MAX_LATE_MINUTES = 50L

    /** Anchored to local waking time, including awake windows that cross midnight and DST. */
    fun nextOpportunity(afterMillis: Long, zone: ZoneId, plan: ReminderPlan): Long? {
        if (!plan.enabled || !plan.valid()) return null
        val day = Instant.ofEpochMilli(afterMillis).atZone(zone).toLocalDate()
        var best: Long? = null
        for (offset in -1L..2L) {
            val date: LocalDate = day.plusDays(offset)
            // Construct each opportunity on the *local clock*. Adding elapsed minutes to
            // midnight shifts 08:00 to 09:00 when the spring DST jump removes an hour.
            // A gap resolves to the first valid time; an overlap uses the earlier offset.
            val count = (plan.awakeDurationMinutes - 1) / plan.intervalMinutes
            for (n in 0..count) {
                val localMinute = plan.awakeStartMinute + n * plan.intervalMinutes
                val localTime = date.atStartOfDay().plusMinutes(localMinute.toLong())
                val t = localTime.atZone(zone).toInstant().toEpochMilli()
                if (t > afterMillis && (best == null || t < best)) best = t
            }
        }
        return best
    }

    /** A snooze is the next one-off alert; it doesn't create catch-up drinking debt. */
    fun nextAlarm(nowMillis: Long, zone: ZoneId, plan: ReminderPlan): Long? {
        if (!plan.enabled || !plan.valid()) return null
        return if (plan.snoozeUntilMillis > nowMillis) plan.snoozeUntilMillis
        else nextOpportunity(nowMillis, zone, plan)
    }

    fun shouldNotify(
        nowMillis: Long,
        expectedMillis: Long,
        zone: ZoneId,
        plan: ReminderPlan,
        latestLoggedDrinkMillis: Long?
    ): Boolean {
        if (!plan.enabled || !plan.valid() || nowMillis < expectedMillis) return false
        if (nowMillis - expectedMillis > MAX_LATE_MINUTES * 60_000L) return false
        val localMinute = Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalTime().let { it.hour * 60 + it.minute }
        val within = if (plan.awakeStartMinute < plan.awakeEndMinute)
            localMinute in plan.awakeStartMinute until plan.awakeEndMinute
        else localMinute >= plan.awakeStartMinute || localMinute < plan.awakeEndMinute
        if (!within) return false
        return latestLoggedDrinkMillis == null ||
            latestLoggedDrinkMillis > nowMillis || nowMillis - latestLoggedDrinkMillis >= RECENT_DRINK_MINUTES * 60_000L
    }

    fun formatMinute(minute: Int): String = "%02d:%02d".format(minute / 60, minute % 60)
}
