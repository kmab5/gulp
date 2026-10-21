#!/usr/bin/env python3
"""Fast offline project contract checks; not a replacement for Gradle compilation."""
import pathlib
import re
import sys
import tomllib
import xml.etree.ElementTree as ET

root = pathlib.Path(__file__).resolve().parents[1]
problems = []
def require(predicate, message):
    if not predicate:
        problems.append(message)

for path in (root / 'app/src/main').rglob('*.xml'):
    try:
        ET.parse(path)
    except ET.ParseError as exc:
        problems.append(f'Malformed {path.relative_to(root)}: {exc}')

versions = tomllib.loads((root / 'gradle/libs.versions.toml').read_text())
require(versions['versions']['agp'] == '8.13.2', 'Unexpected AGP version')
require(versions['versions']['kotlin'] == '2.2.21', 'Unexpected Kotlin version')
require('gradle-8.13-bin.zip' in (root / 'gradle/wrapper/gradle-wrapper.properties').read_text(), 'Gradle wrapper mismatch')
appgradle = (root / 'app/build.gradle.kts').read_text()
for phrase in ('compileSdk = 36', 'minSdk = 28', 'targetSdk = 36', 'namespace = "app.gulp.android"', 'buildFeatures { compose = true }'):
    require(phrase in appgradle, f'Missing build setting {phrase}')

pkg = root / 'app/src/main/java/app/gulp/android'
for rel in ('MainActivity.kt','core/graphics/WaterBackdrop.kt','core/graphics/WaterMath.kt','core/graphics/WaterScene.kt','core/graphics/WaterAdaptiveContent.kt','core/graphics/WaterSensors.kt','core/graphics/WaterInput.kt','core/navigation/GulpNav.kt','core/navigation/GulpTab.kt','core/domain/RecordedProgress.kt','feature/home/HomeScreen.kt','feature/reminders/RemindersScreen.kt','feature/history/HistoryScreen.kt','feature/review/ReviewScreen.kt','feature/settings/SettingsScreen.kt'):
    require((pkg / rel).is_file(), f'Missing {rel}')

for label in ('Home','Reminders','History','Review','Settings'):
    require(f'"{label}"' in (pkg/'core/navigation/GulpTab.kt').read_text(), f'Missing tab {label}')
require('no navbar island' in (root / 'docs/DESIGN_CONTRACT.md').read_text().lower() or 'no navbar' in (root/'docs/DESIGN_CONTRACT.md').read_text().lower(), 'Missing approved navbar rule')
require('mask' in (root/'docs/DESIGN_CONTRACT.md').read_text().lower(), 'Missing shared waterline mask rule')
require('widget' in (root/'docs/NEXT_TURNS.md').read_text().lower(), 'Missing initial release widget scope')
manifest = (root / 'app/src/main/AndroidManifest.xml').read_text()
require('android.permission.INTERNET' not in manifest, 'Unexpected network permission')
require('android:exported="true"' in manifest, 'No launch activity')
require((root / '.github/workflows/android-ci.yml').is_file(), 'Missing CI')
require(not (pkg / 'core/graphics/StaticWaterBackdrop.kt').exists(), 'Old water renderer still in tree')
require('WaterBackdrop(scene)' in (pkg/'MainActivity.kt').read_text(), 'Liquid renderer not connected')
require('waterTouchFeedback(scene,' in (pkg/'MainActivity.kt').read_text() or 'waterTouchFeedback(scene)' in (pkg/'MainActivity.kt').read_text(), 'Touch water feedback not connected')
require('WaterTiltSensor(scene, active,' in (pkg/'MainActivity.kt').read_text() or 'WaterTiltSensor(scene, active)' in (pkg/'MainActivity.kt').read_text(), 'Sensor lifecycle not connected')
require('WaterAdaptiveText' in (pkg/'core/navigation/GulpNav.kt').read_text(), 'Adaptive nav label missing')
require((root/'tools/verify_water.sh').is_file(), 'Water verification script missing')
# Guard against accidental publication of the retired name in live Android/Kotlin resources.
for path in [*pkg.rglob('*.kt'), *(root/'app/src/main/res').rglob('*.xml')]:
    require('STILLWATER' not in path.read_text(), f'Retired brand in {path.name}')


# Phase 3: source-level checks for the persistence and Home integration.
for rel in ('core/data/GulpDatabase.kt','core/data/GulpRepository.kt',
            'core/data/GulpPreferences.kt','core/domain/DrinkModels.kt',
            'feature/home/DrinkEditorSheet.kt','feature/home/TileEditorSheet.kt'):
    require((pkg / rel).exists(), f'Phase 3 file missing: {rel}')
require('alias(libs.plugins.ksp)' in appgradle, 'KSP plugin missing for Room')
require('implementation(libs.room.runtime)' in appgradle, 'Room runtime dependency missing')
require('implementation(libs.datastore.preferences)' in appgradle, 'DataStore dependency missing')
require('repository.add(' in (pkg/'MainActivity.kt').read_text(), 'Real recording not wired')
require('DrinkRules.loggedForDay' in (pkg/'MainActivity.kt').read_text(), 'Today logged volume not wired')
require('onLogShortcut' in (pkg/'feature/home/HomeScreen.kt').read_text(), 'Home quick add missing')
require('repo' in (root/'docs/PHASE3_ARCHITECTURE.md').read_text().lower(), 'Phase 3 data documentation missing')
require((root/'tools/verify_phase3.sh').exists(), 'Phase 3 smoke tests missing')

if problems:
    for p in problems:
        print('FAIL:', p, file=sys.stderr)
    sys.exit(1)
print('PASS: XML, Gradle/catalog, five tabs, source tree, safety/design & CI static checks')
