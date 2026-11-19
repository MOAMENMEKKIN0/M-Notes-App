package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Unified circular floating badge renderer supporting:
 * - The exact original 3D character artwork & derived character states
 * - 100+ cohesive 3D glossy pastel cartoon medallions across 10 categories
 * - Dimensional cartoon pop-out effects (Zzz letters, sparkles, bolts, hearts,
 *   rising steam wisps, moons, stars, flames, drops, checks, notes) emerging
 *   cleanly past the circular border while keeping strict 1:1 proportions.
 */
@Composable
fun FloatingBadgeVisual(
    iconId: String,
    sizeDp: Float,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    val spec = remember(iconId) { FloatingBadgeCatalog.byId(iconId) }
    val clamped = sizeDp.coerceIn(24f, 180f)
    val diameter = clamped.dp
    val ringWidth = (clamped * 0.042f).coerceIn(1.5f, 3.6f).dp
    val shadowElev = (clamped * 0.11f).coerceIn(3.5f, 10f).dp

    // Outer unclipped wrapper lets dimensional pop-out effects emerge slightly past the circle rim
    Box(
        modifier = modifier.size(diameter),
        contentAlignment = Alignment.Center
    ) {
        if (spec.gridTileUri != null) {
            // Pixar 3D character portrait from 3x3 cached sheet inside crisp white-rimmed circular medallion
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .shadow(shadowElev, CircleShape, clip = false)
                    .clip(CircleShape)
                    .background(Color(0xFFE8F7F2))
                    .border(ringWidth, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                MNotesPhotoContent(
                    imgUri = spec.gridTileUri,
                    useBannerDefaultIfBlank = false,
                    contentDescription = contentDescription,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            }
        } else if (spec.drawableRes != null) {
            // Exact original circular character style (#E8F7F2 backdrop, crisp white ring, 1:1 circle)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .shadow(shadowElev, CircleShape, clip = false)
                    .clip(CircleShape)
                    .background(Color(0xFFE8F7F2))
                    .border(ringWidth, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = spec.drawableRes),
                    contentDescription = contentDescription,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            }
        } else {
            // Glossy 3D pastel cartoon medallion matching the app's cute 3D aesthetic
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .shadow(shadowElev, CircleShape, clip = false)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(spec.bgTop, spec.bgBottom)
                        )
                    )
                    .border(ringWidth, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // 3D dome highlight, inner ambient rim, and soft specular gloss
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    // Soft radial highlight near top-left for 3D spherical feel
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.72f),
                                Color.White.copy(alpha = 0.14f),
                                Color.Transparent
                            ),
                            center = Offset(w * 0.32f, h * 0.24f),
                            radius = w * 0.56f
                        ),
                        radius = w * 0.5f,
                        center = Offset(w * 0.5f, h * 0.5f)
                    )
                    // Subtle lower inner shadow for pillowy depth
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.Transparent,
                                spec.accentTint.copy(alpha = 0.18f)
                            ),
                            center = Offset(w * 0.45f, h * 0.40f),
                            radius = w * 0.58f
                        ),
                        radius = w * 0.5f,
                        center = Offset(w * 0.5f, h * 0.5f)
                    )
                    // Crisp glossy bean reflection on top-left
                    drawOval(
                        color = Color.White.copy(alpha = 0.58f),
                        topLeft = Offset(w * 0.20f, h * 0.10f),
                        size = androidx.compose.ui.geometry.Size(w * 0.34f, h * 0.16f)
                    )
                    // Tiny decorative pastel dot accents
                    drawCircle(
                        color = Color.White.copy(alpha = 0.75f),
                        radius = (w * 0.032f).coerceAtLeast(1.2f),
                        center = Offset(w * 0.76f, h * 0.25f)
                    )
                    drawCircle(
                        color = spec.accentTint.copy(alpha = 0.32f),
                        radius = (w * 0.028f).coerceAtLeast(1.1f),
                        center = Offset(w * 0.22f, h * 0.76f)
                    )
                }

                // Sticker-outlined central rounded emblem (soft drop shadow + crisp white halo + rich fill)
                spec.vector?.let { vec ->
                    val emblemSize = (clamped * 0.54f).dp
                    val haloSize = (clamped * 0.59f).dp
                    val shadowOffset = (clamped * 0.028f).coerceIn(1f, 3.5f).dp
                    Box(contentAlignment = Alignment.Center) {
                        // Soft cartoon drop shadow
                        Icon(
                            imageVector = vec,
                            contentDescription = null,
                            tint = spec.iconTint.copy(alpha = 0.22f),
                            modifier = Modifier
                                .offset(x = shadowOffset * 0.5f, y = shadowOffset)
                                .size(emblemSize)
                        )
                        // Subtle white sticker rim glow behind emblem
                        Icon(
                            imageVector = vec,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.86f),
                            modifier = Modifier.size(haloSize)
                        )
                        // Rich main emblem
                        Icon(
                            imageVector = vec,
                            contentDescription = contentDescription,
                            tint = spec.iconTint,
                            modifier = Modifier.size(emblemSize)
                        )
                    }
                }
            }
        }

        // Dimensional pop-out overlay emerging cleanly past the circular rim
        BadgePopEffectOverlay(
            effect = spec.popEffect,
            accentTint = spec.accentTint,
            diameterDp = clamped
        )
    }
}

