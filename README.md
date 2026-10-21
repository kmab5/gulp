# gulp — native Android hydration app

**Version 0.6.4-rc5 · Kotlin / Jetpack Compose · Android 9+ · fully offline by default**

This is the **complete development repository** through all six planned coding phases, including the design reference screenshots, scientific guardrails, native animated water UI, customizable quick-add Home, Room-powered drink history, onboarding, notifications and quiet hours, Review analytics, Settings, CSV export/deletion and two Glance launcher widgets.

## UI review 02: current state (2026-10-21)

See [`docs/UI_FEEDBACK_AUDIT.md`](docs/UI_FEEDBACK_AUDIT.md) for every onboarding, navbar and Home review request, its status, intentional alternatives and outstanding on-device checks. Changes include onboarding water-aware labels and a centered medical-plan card, distinct amber radio selections, custom-goal validation, 2-column edit-grid outlines, stabilized live reordering, and a slightly larger navbar label. The initial launcher icon, custom controls, navbar glyphs and animations, and feathered scrolling were delivered in UI review 01 and remain in place. The project still uses an inexpensive translucent/sheen treatment rather than computing real-time blurred backdrop textures per tile. Full Android compilation and device rendering are **not verified in this handoff**.

## Package identity and signed releases

The application ID and Kotlin packages are now `com.kmab.gulp`. The release signing key is expected as the ignored root file `build-key`; local passwords go in ignored `keystore.properties`. See [docs/RELEASE_SIGNING.md](docs/RELEASE_SIGNING.md) before the first release build. Routine CI builds are debug-only and require no signing key.

## First compilation — Android Studio

1. Extract the archive and open the enclosed **`gulp/`** folder (the one containing `settings.gradle.kts`).
2. Install **JDK 17, Android SDK Platform 36, SDK Build Tools 35.0.0**. AGP **8.13.2** and Kotlin **2.2.21** are pinned in the version catalog; Gradle **8.13** is required.
3. Let Android Studio sync Gradle. In **Run Configurations**, run **app** on Android 9+.
4. Test add/edit, reminders, Review and launcher widgets; the special water preview is in **Settings → Water animation preview**.
5. Follow [`docs/PHASE6_BUILD_AND_DEVICE_ACCEPTANCE.md`](docs/PHASE6_BUILD_AND_DEVICE_ACCEPTANCE.md). Share the first Gradle error/stack trace if any; Android compilation was **not possible** in the authoring container.

### Build-fix patch (v0.6.3-rc4)

The latest Windows Gradle log reached `:app:compileDebugKotlin` and reported errors in three Compose source files. We fixed the outer `ContentDrawScope` receiver call inside the waterline `clipPath` operation, and explicitly opted the two editor sheets into Material 3's experimental bottom-sheet API. We also migrated the deprecated `android.kotlinOptions` to `kotlin.compilerOptions` (JVM 17). The earlier fixes for the `ksp` catalog alias and Reminders callback are retained.

Check the corrections without an Android SDK using `python3 tools/verify_compose_buildfix.py` and `python3 tools/verify_gradle_catalog.py`. **The complete Android Gradle build must still be rerun**; Kotlin-only tests and static checks are not a guarantee of an Android build.

### Previous build-fix patch (v0.6.1-rc2)

- Renamed the invalid uppercase `KSP` version alias to lowercase `ksp` in `gradle/libs.versions.toml` and updated its plugin reference. Gradle 8.13 rejects uppercase initial letters for catalog version aliases.
- Fixed an out-of-scope `medical` variable in the Reminders tab callback; the callback now checks `preferences.clinicianManagedFluids`.
- Added an offline version-catalog preflight (`python3 tools/verify_gradle_catalog.py`) to CI and the verification instructions.
- These changes are **not yet an Android-compiled guarantee**. If the next Gradle run finds other issues, share the first failed task and compiler diagnostics.

### Build from terminal

```bash
# If Gradle 8.13 is installed:
gradle --no-daemon :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
# If you have an emulator/phone attached:
gradle :app:connectedDebugAndroidTest
# Release artifacts (signed using root build-key and keystore.properties):
gradle :app:assembleRelease :app:bundleRelease
python3 tools/report_package_size.py
```

The cross-platform `gradlew` / `gradlew.bat` scripts bootstrap the **official Gradle 8.13 wrapper JAR** from `gradle/gradle` on GitHub on first use, verifying its Git blob ID, then download the official Gradle distribution. The wrapper JAR is **not in the ZIP** because it couldn't be fetched from this restricted container. An installed Gradle 8.13 or Android Studio sync is another option. First sync requires network access.

## Phase 6 code-hardening work

- Local-clock reminder calculation now holds 08:00 at 08:00 across DST (no hour drift); new DST regression tests.
- Disabling reminders clears previously posted notifications; scheduling the next alarm no longer clears a freshly delivered notification. Old notification log/snooze actions are rejected if reminders have been disabled.
- Users with medically managed fluids are not given automatic generic reminders or tracking goals, including when Settings changes later.
- Home shows the actual upcoming reminder (or Off / Notifications off), instead of a permanent "Not set" placeholder.
- Settings > Customize shortcuts enters edit mode immediately, and the request is consumed once; no accidental re-entry on subsequent Home visits.
- Drink date editor now handles month/year changes safely (previous month-end selection could crash).
- Launcher widget receivers are accessible to the Android host; widget actions still use the same Room writer.
- Removed an obsolete Compose pointer-input import, updated Gradle artifacts to `0.6.0-rc1`, expanded CI to test/lint/build debug and prepare release packaging (historical v0.6.0 note), and added package-size reporting.
- Full earlier phase source and reference design assets are preserved. No internet permission or cloud tracking is added.

## Offline verification

Run all source-level checks from the repo root:

```bash
python3 tools/verify_gradle_catalog.py
python3 tools/verify_compose_buildfix.py
bash tools/verify_core.sh
bash tools/verify_water.sh
bash tools/verify_phase3.sh
bash tools/verify_repository_offline.sh
bash tools/verify_phase4.sh
bash tools/verify_phase5.sh
bash tools/verify_phase6.sh
python3 tools/verify_structure.py
python3 tools/verify_phase4_structure.py
python3 tools/verify_phase5_structure.py
```

These checks compile and run the **actual Kotlin domain/geometry code** and exercise repository behavior against lightweight Room substitutes. They **do not** compile Android Compose/Glance or validate Gradle/KSP generation. No APK has been produced here; the app's visual motion and performance must be tested on hardware.

## Project layout

- `app/src/main/java/com/kmab/gulp/core/graphics/`: procedural water, touch ripples, sensors, shared waterline masking.
- `core/data/` and `core/domain/`: Room, DataStore, drinks, undo, reminders, reporting and CSV.
- `core/reminders/`: inexact alarms, notification actions, restart/timezone handling.
- `feature/`: Home, History, Reminders, Review, Settings, onboarding.
- `widget/` and `app/src/main/res/xml/`: two native Android home-screen widgets.
- `docs/reference/`: approved visual references; `docs/SCIENCE_RULES.md`: research guardrails.
- `tools/`: offline checks and artifact-size reporting; `.github/workflows/`: Android compile and lint CI.

The water fill is an artistic representation of **recorded fluid volume**, not a medical hydration measurement or prescription. References and gentle reminders are optional. No arbitrary "catch-up drinking" rule exists.
