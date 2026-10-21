# gulp · Phase 2 — liquid interaction prototype

**Status: Phase 2 full-repo source delivery. Native build and real-device visual signoff pending.**

This is the continuation of the original Phase 1 Kotlin/Jetpack Compose repo. It preserves the five-tab architecture (Home, Reminders, History, Review, Settings), full design contract, previous research and reference screenshots, tests, and CI pipeline. It now contains a real animated water renderer, shared per-pixel waterline text/icon contrast, touch ripples, gravity sensing, spring-moving selection plate, interruptible fill changes and user/system reduced-motion behavior.

**Important:** The demo water level isn't a measured hydration percentage. This milestone does **not** store drinks. All drink actions remain previews. Native widgets remain part of the **first full release** (a later implementation phase).

## Requirements
- Android Studio with Android Platform SDK **36** and Build Tools **35.0.0**.
- JDK **17**, internet for initial dependency resolution; device API **28+**.
- Toolchain: **Gradle 8.13**, Android Gradle Plugin **8.13.2**, Kotlin **2.2.21**, Compose BOM **2025.10.00**.
- A phone with a gravity sensor gives the best motion experience; accelerometer or static fallback otherwise.

## Run it
1. Extract the archive and open the nested `gulp/` folder in Android Studio.
2. Let Android Studio install the SDK and synchronize Gradle.
3. Run the `app` configuration in an emulator or on a physical Android phone.
4. Use the five tabs, tap the background, and tilt the phone. Home has a **Try water effects** test control; change the slider and toggle **Reduce motion** to check both modes.
5. Tap **Add a drink** to trigger a **preview-only** water rise. It does *not* save anything or change the `0 mL` recorded-drinks display.

CLI:
```
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
./gradlew :app:connectedDebugAndroidTest # requires connected device
```
Windows: `gradlew.bat`. Debug APK (after a successful build): `app/build/outputs/apk/debug/app-debug.apk`.

The repository wrapper bootstrap downloads the official `gradle-wrapper.jar` from Gradle's tag `v8.13.0` and verifies its pinned Git blob hash before running the standard wrapper. This is a stopgap because the original foundation was produced in a sandbox without a standard wrapper binary. CI bypasses wrapper bootstrap by installing Gradle 8.13 via `gradle/actions/setup-gradle`.

## Offline checks run here
```
./tools/verify_core.sh
./tools/verify_water.sh
python3 ./tools/verify_structure.py
```
These verify source/Math, not Android compilation. The authoring container has no Android SDK, Gradle installation, or Maven network access, so **a native build and installation were not possible here**. GitHub Actions `android-ci.yml` is configured to compile/test/lint and publish a debug APK upon running in a real repository.

## Where to look
- `core/graphics/WaterMath.kt` — pure, tested geometry, clamping and damping
- `core/graphics/WaterScene.kt` — remembered, interruptible render state and frame loop
- `core/graphics/WaterBackdrop.kt` — lightweight Compose Canvas liquid and glass
- `core/graphics/WaterAdaptiveContent.kt` — same-surface moving glyph contrast mask
- `core/graphics/WaterSensors.kt` — foreground-only gravity/accelerometer registration
- `core/graphics/WaterInput.kt` — nonconsuming touch observer
- `core/navigation/GulpNav.kt` — transparent animated five-tab bar
- `MainActivity.kt` — lifecycle, screen shell and Water preview controls
- `docs/PHASE2_ARCHITECTURE.md` — implementation decisions and limitations
- `docs/PHASE2_RELEASE_CHECKLIST.md` — precisely what requires Android verification
- `docs/DESIGN_CONTRACT.md` — approved UI decisions, authoritative over old screenshots
- `docs/reference/` — preserved original visual studies
- `docs/reference/evidence/` — research basis for upcoming drinking logic

## What is next
**Phase 3** attaches authentic drinking records and customizable Home content to the water model. There is no fake persistence or timer in this build. Test and tune Phase 2 on a physical Android device first, especially the moving text mask, nav submersion in landscape, effects quality and battery.
