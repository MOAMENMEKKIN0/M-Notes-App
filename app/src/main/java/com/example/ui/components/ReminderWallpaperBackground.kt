package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import com.example.ui.theme.LocalMNotesColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class ReminderBgThemeSpec(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val topColor: Color,
    val midColor: Color,
    val bottomColor: Color,
    val glowPrimary: Color,
    val glowSecondary: Color,
    val doodleInk: Color,
    val doodleAccent: Color,
    val isDefaultAppTheme: Boolean = false
)

data class ReminderDoodlePackSpec(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val subtitleAr: String,
    val subtitleEn: String,
    val motifs: List<ReminderDoodleMotif>
)

enum class ReminderDoodleMotif {
    DUMBBELL,
    KETTLEBELL,
    WATER_BOTTLE,
    WEIGHT_PLATE,
    RUNNING_SHOE,
    HEART_PULSE,
    STOPWATCH,
    FLAME,
    APPLE,
    WATERMELON,
    CITRUS,
    COFFEE_CUP,
    CRESCENT_MOON,
    FLUFFY_CLOUD,
    ZZZ_SLEEP,
    TWINKLE_STAR,
    LOTUS,
    ALARM_CLOCK,
    CHECKLIST,
    LEAF_SPROUT,
    SUN_SOFT,
    SPARKLE
}

object ReminderWallpaperCatalog {

    val themes: List<ReminderBgThemeSpec> = listOf(
        ReminderBgThemeSpec(
            id = "default",
            nameAr = "الأصلي (لون التطبيق)",
            nameEn = "App Default",
            topColor = Color(0xFFF3FBF8),
            midColor = Color(0xFFFFFFFF),
            bottomColor = Color(0xFFF6FAF8),
            glowPrimary = Color(0xFF18C28B),
            glowSecondary = Color(0xFF38BDF8),
            doodleInk = Color(0xFF10B981),
            doodleAccent = Color(0xFF0EA5E9),
            isDefaultAppTheme = true
        ),
        ReminderBgThemeSpec(
            id = "mint_breeze",
            nameAr = "نسيم النعناع",
            nameEn = "Mint Breeze",
            topColor = Color(0xFFDFFAF0),
            midColor = Color(0xFFF2FFF9),
            bottomColor = Color(0xFFE3F9F0),
            glowPrimary = Color(0xFF34D399),
            glowSecondary = Color(0xFFA7F3D0),
            doodleInk = Color(0xFF059669),
            doodleAccent = Color(0xFF14B8A6)
        ),
        ReminderBgThemeSpec(
            id = "sky_cloud",
            nameAr = "سماء لبني ناعمة",
            nameEn = "Soft Sky Blue",
            topColor = Color(0xFFDDF1FF),
            midColor = Color(0xFFF4FAFF),
            bottomColor = Color(0xFFE1F2FF),
            glowPrimary = Color(0xFF38BDF8),
            glowSecondary = Color(0xFFBAE6FD),
            doodleInk = Color(0xFF0284C7),
            doodleAccent = Color(0xFF06B6D4)
        ),
        ReminderBgThemeSpec(
            id = "aqua_cyan",
            nameAr = "أكوا سماوي هادئ",
            nameEn = "Aqua Cyan",
            topColor = Color(0xFFD8FAF9),
            midColor = Color(0xFFF1FEFE),
            bottomColor = Color(0xFFDDF7F7),
            glowPrimary = Color(0xFF22D3EE),
            glowSecondary = Color(0xFF99F6E4),
            doodleInk = Color(0xFF0891B2),
            doodleAccent = Color(0xFF10B981)
        ),
        ReminderBgThemeSpec(
            id = "mint_sky_duet",
            nameAr = "نعناع ولبني باستيل",
            nameEn = "Mint & Sky Duet",
            topColor = Color(0xFFE0FBF2),
            midColor = Color(0xFFF5FCFF),
            bottomColor = Color(0xFFDCF2FF),
            glowPrimary = Color(0xFF2DD4BF),
            glowSecondary = Color(0xFF7DD3FC),
            doodleInk = Color(0xFF0D9488),
            doodleAccent = Color(0xFF0284C7)
        ),
        ReminderBgThemeSpec(
            id = "snow_pearl",
            nameAr = "أبيض لؤلؤي نقي",
            nameEn = "Snow Pearl White",
            topColor = Color(0xFFFFFFFF),
            midColor = Color(0xFFFAFBFD),
            bottomColor = Color(0xFFF2F6FA),
            glowPrimary = Color(0xFFCBD5E1),
            glowSecondary = Color(0xFFBAE6FD),
            doodleInk = Color(0xFF64748B),
            doodleAccent = Color(0xFF14B8A6)
        ),
        ReminderBgThemeSpec(
            id = "cream_vanilla",
            nameAr = "كريمي دافئ",
            nameEn = "Warm Cream",
            topColor = Color(0xFFFFF6EA),
            midColor = Color(0xFFFFFDF9),
            bottomColor = Color(0xFFF9EFE1),
            glowPrimary = Color(0xFFFCD34D),
            glowSecondary = Color(0xFFFDBA74),
            doodleInk = Color(0xFFB4692C),
            doodleAccent = Color(0xFFD97706)
        ),
        ReminderBgThemeSpec(
            id = "lavender_mist",
            nameAr = "سحابة لافندر هادئة",
            nameEn = "Lavender Mist",
            topColor = Color(0xFFEEE8FF),
            midColor = Color(0xFFFAF8FF),
            bottomColor = Color(0xFFE8E2FF),
            glowPrimary = Color(0xFFA78BFA),
            glowSecondary = Color(0xFFC4B5FD),
            doodleInk = Color(0xFF7C3AED),
            doodleAccent = Color(0xFF0EA5E9)
        ),
        ReminderBgThemeSpec(
            id = "peach_blossom",
            nameAr = "خوخي باستيل لطيف",
            nameEn = "Soft Peach",
            topColor = Color(0xFFFFECE5),
            midColor = Color(0xFFFFFAF8),
            bottomColor = Color(0xFFFFE5DC),
            glowPrimary = Color(0xFFFB923C),
            glowSecondary = Color(0xFFFDA4AF),
            doodleInk = Color(0xFFEA580C),
            doodleAccent = Color(0xFFE11D48)
        ),
        ReminderBgThemeSpec(
            id = "sage_garden",
            nameAr = "أخضر مريمية ناعم",
            nameEn = "Sage Garden",
            topColor = Color(0xFFE6F5E8),
            midColor = Color(0xFFF6FCF7),
            bottomColor = Color(0xFFDFF2E2),
            glowPrimary = Color(0xFF4ADE80),
            glowSecondary = Color(0xFF86EFAC),
            doodleInk = Color(0xFF16A34A),
            doodleAccent = Color(0xFF0D9488)
        )
    )

