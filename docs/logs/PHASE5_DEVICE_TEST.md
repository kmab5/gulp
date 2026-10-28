# Phase 5 — Android device acceptance checklist

Do not mark any item as passed until actually reproduced in Android Studio or on a physical device.

## Build

- [ ] Gradle 8.13 sync completes with SDK 36, Kotlin 2.2.21, KSP, Room and stable Glance 1.2.0.
- [ ] `gradle :app:testDebugUnitTest :app:lintDebug :app:assembleDebug` succeeds.
- [ ] `gradle :app:connectedDebugAndroidTest` passes with Room in-memory database.

## Widgets

- [ ] Add compact Quick add to the home screen; `+250 mL` adds exactly one Room row and updates Home, History, Review and widget.
- [ ] Add wide Today widget; `+250` and `+500` create the exact types/amounts and reflect edits, deletes and undo.
- [ ] Two quick accidental taps do not produce duplicate records when closer than the debounce window. Two intentional taps separated by time create two records.
- [ ] Change time zone, manually change date (test device), restart device: "today" recalculates correctly.
- [ ] Check launcher clipping, font scaling, widget resizing, and darker/lighter launcher wallpapers; no overflow or invisible buttons.
- [ ] With zero widgets placed, Home logging and data editing still work normally.

## Appearance / accessibility

- [ ] Reduce animation persists through restart and suppresses water motion, tab crossfade and navbar spring.
- [ ] Tilt toggle removes sensor reactions when disabled; touch ripple toggle stops gesture feedback; contrast remains readable at waterline.
- [ ] Android system reduced-motion setting takes priority; accessibility screen readers announce all functional controls.
- [ ] Test 200% font, landscape, and minimum supported window width.

## Privacy

- [ ] Export into Downloads and inspect CSV timestamp, amounts, types and line order; confirm no INTERNET/storage permission requested.
- [ ] Cancel the export picker: no file written and no 'success' shown.
- [ ] Cancel delete-all: entries untouched; confirm delete-all: Room cleared, Review/History/Widgets updated and settings remain.
- [ ] Clear records, restart app, then log a fresh drink; no stale undo or widget total.

## Regression and quality

- [ ] Reminder logging through notification updates launcher widgets exactly once.
- [ ] File permission denial or widget-update failure never loses a successful database write.
- [ ] Measure cold launch, frame pacing, idle battery, APK/AAB download size and notification reliability.
