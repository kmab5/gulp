package com.kmab.gulp.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kmab.gulp.core.design.GulpChoiceChip
import com.kmab.gulp.core.design.GulpToggleRow
import com.kmab.gulp.core.design.GulpPalette
import com.kmab.gulp.core.domain.DrinkKind
import com.kmab.gulp.core.domain.DrinkType
import com.kmab.gulp.core.domain.DrinkTypeCatalog
import com.kmab.gulp.core.design.DrinkTypePicker
import com.kmab.gulp.core.design.NewDrinkTypeDialog
import com.kmab.gulp.core.design.GlassSurface
import androidx.compose.foundation.layout.Box
import com.kmab.gulp.core.domain.DrinkRules
import com.kmab.gulp.core.domain.HomeTile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TileEditorSheet(
    tile: HomeTile?, onDismiss: () -> Unit,
    onSave: (DrinkKind, Int, Boolean) -> Unit,
    onRemove: (() -> Unit)? = null,
    types: List<DrinkType> = DrinkTypeCatalog.builtins,
    onSaveTyped: ((DrinkType, Int, Boolean) -> Unit)? = null,
    onCreateDrinkType: ((String, String, Int) -> Unit)? = null
) {
    var typeId by remember(tile?.id) { mutableStateOf(tile?.drinkTypeId ?: "water") }
    var creatingType by remember { mutableStateOf(false) }
    val chosen = DrinkTypeCatalog.resolve(typeId, types)
    var amount by remember(tile?.id) { mutableStateOf((tile?.amountMl ?: 250).toString()) }
    var wide by remember(tile?.id) { mutableStateOf(tile?.wide ?: false) }
    val ml = amount.toIntOrNull()?.takeIf { it in DrinkRules.MIN_ENTRY_ML..DrinkRules.MAX_ENTRY_ML }
    val maxEditorHeight = (LocalConfiguration.current.screenHeightDp * .72f).dp
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
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
            .padding(horizontal = 22.dp).navigationBarsPadding().padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(17.dp)) {
            Text(if (tile == null) "New shortcut" else "Edit shortcut", fontSize = 25.sp, fontWeight = FontWeight.Bold)
            DrinkTypePicker(types, typeId, onSelect = { typeId = it.id })
            if (onCreateDrinkType != null) {
                TextButton(onClick = { creatingType = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (typeId == "other") "+ Name and save a new drink type" else "+ Create a drink type",
                        color = GulpPalette.Glacier)
                }
            }
            OutlinedTextField(value = amount, onValueChange = { value ->
                if (value.length <= 5 && value.all(Char::isDigit)) amount = value
            }, label = { Text("Amount (mL)") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                    focusedLabelColor = GulpPalette.Glacier, unfocusedLabelColor = Color.White,
                    focusedBorderColor = GulpPalette.Glacier, unfocusedBorderColor = Color.White.copy(alpha = .48f),
                    focusedSupportingTextColor = Color.White, unfocusedSupportingTextColor = Color.White.copy(alpha = .80f)),
                isError = amount.isNotEmpty() && ml == null,
                modifier = Modifier.fillMaxWidth())
            GulpToggleRow("Wide tile", "Make this shortcut span two columns",
                checked = wide, onCheckedChange = { wide = it })
            Button(onClick = { ml?.let {
                if (onSaveTyped != null) onSaveTyped(chosen, it, wide)
                else onSave(chosen.legacyKind, it, wide)
            } }, enabled = ml != null,
                colors = ButtonDefaults.buttonColors(containerColor = GulpPalette.Glacier, contentColor = GulpPalette.Ink),
                modifier = Modifier.fillMaxWidth()) { Text("Save shortcut", fontWeight = FontWeight.Bold) }
            if (onRemove != null) TextButton(onClick = onRemove, modifier = Modifier.fillMaxWidth()) {
                Text("Remove shortcut", color = GulpPalette.SoftWhite)
            }
        }
        }
        }
    }
    if (creatingType && onCreateDrinkType != null) NewDrinkTypeDialog(
        onDismiss = { creatingType = false }, onCreate = onCreateDrinkType)
}
