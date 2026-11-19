package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.example.ui.theme.LocalMNotesColors
import kotlinx.coroutines.launch
import java.io.File

/**
 * Seamless, continuous, edge-to-edge full-screen backdrop for the Main Screen.
 * Covers behind the status bar, top bar, content, and navigation bar with ZERO dividing line
 * or separate top background strip.
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
                // App default: one continuous, uniform full-screen background with zero top dividing line.
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colors.bgMain)
                )
            }

            preset != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(colors = preset.colors))
                )
            }

            else -> {
                val file = remember(wallpaperUri) { File(wallpaperUri) }
                if (file.exists()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(file)
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

/**
 * Dedicated Settings studio for choosing, previewing, and explicitly applying a custom
 * full-screen wallpaper for the Main Screen. Never auto-applies until user taps Apply.
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

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
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
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (lang == "en") {
                "Choose any photo from your device or a seamless pastel gradient. Preview first — nothing changes on your main screen until you press Apply."
            } else {
                "اختر أي صورة من جهازك أو خلفية باستيل متصلة بالكامل. شاهد المعاينة أولاً ولن يتغير شيء على الشاشة الرئيسية إلا بعد الضغط على تطبيق."
            },
            color = colors.textMuted,
            fontSize = 11.5.sp
        )

        // Seamless Full-Screen Live Preview Mockup (Edge-to-Edge: status bar + top bar + task card)
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

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Simulated seamless status bar + brand header (no top strip!)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "9:41",
                                color = colors.textDark,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(colors.cardBg.copy(alpha = 0.85f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (lang == "en") "Seamless Full-Screen Preview" else "معاينة خلفية متصلة بدون خط علوي",
                                    color = colors.primary,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "M Notes",
                                color = colors.textDark,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(colors.primary)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (lang == "en") "+ Add" else "+ إضافة",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Simulated mini task row floating cleanly over continuous wallpaper
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = colors.cardBg.copy(alpha = 0.92f),
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

        // Pick Own Photo from Device (Zero-Permission Android Photo Picker)
        OutlinedButton(
            onClick = {
                photoPicker.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            enabled = !isImporting,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
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
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (lang == "en") "Choose Photo from Device" else "اختيار خلفية من صور الجهاز",
                    color = colors.primary,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.ExtraBold
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

        // Built-In Seamless Pastel Wallpaper Swatches
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = if (lang == "en") "Seamless Soft Wallpapers:" else "خلفيات ناعمة متصلة جاهزة:",
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
                // Default option
                val isDefaultSel = draftUri.isBlank()
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .width(72.dp)
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
                            .size(44.dp)
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

                MainWallpaperCatalog.presets.forEach { preset ->
                    val presetUri = MainWallpaperCatalog.PREFIX_PRESET + preset.id
                    val selected = draftUri == presetUri
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .width(72.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (selected) colors.primarySoft else colors.cardBg)
                            .border(
                                width = if (selected) 1.8.dp else 1.dp,
                                color = if (selected) colors.primary else colors.borderSoft,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { draftUri = presetUri }
                            .padding(6.dp)
                            .testTag("wallpaper_preset_${preset.id}")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Brush.verticalGradient(preset.colors))
                                .border(1.dp, colors.borderSoft, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (selected) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(18.dp)
                                )
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
            }
        }

        // Subtle Dim Overlay Slider (helps bright device photos keep cards readable)
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
