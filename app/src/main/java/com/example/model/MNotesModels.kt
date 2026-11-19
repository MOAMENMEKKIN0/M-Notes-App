package com.example.model

import androidx.annotation.Keep
import androidx.compose.ui.graphics.Color
import com.example.R
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class RepeatMode(val wire: String) {
    NONE("NONE"),
    DAILY("DAILY"),
    WEEKLY("WEEKLY"),
    CYCLE("CYCLE");

    companion object {
        fun fromWire(raw: String): RepeatMode =
            entries.firstOrNull { it.wire == raw } ?: NONE
    }
}

@Keep
@IgnoreExtraProperties
data class SmartTask(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val meta: String = "",
    val imgUri: String = "",
    val repeatMode: String = RepeatMode.NONE.wire,
    val cycleSteps: List<String> = emptyList(),
    val cycleIndex: Long = 0L,
    val weekDays: List<Long> = emptyList(),
    val alertEnabled: Boolean = true,
    val alertTime: String = "",
    val nextDueDate: String = "",
    val lastAlertedDate: String = "",
    val ringId: String = "",
    val sortOrder: Long = 0L,
    val badgeIconId: String = "",
    val badgeVisible: Boolean = true,
    val badgeOffsetX: Float = 20f,
    val badgeOffsetY: Float = -14f,
    val badgeSizeDp: Float = 28f,
    val badgeCategory: String = "auto",
    val badgeSmartMode: String = "manual",
    val badgeLocked: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val repeat: RepeatMode
        get() = RepeatMode.fromWire(repeatMode)

    fun usesWeekDays(): Boolean =
        (repeat == RepeatMode.WEEKLY || repeat == RepeatMode.CYCLE) && weekDays.isNotEmpty()

    fun currentCycleLabel(): String {
        if (repeat != RepeatMode.CYCLE || cycleSteps.isEmpty()) return ""
        val idx = ((cycleIndex % cycleSteps.size).toInt() + cycleSteps.size) % cycleSteps.size
        return "${cycleSteps[idx]} (${idx + 1}/${cycleSteps.size})"
    }

    fun currentCycleStepOnly(): String {
        if (repeat != RepeatMode.CYCLE || cycleSteps.isEmpty()) return ""
        val idx = ((cycleIndex % cycleSteps.size).toInt() + cycleSteps.size) % cycleSteps.size
        return cycleSteps[idx]
    }

    fun displayTitle(): String {
        val step = currentCycleStepOnly()
        return if (step.isNotBlank()) "$title • $step" else title
    }

    fun isActiveOnDate(date: LocalDate = LocalDate.now()): Boolean {
        val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val jsDay = (date.dayOfWeek.value % 7).toLong()
        val notDeferred = nextDueDate.isBlank() || nextDueDate <= dateStr
        return if (usesWeekDays()) {
            weekDays.contains(jsDay) && notDeferred
        } else {
            notDeferred
        }
    }

    fun nextScheduledDayLabel(dayLabels: List<String>, today: LocalDate = LocalDate.now()): String {
        if (dayLabels.size < 7) return nextDueDate
        val tomorrow = today.plusDays(1)
        if (usesWeekDays()) {
            for (offset in 1L..7L) {
                val candidate = today.plusDays(offset)
                val jsDay = (candidate.dayOfWeek.value % 7).toLong()
                if (weekDays.contains(jsDay)) {
                    return dayLabels.getOrNull(jsDay.toInt()).orEmpty()
                }
            }
        }
        val parsed = runCatching { LocalDate.parse(nextDueDate) }.getOrNull()
        val target = parsed ?: tomorrow
        val jsDay = (target.dayOfWeek.value % 7).toLong()
        return dayLabels.getOrNull(jsDay.toInt()).orEmpty()
    }
}

