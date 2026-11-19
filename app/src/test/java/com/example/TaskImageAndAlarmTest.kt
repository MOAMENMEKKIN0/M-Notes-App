package com.example

import com.example.model.ChecklistStyleCatalog
import com.example.model.ChecklistStyleConfig
import com.example.model.MNotesCatalog
import com.example.model.MainWallpaperCatalog
import com.example.model.OrderTextSplitter
import com.example.model.RepeatMode
import com.example.model.SmartTask
import com.example.model.UserPreferences
import com.example.notification.TaskAlarmScheduler
import com.example.ui.components.FloatingBadgeCatalog
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
    fun orderTextSplitter_neverAutoSplitsInWholeModeAndSupportsAllOptionalModes() {
        val sample = "Milk Bread Eggs Cheese"
        // Default WHOLE mode keeps text completely intact as 1 item
        assertEquals(listOf("Milk Bread Eggs Cheese"), OrderTextSplitter.split(sample, OrderTextSplitter.MODE_WHOLE))

        // 1 word each
        assertEquals(listOf("Milk", "Bread", "Eggs", "Cheese"), OrderTextSplitter.split(sample, OrderTextSplitter.MODE_WORDS))

        // 2 words each
        assertEquals(listOf("Milk Bread", "Eggs Cheese"), OrderTextSplitter.split(sample, OrderTextSplitter.MODE_PAIRS))

        // Custom N words (e.g., 3)
        assertEquals(listOf("Milk Bread Eggs", "Cheese"), OrderTextSplitter.split(sample, OrderTextSplitter.MODE_CUSTOM_N, wordsPerGroup = 3))

        // Comma / newline
        val commaSample = "Apples, Bananas\nOranges، Grapes"
        assertEquals(listOf("Apples", "Bananas", "Oranges", "Grapes"), OrderTextSplitter.split(commaSample, OrderTextSplitter.MODE_COMMA_LINE))

        // Custom delimiter
        assertEquals(listOf("Item 1", "Item 2", "Item 3"), OrderTextSplitter.split("Item 1 - Item 2 - Item 3", OrderTextSplitter.MODE_CUSTOM_DELIM, customDelimiter = "-"))
    }

    @Test
    fun floatingBadgeCatalog_contains36Pixar3DMaleCharactersAndResolvesAllDrawables() {
        val pixarChars = FloatingBadgeCatalog.icons.filter { it.categoryId == "pixar_chars" }
        assertEquals("Expected 36 distinct 3D Pixar male characters", 36, pixarChars.size)

        // Ensure original baby character & existing icons remain preserved
        assertTrue(FloatingBadgeCatalog.icons.any { it.id == "char_sleeping_zzz" })
        assertTrue(FloatingBadgeCatalog.icons.size >= 145)

        // Every Pixar 3D character tile URI must resolve to a valid drawable resource and tile 0..8
        pixarChars.forEach { spec ->
            assertNotNull("Missing gridTileUri for ${spec.id}", spec.gridTileUri)
            val parsed = parseBuiltInUri(spec.gridTileUri!!)
            assertNotNull("Failed to parse URI ${spec.gridTileUri} for ${spec.id}", parsed)
            val (resId, tileIdx) = parsed!!
            assertTrue("resId must be non-zero for ${spec.id}", resId != 0)
            assertNotNull("tileIdx must be non-null for ${spec.id}", tileIdx)
            assertTrue("tileIdx out of 0..8 for ${spec.id}", tileIdx!! in 0..8)
        }

        // Verify 3D Pixar Checkmark & X medallion sheet resolves all 9 tiles
        for (idx in 0..8) {
            val parsed = parseBuiltInUri("res://img_pixar_check_x_pack#$idx")
            assertNotNull("Failed to parse check/X pack tile #$idx", parsed)
            assertTrue(parsed!!.first != 0)
            assertEquals(idx, parsed.second)
        }

        // Verify wallpaper catalog & checklist style catalog defaults
        assertEquals("", UserPreferences().mainWallpaperUri)
        assertTrue(MainWallpaperCatalog.presets.size >= 8)
        assertTrue(ChecklistStyleCatalog.styles.size >= 10)
        assertTrue(ChecklistStyleCatalog.shapes.size >= 6)

        val style = ChecklistStyleConfig(styleId = "pixar_gold", shapeId = "squircle", circleSizeDp = 40, markSizeDp = 22)
        val json = Json { ignoreUnknownKeys = true }
        assertEquals(style, json.decodeFromString<ChecklistStyleConfig>(json.encodeToString(style)))
    }
}
