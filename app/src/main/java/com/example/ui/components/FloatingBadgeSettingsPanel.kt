package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.OpenWith
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.model.FloatingBadgeItem
import com.example.model.SmartIconSettings
import com.example.model.SmartTask
import com.example.model.SmartTaskBadgeResolver
import com.example.model.UserPreferences
import com.example.ui.MNotesViewModel
import com.example.ui.theme.LocalMNotesColors
import kotlin.math.roundToInt

/**
 * Full Floating Circular Icon Studio inside Settings:
 * - Tab 0 ("Task Circle & Apply"): Real interactive preview with the selected task's circle,
 *   drag/position/resize/choose from 110+ icons, and a real "Apply" button that applies the
 *   icon directly onto the actual task circle(s) on the main screen!
 * - Tab 1 ("Smart Daily Rules"): Configure user-chosen favorites per category, weekday icons,
 *   rest-day & holiday rules, and automatic rotation.
 * - Tab 2 ("Free Screen Copies"): Independent screen-wide floating copies (Add, Duplicate,
 *   Resize, Hide/Show, Lock, Delete).
 */
@Composable
fun FloatingBadgeSettingsFolderContent(
    tasks: List<SmartTask>,
    smartIconSettings: SmartIconSettings,
    badges: List<FloatingBadgeItem>,
    prefs: UserPreferences,
    lang: String,
    viewModel: MNotesViewModel,
    onAppliedAndClose: () -> Unit = {}
) {
    val colors = LocalMNotesColors.current
    var activeTab by remember { mutableIntStateOf(0) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // 3-Tab Switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(colors.bgMain)
                .border(1.dp, colors.borderSoft, RoundedCornerShape(14.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val tabs = listOf(
                Triple(0, if (lang == "en") "Task Apply" else "دائرة المهمة", "studio_tab_task"),
                Triple(1, if (lang == "en") "Smart Rules" else "قواعد ذكية", "studio_tab_smart"),
                Triple(2, if (lang == "en") "Screen Copies" else "نسخ حرة", "studio_tab_copies")
            )
            tabs.forEach { (idx, label, tag) ->
                val sel = activeTab == idx
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (sel) colors.primary else Color.Transparent)
                        .clickable {
                            viewModel.playSound("nav")
                            activeTab = idx
                        }
                        .padding(vertical = 8.dp, horizontal = 4.dp)
                        .testTag(tag),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (sel) Color.White else colors.textDark,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        when (activeTab) {
            0 -> TaskCircleBadgeApplyPanel(
                tasks = tasks,
                smartIconSettings = smartIconSettings,
                prefs = prefs,
                lang = lang,
                viewModel = viewModel,
                onAppliedAndClose = onAppliedAndClose
            )
            1 -> SmartIconRulesPanel(
                settings = smartIconSettings,
                lang = lang,
                viewModel = viewModel
            )
            else -> ScreenFloatingCopiesPanel(
                badges = badges,
                prefs = prefs,
                sampleTask = tasks.firstOrNull(),
                lang = lang,
                viewModel = viewModel
            )
        }
    }
}

/* =========================================================================
   TAB 0: TASK CIRCLE INTERACTIVE PREVIEW & REAL "APPLY" WORKFLOW
   ========================================================================= */
@Composable
private fun TaskCircleBadgeApplyPanel(
    tasks: List<SmartTask>,
    smartIconSettings: SmartIconSettings,
    prefs: UserPreferences,
    lang: String,
    viewModel: MNotesViewModel,
    onAppliedAndClose: () -> Unit
) {
    val colors = LocalMNotesColors.current

    // Selected target: either a specific task ID or "__ALL__"
    var targetTaskId by remember { mutableStateOf(tasks.firstOrNull()?.id ?: "__ALL__") }
    LaunchedEffect(tasks) {
        if (targetTaskId != "__ALL__" && tasks.none { it.id == targetTaskId }) {
            targetTaskId = tasks.firstOrNull()?.id ?: "__ALL__"
        }
    }

    val selectedTask = tasks.firstOrNull { it.id == targetTaskId } ?: tasks.firstOrNull()
    val applyToAllSelected = targetTaskId == "__ALL__"

    // Draft customization state synced whenever user switches target task
    var draftIconId by remember { mutableStateOf("char_sleeping_zzz") }
    var draftOffsetX by remember { mutableFloatStateOf(20f) }
    var draftOffsetY by remember { mutableFloatStateOf(-14f) }
    var draftSizeDp by remember { mutableFloatStateOf(28f) }
    var draftVisible by remember { mutableStateOf(true) }
    var draftCategory by remember { mutableStateOf("auto") }
    var draftSmartMode by remember { mutableStateOf("manual") }
    var draftLocked by remember { mutableStateOf(false) }
    var justApplied by remember { mutableStateOf(false) }

    LaunchedEffect(targetTaskId, selectedTask?.id) {
        val t = selectedTask
        if (t != null) {
            val resolved = SmartTaskBadgeResolver.resolveTaskBadgeIconId(t, smartIconSettings)
                ?: t.badgeIconId.ifBlank { "char_sleeping_zzz" }
            draftIconId = resolved
            draftOffsetX = t.badgeOffsetX
            draftOffsetY = t.badgeOffsetY
            draftSizeDp = t.badgeSizeDp.coerceIn(20f, 96f)
            draftVisible = t.badgeVisible
            draftCategory = t.badgeCategory.ifBlank { "auto" }
            draftSmartMode = t.badgeSmartMode.ifBlank { "manual" }
            draftLocked = t.badgeLocked
            justApplied = false
        }
    }

    var selectedLibCat by remember { mutableStateOf("all") }
    var searchQuery by remember { mutableStateOf("") }
    val filteredIcons = remember(selectedLibCat, searchQuery) {
        FloatingBadgeCatalog.filter(selectedLibCat, searchQuery)
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // 1. Target Task Selector Chips (Pick which real task circle to customize, or All Tasks)
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = colors.cardBg,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, colors.borderSoft, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = if (lang == "en") "1. Choose Task Circle to Customize" else "1. اختر المهمة التي تريد تطبيق الأيقونة عليها",
                    color = colors.textDark,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                if (tasks.isEmpty()) {
                    Text(
                        text = if (lang == "en") {
                            "Add a task on the main screen first, then apply any icon directly to its circle."
                        } else {
                            "أضف مهمة على الشاشة الرئيسية أولاً لتطبيق الأيقونة مباشرة على دائرتها."
                        },
                        color = colors.textMuted,
                        fontSize = 11.sp
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        tasks.forEach { t ->
                            val isSel = targetTaskId == t.id
                            val curIcon = SmartTaskBadgeResolver.resolveTaskBadgeIconId(t, smartIconSettings)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(if (isSel) colors.primarySoft else colors.bgMain)
                                    .border(
                                        if (isSel) 1.6.dp else 1.dp,
                                        if (isSel) colors.primary else colors.borderSoft,
                                        RoundedCornerShape(999.dp)
                                    )
                                    .clickable {
                                        viewModel.playSound("nav")
                                        targetTaskId = t.id
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("select_target_task_${t.id}")
                            ) {
                                if (!curIcon.isNullOrBlank()) {
                                    FloatingBadgeVisual(iconId = curIcon, sizeDp = 20f, contentDescription = null)
                                }
                                Text(
                                    text = t.title,
                                    color = if (isSel) colors.primary else colors.textDark,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // "All Tasks" option
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(if (applyToAllSelected) colors.primary else colors.bgMain)
                                .border(
                                    1.2.dp,
                                    if (applyToAllSelected) colors.primary else colors.borderSoft,
                                    RoundedCornerShape(999.dp)
                                )
                                .clickable {
                                    viewModel.playSound("nav")
                                    targetTaskId = "__ALL__"
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("select_target_all_tasks")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DoneAll,
                                contentDescription = null,
                                tint = if (applyToAllSelected) Color.White else colors.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (lang == "en") "All Tasks (${tasks.size})" else "كل المهام (${tasks.size})",
                                color = if (applyToAllSelected) Color.White else colors.textDark,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }

        // 2. REAL INTERACTIVE TASK CIRCLE PREVIEW STAGE + APPLY BUTTON
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = colors.cardBg,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.4.dp, colors.primary.copy(alpha = 0.42f), RoundedCornerShape(20.dp))
                .testTag("task_badge_interactive_preview_card")
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.OpenWith,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (lang == "en") "2. Drag, Resize & Apply to Task Circle" else "2. اسحب وعدّل الحجم ثم اضغط تطبيق",
                            color = colors.textDark,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    if (justApplied) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(Color(0xFFDCFCE7))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = Color(0xFF15803D), modifier = Modifier.size(13.dp))
                            Text(
                                text = if (lang == "en") "Applied!" else "تم التطبيق!",
                                color = Color(0xFF15803D),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                // Interactive Preview Stage showing the REAL Task Circle + Draggable Small Icon
                TaskCircleInteractivePreviewStage(
                    task = selectedTask,
                    prefs = prefs,
                    iconId = draftIconId,
                    offsetX = draftOffsetX,
                    offsetY = draftOffsetY,
                    sizeDp = draftSizeDp,
                    visible = draftVisible,
                    lang = lang,
                    onDrag = { newX, newY ->
                        draftOffsetX = newX
                        draftOffsetY = newY
                        justApplied = false
                    }
                )

                // Size slider + quick actions
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (lang == "en") "Icon Size" else "حجم الأيقونة",
                            color = colors.textDark,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            SmallStepButton(
                                icon = Icons.Rounded.Remove,
                                desc = if (lang == "en") "Smaller" else "تصغير",
                                onClick = {
                                    draftSizeDp = (draftSizeDp - 4f).coerceAtLeast(20f)
                                    justApplied = false
                                }
                            )
                            Text(
                                text = "${draftSizeDp.roundToInt()} dp",
                                color = colors.primary,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            SmallStepButton(
                                icon = Icons.Rounded.Add,
                                desc = if (lang == "en") "Larger" else "تكبير",
                                onClick = {
                                    draftSizeDp = (draftSizeDp + 4f).coerceAtMost(96f)
                                    justApplied = false
                                }
                            )
                        }
                    }
                    Slider(
                        value = draftSizeDp.coerceIn(20f, 96f),
                        onValueChange = {
                            draftSizeDp = it
                            justApplied = false
                        },
                        valueRange = 20f..96f,
                        colors = SliderDefaults.colors(
                            thumbColor = colors.primary,
                            activeTrackColor = colors.primary
                        ),
                        modifier = Modifier
                            .height(30.dp)
                            .testTag("settings_task_badge_size_slider")
                    )
                }

                // Quick Position / Visibility / Lock Controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CompactChipBtn(
                        icon = Icons.Rounded.Refresh,
                        label = if (lang == "en") "Reset to Edge" else "موضع الحافة",
                        active = false,
                        onClick = {
                            draftOffsetX = 20f
                            draftOffsetY = -14f
                            justApplied = false
                        }
                    )
                    CompactChipBtn(
                        icon = if (draftVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                        label = if (draftVisible) {
                            if (lang == "en") "Visible" else "ظاهرة"
                        } else {
                            if (lang == "en") "Hidden" else "مخفية"
                        },
                        active = draftVisible,
                        onClick = {
                            draftVisible = !draftVisible
                            justApplied = false
                        }
                    )
                    CompactChipBtn(
                        icon = if (draftLocked) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                        label = if (draftLocked) {
                            if (lang == "en") "Locked" else "مقفولة"
                        } else {
                            if (lang == "en") "Free Drag" else "سحب حر"
                        },
                        active = draftLocked,
                        onClick = {
                            draftLocked = !draftLocked
                            justApplied = false
                        }
                    )
                }

                // Task Category & Smart Mode pills (when editing an individual task)
                if (!applyToAllSelected) {
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(
                            text = if (lang == "en") "Task Activity & Smart Mode" else "نشاط المهمة والوضع الذكي",
                            color = colors.textMuted,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            SmartTaskBadgeResolver.smartModeOptions.forEach { mode ->
                                val sel = draftSmartMode == mode.id
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(if (sel) colors.primary else colors.bgMain)
                                        .border(1.dp, if (sel) colors.primary else colors.borderSoft, RoundedCornerShape(999.dp))
                                        .clickable {
                                            draftSmartMode = mode.id
                                            justApplied = false
                                        }
                                        .padding(horizontal = 9.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = if (lang == "en") mode.nameEn else mode.nameAr,
                                        color = if (sel) Color.White else colors.textDark,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            SmartTaskBadgeResolver.taskCategoryOptions.forEach { cat ->
                                val sel = draftCategory == cat.id
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(if (sel) colors.primarySoft else colors.bgMain)
                                        .border(1.dp, if (sel) colors.primary else colors.borderSoft, RoundedCornerShape(999.dp))
                                        .clickable {
                                            draftCategory = cat.id
                                            justApplied = false
                                        }
                                        .padding(horizontal = 9.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = if (lang == "en") cat.nameEn else cat.nameAr,
                                        color = if (sel) colors.primary else colors.textDark,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // PROMINENT REAL "APPLY" BUTTON + "APPLY & VIEW ON MAIN SCREEN"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (applyToAllSelected) {
                                viewModel.applyBadgeToAllTasks(
                                    iconId = draftIconId,
                                    offsetX = draftOffsetX,
                                    offsetY = draftOffsetY,
                                    sizeDp = draftSizeDp,
                                    visible = draftVisible,
                                    smartMode = draftSmartMode
                                )
                            } else if (selectedTask != null) {
                                viewModel.applyTaskBadge(
                                    taskId = selectedTask.id,
                                    iconId = draftIconId,
                                    offsetX = draftOffsetX,
                                    offsetY = draftOffsetY,
                                    sizeDp = draftSizeDp,
                                    visible = draftVisible,
                                    category = draftCategory,
                                    smartMode = draftSmartMode,
                                    locked = draftLocked
                                )
                            }
                            justApplied = true
                        },
                        enabled = tasks.isNotEmpty(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                        modifier = Modifier
                            .weight(1.35f)
                            .height(48.dp)
                            .testTag("settings_apply_task_badge_btn")
                    ) {
                        Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = if (applyToAllSelected) {
                                if (lang == "en") "Apply to All" else "تطبيق على الكل"
                            } else {
                                if (lang == "en") "Apply" else "تطبيق"
                            },
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            if (applyToAllSelected) {
                                viewModel.applyBadgeToAllTasks(
                                    iconId = draftIconId,
                                    offsetX = draftOffsetX,
                                    offsetY = draftOffsetY,
                                    sizeDp = draftSizeDp,
                                    visible = draftVisible,
                                    smartMode = draftSmartMode
                                )
                            } else if (selectedTask != null) {
                                viewModel.applyTaskBadge(
                                    taskId = selectedTask.id,
                                    iconId = draftIconId,
                                    offsetX = draftOffsetX,
                                    offsetY = draftOffsetY,
                                    sizeDp = draftSizeDp,
                                    visible = draftVisible,
                                    category = draftCategory,
                                    smartMode = draftSmartMode,
                                    locked = draftLocked
                                )
                            }
                            onAppliedAndClose()
                        },
                        enabled = tasks.isNotEmpty(),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("settings_apply_and_view_btn")
                    ) {
                        Text(
                            text = if (lang == "en") "Apply & View" else "تطبيق وعرض",
                            color = colors.primary,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // 3. EMBEDDED 110+ ICON LIBRARY BROWSER (Select -> Preview -> Press Apply!)
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = colors.cardBg,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, colors.borderSoft, RoundedCornerShape(18.dp))
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (lang == "en") {
                        "3. Pick Icon Design (${FloatingBadgeCatalog.icons.size} Icons)"
                    } else {
                        "3. اختر تصميم الأيقونة (${FloatingBadgeCatalog.icons.size} أيقونة)"
                    },
                    color = colors.textDark,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    singleLine = true,
                    placeholder = {
                        Text(
                            text = if (lang == "en") "Search Zzz, gym, coffee, walk, food..." else "ابحث: نوم، جيم، قهوة، مشي، أكل...",
                            fontSize = 11.5.sp
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Rounded.Search, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(16.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = null,
                                tint = colors.textMuted,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { searchQuery = "" }
                            )
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.borderSoft,
                        focusedContainerColor = colors.bgMain,
                        unfocusedContainerColor = colors.bgMain
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FloatingBadgeCatalog.categories.forEach { cat ->
                        val isSel = selectedLibCat == cat.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(if (isSel) colors.primary else colors.bgMain)
                                .border(
                                    1.dp,
                                    if (isSel) colors.primary else colors.borderSoft,
                                    RoundedCornerShape(999.dp)
                                )
                                .clickable { selectedLibCat = cat.id }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (lang == "en") cat.nameEn else cat.nameAr,
                                color = if (isSel) Color.White else colors.textDark,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    contentPadding = PaddingValues(top = 6.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(275.dp)
                        .testTag("settings_task_icon_library_grid")
                ) {
                    items(filteredIcons, key = { it.id }) { spec ->
                        val isActive = draftIconId == spec.id
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isActive) colors.primarySoft else colors.bgMain)
                                .border(
                                    width = if (isActive) 1.6.dp else 1.dp,
                                    color = if (isActive) colors.primary else colors.borderSoft,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    viewModel.playSound("nav")
                                    draftIconId = spec.id
                                    draftVisible = true
                                    justApplied = false
                                }
                                .padding(top = 10.dp, bottom = 6.dp, start = 3.dp, end = 3.dp)
                                .testTag("settings_pick_task_icon_${spec.id}")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                FloatingBadgeVisual(
                                    iconId = spec.id,
                                    sizeDp = 38f,
                                    contentDescription = if (lang == "en") spec.nameEn else spec.nameAr
                                )
                                if (isActive) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(colors.primary)
                                            .border(1.2.dp, Color.White, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(9.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = if (lang == "en") spec.nameEn else spec.nameAr,
                                color = if (isActive) colors.primary else colors.textDark,
                                fontSize = 9.sp,
                                fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Interactive Preview Stage showing the REAL task circle (fixed in place) with the
 * small floating icon freely draggable anywhere around or far away from the circle.
 * Drag coordinates are in exact dp offset relative to the task circle center — the
 * exact same coordinate system rendered on the real TaskCard on the main screen!
 */
@Composable
private fun TaskCircleInteractivePreviewStage(
    task: SmartTask?,
    prefs: UserPreferences,
    iconId: String,
    offsetX: Float,
    offsetY: Float,
    sizeDp: Float,
    visible: Boolean,
    lang: String,
    onDrag: (offsetX: Float, offsetY: Float) -> Unit
) {
    val colors = LocalMNotesColors.current
    val density = LocalDensity.current
    val latestOnDrag by rememberUpdatedState(onDrag)

    var liveX by remember(offsetX) { mutableFloatStateOf(offsetX) }
    var liveY by remember(offsetY) { mutableFloatStateOf(offsetY) }
    var isDragging by remember { mutableStateOf(false) }

    val dragScale by animateFloatAsState(
        targetValue = if (isDragging) 1.12f else 1f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 500f),
        label = "preview_task_badge_scale"
    )

    val taskCircleDp = taskImageDp(prefs.taskImgSize, prefs.taskImgDp).value.coerceIn(48f, 74f).dp
    val photoShape = when (prefs.cardShape) {
        "ring" -> "ring"
        "ios" -> "ios"
        "blob" -> "blob"
        "strip" -> "strip"
        else -> "circle"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(colors.bgTopGlow, colors.bgMain, colors.bgMain)
                )
            )
            .border(1.dp, colors.borderSoft, RoundedCornerShape(18.dp))
            .testTag("task_badge_preview_stage"),
        contentAlignment = Alignment.Center
    ) {
        // Alignment dot grid
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stepX = size.width / 8f
            val stepY = size.height / 6f
            for (ix in 1 until 8) {
                for (iy in 1 until 6) {
                    drawCircle(
                        color = colors.primary.copy(alpha = 0.11f),
                        radius = 2f,
                        center = Offset(ix * stepX, iy * stepY)
                    )
                }
            }
        }

        // Realistic Task Card Backdrop (Main circle stays 100% fixed!)
        Box(
            modifier = Modifier
                .fillMaxWidth(0.90f)
                .padding(horizontal = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(18.dp))
                    .background(colors.cardBg.copy(alpha = 0.95f))
                    .border(1.2.dp, colors.primary.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    MediaAvatarSlot(
                        imgUri = task?.imgUri.orEmpty(),
                        size = taskCircleDp,
                        shapeId = photoShape,
                        useBannerDefaultIfBlank = false,
                        isActiveGlow = true,
                        contentDescription = task?.title ?: "Task Circle"
                    )

                    if (visible) {
                        val offsetXPx = with(density) { liveX.dp.toPx() }
                        val offsetYPx = with(density) { liveY.dp.toPx() }
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .zIndex(50f)
                                .absoluteOffset { IntOffset(offsetXPx.roundToInt(), offsetYPx.roundToInt()) }
                                .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                                .graphicsLayer {
                                    scaleX = dragScale
                                    scaleY = dragScale
                                }
                                .pointerInput(density.density) {
                                    awaitEachGesture {
                                        val down = awaitFirstDown(requireUnconsumed = false)
                                        var startX = 0f
                                        var startY = 0f
                                        val dragStart = awaitTouchSlopOrCancellation(down.id) { change, over ->
                                            change.consume()
                                            startX = over.x
                                            startY = over.y
                                        }
                                        if (dragStart != null) {
                                            isDragging = true
                                            liveX = (liveX + startX / density.density).coerceIn(-160f, 160f)
                                            liveY = (liveY + startY / density.density).coerceIn(-110f, 110f)
                                            latestOnDrag(liveX, liveY)
                                            drag(dragStart.id) { change ->
                                                val delta = change.positionChange()
                                                if (delta.x != 0f || delta.y != 0f) {
                                                    change.consume()
                                                    liveX = (liveX + delta.x / density.density).coerceIn(-160f, 160f)
                                                    liveY = (liveY + delta.y / density.density).coerceIn(-110f, 110f)
                                                    latestOnDrag(liveX, liveY)
                                                }
                                            }
                                            isDragging = false
                                        }
                                    }
                                }
                                .testTag("task_badge_preview_draggable")
                        ) {
                            FloatingBadgeVisual(
                                iconId = iconId,
                                sizeDp = sizeDp.coerceIn(20f, 96f),
                                contentDescription = if (lang == "en") "Draggable preview icon" else "أيقونة المعاينة القابلة للسحب"
                            )
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = task?.title ?: (if (lang == "en") "Sample Task" else "مهمة تجريبية"),
                        color = colors.textDark,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (lang == "en") {
                            "Drag icon freely • X:${liveX.roundToInt()} Y:${liveY.roundToInt()}"
                        } else {
                            "اسحب الأيقونة بحرية • X:${liveX.roundToInt()} Y:${liveY.roundToInt()}"
                        },
                        color = colors.textMuted,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/* =========================================================================
   TAB 2: FREE INDEPENDENT SCREEN COPIES (PRESERVED FULL STUDIO)
   ========================================================================= */
@Composable
private fun ScreenFloatingCopiesPanel(
    badges: List<FloatingBadgeItem>,
    prefs: UserPreferences,
    sampleTask: SmartTask?,
    lang: String,
    viewModel: MNotesViewModel
) {
    val colors = LocalMNotesColors.current
    var selectedBadgeId by remember { mutableStateOf<String?>(badges.firstOrNull()?.id) }

    LaunchedEffect(badges) {
        if (badges.isEmpty()) {
            selectedBadgeId = null
        } else if (selectedBadgeId == null || badges.none { it.id == selectedBadgeId }) {
            selectedBadgeId = badges.first().id
        }
    }

    val selectedBadge = badges.firstOrNull { it.id == selectedBadgeId } ?: badges.firstOrNull()
    val visibleCount = badges.count { it.visible }

    var selectedCategoryId by remember { mutableStateOf("all") }
    var searchQuery by remember { mutableStateOf("") }
    val filteredIcons = remember(selectedCategoryId, searchQuery) {
        FloatingBadgeCatalog.filter(selectedCategoryId, searchQuery)
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = colors.cardBg,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.2.dp, colors.primary.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                .testTag("floating_badge_live_preview_card")
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (lang == "en") "Independent Screen Copies" else "أيقونات عائمة مستقلة على الشاشة",
                        color = colors.textDark,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(colors.primarySoft)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "$visibleCount/${badges.size}",
                            color = colors.primary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                LiveFloatingIconPreviewStage(
                    badges = badges,
                    selectedBadgeId = selectedBadge?.id,
                    prefs = prefs,
                    sampleTask = sampleTask,
                    lang = lang,
                    onSelectBadge = { id -> selectedBadgeId = id },
                    onDragBadge = { id, xFrac, yFrac ->
                        selectedBadgeId = id
                        viewModel.updateFloatingBadgePosition(id, xFrac, yFrac)
                    }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val stateToUse = selectedBadge?.state ?: "sleeping"
                            val sizeToUse = selectedBadge?.sizeDp ?: 48f
                            val newId = viewModel.addFloatingBadge(stateId = stateToUse, sizeDp = sizeToUse)
                            selectedBadgeId = newId
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                        modifier = Modifier
                            .weight(1.35f)
                            .height(42.dp)
                            .testTag("add_floating_badge_btn")
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (lang == "en") "Add Copy" else "إضافة نسخة",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    if (badges.isNotEmpty()) {
                        val allVisible = badges.all { it.visible }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(colors.bgMain)
                                .border(1.dp, colors.borderSoft, RoundedCornerShape(14.dp))
                                .clickable { viewModel.setAllFloatingBadgesVisibility(!allVisible) }
                                .testTag("toggle_all_badges_visibility_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = if (allVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (allVisible) {
                                        if (lang == "en") "Hide All" else "إخفاء الكل"
                                    } else {
                                        if (lang == "en") "Show All" else "إظهار الكل"
                                    },
                                    color = colors.textDark,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        if (selectedBadge != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                badges.forEachIndexed { idx, item ->
                    val isSelected = item.id == selectedBadge.id
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (isSelected) colors.primarySoft else colors.cardBg)
                            .border(
                                if (isSelected) 1.6.dp else 1.dp,
                                if (isSelected) colors.primary else colors.borderSoft,
                                RoundedCornerShape(999.dp)
                            )
                            .clickable {
                                viewModel.playSound("nav")
                                selectedBadgeId = item.id
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        FloatingBadgeVisual(
                            iconId = item.state,
                            sizeDp = 24f,
                            contentDescription = null,
                            modifier = Modifier.alpha(if (item.visible) 1f else 0.4f)
                        )
                        Text(
                            text = if (lang == "en") "Copy #${idx + 1}" else "نسخة #${idx + 1}",
                            color = if (isSelected) colors.primary else colors.textDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = colors.cardBg,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.borderSoft, RoundedCornerShape(18.dp))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (lang == "en") "Size (${selectedBadge.sizeDp.roundToInt()} dp)" else "الحجم (${selectedBadge.sizeDp.roundToInt()})",
                            color = colors.textDark,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            SmallStepButton(
                                icon = Icons.Rounded.Remove,
                                desc = "Smaller",
                                onClick = { viewModel.updateFloatingBadgeSize(selectedBadge.id, (selectedBadge.sizeDp - 6f).coerceAtLeast(26f)) }
                            )
                            SmallStepButton(
                                icon = Icons.Rounded.Add,
                                desc = "Larger",
                                onClick = { viewModel.updateFloatingBadgeSize(selectedBadge.id, (selectedBadge.sizeDp + 6f).coerceAtMost(180f)) }
                            )
                        }
                    }
                    Slider(
                        value = selectedBadge.sizeDp.coerceIn(26f, 180f),
                        onValueChange = { viewModel.updateFloatingBadgeSize(selectedBadge.id, it) },
                        valueRange = 26f..180f,
                        colors = SliderDefaults.colors(thumbColor = colors.primary, activeTrackColor = colors.primary)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CompactChipBtn(
                            icon = Icons.Rounded.ContentCopy,
                            label = if (lang == "en") "Duplicate" else "تكرار",
                            active = true,
                            onClick = {
                                val dupId = viewModel.duplicateFloatingBadge(selectedBadge.id)
                                if (dupId != null) selectedBadgeId = dupId
                            }
                        )
                        CompactChipBtn(
                            icon = if (selectedBadge.visible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                            label = if (selectedBadge.visible) {
                                if (lang == "en") "Hide" else "إخفاء"
                            } else {
                                if (lang == "en") "Show" else "إظهار"
                            },
                            active = false,
                            onClick = { viewModel.toggleFloatingBadgeVisibility(selectedBadge.id) }
                        )
                        CompactChipBtn(
                            icon = if (selectedBadge.locked) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                            label = if (selectedBadge.locked) {
                                if (lang == "en") "Locked" else "مقفولة"
                            } else {
                                if (lang == "en") "Unlocked" else "حر"
                            },
                            active = selectedBadge.locked,
                            onClick = { viewModel.toggleFloatingBadgeLock(selectedBadge.id) }
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(Color(0xFFFEF2F2))
                                .border(1.dp, Color(0xFFFECACA), RoundedCornerShape(999.dp))
                                .clickable { viewModel.deleteFloatingBadge(selectedBadge.id) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(15.dp))
                        }
                    }

                    // Icon picker for selected screen copy
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        placeholder = { Text(if (lang == "en") "Search icons..." else "ابحث...", fontSize = 11.sp) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        FloatingBadgeCatalog.categories.forEach { cat ->
                            val isSel = selectedCategoryId == cat.id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(if (isSel) colors.primary else colors.bgMain)
                                    .border(1.dp, if (isSel) colors.primary else colors.borderSoft, RoundedCornerShape(999.dp))
                                    .clickable { selectedCategoryId = cat.id }
                                    .padding(horizontal = 9.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = if (lang == "en") cat.nameEn else cat.nameAr,
                                    color = if (isSel) Color.White else colors.textDark,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        contentPadding = PaddingValues(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    ) {
                        items(filteredIcons, key = { it.id }) { spec ->
                            val isActive = selectedBadge.state == spec.id
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isActive) colors.primarySoft else colors.bgMain)
                                    .border(
                                        if (isActive) 1.5.dp else 1.dp,
                                        if (isActive) colors.primary else colors.borderSoft,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { viewModel.updateFloatingBadgeState(selectedBadge.id, spec.id) }
                                    .padding(vertical = 8.dp, horizontal = 3.dp)
                            ) {
                                FloatingBadgeVisual(iconId = spec.id, sizeDp = 34f, contentDescription = null)
                                Text(
                                    text = if (lang == "en") spec.nameEn else spec.nameAr,
                                    color = if (isActive) colors.primary else colors.textDark,
                                    fontSize = 8.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
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
private fun LiveFloatingIconPreviewStage(
    badges: List<FloatingBadgeItem>,
    selectedBadgeId: String?,
    prefs: UserPreferences,
    sampleTask: SmartTask?,
    lang: String,
    onSelectBadge: (String) -> Unit,
    onDragBadge: (badgeId: String, xFraction: Float, yFraction: Float) -> Unit
) {
    val colors = LocalMNotesColors.current
    val density = LocalDensity.current
    val latestOnDrag by rememberUpdatedState(onDragBadge)
    val latestOnSelect by rememberUpdatedState(onSelectBadge)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(colors.bgTopGlow, colors.bgMain, colors.bgMain)
                )
            )
            .border(1.dp, colors.borderSoft, RoundedCornerShape(18.dp))
    ) {
        val stageWidthPx = with(density) { maxWidth.toPx() }
        val stageHeightPx = with(density) { maxHeight.toPx() }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val stepX = size.width / 8f
            val stepY = size.height / 6f
            for (ix in 1 until 8) {
                for (iy in 1 until 6) {
                    drawCircle(
                        color = colors.primary.copy(alpha = 0.10f),
                        radius = 2f,
                        center = Offset(ix * stepX, iy * stepY)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = colors.cardBg.copy(alpha = 0.94f),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth(0.90f)
                    .border(1.dp, colors.borderSoft, RoundedCornerShape(20.dp))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MediaAvatarSlot(
                        imgUri = sampleTask?.imgUri?.ifBlank { prefs.bannerImg } ?: prefs.bannerImg,
                        size = 48.dp,
                        shapeId = prefs.bannerPhotoShape,
                        useBannerDefaultIfBlank = true,
                        contentDescription = null
                    )
                    Text(
                        text = sampleTask?.title ?: prefs.bannerTitle.ifBlank { "M Notes" },
                        color = colors.textDark,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        badges.filter { it.visible }.forEach { badge ->
            val previewVisualDp = (badge.sizeDp * 0.72f).coerceIn(28f, 92f)
            val totalBoxPx = with(density) { (previewVisualDp * 1.44f).dp.toPx() }
            val maxX = (stageWidthPx - totalBoxPx).coerceAtLeast(1f)
            val maxY = (stageHeightPx - totalBoxPx).coerceAtLeast(1f)

            var posX by remember(badge.id, maxX) { mutableFloatStateOf((badge.xFraction.coerceIn(0f, 1f)) * maxX) }
            var posY by remember(badge.id, maxY) { mutableFloatStateOf((badge.yFraction.coerceIn(0f, 1f)) * maxY) }
            var isDragging by remember(badge.id) { mutableStateOf(false) }

            LaunchedEffect(badge.xFraction, badge.yFraction, maxX, maxY) {
                if (!isDragging) {
                    posX = badge.xFraction.coerceIn(0f, 1f) * maxX
                    posY = badge.yFraction.coerceIn(0f, 1f) * maxY
                }
            }

            Box(
                modifier = Modifier
                    .absoluteOffset { IntOffset(posX.roundToInt(), posY.roundToInt()) }
                    .pointerInput(badge.id, maxX, maxY) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            var startOverX = 0f
                            var startOverY = 0f
                            val dragStart = awaitTouchSlopOrCancellation(down.id) { change, over ->
                                change.consume()
                                startOverX = over.x
                                startOverY = over.y
                            }
                            if (dragStart == null) {
                                latestOnSelect(badge.id)
                            } else {
                                isDragging = true
                                latestOnSelect(badge.id)
                                posX = (posX + startOverX).coerceIn(0f, maxX)
                                posY = (posY + startOverY).coerceIn(0f, maxY)
                                latestOnDrag(badge.id, (posX / maxX).coerceIn(0f, 1f), (posY / maxY).coerceIn(0f, 1f))
                                drag(dragStart.id) { change ->
                                    val delta = change.positionChange()
                                    if (delta.x != 0f || delta.y != 0f) {
                                        change.consume()
                                        posX = (posX + delta.x).coerceIn(0f, maxX)
                                        posY = (posY + delta.y).coerceIn(0f, maxY)
                                        latestOnDrag(badge.id, (posX / maxX).coerceIn(0f, 1f), (posY / maxY).coerceIn(0f, 1f))
                                    }
                                }
                                isDragging = false
                            }
                        }
                    }
            ) {
                FloatingBadgeVisual(
                    iconId = badge.state,
                    sizeDp = previewVisualDp,
                    contentDescription = null,
                    modifier = if (badge.id == selectedBadgeId) {
                        Modifier.border(1.5.dp, colors.primary.copy(alpha = 0.65f), CircleShape)
                    } else Modifier
                )
            }
        }
    }
}

@Composable
private fun CompactChipBtn(
    icon: ImageVector,
    label: String,
    active: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalMNotesColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (active) colors.primarySoft else colors.bgMain)
            .border(1.dp, if (active) colors.primary else colors.borderSoft, RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Icon(icon, contentDescription = null, tint = if (active) colors.primary else colors.textDark, modifier = Modifier.size(14.dp))
        Text(text = label, color = if (active) colors.primary else colors.textDark, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SmallStepButton(
    icon: ImageVector,
    desc: String,
    onClick: () -> Unit
) {
    val colors = LocalMNotesColors.current
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(colors.bgMain)
            .border(1.dp, colors.borderSoft, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = desc, tint = colors.textDark, modifier = Modifier.size(15.dp))
    }
}
