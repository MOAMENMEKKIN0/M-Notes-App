package com.example.model

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable

/**
 * Full serializable customization blueprint for the Main Screen.
 * Supports permanent presets (Classic, Options A, B, C, D) and unlimited user-saved variations.
 */
@Serializable
data class ScreenDesignConfig(
    val basePresetId: String = "classic_default",
    // Layout & Element Arrangement
    val layoutMode: String = "timeline_list", // "timeline_list" | "gallery_2col" | "spotlight_agenda" | "bento_grouped" | "compact_list"
    val sectionOrder: List<String> = listOf(
        "greeting",
        "week_strip",
        "progress",
        "spotlight",
        "filters",
        "search",
        "tasks"
    ),
    val taskArrangement: String = "row_start", // "row_start" | "centered_card" | "row_end" | "compact_inline"
    // Shapes & Sizing
    val cardShape: String = "rounded", // "rounded" | "pill" | "organic_blob" | "squircle" | "cut_corner" | "circle_bubble" | "frameless"
    val cardCornerRadiusDp: Int = 22,
    val taskImageShape: String = "ring", // "circle" | "ring" | "organic_blob" | "squircle" | "pebble" | "soft_hex" | "ios"
    val taskImageSizeDp: Int = 56,
    val badgeSizeScalePercent: Int = 100,
    val cardElevationDp: Int = 3,
    val cardBorderWidthDp: Float = 1.2f,
    val itemSpacingDp: Int = 10,
    val cardPaddingDp: Int = 12,
    val textScalePercent: Int = 100,
    // Colors, Backgrounds & Decorative Elements
    val paletteId: String = "alabaster_mint", // "app_theme" | "alabaster_mint" | "sky_pastel" | "warm_cream" | "lavender_soft" | "peach_blush" | "sage_zen" | "coral_sun" | "custom"
    val bgGradientStartHex: String = "#F6FBF9",
    val bgGradientEndHex: String = "#EBF7F2",
    val cardSurfaceHex: String = "#FFFFFF",
    val accentHex: String = "#18C28B",
    val secondaryAccentHex: String = "#38BDF8",
    val decorationStyle: String = "soft_orbs", // "none" | "soft_orbs" | "pastel_waves" | "doodle_dots" | "floating_sparkles"
    // Component Styles
    val progressStyle: String = "ring_card", // "ring_card" | "bento_tiles" | "linear_bar" | "mini_pills" | "hidden"
    val filterStyle: String = "pastel_pills", // "pastel_pills" | "outlined_chips" | "segmented" | "minimal_underline"
    val addButtonStyle: String = "capsule_bar", // "capsule_bar" | "floating_fab" | "extended_pill" | "soft_squircle" | "hidden"
    // Granular Visibility Controls (Add / Remove / Show / Hide)
    val showGreetingHeader: Boolean = true,
    val showWeekStrip: Boolean = true,
    val showProgressWidget: Boolean = true,
    val showSpotlightUpNext: Boolean = false,
    val showFilterChips: Boolean = true,
    val showSearchBar: Boolean = false,
    val showTaskFloatingBadges: Boolean = true,
    val showTaskTimePill: Boolean = true,
    val showTaskStatusBadges: Boolean = true,
    val showRestingGroupSeparate: Boolean = false,
    val showQuickActionsOnCard: Boolean = true,
    val showBottomNavBar: Boolean = true,
    val showDecorations: Boolean = true
)

@Serializable
data class SavedScreenDesignVersion(
    val id: String,
    val name: String,
    val basePresetId: String,
    val config: ScreenDesignConfig,
    val createdAtMs: Long = 0L,
    val updatedAtMs: Long = 0L
)

/**
 * Persisted state for screen design themes.
 * IMPORTANT: Defaults to "classic_default" with activeConfig = null so the user's
 * current main screen remains completely unchanged until they explicitly press Apply.
 */
