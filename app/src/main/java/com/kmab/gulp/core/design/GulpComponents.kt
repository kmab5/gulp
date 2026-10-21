package com.kmab.gulp.core.design

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Shared controls, deliberately independent from Material's default switch/chip colors. */
private val PanelShape = RoundedCornerShape(23.dp)

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(title, modifier, fontWeight = FontWeight.Bold, color = GulpPalette.White, fontSize = 23.sp)
}

@Composable
fun Note(text: String, modifier: Modifier = Modifier, color: Color = GulpPalette.SoftWhite) {
    Text(text, modifier, fontSize = 13.sp, color = color, lineHeight = 19.sp)
}

/** Layered translucency and specular borders. No heavyweight image asset or perpetual blur animation. */
@Composable
fun FrostPanel(
    modifier: Modifier = Modifier,
    padding: Dp = 18.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(
                Brush.linearGradient(listOf(
                    Color(0xFF447C93).copy(alpha = .50f),
                    Color(0xFF173E59).copy(alpha = .69f),
                    Color(0xFF0C3049).copy(alpha = .77f)
                )), PanelShape
            )
            .border(1.dp, Brush.linearGradient(listOf(
                Color.White.copy(alpha = .42f), GulpPalette.Glacier.copy(alpha = .13f),
                Color.White.copy(alpha = .23f)
            )), PanelShape)
            .padding(padding)
    ) { content() }
}

@Composable
fun GulpToggle(checked: Boolean, onCheckedChange: (Boolean) -> Unit,
               modifier: Modifier = Modifier, enabled: Boolean = true) {
    val progress by animateFloatAsState(if (checked) 1f else 0f,
        animationSpec = spring(dampingRatio = .78f, stiffness = 410f), label = "toggle travel")
    val tint by animateColorAsState(
        if (checked) Color(0xFF67E5DA) else Color(0xFF32576B), label = "toggle tint")
    Box(modifier.size(width = 60.dp, height = 36.dp)
        .toggleable(value = checked, enabled = enabled, role = Role.Switch,
            onValueChange = onCheckedChange), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(56.dp, 32.dp)) {
            val radius = size.height / 2f
            drawRoundRect(tint.copy(alpha = if (enabled) .96f else .35f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius))
            drawRoundRect(Color.White.copy(alpha = .29f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.2.dp.toPx()))
            val travel = size.width - size.height
            val x = radius + travel * progress
            drawCircle(Color(0xFF0C3046).copy(alpha = .22f),
                radius = radius * .80f, center = Offset(x, radius + 1.5.dp.toPx()))
            drawCircle(Color.White.copy(alpha = if (enabled) 1f else .55f),
                radius = radius * .73f, center = Offset(x, radius))
        }
    }
}

@Composable
fun GulpToggleRow(label: String, supporting: String? = null, checked: Boolean,
                  enabled: Boolean = true, onCheckedChange: (Boolean) -> Unit,
                  modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().heightIn(min = 66.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, fontWeight = FontWeight.SemiBold, color = GulpPalette.White, fontSize = 16.sp,
                lineHeight = 21.sp)
            if (supporting != null) Note(supporting)
        }
        GulpToggle(checked, onCheckedChange, enabled = enabled)
    }
}

@Composable
fun GulpChoiceChip(label: String, selected: Boolean, onClick: () -> Unit,
                   modifier: Modifier = Modifier, enabled: Boolean = true) {
    val fill by animateColorAsState(if (selected) GulpPalette.Glacier.copy(alpha = .95f)
        else Color(0xFF376179).copy(alpha = .76f), label = "choice fill")
    Box(modifier
        .heightIn(min = 44.dp)
        .background(fill, RoundedCornerShape(14.dp))
        .border(1.dp, if (selected) Color.White.copy(alpha = .9f)
            else Color.White.copy(alpha = .22f), RoundedCornerShape(14.dp))
        .clickable(enabled = enabled, onClick = onClick)
        .padding(horizontal = 12.dp, vertical = 11.dp),
        contentAlignment = Alignment.Center) {
        Text(label, fontWeight = FontWeight.SemiBold, fontSize = 12.sp,
            color = if (selected) GulpPalette.Ink else GulpPalette.White,
            textAlign = TextAlign.Center, maxLines = 1)
    }
}

@Composable
fun GulpChoiceRow(label: String, supporting: String? = null, selected: Boolean,
                  onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val bg by animateColorAsState(if (selected) Color(0xFF326D82).copy(alpha = .87f)
        else Color(0xFF0C293F).copy(alpha = .46f), label = "choice highlight")
    Row(modifier.fillMaxWidth().heightIn(min = 61.dp)
        .background(bg, RoundedCornerShape(17.dp))
        .border(1.dp, if (selected) GulpPalette.Glacier.copy(alpha = .8f)
            else Color.White.copy(alpha = .17f), RoundedCornerShape(17.dp))
        .selectable(selected = selected, enabled = enabled, role = Role.RadioButton, onClick = onClick)
        .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Canvas(Modifier.size(24.dp)) {
            drawCircle(if (selected) GulpPalette.Glacier else GulpPalette.Edge,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.8.dp.toPx()))
            if (selected) drawCircle(GulpPalette.Glacier, radius = size.minDimension * .28f)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, color = GulpPalette.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            if (supporting != null) Note(supporting, color = Color(0xFFCDDEE8))
        }
    }
}

@Composable
fun PrimaryAction(label: String, onClick: () -> Unit, modifier: Modifier = Modifier,
                  content: @Composable (() -> Unit)? = null) {
    Box(modifier.fillMaxWidth().heightIn(min = 62.dp)
        .background(Brush.linearGradient(listOf(GulpPalette.Glacier, Color(0xFF71D6DC))),
            RoundedCornerShape(21.dp))
        .border(1.dp, Color.White.copy(alpha = .67f), RoundedCornerShape(21.dp))
        .clickable(onClickLabel = label, onClick = onClick)
        .padding(horizontal = 18.dp, vertical = 14.dp), contentAlignment = Alignment.Center) {
        if (content != null) content() else {
            Text("+  $label", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = GulpPalette.Ink)
        }
    }
}

@Composable
fun PreferenceRow(label: String, supporting: String? = null, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 64.dp).clickable(onClickLabel = label, onClick = onClick)
        .padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(label, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = GulpPalette.White)
            if (supporting != null) Note(supporting)
        }
        Text("›", fontSize = 25.sp, color = GulpPalette.SoftWhite)
    }
}
