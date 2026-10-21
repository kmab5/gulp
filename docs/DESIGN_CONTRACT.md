# gulp — approved design contract v0.3 (2026-10-20)

This contract overrides the rejected first STILLWATER concept and any conflicting older documents. **Approved: visual direction and structure; actual motion must be validated on hardware.**

## Visual language
- Entire screen is the interior of a bottle. Navy/dark above, turquoise water below. Soft glass sheen around edges; mild glass effects only when they improve clarity.
- Water rises from the bottom as logged fluid increases relative to the user's *optional tracking reference*. A decorative minimum underwater region keeps nav submerged even at 0 recorded ml; decorative water is NOT logged intake.
- Water gently moves at rest. Taps, pressing a control, navigating, moving a tile, and logging all cause proportionate reactions. Phone tilt changes apparent gravity. There must be no abrupt transitions.
- Text/icons under water have the **opposite contrasting treatment** of those above. The correct implementation is an *animated mask using the same water surface geometry* so individual glyphs can be bisected by waves. Static foundation does not implement this yet.
- Navigation: **Home · Reminders · History · Review · Settings**. Plain names, sans-serif. A selected tab may have a raised frosted rounded-square plate. Crucially: **no navbar island or enclosing background**. At most a subtle bottom-center radial blur that fades into the screen.
- Nav layer blends into the canvas and remains visually submerged except where rotation exposes it; base water level is not a claim about consumption.
- Main Home action: prominently placed **Add a drink** near the top. Quick logging tiles for water, coffee, custom quantity, user-configurable drink presets. A layout editing mode allows intuitive drag/reorder/resize, inspired by lnk.moe's bento-like configurability (not a copy of its visuals).
- No serif typography, Roman numerals, mystic phrases, filler slogans, blinking status dot, radar decoration, verbose product vanity copy, or scientific jargon in everyday navigation.

## Interaction/motion contract
- All animations should be interruptible and state-based, not abrupt/timeline-only. Touch response is immediate and proportional.
- Ripple on open space; heavier wave when adding a drink; tiny perturbation when changing tabs; drag subtly displaces surface; orientation is filtered/smoothed to remove sensor jitter.
- Initial motion *hypotheses* (tune on device): controls 180–240 ms; tabs 240–320 ms; water rise 700–1100 ms; dialogs 300–420 ms; grid settle <=350 ms. These are not final facts.
- Stop animation / unregister sensors when not visible; respect user/system reduced-motion and battery saver. Contrast, legibility and accessible alternatives must survive reduced effects.
- Navigation must not steal gestures; water input should observe at an appropriate pass instead of intercepting button taps and scrolling.

## Navigation responsibilities
| Tab | Intent | Phase 1 shell | Full planned feature |
|---|---|---|---|
| Home | Add and track drinks | yes, Room + DataStore | water scene, real record, customizable grid |
| Reminders | Manage schedule | yes, preview-only | interval, quiet hours, pause/snooze |
| History | See/edit entries | yes, empty state | entries, calendar, correction/undo |
| Review | Analyze logged patterns | yes, empty state | weekly/monthly charts and summaries |
| Settings | Customize app and data | partially implemented | display scale and effects; other controls later |

## Four Rs (internal philosophy only)
- **Ritual**: effortless, gratifying primary action.
- **Rhythm**: reminders that respect a normal day and quiet time.
- **Road**: visual progression with no pressure to overdrink or to preserve streaks.
- **Reflection**: accurate, digestible summaries of recorded activity.

## Source of truth
- Approved static mockups from design exploration 02, kept under `docs/reference/` for visual comparison.
- This signed-off contract supersedes the mockups where they conflict: **five tabs, fully seamless navbar, live text mask, background water touch feedback, gulp branding**.
- This Phase 1 implementation is intentionally a scaffolding view. `StaticWaterBackdrop.kt` is not the approved water-engine implementation.

## Phase 2 engine (implemented, not yet approved on device)
- Geometry: `WaterMath.surfaceY` is shared by `WaterBackdrop` and `waterBelowClip`.
  Light glyphs are painted first; underwater ink is painted by a clipped second drawing.
- Touch: a parent observes the *Initial* pass without consuming any pointer events.
- Gravity: sensor listener exists only while activity is resumed and motion is enabled;
  gravity sensor is preferred, accelerometer fallback. Missing sensor means calm water.
- Scene state survives navigation and animations are interruptible via smooth state
  updates and spring-based nav selection. Four atmospheric actions remain previews.
- Minimum underwater layer is decorative; it is not a user hydration indicator.
- Shader-free Compose Canvas baseline serves API 28+; on-device profiling remains required.
- The Water preview dialog exposes fill and reduced-motion controls for testing only.
- User's design preferences about bottom-center radial-only optional haze and no island remain binding.

## Phase 3 status (2026-10-20)

Home now logs real drinks with configurable quick-add tiles, and History reads and edits true Room records. The shared waterline mask and transparent navigation remain mandatory. All other feature stubs continue to identify their limitations. Refer to `PHASE3_ARCHITECTURE.md` for the database and visual-scale definitions.
