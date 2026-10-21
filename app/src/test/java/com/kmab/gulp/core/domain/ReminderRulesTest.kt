package com.kmab.gulp.core.domain

import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.*
import org.junit.Test

class ReminderRulesTest {
    private val zone = ZoneId.of("Europe/Istanbul")
    private val normal = ReminderPlan(enabled = true, intervalMinutes = 90, awakeStartMinute = 480, awakeEndMinute = 1320)
    private fun time(day: LocalDate, hour: Int, minute: Int = 0) = day.atTime(hour, minute).atZone(zone).toInstant().toEpochMilli()

    @Test fun daytimeSlots() {
        val day = LocalDate.of(2026,10,20)
        assertEquals(time(day,8), ReminderRules.nextAlarm(time(day,7,59),zone,normal))
        assertEquals(time(day,9,30), ReminderRules.nextAlarm(time(day,8),zone,normal))
        assertEquals(time(day.plusDays(1),8), ReminderRules.nextAlarm(time(day,22),zone,normal))
    }

    @Test fun overnightWindowAndQuietHours() {
        val day = LocalDate.of(2026,10,20)
        val night = normal.copy(awakeStartMinute = 22 * 60, awakeEndMinute = 6 * 60, intervalMinutes = 120)
        assertEquals(time(day,0), ReminderRules.nextAlarm(time(day.minusDays(1),23,59),zone,night))
        assertEquals(time(day,22), ReminderRules.nextAlarm(time(day,6,1),zone,night))
        assertFalse(ReminderRules.shouldNotify(time(day,12),time(day,12),zone,night,null))
    }

    @Test fun snoozeAndRecentDrink() {
        val day = LocalDate.of(2026,10,20)
        val now = time(day,9)
        val snoozed = normal.copy(snoozeUntilMillis = now+15*60_000L)
        assertEquals(now+15*60_000L, ReminderRules.nextAlarm(now,zone,snoozed))
        assertFalse(ReminderRules.shouldNotify(now, now, zone, normal, now-10*60_000L))
        assertTrue(ReminderRules.shouldNotify(now, now, zone, normal, now-50*60_000L))
        assertFalse(ReminderRules.shouldNotify(now+51*60_000L,now,zone,normal,null))
    }

    @Test fun noUnrequestedReminders() {
        val now = System.currentTimeMillis()
        assertNull(ReminderRules.nextAlarm(now,zone,normal.copy(enabled=false)))
        assertFalse(ReminderRules.shouldNotify(now,now,zone,normal.copy(enabled=false),null))
    }
}
