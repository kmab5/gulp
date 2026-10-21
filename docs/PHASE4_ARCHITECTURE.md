# Phase 4 architecture and safety contract

## Scheduling

1. `GulpPreferencesStore.settings` is the only settings source: `ReminderPlan` persists in Preferences DataStore, off by default.
2. First launch collects optional awake times, interval, tracking reference, and medical-plan opt out, then asks for notification permission only when reminders are enabled.
3. `MainActivity` reacts to changes in reminder settings/notification permission to schedule **one** upcoming inexact wakeup through `AlarmManager.setAndAllowWhileIdle(RTC_WAKEUP)`; no exact-alarm special access.
4. `GulpReminderReceiver` sees the due timestamp in its immutable explicit alarm PendingIntent, obtains current preferences and latest *past* recorded drink from Room, and conditionally posts a notification. It reschedules the next opportunity.
5. Action button: `Log 250 mL` inserts a Room record, clears an outstanding snooze and schedules again. `In 15 min` persists a one-off snooze. `Not now` dismisses without logging.
6. `GulpReminderBootReceiver` is triggered on boot, app replacement, timezone or wall-clock changes; it reconstructs a single next alarm from persistent settings.
7. Recent drinks (within 45 minutes) suppress a prompt, and prompts delivered after an extended delay are discarded. The user must be within wake hours. The system does not compute drinking deficits or force catches-up.
8. The system stops scheduling if disabled or notifications are denied. Android may delay all inexact alarms in Doze.

Possible future refinements after device tests: explicitly cancel pending alerts when switching into quiet hours, tune recent-drink suppression and inexact alarm lateness based on user feedback; no medical claims about the optimum values.

## Review

- Last seven **local calendar days** ending today, irrespective of weekday; each bar is the sum of Room entries for that calendar day.
- Average per day is seven-day total divided by seven. A blank bar is an *absence of records*, not proof of zero water consumption.
- No previous-week comparison unless the earlier week contains at least one entry.
- Most-logged drink is determined by recorded volume (not count or medical quality).
- Optional tracking references are a user preference; illustrative 1500/1875 mL values are rough calculations of a beverage share of EFSA total-water references, not EFSA's separate fluid prescriptions.
- Visual water fill scale remains independent of the optional numeric tracking reference.

## Limitations

- The app is not a clinical hydration monitor and must not infer dehydration, overhydration, illness, cognitive performance, or urine chemistry from logged drinks.
- Custom clinician-directed fluid limits are not inferred. Users indicating medical fluid management receive no generic goal.
- Standard beverages count as recorded beverage volume; no caffeine multipliers.
- If permission is denied, reminders are turned off at the platform level and the app shows a permission prompt. Logs remain available.
- The app currently has no export-all or wipe-data action; Settings displays those controls as future phase placeholders.

## Scientific references

- EFSA Panel on Dietetic Products, Nutrition and Allergies (2010), Scientific Opinion on Dietary Reference Values for water, DOI 10.2903/j.efsa.2010.1459.
- NASEM (2005), Dietary Reference Intakes for Water, Potassium, Sodium, Chloride, and Sulfate, DOI 10.17226/10925.
- Lally et al. (2010), How are habits formed: Modelling habit formation in the real world, DOI 10.1002/ejsp.674.
- Hew-Butler et al. (2015), Exercise-associated hyponatremia consensus, DOI 10.1136/bjsports-2015-095004.
- Android documentation on inexact alarms and permissions: https://developer.android.com/develop/background-work/services/alarms and https://developer.android.com/develop/ui/views/notifications/notification-permission.

Sources archived in `docs/reference/evidence`. Specific reminder intervals and suppression periods are **product choices**, not clinically proven ideal values.
