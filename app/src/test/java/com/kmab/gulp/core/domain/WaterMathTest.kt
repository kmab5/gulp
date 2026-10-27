package com.kmab.gulp.core.domain

import com.kmab.gulp.core.graphics.WaterImpulse
import com.kmab.gulp.core.graphics.WaterMath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WaterMathTest {
    @Test
    fun waterCanFallBelowNavbar() {
        assertEquals(0f, WaterMath.visibleFill(0f), .00001f)
        assertEquals(.96f, WaterMath.visibleFill(5f), .00001f)
        assertEquals(
            .5f,
            WaterMath.surfaceY(.5f, .5f, 0f, 0f, emptyList(), false),
            .00001f
        )
        assertTrue(
            WaterMath.surfaceY(.5f, 0f, 0f, 0f, emptyList(), false) > 1f
        )
    }

    @Test fun tiltPreservesCenterOfVolumeAndClamps() {
        val y0 = WaterMath.surfaceY(.5f, .5f, 1f, 0f, emptyList(), false)
        val left = WaterMath.surfaceY(0f, .5f, 1f, 0f, emptyList(), true)
        val right = WaterMath.surfaceY(1f, .5f, 1f, 0f, emptyList(), true)
        assertEquals(.5f, y0, .00001f)
        assertTrue(right > left)
        assertEquals(1f, WaterMath.normalizedTilt(100f), .00001f)
    }

    @Test fun impulsesDissipate() {
        val w = listOf(WaterImpulse(.5f, .3f, 1f, 0f))
        val with = WaterMath.surfaceY(.37f, .5f, 0f, .45f, w, true)
        val without = WaterMath.surfaceY(.37f, .5f, 0f, .45f, emptyList(), true)
        assertTrue(kotlin.math.abs(with - without) > .000001f)
        val old = WaterMath.surfaceY(.37f, .5f, 0f, 3f, w, true)
        val calm = WaterMath.surfaceY(.37f, .5f, 0f, 3f, emptyList(), true)
        assertEquals(calm, old, .000001f)
    }
}
