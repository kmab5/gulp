# Phase 2 — Graphics architecture and interaction contract

## Deliverable
This is a **running-UI source implementation of the liquid-engine prototype**, not a hydration tracker. There is no database or notification engine yet; the Home "Add a drink" button adjusts demo water level but does not record intake. The 0 mL display intentionally remains 0. The **Water preview** action opens a slider for manual fill testing and a reduced-motion switch.

## Rendering graph
```
GulpApp (persistent root, size measured in physical pixels)
├── WaterScene (single model across five tabs)
│   ├── time & exponential easing for fill / tilt
│   ├── bounded wave impulse history
│   └── user/system reduced-motion choice
├── WaterTiltSensor (foreground-only TYPE_GRAVITY, accelerometer fallback)
├── WaterBackdrop (Compose Canvas, 64 points for the waterline)
├── AnimatedContent (content-only crossfade; does not restart water)
│   └── Home ... Settings
│       └── WaterAdaptiveText (two paint passes, dark clipped underwater)
└── GulpNav (5 equal touch regions, no bar surface)
    ├── 1 smoothly moving selected-plate highlight
    └── WaterAdaptiveText + clipped under-water glyph pass
```

## Shared geometry and coordinate system
The renderer and text masking use `WaterMath.surfaceY` with x and y normalized to the *full Compose viewport*. Root coordinates are derived with `onGloballyPositioned { positionInRoot() }`; clipping is applied only to the draw of the second text/glyph copy, not gesture hitboxes. This makes individual characters split along the moving wave.

The water surface is the sum of an adjustable fill baseline, a tilt-driven linear surface lean, two low-amplitude ambient sinusoids, and short-lived damped wave impulses. Every term is bounded. The math makes no claim to be true CFD or to estimate physical hydration.

The visual minimum fill level is **25% of view height**, so nav often stays underwater at zero logged intake. The value is decorative and must be explained separately from drink totals; device testing must confirm nav coverage in landscape/small screens. Where the panel width or safe area makes this insufficient, Phase 2.1 should derive the floor from measured nav top inset rather than increasing logged volume.

## Input hierarchy
`Modifier.waterTouchFeedback` is installed on the root Compose `Box` and observes down events at `PointerEventPass.Initial`. It does not consume the change. Descendant buttons, drags and scroll containers keep their native pointer gestures. Per-event energy is capped, pulse count is capped at 12, and pulses expire at 2.2 seconds.

Screen transitions use `AnimatedContent` crossfade and the nav highlight uses a damped spring. Physical scene state is *not* recreated on tab switches. The water interpolation can reverse direction mid-animation without jumping.

## Sensor + battery lifecycle
`MainActivity.onResume/onPause` controls `foreground`. No continuous rendering or sensor updates while paused. `WaterTiltSensor` registers an Android gravity sensor while foreground and motion is enabled, falls back to accelerometer, and always unregisters via `DisposableEffect`. Missing sensor means a calm surface. Orientation is mapped through the display rotation. System animation-off preference is honored, while the lab provides an additional manual Reduce motion choice. No location/network permission is required.

## Graphics choices
The effect uses Compose Canvas with procedural geometry and mild gradient glass highlights. The first shippable fallback works on API 28+, rather than requiring AGSL which only covers newer versions. AGSL refraction and improved realistic scattering remain optional polish after measuring frame times on devices. No video background or large asset pack.

## Performance risks and measurement
No SDK available in the authoring environment, so the 60 FPS goal is unverified. On a midrange physical phone: use Profile GPU Rendering, Macrobenchmark, FrameTimingMetric, Android Studio Layout Inspector and memory profiler. Check CPU/GPU during idle and repetitive touches, rapid tilts, scroll during animation, tab tapping, and app background/foreground cycling. Verify pauses when hidden. Pay particular attention to duplicate text paint passes and the O(number of texts × 64) clipping path generation. Cache paths or move to AGSL only if measurements show a need.

## Unimplemented by design
Drinking records, reference goal, notification scheduler, Home tile reordering/resizing, widgets, Review charts, true glass blur, and advanced shader effects. These belong to later milestones. All screens use natural labels. Four Rs are internal planning names, not app navigation.
