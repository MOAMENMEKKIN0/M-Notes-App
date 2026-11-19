package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode as AnimRepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.AppStrings
import com.example.model.SmartTask
import com.example.ui.theme.LocalMNotesColors
import com.example.ui.theme.LocalMNotesTextScale
import kotlinx.coroutines.delay

@Composable
fun AlertOverlay(
    task: SmartTask,
    alertStyle: String,
    alertImgDp: Int = 120,
    lang: String,
    reminderBgThemeId: String = "default",
    reminderDoodlePackId: String = "none",
    reminderDoodleOpacity: Int = 14,
    onComplete: () -> Unit,
    onDismiss: () -> Unit
) {
    BackHandler(onBack = onDismiss)

    val colors = LocalMNotesColors.current
    val textScale = LocalMNotesTextScale.current
    var isCompleting by remember(task.id) { mutableStateOf(false) }

    LaunchedEffect(isCompleting) {
        if (isCompleting) {
            delay(650)
            onComplete()
        }
    }

    val cycleStep = task.currentCycleStepOnly()
    val title = if (cycleStep.isNotBlank()) "${task.title} ($cycleStep)" else task.title
    val sub = if (task.alertTime.isNotBlank()) AppStrings.formatTime(task.alertTime, lang) else task.meta
    val doneColor = Color(0xFF18C28B)

    val cardBg by animateColorAsState(
        if (isCompleting) doneColor else colors.cardBg,
        label = "alert_card_bg"
    )
    val contentTextColor = if (isCompleting) Color.White else colors.textDark
    val subTextColor = if (isCompleting) Color.White.copy(alpha = 0.9f) else colors.textMuted

    val needsDim = alertStyle in listOf("A2", "A3", "A4", "A6")
    val hasCustomWallpaper = ReminderWallpaperCatalog.isCustomized(reminderBgThemeId, reminderDoodlePackId)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (needsDim) {
                    Modifier
                        .background(Color(0x6B0F172A))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        )
                } else Modifier
            )
    ) {
        when (alertStyle) {
            "A2" -> {
                // Full Screen Focus
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (isCompleting) {
                                Brush.verticalGradient(listOf(doneColor, doneColor))
                            } else {
                                Brush.verticalGradient(listOf(colors.bgTopGlow, colors.bgMain, colors.cardBg))
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (!isCompleting && hasCustomWallpaper) {
                        ReminderWallpaperCanvas(
                            themeId = reminderBgThemeId,
                            doodlePackId = reminderDoodlePackId,
                            doodleOpacityPercent = reminderDoodleOpacity,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .safeDrawingPadding()
                            .padding(horizontal = 24.dp, vertical = 28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 440.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(if (isCompleting) Color.White.copy(alpha = 0.22f) else colors.primarySoft)
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = AppStrings.get(lang, "dueTitle"),
                                color = if (isCompleting) Color.White else colors.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            AlertAvatar(task = task, sizeDp = alertImgDp.coerceIn(56, 220), ring = true)
                            Spacer(Modifier.height(20.dp))
                            Text(
                                text = title,
                                color = contentTextColor,
                                fontSize = (24f * textScale).sp,
                                lineHeight = (30f * textScale).sp,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center
                            )
                            if (sub.isNotBlank()) {
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = sub,
                                    color = subTextColor,
                                    fontSize = (14f * textScale).sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            DoneAlertButton(
                                isCompleting = isCompleting,
                                lang = lang,
                                primaryColor = colors.primary,
                                onClick = { if (!isCompleting) isCompleting = true }
                            )
                            OutlinedButton(
                                onClick = onDismiss,
                                shape = RoundedCornerShape(15.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("alert_snooze_button")
                            ) {
                                Text(
                                    text = AppStrings.get(lang, "snooze"),
                                    color = if (isCompleting) Color.White else colors.textMuted,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    }
                }
            }

            "A3", "A6" -> {
                val isHalo = alertStyle == "A6"
                val infinite = rememberInfiniteTransition(label = "halo_pulse")
                val haloWidth by infinite.animateFloat(
                    initialValue = 2f,
                    targetValue = 9f,
                    animationSpec = infiniteRepeatable(tween(1000), AnimRepeatMode.Reverse),
                    label = "halo_width"
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(26.dp),
                        color = cardBg,
                        shadowElevation = 16.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 385.dp)
                            .border(
                                width = if (isHalo) haloWidth.dp else 1.5.dp,
                                color = if (isHalo) colors.primary.copy(alpha = 0.55f) else colors.borderSoft,
                                shape = RoundedCornerShape(26.dp)
                            )
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            if (!isCompleting && hasCustomWallpaper) {
                                ReminderWallpaperCanvas(
                                    themeId = reminderBgThemeId,
                                    doodlePackId = reminderDoodlePackId,
                                    doodleOpacityPercent = reminderDoodleOpacity,
                                    compactScale = 0.72f,
                                    modifier = Modifier.matchParentSize()
                                )
                            }
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(22.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                            AlertAvatar(task = task, sizeDp = (alertImgDp * 0.68f).toInt().coerceIn(48, 160), ring = false)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = title,
                                color = contentTextColor,
                                fontSize = (17f * textScale).sp,
                                lineHeight = (22f * textScale).sp,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center
                            )
                            if (sub.isNotBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = sub,
                                    color = subTextColor,
                                    fontSize = (12.5f * textScale).sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                            Spacer(Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    DoneAlertButton(
                                        isCompleting = isCompleting,
                                        lang = lang,
                                        primaryColor = colors.primary,
                                        onClick = { if (!isCompleting) isCompleting = true }
                                    )
                                }
                                OutlinedButton(
                                    onClick = onDismiss,
                                    shape = RoundedCornerShape(15.dp),
                                    modifier = Modifier
                                        .height(48.dp)
                                        .testTag("alert_snooze_button")
                                ) {
                                    Text(
                                        text = AppStrings.get(lang, "snooze"),
                                        color = if (isCompleting) Color.White else colors.textMuted,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        }
                    }
                }
            }

            "A4" -> {
                // Bottom Sheet Alert
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Surface(
                        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                        color = cardBg,
                        shadowElevation = 18.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 460.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            if (!isCompleting && hasCustomWallpaper) {
                                ReminderWallpaperCanvas(
                                    themeId = reminderBgThemeId,
                                    doodlePackId = reminderDoodlePackId,
                                    doodleOpacityPercent = reminderDoodleOpacity,
                                    compactScale = 0.72f,
                                    modifier = Modifier.matchParentSize()
                                )
                            }
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .navigationBarsPadding()
                                    .padding(horizontal = 18.dp, vertical = 16.dp)
                            ) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .width(42.dp)
                                    .height(4.5.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(Color(0xFFCBD5E1))
                            )
                            Spacer(Modifier.height(14.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AlertAvatar(task = task, sizeDp = (alertImgDp * 0.52f).toInt().coerceIn(40, 120), ring = false)
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = title,
                                        color = contentTextColor,
                                        fontSize = (16f * textScale).sp,
                                        lineHeight = (21f * textScale).sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    if (sub.isNotBlank()) {
                                        Text(
                                            text = sub,
                                            color = subTextColor,
                                            fontSize = (12f * textScale).sp
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    DoneAlertButton(
                                        isCompleting = isCompleting,
                                        lang = lang,
                                        primaryColor = colors.primary,
                                        onClick = { if (!isCompleting) isCompleting = true }
                                    )
                                }
                                OutlinedButton(
                                    onClick = onDismiss,
                                    shape = RoundedCornerShape(15.dp),
                                    modifier = Modifier
                                        .height(48.dp)
                                        .testTag("alert_snooze_button")
                                ) {
                                    Text(
                                        text = AppStrings.get(lang, "snooze"),
                                        color = if (isCompleting) Color.White else colors.textMuted,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        }
                    }
                }
            }

            "A5" -> {
                // Compact Pill Alert at top
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = cardBg,
                        shadowElevation = 10.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 420.dp)
                            .border(1.5.dp, colors.primary, RoundedCornerShape(999.dp))
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            if (!isCompleting && hasCustomWallpaper) {
                                ReminderWallpaperCanvas(
                                    themeId = reminderBgThemeId,
                                    doodlePackId = reminderDoodlePackId,
                                    doodleOpacityPercent = reminderDoodleOpacity,
                                    compactScale = 0.55f,
                                    modifier = Modifier.matchParentSize()
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AlertAvatar(task = task, sizeDp = 38, ring = false)
                                Spacer(Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = title,
                                        color = contentTextColor,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (sub.isNotBlank()) {
                                        Text(
                                            text = sub,
                                            color = subTextColor,
                                            fontSize = 10.5.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.width(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = { if (!isCompleting) isCompleting = true },
                                    shape = RoundedCornerShape(999.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isCompleting) Color.White else colors.primary,
                                        contentColor = if (isCompleting) doneColor else Color.White
                                    ),
                                    modifier = Modifier
                                        .height(40.dp)
                                        .testTag("alert_done_button")
                                ) {
                                    Icon(Icons.Rounded.Check, contentDescription = AppStrings.get(lang, "doneBtn"), modifier = Modifier.size(16.dp))
                                }
                                OutlinedButton(
                                    onClick = onDismiss,
                                    shape = RoundedCornerShape(999.dp),
                                    modifier = Modifier
                                        .height(40.dp)
                                        .testTag("alert_snooze_button")
                                ) {
                                    Icon(Icons.Rounded.Close, contentDescription = AppStrings.get(lang, "snooze"), modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                        }
                    }
                }
            }

            else -> {
                // A1: Top Floating Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = cardBg,
                        shadowElevation = 12.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 420.dp)
                            .border(1.5.dp, colors.primary, RoundedCornerShape(22.dp))
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            if (!isCompleting && hasCustomWallpaper) {
                                ReminderWallpaperCanvas(
                                    themeId = reminderBgThemeId,
                                    doodlePackId = reminderDoodlePackId,
                                    doodleOpacityPercent = reminderDoodleOpacity,
                                    compactScale = 0.72f,
                                    modifier = Modifier.matchParentSize()
                                )
                            }
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AlertAvatar(task = task, sizeDp = (alertImgDp * 0.44f).toInt().coerceIn(36, 96), ring = false)
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = title,
                                        color = contentTextColor,
                                        fontSize = (14f * textScale).sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    if (sub.isNotBlank()) {
                                        Text(
                                            text = sub,
                                            color = subTextColor,
                                            fontSize = (11.5f * textScale).sp
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    DoneAlertButton(
                                        isCompleting = isCompleting,
                                        lang = lang,
                                        primaryColor = colors.primary,
                                        onClick = { if (!isCompleting) isCompleting = true }
                                    )
                                }
                                OutlinedButton(
                                    onClick = onDismiss,
                                    shape = RoundedCornerShape(15.dp),
                                    modifier = Modifier
                                        .height(48.dp)
                                        .testTag("alert_snooze_button")
                                ) {
                                    Text(
                                        text = AppStrings.get(lang, "snooze"),
                                        color = if (isCompleting) Color.White else colors.textMuted,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlertAvatar(task: SmartTask, sizeDp: Int, ring: Boolean) {
    val colors = LocalMNotesColors.current
    if (task.imgUri.isNotBlank()) {
        MediaAvatarSlot(
            imgUri = task.imgUri,
            size = sizeDp.dp,
            shapeId = if (ring) "ring" else "circle",
            contentDescription = task.title
        )
    } else {
        Box(
            modifier = Modifier
                .size(sizeDp.dp)
                .shadow(4.dp, CircleShape, clip = false)
                .clip(CircleShape)
                .background(if (ring) Brush.linearGradient(listOf(colors.primary, colors.accent)) else Brush.linearGradient(listOf(colors.primarySoft, colors.primarySoft)))
                .padding(if (ring) 5.dp else 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(colors.primarySoft)
                    .border(2.dp, colors.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.AccessTime,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size((sizeDp * 0.42f).dp)
                )
            }
        }
    }
}

@Composable
private fun DoneAlertButton(
    isCompleting: Boolean,
    lang: String,
    primaryColor: Color,
    onClick: () -> Unit
) {
    val doneGreen = Color(0xFF18C28B)
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(15.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isCompleting) Color.White else primaryColor,
            contentColor = if (isCompleting) doneGreen else Color.White
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("alert_done_button")
    ) {
        Icon(
            imageVector = Icons.Rounded.Check,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = AppStrings.get(lang, if (isCompleting) "doneOk" else "doneBtn"),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 14.sp
        )
    }
}
