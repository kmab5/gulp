package app.gulp.android.core.domain

import app.gulp.android.core.navigation.GulpTab
import org.junit.Assert.assertEquals
import org.junit.Test

class GulpTabTest {
    @Test fun allFiveTabsHaveOrdinaryNamesInTheirApprovedOrder() {
        assertEquals(listOf("Home", "Reminders", "History", "Review", "Settings"), GulpTab.entries.map { it.label })
    }

    @Test fun unknownRouteReturnsHome() {
        assertEquals(GulpTab.HOME, GulpTab.fromRoute(null))
        assertEquals(GulpTab.HOME, GulpTab.fromRoute("unknown"))
        assertEquals(GulpTab.REVIEW, GulpTab.fromRoute("review"))
    }
}
