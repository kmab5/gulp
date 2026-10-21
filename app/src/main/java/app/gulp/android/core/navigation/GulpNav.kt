package app.gulp.android.core.navigation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.gulp.android.core.design.GulpPalette

/** No nav island or enclosing translucent plate. Only the active icon has a platform. */
@Composable
fun GulpNav(current: GulpTab, onSelect: (GulpTab) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().navigationBarsPadding().padding(start = 9.dp, end = 9.dp, bottom = 7.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        GulpTab.entries.forEach { tab ->
            val isCurrent = current == tab
            Column(
                modifier = Modifier.weight(1f)
                    .clickable(role = Role.Tab, onClickLabel = "Open ${tab.label}") { onSelect(tab) }
                    .semantics {
                        contentDescription = tab.label
                        selected = isCurrent
                    }
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    Modifier.size(width = 46.dp, height = 38.dp)
                        .then(
                            if (isCurrent) Modifier
                                .background(GulpPalette.Glacier.copy(alpha = .79f), RoundedCornerShape(12.dp))
                                .border(1.dp, Color.White.copy(alpha = .66f), RoundedCornerShape(12.dp))
                            else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    NavGlyph(tab = tab, color = if (isCurrent) GulpPalette.Ink else GulpPalette.Ink.copy(alpha = .83f))
                }
                Text(
                    tab.label,
                    color = if (isCurrent) GulpPalette.Ink else GulpPalette.Ink.copy(alpha = .86f),
                    fontSize = 10.sp,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

/** Compact hand-drawn icons: avoids a large icon-pack dependency. */
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
                drawArc(color, 0f, 180f, false, topLeft = p(10f, 18f), size = androidx.compose.ui.geometry.Size(4*s, 4*s), style = Stroke(stroke))
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
                    drawLine(color, p(12f + dx*8, 12f + dy*8), p(12f + dx*10, 12f + dy*10), stroke, StrokeCap.Round)
                }
            }
        }
    }
}
