package app.gulp.android.core.domain

import java.time.Instant

/** Export only user-entered records; UTC ISO timestamps avoid device locale ambiguity. */
object DrinkCsv {
    const val HEADER = "id,timestamp_utc,drink_type,amount_ml"

    fun encode(records: List<DrinkRecord>): String = buildString {
        append(HEADER).append('\n')
        records.sortedWith(compareBy<DrinkRecord> { it.timeMillis }.thenBy { it.id }).forEach { record ->
            append(record.id).append(',')
            append(Instant.ofEpochMilli(record.timeMillis)).append(',')
            append(record.kind.name).append(',')
            append(record.amountMl).append('\n')
        }
    }
}
