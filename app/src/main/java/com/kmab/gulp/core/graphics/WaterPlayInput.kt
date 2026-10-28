package com.kmab.gulp.core.graphics

import android.os.SystemClock
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

/** Non-consuming observer: the entire screen becomes a water surface in HUD-free mode. */
fun Modifier.waterPlayTouchFeedback(scene: WaterScene, enabled: Boolean, sensitivity: Float): Modifier =
    pointerInput(scene, enabled, sensitivity) {
        if (!enabled) return@pointerInput
        awaitPointerEventScope {
            var previous: Offset? = null
            var previousId: androidx.compose.ui.input.pointer.PointerId? = null
            var lastEmitted = 0L
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                val pointer = event.changes.firstOrNull { it.pressed }
                if (pointer == null) {
                    previous = null
                    previousId = null
                    continue
                }
                val x = (pointer.position.x / size.width.coerceAtLeast(1)).coerceIn(0f, 1f)
                val y = (pointer.position.y / size.height.coerceAtLeast(1)).coerceIn(0f, 1f)
                if (previousId != pointer.id || !pointer.previousPressed) {
                    previousId = pointer.id
                    previous = pointer.position
                    lastEmitted = SystemClock.uptimeMillis()
                    scene.disturb(x, y, 1.5f * sensitivity)
                    continue
                }
                val last = previous ?: pointer.position
                val delta = pointer.position - last
                val now = SystemClock.uptimeMillis()
                if (delta.getDistance() >= 7.dp.toPx() && now - lastEmitted >= 24L) {
                    scene.playDrag(delta.x / size.width.coerceAtLeast(1), x, y, sensitivity)
                    previous = pointer.position
                    lastEmitted = now
                }
            }
        }
    }
