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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import app.gulp.android.core.data.GulpPreferences
import app.gulp.android.core.data.GulpPreferencesStore
import app.gulp.android.core.data.GulpRepository
import app.gulp.android.core.design.GulpPalette
import app.gulp.android.core.domain.DrinkKind
import app.gulp.android.core.domain.DrinkRecord
import app.gulp.android.core.domain.DrinkRules
import app.gulp.android.core.graphics.WaterBackdrop
import app.gulp.android.core.graphics.WaterTiltSensor
import app.gulp.android.core.graphics.rememberWaterScene
import app.gulp.android.core.graphics.waterTouchFeedback
import app.gulp.android.core.navigation.GulpNav
import app.gulp.android.core.navigation.GulpTab
import app.gulp.android.feature.history.HistoryScreen
import app.gulp.android.feature.home.DrinkEditorSheet
import app.gulp.android.feature.home.HomeScreen
import app.gulp.android.feature.reminders.RemindersScreen
import app.gulp.android.feature.review.ReviewScreen
import app.gulp.android.feature.settings.SettingsScreen
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

@Composable
fun GulpApp(active: Boolean = true) {
    val context = LocalContext.current.applicationContext
    val repository = remember(context) { GulpRepository.create(context) }
    val preferencesStore = remember(context) { GulpPreferencesStore(context) }
    val records by repository.records.collectAsState(initial = emptyList())
    val preferences by preferencesStore.settings.collectAsState(initial = GulpPreferences())
    val scope = rememberCoroutineScope()
    val snackbars = remember { SnackbarHostState() }
    val scene = rememberWaterScene(active)
    var viewport by remember { mutableStateOf(IntSize.Zero) }
    var tabRoute by rememberSaveable { mutableStateOf(GulpTab.HOME.route) }
    val tab = GulpTab.fromRoute(tabRoute)
    var notice by rememberSaveable { mutableStateOf<String?>(null) }
    var showWaterLab by rememberSaveable { mutableStateOf(false) }
    var userReduceMotion by rememberSaveable { mutableStateOf(false) }
    var systemReduced by remember { mutableStateOf(false) }
    var showDrinkSheet by remember { mutableStateOf(false) }
    var recordToEdit by remember { mutableStateOf<DrinkRecord?>(null) }
    var initialKind by remember { mutableStateOf(DrinkKind.WATER) }
    var initialAmount by remember { mutableStateOf(250) }
    var clock by remember { mutableStateOf(System.currentTimeMillis()) }
    val zone = remember { ZoneId.systemDefault() }
    val today = Instant.ofEpochMilli(clock).atZone(zone).toLocalDate()
    val todayMl = DrinkRules.loggedForDay(records, today, zone)

    LaunchedEffect(Unit) { while (true) { delay(30_000); clock = System.currentTimeMillis() } }
    LaunchedEffect(todayMl, preferences.visualCapacityMl) {
        scene.setPreviewFill(DrinkRules.visualFill(todayMl, preferences.visualCapacityMl))
    }
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
            AnimatedContent(targetState = tab, modifier = Modifier.fillMaxSize(),
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(260)) },
                label = "tab crossfade") { destination ->
                when (destination) {
                    GulpTab.HOME -> HomeScreen(scene = scene, viewport = viewport, todayMl = todayMl,
                        tiles = preferences.tiles,
                        onAddDrink = {
                            recordToEdit = null; initialKind = DrinkKind.WATER; initialAmount = 250
                            showDrinkSheet = true
                        },
                        onLogShortcut = { tile ->
                            scope.launch {
                                val ticket = repository.add(tile.kind, tile.amountMl)
                                scene.disturb(.5f, .56f, 1.25f)
                                snackbars.currentSnackbarData?.dismiss()
                                val result = snackbars.showSnackbar(ticket.message, "Undo", withDismissAction = true,
                                    duration = SnackbarDuration.Short)
                                if (result == SnackbarResult.ActionPerformed) repository.undo(ticket)
                            }
                        },
                        onSaveTiles = { layout -> scope.launch { preferencesStore.saveTiles(layout) } })
                    GulpTab.REMINDERS -> RemindersScreen(scene, viewport,
                        onPreviewAction = { notice = "Reminder scheduling is coming in Phase 4." })
                    GulpTab.HISTORY -> HistoryScreen(scene, viewport, records, today,
                        onEdit = { entry -> recordToEdit = entry; showDrinkSheet = true })
                    GulpTab.REVIEW -> ReviewScreen(scene, viewport)
                    GulpTab.SETTINGS -> SettingsScreen(scene, viewport,
                        visualCapacityMl = preferences.visualCapacityMl,
                        onVisualCapacityChange = { amount ->
                            scope.launch { preferencesStore.changeVisualCapacity(amount) }
                        },
                        onCustomizeHome = { tabRoute = GulpTab.HOME.route },
                        onWaterLab = { showWaterLab = true },
                        onPreviewAction = { notice = "This setting is scheduled for a later phase." })
                }
            }
        }
        GulpNav(current = tab, scene = scene, viewport = viewport,
            onSelect = { next ->
                if (next != tab) { scene.disturb(.5f, .88f, .26f); tabRoute = next.route }
            }, modifier = Modifier.align(Alignment.BottomCenter))
        SnackbarHost(snackbars, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 99.dp))
    }

    if (showDrinkSheet) {
        val original = recordToEdit
        DrinkEditorSheet(existing = original, initialKind = initialKind, initialAmount = initialAmount,
            onClose = { showDrinkSheet = false; recordToEdit = null },
            onSave = { kind, ml, timestamp ->
                showDrinkSheet = false; recordToEdit = null
                scope.launch {
                    val ticket = if (original == null) repository.add(kind, ml, timestamp)
                        else repository.edit(original.id, kind, ml, timestamp)
                    if (ticket != null) {
                        scene.disturb(.5f, .56f, 1.1f)
                        snackbars.currentSnackbarData?.dismiss()
                        val result = snackbars.showSnackbar(ticket.message, "Undo", withDismissAction = true,
                            duration = SnackbarDuration.Short)
                        if (result == SnackbarResult.ActionPerformed) repository.undo(ticket)
                    }
                }
            },
            onDelete = original?.let { entry -> {
                showDrinkSheet = false; recordToEdit = null
                scope.launch {
                    val ticket = repository.delete(entry.id)
                    if (ticket != null) {
                        scene.disturb(.5f, .56f, .6f)
                        snackbars.currentSnackbarData?.dismiss()
                        val result = snackbars.showSnackbar(ticket.message, "Undo", withDismissAction = true,
                            duration = SnackbarDuration.Short)
                        if (result == SnackbarResult.ActionPerformed) repository.undo(ticket)
                    }
                }
            } })
    }
    if (notice != null) AlertDialog(onDismissRequest = { notice = null },
        title = { Text("Not available yet") }, text = { Text(notice.orEmpty()) },
        confirmButton = { TextButton(onClick = { notice = null }) { Text("OK") } },
        containerColor = GulpPalette.DeepBlue, titleContentColor = Color.White,
        textContentColor = GulpPalette.SoftWhite)
    if (showWaterLab) AlertDialog(onDismissRequest = {
        showWaterLab = false
        scene.setPreviewFill(DrinkRules.visualFill(todayMl, preferences.visualCapacityMl))
    }, title = { Text("Water preview") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Test the visual level without changing your recorded drinks.")
            Slider(value = scene.targetFill, onValueChange = {
                scene.setPreviewFill(it); scene.disturb(.5f, .5f, .12f)
            }, valueRange = 0f..1f)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Reduce motion", modifier = Modifier.weight(1f))
                Switch(checked = userReduceMotion || systemReduced, enabled = !systemReduced,
                    onCheckedChange = { userReduceMotion = it; scene.setReducedMotion(it) })
            }
        }
    }, confirmButton = { TextButton(onClick = {
        showWaterLab = false
        scene.setPreviewFill(DrinkRules.visualFill(todayMl, preferences.visualCapacityMl))
    }) { Text("Done") } }, containerColor = GulpPalette.DeepBlue,
        titleContentColor = Color.White, textContentColor = GulpPalette.SoftWhite)
}
