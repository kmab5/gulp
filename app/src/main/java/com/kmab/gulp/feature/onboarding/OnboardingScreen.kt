package com.kmab.gulp.feature.onboarding

import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import com.kmab.gulp.core.design.GlassScrollableColumn
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import com.kmab.gulp.core.graphics.WaterAdaptiveSubmark
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kmab.gulp.core.design.FrostPanel
import com.kmab.gulp.core.design.GulpChoiceChip
import com.kmab.gulp.core.design.GulpChoiceRow
import com.kmab.gulp.core.design.GulpPalette
import com.kmab.gulp.core.design.GulpToggleRow
import com.kmab.gulp.core.design.GulpMedicalPlanCard
import com.kmab.gulp.core.graphics.WaterScene
import com.kmab.gulp.core.graphics.WaterAdaptiveText
import androidx.compose.ui.unit.IntSize
import com.kmab.gulp.core.design.Note
import com.kmab.gulp.core.design.PrimaryAction
import com.kmab.gulp.core.domain.ReminderPlan
import com.kmab.gulp.core.domain.ReminderRules
import com.kmab.gulp.core.domain.TrackingReference

/** Functional controls with a consistent dark surface, not OS-default white widgets. */
@Composable
fun OnboardingScreen(scene: WaterScene, viewport: IntSize, onComplete: (ReminderPlan, Int, Boolean) -> Unit) {
    val context = LocalContext.current
    var step by remember { mutableIntStateOf(0) }
    var remindersEnabled by remember { mutableStateOf(false) }
    var interval by remember { mutableIntStateOf(90) }
    var wake by remember { mutableIntStateOf(8 * 60) }
    var bedtime by remember { mutableIntStateOf(22 * 60) }
    var reference by remember { mutableIntStateOf(0) }
    var medicalPlan by remember { mutableStateOf(false) }
    var customSelected by remember { mutableStateOf(false) }
    var customAmount by remember { mutableStateOf("1800") }
    val customParsed = customAmount.toIntOrNull()?.takeIf { it in 250..6000 }
    val chosenReference = if (medicalPlan) 0 else if (customSelected) customParsed ?: 0 else reference
    val plan = ReminderPlan(remindersEnabled, interval, wake, bedtime)
    val canContinue = plan.valid() && (step != 1 || medicalPlan || !customSelected || customParsed != null)
    val progress by animateFloatAsState((step + 1) / 3f, animationSpec = tween(600), label = "setup progress")

    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()
        .padding(horizontal = 24.dp, vertical = 15.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
            Box(Modifier.size(49.dp)
                .background(Color(0xFF19445D).copy(alpha = .88f), RoundedCornerShape(16.dp))
                .clickable(enabled = step > 0) { step-- }, contentAlignment = Alignment.Center) {
                if (step > 0) Text("‹", color = GulpPalette.White,
                    fontWeight = FontWeight.Bold, fontSize = 32.sp)
                else WaterAdaptiveSubmark(scene, viewport, Modifier.size(34.dp))
            }
            WaterAdaptiveText("${step + 1} of 3", scene, viewport, fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(17.dp))
        val wavePhase by androidx.compose.animation.core.rememberInfiniteTransition(label = "setup wave")
            .animateFloat(initialValue = 0f, targetValue = 6.2831855f,
                animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                    animation = tween(1900, easing = LinearEasing)), label = "wave offset")
        Canvas(Modifier.fillMaxWidth().height(9.dp)) {
            drawRoundRect(Color.White.copy(alpha = .15f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2f))
            val filled = size.width * progress
            val water = Path().apply {
                moveTo(0f, size.height)
                lineTo(0f, size.height * .38f)
                val step = 4f
                var x = 0f
                while (x <= filled) {
                    val waveY = size.height * (.40f + .19f * kotlin.math.sin((if (scene.reducedMotion) 0f else wavePhase) + x / 27f))
                    lineTo(x, waveY)
                    x += step
                }
                lineTo(filled, size.height)
                close()
            }
            clipPath(Path().apply { addRoundRect(androidx.compose.ui.geometry.RoundRect(
                0f, 0f, size.width, size.height,
                androidx.compose.ui.geometry.CornerRadius(size.height / 2f))) }) {
                drawPath(water, Brush.horizontalGradient(listOf(GulpPalette.Glacier, GulpPalette.LiquidTop)))
            }
        }
        Spacer(Modifier.height(21.dp))
        Box(Modifier.weight(1f).fillMaxWidth()) {
            AnimatedContent(targetState = step,
                transitionSpec = { fadeIn(tween(350)) togetherWith fadeOut(tween(200)) },
                label = "onboarding step") { current ->
                GlassScrollableColumn(Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(17.dp)) {
                    val title = when (current) {
                        0 -> "Reminders"
                        1 -> "Daily goal"
                        else -> "You're ready"
                    }
                    WaterAdaptiveText(title, scene, viewport, fontSize = 34.sp, lineHeight = 39.sp,
                        fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    WaterAdaptiveText(when (current) {
                        0 -> "Get gentle reminders during your day."
                        1 -> "Choose an optional amount to compare with your drinks."
                        else -> "You can change everything later."
                    }, scene, viewport, fontSize = 16.sp, lineHeight = 23.sp,
                        textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 15.dp))
                    when (current) {
                        0 -> {
                            FrostPanel(Modifier.fillMaxWidth()) {
                                Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
                                    GulpToggleRow("Enable reminders",
                                        "These are suggestions, not instructions to drink.",
                                        checked = remindersEnabled, onCheckedChange = { remindersEnabled = it })
                                    AnimatedVisibility(remindersEnabled,
                                        enter = fadeIn(tween(350)) + expandVertically(tween(420)),
                                        exit = fadeOut(tween(180)) + shrinkVertically(tween(380))) {
                                        Column(verticalArrangement = Arrangement.spacedBy(15.dp)) {
                                            Text("Remind me every", color = GulpPalette.White,
                                                fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                listOf(60, 90, 120, 180).forEach { amount ->
                                                    GulpChoiceChip(if (amount == 60) "1 hr" else if (amount == 90) "90 min" else "${amount / 60} hr",
                                                        selected = interval == amount, onClick = { interval = amount },
                                                        modifier = Modifier.weight(1f))
                                                }
                                            }
                                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                TimeButton("Wake up", ReminderRules.formatMinute(wake), true,
                                                    Modifier.weight(1f)) {
                                                    TimePickerDialog(context, android.R.style.Theme_Material_Dialog_Alert,
                                                        { _, h, m -> wake = h * 60 + m }, wake / 60, wake % 60, true).show()
                                                }
                                                TimeButton("Bedtime", ReminderRules.formatMinute(bedtime), false,
                                                    Modifier.weight(1f)) {
                                                    TimePickerDialog(context, android.R.style.Theme_Material_Dialog_Alert,
                                                        { _, h, m -> bedtime = h * 60 + m },
                                                        bedtime / 60, bedtime % 60, true).show()
                                                }
                                            }
                                            Note("No reminders during quiet hours. Android may sometimes delay them.")
                                        }
                                    }
                                }
                            }
                        }
                        1 -> {
                            FrostPanel(Modifier.fillMaxWidth()) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Note("Your goal is for what you record. It doesn't measure hydration.")
                                    listOf(0, 1500, 1875, 2000, 2500).forEach { amount ->
                                        GulpChoiceRow(if (amount == 0) "No goal" else "${amount.formatMl()} mL",
                                            selected = !customSelected && reference == amount && !medicalPlan,
                                            enabled = !medicalPlan, onClick = {
                                                customSelected = false; reference = amount
                                            })
                                    }
                                    GulpChoiceRow("Custom amount", "Set your own tracking reference",
                                        selected = customSelected && !medicalPlan, enabled = !medicalPlan,
                                        onClick = { customSelected = true })
                                    AnimatedVisibility(customSelected && !medicalPlan,
                                        enter = fadeIn() + expandVertically(),
                                        exit = fadeOut() + shrinkVertically()) {
                                        OutlinedTextField(value = customAmount, onValueChange = { entry ->
                                            if (entry.length <= 4 && entry.all(Char::isDigit)) customAmount = entry
                                        }, label = { Text("Amount in mL") }, suffix = { Text("mL") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true, isError = customParsed == null,
                                            supportingText = { Text("Between 250 and 6,000 mL") },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = GulpPalette.White,
                                                unfocusedTextColor = GulpPalette.White,
                                                focusedLabelColor = GulpPalette.Glacier,
                                                unfocusedLabelColor = GulpPalette.SoftWhite,
                                                focusedBorderColor = GulpPalette.Glacier,
                                                unfocusedBorderColor = GulpPalette.Edge,
                                                focusedSuffixColor = GulpPalette.Glacier,
                                                unfocusedSuffixColor = GulpPalette.SoftWhite),
                                            modifier = Modifier.fillMaxWidth())
                                    }
                                    Note("1,500 and 1,875 mL are illustrative estimates of the beverage share of EFSA adult total-water references. Food also contains water; these are not prescriptions.")
                                }
                            }
                            FrostPanel(Modifier.fillMaxWidth()) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    GulpMedicalPlanCard(
                                        checked = medicalPlan, onCheckedChange = {
                                            medicalPlan = it
                                            if (it) reference = TrackingReference.NONE
                                        })
                                    AnimatedVisibility(medicalPlan,
                                        enter = fadeIn() + expandVertically(),
                                        exit = fadeOut() + shrinkVertically()) {
                                        Note("We'll turn off generic goals and reminders. Follow your clinician's plan.")
                                    }
                                }
                            }
                        }
                        else -> {
                            FrostPanel(Modifier.fillMaxWidth()) {
                                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Text("All set", fontSize = 22.sp, fontWeight = FontWeight.Bold,
                                        color = GulpPalette.White)
                                    Note(if (remindersEnabled && !medicalPlan)
                                        "Android may ask for notification permission. You can say no and enable it later."
                                        else "You can enable reminders whenever you want.")
                                    Note("Log drinks when you remember. There's nothing to catch up on if you miss a reminder.")
                                    Note("Your records stay on your phone. No account needed.")
                                }
                            }
                        }
                    }
                }
            }
        }
        if (!plan.valid()) Note("Wake up and bedtime need to be at least an hour apart.")
        Spacer(Modifier.height(12.dp))
        PrimaryAction(if (step == 2) "Start using gulp" else "Continue",
            onClick = { if (step < 2) step++ else onComplete(plan, chosenReference, medicalPlan) },
            enabled = canContinue,
            modifier = Modifier.heightIn(min = 58.dp)) {
            Text(if (step == 2) "Start using gulp  →" else "Continue  →",
                color = GulpPalette.Ink, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        }
    }
}

