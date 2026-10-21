# gulp — Android foundation (Phase 1 / Turns 1–3)

**Status:** source-complete Phase 1 *foundation preview*, NOT a finished hydration tracker.

gulp is a small, graphics-first Android hydration reminder project. This handoff contains an Android Studio Kotlin/Jetpack Compose application with **all five navigable tabs**, a static placeholder for the approved full-screen water backdrop, accessible text/buttons, a custom transparent navbar, test scaffolding, and scientific/design contracts for the next phases.

The previous working name was STILLWATER. The application name, package namespace, and source tree here use **gulp**.

## What you can do now

- Launch the app and visit **Home, Reminders, History, Review, Settings**.
- Use the transparent 5-tab navigation, including a highlighted selected icon.
- View the navy/turquoise visual foundation and the approved layout proportions.
- Tap Home's *Add a drink*, quick tiles, or preference rows to see an **explicit preview-only** explanation. None of these controls falsely claims to have recorded a drink.
- Build/test through the included GitHub Actions CI workflow or local Android Studio.

## What is *not* implemented yet

- Water physics, touch ripples, gravity/tilt, animated liquid height, live waterline text masking.
- Drink logging, persistence, user-configurable grid layout.
- Real reminders/notifications, reports, settings persistence, onboarding, native widgets.

These are deliberately later milestones in the approved 32-turn roadmap. **There is no genuine intake data, hydration computation, or reminder timer in Phase 1.**

## Requirements

- Android Studio with Android SDK Platform **36** and Build Tools **35.0.0**.
- JDK **17 or 21**.
- Internet connection for the first Gradle and AndroidX dependency resolution.
- Device/emulator running **Android 9 / API 28** or newer.

Toolchain pins: **Gradle 8.13**, **Android Gradle Plugin 8.13.2**, **Kotlin 2.2.21**, **Compose BOM 2025.10.00**. These were selected as an AGP-compatible stable family; upgrades must be tested together.

## Build and run

1. Open this folder (the one containing `settings.gradle.kts`) in Android Studio.
2. Allow Gradle sync and SDK component installation.
3. Select `app` and a device/emulator, then Run.
4. For CLI builds: `./gradlew :app:assembleDebug :app:testDebugUnitTest` (on Windows: `gradlew.bat`).
5. Debug APK after a successful local build: `app/build/outputs/apk/debug/app-debug.apk`.

**Wrapper note:** The official Gradle wrapper JAR is intentionally downloaded on the first `gradlew` invocation and verified against Gradle's published SHA256. This avoids distributing an unverified binary from this restricted build environment. It is different from a traditional checked-in wrapper JAR and requires working internet on first run. In Android Studio you can alternatively regenerate the standard wrapper by running `gradle wrapper --gradle-version 8.13` when Gradle is installed. The `.github/workflows/android-ci.yml` workflow executes the build and publishes the debug APK as an artifact.

## Local checks and verification status

- Source / configuration / XML and design-contract checks are scripted in `tools/verify_structure.py`.
- Pure Kotlin model tests can be run without the Android SDK using `tools/verify_core.sh` (local `kotlinc`).
- Unit tests: `./gradlew :app:testDebugUnitTest`.
- Instrumentation tests: `./gradlew :app:connectedDebugAndroidTest` with an Android emulator/device.
- Android lint: `./gradlew :app:lintDebug`.

**This handoff was created in an environment without Gradle, Android SDK, or external Maven network access. Therefore the Android app has *not* been compiled/installed here.** Until CI or Android Studio returns green, this is a build-ready source handoff rather than a verified APK.

## Repo architecture

```
app/src/main/java/app/gulp/android/
  MainActivity.kt                   // single-activity Compose entry point
  core/design/                     // palette and reusable controls
  core/graphics/                   // static foundation water (Phase 2 replaces it)
  core/navigation/                 // five-tab enum, hand-drawn glyphs and nav
  core/domain/                     // pure rules (logged volume ≠ hydration)
  feature/{home,reminders,history,review,settings}/
app/src/test/                       // unit tests
app/src/androidTest/                // Compose device tests
.github/workflows/android-ci.yml   // build, test, lint and APK artifact
```

## Design authority and next steps

- `docs/DESIGN_CONTRACT.md` is the **approved visual/behavioral baseline**. The included screenshots are historical visual references only, superseded where the contract is more specific.
- `docs/FOUNDATION_ARCHITECTURE.md` records the architecture and remaining decisions.
- `docs/BUILD_VERIFICATION.md` lists checks and sign-off conditions.
- `docs/SCIENCE_RULES.md` protects health language and data integrity for upcoming milestones.
- **Next milestone:** Phase 2 / Turns 4–11, starting with real water simulation, sensor handling, liquid/text shared geometry, and responsive render-rate measurements.

The four Rs—**Ritual, Rhythm, Road, Reflection**—remain internal guiding principles, not the user's tab names.
