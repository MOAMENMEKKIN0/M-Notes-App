package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.AppStrings
import com.example.model.RepeatMode
import com.example.model.SmartTask
import com.example.ui.theme.LocalMNotesCardScale
import com.example.ui.theme.LocalMNotesColors
import com.example.ui.theme.LocalMNotesTextScale
import com.example.ui.theme.LocalMNotesWeight

@Composable
fun TaskCard(
    task: SmartTask,
    lang: String,
    cardShape: String,
    taskImgSize: String,
    taskImgDp: Int = 48,
    isDemo: Boolean = false,
    onPhotoClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
    val colors = LocalMNotesColors.current
    val titleWeight = LocalMNotesWeight.current
    val textScale = LocalMNotesTextScale.current
    val cardScale = LocalMNotesCardScale.current
    val imgDp = taskImageDp(taskImgSize, taskImgDp)

    val isResting = !isDemo && !task.isActiveOnDate()
    val isActiveScheduled = !isDemo && !isResting && (task.repeat != RepeatMode.NONE || task.usesWeekDays())

    val dayLabels = AppStrings.dayNames(lang)
    val subParts = buildList {
        val cycle = task.currentCycleLabel()
        if (cycle.isNotBlank()) add(cycle)
        val timeOrMeta = if (task.alertTime.isNotBlank()) {
            AppStrings.formatTime(task.alertTime, lang)
        } else {
            task.meta
        }
        if (timeOrMeta.isNotBlank()) add(timeOrMeta)
        if (task.usesWeekDays()) {
            val daysStr = task.weekDays.sorted()
                .mapNotNull { dayLabels.getOrNull(it.toInt()) }
                .joinToString(if (lang == "ar") "، " else ", ")
            if (daysStr.isNotBlank()) add(daysStr)
        }
    }
    val subtitle = subParts.joinToString(" • ")

    val cardRadius = when (cardShape) {
        "capsule" -> 999.dp
        "strip" -> 16.dp
        else -> 18.dp
    }
    val photoShape = when (cardShape) {
        "ring" -> "ring"
        "ios" -> "ios"
        "blob" -> "blob"
        "strip" -> "strip"
        else -> "circle"
    }

    val horizontalPad = when (cardShape) {
        "capsule" -> (8f * cardScale).dp
        "strip" -> 4.dp
        else -> (10f * cardScale).dp
    }
    val verticalPad = when (cardShape) {
        "capsule" -> (6f * cardScale).dp
        "strip" -> 4.dp
        else -> (9f * cardScale).dp
    }

    val cardBg = when {
        isDemo -> colors.cardBg
        isResting -> colors.bgMain.copy(alpha = 0.72f)
        isActiveScheduled -> colors.cardBg
        else -> colors.bgMain
    }
    val borderColor = when {
        isResting -> colors.borderSoft.copy(alpha = 0.85f)
        isActiveScheduled -> colors.primary.copy(alpha = 0.42f)
        else -> colors.borderSoft
    }

    Surface(
        shape = RoundedCornerShape(cardRadius),
        color = cardBg,
        shadowElevation = if (isActiveScheduled) 3.dp else 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(cardRadius))
            .border(
                width = if (isActiveScheduled) 1.4.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(cardRadius)
            )
            .then(if (!isDemo) Modifier.clickable { onEditClick() } else Modifier)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = horizontalPad, end = 10.dp, top = verticalPad, bottom = verticalPad),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MediaAvatarSlot(
                    imgUri = task.imgUri,
                    size = imgDp,
                    shapeId = photoShape,
                    useBannerDefaultIfBlank = false,
                    isActiveGlow = !isDemo && !isResting,
                    isResting = isResting,
                    contentDescription = task.title,
                    onClick = if (isDemo) null else onPhotoClick
                )
                Spacer(Modifier.width(12.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .alpha(if (isResting) 0.82f else 1f)
                ) {
                    Text(
                        text = task.title,
                        color = if (isResting) colors.textMuted else colors.textDark,
                        fontSize = (13.5f * textScale).sp,
                        lineHeight = (19f * textScale).sp,
                        fontWeight = titleWeight,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (subtitle.isNotBlank()) {
                        Text(
                            text = subtitle,
                            color = colors.textMuted,
                            fontSize = (11f * textScale).sp,
                            lineHeight = (16f * textScale).sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (isResting) {
                        val nextDay = task.nextScheduledDayLabel(dayLabels)
                        val restText = if (lang == "en") {
                            if (nextDay.isNotBlank()) "Zzz • Resting today • Next: $nextDay" else "Zzz • Resting today"
                        } else {
                            if (nextDay.isNotBlank()) "Zzz • راحة اليوم • القادم: $nextDay" else "Zzz • راحة اليوم"
                        }
                        Spacer(Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(Color(0xFFE2E8F0).copy(alpha = 0.7f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Bedtime,
                                    contentDescription = null,
                                    tint = Color(0xFF475569),
                                    modifier = Modifier.size(11.dp)
                                )
                                Text(
                                    text = restText,
                                    color = Color(0xFF475569),
                                    fontSize = (9.5f * textScale).sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    } else if (isActiveScheduled) {
                        Spacer(Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(colors.primarySoft)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Bolt,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(11.dp)
                                )
                                Text(
                                    text = if (lang == "en") "Active Today" else "نشطة اليوم",
                                    color = colors.primary,
                                    fontSize = (9.5f * textScale).sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }

            if (!isDemo) {
                Spacer(Modifier.width(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (task.alertEnabled && !isResting) colors.primarySoft else colors.cardBg)
                            .border(
                                1.dp,
                                if (task.alertEnabled && !isResting) colors.primary else colors.borderSoft,
                                CircleShape
                            )
                            .clickable(onClick = onEditClick)
                            .testTag("task_edit_${task.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AccessTime,
                            contentDescription = AppStrings.get(lang, "editTask"),
                            tint = if (task.alertEnabled && !isResting) colors.primary else colors.textMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(colors.cardBg)
                            .border(1.dp, Color(0xFFFEE2E2), CircleShape)
                            .clickable(onClick = onDeleteClick)
                            .testTag("task_delete_${task.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "Delete",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

