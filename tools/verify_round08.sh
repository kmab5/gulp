#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
DIR=$(mktemp -d)
trap 'rm -rf "$DIR"' EXIT
cat > "$DIR/Round08.kt" <<'KT'
import com.kmab.gulp.core.domain.*
import com.kmab.gulp.core.graphics.*
import java.time.*
fun main() {
    var count = 0
    fun checkThat(condition: Boolean, description: String) { count++; check(condition) { description } }
    for (interval in 30..360) {
        val plan = ReminderPlan(enabled=true, intervalMinutes=interval)
        checkThat(plan.valid(), "custom interval $interval must be accepted")
    }
    checkThat(!ReminderPlan(enabled=true, intervalMinutes=29).valid(), "interval too short")
    checkThat(!ReminderPlan(enabled=true, intervalMinutes=361).valid(), "interval too long")
    checkThat(WaterMath.visibleFill(0f)==0f, "no artificial water reserve")
    checkThat(WaterMath.surfaceY(.5f,0f,0f,0f,emptyList(),false)>1f,
        "empty illustration goes below nav")
    val utc=ZoneId.of("UTC")
    val date=LocalDate.of(2026,10,27)
    fun log(day:LocalDate, amount:Int, kind:DrinkKind, id:String, name:String, hour:Int): DrinkRecord =
        DrinkRecord(amountMl=amount, kind=kind,
            timeMillis=day.atTime(hour,0).atZone(utc).toInstant().toEpochMilli(),
            drinkTypeId=id, drinkName=name, drinkIcon="glass")
    val records=listOf(log(date,300,DrinkKind.WATER,"water","Water",8),
        log(date,100,DrinkKind.OTHER,"custom_42","Soda",21),
        log(date.minusDays(1),200,DrinkKind.OTHER,"custom_42","Soda",15),
        log(date.minusDays(12),500,DrinkKind.TEA,"tea","Tea",12))
    val info=ReviewInsightRules.summarize(records,date,utc)
    checkThat(info.daysRecorded30 == 3, "30 day count")
    checkThat(info.total30Ml == 1100L, "30 day volume")
    checkThat(info.averageWhenRecordedMl == 300L, "mean on 2 logged days, not 7")
    checkThat(info.drinkShares7.any { it.name == "Soda" && it.amountMl == 300 }, "stable type breakdown")
    checkThat(info.morningCount7 == 1 && info.afternoonCount7 == 1 && info.eveningCount7 == 1,
        "hour grouping")
    checkThat(ReviewInsightRules.summarize(emptyList(),date,utc).averageWhenRecordedMl == null,
        "empty means no data")
    println("PASS: $count round08 domain assertions")
}
KT
kotlinc app/src/main/java/com/kmab/gulp/core/domain/DrinkType.kt \
    app/src/main/java/com/kmab/gulp/core/domain/DrinkModels.kt \
    app/src/main/java/com/kmab/gulp/core/domain/ReminderRules.kt \
    app/src/main/java/com/kmab/gulp/core/domain/ReviewRules.kt \
    app/src/main/java/com/kmab/gulp/core/domain/ReviewInsights.kt \
    app/src/main/java/com/kmab/gulp/core/graphics/WaterMath.kt \
    "$DIR/Round08.kt" -include-runtime -d "$DIR/round08.jar"
java -jar "$DIR/round08.jar"
python3 - <<'PY'
from pathlib import Path
r=Path('app/src/main/java/com/kmab/gulp')
checks={
    'MainActivity.kt':['setExperience(', 'onCreateDrinkType', 'glassBlurEnabled'],
    'core/navigation/GulpNav.kt':['waterAboveClip','waterBelowClip', 'indication = null'],
    'feature/home/HomeScreen.kt':['onCreateDrinkType','GlassSurface'],
    'feature/home/TileEditorSheet.kt':['NewDrinkTypeDialog','GlassSurface'],
    'feature/home/DrinkEditorSheet.kt':['NewDrinkTypeDialog','GlassSurface'],
    'feature/reminders/RemindersScreen.kt':['Custom minutes','enteredInterval','Wake','Sleep'],
    'feature/history/HistoryScreen.kt':['showCalendar','GlassSurface'],
    'feature/review/ReviewScreen.kt':['ReviewInsightRules.summarize','Last 30 days','When you logged'],
    'feature/settings/SettingsScreen.kt':['Use custom amount','glassBlurEnabled','swipeTabsEnabled','motionStrengthPct'],
}
for file, phrases in checks.items():
    src=(r/file).read_text()
    for phrase in phrases: assert phrase in src, f'{file}: missing {phrase}'
print('PASS: screen source integration checks')
PY
