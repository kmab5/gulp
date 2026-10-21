package app.gulp.android.core.domain

/** References are approximate recorded beverage comparisons, not medical water prescriptions. */
object TrackingReference {
    const val NONE = 0
    const val DEFAULT_CUSTOM_ML = 1800
    val selectable = listOf(0, 1500, 1875, 2000, 2500)

    fun sanitized(value: Int): Int = if (value == 0) 0 else value.coerceIn(250, 6000)

    fun description(value: Int): String = when (value) {
        0 -> "No tracking goal"
        1500 -> "About 1,500 mL in drinks — illustrative EFSA adult female beverage share"
        1875 -> "About 1,875 mL in drinks — illustrative EFSA adult male beverage share"
        else -> "${value} mL recorded drinks — your chosen reference"
    }
}
