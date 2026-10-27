package com.kmab.gulp.core.design

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
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

/** Small, self-authored vector set. All glyphs inherit tint; no heavyweight icon dependency. */
@Composable
fun DrinkIcon(id: String, modifier: Modifier = Modifier, tint: Color = GulpPalette.Glacier) {
    Canvas(modifier.size(24.dp)) {
        val u = size.minDimension / 24f
        val center = Offset(12*u,12*u)
        when (id) {
            "water", "bottle" -> {
                val p = Path().apply {
                    moveTo(12*u,2*u); cubicTo(9*u,7*u,5*u,11*u,5*u,15*u)
                    cubicTo(5*u,24*u,19*u,24*u,19*u,15*u)
                    cubicTo(19*u,11*u,15*u,7*u,12*u,2*u); close()
                }
                drawPath(p,tint)
            }
            "coffee", "tea", "cup" -> {
                drawRoundRect(tint,topLeft=Offset(4*u,8*u),size=androidx.compose.ui.geometry.Size(14*u,11*u),
                    cornerRadius=androidx.compose.ui.geometry.CornerRadius(2*u))
                drawArc(tint,-95f,190f,false,topLeft=Offset(14*u,10*u),
                    size=androidx.compose.ui.geometry.Size(8*u,8*u),style=Stroke(2*u))
                if(id=="tea") drawLine(GulpPalette.Ink,Offset(7*u,13*u),Offset(14*u,13*u),u,StrokeCap.Round)
            }
            "milk", "glass", "juice" -> {
                val p = Path().apply { moveTo(6*u,4*u);lineTo(18*u,4*u);lineTo(16*u,21*u)
                    lineTo(8*u,21*u);close() }
                drawPath(p,tint,style=Stroke(2.1f*u))
                if(id=="juice") drawLine(tint,Offset(12*u,8*u),Offset(20*u,2*u),1.8f*u,StrokeCap.Round)
            }
            "leaf" -> {
                val p=Path().apply{moveTo(3*u,19*u);cubicTo(1*u,9*u,11*u,2*u,22*u,3*u)
                    cubicTo(21*u,15*u,13*u,23*u,3*u,19*u);close()}
                drawPath(p,tint,style=Stroke(2*u))
                drawLine(tint,Offset(4*u,20*u),Offset(17*u,8*u),1.5f*u)
            }
            "sparkle" -> {
                val p=Path().apply{ moveTo(12*u,1*u);lineTo(15*u,9*u);lineTo(23*u,12*u)
                    lineTo(15*u,15*u);lineTo(12*u,23*u);lineTo(9*u,15*u);lineTo(1*u,12*u)
                    lineTo(9*u,9*u);close() }
                drawPath(p,tint)
            }
            else -> { drawCircle(tint,9*u,center,style=Stroke(2.5f*u));drawCircle(tint,3*u,center) }
        }
    }
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
            Row(Modifier.clickable { onSelect(type) }
                .then(if(selected) Modifier else Modifier)
                .padding(horizontal=9.dp,vertical=10.dp),
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
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
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
        text={Column(verticalArrangement=Arrangement.spacedBy(9.dp)) {
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
