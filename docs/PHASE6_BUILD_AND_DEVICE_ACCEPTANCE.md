# gulp v0.6.0-rc1 — build and device acceptance

Status: **Unverified Android release candidate source**. The authoring container had Java/Kotlin but no Android SDK, Gradle installation or dependency-network access. Do not treat source assertions as evidence that the Android app compiles or that animations meet FPS targets. User will run the first native build.

## Build on your own system

1. Extract the ZIP and open the inner `gulp/` folder in Android Studio.
2. Configure JDK 17, SDK Platform 36, and Build Tools 35.0.0. AGP 8.13.2 uses Gradle 8.13. Internet connectivity is required for initial Android and Gradle dependencies.
3. Sync Gradle. Run `app` on a physical phone with Android 9 or higher.
4. From a terminal with Gradle 8.13: `gradle :app:testDebugUnitTest :app:lintDebug :app:assembleDebug`.
5. To use wrapper scripts instead of installed Gradle: `./gradlew ...` (macOS/Linux) or `gradlew.bat ...` (Windows). **First execution downloads the official Gradle wrapper bootstrap JAR from GitHub and checks its Git blob SHA**, followed by the official Gradle 8.13 distribution. No bootstrap JAR has been embedded in this ZIP. If GitHub is blocked, use the installed Gradle 8.13 instead.
6. `gradle :app:connectedDebugAndroidTest` requires an emulator or phone. First-launch navigation tests seed the onboarding flag.
7. `gradle :app:assembleRelease :app:bundleRelease` requires local root `build-key` and `keystore.properties` and builds **signed** optimized artifacts. See `docs/RELEASE_SIGNING.md`.

## Native compilation and functionality gates (all currently unchecked)

- [ ] Gradle sync, Room KSP, Compose compilation, Glance compilation and resource linking succeed.
- [ ] Android unit tests, lint, `assembleDebug`, `assembleRelease`, `bundleRelease` succeed.
- [ ] `connectedDebugAndroidTest` passes (Room in-memory, five tabs, add sheet).
- [ ] Fresh install onboarding completes (with and without notifications), then persists on force-close and relaunch.
- [ ] Tap/swipe effects, tilt orientation and reduced-motion behavior are convincing; no movement resets on quick tab switches.
- [ ] A newly logged drink smoothly changes water fill; all adaptive ink/icons mask correctly along the *actual* surface.
- [ ] Home next reminder is accurate and "Off" when disabled; Settings > Customize shortcuts starts Home edit mode, with layout persisted after relaunch.
- [ ] All drink add/edit/delete/undo actions update Home, History, Review and every placed launcher widget; older record edits don't change today's total unexpectedly.
- [ ] Date picker permits selecting month-end dates without crashes (e.g. May 31 -> February 1).
- [ ] Reminders opt in and stop on opt out; disabling clears posted notifications. Snooze/dismiss/action tokens prevent unwanted duplicate actions. Recent-drink suppression and quiet hours work.
- [ ] If "Medical fluid plan" is enabled, generic goals and reminders are disabled, and an old notification action cannot log new water.
- [ ] Cross DST and time zone changes on an emulator: future reminders preserve *local* wake times and widgets show the correct local "today".
- [ ] Widget receivers appear in launcher picker; both widths, shortcuts and summaries render without clipping on at least two launchers.
- [ ] Export CSV, cancel export, confirm/cancel record deletion, app restart; no internet or extra storage permissions used.
- [ ] Screen reader announces all controls; 200% text scaling and small screens don't obscure critical actions; contrast remains legible above/below water.
- [ ] Measure 60 FPS frame pacing (P50/P95/P99) on reference mid-range phone, inspect thermal/battery when idle and during tilt, confirm sensor is unregistered when backgrounded.
- [ ] Inspect AAB/APK compressed sizes with `python3 tools/report_package_size.py` and ideally Play's per-device download size before enforcing a size budget.

## Release boundary

The debug variant uses `com.kmab.gulp.debug`; the eventual release uses `com.kmab.gulp`. Production signing, Play Store listing, policy review, and real-device performance measurements remain outside what can be honestly verified in this environment. **Do not publish without these checks.**
