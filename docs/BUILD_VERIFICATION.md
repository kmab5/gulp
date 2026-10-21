# Verification and sign-off

## Completed here
- Static XML parse for Manifest, themes, icon assets.
- Gradle TOML and app configuration structural checks (via `tools/verify_structure.py`).
- Local Kotlin 1.9 compiler execution of pure model smoke tests (via `tools/verify_core.sh`).
- Source tree and archive integrity.

## NOT VERIFIED HERE
- Gradle plugin resolution, Kotlin 2.2 Compose compiler, and Android resource compilation: environment lacks Android SDK/Gradle/Maven connectivity.
- Android instrumentation interactions on actual emulator/phone.
- AGSL, sensor input, live text mask, motion, notifications, widgets (not built in Phase 1).

## Build proof needed for full Phase 1 acceptance
1. Run GitHub Actions workflow or Android Studio Gradle sync; fix anything detected.
2. `./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug` succeeds.
3. Install with `adb install -r app/build/outputs/apk/debug/app-debug.apk`.
4. Verify the five tabs, no opaque nav container, and no layout overlap on a real Android 9+ device.
5. Verify background stays stable on tab switches, rotate screen and check state restoration, and open preview-only modal.
6. Review device screenshots against `docs/DESIGN_CONTRACT.md`; do not mark the graphics experience approved until Phase 2.

The CI workflow is *configured*, not executed in this environment. It publishes a debug APK after successful compilation.
