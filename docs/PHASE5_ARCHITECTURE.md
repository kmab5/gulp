# Phase 5 — widgets, privacy and accessibility

## Launcher widgets

- Stable `androidx.glance:glance-appwidget:1.2.0` (Aug 2026 stable); chosen over alpha 1.3 for reliability.
- `widget/GulpWidgets.kt`: small quick-add and wide Today widgets. Both query Room via the existing repository for each widget rendering; both use `GulpWidgetLogAction` for direct logging. Quick actions deliberately use a 700ms in-process duplicate-tap guard.
- Repository add/edit/delete/undo/deleteAll call `GulpWidgetRefresh.refresh` after Room writes. Glance `updateAll` broadcasts static RemoteViews to active hosts; widget renderers never run water animation, orientation sensor or foreground service.
- `GulpWidgetDateReceiver` reacts to date/clock/timezone/boot/app-upgrade system events to keep the daily total accurate without minute-by-minute polling.
- Two widget-provider metadata XML resources advertise launcher choices. `updatePeriodMillis=0` avoids background periodic wakeups. All widgets are labelled as recorded intake, **not hydration state**.
- The launcher, Android version and battery policies may throttle updates or affect sizing: test on-device before shipping.

## Preferences and motion

New DataStore keys store reduceMotion, tiltEnabled, touchRipplesEnabled, strongerContrast, with defaults false/true/true/false. These feed the existing water renderer, sensor lifecycle and reusable pixel-aligned water text masks. Reduced motion also disables the navbar spring and tab crossfade. The system reduced-motion preference takes precedence where detectable.

## Privacy actions

- `ActivityResultContracts.CreateDocument("text/csv")` asks Android to create a user-selected document. No storage permissions or sharing server are used.
- `DrinkCsv` formats all records as `id,timestamp_utc,drink_type,amount_ml`, in ascending UTC timestamp order. No notes or third-party information are exported.
- `DELETE FROM drink_records` uses Room. A confirmation dialog says deletion is permanent, retains app settings, clears undo state, and refreshes widgets. **Deleting records after exporting does not delete the user's exported CSV.**

## Tests and known limits

- Pure Kotlin CSV test and offline DAO substitutes verify exported format, deletion counts and state changes.
- Static checks verify manifest widget receivers, metadata, settings and document picker wiring.
- Real Android build, widget callback/launcher rendering, KSP and lifecycle behavior cannot be proven by these offline checks. See `PHASE5_DEVICE_TEST.md`.
