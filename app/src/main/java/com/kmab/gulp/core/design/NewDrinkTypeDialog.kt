package com.kmab.gulp.core.design

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kmab.gulp.core.domain.DrinkRules
import com.kmab.gulp.core.domain.DrinkType
import com.kmab.gulp.core.domain.DrinkTypeCatalog

/** Lightweight reusable custom-drink flow: explicit save, never a silent "Other" entry. */
@Composable
fun NewDrinkTypeDialog(onDismiss: () -> Unit, onCreate: (String, String, Int) -> Unit) {
    var name by remember { mutableStateOf("") }
    var icon by remember { mutableStateOf("glass") }
    var amount by remember { mutableStateOf("250") }
    val value = amount.toIntOrNull()?.takeIf { it in DrinkRules.MIN_ENTRY_ML..DrinkRules.MAX_ENTRY_ML }
    val valid = name.trim().isNotEmpty() && value != null
    AlertDialog(onDismissRequest = onDismiss, containerColor = GulpPalette.DeepBlue,
        title = { Text("New drink", color = GulpPalette.White) },
        text = {
            Column(Modifier.fillMaxWidth()
                .heightIn(max = (LocalConfiguration.current.screenHeightDp * .48f).dp)
                .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Give it a name, icon, and usual amount.", color = GulpPalette.SoftWhite)
                OutlinedTextField(name, onValueChange = { if (it.length <= 32) name = it },
                    label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Text("Choose an icon", color = GulpPalette.White)
                DrinkTypePicker(DrinkTypeCatalog.iconIds.map {
                    DrinkType(it, it.replaceFirstChar(Char::uppercase), it, 250)
                }, icon, onSelect = { icon = it.iconId })
                OutlinedTextField(amount, onValueChange = {
                    if (it.length <= 5 && it.all(Char::isDigit)) amount = it
                }, label = { Text("Usual amount (mL)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { TextButton(enabled = valid, onClick = {
            value?.let { onCreate(DrinkTypeCatalog.sanitizeName(name), icon, it) }
            onDismiss()
        }) { Text("Save drink", color = GulpPalette.Glacier) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = GulpPalette.White) } }
    )
}
