## v0.6.2-rc3 — follow-up to `:app:compileDebugKotlin` failure

The reported Kotlin errors were in `WaterAdaptiveContent.kt`, `DrinkEditorSheet.kt`, and `TileEditorSheet.kt`. The outer drawing scope is now explicitly selected; both Material3 sheets have experimental API opt-ins. The deprecated JVM target DSL has been migrated. Run:

```powershell
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug -Dorg.gradle.java.home="D:/Code/java/jdk-21.0.12.1"
```

The Java native-access notice, outdated SDK XML reader warning, and `stripDebugDebugSymbols` notice in the previous build output were **nonfatal warnings**. `:app:compileDebugKotlin` was the failure. Native Android compilation for this version remains unverified in the authoring environment. If the same errors persist after replacing the project, confirm that the extracted `app/src/main/java/...` files are from v0.6.2-rc3.

# Build and verification history

## Phase 1
Project skeleton and navigation. Static checks and pure Kotlin assertions passed. No Android SDK in the authoring environment; native APK not compiled.

## Phase 2
Pure Kotlin water-geometry assertions and source checks passed. Android compilation and real-device water rendering not verified.

## Phase 3 — October 20, 2026
- [x] Pure navigation/progress checks: 10 assertions.
- [x] Pure water math tests: 401 assertions.
- [x] Pure date, amount and tile-codec checks: 23 assertions.
- [x] Offline repository logic compiled against minimal Room stubs and tested: 11 assertions.
- [x] Gradle/version TOML parsed, Android XML checked, project structure checks passed.
- [x] ZIP archive integrity and full Phase 1+2+3 source tree checked.
- [ ] Native Android APK build (`gradle :app:assembleDebug`) — SDK and dependency downloads unavailable here.
- [ ] Room compiler/KSP generated-source build and Android lint — requires Gradle/SDK.
- [ ] Connected Android Room/UI tests — requires emulator/device.
- [ ] Real-device water shader/tilt/frame-rate/battery performance — requires physical device.

**The static/offline checks cannot replace a real Android compile.** If CI or Android Studio surfaces errors, these must be fixed before calling the implementation release-ready.

Run offline checks with:

```
bash tools/verify_core.sh
bash tools/verify_water.sh
bash tools/verify_phase3.sh
bash tools/verify_repository_offline.sh
python3 tools/verify_structure.py
```

Run Android checks using:

```
gradle :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
gradle :app:connectedDebugAndroidTest
```

Reminder: inherited Phase 2 archive lacked `gradle/wrapper/gradle-wrapper.jar`; use system Gradle 8.13 or generate the wrapper on a networked development machine.


## Phase 4 — October 20, 2026
- [x] Reminder/date/overnight/DST/review/reference pure Kotlin assertions passed; see `tools/verify_phase4.sh`.
- [x] Previous offline checks retained (core, fluid geometry, Phase 3 rules and Room API substitute).
- [x] Manifest/Gradle/Kotlin source/static structure verified.
- [ ] Android SDK build, notification integration, Room/KSP, Compose tests and OEM background delivery not verified in this environment.
- [ ] Device acceptance checks listed in `docs/PHASE4_DEVICE_TEST.md`.

## Phase 5 — October 20, 2026
- [x] Standalone CSV encoder compiled and tested with 801 offline assertions.
- [x] Repository Room-substitute check tested add/edit/delete/undo/export/erase.
- [x] Phase 5 manifest/widget, settings, SAF integration checks passed.
- [x] Earlier Phase 1–4 pure Kotlin checks retained.
- [ ] Native Android Gradle/Glance/Room/KSP compilation — no usable Android SDK/dependency download in this authoring environment.
- [ ] Device-launcher and sensor/accessibility acceptance checklist in `PHASE5_DEVICE_TEST.md`.

Source-integrated does not imply Android-build verified.
