package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.localization.AppStrings
import com.example.model.RepeatMode
import com.example.model.ScreenDesignConfig
import com.example.model.ScreenDesignPresets
import com.example.model.SmartIconSettings
import com.example.model.SmartTask
import com.example.ui.theme.LocalMNotesColors
import java.time.LocalDate
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

internal data class ResolvedScreenPalette(
    val bgStart: Color,
    val bgEnd: Color,
    val cardBg: Color,
    val accent: Color,
    val secondary: Color,
    val textDark: Color,
    val textMuted: Color,
    val borderSoft: Color,
    val accentSoft: Color
)

@Composable
internal fun resolveScreenPalette(config: ScreenDesignConfig): ResolvedScreenPalette {
    val appColors = LocalMNotesColors.current
    val useAppTheme = config.paletteId == "app_theme"
    val bgStart = if (useAppTheme) appColors.bgTopGlow else ScreenDesignPresets.parseHexColor(config.bgGradientStartHex, appColors.bgTopGlow)
    val bgEnd = if (useAppTheme) appColors.bgMain else ScreenDesignPresets.parseHexColor(config.bgGradientEndHex, appColors.bgMain)
    val cardBg = if (useAppTheme) appColors.cardBg else ScreenDesignPresets.parseHexColor(config.cardSurfaceHex, appColors.cardBg)
    val accent = if (useAppTheme) appColors.primary else ScreenDesignPresets.parseHexColor(config.accentHex, appColors.primary)
    val secondary = if (useAppTheme) appColors.accent else ScreenDesignPresets.parseHexColor(config.secondaryAccentHex, appColors.accent)
    return ResolvedScreenPalette(
        bgStart = bgStart,
        bgEnd = bgEnd,
        cardBg = cardBg,
        accent = accent,
        secondary = secondary,
        textDark = appColors.textDark,
        textMuted = appColors.textMuted,
        borderSoft = accent.copy(alpha = 0.22f),
        accentSoft = accent.copy(alpha = 0.13f)
    )
}

internal fun cardShapeForConfig(shapeId: String, cornerRadiusDp: Int): Shape = when (shapeId) {
    "pill" -> RoundedCornerShape(999.dp)
    "organic_blob" -> OrganicBlobShape
    "squircle" -> RoundedCornerShape(percent = 28)
    "cut_corner" -> CutCornerShape(cornerRadiusDp.coerceIn(8, 28).dp)
    "circle_bubble" -> CircleShape
    "frameless" -> RoundedCornerShape(14.dp)
    else -> RoundedCornerShape(cornerRadiusDp.coerceIn(6, 38).dp)
}

/**
 * Background gradient + decorative ambient art layer for any applied or previewed ScreenDesignConfig.
 */
