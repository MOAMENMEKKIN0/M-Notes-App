package com.example.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.localization.AppStrings
import com.example.model.RepeatMode
import com.example.model.SmartIconSettings
import com.example.model.SmartTask
import com.example.model.UserPreferences
import com.example.ui.UiState
import com.example.ui.theme.LocalMNotesCardScale
import com.example.ui.theme.LocalMNotesColors
import com.example.ui.theme.LocalMNotesRefreshHz
import com.example.ui.theme.LocalMNotesTextScale
import com.example.ui.theme.motionDurationMsForHz
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

private enum class TaskFilter(val id: String) {
    ALL("all"),
    GYM("gym"),
    ALERTS("alerts"),
    DAILY("daily")
}

@Composable
fun TasksWorkspaceRouter(
    prefs: UserPreferences,
    allTasks: List<SmartTask>,
    smartIconSettings: SmartIconSettings = SmartIconSettings(),
    tasksState: UiState<List<SmartTask>>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onBannerPhotoClick: () -> Unit,
    onBannerBodyClick: () -> Unit,
    onAddTaskClick: () -> Unit,
    onTaskPhotoClick: (String) -> Unit,
    onTaskEditClick: (SmartTask) -> Unit,
    onTaskDeleteClick: (String) -> Unit,
    onTaskBadgeTap: (SmartTask) -> Unit = {},
    onTaskBadgeDrag: (taskId: String, offsetX: Float, offsetY: Float) -> Unit = { _, _, _ -> },
    onPreviewTaskAlert: (SmartTask) -> Unit,
    onOpenOverview: (Int?) -> Unit
) {
    val dur = motionDurationMsForHz(LocalMNotesRefreshHz.current)
    var activeFilter by remember { mutableStateOf(TaskFilter.ALL) }

    val todayDate = LocalDate.now()
    val today = AppStrings.todayStr()
    val q = searchQuery.trim().lowercase()
    val dueTasks = remember(allTasks, q, activeFilter, today) {
        allTasks
            .filter { t ->
                val queryMatch = q.isEmpty() ||
                    t.title.lowercase().contains(q) ||
                    t.meta.lowercase().contains(q) ||
                    t.currentCycleLabel().lowercase().contains(q)
                val filterMatch = when (activeFilter) {
                    TaskFilter.ALL -> true
                    TaskFilter.GYM -> t.repeat == RepeatMode.CYCLE ||
                        t.usesWeekDays() ||
                        t.title.contains("جيم") ||
                        t.title.contains("تمرين") ||
                        t.title.lowercase().contains("gym")
                    TaskFilter.ALERTS -> t.alertEnabled && t.alertTime.isNotBlank()
                    TaskFilter.DAILY -> t.repeat == RepeatMode.DAILY || t.usesWeekDays()
                }
                queryMatch && filterMatch
            }
            .sortedByDescending { it.isActiveOnDate(todayDate) }
    }
    val dueTodayTotal = remember(allTasks, today) {
        allTasks.count { it.isActiveOnDate(todayDate) }
    }

    Crossfade(
        targetState = prefs.layoutStyle,
        animationSpec = tween(durationMillis = dur),
        label = "main_layout_crossfade",
        modifier = Modifier.fillMaxSize()
    ) { layoutId ->
        when (layoutId) {
            "L2" -> TasksWorkspaceL2(
                prefs = prefs,
                allTasks = allTasks,
                dueTasks = dueTasks,
                smartIconSettings = smartIconSettings,
                dueTodayTotal = dueTodayTotal,
                tasksState = tasksState,
                searchQuery = searchQuery,
                activeFilter = activeFilter,
                onFilterChange = { activeFilter = it },
                onSearchChange = onSearchChange,
                onBannerPhotoClick = onBannerPhotoClick,
                onBannerBodyClick = onBannerBodyClick,
                onAddTaskClick = onAddTaskClick,
                onTaskPhotoClick = onTaskPhotoClick,
                onTaskEditClick = onTaskEditClick,
                onTaskDeleteClick = onTaskDeleteClick,
                onTaskBadgeTap = onTaskBadgeTap,
                onTaskBadgeDrag = onTaskBadgeDrag,
                onPreviewTaskAlert = onPreviewTaskAlert,
                onOpenOverview = onOpenOverview
            )
            "L3" -> TasksWorkspaceL3(
                prefs = prefs,
                allTasks = allTasks,
                dueTasks = dueTasks,
                smartIconSettings = smartIconSettings,
                dueTodayTotal = dueTodayTotal,
                tasksState = tasksState,
                searchQuery = searchQuery,
                activeFilter = activeFilter,
                onFilterChange = { activeFilter = it },
                onSearchChange = onSearchChange,
                onBannerPhotoClick = onBannerPhotoClick,
                onBannerBodyClick = onBannerBodyClick,
                onAddTaskClick = onAddTaskClick,
                onTaskPhotoClick = onTaskPhotoClick,
                onTaskEditClick = onTaskEditClick,
                onTaskDeleteClick = onTaskDeleteClick,
                onTaskBadgeTap = onTaskBadgeTap,
                onTaskBadgeDrag = onTaskBadgeDrag,
                onPreviewTaskAlert = onPreviewTaskAlert,
                onOpenOverview = onOpenOverview
            )
            else -> TasksWorkspaceL1(
                prefs = prefs,
                allTasks = allTasks,
                dueTasks = dueTasks,
                smartIconSettings = smartIconSettings,
                dueTodayTotal = dueTodayTotal,
                tasksState = tasksState,
                searchQuery = searchQuery,
                activeFilter = activeFilter,
                onFilterChange = { activeFilter = it },
                onSearchChange = onSearchChange,
                onBannerPhotoClick = onBannerPhotoClick,
                onBannerBodyClick = onBannerBodyClick,
                onAddTaskClick = onAddTaskClick,
                onTaskPhotoClick = onTaskPhotoClick,
                onTaskEditClick = onTaskEditClick,
                onTaskDeleteClick = onTaskDeleteClick,
                onTaskBadgeTap = onTaskBadgeTap,
                onTaskBadgeDrag = onTaskBadgeDrag,
                onPreviewTaskAlert = onPreviewTaskAlert,
                onOpenOverview = onOpenOverview
            )
        }
    }
}

