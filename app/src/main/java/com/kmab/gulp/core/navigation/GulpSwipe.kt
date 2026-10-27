package com.kmab.gulp.core.navigation

import kotlin.math.abs

/** End-of-gesture horizontal swipe, without consuming vertical scrolling or child taps. */
object GulpSwipe {
    fun nextIndex(current: Int, dx: Float, dy: Float, thresholdPx: Float, count: Int): Int {
        if (count <= 0 || !dx.isFinite() || !dy.isFinite() ||
            abs(dx) < thresholdPx || abs(dx) <= abs(dy) * 1.4f) return current
        return (current + if (dx < 0f) 1 else -1).coerceIn(0, count - 1)
    }
}