private fun Int.formatMl(): String = "%,d".format(java.util.Locale.US, this)

@Composable
private fun TimeButton(label: String, time: String, sun: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Column(modifier.heightIn(min = 85.dp)
        .background(Color(0xFF184761).copy(alpha = .9f), RoundedCornerShape(18.dp))
        .clickable(onClickLabel = "Change $label time", onClick = onClick)
        .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Canvas(Modifier.size(20.dp)) {
                val c = if (sun) Color(0xFFFFCF7E) else Color(0xFFBCD2FF)
                drawCircle(c, radius = size.minDimension * .29f)
                if (sun) for (n in 0..7) {
                    val a = n * Math.PI / 4
                    val dx = kotlin.math.cos(a).toFloat()
                    val dy = kotlin.math.sin(a).toFloat()
                    drawLine(c, Offset(center.x + dx * size.width * .34f, center.y + dy * size.width * .34f),
                        Offset(center.x + dx * size.width * .47f, center.y + dy * size.width * .47f),
                        strokeWidth = 1.5.dp.toPx())
                } else drawCircle(Color(0xFF184761), radius = size.width * .29f,
                    center = Offset(center.x + size.width * .12f, center.y - size.height * .12f))
            }
            Text(label, color = GulpPalette.White, fontSize = 14.sp,
                fontWeight = FontWeight.Medium)
        }
        Text(time, color = GulpPalette.Glacier, fontSize = 19.sp, fontWeight = FontWeight.Bold)
    }
}
