package com.example.model

import androidx.annotation.Keep
import androidx.compose.ui.graphics.Color
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
    val soundNav: Boolean = true,
    val soundEnter: Boolean = true,
    val soundExit: Boolean = true,
    val soundDone: Boolean = true,
    val swipePages: Boolean = true,
    val swipeClose: Boolean = true,
    val hapticEnabled: Boolean = true,
    val activeRingId: String = "G1",
    val autoBackup: Boolean = true,
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
    val soundNav: Boolean = true,
    val soundEnter: Boolean = true,
    val soundExit: Boolean = true,
    val soundDone: Boolean = true,
    val swipePages: Boolean = true,
    val swipeClose: Boolean = true,
    val hapticEnabled: Boolean = true,
    val activeRingId: String = "G1",
    val autoBackup: Boolean = true
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

object MNotesCatalog {
    val themes = listOf(
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
        StyleOption("target", "هدف", "Target")
    )

    val fontOptions = listOf(
        StyleOption("cairo", "Cairo", "Cairo"),
        StyleOption("tajawal", "Tajawal", "Tajawal"),
        StyleOption("ibm_plex", "IBM Plex", "IBM Plex"),
        StyleOption("almarai", "Almarai", "Almarai")
    )

    val gymTracks = listOf(
        GymTrack("G1", "نبض الجيم السريع (Bass Beat)", "Gym Bass Beat (128 BPM)"),
        GymTrack("G2", "طاقة الأبطال (تصاعد حماسي)", "Champion Synth Rise"),
        GymTrack("G3", "جرس حلبة الملاكمة (انطلاق)", "Boxing Round Bell"),
        GymTrack("G4", "روك الطاقة الإلكتروني", "Electric Energy Riff"),
        GymTrack("G5", "عد تنازلي ثم انطلاق!", "Countdown & Go!"),
        GymTrack("G6", "موسيقى الفوز والإنجاز", "Victory Fanfare"),
        GymTrack("G7", "تكنو الجري والتمرين", "Cardio Techno Pulse"),
        GymTrack("G8", "رنة كرستال رايقة", "Crystal Focus Chime")
    )
}
