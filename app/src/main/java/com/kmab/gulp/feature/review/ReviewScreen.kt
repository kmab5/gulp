package com.kmab.gulp.feature.review

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kmab.gulp.core.design.FrostPanel
import com.kmab.gulp.core.design.GulpPalette
import com.kmab.gulp.core.design.Note
import com.kmab.gulp.core.domain.DrinkRecord
import com.kmab.gulp.core.domain.ReviewRules
import com.kmab.gulp.core.graphics.WaterAdaptiveText
import com.kmab.gulp.core.graphics.WaterScene
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Seven-day trends computed from records. An empty day never means that the user drank zero. */
@Composable
fun ReviewScreen(scene: WaterScene, viewport: IntSize, records: List<DrinkRecord>, today: LocalDate, referenceMl: Int) {
    val zone = ZoneId.systemDefault()
    val review = remember(records, today, zone) { ReviewRules.summarize(records, today, zone) }
    val recorded = review.entries > 0
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())
        .padding(horizontal = 22.dp, vertical = 31.dp), verticalArrangement = Arrangement.spacedBy(17.dp)) {
        WaterAdaptiveText("Review", scene, viewport, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        FrostPanel(Modifier.fillMaxWidth(), padding = 20.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Last 7 days", color = GulpPalette.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Note("Average recorded drinks per calendar day")
                Text(if (recorded) "${review.averagePerDayMl} mL" else "No entries yet",
                    color = GulpPalette.Glacier, fontSize = if (recorded) 33.sp else 24.sp,
                    fontWeight = FontWeight.Bold)
                val max = review.days.maxOf { it.loggedMl }.coerceAtLeast(1)
                Canvas(Modifier.fillMaxWidth().height(140.dp)) {
                    val step = size.width / 7f
                    val bottom = size.height - 3f
                    drawLine(GulpPalette.Edge.copy(alpha = .65f), Offset(0f, bottom), Offset(size.width, bottom), 2f)
                    review.days.forEachIndexed { index, day ->
                        if (day.loggedMl > 0) {
                            val height = (day.loggedMl.toFloat() / max) * (size.height - 13f)
                            drawRoundRect(color = GulpPalette.Glacier,
                                topLeft = Offset(index * step + step * .20f, bottom - height),
                                size = Size(step * .60f, height), cornerRadius = CornerRadius(10f, 10f))
                        }
                    }
                }
                Row(Modifier.fillMaxWidth()) {
                    review.days.forEach { day ->
                        Text(day.date.format(DateTimeFormatter.ofPattern("EEE")), modifier = Modifier.weight(1f),
                            fontSize = 10.sp, color = GulpPalette.SoftWhite, textAlign = TextAlign.Center)
                    }
                }
                Note("No bar means no drinks were logged that day. It doesn't mean you didn't drink.")
            }
        }
        FrostPanel(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
                Text("Your records", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = GulpPalette.White)
                MetricRow("Logged fluids", if (recorded) "${review.totalMl} mL" else "—")
                MetricRow("Days with entries", "${review.recordedDays} of 7")
                MetricRow("Drinks logged", "${review.entries}")
                MetricRow("Most logged drink", review.mostLoggedDrink?.title ?: "—")
                if (referenceMl > 0) {
                    MetricRow("Your chosen daily reference", "$referenceMl mL")
                    Note("A comparison of recorded drinks only. Food contains water too; this isn't a health score.")
                }
                if (review.previousWeekTotalMl != null) {
                    val diff = review.totalMl - review.previousWeekTotalMl
                    MetricRow("Versus previous 7 days", "${if (diff >= 0) "+" else ""}$diff mL logged")
                    Note("Comparisons are of recorded amounts, not changes in your body's hydration.")
                } else Note("Log in another week to see a week-to-week comparison.")
            }
        }
        WaterAdaptiveText("This is a summary of your records, not a health assessment.", scene, viewport, fontSize = 12.sp)
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = GulpPalette.SoftWhite, modifier = Modifier.weight(1f))
        Text(value, fontSize = 14.sp, color = GulpPalette.Glacier, fontWeight = FontWeight.Bold)
    }
}
