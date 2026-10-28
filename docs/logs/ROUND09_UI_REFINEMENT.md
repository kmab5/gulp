# Round 9 — UI refinement and verification

Base: GitHub `kmab5/gulp` `main` at `7809005418a247158d1e4962ee83f29c9c847b39`.
Target: 0.6.8-rc9, versionCode 14.

## Requested changes and implementation
- **Scrolling:** removed blurred rectangular overlays; a 22dp feathered `DstIn` fade affects scrolling content only when hidden content exists above/below. No dark overlay. This is deliberately not blur.
- **Editor sheets:** maximum 72% of screen height for scrollable editor content, stable dark blurred surface and high-contrast fields; date/time selectors have 12dp separation and consistent 23dp vector icons. New drink picker can scroll.
- **Gestures:** full-app horizontal gesture observation at `PointerEventPass.Initial`, no event consumption; vertical scroll remains functional; on a clear horizontal gesture, water receives an immediate directional shove and a ripple. Gesture navigation completes only above the threshold. Gestures inside separate modal windows do not switch tabs.
- **Home:** brighter Done treatment; wordmark/submark constrained to a right-aligned vertically centered stable 92×48dp header area.
- **Reminders:** Wake/Sleep are now Lucide sun/moon vectors with a 12dp gap; glass cards share the flat material.
- **Flat glass:** water backdrop blur retained; removed shiny multi-stop tint and reflective raised border gradient.
- **History:** selected clock is filled with negative-space (transparent) hands; calendar icon is recognizable, not a grid.
- **Icons:** 23 selectable tintable icon IDs, 24×24 vectors bundled locally, ISC licensed (see `third_party/LUCIDE_ICONS_ISC.md`). Existing icon IDs and drink-type data are unchanged; no database migration.

## Acceptance checks on phone
1. Swipe left/right from the top header, a card body and empty water. It should change tabs without interrupting vertical scrolling; the water should move in the opposite direction and settle.
2. On long History and Review lists, the top fade should appear after scrolling down, bottom fade while records remain below; no square or dark strip and no effect when everything fits.
3. Onboarding, Home, Settings, Reminders, History and Review: blurred cards should remain flat, without bright rims.
4. Open both drink and shortcut editors at a small font/large font and with the soft keyboard showing. All Save/Delete fields remain scrollable and accessible; content doesn't overrun the device height.
5. Date/time controls in drink editor and History use a recognizable calendar and clock. Wake/Sleep selectors have separated hit targets.
6. Quick-add and drink creation can select from the extended icon collection. Old saved icon IDs still resolve.
7. History selected navbar tab is a filled circle with transparent hands, while inactive is outlined.

## Limits
- No Android SDK was available for compilation in the authoring environment. Native Compose compilation, animation smoothness, and visual contrast require device verification.
- Horizontal scrolling inside complex controls can still require gesture-priority tuning after device tests; the global swipe observer deliberately does not consume touch events.
- No signing key, application ID, Room schema, reminders, or hydration recommendations were changed.
