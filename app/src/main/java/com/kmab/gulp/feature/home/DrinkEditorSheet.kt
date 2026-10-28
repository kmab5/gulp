package com.kmab.gulp.feature.home

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.ColorFilter
import com.kmab.gulp.R
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kmab.gulp.core.design.GulpChoiceChip
import com.kmab.gulp.core.design.GulpPalette
import com.kmab.gulp.core.domain.DrinkKind
import com.kmab.gulp.core.domain.DrinkType
import com.kmab.gulp.core.domain.DrinkTypeCatalog
import com.kmab.gulp.core.design.DrinkTypePicker
import com.kmab.gulp.core.design.NewDrinkTypeDialog
import com.kmab.gulp.core.design.GlassSurface
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
    onDelete: (() -> Unit)? = null,
    types: List<DrinkType> = DrinkTypeCatalog.builtins,
    onSaveTyped: ((DrinkType, Int, Long) -> Unit)? = null,
    onCreateDrinkType: ((String, String, Int) -> Unit)? = null
) {
    val context = LocalContext.current
    var creatingType by remember { mutableStateOf(false) }
    val zone = remember { ZoneId.systemDefault() }
    var selectedTypeId by remember(existing?.id) {
        mutableStateOf(existing?.drinkTypeId ?: initialKind.name.lowercase())
    }
    val selectedType = DrinkTypeCatalog.resolve(selectedTypeId, types)
    var text by remember(existing?.id) { mutableStateOf((existing?.amountMl ?: initialAmount).toString()) }
    var localTime by remember(existing?.id) {
        mutableStateOf(LocalDateTime.ofInstant(Instant.ofEpochMilli(existing?.timeMillis ?: System.currentTimeMillis()), zone))
    }
    val parsed = text.toIntOrNull()?.takeIf { it in DrinkRules.MIN_ENTRY_ML..DrinkRules.MAX_ENTRY_ML }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val maxEditorHeight = (LocalConfiguration.current.screenHeightDp * .72f).dp
    ModalBottomSheet(onDismissRequest = onClose, sheetState = sheetState,
        containerColor = Color.Transparent,
        dragHandle = null,
        contentColor = GulpPalette.White) {
        GlassSurface(Modifier.fillMaxWidth(), radius = 28, tint = .20f) {
        Column(Modifier.fillMaxWidth()) {
            // The drag handle shares the *same* frosted pane as the rest of the editor.
            Box(Modifier.fillMaxWidth().padding(top = 17.dp, bottom = 22.dp),
                contentAlignment = Alignment.Center) {
                Box(Modifier.size(width = 46.dp, height = 5.dp)
                    .background(GulpPalette.SoftWhite.copy(alpha = .85f), RoundedCornerShape(50)))
            }
        Column(Modifier.fillMaxWidth().heightIn(max = maxEditorHeight)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp).navigationBarsPadding().padding(bottom = 38.dp),
            verticalArrangement = Arrangement.spacedBy(17.dp)) {
            Text(if (existing == null) "Add a drink" else "Edit drink", fontSize = 26.sp, fontWeight = FontWeight.Bold)
            DrinkTypePicker(types, selectedTypeId,
                onSelect = { selectedTypeId = it.id })
            if (onCreateDrinkType != null) TextButton(onClick = { creatingType = true },
                modifier = Modifier.fillMaxWidth()) {
                Text(if (selectedTypeId == "other") "+ Name and save a new drink type"
                    else "+ Create a drink type", color = GulpPalette.Glacier)
            }
            OutlinedTextField(value = text, onValueChange = { value ->
                if (value.length <= 5 && value.all { it.isDigit() }) text = value
            }, label = { Text("Amount (mL)") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                    focusedLabelColor = GulpPalette.Glacier, unfocusedLabelColor = Color.White,
                    focusedBorderColor = GulpPalette.Glacier, unfocusedBorderColor = Color.White.copy(alpha = .48f),
                    focusedSupportingTextColor = Color.White, unfocusedSupportingTextColor = Color.White.copy(alpha = .80f)),
                isError = text.isNotEmpty() && parsed == null,
                supportingText = { Text("Recorded volume, not a recommended amount") },
                modifier = Modifier.fillMaxWidth())
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically) {
                GlassSurface(Modifier.weight(1f), radius = 15, tint = .30f) {
                TextButton(onClick = {
                    DatePickerDialog(context, android.R.style.Theme_Material_Dialog_Alert, { _, year, month, day ->
                        localTime = java.time.LocalDate.of(year, month + 1, day).atTime(localTime.toLocalTime())
                    }, localTime.year, localTime.monthValue - 1, localTime.dayOfMonth).show()
                }, modifier = Modifier.fillMaxWidth()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.foundation.Image(painterResource(R.drawable.ic_calendar_days),
                            null, Modifier.size(23.dp), colorFilter = ColorFilter.tint(Color.White))
                        Text(localTime.format(DateTimeFormatter.ofPattern("MMM d, yyyy")),
                            color = Color.White, fontSize = 13.sp, maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    }
                }
                }
                GlassSurface(Modifier.weight(1f), radius = 15, tint = .30f) {
                TextButton(onClick = {
                    TimePickerDialog(context, android.R.style.Theme_Material_Dialog_Alert, { _, hour, minute ->
                        localTime = localTime.withHour(hour).withMinute(minute)
                    }, localTime.hour, localTime.minute, true).show()
                }, modifier = Modifier.fillMaxWidth()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.foundation.Image(painterResource(R.drawable.ic_clock),
                            null, Modifier.size(23.dp), colorFilter = ColorFilter.tint(Color.White))
                        Text(localTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                            color = Color.White, fontSize = 14.sp)
                    }
                }
                }
            }
            Button(onClick = {
                val ml = parsed ?: return@Button
                val timestamp = localTime.atZone(zone).toInstant().toEpochMilli()
                if (onSaveTyped != null) onSaveTyped(selectedType, ml, timestamp)
                else onSave(selectedType.legacyKind, ml, timestamp)
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
    }
    if (creatingType && onCreateDrinkType != null) NewDrinkTypeDialog(
        onDismiss = { creatingType = false }, onCreate = onCreateDrinkType)
}