@Serializable
data class ScreenDesignStoreState(
    val activeDesignId: String = ScreenDesignPresets.ID_CLASSIC,
    val activeDesignLabel: String = "",
    val activeConfig: ScreenDesignConfig? = null,
    val savedVersions: List<SavedScreenDesignVersion> = emptyList()
)

data class ScreenDesignPresetSpec(
    val id: String,
    val badgeCode: String,
    val nameEn: String,
    val nameAr: String,
    val subtitleEn: String,
    val subtitleAr: String,
    val highlightsEn: List<String>,
    val highlightsAr: List<String>,
    val config: ScreenDesignConfig,
    val isClassicDefault: Boolean = false
)

data class ScreenPaletteSpec(
    val id: String,
    val nameEn: String,
    val nameAr: String,
    val bgStartHex: String,
    val bgEndHex: String,
    val cardHex: String,
    val accentHex: String,
    val secondaryHex: String
)

object ScreenDesignPresets {
    const val ID_CLASSIC = "classic_default"
    const val ID_OPTION_A = "option_a"
    const val ID_OPTION_B = "option_b"
    const val ID_OPTION_C = "option_c"
    const val ID_OPTION_D = "option_d"

    val defaultSectionOrder = listOf(
        "greeting",
        "week_strip",
        "progress",
        "spotlight",
        "filters",
        "search",
        "tasks"
    )

    val palettes: List<ScreenPaletteSpec> = listOf(
        ScreenPaletteSpec(
            id = "app_theme",
            nameEn = "Current App Theme",
            nameAr = "لون التطبيق الحالي",
            bgStartHex = "#F3FBF8",
            bgEndHex = "#EBF7F2",
            cardHex = "#FFFFFF",
            accentHex = "#18C28B",
            secondaryHex = "#38BDF8"
        ),
        ScreenPaletteSpec(
            id = "alabaster_mint",
            nameEn = "Alabaster Mint",
            nameAr = "أبيض نعناعي هادئ",
            bgStartHex = "#F4FCF8",
            bgEndHex = "#E9F8F1",
            cardHex = "#FFFFFF",
            accentHex = "#10B981",
            secondaryHex = "#38BDF8"
        ),
        ScreenPaletteSpec(
            id = "sky_pastel",
            nameEn = "Soft Sky Cloud",
            nameAr = "سماء لبني ناعمة",
            bgStartHex = "#EEF7FF",
            bgEndHex = "#E4F2FE",
            cardHex = "#FFFFFF",
            accentHex = "#0EA5E9",
            secondaryHex = "#6366F1"
        ),
        ScreenPaletteSpec(
            id = "warm_cream",
            nameEn = "Warm Bento Cream",
            nameAr = "كريمي دافئ",
            bgStartHex = "#FDF8F0",
            bgEndHex = "#F7EFE2",
            cardHex = "#FFFDF9",
            accentHex = "#E67E22",
            secondaryHex = "#18C28B"
        ),
        ScreenPaletteSpec(
            id = "aqua_breeze",
            nameEn = "Aqua Cyan Breeze",
            nameAr = "أكوا سماوي منعش",
            bgStartHex = "#ECFEFE",
            bgEndHex = "#E0F9FA",
            cardHex = "#FFFFFF",
            accentHex = "#06B6D4",
            secondaryHex = "#10B981"
        ),
        ScreenPaletteSpec(
            id = "lavender_soft",
            nameEn = "Lavender Mist",
            nameAr = "لافندر ناعم",
            bgStartHex = "#F6F3FF",
            bgEndHex = "#EDE8FF",
            cardHex = "#FFFFFF",
            accentHex = "#8B5CF6",
            secondaryHex = "#EC4899"
        ),
        ScreenPaletteSpec(
            id = "peach_blush",
            nameEn = "Soft Peach Blush",
            nameAr = "خوخي باستيل",
            bgStartHex = "#FFF4EE",
            bgEndHex = "#FDEBE2",
            cardHex = "#FFFFFF",
            accentHex = "#F97316",
            secondaryHex = "#F43F5E"
        ),
        ScreenPaletteSpec(
            id = "sage_zen",
            nameEn = "Sage Botanical",
            nameAr = "أخضر سيج هادئ",
            bgStartHex = "#F2F7F3",
            bgEndHex = "#E6F0E8",
            cardHex = "#FCFDFC",
            accentHex = "#2E8B57",
            secondaryHex = "#84CC16"
        )
    )

