package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.MainWallpaperCatalog
import com.example.model.MainWallpaperPreset
import com.example.model.WallpaperPatternType
import com.example.ui.theme.LocalMNotesColors
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Seamless, continuous, edge-to-edge full-screen backdrop for the Main Screen.
 * Supports:
 * - Default clean theme background
 * - WhatsApp chat-style patterned wallpapers (doodles, botanical, geometric, linen)
 * - Soft pastel seamless gradients
 * - User's own photos picked from device storage
 */
@Composable
fun MainScreenWallpaperBackdrop(
    wallpaperUri: String,
    dimPercent: Int,
    modifier: Modifier = Modifier
) {
    val colors = LocalMNotesColors.current
    val context = LocalContext.current
    val clampedDim = dimPercent.coerceIn(0, 45)
    val preset = remember(wallpaperUri) { MainWallpaperCatalog.presetForUri(wallpaperUri) }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            wallpaperUri.isBlank() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colors.bgMain)
                )
            }

            preset != null -> {
                WallpaperPresetCanvasSurface(
                    preset = preset,
                    tileScale = 1f,
                    modifier = Modifier.fillMaxSize()
                )
            }

            else -> {
                val file = remember(wallpaperUri) { File(wallpaperUri) }
                if (file.exists()) {
                    val cacheKey = "${file.absolutePath}:${file.lastModified()}"
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(file)
                            .memoryCacheKey(cacheKey)
                            .diskCacheKey(cacheKey)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    MNotesPhotoContent(
                        imgUri = wallpaperUri,
                        contentDescription = null,
                        useBannerDefaultIfBlank = false,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        if (wallpaperUri.isNotBlank() && clampedDim > 0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = clampedDim / 100f))
            )
        }
    }
}

@Composable
private fun WallpaperPresetCanvasSurface(
    preset: MainWallpaperPreset,
    tileScale: Float = 1f,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.background(Brush.verticalGradient(colors = preset.colors))
    ) {
        if (preset.pattern != WallpaperPatternType.NONE) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val ink = preset.patternTint.copy(alpha = preset.patternAlpha)
                when (preset.pattern) {
                    WallpaperPatternType.WHATSAPP_DOODLE,
                    WallpaperPatternType.WHATSAPP_DARK_DOODLE -> {
                        drawWhatsAppChatDoodles(ink = ink, tileScale = tileScale)
                    }
                    WallpaperPatternType.BOTANICAL_LEAVES -> {
                        drawBotanicalLeavesPattern(ink = ink, tileScale = tileScale)
                    }
                    WallpaperPatternType.SOFT_GEOMETRIC -> {
                        drawGeometricChatPattern(ink = ink, tileScale = tileScale)
                    }
                    WallpaperPatternType.LINEN_WEAVE -> {
                        drawLinenWeavePattern(ink = ink, tileScale = tileScale)
                    }
                    WallpaperPatternType.NONE -> Unit
                }
            }
        }
    }
}

