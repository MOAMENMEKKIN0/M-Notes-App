package com.example.ui.components

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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FloatingBadgeItem
import com.example.model.SmartIconSettings
import com.example.model.SmartTask
import com.example.model.SmartTaskBadgeResolver
import com.example.ui.theme.LocalMNotesColors
import kotlin.math.roundToInt

/**
 * Dedicated Icon Selection Library opened when tapping a TASK CIRCLE'S small
 * floating icon directly on the main screen (never opens the photo picker or task editor).
 * Lets the user pick a new icon from the 110+ library, adjust size, category/smart mode,
 * lock/hide/remove, and press Apply to update the real task circle immediately.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskBadgeLibrarySheet(
    task: SmartTask,
    smartIconSettings: SmartIconSettings,
    lang: String,
    onApply: (
        iconId: String,
        sizeDp: Float,
        offsetX: Float,
        offsetY: Float,
        category: String,
        smartMode: String,
        locked: Boolean
    ) -> Unit,
    onHide: () -> Unit,
    onRemove: () -> Unit,
    onOpenFullStudio: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalMNotesColors.current
    val density = LocalDensity.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val safeDismisser = rememberSafeSheetDismisser(sheetState, onDismiss)

    val initialResolved = remember(task.id, task.badgeIconId, smartIconSettings) {
        SmartTaskBadgeResolver.resolveTaskBadgeIconId(task, smartIconSettings)
            ?: task.badgeIconId.ifBlank { "char_sleeping_zzz" }
    }
    var draftIconId by remember(task.id) { mutableStateOf(initialResolved) }
    var draftSizeDp by remember(task.id) { mutableFloatStateOf(task.badgeSizeDp.coerceIn(20f, 96f)) }
    var draftOffsetX by remember(task.id) { mutableFloatStateOf(task.badgeOffsetX) }
    var draftOffsetY by remember(task.id) { mutableFloatStateOf(task.badgeOffsetY) }
    var draftCategory by remember(task.id) { mutableStateOf(task.badgeCategory.ifBlank { "auto" }) }
    var draftSmartMode by remember(task.id) { mutableStateOf(task.badgeSmartMode.ifBlank { "manual" }) }
    var draftLocked by remember(task.id) { mutableStateOf(task.badgeLocked) }

    var selectedCategoryId by remember { mutableStateOf("all") }
    var searchQuery by remember { mutableStateOf("") }

    val activeSpec = remember(draftIconId) { FloatingBadgeCatalog.byId(draftIconId) }
    val filteredIcons = remember(selectedCategoryId, searchQuery) {
        FloatingBadgeCatalog.filter(selectedCategoryId, searchQuery)
    }

    ModalBottomSheet(
        onDismissRequest = { safeDismisser.onSystemDismiss() },
        sheetState = sheetState,
        containerColor = colors.bgMain,
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Live mini task circle + icon preview + Task name + Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(colors.cardBg)
                        .border(1.2.dp, colors.primary.copy(alpha = 0.35f), RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    MediaAvatarSlot(
                        imgUri = task.imgUri,
                        size = 44.dp,
                        shapeId = "circle",
                        useBannerDefaultIfBlank = false,
                        contentDescription = task.title
                    )
                    val pxX = with(density) { (draftOffsetX.coerceIn(-24f, 24f)).dp.toPx() }
                    val pxY = with(density) { (draftOffsetY.coerceIn(-22f, 22f)).dp.toPx() }
                    Box(
                        modifier = Modifier.absoluteOffset {
                            IntOffset(pxX.roundToInt(), pxY.roundToInt())
                        }
                    ) {
                        FloatingBadgeVisual(
                            iconId = draftIconId,
                            sizeDp = draftSizeDp.coerceIn(22f, 38f),
                            contentDescription = if (lang == "en") activeSpec.nameEn else activeSpec.nameAr
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = task.title,
                        color = colors.textDark,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (lang == "en") {
                            "${activeSpec.nameEn} • ${draftSizeDp.roundToInt()}dp • Hold & drag on main screen"
                        } else {
                            "${activeSpec.nameAr} • الحجم ${draftSizeDp.roundToInt()} • اسحبها بحرية على الشاشة"
                        },
                        color = colors.textMuted,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colors.cardBg)
                        .border(1.dp, colors.borderSoft, CircleShape)
                        .clickable { safeDismisser.dismiss() }
                        .testTag("task_badge_library_close"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = if (lang == "en") "Close" else "إغلاق",
                        tint = colors.textDark,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Prominent OK • Save & Apply Button (saves immediately BEFORE closing)
            Button(
                onClick = {
                    onApply(
                        draftIconId,
                        draftSizeDp,
                        draftOffsetX,
                        draftOffsetY,
                        draftCategory,
                        draftSmartMode,
                        draftLocked
                    )
                    safeDismisser.dismiss()
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("task_library_apply_btn")
            ) {
                Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = if (lang == "en") "OK • Save & Apply to Task Circle" else "موافق • حفظ وتطبيق على دائرة المهمة",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            // Live Size Control
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = colors.cardBg,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.borderSoft, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (lang == "en") "Icon Size" else "حجم الأيقونة",
                            color = colors.textDark,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            LibraryStepBtn(
                                icon = Icons.Rounded.Remove,
                                desc = if (lang == "en") "Smaller" else "تصغير",
                                onClick = { draftSizeDp = (draftSizeDp - 4f).coerceAtLeast(20f) }
                            )
                            Text(
                                text = "${draftSizeDp.roundToInt()} dp",
                                color = colors.primary,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            LibraryStepBtn(
                                icon = Icons.Rounded.Add,
                                desc = if (lang == "en") "Larger" else "تكبير",
                                onClick = { draftSizeDp = (draftSizeDp + 4f).coerceAtMost(96f) }
                            )
                        }
                    }
                    Slider(
                        value = draftSizeDp.coerceIn(20f, 96f),
                        onValueChange = { draftSizeDp = it },
                        valueRange = 20f..96f,
                        colors = SliderDefaults.colors(
                            thumbColor = colors.primary,
                            activeTrackColor = colors.primary
                        ),
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("task_badge_size_slider")
                    )
                }
            }

            // Quick Actions Row: Reset Edge | Lock Drag | Smart Studio | Hide | Remove
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                LibraryActionPill(
                    icon = Icons.Rounded.Refresh,
                    label = if (lang == "en") "Reset Edge" else "إعادة للحافة",
                    tint = colors.primary,
                    bg = colors.primarySoft,
                    onClick = {
                        draftOffsetX = 20f
                        draftOffsetY = -14f
                    },
                    tag = "task_library_reset_pos"
                )
                LibraryActionPill(
                    icon = if (draftLocked) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                    label = if (draftLocked) {
                        if (lang == "en") "Locked" else "مقفولة"
                    } else {
                        if (lang == "en") "Free Drag" else "سحب حر"
                    },
                    tint = if (draftLocked) colors.primary else colors.textDark,
                    bg = if (draftLocked) colors.primarySoft else colors.cardBg,
                    onClick = { draftLocked = !draftLocked },
                    tag = "task_library_lock"
                )
                LibraryActionPill(
                    icon = Icons.Rounded.AutoAwesome,
                    label = if (lang == "en") "Preview & Smart Rules" else "المعاينة والقواعد الذكية",
                    tint = colors.textDark,
                    bg = colors.cardBg,
                    onClick = {
                        safeDismisser.dismiss { onOpenFullStudio() }
                    },
                    tag = "task_library_open_studio"
                )
                LibraryActionPill(
                    icon = Icons.Rounded.VisibilityOff,
                    label = if (lang == "en") "Hide" else "إخفاء",
                    tint = colors.textMuted,
                    bg = colors.cardBg,
                    onClick = {
                        onHide()
                        safeDismisser.dismiss()
                    },
                    tag = "task_library_hide"
                )
                LibraryActionPill(
                    icon = Icons.Rounded.DeleteOutline,
                    label = if (lang == "en") "Remove" else "إزالة",
                    tint = Color(0xFFEF4444),
                    bg = Color(0xFFFEF2F2),
                    onClick = {
                        onRemove()
                        safeDismisser.dismiss()
                    },
                    tag = "task_library_remove"
                )
            }

            // Task Smart Mode selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SmartTaskBadgeResolver.smartModeOptions.forEach { mode ->
                    val sel = draftSmartMode == mode.id
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (sel) colors.primary else colors.cardBg)
                            .border(
                                1.dp,
                                if (sel) colors.primary else colors.borderSoft,
                                RoundedCornerShape(999.dp)
                            )
                            .clickable { draftSmartMode = mode.id }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (lang == "en") mode.nameEn else mode.nameAr,
                            color = if (sel) Color.White else colors.textDark,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                singleLine = true,
                placeholder = {
                    Text(
                        text = if (lang == "en") "Search 150+ icons (3D guys, Zzz, gym, coffee, food...)" else "ابحث بين 150+ أيقونة (شخصيات 3D، نوم، جيم، قهوة...)",
                        fontSize = 12.sp
                    )
                },
                leadingIcon = {
                    Icon(Icons.Rounded.Search, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(18.dp))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = null,
                            tint = colors.textMuted,
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { searchQuery = "" }
                        )
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.borderSoft,
                    focusedContainerColor = colors.cardBg,
                    unfocusedContainerColor = colors.cardBg
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("task_badge_library_search")
            )

            // Category Filter Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FloatingBadgeCatalog.categories.forEach { cat ->
                    val selected = selectedCategoryId == cat.id
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (selected) colors.primary else colors.cardBg)
                            .border(
                                width = 1.dp,
                                color = if (selected) colors.primary else colors.borderSoft,
                                shape = RoundedCornerShape(999.dp)
                            )
                            .clickable { selectedCategoryId = cat.id }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = if (lang == "en") cat.nameEn else cat.nameAr,
                            color = if (selected) Color.White else colors.textDark,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 4-Column Visual Icon Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(top = 6.dp, bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(310.dp)
                    .testTag("task_badge_library_grid")
            ) {
                items(filteredIcons, key = { it.id }) { spec ->
                    val isSelected = draftIconId == spec.id
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) colors.primarySoft else colors.cardBg)
                            .border(
                                width = if (isSelected) 1.8.dp else 1.dp,
                                color = if (isSelected) colors.primary else colors.borderSoft,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable {
                                draftIconId = spec.id
                                draftSmartMode = "manual"
                            }
                            .padding(top = 12.dp, bottom = 7.dp, start = 4.dp, end = 4.dp)
                            .testTag("task_badge_pick_${spec.id}")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            FloatingBadgeVisual(
                                iconId = spec.id,
                                sizeDp = 42f,
                                contentDescription = if (lang == "en") spec.nameEn else spec.nameAr
                            )
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(colors.primary)
                                        .border(1.5.dp, Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = if (lang == "en") spec.nameEn else spec.nameAr,
                            color = if (isSelected) colors.primary else colors.textDark,
                            fontSize = 9.5.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
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

/**
 * Dedicated Icon Selection Library opened when tapping an independent screen-wide
 * floating circular icon on screen.
 * Uses local draft state + an explicit prominent OK / Save & Apply button that saves
 * immediately before closing so changes always persist permanently.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloatingBadgeLibrarySheet(
    badge: FloatingBadgeItem,
    lang: String,
    onConfirmSave: (iconId: String, sizeDp: Float, locked: Boolean) -> Unit,
    onDuplicate: () -> Unit,
    onHide: () -> Unit,
    onDelete: () -> Unit,
    onOpenFullStudio: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalMNotesColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val safeDismisser = rememberSafeSheetDismisser(sheetState, onDismiss)

    var draftIconId by remember(badge.id) { mutableStateOf(badge.state) }
    var draftSizeDp by remember(badge.id) { mutableFloatStateOf(badge.sizeDp.coerceIn(26f, 180f)) }
    var draftLocked by remember(badge.id) { mutableStateOf(badge.locked) }

    var selectedCategoryId by remember { mutableStateOf("all") }
    var searchQuery by remember { mutableStateOf("") }

    val activeSpec = remember(draftIconId) { FloatingBadgeCatalog.byId(draftIconId) }
    val filteredIcons = remember(selectedCategoryId, searchQuery) {
        FloatingBadgeCatalog.filter(selectedCategoryId, searchQuery)
    }

    ModalBottomSheet(
        onDismissRequest = { safeDismisser.onSystemDismiss() },
        sheetState = sheetState,
        containerColor = colors.bgMain,
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(colors.cardBg)
                        .border(1.2.dp, colors.primary.copy(alpha = 0.35f), RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    FloatingBadgeVisual(
                        iconId = draftIconId,
                        sizeDp = draftSizeDp.coerceIn(28f, 54f),
                        contentDescription = if (lang == "en") activeSpec.nameEn else activeSpec.nameAr
                    )
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = if (lang == "en") "Floating Icon Library (${FloatingBadgeCatalog.icons.size})" else "مكتبة الأيقونة العائمة (${FloatingBadgeCatalog.icons.size} أيقونة)",
                        color = colors.textDark,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = if (lang == "en") {
                            "${activeSpec.nameEn} • Size ${draftSizeDp.roundToInt()}dp • Press OK to save"
                        } else {
                            "${activeSpec.nameAr} • الحجم ${draftSizeDp.roundToInt()} • اضغط موافق للحفظ"
                        },
                        color = colors.textMuted,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colors.cardBg)
                        .border(1.dp, colors.borderSoft, CircleShape)
                        .clickable { safeDismisser.dismiss() }
                        .testTag("badge_library_close"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = if (lang == "en") "Close" else "إغلاق",
                        tint = colors.textDark,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Real Prominent OK / Save & Apply Button (commits immediately BEFORE dismissing!)
            Button(
                onClick = {
                    onConfirmSave(draftIconId, draftSizeDp, draftLocked)
                    safeDismisser.dismiss()
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("floating_badge_ok_button")
            ) {
                Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = if (lang == "en") "OK • Save & Apply Icon" else "موافق • حفظ وتطبيق الأيقونة",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = colors.cardBg,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.borderSoft, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (lang == "en") "Icon Size" else "حجم الأيقونة",
                            color = colors.textDark,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            LibraryStepBtn(
                                icon = Icons.Rounded.Remove,
                                desc = if (lang == "en") "Smaller" else "تصغير",
                                onClick = { draftSizeDp = (draftSizeDp - 6f).coerceAtLeast(26f) }
                            )
                            Text(
                                text = "${draftSizeDp.roundToInt()} dp",
                                color = colors.primary,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            LibraryStepBtn(
                                icon = Icons.Rounded.Add,
                                desc = if (lang == "en") "Larger" else "تكبير",
                                onClick = { draftSizeDp = (draftSizeDp + 6f).coerceAtMost(180f) }
                            )
                        }
                    }
                    Slider(
                        value = draftSizeDp.coerceIn(26f, 180f),
                        onValueChange = { draftSizeDp = it },
                        valueRange = 26f..180f,
                        colors = SliderDefaults.colors(
                            thumbColor = colors.primary,
                            activeTrackColor = colors.primary
                        ),
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("badge_library_size_slider")
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                LibraryActionPill(
                    icon = Icons.Rounded.ContentCopy,
                    label = if (lang == "en") "Duplicate" else "تكرار",
                    tint = colors.primary,
                    bg = colors.primarySoft,
                    onClick = onDuplicate,
                    tag = "library_duplicate_badge"
                )
                LibraryActionPill(
                    icon = if (draftLocked) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                    label = if (draftLocked) {
                        if (lang == "en") "Locked" else "مقفولة"
                    } else {
                        if (lang == "en") "Free Drag" else "سحب حر"
                    },
                    tint = if (draftLocked) colors.primary else colors.textDark,
                    bg = if (draftLocked) colors.primarySoft else colors.cardBg,
                    onClick = { draftLocked = !draftLocked },
                    tag = "library_lock_badge"
                )
                LibraryActionPill(
                    icon = Icons.Rounded.Settings,
                    label = if (lang == "en") "Preview & Settings" else "معاينة وضبط",
                    tint = colors.textDark,
                    bg = colors.cardBg,
                    onClick = {
                        safeDismisser.dismiss { onOpenFullStudio() }
                    },
                    tag = "library_open_studio"
                )
                LibraryActionPill(
                    icon = Icons.Rounded.VisibilityOff,
                    label = if (lang == "en") "Hide" else "إخفاء",
                    tint = colors.textMuted,
                    bg = colors.cardBg,
                    onClick = {
                        onHide()
                        safeDismisser.dismiss()
                    },
                    tag = "library_hide_badge"
                )
                LibraryActionPill(
                    icon = Icons.Rounded.DeleteOutline,
                    label = if (lang == "en") "Delete" else "حذف",
                    tint = Color(0xFFEF4444),
                    bg = Color(0xFFFEF2F2),
                    onClick = {
                        onDelete()
                        safeDismisser.dismiss()
                    },
                    tag = "library_delete_badge"
                )
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                singleLine = true,
                placeholder = {
                    Text(
                        text = if (lang == "en") "Search 150+ icons (3D guys, Zzz, gym, coffee, food...)" else "ابحث بين 150+ أيقونة (شخصيات 3D، نوم، جيم، قهوة...)",
                        fontSize = 12.sp
                    )
                },
                leadingIcon = {
                    Icon(Icons.Rounded.Search, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(18.dp))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = null,
                            tint = colors.textMuted,
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { searchQuery = "" }
                        )
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.borderSoft,
                    focusedContainerColor = colors.cardBg,
                    unfocusedContainerColor = colors.cardBg
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("badge_library_search")
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FloatingBadgeCatalog.categories.forEach { cat ->
                    val selected = selectedCategoryId == cat.id
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (selected) colors.primary else colors.cardBg)
                            .border(
                                width = 1.dp,
                                color = if (selected) colors.primary else colors.borderSoft,
                                shape = RoundedCornerShape(999.dp)
                            )
                            .clickable { selectedCategoryId = cat.id }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                            .testTag("badge_cat_${cat.id}")
                    ) {
                        Text(
                            text = if (lang == "en") cat.nameEn else cat.nameAr,
                            color = if (selected) Color.White else colors.textDark,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(top = 6.dp, bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(330.dp)
                    .testTag("badge_library_grid")
            ) {
                items(filteredIcons, key = { it.id }) { spec ->
                    val isSelected = draftIconId == spec.id
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) colors.primarySoft else colors.cardBg)
                            .border(
                                width = if (isSelected) 1.8.dp else 1.dp,
                                color = if (isSelected) colors.primary else colors.borderSoft,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable {
                                draftIconId = spec.id
                            }
                            .padding(top = 12.dp, bottom = 7.dp, start = 4.dp, end = 4.dp)
                            .testTag("badge_pick_${spec.id}")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            FloatingBadgeVisual(
                                iconId = spec.id,
                                sizeDp = 44f,
                                contentDescription = if (lang == "en") spec.nameEn else spec.nameAr
                            )
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(colors.primary)
                                        .border(1.5.dp, Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = if (lang == "en") spec.nameEn else spec.nameAr,
                            color = if (isSelected) colors.primary else colors.textDark,
                            fontSize = 9.5.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
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

@Composable
private fun LibraryStepBtn(
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

@Composable
private fun LibraryActionPill(
    icon: ImageVector,
    label: String,
    tint: Color,
    bg: Color,
    onClick: () -> Unit,
    tag: String
) {
    val colors = LocalMNotesColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .border(1.dp, colors.borderSoft, RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 7.dp)
            .testTag(tag)
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
        Text(text = label, color = tint, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}