    val classicDefault = ScreenDesignPresetSpec(
        id = ID_CLASSIC,
        badgeCode = "0",
        nameEn = "Current Main Screen",
        nameAr = "الواجهة الحالية الأصلية",
        subtitleEn = "Your existing unchanged M Notes layout",
        subtitleAr = "تصميمك الحالي بدون أي تغيير",
        highlightsEn = listOf("Unchanged default", "All existing panels"),
        highlightsAr = listOf("الوضع الافتراضي الحالي", "نفس الواجهة الأصلية"),
        config = ScreenDesignConfig(
            basePresetId = ID_CLASSIC,
            layoutMode = "timeline_list",
            paletteId = "app_theme"
        ),
        isClassicDefault = true
    )

    val optionA = ScreenDesignPresetSpec(
        id = ID_OPTION_A,
        badgeCode = "A",
        nameEn = "Option A — Calm Focus Timeline",
        nameAr = "خيار A — جدول التركيز الهادئ",
        subtitleEn = "Ultra-clean alabaster agenda with generous spacing, large Pixar circles & floating capsule bar",
        subtitleAr = "قائمة رأسية فائقة النظافة ببطاقات ناعمة ودوائر كبيرة وشريط كبسولة سفلي عائم",
        highlightsEn = listOf("Clean vertical agenda", "Compact week strip", "Floating bottom capsule"),
        highlightsAr = listOf("جدول يومي هادئ", "شريط أيام الأسبوع", "شريط كبسولة سفلي عائم"),
        config = ScreenDesignConfig(
            basePresetId = ID_OPTION_A,
            layoutMode = "timeline_list",
            sectionOrder = listOf("greeting", "week_strip", "progress", "filters", "tasks", "search", "spotlight"),
            taskArrangement = "row_start",
            cardShape = "rounded",
            cardCornerRadiusDp = 24,
            taskImageShape = "ring",
            taskImageSizeDp = 58,
            badgeSizeScalePercent = 100,
            cardElevationDp = 3,
            cardBorderWidthDp = 1.0f,
            itemSpacingDp = 12,
            cardPaddingDp = 14,
            textScalePercent = 102,
            paletteId = "alabaster_mint",
            bgGradientStartHex = "#F5FBF8",
            bgGradientEndHex = "#EBF6F1",
            cardSurfaceHex = "#FFFFFF",
            accentHex = "#10B981",
            secondaryAccentHex = "#38BDF8",
            decorationStyle = "soft_orbs",
            progressStyle = "linear_bar",
            filterStyle = "pastel_pills",
            addButtonStyle = "capsule_bar",
            showGreetingHeader = true,
            showWeekStrip = true,
            showProgressWidget = true,
            showSpotlightUpNext = false,
            showFilterChips = true,
            showSearchBar = false,
            showTaskFloatingBadges = true,
            showTaskTimePill = true,
            showTaskStatusBadges = true,
            showRestingGroupSeparate = false,
            showQuickActionsOnCard = true,
            showBottomNavBar = true,
            showDecorations = true
        )
    )

