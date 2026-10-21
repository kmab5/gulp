package com.kmab.gulp.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kmab.gulp.core.data.GulpPreferences
import com.kmab.gulp.core.design.FrostPanel
import com.kmab.gulp.core.design.GulpPalette
import com.kmab.gulp.core.design.Note
import com.kmab.gulp.core.design.PreferenceRow
import com.kmab.gulp.core.design.GulpToggleRow
import com.kmab.gulp.core.domain.TrackingReference
import com.kmab.gulp.core.graphics.WaterAdaptiveText
import com.kmab.gulp.core.graphics.WaterScene

@Composable
fun SettingsScreen(
    scene: WaterScene, viewport: IntSize, preferences: GulpPreferences,
    onVisualCapacityChange: (Int) -> Unit,
    onTrackingChange: (Int, Boolean) -> Unit,
    onCustomizeHome: () -> Unit, onWaterLab: () -> Unit,
    onReminders: () -> Unit,
    onAppearanceChange: (Boolean, Boolean, Boolean, Boolean) -> Unit,
    onExport: () -> Unit, onDeleteAll: () -> Unit
) {
    val visualCapacityMl = preferences.visualCapacityMl
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 31.dp),
        verticalArrangement = Arrangement.spacedBy(19.dp)) {
        WaterAdaptiveText("Settings", scene, viewport, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        FrostPanel(Modifier.fillMaxWidth(), padding = 20.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Home", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = GulpPalette.White)
                PreferenceRow("Customize shortcuts", "Move, resize and edit your quick-add tiles", onCustomizeHome)
                Text("Water display scale", fontSize = 14.sp, color = GulpPalette.White, fontWeight = FontWeight.Medium)
                Note("Only affects the illustration. This isn't a drinking recommendation.")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = { onVisualCapacityChange((visualCapacityMl - 250).coerceAtLeast(250)) },
                        enabled = visualCapacityMl > 250) { Text("− 250", color = GulpPalette.Glacier) }
                    Text("$visualCapacityMl mL", color = GulpPalette.Glacier, fontSize = 16.sp,
                        fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 13.dp))
                    TextButton(onClick = { onVisualCapacityChange((visualCapacityMl + 250).coerceAtMost(6000)) },
                        enabled = visualCapacityMl < 6000) { Text("+ 250", color = GulpPalette.Glacier) }
                }
            }
        }
        FrostPanel(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Tracking", color = GulpPalette.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                Note("Optional comparison for drinks you log. It doesn't measure hydration.")
                GulpToggleRow("Medical fluid plan", "Use your clinician's fluid instructions",
                    checked = preferences.clinicianManagedFluids,
                    onCheckedChange = { onTrackingChange(preferences.trackingReferenceMl, it) })
                if (preferences.clinicianManagedFluids) {
                    Note("Follow your clinician's fluid guidance. Generic goals and drinking reminders are disabled until you switch this off.")
                } else {
                    listOf(0,1500,1875,2000,2500).forEach { amount ->
                        TextButton(onClick = { onTrackingChange(amount, false) }, modifier = Modifier.fillMaxWidth()) {
                            Text((if (preferences.trackingReferenceMl == amount) "✓ " else "") +
                                (if (amount == 0) "No tracking reference" else "$amount mL recorded drinks"),
                                color = GulpPalette.Glacier)
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        TextButton(onClick = {
                            onTrackingChange((preferences.trackingReferenceMl.coerceAtLeast(500) - 250).coerceAtLeast(250), false)
                        }) { Text("− 250 mL") }
                        TextButton(onClick = {
                            onTrackingChange((preferences.trackingReferenceMl + 250).coerceIn(250, 6000), false)
                        }) { Text("+ 250 mL") }
                    }
                    Note("You can adjust the comparison in 250 mL steps. 1,500 and 1,875 mL are illustrative beverage shares of EFSA adult total-water references, not medical targets.")
                }
            }
        }
        FrostPanel(Modifier.fillMaxWidth(), padding = 17.dp) {
            Column {
                PreferenceRow("Water animation preview", "Try the water level and reduced motion", onWaterLab)
                PreferenceRow("Reminder preferences", "Times, intervals, quiet hours", onReminders)
                Note("Home-screen widgets: touch and hold an empty part of your Android home screen, choose Widgets, then find gulp.")
            }
        }
        FrostPanel(Modifier.fillMaxWidth(), padding = 17.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Appearance and accessibility", color = GulpPalette.White, fontWeight = FontWeight.Bold)
                Note("These choices are saved on your phone. System reduced-motion settings are also respected.")
                AppearanceToggle("Reduce animation", "Keeps the water still and avoids extra movement", preferences.reduceMotion) {
                    onAppearanceChange(it, preferences.tiltEnabled, preferences.touchRipplesEnabled, preferences.strongerContrast)
                }
                AppearanceToggle("Tilt response", "Water moves when you tilt your phone", preferences.tiltEnabled) {
                    onAppearanceChange(preferences.reduceMotion, it, preferences.touchRipplesEnabled, preferences.strongerContrast)
                }
                AppearanceToggle("Touch ripples", "Small waves when you touch the screen", preferences.touchRipplesEnabled) {
                    onAppearanceChange(preferences.reduceMotion, preferences.tiltEnabled, it, preferences.strongerContrast)
                }
                AppearanceToggle("Stronger text contrast", "High-contrast text above and below the water", preferences.strongerContrast) {
                    onAppearanceChange(preferences.reduceMotion, preferences.tiltEnabled, preferences.touchRipplesEnabled, it)
                }
            }
        }
        FrostPanel(Modifier.fillMaxWidth(), padding = 17.dp) {
            Column {
                Text("Data and privacy", color = GulpPalette.White, fontWeight = FontWeight.Bold)
                Note("Your drinks stay on this device unless you export them. No account or internet needed.")
                PreferenceRow("Export drinks as CSV", "Choose where to save a copy", onExport)
                PreferenceRow("Delete all drinks", "Permanently remove your saved drink history", onDeleteAll)
            }
        }
    }
}

@Composable
private fun AppearanceToggle(label: String, supporting: String, selected: Boolean, onChange: (Boolean) -> Unit) {
    GulpToggleRow(label, supporting, checked = selected, onCheckedChange = onChange,
        modifier = Modifier.padding(vertical = 6.dp))
}
