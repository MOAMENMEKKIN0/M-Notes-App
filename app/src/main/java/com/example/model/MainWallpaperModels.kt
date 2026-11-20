package com.example.model

import androidx.compose.ui.graphics.Color

enum class WallpaperPatternType {
    NONE,
    WHATSAPP_DOODLE,
    WHATSAPP_DARK_DOODLE,
    BOTANICAL_LEAVES,
    SOFT_GEOMETRIC,
    LINEN_WEAVE
}

/**
 * Built-in seamless full-screen wallpaper presets (both WhatsApp chat-style patterned
 * wallpapers and soft pastel gradients), in addition to the user's own device photo picker.
 * All wallpapers render edge-to-edge with zero dividing line or separate top strip.
 */
data class MainWallpaperPreset(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val colors: List<Color>,
    val category: String = "soft",
    val pattern: WallpaperPatternType = WallpaperPatternType.NONE,
    val patternTint: Color = Color.Transparent,
    val patternAlpha: Float = 0.09f,
    val isDark: Boolean = false
)

object MainWallpaperCatalog {
    const val PREFIX_PRESET = "preset:"
    const val CATEGORY_WHATSAPP = "whatsapp"
    const val CATEGORY_SOFT = "soft"

    val whatsappPresets: List<MainWallpaperPreset> = listOf(
        MainWallpaperPreset(
            id = "wa_classic_sand",
            nameAr = "واتساب كلاسيك دافئ",
            nameEn = "Classic Chat Sand",
            colors = listOf(Color(0xFFEFEAE2), Color(0xFFEBE4D9), Color(0xFFE7DFD3)),
            category = CATEGORY_WHATSAPP,
            pattern = WallpaperPatternType.WHATSAPP_DOODLE,
            patternTint = Color(0xFF7B6F5D),
            patternAlpha = 0.13f
        ),
        MainWallpaperPreset(
            id = "wa_mint_chat",
            nameAr = "واتساب نعناعي ناعم",
            nameEn = "Mint Chat Doodle",
            colors = listOf(Color(0xFFE2F6EE), Color(0xFFD9F2E8), Color(0xFFCEF0E2)),
            category = CATEGORY_WHATSAPP,
            pattern = WallpaperPatternType.WHATSAPP_DOODLE,
            patternTint = Color(0xFF0F766E),
            patternAlpha = 0.12f
        ),
        MainWallpaperPreset(
            id = "wa_dark_obsidian",
            nameAr = "واتساب داكن أصلي",
            nameEn = "Dark Chat Obsidian",
            colors = listOf(Color(0xFF0B141A), Color(0xFF101C24), Color(0xFF13222B)),
            category = CATEGORY_WHATSAPP,
            pattern = WallpaperPatternType.WHATSAPP_DARK_DOODLE,
            patternTint = Color(0xFF94A3B8),
            patternAlpha = 0.12f,
            isDark = true
        ),
        MainWallpaperPreset(
            id = "wa_emerald_night",
            nameAr = "ليل زمردي واتساب",
            nameEn = "Emerald Night Chat",
            colors = listOf(Color(0xFF0B2220), Color(0xFF102E2A), Color(0xFF133832)),
            category = CATEGORY_WHATSAPP,
            pattern = WallpaperPatternType.WHATSAPP_DARK_DOODLE,
            patternTint = Color(0xFF5EEAD4),
            patternAlpha = 0.11f,
            isDark = true
        ),
        MainWallpaperPreset(
            id = "wa_sky_chat",
            nameAr = "واتساب سماوي هادئ",
            nameEn = "Sky Blue Chat",
            colors = listOf(Color(0xFFE3F2FD), Color(0xFFD8ECFC), Color(0xFFCDE6FA)),
            category = CATEGORY_WHATSAPP,
            pattern = WallpaperPatternType.WHATSAPP_DOODLE,
            patternTint = Color(0xFF1D4ED8),
            patternAlpha = 0.10f
        ),
        MainWallpaperPreset(
            id = "wa_rose_chat",
            nameAr = "واتساب وردي لطيف",
            nameEn = "Blush Rose Chat",
            colors = listOf(Color(0xFFFCECF0), Color(0xFFF9E2E8), Color(0xFFF6D8E0)),
            category = CATEGORY_WHATSAPP,
            pattern = WallpaperPatternType.WHATSAPP_DOODLE,
            patternTint = Color(0xFFBE185D),
            patternAlpha = 0.10f
        ),
        MainWallpaperPreset(
            id = "wa_lavender_chat",
            nameAr = "واتساب لافندر ناعم",
            nameEn = "Lavender Chat",
            colors = listOf(Color(0xFFF1ECFF), Color(0xFFE9E0FF), Color(0xFFE1D6FC)),
            category = CATEGORY_WHATSAPP,
            pattern = WallpaperPatternType.WHATSAPP_DOODLE,
            patternTint = Color(0xFF6D28D9),
            patternAlpha = 0.10f
        ),
        MainWallpaperPreset(
            id = "wa_botanical_sage",
            nameAr = "أوراق نباتية هادئة",
            nameEn = "Botanical Sage Chat",
            colors = listOf(Color(0xFFE5F1E8), Color(0xFFDCECE0), Color(0xFFD2E6D7)),
            category = CATEGORY_WHATSAPP,
            pattern = WallpaperPatternType.BOTANICAL_LEAVES,
            patternTint = Color(0xFF166534),
            patternAlpha = 0.11f
        ),
        MainWallpaperPreset(
            id = "wa_midnight_geo",
            nameAr = "هندسي ليلي أنيق",
            nameEn = "Midnight Geometric",
            colors = listOf(Color(0xFF121826), Color(0xFF182235), Color(0xFF1D2940)),
            category = CATEGORY_WHATSAPP,
            pattern = WallpaperPatternType.SOFT_GEOMETRIC,
            patternTint = Color(0xFF93C5FD),
            patternAlpha = 0.10f,
            isDark = true
        ),
        MainWallpaperPreset(
            id = "wa_warm_linen",
            nameAr = "قماش كتان دافئ",
            nameEn = "Warm Linen Texture",
            colors = listOf(Color(0xFFF7F2EA), Color(0xFFF2EADF), Color(0xFFECE2D4)),
            category = CATEGORY_WHATSAPP,
            pattern = WallpaperPatternType.LINEN_WEAVE,
            patternTint = Color(0xFF927B63),
            patternAlpha = 0.11f
        )
    )

