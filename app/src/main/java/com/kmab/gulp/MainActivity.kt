package com.kmab.gulp

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import androidx.compose.runtime.CompositionLocalProvider
import com.kmab.gulp.core.graphics.LocalStrongerContrast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.kmab.gulp.core.data.GulpPreferences
import com.kmab.gulp.core.data.GulpPreferencesStore
import com.kmab.gulp.core.data.GulpRepository
import com.kmab.gulp.core.design.GulpPalette
import com.kmab.gulp.core.design.GulpToggleRow
import com.kmab.gulp.core.domain.DrinkKind
import com.kmab.gulp.core.domain.DrinkRecord
import com.kmab.gulp.core.domain.DrinkRules
import com.kmab.gulp.core.domain.ReminderPlan
import com.kmab.gulp.core.domain.ReminderRules
import com.kmab.gulp.core.graphics.WaterBackdrop
import com.kmab.gulp.core.graphics.WaterTiltSensor
import com.kmab.gulp.core.graphics.rememberWaterScene
import com.kmab.gulp.core.graphics.waterTouchFeedback
import com.kmab.gulp.core.reminders.GulpReminderScheduler
import com.kmab.gulp.core.navigation.GulpNav
import com.kmab.gulp.core.navigation.GulpTab
import com.kmab.gulp.feature.history.HistoryScreen
import com.kmab.gulp.feature.onboarding.OnboardingScreen
import com.kmab.gulp.feature.home.DrinkEditorSheet
import com.kmab.gulp.feature.home.HomeScreen
import com.kmab.gulp.feature.reminders.RemindersScreen
import com.kmab.gulp.feature.review.ReviewScreen
import com.kmab.gulp.feature.settings.SettingsScreen
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var foreground by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(
                primary = GulpPalette.Glacier,
                onPrimary = GulpPalette.Ink,
                background = GulpPalette.Abyss,
                onBackground = GulpPalette.White,
                surface = GulpPalette.DeepBlue,
                onSurface = GulpPalette.White,
                surfaceVariant = GulpPalette.Surface,
                onSurfaceVariant = GulpPalette.SoftWhite,
                outline = GulpPalette.Edge
            )) { GulpApp(foreground) }
        }
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
    val loadedPreferences by produceState<GulpPreferences?>(initialValue = null, preferencesStore) {
        preferencesStore.settings.collect { value = it }
    }
    if (loadedPreferences == null) {
        Box(Modifier.fillMaxSize().background(GulpPalette.DeepBlue))
        return
    }
    val preferences = loadedPreferences ?: return
    val scope = rememberCoroutineScope()
    val snackbars = remember { SnackbarHostState() }
    val scene = rememberWaterScene(active)
    var viewport by remember { mutableStateOf(IntSize.Zero) }
    var tabRoute by rememberSaveable { mutableStateOf(GulpTab.HOME.route) }
    var showHomeBrandSubmark by rememberSaveable { mutableStateOf(false) }
    var homeEditRequest by remember { mutableIntStateOf(0) }
    val tab = GulpTab.fromRoute(tabRoute)
    var showWaterLab by rememberSaveable { mutableStateOf(false) }
    var systemReduced by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showDrinkSheet by remember { mutableStateOf(false) }
    var recordToEdit by remember { mutableStateOf<DrinkRecord?>(null) }
    var initialKind by remember { mutableStateOf(DrinkKind.WATER) }
    var initialAmount by remember { mutableStateOf(250) }
    var clock by remember { mutableStateOf(System.currentTimeMillis()) }
    val zone = ZoneId.systemDefault()
    val today = Instant.ofEpochMilli(clock).atZone(zone).toLocalDate()
    val todayMl = DrinkRules.loggedForDay(records, today, zone)
    val nextReminder = if (GulpReminderScheduler.notificationsAllowed(context)) {
        ReminderRules.nextAlarm(clock, zone, preferences.reminders)
    } else null
    val nextReminderLabel = when {
        !preferences.reminders.enabled -> "Off"
        !GulpReminderScheduler.notificationsAllowed(context) -> "Notifications off"
        nextReminder == null -> "Not scheduled"
        else -> java.time.Instant.ofEpochMilli(nextReminder).atZone(zone)
            .format(java.time.format.DateTimeFormatter.ofPattern("EEE, HH:mm"))
    }
    var notificationPermission by remember { mutableStateOf(GulpReminderScheduler.notificationsAllowed(context)) }
    val notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        notificationPermission = granted
    }
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        if (uri != null) scope.launch {
            val result = runCatching {
                val csv = withContext(Dispatchers.IO) { repository.exportCsv() }
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)?.use { writer ->
                        writer.write(csv)
                    } ?: throw IOException("Couldn't open the selected file")
                }
            }
            snackbars.showSnackbar(if (result.isSuccess) "Drinks exported" else "Couldn't export: ${result.exceptionOrNull()?.message ?: "unknown error"}")
        }
    }
    val requestPermission = {
        if (Build.VERSION.SDK_INT >= 33) {
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else notificationPermission = true
    }

    LaunchedEffect(preferences.reminders, notificationPermission) {
        GulpReminderScheduler.createChannel(context)
        if (notificationPermission) GulpReminderScheduler.schedule(context, preferences.reminders)
        else GulpReminderScheduler.cancel(context)
    }
    LaunchedEffect(active) { notificationPermission = GulpReminderScheduler.notificationsAllowed(context) }
    if (!preferences.hasCompletedOnboarding) {
        Box(Modifier.fillMaxSize().onSizeChanged { viewport = it }) {
            WaterBackdrop(scene)
            OnboardingScreen(scene, viewport, onComplete = { plan, reference, medical ->
                scope.launch { preferencesStore.finishOnboarding(plan, reference, medical) }
                if (plan.enabled && !medical && !notificationPermission) requestPermission()
            })
        }
        return
    }

    // Pause the wall-clock updater in the background; refresh immediately on resume.
    LaunchedEffect(active) {
        if (active) {
            clock = System.currentTimeMillis()
            while (true) { delay(30_000); clock = System.currentTimeMillis() }
        }
    }
    LaunchedEffect(todayMl, preferences.visualCapacityMl) {
        scene.setPreviewFill(DrinkRules.visualFill(todayMl, preferences.visualCapacityMl))
    }
    LaunchedEffect(context, active, preferences.reduceMotion) {
        if (active) {
            systemReduced = runCatching {
                Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
            }.getOrDefault(false)
            scene.updateReducedMotion(systemReduced || preferences.reduceMotion)
        }
    }
    WaterTiltSensor(scene, active, preferences.tiltEnabled)

    CompositionLocalProvider(LocalStrongerContrast provides preferences.strongerContrast) {
    Box(Modifier.fillMaxSize().onSizeChanged { viewport = it }.waterTouchFeedback(scene, preferences.touchRipplesEnabled)) {
        WaterBackdrop(scene)
        Box(Modifier.fillMaxSize().statusBarsPadding().padding(bottom = 92.dp)) {
            AnimatedContent(targetState = tab, modifier = Modifier.fillMaxSize(),
                transitionSpec = {
                    val enterMs = if (scene.reducedMotion) 0 else 300
                    val exitMs = if (scene.reducedMotion) 0 else 260
                    fadeIn(tween(enterMs)) togetherWith fadeOut(tween(exitMs))
                },
                label = "tab crossfade") { destination ->
                when (destination) {
                    GulpTab.HOME -> HomeScreen(scene = scene, viewport = viewport, todayMl = todayMl,
                        nextReminderLabel = nextReminderLabel,
                        editRequest = homeEditRequest,
                        onEditRequestHandled = { homeEditRequest = 0 },
                        tiles = preferences.tiles,
                        showBrandSubmark = showHomeBrandSubmark,
                        onToggleBrand = {
                            showHomeBrandSubmark = !showHomeBrandSubmark
                            scene.disturb(.85f, .10f, .20f)
                        },
                        onAddDrink = {
                            recordToEdit = null; initialKind = DrinkKind.WATER; initialAmount = 250
                            showDrinkSheet = true
                        },
                        onLogShortcut = { tile ->
                            scope.launch {
                                val ticket = repository.add(tile.kind, tile.amountMl)
                                preferencesStore.snoozeUntil(0L)
                                scene.disturb(.5f, .56f, 1.25f)
                                snackbars.currentSnackbarData?.dismiss()
                                val result = snackbars.showSnackbar(ticket.message, "Undo", withDismissAction = true,
                                    duration = SnackbarDuration.Short)
                                if (result == SnackbarResult.ActionPerformed) repository.undo(ticket)
                            }
                        },
                        onSaveTiles = { layout -> scope.launch { preferencesStore.saveTiles(layout) } })
                    GulpTab.REMINDERS -> RemindersScreen(scene, viewport, preferences, notificationPermission, clock,
                        onPlanChange = { plan ->
                            scope.launch { preferencesStore.setReminders(plan) }
                            if (plan.enabled && !preferences.clinicianManagedFluids && !notificationPermission) requestPermission()
                        }, requestNotificationPermission = requestPermission)
                    GulpTab.HISTORY -> HistoryScreen(scene, viewport, records, today,
                        onEdit = { entry -> recordToEdit = entry; showDrinkSheet = true })
                    GulpTab.REVIEW -> ReviewScreen(scene, viewport, records, today, preferences.trackingReferenceMl)
                    GulpTab.SETTINGS -> SettingsScreen(scene, viewport, preferences,
                        onVisualCapacityChange = { amount ->
                            scope.launch { preferencesStore.changeVisualCapacity(amount) }
                        },
                        onTrackingChange = { amount, medical ->
                            scope.launch { preferencesStore.setTracking(amount, medical) }
                        },
                        onCustomizeHome = {
                            homeEditRequest += 1
                            tabRoute = GulpTab.HOME.route
                        },
                        onReminders = { tabRoute = GulpTab.REMINDERS.route },
                        onWaterLab = { showWaterLab = true },
                        onAppearanceChange = { reduced, tilt, ripples, contrast ->
                            scope.launch { preferencesStore.setAppearance(reduced, tilt, ripples, contrast) }
                        },
                        onExport = { exportLauncher.launch("gulp-drinks.csv") },
                        onDeleteAll = { showDeleteConfirmation = true })
                }
            }
        }
        GulpNav(current = tab, scene = scene, viewport = viewport,
            onSelect = { next ->
                if (next != tab) { scene.disturb(.5f, .88f, .26f); tabRoute = next.route }
            }, modifier = Modifier.align(Alignment.BottomCenter))
        SnackbarHost(snackbars, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 99.dp))
    }

    } // end preference-driven contrast provider
    if (showDeleteConfirmation) AlertDialog(
        onDismissRequest = { showDeleteConfirmation = false },
        title = { Text("Delete all drinks?") },
        text = { Text("This permanently deletes your saved drink history. It can't be undone. Your settings and reminders stay as they are.") },
        dismissButton = { TextButton(onClick = { showDeleteConfirmation = false }) { Text("Cancel") } },
        confirmButton = { TextButton(onClick = {
            showDeleteConfirmation = false
            scope.launch {
                val deleted = repository.deleteAll()
                snackbars.showSnackbar(if (deleted == 0) "No drinks to delete" else "$deleted drink records deleted")
            }
        }) { Text("Delete all") } },
        containerColor = GulpPalette.DeepBlue, titleContentColor = Color.White,
        textContentColor = GulpPalette.SoftWhite
    )
    if (showDrinkSheet) {
        val original = recordToEdit
        DrinkEditorSheet(existing = original, initialKind = initialKind, initialAmount = initialAmount,
            onClose = { showDrinkSheet = false; recordToEdit = null },
            onSave = { kind, ml, timestamp ->
                showDrinkSheet = false; recordToEdit = null
                scope.launch {
                    val ticket = if (original == null) {
                        repository.add(kind, ml, timestamp).also { preferencesStore.snoozeUntil(0L) }
                    } else repository.edit(original.id, kind, ml, timestamp)
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
    if (showWaterLab) AlertDialog(onDismissRequest = {
        showWaterLab = false
        scene.setPreviewFill(DrinkRules.visualFill(todayMl, preferences.visualCapacityMl))
    }, title = { Text("Water preview") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Test the visual level without changing your recorded drinks.")
            Slider(value = scene.targetFill, onValueChange = {
                scene.setPreviewFill(it); scene.disturb(.5f, .5f, .12f)
            }, valueRange = 0f..1f)
            GulpToggleRow("Reduce motion", "Turn off extra water movement",
                checked = preferences.reduceMotion || systemReduced,
                enabled = !systemReduced, onCheckedChange = {
                    scene.updateReducedMotion(it)
                    scope.launch { preferencesStore.setAppearance(it, preferences.tiltEnabled,
                        preferences.touchRipplesEnabled, preferences.strongerContrast) }
                })
        }
    }, confirmButton = { TextButton(onClick = {
        showWaterLab = false
        scene.setPreviewFill(DrinkRules.visualFill(todayMl, preferences.visualCapacityMl))
    }) { Text("Done") } }, containerColor = GulpPalette.DeepBlue,
        titleContentColor = Color.White, textContentColor = GulpPalette.SoftWhite)
}
