package com.kmab.gulp.core.domain

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** Descriptive statistics about *records*, never estimates of physiological hydration. */
data class DrinkShare(val id: String, val name: String, val icon: String,
                      val amountMl: Int, val count: Int)
data class ReviewInsights(
    val last30Days: List<DayRecord>,
    val total30Ml: Long,
    val daysRecorded30: Int,
    val averageWhenRecordedMl: Long?,
    val busiestRecordedDay: DayRecord?,
    val drinkShares7: List<DrinkShare>,
    val morningCount7: Int, val afternoonCount7: Int, val eveningCount7: Int,
    val latestRecordedTime: Long?
)

object ReviewInsightRules {
    fun summarize(records: List<DrinkRecord>, today: LocalDate, zone: ZoneId): ReviewInsights {
        val thirtyStart = today.minusDays(29)
        val weekStart = today.minusDays(6)
        val inRange = records.filter { DrinkRules.localDate(it, zone) in thirtyStart..today }
        val daysByDate = inRange.groupBy { DrinkRules.localDate(it, zone) }
        val month = (0L..29L).map { offset ->
            val date = thirtyStart.plusDays(offset)
            val drinks = daysByDate[date].orEmpty()
            DayRecord(date, drinks.sumOf { it.amountMl }, drinks.size)
        }
        val week = inRange.filter { DrinkRules.localDate(it, zone) >= weekStart }
        val activeWeekDays = week.map { DrinkRules.localDate(it, zone) }.distinct().size
        val shares = week.groupBy { it.drinkTypeId }.values.map { group ->
            val sample = group.first()
            DrinkShare(sample.drinkTypeId, sample.drinkName, sample.drinkIcon,
                group.sumOf { it.amountMl }, group.size)
        }.sortedByDescending { it.amountMl }
        val byHour = week.map { Instant.ofEpochMilli(it.timeMillis).atZone(zone).hour }
        return ReviewInsights(
            month, month.sumOf { it.loggedMl.toLong() }, month.count { it.entryCount > 0 },
            if (activeWeekDays == 0) null else week.sumOf { it.amountMl.toLong() } / activeWeekDays,
            month.takeLast(7).filter { it.entryCount > 0 }.maxByOrNull { it.loggedMl },
            shares, byHour.count { it < 12 }, byHour.count { it in 12..17 },
            byHour.count { it >= 18 },
            inRange.maxOfOrNull { it.timeMillis }
        )
    }
}
