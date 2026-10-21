package com.kmab.gulp.core.domain

import java.time.LocalDate
import java.time.ZoneId

data class DayRecord(val date: LocalDate, val loggedMl: Int, val entryCount: Int)
data class WeekReview(
    val days: List<DayRecord>, val totalMl: Long, val averagePerDayMl: Long,
    val recordedDays: Int, val entries: Int, val mostLoggedDrink: DrinkKind?,
    val previousWeekTotalMl: Long?, val previousWeekRecordedDays: Int
)

object ReviewRules {
    /** A fixed last-seven-calendar-day window; zeros are days without *entries*, not zero intake. */
    fun summarize(records: List<DrinkRecord>, today: LocalDate, zone: ZoneId): WeekReview {
        val currentStart = today.minusDays(6)
        val lastStart = currentStart.minusDays(7)
        val dates = records.groupBy { DrinkRules.localDate(it, zone) }
        val days = (0L..6L).map { n ->
            val date = currentStart.plusDays(n)
            val entries = dates[date].orEmpty()
            DayRecord(date, entries.sumOf { it.amountMl }, entries.size)
        }
        val current = records.filter { DrinkRules.localDate(it, zone) in currentStart..today }
        val previous = records.filter { DrinkRules.localDate(it, zone) in lastStart..currentStart.minusDays(1) }
        val most = current.groupBy { it.kind }.maxByOrNull { (_, values) -> values.sumOf { it.amountMl } }?.key
        val prevDays = previous.map { DrinkRules.localDate(it, zone) }.distinct().size
        return WeekReview(days, days.sumOf { it.loggedMl.toLong() },
            days.sumOf { it.loggedMl.toLong() } / 7L,
            days.count { it.entryCount > 0 }, current.size, most,
            if (prevDays > 0) previous.sumOf { it.amountMl.toLong() } else null, prevDays)
    }
}
