package app.gulp.android

import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import app.gulp.android.core.design.GulpPalette
import app.gulp.android.core.graphics.WaterBackdrop
import app.gulp.android.core.graphics.WaterTiltSensor
import app.gulp.android.core.graphics.rememberWaterScene
import app.gulp.android.core.graphics.waterTouchFeedback
import app.gulp.android.core.navigation.GulpNav
import app.gulp.android.core.navigation.GulpTab
import app.gulp.android.feature.home.HomeScreen
import app.gulp.android.feature.reminders.RemindersScreen
import app.gulp.android.feature.history.HistoryScreen
import app.gulp.android.feature.review.ReviewScreen
import app.gulp.android.feature.settings.SettingsScreen

class MainActivity : ComponentActivity() {
    private var foreground by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { GulpApp(foreground) }
    }

    override fun onResume() {
        super.onResume()
        foreground = true
    }

    override fun onPause() {
        foreground = false
        super.onPause()
    }
}

/** Preview scene survives tab switches; nothing in this milestone writes a drink record. */
@Composable
fun GulpApp(active: Boolean = true) {
    val context = LocalContext.current
    val scene = rememberWaterScene(active)
    var viewport by remember { mutableStateOf(IntSize.Zero) }
    var tabRoute by rememberSaveable { mutableStateOf(GulpTab.HOME.route) }
    val tab = GulpTab.fromRoute(tabRoute)
    var notice by rememberSaveable { mutableStateOf<String?>(null) }
    var showWaterLab by rememberSaveable { mutableStateOf(false) }
    var userReduceMotion by rememberSaveable { mutableStateOf(false) }
    var systemReduced by remember { mutableStateOf(false) }
    LaunchedEffect(context, active, userReduceMotion) {
        if (active) {
            systemReduced = runCatching {
                Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
            }.getOrDefault(false)
            scene.setReducedMotion(systemReduced || userReduceMotion)
        }
    }
    WaterTiltSensor(scene, active)

    Box(Modifier.fillMaxSize().onSizeChanged { viewport = it }.waterTouchFeedback(scene)) {
        WaterBackdrop(scene)
        Box(Modifier.fillMaxSize().statusBarsPadding().padding(bottom = 92.dp)) {
            AnimatedContent(
                targetState = tab,
                modifier = Modifier.fillMaxSize(),
                transitionSpec = {
                    fadeIn(tween(300)) togetherWith fadeOut(tween(260))
                },
                label = "tab crossfade"
            ) { destination ->
                when (destination) {
                    GulpTab.HOME -> HomeScreen(
                        scene, viewport,
                        onPreviewAction = {
                            scene.setPreviewFill(scene.targetFill + .085f)
                            scene.disturb(.5f, .55f, 1.2f)
                            notice = "This is a water-animation demo. No drink has been saved. Real logging arrives in Phase 3."
                        },
                        onWaterLab = { showWaterLab = true }
                    )
                    GulpTab.REMINDERS -> RemindersScreen(scene, viewport, onPreviewAction = { notice = "Reminder scheduling is coming in the core-features phase." })
                    GulpTab.HISTORY -> HistoryScreen(scene, viewport)
                    GulpTab.REVIEW -> ReviewScreen(scene, viewport)
                    GulpTab.SETTINGS -> SettingsScreen(scene, viewport, onPreviewAction = { notice = "These preferences are added after the water engine and data layer." })
                }
            }
        }
        GulpNav(
            current = tab, scene = scene, viewport = viewport,
            onSelect = { next ->
                if (next != tab) {
                    scene.disturb(.5f, .88f, .26f)
                    tabRoute = next.route
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
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
    if (showWaterLab) {
        AlertDialog(
            onDismissRequest = { showWaterLab = false },
            title = { Text("Water preview") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Drag to test a water level. This doesn't log a drink.")
                    Slider(value = scene.targetFill, onValueChange = {
                        scene.setPreviewFill(it)
                        scene.disturb(.5f, .5f, .12f)
                    }, valueRange = 0f..1f)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Reduce motion", modifier = Modifier.weight(1f))
                        Switch(checked = userReduceMotion || systemReduced, enabled = !systemReduced,
                            onCheckedChange = { userReduceMotion = it; scene.setReducedMotion(it) })
                    }
                    Text("Tilting and touch effects are available on a phone with motion sensors.")
                }
            },
            confirmButton = { TextButton(onClick = { showWaterLab = false }) { Text("Done") } },
            containerColor = GulpPalette.DeepBlue,
            titleContentColor = Color.White,
            textContentColor = GulpPalette.SoftWhite
        )
    }
}
