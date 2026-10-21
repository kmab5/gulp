package com.kmab.gulp.core.graphics

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin

/** Pure, allocation-free geometry shared by the background renderer and ink clipping. */
object WaterMath {
    const val MIN_VISUAL_FILL = 0.25f // decorative nav reserve, not a recorded drink
    private const val TAU = (2.0 * PI).toFloat()

    fun visibleFill(requested: Float): Float = requested.coerceIn(MIN_VISUAL_FILL, 0.96f)

    /** X/Y values are normalized to the full viewport, not a particular text widget. */
    fun surfaceY(x: Float, fill: Float, tilt: Float, seconds: Float, waves: List<WaterImpulse>, animate: Boolean): Float {
        val nx = x.coerceIn(0f, 1f)
        val baseline = 1f - visibleFill(fill)
        if (!animate) return baseline
        val lean = (nx - 0.5f) * tilt.coerceIn(-1f, 1f) * 0.32f
        val idle = sin(nx * TAU * 1.65f - seconds * 1.25f) * 0.0035f +
            sin(nx * TAU * 2.55f + seconds * 0.78f) * 0.0017f
        var impulseOffset = 0f
        for (wave in waves) {
            val age = seconds - wave.bornAt
            if (age < 0f || age > 2.2f) continue
            val distance = abs(nx - wave.x)
            val reach = 0.045f + age * 0.36f
            val envelope = exp((-distance * distance / (reach * reach)).toDouble()).toFloat()
            val frequency = (distance * 5.6f - age * 2.9f) * TAU
            impulseOffset += sin(frequency) * envelope * exp((-age * 1.7f).toDouble()).toFloat() * wave.power * 0.019f
        }
        // Keep even strong disturbances inside the display; physically simulated, visually bounded.
        return (baseline + lean + idle + impulseOffset).coerceIn(0.025f, 0.98f)
    }

    fun easingStep(current: Float, target: Float, dt: Float, response: Float): Float {
        val t = dt.coerceIn(0f, 0.05f)
        return current + (target - current) * (1f - exp((-response * t).toDouble()).toFloat())
    }

    /** Orientation x axis is measured in m/s² for a standard Android gravity sensor. */
    fun normalizedTilt(gravityX: Float): Float = (gravityX / 9.81f).coerceIn(-1f, 1f)
}

data class WaterImpulse(val x: Float, val y: Float, val power: Float, val bornAt: Float)
