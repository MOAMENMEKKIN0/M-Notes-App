package com.example.notification

import android.Manifest
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.model.RepeatMode
import com.example.model.SmartTask
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class NotificationDiagnostics(
    val hasNotificationPermission: Boolean,
    val areNotificationsEnabled: Boolean,
    val canScheduleExactAlarms: Boolean,
    val isIgnoringBatteryOptimizations: Boolean
) {
    val isFullyReady: Boolean
        get() = hasNotificationPermission && areNotificationsEnabled && canScheduleExactAlarms
}

/**
 * Native Android exact alarm & notification channel manager.
 * Guarantees task reminders fire on time even when the app is swiped closed,
 * in the background, in Doze mode, or on a locked screen, while respecting
 * sound & vibration preferences and preserving active snooze alarms.
 */
object TaskAlarmScheduler {

    const val CHANNEL_ID_SOUND_VIBE = "mnotes_reminders_v3_sound_vibe"
    const val CHANNEL_ID_SOUND_ONLY = "mnotes_reminders_v3_sound_only"
    const val CHANNEL_ID_VIBE_ONLY = "mnotes_reminders_v3_vibe_only"
    const val CHANNEL_ID_SILENT = "mnotes_reminders_v3_silent"

    // Legacy alias kept for compatibility
    const val CHANNEL_ID = CHANNEL_ID_SOUND_VIBE

    const val ACTION_TRIGGER_TASK_REMINDER = "com.example.action.TRIGGER_TASK_REMINDER"
    const val ACTION_TEST_REMINDER = "com.example.action.TEST_REMINDER"
    const val ACTION_MARK_DONE = "com.example.action.MARK_TASK_DONE"
    const val ACTION_SNOOZE_10M = "com.example.action.SNOOZE_TASK_10M"

    const val EXTRA_TASK_ID = "extra_task_id"
    const val EXTRA_TASK_TITLE = "extra_task_title"
    const val EXTRA_TASK_META = "extra_task_meta"
    const val EXTRA_TASK_IMG = "extra_task_img"
    const val EXTRA_LANG = "extra_lang"
    const val EXTRA_ALERT_TASK_ID = "extra_open_alert_task_id"
    const val EXTRA_IS_SNOOZE = "extra_is_snooze"

    private const val PREFS_NAME = "mnotes_scheduled_alarms"
    private const val KEY_TRACKED_IDS = "tracked_task_ids"
    private const val TEST_ALARM_REQUEST_CODE = 790_001
    private const val SNOOZE_OFFSET = 500_000

    fun channelIdForPreferences(soundEnabled: Boolean, vibrationEnabled: Boolean): String = when {
        soundEnabled && vibrationEnabled -> CHANNEL_ID_SOUND_VIBE
        soundEnabled -> CHANNEL_ID_SOUND_ONLY
        vibrationEnabled -> CHANNEL_ID_VIBE_ONLY
        else -> CHANNEL_ID_SILENT
    }

    fun ensureNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        val notifSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val audioAttrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val pattern = longArrayOf(0, 320, 160, 320, 160, 420)

        val specs = listOf(
            Triple(CHANNEL_ID_SOUND_VIBE, "Task Reminders (Sound & Vibration)", true to true),
            Triple(CHANNEL_ID_SOUND_ONLY, "Task Reminders (Sound Only)", true to false),
            Triple(CHANNEL_ID_VIBE_ONLY, "Task Reminders (Vibration Only)", false to true),
            Triple(CHANNEL_ID_SILENT, "Task Reminders (Silent)", false to false)
        )

