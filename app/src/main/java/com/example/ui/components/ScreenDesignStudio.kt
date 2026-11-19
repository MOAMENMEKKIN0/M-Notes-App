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
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Compare
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.localization.AppStrings
import com.example.model.RepeatMode
import com.example.model.SavedScreenDesignVersion
import com.example.model.ScreenDesignConfig
import com.example.model.ScreenDesignPresetSpec
import com.example.model.ScreenDesignPresets
import com.example.model.ScreenDesignStoreState
import com.example.model.SmartIconSettings
import com.example.model.SmartTask
import com.example.ui.theme.LocalMNotesColors

/**
 * Dedicated Settings Studio for previewing, testing, deeply customizing, saving multiple versions,
 * comparing, resetting, and explicitly applying Main Screen Design Presets (Options A, B, C, D + Classic).
 *
 * CRITICAL GUARANTEE:
 * - Selecting presets, editing any setting, or saving custom versions NEVER modifies the live Main Screen.
 * - The user's current main screen stays completely unchanged until they explicitly tap "Apply to Main Screen".
 * - All 4 original presets (A, B, C, D) and Classic remain permanently available and immutable.
 */
@Composable
fun ScreenDesignStudio(
    designState: ScreenDesignStoreState,
    tasks: List<SmartTask>,
    smartIconSettings: SmartIconSettings,
    requestsCount: Int,
    lang: String,
    onApplyDesign: (designId: String, config: ScreenDesignConfig?, displayLabel: String) -> Unit,
    onSaveCustomVersion: (name: String, basePresetId: String, config: ScreenDesignConfig, overwriteId: String?) -> String,
    onDuplicateVersion: (String) -> String?,
    onRenameVersion: (String, String) -> Unit,
    onDeleteVersion: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalMNotesColors.current

    // Local draft state — strictly isolated from the live Main Screen until user explicitly clicks Apply!
    var selectedSourceId by remember {
        mutableStateOf(
            if (designState.activeDesignId == ScreenDesignPresets.ID_CLASSIC) {
                ScreenDesignPresets.ID_OPTION_A
            } else {
                designState.activeDesignId
            }
        )
    }
    var draftBasePresetId by remember {
        mutableStateOf(
            designState.activeConfig?.basePresetId ?: ScreenDesignPresets.ID_OPTION_A
        )
    }
    var draftConfig by remember {
        mutableStateOf(
            designState.activeConfig ?: ScreenDesignPresets.optionA.config
        )
    }
    var editingSavedVersionId by remember {
        mutableStateOf(
            designState.savedVersions.firstOrNull { it.id == designState.activeDesignId }?.id
        )
    }
    var selectedTab by remember { mutableIntStateOf(0) } // 0=Layout, 1=Shapes, 2=Colors, 3=Elements
    var showFullScreenTest by remember { mutableStateOf(false) }
    var showCompareDialog by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var renamingVersion by remember { mutableStateOf<SavedScreenDesignVersion?>(null) }

    val previewTasks = remember(tasks, lang) {
        if (tasks.isNotEmpty()) {
            tasks.take(6)
        } else {
            val today = AppStrings.todayStr()
            listOf(
                SmartTask(
                    id = "demo_1",
                    title = if (lang == "en") "Leg Day Workout" else "تمرين الرجل - الجيم",
                    meta = "18:00",
                    imgUri = "res://img_3d_gym_legs",
                    repeatMode = RepeatMode.CYCLE.wire,
                    cycleSteps = listOf(if (lang == "en") "Squats" else "سكوات", if (lang == "en") "Lunges" else "لانجز"),
                    alertTime = "18:00",
                    nextDueDate = today,
                    badgeIconId = "gym_dumbbell_mint",
                    badgeVisible = true
                ),
                SmartTask(
                    id = "demo_2",
                    title = if (lang == "en") "Morning Cardio Run" else "كارديو وجري صباحي",
                    meta = "08:30",
                    imgUri = "res://img_3d_cardio_activities#0",
                    repeatMode = RepeatMode.DAILY.wire,
                    alertTime = "08:30",
                    nextDueDate = today,
                    badgeIconId = "walk_run_fast",
                    badgeVisible = true
                ),
                SmartTask(
                    id = "demo_3",
                    title = if (lang == "en") "Chest & Triceps" else "تمرين الصدر والتراي",
                    meta = "20:00",
                    imgUri = "res://img_3d_gym_chest",
                    repeatMode = RepeatMode.WEEKLY.wire,
                    weekDays = listOf(6L), // Rest day on non-Saturday for realistic Zzz demo
                    alertTime = "20:00",
                    nextDueDate = "2099-01-01",
                    badgeIconId = "char_sleeping_zzz",
                    badgeVisible = true
                ),
                SmartTask(
                    id = "demo_4",
                    title = if (lang == "en") "Healthy Fruit Bowl" else "وجبة فواكه صحية",
                    meta = "13:00",
                    imgUri = "res://img_medieval_fruits#0",
                    repeatMode = RepeatMode.DAILY.wire,
                    alertTime = "13:00",
                    nextDueDate = today,
                    badgeIconId = "food_healthy_meal",
                    badgeVisible = true
                )
            )
        }
    }

    val isPreviewClassic = selectedSourceId == ScreenDesignPresets.ID_CLASSIC
    val activeMainScreenLabel = remember(designState, lang) {
        when {
            designState.activeDesignId == ScreenDesignPresets.ID_CLASSIC || designState.activeConfig == null ->
                if (lang == "en") "Current Original Screen (Unchanged)" else "الواجهة الأصلية الحالية (بدون تغيير)"
            designState.activeDesignLabel.isNotBlank() -> designState.activeDesignLabel
            else -> {
                val p = ScreenDesignPresets.presetById(designState.activeDesignId)
                if (lang == "en") p.nameEn else p.nameAr
            }
        }
    }
    val currentDraftDisplayName = remember(selectedSourceId, editingSavedVersionId, designState.savedVersions, lang) {
        val saved = editingSavedVersionId?.let { id -> designState.savedVersions.firstOrNull { it.id == id } }
        if (saved != null) {
            saved.name
        } else {
            val preset = ScreenDesignPresets.presetById(selectedSourceId)
            if (lang == "en") preset.nameEn else preset.nameAr
        }
    }

    val isExactActive = if (isPreviewClassic) {
        designState.activeDesignId == ScreenDesignPresets.ID_CLASSIC || designState.activeConfig == null
    } else {
        designState.activeConfig == draftConfig && designState.activeDesignId == selectedSourceId
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Active Main Screen Status Banner (explicit guarantee nothing changes until Apply)
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = colors.primarySoft,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, colors.primary.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (lang == "en") "Active on Main Screen" else "المطبّق حالياً على الشاشة الرئيسية",
                        color = colors.primary,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = activeMainScreenLabel,
                        color = colors.textDark,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (designState.activeDesignId != ScreenDesignPresets.ID_CLASSIC && designState.activeConfig != null) {
                    TextButton(
                        onClick = {
                            onApplyDesign(
                                ScreenDesignPresets.ID_CLASSIC,
                                null,
                                if (lang == "en") "Current Main Screen" else "الواجهة الحالية الأصلية"
                            )
                        }
                    ) {
                        Text(
                            text = if (lang == "en") "Restore Original" else "استعادة الأصلية",
                            color = colors.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        // 1. Permanent Original Presets Gallery (Options A, B, C, D + Current Classic)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StudioGroupHeader(
                if (lang == "en") "Permanent Design Presets (A, B, C, D)" else "التصاميم الأصلية الثابتة (A و B و C و D)"
            )
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = colors.bgMain,
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .border(1.dp, colors.borderSoft, RoundedCornerShape(999.dp))
                    .clickable { showCompareDialog = true }
                    .testTag("compare_designs_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Rounded.Compare, contentDescription = null, tint = colors.primary, modifier = Modifier.size(14.dp))
                    Text(
                        text = if (lang == "en") "Compare" else "مقارنة",
                        color = colors.primary,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ScreenDesignPresets.allPresets.forEach { preset ->
                PresetThumbnailCard(
                    preset = preset,
                    isSelectedInStudio = selectedSourceId == preset.id && editingSavedVersionId == null,
                    isActiveOnMain = designState.activeDesignId == preset.id,
                    lang = lang,
                    onClick = {
                        selectedSourceId = preset.id
                        editingSavedVersionId = null
                        draftBasePresetId = preset.id
                        draftConfig = preset.config
                    }
                )
            }
        }

        // 2. Saved Custom Versions Shelf (Multiple Variations per Design)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StudioGroupHeader(
                    if (lang == "en") {
                        "My Saved Custom Versions (${designState.savedVersions.size})"
                    } else {
                        "نسخي المخصصة المحفوظة (${designState.savedVersions.size})"
                    }
                )
            }
            if (designState.savedVersions.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = colors.bgMain,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, colors.borderSoft, RoundedCornerShape(14.dp))
                ) {
                    Text(
                        text = if (lang == "en") {
                            "Customize any preset below (shapes, organic blobs, colors, layout, hidden elements) and tap \"Save Version\" to store multiple variations without overwriting A, B, C, or D."
                        } else {
                            "عدّل أي تصميم بالأسفل (الأشكال العضوية، الألوان، التخطيط، إخفاء العناصر) واضغط \"حفظ نسخة\" لتخزين عدة إصدارات مخصصة مع بقاء القوالب الأصلية A و B و C و D ثابتة دائماً."
                        },
                        color = colors.textMuted,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(11.dp)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    designState.savedVersions.forEach { version ->
                        SavedVersionRowCard(
                            version = version,
                            isSelectedInStudio = editingSavedVersionId == version.id,
                            isActiveOnMain = designState.activeDesignId == version.id,
                            lang = lang,
                            onLoad = {
                                selectedSourceId = version.id
                                editingSavedVersionId = version.id
                                draftBasePresetId = version.basePresetId
                                draftConfig = version.config
                            },
                            onApplyNow = {
                                onApplyDesign(version.id, version.config, version.name)
                            },
                            onDuplicate = {
                                val newId = onDuplicateVersion(version.id)
                                if (newId != null) {
                                    editingSavedVersionId = newId
                                    selectedSourceId = newId
                                }
                            },
                            onRename = { renamingVersion = version },
                            onDelete = {
                                if (editingSavedVersionId == version.id) {
                                    editingSavedVersionId = null
                                    selectedSourceId = version.basePresetId
                                    draftConfig = ScreenDesignPresets.presetById(version.basePresetId).config
                                }
                                onDeleteVersion(version.id)
                            }
                        )
                    }
                }
            }
        }

        // 3. Dedicated Interactive Live Preview Window
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = colors.cardBg,
            shadowElevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, colors.primary.copy(alpha = 0.32f), RoundedCornerShape(22.dp))
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Preview Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.bgMain)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isExactActive) Color(0xFF10B981) else Color(0xFFF59E0B))
                        )
                        Text(
                            text = if (isExactActive) {
                                if (lang == "en") "Active on Main Screen" else "مطبّق على الشاشة الرئيسية"
                            } else {
                                if (lang == "en") "Live Preview (Not Applied Yet)" else "معاينة حية (غير مطبّقة بعد)"
                            },
                            color = if (isExactActive) Color(0xFF059669) else colors.textDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (!isPreviewClassic) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = colors.primarySoft,
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .clickable { showFullScreenTest = true }
                                .testTag("fullscreen_design_preview_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Rounded.Fullscreen, contentDescription = null, tint = colors.primary, modifier = Modifier.size(14.dp))
                                Text(
                                    text = if (lang == "en") "Full Test" else "اختبار كامل",
                                    color = colors.primary,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }

                // Live Preview Body
                if (isPreviewClassic) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(175.dp)
                            .background(Brush.verticalGradient(listOf(colors.bgTopGlow, colors.bgMain)))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = colors.primary, modifier = Modifier.size(30.dp))
                            Text(
                                text = if (lang == "en") "Your Current Original Main Screen" else "الشاشة الرئيسية الأصلية الحالية",
                                color = colors.textDark,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = if (lang == "en") {
                                    "Press Apply to return to your unchanged default screen, or select Option A, B, C, or D above to customize."
                                } else {
                                    "اضغط تطبيق للعودة لواجهتك الأصلية، أو اختر خيار A أو B أو C أو D بالأعلى لتخصيصه بحرية."
                                },
                                color = colors.textMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(315.dp)
                            .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                    ) {
                        CustomScreenThemeWorkspace(
                            config = draftConfig,
                            allTasks = previewTasks,
                            smartIconSettings = smartIconSettings,
                            requestsCount = requestsCount,
                            lang = lang,
                            isPreview = true,
                            compactScale = 0.84f,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        // 4. Full Customization Controls Workbench (only shown when editing Option A/B/C/D or a Saved Version)
        if (!isPreviewClassic) {
            val tabs = listOf(
                Triple(0, Icons.Rounded.Layers, if (lang == "en") "Layout" else "التخطيط"),
                Triple(1, Icons.Rounded.Tune, if (lang == "en") "Shapes" else "الأشكال"),
                Triple(2, Icons.Rounded.Palette, if (lang == "en") "Colors" else "الألوان"),
                Triple(3, Icons.Rounded.Visibility, if (lang == "en") "Elements" else "العناصر")
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.bgMain)
                    .border(1.dp, colors.borderSoft, RoundedCornerShape(16.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                tabs.forEach { (idx, icon, label) ->
                    val isSel = selectedTab == idx
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSel) colors.primary else Color.Transparent)
                            .clickable { selectedTab = idx }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSel) Color.White else colors.textMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(
                            text = label,
                            color = if (isSel) Color.White else colors.textDark,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1
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
                Box(modifier = Modifier.padding(12.dp)) {
                    when (selectedTab) {
                        0 -> StudioLayoutAndOrderTab(
                            config = draftConfig,
                            lang = lang,
                            onChange = { draftConfig = it }
                        )
                        1 -> StudioShapesAndSizesTab(
                            config = draftConfig,
                            lang = lang,
                            onChange = { draftConfig = it }
                        )
                        2 -> StudioColorsAndStyleTab(
                            config = draftConfig,
                            lang = lang,
                            onChange = { draftConfig = it }
                        )
                        else -> StudioElementsVisibilityTab(
                            config = draftConfig,
                            lang = lang,
                            onChange = { draftConfig = it }
                        )
                    }
                }
            }
        }

        // 5. Explicit Action Bar: Apply to Main Screen | Save Custom Version | Reset to Original Preset
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    if (isPreviewClassic) {
                        onApplyDesign(
                            ScreenDesignPresets.ID_CLASSIC,
                            null,
                            if (lang == "en") "Current Main Screen" else "الواجهة الحالية الأصلية"
                        )
                    } else {
                        onApplyDesign(
                            selectedSourceId,
                            draftConfig,
                            currentDraftDisplayName
                        )
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("apply_screen_design_button")
            ) {
                Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = if (lang == "en") {
                        "Apply to Main Screen"
                    } else {
                        "تطبيق هذا التصميم على الشاشة الرئيسية"
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            if (!isPreviewClassic) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showSaveDialog = true },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("save_custom_design_button")
                    ) {
                        Icon(Icons.Rounded.BookmarkAdd, contentDescription = null, tint = colors.primary, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = if (lang == "en") "Save Version" else "حفظ كنسخة",
                            color = colors.primary,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            val originalPreset = ScreenDesignPresets.presetById(draftBasePresetId)
                            draftConfig = originalPreset.config
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("reset_screen_design_button")
                    ) {
                        Icon(Icons.Rounded.RestartAlt, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = if (lang == "en") "Reset Preset" else "إعادة للأصلي",
                            color = colors.textDark,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }

    // Full-Screen Interactive Test Dialog
    if (showFullScreenTest && !isPreviewClassic) {
        Dialog(
            onDismissRequest = { showFullScreenTest = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                ScreenDesignBackdrop(config = draftConfig, modifier = Modifier.fillMaxSize())
                Column(modifier = Modifier.fillMaxSize()) {
                    Surface(
                        color = colors.cardBg.copy(alpha = 0.94f),
                        shadowElevation = 4.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentDraftDisplayName,
                                    color = colors.textDark,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (lang == "en") "Interactive Full-Screen Preview" else "معاينة تفاعلية كاملة (لم تُطبّق بعد)",
                                    color = colors.textMuted,
                                    fontSize = 10.5.sp
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Button(
                                    onClick = {
                                        onApplyDesign(selectedSourceId, draftConfig, currentDraftDisplayName)
                                        showFullScreenTest = false
                                    },
                                    shape = RoundedCornerShape(999.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                                ) {
                                    Text(
                                        text = if (lang == "en") "Apply" else "تطبيق",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                                IconButton(onClick = { showFullScreenTest = false }) {
                                    Icon(Icons.Rounded.Close, contentDescription = null, tint = colors.textDark)
                                }
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        CustomScreenThemeWorkspace(
                            config = draftConfig,
                            allTasks = previewTasks,
                            smartIconSettings = smartIconSettings,
                            requestsCount = requestsCount,
                            lang = lang,
                            isPreview = true,
                            compactScale = 1f,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }

    // Side-by-Side Comparison Modal (Compare any two Presets or Saved Custom Versions)
    if (showCompareDialog) {
        CompareDesignsDialog(
            designState = designState,
            previewTasks = previewTasks,
            smartIconSettings = smartIconSettings,
            requestsCount = requestsCount,
            lang = lang,
            onApplyChoice = { id, cfg, label ->
                onApplyDesign(id, cfg, label)
                showCompareDialog = false
            },
            onLoadIntoEditor = { id, baseId, cfg, savedId ->
                selectedSourceId = id
                draftBasePresetId = baseId
                draftConfig = cfg
                editingSavedVersionId = savedId
                showCompareDialog = false
            },
            onDismiss = { showCompareDialog = false }
        )
    }

    // Save Custom Version Dialog (Never overwrites original A/B/C/D presets!)
    if (showSaveDialog) {
        val basePreset = ScreenDesignPresets.presetById(draftBasePresetId)
        val existingEditing = editingSavedVersionId?.let { id -> designState.savedVersions.firstOrNull { it.id == id } }
        var versionNameInput by remember(showSaveDialog) {
            mutableStateOf(
                existingEditing?.name ?: if (lang == "en") {
                    "My Option ${basePreset.badgeCode} Style"
                } else {
                    "تخصيصي لخيار ${basePreset.badgeCode}"
                }
            )
        }
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = {
                Text(
                    text = if (lang == "en") "Save Custom Version" else "حفظ نسخة مخصصة",
                    fontWeight = FontWeight.ExtraBold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (lang == "en") {
                            "Original preset Option ${basePreset.badgeCode} stays permanently untouched. Name your custom variation:"
                        } else {
                            "القالب الأصلي خيار ${basePreset.badgeCode} سيبقى ثابتاً بدون تعديل. اكتب اسماً لنسختك المخصصة:"
                        },
                        fontSize = 12.sp,
                        color = colors.textMuted
                    )
                    OutlinedTextField(
                        value = versionNameInput,
                        onValueChange = { versionNameInput = it },
                        singleLine = true,
                        label = { Text(if (lang == "en") "Version Name" else "اسم النسخة") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (existingEditing != null) {
                        TextButton(
                            onClick = {
                                val id = onSaveCustomVersion(
                                    versionNameInput,
                                    draftBasePresetId,
                                    draftConfig,
                                    existingEditing.id
                                )
                                editingSavedVersionId = id
                                selectedSourceId = id
                                showSaveDialog = false
                            }
                        ) {
                            Text(if (lang == "en") "Update" else "تحديث الحالية")
                        }
                    }
                    Button(
                        onClick = {
                            val newId = onSaveCustomVersion(
                                versionNameInput,
                                draftBasePresetId,
                                draftConfig,
                                null
                            )
                            editingSavedVersionId = newId
                            selectedSourceId = newId
                            showSaveDialog = false
                        }
                    ) {
                        Text(if (lang == "en") "Save as New" else "حفظ كجديدة")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text(if (lang == "en") "Cancel" else "إلغاء")
                }
            }
        )
    }

    // Rename Saved Version Dialog
    val targetRename = renamingVersion
    if (targetRename != null) {
        var renameDraft by remember(targetRename.id) { mutableStateOf(targetRename.name) }
        AlertDialog(
            onDismissRequest = { renamingVersion = null },
            title = { Text(if (lang == "en") "Rename Version" else "إعادة تسمية النسخة", fontWeight = FontWeight.ExtraBold) },
            text = {
                OutlinedTextField(
                    value = renameDraft,
                    onValueChange = { renameDraft = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRenameVersion(targetRename.id, renameDraft)
                        renamingVersion = null
                    }
                ) {
                    Text(if (lang == "en") "Save" else "حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { renamingVersion = null }) {
                    Text(if (lang == "en") "Cancel" else "إلغاء")
                }
            }
        )
    }
}

@Composable
private fun PresetThumbnailCard(
    preset: ScreenDesignPresetSpec,
    isSelectedInStudio: Boolean,
    isActiveOnMain: Boolean,
    lang: String,
    onClick: () -> Unit
) {
    val colors = LocalMNotesColors.current
    val palTop = ScreenDesignPresets.parseHexColor(preset.config.bgGradientStartHex, colors.bgTopGlow)
    val palBot = ScreenDesignPresets.parseHexColor(preset.config.bgGradientEndHex, colors.bgMain)
    val accent = ScreenDesignPresets.parseHexColor(preset.config.accentHex, colors.primary)

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (isSelectedInStudio) colors.primarySoft else colors.cardBg,
        shadowElevation = if (isSelectedInStudio) 4.dp else 1.dp,
        modifier = Modifier
            .width(136.dp)
            .border(
                width = if (isSelectedInStudio) 2.dp else 1.dp,
                color = if (isSelectedInStudio) colors.primary else colors.borderSoft,
                shape = RoundedCornerShape(18.dp)
            )
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("preset_card_${preset.id}")
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            // Miniature schematic preview matching A / B / C / D
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.verticalGradient(listOf(palTop, palBot)))
                    .border(1.dp, accent.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                    .padding(6.dp)
            ) {
                when (preset.id) {
                    ScreenDesignPresets.ID_OPTION_B -> {
                        // 2-Column Gallery miniature
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.fillMaxSize()) {
                            repeat(2) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(Color.White.copy(alpha = 0.92f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(accent.copy(alpha = 0.35f)))
                                }
                            }
                        }
                    }
                    ScreenDesignPresets.ID_OPTION_C -> {
                        // Hero Spotlight + Agenda miniature
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxSize()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(28.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(accent.copy(alpha = 0.85f))
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .clip(RoundedCornerShape(7.dp))
                                    .background(Color.White.copy(alpha = 0.92f))
                            )
                        }
                    }
                    ScreenDesignPresets.ID_OPTION_D -> {
                        // Warm Bento Tiles + Grouped rows miniature
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxSize()) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth().height(20.dp)) {
                                Box(modifier = Modifier.weight(1f).fillMaxSize().clip(RoundedCornerShape(6.dp)).background(Color.White))
                                Box(modifier = Modifier.weight(1f).fillMaxSize().clip(RoundedCornerShape(6.dp)).background(Color.White))
                            }
                            Box(modifier = Modifier.fillMaxWidth().height(14.dp).clip(RoundedCornerShape(6.dp)).background(Color.White))
                            Box(modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)).background(Color.White.copy(alpha = 0.65f)))
                        }
                    }
                    else -> {
                        // Option A Calm Focus Timeline / Classic
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxSize()) {
                            repeat(3) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .clip(RoundedCornerShape(7.dp))
                                        .background(Color.White.copy(alpha = 0.92f))
                                        .padding(horizontal = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(accent))
                                    Box(modifier = Modifier.weight(1f).height(4.dp).clip(CircleShape).background(accent.copy(alpha = 0.25f)))
                                }
                            }
                        }
                    }
                }

                // Badge Code Pill
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .clip(CircleShape)
                        .background(accent)
                        .padding(horizontal = 6.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = preset.badgeCode,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = if (lang == "en") preset.nameEn else preset.nameAr,
                    color = colors.textDark,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 14.sp
                )
                if (isActiveOnMain) {
                    Text(
                        text = if (lang == "en") "Active" else "مطبّق الآن",
                        color = Color(0xFF059669),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun SavedVersionRowCard(
    version: SavedScreenDesignVersion,
    isSelectedInStudio: Boolean,
    isActiveOnMain: Boolean,
    lang: String,
    onLoad: () -> Unit,
    onApplyNow: () -> Unit,
    onDuplicate: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = LocalMNotesColors.current
    val accent = ScreenDesignPresets.parseHexColor(version.config.accentHex, colors.primary)
    val base = ScreenDesignPresets.presetById(version.basePresetId)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelectedInStudio) colors.primarySoft else colors.bgMain,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelectedInStudio) 1.6.dp else 1.dp,
                color = if (isSelectedInStudio) colors.primary else colors.borderSoft,
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onLoad)
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(accent),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = base.badgeCode,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = version.name,
                            color = colors.textDark,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (lang == "en") {
                                "Based on Option ${base.badgeCode} • ${version.config.taskImageShape}" +
                                    if (isActiveOnMain) " • Active" else ""
                            } else {
                                "مبني على خيار ${base.badgeCode}" +
                                    if (isActiveOnMain) " • مطبّق الآن" else ""
                            },
                            color = if (isActiveOnMain) Color(0xFF059669) else colors.textMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDuplicate, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Rounded.ContentCopy, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onRename, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Rounded.Edit, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Rounded.DeleteOutline, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onLoad,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(34.dp)
                ) {
                    Text(
                        text = if (lang == "en") "Preview & Edit" else "معاينة وتعديل",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Button(
                    onClick = onApplyNow,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                    modifier = Modifier.weight(1f).height(34.dp)
                ) {
                    Text(
                        text = if (lang == "en") "Apply" else "تطبيق",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

private data class CompareCandidate(
    val id: String,
    val basePresetId: String,
    val savedId: String?,
    val label: String,
    val config: ScreenDesignConfig
)

@Composable
private fun CompareDesignsDialog(
    designState: ScreenDesignStoreState,
    previewTasks: List<SmartTask>,
    smartIconSettings: SmartIconSettings,
    requestsCount: Int,
    lang: String,
    onApplyChoice: (id: String, config: ScreenDesignConfig, label: String) -> Unit,
    onLoadIntoEditor: (id: String, basePresetId: String, config: ScreenDesignConfig, savedId: String?) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalMNotesColors.current
    val candidates = remember(designState.savedVersions, lang) {
        val base = ScreenDesignPresets.fourApprovedPresets.map { p ->
            CompareCandidate(
                id = p.id,
                basePresetId = p.id,
                savedId = null,
                label = if (lang == "en") "Option ${p.badgeCode}" else "خيار ${p.badgeCode}",
                config = p.config
            )
        }
        val custom = designState.savedVersions.map { v ->
            CompareCandidate(
                id = v.id,
                basePresetId = v.basePresetId,
                savedId = v.id,
                label = v.name,
                config = v.config
            )
        }
        base + custom
    }
    var leftIdx by remember { mutableIntStateOf(0) }
    var rightIdx by remember { mutableIntStateOf(1.coerceAtMost(candidates.lastIndex)) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = colors.cardBg,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (lang == "en") "Compare Designs Side by Side" else "مقارنة التصاميم جنباً إلى جنب",
                        color = colors.textDark,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = null, tint = colors.textDark)
                    }
                }

                // Selector pills for Left & Right slots
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(0 to leftIdx, 1 to rightIdx).forEach { (slot, curIdx) ->
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = if (slot == 0) {
                                    if (lang == "en") "Left Preview" else "التصميم الأول"
                                } else {
                                    if (lang == "en") "Right Preview" else "التصميم الثاني"
                                },
                                color = colors.textMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                candidates.forEachIndexed { idx, c ->
                                    val sel = idx == curIdx
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(999.dp))
                                            .background(if (sel) colors.primary else colors.bgMain)
                                            .clickable {
                                                if (slot == 0) leftIdx = idx else rightIdx = idx
                                            }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = c.label,
                                            color = if (sel) Color.White else colors.textDark,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Side-by-side live previews
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(330.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(candidates[leftIdx], candidates[rightIdx]).forEach { cand ->
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(1.dp, colors.borderSoft, RoundedCornerShape(16.dp))
                            ) {
                                CustomScreenThemeWorkspace(
                                    config = cand.config,
                                    allTasks = previewTasks,
                                    smartIconSettings = smartIconSettings,
                                    requestsCount = requestsCount,
                                    lang = lang,
                                    isPreview = true,
                                    compactScale = 0.64f,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    onClick = { onLoadIntoEditor(cand.id, cand.basePresetId, cand.config, cand.savedId) },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).height(34.dp)
                                ) {
                                    Text(if (lang == "en") "Edit" else "تعديل", fontSize = 10.sp)
                                }
                                Button(
                                    onClick = { onApplyChoice(cand.id, cand.config, cand.label) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                                    modifier = Modifier.weight(1f).height(34.dp)
                                ) {
                                    Text(if (lang == "en") "Apply" else "تطبيق", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
