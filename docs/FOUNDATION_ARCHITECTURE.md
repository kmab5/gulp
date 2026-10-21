# Phase 1 architecture and technical decisions

## Deliberately small app architecture
- One `:app` Gradle module; feature-oriented packages, domain objects separated from UI, native Android Kotlin/Compose UI.
- All screen state in this foundation uses saveable Compose state. The selected tab survives config changes/process restoration where Android has state to restore. No global singleton navigation manager or invented backend.
- `StaticWaterBackdrop` is drawn once below the screen transition box and navbar; replacing it with the Phase 2 stateful renderer requires no changes to feature interfaces.
- Five labels in `GulpTab` form an ordinary user-facing vocabulary. Internal terminology never leaks into the navigation labels.
- No login, API clients, ads, background services, runtime permissions or network dependencies in the app APK itself.
- The app makes no health-related calculations yet. The pure `RecordedProgress` type is the future domain boundary and explicitly names *logged / reference* fraction, not hydration.

## Stack rationale
| Need | Choice | Why |
|---|---|---|
| Android-first integration | Kotlin, native Activity | reliable sensors, launcher widgets and notifications later |
| Rich reactive UI | Compose + Canvas | shared rendering geometry, custom gestures and accessibility |
| Smooth transition | Compose animation + future frame-state renderer | interrupts without tearing screens apart |
| Future GPU shader | AGSL when API >=33, Canvas fallback below | preserves Android 9+ without a heavy 3D runtime |
| Later persistent storage | Room + DataStore (not included yet) | strongly typed local ledger and small preference store |
| Later scheduled reminders | AlarmManager/WorkManager after battery testing | schedule respecting quiet hours, do not request exact-alarm permission without evidence |
| Later home-screen widgets | Glance (planned first complete release) | native low-power widgets, same local record rules |

Build toolchain currently intentionally uses mature AGP 8.13.2 and Compose BOM 2025.10.00 with compile/target SDK 36, so it doesn't require API 37 / AGP 9. Move forward only with explicit dependency verification.

## Required downstream domain architecture
- `DrinkEntry(id, timestamp, timezone, volumeMl, beverageType)` persisted with Room; no hydration scores stored.
- A single append/update/delete entry use case that widgets, in-app loggers and notifications all invoke through the same repository.
- `DailyLoggedVolume` aggregates records by local date with clear timezone semantics. An empty day is 'no entries', not 'zero actual consumption'.
- `ReminderPolicy` emits candidates given waking hours, chosen interval, quiet time and recent entries; Android scheduler is an adapter. No water debt and no catch-up triggers.
- `RenderState` receives current data plus temporary impulse events. Touch ripples never modify `DrinkEntry`. One shared `WaterSurface` shape drives fill, cover and text masking.
- `SettingsRepository` later stores explicit user preferences, including reduced motion and quick tile ordering.

## Engineering budget
- Aim for 60 fps on target mid-range hardware, interruptible low-jitter motion, app-specific bytes under 20 MB device-specific download; these are future measured goals, not Phase 1 performance claims.
- Use profiler, frame timing and Macrobenchmark after the graphics engine is implemented. Foreground-only animation/sensors.
- The static Phase 1 art is built in Compose drawing primitives and tiny vector assets; no animations-as-video, extra render engine or oversized icon bundle.
