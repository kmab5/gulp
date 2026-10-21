package app.gulp.android.core.graphics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import app.gulp.android.core.design.GulpPalette

/**
 * PHASE 1 ONLY: a static compositional placeholder for the approved water surface.
 * No gravity, actual tracking data, ripple simulation or live cross-water masking yet.
 * Replace this view with the dedicated liquid engine in Phase 2.
 */
@Composable
fun StaticWaterBackdrop(modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxSize()) {
        drawRect(Brush.verticalGradient(listOf(GulpPalette.DeepBlue, GulpPalette.Abyss), 0f, size.height))
        val y = size.height * 0.55f
        val surface = Path().apply {
            moveTo(0f, y)
            cubicTo(size.width * .25f, y - 12f, size.width * .54f, y + 12f, size.width, y - 5f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(surface, Brush.verticalGradient(listOf(GulpPalette.LiquidTop, GulpPalette.LiquidBottom), y, size.height))
        val glint = Path().apply {
            moveTo(0f, y)
            cubicTo(size.width * .25f, y - 12f, size.width * .54f, y + 12f, size.width, y - 5f)
        }
        drawPath(glint, GulpPalette.Glacier.copy(alpha = .55f), style = Stroke(width = 2f))
        drawLine(
            Brush.verticalGradient(listOf(GulpPalette.Edge.copy(alpha = .18f), GulpPalette.Edge.copy(alpha = .05f))),
            Offset(10f, 0f), Offset(10f, size.height), strokeWidth = 2f
        )
        drawLine(
            Brush.verticalGradient(listOf(GulpPalette.Edge.copy(alpha = .18f), GulpPalette.Edge.copy(alpha = .05f))),
            Offset(size.width - 10f, 0f), Offset(size.width - 10f, size.height), strokeWidth = 2f
        )
    }
}
