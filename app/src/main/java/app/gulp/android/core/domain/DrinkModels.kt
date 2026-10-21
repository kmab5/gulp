package app.gulp.android.core.domain

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** A drink type describes recorded beverages, never a medical hydration multiplier. */
enum class DrinkKind(val title: String) {
    WATER("Water"), COFFEE("Coffee"), TEA("Tea"), OTHER("Other");

    companion object {
        fun fromStored(value: String): DrinkKind = entries.firstOrNull { it.name == value } ?: OTHER
    }
}

data class DrinkRecord(
    val id: Long = 0L,
    val amountMl: Int,
    val kind: DrinkKind,
    val timeMillis: Long
)

object DrinkRules {
    const val MIN_ENTRY_ML = 1
    const val MAX_ENTRY_ML = 5000 // Input validation, not a recommendation to drink this amount.

    fun checkAmount(ml: Int): Int {
        require(ml in MIN_ENTRY_ML..MAX_ENTRY_ML) { "Enter an amount from 1 to 5000 mL." }
        return ml
    }

    fun localDate(record: DrinkRecord, zone: ZoneId): LocalDate =
        Instant.ofEpochMilli(record.timeMillis).atZone(zone).toLocalDate()

    fun loggedForDay(records: List<DrinkRecord>, day: LocalDate, zone: ZoneId): Int =
        records.asSequence().filter { localDate(it, zone) == day }.sumOf { it.amountMl }

    fun totalForDay(records: List<DrinkRecord>, timestamp: Long, zone: ZoneId): Int =
        loggedForDay(records, Instant.ofEpochMilli(timestamp).atZone(zone).toLocalDate(), zone)

    /** 0..1 describes the water illustration only, not physiological hydration. */
    fun visualFill(loggedMl: Int, displayCapacityMl: Int): Float =
        (loggedMl.toDouble() / displayCapacityMl.coerceAtLeast(1)).coerceIn(0.0, 1.0).toFloat()
}

/** One quick action is a stable, editable tile on Home. */
data class HomeTile(
    val id: String,
    val kind: DrinkKind,
    val amountMl: Int,
    val wide: Boolean = false
)

object HomeTileRules {
    const val MAX_TILES = 10
    const val MIN_TILES = 1
    val defaults: List<HomeTile> = listOf(
        HomeTile("water-250", DrinkKind.WATER, 250, true),
        HomeTile("water-500", DrinkKind.WATER, 500),
        HomeTile("coffee-200", DrinkKind.COFFEE, 200)
    )

    fun reorder(tiles: List<HomeTile>, fromId: String, toId: String): List<HomeTile> {
        val from = tiles.indexOfFirst { it.id == fromId }
        val to = tiles.indexOfFirst { it.id == toId }
        if (from < 0 || to < 0 || from == to) return tiles
        return tiles.toMutableList().apply { add(to, removeAt(from)) }
    }

    /** Only internal identifiers are serialized; the user cannot inject delimiters. */
    fun encode(tiles: List<HomeTile>): String = tiles.joinToString(";") {
        "${it.id},${it.kind.name},${it.amountMl},${if (it.wide) 1 else 0}"
    }

    fun decode(value: String?): List<HomeTile> {
        if (value == null) return defaults
        if (value.isBlank()) return defaults
        val result = value.split(';').mapNotNull { token ->
            val parts = token.split(',')
            if (parts.size != 4) return@mapNotNull null
            val id = parts[0].takeIf { it.length in 1..40 && it.all { c -> c.isLetterOrDigit() || c == '-' } }
                ?: return@mapNotNull null
            val kind = DrinkKind.entries.firstOrNull { it.name == parts[1] } ?: return@mapNotNull null
            val ml = parts[2].toIntOrNull()?.takeIf { it in DrinkRules.MIN_ENTRY_ML..DrinkRules.MAX_ENTRY_ML }
                ?: return@mapNotNull null
            HomeTile(id, kind, ml, parts[3] == "1")
        }.distinctBy { it.id }.take(MAX_TILES)
        return result.ifEmpty { defaults }
    }
}
