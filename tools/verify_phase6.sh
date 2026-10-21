#!/usr/bin/env bash
# Phase 6 regressions against actual JVM Kotlin sources, plus source/manifest contracts.
set -euo pipefail
cd "$(dirname "$0")/.."
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT
cat > "$TMP/Regression.kt" <<'KT'
import app.gulp.android.core.domain.*
import app.gulp.android.core.graphics.*
import java.time.*
import kotlin.math.abs

fun main() {
    var checks = 0
    fun verify(condition: Boolean, explanation: String) { checks++; check(condition) { explanation } }
    fun stamp(z: ZoneId, date: LocalDate, h: Int, m: Int = 0) = date.atTime(h, m).atZone(z).toInstant().toEpochMilli()
    val berlin = ZoneId.of("Europe/Berlin")
    val spring = LocalDate.of(2026, 3, 29)
    val autumn = LocalDate.of(2026, 10, 25)
    val plan = ReminderPlan(enabled = true, intervalMinutes = 90,
        awakeStartMinute = 8 * 60, awakeEndMinute = 22 * 60)
    verify(ReminderRules.nextOpportunity(stamp(berlin,spring,7,59),berlin,plan) == stamp(berlin,spring,8),
        "DST spring: 08:00 must not shift to 09:00")
    verify(ReminderRules.nextOpportunity(stamp(berlin,autumn,7,59),berlin,plan) == stamp(berlin,autumn,8),
        "DST fall: 08:00 must not shift to 07:00")
    for (date in listOf(spring, autumn)) {
        verify(ReminderRules.nextOpportunity(stamp(berlin,date,8), berlin,plan) == stamp(berlin,date,9,30),
            "wall-clock interval must be maintained through DST")
        verify(ReminderRules.nextOpportunity(stamp(berlin,date,21,45),berlin,plan) ==
            stamp(berlin,date.plusDays(1),8), "quiet hours should have no opportunities")
    }
    val night = plan.copy(awakeStartMinute = 22*60, awakeEndMinute = 6*60)
    verify(ReminderRules.nextOpportunity(stamp(berlin,spring,23,50),berlin,night) ==
        stamp(berlin,spring.plusDays(1),1,0), "overnight opportunities should cross midnight")
    verify(!ReminderRules.shouldNotify(stamp(berlin,spring,15),stamp(berlin,spring,15),berlin,night,null),
        "never notify in quiet hours")
    verify(ReminderRules.nextOpportunity(stamp(berlin,spring,8),berlin,plan.copy(enabled=false)) == null,
        "disabled reminders must not fire")
    verify(ReminderRules.nextOpportunity(stamp(berlin,spring,8),berlin,plan.copy(intervalMinutes=17)) == null,
        "invalid intervals must not fire")
    // Progress is visual only, bounded, and the minimum screen water is decorative.
    for (amount in 0..6000 step 17) {
        val fill = DrinkRules.visualFill(amount,2000)
        verify(fill in 0f..1f, "visual fill outside range")
        verify(WaterMath.visibleFill(fill) in WaterMath.MIN_VISUAL_FILL..0.96f, "visible fill outside range")
        for (x in 0..10) verify(WaterMath.surfaceY(x/10f,fill,0.3f,1f,emptyList(),true) in 0.025f..0.98f,
            "water surface outside safe viewport")
    }
    println("PASS: $checks Phase 6 Kotlin assertions, including two DST boundaries and water bounds")
}
KT
kotlinc \
    app/src/main/java/app/gulp/android/core/domain/DrinkModels.kt \
    app/src/main/java/app/gulp/android/core/domain/ReminderRules.kt \
    app/src/main/java/app/gulp/android/core/graphics/WaterMath.kt \
    "$TMP/Regression.kt" -include-runtime -d "$TMP/check.jar"
java -jar "$TMP/check.jar"
python3 - <<'PY'
from pathlib import Path
import xml.etree.ElementTree as ET
import tomllib
root=Path('.')
manifest=ET.parse(root/'app/src/main/AndroidManifest.xml').getroot()
android='{http://schemas.android.com/apk/res/android}'
widgets=[r for r in manifest.findall('.//receiver') if '.widget.Gulp' in r.attrib.get(android+'name','') and 'WidgetReceiver' in r.attrib.get(android+'name','')]
assert len(widgets)==2, f'expected two launcher widgets: {len(widgets)}'
for r in widgets:
    assert r.attrib[android+'exported']=='true', 'widget host cannot reach non-exported receiver'
assert not list(manifest.iter('uses-permission')) or all(e.attrib.get(android+'name')!='android.permission.INTERNET' for e in manifest.findall('uses-permission'))
libs=tomllib.loads((root/'gradle/libs.versions.toml').read_text())
assert libs['versions']['agp']=='8.13.2'
assert libs['versions']['kotlin']=='2.2.21'
assert libs['versions']['ksp']=='2.2.21-2.0.5'
assert libs['plugins']['ksp']['version']['ref']=='ksp'
assert (root/'gradle/wrapper/gradle-wrapper.properties').exists()
assert '0.6.2-rc3' in (root/'app/build.gradle.kts').read_text()
main=(root/'app/src/main/java/app/gulp/android/MainActivity.kt').read_text()
assert 'nextReminderLabel = nextReminderLabel' in main
assert 'if (plan.enabled && !preferences.clinicianManagedFluids && !notificationPermission)' in main
assert 'homeEditRequest += 1' in main
assert 'onEditRequestHandled' in main
prefs=(root/'app/src/main/java/app/gulp/android/core/data/GulpPreferences.kt').read_text()
assert 'plan.enabled && p[medicalKey] != true' in prefs
assert 'p[reminderEnabled] = false' in prefs
sched=(root/'app/src/main/java/app/gulp/android/core/reminders/GulpReminderScheduler.kt').read_text()
assert 'cancelAlarm(context)' in sched and 'clearNotification(context)' in sched
assert 'if (!current.enabled)' in sched
assert 'androidx.compose.ui.input.pointer.consume' not in (root/'app/src/main/java/app/gulp/android/feature/home/HomeScreen.kt').read_text()
assert 'LocalDate.of(year, month + 1, day)' in (root/'app/src/main/java/app/gulp/android/feature/home/DrinkEditorSheet.kt').read_text()
for path in (root/'app/src/main/res').rglob('*.xml'): ET.parse(path)
print('PASS: Phase 6 manifest, Android build files, medical safety and UI integration checks')
PY
