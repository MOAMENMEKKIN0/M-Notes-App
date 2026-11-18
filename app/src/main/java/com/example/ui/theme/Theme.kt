package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AppThemeSpec
import com.example.model.MNotesCatalog

val LocalMNotesColors = staticCompositionLocalOf { MNotesCatalog.themes.first() }
val LocalMNotesWeight = staticCompositionLocalOf { FontWeight.SemiBold }
val LocalMNotesRefreshHz = staticCompositionLocalOf { 120 }
val LocalMNotesTextScale = staticCompositionLocalOf { 1.0f }
val LocalMNotesCardScale = staticCompositionLocalOf { 1.0f }

fun motionStiffnessForHz(hz: Int): Float = when (hz) {
    120 -> 1500f
    90 -> 950f
    else -> 520f
}

fun motionDurationMsForHz(hz: Int): Int = when (hz) {
    120 -> 110
    90 -> 165
    else -> 240
}

fun fontFamilyForId(fontId: String): FontFamily = when (fontId) {
    "tajawal" -> FontFamily(Font(R.font.tajawal, FontWeight.Normal))
    "ibm_plex" -> FontFamily(Font(R.font.ibm_plex_sans_arabic, FontWeight.Normal))
    "almarai" -> FontFamily(Font(R.font.almarai, FontWeight.Normal))
    else -> FontFamily(Font(R.font.cairo, FontWeight.Normal))
}

fun fontWeightForValue(weight: Long): FontWeight = when (weight) {
    400L -> FontWeight.Normal
    800L -> FontWeight.ExtraBold
    else -> FontWeight.SemiBold
}

private fun parseHexOrNull(hex: String): Color? {
    val clean = hex.trim()
    if (clean.isEmpty()) return null
    return runCatching {
        Color(android.graphics.Color.parseColor(if (clean.startsWith("#")) clean else "#$clean"))
    }.getOrNull()
}

@Composable
fun MNotesTheme(
    themeId: String,
    fontId: String,
    fontWeightValue: Long,
    refreshRateHz: Int = 120,
    textScalePercent: Int = 100,
    cardScalePercent: Int = 100,
    customPrimaryHex: String = "",
    customBgHex: String = "",
    customAccentHex: String = "",
    content: @Composable () -> Unit
) {
    val baseSpec = MNotesCatalog.themeById(themeId)
    val customPrimary = parseHexOrNull(customPrimaryHex)
    val customBg = parseHexOrNull(customBgHex)
    val customAccent = parseHexOrNull(customAccentHex)
    val spec = baseSpec.copy(
        primary = customPrimary ?: baseSpec.primary,
        primarySoft = customPrimary?.copy(alpha = 0.14f) ?: baseSpec.primarySoft,
        bgMain = customBg ?: baseSpec.bgMain,
        bgTopGlow = customBg?.let { Color(
            red = (it.red * 0.92f).coerceIn(0f, 1f),
            green = (it.green * 0.96f).coerceIn(0f, 1f),
            blue = (it.blue * 0.96f).coerceIn(0f, 1f),
            alpha = 1f
        ) } ?: baseSpec.bgTopGlow,
        accent = customAccent ?: baseSpec.accent
    )
    val fontFamily = fontFamilyForId(fontId)
    val baseWeight = fontWeightForValue(fontWeightValue)
    val textScale = (textScalePercent.coerceIn(75, 185) / 100f)
    val cardScale = (cardScalePercent.coerceIn(80, 160) / 100f)

    val colorScheme = lightColorScheme(
        primary = spec.primary,
        onPrimary = Color.White,
        primaryContainer = spec.primarySoft,
        onPrimaryContainer = spec.primary,
        secondary = spec.accent,
        onSecondary = Color.White,
        background = spec.bgMain,
        onBackground = spec.textDark,
        surface = spec.cardBg,
        onSurface = spec.textDark,
        surfaceVariant = spec.bgMain,
        onSurfaceVariant = spec.textMuted,
        outline = spec.borderSoft,
        error = Color(0xFFEF4444),
        onError = Color.White
    )

    val typography = Typography(
        headlineMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = (22f * textScale).sp,
            lineHeight = (28f * textScale).sp
        ),
        titleLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = (18f * textScale).sp,
            lineHeight = (24f * textScale).sp
        ),
        titleMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (15f * textScale).sp,
            lineHeight = (20f * textScale).sp
        ),
        bodyLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = baseWeight,
            fontSize = (14f * textScale).sp,
            lineHeight = (20f * textScale).sp
        ),
        bodyMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = baseWeight,
            fontSize = (13f * textScale).sp,
            lineHeight = (18f * textScale).sp
        ),
        bodySmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (11.5f * textScale).sp,
            lineHeight = (16f * textScale).sp
        ),
        labelLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (13f * textScale).sp,
            lineHeight = (18f * textScale).sp
        ),
        labelSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (11f * textScale).sp,
            lineHeight = (14f * textScale).sp
        )
    )

    CompositionLocalProvider(
        LocalMNotesColors provides spec,
        LocalMNotesWeight provides baseWeight,
        LocalMNotesRefreshHz provides refreshRateHz,
        LocalMNotesTextScale provides textScale,
        LocalMNotesCardScale provides cardScale
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content
        )
    }
}