    val optionB = ScreenDesignPresetSpec(
        id = ID_OPTION_B,
        badgeCode = "B",
        nameEn = "Option B — Visual 2-Column Gallery",
        nameAr = "خيار B — معرض البطاقات المصوّر (عمودين)",
        subtitleEn = "Playful image-first 2-column Bento gallery showcasing Pixar task portraits & category pills",
        subtitleAr = "شبكة عمودين تبرز صور التمارين والمهام بشكل كبير ومرح مع فلاتر سريعة",
        highlightsEn = listOf("2-column photo gallery", "Large centered portraits", "Category filter pills"),
        highlightsAr = listOf("شبكة صور بعمودين", "صور كبيرة بمنتصف البطاقة", "أزرار تصنيف سريعة"),
        config = ScreenDesignConfig(
            basePresetId = ID_OPTION_B,
            layoutMode = "gallery_2col",
            sectionOrder = listOf("greeting", "filters", "progress", "tasks", "week_strip", "search", "spotlight"),
            taskArrangement = "centered_card",
            cardShape = "squircle",
            cardCornerRadiusDp = 26,
            taskImageShape = "circle",
            taskImageSizeDp = 76,
            badgeSizeScalePercent = 105,
            cardElevationDp = 4,
            cardBorderWidthDp = 1.2f,
            itemSpacingDp = 12,
            cardPaddingDp = 14,
            textScalePercent = 100,
            paletteId = "sky_pastel",
            bgGradientStartHex = "#F0F8FF",
            bgGradientEndHex = "#E6F4FE",
            cardSurfaceHex = "#FFFFFF",
            accentHex = "#0EA5E9",
            secondaryAccentHex = "#10B981",
            decorationStyle = "pastel_waves",
            progressStyle = "mini_pills",
            filterStyle = "pastel_pills",
            addButtonStyle = "floating_fab",
            showGreetingHeader = true,
            showWeekStrip = false,
            showProgressWidget = true,
            showSpotlightUpNext = false,
            showFilterChips = true,
            showSearchBar = false,
            showTaskFloatingBadges = true,
            showTaskTimePill = true,
            showTaskStatusBadges = true,
            showRestingGroupSeparate = false,
            showQuickActionsOnCard = true,
            showBottomNavBar = false,
            showDecorations = true
        )
    )

    val optionC = ScreenDesignPresetSpec(
        id = ID_OPTION_C,
        badgeCode = "C",
        nameEn = "Option C — Hero Up Next Spotlight",
        nameAr = "خيار C — بطاقة المهمة القادمة البارزة",
        subtitleEn = "Editorial coach layout with a featured Up Next Spotlight card, countdown & tidy agenda below",
        subtitleAr = "بطاقة كبيرة للمهمة القادمة في الأعلى مع عدّاد وزر تنفيذ سريع تليها قائمة منظمة",
        highlightsEn = listOf("Hero Up Next spotlight", "Quick alert & edit CTA", "Clean agenda below"),
        highlightsAr = listOf("بطاقة بارزة للمهمة القادمة", "توقيت وتنفيذ سريع", "قائمة مهام مرتبة أسفلها"),
        config = ScreenDesignConfig(
            basePresetId = ID_OPTION_C,
            layoutMode = "spotlight_agenda",
            sectionOrder = listOf("greeting", "spotlight", "filters", "tasks", "progress", "week_strip", "search"),
            taskArrangement = "row_start",
            cardShape = "rounded",
            cardCornerRadiusDp = 22,
            taskImageShape = "ring",
            taskImageSizeDp = 54,
            badgeSizeScalePercent = 100,
            cardElevationDp = 4,
            cardBorderWidthDp = 1.2f,
            itemSpacingDp = 10,
            cardPaddingDp = 13,
            textScalePercent = 100,
            paletteId = "aqua_breeze",
            bgGradientStartHex = "#EDFEFE",
            bgGradientEndHex = "#E4F7F9",
            cardSurfaceHex = "#FFFFFF",
            accentHex = "#06B6D4",
            secondaryAccentHex = "#10B981",
            decorationStyle = "floating_sparkles",
            progressStyle = "mini_pills",
            filterStyle = "segmented",
            addButtonStyle = "extended_pill",
            showGreetingHeader = true,
            showWeekStrip = false,
            showProgressWidget = false,
            showSpotlightUpNext = true,
            showFilterChips = true,
            showSearchBar = false,
            showTaskFloatingBadges = true,
            showTaskTimePill = true,
            showTaskStatusBadges = true,
            showRestingGroupSeparate = false,
            showQuickActionsOnCard = true,
            showBottomNavBar = true,
            showDecorations = true
        )
    )

