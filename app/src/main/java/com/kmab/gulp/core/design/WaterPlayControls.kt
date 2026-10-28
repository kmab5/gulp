package com.kmab.gulp.core.design

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.kmab.gulp.core.graphics.WaterScene
import com.kmab.gulp.core.graphics.waterAboveClip
import com.kmab.gulp.core.graphics.waterBelowClip

/** Minimal purpose-drawn vector glyphs; both copies are clipped to the live waterline. */
enum class PlayControlGlyph { HIDE_HUD, SHOW_HUD, SETTINGS }

@Composable
fun WaterPlayControl(scene: WaterScene, viewport: IntSize, glyph: PlayControlGlyph,
                     label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier.size(48.dp)
        .clickable(role = Role.Button, onClickLabel = label, onClick = onClick),
        contentAlignment = Alignment.Center) {
        GlassSurface(Modifier.fillMaxSize(), radius = 17, tint = .11f) { }
        Box(Modifier.size(23.dp)) {
            Canvas(Modifier.fillMaxSize().waterAboveClip(scene, viewport)) {
                drawPlayGlyph(glyph, GulpPalette.White)
            }
            Canvas(Modifier.fillMaxSize().waterBelowClip(scene, viewport)
                .semantics { invisibleToUser() }) {
                drawPlayGlyph(glyph, GulpPalette.Ink)
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPlayGlyph(
    glyph: PlayControlGlyph, color: Color
) {
    val unit = size.minDimension / 24f
    val stroke = 1.9f * unit
    fun line(ax: Float, ay: Float, bx: Float, by: Float) =
        drawLine(color, Offset(ax*unit, ay*unit), Offset(bx*unit, by*unit),
            strokeWidth = stroke, cap = StrokeCap.Round)
    when (glyph) {
        PlayControlGlyph.HIDE_HUD, PlayControlGlyph.SHOW_HUD -> {
            drawRoundRect(color, topLeft = Offset(3f*unit, 4f*unit),
                size = androidx.compose.ui.geometry.Size(18f*unit, 16f*unit),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f*unit),
                style = Stroke(stroke))
            if (glyph == PlayControlGlyph.HIDE_HUD) {
                line(7f, 9f, 17f, 9f)
                line(7f, 13f, 14f, 13f)
                line(7f, 17f, 11f, 17f)
                line(3f, 21f, 21f, 3f)
            } else {
                line(7f, 9f, 17f, 9f)
                line(7f, 13f, 17f, 13f)
                line(7f, 17f, 14f, 17f)
            }
        }
        PlayControlGlyph.SETTINGS -> {
            line(4f, 7f, 20f, 7f)
            line(4f, 12f, 20f, 12f)
            line(4f, 17f, 20f, 17f)
            drawCircle(GulpPalette.DeepBlue, radius = 3.5f*unit, center = Offset(9f*unit, 7f*unit))
            drawCircle(color, radius = 2f*unit, center = Offset(9f*unit, 7f*unit), style=Stroke(stroke))
            drawCircle(GulpPalette.DeepBlue, radius = 3.5f*unit, center = Offset(15f*unit, 12f*unit))
            drawCircle(color, radius = 2f*unit, center = Offset(15f*unit, 12f*unit), style=Stroke(stroke))
            drawCircle(GulpPalette.DeepBlue, radius = 3.5f*unit, center = Offset(10f*unit, 17f*unit))
            drawCircle(color, radius = 2f*unit, center = Offset(10f*unit, 17f*unit), style=Stroke(stroke))
        }
    }
}