/* =========================================================================
   LAYOUT 1: Interactive Frosted Bento + Progress Ring + Quick Action Dock
   ========================================================================= */
@Composable
private fun TasksWorkspaceL1(
    prefs: UserPreferences,
    allTasks: List<SmartTask>,
    dueTasks: List<SmartTask>,
    smartIconSettings: SmartIconSettings,
    dueTodayTotal: Int,
    tasksState: UiState<List<SmartTask>>,
    searchQuery: String,
    activeFilter: TaskFilter,
    onFilterChange: (TaskFilter) -> Unit,
    onSearchChange: (String) -> Unit,
    onBannerPhotoClick: () -> Unit,
    onBannerBodyClick: () -> Unit,
    onAddTaskClick: () -> Unit,
    onTaskPhotoClick: (String) -> Unit,
    onTaskEditClick: (SmartTask) -> Unit,
    onTaskDeleteClick: (String) -> Unit,
    onTaskBadgeTap: (SmartTask) -> Unit,
    onTaskBadgeDrag: (String, Float, Float) -> Unit,
    onPreviewTaskAlert: (SmartTask) -> Unit,
    onOpenOverview: (Int?) -> Unit
) {
    val colors = LocalMNotesColors.current
    val textScale = LocalMNotesTextScale.current
    val cardScale = LocalMNotesCardScale.current
    val lang = prefs.lang
    val completedCount = (allTasks.size - dueTodayTotal).coerceAtLeast(0)
    val totalCount = allTasks.size.coerceAtLeast(1)
    val progress = (completedCount.toFloat() / totalCount.toFloat()).coerceIn(0.08f, 1f)
    val cycleTasks = remember(allTasks) { allTasks.filter { it.repeat == RepeatMode.CYCLE && it.cycleSteps.isNotEmpty() } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (prefs.showBanner) {
            HeroBanner(
                prefs = prefs,
                dueTodayCount = dueTodayTotal,
                onBannerPhotoClick = onBannerPhotoClick,
                onBannerBodyClick = onBannerBodyClick
            )
        }

        if (prefs.showStatsSummary) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = colors.cardBg,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.borderSoft, RoundedCornerShape(22.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding((14f * cardScale).dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size((66f * cardScale.coerceIn(0.85f, 1.35f)).dp)
                            .clip(CircleShape)
                            .clickable { onOpenOverview(null) }
                            .testTag("home_progress_circle"),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val stroke = 7.dp.toPx()
                            drawArc(
                                color = colors.primarySoft,
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = stroke, cap = StrokeCap.Round)
                            )
                            drawArc(
                                brush = Brush.sweepGradient(listOf(colors.primary, colors.accent, colors.primary)),
                                startAngle = -90f,
                                sweepAngle = 360f * progress,
                                useCenter = false,
                                style = Stroke(width = stroke, cap = StrokeCap.Round)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$dueTodayTotal",
                                color = colors.textDark,
                                fontSize = (17f * textScale).sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = if (lang == "en") "due" else "لليوم",
                                color = colors.textMuted,
                                fontSize = (9.5f * textScale).sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (lang == "en") "Today's Focus" else "نبض اليوم الذكي",
                            color = colors.textDark,
                            fontSize = (14f * textScale).sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onOpenOverview(null) }
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            StatMiniChip(
                                icon = Icons.Rounded.FitnessCenter,
                                label = if (lang == "en") "${cycleTasks.size} Cycles" else "${cycleTasks.size} دورات",
                                onClick = {
                                    onFilterChange(if (activeFilter == TaskFilter.GYM) TaskFilter.ALL else TaskFilter.GYM)
                                }
                            )
                            val alertsCount = allTasks.count { it.alertEnabled && it.alertTime.isNotBlank() }
                            StatMiniChip(
                                icon = Icons.Rounded.NotificationsActive,
                                label = if (lang == "en") "$alertsCount Alerts" else "$alertsCount تنبيهات",
                                onClick = {
                                    onFilterChange(if (activeFilter == TaskFilter.ALERTS) TaskFilter.ALL else TaskFilter.ALERTS)
                                }
                            )
                        }
                        val firstCycle = cycleTasks.firstOrNull()
                        if (firstCycle != null) {
                            Text(
                                text = if (lang == "en") {
                                    "Next workout: ${firstCycle.currentCycleLabel()}"
                                } else {
                                    "الدور الحالي: ${firstCycle.currentCycleLabel()}"
                                },
                                color = colors.primary,
                                fontSize = (11f * textScale).sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onTaskEditClick(firstCycle) }
                            )
                        }
                    }
                }
            }
        }

        if (prefs.showFilterChips) {
            TaskFilterBar(
                lang = lang,
                activeFilter = activeFilter,
                onFilterChange = onFilterChange
            )
        }

        if (prefs.showSearchBar) {
            TaskSearchBar(
                lang = lang,
                searchQuery = searchQuery,
                onSearchChange = onSearchChange
            )
        }

        TasksListCard(
            prefs = prefs,
            dueTasks = dueTasks,
            smartIconSettings = smartIconSettings,
            tasksState = tasksState,
            onDuePillClick = { onOpenOverview(null) },
            onTaskPhotoClick = onTaskPhotoClick,
            onTaskEditClick = onTaskEditClick,
            onTaskDeleteClick = onTaskDeleteClick,
            onTaskBadgeTap = onTaskBadgeTap,
            onTaskBadgeDrag = onTaskBadgeDrag
        )

        if (prefs.showQuickDock) {
            val nearestTask = dueTasks.firstOrNull()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = colors.primary,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(onClick = onAddTaskClick)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = AppStrings.get(lang, "newTask"),
                            color = Color.White,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                if (nearestTask != null) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = colors.cardBg,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .border(1.dp, colors.borderSoft, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onPreviewTaskAlert(nearestTask) }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.NotificationsActive, contentDescription = null, tint = colors.primary, modifier = Modifier.size(17.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = AppStrings.get(lang, "testAlert"),
                                color = colors.textDark,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))
    }
}

