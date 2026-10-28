package com.kmab.gulp.core.domain

import org.junit.Assert.*
import org.junit.Test

class WaterPlayRulesTest {
    @Test fun testPlayLevelIsPreviewOnlyAndBounded() {
        assertEquals(.62f, WaterPlayRules.playFill(.62f), .0001f)
        assertEquals(.20f, WaterPlayRules.playFill(-1f), .0001f)
        assertEquals(.94f, WaterPlayRules.playFill(2f), .0001f)
    }
    @Test fun testStrengthRemainsInSupportedMotionBounds() {
        assertEquals(1.5f, WaterPlayRules.sensitivity(9f), .0001f)
        assertEquals(.5f, WaterPlayRules.sensitivity(-1f), .0001f)
    }
}
