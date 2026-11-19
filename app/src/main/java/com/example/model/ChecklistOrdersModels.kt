package com.example.model

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable

/**
 * A single customizable order / checklist row on the blank Orders page.
 * [status]: "none" (neutral / unselected), "check" (completed checkmark), "x" (not completed / rejected X).
 */
@Serializable
data class ChecklistOrderItem(
    val id: String,
    val text: String,
    val status: String = STATUS_NONE,
    val sortOrder: Long = 0L
) {
    companion object {
        const val STATUS_NONE = "none"
        const val STATUS_CHECK = "check"
        const val STATUS_X = "x"

        fun nextCycledStatus(current: String): String = when (current) {
            STATUS_NONE -> STATUS_CHECK
            STATUS_CHECK -> STATUS_X
            else -> STATUS_NONE
        }
    }
}

/**
 * Full persistent customization config for the Orders checklist checkmark & X controls.
 */
@Serializable
data class ChecklistStyleConfig(
    val styleId: String = "pixar_3d",
    val shapeId: String = "circle",
    val circleSizeDp: Int = 34,
    val markSizeDp: Int = 18,
    val checkColorHex: String = "#10B981",
    val xColorHex: String = "#EF4444",
    val neutralColorHex: String = "#CBD5E1",
    val itemSpacingDp: Int = 10
)

data class ChecklistMarkStyleOption(
    val id: String,
    val nameAr: String,
    val nameEn: String
)

data class ChecklistShapeOption(
    val id: String,
    val nameAr: String,
    val nameEn: String
)

object ChecklistStyleCatalog {
    val styles: List<ChecklistMarkStyleOption> = listOf(
        ChecklistMarkStyleOption("pixar_3d", "ثلاثي الأبعاد Pixar لامع", "Glossy 3D Pixar"),
        ChecklistMarkStyleOption("pixar_mint", "ثلاثي الأبعاد مينت ناعم", "3D Pastel Mint"),
        ChecklistMarkStyleOption("pixar_gold", "ثلاثي الأبعاد ذهبي ووردي", "3D Gold & Rose"),
        ChecklistMarkStyleOption("bold_filled", "ممتلئ عصري قوي", "Bold Modern"),
        ChecklistMarkStyleOption("minimal", "بسيط وناعم", "Simple Minimal"),
        ChecklistMarkStyleOption("soft_pastel", "باستيل هادئ", "Soft Pastel"),
        ChecklistMarkStyleOption("outlined_ring", "حلقة مفرغة كلاسيك", "Outlined Ring"),
        ChecklistMarkStyleOption("neon_glow", "توهج نيون لامع", "Neon Glow"),
        ChecklistMarkStyleOption("stamp_classic", "ختم دائري مزدوج", "Double Stamp"),
        ChecklistMarkStyleOption("sketch_soft", "رسم يدوي لطيف", "Hand Sketch")
    )

    val shapes: List<ChecklistShapeOption> = listOf(
        ChecklistShapeOption("circle", "دائري", "Circle"),
        ChecklistShapeOption("squircle", "مربع ناعم", "Squircle"),
        ChecklistShapeOption("rounded_square", "مربع بحواف", "Rounded"),
        ChecklistShapeOption("organic_blob", "عضوي مرن", "Organic"),
        ChecklistShapeOption("soft_hex", "سداسي ناعم", "Hexagon"),
        ChecklistShapeOption("diamond", "معين ناعم", "Diamond")
    )

    val checkColorSwatches: List<String> = listOf(
        "#10B981", "#18C28B", "#059669", "#22C55E",
        "#0EA5E9", "#2563EB", "#8B5CF6", "#F59E0B"
    )

    val xColorSwatches: List<String> = listOf(
        "#EF4444", "#DC2626", "#F43F5E", "#E11D48",
        "#F97316", "#EA580C", "#EC4899", "#64748B"
    )

    val neutralColorSwatches: List<String> = listOf(
        "#CBD5E1", "#94A3B8", "#D1FAE5", "#E0F2FE",
        "#F1F5F9", "#E2E8F0", "#FDE68A", "#FECDD3"
    )
}

/**
 * Pure, deterministic optional text splitting helper for the blank Orders page.
 * Splitting is strictly opt-in: [MODE_WHOLE] keeps the entire text intact as a single item.
 */
