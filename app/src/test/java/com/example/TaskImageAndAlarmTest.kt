package com.example

import com.example.model.MNotesCatalog
import com.example.model.MainWallpaperCatalog
import com.example.model.RepeatMode
import com.example.model.SmartTask
import com.example.model.UserPreferences
import com.example.notification.TaskAlarmScheduler
import com.example.ui.components.FloatingBadgeCatalog
import com.example.ui.components.ReminderWallpaperCatalog
import com.example.ui.components.parseBuiltInUri
import com.example.ui.components.toggleFloatingIconSelection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class TaskImageAndAlarmTest {

    @Test
    fun exerciseCatalogHasAllRequiredCategoriesAndValidDrawables() {
        val requiredCategories = listOf(
            "all",
            "chest",
            "back",
            "legs",
            "cardio",
            "abs",
            "shoulders_arms",
            "recovery",
            "gym",
            "fruits",
            "vegetables",
            "heritage"
        )
        val categoryIds = MNotesCatalog.photoCategories.map { it.id }.toSet()
        requiredCategories.forEach { catId ->
            assertTrue("Missing category: $catId", categoryIds.contains(catId))
        }

        // Verify every category has substantial entries
        listOf("chest", "back", "legs", "cardio", "abs", "shoulders_arms", "recovery", "gym").forEach { cat ->
            val count = MNotesCatalog.pixel3dPhotos.count { it.matchesCategory(cat) }
            assertTrue("Category '$cat' should have at least 4 exercises, got $count", count >= 4)
        }

        // Verify every single photo URI in the catalog parses to a real non-zero drawable resource
        assertTrue("Catalog should contain 100+ photos, got ${MNotesCatalog.pixel3dPhotos.size}", MNotesCatalog.pixel3dPhotos.size >= 100)
        MNotesCatalog.pixel3dPhotos.forEach { spec ->
            val parsed = parseBuiltInUri(spec.uri)
            assertNotNull("Failed to parse built-in URI: ${spec.uri}", parsed)
            val (resId, tileIdx) = parsed!!
            assertTrue("Drawable resId must be non-zero for ${spec.uri}", resId != 0)
            if (tileIdx != null) {
                assertTrue("Tile index out of 0..8 range for ${spec.uri}: $tileIdx", tileIdx in 0..8)
            }
        }
    }

    @Test
    fun legacyTaskPhotoUrisArePreserved() {
        val uris = MNotesCatalog.pixel3dPhotos.map { it.uri }.toSet()
        val legacySampleUris = listOf(
            "res://img_3d_gym_workout",
            "res://img_3d_gym_chest",
            "res://img_3d_gym_back",
            "res://img_3d_gym_legs",
            "res://img_3d_strength_exercises#0",
            "res://img_3d_cardio_activities#0",
            "res://img_cardio_gym_machines#0",
            "res://img_gym_gear#0",
            "res://img_medieval_fruits#0",
            "res://img_medieval_veggies#0",
            "res://img_3d_elder_beige",
            "res://img_3d_chest_pack#0",
            "res://img_3d_back_pack#0",
            "res://img_3d_legs_pack#0",
            "res://img_3d_abs_core_pack#0",
            "res://img_3d_shoulders_arms_recovery_pack#8"
        )
        legacySampleUris.forEach { legacyUri ->
            assertTrue("Expected URI missing: $legacyUri", uris.contains(legacyUri))
            assertNotNull("Expected URI failed to resolve: $legacyUri", parseBuiltInUri(legacyUri))
        }
    }

    @Test
    fun alarmScheduler_disabledOrBlankTimeReturnsNull() {
        val now = LocalDateTime.of(2026, 11, 19, 10, 0)
        val zone = ZoneId.of("UTC")

        val disabled = SmartTask(id = "t1", title = "Chest", alertEnabled = false, alertTime = "18:00")
        assertNull(TaskAlarmScheduler.computeNextTriggerMillis(disabled, now, zone))

        val blankTime = SmartTask(id = "t2", title = "Back", alertEnabled = true, alertTime = "")
        assertNull(TaskAlarmScheduler.computeNextTriggerMillis(blankTime, now, zone))

        val invalidTime = SmartTask(id = "t3", title = "Legs", alertEnabled = true, alertTime = "25:99")
        assertNull(TaskAlarmScheduler.computeNextTriggerMillis(invalidTime, now, zone))
    }

    @Test
    fun alarmScheduler_oneShotNoneModeSchedulesOnlyFuture() {
        val now = LocalDateTime.of(2026, 11, 19, 10, 0)
        val zone = ZoneId.of("UTC")

        val laterToday = SmartTask(
            id = "t1",
            title = "Run",
            repeatMode = RepeatMode.NONE.wire,
            alertEnabled = true,
            alertTime = "14:30",
            nextDueDate = "2026-11-19"
        )
        val trigger = TaskAlarmScheduler.computeNextTriggerMillis(laterToday, now, zone)
        assertNotNull(trigger)
        val dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(trigger!!), zone)
        assertEquals(LocalDateTime.of(2026, 11, 19, 14, 30), dt)

        val alreadyPassed = laterToday.copy(alertTime = "09:15")
        assertNull(TaskAlarmScheduler.computeNextTriggerMillis(alreadyPassed, now, zone))
    }

    @Test
    fun alarmScheduler_dailyRollsToTomorrowWhenPassedOrAlreadyAlerted() {
        val now = LocalDateTime.of(2026, 11, 19, 10, 0)
        val zone = ZoneId.of("UTC")

        val passedDaily = SmartTask(
            id = "daily1",
            title = "Stretch",
            repeatMode = RepeatMode.DAILY.wire,
            alertEnabled = true,
            alertTime = "08:00",
            nextDueDate = "2026-11-19"
        )
        val trigger1 = TaskAlarmScheduler.computeNextTriggerMillis(passedDaily, now, zone)!!
        assertEquals(
            LocalDateTime.of(2026, 11, 20, 8, 0),
            LocalDateTime.ofInstant(Instant.ofEpochMilli(trigger1), zone)
        )

        val alreadyAlertedToday = passedDaily.copy(
            alertTime = "18:00",
            lastAlertedDate = "2026-11-19"
        )
        val trigger2 = TaskAlarmScheduler.computeNextTriggerMillis(alreadyAlertedToday, now, zone)!!
        assertEquals(
            LocalDateTime.of(2026, 11, 20, 18, 0),
            LocalDateTime.ofInstant(Instant.ofEpochMilli(trigger2), zone)
        )
    }

    @Test
    fun alarmScheduler_weeklyWithSpecificWeekDaysSelectsNextScheduledDay() {
        // 2026-11-19 is Thursday -> DayOfWeek.THURSDAY.value % 7 == 4
        val now = LocalDateTime.of(2026, 11, 19, 19, 0)
        val zone = ZoneId.of("UTC")

        // Scheduled on Saturday (6) and Monday (1) at 17:00
        val legsTask = SmartTask(
            id = "legs",
            title = "Leg Day",
            repeatMode = RepeatMode.WEEKLY.wire,
            weekDays = listOf(1L, 6L),
            alertEnabled = true,
            alertTime = "17:00",
            nextDueDate = "2026-11-19"
        )
        val trigger = TaskAlarmScheduler.computeNextTriggerMillis(legsTask, now, zone)!!
        // Next Saturday is 2026-11-21 at 17:00
        assertEquals(
            LocalDateTime.of(2026, 11, 21, 17, 0),
            LocalDateTime.ofInstant(Instant.ofEpochMilli(trigger), zone)
        )
    }

    @Test
    fun alarmScheduler_requestCodesArePositiveAndDistinct() {
        val rc1 = TaskAlarmScheduler.requestCodeForTask("task_alpha")
        val rc2 = TaskAlarmScheduler.requestCodeForTask("task_beta")
        assertTrue(rc1 > 0)
        assertTrue(rc2 > 0)
        assertNotEquals(rc1, rc2)
        assertEquals(rc1, TaskAlarmScheduler.requestCodeForTask("task_alpha"))
    }

    @Test
    fun reminderWallpaperCatalog_hasAllPastelThemesAndDoodlePacksWithSafeDefaults() {
        val prefs = UserPreferences()
        assertEquals("default", prefs.reminderBgThemeId)
        assertEquals("none", prefs.reminderDoodlePackId)
        assertEquals(14, prefs.reminderDoodleOpacity)
        assertFalse(
            "Default reminder wallpaper must not activate customizations until user applies",
            ReminderWallpaperCatalog.isCustomized(prefs.reminderBgThemeId, prefs.reminderDoodlePackId)
        )

        assertTrue("Expected at least 10 pastel themes", ReminderWallpaperCatalog.themes.size >= 10)
        assertTrue("Expected at least 7 doodle packs", ReminderWallpaperCatalog.doodlePacks.size >= 7)

        val expectedThemeIds = listOf(
            "default", "mint_breeze", "sky_cloud", "aqua_cyan",
            "mint_sky_duet", "snow_pearl", "cream_vanilla",
            "lavender_mist", "peach_blossom", "sage_garden"
        )
        val actualThemeIds = ReminderWallpaperCatalog.themes.map { it.id }.toSet()
        expectedThemeIds.forEach { id ->
            assertTrue("Missing reminder theme: $id", actualThemeIds.contains(id))
        }

        ReminderWallpaperCatalog.themes.forEach { theme ->
            assertTrue(theme.nameEn.isNotBlank())
            assertTrue(theme.nameAr.isNotBlank())
        }

        val expectedPackIds = listOf(
            "none", "all_mix", "gym_fitness", "cardio_energy",
            "fruits_food", "sleep_calm", "daily_routine"
        )
        val actualPackIds = ReminderWallpaperCatalog.doodlePacks.map { it.id }.toSet()
        expectedPackIds.forEach { id ->
            assertTrue("Missing doodle pack: $id", actualPackIds.contains(id))
        }

        ReminderWallpaperCatalog.doodlePacks.forEach { pack ->
            assertTrue(pack.nameEn.isNotBlank())
            assertTrue(pack.nameAr.isNotBlank())
            if (pack.id == "none") {
                assertTrue(pack.motifs.isEmpty())
            } else {
                assertTrue("Pack '${pack.id}' must contain motifs", pack.motifs.isNotEmpty())
            }
        }

        // Custom selections are detected as customized
        assertTrue(ReminderWallpaperCatalog.isCustomized("mint_breeze", "none"))
        assertTrue(ReminderWallpaperCatalog.isCustomized("default", "gym_fitness"))
        assertTrue(ReminderWallpaperCatalog.isCustomized("sky_cloud", "all_mix"))

        // Safe fallbacks for unknown IDs
        assertEquals("default", ReminderWallpaperCatalog.themeById("unknown_id").id)
        assertEquals("none", ReminderWallpaperCatalog.doodlePackById("unknown_pack").id)
    }

    @Test
    fun samsungOneUiPixarAppIcons_allTwelveHaveValidDrawableResources() {
        assertEquals(12, MNotesCatalog.launcherIcons.size)
        val ids = MNotesCatalog.launcherIcons.map { it.id }
        assertEquals((1..12).map { "I$it" }, ids)
        MNotesCatalog.launcherIcons.forEach { spec ->
            assertTrue("Icon ${spec.id} must be bitmap Pixar 3D art", spec.isBitmapArt)
            assertTrue("Icon ${spec.id} must have a non-zero drawable resource", spec.fgDrawableRes != 0)
            assertTrue(spec.nameEn.isNotBlank())
            assertTrue(spec.nameAr.isNotBlank())
        }
    }

    @Test
    fun baldMaleWorkoutsAndExpressions_existAndResolveAcrossBothLibraries() {
        val uris = MNotesCatalog.pixel3dPhotos.map { it.uri }.toSet()
        for (i in 0..8) {
            val w1 = "res://img_bald_male_workouts_1#$i"
            val w2 = "res://img_bald_male_workouts_2#$i"
            val exp = "res://img_bald_male_expressions#$i"
            assertTrue("Missing bald male strength workout $w1", uris.contains(w1))
            assertTrue("Missing bald male cardio workout $w2", uris.contains(w2))
            assertTrue("Missing bald male expression $exp", uris.contains(exp))
            assertNotNull(parseBuiltInUri(w1))
            assertNotNull(parseBuiltInUri(w2))
            assertNotNull(parseBuiltInUri(exp))
        }

        // Bald male Pixar expressions & gym icons exist at top of FloatingBadgeCatalog
        val baldExpressions = FloatingBadgeCatalog.icons.filter { it.categoryId == "bald_expressions" }
        assertEquals("Expected 9 bald male Pixar 3D expression icons", 9, baldExpressions.size)
        baldExpressions.forEach { spec ->
            val parsed = parseBuiltInUri(spec.gridTileUri!!)
            assertNotNull("Failed to resolve ${spec.id}", parsed)
            assertTrue(parsed!!.first != 0)
        }

        val pixarChars = FloatingBadgeCatalog.icons.filter { it.categoryId == "pixar_chars" }
        assertTrue("Expected at least 36 Pixar 3D male characters", pixarChars.size >= 36)
        assertTrue(FloatingBadgeCatalog.icons.any { it.id == "char_sleeping_zzz" })
        assertTrue(FloatingBadgeCatalog.icons.size >= 160)
    }

    @Test
    fun whatsappChatWallpapersAndSelectionToggle_workAsExpected() {
        // Req #6: WhatsApp chat-style wallpapers + soft presets
        assertEquals("", UserPreferences().mainWallpaperUri)
        assertTrue("Expected 10 WhatsApp chat wallpapers", MainWallpaperCatalog.whatsappPresets.size >= 10)
        assertTrue("Expected 8 soft pastel presets", MainWallpaperCatalog.softPresets.size >= 8)
        MainWallpaperCatalog.whatsappPresets.forEach { preset ->
            val uri = MainWallpaperCatalog.PREFIX_PRESET + preset.id
            val resolved = MainWallpaperCatalog.presetForUri(uri)
            assertNotNull("Expected presetForUri to resolve $uri", resolved)
            assertEquals(preset.id, resolved?.id)
            assertEquals(MainWallpaperCatalog.CATEGORY_WHATSAPP, resolved?.category)
        }

        // Req #8: 1st tap selects; 2nd tap on SAME icon deselects; tapping another selects the new icon
        val afterFirstTap = toggleFloatingIconSelection(currentSelectedId = null, tappedId = "badge_1")
        assertEquals("badge_1", afterFirstTap)

        val afterSecondTapSame = toggleFloatingIconSelection(currentSelectedId = afterFirstTap, tappedId = "badge_1")
        assertNull("Second tap on same icon must deselect immediately", afterSecondTapSame)

        val afterTapOther = toggleFloatingIconSelection(currentSelectedId = "badge_1", tappedId = "badge_2")
        assertEquals("badge_2", afterTapOther)
    }
}
