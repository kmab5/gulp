package com.kmab.gulp.core.graphics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.kmab.gulp.core.design.GulpPalette
import kotlin.math.max

/** CPU-light, shader-free fallback on all supported Android versions (API 28+). */
@Composable
fun WaterBackdrop(scene: WaterScene, modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxSize()) {
        val seconds = scene.seconds
        val fill = scene.fill
        val tilt = scene.tilt
        val pulses = scene.impulses
        val animated = !scene.reducedMotion
        val w = size.width
        val h = size.height
        if (w < 1f || h < 1f) return@Canvas

        drawRect(Brush.verticalGradient(listOf(GulpPalette.DeepBlue, GulpPalette.Abyss), 0f, h))
        val water = Path()
        val glint = Path()
        val count = 64 // good quality without constructing hundreds of segments
        for (i in 0..count) {
            val x = i.toFloat() / count
            val py = WaterMath.surfaceY(x, fill, tilt, seconds, pulses, animated) * h
            if (i == 0) {
                water.moveTo(0f, py)
                glint.moveTo(0f, py)
            } else {
                water.lineTo(x * w, py)
                glint.lineTo(x * w, py)
            }
        }
        water.lineTo(w, h)
        water.lineTo(0f, h)
        water.close()
        drawPath(water, Brush.verticalGradient(
            listOf(GulpPalette.LiquidTop, GulpPalette.LiquidBottom),
            startY = max(0f, h * (1f - WaterMath.visibleFill(fill)) - 24f),
            endY = h
        ))
        drawPath(glint, GulpPalette.Glacier.copy(alpha = 0.52f), style = Stroke(width = 1.8f, cap = StrokeCap.Round))

        // Broad highlights create the bottle impression without a heavyweight glass shader.
        drawLine(Brush.verticalGradient(listOf(GulpPalette.Edge.copy(alpha = .21f), GulpPalette.Edge.copy(alpha = .02f))),
            Offset(12f, 0f), Offset(12f, h), strokeWidth = 2.3f)
        drawLine(Brush.verticalGradient(listOf(GulpPalette.Edge.copy(alpha = .24f), GulpPalette.Edge.copy(alpha = .02f))),
            Offset(w - 12f, 0f), Offset(w - 12f, h), strokeWidth = 2.3f)
        drawRect(Brush.horizontalGradient(listOf(GulpPalette.White.copy(alpha = .05f), GulpPalette.White.copy(alpha = 0f), GulpPalette.Abyss.copy(alpha = .1f))))

        // Soft concentric touch traces, independent of whether the user touched water or air.
        if (animated) for (impulse in pulses) {
            val age = seconds - impulse.bornAt
            if (age in 0f..1.5f) {
                val radius = (28f + age * 155f) * density
                val alpha = (1f - age / 1.5f).coerceIn(0f, 1f) * .21f * impulse.power
                if (alpha > .002f) {
                    drawCircle(GulpPalette.Glacier.copy(alpha = alpha), radius,
                        center = Offset(impulse.x * w, impulse.y * h), style = Stroke(1.2f * density))
                    drawCircle(GulpPalette.Glacier.copy(alpha = alpha * .45f), radius * .70f,
                        center = Offset(impulse.x * w, impulse.y * h), style = Stroke(.9f * density))
                }
            }
        }
    }
}
