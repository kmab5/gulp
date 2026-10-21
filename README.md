# gulp — Android application (Phase 3)

**Native Kotlin / Jetpack Compose · Android 9+ · fully offline · version 0.3.0-phase3**

This is the complete source repository through Phase 3, including the approved design references and previous water-engine implementation. The Android APK has **not** been compiled or run in this handoff environment; we cannot claim device performance, graphics correctness or fully passing Room/KSP integration without Android Studio or an SDK-equipped CI run.

## What works in the source

- Five plain-language navigation tabs: Home, Reminders, History, Review, Settings.
- Full-viewport water, live light/dark text masking, touch ripples and phone-tilt motion from Phase 2.
- Home's large **Add a drink** action, a real add/edit/delete sheet and persistent Room records.
- Types: water, coffee, tea, other; editable amount and date/time. Daily totals respect local calendar dates.
- Quick-add tiles with chosen amount and type, adjustable widths, hold-and-drag reordering, adding and removing up to 10 tiles. Home layout persists in Preferences DataStore.
- Undo after adding, editing and deleting the most recent drink action.
- A configurable visual water-display capacity in Settings; changes never change the recorded amount.
- History date selector and records for that day, with tap-to-edit/delete.

**Not implemented yet:** actual reminder scheduling, Review analytics, onboarding, export and Android launcher widgets. Widgets are in scope for the first full release. Placeholder screens do not claim that their functions work.

## How to run

1. Install Android Studio with Android SDK API 36 and a Java 17 toolchain.
2. Open this `gulp` directory, let Gradle synchronize, and select the `app` run configuration.
3. Build with **Gradle 8.13** using Android Studio or an installed Gradle executable:

```bash
gradle :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
```

4. Install the debug build on a phone or Android emulator. For the instrumented Room and UI tests run:

```bash
gradle :app:connectedDebugAndroidTest
```

5. Test using `docs/PHASE3_DEVICE_TEST.md`. A physical phone is essential for tilt and frame-rate evaluation.

**Gradle wrapper note:** The previous archive supplied `gradlew`, `gradlew.bat` and `gradle-wrapper.properties` but **not** the required `gradle/wrapper/gradle-wrapper.jar`. These files are retained. Use an installed Gradle 8.13 or Android Studio, then generate the jar with `gradle wrapper --gradle-version 8.13` before invoking `./gradlew`. GitHub Actions uses `gradle/actions/setup-gradle` and calls `gradle` directly.

## Run SDK-independent checks

These checks do not compile Android source:

```bash
bash tools/verify_core.sh
bash tools/verify_water.sh
bash tools/verify_phase3.sh
python3 tools/verify_structure.py
```

## Project map

```text
app/src/main/java/app/gulp/android/
  core/
    data/                 Room database, DAO, repository, DataStore preferences
    domain/               pure date/volume/shortcut behavior
    graphics/             water geometry, renderer, sensors, masking
    design/               reused UI components and palette
    navigation/           five tabs and embedded bottom navigation
  feature/
    home/                 logging, quick tiles, edit sheets
    history/              day picker, stored records
    reminders/            next-phase stub
    review/               next-phase stub
    settings/             visual scale and forthcoming settings
  MainActivity.kt         composition root, persistence flows, undo/snackbar
```

For safety and design rules read `docs/SCIENCE_RULES.md`, `docs/DESIGN_CONTRACT.md` and `docs/PHASE3_ARCHITECTURE.md`. Original approved screen references are in `docs/reference/`; some contain previous naming or layouts and the design contract supersedes those parts.

## Data and safety

No internet permission, account requirement or analytics SDK. Logged beverages and tile choices stay on-device. Water volume relative to the chosen **display scale** is a visual, not a hydration diagnosis or medically recommended target. We count ordinary beverage volume without invented multipliers. An unlogged day is not a day without drinking.

## Important verification boundary

Static/XML checks and pure Kotlin assertions pass in this handoff environment. Gradle plugins, Compose, Room generation, UI integration, and real-device animations **remain unverified** until the CI workflow or Android Studio builds and tests them. If the first actual build surfaces compatibility issues, those must be fixed before declaring Phase 3 build-verified.
