#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT
cat > "$TMP/Phase3Check.kt" <<'KT'
import com.kmab.gulp.core.domain.*
import java.time.*
fun main() {
    var checks = 0
    fun verify(ok: Boolean) { check(ok); checks++ }
    val day = LocalDate.of(2026, 10, 20)
    val zone = ZoneId.of("Europe/Istanbul")
    val midnight = day.atStartOfDay(zone).toInstant().toEpochMilli()
    val records = listOf(
        DrinkRecord(1, 250, DrinkKind.WATER, midnight + 1000),
        DrinkRecord(2, 200, DrinkKind.COFFEE, midnight + 9000),
        DrinkRecord(3, 700, DrinkKind.TEA, midnight - 1000)
    )
    verify(DrinkRules.loggedForDay(records, day, zone) == 450)
    verify(DrinkRules.loggedForDay(records, day.minusDays(1), zone) == 700)
    verify(DrinkRules.totalForDay(records, midnight, zone) == 450)
    for (k in DrinkKind.entries) verify(DrinkKind.fromStored(k.name) == k)
    verify(DrinkKind.fromStored("unknown") == DrinkKind.OTHER)
    for (invalid in listOf(-250, 0, 5001)) {
        try { DrinkRules.checkAmount(invalid); error("Invalid amount allowed") }
        catch (_: IllegalArgumentException) { checks++ }
    }
    verify(DrinkRules.checkAmount(300) == 300)
    verify(DrinkRules.visualFill(0, 2000) == 0f)
    verify(DrinkRules.visualFill(1000, 2000) == .5f)
    verify(DrinkRules.visualFill(2500, 2000) == 1f)
    val a = HomeTile("one", DrinkKind.WATER, 200, true)
    val b = HomeTile("two", DrinkKind.COFFEE, 350, false)
    val c = HomeTile("three", DrinkKind.TEA, 650, false)
    val tiles = listOf(a, b, c)
    verify(HomeTileRules.decode(HomeTileRules.encode(tiles)) == tiles)
    verify(HomeTileRules.reorder(tiles, "one", "three") == listOf(b,c,a))
    verify(HomeTileRules.reorder(tiles, "three", "one") == listOf(c,a,b))
    verify(HomeTileRules.reorder(tiles, "bad", "one") == tiles)
    verify(HomeTileRules.decode(null) == HomeTileRules.defaults)
    verify(HomeTileRules.decode("invalid") == HomeTileRules.defaults)
    verify(HomeTileRules.decode("a,WATER,250,1;a,WATER,250,1").size == 1)
    verify(HomeTileRules.decode("x,ILLEGAL,250,0") == HomeTileRules.defaults)
    println("PASS: $checks pure-Kotlin Phase 3 rules/shortcut/date assertions")
}
KT
kotlinc app/src/main/java/com/kmab/gulp/core/domain/DrinkModels.kt \
    app/src/main/java/com/kmab/gulp/core/domain/DrinkType.kt "$TMP/Phase3Check.kt" -include-runtime -d "$TMP/phase3.jar"
java -jar "$TMP/phase3.jar"
