package app.gulp.android.core.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import app.gulp.android.core.domain.HomeTile
import app.gulp.android.core.domain.HomeTileRules
import app.gulp.android.core.domain.ReminderPlan
import app.gulp.android.core.domain.TrackingReference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.gulpDataStore by preferencesDataStore(name = "gulp_settings")

data class GulpPreferences(
    val tiles: List<HomeTile> = HomeTileRules.defaults,
    val visualCapacityMl: Int = 2000,
    val hasCompletedOnboarding: Boolean = false,
    val trackingReferenceMl: Int = 0,
    val clinicianManagedFluids: Boolean = false,
    val reminders: ReminderPlan = ReminderPlan()
)

/** UI, receiver and alarm scheduler all read this single persistent source of truth. */
class GulpPreferencesStore(private val context: Context) {
    private val tileKey = stringPreferencesKey("home_tiles_v1")
    private val displayCapacityKey = intPreferencesKey("water_display_capacity_ml_v1")
    private val onboardKey = booleanPreferencesKey("onboarding_completed_v1")
    private val referenceKey = intPreferencesKey("tracking_reference_ml_v1")
    private val medicalKey = booleanPreferencesKey("clinician_managed_fluids_v1")
    private val reminderEnabled = booleanPreferencesKey("reminder_enabled_v1")
    private val intervalKey = intPreferencesKey("reminder_interval_min_v1")
    private val wakeKey = intPreferencesKey("reminder_awake_start_v1")
    private val quietKey = intPreferencesKey("reminder_awake_end_v1")
    private val snoozeKey = longPreferencesKey("reminder_snooze_until_v1")
    private val alertTokenKey = longPreferencesKey("last_visible_notification_token_v1")
    private val consumedTokenKey = longPreferencesKey("last_used_notification_action_token_v1")

    val settings: Flow<GulpPreferences> = context.gulpDataStore.data.map { p ->
        val plan = ReminderPlan(
            enabled = p[reminderEnabled] ?: false,
            intervalMinutes = p[intervalKey] ?: 90,
            awakeStartMinute = p[wakeKey] ?: 480,
            awakeEndMinute = p[quietKey] ?: 1320,
            snoozeUntilMillis = p[snoozeKey] ?: 0L
        )
        GulpPreferences(
            tiles = HomeTileRules.decode(p[tileKey]),
            visualCapacityMl = (p[displayCapacityKey] ?: 2000).coerceIn(250, 6000),
            hasCompletedOnboarding = p[onboardKey] ?: false,
            trackingReferenceMl = TrackingReference.sanitized(p[referenceKey] ?: 0),
            clinicianManagedFluids = p[medicalKey] ?: false,
            reminders = if (plan.valid()) plan else ReminderPlan()
        )
    }

    suspend fun snapshot(): GulpPreferences = settings.first()

    suspend fun saveTiles(tiles: List<HomeTile>) {
        require(tiles.size in HomeTileRules.MIN_TILES..HomeTileRules.MAX_TILES)
        require(tiles.distinctBy { it.id }.size == tiles.size)
        require(tiles.all { it.amountMl in 1..5000 })
        context.gulpDataStore.edit { it[tileKey] = HomeTileRules.encode(tiles) }
    }

    suspend fun changeVisualCapacity(ml: Int) {
        require(ml in 250..6000)
        context.gulpDataStore.edit { it[displayCapacityKey] = ml }
    }

    suspend fun finishOnboarding(plan: ReminderPlan, referenceMl: Int, clinicianManaged: Boolean) {
        require(plan.valid())
        context.gulpDataStore.edit { p ->
            p[onboardKey] = true
            p[reminderEnabled] = plan.enabled
            p[intervalKey] = plan.intervalMinutes
            p[wakeKey] = plan.awakeStartMinute
            p[quietKey] = plan.awakeEndMinute
            p[snoozeKey] = 0L
            p[medicalKey] = clinicianManaged
            p[referenceKey] = if (clinicianManaged) 0 else TrackingReference.sanitized(referenceMl)
        }
    }

    suspend fun setReminders(plan: ReminderPlan) {
        require(plan.valid())
        context.gulpDataStore.edit { p ->
            p[reminderEnabled] = plan.enabled
            p[intervalKey] = plan.intervalMinutes
            p[wakeKey] = plan.awakeStartMinute
            p[quietKey] = plan.awakeEndMinute
            p[snoozeKey] = plan.snoozeUntilMillis
        }
    }

    /** Each notification can trigger at most one action, even under double taps/redelivery. */
    suspend fun markNotificationVisible(token: Long) {
        require(token > 0L)
        context.gulpDataStore.edit { it[alertTokenKey] = token }
    }

    suspend fun consumeNotificationAction(token: Long): Boolean {
        if (token <= 0L) return false
        var accepted = false
        context.gulpDataStore.edit { p ->
            if (p[alertTokenKey] == token && p[consumedTokenKey] != token) {
                p[consumedTokenKey] = token
                accepted = true
            }
        }
        return accepted
    }

    suspend fun snoozeUntil(timeMillis: Long) {
        context.gulpDataStore.edit { it[snoozeKey] = timeMillis.coerceAtLeast(0) }
    }

    suspend fun setTracking(referenceMl: Int, clinicianManaged: Boolean) {
        context.gulpDataStore.edit { p ->
            p[medicalKey] = clinicianManaged
            p[referenceKey] = if (clinicianManaged) 0 else TrackingReference.sanitized(referenceMl)
        }
    }
}
