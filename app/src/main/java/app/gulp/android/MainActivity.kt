package app.gulp.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.gulp.android.core.design.GulpPalette
import app.gulp.android.core.graphics.StaticWaterBackdrop
import app.gulp.android.core.navigation.GulpNav
import app.gulp.android.core.navigation.GulpTab
import app.gulp.android.feature.home.HomeScreen
import app.gulp.android.feature.reminders.RemindersScreen
import app.gulp.android.feature.history.HistoryScreen
import app.gulp.android.feature.review.ReviewScreen
import app.gulp.android.feature.settings.SettingsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { GulpApp() }
    }
}

/** The background outlives navigation transitions; Phase 2 will replace its static renderer. */
@Composable
fun GulpApp() {
    var tabRoute by rememberSaveable { mutableStateOf(GulpTab.HOME.route) }
    val tab = GulpTab.fromRoute(tabRoute)
    var notice by rememberSaveable { mutableStateOf<String?>(null) }

    Box(Modifier.fillMaxSize()) {
        StaticWaterBackdrop()
        Box(Modifier.fillMaxSize().statusBarsPadding().padding(bottom = 92.dp)) {
            AnimatedContent(
                targetState = tab,
                modifier = Modifier.fillMaxSize(),
                transitionSpec = { fadeIn(tween(260)) togetherWith fadeOut(tween(180)) },
                label = "screen transition"
            ) { destination ->
                when (destination) {
                    GulpTab.HOME -> HomeScreen(onPreviewAction = { notice = "Drink logging will be added in the Home milestone. This build does not save drinks." })
                    GulpTab.REMINDERS -> RemindersScreen(onPreviewAction = { notice = "Reminder scheduling will be added after the water engine and drink records." })
                    GulpTab.HISTORY -> HistoryScreen()
                    GulpTab.REVIEW -> ReviewScreen()
                    GulpTab.SETTINGS -> SettingsScreen(onPreviewAction = { notice = "Personal settings become editable in later milestones. This build is a layout preview." })
                }
            }
        }
        GulpNav(current = tab, onSelect = { tabRoute = it.route }, modifier = Modifier.align(Alignment.BottomCenter))
    }
    if (notice != null) {
        AlertDialog(
            onDismissRequest = { notice = null },
            title = { Text("Preview build") },
            text = { Text(notice.orEmpty()) },
            confirmButton = { TextButton(onClick = { notice = null }) { Text("OK") } },
            containerColor = GulpPalette.DeepBlue,
            titleContentColor = Color.White,
            textContentColor = GulpPalette.SoftWhite
        )
    }
}
