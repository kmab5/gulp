package com.kmab.gulp.core.navigation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kmab.gulp.core.design.GlassSurface
import com.kmab.gulp.core.graphics.WaterScene
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/** No backdrop or divider behind the row; only the current destination has glass. */
@Composable
fun GulpNav(current: GulpTab, scene: WaterScene, viewport: IntSize,
            onSelect: (GulpTab) -> Unit, modifier: Modifier = Modifier) {
    val index by animateFloatAsState(current.ordinal.toFloat(),
        animationSpec = if (scene.reducedMotion) snap() else spring(
            stiffness = Spring.StiffnessMediumLow, dampingRatio = .84f), label = "nav glide")
    var showLabels by remember { mutableStateOf(true) }
    LaunchedEffect(current) { showLabels = true; delay(1650); showLabels = false }
    val labelAlpha by animateFloatAsState(if (showLabels) 1f else 0f,
        animationSpec = if (scene.reducedMotion) snap() else tween(280), label = "nav reveal")
    val iconShift by animateDpAsState(if (showLabels) 0.dp else 12.dp,
        animationSpec = if (scene.reducedMotion) snap() else spring(), label = "nav icon center")
    val markerHeight by animateDpAsState(if (showLabels) 70.dp else 53.dp,
        animationSpec = if (scene.reducedMotion) snap() else spring(), label = "nav marker height")
    val markerShift by animateDpAsState(if (showLabels) 0.dp else 10.dp,
        animationSpec = if (scene.reducedMotion) snap() else spring(), label = "nav marker center")
    val density = LocalDensity.current
    BoxWithConstraints(modifier.fillMaxWidth().navigationBarsPadding().height(80.dp)
        .padding(horizontal = 7.dp, vertical = 3.dp)) {
        val slot = maxWidth / GulpTab.entries.size
        val markerWidth = slot - 4.dp
        val cellPx = constraints.maxWidth.toFloat() / GulpTab.entries.size
        val markerPx = with(density) { markerWidth.toPx() }
        GlassSurface(Modifier.offset {
                IntOffset((index * cellPx + (cellPx - markerPx) / 2f).roundToInt(),
                    with(density) { markerShift.roundToPx() })
            }.width(markerWidth).height(markerHeight), radius = 20, tint = .11f) {
            Spacer(Modifier.fillMaxSize())
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(0.dp)) {
            GulpTab.entries.forEach { tab ->
                val selectedTab = tab == current
                val phase = remember(tab) { Animatable(0f) }
                LaunchedEffect(current, scene.reducedMotion) {
                    phase.snapTo(0f)
                    if (selectedTab && !scene.reducedMotion)
                        phase.animateTo(1f, tween(700, easing = FastOutSlowInEasing))
                }
                Column(Modifier.weight(1f).height(74.dp)
                    .clickable(role = Role.Tab, onClickLabel = "Open ${tab.label}") { onSelect(tab) }
                    .semantics { contentDescription = tab.label; selected = selectedTab }
                    .padding(top = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(Modifier.size(28.dp).offset(y = iconShift), contentAlignment = Alignment.Center) {
                        // Draw once in solid white. Previous overlapping light/dark copies
                        // caused a pale raster halo on some OEM devices.
                        NavGlyph(tab, scene, phase.value, selectedTab, Color.White)
                    }
                    Box(Modifier.graphicsLayer { alpha = labelAlpha }, contentAlignment = Alignment.Center) {
                        Text(tab.label, color = Color.White, fontSize = 11.5.sp,
                            fontWeight = if (selectedTab) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1)
                    }
                }
            }
        }
    }
}

/** Stroke/solid variants, tiny one-shot icon gestures, and a shared low-amplitude water wobble. */
@Composable
private fun NavGlyph(tab: GulpTab, scene: WaterScene, activation: Float, filled: Boolean, tint: Color) {
    Canvas(Modifier.size(24.dp).graphicsLayer {
        // Read frame state in layer placement, not parent recomposition.
        translationY = if (scene.reducedMotion) 0f else sin(scene.seconds * 1.10f + tab.ordinal * 1.3f) * 1.15.dp.toPx()
    }) {
        val u = size.width / 24f
        val p: (Float, Float) -> Offset = { x, y -> Offset(x*u, y*u) }
        val stroke = if (filled) 2.6f*u else 1.85f*u
        when(tab) {
            GulpTab.HOME -> {
                val house = Path().apply {
                    moveTo(3*u,11*u); lineTo(12*u,4*u); lineTo(21*u,11*u)
                    lineTo(21*u,20*u); quadraticTo(21*u,21*u,20*u,21*u)
                    lineTo(4*u,21*u); quadraticTo(3*u,21*u,3*u,20*u); close()
                }
                drawPath(house, tint, style = if (filled) Fill else Stroke(stroke, cap = StrokeCap.Round))
            }
            GulpTab.REMINDERS -> rotate(sin(activation * Math.PI.toFloat() * 3f) * (1f-activation)*19f) {
                val bell = Path().apply {
                    moveTo(6*u,10*u)
                    cubicTo(6*u,3*u,18*u,3*u,18*u,10*u)
                    lineTo(18*u,15*u); lineTo(20*u,18*u)
                    lineTo(4*u,18*u); lineTo(6*u,15*u); close()
                }
                drawPath(bell, tint, style = if (filled) Fill else Stroke(stroke, cap = StrokeCap.Round))
                drawArc(tint, 0f, 180f, false, topLeft = p(10f,18f),
                    size = androidx.compose.ui.geometry.Size(4*u,4*u), style = Stroke(stroke))
            }
            GulpTab.HISTORY -> {
                drawCircle(tint, 9*u, p(12f,12f), style = if (filled) Stroke(3.2f*u) else Stroke(stroke))
                rotate(activation*300f, pivot=p(12f,12f)) {
                    drawLine(tint, p(12f,7f), p(12f,12f), stroke, StrokeCap.Round)
                    drawLine(tint, p(12f,12f), p(16f,14f), stroke, StrokeCap.Round)
                }
            }
            GulpTab.REVIEW -> {
                val bars = floatArrayOf(12f,6f,9f)
                for (i in 0..2) {
                    val bounce = sin(activation * Math.PI.toFloat() * 2 + i).toFloat() * (1f-activation)*3f
                    val left = (4f + i*6.5f)*u
                    val top = (bars[i] + bounce)*u
                    drawRoundRect(tint, topLeft=p(4f+i*6.5f,bars[i]+bounce),
                        size=androidx.compose.ui.geometry.Size(3.4f*u,(21f-bars[i]-bounce)*u),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.5f*u),
                        style = if (filled) Fill else Stroke(stroke*.82f))
                }
            }
            GulpTab.SETTINGS -> rotate(activation * 105f) {
                drawCircle(tint, 7.2f*u, p(12f,12f),
                    style=Stroke(if (filled) 3.7f*u else stroke))
                for (i in 0 until 8) {
                    val a = i * Math.PI / 4.0
                    val dx = cos(a).toFloat(); val dy = sin(a).toFloat()
                    drawLine(tint, p(12f + dx*8f,12f + dy*8f),
                        p(12f + dx*10.6f,12f + dy*10.6f),
                        if (filled) 3.4f*u else 2.6f*u, StrokeCap.Round)
                }
                drawCircle(tint, 2.8f*u, p(12f,12f), style = Stroke(stroke))
            }
        }
    }
}
