package com.kmab.gulp.core.graphics

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.kmab.gulp.core.design.GulpPalette

/** Optional preference strengthens colors without changing which pixels are submerged. */
val LocalStrongerContrast = staticCompositionLocalOf { false }

/**
 * Paint light content first, then paint the IDENTICAL shape dark only where it is underwater.
 * The second visual copy is inaccessible to screen readers and has no gesture handlers.
 * The water and text sample the same surface function in full-viewport coordinates.
 */
@Composable
fun WaterAdaptiveText(
    text: String,
    scene: WaterScene,
    viewport: IntSize,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 14.sp,
    fontWeight: FontWeight? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    lineHeight: TextUnit = TextUnit.Unspecified,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    above: Color = GulpPalette.White,
    below: Color = GulpPalette.Ink
) {
    val isStrong = LocalStrongerContrast.current
    val lightColor = if (isStrong) Color.White else above
    val darkColor = if (isStrong) Color(0xFF031120) else below
    Box(modifier = modifier) {
        Text(text, color = lightColor, fontSize = fontSize, fontWeight = fontWeight,
            letterSpacing = letterSpacing, lineHeight = lineHeight,
            textAlign = textAlign, maxLines = maxLines,
            modifier = Modifier.waterAboveClip(scene, viewport))
        Text(text, color = darkColor, fontSize = fontSize, fontWeight = fontWeight,
            letterSpacing = letterSpacing, lineHeight = lineHeight,
            textAlign = textAlign, maxLines = maxLines,
            modifier = Modifier.matchParentSize().waterBelowClip(scene, viewport)
                .semantics { invisibleToUser() })
    }
}

/** Clip both colors to complementary waterline halves: no overpaint/white outlines. */
@Composable
fun Modifier.waterAboveClip(scene: WaterScene, viewport: IntSize): Modifier =
    waterSideClip(scene, viewport, below = false)

@Composable
fun Modifier.waterBelowClip(scene: WaterScene, viewport: IntSize): Modifier =
    waterSideClip(scene, viewport, below = true)

/** Apply only to noninteractive DRAWING copies; visual masks must not clip taps. */
@Composable
private fun Modifier.waterSideClip(scene: WaterScene, viewport: IntSize, below: Boolean): Modifier {
    var rootOrigin by remember { mutableStateOf(Offset.Zero) }
    return this.onGloballyPositioned { rootOrigin = it.positionInRoot() }
        .drawWithContent {
            val rootW = viewport.width.toFloat().coerceAtLeast(1f)
            val rootH = viewport.height.toFloat().coerceAtLeast(1f)
            val w = size.width
            val h = size.height
            if (w <= 0 || h <= 0) return@drawWithContent
            val path = Path()
            val steps = (w / rootW * 64f).toInt().coerceIn(8, 64)
            if (!below) {
                path.moveTo(0f, 0f)
                path.lineTo(w, 0f)
            }
            for (i in 0..steps) {
                val localX = if (below) w * i / steps else w * (steps - i) / steps
                val globalX = (rootOrigin.x + localX) / rootW
                val globalY = WaterMath.surfaceY(globalX, scene.fill, scene.tilt,
                    scene.seconds, scene.impulses, !scene.reducedMotion) * rootH
                val localY = globalY - rootOrigin.y
                if (i == 0 && below) path.moveTo(0f, localY)
                else path.lineTo(localX, localY)
            }
            if (below) {
                path.lineTo(w, h)
                path.lineTo(0f, h)
            }
            path.close()
            clipPath(path) { this@drawWithContent.drawContent() }
        }
}
