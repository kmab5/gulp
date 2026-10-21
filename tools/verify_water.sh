#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
command -v kotlinc >/dev/null || { echo 'Install Kotlin CLI to run the SDK-free water tests.' >&2; exit 1; }
DIR="$(mktemp -d)"
trap 'rm -rf "$DIR"' EXIT
cat > "$DIR/WaterCheck.kt" <<'KT'
import app.gulp.android.core.graphics.*
import kotlin.math.abs
fun main() {
  var count = 0
  fun verify(ok: Boolean) { check(ok); count++ }
  verify(WaterMath.visibleFill(0f) == WaterMath.MIN_VISUAL_FILL)
  verify(WaterMath.visibleFill(2f) == 0.96f)
  verify(WaterMath.surfaceY(.5f,.5f,0f,0f,emptyList(), false) == .5f)
  verify(WaterMath.surfaceY(0f,.5f,1f,0f,emptyList(),true) < WaterMath.surfaceY(1f,.5f,1f,0f,emptyList(),true))
  verify(WaterMath.normalizedTilt(100f) == 1f)
  verify(WaterMath.normalizedTilt(-100f) == -1f)
  verify(WaterMath.easingStep(.5f,1f,.01f,2f) in .5f..1f)
  verify(WaterMath.easingStep(1f,0f,.01f,2f) in 0f..1f)
  val waves = listOf(WaterImpulse(.5f,.5f,1f,0f))
  verify(abs(WaterMath.surfaceY(.37f,.5f,0f,.45f,waves,true) - WaterMath.surfaceY(.37f,.5f,0f,.45f,emptyList(),true)) > 0.000001f)
  verify(WaterMath.surfaceY(.37f,.5f,0f,3f,waves,true) == WaterMath.surfaceY(.37f,.5f,0f,3f,emptyList(),true))
  for (fill in listOf(-3f,0f,.1f,.5f,1f,3f)) for (x in 0..64) {
    val y=WaterMath.surfaceY(x/64f,fill,.8f,1f,waves,true)
    verify(y in .025f.. .98f)
  }
  verify(WaterMath.surfaceY(.8f,.4f,.8f,1f,waves,false) == WaterMath.surfaceY(.2f,.4f,-.8f,1f,emptyList(),false))
  println("PASS: $count pure Kotlin water-geometry assertions")
}
KT
kotlinc app/src/main/java/app/gulp/android/core/graphics/WaterMath.kt "$DIR/WaterCheck.kt" -include-runtime -d "$DIR/water.jar"
java -jar "$DIR/water.jar"