/* =========================================================================
   LAYOUT 2: Clean Slate Minimalist Hub (Screenshot-Inspired Sheet + Cards)
   ========================================================================= */
@Composable
private fun TasksWorkspaceL2(
    prefs: UserPreferences,
    allTasks: List<SmartTask>,
    dueTasks: List<SmartTask>,
    smartIconSettings: SmartIconSettings,
    dueTodayTotal: Int,
    tasksState: UiState<List<SmartTask>>,
    searchQuery: String,
    activeFilter: TaskFilter,
    onFilterChange: (TaskFilter) -> Unit,
    onSearchChange: (String) -> Unit,
    onBannerPhotoClick: () -> Unit,
    onBannerBodyClick: () -> Unit,
    onAddTaskClick: () -> Unit,
    onTaskPhotoClick: (String) -> Unit,
    onTaskEditClick: (SmartTask) -> Unit,
    onTaskDeleteClick: (String) -> Unit,
    onTaskBadgeTap: (SmartTask) -> Unit,
    onTaskBadgeDrag: (String, Float, Float) -> Unit,
    onPreviewTaskAlert: (SmartTask) -> Unit,
    onOpenOverview: (Int?) -> Unit
) {
    val colors = LocalMNotesColors.current
    val textScale = LocalMNotesTextScale.current
    val cardScale = LocalMNotesCardScale.current
    val bannerDp = bannerImageDp(prefs.bannerImgSize, prefs.bannerImgDp)
    val lang = prefs.lang
    val nextTaskWithTime = remember(dueTasks) {
        dueTasks.firstOrNull { it.alertTime.isNotBlank() } ?: dueTasks.firstOrNull()
    }
    val heroNumber = remember(nextTaskWithTime, dueTodayTotal) {
        if (nextTaskWithTime != null && nextTaskWithTime.alertTime.isNotBlank()) {
            computeCountdownLabel(nextTaskWithTime.alertTime)
        } else {
            "$dueTodayTotal"
        }
    }
    val heroUnit = remember(nextTaskWithTime, lang) {
        if (nextTaskWithTime != null && nextTaskWithTime.alertTime.isNotBlank()) {
            if (lang == "en") "TO NEXT" else "للتنبيه"
        } else {
            if (lang == "en") "TASKS" else "مهام"
        }
    }
    val cycleTasksCount = remember(allTasks) { allTasks.count { it.repeat == RepeatMode.CYCLE } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (prefs.showBanner || prefs.showStatsSummary) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (prefs.showBanner) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box {
                                MediaAvatarSlot(
                                    imgUri = prefs.bannerImg,
                                    size = bannerDp,
                                    shapeId = prefs.bannerPhotoShape,
                                    useBannerDefaultIfBlank = true,
                                    contentDescription = prefs.bannerTitle,
                                    onClick = onBannerPhotoClick
                                )
                                Box(
                                    modifier = Modifier
                                        .size(13.dp)
                                        .align(Alignment.BottomEnd)
                                        .clip(CircleShape)
                                        .background(colors.primary)
                                        .border(2.dp, colors.cardBg, CircleShape)
                                        .clickable(onClick = onBannerPhotoClick)
                                )
                            }
                            Column(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable(onClick = onBannerBodyClick)
                                    .padding(vertical = 2.dp)
                            ) {
                                Text(
                                    text = prefs.bannerTitle.ifBlank { AppStrings.get(lang, "appName") },
                                    color = colors.textDark,
                                    fontSize = (15f * textScale).sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = prefs.bannerSub.ifBlank { nextTaskWithTime?.title ?: AppStrings.get(lang, "curTasks") },
                                    color = colors.textMuted,
                                    fontSize = (11.5f * textScale).sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    if (nextTaskWithTime != null) onPreviewTaskAlert(nextTaskWithTime)
                                    else onOpenOverview(null)
                                }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text(
                                    text = heroUnit,
                                    color = colors.primary,
                                    fontSize = (12f * textScale).sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(bottom = 5.dp)
                                )
                                Text(
                                    text = heroNumber,
                                    color = colors.textDark,
                                    fontSize = (30f * textScale).sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Text(
                                text = if (lang == "en") "Total Today: $dueTodayTotal" else "إجمالي مهام اليوم: $dueTodayTotal",
                                color = colors.textMuted,
                                fontSize = (11f * textScale).sp
                            )
                        }
                    }
                }

                if (prefs.showStatsSummary) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = colors.cardBg,
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, colors.borderSoft, RoundedCornerShape(18.dp))
                                .clip(RoundedCornerShape(18.dp))
                                .clickable {
                                    onFilterChange(if (activeFilter == TaskFilter.GYM) TaskFilter.ALL else TaskFilter.GYM)
                                }
                        ) {
                            Column(
                                modifier = Modifier.padding((14f * cardScale).dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "$cycleTasksCount ${if (lang == "en") "Active" else "نشطة"}",
                                    color = colors.textDark,
                                    fontSize = (17f * textScale).sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = if (lang == "en") "Gym & Rotation" else "دورات الجيم والتكرار",
                                    color = colors.textMuted,
                                    fontSize = (11.5f * textScale).sp
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = colors.cardBg,
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, colors.borderSoft, RoundedCornerShape(18.dp))
                                .clip(RoundedCornerShape(18.dp))
                                .clickable { onOpenOverview(null) }
                        ) {
                            Column(
                                modifier = Modifier.padding((14f * cardScale).dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "${allTasks.size} ${if (lang == "en") "Tasks" else "مهمة"}",
                                    color = colors.textDark,
                                    fontSize = (17f * textScale).sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = if (lang == "en") "All Scheduled Tasks" else "إجمالي المهام المجدولة",
                                    color = colors.textMuted,
                                    fontSize = (11.5f * textScale).sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Crisp Rounded Sheet Panel (unclipped outer Box so task badges can drag freely)
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .shadow(2.dp, RoundedCornerShape(26.dp), clip = false)
                    .clip(RoundedCornerShape(26.dp))
                    .background(colors.cardBg)
                    .border(1.dp, colors.borderSoft, RoundedCornerShape(26.dp))
            )
            Column(
                modifier = Modifier.padding((14f * cardScale).dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = AppStrings.get(lang, "curTasks"),
                        color = colors.textDark,
                        fontSize = (15.5f * textScale).sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onOpenOverview(null) }
                    )
                    if (prefs.showQuickDock) {
                        Surface(
                            shape = CircleShape,
                            color = colors.primary,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .clickable(onClick = onAddTaskClick)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.Add, contentDescription = AppStrings.get(lang, "add"), tint = Color.White, modifier = Modifier.size(19.dp))
                            }
                        }
                    }
                }

                if (prefs.showWeekStrip) {
                    WeekStripRow(
                        lang = lang,
                        allTasks = allTasks,
                        onDayClick = { dayIdx -> onOpenOverview(dayIdx) }
                    )
                }

                if (prefs.showSearchBar) {
                    TaskSearchBar(
                        lang = lang,
                        searchQuery = searchQuery,
                        onSearchChange = onSearchChange
                    )
                }

                if (prefs.showFilterChips) {
                    TaskFilterBar(
                        lang = lang,
                        activeFilter = activeFilter,
                        onFilterChange = onFilterChange
                    )
                }

                TasksListInner(
                    prefs = prefs,
                    dueTasks = dueTasks,
                    smartIconSettings = smartIconSettings,
                    tasksState = tasksState,
                    onTaskPhotoClick = onTaskPhotoClick,
                    onTaskEditClick = onTaskEditClick,
                    onTaskDeleteClick = onTaskDeleteClick,
                    onTaskBadgeTap = onTaskBadgeTap,
                    onTaskBadgeDrag = onTaskBadgeDrag
                )
            }
        }

        Spacer(Modifier.height(14.dp))
    }
}

