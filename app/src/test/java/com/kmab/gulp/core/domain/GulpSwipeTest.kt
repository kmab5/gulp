package com.kmab.gulp.core.domain

import com.kmab.gulp.core.navigation.GulpSwipe
import org.junit.Assert.assertEquals
import org.junit.Test

class GulpSwipeTest {
    @Test fun horizontalSwipesAndEdges() {
        assertEquals(2, GulpSwipe.nextIndex(1, -120f, 7f, 84f, 5))
        assertEquals(0, GulpSwipe.nextIndex(1, 120f, 8f, 84f, 5))
        assertEquals(1, GulpSwipe.nextIndex(1, 60f, 8f, 84f, 5))
        assertEquals(1, GulpSwipe.nextIndex(1, 120f, 130f, 84f, 5))
        assertEquals(0, GulpSwipe.nextIndex(0, 150f, 0f, 84f, 5))
        assertEquals(4, GulpSwipe.nextIndex(4, -150f, 0f, 84f, 5))
    }
}
