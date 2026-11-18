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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.AppStrings
import com.example.model.SmartTask
import com.example.ui.theme.LocalMNotesColors
import com.example.ui.theme.LocalMNotesWeight

@Composable
fun TaskCard(
    task: SmartTask,
    lang: String,
    cardShape: String,
    taskImgSize: String,
    isDemo: Boolean = false,
    onPhotoClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
    val colors = LocalMNotesColors.current
    val titleWeight = LocalMNotesWeight.current
    val imgDp = taskImageDp(taskImgSize)

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
        "capsule" -> 6.dp
        "strip" -> 0.dp
        else -> 10.dp
    }
    val verticalPad = when (cardShape) {
        "capsule" -> 5.dp
        "strip" -> 0.dp
        else -> 8.dp
    }

    Surface(
        shape = RoundedCornerShape(cardRadius),
        color = if (isDemo) colors.cardBg else colors.bgMain,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(cardRadius))
            .border(1.dp, colors.borderSoft, RoundedCornerShape(cardRadius))
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
                    contentDescription = task.title,
                    onClick = if (isDemo) null else onPhotoClick
                )
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        color = colors.textDark,
                        fontSize = 13.5.sp,
                        fontWeight = titleWeight,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (subtitle.isNotBlank()) {
                        Text(
                            text = subtitle,
                            color = colors.textMuted,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
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
                            .background(if (task.alertEnabled) colors.primarySoft else colors.cardBg)
                            .border(
                                1.dp,
                                if (task.alertEnabled) colors.primary else colors.borderSoft,
                                CircleShape
                            )
                            .clickable(onClick = onEditClick)
                            .testTag("task_edit_${task.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AccessTime,
                            contentDescription = AppStrings.get(lang, "editTask"),
                            tint = if (task.alertEnabled) colors.primary else colors.textMuted,
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
