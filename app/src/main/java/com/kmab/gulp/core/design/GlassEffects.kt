package com.kmab.gulp.core.design

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.kmab.gulp.core.graphics.WaterMath
import com.kmab.gulp.core.graphics.WaterScene

/** Glass samples the *same* fluid state as the backdrop. No screenshot readback or texture assets. */
val LocalGlassScene = staticCompositionLocalOf<WaterScene?> { null }
val LocalGlassEnabled = staticCompositionLocalOf { true }
val LocalGlassViewport = staticCompositionLocalOf { IntSize.Zero }

/**
 * True GPU blur on Android 12+. We redraw the procedural water using screen-space
 * coordinates before blurring its own layer; blurring a translucent tint alone does nothing.
 * Android 9–11 get a matching translucent fallback because RenderEffect is unavailable.
 */
@Composable
fun GlassWaterLayer(modifier: Modifier = Modifier, blurDp: Int = 18) {
    val scene = LocalGlassScene.current
    val viewport = LocalGlassViewport.current
    var origin by remember { mutableStateOf(Offset.Zero) }
    Canvas(modifier.onGloballyPositioned { origin = it.positionInRoot() }
        .blur(blurDp.dp, edgeTreatment = BlurredEdgeTreatment.Rectangle)) {
        if (scene == null || viewport.width <= 0 || viewport.height <= 0) {
            drawRect(Brush.verticalGradient(listOf(Color(0xFF315B70), Color(0xFF14314A))))
            return@Canvas
        }
        val w = size.width
        val h = size.height
        val rootW = viewport.width.toFloat().coerceAtLeast(1f)
        val rootH = viewport.height.toFloat().coerceAtLeast(1f)
        // Match the unblurred fullscreen background in root/window coordinates.
        drawRect(Brush.verticalGradient(
            listOf(GulpPalette.DeepBlue, GulpPalette.Abyss),
            startY = -origin.y, endY = rootH - origin.y))
        val path = Path()
        val steps = (w / rootW * 64).toInt().coerceIn(12, 64)
        for (i in 0..steps) {
            val x = i * w / steps
            val rootX = (origin.x + x) / rootW
            val rootY = WaterMath.surfaceY(rootX, scene.fill, scene.tilt, scene.seconds,
                scene.impulses, !scene.reducedMotion) * rootH
            if (i == 0) path.moveTo(0f, rootY - origin.y)
            else path.lineTo(x, rootY - origin.y)
        }
        path.lineTo(w, h)
        path.lineTo(0f, h)
        path.close()
        drawPath(path, Brush.verticalGradient(listOf(GulpPalette.LiquidTop, GulpPalette.LiquidBottom),
            startY = rootH * (1f - WaterMath.visibleFill(scene.fill)) - origin.y,
            endY = rootH - origin.y))
    }
}

/** Only the supplied content determines size; decorative blurred drawing has no layout effect. */
@Composable
fun GlassSurface(modifier: Modifier = Modifier,
                 radius: Int = 21, tint: Float = .17f,
                 content: @Composable BoxScope.() -> Unit) {
    val shape = RoundedCornerShape(radius.dp)
    Box(modifier.clip(shape)) {
        if (LocalGlassEnabled.current) GlassWaterLayer(Modifier.matchParentSize())
        else Canvas(Modifier.matchParentSize()) {
            drawRect(GulpPalette.DeepBlue.copy(alpha = .75f))
        }
        Box(Modifier.matchParentSize().graphicsLayer { alpha = tint }) {
            Canvas(Modifier.matchParentSize()) {
                drawRect(Brush.verticalGradient(listOf(Color.White.copy(alpha=.77f),
                    Color(0xFF36708B), Color(0xFF173D58))))
            }
        }
        Canvas(Modifier.matchParentSize()) {
            val px = 1.dp.toPx()
            drawRoundRect(Brush.linearGradient(listOf(Color.White.copy(alpha=.57f),
                Color.White.copy(alpha=.08f), Color.White.copy(alpha=.27f))),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius.dp.toPx()),
                style = Stroke(px))
        }
        content()
    }
}

/**
 * Draw the scrolling content once into a reusable graphics layer. Draw it normally, then
 * re-draw blurred versions *only* in clipped 24dp boundary strips when scrolling is possible.
 * This is a real blur of the actual text/cards, not a translucent dark water overlay.
 * Available on Android 12+; on older devices preserve sharp content instead of a dark scrim.
 */
@Composable
fun Modifier.conditionalScrollBlur(
    canScrollBack: () -> Boolean,
    canScrollForward: () -> Boolean
): Modifier {
    val sharpLayer = androidx.compose.ui.graphics.rememberGraphicsLayer()
    val blurLayer = androidx.compose.ui.graphics.rememberGraphicsLayer()
    return this.drawWithContent {
        val above = canScrollBack()
        val below = canScrollForward()
        // Avoid GPU layer recording when the contents fit or RenderEffect is unsupported.
        if (android.os.Build.VERSION.SDK_INT < 31 || (!above && !below)) {
            drawContent()
        } else {
            sharpLayer.record { this@drawWithContent.drawContent() }
            drawLayer(sharpLayer)
            blurLayer.record { this@drawWithContent.drawContent() }
            blurLayer.renderEffect = androidx.compose.ui.graphics.BlurEffect(
                12.dp.toPx(), 12.dp.toPx(), androidx.compose.ui.graphics.TileMode.Decal)
            val edge = 28.dp.toPx().coerceAtMost(size.height / 2f)
            // Graduated, transparent strips: no opaque scrim and no hard blur boundary.
            val bands = floatArrayOf(1f, .69f, .37f)
            val alphas = floatArrayOf(.24f, .38f, .64f)
            for (i in bands.indices) {
                val extent = edge * bands[i]
                blurLayer.alpha = alphas[i]
                if (above) {
                    clipRect(0f, 0f, size.width, extent) { drawLayer(blurLayer) }
                }
                if (below) {
                    clipRect(0f, size.height - extent, size.width, size.height) {
                        drawLayer(blurLayer)
                    }
                }
            }
            blurLayer.alpha = 1f
        }
    }
}

/** Compatibility helper for the Home view; no permanent edge overlays. */
@Composable
fun BoxScope.GlassScrollEdges(scrollState: ScrollState, modifier: Modifier = Modifier) {
    // Intentionally empty: use conditionalScrollBlur on the scroll viewport itself.
}

/** All secondary tabs use the same actual-content blur, conditional on scroll boundaries. */
@Composable
fun GlassScrollableColumn(
    modifier: Modifier = Modifier,
    verticalArrangement: androidx.compose.foundation.layout.Arrangement.Vertical = androidx.compose.foundation.layout.Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    val scroll = androidx.compose.foundation.rememberScrollState()
    Box(modifier.conditionalScrollBlur({ scroll.canScrollBackward }, { scroll.canScrollForward })) {
        androidx.compose.foundation.layout.Column(
            Modifier.fillMaxSize().verticalScroll(scroll),
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment,
            content = content
        )
    }
}
