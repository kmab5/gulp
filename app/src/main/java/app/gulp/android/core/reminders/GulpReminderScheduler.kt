package app.gulp.android.core.reminders

import android.Manifest
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import app.gulp.android.MainActivity
import app.gulp.android.R
import app.gulp.android.core.data.GulpPreferencesStore
import app.gulp.android.core.data.GulpRepository
import app.gulp.android.core.domain.DrinkKind
import app.gulp.android.core.domain.ReminderPlan
import app.gulp.android.core.domain.ReminderRules
import java.time.ZoneId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Inexact one-shot alarms avoid exact-alarm special access and a continuously running service. */
object GulpReminderScheduler {
    const val CHANNEL_ID = "gulp_drink_reminders_v1"
    const val ACTION_TICK = "app.gulp.android.REMINDER_TICK"
    const val ACTION_SNOOZE = "app.gulp.android.REMINDER_SNOOZE"
    const val ACTION_LOG = "app.gulp.android.REMINDER_LOG"
    const val ACTION_DISMISS = "app.gulp.android.REMINDER_DISMISS"
    const val EXTRA_DUE = "expected_due_at"
    const val EXTRA_ACTION_TOKEN = "notification_action_token"
    const val NOTIFICATION_ID = 761

    private fun tickIntent(context: Context) = Intent(context, GulpReminderReceiver::class.java).setAction(ACTION_TICK)

    fun pendingTick(context: Context): PendingIntent = PendingIntent.getBroadcast(
        context, 761, tickIntent(context), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    private fun cancelAlarm(context: Context) {
        val alarms = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarms.cancel(pendingTick(context))
    }

    fun cancel(context: Context) {
        cancelAlarm(context)
        clearNotification(context)
    }

    fun schedule(context: Context, plan: ReminderPlan, now: Long = System.currentTimeMillis()) {
        cancelAlarm(context)
        if (!plan.enabled || !notificationsAllowed(context)) {
            clearNotification(context)
            return
        }
        val next = ReminderRules.nextAlarm(now, ZoneId.systemDefault(), plan) ?: return
        val operation = PendingIntent.getBroadcast(
            context, 761, tickIntent(context).putExtra(EXTRA_DUE, next),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarms = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next, operation)
    }

    fun createChannel(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(CHANNEL_ID, "Drink reminders", NotificationManager.IMPORTANCE_DEFAULT)
        channel.description = "Optional, gentle reminders during your chosen waking hours"
        nm.createNotificationChannel(channel)
    }

    fun notificationsAllowed(context: Context): Boolean =
        Build.VERSION.SDK_INT < 33 || context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    private fun action(context: Context, code: Int, action: String, label: String, token: Long): Notification.Action {
        val intent = Intent(context, GulpReminderReceiver::class.java).setAction(action)
            .putExtra(EXTRA_ACTION_TOKEN, token)
        val pending = PendingIntent.getBroadcast(context, code, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        return Notification.Action.Builder(null, label, pending).build()
    }

    fun showNotification(context: Context, token: Long) {
        if (!notificationsAllowed(context)) return
        createChannel(context)
        val open = PendingIntent.getActivity(context, 762, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_drop)
            .setContentTitle("A quick water break?")
            .setContentText("Drink if you'd like. You can always skip this one.")
            .setContentIntent(open).setAutoCancel(true).setOnlyAlertOnce(true)
            .addAction(action(context, 763, ACTION_LOG, "Log 250 mL", token))
            .addAction(action(context, 764, ACTION_SNOOZE, "In 15 min", token))
            .addAction(action(context, 765, ACTION_DISMISS, "Not now", token))
            .build()
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .notify(NOTIFICATION_ID, notification)
    }

    fun clearNotification(context: Context) {
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(NOTIFICATION_ID)
    }
}

/** Receivers finish quickly; goAsync allows the Room/DataStore operations to finish off main thread. */
class GulpReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        val app = context.applicationContext
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val prefs = GulpPreferencesStore(app)
                val repo = GulpRepository.create(app)
                val now = System.currentTimeMillis()
                val current = prefs.snapshot().reminders
                when (intent.action) {
                    GulpReminderScheduler.ACTION_TICK -> {
                        val due = intent.getLongExtra(GulpReminderScheduler.EXTRA_DUE, 0L)
                        if (due > 0L && ReminderRules.shouldNotify(now, due, ZoneId.systemDefault(), current, repo.latestTimeMillis())) {
                            prefs.markNotificationVisible(due)
                            GulpReminderScheduler.showNotification(app, due)
                        }
                        if (current.snoozeUntilMillis != 0L && current.snoozeUntilMillis <= now) {
                            prefs.snoozeUntil(0L)
                        }
                    }
                    GulpReminderScheduler.ACTION_LOG -> {
                        if (!current.enabled) {
                            GulpReminderScheduler.clearNotification(app)
                            return@launch
                        }
                        if (!prefs.consumeNotificationAction(intent.getLongExtra(GulpReminderScheduler.EXTRA_ACTION_TOKEN, 0L))) return@launch
                        // A button press records a drink; receiving an ordinary reminder never does.
                        repo.add(DrinkKind.WATER, 250, now)
                        GulpReminderScheduler.clearNotification(app)
                        prefs.snoozeUntil(0L)
                    }
                    GulpReminderScheduler.ACTION_SNOOZE -> {
                        if (!current.enabled) {
                            GulpReminderScheduler.clearNotification(app)
                            return@launch
                        }
                        if (!prefs.consumeNotificationAction(intent.getLongExtra(GulpReminderScheduler.EXTRA_ACTION_TOKEN, 0L))) return@launch
                        GulpReminderScheduler.clearNotification(app)
                        prefs.snoozeUntil(now + 15 * 60_000L)
                    }
                    GulpReminderScheduler.ACTION_DISMISS -> {
                        if (!prefs.consumeNotificationAction(intent.getLongExtra(GulpReminderScheduler.EXTRA_ACTION_TOKEN, 0L))) return@launch
                        GulpReminderScheduler.clearNotification(app)
                        prefs.snoozeUntil(0L)
                    }
                    else -> return@launch
                }
                GulpReminderScheduler.schedule(app, prefs.snapshot().reminders)
            } catch (failure: Exception) {
                android.util.Log.e("gulp-reminders", "Reminder action failed", failure)
            } finally {
                pending.finish()
            }
        }
    }
}

/** Reschedules a single upcoming reminder after restart, wall-clock or timezone changes. */
class GulpReminderBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val allowedActions = setOf(Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED, Intent.ACTION_MY_PACKAGE_REPLACED)
        if (intent.action !in allowedActions) return
        val pending = goAsync()
        val app = context.applicationContext
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                GulpReminderScheduler.schedule(app, GulpPreferencesStore(app).snapshot().reminders)
            } catch (failure: Exception) {
                android.util.Log.e("gulp-reminders", "Couldn't reschedule reminder", failure)
            } finally { pending.finish() }
        }
    }
}
