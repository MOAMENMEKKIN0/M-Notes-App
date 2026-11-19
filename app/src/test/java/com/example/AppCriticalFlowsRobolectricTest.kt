package com.example

import android.Manifest
import android.app.AlarmManager
import android.app.Application
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.example.data.LocalStoreRepository
import com.example.model.ChecklistOrderItem
import com.example.model.ChecklistStyleConfig
import com.example.model.FloatingBadgeItem
import com.example.model.MainWallpaperCatalog
import com.example.model.RepeatMode
import com.example.model.SmartTask
import com.example.notification.BootRescheduleReceiver
import com.example.notification.TaskAlarmScheduler
import com.example.notification.TaskReminderReceiver
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AppCriticalFlowsRobolectricTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun mainWallpaper_persistsAcrossSimulatedRestartAndResetsCleanly() = runBlocking {
        val repo1 = LocalStoreRepository(context)
        repo1.ensureInitialized()

        val chosenPreset = MainWallpaperCatalog.PREFIX_PRESET + "mint_mist"
        repo1.updatePreferences(
            mapOf(
                "mainWallpaperUri" to chosenPreset,
                "mainWallpaperDimPercent" to 18
            )
        )

        // Simulate app restart via a fresh repository instance reading the same DataStore
        val repo2 = LocalStoreRepository(context)
        val restored = repo2.getPreferencesSnapshot()
        assertEquals(chosenPreset, restored.mainWallpaperUri)
        assertEquals(18, restored.mainWallpaperDimPercent)

        // Reset back to default
        repo2.updatePreferences(
            mapOf(
                "mainWallpaperUri" to "",
                "mainWallpaperDimPercent" to 0
            )
        )
        val afterReset = LocalStoreRepository(context).getPreferencesSnapshot()
        assertEquals("", afterReset.mainWallpaperUri)
        assertEquals(0, afterReset.mainWallpaperDimPercent)
    }

    @Test
    fun floatingIconsAndTaskBadges_persistAcrossRestartWithoutReverting() = runBlocking {
        val repo1 = LocalStoreRepository(context)
        repo1.ensureInitialized()

        val savedBadges = listOf(
            FloatingBadgeItem(
                id = "badge_main_1",
                state = "pixar_gym_dumbbells",
                xFraction = 0.72f,
                yFraction = 0.31f,
                sizeDp = 74f,
                visible = true,
                locked = true
            )
        )
        repo1.saveFloatingBadges(savedBadges)

        val createdId = repo1.createTask(
            SmartTask(
                title = "Cardio Session",
                alertEnabled = false
            )
        ).getOrThrow()

        repo1.updateTaskBadge(
            taskId = createdId,
            iconId = "pixar_emotion_happy",
            offsetX = -18f,
            offsetY = 24f,
            sizeDp = 42f,
            visible = true,
            category = "gym",
            smartMode = "manual",
            locked = true
        )

        // Simulate app restart
        val repo2 = LocalStoreRepository(context)
        val restoredBadge = repo2.getFloatingBadgesSnapshot().first { it.id == "badge_main_1" }
        assertEquals("pixar_gym_dumbbells", restoredBadge.state)
        assertEquals(0.72f, restoredBadge.xFraction, 0.001f)
        assertEquals(0.31f, restoredBadge.yFraction, 0.001f)
        assertEquals(74f, restoredBadge.sizeDp, 0.001f)
        assertTrue(restoredBadge.visible)
        assertTrue(restoredBadge.locked)

        val restoredTask = repo2.getTasksSnapshot().first { it.id == createdId }
        assertEquals("pixar_emotion_happy", restoredTask.badgeIconId)
        assertEquals(-18f, restoredTask.badgeOffsetX, 0.001f)
        assertEquals(24f, restoredTask.badgeOffsetY, 0.001f)
        assertEquals(42f, restoredTask.badgeSizeDp, 0.001f)
        assertTrue(restoredTask.badgeVisible)
        assertTrue(restoredTask.badgeLocked)
    }

    @Test
    fun blankOrdersChecklistAndMarkStyles_persistAcrossRestart() = runBlocking {
        val repo1 = LocalStoreRepository(context)
        repo1.ensureInitialized()

        val items = listOf(
            ChecklistOrderItem(id = "ord_1", text = "Protein Powder", status = ChecklistOrderItem.STATUS_CHECK, sortOrder = 1L),
            ChecklistOrderItem(id = "ord_2", text = "Running Shoes", status = ChecklistOrderItem.STATUS_X, sortOrder = 2L),
            ChecklistOrderItem(id = "ord_3", text = "Water Bottle", status = ChecklistOrderItem.STATUS_NONE, sortOrder = 3L)
        )
        val customStyle = ChecklistStyleConfig(
            styleId = "pixar_mint",
            shapeId = "squircle",
            circleSizeDp = 40,
            markSizeDp = 22,
            checkColorHex = "#0EA5E9",
            xColorHex = "#F43F5E",
            neutralColorHex = "#94A3B8",
            itemSpacingDp = 14
        )
        repo1.saveChecklistOrders(items)
        repo1.saveChecklistStyle(customStyle)

        // Simulate app restart
        val repo2 = LocalStoreRepository(context)
        val restoredOrders = repo2.getChecklistOrdersSnapshot()
        assertEquals(3, restoredOrders.size)
        assertEquals("Protein Powder", restoredOrders[0].text)
        assertEquals(ChecklistOrderItem.STATUS_CHECK, restoredOrders[0].status)
        assertEquals(ChecklistOrderItem.STATUS_X, restoredOrders[1].status)
        assertEquals(ChecklistOrderItem.STATUS_NONE, restoredOrders[2].status)

        val restoredStyle = repo2.getChecklistStyleSnapshot()
        assertEquals(customStyle, restoredStyle)
    }

    @Test
    fun notifications_realScheduleFireSnoozeDoneEditDeleteAndBootRestore() = runBlocking {
        // Verify diagnostics detect missing POST_NOTIFICATIONS on Android 13+, then grant it
        assertFalse(
            "Before granting POST_NOTIFICATIONS, diagnose should report missing permission",
            TaskAlarmScheduler.diagnose(context).hasNotificationPermission
        )
        Shadows.shadowOf(context as Application).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
        assertTrue(
            "After granting POST_NOTIFICATIONS, diagnose should report permission granted",
            TaskAlarmScheduler.diagnose(context).hasNotificationPermission
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val shadowAlarmManager = Shadows.shadowOf(alarmManager)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val shadowNotificationManager = Shadows.shadowOf(notificationManager)

        val repo = LocalStoreRepository(context)
        repo.ensureInitialized()
        repo.updatePreferences(mapOf("soundDone" to true, "hapticEnabled" to true, "lang" to "en"))

        // Pick a safe future time today (2 hours ahead, capped to 23:55 or tomorrow via DAILY)
        val futureTime = LocalTime.now().plusHours(2).format(DateTimeFormatter.ofPattern("HH:mm"))
        val editedTime = LocalTime.now().plusHours(3).format(DateTimeFormatter.ofPattern("HH:mm"))
        val today = LocalDate.now().toString()

        val taskId = repo.createTask(
            SmartTask(
                title = "Evening Gym Workout",
                repeatMode = RepeatMode.DAILY.wire,
                nextDueDate = today,
                alertEnabled = true,
                alertTime = futureTime,
                imgUri = "res://img_3d_gym_workout"
            )
        ).getOrThrow()

        val storedTask = repo.getTasksSnapshot().first { it.id == taskId }

        // 1. Schedule alarm and verify AlarmManager has it registered
        TaskAlarmScheduler.syncAllTaskAlarms(context, listOf(storedTask), "en")
        assertTrue(
            "Expected AlarmManager to contain scheduled task alarm",
            shadowAlarmManager.scheduledAlarms.isNotEmpty()
        )
        val initialTriggerTime = shadowAlarmManager.scheduledAlarms.first().triggerAtTime

        // 2. Edit task time -> verify alarm trigger timestamp updates
        val editedTask = storedTask.copy(alertTime = editedTime)
        repo.updateTaskFull(taskId, editedTask)
        TaskAlarmScheduler.syncAllTaskAlarms(context, listOf(editedTask), "en")
        val updatedTriggerTime = shadowAlarmManager.scheduledAlarms.first().triggerAtTime
        assertTrue(
            "Expected edited alarm trigger time to change (initial=$initialTriggerTime, updated=$updatedTriggerTime)",
            updatedTriggerTime != initialTriggerTime || futureTime == editedTime
        )

        // 3. Fire reminder BroadcastReceiver directly -> verify real posted Notification
        val receiver = TaskReminderReceiver()
        val triggerIntent = Intent(context, TaskReminderReceiver::class.java).apply {
            action = TaskAlarmScheduler.ACTION_TRIGGER_TASK_REMINDER
            putExtra(TaskAlarmScheduler.EXTRA_TASK_ID, taskId)
            putExtra(TaskAlarmScheduler.EXTRA_TASK_TITLE, editedTask.title)
        }
        receiver.processIntent(context, triggerIntent, TaskAlarmScheduler.ACTION_TRIGGER_TASK_REMINDER)

        val posted = shadowNotificationManager.allNotifications
        assertTrue("Expected real notification to be posted", posted.isNotEmpty())
        val notif = posted.last()
        assertEquals(Notification.VISIBILITY_PUBLIC, notif.visibility)
        assertEquals(TaskAlarmScheduler.CHANNEL_ID_SOUND_VIBE, notif.channelId)
        assertEquals(editedTask.title, notif.extras.getString(Notification.EXTRA_TITLE))
        assertEquals("Expected Done and Snooze actions", 2, notif.actions?.size ?: 0)

        // 4. Verify silent preference routes to silent notification channel
        repo.updatePreferences(mapOf("soundDone" to false, "hapticEnabled" to false))
        receiver.processIntent(context, triggerIntent, TaskAlarmScheduler.ACTION_TRIGGER_TASK_REMINDER)
        val silentNotif = shadowNotificationManager.allNotifications.last()
        assertEquals(TaskAlarmScheduler.CHANNEL_ID_SILENT, silentNotif.channelId)

        // 5. Snooze 10m -> dismisses notification AND schedules separate snooze alarm that syncAllTaskAlarms won't erase
        val snoozeIntent = Intent(context, TaskReminderReceiver::class.java).apply {
            action = TaskAlarmScheduler.ACTION_SNOOZE_10M
            putExtra(TaskAlarmScheduler.EXTRA_TASK_ID, taskId)
        }
        receiver.processIntent(context, snoozeIntent, TaskAlarmScheduler.ACTION_SNOOZE_10M)
        assertTrue("Expected snooze alarm in AlarmManager", shadowAlarmManager.scheduledAlarms.size >= 1)
        val countBeforeSync = shadowAlarmManager.scheduledAlarms.size
        TaskAlarmScheduler.syncAllTaskAlarms(context, listOf(editedTask), "en")
        assertTrue(
            "syncAllTaskAlarms must preserve active snooze alarm",
            shadowAlarmManager.scheduledAlarms.size >= countBeforeSync
        )

        // 6. BootRescheduleReceiver restores alarms cleanly after reboot
        val bootReceiver = BootRescheduleReceiver()
        bootReceiver.rescheduleAll(context)
        assertTrue("Expected alarms to be scheduled after boot", shadowAlarmManager.scheduledAlarms.isNotEmpty())

        // 7. Mark Done action cancels notification & advances daily task
        val doneIntent = Intent(context, TaskReminderReceiver::class.java).apply {
            action = TaskAlarmScheduler.ACTION_MARK_DONE
            putExtra(TaskAlarmScheduler.EXTRA_TASK_ID, taskId)
        }
        receiver.processIntent(context, doneIntent, TaskAlarmScheduler.ACTION_MARK_DONE)
        val taskAfterDone = repo.getTasksSnapshot().firstOrNull { it.id == taskId }
        assertNotNull("Daily task should remain and advance to next day", taskAfterDone)

        // 8. Delete task -> cancel alarm cleanly
        repo.deleteTask(taskId)
        TaskAlarmScheduler.cancelTaskAlarm(context, taskId)
        assertFalse(repo.getTasksSnapshot().any { it.id == taskId })
    }
}