        specs.forEach { (id, name, flags) ->
            if (nm.getNotificationChannel(id) != null) return@forEach
            val (useSound, useVibe) = flags
            val channel = NotificationChannel(
                id,
                name,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Scheduled task reminders and alerts that appear reliably on the lock screen."
                enableLights(true)
                enableVibration(useVibe)
                if (useVibe) {
                    vibrationPattern = pattern
                }
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                if (useSound) {
                    setSound(notifSound, audioAttrs)
                } else {
                    setSound(null, null)
                }
                setShowBadge(true)
            }
            nm.createNotificationChannel(channel)
        }
    }

    /**
     * Pure, deterministic next-trigger computation for any [SmartTask].
     * Returns epoch milliseconds in [zone], or null if the task has no upcoming reminder.
     * Handles same-minute saves cleanly: if the user saves a reminder matching the current
     * HH:mm and it has not alerted today yet, schedules 2 seconds ahead instead of skipping it.
     */
    fun computeNextTriggerMillis(
        task: SmartTask,
        now: LocalDateTime = LocalDateTime.now(),
        zone: ZoneId = ZoneId.systemDefault()
    ): Long? {
        if (!task.alertEnabled) return null
        val alertLocalTime = parseAlertTime(task.alertTime) ?: return null

        val today = now.toLocalDate()
        val todayStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val parsedNextDue = task.nextDueDate
            .takeIf { it.isNotBlank() }
            ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

        val minDate = if (parsedNextDue != null && parsedNextDue.isAfter(today)) parsedNextDue else today

        fun adjustSameMinuteOrFuture(dt: LocalDateTime, dateStr: String): LocalDateTime? {
            val alreadyAlertedToday = dateStr == todayStr && task.lastAlertedDate == todayStr
            if (alreadyAlertedToday) return null
            if (dt.isAfter(now)) return dt
            val isSameMinuteToday = dateStr == todayStr &&
                dt.hour == now.hour &&
                dt.minute == now.minute
            return if (isSameMinuteToday) now.plusSeconds(2) else null
        }

        val candidateDateTime: LocalDateTime? = when {
            task.usesWeekDays() -> {
                (0L..7L).firstNotNullOfOrNull { offset ->
                    val d = minDate.plusDays(offset)
                    val jsDay = (d.dayOfWeek.value % 7).toLong()
                    if (!task.weekDays.contains(jsDay)) return@firstNotNullOfOrNull null
                    val dStr = d.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    adjustSameMinuteOrFuture(LocalDateTime.of(d, alertLocalTime), dStr)
                }
            }
            task.repeat == RepeatMode.DAILY -> {
                val base = LocalDateTime.of(minDate, alertLocalTime)
                val baseStr = minDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                adjustSameMinuteOrFuture(base, baseStr) ?: base.plusDays(1)
            }
            task.repeat == RepeatMode.WEEKLY -> {
                val base = LocalDateTime.of(minDate, alertLocalTime)
                val baseStr = minDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                adjustSameMinuteOrFuture(base, baseStr) ?: base.plusWeeks(1)
            }
            task.repeat == RepeatMode.CYCLE -> {
                val base = LocalDateTime.of(minDate, alertLocalTime)
                val baseStr = minDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                adjustSameMinuteOrFuture(base, baseStr) ?: base.plusDays(1)
            }
            else -> {
                val targetDate = parsedNextDue ?: today
                val targetStr = targetDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                adjustSameMinuteOrFuture(LocalDateTime.of(targetDate, alertLocalTime), targetStr)
            }
        }

        return candidateDateTime?.atZone(zone)?.toInstant()?.toEpochMilli()
    }

    internal fun parseAlertTime(raw: String): LocalTime? {
        val parts = raw.trim().split(":")
        if (parts.size < 2) return null
        val h = parts[0].toIntOrNull() ?: return null
        val m = parts[1].toIntOrNull() ?: return null
        if (h !in 0..23 || m !in 0..59) return null
        return LocalTime.of(h, m)
    }

    fun requestCodeForTask(taskId: String): Int {
        // Stable positive request code bounded so offsets (+1..+4, +SNOOZE_OFFSET) never overflow
        val base = (taskId.hashCode() and 0x0FFF_FFFF)
        return if (base < 10_000) base + 10_000 else base
    }

    fun snoozeRequestCodeForTask(taskId: String): Int = requestCodeForTask(taskId) + SNOOZE_OFFSET

    fun notificationIdForTask(taskId: String): Int = requestCodeForTask(taskId)

    fun scheduleTaskAlarm(
        context: Context,
        task: SmartTask,
        lang: String = "ar",
        overrideTriggerAtMillis: Long? = null,
        isSnooze: Boolean = false
    ): Long? {
        ensureNotificationChannel(context)
        val appCtx = context.applicationContext
        val am = appCtx.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return null
        val triggerAt = overrideTriggerAtMillis ?: computeNextTriggerMillis(task)
        if (triggerAt == null) {
            if (!isSnooze) {
                cancelRegularTaskAlarmOnly(appCtx, task.id)
            }
            return null
        }

        val pi = buildReminderPendingIntent(appCtx, task, lang, isSnooze)
        val reqCode = if (isSnooze) snoozeRequestCodeForTask(task.id) else requestCodeForTask(task.id)
        scheduleExactOrAlarmClock(appCtx, am, triggerAt, pi, task.id, reqCode)
        trackTaskId(appCtx, task.id)
        return triggerAt
    }

    /**
     * Cancels BOTH the regular scheduled alarm AND any active snooze alarm for [taskId].
     */
    fun cancelTaskAlarm(context: Context, taskId: String) {
        val appCtx = context.applicationContext
        cancelRegularTaskAlarmOnly(appCtx, taskId)
        cancelSnoozeAlarmOnly(appCtx, taskId)
        untrackTaskId(appCtx, taskId)
    }

    fun cancelSnoozeAlarmOnly(context: Context, taskId: String) {
        val appCtx = context.applicationContext
        val am = appCtx.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(appCtx, TaskReminderReceiver::class.java).apply {
            action = ACTION_TRIGGER_TASK_REMINDER
            putExtra(EXTRA_TASK_ID, taskId)
            putExtra(EXTRA_IS_SNOOZE, true)
        }
        val pi = PendingIntent.getBroadcast(
            appCtx,
            snoozeRequestCodeForTask(taskId),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        am.cancel(pi)
        pi.cancel()
    }

    private fun cancelRegularTaskAlarmOnly(context: Context, taskId: String) {
        val appCtx = context.applicationContext
        val am = appCtx.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(appCtx, TaskReminderReceiver::class.java).apply {
            action = ACTION_TRIGGER_TASK_REMINDER
            putExtra(EXTRA_TASK_ID, taskId)
        }
        val pi = PendingIntent.getBroadcast(
            appCtx,
            requestCodeForTask(taskId),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        am.cancel(pi)
        pi.cancel()
    }

    /**
     * Synchronizes regular alarms for all tasks WITHOUT cancelling active snooze alarms
     * on existing tasks (deleted or disabled tasks DO have both regular and snooze cancelled).
     */
    fun syncAllTaskAlarms(context: Context, tasks: List<SmartTask>, lang: String = "ar") {
        val appCtx = context.applicationContext
        ensureNotificationChannel(appCtx)

        val currentIds = tasks.map { it.id }.toSet()
        val previouslyTracked = getTrackedTaskIds(appCtx)
        // Cancel alarms (regular + snooze) for tasks that were deleted
        (previouslyTracked - currentIds).forEach { staleId ->
            cancelTaskAlarm(appCtx, staleId)
        }
        // Schedule or cancel each current task based on its alert state
        tasks.forEach { task ->
            if (task.alertEnabled && task.alertTime.isNotBlank()) {
                scheduleTaskAlarm(appCtx, task, lang, isSnooze = false)
            } else {
                cancelTaskAlarm(appCtx, task.id)
            }
        }
    }

    fun scheduleTestNotificationInSeconds(
        context: Context,
        delaySeconds: Int = 5,
        lang: String = "ar"
    ): Long {
        val appCtx = context.applicationContext
        ensureNotificationChannel(appCtx)
        val am = appCtx.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val triggerAt = System.currentTimeMillis() + (delaySeconds.coerceIn(2, 60) * 1000L)
        val intent = Intent(appCtx, TaskReminderReceiver::class.java).apply {
            action = ACTION_TEST_REMINDER
            putExtra(EXTRA_LANG, lang)
        }
        val pi = PendingIntent.getBroadcast(
            appCtx,
            TEST_ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (am != null) {
            scheduleExactOrAlarmClock(appCtx, am, triggerAt, pi, null, TEST_ALARM_REQUEST_CODE)
        }
        return triggerAt
    }

    private fun buildReminderPendingIntent(
        context: Context,
        task: SmartTask,
        lang: String,
        isSnooze: Boolean
    ): PendingIntent {
        val intent = Intent(context, TaskReminderReceiver::class.java).apply {
            action = ACTION_TRIGGER_TASK_REMINDER
            putExtra(EXTRA_TASK_ID, task.id)
            putExtra(EXTRA_TASK_TITLE, task.displayTitle())
            putExtra(EXTRA_TASK_META, task.meta)
            putExtra(EXTRA_TASK_IMG, task.imgUri)
            putExtra(EXTRA_LANG, lang)
            putExtra(EXTRA_IS_SNOOZE, isSnooze)
        }
        val reqCode = if (isSnooze) snoozeRequestCodeForTask(task.id) else requestCodeForTask(task.id)
        return PendingIntent.getBroadcast(
            context,
            reqCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun scheduleExactOrAlarmClock(
        context: Context,
        am: AlarmManager,
        triggerAtMillis: Long,
        operation: PendingIntent,
        taskId: String?,
        requestCodeBase: Int
    ) {
        val canExact = canScheduleExactAlarms(context)
        if (canExact) {
            try {
                // Highest priority wake on locked screens & Doze
                val showIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    if (!taskId.isNullOrBlank()) {
                        putExtra(EXTRA_ALERT_TASK_ID, taskId)
                    }
                }
                val showPi = PendingIntent.getActivity(
                    context,
                    requestCodeBase + 1,
                    showIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val info = AlarmManager.AlarmClockInfo(triggerAtMillis, showPi)
                am.setAlarmClock(info, operation)
                return
            } catch (_: SecurityException) {
                // Fall through to setExactAndAllowWhileIdle
            }
            try {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation)
                return
            } catch (_: SecurityException) {
                // Fall through to inexact allow-while-idle
            }
        }
        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation)
    }

    // ---- Permission & Device Delivery Health Checks ----

    fun diagnose(context: Context): NotificationDiagnostics {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        val enabled = nm?.areNotificationsEnabled() ?: true
        val perm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        return NotificationDiagnostics(
            hasNotificationPermission = perm,
            areNotificationsEnabled = enabled,
            canScheduleExactAlarms = canScheduleExactAlarms(context),
            isIgnoringBatteryOptimizations = isIgnoringBatteryOptimizations(context)
        )
    }

    fun hasNotificationPermission(context: Context): Boolean {
        val diag = diagnose(context)
        return diag.hasNotificationPermission && diag.areNotificationsEnabled
    }

    fun canScheduleExactAlarms(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val am = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            am?.canScheduleExactAlarms() ?: true
        } else {
            true
        }
    }

    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            pm?.isIgnoringBatteryOptimizations(context.packageName) ?: true
        } else {
            true
        }
    }

    fun openNotificationSettings(context: Context) {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
    }

    fun openExactAlarmSettings(context: Context) {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
    }

    /**
     * Play-policy-compliant battery optimization settings shortcut (opens system list,
     * does NOT require REQUEST_IGNORE_BATTERY_OPTIMIZATIONS permission).
     */
    fun openBatteryOptimizationSettings(context: Context) {
        val primary = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val fallback = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(primary) }.onFailure {
            runCatching { context.startActivity(fallback) }
        }
    }

    private fun getTrackedTaskIds(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_TRACKED_IDS, emptySet())?.toSet() ?: emptySet()
    }

    private fun trackTaskId(context: Context, taskId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val updated = getTrackedTaskIds(context) + taskId
        prefs.edit().putStringSet(KEY_TRACKED_IDS, updated).apply()
    }

    private fun untrackTaskId(context: Context, taskId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val updated = getTrackedTaskIds(context) - taskId
        prefs.edit().putStringSet(KEY_TRACKED_IDS, updated).apply()
    }
}
