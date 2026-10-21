package app.gulp.android.core.domain

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DrinkCsvTest {
    @Test fun emptyExportIsHeaderOnly() {
        assertEquals("${DrinkCsv.HEADER}\n", DrinkCsv.encode(emptyList()))
    }

    @Test fun stableUtcOrderAndTypes() {
        val first = DrinkRecord(1, 250, DrinkKind.WATER, Instant.parse("2026-10-20T07:00:00Z").toEpochMilli())
        val second = DrinkRecord(2, 200, DrinkKind.COFFEE, Instant.parse("2026-10-20T10:30:00Z").toEpochMilli())
        val encoded = DrinkCsv.encode(listOf(second, first))
        assertEquals("id,timestamp_utc,drink_type,amount_ml\n" +
            "1,2026-10-20T07:00:00Z,WATER,250\n" +
            "2,2026-10-20T10:30:00Z,COFFEE,200\n", encoded)
        assertTrue(encoded.endsWith('\n'))
    }
}
