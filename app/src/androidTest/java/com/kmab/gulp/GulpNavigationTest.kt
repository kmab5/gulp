package com.kmab.gulp

import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Before
import androidx.test.platform.app.InstrumentationRegistry
import com.kmab.gulp.core.data.GulpPreferencesStore
import com.kmab.gulp.core.domain.ReminderPlan
import kotlinx.coroutines.runBlocking
import org.junit.Test

class GulpNavigationTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Before fun prepareCompletedOnboarding() = runBlocking {
        // Activity launches before @Before; the DataStore flow updates the UI automatically.
        GulpPreferencesStore(InstrumentationRegistry.getInstrumentation().targetContext)
            .finishOnboarding(ReminderPlan(), 0, false)
    }

    @Test fun allFiveDestinationsAreReachable() {
        listOf("Home", "Reminders", "History", "Review", "Settings").forEach { label ->
            rule.onNodeWithContentDescription(label).performClick()
            rule.onNodeWithText(label, useUnmergedTree = true).assertExists()
        }
    }

    @Test fun addDrinkOpensRealEntrySheet() {
        rule.onNodeWithContentDescription("Home").performClick()
        rule.onNodeWithText("Add a drink").performClick()
        rule.onNodeWithText("Add drink").assertExists()
        rule.onNodeWithText("Amount (mL)").assertExists()
    }
}
