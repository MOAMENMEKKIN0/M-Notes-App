package com.example.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.LocalStoreRepository
import com.example.localization.AppStrings
import com.example.model.RepeatMode
import com.example.ui.components.decodeTaskPhotoBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Handles scheduled task alarms, test lock-screen reminders, and notification quick actions
 * (Mark Done / Snooze 10 min) even when the app process was completely closed or the phone is locked.
 */
class TaskReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        val appCtx = context.applicationContext
        TaskAlarmScheduler.ensureNotificationChannel(appCtx)

        val pendingResult = goAsync()
        val pm = appCtx.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val wakeLock = runCatching {
            pm?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "mnotes:task_reminder_wl")
                ?.apply { acquire(5_000L) }
        }.getOrNull()

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                when (action) {
                    TaskAlarmScheduler.ACTION_TRIGGER_TASK_REMINDER -> {
                        handleTaskReminder(appCtx, intent)
                    }
                    TaskAlarmScheduler.ACTION_TEST_REMINDER -> {
                        val lang = intent.getStringExtra(TaskAlarmScheduler.EXTRA_LANG) ?: "ar"
                        showTestLockScreenNotification(appCtx, lang)
                    }
                    TaskAlarmScheduler.ACTION_MARK_DONE -> {
                        val taskId = intent.getStringExtra(TaskAlarmScheduler.EXTRA_TASK_ID).orEmpty()
                        if (taskId.isNotBlank()) {
                            handleMarkDoneAction(appCtx, taskId)
                        }
                    }
                    TaskAlarmScheduler.ACTION_SNOOZE_10M -> {
                        val taskId = intent.getStringExtra(TaskAlarmScheduler.EXTRA_TASK_ID).orEmpty()
                        if (taskId.isNotBlank()) {
                            handleSnoozeAction(appCtx, taskId)
                        }
                    }
                }
            } finally {
                runCatching {
                    if (wakeLock?.isHeld == true) wakeLock.release()
                }
                pendingResult.finish()
            }
        }
    }

    private suspend fun handleTaskReminder(context: Context, intent: Intent) {
        val taskId = intent.getStringExtra(TaskAlarmScheduler.EXTRA_TASK_ID).orEmpty()
        val isSnooze = intent.getBooleanExtra(TaskAlarmScheduler.EXTRA_IS_SNOOZE, false)
        if (taskId.isBlank()) return

        val repo = LocalStoreRepository(context)
        val prefs = repo.getPreferencesSnapshot()
        val lang = prefs.lang
        val storedTask = repo.getTasksSnapshot().firstOrNull { it.id == taskId }

        // If the task was deleted or alerts were disabled since scheduling, do not fire
        if (storedTask == null || !storedTask.alertEnabled) {
            TaskAlarmScheduler.cancelTaskAlarm(context, taskId)
            return
        }

        val today = AppStrings.todayStr()
        if (!isSnooze) {
            repo.markTaskAlerted(storedTask.id, today)
        }

        val title = storedTask.displayTitle().ifBlank {
            intent.getStringExtra(TaskAlarmScheduler.EXTRA_TASK_TITLE).orEmpty()
                .ifBlank { if (lang == "en") "Task Reminder" else "تذكير بمهمة" }
        }
        val timeText = if (storedTask.alertTime.isNotBlank()) {
            AppStrings.formatTime(storedTask.alertTime, lang)
        } else {
            storedTask.meta
        }
        val bodyText = buildString {
            if (timeText.isNotBlank()) {
                append(timeText)
            }
            if (storedTask.repeat != RepeatMode.NONE) {
                if (isNotEmpty()) append(" • ")
                append(
                    when (storedTask.repeat) {
                        RepeatMode.DAILY -> if (lang == "en") "Daily" else "يومياً"
                        RepeatMode.WEEKLY -> if (lang == "en") "Weekly" else "أسبوعياً"
                        RepeatMode.CYCLE -> if (lang == "en") "Workout Cycle" else "دورة تمرين"
                        RepeatMode.NONE -> ""
                    }
                )
            }
            if (isEmpty()) {
                append(if (lang == "en") "It's time for your scheduled task!" else "حان وقت مهمتك المجدولة الآن!")
            }
        }

        val largeIcon = decodeTaskPhotoBitmap(context, storedTask.imgUri)

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(TaskAlarmScheduler.EXTRA_ALERT_TASK_ID, storedTask.id)
        }
        val contentPi = PendingIntent.getActivity(
            context,
            TaskAlarmScheduler.requestCodeForTask(storedTask.id) + 2,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val doneIntent = Intent(context, TaskReminderReceiver::class.java).apply {
            action = TaskAlarmScheduler.ACTION_MARK_DONE
            putExtra(TaskAlarmScheduler.EXTRA_TASK_ID, storedTask.id)
        }
        val donePi = PendingIntent.getBroadcast(
            context,
            TaskAlarmScheduler.requestCodeForTask(storedTask.id) + 3,
            doneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(context, TaskReminderReceiver::class.java).apply {
            action = TaskAlarmScheduler.ACTION_SNOOZE_10M
            putExtra(TaskAlarmScheduler.EXTRA_TASK_ID, storedTask.id)
        }
        val snoozePi = PendingIntent.getBroadcast(
            context,
            TaskAlarmScheduler.requestCodeForTask(storedTask.id) + 4,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(context, TaskAlarmScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_bell)
            .setContentTitle(title)
            .setContentText(bodyText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bodyText))
            .apply {
                if (largeIcon != null) {
                    setLargeIcon(largeIcon)
                }
            }
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_LIGHTS)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 350, 180, 350, 180, 500))
            .setContentIntent(contentPi)
            .addAction(
                0,
                if (lang == "en") "Done" else "تم الإنجاز",
                donePi
            )
            .addAction(
                0,
                if (lang == "en") "Snooze 10m" else "غفوة ١٠ د",
                snoozePi
            )
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        if (TaskAlarmScheduler.hasNotificationPermission(context)) {
            nm?.notify(TaskAlarmScheduler.notificationIdForTask(storedTask.id), notification)
        }

        // Immediately schedule the next occurrence for recurring tasks so they keep working
        // even if the user never opens the app!
        if (!isSnooze && storedTask.repeat != RepeatMode.NONE) {
            val updatedTask = storedTask.copy(lastAlertedDate = today)
            TaskAlarmScheduler.scheduleTaskAlarm(context, updatedTask, lang)
        }
    }

    private suspend fun handleMarkDoneAction(context: Context, taskId: String) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        nm?.cancel(TaskAlarmScheduler.notificationIdForTask(taskId))

        val repo = LocalStoreRepository(context)
        val prefs = repo.getPreferencesSnapshot()
        val task = repo.getTasksSnapshot().firstOrNull { it.id == taskId } ?: return
        val today = AppStrings.todayStr()

        when (task.repeat) {
            RepeatMode.CYCLE -> {
                val steps = task.cycleSteps
                val nextIdx = if (steps.isNotEmpty()) ((task.cycleIndex + 1L) % steps.size) else 0L
                val tomorrow = AppStrings.addDaysStr(1)
                val nextDate = if (task.usesWeekDays()) AppStrings.nextWeekDay(task.weekDays, tomorrow) else tomorrow
                repo.advanceTaskSchedule(task.id, nextIdx, nextDate, today)
            }
            RepeatMode.DAILY -> {
                repo.advanceTaskSchedule(task.id, task.cycleIndex, AppStrings.addDaysStr(1), today)
            }
            RepeatMode.WEEKLY -> {
                val nextDate = if (task.usesWeekDays()) {
                    AppStrings.nextWeekDay(task.weekDays, AppStrings.addDaysStr(1))
                } else {
                    AppStrings.addDaysStr(7)
                }
                repo.advanceTaskSchedule(task.id, task.cycleIndex, nextDate, today)
            }
            RepeatMode.NONE -> {
                repo.deleteTask(task.id)
            }
        }

        val freshTasks = repo.getTasksSnapshot()
        TaskAlarmScheduler.syncAllTaskAlarms(context, freshTasks, prefs.lang)
    }

    private suspend fun handleSnoozeAction(context: Context, taskId: String) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        nm?.cancel(TaskAlarmScheduler.notificationIdForTask(taskId))

        val repo = LocalStoreRepository(context)
        val prefs = repo.getPreferencesSnapshot()
        val task = repo.getTasksSnapshot().firstOrNull { it.id == taskId } ?: return
        val snoozeTriggerAt = System.currentTimeMillis() + 10 * 60 * 1000L
        TaskAlarmScheduler.scheduleTaskAlarm(
            context = context,
            task = task,
            lang = prefs.lang,
            overrideTriggerAtMillis = snoozeTriggerAt,
            isSnooze = true
        )
    }

    private fun showTestLockScreenNotification(context: Context, lang: String) {
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val contentPi = PendingIntent.getActivity(
            context,
            790_002,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (lang == "en") "Background Reminders Active" else "إشعارات الخلفية وقفل الشاشة تعمل بنجاح"
        val body = if (lang == "en") {
            "Your scheduled task reminders will appear on time even when the app is closed or the screen is locked."
        } else {
            "ستصلك تنبيهات مهامك وتمارينك في موعدها حتى عند إغلاق التطبيق أو قفل الشاشة."
        }

        val notification = NotificationCompat.Builder(context, TaskAlarmScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_bell)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(contentPi)
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        if (TaskAlarmScheduler.hasNotificationPermission(context)) {
            nm?.notify(790_003, notification)
        }
    }
}
