package app.gulp.android.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.gulp.android.core.data.GulpPreferences
import app.gulp.android.core.design.FrostPanel
import app.gulp.android.core.design.GulpPalette
import app.gulp.android.core.design.Note
import app.gulp.android.core.design.PreferenceRow
import app.gulp.android.core.domain.TrackingReference
import app.gulp.android.core.graphics.WaterAdaptiveText
import app.gulp.android.core.graphics.WaterScene

@Composable
fun SettingsScreen(
    scene: WaterScene, viewport: IntSize, preferences: GulpPreferences,
    onVisualCapacityChange: (Int) -> Unit,
    onTrackingChange: (Int, Boolean) -> Unit,
    onCustomizeHome: () -> Unit, onWaterLab: () -> Unit,
    onReminders: () -> Unit, onPreviewAction: () -> Unit
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
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Medical fluid plan", color = GulpPalette.White, modifier = Modifier.weight(1f))
                    Checkbox(checked = preferences.clinicianManagedFluids,
                        onCheckedChange = { onTrackingChange(preferences.trackingReferenceMl, it) })
                }
                if (preferences.clinicianManagedFluids) {
                    Note("Follow your clinician's fluid guidance. Generic comparison goals are disabled.")
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
                PreferenceRow("Appearance and accessibility", null, onPreviewAction)
            }
        }
        FrostPanel(Modifier.fillMaxWidth(), padding = 17.dp) {
            Column {
                Text("Data and privacy", color = GulpPalette.White, fontWeight = FontWeight.Bold)
                Note("Drink records are stored locally. No account or internet permission is required.")
                PreferenceRow("Export your drinks", null, onPreviewAction)
                PreferenceRow("Delete all records", null, onPreviewAction)
            }
        }
        WaterAdaptiveText("gulp · Phase 4", scene, viewport, fontSize = 12.sp)
    }
}
