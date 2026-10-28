package com.kmab.gulp.core.graphics

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
    var fill by mutableFloatStateOf(0f)
        private set
    var targetFill by mutableFloatStateOf(0f)
        private set
    var tilt by mutableFloatStateOf(0f)
        private set
    var targetTilt by mutableFloatStateOf(0f)
        private set
    var reducedMotion by mutableStateOf(false)
        private set
    var motionIntensity by mutableFloatStateOf(1f)
        private set
    val impulses = mutableStateListOf<WaterImpulse>()

    fun updateMotionIntensity(value: Float) {
        motionIntensity = value.coerceIn(.25f, 1.5f)
    }

    fun setPreviewFill(value: Float) {
        targetFill = value.coerceIn(0f, 1f)
        if (reducedMotion) fill = targetFill
    }

    fun updateReducedMotion(reduced: Boolean) {
        reducedMotion = reduced
        if (reduced) {
            impulses.clear()
            fill = targetFill
            tilt = 0f
        }
    }

    fun setGravityX(gravityX: Float) {
        targetTilt = WaterMath.normalizedTilt(gravityX) * motionIntensity
    }

    /** Transient inertia from a horizontal page swipe, opposite its travel direction. */
    fun pushFromSwipe(horizontal: Float, y: Float = .52f) {
        if (reducedMotion || horizontal == 0f) return
        val side = if (horizontal < 0f) -1f else 1f
        tilt = (tilt + side * .68f * motionIntensity).coerceIn(-1f, 1f)
        disturb(if (horizontal < 0f) .88f else .12f, y, 1.75f)
    }

    /** Direct manipulation in play mode; transient and never changes recorded volume. */
    fun playDrag(dxFraction: Float, x: Float, y: Float, sensitivity: Float) {
        if (reducedMotion) return
        val strength = sensitivity.coerceIn(.5f, 1.5f)
        tilt = (tilt + dxFraction.coerceIn(-.2f, .2f) * 3.5f * strength)
            .coerceIn(-1f, 1f)
        disturb(x, y, 1.15f * strength)
    }

    fun disturb(x: Float, y: Float, power: Float = 0.45f) {
        if (reducedMotion) return
        if (impulses.size >= 12) impulses.removeAt(0)
        impulses.add(WaterImpulse(x.coerceIn(0f, 1f), y.coerceIn(0f, 1f), (power * motionIntensity).coerceIn(0f, 2f), seconds))
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
