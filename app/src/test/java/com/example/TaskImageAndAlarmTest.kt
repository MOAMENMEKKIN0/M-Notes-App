package com.example

import com.example.model.MNotesCatalog
import com.example.model.RepeatMode
import com.example.model.SavedScreenDesignVersion
import com.example.model.ScreenDesignPresets
import com.example.model.ScreenDesignStoreState
import com.example.model.SmartTask
import com.example.model.UserPreferences
import com.example.notification.TaskAlarmScheduler
import com.example.ui.components.ReminderWallpaperCatalog
import com.example.ui.components.parseBuiltInUri
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
    fun screenDesignPresets_defaultUnchangedAndAllFourPermanentPresetsImmutable() {
        // Default state MUST keep classic main screen unchanged (activeConfig == null)
        val defaultState = ScreenDesignStoreState()
        assertEquals(ScreenDesignPresets.ID_CLASSIC, defaultState.activeDesignId)
        assertNull(defaultState.activeConfig)
        assertTrue(defaultState.savedVersions.isEmpty())

        // All 4 approved presets (A, B, C, D) permanently available with distinct modes
        assertEquals(4, ScreenDesignPresets.fourApprovedPresets.size)
        assertEquals(5, ScreenDesignPresets.allPresets.size)
        val codes = ScreenDesignPresets.fourApprovedPresets.map { it.badgeCode }
        assertEquals(listOf("A", "B", "C", "D"), codes)

        assertEquals("timeline_list", ScreenDesignPresets.optionA.config.layoutMode)
        assertEquals("gallery_2col", ScreenDesignPresets.optionB.config.layoutMode)
        assertEquals("spotlight_agenda", ScreenDesignPresets.optionC.config.layoutMode)
        assertTrue(ScreenDesignPresets.optionC.config.showSpotlightUpNext)
        assertEquals("bento_grouped", ScreenDesignPresets.optionD.config.layoutMode)
        assertTrue(ScreenDesignPresets.optionD.config.showRestingGroupSeparate)

        // Customizing a copy of Option B must NOT mutate original Option B
        val customOptionB = ScreenDesignPresets.optionB.config.copy(
            taskImageShape = "organic_blob",
            cardShape = "pill",
            accentHex = "#8B5CF6",
            showWeekStrip = true,
            showFilterChips = false
        )
        assertEquals("circle", ScreenDesignPresets.optionB.config.taskImageShape)
        assertEquals("squircle", ScreenDesignPresets.optionB.config.cardShape)
        assertTrue(ScreenDesignPresets.optionB.config.showFilterChips)

        // Multiple saved custom versions serialize and restore cleanly
        val savedState = ScreenDesignStoreState(
            activeDesignId = ScreenDesignPresets.ID_CLASSIC,
            activeConfig = null,
            savedVersions = listOf(
                SavedScreenDesignVersion(
                    id = "custom_1",
                    name = "My Organic Option B",
                    basePresetId = ScreenDesignPresets.ID_OPTION_B,
                    config = customOptionB
                ),
                SavedScreenDesignVersion(
                    id = "custom_2",
                    name = "Mint Timeline A",
                    basePresetId = ScreenDesignPresets.ID_OPTION_A,
                    config = ScreenDesignPresets.optionA.config.copy(cardElevationDp = 10)
                )
            )
        )
        val json = Json { ignoreUnknownKeys = true }
        val encoded = json.encodeToString(savedState)
        val decoded = json.decodeFromString<ScreenDesignStoreState>(encoded)
        assertEquals(savedState, decoded)
        assertEquals(2, decoded.savedVersions.size)
        assertEquals("organic_blob", decoded.savedVersions[0].config.taskImageShape)
        assertEquals(ScreenDesignPresets.ID_CLASSIC, decoded.activeDesignId)
        assertNull(decoded.activeConfig)
    }
}
