# Phase 3: persistence, Home and History

## Scope
- Real local drink events: type, amount in mL, timestamp and stable database ID.
- Add and quick-log actions; edit amount/type/time; delete; snackbar Undo.
- History with local-calendar-day selection, stored records and daily total.
- Editable quick-add tiles: add, remove, resize, reorder by hold-and-drag, modify drink and amount.
- Presets and a purely **visual** water-display scale saved in Preferences DataStore.
- Approved Phase 2 liquid engine is retained: sensor tilt, tap disturbances, shared waterline ink mask and transparent navigation.

## Exact semantics
The Home water level is `sum(logged drink volume today) / visualCapacityMl`, clamped to [0,1].
The background reserves an independent decorative minimum for the navigation area, even when the records sum to zero. This is explicitly **NOT a hydration score**, not a true proportion of body water, and not a medical goal. There is no catch-up behavior.

Drink kinds WATER/COFFEE/TEA/OTHER count entered volume with no invented hydration multipliers. Food-derived water is not tracked. The user can modify old timestamps; local day grouping uses the current device timezone, including daylight-saving rules through `java.time`.

Room v1 database name: `gulp_records.db`, entity `drink_records`. Date/time represented as UTC epoch millis. Invalid volume of 0 or > 5000 mL is rejected. No network permission is needed.

Preferences file: `gulp_settings.preferences_pb` via `preferencesDataStore`. Home tiles use an internal ID, kind, amount and width. The encoding only uses controlled enum identifiers and generated alphanumeric IDs. Damaged/empty preset strings safely fall back to defaults. No login or remote sync.

## Undo and concurrency
Repository writes use a coroutine Mutex. Each operation returns a single-use UndoTicket; currently the newest mutation invalidates previous UndoTickets. The UI dismisses the previous snackbar when a new action completes, so the most recent action can be undone without offering an obsolete ticket. Undo state is intentionally ephemeral and is not persisted across process death. A user can always repair a record in History.

## User interaction
- Home primary action opens a bottom sheet with selectable drink kind, editable volume, and date/time.
- Quick tile adds immediately and shows an Undo snackbar.
- Edit Home toggles the customizable grid; long-press-drag a tile to its destination or tap to modify type, amount, wide/small. Add/remove up to 10 tiles (minimum 1). `Done` flushes edits to DataStore.
- History defaults to today; Previous/Next and tap-date open navigation. Each record opens an edit/delete sheet.
- Settings allows changing the visual fill scale between 250 and 6000 mL in 250 mL increments, and provides a temporary water-animation test control. The visual scale is not a health recommendation.

## Non-goals (next phases)
- Automatic reminders/notifications and Review reporting.
- Onboarding, Android widgets, export/import and final accessibility/performance tuning.

## Verification
- `bash tools/verify_core.sh`
- `bash tools/verify_water.sh`
- `bash tools/verify_phase3.sh`
- `bash tools/verify_repository_offline.sh` (Room API stubs, not native instrumentation)
- `python3 tools/verify_structure.py`
- CI Gradle tasks `:app:testDebugUnitTest :app:lintDebug :app:assembleDebug` and instrumented repository/UI tests on an Android emulator.

Android compilation was not possible on the handoff container without Android SDK/Gradle artifacts and networking. Structural and pure Kotlin checks are **not** evidence that Compose/Room integration compiles; a real CI or Android Studio build must be performed before Phase 3 can be formally certified.