@Keep
@IgnoreExtraProperties
data class QuickRequest(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val body: String = "",
    val docType: String = "invoice",
    val customerName: String = "",
    val amountText: String = "",
    val stampId: String = "none",
    val isSigned: Boolean = false,
    val signatureName: String = "",
    val statusDot: String = "none",
    val statusLabel: String = "",
    val splitMode: String = "full",
    val isChecklistOrder: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class UserPreferences(
    val userId: String = "",
    val lang: String = "ar",
    val themeId: String = "mint-clean",
    val fontId: String = "cairo",
    val fontWeight: Long = 600L,
    val topBarStyle: String = "T11",
    val alertStyle: String = "A2",
    val bannerStyle: String = "B1",
    val bannerIcon: String = "star",
    val bannerTitle: String = "M Notes",
    val bannerSub: String = "",
    val bannerImg: String = "default",
    val bannerImgSize: String = "medium",
    val bannerImgDp: Int = 54,
    val bannerPhotoShape: String = "ring",
    val taskImgSize: String = "medium",
    val taskImgDp: Int = 48,
    val alertImgDp: Int = 120,
    val textScalePercent: Int = 100,
    val cardScalePercent: Int = 100,
    val cardShape: String = "ring",
    val showBanner: Boolean = true,
    val showTopBarTabs: Boolean = true,
    val showStatsSummary: Boolean = true,
    val showWeekStrip: Boolean = true,
    val showSpotlightCard: Boolean = true,
    val showFilterChips: Boolean = true,
    val showSearchBar: Boolean = true,
    val showQuickDock: Boolean = true,
    val customPrimaryHex: String = "",
    val customBgHex: String = "",
    val customAccentHex: String = "",
    val customIconShape: String = "circle",
    val soundNav: Boolean = true,
    val soundEnter: Boolean = true,
    val soundExit: Boolean = true,
    val soundDone: Boolean = true,
    val swipePages: Boolean = true,
    val swipeClose: Boolean = true,
    val hapticEnabled: Boolean = true,
    val activeRingId: String = "G1",
    val autoBackup: Boolean = true,
    val refreshRateHz: Int = 120,
    val layoutStyle: String = "L1",
    val appIconId: String = "I1",
    val lastBackupLabel: String = "",
    val reqSignatureName: String = "",
    val reqDefaultDocType: String = "invoice",
    val reqDefaultSplitMode: String = "word_2",
    val reqDefaultStatusLabel: String = "جاهز",
    val reqCustomStatuses: String = "متاح، غير متاح، وصل، لم يصل، جاهز، غير مكتمل",
    val reqAutoSignOnComplete: Boolean = true,
    val reqShowDocTabs: Boolean = true,
    val reqShowStamps: Boolean = true,
    val reqShowSignature: Boolean = true,
    val reqShowSplitControls: Boolean = true,
    val reqShowOrdersSection: Boolean = true,
    val reqShowRedGreenDots: Boolean = true,
    val reqShowStatusBadge: Boolean = true,
    val reqShowCustomer: Boolean = true,
    val reqShowAmount: Boolean = true,
    val reqShowDate: Boolean = true,
    val reqShowHeaderBadge: Boolean = true,
    val reqDefaultStampId: String = "paid_thanks",
    val reminderBgThemeId: String = "default",
    val reminderDoodlePackId: String = "none",
    val reminderDoodleOpacity: Int = 14,
    val mainWallpaperUri: String = "",
    val mainWallpaperDimPercent: Int = 0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CloudBackupRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val accountLabel: String = "",
    val clientTime: String = "",
    val tasksCount: Long = 0L,
    val requestsCount: Long = 0L,
    val snapshotJson: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Serializable
data class LibraryPhoto(
    val id: String,
    val name: String,
    val filePath: String,
    val enabled: Boolean = true
)

@Serializable
data class TrimmedRingtone(
    val id: String,
    val name: String,
    val filePath: String,
    val durationSec: Int
)

@Serializable
data class FloatingBadgeItem(
    val id: String,
    val state: String = "sleeping",
    val xFraction: Float = 0.82f,
    val yFraction: Float = 0.14f,
    val sizeDp: Float = 46f,
    val visible: Boolean = true,
    val locked: Boolean = false
)

@Serializable
data class SmartIconSettings(
    val enableSmartAutoIcons: Boolean = false,
    val smartDefaultMode: String = "manual", // "manual" | "category" | "rotate" | "schedule"
    val useRestDayIconOnOffDays: Boolean = true,
    val todayIsHoliday: Boolean = false,
    val categoryFavorites: Map<String, List<String>> = mapOf(
        "gym" to listOf("gym_dumbbell_mint", "gym_dumbbell_fire", "char_active_bolt"),
        "rest" to listOf("char_sleeping_zzz", "sleep_moon_zzz"),
        "holiday" to listOf("relax_spa_lotus", "char_happy_heart"),
        "work" to listOf("work_laptop_focus", "work_briefcase"),
        "study" to listOf("work_study_book", "work_grad_cap"),
        "sleep" to listOf("char_sleeping_zzz", "sleep_zzz_cloud"),
        "food" to listOf("food_healthy_meal", "food_breakfast"),
        "coffee" to listOf("drink_coffee_steam", "drink_mint_tea"),
        "walk" to listOf("walk_steps_mint", "walk_run_fast"),
        "relax" to listOf("relax_zen_cloud", "char_resting_moon")
    ),
    val dayOfWeekIcons: Map<Int, String> = emptyMap(),
    val holidayDates: List<String> = emptyList()
)

@Serializable
data class BackupTaskDto(
    val title: String,
    val meta: String = "",
    val imgUri: String = "",
    val repeatMode: String = "NONE",
    val cycleSteps: List<String> = emptyList(),
    val cycleIndex: Long = 0L,
    val weekDays: List<Long> = emptyList(),
    val alertEnabled: Boolean = true,
    val alertTime: String = "",
    val nextDueDate: String = "",
    val lastAlertedDate: String = "",
    val ringId: String = "",
    val sortOrder: Long = 0L,
    val badgeIconId: String = "",
    val badgeVisible: Boolean = true,
    val badgeOffsetX: Float = 20f,
    val badgeOffsetY: Float = -14f,
    val badgeSizeDp: Float = 28f,
    val badgeCategory: String = "auto",
    val badgeSmartMode: String = "manual",
    val badgeLocked: Boolean = false
)

@Serializable
data class BackupRequestDto(
    val title: String,
    val body: String = "",
    val docType: String = "invoice",
    val customerName: String = "",
    val amountText: String = "",
    val stampId: String = "none",
    val isSigned: Boolean = false,
    val signatureName: String = "",
    val statusDot: String = "none",
    val statusLabel: String = "",
    val splitMode: String = "full",
    val isChecklistOrder: Boolean = false
)

@Serializable
data class BackupPreferencesDto(
    val lang: String = "ar",
    val themeId: String = "mint-clean",
    val fontId: String = "cairo",
    val fontWeight: Long = 600L,
    val topBarStyle: String = "T11",
    val alertStyle: String = "A2",
    val bannerStyle: String = "B1",
    val bannerIcon: String = "star",
    val bannerTitle: String = "M Notes",
    val bannerSub: String = "",
    val bannerImgSize: String = "medium",
    val bannerImgDp: Int = 54,
    val bannerPhotoShape: String = "ring",
    val taskImgSize: String = "medium",
    val taskImgDp: Int = 48,
    val alertImgDp: Int = 120,
    val textScalePercent: Int = 100,
    val cardScalePercent: Int = 100,
    val cardShape: String = "ring",
    val showBanner: Boolean = true,
    val showTopBarTabs: Boolean = true,
    val showStatsSummary: Boolean = true,
    val showWeekStrip: Boolean = true,
    val showSpotlightCard: Boolean = true,
    val showFilterChips: Boolean = true,
    val showSearchBar: Boolean = true,
    val showQuickDock: Boolean = true,
    val customPrimaryHex: String = "",
    val customBgHex: String = "",
    val customAccentHex: String = "",
    val customIconShape: String = "circle",
    val soundNav: Boolean = true,
    val soundEnter: Boolean = true,
    val soundExit: Boolean = true,
    val soundDone: Boolean = true,
    val swipePages: Boolean = true,
    val swipeClose: Boolean = true,
    val hapticEnabled: Boolean = true,
    val activeRingId: String = "G1",
    val autoBackup: Boolean = true,
    val refreshRateHz: Int = 120,
    val layoutStyle: String = "L1",
    val appIconId: String = "I1",
    val reqSignatureName: String = "",
    val reqDefaultDocType: String = "invoice",
    val reqDefaultSplitMode: String = "word_2",
    val reqDefaultStatusLabel: String = "جاهز",
    val reqCustomStatuses: String = "متاح، غير متاح، وصل، لم يصل، جاهز، غير مكتمل",
    val reqAutoSignOnComplete: Boolean = true,
    val reqShowDocTabs: Boolean = true,
    val reqShowStamps: Boolean = true,
    val reqShowSignature: Boolean = true,
    val reqShowSplitControls: Boolean = true,
    val reqShowOrdersSection: Boolean = true,
    val reqShowRedGreenDots: Boolean = true,
    val reqShowStatusBadge: Boolean = true,
    val reqShowCustomer: Boolean = true,
    val reqShowAmount: Boolean = true,
    val reqShowDate: Boolean = true,
    val reqShowHeaderBadge: Boolean = true,
    val reqDefaultStampId: String = "paid_thanks",
    val reminderBgThemeId: String = "default",
    val reminderDoodlePackId: String = "none",
    val reminderDoodleOpacity: Int = 14,
    val mainWallpaperUri: String = "",
    val mainWallpaperDimPercent: Int = 0
)

@Serializable
data class BackupSnapshotPayload(
    val version: String = "1.0",
    val savedAt: String = "",
    val preferences: BackupPreferencesDto = BackupPreferencesDto(),
    val tasks: List<BackupTaskDto> = emptyList(),
    val requests: List<BackupRequestDto> = emptyList(),
    val floatingBadges: List<FloatingBadgeItem> = emptyList(),
    val smartIconSettings: SmartIconSettings = SmartIconSettings(),
    val checklistOrders: List<ChecklistOrderItem> = emptyList(),
    val checklistStyle: ChecklistStyleConfig = ChecklistStyleConfig()
)

data class AppThemeSpec(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val bgMain: Color,
    val bgTopGlow: Color,
    val cardBg: Color,
    val borderSoft: Color,
    val textDark: Color,
    val textMuted: Color,
    val primary: Color,
    val primarySoft: Color,
    val accent: Color
)

data class GymTrack(
    val id: String,
    val nameAr: String,
    val nameEn: String
)

data class StyleOption(
    val id: String,
    val nameAr: String,
    val nameEn: String
)

data class PhotoCategorySpec(
    val id: String,
    val nameAr: String,
    val nameEn: String
)

data class BuiltInPhotoSpec(
    val uri: String,
    val nameAr: String,
    val nameEn: String,
    val category: String = "heritage",
    val extraCategories: Set<String> = emptySet()
) {
    fun matchesCategory(categoryId: String): Boolean =
        categoryId == "all" || category == categoryId || extraCategories.contains(categoryId)
}

data class LauncherIconSpec(
    val id: String,
    val aliasClassName: String,
    val nameAr: String,
    val nameEn: String,
    val bgStart: Color,
    val bgEnd: Color,
    val fgDrawableRes: Int,
    val isBitmapArt: Boolean = false
)

data class CharacterBadgeStateSpec(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val drawableRes: Int
)

object MNotesCatalog {
    val themes = listOf(
        AppThemeSpec(
            id = "caramel-sand", nameAr = "كراميل رملي وزجاج دافئ", nameEn = "Caramel Sand",
            bgMain = Color(0xFFFAF5EE), bgTopGlow = Color(0xFFDEC1A4), cardBg = Color(0xFFFFFDF9),
            borderSoft = Color(0xFFEADBC8), textDark = Color(0xFF291408), textMuted = Color(0xFF7A563C),
            primary = Color(0xFF9A5B25), primarySoft = Color(0xFFFAF0E6), accent = Color(0xFFD97706)
        ),
        AppThemeSpec(
            id = "emerald-frost", nameAr = "أبيض جليدي وأخضر زمردي", nameEn = "Emerald Frost",
            bgMain = Color(0xFFF5F8FA), bgTopGlow = Color(0xFFDAF5EC), cardBg = Color(0xFFFFFFFF),
            borderSoft = Color(0xFFE7EEF3), textDark = Color(0xFF0D1B2A), textMuted = Color(0xFF64748B),
            primary = Color(0xFF10B981), primarySoft = Color(0xFFE6FBF3), accent = Color(0xFFEA580C)
        ),
        AppThemeSpec(
            id = "mint-clean", nameAr = "نعناعي صافي", nameEn = "Mint Clean",
            bgMain = Color(0xFFF7FAFC), bgTopGlow = Color(0xFFE2F6EF), cardBg = Color(0xFFFFFFFF),
            borderSoft = Color(0xFFEBF1F5), textDark = Color(0xFF0F172A), textMuted = Color(0xFF64748B),
            primary = Color(0xFF18C28B), primarySoft = Color(0xFFE6F9F2), accent = Color(0xFF2AA3F8)
        ),
        AppThemeSpec(
            id = "sky-saas", nameAr = "أزرق سماوي", nameEn = "Sky Blue",
            bgMain = Color(0xFFF6FAFF), bgTopGlow = Color(0xFFDCEFFF), cardBg = Color(0xFFFFFFFF),
            borderSoft = Color(0xFFE3EEF9), textDark = Color(0xFF0F172A), textMuted = Color(0xFF64748B),
            primary = Color(0xFF2AA3F8), primarySoft = Color(0xFFE6F4FF), accent = Color(0xFF0EA5E9)
        ),
        AppThemeSpec(
            id = "ocean-teal", nameAr = "تيل محيطي هادئ", nameEn = "Ocean Teal",
            bgMain = Color(0xFFF4FBFB), bgTopGlow = Color(0xFFCCFBF1), cardBg = Color(0xFFFFFFFF),
            borderSoft = Color(0xFFE2F2F0), textDark = Color(0xFF134E4A), textMuted = Color(0xFF5E7B78),
            primary = Color(0xFF0D9488), primarySoft = Color(0xFFE6FAF8), accent = Color(0xFF06B6D4)
        ),
        AppThemeSpec(
            id = "mono-bw", nameAr = "أبيض وأسود كلاسيك", nameEn = "Classic Mono",
            bgMain = Color(0xFFFAFAFA), bgTopGlow = Color(0xFFEAEAEA), cardBg = Color(0xFFFFFFFF),
            borderSoft = Color(0xFFE5E5E5), textDark = Color(0xFF171717), textMuted = Color(0xFF737373),
            primary = Color(0xFF171717), primarySoft = Color(0xFFEFEFEF), accent = Color(0xFF525252)
        ),
        AppThemeSpec(
            id = "arctic-ice", nameAr = "ثلجي جليدي", nameEn = "Arctic Ice",
            bgMain = Color(0xFFF4F9FC), bgTopGlow = Color(0xFFD8EEFA), cardBg = Color(0xFFFFFFFF),
            borderSoft = Color(0xFFE2EFF7), textDark = Color(0xFF0C2D48), textMuted = Color(0xFF5B7B94),
            primary = Color(0xFF0284C7), primarySoft = Color(0xFFE3F4FD), accent = Color(0xFF38BDF8)
        ),
        AppThemeSpec(
            id = "sage-leaf", nameAr = "أخضر مريمية", nameEn = "Sage Leaf",
            bgMain = Color(0xFFF5F8F6), bgTopGlow = Color(0xFFDCE9E0), cardBg = Color(0xFFFFFFFF),
            borderSoft = Color(0xFFE2EBE5), textDark = Color(0xFF1B3326), textMuted = Color(0xFF61796B),
            primary = Color(0xFF2E7D5B), primarySoft = Color(0xFFE5F3EC), accent = Color(0xFF4CAF50)
        ),
        AppThemeSpec(
            id = "nordic-gray", nameAr = "نورديك رمادي أزرق", nameEn = "Nordic Gray",
            bgMain = Color(0xFFF4F6F9), bgTopGlow = Color(0xFFE1E7F0), cardBg = Color(0xFFFFFFFF),
            borderSoft = Color(0xFFE5EAF1), textDark = Color(0xFF1E293B), textMuted = Color(0xFF64748B),
            primary = Color(0xFF475569), primarySoft = Color(0xFFE9EEF5), accent = Color(0xFF64748B)
        )
    )

    fun themeById(id: String): AppThemeSpec = themes.firstOrNull { it.id == id } ?: themes.first()

    val alertStyles = listOf(
        StyleOption("A1", "شريط علوي انسيابي", "Top Floating Bar"),
        StyleOption("A2", "تنبيه ملء الشاشة", "Full Screen Focus"),
        StyleOption("A3", "كارت وسطي بارز", "Center Modal Card"),
        StyleOption("A4", "بطاقة سفلية منبثقة", "Bottom Sheet Alert"),
        StyleOption("A5", "كبسولة رفيعة عائمة", "Compact Pill Alert"),
        StyleOption("A6", "تنبيه بحلقة نبض ضوئية", "Pulse Halo Alert")
    )

    val topBarStyles = listOf(
        StyleOption("T1", "كبسولة بمؤشر منزلق", "Sliding Pill"),
        StyleOption("T11", "كبسولة iOS مرتفعة", "Elevated iOS Pill"),
        StyleOption("T13", "خط متحرك تحت الكلمة", "Animated Underline"),
        StyleOption("T18", "كبسولة وسطية مضغوطة", "Compact Pill"),
        StyleOption("T20", "كروت بنقطة لونية", "Active Dot Cards")
    )

    val bannerStyles = listOf(
        StyleOption("B1", "تدرج نعناعي", "Soft Glow"),
        StyleOption("B2", "صورة كاملة", "Full Photo"),
        StyleOption("B3", "مقسوم نصين", "Split Card"),
        StyleOption("B4", "كبسولة عائمة", "Floating Pill"),
        StyleOption("B5", "زجاجي شفاف", "Glassmorphism"),
        StyleOption("B7", "بشريط التقدم", "Progress Hero"),
        StyleOption("B8", "مزدوج الطبقات", "3D Layered")
    )

    val bannerIcons = listOf(
        StyleOption("none", "بدون أيقونة", "No Icon"),
        StyleOption("clock", "ساعة", "Clock"),
        StyleOption("award", "وسام", "Award"),
        StyleOption("star", "نجمة", "Star"),
        StyleOption("target", "هدف", "Target"),
        StyleOption("gym", "دمبل الجيم", "Gym Dumbbell"),
        StyleOption("flame", "شعلة النشاط", "Streak Flame"),
        StyleOption("heart", "صحة وعافية", "Wellness"),
        StyleOption("check", "وسام الإنجاز", "Check Badge")
    )

    val fontOptions = listOf(
        StyleOption("cairo", "Cairo", "Cairo"),
        StyleOption("tajawal", "Tajawal", "Tajawal"),
        StyleOption("ibm_plex", "IBM Plex", "IBM Plex"),
        StyleOption("almarai", "Almarai", "Almarai")
    )

    val gymTracks = listOf(
        GymTrack("G1", "نبض الجيم القوي (١٠ ث)", "Gym Power Beat (10s)"),
        GymTrack("G2", "طاقة الأبطال الحماسية (١٠ ث)", "Champion Anthem (10s)"),
        GymTrack("G3", "جرس حلبة الملاكمة وانطلاق (١٠ ث)", "Boxing Ring Rush (10s)"),
        GymTrack("G4", "إيقاع الحديد والتركيز (١٠ ث)", "Iron Focus Groove (10s)"),
        GymTrack("G5", "عد تنازلي ثم هجوم! (١٠ ث)", "Countdown Attack (10s)"),
        GymTrack("G6", "نشيد الفوز والتحدي (١٠ ث)", "Victory Challenge (10s)"),
        GymTrack("G7", "تكنو الجري والكارديو (١٠ ث)", "Cardio Techno Beat (10s)"),
        GymTrack("G8", "انطلاقة الصباح النشطة (١٠ ث)", "Morning Warmup Rush (10s)"),
        GymTrack("G9", "ضخ الأوزان الثقيلة (١٠ ث)", "Heavy Lift Drop (10s)"),
        GymTrack("G10", "أدرينالين الجولة الأخيرة (١٠ ث)", "Final Set Adrenaline (10s)")
    )

    val photoCategories = listOf(
        PhotoCategorySpec("all", "كل الصور", "All Photos"),
        PhotoCategorySpec("chest", "تمارين الصدر", "Chest"),
        PhotoCategorySpec("back", "تمارين الظهر", "Back"),
        PhotoCategorySpec("legs", "تمارين الرجل", "Legs"),
        PhotoCategorySpec("cardio", "كارديو ولياقة", "Cardio"),
        PhotoCategorySpec("abs", "البطن والكور", "Abs & Core"),
        PhotoCategorySpec("shoulders_arms", "الأكتاف والذراع", "Shoulders & Arms"),
        PhotoCategorySpec("recovery", "إحماء وإطالة", "Warm-Up & Recovery"),
        PhotoCategorySpec("gym", "أدوات الجيم", "Gym & Gear"),
        PhotoCategorySpec("fruits", "فواكه زمان", "Fruits"),
        PhotoCategorySpec("vegetables", "خضروات زمان", "Vegetables"),
        PhotoCategorySpec("heritage", "شخصيات وتراث", "Characters")
    )

    val pixel3dPhotos = listOf(
        // === CHEST EXERCISES (New Pixar 3D Pack + Preserved Classics) ===
        BuiltInPhotoSpec("res://img_3d_chest_pack#0", "بنش برس مسطح بالبار", "Flat Barbell Bench Press", "chest", setOf("gym")),
        BuiltInPhotoSpec("res://img_3d_chest_pack#1", "صدر عالي بالدمبل", "Incline Dumbbell Press", "chest", setOf("gym")),
        BuiltInPhotoSpec("res://img_3d_chest_pack#2", "جهاز ضغط صدر سفلي", "Decline Chest Machine", "chest", setOf("gym")),
        BuiltInPhotoSpec("res://img_3d_chest_pack#3", "ضغط صدر مسطح بالدمبل", "Flat Dumbbell Press", "chest", setOf("gym")),
        BuiltInPhotoSpec("res://img_3d_chest_pack#4", "تفتيح كابل كروس أوفر", "Cable Chest Crossover", "chest", setOf("gym")),
        BuiltInPhotoSpec("res://img_3d_chest_pack#5", "جهاز الفراشة للصدر", "Pec Deck Butterfly Fly", "chest", setOf("gym")),
        BuiltInPhotoSpec("res://img_3d_chest_pack#6", "تفتيح صدر بالدمبل", "Dumbbell Chest Fly", "chest", setOf("gym")),
        BuiltInPhotoSpec("res://img_3d_chest_pack#7", "تمرين الضغط الواسع", "Wide-Grip Push-Ups", "chest", setOf("gym")),
        BuiltInPhotoSpec("res://img_3d_chest_pack#8", "متوازي للصدر", "Parallel Bar Chest Dips", "chest", setOf("gym")),

        // === BACK EXERCISES (New Pixar 3D Pack) ===
        BuiltInPhotoSpec("res://img_3d_back_pack#0", "سحب عالي واسع للظهر", "Wide-Grip Lat Pulldown", "back", setOf("gym")),
        BuiltInPhotoSpec("res://img_3d_back_pack#1", "عقلة كاملة للظهر", "Overhead Pull-Ups", "back", setOf("gym")),
        BuiltInPhotoSpec("res://img_3d_back_pack#2", "سحب أرضي جالس بالكابل", "Seated Cable Row", "back", setOf("gym")),
        BuiltInPhotoSpec("res://img_3d_back_pack#3", "منشار ظهر بالدمبل", "One-Arm Dumbbell Row", "back", setOf("gym")),
        BuiltInPhotoSpec("res://img_3d_back_pack#4", "تجديف بالبار منحني", "Bent-Over Barbell Row", "back", setOf("gym")),
        BuiltInPhotoSpec("res://img_3d_back_pack#5", "تجديف تي-بار للظهر", "T-Bar Landmine Row", "back", setOf("gym")),
        BuiltInPhotoSpec("res://img_3d_back_pack#6", "سحب حبل للوجه والكتف الخلفي", "Cable Face Pulls", "back", setOf("shoulders_arms", "gym")),
        BuiltInPhotoSpec("res://img_3d_back_pack#7", "تمرين القطنية وأسفل الظهر", "Lower Back Extension", "back", setOf("gym")),
        BuiltInPhotoSpec("res://img_3d_back_pack#8", "سحب كابل ذراع مستقيمة", "Straight-Arm Pulldown", "back", setOf("gym")),

        // === LEGS & GLUTES EXERCISES (New Pixar 3D Pack) ===
        BuiltInPhotoSpec("res://img_3d_legs_pack#0", "سكوات خلفي بالبار", "Barbell Back Squat", "legs", setOf("gym")),
        BuiltInPhotoSpec("res://img_3d_legs_pack#1", "دفع رجل مائل ٤٥ درجة", "Angled Leg Press", "legs", setOf("gym")),
        BuiltInPhotoSpec("res://img_3d_legs_pack#2", "مشي الطعن بالدمبل", "Dumbbell Walking Lunges", "legs", setOf("gym")),
        BuiltInPhotoSpec("res://img_3d_legs_pack#3", "رفرفة أماميات الرجل", "Seated Leg Extension", "legs", setOf("gym")),
        BuiltInPhotoSpec("res://img_3d_legs_pack#4", "جهاز خلفيات الرجل نائم", "Lying Hamstring Curl", "legs", setOf("gym")),
        BuiltInPhotoSpec("res://img_3d_legs_pack#5", "تمرين السمانة واقف", "Standing Calf Raises", "legs", setOf("gym")),
        BuiltInPhotoSpec("res://img_3d_legs_pack#6", "ديدليفت روماني للخلفيات", "Romanian Deadlift", "legs", setOf("back", "gym")),
        BuiltInPhotoSpec("res://img_3d_legs_pack#7", "دفع الحوض بالبار", "Barbell Hip Thrust", "legs", setOf("gym")),
        BuiltInPhotoSpec("res://img_3d_legs_pack#8", "سكوات بلغاري فردي", "Bulgarian Split Squat", "legs", setOf("gym")),

        // === ABS & CORE EXERCISES (New Pixar 3D Pack) ===
        BuiltInPhotoSpec("res://img_3d_abs_core_pack#0", "طحن البطن على المرتبة", "Mat Ab Crunches", "abs", setOf("gym")),
        BuiltInPhotoSpec("res://img_3d_abs_core_pack#1", "رفع الرجل معلق على العقلة", "Hanging Leg Raises", "abs", setOf("gym")),
        BuiltInPhotoSpec("res://img_3d_abs_core_pack#2", "تمرين البلانك الثابت", "Forearm Plank Hold", "abs", setOf("gym")),
        BuiltInPhotoSpec("res://img_3d_abs_core_pack#3", "بلانك جانبي للخواصر", "Side Plank Hold", "abs", setOf("gym")),
        BuiltInPhotoSpec("res://img_3d_abs_core_pack#4", "بطن الدراجة الهوائية", "Bicycle Crunches", "abs", setOf("gym")),
        BuiltInPhotoSpec("res://img_3d_abs_core_pack#5", "تمرين بطن بالحبل كابل", "Cable Rope Crunches", "abs", setOf("gym")),
        BuiltInPhotoSpec("res://img_3d_abs_core_pack#6", "التفاف روسي بالكرة الطبية", "Russian Twists", "abs", setOf("gym")),
        BuiltInPhotoSpec("res://img_3d_abs_core_pack#7", "عجلة البطن القوية", "Ab Wheel Rollout", "abs", setOf("gym")),
        BuiltInPhotoSpec("res://img_3d_abs_core_pack#8", "متسلق الجبال للبطن والكارديو", "Mountain Climbers", "abs", setOf("cardio", "gym")),

        // === SHOULDERS, ARMS, WARM-UP & RECOVERY (New Pixar 3D Pack) ===
        BuiltInPhotoSpec("res://img_3d_shoulders_arms_recovery_pack#0", "رفرفة جانبي للأكتاف", "Lateral Shoulder Raises", "shoulders_arms", setOf("gym")),
        BuiltInPhotoSpec("res://img_3d_shoulders_arms_recovery_pack#1", "ضغط أكتاف علوي بالدمبل", "Overhead Dumbbell Press", "shoulders_arms", setOf("gym")),
        BuiltInPhotoSpec("res://img_3d_shoulders_arms_recovery_pack#2", "تبادل بايسبس بالدمبل", "Dumbbell Bicep Curls", "shoulders_arms", setOf("gym")),
        BuiltInPhotoSpec("res://img_3d_shoulders_arms_recovery_pack#3", "تمرين شاكوش للذراع", "Dumbbell Hammer Curls", "shoulders_arms", setOf("gym")),
        BuiltInPhotoSpec("res://img_3d_shoulders_arms_recovery_pack#4", "ترايسبس كابل بالحبل", "Cable Tricep Pushdown", "shoulders_arms", setOf("gym")),
        BuiltInPhotoSpec("res://img_3d_shoulders_arms_recovery_pack#5", "غطس ترايسبس على البنش", "Bench Tricep Dips", "shoulders_arms", setOf("gym")),
        BuiltInPhotoSpec("res://img_3d_shoulders_arms_recovery_pack#6", "إحماء القفز النجمي", "Jumping Jacks Warm-Up", "recovery", setOf("cardio")),
        BuiltInPhotoSpec("res://img_3d_shoulders_arms_recovery_pack#7", "مشي سريع مائل على المشاية", "Incline Treadmill Walk", "cardio", setOf("recovery")),
        BuiltInPhotoSpec("res://img_3d_shoulders_arms_recovery_pack#8", "إطالة واستشفاء بالفوم رولر", "Stretch & Foam Roll", "recovery", setOf("gym")),

        // === PRESERVED EXISTING GYM, STRENGTH & GEAR COLLECTION (100% KEPT) ===
        BuiltInPhotoSpec("res://img_3d_gym_workout", "دمبل تمرين", "Dumbbells", "gym", setOf("shoulders_arms")),
        BuiltInPhotoSpec("res://img_3d_gym_chest", "تمرين الصدر", "Chest Press", "gym", setOf("chest")),
        BuiltInPhotoSpec("res://img_3d_gym_back", "تمرين الظهر", "Back Workout", "gym", setOf("back")),
        BuiltInPhotoSpec("res://img_3d_gym_legs", "تمرين الرجل", "Leg Day", "gym", setOf("legs")),
        BuiltInPhotoSpec("res://img_3d_gym_legpress", "جهاز دفع الرجل", "Leg Press Machine", "gym", setOf("legs")),
        BuiltInPhotoSpec("res://img_3d_gym_lunges", "طعن بالدمبل", "Dumbbell Lunges", "gym", setOf("legs")),
        BuiltInPhotoSpec("res://img_3d_gym_arms", "تمرين الذراع والبار", "Arm Curl Bar", "gym", setOf("shoulders_arms")),
        BuiltInPhotoSpec("res://img_3d_gym_shoulders", "تمرين الأكتاف", "Shoulder Press", "gym", setOf("shoulders_arms")),
        BuiltInPhotoSpec("res://img_3d_strength_exercises#0", "الرفعة الميتة بالبار", "Barbell Deadlift", "gym", setOf("back", "legs")),
        BuiltInPhotoSpec("res://img_3d_strength_exercises#1", "تمرين العقلة", "Pull-Ups", "gym", setOf("back")),
        BuiltInPhotoSpec("res://img_3d_strength_exercises#2", "سحب الكابل للظهر", "Lat Pulldown", "gym", setOf("back")),
        BuiltInPhotoSpec("res://img_3d_strength_exercises#3", "ضغط البنش بالبار", "Barbell Bench Press", "gym", setOf("chest")),
        BuiltInPhotoSpec("res://img_3d_strength_exercises#4", "تمرين الضغط", "Push-Ups", "gym", setOf("chest")),
        BuiltInPhotoSpec("res://img_3d_strength_exercises#5", "تمرين البلانك والبطن", "Core Plank", "gym", setOf("abs")),
        BuiltInPhotoSpec("res://img_3d_strength_exercises#6", "حبال القوة والتحدي", "Battle Ropes HIIT", "gym", setOf("cardio")),
        BuiltInPhotoSpec("res://img_3d_strength_exercises#7", "أرجحة الكيتل بل", "Kettlebell Swing", "gym", setOf("legs", "cardio")),
        BuiltInPhotoSpec("res://img_3d_strength_exercises#8", "إطالة ومرونة يوجا", "Stretch & Yoga", "gym", setOf("recovery")),
        BuiltInPhotoSpec("res://img_gym_gear#0", "دمبل سداسي أسود", "Hex Dumbbells", "gym"),
        BuiltInPhotoSpec("res://img_gym_gear#1", "كيتل بل نعناعي", "Kettlebell", "gym"),
        BuiltInPhotoSpec("res://img_gym_gear#2", "بار أولمبي وأوزان", "Olympic Barbell", "gym"),
        BuiltInPhotoSpec("res://img_gym_gear#3", "بنش تمرين مائل", "Workout Bench", "gym"),
        BuiltInPhotoSpec("res://img_gym_gear#4", "جهاز العقلة والمتوازي", "Pull-Up Station", "gym"),
        BuiltInPhotoSpec("res://img_gym_gear#5", "حبل قفز وأحزمة", "Jump Rope & Straps", "gym", setOf("cardio")),
        BuiltInPhotoSpec("res://img_gym_gear#6", "قفازات ملاكمة", "Boxing Gloves", "gym", setOf("cardio")),
        BuiltInPhotoSpec("res://img_gym_gear#7", "طارات أوزان ملونة", "Weight Plates", "gym"),
        BuiltInPhotoSpec("res://img_gym_gear#8", "شيكر ومنشفة جيم", "Shaker & Towel", "gym", setOf("recovery")),
        BuiltInPhotoSpec("res://img_cardio_gym_machines#3", "جهاز الكابل كروس", "Cable Crossover", "gym", setOf("chest")),
        BuiltInPhotoSpec("res://img_cardio_gym_machines#4", "باور راك وسكوات", "Power Squat Rack", "gym", setOf("legs")),
        BuiltInPhotoSpec("res://img_cardio_gym_machines#6", "كرة طبية ومات تمرين", "Med Ball & Mat", "gym", setOf("abs", "recovery")),
        BuiltInPhotoSpec("res://img_cardio_gym_machines#7", "كيس ملاكمة رياضي", "Heavy Punching Bag", "gym", setOf("cardio")),
        BuiltInPhotoSpec("res://img_cardio_gym_machines#8", "فوم رولر وحبال مقاومة", "Roller & Bands", "gym", setOf("recovery")),

        // === PRESERVED CARDIO & FITNESS ACTIVITIES & MACHINES (100% KEPT) ===
        BuiltInPhotoSpec("res://img_3d_cardio_activities#0", "الجري على المشاية", "Treadmill Run", "cardio"),
        BuiltInPhotoSpec("res://img_3d_cardio_activities#1", "الجري والهرولة", "Outdoor Running", "cardio"),
        BuiltInPhotoSpec("res://img_3d_cardio_activities#2", "دراجة الكارديو الثابتة", "Spin Cycling", "cardio"),
        BuiltInPhotoSpec("res://img_3d_cardio_activities#3", "ركوب الدراجة", "Road Cycling", "cardio"),
        BuiltInPhotoSpec("res://img_3d_cardio_activities#4", "نط الحبل كارديو", "Jump Rope Cardio", "cardio"),
        BuiltInPhotoSpec("res://img_3d_cardio_activities#5", "جهاز التجديف", "Rowing Workout", "cardio", setOf("back")),
        BuiltInPhotoSpec("res://img_3d_cardio_activities#6", "جهاز الأوربتراك", "Elliptical Trainer", "cardio"),
        BuiltInPhotoSpec("res://img_3d_cardio_activities#7", "جهاز صعود الدرج", "Stair Climber", "cardio", setOf("legs")),
        BuiltInPhotoSpec("res://img_3d_cardio_activities#8", "كارديو وملاكمة", "Boxing Cardio", "cardio"),
        BuiltInPhotoSpec("res://img_cardio_gym_machines#0", "مشاية كهربائية", "Treadmill Machine", "cardio"),
        BuiltInPhotoSpec("res://img_cardio_gym_machines#1", "دراجة تمرين رياضية", "Exercise Spin Bike", "cardio"),
        BuiltInPhotoSpec("res://img_cardio_gym_machines#2", "جهاز التجديف الرياضي", "Indoor Rower", "cardio", setOf("back")),
        BuiltInPhotoSpec("res://img_cardio_gym_machines#5", "حذاء جري وساعة", "Running Shoes", "cardio"),

        // Medieval Fruits
        BuiltInPhotoSpec("res://img_3d_ancient_fruits", "سلة فواكه زمان", "Ancient Fruit Bowl", "fruits"),
        BuiltInPhotoSpec("res://img_medieval_fruits#0", "رمان في إناء فخار", "Clay Bowl Pomegranate", "fruits"),
        BuiltInPhotoSpec("res://img_medieval_fruits#1", "تين طازج في فخار", "Fresh Figs", "fruits"),
        BuiltInPhotoSpec("res://img_medieval_fruits#2", "تمر ذهبي في صحن نحاس", "Golden Dates", "fruits"),
        BuiltInPhotoSpec("res://img_medieval_fruits#3", "عنب عنبري على خشب", "Amber Grapes", "fruits"),
        BuiltInPhotoSpec("res://img_medieval_fruits#4", "تفاح أحمر في سلة خوص", "Basket Apples", "fruits"),
        BuiltInPhotoSpec("res://img_medieval_fruits#5", "خوخ في إناء حجري", "Stoneware Peaches", "fruits"),
        BuiltInPhotoSpec("res://img_medieval_fruits#6", "كمثرى ذهبية في فخار", "Earthenware Pears", "fruits"),
        BuiltInPhotoSpec("res://img_medieval_fruits#7", "شمام على طبق خشب", "Carved Wood Melon", "fruits"),
        BuiltInPhotoSpec("res://img_medieval_fruits#8", "برقوق داكن في خزف", "Ceramic Plums", "fruits"),

        // Medieval Vegetables
        BuiltInPhotoSpec("res://img_medieval_veggies#0", "قرع عسلي على خشب", "Heirloom Pumpkin", "vegetables"),
        BuiltInPhotoSpec("res://img_medieval_veggies#1", "لفت أرجواني في فخار", "Purple Turnips", "vegetables"),
        BuiltInPhotoSpec("res://img_medieval_veggies#2", "جزر طازج على لوح خشب", "Fresh Carrots", "vegetables"),
        BuiltInPhotoSpec("res://img_medieval_veggies#3", "بصل ذهبي في سلة", "Woven Basket Onions", "vegetables"),
        BuiltInPhotoSpec("res://img_medieval_veggies#4", "ثوم مضفور في طبق حجر", "Braided Garlic", "vegetables"),
        BuiltInPhotoSpec("res://img_medieval_veggies#5", "ملفوف أخضر طازج", "Heirloom Cabbage", "vegetables"),
        BuiltInPhotoSpec("res://img_medieval_veggies#6", "كراث وأعشاب ريفية", "Leeks & Herbs", "vegetables"),
        BuiltInPhotoSpec("res://img_medieval_veggies#7", "فجل أحمر في فخار", "Terracotta Radishes", "vegetables"),
        BuiltInPhotoSpec("res://img_medieval_veggies#8", "زيتون في إناء قديم", "Ancient Olives", "vegetables"),

        // Heritage & 3D Characters
        BuiltInPhotoSpec("res://img_3d_elder_beige", "رجل كبير - بيج", "Elder - Beige", "heritage"),
        BuiltInPhotoSpec("res://img_3d_elder_man", "رجل كبير - فاتح", "Elder - Light", "heritage"),
        BuiltInPhotoSpec("res://img_3d_bald_brown", "شاب أقرع - بني", "Bald - Brown", "heritage"),
        BuiltInPhotoSpec("res://img_3d_bald_man", "شاب أقرع - فاتح", "Bald - Light", "heritage"),
        BuiltInPhotoSpec("res://img_3d_haired_mint", "شاب بشعر - نعناعي", "Man - Mint", "heritage"),
        BuiltInPhotoSpec("res://img_3d_haired_man", "شاب بشعر - فاتح", "Man - Light", "heritage"),
        BuiltInPhotoSpec("res://img_3d_child_cream", "طفلة - كريمي", "Child - Cream", "heritage"),
        BuiltInPhotoSpec("res://img_3d_child", "طفل - فاتح", "Child - Light", "heritage"),
        BuiltInPhotoSpec("res://img_3d_woman_beige", "امرأة - بيج", "Woman - Beige", "heritage"),
        BuiltInPhotoSpec("res://img_3d_woman", "امرأة - فاتح", "Woman - Light", "heritage"),
        BuiltInPhotoSpec("res://img_3d_medieval_bald", "القرون الوسطى - أصلع", "Medieval Bald", "heritage"),
        BuiltInPhotoSpec("res://img_3d_jahiliyyah", "أيام الجاهلية - صحراء", "Ancient Arabian", "heritage"),
        BuiltInPhotoSpec("res://img_3d_ancient_bread_grain", "خبز زمان وقمح وشعير", "Bread & Grains", "heritage")
    )

    val layoutOptions = listOf(
        StyleOption("L1", "١ • التفاعلية الزجاجية", "1 • Frosted Interactive"),
        StyleOption("L2", "٢ • لوحة اليوم الصافية", "2 • Clean Slate Hub"),
        StyleOption("L3", "٣ • الطبقات المنحنية", "3 • Curved Spotlight")
    )

    val launcherIcons = listOf(
        LauncherIconSpec(
            id = "I1", aliasClassName = "com.example.LauncherIcon1",
            nameAr = "١ • مينت الأساسي", nameEn = "1 • Classic Mint",
            bgStart = Color(0xFF18C28B), bgEnd = Color(0xFF0D9488),
            fgDrawableRes = R.drawable.ic_mnotes_foreground_art, isBitmapArt = true
        ),
        LauncherIconSpec(
            id = "I2", aliasClassName = "com.example.LauncherIcon2",
            nameAr = "٢ • مدور بيج وتذكير", nameEn = "2 • Circle Beige",
            bgStart = Color(0xFFF5EBDD), bgEnd = Color(0xFFEADBC8),
            fgDrawableRes = R.drawable.ic_app_icon_2_fg
        ),
        LauncherIconSpec(
            id = "I3", aliasClassName = "com.example.LauncherIcon3",
            nameAr = "٣ • عضوي مينت في أبيض", nameEn = "3 • Organic Mint-White",
            bgStart = Color(0xFFFFFFFF), bgEnd = Color(0xFFF8FAFC),
            fgDrawableRes = R.drawable.ic_app_icon_3_fg
        ),
        LauncherIconSpec(
            id = "I4", aliasClassName = "com.example.LauncherIcon4",
            nameAr = "٤ • مدور مينت وجرس", nameEn = "4 • Circle Mint Bell",
            bgStart = Color(0xFF18C28B), bgEnd = Color(0xFF10B981),
            fgDrawableRes = R.drawable.ic_app_icon_4_fg
        ),
        LauncherIconSpec(
            id = "I5", aliasClassName = "com.example.LauncherIcon5",
            nameAr = "٥ • عضوي لبني سماوي", nameEn = "5 • Organic Baby Blue",
            bgStart = Color(0xFFEFF8FF), bgEnd = Color(0xFFD8EEFA),
            fgDrawableRes = R.drawable.ic_app_icon_5_fg
        ),
        LauncherIconSpec(
            id = "I6", aliasClassName = "com.example.LauncherIcon6",
            nameAr = "٦ • مدور أبيض وتذكير", nameEn = "6 • Circle White Check",
            bgStart = Color(0xFFFFFFFF), bgEnd = Color(0xFFF1F5F9),
            fgDrawableRes = R.drawable.ic_app_icon_6_fg
        ),
        LauncherIconSpec(
            id = "I7", aliasClassName = "com.example.LauncherIcon7",
            nameAr = "٧ • عضوي بيج دافئ", nameEn = "7 • Organic Sand Beige",
            bgStart = Color(0xFFEADBC8), bgEnd = Color(0xFFDEC6A9),
            fgDrawableRes = R.drawable.ic_app_icon_7_fg
        ),
        LauncherIconSpec(
            id = "I8", aliasClassName = "com.example.LauncherIcon8",
            nameAr = "٨ • مينت في أبيض ناعم", nameEn = "8 • Soft Mint-Ice",
            bgStart = Color(0xFFE6FBF3), bgEnd = Color(0xFFD1F7E8),
            fgDrawableRes = R.drawable.ic_app_icon_8_fg
        ),
        LauncherIconSpec(
            id = "I9", aliasClassName = "com.example.LauncherIcon9",
            nameAr = "٩ • مدور لبني في أبيض", nameEn = "9 • Circle Blue-White",
            bgStart = Color(0xFFFFFFFF), bgEnd = Color(0xFFF8FAFC),
            fgDrawableRes = R.drawable.ic_app_icon_9_fg
        ),
        LauncherIconSpec(
            id = "I10", aliasClassName = "com.example.LauncherIcon10",
            nameAr = "١٠ • عضوي مينت جيم", nameEn = "10 • Organic Mint Gym",
            bgStart = Color(0xFF10B981), bgEnd = Color(0xFF18C28B),
            fgDrawableRes = R.drawable.ic_app_icon_10_fg
        ),
        LauncherIconSpec(
            id = "I11", aliasClassName = "com.example.LauncherIcon11",
            nameAr = "١١ • مدور بيج وساعة", nameEn = "11 • Circle Beige Clock",
            bgStart = Color(0xFFFFFDF9), bgEnd = Color(0xFFF5EBDD),
            fgDrawableRes = R.drawable.ic_app_icon_11_fg
        ),
        LauncherIconSpec(
            id = "I12", aliasClassName = "com.example.LauncherIcon12",
            nameAr = "١٢ • عضوي لبني ومينت", nameEn = "12 • Organic Mint-Sky",
            bgStart = Color(0xFFE0F2FE), bgEnd = Color(0xFFE6FBF3),
            fgDrawableRes = R.drawable.ic_app_icon_12_fg
        )
    )

    fun launcherIconById(id: String): LauncherIconSpec =
        launcherIcons.firstOrNull { it.id == id } ?: launcherIcons.first()

    val characterBadgeStates = listOf(
        CharacterBadgeStateSpec(
            id = "sleeping",
            nameAr = "نائم (الأصلي)",
            nameEn = "Sleeping (Original)",
            drawableRes = R.drawable.img_sleepy_badge_1795028747325
        ),
        CharacterBadgeStateSpec(
            id = "awake",
            nameAr = "مستيقظ",
            nameEn = "Awake",
            drawableRes = R.drawable.badge_state_awake_1795086987920
        ),
        CharacterBadgeStateSpec(
            id = "resting",
            nameAr = "مسترخي",
            nameEn = "Resting",
            drawableRes = R.drawable.badge_state_resting_1795086988544
        ),
        CharacterBadgeStateSpec(
            id = "active",
            nameAr = "نشيط",
            nameEn = "Active",
            drawableRes = R.drawable.badge_state_active_1795086988285
        ),
        CharacterBadgeStateSpec(
            id = "happy",
            nameAr = "سعيد",
            nameEn = "Happy",
            drawableRes = R.drawable.badge_state_happy_1795086989074
        ),
        CharacterBadgeStateSpec(
            id = "focused",
            nameAr = "مُركّز",
            nameEn = "Focused",
            drawableRes = R.drawable.badge_state_focused_1795087011566
        )
    )

    fun characterBadgeStateById(id: String): CharacterBadgeStateSpec =
        characterBadgeStates.firstOrNull { it.id == id } ?: characterBadgeStates.first()
}
