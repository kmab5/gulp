# gulp — full Android repository through Phase 4

**Kotlin · Jetpack Compose · local-first hydration reminders · Android 9+ · 0.4.0-phase4**

This is the **complete** project, including Phase 1 foundation, Phase 2 animated liquid interface, Phase 3 recording/customizable Home, and Phase 4 onboarding, reminders, tracking references, and Review analytics. It is source-complete for Phase 4; **Android build and on-device verification are not yet confirmed**. No APK is included.

## Phase 4 additions

- First-run onboarding: choose optional notification reminders, wake and sleep times, and optional recorded-drinks reference. Skip all goals. Medical fluid plans disable generic references.
- Android one-shot **inexact** alarms: configurable 60–240 minute intervals, wakes only within chosen awake window (including overnight windows), suppresses a prompt after a recent logged drink, and never accumulates "missed water".
- Notification actions: `Log 250 mL`, `In 15 min`, `Not now`. Nothing is logged unless user acts. Notifications require runtime permission on Android 13+.
- Reboot, app upgrade, timezone and clock-change rescheduling. No exact-alarm permission, persistent service, internet or account needed.
- Reminders tab edits schedule and quiet hours and shows the next estimated opportunity.
- Review tab computes its seven-day charts, totals, per-calendar-day average, entry count, days with entries, most-logged drink, and optional prior-week comparison from Room records. Empty days mean *not logged*, not *didn't drink*.
- Settings shows and adjusts an optional logged-drink reference independently of the **visual** water fill scale. No hydration-status claims.

## Previous phases retained

- Five screens: Home / Reminders / History / Review / Settings, with fully integrated transparent navigation.
- Procedural full-screen liquid scene, touch ripple feedback, tilt-driven gravity, fill transitions, and pixel-level waterline-aware text/icon contrast as provided in the Phase 2 source.
- Local Room drink records, add/edit/delete/undo, History day selection, reordering/resizing quick actions saved in DataStore.
- No internet permission, account, or remote analytics.

## Run

1. Install **Android Studio**, JDK 17, Gradle **8.13**, and Android API 36 SDK (build-tools appropriate for API 36).
2. Open this `gulp/` folder in Android Studio and sync Gradle.
3. Run the `app` target on Android 9+ emulator or preferably a physical phone.
4. Build or test with `gradle :app:testDebugUnitTest :app:lintDebug :app:assembleDebug` or the repo's `./gradlew` on a network-connected machine. The bootstrap wrapper verifies the official Gradle wrapper JAR on download; the JAR isn't bundled in this archive.
5. Install the debug APK, opt into reminders during onboarding, and allow notifications when asked.
6. Use `docs/PHASE4_DEVICE_TEST.md` to test reboot, locked screen, time changes, quiet hours, and notification actions.

GitHub Actions: `.github/workflows/android-ci.yml` uses SDK 36 and system Gradle 8.13, then uploads `gulp-phase4-debug` if the build passes. Running the workflow requires placing the project in a Git repository with GitHub Actions enabled. **It was not run here.**

## Offline verification

```bash
bash tools/verify_core.sh
bash tools/verify_water.sh
bash tools/verify_phase3.sh
bash tools/verify_repository_offline.sh
bash tools/verify_phase4.sh
python3 tools/verify_structure.py
```

The verification uses the installed Kotlin CLI for pure business rules; the Room repository test uses API substitutes. These checks do **not** validate Compose compilation, KSP processing, real alarm delivery, or Android notifications. No claim of a working APK is made until real Android builds pass.

## Code map

- `app/src/main/java/app/gulp/android/core/reminders/`: AlarmManager, channel, explicit receivers, rescheduling and notification actions.
- `core/domain/ReminderRules.kt`: local-time opportunities, night windows, snooze, late delivery and recent-drink suppression.
- `core/domain/ReviewRules.kt`: real seven-day statistics, no invented values.
- `core/domain/TrackingReference.kt`: goal-free default and optional illustrative references.
- `core/data/GulpPreferences.kt`: persistent onboarding, reminder, and tracking preferences.
- `feature/onboarding/`: short setup flow.
- `feature/reminders/` and `feature/review/`: fully data-driven screen implementations.
- `feature/home/`, `feature/history/`, `core/graphics/`: previous functionality preserved.

See `docs/DESIGN_CONTRACT.md`, `docs/SCIENCE_RULES.md`, `docs/PHASE4_ARCHITECTURE.md`, and `docs/PHASE4_DEVICE_TEST.md` for behavior and acceptance checks. Approved visual references and the research archive remain in `docs/reference/`.

## Not yet built

Phase 5: native Android launcher widgets, full data export/deletion flow, accessibility settings and final configuration polishing. Phase 6: build/device validation, responsiveness, performance/battery optimization, APK/AAB release preparation.

**Important:** On Android 12+, inexact alarms can be delayed by the system and are not guaranteed to fire at the displayed minute. This is intentional for non-urgent drinking prompts; never promise exact timing.
