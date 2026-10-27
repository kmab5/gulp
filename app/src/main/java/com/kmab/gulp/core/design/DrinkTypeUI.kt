package com.kmab.gulp.core.design

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.kmab.gulp.core.domain.DrinkType
import com.kmab.gulp.core.domain.DrinkTypeCatalog
import com.kmab.gulp.core.domain.DrinkRules
import com.kmab.gulp.R

/**
 * Lightweight Lucide (ISC) vectors, bundled locally. Tint remains dynamic and
 * works with the surrounding waterline color treatment; no font or runtime download.
 */
@Composable
fun DrinkIcon(id: String, modifier: Modifier = Modifier, tint: Color = GulpPalette.Glacier) {
    val iconId = when (id) {
        "coffee" -> R.drawable.ic_drink_coffee
        "tea" -> R.drawable.ic_drink_tea
        "bottle" -> R.drawable.ic_drink_bottle
        "glass" -> R.drawable.ic_drink_glass
        "milk" -> R.drawable.ic_drink_milk
        "juice" -> R.drawable.ic_drink_juice
        "sparkle" -> R.drawable.ic_drink_sparkle
        "cup" -> R.drawable.ic_drink_cup
        "leaf" -> R.drawable.ic_drink_leaf
        "other" -> R.drawable.ic_drink_other
        "cocktail" -> R.drawable.ic_drink_cocktail
        "beer" -> R.drawable.ic_drink_beer
        "wine" -> R.drawable.ic_drink_wine
        "iced" -> R.drawable.ic_drink_iced
        "heart" -> R.drawable.ic_drink_heart
        "sun" -> R.drawable.ic_drink_sun
        "moon" -> R.drawable.ic_drink_moon
        "hot" -> R.drawable.ic_drink_hot
        "smoothie" -> R.drawable.ic_drink_smoothie
        "flask" -> R.drawable.ic_drink_flask
        "soda" -> R.drawable.ic_drink_soda
        "lemon" -> R.drawable.ic_drink_lemon
        else -> R.drawable.ic_drink_water
    }
    androidx.compose.foundation.Image(
        painter = androidx.compose.ui.res.painterResource(iconId),
        contentDescription = null,
        modifier = modifier.size(24.dp),
        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(tint)
    )
}