/* =========================================================================
   LAYOUT 3: Curved Gradient Header + "Up Next" Spotlight
   ========================================================================= */
@Composable
private fun TasksWorkspaceL3(
    prefs: UserPreferences,
    allTasks: List<SmartTask>,
    dueTasks: List<SmartTask>,
    smartIconSettings: SmartIconSettings,
    dueTodayTotal: Int,
    tasksState: UiState<List<SmartTask>>,
    searchQuery: String,
    activeFilter: TaskFilter,
    onFilterChange: (TaskFilter) -> Unit,
    onSearchChange: (String) -> Unit,
    onBannerPhotoClick: () -> Unit,
    onBannerBodyClick: () -> Unit,
    onAddTaskClick: () -> Unit,
    onTaskPhotoClick: (String) -> Unit,
    onTaskEditClick: (SmartTask) -> Unit,
    onTaskDeleteClick: (String) -> Unit,
    onTaskBadgeTap: (SmartTask) -> Unit,
    onTaskBadgeDrag: (String, Float, Float) -> Unit,
    onPreviewTaskAlert: (SmartTask) -> Unit,
    onOpenOverview: (Int?) -> Unit
) {
    val colors = LocalMNotesColors.current
    val textScale = LocalMNotesTextScale.current
    val bannerDp = bannerImageDp(prefs.bannerImgSize, prefs.bannerImgDp)
    val taskDp = taskImageDp(prefs.taskImgSize, prefs.taskImgDp)
    val lang = prefs.lang
    val spotlightTask = dueTasks.firstOrNull() ?: allTasks.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (prefs.showBanner) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, colors.borderSoft, RoundedCornerShape(24.dp))
                    .clickable(onClick = onBannerBodyClick)
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(colors.primary, colors.accent.copy(alpha = 0.88f))
                        )
                    )
                    val wavePath = Path().apply {
                        moveTo(0f, size.height * 0.65f)
                        cubicTo(
                            size.width * 0.35f, size.height * 0.45f,
                            size.width * 0.68f, size.height * 0.92f,
                            size.width, size.height * 0.58f
                        )
                        lineTo(size.width, size.height)
                        lineTo(0f, size.height)
                        close()
                    }
                    drawPath(path = wavePath, color = Color.White.copy(alpha = 0.14f))
                    drawCircle(
                        color = Color.White.copy(alpha = 0.10f),
                        radius = size.height * 0.48f,
                        center = Offset(size.width * 0.88f, size.height * 0.22f)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MediaAvatarSlot(
                            imgUri = prefs.bannerImg,
                            size = bannerDp,
                            shapeId = prefs.bannerPhotoShape,
                            useBannerDefaultIfBlank = true,
                            contentDescription = prefs.bannerTitle,
                            onClick = onBannerPhotoClick
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = prefs.bannerTitle.ifBlank { AppStrings.get(lang, "appName") },
                                color = Color.White,
                                fontSize = (17f * textScale).sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = prefs.bannerSub.ifBlank {
                                    if (lang == "en") {
                                        "$dueTodayTotal active today • ${allTasks.size} tasks"
                                    } else {
                                        "$dueTodayTotal مهام نشطة اليوم • ${allTasks.size} الإجمالي"
                                    }
                                },
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = (11.5f * textScale).sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (prefs.showQuickDock) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.22f))
                                .clickable(onClick = onAddTaskClick)
                                .padding(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = AppStrings.get(lang, "add"),
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        if (prefs.showSpotlightCard && spotlightTask != null) {
            var spotlightBadgeDragging by remember(spotlightTask.id) { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(if (spotlightBadgeDragging) 100f else 1f)
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .shadow(3.dp, RoundedCornerShape(22.dp), clip = false)
                        .clip(RoundedCornerShape(22.dp))
                        .background(colors.cardBg)
                        .border(1.5.dp, colors.primary.copy(alpha = 0.45f), RoundedCornerShape(22.dp))
                )
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(colors.primarySoft)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (lang == "en") "UP NEXT SPOTLIGHT" else "المهمة القادمة الآن",
                                color = colors.primary,
                                fontSize = (10.5f * textScale).sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        if (spotlightTask.alertTime.isNotBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Alarm,
                                    contentDescription = null,
                                    tint = colors.accent,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = AppStrings.formatTime(spotlightTask.alertTime, lang),
                                    color = colors.textDark,
                                    fontSize = (11.5f * textScale).sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TaskCircleWithFloatingBadge(
                            task = spotlightTask,
                            imgDp = taskDp,
                            photoShape = prefs.cardShape,
                            isDemo = false,
                            isResting = !spotlightTask.isActiveOnDate(),
                            smartIconSettings = smartIconSettings,
                            onPhotoClick = { onTaskPhotoClick(spotlightTask.id) },
                            onBadgeTap = onTaskBadgeTap,
                            onBadgeDrag = onTaskBadgeDrag,
                            onDraggingChanged = { spotlightBadgeDragging = it }
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = spotlightTask.title,
                                color = colors.textDark,
                                fontSize = (15.5f * textScale).sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            val detail = listOf(
                                spotlightTask.currentCycleLabel(),
                                spotlightTask.meta
                            ).filter { it.isNotBlank() }.joinToString(" • ")
                            if (detail.isNotBlank()) {
                                Text(
                                    text = detail,
                                    color = colors.textMuted,
                                    fontSize = (12f * textScale).sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.primarySoft)
                                .clickable { onPreviewTaskAlert(spotlightTask) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = AppStrings.get(lang, "testAlert"),
                                color = colors.primary,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.bgMain)
                                .border(1.dp, colors.borderSoft, RoundedCornerShape(12.dp))
                                .clickable { onTaskEditClick(spotlightTask) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = AppStrings.get(lang, "editTask"),
                                color = colors.textDark,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        if (prefs.showWeekStrip) {
            WeekStripRow(
                lang = lang,
                allTasks = allTasks,
                onDayClick = { dayIdx -> onOpenOverview(dayIdx) }
            )
        }

        if (prefs.showSearchBar) {
            TaskSearchBar(
                lang = lang,
                searchQuery = searchQuery,
                onSearchChange = onSearchChange
            )
        }

        if (prefs.showFilterChips) {
            TaskFilterBar(
                lang = lang,
                activeFilter = activeFilter,
                onFilterChange = onFilterChange
            )
        }

        TasksListCard(
            prefs = prefs,
            dueTasks = dueTasks,
            smartIconSettings = smartIconSettings,
            tasksState = tasksState,
            onDuePillClick = { onOpenOverview(null) },
            onTaskPhotoClick = onTaskPhotoClick,
            onTaskEditClick = onTaskEditClick,
            onTaskDeleteClick = onTaskDeleteClick,
            onTaskBadgeTap = onTaskBadgeTap,
            onTaskBadgeDrag = onTaskBadgeDrag
        )

        Spacer(Modifier.height(14.dp))
    }
}

/* =========================================================================
   Shared Sub-Components
   ========================================================================= */

@Composable
private fun StatMiniChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit = {}
) {
    val colors = LocalMNotesColors.current
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(colors.primarySoft)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(12.dp))
        Text(text = label, color = colors.primary, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun TaskFilterBar(
    lang: String,
    activeFilter: TaskFilter,
    onFilterChange: (TaskFilter) -> Unit
) {
    val colors = LocalMNotesColors.current
    val items = listOf(
        TaskFilter.ALL to (if (lang == "en") "All" else "الكل"),
        TaskFilter.GYM to (if (lang == "en") "Gym & Cycles" else "الجيم والدورات"),
        TaskFilter.ALERTS to (if (lang == "en") "With Alert" else "بتنبيه"),
        TaskFilter.DAILY to (if (lang == "en") "Daily" else "يومي")
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items.forEach { (filter, label) ->
            val selected = activeFilter == filter
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (selected) colors.primary else colors.cardBg)
                    .border(
                        1.dp,
                        if (selected) colors.primary else colors.borderSoft,
                        RoundedCornerShape(999.dp)
                    )
                    .clickable { onFilterChange(filter) }
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Text(
                    text = label,
                    color = if (selected) Color.White else colors.textMuted,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun WeekStripRow(
    lang: String,
    allTasks: List<SmartTask>,
    onDayClick: (Int) -> Unit = {}
) {
    val colors = LocalMNotesColors.current
    val labels = AppStrings.dayNames(lang)
    val todayJsDay = (LocalDate.now().dayOfWeek.value % 7).toLong()
    val scheduledDays = remember(allTasks) {
        allTasks.flatMap { it.weekDays }.toSet()
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        labels.forEachIndexed { idx, name ->
            val dayNum = idx.toLong()
            val isToday = dayNum == todayJsDay
            val hasTasks = scheduledDays.contains(dayNum)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isToday) colors.primarySoft else colors.bgMain)
                    .border(
                        1.dp,
                        if (isToday) colors.primary else colors.borderSoft,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onDayClick(idx) }
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = name,
                    color = if (isToday) colors.primary else colors.textMuted,
                    fontSize = 9.5.sp,
                    fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Bold,
                    maxLines = 1
                )
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isToday -> colors.primary
                                hasTasks -> colors.accent
                                else -> Color.Transparent
                            }
                        )
                )
            }
        }
    }
}

