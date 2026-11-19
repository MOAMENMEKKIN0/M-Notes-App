package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DashboardCustomize
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.AppStrings
import com.example.model.SmartTask
import com.example.ui.theme.LocalMNotesColors
import com.example.ui.theme.LocalMNotesTextScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeQuickOverviewSheet(
    lang: String,
    dueTodayCount: Int,
    allTasks: List<SmartTask>,
    selectedDayFilter: Int?,
    requestsCount: Int,
    onEditTask: (SmartTask) -> Unit,
    onPreviewAlert: (SmartTask) -> Unit,
    onAddTask: () -> Unit,
    onOpenRequestsPage: () -> Unit,
    onOpenHomeSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalMNotesColors.current
    val textScale = LocalMNotesTextScale.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val safeDismisser = rememberSafeSheetDismisser(sheetState, onDismiss)
    val dayNames = AppStrings.dayNames(lang)

    val displayedTasks = if (selectedDayFilter != null) {
        allTasks.filter { it.weekDays.contains(selectedDayFilter.toLong()) }
    } else {
        allTasks
    }
    val heading = if (selectedDayFilter != null && selectedDayFilter in dayNames.indices) {
        if (lang == "en") "Tasks for ${dayNames[selectedDayFilter]}" else "مهام يوم ${dayNames[selectedDayFilter]}"
    } else {
        if (lang == "en") "Today's Tasks & Quick Access" else "نظرة سريعة والوصول لكل الأقسام"
    }

    ModalBottomSheet(
        onDismissRequest = { safeDismisser.onSystemDismiss() },
        sheetState = sheetState,
        containerColor = colors.bgMain,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = heading,
                        color = colors.textDark,
                        fontSize = (16.5f * textScale).sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = if (lang == "en") {
                            "$dueTodayCount due today • ${allTasks.size} total • $requestsCount orders"
                        } else {
                            "$dueTodayCount مهام اليوم • ${allTasks.size} الإجمالي • $requestsCount طلبات"
                        },
                        color = colors.textMuted,
                        fontSize = (11.5f * textScale).sp
                    )
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colors.cardBg)
                        .border(1.dp, colors.borderSoft, CircleShape)
                        .clickable { safeDismisser.dismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = "Close", tint = colors.textMuted, modifier = Modifier.size(18.dp))
                }
            }

            // Quick Jump Action Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = colors.primarySoft,
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, colors.primary.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            safeDismisser.dismiss { onOpenHomeSettings() }
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Rounded.DashboardCustomize, contentDescription = null, tint = colors.primary, modifier = Modifier.size(18.dp))
                        Text(
                            text = if (lang == "en") "Customize Home" else "ضبط الواجهة والأحجام",
                            color = colors.primary,
                            fontSize = (11.5f * textScale).sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = colors.cardBg,
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, colors.borderSoft, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            safeDismisser.dismiss { onOpenRequestsPage() }
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Rounded.ReceiptLong, contentDescription = null, tint = colors.primary, modifier = Modifier.size(18.dp))
                        Text(
                            text = if (lang == "en") "Orders ($requestsCount)" else "الطلبات ($requestsCount)",
                            color = colors.textDark,
                            fontSize = (11.5f * textScale).sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            if (displayedTasks.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = colors.cardBg,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, colors.borderSoft, RoundedCornerShape(16.dp))
                ) {
                    Text(
                        text = if (lang == "en") "No tasks listed here yet." else "لا توجد مهام مسجلة في هذا التصنيف حالياً.",
                        color = colors.textMuted,
                        fontSize = 12.5.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    displayedTasks.forEach { task ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = colors.cardBg,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, colors.borderSoft, RoundedCornerShape(16.dp))
                                .clickable {
                                    safeDismisser.dismiss { onEditTask(task) }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                val isResting = !task.isActiveOnDate()
                                MediaAvatarSlot(
                                    imgUri = task.imgUri,
                                    size = 42.dp,
                                    shapeId = "circle",
                                    isActiveGlow = !isResting,
                                    isResting = isResting,
                                    contentDescription = task.title
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = task.title,
                                        color = colors.textDark,
                                        fontSize = (13f * textScale).sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    val meta = listOf(task.currentCycleLabel(), task.meta)
                                        .filter { it.isNotBlank() }
                                        .joinToString(" • ")
                                    if (meta.isNotBlank()) {
                                        Text(
                                            text = meta,
                                            color = colors.textMuted,
                                            fontSize = (11f * textScale).sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(colors.primarySoft)
                                        .clickable {
                                            safeDismisser.dismiss { onPreviewAlert(task) }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Rounded.NotificationsActive, contentDescription = null, tint = colors.primary, modifier = Modifier.size(16.dp))
                                }
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(colors.bgMain)
                                        .border(1.dp, colors.borderSoft, CircleShape)
                                        .clickable {
                                            safeDismisser.dismiss { onEditTask(task) }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Rounded.Edit, contentDescription = null, tint = colors.textDark, modifier = Modifier.size(15.dp))
                                }
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    safeDismisser.dismiss { onAddTask() }
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = AppStrings.get(lang, "newTask"),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}
