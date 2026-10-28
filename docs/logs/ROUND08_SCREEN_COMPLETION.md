# gulp — Round 08: Screen completion (0.6.7-rc8)

**Baseline:** gulp 0.6.6-rc7, delivered in Round 07. Apply this incremental update after Round 07; do not apply it directly to the unpatched `f5b91e6` GitHub head.

## What changed

| Screen | Changes |
|---|---|
| Navbar | Removed the visual water reserve: no recorded drink can leave the nav above the water. Two-color water masks on icons and labels; press ripple disabled on navigation controls. Increased bounded tilt response. |
| Home | Right-aligned Fluid Nucleus toggle retained. Edit Home is the lighter shared frosted surface. The underlying content soft-blurs while the shortcut editor is visible. Added an inline create-drink action available for Other and other types. |
| Drink/shortcut editors | True procedural-water glass behind sheets, dimmed and blurred underlying UI, creation of custom named drink types, icon chooser and usual volume. Improved date and time affordances in drink editor. |
| Reminders | Custom intervals (30–360 minutes, one-minute increments). Prominent Wake/Sleep controls with icons and glass; quiet hour rules unchanged. |
| History | New previous/date/next glass controls, calendar date picker, icon-labeled records and clear edit affordances. |
| Review | Week summary, 30-day chart, distinct average per *logged* day, most recorded day, type distribution and time-of-day counts. No invented zero-intake assumptions. |
| Settings | 50 mL step display scale slider; onboarding-style tracking reference selection and custom amount. Glass blur, tab swipe and adjustable water motion intensity, all persisted in DataStore. |

## Data and privacy

Round 07 already introduced `DrinkType`: stable ID, name, icon ID, default amount and archived flag. No new database migration is introduced in Round 08. Custom types created from the editors use `GulpRepository.newDrinkType`, backed by the existing Round 07 `DrinkTypeDao`. Old drink records keep snapshots of their original name/icon/type. Customization and display settings use new versioned DataStore keys with safe defaults.

## Design decisions

- Continue to use native procedural water and lightweight in-app recolorable vector icons; do not embed a heavyweight icon library.
- Retain *bounded* phone-tilt physics rather than unrestricted full inversion. Enlarge the tilt range without rotating controls.
- Background blur is selective and may be limited or disabled on older Android versions. Touch targets remain stable.
- A tracking reference is optional and does not measure hydration or prescribe daily water intake.
- Sideways swiping was completed in Round 07. This round adds an explicit preference to disable it.

## Device verification

1. Build and test on API 28 and API 35/36 where possible. No full native Android build was performed in the authoring container.
2. Ensure no disappearing button labels when the waterline crosses the selected nav icon. Inspect both light and dark masks.
3. At 0 mL, water should fall **below** the navbar; after logging it should rise smoothly.
4. Open Tile Editor and Drink Editor. Check backdrop blur and that custom drink creation remains visible on small devices with the keyboard open.
5. Create a drink named `Sparkling Water`, select an icon and default amount, then select it in a shortcut and record it. Restart the app and verify it is still available.
6. Confirm no notification is added without opting in. Try 45, 75 and 135 minute intervals and overnight quiet hours.
7. Verify editing a historical record preserves its time zone/day and can be undone.
8. With no data, Review should say no entries rather than report zero drinking; with data the charts and categories should use the saved records.
9. Confirm glass, swipe and motion controls survive app restart; reduced-motion setting overrides animation intensity.
10. Validate talkback, font scaling, and all 5 navigation labels on a narrow screen.

## Known limits

The system date/time picker is still the Android dark Material dialog, not a custom wheel. Drink creation in an open editor saves the type and keeps the editor open: select the newly created type from the refreshed list before logging. Full performance and GPU blur behavior require a physical Android device.