private fun DrawScope.drawWhatsAppChatDoodles(ink: Color, tileScale: Float) {
    val step = (64.dp.toPx() * tileScale).coerceAtLeast(18f)
    val iconSize = step * 0.34f
    val strokeW = (1.5.dp.toPx() * tileScale).coerceAtLeast(0.8f)
    val stroke = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)

    val cols = (size.width / step).toInt() + 2
    val rows = (size.height / step).toInt() + 2

    for (r in 0..rows) {
        for (c in 0..cols) {
            val hash = (r * 31 + c * 17) and 0x7FFFFFFF
            val jitterX = ((hash % 11) - 5) * step * 0.035f
            val jitterY = (((hash / 7) % 11) - 5) * step * 0.035f
            val stagger = if (r % 2 == 1) step * 0.48f else 0f
            val cx = c * step + stagger + jitterX
            val cy = r * step + jitterY
            val angle = ((hash % 7) - 3) * 11f

            rotate(degrees = angle, pivot = Offset(cx, cy)) {
                when (hash % 8) {
                    0 -> { // Chat bubble
                        drawRoundRect(
                            color = ink,
                            topLeft = Offset(cx - iconSize * 0.52f, cy - iconSize * 0.38f),
                            size = Size(iconSize * 1.04f, iconSize * 0.72f),
                            cornerRadius = CornerRadius(iconSize * 0.24f, iconSize * 0.24f),
                            style = stroke
                        )
                        drawLine(ink, Offset(cx - iconSize * 0.25f, cy - iconSize * 0.08f), Offset(cx + iconSize * 0.25f, cy - iconSize * 0.08f), strokeW, StrokeCap.Round)
                        drawLine(ink, Offset(cx - iconSize * 0.25f, cy + iconSize * 0.12f), Offset(cx + iconSize * 0.10f, cy + iconSize * 0.12f), strokeW, StrokeCap.Round)
                    }
                    1 -> { // Heart
                        val p = Path().apply {
                            moveTo(cx, cy + iconSize * 0.38f)
                            cubicTo(cx - iconSize * 0.65f, cy, cx - iconSize * 0.48f, cy - iconSize * 0.48f, cx, cy - iconSize * 0.14f)
                            cubicTo(cx + iconSize * 0.48f, cy - iconSize * 0.48f, cx + iconSize * 0.65f, cy, cx, cy + iconSize * 0.38f)
                            close()
                        }
                        drawPath(p, color = ink, style = stroke)
                    }
                    2 -> { // 5-point Star
                        val p = Path()
                        for (i in 0 until 10) {
                            val rad = if (i % 2 == 0) iconSize * 0.48f else iconSize * 0.22f
                            val a = -PI / 2.0 + i * PI / 5.0
                            val px = cx + (cos(a) * rad).toFloat()
                            val py = cy + (sin(a) * rad).toFloat()
                            if (i == 0) p.moveTo(px, py) else p.lineTo(px, py)
                        }
                        p.close()
                        drawPath(p, color = ink, style = stroke)
                    }
                    3 -> { // Dumbbell
                        drawLine(ink, Offset(cx - iconSize * 0.42f, cy), Offset(cx + iconSize * 0.42f, cy), strokeW * 1.15f, StrokeCap.Round)
                        drawRoundRect(ink, Offset(cx - iconSize * 0.56f, cy - iconSize * 0.28f), Size(iconSize * 0.18f, iconSize * 0.56f), CornerRadius(3f, 3f), style = stroke)
                        drawRoundRect(ink, Offset(cx + iconSize * 0.38f, cy - iconSize * 0.28f), Size(iconSize * 0.18f, iconSize * 0.56f), CornerRadius(3f, 3f), style = stroke)
                    }
                    4 -> { // Coffee cup
                        drawRoundRect(ink, Offset(cx - iconSize * 0.35f, cy - iconSize * 0.22f), Size(iconSize * 0.62f, iconSize * 0.58f), CornerRadius(iconSize * 0.14f, iconSize * 0.14f), style = stroke)
                        drawArc(ink, startAngle = -70f, sweepAngle = 140f, useCenter = false, topLeft = Offset(cx + iconSize * 0.18f, cy - iconSize * 0.10f), size = Size(iconSize * 0.28f, iconSize * 0.32f), style = stroke)
                    }
                    5 -> { // Smiley face
                        drawCircle(ink, radius = iconSize * 0.44f, center = Offset(cx, cy), style = stroke)
                        drawCircle(ink, radius = strokeW * 0.9f, center = Offset(cx - iconSize * 0.15f, cy - iconSize * 0.10f))
                        drawCircle(ink, radius = strokeW * 0.9f, center = Offset(cx + iconSize * 0.15f, cy - iconSize * 0.10f))
                        drawArc(ink, startAngle = 20f, sweepAngle = 140f, useCenter = false, topLeft = Offset(cx - iconSize * 0.24f, cy - iconSize * 0.12f), size = Size(iconSize * 0.48f, iconSize * 0.36f), style = stroke)
                    }
                    6 -> { // Crescent moon + dot
                        drawArc(ink, startAngle = 40f, sweepAngle = 250f, useCenter = false, topLeft = Offset(cx - iconSize * 0.42f, cy - iconSize * 0.42f), size = Size(iconSize * 0.84f, iconSize * 0.84f), style = stroke)
                        drawCircle(ink, radius = strokeW * 1.1f, center = Offset(cx + iconSize * 0.26f, cy - iconSize * 0.24f))
                    }
                    else -> { // Sparkle diamond
                        val p = Path().apply {
                            moveTo(cx, cy - iconSize * 0.48f)
                            quadraticTo(cx + iconSize * 0.08f, cy - iconSize * 0.08f, cx + iconSize * 0.48f, cy)
                            quadraticTo(cx + iconSize * 0.08f, cy + iconSize * 0.08f, cx, cy + iconSize * 0.48f)
                            quadraticTo(cx - iconSize * 0.08f, cy + iconSize * 0.08f, cx - iconSize * 0.48f, cy)
                            quadraticTo(cx - iconSize * 0.08f, cy - iconSize * 0.08f, cx, cy - iconSize * 0.48f)
                            close()
                        }
                        drawPath(p, color = ink, style = stroke)
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawBotanicalLeavesPattern(ink: Color, tileScale: Float) {
    val step = (68.dp.toPx() * tileScale).coerceAtLeast(20f)
    val leafR = step * 0.30f
    val strokeW = (1.5.dp.toPx() * tileScale).coerceAtLeast(0.8f)
    val stroke = Stroke(width = strokeW, cap = StrokeCap.Round)
    val cols = (size.width / step).toInt() + 2
    val rows = (size.height / step).toInt() + 2

    for (r in 0..rows) {
        for (c in 0..cols) {
            val stagger = if (r % 2 == 1) step * 0.5f else 0f
            val cx = c * step + stagger
            val cy = r * step
            val angle = (((r * 19 + c * 13) % 8) - 4) * 18f
            rotate(angle, Offset(cx, cy)) {
                val p = Path().apply {
                    moveTo(cx, cy - leafR)
                    quadraticTo(cx + leafR * 0.85f, cy, cx, cy + leafR)
                    quadraticTo(cx - leafR * 0.85f, cy, cx, cy - leafR)
                    close()
                }
                drawPath(p, color = ink, style = stroke)
                drawLine(ink, Offset(cx, cy - leafR * 0.72f), Offset(cx, cy + leafR * 0.88f), strokeW, StrokeCap.Round)
            }
        }
    }
}

private fun DrawScope.drawGeometricChatPattern(ink: Color, tileScale: Float) {
    val step = (46.dp.toPx() * tileScale).coerceAtLeast(16f)
    val strokeW = (1.2.dp.toPx() * tileScale).coerceAtLeast(0.7f)
    val stroke = Stroke(width = strokeW)
    val cols = (size.width / step).toInt() + 2
    val rows = (size.height / step).toInt() + 2

    for (r in 0..rows) {
        for (c in 0..cols) {
            val cx = c * step + if (r % 2 == 1) step * 0.5f else 0f
            val cy = r * step
            val half = step * 0.36f
            val diamond = Path().apply {
                moveTo(cx, cy - half)
                lineTo(cx + half, cy)
                lineTo(cx, cy + half)
                lineTo(cx - half, cy)
                close()
            }
            drawPath(diamond, color = ink, style = stroke)
            drawCircle(ink, radius = strokeW * 1.4f, center = Offset(cx, cy))
        }
    }
}

private fun DrawScope.drawLinenWeavePattern(ink: Color, tileScale: Float) {
    val step = (16.dp.toPx() * tileScale).coerceAtLeast(6f)
    val strokeW = (0.9.dp.toPx() * tileScale).coerceAtLeast(0.6f)
    var y = 0f
    while (y < size.height + step) {
        drawLine(ink, Offset(0f, y), Offset(size.width, y), strokeW)
        y += step
    }
    var x = 0f
    while (x < size.width + step) {
        drawLine(ink.copy(alpha = ink.alpha * 0.7f), Offset(x, 0f), Offset(x, size.height), strokeW)
        x += step
    }
}

/**
 * Dedicated Settings studio for choosing, previewing, and explicitly applying a custom
 * full-screen wallpaper for the Main Screen (Device Photo, WhatsApp Chat-Style, or Soft Pastel).
 */
@Composable
fun MainWallpaperStudio(
    appliedWallpaperUri: String,
    appliedDimPercent: Int,
    lang: String,
    onPickDeviceImage: suspend (Uri) -> Result<String>,
    onApply: (uri: String, dimPercent: Int) -> Unit,
    onReset: () -> Unit
) {
    val colors = LocalMNotesColors.current
    val scope = rememberCoroutineScope()

    var draftUri by remember(appliedWallpaperUri) { mutableStateOf(appliedWallpaperUri) }
    var draftDim by remember(appliedDimPercent) { mutableIntStateOf(appliedDimPercent.coerceIn(0, 45)) }
    var isImporting by remember { mutableStateOf(false) }
    var importError by remember { mutableStateOf<String?>(null) }

    val hasUnappliedChanges = draftUri != appliedWallpaperUri || draftDim != appliedDimPercent

    fun handlePickedUri(uri: Uri?) {
        if (uri == null) return
        isImporting = true
        importError = null
        scope.launch {
            val res = onPickDeviceImage(uri)
            isImporting = false
            res.onSuccess { savedPath ->
                draftUri = savedPath
            }.onFailure { err ->
                importError = err.localizedMessage
                    ?: if (lang == "en") "Could not load selected photo." else "تعذر تحميل الصورة المختارة."
            }
        }
    }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> handlePickedUri(uri) }

    val fileImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> handlePickedUri(uri) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (lang == "en") {
                "Pick any photo from your device, a WhatsApp chat-style patterned wallpaper, or a soft pastel gradient. Nothing applies until you tap Apply."
            } else {
                "اختر أي صورة من جهازك أو خلفية بنمط محادثات واتساب المزخرفة أو خلفية ناعمة. لن يتغير شيء إلا بعد الضغط على تطبيق."
            },
            color = colors.textMuted,
            fontSize = 11.5.sp
        )

        // Seamless Full-Screen Live Preview Mockup
        Surface(
            shape = RoundedCornerShape(24.dp),
            shadowElevation = 3.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .border(1.5.dp, colors.primary.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
                .testTag("main_wallpaper_preview_card")
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                MainScreenWallpaperBackdrop(
                    wallpaperUri = draftUri,
                    dimPercent = draftDim,
                    modifier = Modifier.fillMaxSize()
                )

                val previewPreset = remember(draftUri) { MainWallpaperCatalog.presetForUri(draftUri) }
                val previewTextColor = if (previewPreset?.isDark == true) Color.White else colors.textDark

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "9:41",
                                color = previewTextColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(colors.cardBg.copy(alpha = 0.88f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (lang == "en") "Live Full-Screen Preview" else "معاينة خلفية الشاشة الكاملة",
                                    color = colors.primary,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        Text(
                            text = "M Notes",
                            color = previewTextColor,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = colors.cardBg.copy(alpha = 0.94f),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, colors.borderSoft, RoundedCornerShape(18.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(colors.primarySoft),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Wallpaper,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (lang == "en") "Morning Workout" else "تمرين الصباح",
                                    color = colors.textDark,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = if (lang == "en") "Daily • 08:00" else "يومي • 08:00",
                                    color = colors.textMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Device Gallery Picker (Zero-Permission Photo Picker + Files Fallback)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    photoPicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                enabled = !isImporting,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1.35f)
                    .height(48.dp)
                    .testTag("pick_device_wallpaper_button")
            ) {
                if (isImporting) {
                    CircularProgressIndicator(
                        color = colors.primary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.AddPhotoAlternate,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (lang == "en") "From Gallery" else "من صور الجهاز",
                        color = colors.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            OutlinedButton(
                onClick = { fileImagePicker.launch("image/*") },
                enabled = !isImporting,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("pick_file_wallpaper_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.FolderOpen,
                    contentDescription = null,
                    tint = colors.textDark,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    text = if (lang == "en") "Files" else "الملفات",
                    color = colors.textDark,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (!importError.isNullOrBlank()) {
            Text(
                text = importError!!,
                color = Color(0xFFDC2626),
                fontSize = 11.sp
            )
        }

        // Section 1: WhatsApp Chat-Style Wallpapers (Doodles, Textures, Dark & Light Chat Themes)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = if (lang == "en") {
                    "WhatsApp Chat-Style Wallpapers (${MainWallpaperCatalog.whatsappPresets.size}):"
                } else {
                    "خلفيات بنمط محادثات واتساب (${MainWallpaperCatalog.whatsappPresets.size} خلفية):"
                },
                color = colors.textDark,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MainWallpaperCatalog.whatsappPresets.forEach { preset ->
                    val presetUri = MainWallpaperCatalog.PREFIX_PRESET + preset.id
                    val selected = draftUri == presetUri
                    WallpaperSwatchTile(
                        preset = preset,
                        selected = selected,
                        lang = lang,
                        onClick = { draftUri = presetUri }
                    )
                }
            }
        }

        // Section 2: Built-In Seamless Soft Wallpapers
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = if (lang == "en") "Seamless Soft Wallpapers:" else "خلفيات ناعمة متصلة:",
                color = colors.textDark,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isDefaultSel = draftUri.isBlank()
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .width(76.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isDefaultSel) colors.primarySoft else colors.cardBg)
                        .border(
                            width = if (isDefaultSel) 1.8.dp else 1.dp,
                            color = if (isDefaultSel) colors.primary else colors.borderSoft,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { draftUri = "" }
                        .padding(6.dp)
                        .testTag("wallpaper_preset_default")
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.bgMain)
                            .border(1.dp, colors.borderSoft, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isDefaultSel) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Text(
                        text = if (lang == "en") "Default" else "الافتراضي",
                        color = if (isDefaultSel) colors.primary else colors.textDark,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                MainWallpaperCatalog.softPresets.forEach { preset ->
                    val presetUri = MainWallpaperCatalog.PREFIX_PRESET + preset.id
                    val selected = draftUri == presetUri
                    WallpaperSwatchTile(
                        preset = preset,
                        selected = selected,
                        lang = lang,
                        onClick = { draftUri = presetUri }
                    )
                }
            }
        }

        // Subtle Dim Overlay Slider
        if (draftUri.isNotBlank()) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = colors.bgMain,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.borderSoft, RoundedCornerShape(14.dp))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (lang == "en") "Wallpaper Dimming (Readability)" else "تعتيم لطيف للخلفية (لوضوح الكلام)",
                            color = colors.textDark,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$draftDim%",
                            color = colors.primary,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Slider(
                        value = draftDim.toFloat(),
                        onValueChange = { draftDim = it.toInt().coerceIn(0, 45) },
                        valueRange = 0f..45f,
                        colors = SliderDefaults.colors(
                            thumbColor = colors.primary,
                            activeTrackColor = colors.primary
                        ),
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("wallpaper_dim_slider")
                    )
                }
            }
        }

        // Explicit Apply & Reset Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    draftUri = ""
                    draftDim = 0
                    onReset()
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(0.9f)
                    .height(48.dp)
                    .testTag("reset_main_wallpaper_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = null,
                    tint = colors.textDark,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = if (lang == "en") "Reset" else "إعادة للوضع الأصلي",
                    color = colors.textDark,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Button(
                onClick = { onApply(draftUri, draftDim) },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                modifier = Modifier
                    .weight(1.3f)
                    .height(48.dp)
                    .testTag("apply_main_wallpaper_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = if (hasUnappliedChanges) {
                        if (lang == "en") "Apply Wallpaper" else "تطبيق الخلفية الآن"
                    } else {
                        if (lang == "en") "Save Wallpaper" else "حفظ الخلفية"
                    },
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun WallpaperSwatchTile(
    preset: MainWallpaperPreset,
    selected: Boolean,
    lang: String,
    onClick: () -> Unit
) {
    val colors = LocalMNotesColors.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .width(76.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) colors.primarySoft else colors.cardBg)
            .border(
                width = if (selected) 1.8.dp else 1.dp,
                color = if (selected) colors.primary else colors.borderSoft,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(6.dp)
            .testTag("wallpaper_preset_${preset.id}")
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, colors.borderSoft, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            WallpaperPresetCanvasSurface(
                preset = preset,
                tileScale = 0.34f,
                modifier = Modifier.fillMaxSize()
            )
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(colors.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
        Text(
            text = if (lang == "en") preset.nameEn else preset.nameAr,
            color = if (selected) colors.primary else colors.textDark,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