object OrderTextSplitter {
    const val MODE_WHOLE = "whole"
    const val MODE_WORDS = "words"
    const val MODE_PAIRS = "pairs"
    const val MODE_COMMA_LINE = "comma_line"
    const val MODE_CUSTOM_N = "custom_n"
    const val MODE_CUSTOM_DELIM = "custom_delim"

    fun split(
        rawText: String,
        mode: String,
        wordsPerGroup: Int = 3,
        customDelimiter: String = ""
    ): List<String> {
        val cleaned = rawText.trim()
        if (cleaned.isEmpty()) return emptyList()

        return when (mode) {
            MODE_WHOLE -> listOf(cleaned.replace(Regex("\\s+"), " "))

            MODE_WORDS -> cleaned
                .split(Regex("[\\s,،;؛\\n]+"))
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            MODE_PAIRS -> {
                val words = cleaned
                    .split(Regex("[\\s,،;؛\\n]+"))
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                words.chunked(2).map { it.joinToString(" ") }
            }

            MODE_CUSTOM_N -> {
                val n = wordsPerGroup.coerceIn(1, 8)
                val words = cleaned
                    .split(Regex("[\\s,،;؛\\n]+"))
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                words.chunked(n).map { it.joinToString(" ") }
            }

            MODE_COMMA_LINE -> cleaned
                .split(Regex("[,،;؛\\n]+"))
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            MODE_CUSTOM_DELIM -> {
                val sep = customDelimiter.ifEmpty { "," }
                cleaned.split(sep)
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
            }

            else -> listOf(cleaned)
        }
    }
}

/**
 * Built-in seamless full-screen pastel wallpaper presets (in addition to user's own photo picker).
 * All gradients are continuous edge-to-edge with zero top strip or dividing line.
 */
data class MainWallpaperPreset(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val colors: List<Color>
)

object MainWallpaperCatalog {
    const val PREFIX_PRESET = "preset:"

    val presets: List<MainWallpaperPreset> = listOf(
        MainWallpaperPreset(
            id = "mint_mist",
            nameAr = "ضباب نعناعي ناعم",
            nameEn = "Mint Mist",
            colors = listOf(Color(0xFFE9FAF3), Color(0xFFF3FCF8), Color(0xFFFAFEFC))
        ),
        MainWallpaperPreset(
            id = "sky_silk",
            nameAr = "حرير سماوي هادئ",
            nameEn = "Sky Silk",
            colors = listOf(Color(0xFFEAF4FF), Color(0xFFF3F9FF), Color(0xFFFAFCFF))
        ),
        MainWallpaperPreset(
            id = "warm_cream",
            nameAr = "كريمي دافئ ناعم",
            nameEn = "Warm Cream",
            colors = listOf(Color(0xFFFAF3E8), Color(0xFFFDF8F1), Color(0xFFFFFDFA))
        ),
        MainWallpaperPreset(
            id = "lavender_cloud",
            nameAr = "سحابة لافندر",
            nameEn = "Lavender Cloud",
            colors = listOf(Color(0xFFF3EEFF), Color(0xFFF8F5FF), Color(0xFFFCFBFF))
        ),
        MainWallpaperPreset(
            id = "peach_breeze",
            nameAr = "نسيم خوخي لطيف",
            nameEn = "Peach Breeze",
            colors = listOf(Color(0xFFFFF0E8), Color(0xFFFFF6F1), Color(0xFFFFFCFA))
        ),
        MainWallpaperPreset(
            id = "ocean_pastel",
            nameAr = "أكوا ومينت متصل",
            nameEn = "Aqua Pastel",
            colors = listOf(Color(0xFFE4F8FA), Color(0xFFEAFBF5), Color(0xFFF7FCFA))
        ),
        MainWallpaperPreset(
            id = "sage_meadow",
            nameAr = "مريمية صافية",
            nameEn = "Sage Meadow",
            colors = listOf(Color(0xFFEAF3EC), Color(0xFFF3F8F4), Color(0xFFFAFCFA))
        ),
        MainWallpaperPreset(
            id = "pure_white",
            nameAr = "أبيض نقي متصل",
            nameEn = "Pure White",
            colors = listOf(Color(0xFFFFFFFF), Color(0xFFFBFCFE), Color(0xFFF8FAFC))
        )
    )

    fun presetForUri(uri: String): MainWallpaperPreset? {
        if (!uri.startsWith(PREFIX_PRESET)) return null
        val id = uri.removePrefix(PREFIX_PRESET)
        return presets.firstOrNull { it.id == id }
    }
}