@Composable
fun ScreenDesignBackdrop(
    config: ScreenDesignConfig,
    modifier: Modifier = Modifier
) {
    val pal = resolveScreenPalette(config)
    Box(
        modifier = modifier.background(
            Brush.verticalGradient(listOf(pal.bgStart, pal.bgEnd))
        )
    ) {
        if (config.showDecorations && config.decorationStyle != "none") {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                when (config.decorationStyle) {
                    "soft_orbs" -> {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(pal.accent.copy(alpha = 0.14f), Color.Transparent),
                                center = Offset(w * 0.88f, h * 0.10f),
                                radius = w * 0.45f
                            ),
                            radius = w * 0.45f,
                            center = Offset(w * 0.88f, h * 0.10f)
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(pal.secondary.copy(alpha = 0.12f), Color.Transparent),
                                center = Offset(w * 0.12f, h * 0.72f),
                                radius = w * 0.42f
                            ),
                            radius = w * 0.42f,
                            center = Offset(w * 0.12f, h * 0.72f)
                        )
                    }
                    "pastel_waves" -> {
                        val topWave = Path().apply {
                            moveTo(0f, h * 0.14f)
                            cubicTo(w * 0.30f, h * 0.04f, w * 0.68f, h * 0.24f, w, h * 0.12f)
                            lineTo(w, 0f)
                            lineTo(0f, 0f)
                            close()
                        }
                        drawPath(topWave, color = pal.accent.copy(alpha = 0.09f))
                        val bottomWave = Path().apply {
                            moveTo(0f, h * 0.86f)
                            cubicTo(w * 0.32f, h * 0.78f, w * 0.66f, h * 0.94f, w, h * 0.82f)
                            lineTo(w, h)
                            lineTo(0f, h)
                            close()
                        }
                        drawPath(bottomWave, color = pal.secondary.copy(alpha = 0.09f))
                    }
                    "doodle_dots" -> {
                        val step = 54.dp.toPx().coerceAtLeast(36f)
                        var y = step * 0.5f
                        var row = 0
                        while (y < h) {
                            var x = if (row % 2 == 0) step * 0.45f else step * 0.95f
                            var col = 0
                            while (x < w) {
                                val dotColor = if ((row + col) % 2 == 0) pal.accent else pal.secondary
                                drawCircle(
                                    color = dotColor.copy(alpha = 0.11f),
                                    radius = if ((row + col) % 3 == 0) 4.dp.toPx() else 2.2.dp.toPx(),
                                    center = Offset(x, y)
                                )
                                x += step
                                col++
                            }
                            y += step
                            row++
                        }
                    }
                    "floating_sparkles" -> {
                        val pts = listOf(
                            Offset(w * 0.14f, h * 0.12f) to 10.dp.toPx(),
                            Offset(w * 0.86f, h * 0.16f) to 12.dp.toPx(),
                            Offset(w * 0.78f, h * 0.44f) to 8.dp.toPx(),
                            Offset(w * 0.12f, h * 0.62f) to 11.dp.toPx(),
                            Offset(w * 0.88f, h * 0.80f) to 9.dp.toPx(),
                            Offset(w * 0.24f, h * 0.88f) to 7.dp.toPx()
                        )
                        pts.forEachIndexed { idx, (center, r) ->
                            val c = if (idx % 2 == 0) pal.accent.copy(alpha = 0.20f) else pal.secondary.copy(alpha = 0.20f)
                            val starPath = Path().apply {
                                moveTo(center.x, center.y - r)
                                quadraticTo(center.x, center.y, center.x + r, center.y)
                                quadraticTo(center.x, center.y, center.x, center.y + r)
                                quadraticTo(center.x, center.y, center.x - r, center.y)
                                quadraticTo(center.x, center.y, center.x, center.y - r)
                                close()
                            }
                            drawPath(starPath, color = c)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Renders the full Main Screen Tasks workspace according to any [ScreenDesignConfig]
 * (Options A, B, C, D or any user-customized saved version).
 * Works identically both on the live Main Screen AND inside the Settings Interactive Preview!
 */
@Composable
fun CustomScreenThemeWorkspace(
    config: ScreenDesignConfig,
    allTasks: List<SmartTask>,
    smartIconSettings: SmartIconSettings,
    requestsCount: Int,
    lang: String,
    searchQuery: String = "",
    onSearchChange: (String) -> Unit = {},
    onAddTaskClick: () -> Unit = {},
    onTaskPhotoClick: (String) -> Unit = {},
    onTaskEditClick: (SmartTask) -> Unit = {},
    onTaskDeleteClick: (String) -> Unit = {},
    onTaskBadgeTap: (SmartTask) -> Unit = {},
    onTaskBadgeDrag: (taskId: String, offsetX: Float, offsetY: Float) -> Unit = { _, _, _ -> },
    onPreviewTaskAlert: (SmartTask) -> Unit = {},
    onOpenOverview: (Int?) -> Unit = {},
    onOpenRequestsPage: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    isPreview: Boolean = false,
    compactScale: Float = 1f,
    modifier: Modifier = Modifier
) {
    val pal = resolveScreenPalette(config)
    val scale = (config.textScalePercent / 100f) * compactScale
    val todayDate = LocalDate.now()
    var selectedFilter by remember { mutableStateOf("all") }

    val filteredTasks = remember(allTasks, searchQuery, selectedFilter) {
        val q = searchQuery.trim().lowercase()
        allTasks
            .filter { t ->
                val matchesQuery = q.isEmpty() ||
                    t.title.lowercase().contains(q) ||
                    t.meta.lowercase().contains(q) ||
                    t.currentCycleLabel().lowercase().contains(q)
                val matchesFilter = when (selectedFilter) {
                    "today" -> t.isActiveOnDate(todayDate)
                    "gym" -> t.repeat == RepeatMode.CYCLE ||
                        t.usesWeekDays() ||
                        t.title.contains("جيم") ||
                        t.title.contains("تمرين") ||
                        t.title.lowercase().contains("gym")
                    "alerts" -> t.alertEnabled && t.alertTime.isNotBlank()
                    else -> true
                }
                matchesQuery && matchesFilter
            }
            .sortedByDescending { it.isActiveOnDate(todayDate) }
    }

    val activeTasks = remember(filteredTasks) { filteredTasks.filter { it.isActiveOnDate(todayDate) } }
    val restingTasks = remember(filteredTasks) { filteredTasks.filter { !it.isActiveOnDate(todayDate) } }
    val dueTodayTotal = remember(allTasks) { allTasks.count { it.isActiveOnDate(todayDate) } }
    val spotlightTask = remember(activeTasks, filteredTasks) {
        activeTasks.firstOrNull { it.alertEnabled && it.alertTime.isNotBlank() }
            ?: activeTasks.firstOrNull()
            ?: filteredTasks.firstOrNull()
    }

    val orderedSections = remember(config.sectionOrder) {
        val base = config.sectionOrder.ifEmpty { ScreenDesignPresets.defaultSectionOrder }
        val missing = ScreenDesignPresets.defaultSectionOrder.filterNot { it in base }
        base + missing
    }

    val scrollState = rememberScrollState()
    val sectionGap = (config.itemSpacingDp.coerceIn(4, 22) * compactScale).dp

    Box(modifier = modifier.fillMaxSize()) {
        if (isPreview) {
            ScreenDesignBackdrop(config = config, modifier = Modifier.matchParentSize())
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(
                    start = if (isPreview) (10f * compactScale).dp else 2.dp,
                    end = if (isPreview) (10f * compactScale).dp else 2.dp,
                    top = if (isPreview) (10f * compactScale).dp else 2.dp,
                    bottom = if (config.showBottomNavBar || config.addButtonStyle != "hidden") {
                        (82f * compactScale).dp
                    } else {
                        (18f * compactScale).dp
                    }
                ),
            verticalArrangement = Arrangement.spacedBy(sectionGap)
        ) {
            orderedSections.forEach { sectionId ->
                when (sectionId) {
                    "greeting" -> if (config.showGreetingHeader) {
                        ThemeGreetingHeader(
                            lang = lang,
                            dueTodayTotal = dueTodayTotal,
                            totalCount = allTasks.size,
                            pal = pal,
                            scale = scale,
                            compactScale = compactScale,
                            isPreview = isPreview,
                            onOpenOverview = { onOpenOverview(null) },
                            onOpenSettings = onOpenSettings
                        )
                    }

                    "week_strip" -> if (config.showWeekStrip) {
                        ThemeWeekStrip(
                            lang = lang,
                            pal = pal,
                            config = config,
                            scale = scale,
                            compactScale = compactScale,
                            onSelectDay = { dayIdx -> onOpenOverview(dayIdx) }
                        )
                    }

                    "progress" -> if (config.showProgressWidget && config.progressStyle != "hidden") {
                        ThemeProgressWidget(
                            style = config.progressStyle,
                            dueTodayTotal = dueTodayTotal,
                            totalTasks = allTasks.size,
                            requestsCount = requestsCount,
                            lang = lang,
                            pal = pal,
                            config = config,
                            scale = scale,
                            compactScale = compactScale,
                            onClick = { onOpenOverview(null) }
                        )
                    }

                    "spotlight" -> if (config.showSpotlightUpNext && spotlightTask != null) {
                        ThemeSpotlightCard(
                            task = spotlightTask,
                            lang = lang,
                            pal = pal,
                            config = config,
                            smartIconSettings = smartIconSettings,
                            scale = scale,
                            compactScale = compactScale,
                            isPreview = isPreview,
                            onPhotoClick = { onTaskPhotoClick(spotlightTask.id) },
                            onEditClick = { onTaskEditClick(spotlightTask) },
                            onAlertPreview = { onPreviewTaskAlert(spotlightTask) },
                            onBadgeTap = onTaskBadgeTap,
                            onBadgeDrag = onTaskBadgeDrag
                        )
                    }

                    "filters" -> if (config.showFilterChips) {
                        ThemeFilterBar(
                            style = config.filterStyle,
                            selected = selectedFilter,
                            lang = lang,
                            pal = pal,
                            scale = scale,
                            compactScale = compactScale,
                            onSelect = { selectedFilter = it }
                        )
                    }

                    "search" -> if (config.showSearchBar) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchChange,
                            singleLine = true,
                            readOnly = isPreview,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Search,
                                    contentDescription = null,
                                    tint = pal.accent,
                                    modifier = Modifier.size((18f * compactScale).dp)
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotBlank() && !isPreview) {
                                    IconButton(onClick = { onSearchChange("") }) {
                                        Icon(Icons.Rounded.Close, contentDescription = null, tint = pal.textMuted)
                                    }
                                }
                            },
                            placeholder = {
                                Text(
                                    text = if (lang == "en") "Search tasks..." else "ابحث في المهام...",
                                    fontSize = (12.5f * scale).sp,
                                    color = pal.textMuted
                                )
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = pal.cardBg,
                                unfocusedContainerColor = pal.cardBg,
                                focusedBorderColor = pal.accent,
                                unfocusedBorderColor = pal.borderSoft
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    "tasks" -> {
                        val tasksToRender = if (config.showSpotlightUpNext && config.layoutMode == "spotlight_agenda" && filteredTasks.size > 1 && spotlightTask != null) {
                            filteredTasks.filterNot { it.id == spotlightTask.id }
                        } else {
                            filteredTasks
                        }

                        if (tasksToRender.isEmpty()) {
                            Surface(
                                shape = cardShapeForConfig(config.cardShape, config.cardCornerRadiusDp),
                                color = pal.cardBg,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, pal.borderSoft, cardShapeForConfig(config.cardShape, config.cardCornerRadiusDp))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding((18f * compactScale).dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.TaskAlt,
                                        contentDescription = null,
                                        tint = pal.accent,
                                        modifier = Modifier.size((28f * compactScale).dp)
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = if (lang == "en") "No tasks in this view yet" else "لا توجد مهام في هذا القسم حالياً",
                                        color = pal.textMuted,
                                        fontSize = (12f * scale).sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        } else if (config.showRestingGroupSeparate && restingTasks.isNotEmpty()) {
                            // Grouped Active Today vs Resting (Zzz) — signature of Option D (customizable on any preset!)
                            if (activeTasks.isNotEmpty()) {
                                GroupSectionHeader(
                                    title = if (lang == "en") "Active Today (${activeTasks.size})" else "نشطة اليوم (${activeTasks.size})",
                                    icon = Icons.Rounded.Bolt,
                                    tint = pal.accent,
                                    scale = scale
                                )
                                RenderTaskCollection(
                                    tasks = activeTasks,
                                    config = config,
                                    pal = pal,
                                    smartIconSettings = smartIconSettings,
                                    lang = lang,
                                    scale = scale,
                                    compactScale = compactScale,
                                    isPreview = isPreview,
                                    onTaskPhotoClick = onTaskPhotoClick,
                                    onTaskEditClick = onTaskEditClick,
                                    onTaskDeleteClick = onTaskDeleteClick,
                                    onTaskBadgeTap = onTaskBadgeTap,
                                    onTaskBadgeDrag = onTaskBadgeDrag,
                                    onPreviewTaskAlert = onPreviewTaskAlert
                                )
                            }
                            GroupSectionHeader(
                                title = if (lang == "en") "Resting / Off-Day Zzz (${restingTasks.size})" else "في الراحة / نائمة Zzz (${restingTasks.size})",
                                icon = Icons.Rounded.Bedtime,
                                tint = pal.textMuted,
                                scale = scale
                            )
                            RenderTaskCollection(
                                tasks = restingTasks,
                                config = config,
                                pal = pal,
                                smartIconSettings = smartIconSettings,
                                lang = lang,
                                scale = scale,
                                compactScale = compactScale,
                                isPreview = isPreview,
                                onTaskPhotoClick = onTaskPhotoClick,
                                onTaskEditClick = onTaskEditClick,
                                onTaskDeleteClick = onTaskDeleteClick,
                                onTaskBadgeTap = onTaskBadgeTap,
                                onTaskBadgeDrag = onTaskBadgeDrag,
                                onPreviewTaskAlert = onPreviewTaskAlert
                            )
                        } else {
                            RenderTaskCollection(
                                tasks = tasksToRender,
                                config = config,
                                pal = pal,
                                smartIconSettings = smartIconSettings,
                                lang = lang,
                                scale = scale,
                                compactScale = compactScale,
                                isPreview = isPreview,
                                onTaskPhotoClick = onTaskPhotoClick,
                                onTaskEditClick = onTaskEditClick,
                                onTaskDeleteClick = onTaskDeleteClick,
                                onTaskBadgeTap = onTaskBadgeTap,
                                onTaskBadgeDrag = onTaskBadgeDrag,
                                onPreviewTaskAlert = onPreviewTaskAlert
                            )
                        }
                    }
                }
            }
        }

        // Bottom Capsule Navigation Bar or Floating Add Action Button
        if (config.showBottomNavBar && config.addButtonStyle == "capsule_bar") {
            ThemeBottomCapsuleBar(
                lang = lang,
                requestsCount = requestsCount,
                pal = pal,
                scale = scale,
                compactScale = compactScale,
                onAddClick = onAddTaskClick,
                onRequestsClick = onOpenRequestsPage,
                onSettingsClick = onOpenSettings,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = (8f * compactScale).dp)
            )
        } else if (config.addButtonStyle != "hidden") {
            ThemeFloatingAddButton(
                style = config.addButtonStyle,
                lang = lang,
                pal = pal,
                scale = scale,
                compactScale = compactScale,
                onClick = onAddTaskClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding((10f * compactScale).dp)
            )
        }
    }
}

@Composable
private fun GroupSectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    scale: Float
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(top = 2.dp, start = 4.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(15.dp))
        Text(
            text = title,
            color = tint,
            fontSize = (11.5f * scale).sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun RenderTaskCollection(
    tasks: List<SmartTask>,
    config: ScreenDesignConfig,
    pal: ResolvedScreenPalette,
    smartIconSettings: SmartIconSettings,
    lang: String,
    scale: Float,
    compactScale: Float,
    isPreview: Boolean,
    onTaskPhotoClick: (String) -> Unit,
    onTaskEditClick: (SmartTask) -> Unit,
    onTaskDeleteClick: (String) -> Unit,
    onTaskBadgeTap: (SmartTask) -> Unit,
    onTaskBadgeDrag: (String, Float, Float) -> Unit,
    onPreviewTaskAlert: (SmartTask) -> Unit
) {
    val gap = (config.itemSpacingDp.coerceIn(4, 22) * compactScale).dp
    if (config.layoutMode == "gallery_2col") {
        Column(verticalArrangement = Arrangement.spacedBy(gap)) {
            tasks.chunked(2).forEach { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(gap)
                ) {
                    pair.forEach { task ->
                        Box(modifier = Modifier.weight(1f)) {
                            ThemeTaskItemCard(
                                task = task,
                                config = config.copy(taskArrangement = "centered_card"),
                                pal = pal,
                                smartIconSettings = smartIconSettings,
                                lang = lang,
                                scale = scale,
                                compactScale = compactScale,
                                isPreview = isPreview,
                                onPhotoClick = { onTaskPhotoClick(task.id) },
                                onEditClick = { onTaskEditClick(task) },
                                onDeleteClick = { onTaskDeleteClick(task.id) },
                                onBadgeTap = onTaskBadgeTap,
                                onBadgeDrag = onTaskBadgeDrag,
                                onPreviewAlert = { onPreviewTaskAlert(task) }
                            )
                        }
                    }
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(gap)) {
            tasks.forEach { task ->
                ThemeTaskItemCard(
                    task = task,
                    config = config,
                    pal = pal,
                    smartIconSettings = smartIconSettings,
                    lang = lang,
                    scale = scale,
                    compactScale = compactScale,
                    isPreview = isPreview,
                    onPhotoClick = { onTaskPhotoClick(task.id) },
                    onEditClick = { onTaskEditClick(task) },
                    onDeleteClick = { onTaskDeleteClick(task.id) },
                    onBadgeTap = onTaskBadgeTap,
                    onBadgeDrag = onTaskBadgeDrag,
                    onPreviewAlert = { onPreviewTaskAlert(task) }
                )
            }
        }
    }
}

@Composable
private fun ThemeTaskItemCard(
    task: SmartTask,
    config: ScreenDesignConfig,
    pal: ResolvedScreenPalette,
    smartIconSettings: SmartIconSettings,
    lang: String,
    scale: Float,
    compactScale: Float,
    isPreview: Boolean,
    onPhotoClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onBadgeTap: (SmartTask) -> Unit,
    onBadgeDrag: (String, Float, Float) -> Unit,
    onPreviewAlert: () -> Unit
) {
    val isResting = !task.isActiveOnDate()
    val cardShape = cardShapeForConfig(config.cardShape, config.cardCornerRadiusDp)
    val isFrameless = config.cardShape == "frameless"
    val borderW = if (isFrameless) 0.dp else config.cardBorderWidthDp.coerceIn(0f, 3.5f).dp
    val elev = if (isFrameless) 0.dp else (config.cardElevationDp.coerceIn(0, 18) * compactScale).dp
    val pad = (config.cardPaddingDp.coerceIn(6, 22) * compactScale).dp
    val imgDp = (config.taskImageSizeDp.coerceIn(40, 112) * compactScale).dp
    var isDraggingBadge by remember(task.id) { mutableStateOf(false) }

    val timeLabel = if (task.alertTime.isNotBlank()) AppStrings.formatTime(task.alertTime, lang) else task.meta
    val cycleLabel = task.currentCycleLabel()

    // Scale the badge smoothly if user customized badgeSizeScalePercent
    val scaledTask = remember(task, config.badgeSizeScalePercent, config.showTaskFloatingBadges) {
        val factor = (config.badgeSizeScalePercent / 100f).coerceIn(0.65f, 1.65f)
        task.copy(
            badgeVisible = task.badgeVisible && config.showTaskFloatingBadges,
            badgeSizeDp = (task.badgeSizeDp * factor * compactScale).coerceIn(18f, 92f)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(if (isDraggingBadge) 100f else 1f)
    ) {
        // Card Background Surface
        if (!isFrameless) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .shadow(elevation = elev, shape = cardShape, clip = false)
                    .clip(cardShape)
                    .background(if (isResting) pal.cardBg.copy(alpha = 0.78f) else pal.cardBg)
                    .then(
                        if (borderW > 0.dp) {
                            Modifier.border(
                                width = borderW,
                                color = if (isResting) pal.borderSoft.copy(alpha = 0.65f) else pal.borderSoft,
                                shape = cardShape
                            )
                        } else Modifier
                    )
            )
        }

        val clickMod = if (!isPreview) Modifier.clickable(onClick = onEditClick) else Modifier

        when (config.taskArrangement) {
            "centered_card" -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(clickMod)
                        .padding(pad),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy((6f * compactScale).dp)
                ) {
                    TaskAvatarRenderSlot(
                        task = scaledTask,
                        imgDp = imgDp,
                        photoShape = config.taskImageShape,
                        showBadge = config.showTaskFloatingBadges,
                        isResting = isResting,
                        isPreview = isPreview,
                        smartIconSettings = smartIconSettings,
                        onPhotoClick = onPhotoClick,
                        onBadgeTap = onBadgeTap,
                        onBadgeDrag = onBadgeDrag,
                        onDraggingChanged = { isDraggingBadge = it }
                    )
                    Text(
                        text = task.title,
                        color = if (isResting) pal.textMuted else pal.textDark,
                        fontSize = (13f * scale).sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (config.showTaskTimePill && timeLabel.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(pal.accentSoft)
                                .padding(horizontal = (8f * compactScale).dp, vertical = (3f * compactScale).dp)
                        ) {
                            Text(
                                text = timeLabel,
                                color = pal.accent,
                                fontSize = (10f * scale).sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                    if (config.showTaskStatusBadges && (cycleLabel.isNotBlank() || isResting)) {
                        Text(
                            text = if (isResting) "Zzz" else cycleLabel,
                            color = if (isResting) pal.textMuted else pal.secondary,
                            fontSize = (9.5f * scale).sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (config.showQuickActionsOnCard && !isPreview) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = onPreviewAlert,
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.NotificationsActive,
                                    contentDescription = AppStrings.get(lang, "tryNow"),
                                    tint = pal.accent,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            IconButton(
                                onClick = onEditClick,
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Edit,
                                    contentDescription = AppStrings.get(lang, "editTask"),
                                    tint = pal.textMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            "row_end" -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(clickMod)
                        .padding(pad),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .alpha(if (isResting) 0.78f else 1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = task.title,
                            color = pal.textDark,
                            fontSize = (13.5f * scale).sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        ThemeTaskMetaRow(
                            timeLabel = timeLabel,
                            cycleLabel = cycleLabel,
                            isResting = isResting,
                            config = config,
                            pal = pal,
                            scale = scale,
                            compactScale = compactScale
                        )
                    }
                    Spacer(Modifier.width((10f * compactScale).dp))
                    TaskAvatarRenderSlot(
                        task = scaledTask,
                        imgDp = imgDp,
                        photoShape = config.taskImageShape,
                        showBadge = config.showTaskFloatingBadges,
                        isResting = isResting,
                        isPreview = isPreview,
                        smartIconSettings = smartIconSettings,
                        onPhotoClick = onPhotoClick,
                        onBadgeTap = onBadgeTap,
                        onBadgeDrag = onBadgeDrag,
                        onDraggingChanged = { isDraggingBadge = it }
                    )
                }
            }

            else -> {
                // "row_start" & "compact_inline"
                val isCompact = config.taskArrangement == "compact_inline" || config.layoutMode == "compact_list"
                val effectiveImg = if (isCompact) (imgDp.value * 0.78f).coerceAtLeast(36f).dp else imgDp
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(clickMod)
                        .padding(
                            horizontal = pad,
                            vertical = if (isCompact) (pad.value * 0.65f).dp else pad
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TaskAvatarRenderSlot(
                            task = scaledTask,
                            imgDp = effectiveImg,
                            photoShape = config.taskImageShape,
                            showBadge = config.showTaskFloatingBadges,
                            isResting = isResting,
                            isPreview = isPreview,
                            smartIconSettings = smartIconSettings,
                            onPhotoClick = onPhotoClick,
                            onBadgeTap = onBadgeTap,
                            onBadgeDrag = onBadgeDrag,
                            onDraggingChanged = { isDraggingBadge = it }
                        )
                        Spacer(Modifier.width((10f * compactScale).dp))
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .alpha(if (isResting) 0.78f else 1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = task.title,
                                color = if (isResting) pal.textMuted else pal.textDark,
                                fontSize = ((if (isCompact) 12.5f else 13.8f) * scale).sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = if (isCompact) 1 else 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            ThemeTaskMetaRow(
                                timeLabel = timeLabel,
                                cycleLabel = cycleLabel,
                                isResting = isResting,
                                config = config,
                                pal = pal,
                                scale = scale,
                                compactScale = compactScale
                            )
                        }
                    }

                    if (config.showQuickActionsOnCard && !isPreview) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onPreviewAlert,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.NotificationsActive,
                                    contentDescription = AppStrings.get(lang, "tryNow"),
                                    tint = pal.accent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(
                                onClick = onDeleteClick,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.DeleteOutline,
                                    contentDescription = AppStrings.get(lang, "delete"),
                                    tint = Color(0xFFEF4444).copy(alpha = 0.75f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeTaskMetaRow(
    timeLabel: String,
    cycleLabel: String,
    isResting: Boolean,
    config: ScreenDesignConfig,
    pal: ResolvedScreenPalette,
    scale: Float,
    compactScale: Float
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy((6f * compactScale).dp)
    ) {
        if (config.showTaskTimePill && timeLabel.isNotBlank()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(pal.accentSoft)
                    .padding(horizontal = (7f * compactScale).dp, vertical = (2f * compactScale).dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.AccessTime,
                    contentDescription = null,
                    tint = pal.accent,
                    modifier = Modifier.size((11f * compactScale).dp)
                )
                Spacer(Modifier.width(3.dp))
                Text(
                    text = timeLabel,
                    color = pal.accent,
                    fontSize = (10f * scale).sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
        if (config.showTaskStatusBadges && (cycleLabel.isNotBlank() || isResting)) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (isResting) pal.textMuted.copy(alpha = 0.14f) else pal.secondary.copy(alpha = 0.14f))
                    .padding(horizontal = (7f * compactScale).dp, vertical = (2f * compactScale).dp)
            ) {
                Text(
                    text = if (isResting) "Zzz" else cycleLabel,
                    color = if (isResting) pal.textMuted else pal.secondary,
                    fontSize = (9.5f * scale).sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun TaskAvatarRenderSlot(
    task: SmartTask,
    imgDp: Dp,
    photoShape: String,
    showBadge: Boolean,
    isResting: Boolean,
    isPreview: Boolean,
    smartIconSettings: SmartIconSettings,
    onPhotoClick: () -> Unit,
    onBadgeTap: (SmartTask) -> Unit,
    onBadgeDrag: (String, Float, Float) -> Unit,
    onDraggingChanged: (Boolean) -> Unit
) {
    if (showBadge) {
        TaskCircleWithFloatingBadge(
            task = task,
            imgDp = imgDp,
            photoShape = photoShape,
            isDemo = isPreview,
            isResting = isResting,
            smartIconSettings = smartIconSettings,
            onPhotoClick = if (isPreview) null else onPhotoClick,
            onBadgeTap = onBadgeTap,
            onBadgeDrag = onBadgeDrag,
            onDraggingChanged = onDraggingChanged
        )
    } else {
        MediaAvatarSlot(
            imgUri = task.imgUri,
            size = imgDp,
            shapeId = photoShape,
            isActiveGlow = !isResting,
            isResting = isResting,
            contentDescription = task.title,
            onClick = if (isPreview) null else onPhotoClick
        )
    }
}

@Composable
private fun ThemeGreetingHeader(
    lang: String,
    dueTodayTotal: Int,
    totalCount: Int,
    pal: ResolvedScreenPalette,
    scale: Float,
    compactScale: Float,
    isPreview: Boolean,
    onOpenOverview: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = (4f * compactScale).dp, vertical = (2f * compactScale).dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (lang == "en") "Today's Plan" else "خطة اليوم",
                color = pal.textDark,
                fontSize = (18f * scale).sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = if (lang == "en") {
                    "$dueTodayTotal active today • $totalCount total"
                } else {
                    "$dueTodayTotal نشط اليوم • $totalCount إجمالي"
                },
                color = pal.textMuted,
                fontSize = (11f * scale).sp,
                fontWeight = FontWeight.Medium
            )
        }
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = pal.accentSoft,
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .then(if (!isPreview) Modifier.clickable(onClick = onOpenOverview) else Modifier)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = (10f * compactScale).dp, vertical = (5f * compactScale).dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.FitnessCenter,
                    contentDescription = null,
                    tint = pal.accent,
                    modifier = Modifier.size((13f * compactScale).dp)
                )
                Text(
                    text = "$dueTodayTotal/$totalCount",
                    color = pal.accent,
                    fontSize = (11f * scale).sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun ThemeWeekStrip(
    lang: String,
    pal: ResolvedScreenPalette,
    config: ScreenDesignConfig,
    scale: Float,
    compactScale: Float,
    onSelectDay: (Int) -> Unit
) {
    val days = AppStrings.dayNames(lang)
    val todayIdx = LocalDate.now().dayOfWeek.value % 7
    val cardShape = RoundedCornerShape(config.cardCornerRadiusDp.coerceIn(12, 26).dp)
    Surface(
        shape = cardShape,
        color = pal.cardBg,
        shadowElevation = (config.cardElevationDp.coerceIn(0, 8) * compactScale).dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, pal.borderSoft, cardShape)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = (6f * compactScale).dp, vertical = (8f * compactScale).dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            days.take(7).forEachIndexed { idx, label ->
                val isToday = idx == todayIdx
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isToday) pal.accent else Color.Transparent)
                        .clickable { onSelectDay(idx) }
                        .padding(horizontal = (7f * compactScale).dp, vertical = (5f * compactScale).dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label.take(3),
                        color = if (isToday) Color.White else pal.textMuted,
                        fontSize = (10f * scale).sp,
                        fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeProgressWidget(
    style: String,
    dueTodayTotal: Int,
    totalTasks: Int,
    requestsCount: Int,
    lang: String,
    pal: ResolvedScreenPalette,
    config: ScreenDesignConfig,
    scale: Float,
    compactScale: Float,
    onClick: () -> Unit
) {
    val ratio = if (totalTasks > 0) (dueTodayTotal.toFloat() / totalTasks.toFloat()).coerceIn(0f, 1f) else 0f
    val shape = RoundedCornerShape(config.cardCornerRadiusDp.coerceIn(14, 28).dp)

    when (style) {
        "bento_tiles" -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy((8f * compactScale).dp)
            ) {
                Surface(
                    shape = shape,
                    color = pal.cardBg,
                    shadowElevation = (config.cardElevationDp.coerceIn(0, 8) * compactScale).dp,
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, pal.borderSoft, shape)
                        .clickable(onClick = onClick)
                ) {
                    Row(
                        modifier = Modifier.padding((11f * compactScale).dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy((8f * compactScale).dp)
                    ) {
                        MiniRingProgress(ratio = ratio, accent = pal.accent, sizeDp = (38f * compactScale).dp)
                        Column {
                            Text(
                                text = if (lang == "en") "Today" else "اليوم",
                                color = pal.textMuted,
                                fontSize = (10f * scale).sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$dueTodayTotal / $totalTasks",
                                color = pal.textDark,
                                fontSize = (14f * scale).sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
                Surface(
                    shape = shape,
                    color = pal.cardBg,
                    shadowElevation = (config.cardElevationDp.coerceIn(0, 8) * compactScale).dp,
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, pal.borderSoft, shape)
                        .clickable(onClick = onClick)
                ) {
                    Row(
                        modifier = Modifier.padding((11f * compactScale).dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy((8f * compactScale).dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size((36f * compactScale).dp)
                                .clip(CircleShape)
                                .background(pal.secondary.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.LocalFireDepartment,
                                contentDescription = null,
                                tint = pal.secondary,
                                modifier = Modifier.size((20f * compactScale).dp)
                            )
                        }
                        Column {
                            Text(
                                text = if (lang == "en") "Streak" else "النشاط",
                                color = pal.textMuted,
                                fontSize = (10f * scale).sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (lang == "en") "$requestsCount Orders" else "$requestsCount طلب",
                                color = pal.textDark,
                                fontSize = (13f * scale).sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }

        "linear_bar" -> {
            Surface(
                shape = shape,
                color = pal.cardBg,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, pal.borderSoft, shape)
                    .clickable(onClick = onClick)
            ) {
                Column(
                    modifier = Modifier.padding((11f * compactScale).dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (lang == "en") "Daily Focus Progress" else "تقدم تركيز اليوم",
                            color = pal.textDark,
                            fontSize = (11.5f * scale).sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${(ratio * 100).toInt()}%",
                            color = pal.accent,
                            fontSize = (11.5f * scale).sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    LinearProgressIndicator(
                        progress = { ratio },
                        color = pal.accent,
                        trackColor = pal.accentSoft,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((7f * compactScale).dp)
                            .clip(RoundedCornerShape(999.dp))
                    )
                }
            }
        }

        "mini_pills" -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    (if (lang == "en") "Active $dueTodayTotal" else "نشط $dueTodayTotal") to pal.accent,
                    (if (lang == "en") "Total $totalTasks" else "الكل $totalTasks") to pal.secondary,
                    (if (lang == "en") "Orders $requestsCount" else "طلبات $requestsCount") to pal.textMuted
                ).forEach { (txt, clr) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(clr.copy(alpha = 0.13f))
                            .clickable(onClick = onClick)
                            .padding(horizontal = (10f * compactScale).dp, vertical = (5f * compactScale).dp)
                    ) {
                        Text(
                            text = txt,
                            color = clr,
                            fontSize = (10.5f * scale).sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        else -> {
            // "ring_card"
            Surface(
                shape = shape,
                color = pal.cardBg,
                shadowElevation = (config.cardElevationDp.coerceIn(0, 8) * compactScale).dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, pal.borderSoft, shape)
                    .clickable(onClick = onClick)
            ) {
                Row(
                    modifier = Modifier.padding((12f * compactScale).dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MiniRingProgress(ratio = ratio, accent = pal.accent, sizeDp = (44f * compactScale).dp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (lang == "en") "Active Schedule Today" else "المهام النشطة اليوم",
                            color = pal.textDark,
                            fontSize = (13f * scale).sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = if (lang == "en") "$dueTodayTotal of $totalTasks ready" else "$dueTodayTotal من $totalTasks جاهزة اليوم",
                            color = pal.textMuted,
                            fontSize = (11f * scale).sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniRingProgress(ratio: Float, accent: Color, sizeDp: Dp) {
    Box(modifier = Modifier.size(sizeDp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(width = 4.5.dp.toPx(), cap = StrokeCap.Round)
            drawArc(
                color = accent.copy(alpha = 0.16f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = stroke
            )
            drawArc(
                color = accent,
                startAngle = -90f,
                sweepAngle = 360f * ratio.coerceIn(0.06f, 1f),
                useCenter = false,
                style = stroke
            )
        }
        Text(
            text = "${(ratio * 100).toInt()}%",
            color = accent,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun ThemeSpotlightCard(
    task: SmartTask,
    lang: String,
    pal: ResolvedScreenPalette,
    config: ScreenDesignConfig,
    smartIconSettings: SmartIconSettings,
    scale: Float,
    compactScale: Float,
    isPreview: Boolean,
    onPhotoClick: () -> Unit,
    onEditClick: () -> Unit,
    onAlertPreview: () -> Unit,
    onBadgeTap: (SmartTask) -> Unit,
    onBadgeDrag: (String, Float, Float) -> Unit
) {
    val shape = RoundedCornerShape(config.cardCornerRadiusDp.coerceIn(18, 32).dp)
    val timeText = if (task.alertTime.isNotBlank()) AppStrings.formatTime(task.alertTime, lang) else task.meta
    Surface(
        shape = shape,
        color = Color.Transparent,
        shadowElevation = (6f * compactScale).dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(
                        pal.accent,
                        pal.secondary
                    )
                )
            )
            .then(if (!isPreview) Modifier.clickable(onClick = onEditClick) else Modifier)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding((14f * compactScale).dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy((12f * compactScale).dp)
        ) {
            TaskAvatarRenderSlot(
                task = task.copy(badgeVisible = task.badgeVisible && config.showTaskFloatingBadges),
                imgDp = (64f * compactScale).dp,
                photoShape = config.taskImageShape,
                showBadge = config.showTaskFloatingBadges,
                isResting = false,
                isPreview = isPreview,
                smartIconSettings = smartIconSettings,
                onPhotoClick = onPhotoClick,
                onBadgeTap = onBadgeTap,
                onBadgeDrag = onBadgeDrag,
                onDraggingChanged = {}
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color.White.copy(alpha = 0.22f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (lang == "en") "UP NEXT" else "المهمة القادمة",
                        color = Color.White,
                        fontSize = (9.5f * scale).sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Text(
                    text = task.title,
                    color = Color.White,
                    fontSize = (15.5f * scale).sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (timeText.isNotBlank()) {
                    Text(
                        text = timeText,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = (11f * scale).sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (!isPreview) {
                    Spacer(Modifier.height(2.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = Color.White,
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .clickable(onClick = onAlertPreview)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = pal.accent, modifier = Modifier.size(14.dp))
                                Text(
                                    text = if (lang == "en") "Start / Alert" else "بدء / تذكير",
                                    color = pal.accent,
                                    fontSize = (10.5f * scale).sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeFilterBar(
    style: String,
    selected: String,
    lang: String,
    pal: ResolvedScreenPalette,
    scale: Float,
    compactScale: Float,
    onSelect: (String) -> Unit
) {
    val chips = listOf(
        "all" to if (lang == "en") "All" else "الكل",
        "today" to if (lang == "en") "Today" else "اليوم",
        "gym" to if (lang == "en") "Gym" else "الجيم",
        "alerts" to if (lang == "en") "Alerts" else "التنبيهات"
    )
    val isSegmented = style == "segmented"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isSegmented) {
                    Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(pal.cardBg)
                        .border(1.dp, pal.borderSoft, RoundedCornerShape(16.dp))
                        .padding(3.dp)
                } else {
                    Modifier.horizontalScroll(rememberScrollState())
                }
            ),
        horizontalArrangement = Arrangement.spacedBy((6f * compactScale).dp)
    ) {
        chips.forEach { (id, label) ->
            val isActive = selected == id
            val bg = when {
                style == "minimal_underline" -> Color.Transparent
                style == "outlined_chips" -> if (isActive) pal.accentSoft else pal.cardBg
                else -> if (isActive) pal.accent else pal.cardBg
            }
            val fg = when {
                style == "minimal_underline" -> if (isActive) pal.accent else pal.textMuted
                style == "outlined_chips" -> if (isActive) pal.accent else pal.textMuted
                else -> if (isActive) Color.White else pal.textMuted
            }
            val chipModifier = if (isSegmented) Modifier.weight(1f) else Modifier
            Column(
                modifier = chipModifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(bg)
                    .then(
                        if (style == "outlined_chips" || (!isSegmented && style != "minimal_underline" && !isActive)) {
                            Modifier.border(
                                1.dp,
                                if (isActive) pal.accent else pal.borderSoft,
                                RoundedCornerShape(999.dp)
                            )
                        } else Modifier
                    )
                    .clickable { onSelect(id) }
                    .padding(horizontal = (11f * compactScale).dp, vertical = (6f * compactScale).dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = label,
                    color = fg,
                    fontSize = (11f * scale).sp,
                    fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.SemiBold,
                    maxLines = 1
                )
                if (style == "minimal_underline" && isActive) {
                    Spacer(Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .width(18.dp)
                            .height(2.5.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(pal.accent)
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeBottomCapsuleBar(
    lang: String,
    requestsCount: Int,
    pal: ResolvedScreenPalette,
    scale: Float,
    compactScale: Float,
    onAddClick: () -> Unit,
    onRequestsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = pal.cardBg.copy(alpha = 0.96f),
        shadowElevation = (10f * compactScale).dp,
        modifier = modifier.border(1.dp, pal.borderSoft, RoundedCornerShape(999.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = (10f * compactScale).dp, vertical = (6f * compactScale).dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy((8f * compactScale).dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(pal.accentSoft)
                    .padding(horizontal = (10f * compactScale).dp, vertical = (6f * compactScale).dp)
            ) {
                Icon(Icons.Rounded.TaskAlt, contentDescription = null, tint = pal.accent, modifier = Modifier.size((15f * compactScale).dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    text = if (lang == "en") "Tasks" else "المهام",
                    color = pal.accent,
                    fontSize = (11f * scale).sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .clickable(onClick = onRequestsClick)
                    .padding(horizontal = (10f * compactScale).dp, vertical = (6f * compactScale).dp)
            ) {
                Icon(Icons.Rounded.ReceiptLong, contentDescription = null, tint = pal.textMuted, modifier = Modifier.size((15f * compactScale).dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    text = if (lang == "en") "Orders" else "الطلبات",
                    color = pal.textMuted,
                    fontSize = (11f * scale).sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                modifier = Modifier
                    .size((36f * compactScale).dp)
                    .clip(CircleShape)
                    .background(pal.accent)
                    .clickable(onClick = onAddClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = AppStrings.get(lang, "add"),
                    tint = Color.White,
                    modifier = Modifier.size((20f * compactScale).dp)
                )
            }
        }
    }
}

@Composable
private fun ThemeFloatingAddButton(
    style: String,
    lang: String,
    pal: ResolvedScreenPalette,
    scale: Float,
    compactScale: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = when (style) {
        "soft_squircle" -> RoundedCornerShape(16.dp)
        "extended_pill" -> RoundedCornerShape(999.dp)
        else -> CircleShape
    }
    Surface(
        shape = shape,
        color = pal.accent,
        shadowElevation = (8f * compactScale).dp,
        modifier = modifier
            .clip(shape)
            .clickable(onClick = onClick)
            .testTag("theme_floating_add_button")
    ) {
        if (style == "extended_pill") {
            Row(
                modifier = Modifier.padding(horizontal = (14f * compactScale).dp, vertical = (10f * compactScale).dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size((18f * compactScale).dp))
                Text(
                    text = if (lang == "en") "New Task" else "مهمة جديدة",
                    color = Color.White,
                    fontSize = (11.5f * scale).sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        } else {
            Box(
                modifier = Modifier.size((46f * compactScale).dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = AppStrings.get(lang, "add"),
                    tint = Color.White,
                    modifier = Modifier.size((22f * compactScale).dp)
                )
            }
        }
    }
}
