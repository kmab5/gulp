# gulp — Onboarding, Home and navigation review (2026-10-21)

Scope: the first **real-device UI review** based on the supplied photos. Other tab layouts remain intentionally out of scope; the shared controls and dark Material theme used by those tabs have been updated.

## Changes

- Safe-area adaptive launcher mark: centered lowercase-g liquid loop, themed icon, no cut-off.
- Global dark Material 3 color scheme and dark Android dialog theme. Native time/date pickers are requested in Android's dark Material dialog theme.
- Consistent **GulpToggle**, **GulpChoiceChip**, **GulpChoiceRow** and larger labeled switch rows instead of stock Material switches, checkboxes, radio buttons and filter chips.
- Onboarding: centered hierarchy, animated progress, animated conditional content, visually distinct wake/bedtime buttons, prominent back affordance, custom amount with validation, retained research caveats, medical-plan setting.
- Navigation: seamless transparent region, full-height highlighted selected destination including label space, brief labels after navigation, filled/outline glyph variations, per-destination icon gestures, small water-synchronized drift.
- Home: fixed reminder/gulp header, feathered scroll clipping using offscreen alpha mask (no solid-colored top/bottom overlays), tile and action waterline-aware glyph/text, real-time grid reordering with lazy-grid placement animations, edit outlines, emphasized Done action and relaxed total spacing.
- Water rotation: tilt influence increased while keeping the UI upright and surface values bounded. 360-degree liquid inversion is intentionally **not** simulated.

## On-device acceptance checklist

- [ ] Launcher icon has breathing room when cropped by circle/squircle masks; themed icon legible.
- [ ] Onboarding Reminders header is centered and sufficiently large at 360 dp width and font scale 1.3.
- [ ] Toggling reminders animates controls into/out of the page without overlap or clipping.
- [ ] Wake/Bedtime picker appears in dark mode on the device; time can be saved.
- [ ] Medical plan setting turns off generic tracking goals and reminders; switching it off restores choice without inventing a target.
- [ ] Custom reference 250–6000 mL saves; invalid values prevent proceeding.
- [ ] Toggling a branded switch triggers one state change and is accessible to TalkBack.
- [ ] Nav icons appear as correct filled/outline variants, and short animation does not obstruct navigation.
- [ ] Nav labels appear on first render and tab change, then fade after around 1.8 seconds.
- [ ] Underwater icon/text pixels become dark while above-water pixels stay light.
- [ ] Active nav highlight surrounds both icon and label area; no large floating navbar background is visible.
- [ ] Dragging a tile across the center of a neighbor changes order before release; the other cards move smoothly.
- [ ] Reordered tiles persist after leaving edit mode, force-stop/restart, and rotating the device.
- [ ] Holding a handle doesn't log a drink; tapping a normal tile still logs one record.
- [ ] Scroll edge masks taper content without abruptly drawing an opaque screen-colored rectangle.
- [ ] Reminder/gulp header stays still as the shortcut area scrolls.
- [ ] Test water tilt at ±45° and ±90° and then return flat; edges must not flicker.
- [ ] Settings' reduced-motion toggle stops motion and does not disable the underlying functions.

## Rendering trade-off

The translucent panels use layered gradients, tinted translucency and specular borders. This creates a frosted-glass look without re-rendering/blur-filtering a full-screen scene behind **every tile** on each water frame; a true dynamic backdrop Gaussian blur is a separate performance-heavy effect and is **not claimed** in this revision. Test the look on an actual Android display before considering a more expensive GPU pass.

## Build and packaging

Run `./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug` on the Android SDK-equipped machine. There was no Android SDK or Gradle resolution available in the authoring environment, so **native compilation and fluidity are not verified** here. Run the device checklist before approving any redesign of other tabs.

## Review 02: additional device checks

- [ ] With the water crossing the opening Reminders heading and step count, each character switches to readable submerged ink at the moving waterline.
- [ ] The clinician-plan heading and its toggle are visually centered; disabling/enabling the plan remains accessible with TalkBack.
- [ ] Radio choices use a warm amber focus indication distinct from the blue-green reminder switches.
- [ ] Selecting Custom amount and entering a value outside 250–6000 mL visibly disables Continue; valid values re-enable it.
- [ ] Home edit-mode guide slots fade into place and remain behind tiles as tiles reorder during a long-press drag.
- [ ] Dragging over a neighbor's central area triggers live reflow without jitter at tile boundaries.
- [ ] Nav labels remain legible at 360dp and 1.3× font scale. The adaptive waterline edge should not look like an outline.
- [ ] Check the extra gap above Today and the top/bottom scroll fade at large accessibility font scales.

See `UI_FEEDBACK_AUDIT.md` for the full item-by-item matrix, including intentional visual alternatives.
