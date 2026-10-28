# Phase 3 Android acceptance checklist

Run on an actual device or emulator after a successful Gradle build.

1. First install: opens with Home, empty total, decorative water reserve only. No fake records.
2. Add water 250 mL with main button. Check Home reads 250, water rises smoothly, History shows same event.
3. Quick-add 500 mL repeatedly. Tap Undo for last entry and verify both Home and History revert together.
4. In History edit one drink to tea, different volume, yesterday and a different time. Check local date groups and total.
5. Delete a record, then Undo; check record ID/time/amount reappear once.
6. Test malformed quantities (blank, 0, >5000 mL) cannot be saved; verify button disabled.
7. Edit Home: add tea, change to 350 mL, widen; move shortcut by long press and drag, delete another tile. Press Done.
8. Force stop/relaunch/rotate device: record data and tile configuration persist; undo does not persist (by design).
9. Change display scale in Settings: only visual water fill changes; the mL total stays unchanged.
10. Rapidly log twice from shortcuts; ensure each record appears once. Verify latest Undo is shown.
11. Tilt and tap while scrolling Home; no stuck gestures, water motion/ink masking remains continuous.
12. Navigate Home → History → Settings → Home repeatedly, including during fill animation.
13. Switch device timezone and re-open; dates regroup correctly. Test a daylight-saving change where available.
14. Accessibility: screen reader can reach main Add and quick tiles; large text and compact device fit without cropped actions.
15. Check logs for crashes, DB corruption, frame jank and excessive idle sensor activity.

NOTE: no device test results are claimed by this handoff.
