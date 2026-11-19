package com.example.model

import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class SmartCategorySpec(
    val id: String,
    val nameAr: String,
    val nameEn: String
)

/**
 * Deterministic, user-preference-driven icon resolver for task circles.
 *
 * Rules:
 * 1. If task.badgeVisible is false -> always returns null (hidden).
 * 2. Manual selections are ALWAYS respected unless the user explicitly enables
 *    smart automatic icon changes (global enableSmartAutoIcons or per-task badgeSmartMode != "manual").
 * 3. Smart modes ONLY pick from the user's own selected favorites or weekday assignments
 *    — never random unselected icons.
 */
object SmartTaskBadgeResolver {

    val smartCategories: List<SmartCategorySpec> = listOf(
        SmartCategorySpec("gym", "الجيم والتمارين", "Gym & Workout"),
        SmartCategorySpec("rest", "أيام الراحة", "Rest Days"),
        SmartCategorySpec("holiday", "إجازة / يوم عطلة", "Holiday / Day Off"),
        SmartCategorySpec("work", "أيام العمل", "Work Days"),
        SmartCategorySpec("study", "الدراسة والقراءة", "Study & Reading"),
        SmartCategorySpec("sleep", "النوم والراحة", "Sleep & Bedtime"),
        SmartCategorySpec("food", "الوجبات والطعام", "Meals & Food"),
        SmartCategorySpec("coffee", "القهوة والمشروبات", "Coffee & Drinks"),
        SmartCategorySpec("walk", "المشي والحركة", "Walking & Cardio"),
        SmartCategorySpec("relax", "الاسترخاء والهدوء", "Relaxation")
    )

    val taskCategoryOptions: List<SmartCategorySpec> = listOf(
        SmartCategorySpec("auto", "تلقائي حسب الاسم", "Auto Detect")
    ) + smartCategories

    val smartModeOptions: List<SmartCategorySpec> = listOf(
        SmartCategorySpec("manual", "يدوي (اختياري الثابت)", "Manual Selection"),
        SmartCategorySpec("category", "أيقونة الفئة المفضلة", "Category Preferred"),
        SmartCategorySpec("rotate", "تبديل دوري من مفضلاتي", "Rotate My Favorites"),
        SmartCategorySpec("schedule", "حسب يوم الأسبوع", "By Weekday Schedule")
    )

    fun inferCategory(task: SmartTask): String {
        val explicit = task.badgeCategory.trim().lowercase()
        if (explicit.isNotBlank() && explicit != "auto") return explicit

        val text = "${task.title} ${task.meta} ${task.cycleSteps.joinToString(" ")}".lowercase()
        return when {
            containsAny(text, "جيم", "تمرين", "رجل", "صدر", "ظهر", "أكتاف", "ذراع", "حديد", "gym", "workout", "leg", "push", "pull", "cardio", "fitness") ||
                task.repeat == RepeatMode.CYCLE -> "gym"
            containsAny(text, "إجازة", "عطلة", "holiday", "day off", "vacation") -> "holiday"
            containsAny(text, "راحة", "استشفاء", "rest day", "off day") -> "rest"
            containsAny(text, "نوم", "سرير", "غفوة", "منام", "sleep", "bed", "nap", "night") -> "sleep"
            containsAny(text, "أكل", "وجبة", "فطار", "غداء", "عشاء", "طعام", "بروتين", "food", "meal", "eat", "lunch", "dinner", "breakfast") -> "food"
            containsAny(text, "قهوة", "شاي", "ماء", "مياه", "مشروب", "عصير", "coffee", "tea", "water", "drink", "cafe") -> "coffee"
            containsAny(text, "مشي", "جري", "خطوات", "دراجة", "سباحة", "walk", "run", "steps", "bike", "hike", "swim") -> "walk"
            containsAny(text, "مذاكرة", "دراسة", "قراءة", "كتاب", "أذكار", "قرآن", "study", "read", "book", "exam", "course") -> "study"
            containsAny(text, "عمل", "شغل", "مكتب", "اجتماع", "مشروع", "work", "office", "meeting", "project", "client") -> "work"
            containsAny(text, "استرخاء", "تأمل", "يوجا", "هدوء", "relax", "yoga", "meditate", "calm", "spa") -> "relax"
            else -> "work"
        }
    }

    fun resolveTaskBadgeIconId(
        task: SmartTask,
        settings: SmartIconSettings,
        date: LocalDate = LocalDate.now()
    ): String? {
        if (!task.badgeVisible) return null

        val taskMode = task.badgeSmartMode.ifBlank { "manual" }
        val autoAllowed = settings.enableSmartAutoIcons || taskMode != "manual"

        // Manual selections are ALWAYS respected unless automatic icon changes are explicitly enabled
        if (!autoAllowed) {
            return task.badgeIconId.takeIf { it.isNotBlank() }
        }

        val effectiveMode = if (taskMode != "manual") taskMode else settings.smartDefaultMode
        val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val jsDay = (date.dayOfWeek.value % 7) // 0=Sun .. 6=Sat

        // 1. Holiday / Day Off override (uses ONLY user's selected holiday icon(s))
        if (settings.todayIsHoliday || settings.holidayDates.contains(dateStr)) {
            val holidayFavs = settings.categoryFavorites["holiday"].orEmpty().filter { it.isNotBlank() }
            if (holidayFavs.isNotEmpty()) {
                return if (effectiveMode == "rotate" && holidayFavs.size > 1) {
                    val idx = ((date.toEpochDay().toInt() % holidayFavs.size) + holidayFavs.size) % holidayFavs.size
                    holidayFavs[idx]
                } else {
                    holidayFavs.first()
                }
            }
        }

        // 2. Recurring Task Off-Day / Rest-Day override (uses ONLY user's selected rest-day icon(s))
        if (settings.useRestDayIconOnOffDays && !task.isActiveOnDate(date)) {
            val restFavs = settings.categoryFavorites["rest"].orEmpty().filter { it.isNotBlank() }
            if (restFavs.isNotEmpty()) {
                return if (effectiveMode == "rotate" && restFavs.size > 1) {
                    val idx = ((date.toEpochDay().toInt() % restFavs.size) + restFavs.size) % restFavs.size
                    restFavs[idx]
                } else {
                    restFavs.first()
                }
            }
        }

        val category = inferCategory(task)
        val userCategoryFavs = settings.categoryFavorites[category].orEmpty().filter { it.isNotBlank() }

        return when (effectiveMode) {
            "manual" -> task.badgeIconId.takeIf { it.isNotBlank() }

            "schedule" -> {
                settings.dayOfWeekIcons[jsDay]?.takeIf { it.isNotBlank() }
                    ?: task.badgeIconId.takeIf { it.isNotBlank() }
                    ?: userCategoryFavs.firstOrNull()
            }

            "rotate" -> {
                if (userCategoryFavs.isNotEmpty()) {
                    val seed = date.toEpochDay() + task.cycleIndex
                    val idx = ((seed % userCategoryFavs.size).toInt() + userCategoryFavs.size) % userCategoryFavs.size
                    userCategoryFavs[idx]
                } else {
                    task.badgeIconId.takeIf { it.isNotBlank() }
                }
            }

            "category" -> {
                userCategoryFavs.firstOrNull()
                    ?: task.badgeIconId.takeIf { it.isNotBlank() }
            }

            else -> task.badgeIconId.takeIf { it.isNotBlank() }
        }
    }

    private fun containsAny(haystack: String, vararg needles: String): Boolean =
        needles.any { haystack.contains(it) }
}
