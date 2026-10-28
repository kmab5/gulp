# gulp — Onboarding, navigation and Home review status

Source baseline: `kmab5/gulp` `main` at `04020121372be1f772cb9cb1e828e1204ec80e1e`, user-pushed UI review 01. Updated for UI review 02 (v0.6.4-rc5).

Status definitions: **Implemented** = source path exists; **follow-up fixed** = this round closed a gap; **alternative** = researched/considered but another approach deliberately used; **device check** = code exists but actual rendering or feel cannot be asserted without testing on Android. No item below is marked device-verified.

## Onboarding

| Feedback | Result | Source / note |
|---|---|---|
| Reminders heading too small/off-center | Implemented (01) | 34sp centered title and 16sp centered subtitle |
| Basic toggle, across app | Implemented (01) | Shared `GulpToggle`/`GulpToggleRow` replace stock toggles in affected screens |
| Static progress bar | Implemented (01) | Animated, spring-free step progress (`animateFloatAsState`) |
| Abrupt reveal after toggle | Implemented (01), device check | `AnimatedVisibility` fade + size changes; real-world clipping remains testable |
| Contrast behind water | **Follow-up fixed (02)** | Headings, subtitle, step count now use `WaterAdaptiveText`; cards keep fixed white text over dark panels |
| Wake/sleep not discoverable | Implemented (01) | Elevated time controls with sunrise/moon illustration, clear wake/bedtime labels |
| Spacing too cramped | Implemented (01), device check | Increased page spacing, titles and button sizes; check smaller screens/large fonts |
| Plain clock/time picker | Alternative (01) | Dark Android-native Material dialog, not a custom time wheel |
| Medical fluid plan misaligned/underemphasized | **Follow-up fixed (02)** | Centered bold 19sp heading, centered description and centered switch |
| Radio same color as switches | **Follow-up fixed (02)** | Amber radio selections, teal animated toggles |
| Custom tracking amount | Implemented (01) + validated (02) | mL field 250–6000, explanatory footnote preserved; Continue disabled for invalid custom mL |
| Inconspicuous Back | Implemented (01) | Dedicated 49dp colored button at upper left |

## Navigation

| Feedback | Result | Source / note |
|---|---|---|
| Outlined / tiny tab labels | Follow-up (02), device check | Label size increased 10→11.5sp; two-pass waterline ink remains by design |
| No visible blur | **Alternative (01)** | Specular/translucent frosted selected item; no real-time Gaussian blur to preserve performance |
| Highlight excludes text | Implemented (01) | Full 69dp selection surface covers glyph and text positions |
| Labels briefly appear then vanish | Implemented (01) | 1800ms delay, 350ms alpha transition after tab change |
| Home doors, Review baseline, sun Settings | Implemented (01) | Custom house silhouette, three bars only, toothed gear |
| Filled current and outlined inactive | Implemented (01) | Separate glyph styles per state |
| Micro-animations per tab | Implemented (01) | Bell ring, clock hand sweep, bar bounce, gear rotation |
| Gentle water wobble | Implemented (01) | `WaterScene.seconds`-derived small synchronized translation; no extra sensor stream |

## Home

| Feedback | Result | Source / note |
|---|---|---|
| Greater tilt or full rotation | **Alternative (01)** | More responsive bounded tilt; 360° inversion deliberately declined to protect legibility |
| No tile blur | **Alternative (01)** | Low-cost layered tinted translucency and specular borders; actual backdrop blur excluded pending profiling |
| More text/icons adapt to moving water | Implemented (01), device check | Adaptive text on quick tiles, + buttons, main action, edit controls and daily total |
| Persistent contrast issues | Follow-up (02), device check | Onboarding foreground follows water; popup retains dark Material scheme; check physical-device contrast |
| Switch label alignment | Implemented (01), device check | Shared vertically centered `GulpToggleRow`, including shortcut editor |
| Edit-mode grid guides | **Follow-up fixed (02)** | Visible rounded bento-slot outlines behind editing tiles, fade smoothly |
| Reorder during drag | Implemented (01), **stabilized (02)** | During-drag hit testing with inset center zone, 85ms swap spacing; Compose placement animation |
| Drag handle misaligned | Follow-up (02) | 42dp centered target, 27sp handle nudged vertically |
| Emphasize Done | Implemented (01) | Bright accent, bold completed action |
| Sticky gulp/next reminder | Implemented (01) | Header outside scrollable content |
| Today header needs space | Follow-up (02) | Extra top separation before Today total |
| Harsh scrolling cutoff | Implemented (01), device check | Offscreen alpha-mask fade at top and bottom of Home scroll |

## Outstanding verification and deliberate exclusions

- **Native Android build:** this environment has no configured Android SDK/Gradle artifact resolution. Run Gradle test, lint and build on the real development machine.
- **Motion feel:** inspect tab changes, repeated rapid taps, sensor tilt, small-screen drag, UI opening/closing and reduced-motion mode on a physical phone. Source presence cannot establish perfect animation smoothness.
- **True backdrop blur:** not implemented. The alternative is composited glass color/lighting for a smaller/less power-intensive app. Reconsider only if device testing shows inadequate visual depth and performance allows it.
- **Entire-app makeover:** only shared controls are propagated. Full visual review of History, Review, Reminders and Settings remains a separate stage as requested.
- **Contrast:** test moving waterline through letters and buttons over both dark and light backgrounds, especially bright/turquoise waves; check accessibility font scale.
