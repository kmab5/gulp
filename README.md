# gulp — complete Android repository through Phase 5

**Version 0.5.0-phase5 · Kotlin / Jetpack Compose · Android 9+ · offline-first**

This is the **entire project** (Phases 1–5), not a patch. It includes the approved liquid UI system, Room drink history, customizable Home, onboarding, reminders, Review, Settings, and two Android launcher widgets. No remote server or account is required. **This source is not yet Android-build verified; no APK is included.**

## Phase 5 — new features

- Two launcher widgets built with **Jetpack Glance 1.2.0 stable**: a compact quick-log widget (`+250 mL`) and a larger Today widget (`+250 mL`/`+500 mL`). Both show the day's *recorded* drinks and use the same Room repository as Home.
- Widget appearance follows gulp's colors using lightweight static native views rather than keeping the live water physics running on the launcher.
- All add/edit/delete/undo actions request widget refresh; date, timezone, reboot and package-replacement broadcasts refresh the "Today" boundary. Glance updates may be delayed by launcher/device policy.
- Settings now saves **Reduce animation**, **Tilt response**, **Touch ripples**, and **Stronger text contrast** in DataStore. System animation settings still take precedence where relevant.
- **Export drinks as CSV** uses the Android system file picker (`ACTION_CREATE_DOCUMENT`). The CSV contains a UTC ISO timestamp, saved drink type and quantity for every recorded drink; its data stays in the user-selected destination.
- **Delete all drinks** requires a separate confirmation and removes drink records from Room. It preserves configuration and reminder choices. Deletion cannot be undone.
- Added widget update, CSV structure, repository deleteAll and manifest tests. All previous phase features are retained.

## Open in Android Studio

1. Extract this ZIP and open the enclosed **`gulp/`** directory as a project.
2. Use JDK **17**, Android SDK **API 36** and **Gradle 8.13** (the repository contains the Gradle wrapper scripts and properties, but the bootstrap JAR is **not bundled**; use an installed Gradle 8.13 or generate the official Gradle wrapper in a connected environment).
3. Sync project and run `app` on Android 9+.
4. On the device, long-press Android's home screen, select **Widgets**, choose **gulp · Quick add** or **gulp · Today** and add it.
5. Change Settings → Appearance and accessibility; use export/delete in Settings → Data and privacy.
6. See `docs/PHASE5_DEVICE_TEST.md` and previous phase device checklists before approving an APK.

### Build, tests, release

```bash
gradle --no-daemon :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
# With an emulator or physical device:
gradle :app:connectedDebugAndroidTest
```

`.github/workflows/android-ci.yml` downloads SDK 36 and runs tests, lint and debug APK compilation; it uploads `gulp-phase5-debug` upon success. This CI job **has not been run by the authoring environment**.

### Offline checks performed here

```bash
bash tools/verify_core.sh
bash tools/verify_water.sh
bash tools/verify_phase3.sh
bash tools/verify_repository_offline.sh
bash tools/verify_phase4.sh
bash tools/verify_phase5.sh
python3 tools/verify_structure.py
python3 tools/verify_phase4_structure.py
```

These tests exercise pure Kotlin water/domain code and compile repository logic against *Room substitutes*. The Android widget screens, DataStore wiring, notifications, Compose UI and KSP-generated Room implementation still require native Android/CI verification.

## Project directories

- `app/src/main/java/app/gulp/android/widget/`: Glance widgets, callback, date refresh receiver.
- `core/data/`: persistent Room records, DataStore config, writes/undo/export/delete and widget refresh coordination.
- `core/domain/`: drinking rules, reminders, Review analytics, CSV encoding.
- `core/graphics/`: water simulation, touch/sensor effects, waterline-aware contrast.
- `feature/`: Home, History, Reminders, Review, Settings, onboarding.
- `app/src/main/res/xml/`: launcher widget declarations.
- `tools/`: offline verification scripts; `docs/`: design contract, safety science and device acceptance plans.

## Phase 6 / outstanding quality gates

1. Compile and fix **all** Android/Glance/KSP/Compose errors in Android Studio or CI.
2. Run instrumented database and UI tests, test both widgets on multiple launchers, including rapid taps and date changes.
3. Check native performance, sensor behavior, frame pacing and accessibility on physical phones.
4. Tune APK/AAB footprint, performance and visuals based on measured results.
5. Produce installable APK/AAB and release candidate. Play Store publishing requires signing/account access.

**Do not claim the project is release-ready or clinically assessing hydration.** The display represents self-reported, recorded drinks, not actual body hydration.
