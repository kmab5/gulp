package com.kmab.gulp.core.domain

/**
 * The progress of *logged beverages relative to an optional chosen tracking reference*.
 * NEVER present the result as a hydration percentage or a medical assessment.
 * A null result means there is no usable personal tracking reference.
 */
object RecordedProgress {
    fun fraction(loggedMl: Int, referenceMl: Int?): Float? {
        if (referenceMl == null || referenceMl <= 0) return null
        return (loggedMl.coerceAtLeast(0).toDouble() / referenceMl.toDouble())
            .coerceIn(0.0, 1.0)
            .toFloat()
    }

    fun formatVolume(ml: Int): String {
        require(ml >= 0) { "Recorded volume must be nonnegative" }
        return "${ml} mL"
    }
}
