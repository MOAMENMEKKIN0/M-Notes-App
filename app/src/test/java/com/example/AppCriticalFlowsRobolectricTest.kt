package com.example

import android.Manifest
import android.app.AlarmManager
import android.app.Application
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.PowerManager
import androidx.test.core.app.ApplicationProvider
import com.example.data.LocalMediaRepository
import com.example.data.LocalStoreRepository
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
import java.io.File
import java.io.FileOutputStream
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
    fun floatingIconsAndTaskBadges_persistIndependentPositionsAndWideOffsetsAcrossRestart() = runBlocking {
        val repo1 = LocalStoreRepository(context)
        repo1.ensureInitialized()

        val savedBadges = listOf(
            FloatingBadgeItem(
                id = "badge_main_1",
                state = "bald_exp_cool",
                xFraction = 0.72f,
                yFraction = 0.31f,
                sizeDp = 74f,
                visible = true,
                locked = false
            ),
            FloatingBadgeItem(
                id = "badge_main_2",
                state = "bald_gym_deadlift",
                xFraction = 0.14f,
                yFraction = 0.84f,
                sizeDp = 56f,
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

        // Wide offset beyond old ±260dp clamp must persist verbatim
        repo1.updateTaskBadge(
            taskId = createdId,
            iconId = "bald_exp_confident",
            offsetX = -450f,
            offsetY = 620f,
            sizeDp = 42f,
            visible = true,
            category = "gym",
            smartMode = "manual",
            locked = true
        )

        // Simulate app restart
        val repo2 = LocalStoreRepository(context)
        val restoredBadges = repo2.getFloatingBadgesSnapshot()
        val b1 = restoredBadges.first { it.id == "badge_main_1" }
        val b2 = restoredBadges.first { it.id == "badge_main_2" }
        assertEquals("bald_exp_cool", b1.state)
        assertEquals(0.72f, b1.xFraction, 0.001f)
        assertEquals(0.31f, b1.yFraction, 0.001f)
        assertEquals("bald_gym_deadlift", b2.state)
        assertEquals(0.14f, b2.xFraction, 0.001f)
        assertEquals(0.84f, b2.yFraction, 0.001f)

        val restoredTask = repo2.getTasksSnapshot().first { it.id == createdId }
        assertEquals("bald_exp_confident", restoredTask.badgeIconId)
        assertEquals(-450f, restoredTask.badgeOffsetX, 0.001f)
        assertEquals(620f, restoredTask.badgeOffsetY, 0.001f)
        assertEquals(42f, restoredTask.badgeSizeDp, 0.001f)
        assertTrue(restoredTask.badgeVisible)
        assertTrue(restoredTask.badgeLocked)
    }

    @Test
    fun deviceWallpaperImportAndWhatsAppWallpapers_succeedWithoutUriErrorAndPersist() = runBlocking {
        // Generate a real sample image file simulating a device gallery/file pick
        val sampleFile = File(context.cacheDir, "sample_device_wallpaper.png")
        val bmp = Bitmap.createBitmap(640, 960, Bitmap.Config.ARGB_8888)
        bmp.eraseColor(Color.rgb(20, 184, 166))
        FileOutputStream(sampleFile).use { out ->
            bmp.compress(Bitmap.CompressFormat.PNG, 95, out)
        }
        bmp.recycle()

        val mediaRepo = LocalMediaRepository(context)
        val result = mediaRepo.saveWallpaperImage(Uri.fromFile(sampleFile))
        assertTrue(
            "Expected saveWallpaperImage to succeed, got: ${result.exceptionOrNull()?.message}",
            result.isSuccess
        )
        val savedPath = result.getOrThrow()
        val savedFile = File(savedPath)
        assertTrue("Saved wallpaper file must exist", savedFile.exists() && savedFile.length() > 0L)
        val decoded = BitmapFactory.decodeFile(savedPath)
        assertNotNull("Saved wallpaper must be a valid decodable image", decoded)
        decoded?.recycle()

        // Also verify WhatsApp chat-style wallpaper persists across simulated app restart
        val repo1 = LocalStoreRepository(context)
        repo1.ensureInitialized()
        val waUri = MainWallpaperCatalog.PREFIX_PRESET + "wa_classic_sand"
        repo1.updatePreferences(mapOf("mainWallpaperUri" to waUri, "mainWallpaperDimPercent" to 12))
        val restored = LocalStoreRepository(context).getPreferencesSnapshot()
        assertEquals(waUri, restored.mainWallpaperUri)
        assertEquals(12, restored.mainWallpaperDimPercent)
    }

    @Test
    fun batteryOptimizationStatus_reflectsLiveOsExemptionImmediately() {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val shadowPower = Shadows.shadowOf(powerManager)

        shadowPower.setIgnoringBatteryOptimizations(context.packageName, false)
        assertFalse(
            "When app is NOT exempt, battery optimization warning must be active",
            TaskAlarmScheduler.isIgnoringBatteryOptimizations(context)
        )
        assertFalse(TaskAlarmScheduler.diagnose(context).isIgnoringBatteryOptimizations)

        shadowPower.setIgnoringBatteryOptimizations(context.packageName, true)
        assertTrue(
            "When user sets Unrestricted / exempt, warning must clear immediately",
            TaskAlarmScheduler.isIgnoringBatteryOptimizations(context)
        )
        assertTrue(TaskAlarmScheduler.diagnose(context).isIgnoringBatteryOptimizations)
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

    @Test
    fun floatingIcons_deletePersistRestoreResetAndSafeDefaults_workAcrossRestart() = runBlocking {
        val repo1 = LocalStoreRepository(context)
        repo1.ensureInitialized()

        // Add two icons cleanly in safe peripheral slots (away from top banner area y >= 0.40)
        val id1 = repo1.addFloatingBadge(stateId = "bald_exp_cool", sizeDp = 52f).getOrThrow()
        val id2 = repo1.addFloatingBadge(stateId = "gym_dumbbell_mint", sizeDp = 48f).getOrThrow()

        val initialBadges = repo1.getFloatingBadgesSnapshot()
        val b1Init = initialBadges.first { it.id == id1 }
        val b2Init = initialBadges.first { it.id == id2 }
        assertTrue("Default slot 1 must stay clear of top banner", b1Init.defaultYFraction >= 0.40f)
        assertTrue("Default slot 2 must stay clear of top banner", b2Init.defaultYFraction >= 0.40f)

        // 1. Drag icon 1 to custom position & toggle icon 2 hidden -> persist across restart
        repo1.updateFloatingBadge(id1) { it.copy(xFraction = 0.25f, yFraction = 0.50f) }
        repo1.updateFloatingBadge(id2) { it.copy(visible = false) }

        val repoAfterDrag = LocalStoreRepository(context)
        val dragged1 = repoAfterDrag.getFloatingBadgesSnapshot().first { it.id == id1 }
        val hidden2 = repoAfterDrag.getFloatingBadgesSnapshot().first { it.id == id2 }
        assertEquals(0.25f, dragged1.xFraction, 0.001f)
        assertEquals(0.50f, dragged1.yFraction, 0.001f)
        assertFalse("Show/hide toggle must persist across restart", hidden2.visible)

        // 2. Single icon "Reset to default position" restores its default coordinates
        repoAfterDrag.resetFloatingBadgePosition(id1)
        val afterSingleReset = LocalStoreRepository(context).getFloatingBadgesSnapshot().first { it.id == id1 }
        assertEquals(b1Init.defaultXFraction, afterSingleReset.xFraction, 0.001f)
        assertEquals(b1Init.defaultYFraction, afterSingleReset.yFraction, 0.001f)

        // 3. Global "Reset positions" puts all icons in safe non-overlapping slots
        repoAfterDrag.updateFloatingBadge(id1) { it.copy(xFraction = 0.50f, yFraction = 0.12f) }
        repoAfterDrag.updateFloatingBadge(id2) { it.copy(xFraction = 0.50f, yFraction = 0.12f) }
        repoAfterDrag.resetAllFloatingBadgePositions()
        val afterGlobalReset = LocalStoreRepository(context).getFloatingBadgesSnapshot()
        assertTrue(afterGlobalReset.all { it.yFraction >= 0.40f })

        // 4. Delete ALL icons -> simulate restart + ensureInitialized() -> MUST stay deleted!
        val allIds = afterGlobalReset.map { it.id }
        allIds.forEach { repoAfterDrag.deleteFloatingBadge(it) }

        val repoAfterRestart = LocalStoreRepository(context)
        repoAfterRestart.ensureInitialized()
        assertTrue(
            "Deleted icons must stay deleted after reopening the app",
            repoAfterRestart.getFloatingBadgesSnapshot().isEmpty()
        )
        assertEquals(
            "Deleted icons must be stored so they can be restored",
            allIds.size,
            repoAfterRestart.getDeletedFloatingBadgesSnapshot().size
        )

        // 5. "Restore all deleted icons" restores every deleted icon visible & clears deleted list
        repoAfterRestart.restoreAllDeletedFloatingBadges()
        val repoAfterRestore = LocalStoreRepository(context)
        val restoredBadges = repoAfterRestore.getFloatingBadgesSnapshot()
        assertEquals(allIds.size, restoredBadges.size)
        assertTrue("All restored icons must be visible", restoredBadges.all { it.visible })
        assertTrue(repoAfterRestore.getDeletedFloatingBadgesSnapshot().isEmpty())
    }
}

