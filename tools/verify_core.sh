#!/usr/bin/env bash
# Pure-Kotlin smoke tests. Does not verify Compose/Android integration.
set -euo pipefail
cd "$(dirname "$0")/.."
command -v kotlinc >/dev/null || { echo 'kotlinc required' >&2; exit 1; }
DIR="$(mktemp -d)"
trap 'rm -rf "$DIR"' EXIT
cat > "$DIR/TestMain.kt" <<'KT'
import app.gulp.android.core.domain.RecordedProgress
import app.gulp.android.core.navigation.GulpTab
fun main() {
    check(GulpTab.entries.map { it.label } == listOf("Home", "Reminders", "History", "Review", "Settings"))
    check(GulpTab.fromRoute("review") == GulpTab.REVIEW)
    check(GulpTab.fromRoute("nope") == GulpTab.HOME)
    check(RecordedProgress.fraction(500, null) == null)
    check(RecordedProgress.fraction(500, 0) == null)
    check(RecordedProgress.fraction(500, 2000) == 0.25f)
    check(RecordedProgress.fraction(5000, 2000) == 1.0f)
    check(RecordedProgress.fraction(-20, 2000) == 0.0f)
    check(RecordedProgress.formatVolume(750) == "750 mL")
    try { RecordedProgress.formatVolume(-1); error("Expected validation") }
    catch (_: IllegalArgumentException) { }
    println("PASS: 10 pure-Kotlin navigation/progress assertions")
}
KT
kotlinc \
  app/src/main/java/app/gulp/android/core/domain/RecordedProgress.kt \
  app/src/main/java/app/gulp/android/core/navigation/GulpTab.kt \
  "$DIR/TestMain.kt" -include-runtime -d "$DIR/gulp-tests.jar"
java -jar "$DIR/gulp-tests.jar"
