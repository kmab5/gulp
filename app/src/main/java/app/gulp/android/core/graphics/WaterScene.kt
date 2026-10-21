package app.gulp.android.core.graphics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos

/** 
 * Presentation state only. Not the hydration database or a physiological hydration estimate.
 * Scalar state is observed in the DRAW pass; other UI doesn't recompose at 60 FPS.
 */
class WaterScene {
    var seconds by mutableFloatStateOf(0f)
        private set
    var fill by mutableFloatStateOf(0.47f)
        private set
    var targetFill by mutableFloatStateOf(0.47f)
        private set
    var tilt by mutableFloatStateOf(0f)
        private set
    var targetTilt by mutableFloatStateOf(0f)
        private set
    var reducedMotion by mutableStateOf(false)
        private set
    val impulses = mutableStateListOf<WaterImpulse>()

    fun setPreviewFill(value: Float) {
        targetFill = value.coerceIn(0f, 1f)
        if (reducedMotion) fill = targetFill
    }

    fun setReducedMotion(reduced: Boolean) {
        reducedMotion = reduced
        if (reduced) {
            impulses.clear()
            fill = targetFill
            tilt = 0f
        }
    }

    fun setGravityX(gravityX: Float) {
        targetTilt = WaterMath.normalizedTilt(gravityX)
    }

    fun disturb(x: Float, y: Float, power: Float = 0.45f) {
        if (reducedMotion) return
        if (impulses.size >= 12) impulses.removeAt(0)
        impulses.add(WaterImpulse(x.coerceIn(0f, 1f), y.coerceIn(0f, 1f), power.coerceIn(0f, 2f), seconds))
    }

    fun frame(deltaSeconds: Float) {
        val dt = deltaSeconds.coerceIn(0f, 0.05f)
        if (reducedMotion) return
        seconds += dt
        fill = WaterMath.easingStep(fill, targetFill, dt, 2.7f)
        tilt = WaterMath.easingStep(tilt, targetTilt, dt, 5.5f)
        impulses.removeAll { seconds - it.bornAt > 2.2f }
    }
}

@Composable
fun rememberWaterScene(active: Boolean): WaterScene {
    val scene = remember { WaterScene() }
    LaunchedEffect(scene, active, scene.reducedMotion) {
        if (!active || scene.reducedMotion) return@LaunchedEffect
        var previous: Long? = null
        while (true) {
            withFrameNanos { nanos ->
                val p = previous
                previous = nanos
                if (p != null) scene.frame((nanos - p).coerceAtLeast(0L) / 1_000_000_000f)
            }
        }
    }
    return scene
}