    val doodlePacks: List<ReminderDoodlePackSpec> = listOf(
        ReminderDoodlePackSpec(
            id = "none",
            nameAr = "صافي بدون رسومات",
            nameEn = "Clean (No Doodles)",
            subtitleAr = "خلفية باستيل ناعمة فقط",
            subtitleEn = "Soft pastel backdrop only",
            motifs = emptyList()
        ),
        ReminderDoodlePackSpec(
            id = "all_mix",
            nameAr = "مزيج لطيف شامل",
            nameEn = "Cute Lifestyle Mix",
            subtitleAr = "جيم • فواكه • نوم • روتين يومي",
            subtitleEn = "Gym • Fruits • Sleep • Daily",
            motifs = listOf(
                ReminderDoodleMotif.DUMBBELL,
                ReminderDoodleMotif.APPLE,
                ReminderDoodleMotif.CRESCENT_MOON,
                ReminderDoodleMotif.RUNNING_SHOE,
                ReminderDoodleMotif.COFFEE_CUP,
                ReminderDoodleMotif.SPARKLE,
                ReminderDoodleMotif.ALARM_CLOCK,
                ReminderDoodleMotif.WATERMELON,
                ReminderDoodleMotif.FLUFFY_CLOUD,
                ReminderDoodleMotif.HEART_PULSE,
                ReminderDoodleMotif.LEAF_SPROUT,
                ReminderDoodleMotif.TWINKLE_STAR
            )
        ),
        ReminderDoodlePackSpec(
            id = "gym_fitness",
            nameAr = "جيم وتمارين",
            nameEn = "Gym & Fitness",
            subtitleAr = "دمبل • كيتل بل • أوزان • زجاجة مياه",
            subtitleEn = "Dumbbells • Kettlebells • Plates • Bottle",
            motifs = listOf(
                ReminderDoodleMotif.DUMBBELL,
                ReminderDoodleMotif.KETTLEBELL,
                ReminderDoodleMotif.WEIGHT_PLATE,
                ReminderDoodleMotif.WATER_BOTTLE,
                ReminderDoodleMotif.HEART_PULSE,
                ReminderDoodleMotif.FLAME,
                ReminderDoodleMotif.STOPWATCH,
                ReminderDoodleMotif.SPARKLE
            )
        ),
        ReminderDoodlePackSpec(
            id = "cardio_energy",
            nameAr = "كارديو وجري",
            nameEn = "Cardio & Energy",
            subtitleAr = "حذاء جري • نبض القلب • مؤقت • شعلة",
            subtitleEn = "Running shoe • Pulse • Timer • Energy",
            motifs = listOf(
                ReminderDoodleMotif.RUNNING_SHOE,
                ReminderDoodleMotif.HEART_PULSE,
                ReminderDoodleMotif.STOPWATCH,
                ReminderDoodleMotif.FLAME,
                ReminderDoodleMotif.WATER_BOTTLE,
                ReminderDoodleMotif.SUN_SOFT,
                ReminderDoodleMotif.SPARKLE
            )
        ),
        ReminderDoodlePackSpec(
            id = "fruits_food",
            nameAr = "فواكه وطعام صحي",
            nameEn = "Fruits & Healthy Food",
            subtitleAr = "تفاح • بطيخ • حمضيات • قهوة • أوراق",
            subtitleEn = "Apple • Watermelon • Citrus • Coffee",
            motifs = listOf(
                ReminderDoodleMotif.APPLE,
                ReminderDoodleMotif.WATERMELON,
                ReminderDoodleMotif.CITRUS,
                ReminderDoodleMotif.COFFEE_CUP,
                ReminderDoodleMotif.LEAF_SPROUT,
                ReminderDoodleMotif.SPARKLE,
                ReminderDoodleMotif.HEART_PULSE
            )
        ),
        ReminderDoodlePackSpec(
            id = "sleep_calm",
            nameAr = "نوم واسترخاء",
            nameEn = "Sleep & Relaxation",
            subtitleAr = "هلال • سحب • نجوم • Zzz • لوتس",
            subtitleEn = "Moon • Clouds • Stars • Zzz • Lotus",
            motifs = listOf(
                ReminderDoodleMotif.CRESCENT_MOON,
                ReminderDoodleMotif.FLUFFY_CLOUD,
                ReminderDoodleMotif.ZZZ_SLEEP,
                ReminderDoodleMotif.TWINKLE_STAR,
                ReminderDoodleMotif.LOTUS,
                ReminderDoodleMotif.SPARKLE
            )
        ),
        ReminderDoodlePackSpec(
            id = "daily_routine",
            nameAr = "روتين ومهام يومية",
            nameEn = "Daily Routines",
            subtitleAr = "منبه • قائمة مهام • شمس • قهوة • أوراق",
            subtitleEn = "Alarm • Checklist • Sun • Coffee • Leaves",
            motifs = listOf(
                ReminderDoodleMotif.ALARM_CLOCK,
                ReminderDoodleMotif.CHECKLIST,
                ReminderDoodleMotif.SUN_SOFT,
                ReminderDoodleMotif.COFFEE_CUP,
                ReminderDoodleMotif.LEAF_SPROUT,
                ReminderDoodleMotif.TWINKLE_STAR,
                ReminderDoodleMotif.SPARKLE
            )
        )
    )

