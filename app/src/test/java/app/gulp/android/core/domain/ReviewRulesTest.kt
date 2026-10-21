package app.gulp.android.core.domain

import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.*
import org.junit.Test

class ReviewRulesTest {
    private val zone = ZoneId.of("Europe/Istanbul")
    private val today = LocalDate.of(2026,10,20)
    private fun drink(d: LocalDate, ml: Int, kind: DrinkKind = DrinkKind.WATER) =
        DrinkRecord(amountMl=ml, kind=kind, timeMillis=d.atTime(12,0).atZone(zone).toInstant().toEpochMilli())

    @Test fun summaryCountsEntriesAndSevenCalendarDays() {
        val r = ReviewRules.summarize(listOf(drink(today,500),drink(today,200,DrinkKind.COFFEE),
            drink(today.minusDays(4),300), drink(today.minusDays(7),1000)),today,zone)
        assertEquals(1000L,r.totalMl)
        assertEquals(1000L/7,r.averagePerDayMl)
        assertEquals(2,r.recordedDays)
        assertEquals(3,r.entries)
        assertEquals(DrinkKind.WATER,r.mostLoggedDrink)
        assertEquals(1000L,r.previousWeekTotalMl)
        assertEquals(1,r.previousWeekRecordedDays)
    }

    @Test fun emptyWeekIsNotZeroIntakeClaim() {
        val r=ReviewRules.summarize(emptyList(),today,zone)
        assertEquals(0,r.recordedDays)
        assertEquals(null,r.mostLoggedDrink)
        assertEquals(null,r.previousWeekTotalMl)
    }
}
