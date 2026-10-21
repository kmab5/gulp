# Phase 4 device checklist — must pass before phase is considered Android-verified

## Build and launch

- [ ] Run `gradle :app:testDebugUnitTest :app:lintDebug :app:assembleDebug` on Android SDK-equipped machine and fix ALL failures.
- [ ] Install on Android 9, Android 13, Android 15/16 when available.
- [ ] First-run onboarding has no flashing old app screen, can skip all goals and notifications; once completed it doesn't reappear on restart.
- [ ] Notify permission asked only after opting in; denial leaves app usable and Reminders shows why alerts cannot arrive.

## Reminder behavior

- [ ] Choose wake/sleep and 60/90/120/180/240 min; verify next displayed opportunity updates.
- [ ] Phone sleeping / screen locked: scheduled notifications arrive *approximately*, allowing normal system delays.
- [ ] Notification action `Log 250 mL` inserts exactly ONE 250 mL record that appears in Home, History and Review after launch.
- [ ] `In 15 min` snoozes; snooze inside quiet hours suppresses the notification.
- [ ] `Not now` makes no record. Changing interval or pausing cancels/reschedules pending alert.
- [ ] Recently logged drink suppresses reminder within 45 minutes.
- [ ] Airplane mode and no internet: all essential functionality remains available.
- [ ] Reboot and manual timezone/time changes do not create duplicate reminders or alerts in quiet hours.
- [ ] System notification permission off: no scheduling/wakeups; re-grant and reopening app re-enables scheduling.

## Analytics and references

- [ ] Log drinks across midnight and DST; History and Review use *local day* correctly.
- [ ] Seven-day chart and summary match real records; empty-day messaging does not imply actual zero intake.
- [ ] Previous-week comparison absent until older week contains records.
- [ ] Change optional reference or enable clinician-managed plan; no generic medical goal shown afterward.
- [ ] App unchanged water graphics, text masks, touch reactions and navigation animation remain functional.

## Pending engineering validation

- [ ] Investigate Android OEM restrictions on background receivers and inexact alarm delays.
- [ ] Test rotation, dynamic text sizes, low-memory process recreation and dark/light waterline readability.
- [ ] Profiling battery/render smoothness and verifying package-size target remain in Phase 6.