    fun themeById(id: String): ReminderBgThemeSpec =
        themes.firstOrNull { it.id == id } ?: themes.first()

    fun doodlePackById(id: String): ReminderDoodlePackSpec =
        doodlePacks.firstOrNull { it.id == id } ?: doodlePacks.first()

    fun isCustomized(themeId: String, doodlePackId: String): Boolean =
        (themeId.isNotBlank() && themeId != "default") ||
            (doodlePackId.isNotBlank() && doodlePackId != "none")
}

/**
 * Renders the customizable soft pastel backdrop + subtle messaging-wallpaper-style scattered cartoon
 * doodles behind the reminder screen while keeping text, task image, and controls completely legible.
 */
@Composable
fun ReminderWallpaperCanvas(
    themeId: String,
    doodlePackId: String,
    doodleOpacityPercent: Int = 14,
    modifier: Modifier = Modifier,
    compactScale: Float = 1f
) {
    val appColors = LocalMNotesColors.current
    val theme = ReminderWallpaperCatalog.themeById(themeId)
    val pack = ReminderWallpaperCatalog.doodlePackById(doodlePackId)

    val topColor = if (theme.isDefaultAppTheme) appColors.bgTopGlow else theme.topColor
    val midColor = if (theme.isDefaultAppTheme) appColors.bgMain else theme.midColor
    val bottomColor = if (theme.isDefaultAppTheme) appColors.cardBg else theme.bottomColor
    val inkColor = if (theme.isDefaultAppTheme) appColors.primary else theme.doodleInk
    val accentColor = if (theme.isDefaultAppTheme) appColors.accent else theme.doodleAccent
    val glowPrimary = if (theme.isDefaultAppTheme) appColors.primary else theme.glowPrimary
    val glowSecondary = if (theme.isDefaultAppTheme) appColors.accent else theme.glowSecondary

    val baseAlpha = (doodleOpacityPercent.coerceIn(4, 34) / 100f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(topColor, midColor, bottomColor)))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Soft atmospheric pastel clouds/glows (never harsh)
            val w = size.width
            val h = size.height
            if (w <= 1f || h <= 1f) return@Canvas

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glowPrimary.copy(alpha = 0.14f), Color.Transparent),
                    center = Offset(w * 0.16f, h * 0.14f),
                    radius = maxOf(w, h) * 0.45f
                ),
                center = Offset(w * 0.16f, h * 0.14f),
                radius = maxOf(w, h) * 0.45f
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glowSecondary.copy(alpha = 0.13f), Color.Transparent),
                    center = Offset(w * 0.86f, h * 0.84f),
                    radius = maxOf(w, h) * 0.48f
                ),
                center = Offset(w * 0.86f, h * 0.84f),
                radius = maxOf(w, h) * 0.48f
            )

            // Subtle scattered messaging-style cartoon doodles
            val motifs = pack.motifs
            if (motifs.isEmpty()) return@Canvas

            val cellPx = (66f * density * compactScale).coerceIn(30f, 190f)
            val cols = (w / cellPx).toInt() + 2
            val rows = (h / cellPx).toInt() + 2
            val iconSize = cellPx * 0.42f
            val strokeW = (1.6f * density * compactScale).coerceIn(0.9f, 4.5f)

            for (row in 0 until rows) {
                val staggerX = if (row % 2 == 1) cellPx * 0.5f else 0f
                for (col in 0 until cols) {
                    val hash = (row * 31 + col * 17 + row * col * 7) and 0x7FFF
                    val motif = motifs[hash % motifs.size]
                    val jitterX = (((hash % 11) - 5) / 5f) * cellPx * 0.11f
                    val jitterY = ((((hash / 11) % 11) - 5) / 5f) * cellPx * 0.11f
                    val cx = col * cellPx + staggerX + cellPx * 0.35f + jitterX
                    val cy = row * cellPx + cellPx * 0.38f + jitterY
                    val angle = (((hash % 9) - 4) * 6.5f)
                    val useAccent = (hash % 3 == 0)
                    val strokeColor = (if (useAccent) accentColor else inkColor).copy(alpha = baseAlpha)
                    val fillColor = (if (useAccent) accentColor else inkColor).copy(alpha = baseAlpha * 0.28f)

                    drawCuteDoodle(
                        motif = motif,
                        center = Offset(cx, cy),
                        size = iconSize,
                        rotationDeg = angle,
                        strokeColor = strokeColor,
                        fillColor = fillColor,
                        strokeWidth = strokeW
                    )

                    // Add tiny floating pastel dot between cells for cohesive messaging-wallpaper rhythm
                    if ((row + col) % 2 == 0) {
                        val dotOffset = Offset(cx + cellPx * 0.36f, cy + cellPx * 0.26f)
                        drawCircle(
                            color = inkColor.copy(alpha = baseAlpha * 0.55f),
                            radius = (2.1f * density * compactScale).coerceIn(1f, 5f),
                            center = dotOffset
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawCuteDoodle(
    motif: ReminderDoodleMotif,
    center: Offset,
    size: Float,
    rotationDeg: Float,
    strokeColor: Color,
    fillColor: Color,
    strokeWidth: Float
) {
    val r = size * 0.5f
    val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
    rotate(degrees = rotationDeg, pivot = center) {
        translate(left = center.x - r, top = center.y - r) {
            when (motif) {
                ReminderDoodleMotif.DUMBBELL -> {
                    // Center bar
                    drawLine(strokeColor, Offset(size * 0.28f, size * 0.5f), Offset(size * 0.72f, size * 0.5f), strokeWidth * 1.2f, StrokeCap.Round)
                    // Left plates
                    drawRoundRect(fillColor, Offset(size * 0.10f, size * 0.30f), Size(size * 0.14f, size * 0.40f), CornerRadius(size * 0.06f))
                    drawRoundRect(strokeColor, Offset(size * 0.10f, size * 0.30f), Size(size * 0.14f, size * 0.40f), CornerRadius(size * 0.06f), style = stroke)
                    drawRoundRect(strokeColor, Offset(size * 0.24f, size * 0.24f), Size(size * 0.10f, size * 0.52f), CornerRadius(size * 0.04f), style = stroke)
                    // Right plates
                    drawRoundRect(fillColor, Offset(size * 0.76f, size * 0.30f), Size(size * 0.14f, size * 0.40f), CornerRadius(size * 0.06f))
                    drawRoundRect(strokeColor, Offset(size * 0.76f, size * 0.30f), Size(size * 0.14f, size * 0.40f), CornerRadius(size * 0.06f), style = stroke)
                    drawRoundRect(strokeColor, Offset(size * 0.66f, size * 0.24f), Size(size * 0.10f, size * 0.52f), CornerRadius(size * 0.04f), style = stroke)
                }

                ReminderDoodleMotif.KETTLEBELL -> {
                    // Handle arc
                    drawArc(
                        color = strokeColor,
                        startAngle = 190f,
                        sweepAngle = 160f,
                        useCenter = false,
                        topLeft = Offset(size * 0.30f, size * 0.14f),
                        size = Size(size * 0.40f, size * 0.34f),
                        style = stroke
                    )
                    // Rounded bell body
                    drawCircle(fillColor, radius = size * 0.28f, center = Offset(size * 0.50f, size * 0.62f))
                    drawCircle(strokeColor, radius = size * 0.28f, center = Offset(size * 0.50f, size * 0.62f), style = stroke)
                }

                ReminderDoodleMotif.WEIGHT_PLATE -> {
                    drawCircle(fillColor, radius = size * 0.36f, center = Offset(r, r))
                    drawCircle(strokeColor, radius = size * 0.36f, center = Offset(r, r), style = stroke)
                    drawCircle(strokeColor, radius = size * 0.13f, center = Offset(r, r), style = stroke)
                }

                ReminderDoodleMotif.WATER_BOTTLE -> {
                    drawRoundRect(fillColor, Offset(size * 0.31f, size * 0.28f), Size(size * 0.38f, size * 0.60f), CornerRadius(size * 0.12f))
                    drawRoundRect(strokeColor, Offset(size * 0.31f, size * 0.28f), Size(size * 0.38f, size * 0.60f), CornerRadius(size * 0.12f), style = stroke)
                    drawRoundRect(strokeColor, Offset(size * 0.40f, size * 0.14f), Size(size * 0.20f, size * 0.14f), CornerRadius(size * 0.04f), style = stroke)
                    drawLine(strokeColor, Offset(size * 0.36f, size * 0.56f), Offset(size * 0.64f, size * 0.56f), strokeWidth, StrokeCap.Round)
                }

                ReminderDoodleMotif.RUNNING_SHOE -> {
                    val p = Path().apply {
                        moveTo(size * 0.14f, size * 0.66f)
                        lineTo(size * 0.20f, size * 0.42f)
                        lineTo(size * 0.40f, size * 0.48f)
                        lineTo(size * 0.54f, size * 0.38f)
                        quadraticTo(size * 0.72f, size * 0.52f, size * 0.86f, size * 0.58f)
                        quadraticTo(size * 0.90f, size * 0.70f, size * 0.82f, size * 0.72f)
                        lineTo(size * 0.16f, size * 0.72f)
                        close()
                    }
                    drawPath(p, fillColor)
                    drawPath(p, strokeColor, style = stroke)
                }

                ReminderDoodleMotif.HEART_PULSE -> {
                    val heart = Path().apply {
                        moveTo(r, size * 0.78f)
                        cubicTo(size * 0.16f, size * 0.56f, size * 0.14f, size * 0.26f, size * 0.36f, size * 0.26f)
                        cubicTo(size * 0.45f, size * 0.26f, r, size * 0.34f, r, size * 0.38f)
                        cubicTo(r, size * 0.34f, size * 0.55f, size * 0.26f, size * 0.64f, size * 0.26f)
                        cubicTo(size * 0.86f, size * 0.26f, size * 0.84f, size * 0.56f, r, size * 0.78f)
                        close()
                    }
                    drawPath(heart, fillColor)
                    drawPath(heart, strokeColor, style = stroke)
                }

                ReminderDoodleMotif.STOPWATCH -> {
                    drawCircle(fillColor, radius = size * 0.32f, center = Offset(r, size * 0.56f))
                    drawCircle(strokeColor, radius = size * 0.32f, center = Offset(r, size * 0.56f), style = stroke)
                    drawLine(strokeColor, Offset(size * 0.42f, size * 0.16f), Offset(size * 0.58f, size * 0.16f), strokeWidth, StrokeCap.Round)
                    drawLine(strokeColor, Offset(r, size * 0.16f), Offset(r, size * 0.24f), strokeWidth, StrokeCap.Round)
                    drawLine(strokeColor, Offset(r, size * 0.56f), Offset(size * 0.62f, size * 0.44f), strokeWidth, StrokeCap.Round)
                }

                ReminderDoodleMotif.FLAME -> {
                    val flame = Path().apply {
                        moveTo(r, size * 0.14f)
                        cubicTo(size * 0.78f, size * 0.36f, size * 0.80f, size * 0.76f, r, size * 0.84f)
                        cubicTo(size * 0.20f, size * 0.76f, size * 0.22f, size * 0.40f, r, size * 0.14f)
                        close()
                    }
                    drawPath(flame, fillColor)
                    drawPath(flame, strokeColor, style = stroke)
                }

                ReminderDoodleMotif.APPLE -> {
                    val body = Path().apply {
                        moveTo(r, size * 0.34f)
                        cubicTo(size * 0.28f, size * 0.24f, size * 0.18f, size * 0.58f, size * 0.36f, size * 0.80f)
                        quadraticTo(r, size * 0.84f, size * 0.64f, size * 0.80f)
                        cubicTo(size * 0.82f, size * 0.58f, size * 0.72f, size * 0.24f, r, size * 0.34f)
                        close()
                    }
                    drawPath(body, fillColor)
                    drawPath(body, strokeColor, style = stroke)
                    // Stem + leaf
                    drawLine(strokeColor, Offset(r, size * 0.32f), Offset(size * 0.52f, size * 0.18f), strokeWidth, StrokeCap.Round)
                    drawOval(fillColor, Offset(size * 0.52f, size * 0.16f), Size(size * 0.20f, size * 0.11f))
                    drawOval(strokeColor, Offset(size * 0.52f, size * 0.16f), Size(size * 0.20f, size * 0.11f), style = stroke)
                }

                ReminderDoodleMotif.WATERMELON -> {
                    val slice = Path().apply {
                        moveTo(size * 0.16f, size * 0.44f)
                        lineTo(size * 0.84f, size * 0.44f)
                        quadraticTo(r, size * 0.90f, size * 0.16f, size * 0.44f)
                        close()
                    }
                    drawPath(slice, fillColor)
                    drawPath(slice, strokeColor, style = stroke)
                    drawCircle(strokeColor, strokeWidth * 0.7f, Offset(size * 0.40f, size * 0.54f))
                    drawCircle(strokeColor, strokeWidth * 0.7f, Offset(size * 0.52f, size * 0.60f))
                    drawCircle(strokeColor, strokeWidth * 0.7f, Offset(size * 0.62f, size * 0.53f))
                }

                ReminderDoodleMotif.CITRUS -> {
                    drawCircle(fillColor, radius = size * 0.34f, center = Offset(r, r))
                    drawCircle(strokeColor, radius = size * 0.34f, center = Offset(r, r), style = stroke)
                    for (i in 0 until 6) {
                        val rad = (i * 60.0) * PI / 180.0
                        val ex = r + (size * 0.28f * cos(rad)).toFloat()
                        val ey = r + (size * 0.28f * sin(rad)).toFloat()
                        drawLine(strokeColor, Offset(r, r), Offset(ex, ey), strokeWidth * 0.75f, StrokeCap.Round)
                    }
                }

                ReminderDoodleMotif.COFFEE_CUP -> {
                    drawRoundRect(fillColor, Offset(size * 0.22f, size * 0.38f), Size(size * 0.46f, size * 0.40f), CornerRadius(size * 0.10f))
                    drawRoundRect(strokeColor, Offset(size * 0.22f, size * 0.38f), Size(size * 0.46f, size * 0.40f), CornerRadius(size * 0.10f), style = stroke)
                    drawArc(strokeColor, -90f, 180f, false, Offset(size * 0.60f, size * 0.45f), Size(size * 0.18f, size * 0.22f), style = stroke)
                    // Steam wisps
                    drawLine(strokeColor, Offset(size * 0.38f, size * 0.30f), Offset(size * 0.40f, size * 0.18f), strokeWidth * 0.85f, StrokeCap.Round)
                    drawLine(strokeColor, Offset(size * 0.52f, size * 0.30f), Offset(size * 0.54f, size * 0.18f), strokeWidth * 0.85f, StrokeCap.Round)
                }

                ReminderDoodleMotif.CRESCENT_MOON -> {
                    val moon = Path().apply {
                        moveTo(size * 0.64f, size * 0.20f)
                        cubicTo(size * 0.30f, size * 0.22f, size * 0.22f, size * 0.66f, size * 0.56f, size * 0.80f)
                        cubicTo(size * 0.70f, size * 0.84f, size * 0.78f, size * 0.76f, size * 0.78f, size * 0.72f)
                        cubicTo(size * 0.52f, size * 0.72f, size * 0.44f, size * 0.42f, size * 0.64f, size * 0.20f)
                        close()
                    }
                    drawPath(moon, fillColor)
                    drawPath(moon, strokeColor, style = stroke)
                }

                ReminderDoodleMotif.FLUFFY_CLOUD -> {
                    val cloud = Path().apply {
                        moveTo(size * 0.24f, size * 0.68f)
                        quadraticTo(size * 0.12f, size * 0.68f, size * 0.16f, size * 0.54f)
                        quadraticTo(size * 0.18f, size * 0.40f, size * 0.34f, size * 0.42f)
                        quadraticTo(size * 0.42f, size * 0.24f, size * 0.60f, size * 0.32f)
                        quadraticTo(size * 0.74f, size * 0.30f, size * 0.78f, size * 0.46f)
                        quadraticTo(size * 0.88f, size * 0.50f, size * 0.82f, size * 0.64f)
                        quadraticTo(size * 0.78f, size * 0.68f, size * 0.24f, size * 0.68f)
                        close()
                    }
                    drawPath(cloud, fillColor)
                    drawPath(cloud, strokeColor, style = stroke)
                }

                ReminderDoodleMotif.ZZZ_SLEEP -> {
                    // Cute big Z + tiny z
                    val z1 = Path().apply {
                        moveTo(size * 0.20f, size * 0.36f)
                        lineTo(size * 0.52f, size * 0.36f)
                        lineTo(size * 0.22f, size * 0.72f)
                        lineTo(size * 0.54f, size * 0.72f)
                    }
                    val z2 = Path().apply {
                        moveTo(size * 0.58f, size * 0.20f)
                        lineTo(size * 0.78f, size * 0.20f)
                        lineTo(size * 0.58f, size * 0.42f)
                        lineTo(size * 0.78f, size * 0.42f)
                    }
                    drawPath(z1, strokeColor, style = stroke)
                    drawPath(z2, strokeColor, style = stroke)
                }

                ReminderDoodleMotif.TWINKLE_STAR -> {
                    val star = Path().apply {
                        val outer = size * 0.36f
                        val inner = size * 0.16f
                        for (i in 0 until 10) {
                            val rad = (i * 36.0 - 90.0) * PI / 180.0
                            val radLen = if (i % 2 == 0) outer else inner
                            val px = r + (radLen * cos(rad)).toFloat()
                            val py = r + (radLen * sin(rad)).toFloat()
                            if (i == 0) moveTo(px, py) else lineTo(px, py)
                        }
                        close()
                    }
                    drawPath(star, fillColor)
                    drawPath(star, strokeColor, style = stroke)
                }

                ReminderDoodleMotif.LOTUS -> {
                    val centerPetal = Path().apply {
                        moveTo(r, size * 0.22f)
                        quadraticTo(size * 0.68f, size * 0.50f, r, size * 0.76f)
                        quadraticTo(size * 0.32f, size * 0.50f, r, size * 0.22f)
                        close()
                    }
                    val leftPetal = Path().apply {
                        moveTo(r, size * 0.76f)
                        quadraticTo(size * 0.18f, size * 0.66f, size * 0.20f, size * 0.40f)
                        quadraticTo(size * 0.36f, size * 0.48f, r, size * 0.76f)
                        close()
                    }
                    val rightPetal = Path().apply {
                        moveTo(r, size * 0.76f)
                        quadraticTo(size * 0.82f, size * 0.66f, size * 0.80f, size * 0.40f)
                        quadraticTo(size * 0.64f, size * 0.48f, r, size * 0.76f)
                        close()
                    }
                    drawPath(leftPetal, fillColor)
                    drawPath(rightPetal, fillColor)
                    drawPath(centerPetal, fillColor)
                    drawPath(leftPetal, strokeColor, style = stroke)
                    drawPath(rightPetal, strokeColor, style = stroke)
                    drawPath(centerPetal, strokeColor, style = stroke)
                }

                ReminderDoodleMotif.ALARM_CLOCK -> {
                    drawCircle(fillColor, radius = size * 0.30f, center = Offset(r, size * 0.54f))
                    drawCircle(strokeColor, radius = size * 0.30f, center = Offset(r, size * 0.54f), style = stroke)
                    drawArc(strokeColor, 180f, 120f, false, Offset(size * 0.14f, size * 0.18f), Size(size * 0.24f, size * 0.24f), style = stroke)
                    drawArc(strokeColor, -120f, 120f, false, Offset(size * 0.62f, size * 0.18f), Size(size * 0.24f, size * 0.24f), style = stroke)
                    drawLine(strokeColor, Offset(r, size * 0.54f), Offset(r, size * 0.36f), strokeWidth, StrokeCap.Round)
                    drawLine(strokeColor, Offset(r, size * 0.54f), Offset(size * 0.62f, size * 0.58f), strokeWidth, StrokeCap.Round)
                }

                ReminderDoodleMotif.CHECKLIST -> {
                    drawRoundRect(fillColor, Offset(size * 0.22f, size * 0.18f), Size(size * 0.56f, size * 0.64f), CornerRadius(size * 0.10f))
                    drawRoundRect(strokeColor, Offset(size * 0.22f, size * 0.18f), Size(size * 0.56f, size * 0.64f), CornerRadius(size * 0.10f), style = stroke)
                    drawLine(strokeColor, Offset(size * 0.34f, size * 0.40f), Offset(size * 0.66f, size * 0.40f), strokeWidth, StrokeCap.Round)
                    drawLine(strokeColor, Offset(size * 0.34f, size * 0.56f), Offset(size * 0.60f, size * 0.56f), strokeWidth, StrokeCap.Round)
                    drawLine(strokeColor, Offset(size * 0.34f, size * 0.70f), Offset(size * 0.54f, size * 0.70f), strokeWidth, StrokeCap.Round)
                }

                ReminderDoodleMotif.LEAF_SPROUT -> {
                    val leaf = Path().apply {
                        moveTo(size * 0.24f, size * 0.76f)
                        quadraticTo(size * 0.24f, size * 0.26f, size * 0.78f, size * 0.22f)
                        quadraticTo(size * 0.74f, size * 0.74f, size * 0.24f, size * 0.76f)
                        close()
                    }
                    drawPath(leaf, fillColor)
                    drawPath(leaf, strokeColor, style = stroke)
                    drawLine(strokeColor, Offset(size * 0.22f, size * 0.78f), Offset(size * 0.62f, size * 0.38f), strokeWidth * 0.9f, StrokeCap.Round)
                }

                ReminderDoodleMotif.SUN_SOFT -> {
                    drawCircle(fillColor, radius = size * 0.20f, center = Offset(r, r))
                    drawCircle(strokeColor, radius = size * 0.20f, center = Offset(r, r), style = stroke)
                    for (i in 0 until 8) {
                        val rad = (i * 45.0) * PI / 180.0
                        val sx = r + (size * 0.28f * cos(rad)).toFloat()
                        val sy = r + (size * 0.28f * sin(rad)).toFloat()
                        val ex = r + (size * 0.38f * cos(rad)).toFloat()
                        val ey = r + (size * 0.38f * sin(rad)).toFloat()
                        drawLine(strokeColor, Offset(sx, sy), Offset(ex, ey), strokeWidth * 0.9f, StrokeCap.Round)
                    }
                }

                ReminderDoodleMotif.SPARKLE -> {
                    val sp = Path().apply {
                        moveTo(r, size * 0.16f)
                        quadraticTo(r, r, size * 0.84f, r)
                        quadraticTo(r, r, r, size * 0.84f)
                        quadraticTo(r, r, size * 0.16f, r)
                        quadraticTo(r, r, r, size * 0.16f)
                        close()
                    }
                    drawPath(sp, fillColor)
                    drawPath(sp, strokeColor, style = stroke)
                }
            }
        }
    }
}