/** Lightweight selection view shared by recording, shortcuts and widget configuration. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DrinkTypePicker(types: List<DrinkType>, selectedId: String, onSelect: (DrinkType)->Unit,
                    modifier: Modifier = Modifier) {
    FlowRow(modifier, horizontalArrangement=Arrangement.spacedBy(7.dp),
        verticalArrangement=Arrangement.spacedBy(7.dp), maxItemsInEachRow=3) {
        types.filterNot { it.archived }.forEach { type ->
            val selected=type.id==selectedId
            val shape = RoundedCornerShape(12.dp)
            Row(Modifier.background(if (selected) GulpPalette.Glacier.copy(alpha=.20f)
                    else GulpPalette.DeepBlue.copy(alpha=.48f), shape)
                .border(1.dp, if (selected) GulpPalette.Glacier.copy(alpha=.72f)
                    else Color.White.copy(alpha=.16f),shape)
                .clickable { onSelect(type) }
                .padding(horizontal=11.dp,vertical=12.dp),
                verticalAlignment=Alignment.CenterVertically,
                horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                DrinkIcon(type.iconId,Modifier.size(19.dp),
                    if(selected) GulpPalette.Glacier else GulpPalette.SoftWhite)
                Text(type.name,color=if(selected)GulpPalette.Glacier else GulpPalette.White)
            }
        }
    }
}

/** A shared creation/edit/archive dialog; editing never mutates logged snapshot names/icons. */
@Composable
fun DrinkTypeEditor(types: List<DrinkType>, onDismiss:()->Unit,
                    onSave:(DrinkType)->Unit, onCreate:(String,String,Int)->Unit,
                    onArchive:(String)->Unit) {
    var selected by remember { mutableStateOf<DrinkType?>(null) }
    var name by remember(selected) { mutableStateOf(selected?.name ?: "") }
    var icon by remember(selected) { mutableStateOf(selected?.iconId ?: "water") }
    var amount by remember(selected) { mutableStateOf((selected?.defaultAmountMl ?: 250).toString()) }
    val parsed = amount.toIntOrNull()?.takeIf { it in DrinkRules.MIN_ENTRY_ML..DrinkRules.MAX_ENTRY_ML }
    AlertDialog(onDismissRequest=onDismiss,containerColor=GulpPalette.DeepBlue,
        title={ Text("My drinks",color=GulpPalette.White,fontWeight=FontWeight.Bold) },
        text={
            Column(Modifier.fillMaxWidth()
                .heightIn(max=(LocalConfiguration.current.screenHeightDp*.52f).dp)
                .verticalScroll(rememberScrollState()),
                verticalArrangement=Arrangement.spacedBy(10.dp)) {
                Text("Choose a drink to edit or create a new one.",color=GulpPalette.SoftWhite)
                DrinkTypePicker(types.filterNot { it.archived },
                    selected?.id ?: "", onSelect={selected=it})
                TextButton(onClick={selected=null}) { Text("+ New drink") }
                OutlinedTextField(name,{ if(it.length<=32) name=it },
                    label={Text("Drink name")},singleLine=true)
                Text("Icon",color=GulpPalette.White)
                DrinkTypePicker(DrinkTypeCatalog.iconIds.map { DrinkType(it,it.replaceFirstChar(Char::uppercase),it,250) },
                    icon,onSelect={icon=it.iconId})
                OutlinedTextField(amount,{ if(it.length<=5 && it.all(Char::isDigit)) amount=it },
                    label={Text("Default amount (mL)")},singleLine=true,
                    keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number))
                if(selected!=null && selected?.id !in DrinkTypeCatalog.builtins.map { it.id })
                    TextButton(onClick={selected?.let { onArchive(it.id) };selected=null}) {
                    Text("Archive this drink",color=GulpPalette.SoftWhite)
                }
            }
        },
        confirmButton={ TextButton(enabled=name.trim().isNotEmpty() && parsed!=null,onClick={
            val ml=parsed ?: return@TextButton
            val item=selected
            if(item==null)onCreate(name,icon,ml)
            else onSave(item.copy(name=DrinkTypeCatalog.sanitizeName(name),iconId=icon,defaultAmountMl=ml))
            onDismiss()
        }) { Text("Save",color=GulpPalette.Glacier) } },
        dismissButton={TextButton(onClick=onDismiss){Text("Cancel",color=GulpPalette.White)}})
}

/** One shared launcher-widget preset; future rounds may add independent per-widget profiles. */
@Composable
fun WidgetDrinkEditor(types: List<DrinkType>, currentId: String, currentAmount: Int,
                      onDismiss:()->Unit, onSave:(String,Int)->Unit) {
    var typeId by remember { mutableStateOf(currentId) }
    var amount by remember { mutableStateOf(currentAmount.toString()) }
    val validAmount=amount.toIntOrNull()?.takeIf { it in 1..5000 }
    AlertDialog(onDismissRequest=onDismiss,containerColor=GulpPalette.DeepBlue,
        title={Text("Widget drink",color=GulpPalette.White)},
        text={Column(Modifier.fillMaxWidth()
            .heightIn(max=(LocalConfiguration.current.screenHeightDp*.50f).dp)
            .verticalScroll(rememberScrollState()),
            verticalArrangement=Arrangement.spacedBy(9.dp)) {
            Text("Choose the drink and amount used by the launcher widgets.",color=GulpPalette.SoftWhite)
            DrinkTypePicker(types,typeId,{typeId=it.id})
            OutlinedTextField(amount,{ if(it.length<=5 && it.all(Char::isDigit)) amount=it },
                label={Text("Amount (mL)")},
                keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number))
        }},
        confirmButton={ TextButton(enabled=validAmount!=null,onClick={
            validAmount?.let { onSave(typeId,it) };onDismiss()
        }) {Text("Save",color=GulpPalette.Glacier)} },
        dismissButton={TextButton(onClick=onDismiss){Text("Cancel",color=GulpPalette.White)}})
}