@Composable
private fun TaskSearchBar(
    lang: String,
    searchQuery: String,
    onSearchChange: (String) -> Unit
) {
    val colors = LocalMNotesColors.current
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchChange,
        placeholder = {
            Text(
                text = AppStrings.get(lang, "search"),
                color = colors.textMuted,
                fontSize = 13.sp
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                tint = colors.textMuted
            )
        },
        trailingIcon = {
            if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { onSearchChange("") }) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = AppStrings.get(lang, "close"),
                        tint = colors.textMuted
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(999.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colors.primary,
            unfocusedBorderColor = colors.borderSoft,
            focusedContainerColor = colors.cardBg,
            unfocusedContainerColor = colors.bgMain
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("task_search_input")
    )
}

@Composable
private fun TasksListCard(
    prefs: UserPreferences,
    dueTasks: List<SmartTask>,
    smartIconSettings: SmartIconSettings,
    tasksState: UiState<List<SmartTask>>,
    onDuePillClick: () -> Unit = {},
    onTaskPhotoClick: (String) -> Unit,
    onTaskEditClick: (SmartTask) -> Unit,
    onTaskDeleteClick: (String) -> Unit,
    onTaskBadgeTap: (SmartTask) -> Unit,
    onTaskBadgeDrag: (String, Float, Float) -> Unit
) {
    val colors = LocalMNotesColors.current
    val textScale = LocalMNotesTextScale.current
    val cardScale = LocalMNotesCardScale.current
    val lang = prefs.lang
    // Outer Box does NOT clip so task circle badges can drag past card boundaries
    Box(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .shadow(2.dp, RoundedCornerShape(22.dp), clip = false)
                .clip(RoundedCornerShape(22.dp))
                .background(colors.cardBg)
                .border(1.dp, colors.borderSoft, RoundedCornerShape(22.dp))
        )
        Column(
            modifier = Modifier.padding((14f * cardScale).dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = AppStrings.get(lang, "curTasks"),
                    color = colors.textDark,
                    fontSize = (14.5f * textScale).sp,
                    fontWeight = FontWeight.ExtraBold
                )
                val activeCount = dueTasks.count { it.isActiveOnDate() }
                val restingCount = (dueTasks.size - activeCount).coerceAtLeast(0)
                val pillLabel = when {
                    restingCount > 0 && lang == "en" -> "$activeCount active • $restingCount Zzz"
                    restingCount > 0 -> "$activeCount نشط • $restingCount راحة"
                    else -> "$activeCount ${AppStrings.get(lang, "forToday")}"
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(colors.primarySoft)
                        .clickable(onClick = onDuePillClick)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = pillLabel,
                        color = colors.primary,
                        fontSize = (11.5f * textScale).sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            TasksListInner(
                prefs = prefs,
                dueTasks = dueTasks,
                smartIconSettings = smartIconSettings,
                tasksState = tasksState,
                onTaskPhotoClick = onTaskPhotoClick,
                onTaskEditClick = onTaskEditClick,
                onTaskDeleteClick = onTaskDeleteClick,
                onTaskBadgeTap = onTaskBadgeTap,
                onTaskBadgeDrag = onTaskBadgeDrag
            )
        }
    }
}

