package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.ui.theme.LocalMNotesColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.roundToInt

val OrganicBlobShape: Shape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    moveTo(w * 0.58f, 0f)
    cubicTo(w * 0.92f, 0f, w, h * 0.25f, w, h * 0.52f)
    cubicTo(w, h * 0.85f, w * 0.72f, h, w * 0.45f, h)
    cubicTo(w * 0.14f, h, 0f, h * 0.78f, 0f, h * 0.46f)
    cubicTo(0f, h * 0.14f, w * 0.24f, 0f, w * 0.58f, 0f)
    close()
}

val SoftPebbleShape: Shape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    moveTo(w * 0.46f, 0f)
    cubicTo(w * 0.86f, 0f, w, h * 0.18f, w * 0.96f, h * 0.54f)
    cubicTo(w * 0.92f, h * 0.92f, w * 0.68f, h, w * 0.40f, h * 0.97f)
    cubicTo(w * 0.08f, h * 0.94f, 0f, h * 0.70f, w * 0.03f, h * 0.38f)
    cubicTo(w * 0.06f, h * 0.10f, w * 0.20f, 0f, w * 0.46f, 0f)
    close()
}

val SoftHexagonShape: Shape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    moveTo(w * 0.5f, 0f)
    cubicTo(w * 0.64f, 0f, w * 0.92f, h * 0.14f, w * 0.96f, h * 0.28f)
    cubicTo(w, h * 0.42f, w, h * 0.58f, w * 0.96f, h * 0.72f)
    cubicTo(w * 0.92f, h * 0.86f, w * 0.64f, h, w * 0.5f, h)
    cubicTo(w * 0.36f, h, w * 0.08f, h * 0.86f, w * 0.04f, h * 0.72f)
    cubicTo(0f, h * 0.58f, 0f, h * 0.42f, w * 0.04f, h * 0.28f)
    cubicTo(w * 0.08f, h * 0.14f, w * 0.36f, 0f, w * 0.5f, 0f)
    close()
}

fun shapeForPhoto(shapeId: String): Shape = when (shapeId) {
    "ios" -> RoundedCornerShape(14.dp)
    "squircle" -> RoundedCornerShape(28)
    "blob", "organic_blob" -> OrganicBlobShape
    "pebble" -> SoftPebbleShape
    "soft_hex" -> SoftHexagonShape
    "strip" -> RectangleShape
    else -> CircleShape
}

fun bannerImageDp(sizeId: String, customDp: Int? = null): Dp {
    if (customDp != null && customDp > 0) return customDp.coerceIn(28, 170).dp
    return when (sizeId) {
        "small" -> 42.dp
        "large" -> 66.dp
        else -> 52.dp
    }
}

fun taskImageDp(sizeId: String, customDp: Int? = null): Dp {
    if (customDp != null && customDp > 0) return customDp.coerceIn(28, 150).dp
    return when (sizeId) {
        "small" -> 38.dp
        "large" -> 58.dp
        else -> 46.dp
    }
}

