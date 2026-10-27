package com.kmab.gulp.feature.reminders

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.Alignment
import com.kmab.gulp.R
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.kmab.gulp.core.design.GlassScrollableColumn
import androidx.compose.material3.OutlinedTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.kmab.gulp.core.design.GlassSurface
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import com.kmab.gulp.core.design.GulpWaterText as Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kmab.gulp.core.data.GulpPreferences
import com.kmab.gulp.core.design.FrostPanel
import com.kmab.gulp.core.design.GulpPalette
import com.kmab.gulp.core.design.Note
import com.kmab.gulp.core.design.GulpToggleRow
import com.kmab.gulp.core.design.GulpChoiceChip
import com.kmab.gulp.core.domain.ReminderPlan
import com.kmab.gulp.core.domain.ReminderRules
import com.kmab.gulp.core.graphics.WaterAdaptiveText
import com.kmab.gulp.core.graphics.WaterScene
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun RemindersScreen(
    scene: WaterScene, viewport: IntSize, preferences: GulpPreferences,
    notificationPermission: Boolean, nowMillis: Long,
    onPlanChange: (ReminderPlan) -> Unit,
    requestNotificationPermission: () -> Unit
) {
    val context = LocalContext.current
    val plan = preferences.reminders
    var customInterval by remember { mutableStateOf(plan.intervalMinutes.toString()) }
    val enteredInterval = customInterval.toIntOrNull()?.takeIf { it in 30..360 }
    val next = ReminderRules.nextAlarm(nowMillis, ZoneId.systemDefault(), plan)
    GlassScrollableColumn(Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 31.dp),
        verticalArrangement = Arrangement.spacedBy(17.dp)) {
        WaterAdaptiveText("Reminders", scene, viewport, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        FrostPanel(Modifier.fillMaxWidth(), padding = 22.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Next reminder", fontSize = 14.sp, color = GulpPalette.SoftWhite)
                Text(if (next == null) "Paused" else Instant.ofEpochMilli(next)
                    .atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("EEE, HH:mm")),
                    color = GulpPalette.Glacier, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Note("Times are approximate. Android may delay notifications to save battery.")
                GulpToggleRow("Reminders on", "Pause or resume notifications",
                    checked = plan.enabled, enabled = !preferences.clinicianManagedFluids,
                    onCheckedChange = { onPlanChange(plan.copy(enabled = it, snoozeUntilMillis = 0)) })
            }
        }
        if (preferences.clinicianManagedFluids) {
            FrostPanel(Modifier.fillMaxWidth()) {
                Note("Reminders are paused for medically managed fluid plans. Follow your clinician's instructions; you can still log drinks manually.")
            }
        }
        if (plan.enabled && !notificationPermission) {
            FrostPanel(Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Notifications are off", color = GulpPalette.White, fontWeight = FontWeight.Bold)
                    Note("Turn on Android notifications to receive reminders.")
                    Button(onClick = requestNotificationPermission,
                        colors = ButtonDefaults.buttonColors(containerColor = GulpPalette.Glacier,
                            contentColor = GulpPalette.Ink)) { Text("Allow notifications") }
                }
            }
        }
        FrostPanel(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Every", fontSize = 17.sp, color = GulpPalette.White, fontWeight = FontWeight.Bold)
                listOf(60,90,120,180,240).chunked(3).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { amount ->
                            GulpChoiceChip(label = if (amount < 120) "$amount min" else "${amount / 60} hr",
                                selected = plan.intervalMinutes == amount, onClick = {
                                    onPlanChange(plan.copy(intervalMinutes = amount, snoozeUntilMillis = 0))
                                })
                        }
                    }
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = customInterval, onValueChange = { input ->
                        if (input.length <= 3 && input.all(Char::isDigit)) customInterval = input
                    }, modifier = Modifier.weight(1f), label = { Text("Custom minutes") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = customInterval.isNotEmpty() && enteredInterval == null,
                        supportingText = { Text("30–360 minutes") })
                    Button(enabled = enteredInterval != null, onClick = {
                        enteredInterval?.let { onPlanChange(plan.copy(intervalMinutes = it,
                            snoozeUntilMillis = 0)) }
                    }, colors = ButtonDefaults.buttonColors(containerColor = GulpPalette.Glacier,
                        contentColor = GulpPalette.Ink)) { Text("Set") }
                }
                Note("Choose what feels comfortable. There's no proven optimal drinking interval.")
            }
        }
        FrostPanel(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Quiet hours", color = GulpPalette.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Note("You won't get drink notifications outside these hours.")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GlassSurface(Modifier.weight(1f), radius = 16, tint = .29f) {
                    TextButton(onClick = {
                        TimePickerDialog(context, android.R.style.Theme_Material_Dialog_Alert, { _, h, m ->
                            val changed = plan.copy(awakeStartMinute = h * 60 + m, snoozeUntilMillis = 0)
                            if (changed.valid()) onPlanChange(changed)
                        }, plan.awakeStartMinute / 60, plan.awakeStartMinute % 60, true).show()
                    }, modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            androidx.compose.foundation.Image(painterResource(R.drawable.ic_drink_sun),
                                null, Modifier.size(21.dp),
                                colorFilter = ColorFilter.tint(GulpPalette.White))
                            Text("Wake ${ReminderRules.formatMinute(plan.awakeStartMinute)}",
                                fontSize = 13.sp, color = GulpPalette.White)
                        }
                    }
                    }
                    GlassSurface(Modifier.weight(1f), radius = 16, tint = .29f) {
                    TextButton(onClick = {
                        TimePickerDialog(context, android.R.style.Theme_Material_Dialog_Alert, { _, h, m ->
                            val changed = plan.copy(awakeEndMinute = h * 60 + m, snoozeUntilMillis = 0)
                            if (changed.valid()) onPlanChange(changed)
                        }, plan.awakeEndMinute / 60, plan.awakeEndMinute % 60, true).show()
                    }, modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            androidx.compose.foundation.Image(painterResource(R.drawable.ic_drink_moon),
                                null, Modifier.size(21.dp),
                                colorFilter = ColorFilter.tint(GulpPalette.White))
                            Text("Sleep ${ReminderRules.formatMinute(plan.awakeEndMinute)}",
                                fontSize = 13.sp, color = GulpPalette.White)
                        }
                    }
                    }
                }
                Note("Choose a wake and sleep time at least one hour apart.")
            }
        }
        WaterAdaptiveText("A reminder is an invitation, not an order to drink.", scene, viewport, fontSize = 12.sp)
    }
}
