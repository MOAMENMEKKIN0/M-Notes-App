package com.example.ui.components

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
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.localization.AppStrings
import com.example.model.RepeatMode
import com.example.model.SmartTask
import com.example.ui.theme.LocalMNotesColors
import kotlin.math.roundToInt

/**
 * Dedicated Settings customization studio for the Reminder Screen background & subtle scattered doodles.
 *
 * Strictly enforces preview-before-apply:
 * - Selecting pastel backgrounds, doodle packs, or subtlety ONLY updates the live preview inside Settings.
 * - Nothing is saved or activated on the real reminder screen until the user explicitly taps "Apply".
 * - Fully independent of the main app theme/wallpaper and floating icon settings.
 */
@Composable
fun ReminderBackgroundStudio(
    appliedThemeId: String,
    appliedDoodlePackId: String,
    appliedDoodleOpacity: Int,
    alertStyle: String,
    alertImgDp: Int,
    sampleTask: SmartTask?,
    lang: String,
    onApply: (themeId: String, doodlePackId: String, opacityPercent: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalMNotesColors.current

    // Local draft state — strictly decoupled from persisted prefs until user presses Apply
    var draftThemeId by remember { mutableStateOf(appliedThemeId) }
    var draftDoodlePackId by remember { mutableStateOf(appliedDoodlePackId) }
    var draftOpacity by remember { mutableIntStateOf(appliedDoodleOpacity.coerceIn(6, 30)) }
    var showFullScreenDraftPreview by remember { mutableStateOf(false) }

    // Sync if external backup restore updates applied values
    LaunchedEffect(appliedThemeId, appliedDoodlePackId, appliedDoodleOpacity) {
        draftThemeId = appliedThemeId
        draftDoodlePackId = appliedDoodlePackId
        draftOpacity = appliedDoodleOpacity.coerceIn(6, 30)
    }

    val hasUnappliedChanges =
        draftThemeId != appliedThemeId ||
            draftDoodlePackId != appliedDoodlePackId ||
            draftOpacity != appliedDoodleOpacity

    val previewTask = remember(sampleTask, lang) {
        sampleTask ?: SmartTask(
            id = "reminder_bg_preview",
            userId = "local",
            title = if (lang == "en") "Leg Day Workout" else "تمرين الرجل في الجيم",
            meta = if (lang == "en") "7:00 PM" else "7:00 م",
            imgUri = "res://img_3d_legs_pack#0",
            repeatMode = RepeatMode.CYCLE.wire,
            cycleSteps = listOf(
                if (lang == "en") "Legs" else "رجل",
                if (lang == "en") "Chest" else "صدر"
            ),
            cycleIndex = 0L,
            alertEnabled = true,
            alertTime = "19:00"
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("reminder_bg_studio"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Live Interactive Reminder Screen Preview Card
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = colors.cardBg,
            shadowElevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = if (hasUnappliedChanges) 2.dp else 1.2.dp,
                    color = if (hasUnappliedChanges) colors.primary else colors.borderSoft,
                    shape = RoundedCornerShape(22.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(256.dp)
                    .clip(RoundedCornerShape(22.dp))
            ) {
                // Live pastel wallpaper + scattered cartoon doodles preview
                ReminderWallpaperCanvas(
                    themeId = draftThemeId,
                    doodlePackId = draftDoodlePackId,
                    doodleOpacityPercent = draftOpacity,
                    compactScale = 0.78f,
                    modifier = Modifier.fillMaxSize()
                )

                // Top status pill + Full-screen preview button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(
                                if (hasUnappliedChanges) colors.primary else Color.White.copy(alpha = 0.88f)
                            )
                            .border(
                                1.dp,
                                if (hasUnappliedChanges) colors.primary else colors.borderSoft,
                                RoundedCornerShape(999.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (hasUnappliedChanges) {
                                if (lang == "en") "Preview (Not Applied Yet)" else "معاينة قبل التطبيق"
                            } else {
                                if (lang == "en") "Active Reminder Design" else "التصميم المُطبّق حالياً"
                            },
                            color = if (hasUnappliedChanges) Color.White else colors.primary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color.White.copy(alpha = 0.9f))
                            .border(1.dp, colors.borderSoft, RoundedCornerShape(999.dp))
                            .clickable { showFullScreenDraftPreview = true }
                            .padding(horizontal = 9.dp, vertical = 5.dp)
                            .testTag("preview_reminder_bg_fullscreen_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Fullscreen,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (lang == "en") "Full Preview" else "معاينة كاملة",
                                color = colors.textDark,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Miniature realistic Reminder Content (identical layout & elements preserved)
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .padding(top = 26.dp, start = 20.dp, end = 20.dp, bottom = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color.White.copy(alpha = 0.85f))
                            .border(1.dp, colors.primary.copy(alpha = 0.25f), RoundedCornerShape(999.dp))
                            .padding(horizontal = 12.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = AppStrings.get(lang, "dueTitle"),
                            color = colors.primary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    MediaAvatarSlot(
                        imgUri = previewTask.imgUri,
                        size = 64.dp,
                        shapeId = "ring",
                        contentDescription = previewTask.title
                    )

                    Text(
                        text = previewTask.displayTitle(),
                        color = colors.textDark,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )

                    val timeTxt = if (previewTask.alertTime.isNotBlank()) {
                        AppStrings.formatTime(previewTask.alertTime, lang)
                    } else {
                        previewTask.meta
                    }
                    if (timeTxt.isNotBlank()) {
                        Text(
                            text = timeTxt,
                            color = colors.textMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(Modifier.height(2.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(0.86f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1.25f)
                                .height(34.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = AppStrings.get(lang, "doneBtn"),
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .weight(0.85f)
                                .height(34.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.85f))
                                .border(1.dp, colors.borderSoft, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = AppStrings.get(lang, "snooze"),
                                color = colors.textMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // 2. Pastel Background Theme Picker
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Palette,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = if (lang == "en") "Pastel Background Theme" else "لون وخلفية شاشة التذكير (باستيل ناعم)",
                    color = colors.textDark,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReminderWallpaperCatalog.themes.forEach { theme ->
                    val selected = draftThemeId == theme.id
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = colors.cardBg,
                        modifier = Modifier
                            .width(92.dp)
                            .border(
                                width = if (selected) 2.2.dp else 1.dp,
                                color = if (selected) colors.primary else colors.borderSoft,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { draftThemeId = theme.id }
                            .testTag("reminder_bg_theme_${theme.id}")
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .clip(RoundedCornerShape(11.dp))
                                    .border(1.dp, colors.borderSoft.copy(alpha = 0.7f), RoundedCornerShape(11.dp))
                            ) {
                                ReminderWallpaperCanvas(
                                    themeId = theme.id,
                                    doodlePackId = if (draftDoodlePackId == "none") "all_mix" else draftDoodlePackId,
                                    doodleOpacityPercent = 18,
                                    compactScale = 0.48f,
                                    modifier = Modifier.fillMaxSize()
                                )
                                if (selected) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(colors.primary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(5.dp))
                            Text(
                                text = if (lang == "en") theme.nameEn else theme.nameAr,
                                color = if (selected) colors.primary else colors.textDark,
                                fontSize = 9.5.sp,
                                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // 3. Cute Scattered Doodle Illustrations Picker
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = if (lang == "en") "Background Doodle Decorations" else "رسومات وزخارف الخلفية (بأسلوب خلفيات المحادثات)",
                    color = colors.textDark,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ReminderWallpaperCatalog.doodlePacks.chunked(2).forEach { rowPacks ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowPacks.forEach { pack ->
                            val selected = draftDoodlePackId == pack.id
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (selected) colors.primarySoft else colors.bgMain,
                                modifier = Modifier
                                    .weight(1f)
                                    .border(
                                        width = if (selected) 1.8.dp else 1.dp,
                                        color = if (selected) colors.primary else colors.borderSoft,
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { draftDoodlePackId = pack.id }
                                    .testTag("reminder_doodle_pack_${pack.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(9.dp))
                                            .border(1.dp, colors.borderSoft, RoundedCornerShape(9.dp))
                                    ) {
                                        ReminderWallpaperCanvas(
                                            themeId = draftThemeId,
                                            doodlePackId = pack.id,
                                            doodleOpacityPercent = 28,
                                            compactScale = 0.42f,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (lang == "en") pack.nameEn else pack.nameAr,
                                            color = if (selected) colors.primary else colors.textDark,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = if (lang == "en") pack.subtitleEn else pack.subtitleAr,
                                            color = colors.textMuted,
                                            fontSize = 9.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                        if (rowPacks.size == 1) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // 4. Subtlety / Transparency Slider (keeps doodles gentle & readable)
        if (draftDoodlePackId != "none") {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = colors.bgMain,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.borderSoft, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (lang == "en") "Doodle Subtlety" else "نعومة وشفافية الزخارف",
                            color = colors.textDark,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$draftOpacity%",
                            color = colors.primary,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Slider(
                        value = draftOpacity.toFloat(),
                        onValueChange = { draftOpacity = it.roundToInt().coerceIn(6, 30) },
                        valueRange = 6f..30f,
                        colors = SliderDefaults.colors(
                            thumbColor = colors.primary,
                            activeTrackColor = colors.primary,
                            inactiveTrackColor = colors.borderSoft
                        ),
                        modifier = Modifier.testTag("reminder_doodle_opacity_slider")
                    )
                }
            }
        }

        // 5. Explicit Apply & Reset Buttons (Nothing is saved until user taps Apply!)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onApply(draftThemeId, draftDoodlePackId, draftOpacity) },
                enabled = hasUnappliedChanges,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = Color.White,
                    disabledContainerColor = colors.primary.copy(alpha = 0.35f),
                    disabledContentColor = Color.White.copy(alpha = 0.85f)
                ),
                modifier = Modifier
                    .weight(1.4f)
                    .height(48.dp)
                    .testTag("apply_reminder_bg_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = if (hasUnappliedChanges) {
                        if (lang == "en") "Apply Background" else "تطبيق الخلفية"
                    } else {
                        if (lang == "en") "Applied" else "مُطبّق بالفعل"
                    },
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            OutlinedButton(
                onClick = {
                    draftThemeId = "default"
                    draftDoodlePackId = "none"
                    draftOpacity = 14
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(0.7f)
                    .height(48.dp)
                    .testTag("reset_reminder_bg_draft_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.RestartAlt,
                    contentDescription = null,
                    tint = colors.textMuted,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = if (lang == "en") "Reset" else "افتراضي",
                    color = colors.textMuted,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // Temporary full-screen live preview modal — shows exact real AlertOverlay with draft values
    // and lets the user Apply directly or Close without applying!
    if (showFullScreenDraftPreview) {
        Dialog(
            onDismissRequest = { showFullScreenDraftPreview = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AlertOverlay(
                    task = previewTask,
                    alertStyle = alertStyle,
                    alertImgDp = alertImgDp,
                    lang = lang,
                    reminderBgThemeId = draftThemeId,
                    reminderDoodlePackId = draftDoodlePackId,
                    reminderDoodleOpacity = draftOpacity,
                    onComplete = { showFullScreenDraftPreview = false },
                    onDismiss = { showFullScreenDraftPreview = false }
                )

                // Floating preview banner at bottom-top so user can Apply or Cancel from full-screen preview
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            onApply(draftThemeId, draftDoodlePackId, draftOpacity)
                            showFullScreenDraftPreview = false
                        },
                        shape = RoundedCornerShape(999.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("fullscreen_preview_apply_btn")
                    ) {
                        Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (lang == "en") "Apply This Design" else "تطبيق هذا التصميم",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.5.sp
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 6.dp,
                        modifier = Modifier
                            .size(48.dp)
                            .border(1.dp, colors.borderSoft, CircleShape)
                            .clip(CircleShape)
                            .clickable { showFullScreenDraftPreview = false }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = AppStrings.get(lang, "close"),
                                tint = colors.textDark
                            )
                        }
                    }
                }
            }
        }
    }
}