private fun builtInDrawableForName(name: String): Int? = when (name) {
    "img_3d_elder_man" -> R.drawable.img_3d_elder_man_1795021150883
    "img_3d_elder_beige" -> R.drawable.img_3d_elder_beige_1795021564309
    "img_3d_bald_man" -> R.drawable.img_3d_bald_man_1795021150430
    "img_3d_bald_brown" -> R.drawable.img_3d_bald_brown_1795021591352
    "img_3d_haired_man" -> R.drawable.img_3d_haired_man_1795021151702
    "img_3d_haired_mint" -> R.drawable.img_3d_haired_mint_1795021612096
    "img_3d_child" -> R.drawable.img_3d_child_1795021152915
    "img_3d_child_cream" -> R.drawable.img_3d_child_cream_1795021589727
    "img_3d_woman" -> R.drawable.img_3d_woman_1795021154226
    "img_3d_woman_beige" -> R.drawable.img_3d_woman_beige_1795021590044
    "img_3d_medieval_bald" -> R.drawable.img_3d_medieval_bald_1795022190347
    "img_3d_jahiliyyah" -> R.drawable.img_3d_jahiliyyah_1795022191921
    "img_3d_ancient_fruits" -> R.drawable.img_3d_ancient_fruits_1795022223609
    "img_3d_ancient_bread_grain" -> R.drawable.img_3d_ancient_bread_grain_1795022221497
    "img_3d_gym_workout" -> R.drawable.img_3d_gym_workout_1795021155953
    "img_3d_gym_chest" -> R.drawable.img_3d_gym_chest_1795021564566
    "img_3d_gym_back" -> R.drawable.img_3d_gym_back_1795021563371
    "img_3d_gym_legs" -> R.drawable.img_3d_gym_legs_1795021566532
    "img_3d_gym_legpress" -> R.drawable.img_3d_gym_legpress_1795022190947
    "img_3d_gym_lunges" -> R.drawable.img_3d_gym_lunges_1795022188475
    "img_3d_gym_arms" -> R.drawable.img_3d_gym_arms_1795021588933
    "img_3d_gym_shoulders" -> R.drawable.img_3d_gym_shoulders_1795021611562
    "img_sleepy_badge" -> R.drawable.img_sleepy_badge_1795028747325
    "img_medieval_fruits" -> R.drawable.img_medieval_fruits_1795028748903
    "img_medieval_veggies" -> R.drawable.img_medieval_veggies_1795028749693
    "img_gym_gear" -> R.drawable.img_gym_gear_1795028749019
    "img_3d_cardio_activities" -> R.drawable.img_3d_cardio_activities_1795097534637
    "img_3d_strength_exercises" -> R.drawable.img_3d_strength_exercises_1795097533008
    "img_cardio_gym_machines" -> R.drawable.img_cardio_gym_machines_1795097532792
    // Expanded Pixar 3D Exercise Library Packs (45 new exercises)
    "img_3d_chest_pack" -> R.drawable.img_3d_chest_pack_1795101792319
    "img_3d_back_pack" -> R.drawable.img_3d_back_pack_1795101793333
    "img_3d_legs_pack" -> R.drawable.img_3d_legs_pack_1795101792103
    "img_3d_abs_core_pack" -> R.drawable.img_3d_abs_core_pack_1795101792740
    "img_3d_shoulders_arms_recovery_pack" -> R.drawable.img_3d_shoulders_arms_recovery_pack_1795101792012
    // Pixar 3D Male Character Floating Icon Packs (36 distinct characters)
    "img_pixar_chars_emotions_1" -> R.drawable.img_pixar_chars_emotions_1_1795106628637
    "img_pixar_chars_fitness_1" -> R.drawable.img_pixar_chars_fitness_1_1795106627995
    "img_pixar_chars_vibes_1" -> R.drawable.img_pixar_chars_vibes_1_1795106627639
    "img_pixar_chars_poses_1" -> R.drawable.img_pixar_chars_poses_1_1795106628868
    // Pixar 3D Glossy Checkmark & X Medallions Pack
    "img_pixar_check_x_pack" -> R.drawable.img_pixar_check_x_pack_1795106627433
    else -> null
}

/** Sheets that contain small bottom text captions inside each 3x3 cell; trim bottom 12% so circular avatars show pure artwork. */
private fun bottomCaptionTrimFraction(resId: Int): Float = when (resId) {
    R.drawable.img_3d_chest_pack_1795101792319,
    R.drawable.img_3d_legs_pack_1795101792103 -> 0.13f
    R.drawable.img_pixar_check_x_pack_1795106627433 -> 0.02f
    else -> 0.04f
}

internal fun parseBuiltInUri(uri: String): Pair<Int, Int?>? {
    if (!uri.startsWith("res://")) return null
    val raw = uri.removePrefix("res://")
    val hashIdx = raw.indexOf('#')
    val name = if (hashIdx >= 0) raw.substring(0, hashIdx) else raw
    val tileIdx = if (hashIdx >= 0) raw.substring(hashIdx + 1).toIntOrNull() else null
    val resId = builtInDrawableForName(name) ?: return null
    return resId to tileIdx
}

internal object GridTileBitmapCache {
    private val cache = LruCache<String, ImageBitmap>(180)
    private val rawBitmapCache = LruCache<String, Bitmap>(180)
    private val sheetLocks = ConcurrentHashMap<Int, Any>()

    fun get(key: String): ImageBitmap? = cache.get(key)

    fun getAndroidBitmap(key: String): Bitmap? = rawBitmapCache.get(key)

