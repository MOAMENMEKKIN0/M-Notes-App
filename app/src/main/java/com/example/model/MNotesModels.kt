package com.example.model

import androidx.annotation.Keep
import androidx.compose.ui.graphics.Color
import com.example.R
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.serialization.Serializable

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
    val sortOrder: Long = 0L,
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
}

@Keep
@IgnoreExtraProperties
data class QuickRequest(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val body: String = "",
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
    val bannerPhotoShape: String = "ring",
    val taskImgSize: String = "medium",
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
    val sortOrder: Long = 0L
)

@Serializable
data class BackupRequestDto(
    val title: String,
    val body: String = ""
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
    val bannerPhotoShape: String = "ring",
    val taskImgSize: String = "medium",
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
    val appIconId: String = "I1"
)

@Serializable
data class BackupSnapshotPayload(
    val version: String = "1.0",
    val savedAt: String = "",
    val preferences: BackupPreferencesDto = BackupPreferencesDto(),
    val tasks: List<BackupTaskDto> = emptyList(),
    val requests: List<BackupRequestDto> = emptyList()
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

data class BuiltInPhotoSpec(
    val uri: String,
    val nameAr: String,
    val nameEn: String
)

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

    val pixel3dPhotos = listOf(
        BuiltInPhotoSpec("res://img_3d_elder_beige", "رجل كبير - بيج", "Elder - Beige"),
        BuiltInPhotoSpec("res://img_3d_elder_man", "رجل كبير - فاتح", "Elder - Light"),
        BuiltInPhotoSpec("res://img_3d_bald_brown", "شاب أقرع - بني", "Bald - Brown"),
        BuiltInPhotoSpec("res://img_3d_bald_man", "شاب أقرع - فاتح", "Bald - Light"),
        BuiltInPhotoSpec("res://img_3d_haired_mint", "شاب بشعر - نعناعي", "Man - Mint"),
        BuiltInPhotoSpec("res://img_3d_haired_man", "شاب بشعر - فاتح", "Man - Light"),
        BuiltInPhotoSpec("res://img_3d_child_cream", "طفلة - كريمي", "Child - Cream"),
        BuiltInPhotoSpec("res://img_3d_child", "طفل - فاتح", "Child - Light"),
        BuiltInPhotoSpec("res://img_3d_woman_beige", "امرأة - بيج", "Woman - Beige"),
        BuiltInPhotoSpec("res://img_3d_woman", "امرأة - فاتح", "Woman - Light"),
        BuiltInPhotoSpec("res://img_3d_medieval_bald", "القرون الوسطى - أصلع", "Medieval Bald"),
        BuiltInPhotoSpec("res://img_3d_jahiliyyah", "أيام الجاهلية - صحراء", "Ancient Arabian"),
        BuiltInPhotoSpec("res://img_3d_ancient_fruits", "فواكه زمان في أواني", "Heritage Fruits"),
        BuiltInPhotoSpec("res://img_3d_ancient_bread_grain", "خبز زمان وقمح وشعير", "Bread & Grains"),
        BuiltInPhotoSpec("res://img_3d_gym_chest", "جيم - صدر (بيج)", "Gym - Chest"),
        BuiltInPhotoSpec("res://img_3d_gym_back", "جيم - ظهر (بني)", "Gym - Back"),
        BuiltInPhotoSpec("res://img_3d_gym_legs", "جيم - رجل (سماوي)", "Gym - Legs"),
        BuiltInPhotoSpec("res://img_3d_gym_legpress", "جيم - جهاز الرجل", "Gym - Leg Press"),
        BuiltInPhotoSpec("res://img_3d_gym_lunges", "جيم - رجل بالدمبل", "Gym - Lunges"),
        BuiltInPhotoSpec("res://img_3d_gym_arms", "جيم - ذراع وبار", "Gym - Arms"),
        BuiltInPhotoSpec("res://img_3d_gym_shoulders", "جيم - أكتاف (بيج)", "Gym - Shoulders"),
        BuiltInPhotoSpec("res://img_3d_gym_workout", "جيم - دمبل (فاتح)", "Gym - Dumbbells")
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
}
