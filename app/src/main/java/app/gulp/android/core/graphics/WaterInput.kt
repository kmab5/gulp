package app.gulp.android.core.graphics

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput

/** Observe initial DOWN events without consuming: actual buttons/drag/scroll keep their events. */
fun Modifier.waterTouchFeedback(scene: WaterScene): Modifier = pointerInput(scene) {
    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            for (change in event.changes) {
                if (change.pressed && !change.previousPressed && size.width > 0 && size.height > 0) {
                    scene.disturb(change.position.x / size.width, change.position.y / size.height)
                }
            }
        }
    }
}