    /**
     * Decodes a 3x3 sheet ONCE per drawable resource and slices all 9 tiles into cache in one pass.
     * Eliminates 9x duplicate JPEG decoding and prevents lag when browsing or switching images.
     */
    private fun decodeAndCacheEntireSheet(context: android.content.Context, resId: Int) {
        val lock = sheetLocks.computeIfAbsent(resId) { Any() }
        synchronized(lock) {
            if (cache.get("$resId#0") != null && cache.get("$resId#8") != null) return
            runCatching {
                val opts = BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.ARGB_8888 }
                val full = BitmapFactory.decodeResource(context.resources, resId, opts) ?: return
                val cellW = full.width / 3f
                val cellH = full.height / 3f
                val marginX = cellW * 0.045f
                val marginTop = cellH * 0.035f
                val marginBottom = cellH * bottomCaptionTrimFraction(resId)
                val targetMax = 300

                for (tileIdx in 0..8) {
                    val col = tileIdx % 3
                    val row = tileIdx / 3
                    val availW = (cellW - marginX * 2f).coerceAtLeast(1f)
                    val availH = (cellH - marginTop - marginBottom).coerceAtLeast(1f)
                    val side = minOf(availW, availH)
                    val cropX = (col * cellW + (cellW - side) * 0.5f).roundToInt().coerceIn(0, full.width - 1)
                    val cropY = (row * cellH + marginTop + (availH - side) * 0.5f).roundToInt().coerceIn(0, full.height - 1)
                    val cropSide = side.roundToInt().coerceAtLeast(1)
                        .coerceAtMost(minOf(full.width - cropX, full.height - cropY))
                    val cell = Bitmap.createBitmap(full, cropX, cropY, cropSide, cropSide)
                    val finalBmp = if (cell.width > targetMax || cell.height > targetMax) {
                        val scaled = Bitmap.createScaledBitmap(cell, targetMax, targetMax, true)
                        if (scaled != cell) cell.recycle()
                        scaled
                    } else {
                        cell
                    }
                    val tileKey = "$resId#$tileIdx"
                    rawBitmapCache.put(tileKey, finalBmp)
                    cache.put(tileKey, finalBmp.asImageBitmap())
                }
                full.recycle()
            }
        }
    }

    fun getOrLoad(context: android.content.Context, key: String, resId: Int, tileIndex: Int): ImageBitmap? {
        cache.get(key)?.let { return it }
        decodeAndCacheEntireSheet(context, resId)
        return cache.get(key)
    }

    fun getOrLoadAndroidBitmap(context: android.content.Context, resId: Int, tileIndex: Int): Bitmap? {
        val key = "$resId#$tileIndex"
        rawBitmapCache.get(key)?.let { return it }
        decodeAndCacheEntireSheet(context, resId)
        return rawBitmapCache.get(key)
    }

    fun prewarmUri(context: android.content.Context, uri: String) {
        val parsed = parseBuiltInUri(uri) ?: return
        val resId = parsed.first
        val tileIndex = parsed.second ?: return
        if (cache.get("$resId#$tileIndex") != null) return
        decodeAndCacheEntireSheet(context, resId)
    }
}

/**
 * Decodes a task's image URI (built-in single, built-in 3x3 tile, or local file) into a compact
 * Android [Bitmap] suitable for a lock-screen notification LargeIcon.
 */
fun decodeTaskPhotoBitmap(context: android.content.Context, imgUri: String): Bitmap? {
    if (imgUri.isBlank()) return null
    val parsed = parseBuiltInUri(imgUri)
    return runCatching {
        when {
            parsed != null && parsed.second != null -> {
                GridTileBitmapCache.getOrLoadAndroidBitmap(context, parsed.first, parsed.second!!)
            }
            parsed != null -> {
                val opts = BitmapFactory.Options().apply { inSampleSize = 2 }
                BitmapFactory.decodeResource(context.resources, parsed.first, opts)
            }
            File(imgUri).exists() -> {
                val opts = BitmapFactory.Options().apply { inSampleSize = 2 }
                BitmapFactory.decodeFile(imgUri, opts)
            }
            else -> null
        }
    }.getOrNull()
}

