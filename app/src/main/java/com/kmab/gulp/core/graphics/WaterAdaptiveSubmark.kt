package com.kmab.gulp.core.graphics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntSize
import com.kmab.gulp.core.design.GulpPalette
import kotlin.math.min

/**
 * The single-color Fluid Nucleus symbol.
 * The center is a real cutout, not a white-painted center, so it works on any background.
 * This same silhouette is supplied in the brand kit's SVG and Android themed icon.
 */
private val nucleusCutoutPath: Path = Path().apply {
    fillType = PathFillType.EvenOdd
    moveTo(122f, 29f)
    cubicTo(149f, 16f, 175f, 30f, 183f, 60f)
    cubicTo(191f, 85f, 184f, 98f, 198f, 116f)
    cubicTo(221f, 137f, 231f, 166f, 216f, 191f)
    cubicTo(199f, 221f, 170f, 230f, 148f, 212f)
    cubicTo(132f, 201f, 122f, 186f, 102f, 190f)
    cubicTo(74f, 198f, 45f, 194f, 32f, 170f)
    cubicTo(15f, 139f, 36f, 109f, 62f, 99f)
    cubicTo(89f, 88f, 100f, 50f, 122f, 29f)
    close()
    moveTo(144f, 80f)
    cubicTo(161f, 77f, 169f, 95f, 176f, 119f)
    cubicTo(181f, 133f, 194f, 152f, 191f, 165f)
    cubicTo(187f, 185f, 168f, 188f, 152f, 175f)
    cubicTo(138f, 164f, 127f, 151f, 112f, 156f)
    cubicTo(96f, 163f, 81f, 164f, 73f, 151f)
    cubicTo(60f, 130f, 76f, 120f, 99f, 116f)
    cubicTo(127f, 113f, 122f, 87f, 144f, 80f)
    close()
}

private fun DrawScope.drawNucleus(color: Color) {
    val extent = min(size.width, size.height)
    if (extent <= 0f) return
    val scale = extent / 256f
    withTransform({
        translate(left = (size.width - extent) / 2, top = (size.height - extent) / 2)
        scale(scaleX = scale, scaleY = scale, pivot = Offset.Zero)
    }) {
        drawPath(path = nucleusCutoutPath, color = color)
    }
}

/**
 * Uses the *same* waterBelowClip() draw modifier as WaterAdaptiveText.
 * No hit targets or pointer modifiers live in the clipped copy.
 */
@Composable
fun WaterAdaptiveSubmark(
    scene: WaterScene,
    viewport: IntSize,
    modifier: Modifier = Modifier,
) {
    val stronger = LocalStrongerContrast.current
    val above = if (stronger) Color.White else GulpPalette.White
    val below = if (stronger) Color(0xFF031120) else GulpPalette.Ink
    Box(modifier = modifier) {
        Canvas(Modifier.matchParentSize()) { drawNucleus(above) }
        Canvas(Modifier.matchParentSize().waterBelowClip(scene, viewport)
            .semantics { invisibleToUser() }) { drawNucleus(below) }
    }
}