    val optionD = ScreenDesignPresetSpec(
        id = ID_OPTION_D,
        badgeCode = "D",
        nameEn = "Option D — Warm Bento Dashboard",
        nameAr = "خيار D — لوحة البينتو الدافئة والمجموعات",
        subtitleEn = "Warm cream dashboard pairing Today Progress & Streak Bento tiles with Active vs Resting (Zzz) groups",
        subtitleAr = "تصميم كريمي دافئ يجمع بطاقات الإنجاز والاستمرارية في الأعلى مع فصل المهام النشطة عن المهام النائمة",
        highlightsEn = listOf("Progress & streak Bento", "Active vs Resting (Zzz) groups", "Warm tactile cream palette"),
        highlightsAr = listOf("بطاقات الإنجاز والاستمرارية", "فصل النشط عن النائم Zzz", "ألوان كريمية دافئة مريحة"),
        config = ScreenDesignConfig(
            basePresetId = ID_OPTION_D,
            layoutMode = "bento_grouped",
            sectionOrder = listOf("greeting", "progress", "week_strip", "filters", "tasks", "spotlight", "search"),
            taskArrangement = "row_start",
            cardShape = "rounded",
            cardCornerRadiusDp = 24,
            taskImageShape = "squircle",
            taskImageSizeDp = 56,
            badgeSizeScalePercent = 105,
            cardElevationDp = 3,
            cardBorderWidthDp = 1.2f,
            itemSpacingDp = 10,
            cardPaddingDp = 13,
            textScalePercent = 100,
            paletteId = "warm_cream",
            bgGradientStartHex = "#FDF8F0",
            bgGradientEndHex = "#F7EFE2",
            cardSurfaceHex = "#FFFDF9",
            accentHex = "#E67E22",
            secondaryAccentHex = "#18C28B",
            decorationStyle = "doodle_dots",
            progressStyle = "bento_tiles",
            filterStyle = "outlined_chips",
            addButtonStyle = "capsule_bar",
            showGreetingHeader = true,
            showWeekStrip = true,
            showProgressWidget = true,
            showSpotlightUpNext = false,
            showFilterChips = true,
            showSearchBar = false,
            showTaskFloatingBadges = true,
            showTaskTimePill = true,
            showTaskStatusBadges = true,
            showRestingGroupSeparate = true,
            showQuickActionsOnCard = true,
            showBottomNavBar = true,
            showDecorations = true
        )
    )

    /** Permanent immutable list of all 4 approved concept presets (A, B, C, D). */
    val fourApprovedPresets: List<ScreenDesignPresetSpec> = listOf(optionA, optionB, optionC, optionD)

    /** All built-in presets including Classic Original. */
    val allPresets: List<ScreenDesignPresetSpec> = listOf(classicDefault, optionA, optionB, optionC, optionD)

    fun presetById(id: String): ScreenDesignPresetSpec =
        allPresets.firstOrNull { it.id == id } ?: classicDefault

    fun paletteById(id: String): ScreenPaletteSpec =
        palettes.firstOrNull { it.id == id } ?: palettes.first()

    fun parseHexColor(hex: String, fallback: Color): Color {
        val clean = hex.trim().removePrefix("#")
        return runCatching {
            when (clean.length) {
                6 -> Color(android.graphics.Color.parseColor("#$clean"))
                8 -> Color(android.graphics.Color.parseColor("#$clean"))
                else -> fallback
            }
        }.getOrDefault(fallback)
    }
}
