#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
DIR="$(mktemp -d)"; trap 'rm -rf "$DIR"' EXIT
cat > "$DIR/Test.kt" <<'KT'
import com.kmab.gulp.core.domain.*
import java.time.Instant
fun main() {
    var checks = 0
    fun ok(v: Boolean) { check(v); checks++ }
    ok(DrinkCsv.encode(emptyList()) == DrinkCsv.HEADER + "\n")
    for (n in 1..200) {
        val records = (0 until n).map { DrinkRecord((n-it).toLong(),(it%5+1)*125,DrinkKind.entries[it%4],(it*100000).toLong()) }
        val csv = DrinkCsv.encode(records)
        ok(csv.lines().size == n+2)
        ok(csv.startsWith(DrinkCsv.HEADER + "\n"))
        ok(csv.lines()[1].startsWith("${records.first().id},"))
        ok(csv.split('\n').drop(1).filter { it.isNotBlank() }.all { it.split(',').size == 7 })
    }
    println("PASS: $checks offline CSV structure, UTC order, bounded rows")
}
KT
kotlinc app/src/main/java/com/kmab/gulp/core/domain/DrinkModels.kt \
    app/src/main/java/com/kmab/gulp/core/domain/DrinkType.kt \
    app/src/main/java/com/kmab/gulp/core/domain/DrinkCsv.kt \
    "$DIR/Test.kt" -include-runtime -d "$DIR/check.jar"
java -jar "$DIR/check.jar"
python3 tools/verify_phase5_structure.py
