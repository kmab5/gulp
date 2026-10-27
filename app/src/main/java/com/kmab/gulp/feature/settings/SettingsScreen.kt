package com.kmab.gulp.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.kmab.gulp.core.design.GlassScrollableColumn
import com.kmab.gulp.core.design.GulpWaterText as Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Slider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import kotlin.math.roundToInt
import com.kmab.gulp.core.design.GulpChoiceRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
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
    onExperienceChange: (Boolean, Boolean, Int) -> Unit,
    onExport: () -> Unit, onDeleteAll: () -> Unit,
    onManageDrinks: () -> Unit = {},
    onWidgetPreset: () -> Unit = {}
) {
    val visualCapacityMl = preferences.visualCapacityMl
    var displayDraft by remember(visualCapacityMl) { mutableStateOf(visualCapacityMl.toFloat()) }
    var customReferenceSelected by remember(preferences.trackingReferenceMl) {
        mutableStateOf(preferences.trackingReferenceMl !in TrackingReference.selectable)
    }
    var customReference by remember(preferences.trackingReferenceMl) {
        mutableStateOf(preferences.trackingReferenceMl.coerceAtLeast(500).toString())
    }
    val validReference = customReference.toIntOrNull()?.takeIf { it in 250..6000 }
    var motionDraft by remember(preferences.motionStrengthPct) {
        mutableStateOf(preferences.motionStrengthPct.toFloat())
    }
    val context = LocalContext.current
    val version = remember(context) {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }
            .getOrNull() ?: "unknown"
    }
    fun openWeb(url: Uri) {
        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, url).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    }
    fun feedback(type: String) {
        openWeb(Uri.parse("https://kmab5.github.io/kmab-feedback/").buildUpon()
            .appendQueryParameter("product", "gulp")
            .appendQueryParameter("type", type)
            .appendQueryParameter("source", "android")
            .appendQueryParameter("version", version)
            .build())
    }
    GlassScrollableColumn(Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 31.dp),
        verticalArrangement = Arrangement.spacedBy(19.dp)) {
        WaterAdaptiveText("Settings", scene, viewport, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        FrostPanel(Modifier.fillMaxWidth(), padding = 20.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Home", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = GulpPalette.White)
                PreferenceRow("Customize shortcuts", "Move, resize and edit your quick-add tiles", onCustomizeHome)
                PreferenceRow("My drinks", "Create drinks, choose icons and set default amounts", onManageDrinks)
                PreferenceRow("Widget quick add", "Choose your widget drink and amount", onWidgetPreset)
                Text("Water display scale", fontSize = 14.sp, color = GulpPalette.White, fontWeight = FontWeight.Medium)
                Note("Only affects the illustration. This isn't a drinking recommendation.")
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("250 mL", fontSize = 12.sp, color = GulpPalette.SoftWhite)
                    Text("${displayDraft.roundToInt()} mL", fontSize = 19.sp,
                        color = GulpPalette.Glacier, fontWeight = FontWeight.Bold)
                    Text("6,000 mL", fontSize = 12.sp, color = GulpPalette.SoftWhite)
                }
                Slider(value = displayDraft, onValueChange = {
                    displayDraft = (it / 50f).roundToInt() * 50f
                }, onValueChangeFinished = {
                    onVisualCapacityChange(displayDraft.roundToInt().coerceIn(250, 6000))
                }, valueRange = 250f..6000f, modifier = Modifier.fillMaxWidth())
                Note("Changes the illustration only, in 50 mL steps. Not a drinking target.")
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
                    TrackingReference.selectable.forEach { amount ->
                        GulpChoiceRow(if (amount == 0) "No reference" else "$amount mL",
                            supporting = when (amount) {
                                0 -> "Just record drinks without a target"
                                1500, 1875 -> "Illustrative beverage share of an EFSA reference"
                                else -> "Your chosen amount for comparison"
                            }, selected = !customReferenceSelected && preferences.trackingReferenceMl == amount,
                            onClick = { customReferenceSelected = false; onTrackingChange(amount, false) })
                    }
                    GulpChoiceRow("Custom amount", "Enter any number from 250 to 6,000 mL",
                        selected = customReferenceSelected,
                        onClick = { customReferenceSelected = true })
                    if (customReferenceSelected) {
                        OutlinedTextField(value = customReference, onValueChange = { input ->
                            if (input.length <= 5 && input.all(Char::isDigit)) customReference = input
                        }, label = { Text("Amount (mL)") }, singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = customReference.isNotBlank() && validReference == null,
                            modifier = Modifier.fillMaxWidth())
                        TextButton(enabled = validReference != null, onClick = {
                            validReference?.let { onTrackingChange(it, false) }
                        }, modifier = Modifier.fillMaxWidth()) {
                            Text("Use custom amount", color = GulpPalette.Glacier, fontWeight = FontWeight.Bold)
                        }
                    }
                    Note("Food contributes water too. These are optional recorded-beverage comparisons, not medical targets.")
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
                AppearanceToggle("Glass blur", "Frosted surfaces over the moving water",
                    preferences.glassBlurEnabled) {
                    onExperienceChange(it, preferences.swipeTabsEnabled, preferences.motionStrengthPct)
                }
                AppearanceToggle("Swipe between tabs", "Switch tabs by swiping left or right",
                    preferences.swipeTabsEnabled) {
                    onExperienceChange(preferences.glassBlurEnabled, it, preferences.motionStrengthPct)
                }
                Text("Water motion intensity", color = GulpPalette.White, fontWeight = FontWeight.Medium)
                Text("${motionDraft.roundToInt()}%", color = GulpPalette.Glacier)
                Slider(value = motionDraft, onValueChange = { motionDraft = (it / 5f).roundToInt() * 5f },
                    onValueChangeFinished = {
                        onExperienceChange(preferences.glassBlurEnabled, preferences.swipeTabsEnabled,
                            motionDraft.roundToInt().coerceIn(25, 150))
                    }, valueRange = 25f..150f)
                Note("Set to 100% for the original feel. Reduce motion overrides this control.")
            }
        }
        FrostPanel(Modifier.fillMaxWidth(), padding = 17.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Feedback", color = GulpPalette.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Note("Opens the feedback form in your browser. You choose what to share.")
                PreferenceRow("Report a bug", "Tell me what went wrong", onClick = { feedback("bug") })
                PreferenceRow("Suggest an improvement", "Help make gulp better", onClick = { feedback("improvement") })
                PreferenceRow("Share an idea", "Request a feature", onClick = { feedback("idea") })
            }
        }
        FrostPanel(Modifier.fillMaxWidth(), padding = 17.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("About gulp", color = GulpPalette.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Note("A gentle reminder and record of the drinks you log. Created by kmab.")
                Note("Version $version · Android · Offline-first")
                PreferenceRow("Source code", "github.com/kmab5/gulp", onClick = {
                    openWeb(Uri.parse("https://github.com/kmab5/gulp"))
                })
                PreferenceRow("Developer", "kmab5 on GitHub", onClick = {
                    openWeb(Uri.parse("https://github.com/kmab5"))
                })
                Note("gulp doesn't measure hydration or replace medical advice.")
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