@Composable
fun MNotesPhotoContent(
    imgUri: String,
    useBannerDefaultIfBlank: Boolean,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val parsedBuiltIn = parseBuiltInUri(imgUri)

    when {
        parsedBuiltIn != null && parsedBuiltIn.second != null -> {
            val resId = parsedBuiltIn.first
            val tileIndex = parsedBuiltIn.second!!
            val cacheKey = "$resId#$tileIndex"
            val immediateCached = GridTileBitmapCache.get(cacheKey)
            // Tag produceState with cacheKey so switching from one tile image to another ALWAYS
            // resets and loads the new tile immediately (never retains the previous task's bitmap).
            val loadedPair by produceState<Pair<String, ImageBitmap?>>(
                initialValue = cacheKey to immediateCached,
                key1 = cacheKey
            ) {
                val existing = GridTileBitmapCache.get(cacheKey)
                if (existing != null) {
                    value = cacheKey to existing
                } else {
                    value = cacheKey to null
                    val decoded = withContext(Dispatchers.IO) {
                        GridTileBitmapCache.getOrLoad(context, cacheKey, resId, tileIndex)
                    }
                    value = cacheKey to decoded
                }
            }
            val bmp = immediateCached ?: if (loadedPair.first == cacheKey) loadedPair.second else null
            if (bmp != null) {
                Image(
                    bitmap = bmp,
                    contentDescription = contentDescription,
                    contentScale = ContentScale.Crop,
                    modifier = modifier
                )
            } else {
                val colors = LocalMNotesColors.current
                Box(modifier = modifier.background(colors.primarySoft))
            }
        }
        parsedBuiltIn != null -> {
            Image(
                painter = painterResource(id = parsedBuiltIn.first),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = modifier
            )
        }
        imgUri == "default" || (imgUri.isBlank() && useBannerDefaultIfBlank) -> {
            Image(
                painter = painterResource(id = R.drawable.img_banner_default),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = modifier
            )
        }
        imgUri.isNotBlank() && File(imgUri).exists() -> {
            val file = File(imgUri)
            val cacheKey = "${file.absolutePath}:${file.lastModified()}"
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(file)
                    .memoryCacheKey(cacheKey)
                    .diskCacheKey(cacheKey)
                    .crossfade(false)
                    .build(),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = modifier
            )
        }
        imgUri.isNotBlank() -> {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imgUri)
                    .memoryCacheKey(imgUri)
                    .diskCacheKey(imgUri)
                    .crossfade(false)
                    .build(),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = modifier
            )
        }
        else -> {
            val colors = LocalMNotesColors.current
            Box(
                modifier = modifier.background(colors.primarySoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Image,
                    contentDescription = contentDescription,
                    tint = colors.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun MediaAvatarSlot(
    imgUri: String,
    size: Dp,
    shapeId: String,
    useBannerDefaultIfBlank: Boolean = false,
    isActiveGlow: Boolean = false,
    isResting: Boolean = false,
    contentDescription: String? = null,
    onClick: (() -> Unit)? = null
) {
    val colors = LocalMNotesColors.current
    val outerShape = shapeForPhoto(shapeId)
    val clickMod = if (onClick != null) {
        Modifier
            .minimumInteractiveComponentSize()
            .clickable(onClick = onClick)
    } else Modifier

    val glowAlpha = if (isActiveGlow && !isResting) {
        val transition = rememberInfiniteTransition(label = "active_task_glow")
        val anim by transition.animateFloat(
            initialValue = 0.28f,
            targetValue = 0.72f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1350, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glow_pulse"
        )
        anim
    } else {
        0f
    }

    Box(contentAlignment = Alignment.Center) {
        if (isActiveGlow && !isResting) {
            val haloShape = if (shapeId == "strip") RoundedCornerShape(10.dp) else outerShape
            Box(
                modifier = Modifier
                    .size(size + 8.dp)
                    .clip(haloShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                colors.primary.copy(alpha = glowAlpha),
                                colors.accent.copy(alpha = glowAlpha * 0.45f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        val avatarAlpha = if (isResting) 0.76f else 1f
        if (shapeId == "ring") {
            val ringBrush = if (isResting) {
                Brush.linearGradient(listOf(Color(0xFF94A3B8), Color(0xFFCBD5E1)))
            } else {
                Brush.linearGradient(listOf(colors.primary, colors.accent))
            }
            Box(
                modifier = Modifier
                    .size(size)
                    .alpha(avatarAlpha)
                    .shadow(if (isActiveGlow && !isResting) 7.dp else 4.dp, CircleShape, clip = false)
                    .clip(CircleShape)
                    .background(ringBrush)
                    .then(clickMod)
                    .padding(2.5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .border(2.dp, Color.White, CircleShape)
                        .background(colors.primarySoft),
                    contentAlignment = Alignment.Center
                ) {
                    MNotesPhotoContent(
                        imgUri = imgUri,
                        useBannerDefaultIfBlank = useBannerDefaultIfBlank,
                        contentDescription = contentDescription,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        } else if (shapeId == "strip") {
            Box(
                modifier = Modifier
                    .size(size + 10.dp)
                    .alpha(avatarAlpha)
                    .background(colors.primarySoft)
                    .then(clickMod),
                contentAlignment = Alignment.Center
            ) {
                MNotesPhotoContent(
                    imgUri = imgUri,
                    useBannerDefaultIfBlank = useBannerDefaultIfBlank,
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(size)
                    .alpha(avatarAlpha)
                    .shadow(if (isActiveGlow && !isResting) 7.dp else 4.dp, outerShape, clip = false)
                    .clip(outerShape)
                    .border(
                        width = if (isActiveGlow && !isResting) 2.5.dp else 2.dp,
                        color = if (isActiveGlow && !isResting) colors.primary else Color.White,
                        shape = outerShape
                    )
                    .background(colors.primarySoft)
                    .then(clickMod),
                contentAlignment = Alignment.Center
            ) {
                MNotesPhotoContent(
                    imgUri = imgUri,
                    useBannerDefaultIfBlank = useBannerDefaultIfBlank,
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

