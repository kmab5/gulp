package app.gulp.android.core.domain

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class DrinkRulesTest {
    private val zone = ZoneId.of("Europe/Istanbul")
    @Test fun dayTotalsUseLocalCalendarAndIncludeAllBeverages() {
        val start = LocalDate.of(2026, 10, 20).atStartOfDay(zone).toInstant().toEpochMilli()
        val items = listOf(
            DrinkRecord(1, 250, DrinkKind.WATER, start + 60_000),
            DrinkRecord(2, 200, DrinkKind.COFFEE, start + 5 * 60_000),
            DrinkRecord(3, 500, DrinkKind.TEA, start - 60_000)
        )
        assertEquals(450, DrinkRules.totalForDay(items, start, zone))
        assertEquals(500, DrinkRules.loggedForDay(items, LocalDate.of(2026, 10, 19), zone))
    }
    @Test fun visualScaleIsDisplayOnlyAndClamped() {
        assertEquals(0.0f, DrinkRules.visualFill(0, 2000), 0.0001f)
        assertEquals(0.5f, DrinkRules.visualFill(1000, 2000), 0.0001f)
        assertEquals(1.0f, DrinkRules.visualFill(10000, 2000), 0.0001f)
    }
    @Test fun validationRejectsInvalidEntries() {
        for (ml in listOf(-1, 0, 5001)) {
            try { DrinkRules.checkAmount(ml); fail("Expected rejection of $ml") }
            catch (_: IllegalArgumentException) { }
        }
        assertEquals(250, DrinkRules.checkAmount(250))
    }
    @Test fun tilesRoundTripPreservesOrderWidthsAndTypes() {
        val tiles = listOf(HomeTile("one", DrinkKind.WATER, 120, true), HomeTile("two", DrinkKind.COFFEE, 400))
        assertEquals(tiles, HomeTileRules.decode(HomeTileRules.encode(tiles)))
        assertEquals(listOf("two", "one"), HomeTileRules.reorder(tiles, "two", "one").map { it.id })
        assertEquals(tiles, HomeTileRules.reorder(tiles, "missing", "one"))
    }
    @Test fun invalidTileDataFallsBackAndNeverDuplicates() {
        assertEquals(HomeTileRules.defaults, HomeTileRules.decode(null))
        assertEquals(HomeTileRules.defaults, HomeTileRules.decode("garbage"))
        assertEquals(1, HomeTileRules.decode("duplicate,WATER,250,1;duplicate,WATER,250,1").size)
        assertEquals(1, HomeTileRules.decode("a,WATER,250,1;b,WRONG,10,0").size)
    }
}
