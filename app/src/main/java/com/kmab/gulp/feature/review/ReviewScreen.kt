package com.kmab.gulp.feature.review

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kmab.gulp.core.design.FrostPanel
import com.kmab.gulp.core.design.GlassScrollableColumn
import com.kmab.gulp.core.design.GulpPalette
import com.kmab.gulp.core.design.GulpWaterText as Text
import com.kmab.gulp.core.design.Note
import com.kmab.gulp.core.design.DrinkIcon
import com.kmab.gulp.core.domain.DrinkRecord
import com.kmab.gulp.core.domain.ReviewRules
import com.kmab.gulp.core.domain.ReviewInsightRules
import com.kmab.gulp.core.graphics.WaterAdaptiveText
import com.kmab.gulp.core.graphics.WaterScene
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** An honest dashboard: missing data means unrecorded, not zero actual intake. */
@Composable
fun ReviewScreen(scene: WaterScene, viewport: IntSize, records: List<DrinkRecord>,
                 today: LocalDate, referenceMl: Int) {
    val zone = remember { ZoneId.systemDefault() }
    val week = remember(records, today, zone) { ReviewRules.summarize(records, today, zone) }
    val insights = remember(records, today, zone) { ReviewInsightRules.summarize(records, today, zone) }
    val hasWeek = week.entries > 0
    GlassScrollableColumn(Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 31.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        WaterAdaptiveText("Review", scene, viewport, fontWeight = FontWeight.Bold, fontSize = 23.sp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricCard("Last 7 days", if (hasWeek) "${week.totalMl} mL" else "—", Modifier.weight(1f))
            MetricCard("Days logged", "${week.recordedDays} / 7", Modifier.weight(1f))
        }
        FrostPanel(Modifier.fillMaxWidth(), padding = 19.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Your week", fontSize = 19.sp, color = GulpPalette.White,
                    fontWeight = FontWeight.Bold)
                Note("Recorded amounts, by day")
                RecordBars(week.days.map { it.loggedMl }, Modifier.fillMaxWidth().height(144.dp))
                Row(Modifier.fillMaxWidth()) {
                    week.days.forEach { day ->
                        Text(day.date.format(DateTimeFormatter.ofPattern("EEE")),
                            modifier = Modifier.weight(1f), fontSize = 11.sp,
                            color = GulpPalette.SoftWhite, textAlign = TextAlign.Center)
                    }
                }
                Note("No bar means nothing was logged on that date.")
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricCard("Per logged day", insights.averageWhenRecordedMl?.let { "$it mL" } ?: "—",
                Modifier.weight(1f))
            MetricCard("Entries", week.entries.toString(), Modifier.weight(1f))
        }
        FrostPanel(Modifier.fillMaxWidth(), padding = 19.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Last 30 days", fontWeight = FontWeight.Bold, fontSize = 19.sp,
                    color = GulpPalette.White)
                RecordBars(insights.last30Days.map { it.loggedMl },
                    Modifier.fillMaxWidth().height(100.dp), gapRatio = .17f)
                SummaryLine("Total recorded", "${insights.total30Ml} mL")
                SummaryLine("Days with entries", "${insights.daysRecorded30} / 30")
                insights.busiestRecordedDay?.let { day ->
                    SummaryLine("Highest logged day", "${day.date.format(DateTimeFormatter.ofPattern("MMM d"))} · ${day.loggedMl} mL")
                }
                Note("These figures exclude unrecorded drinks and food moisture.")
            }
        }
        FrostPanel(Modifier.fillMaxWidth(), padding = 19.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Drinks this week", color = GulpPalette.White,
                    fontSize = 19.sp, fontWeight = FontWeight.Bold)
                if (insights.drinkShares7.isEmpty()) {
                    Note("Log a few drinks to see how your recorded drinks break down.")
                } else {
                    val sum = insights.drinkShares7.sumOf { it.amountMl }.coerceAtLeast(1)
                    insights.drinkShares7.forEach { share ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            DrinkIcon(share.icon, tint = GulpPalette.Glacier)
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                SummaryLine(share.name, "${share.amountMl} mL · ${share.count} logs")
                                Canvas(Modifier.fillMaxWidth().height(5.dp)) {
                                    drawRoundRect(GulpPalette.Surface.copy(alpha = .65f),
                                        cornerRadius = CornerRadius(size.height / 2))
                                    drawRoundRect(GulpPalette.Glacier,
                                        size = Size(size.width * (share.amountMl.toFloat() / sum), size.height),
                                        cornerRadius = CornerRadius(size.height / 2))
                                }
                            }
                        }
                    }
                }
            }
        }
        FrostPanel(Modifier.fillMaxWidth(), padding = 19.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                Text("When you logged", fontSize = 19.sp, fontWeight = FontWeight.Bold,
                    color = GulpPalette.White)
                SummaryLine("Before noon", "${insights.morningCount7} entries")
                SummaryLine("12–6 PM", "${insights.afternoonCount7} entries")
                SummaryLine("After 6 PM", "${insights.eveningCount7} entries")
                if (referenceMl > 0) {
                    SummaryLine("Your chosen reference", "$referenceMl mL")
                    Note("A reference for records only, not a required dose or hydration assessment.")
                }
                week.previousWeekTotalMl?.let { previous ->
                    val diff = week.totalMl - previous
                    SummaryLine("Versus prior week", "${if (diff >= 0) "+" else ""}$diff mL")
                }
            }
        }
        if (records.isEmpty()) {
            Note("Start by logging a drink from Home. There's no minimum history required to use Review.")
        }
        WaterAdaptiveText("All charts show recorded activity, not physical hydration.",
            scene, viewport, fontSize = 12.sp)
    }
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    FrostPanel(modifier, padding = 14.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text(label, fontSize = 12.sp, color = GulpPalette.SoftWhite)
            Text(value, fontSize = 20.sp, color = GulpPalette.Glacier,
                fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

@Composable
private fun SummaryLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = GulpPalette.SoftWhite,
            modifier = Modifier.weight(1f))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
            color = GulpPalette.Glacier)
    }
}

@Composable
private fun RecordBars(values: List<Int>, modifier: Modifier = Modifier,
                       gapRatio: Float = .29f) {
    Canvas(modifier) {
        if (values.isEmpty()) return@Canvas
        val max = values.maxOrNull()?.coerceAtLeast(1) ?: 1
        val width = size.width / values.size
        drawLine(GulpPalette.Edge.copy(alpha = .6f),
            Offset(0f, size.height - 2f), Offset(size.width, size.height - 2f), 1.5f)
        values.forEachIndexed { i, ml ->
            if (ml > 0) {
                val height = (ml.toFloat() / max) * (size.height - 10f)
                val left = i * width + width * gapRatio
                val barWidth = (width * (1f - 2f * gapRatio)).coerceAtLeast(1.5f)
                drawRoundRect(GulpPalette.Glacier.copy(alpha = .88f),
                    topLeft = Offset(left, size.height - height - 2f),
                    size = Size(barWidth, height),
                    cornerRadius = CornerRadius(barWidth.coerceAtMost(8f)))
            }
        }
    }
}