@Composable
private fun TasksListInner(
    prefs: UserPreferences,
    dueTasks: List<SmartTask>,
    smartIconSettings: SmartIconSettings,
    tasksState: UiState<List<SmartTask>>,
    onTaskPhotoClick: (String) -> Unit,
    onTaskEditClick: (SmartTask) -> Unit,
    onTaskDeleteClick: (String) -> Unit,
    onTaskBadgeTap: (SmartTask) -> Unit,
    onTaskBadgeDrag: (String, Float, Float) -> Unit
) {
    val colors = LocalMNotesColors.current
    val textScale = LocalMNotesTextScale.current
    val lang = prefs.lang
    when (tasksState) {
        is UiState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colors.primary)
            }
        }
        is UiState.Error -> {
            Text(
                text = tasksState.message,
                color = Color(0xFFEF4444),
                fontSize = (12.5f * textScale).sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            )
        }
        is UiState.Success -> {
            if (dueTasks.isEmpty()) {
                Text(
                    text = AppStrings.get(lang, "allDone"),
                    color = colors.textMuted,
                    fontSize = (12.5f * textScale).sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 22.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    dueTasks.forEach { task ->
                        TaskCard(
                            task = task,
                            lang = lang,
                            cardShape = prefs.cardShape,
                            taskImgSize = prefs.taskImgSize,
                            taskImgDp = prefs.taskImgDp,
                            smartIconSettings = smartIconSettings,
                            onPhotoClick = { onTaskPhotoClick(task.id) },
                            onEditClick = { onTaskEditClick(task) },
                            onDeleteClick = { onTaskDeleteClick(task.id) },
                            onBadgeTap = onTaskBadgeTap,
                            onBadgeDrag = onTaskBadgeDrag
                        )
                    }
                }
            }
        }
    }
}

private fun computeCountdownLabel(alertHm: String): String {
    val parts = alertHm.split(":")
    val h = parts.getOrNull(0)?.toIntOrNull() ?: return alertHm
    val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
    val now = LocalTime.now()
    val target = LocalTime.of(h.coerceIn(0, 23), m.coerceIn(0, 59))
    var mins = ChronoUnit.MINUTES.between(now, target)
    if (mins < 0) mins += 24 * 60
    val hh = mins / 60
    val mm = mins % 60
    return if (hh > 0) "${hh}س ${mm}د" else "${mm}د"
}
