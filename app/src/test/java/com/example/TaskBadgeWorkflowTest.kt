package com.example

import com.example.model.BackupSnapshotPayload
import com.example.model.BackupTaskDto
import com.example.model.RepeatMode
import com.example.model.SmartIconSettings
import com.example.model.SmartTask
import com.example.model.SmartTaskBadgeResolver
import com.example.ui.components.FloatingBadgeCatalog
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class TaskBadgeWorkflowTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun catalog_hasOver100Icons_andAllDefaultSmartFavoritesExist() {
        assertTrue(
            "Expected at least 100 icons in FloatingBadgeCatalog, found ${FloatingBadgeCatalog.icons.size}",
            FloatingBadgeCatalog.icons.size >= 100
        )

        val defaults = SmartIconSettings()
        defaults.categoryFavorites.forEach { (category, iconIds) ->
            assertTrue("Category '$category' should have default user-editable favorites", iconIds.isNotEmpty())
            iconIds.forEach { id ->
                val spec = FloatingBadgeCatalog.byId(id)
                assertEquals("Favorite icon '$id' in category '$category' must resolve in catalog", id, spec.id)
            }
        }
    }

    @Test
    fun manualSelection_isAlwaysRespectedWhenAutoIconsDisabled() {
        val task = SmartTask(
            id = "task_gym",
            title = "تمرين الجيم",
            repeatMode = RepeatMode.DAILY.wire,
            badgeIconId = "food_avocado",
            badgeVisible = true,
            badgeOffsetX = 34f,
            badgeOffsetY = -22f,
            badgeSizeDp = 42f,
            badgeCategory = "gym",
            badgeSmartMode = "manual"
        )
        val settings = SmartIconSettings(
            enableSmartAutoIcons = false,
            todayIsHoliday = true,
            categoryFavorites = mapOf(
                "gym" to listOf("gym_dumbbell_mint"),
                "holiday" to listOf("relax_beach")
            )
        )

        val resolved = SmartTaskBadgeResolver.resolveTaskBadgeIconId(
            task = task,
            settings = settings,
            date = LocalDate.of(2026, 11, 19)
        )
        assertEquals("food_avocado", resolved)
    }

    @Test
    fun hiddenBadge_alwaysReturnsNull() {
        val task = SmartTask(
            id = "task_hidden",
            title = "Sleep",
            badgeIconId = "char_sleeping_zzz",
            badgeVisible = false,
            badgeSmartMode = "category"
        )
        val settings = SmartIconSettings(enableSmartAutoIcons = true)
        assertNull(SmartTaskBadgeResolver.resolveTaskBadgeIconId(task, settings, LocalDate.of(2026, 11, 19)))
    }

    @Test
    fun smartCategoryMode_usesOnlyUserChosenCategoryFavorite() {
        val gymTask = SmartTask(
            id = "task_1",
            title = "Leg Day Workout",
            repeatMode = RepeatMode.DAILY.wire,
            badgeIconId = "char_sleeping_zzz",
            badgeVisible = true,
            badgeCategory = "auto",
            badgeSmartMode = "category"
        )
        val settings = SmartIconSettings(
            enableSmartAutoIcons = true,
            categoryFavorites = mapOf("gym" to listOf("gym_kettlebell", "gym_protein"))
        )

        assertEquals("gym", SmartTaskBadgeResolver.inferCategory(gymTask))
        assertEquals(
            "gym_kettlebell",
            SmartTaskBadgeResolver.resolveTaskBadgeIconId(gymTask, settings, LocalDate.of(2026, 11, 19))
        )
    }

    @Test
    fun smartRotateMode_cyclesPredictablyOnlyThroughUserSelectedFavorites() {
        val favs = listOf("gym_dumbbell_mint", "gym_kettlebell", "gym_bicep")
        val task = SmartTask(
            id = "task_rot",
            title = "تمرين الجيم",
            repeatMode = RepeatMode.DAILY.wire,
            cycleIndex = 0L,
            badgeVisible = true,
            badgeCategory = "gym",
            badgeSmartMode = "rotate"
        )
        val settings = SmartIconSettings(
            enableSmartAutoIcons = true,
            smartDefaultMode = "rotate",
            categoryFavorites = mapOf("gym" to favs)
        )

        val day1 = SmartTaskBadgeResolver.resolveTaskBadgeIconId(task, settings, LocalDate.of(2026, 11, 19))
        val day2 = SmartTaskBadgeResolver.resolveTaskBadgeIconId(task, settings, LocalDate.of(2026, 11, 20))
        val day3 = SmartTaskBadgeResolver.resolveTaskBadgeIconId(task, settings, LocalDate.of(2026, 11, 21))

        assertNotNull(day1)
        assertNotNull(day2)
        assertNotNull(day3)
        assertTrue(favs.contains(day1))
        assertTrue(favs.contains(day2))
        assertTrue(favs.contains(day3))
        assertEquals(3, setOf(day1, day2, day3).size)
    }

    @Test
    fun recurringTaskOffDay_usesUserChosenRestDayIcon() {
        // 2026-11-19 is Thursday -> jsDay = 4 (0=Sun..6=Sat). Schedule task only on Mon (1) & Wed (3).
        val offDayDate = LocalDate.of(2026, 11, 19)
        val activeDayDate = LocalDate.of(2026, 11, 18) // Wednesday -> jsDay = 3

        val gymTask = SmartTask(
            id = "task_weekly_gym",
            title = "Gym Workout",
            repeatMode = RepeatMode.WEEKLY.wire,
            weekDays = listOf(1L, 3L),
            badgeIconId = "gym_dumbbell_mint",
            badgeVisible = true,
            badgeCategory = "gym",
            badgeSmartMode = "category"
        )
        val settings = SmartIconSettings(
            enableSmartAutoIcons = true,
            useRestDayIconOnOffDays = true,
            categoryFavorites = mapOf(
                "gym" to listOf("gym_bicep"),
                "rest" to listOf("char_resting_pillow")
            )
        )

        assertFalse(gymTask.isActiveOnDate(offDayDate))
        assertTrue(gymTask.isActiveOnDate(activeDayDate))

        assertEquals(
            "char_resting_pillow",
            SmartTaskBadgeResolver.resolveTaskBadgeIconId(gymTask, settings, offDayDate)
        )
        assertEquals(
            "gym_bicep",
            SmartTaskBadgeResolver.resolveTaskBadgeIconId(gymTask, settings, activeDayDate)
        )
    }

    @Test
    fun holidayOverride_usesUserChosenHolidayIcon() {
        val task = SmartTask(
            id = "task_work",
            title = "Office Standup",
            repeatMode = RepeatMode.DAILY.wire,
            badgeVisible = true,
            badgeCategory = "work",
            badgeSmartMode = "category"
        )
        val settings = SmartIconSettings(
            enableSmartAutoIcons = true,
            todayIsHoliday = true,
            categoryFavorites = mapOf(
                "work" to listOf("work_laptop"),
                "holiday" to listOf("relax_beach")
            )
        )

        assertEquals(
            "relax_beach",
            SmartTaskBadgeResolver.resolveTaskBadgeIconId(task, settings, LocalDate.of(2026, 11, 19))
        )
    }

    @Test
    fun weekdayScheduleMode_usesUserChosenDayOfWeekIcon() {
        // 2026-11-20 is Friday -> jsDay = 5
        val friday = LocalDate.of(2026, 11, 20)
        val task = SmartTask(
            id = "task_sched",
            title = "Morning Routine",
            repeatMode = RepeatMode.DAILY.wire,
            badgeIconId = "coffee_cup_steam",
            badgeVisible = true,
            badgeSmartMode = "schedule"
        )
        val settings = SmartIconSettings(
            enableSmartAutoIcons = true,
            dayOfWeekIcons = mapOf(5 to "relax_sparkles")
        )

        assertEquals(
            "relax_sparkles",
            SmartTaskBadgeResolver.resolveTaskBadgeIconId(task, settings, friday)
        )
    }

    @Test
    fun backupSnapshot_roundTripPreservesTaskBadgesAndSmartIconSettings() {
        val original = BackupSnapshotPayload(
            version = "1.0",
            savedAt = "2026-11-19T12:00:00Z",
            tasks = listOf(
                BackupTaskDto(
                    title = "Gym Session",
                    meta = "7:00 PM",
                    repeatMode = RepeatMode.WEEKLY.wire,
                    weekDays = listOf(1L, 3L, 5L),
                    badgeIconId = "gym_dumbbell_mint",
                    badgeVisible = true,
                    badgeOffsetX = -48.5f,
                    badgeOffsetY = 26.0f,
                    badgeSizeDp = 44f,
                    badgeCategory = "gym",
                    badgeSmartMode = "rotate",
                    badgeLocked = true
                )
            ),
            smartIconSettings = SmartIconSettings(
                enableSmartAutoIcons = true,
                smartDefaultMode = "rotate",
                useRestDayIconOnOffDays = true,
                todayIsHoliday = false,
                categoryFavorites = mapOf("gym" to listOf("gym_dumbbell_mint", "gym_kettlebell")),
                dayOfWeekIcons = mapOf(5 to "relax_beach")
            )
        )

        val encoded = json.encodeToString(original)
        val decoded = json.decodeFromString<BackupSnapshotPayload>(encoded)

        assertEquals(1, decoded.tasks.size)
        val t = decoded.tasks.first()
        assertEquals("gym_dumbbell_mint", t.badgeIconId)
        assertTrue(t.badgeVisible)
        assertEquals(-48.5f, t.badgeOffsetX, 0.001f)
        assertEquals(26.0f, t.badgeOffsetY, 0.001f)
        assertEquals(44f, t.badgeSizeDp, 0.001f)
        assertEquals("gym", t.badgeCategory)
        assertEquals("rotate", t.badgeSmartMode)
        assertTrue(t.badgeLocked)

        assertTrue(decoded.smartIconSettings.enableSmartAutoIcons)
        assertEquals("rotate", decoded.smartIconSettings.smartDefaultMode)
        assertEquals(listOf("gym_dumbbell_mint", "gym_kettlebell"), decoded.smartIconSettings.categoryFavorites["gym"])
        assertEquals("relax_beach", decoded.smartIconSettings.dayOfWeekIcons[5])
    }
}
