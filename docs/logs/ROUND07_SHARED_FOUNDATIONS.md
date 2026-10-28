# gulp · Round 07 — Shared foundations (0.6.6-rc7)

GitHub base: `kmab5/gulp` main commit `f5b91e6235b2d0358831743fd7247086dc8944a1`.

## Included

- Navbar-specific rendering, minimum water level, and tap-animation changes are deliberately excluded; the existing navbar is preserved.
- Predominantly horizontal unconsumed swipes change tabs, while a child vertical scroll/gesture gets priority; all screens retain their existing content.
- `conditionalScrollBlur` captures the actual scrolling content in Compose graphics layers, draws it sharp, and overlays a blurred copy only inside a clipped top/bottom strip when content is hidden beyond the respective edge. On API <31 the content remains sharp; no dark cover is drawn.
- Water-aware text covers exposed content across the five tabs and shared text controls. Further visual contrast refinements require device testing.
- New editable `DrinkType` entity with stable ID, name, vector icon ID, default amount and archive status. Water/Coffee/Tea/Other remain stable built-in IDs, but their names, vector icons, and starter amounts can now be customized. A curated self-authored vector set requires no icon library or internet.
- Room v1→v2 migration adds `drink_types` and three snapshot fields to `drink_records`; older amounts/timestamps and kinds are preserved. Snapshot name/icon stop later catalog changes from rewriting prior records. Type archives do not erase history.
- Home shortcuts accept custom drink IDs while still reading 4-field legacy tiles. Home logging, edit sheet, Review breakdown, History labels, widget preset and CSV export use the new catalog. Settings exposes a simple catalog editor and widget preset dialog.

## Not part of this round

Navbar-specific water-level/icon/press effects and screen-level visual redesigns: new shortcut overlay aesthetics, reminder custom interval UI and quiet-hour icons, History date/time navigation, fuller Review dashboard, and Settings sliders/accessibility controls. These remain for the next agreed round.

## Test plan

From project root run:

```text
python tools/verify_drink_types.py
python tools/verify_glass_ui.py
python tools/verify_structure.py
bash tools/verify_phase3.sh
bash tools/verify_repository_offline.sh
python tools/verify_general_round.py
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
```

Offline checks validate source-level and SQLite behavior; they DO NOT prove full Android compilation. Validate the blur and swipe behavior on Android 12+, lower-powered Android 9 devices, in large fonts, and during Home drag-edit mode.

**Safety:** The amount logged is a record of beverages, not a clinical assessment of hydration. Generic recommendations remain optional and never override clinician-directed fluid management.
