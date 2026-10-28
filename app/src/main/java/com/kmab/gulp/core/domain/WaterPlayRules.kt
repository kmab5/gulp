package com.kmab.gulp.core.domain

/** Preview-only bounds. These are not hydration recommendations or logged volume. */
object WaterPlayRules {
    fun playFill(value: Float): Float = value.coerceIn(.20f, .94f)
    fun sensitivity(value: Float): Float = value.coerceIn(.5f, 1.5f)
}
