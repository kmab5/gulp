package app.gulp.android.core.navigation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.snap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.gulp.android.core.design.GulpPalette
import app.gulp.android.core.graphics.WaterAdaptiveText
import app.gulp.android.core.graphics.WaterScene
import app.gulp.android.core.graphics.waterBelowClip
import kotlin.math.roundToInt

/** One continuous viewport: the navbar has NO enclosing island, scrim, or bar fill. */
@Composable
fun GulpNav(
    current: GulpTab,
    scene: WaterScene,
    viewport: IntSize,
    onSelect: (GulpTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val index = animateFloatAsState(
        targetValue = current.ordinal.toFloat(),
        animationSpec = if (scene.reducedMotion) snap() else spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = 0.88f),
        label = "selected nav position"
    )
    val density = LocalDensity.current
    BoxWithConstraints(modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 8.dp, vertical = 6.dp)) {
        val widthPx = constraints.maxWidth.toFloat()
        val markerPx = with(density) { 46.dp.toPx() }
        val markerX = ((index.value + .5f) * widthPx / GulpTab.entries.size - markerPx / 2f).roundToInt()
        Box(
            Modifier.offset { IntOffset(markerX, 4.dp.roundToPx()) }
                .size(46.dp, 39.dp)
                .background(
                    Brush.verticalGradient(listOf(GulpPalette.Glacier.copy(alpha = .27f), GulpPalette.Edge.copy(alpha = .13f))),
                    RoundedCornerShape(13.dp)
                )
                .border(1.dp, Color.White.copy(alpha = .34f), RoundedCornerShape(13.dp))
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Top) {
            GulpTab.entries.forEach { tab ->
                Column(
                    modifier = Modifier.weight(1f)
                        .clickable(role = Role.Tab, onClickLabel = "Open ${tab.label}") { onSelect(tab) }
                        .semantics {
                            contentDescription = tab.label
                            selected = current == tab
                        }
                        .padding(top = 7.dp, bottom = 3.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(29.dp)) {
                        NavGlyph(tab, GulpPalette.White)
                        Box(Modifier.waterBelowClip(scene, viewport).semantics { invisibleToUser() }) {
                            NavGlyph(tab, GulpPalette.Ink)
                        }
                    }
                    WaterAdaptiveText(tab.label, scene, viewport, fontSize = 10.sp,
                        fontWeight = if (current == tab) FontWeight.Bold else FontWeight.Medium)
                }
            }
        }
    }
}

/** Tiny procedural icons rather than a dependency on a third-party icon bundle. */
@Composable
private fun NavGlyph(tab: GulpTab, color: Color) {
    Canvas(Modifier.size(22.dp)) {
        val s = size.width / 24f
        val p: (Float, Float) -> Offset = { x, y -> Offset(x * s, y * s) }
        val stroke = 1.8f * s
        when (tab) {
            GulpTab.HOME -> {
                val roof = Path().apply {
                    moveTo(3*s, 11*s); lineTo(12*s, 4*s); lineTo(21*s, 11*s)
                    lineTo(21*s, 21*s); lineTo(3*s, 21*s); close()
                }
                drawPath(roof, color, style = Stroke(stroke, cap = StrokeCap.Round))
                drawLine(color, p(10f, 21f), p(10f, 14f), stroke, StrokeCap.Round)
                drawLine(color, p(14f, 14f), p(14f, 21f), stroke, StrokeCap.Round)
            }
            GulpTab.REMINDERS -> {
                val bell = Path().apply {
                    moveTo(6*s, 10*s)
                    cubicTo(6*s, 2*s, 18*s, 2*s, 18*s, 10*s)
                    lineTo(18*s, 15*s); lineTo(20*s, 18*s); lineTo(4*s, 18*s)
                    lineTo(6*s, 15*s); close()
                }
                drawPath(bell, color, style = Stroke(stroke, cap = StrokeCap.Round))
                drawArc(color, 0f, 180f, false, topLeft = p(10f, 18f),
                    size = androidx.compose.ui.geometry.Size(4*s, 4*s), style = Stroke(stroke))
            }
            GulpTab.HISTORY -> {
                drawCircle(color, 9f*s, p(12f, 12f), style = Stroke(stroke))
                drawLine(color, p(12f, 7f), p(12f, 12f), stroke, StrokeCap.Round)
                drawLine(color, p(12f, 12f), p(16f, 14f), stroke, StrokeCap.Round)
            }
            GulpTab.REVIEW -> {
                drawLine(color, p(4f, 20f), p(20f, 20f), stroke, StrokeCap.Round)
                drawLine(color, p(6f, 20f), p(6f, 12f), 3*s, StrokeCap.Round)
                drawLine(color, p(12f, 20f), p(12f, 5f), 3*s, StrokeCap.Round)
                drawLine(color, p(18f, 20f), p(18f, 9f), 3*s, StrokeCap.Round)
            }
            GulpTab.SETTINGS -> {
                drawCircle(color, 5*s, p(12f, 12f), style = Stroke(stroke))
                for (a in 0 until 8) {
                    val angle = a * Math.PI / 4.0
                    val dx = kotlin.math.cos(angle).toFloat()
                    val dy = kotlin.math.sin(angle).toFloat()
                    drawLine(color, p(12f + dx*8, 12f + dy*8),
                        p(12f + dx*10, 12f + dy*10), stroke, StrokeCap.Round)
                }
            }
        }
    }
}
