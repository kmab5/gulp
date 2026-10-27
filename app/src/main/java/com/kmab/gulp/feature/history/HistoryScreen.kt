package com.kmab.gulp.feature.history

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kmab.gulp.core.design.FrostPanel
import com.kmab.gulp.core.design.GlassSurface
import com.kmab.gulp.core.design.GlassScrollableColumn
import com.kmab.gulp.core.design.GulpPalette
import com.kmab.gulp.core.design.GulpWaterText as Text
import com.kmab.gulp.core.design.Note
import com.kmab.gulp.core.design.DrinkIcon
import com.kmab.gulp.core.domain.DrinkRecord
import com.kmab.gulp.core.domain.DrinkRules
import com.kmab.gulp.core.graphics.WaterAdaptiveText
import com.kmab.gulp.core.graphics.WaterScene
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Calendar navigation is reachable, large enough to touch, and bounded at today. */
@Composable
fun HistoryScreen(
    scene: WaterScene, viewport: IntSize,
    records: List<DrinkRecord>, today: LocalDate,
    onEdit: (DrinkRecord) -> Unit
) {
    val zone = remember { ZoneId.systemDefault() }
    val context = LocalContext.current
    var selected by remember { mutableStateOf(today) }
    val entries = remember(records, selected, zone) {
        records.filter { DrinkRules.localDate(it, zone) == selected }.sortedByDescending { it.timeMillis }
    }
    val total = remember(entries) { entries.sumOf { it.amountMl } }
    fun showCalendar() {
        DatePickerDialog(context, android.R.style.Theme_Material_Dialog_Alert,
            { _, year, month, day ->
                val proposed = LocalDate.of(year, month + 1, day)
                if (!proposed.isAfter(today)) selected = proposed
            }, selected.year, selected.monthValue - 1, selected.dayOfMonth).apply {
                datePicker.maxDate = today.atStartOfDay(zone).toInstant().toEpochMilli()
            }.show()
    }
    GlassScrollableColumn(Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 31.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)) {
        WaterAdaptiveText("History", scene, viewport, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        FrostPanel(Modifier.fillMaxWidth(), padding = 16.dp) {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(15.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlassSurface(Modifier.sizeIn(minWidth = 46.dp, minHeight = 48.dp), radius = 16,
                        tint = .26f) {
                        TextButton(onClick = { selected = selected.minusDays(1) }) {
                            Text("‹", color = GulpPalette.White, fontSize = 26.sp)
                        }
                    }
                    GlassSurface(Modifier.weight(1f), radius = 16, tint = .27f) {
                        TextButton(onClick = ::showCalendar, modifier = Modifier.fillMaxWidth()) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("▦  ${selected.format(DateTimeFormatter.ofPattern("EEE, MMM d"))}",
                                    color = GulpPalette.White, fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp, textAlign = TextAlign.Center)
                                Text(if (selected == today) "Today · tap to change" else "Tap to choose a date",
                                    color = GulpPalette.SoftWhite, fontSize = 11.sp)
                            }
                        }
                    }
                    GlassSurface(Modifier.sizeIn(minWidth = 46.dp, minHeight = 48.dp), radius = 16,
                        tint = .26f) {
                        TextButton(onClick = { if (selected.isBefore(today)) selected = selected.plusDays(1) },
                            enabled = selected.isBefore(today)) {
                            Text("›", color = if (selected.isBefore(today)) GulpPalette.White else
                                GulpPalette.SoftWhite.copy(alpha = .4f), fontSize = 26.sp)
                        }
                    }
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("$total mL", fontSize = 27.sp, fontWeight = FontWeight.Bold,
                        color = GulpPalette.Glacier)
                    Text("recorded", fontSize = 13.sp, color = GulpPalette.SoftWhite,
                        modifier = Modifier.padding(bottom = 5.dp))
                }
            }
        }
        if (entries.isEmpty()) {
            FrostPanel(Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("No drinks recorded", fontSize = 17.sp, fontWeight = FontWeight.Bold,
                        color = GulpPalette.White)
                    Note("Nothing was logged on this day. That doesn't mean you didn't drink.")
                }
            }
        } else {
            entries.forEach { drink ->
                FrostPanel(Modifier.fillMaxWidth().clickable(
                    onClickLabel = "Edit ${drink.drinkName} entry", onClick = { onEdit(drink) }),
                    padding = 16.dp) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DrinkIcon(drink.drinkIcon, tint = GulpPalette.Glacier)
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(drink.drinkName, fontSize = 17.sp, color = GulpPalette.White,
                                fontWeight = FontWeight.Bold)
                            Note(Instant.ofEpochMilli(drink.timeMillis).atZone(zone)
                                .format(DateTimeFormatter.ofPattern("HH:mm")))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${drink.amountMl} mL", fontSize = 18.sp,
                                fontWeight = FontWeight.Bold, color = GulpPalette.Glacier)
                            Text("Edit ›", color = GulpPalette.SoftWhite, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
        WaterAdaptiveText("Only recorded drinks are shown here.", scene, viewport, fontSize = 12.sp)
    }
}
