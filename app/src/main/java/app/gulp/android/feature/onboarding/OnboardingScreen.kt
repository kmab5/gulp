package app.gulp.android.feature.onboarding

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.gulp.android.core.design.FrostPanel
import app.gulp.android.core.design.GulpPalette
import app.gulp.android.core.design.Note
import app.gulp.android.core.domain.ReminderPlan
import app.gulp.android.core.domain.ReminderRules
import app.gulp.android.core.domain.TrackingReference

@Composable
fun OnboardingScreen(onComplete: (ReminderPlan, Int, Boolean) -> Unit) {
    val context = LocalContext.current
    var page by remember { mutableIntStateOf(0) }
    var enabled by remember { mutableStateOf(false) }
    var interval by remember { mutableIntStateOf(90) }
    var wake by remember { mutableIntStateOf(8 * 60) }
    var bedtime by remember { mutableIntStateOf(22 * 60) }
    var reference by remember { mutableIntStateOf(0) }
    var clinicianManaged by remember { mutableStateOf(false) }
    val plan = ReminderPlan(enabled, interval, wake, bedtime)
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 25.dp, vertical = 48.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text("gulp", fontSize = 42.sp, color = GulpPalette.White, fontWeight = FontWeight.Bold)
        Note("Set up your reminders. You can change everything later.")
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(3) { idx ->
                Spacer(Modifier.weight(1f).height(4.dp).background(if (idx <= page) GulpPalette.Glacier else GulpPalette.Edge))
            }
        }
        when (page) {
            0 -> {
                FrostPanel(Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(15.dp)) {
                        Text("When should we remind you?", fontSize = 22.sp, color = GulpPalette.White, fontWeight = FontWeight.Bold)
                        Note("These are optional reminders, not times you're required to drink.")
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Reminders", color = GulpPalette.White)
                            Switch(enabled, onCheckedChange = { enabled = it })
                        }
                        if (enabled) {
                            Text("How often?", color = GulpPalette.White)
                            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                listOf(60,90,120,180).forEach { amount ->
                                    FilterChip(selected = interval == amount, onClick = { interval = amount },
                                        label = { Text("${amount}m") })
                                }
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                TextButton(onClick = {
                                    TimePickerDialog(context, { _, h, m -> wake = h * 60 + m }, wake / 60, wake % 60, true).show()
                                }) { Text("Wake ${ReminderRules.formatMinute(wake)}") }
                                TextButton(onClick = {
                                    TimePickerDialog(context, { _, h, m -> bedtime = h * 60 + m }, bedtime / 60, bedtime % 60, true).show()
                                }) { Text("Sleep ${ReminderRules.formatMinute(bedtime)}") }
                            }
                            Note("No reminders during your quiet hours. Delivery may be delayed by Android battery settings.")
                        }
                    }
                }
            }
            1 -> {
                FrostPanel(Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Want a tracking reference?", fontSize = 22.sp, color = GulpPalette.White, fontWeight = FontWeight.Bold)
                        Note("This compares what you log with a chosen amount. It doesn't measure your hydration.")
                        listOf(0,1500,1875,2000,2500).forEach { amount ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(if (amount == 0) "No goal" else "${amount} mL", color = GulpPalette.White)
                                androidx.compose.material3.RadioButton(selected = reference == amount,
                                    onClick = { reference = amount }, enabled = !clinicianManaged)
                            }
                        }
                        Note("1,500 and 1,875 mL are illustrative estimates of the beverage share of EFSA adult total-water references. Food also contains water; these are not prescriptions.")
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("I follow a medical fluid plan", modifier = Modifier.weight(1f), color = GulpPalette.White)
                            Checkbox(clinicianManaged, onCheckedChange = {
                                clinicianManaged = it
                                if (it) reference = TrackingReference.NONE
                            })
                        }
                        if (clinicianManaged) Note("We'll avoid generic volume goals. Follow the plan from your healthcare professional.")
                    }
                }
            }
            else -> {
                FrostPanel(Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("You're all set", fontSize = 24.sp, color = GulpPalette.White, fontWeight = FontWeight.Bold)
                        Note(if (enabled) "Android will ask if gulp can send you notifications. You can decline and enable them later."
                             else "You can always enable reminders later in the Reminders tab.")
                        Note("Log whenever you drink. Missing a reminder never creates water you need to catch up on.")
                        Note("Your entries stay on this device. No account is needed.")
                    }
                }
            }
        }
        if (!plan.valid()) Note("Your wake and sleep times need at least one hour between them.")
        Spacer(Modifier.height(8.dp))
        Button(onClick = {
            if (page < 2) page++ else onComplete(plan, reference, clinicianManaged)
        }, enabled = plan.valid(), modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GulpPalette.Glacier,
                contentColor = GulpPalette.Ink)) {
            Text(if (page == 2) "Start using gulp" else "Continue", fontWeight = FontWeight.Bold)
        }
        if (page > 0) TextButton(onClick = { page-- }) { Text("Back") }
    }
}
