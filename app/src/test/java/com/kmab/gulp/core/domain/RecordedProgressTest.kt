package com.kmab.gulp.core.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class RecordedProgressTest {
    @Test fun referenceOptional() {
        assertNull(RecordedProgress.fraction(500, null))
        assertNull(RecordedProgress.fraction(500, 0))
        assertNull(RecordedProgress.fraction(500, -3))
    }

    @Test fun recordedFractionClampsWithoutBeingAHydrationScore() {
        assertEquals(0f, RecordedProgress.fraction(-30, 2000)!!, 0.0001f)
        assertEquals(0.25f, RecordedProgress.fraction(500, 2000)!!, 0.0001f)
        assertEquals(1f, RecordedProgress.fraction(2500, 2000)!!, 0.0001f)
    }

    @Test fun formatVolumeDisallowsNegativeAmounts() {
        assertEquals("250 mL", RecordedProgress.formatVolume(250))
        assertThrows(IllegalArgumentException::class.java) { RecordedProgress.formatVolume(-1) }
    }
}
