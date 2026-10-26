package com.kmab.gulp.feature.history

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.kmab.gulp.core.design.GlassScrollableColumn
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kmab.gulp.core.design.FrostPanel
import com.kmab.gulp.core.design.GulpPalette
import com.kmab.gulp.core.design.Note
import com.kmab.gulp.core.domain.DrinkRecord
import com.kmab.gulp.core.domain.DrinkRules
import com.kmab.gulp.core.graphics.WaterAdaptiveText
import com.kmab.gulp.core.graphics.WaterScene
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun HistoryScreen(
    scene: WaterScene, viewport: IntSize,
    records: List<DrinkRecord>, today: LocalDate,
    onEdit: (DrinkRecord) -> Unit
) {
    val zone = remember { ZoneId.systemDefault() }
    val context = LocalContext.current
    var selected by remember { mutableStateOf(today) }
    val entries = remember(records, selected, zone) { records.filter { DrinkRules.localDate(it, zone) == selected } }
    val total = remember(entries) { entries.sumOf { it.amountMl } }
    GlassScrollableColumn(Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 31.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)) {
        WaterAdaptiveText("History", scene, viewport, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        FrostPanel(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = { selected = selected.minusDays(1) }) {
                        Text("‹ Previous", color = GulpPalette.Glacier)
                    }
                    TextButton(onClick = { if (selected < today) selected = selected.plusDays(1) },
                        enabled = selected < today) { Text("Next ›", color = GulpPalette.Glacier) }
                }
                Text(selected.format(DateTimeFormatter.ofPattern("EEEE, MMM d")),
                    modifier = Modifier.fillMaxWidth().clickable {
                        DatePickerDialog(context, { _, year, month, day ->
                            selected = LocalDate.of(year, month + 1, day)
                        }, selected.year, selected.monthValue - 1, selected.dayOfMonth).apply {
                            datePicker.maxDate = System.currentTimeMillis()
                        }.show()
                    }, color = GulpPalette.White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Text("$total mL recorded", fontSize = 23.sp, fontWeight = FontWeight.Bold, color = GulpPalette.Glacier)
                Note("Tap the date to choose another day")
            }
        }
        if (entries.isEmpty()) {
            FrostPanel(Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("No drinks logged", color = GulpPalette.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Note("An empty day only means nothing was recorded.")
                }
            }
        } else {
            entries.forEach { drink ->
                FrostPanel(Modifier.fillMaxWidth().clickable(onClickLabel = "Edit ${drink.kind.title} entry") { onEdit(drink) },
                    padding = 16.dp) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(drink.kind.title, fontSize = 17.sp, color = GulpPalette.White, fontWeight = FontWeight.Bold)
                            Note(Instant.ofEpochMilli(drink.timeMillis).atZone(zone)
                                .format(DateTimeFormatter.ofPattern("HH:mm")))
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text("${drink.amountMl} mL", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = GulpPalette.Glacier)
                            Note("Tap to edit")
                        }
                    }
                }
            }
        }
        WaterAdaptiveText("Only logged drinks are shown here.", scene, viewport, fontSize = 12.sp)
    }
}
