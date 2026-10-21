package com.kmab.gulp.core.graphics

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalContext

/** Sensor registration strictly follows foreground + effects-enabled lifecycle. No permission needed. */
@Composable
fun WaterTiltSensor(scene: WaterScene, active: Boolean, tiltEnabled: Boolean = true) {
    val context = LocalContext.current
    val view = LocalView.current
    DisposableEffect(context, view, scene, active, tiltEnabled, scene.reducedMotion) {
        if (!active || !tiltEnabled || scene.reducedMotion) {
            scene.setGravityX(0f)
            onDispose { }
        } else {
            val manager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
            val sensor = manager?.getDefaultSensor(Sensor.TYPE_GRAVITY)
                ?: manager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            if (sensor == null || manager == null) {
                scene.setGravityX(0f) // sensorless devices show an ordinary calm surface
                onDispose { }
            } else {
                val listener = object : SensorEventListener {
                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
                    override fun onSensorChanged(event: SensorEvent) {
                        val rawX = event.values[0]
                        val rawY = event.values[1]
                        val rotation = view.display?.rotation ?: Surface.ROTATION_0
                        val axis = when (rotation) {
                            Surface.ROTATION_90 -> -rawY
                            Surface.ROTATION_180 -> -rawX
                            Surface.ROTATION_270 -> rawY
                            else -> rawX
                        }
                        scene.setGravityX(axis)
                    }
                }
                manager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)
                onDispose {
                    manager.unregisterListener(listener)
                    scene.setGravityX(0f)
                }
            }
        }
    }
}