@Composable
private fun BadgePopEffectOverlay(
    effect: BadgePopEffect,
    accentTint: Color,
    diameterDp: Float
) {
    when (effect) {
        BadgePopEffect.NONE -> Unit

        BadgePopEffect.ZZZ_POP -> {
            // Raised 3D cartoon Zzz stepping diagonally out of the top-right rim
            CartoonZLetter(
                letter = "z",
                fontSizeSp = (diameterDp * 0.22f).coerceIn(8f, 30f),
                fillColor = accentTint,
                rotationDeg = -10f,
                modifier = Modifier.offset(
                    x = (diameterDp * 0.22f).dp,
                    y = (-diameterDp * 0.24f).dp
                )
            )
            CartoonZLetter(
                letter = "Z",
                fontSizeSp = (diameterDp * 0.30f).coerceIn(10f, 40f),
                fillColor = accentTint,
                rotationDeg = 8f,
                modifier = Modifier.offset(
                    x = (diameterDp * 0.38f).dp,
                    y = (-diameterDp * 0.38f).dp
                )
            )
            CartoonZLetter(
                letter = "Z",
                fontSizeSp = (diameterDp * 0.38f).coerceIn(12f, 52f),
                fillColor = Color(0xFF1D4ED8),
                rotationDeg = 16f,
                modifier = Modifier.offset(
                    x = (diameterDp * 0.55f).dp,
                    y = (-diameterDp * 0.54f).dp
                )
            )
        }

        BadgePopEffect.ZZZ_BIG -> {
            // Bold, extra-dimensional popping Zzz cluster bursting outside the circle
            CartoonZLetter(
                letter = "z",
                fontSizeSp = (diameterDp * 0.26f).coerceIn(9f, 36f),
                fillColor = Color(0xFF60A5FA),
                rotationDeg = -14f,
                modifier = Modifier.offset(
                    x = (diameterDp * 0.16f).dp,
                    y = (-diameterDp * 0.28f).dp
                )
            )
            CartoonZLetter(
                letter = "Z",
                fontSizeSp = (diameterDp * 0.36f).coerceIn(12f, 48f),
                fillColor = accentTint,
                rotationDeg = 6f,
                modifier = Modifier.offset(
                    x = (diameterDp * 0.36f).dp,
                    y = (-diameterDp * 0.44f).dp
                )
            )
            CartoonZLetter(
                letter = "Z",
                fontSizeSp = (diameterDp * 0.46f).coerceIn(14f, 62f),
                fillColor = Color(0xFF4338CA),
                rotationDeg = 18f,
                modifier = Modifier.offset(
                    x = (diameterDp * 0.58f).dp,
                    y = (-diameterDp * 0.62f).dp
                )
            )
        }

        BadgePopEffect.SPARKLE_POP -> {
            PopOutStickerPin(
                icon = Icons.Rounded.AutoAwesome,
                badgeBg = Color(0xFFFFFBEB),
                iconTint = Color(0xFFF59E0B),
                pinSize = (diameterDp * 0.36f).coerceIn(13f, 44f).dp,
                offsetX = (diameterDp * 0.38f).dp,
                offsetY = (-diameterDp * 0.36f).dp,
                rotationDeg = 12f
            )
        }

        BadgePopEffect.BOLT_POP -> {
            PopOutStickerPin(
                icon = Icons.Rounded.Bolt,
                badgeBg = Color(0xFFFEF3C7),
                iconTint = Color(0xFFEA580C),
                pinSize = (diameterDp * 0.38f).coerceIn(14f, 46f).dp,
                offsetX = (diameterDp * 0.38f).dp,
                offsetY = (-diameterDp * 0.35f).dp,
                rotationDeg = 14f
            )
        }

        BadgePopEffect.HEART_POP -> {
            PopOutStickerPin(
                icon = Icons.Rounded.Favorite,
                badgeBg = Color(0xFFFFF1F2),
                iconTint = Color(0xFFE11D48),
                pinSize = (diameterDp * 0.36f).coerceIn(13f, 44f).dp,
                offsetX = (diameterDp * 0.38f).dp,
                offsetY = (-diameterDp * 0.34f).dp,
                rotationDeg = 15f
            )
        }

        BadgePopEffect.STEAM_POP -> {
            // Soft rising cartoon steam curls floating above the top rim
            val steamW = (diameterDp * 0.56f).coerceIn(18f, 76f).dp
            val steamH = (diameterDp * 0.34f).coerceIn(12f, 46f).dp
            Canvas(
                modifier = Modifier
                    .offset(x = (diameterDp * 0.06f).dp, y = (-diameterDp * 0.52f).dp)
                    .size(width = steamW, height = steamH)
            ) {
                val strokeMain = (size.height * 0.16f).coerceIn(2f, 6.5f)
                val strokeOutline = strokeMain + (size.height * 0.14f).coerceIn(2f, 5f)
                val wisps = listOf(0.28f, 0.52f, 0.76f)
                wisps.forEachIndexed { idx, xFrac ->
                    val startX = size.width * xFrac
                    val topY = if (idx == 1) size.height * 0.08f else size.height * 0.24f
                    val botY = size.height * 0.92f
                    val amp = size.width * 0.08f
                    val path = Path().apply {
                        moveTo(startX, botY)
                        cubicTo(
                            startX - amp, (botY + topY) * 0.65f,
                            startX + amp, (botY + topY) * 0.35f,
                            startX, topY
                        )
                    }
                    // Crisp white outer cartoon stroke
                    drawPath(
                        path = path,
                        color = Color.White.copy(alpha = 0.92f),
                        style = Stroke(width = strokeOutline, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                    // Soft warm tinted steam center
                    drawPath(
                        path = path,
                        color = accentTint.copy(alpha = 0.72f),
                        style = Stroke(width = strokeMain, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }
            }
        }

        BadgePopEffect.MOON_POP -> {
            PopOutStickerPin(
                icon = Icons.Rounded.Bedtime,
                badgeBg = Color(0xFFEEF2FF),
                iconTint = Color(0xFF4F46E5),
                pinSize = (diameterDp * 0.37f).coerceIn(13f, 44f).dp,
                offsetX = (diameterDp * 0.38f).dp,
                offsetY = (-diameterDp * 0.35f).dp,
                rotationDeg = -10f
            )
        }

        BadgePopEffect.STAR_POP -> {
            PopOutStickerPin(
                icon = Icons.Rounded.Star,
                badgeBg = Color(0xFFFEF9C3),
                iconTint = Color(0xFFEAB308),
                pinSize = (diameterDp * 0.37f).coerceIn(13f, 44f).dp,
                offsetX = (diameterDp * 0.38f).dp,
                offsetY = (-diameterDp * 0.35f).dp,
                rotationDeg = 14f
            )
        }

        BadgePopEffect.FLAME_POP -> {
            PopOutStickerPin(
                icon = Icons.Rounded.LocalFireDepartment,
                badgeBg = Color(0xFFFFEDD5),
                iconTint = Color(0xFFEA580C),
                pinSize = (diameterDp * 0.38f).coerceIn(14f, 46f).dp,
                offsetX = (diameterDp * 0.38f).dp,
                offsetY = (-diameterDp * 0.35f).dp,
                rotationDeg = 10f
            )
        }

        BadgePopEffect.DROP_POP -> {
            PopOutStickerPin(
                icon = Icons.Rounded.WaterDrop,
                badgeBg = Color(0xFFE0F2FE),
                iconTint = Color(0xFF0284C7),
                pinSize = (diameterDp * 0.36f).coerceIn(13f, 44f).dp,
                offsetX = (diameterDp * 0.38f).dp,
                offsetY = (-diameterDp * 0.34f).dp,
                rotationDeg = 10f
            )
        }

        BadgePopEffect.CHECK_POP -> {
            PopOutStickerPin(
                icon = Icons.Rounded.Check,
                badgeBg = Color(0xFFDCFCE7),
                iconTint = Color(0xFF16A34A),
                pinSize = (diameterDp * 0.36f).coerceIn(13f, 44f).dp,
                offsetX = (diameterDp * 0.38f).dp,
                offsetY = (-diameterDp * 0.34f).dp,
                rotationDeg = 0f
            )
        }

        BadgePopEffect.NOTE_POP -> {
            PopOutStickerPin(
                icon = Icons.Rounded.MusicNote,
                badgeBg = Color(0xFFF3E8FF),
                iconTint = Color(0xFF7C3AED),
                pinSize = (diameterDp * 0.37f).coerceIn(13f, 44f).dp,
                offsetX = (diameterDp * 0.38f).dp,
                offsetY = (-diameterDp * 0.35f).dp,
                rotationDeg = 14f
            )
        }
    }
}

@Composable
private fun CartoonZLetter(
    letter: String,
    fontSizeSp: Float,
    fillColor: Color,
    rotationDeg: Float,
    modifier: Modifier = Modifier
) {
    val outlineStroke = (fontSizeSp * 0.28f).coerceIn(3f, 11f)
    Box(
        modifier = modifier.graphicsLayer { rotationZ = rotationDeg },
        contentAlignment = Alignment.Center
    ) {
        // Soft shadow layer
        Text(
            text = letter,
            color = Color(0xFF0F172A).copy(alpha = 0.22f),
            fontSize = fontSizeSp.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.offset(x = 1.dp, y = 1.8.dp)
        )
        // Thick crisp white cartoon sticker outline
        Text(
            text = letter,
            color = Color.White,
            fontSize = fontSizeSp.sp,
            fontWeight = FontWeight.Black,
            style = TextStyle(
                drawStyle = Stroke(
                    width = outlineStroke,
                    miter = 4f,
                    join = StrokeJoin.Round,
                    cap = StrokeCap.Round
                )
            )
        )
        // Rich foreground fill
        Text(
            text = letter,
            color = fillColor,
            fontSize = fontSizeSp.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun PopOutStickerPin(
    icon: ImageVector,
    badgeBg: Color,
    iconTint: Color,
    pinSize: Dp,
    offsetX: Dp,
    offsetY: Dp,
    rotationDeg: Float
) {
    Box(
        modifier = Modifier
            .offset(x = offsetX, y = offsetY)
            .size(pinSize)
            .graphicsLayer { rotationZ = rotationDeg }
            .shadow(4.dp, CircleShape, clip = false)
            .clip(CircleShape)
            .background(badgeBg)
            .border(1.6.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(pinSize * 0.64f)
        )
    }
}
