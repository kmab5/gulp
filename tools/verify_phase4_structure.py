#!/usr/bin/env python3
"""Checks critical Phase-4 integration; does not emulate Android or test UI behavior."""
from pathlib import Path
import xml.etree.ElementTree as ET
root=Path(__file__).resolve().parent.parent
src=root/'app/src/main/java/com/kmab/gulp'
manifest=root/'app/src/main/AndroidManifest.xml'
text=manifest.read_text()
a='http://schemas.android.com/apk/res/android'
tree=ET.parse(manifest)
receivers=tree.findall('application/receiver')
receiver_names={r.get(f'{{{a}}}name') for r in receivers}
assert '.core.reminders.GulpReminderReceiver' in receiver_names
assert '.core.reminders.GulpReminderBootReceiver' in receiver_names
assert 'android.permission.RECEIVE_BOOT_COMPLETED' in text
assert 'android.permission.POST_NOTIFICATIONS' in text
assert 'android.permission.SCHEDULE_EXACT_ALARM' not in text
assert 'android.permission.INTERNET' not in text
scheduler=(src/'core/reminders/GulpReminderScheduler.kt').read_text()
assert 'setAndAllowWhileIdle' in scheduler
assert 'setExact' not in scheduler
assert 'ACTION_LOG' in scheduler and 'ACTION_SNOOZE' in scheduler and 'ACTION_DISMISS' in scheduler
assert 'consumeNotificationAction' in scheduler and 'markNotificationVisible' in scheduler
assert 'ACTION_BOOT_COMPLETED' in scheduler and 'ACTION_TIMEZONE_CHANGED' in scheduler
prefs=(src/'core/data/GulpPreferences.kt').read_text()
assert 'finishOnboarding' in prefs and 'setReminders' in prefs and 'snoozeUntil' in prefs
assert 'hasCompletedOnboarding' in prefs and 'clinicianManagedFluids' in prefs
assert (src/'feature/onboarding/OnboardingScreen.kt').exists()
review=(src/'feature/review/ReviewScreen.kt').read_text()
assert 'ReviewRules.summarize' in review and 'Day' in review
assert (src/'core/domain/ReminderRules.kt').is_file()
assert (src/'core/domain/ReviewRules.kt').is_file()
assert (src/'core/domain/TrackingReference.kt').is_file()
main=(src/'MainActivity.kt').read_text()
assert 'OnboardingScreen(' in main and 'RemindersScreen(' in main and 'ReviewScreen(' in main
assert 'preferences.reminders' in main and 'GulpReminderScheduler.schedule' in main
# Later phases must preserve Phase 4's working feature contracts while bumping version and CI artifact names.
assert 'versionName = "0.' in (root/'app/build.gradle.kts').read_text()
assert 'gulp-phase' in (root/'.github/workflows/android-ci.yml').read_text()
print('PASS: Phase 4 manifest, alarms/permissions, state & UI wiring static checks')
