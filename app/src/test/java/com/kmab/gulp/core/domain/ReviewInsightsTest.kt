package com.kmab.gulp.core.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class ReviewInsightsTest {
    private val zone = ZoneId.of("UTC")
    private val today = LocalDate.of(2026, 10, 27)
    private fun record(date: LocalDate, hour: Int, amount: Int, type: String): DrinkRecord = DrinkRecord(
        amountMl = amount, kind = if (type == "water") DrinkKind.WATER else DrinkKind.OTHER,
        timeMillis = date.atTime(hour, 0).atZone(zone).toInstant().toEpochMilli(),
        drinkTypeId = type, drinkName = type, drinkIcon = "glass"
    )

    @Test fun emptyReportHasNoInventedAverage() {
        val report = ReviewInsightRules.summarize(emptyList(), today, zone)
        assertEquals(0L, report.total30Ml)
        assertEquals(0, report.daysRecorded30)
        assertNull(report.averageWhenRecordedMl)
        assertTrue(report.drinkShares7.isEmpty())
    }

    @Test fun separatesSevenAndThirtyDayWindows() {
        val logs = listOf(record(today,8,200,"water"),
            record(today.minusDays(1),21,300,"juice"),
            record(today.minusDays(15),14,400,"juice"),
            record(today.minusDays(40),9,900,"water"))
        val report = ReviewInsightRules.summarize(logs,today,zone)
        assertEquals(900L,report.total30Ml)
        assertEquals(3,report.daysRecorded30)
        assertEquals(250L,report.averageWhenRecordedMl)
        assertEquals(1,report.morningCount7)
        assertEquals(1,report.eveningCount7)
        assertEquals(0,report.afternoonCount7)
        assertEquals(2,report.drinkShares7.size)
        assertTrue(report.drinkShares7.any { it.id == "juice" && it.amountMl == 300 })
    }
}
