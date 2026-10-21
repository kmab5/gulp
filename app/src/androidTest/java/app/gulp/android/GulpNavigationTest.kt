package app.gulp.android

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class GulpNavigationTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Test fun allFiveDestinationsAreReachable() {
        listOf("Home", "Reminders", "History", "Review", "Settings").forEach { label ->
            rule.onNodeWithContentDescription(label).performClick()
            rule.onNodeWithText(label, useUnmergedTree = true).assertExists()
        }
    }

    @Test fun addDrinkExplainsPreviewModeWithoutCreatingFakeData() {
        rule.onNodeWithContentDescription("Home").performClick()
        rule.onNodeWithText("Add a drink").performClick()
        rule.onNodeWithText("Preview build").assertIsDisplayed()
    }
}
