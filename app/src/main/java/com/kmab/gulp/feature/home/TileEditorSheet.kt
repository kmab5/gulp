package com.kmab.gulp.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kmab.gulp.core.design.GulpChoiceChip
import com.kmab.gulp.core.design.GulpToggleRow
import com.kmab.gulp.core.design.GulpPalette
import com.kmab.gulp.core.domain.DrinkKind
import com.kmab.gulp.core.domain.DrinkRules
import com.kmab.gulp.core.domain.HomeTile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TileEditorSheet(
    tile: HomeTile?, onDismiss: () -> Unit,
    onSave: (DrinkKind, Int, Boolean) -> Unit,
    onRemove: (() -> Unit)? = null
) {
    var kind by remember(tile?.id) { mutableStateOf(tile?.kind ?: DrinkKind.WATER) }
    var amount by remember(tile?.id) { mutableStateOf((tile?.amountMl ?: 250).toString()) }
    var wide by remember(tile?.id) { mutableStateOf(tile?.wide ?: false) }
    val ml = amount.toIntOrNull()?.takeIf { it in DrinkRules.MIN_ENTRY_ML..DrinkRules.MAX_ENTRY_ML }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = GulpPalette.DeepBlue, contentColor = GulpPalette.White) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(17.dp)) {
            Text(if (tile == null) "New shortcut" else "Edit shortcut", fontSize = 25.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                DrinkKind.entries.forEach { option ->
                    GulpChoiceChip(label = option.title, selected = kind == option,
                        onClick = { kind = option })
                }
            }
            OutlinedTextField(value = amount, onValueChange = { value ->
                if (value.length <= 5 && value.all(Char::isDigit)) amount = value
            }, label = { Text("Amount (mL)") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = amount.isNotEmpty() && ml == null,
                modifier = Modifier.fillMaxWidth())
            GulpToggleRow("Wide tile", "Make this shortcut span two columns",
                checked = wide, onCheckedChange = { wide = it })
            Button(onClick = { ml?.let { onSave(kind, it, wide) } }, enabled = ml != null,
                colors = ButtonDefaults.buttonColors(containerColor = GulpPalette.Glacier, contentColor = GulpPalette.Ink),
                modifier = Modifier.fillMaxWidth()) { Text("Save shortcut", fontWeight = FontWeight.Bold) }
            if (onRemove != null) TextButton(onClick = onRemove, modifier = Modifier.fillMaxWidth()) {
                Text("Remove shortcut", color = GulpPalette.SoftWhite)
            }
        }
    }
}
