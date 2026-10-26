package com.kmab.gulp.feature.home

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kmab.gulp.core.design.GulpChoiceChip
import com.kmab.gulp.core.design.GulpPalette
import com.kmab.gulp.core.domain.DrinkKind
import com.kmab.gulp.core.domain.DrinkRecord
import com.kmab.gulp.core.domain.DrinkRules
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Create and edit share exactly the same validation and date/time controls. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrinkEditorSheet(
    existing: DrinkRecord?,
    initialKind: DrinkKind,
    initialAmount: Int,
    onClose: () -> Unit,
    onSave: (DrinkKind, Int, Long) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val zone = remember { ZoneId.systemDefault() }
    var type by remember(existing?.id) { mutableStateOf(existing?.kind ?: initialKind) }
    var text by remember(existing?.id) { mutableStateOf((existing?.amountMl ?: initialAmount).toString()) }
    var localTime by remember(existing?.id) {
        mutableStateOf(LocalDateTime.ofInstant(Instant.ofEpochMilli(existing?.timeMillis ?: System.currentTimeMillis()), zone))
    }
    val parsed = text.toIntOrNull()?.takeIf { it in DrinkRules.MIN_ENTRY_ML..DrinkRules.MAX_ENTRY_ML }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onClose, sheetState = sheetState,
        containerColor = androidx.compose.ui.graphics.Color(0xEE102F46),
        contentColor = GulpPalette.White) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 38.dp),
            verticalArrangement = Arrangement.spacedBy(17.dp)) {
            Text(if (existing == null) "Add a drink" else "Edit drink", fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                DrinkKind.entries.forEach { kind ->
                    GulpChoiceChip(label = kind.title, selected = type == kind,
                        onClick = { type = kind })
                }
            }
            OutlinedTextField(value = text, onValueChange = { value ->
                if (value.length <= 5 && value.all { it.isDigit() }) text = value
            }, label = { Text("Amount (mL)") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = text.isNotEmpty() && parsed == null,
                supportingText = { Text("Recorded volume, not a recommended amount") },
                modifier = Modifier.fillMaxWidth())
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = {
                    DatePickerDialog(context, android.R.style.Theme_Material_Dialog_Alert, { _, year, month, day ->
                        localTime = java.time.LocalDate.of(year, month + 1, day).atTime(localTime.toLocalTime())
                    }, localTime.year, localTime.monthValue - 1, localTime.dayOfMonth).show()
                }) { Text(localTime.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))) }
                TextButton(onClick = {
                    TimePickerDialog(context, android.R.style.Theme_Material_Dialog_Alert, { _, hour, minute ->
                        localTime = localTime.withHour(hour).withMinute(minute)
                    }, localTime.hour, localTime.minute, true).show()
                }) { Text(localTime.format(DateTimeFormatter.ofPattern("HH:mm"))) }
            }
            Button(onClick = {
                val ml = parsed ?: return@Button
                onSave(type, ml, localTime.atZone(zone).toInstant().toEpochMilli())
            }, enabled = parsed != null, modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = GulpPalette.Glacier, contentColor = GulpPalette.Ink)) {
                Text(if (existing == null) "Add drink" else "Save changes", fontWeight = FontWeight.Bold)
            }
            if (onDelete != null) {
                TextButton(onClick = onDelete, modifier = Modifier.fillMaxWidth()) {
                    Text("Delete this drink", color = GulpPalette.SoftWhite)
                }
            }
        }
    }
}