    val softPresets: List<MainWallpaperPreset> = listOf(
        MainWallpaperPreset(
            id = "mint_mist",
            nameAr = "ضباب نعناعي ناعم",
            nameEn = "Mint Mist",
            colors = listOf(Color(0xFFE9FAF3), Color(0xFFF3FCF8), Color(0xFFFAFEFC)),
            category = CATEGORY_SOFT
        ),
        MainWallpaperPreset(
            id = "sky_silk",
            nameAr = "حرير سماوي هادئ",
            nameEn = "Sky Silk",
            colors = listOf(Color(0xFFEAF4FF), Color(0xFFF3F9FF), Color(0xFFFAFCFF)),
            category = CATEGORY_SOFT
        ),
        MainWallpaperPreset(
            id = "warm_cream",
            nameAr = "كريمي دافئ ناعم",
            nameEn = "Warm Cream",
            colors = listOf(Color(0xFFFAF3E8), Color(0xFFFDF8F1), Color(0xFFFFFDFA)),
            category = CATEGORY_SOFT
        ),
        MainWallpaperPreset(
            id = "lavender_cloud",
            nameAr = "سحابة لافندر",
            nameEn = "Lavender Cloud",
            colors = listOf(Color(0xFFF3EEFF), Color(0xFFF8F5FF), Color(0xFFFCFBFF)),
            category = CATEGORY_SOFT
        ),
        MainWallpaperPreset(
            id = "peach_breeze",
            nameAr = "نسيم خوخي لطيف",
            nameEn = "Peach Breeze",
            colors = listOf(Color(0xFFFFF0E8), Color(0xFFFFF6F1), Color(0xFFFFFCFA)),
            category = CATEGORY_SOFT
        ),
        MainWallpaperPreset(
            id = "ocean_pastel",
            nameAr = "أكوا ومينت متصل",
            nameEn = "Aqua Pastel",
            colors = listOf(Color(0xFFE4F8FA), Color(0xFFEAFBF5), Color(0xFFF7FCFA)),
            category = CATEGORY_SOFT
        ),
        MainWallpaperPreset(
            id = "sage_meadow",
            nameAr = "مريمية صافية",
            nameEn = "Sage Meadow",
            colors = listOf(Color(0xFFEAF3EC), Color(0xFFF3F8F4), Color(0xFFFAFCFA)),
            category = CATEGORY_SOFT
        ),
        MainWallpaperPreset(
            id = "pure_white",
            nameAr = "أبيض نقي متصل",
            nameEn = "Pure White",
            colors = listOf(Color(0xFFFFFFFF), Color(0xFFFBFCFE), Color(0xFFF8FAFC)),
            category = CATEGORY_SOFT
        )
    )

    val presets: List<MainWallpaperPreset> = whatsappPresets + softPresets

    fun presetForUri(uri: String): MainWallpaperPreset? {
        if (!uri.startsWith(PREFIX_PRESET)) return null
        val id = uri.removePrefix(PREFIX_PRESET)
        return presets.firstOrNull { it.id == id }
    }
}
