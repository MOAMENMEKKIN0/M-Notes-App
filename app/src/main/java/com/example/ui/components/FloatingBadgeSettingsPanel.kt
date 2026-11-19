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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.OpenWith
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import com.example.model.FloatingBadgeItem
import com.example.model.SmartTask
import com.example.model.UserPreferences
import com.example.ui.MNotesViewModel
import com.example.ui.theme.LocalMNotesColors
import kotlin.math.roundToInt

/**
 * Full Floating Circular Icon Studio inside Settings:
 * - REAL Live Interactive Preview Stage showing the user's actual large main circular icon
 *   alongside all floating small icons. Drag any small icon with your finger directly inside
 *   the preview (around or far away from the main icon), resize it, or change its design —
 *   every change immediately updates the REAL floating icon on the main app screen!
 * - Independent copy tabs (#1, #2, #3...) with Add, Duplicate, Hide/Show, Lock, Delete
 * - Embedded searchable, categorized 110+ icon library browser
 */
@Composable
fun FloatingBadgeSettingsFolderContent(
    badges: List<FloatingBadgeItem>,
    prefs: UserPreferences,
    sampleTask: SmartTask?,
    lang: String,
    viewModel: MNotesViewModel
) {
    val colors = LocalMNotesColors.current
    var selectedBadgeId by remember { mutableStateOf<String?>(badges.firstOrNull()?.id) }

    // Keep selected badge valid when copies are added/deleted
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

        // 1. REAL LIVE INTERACTIVE PREVIEW STAGE
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
                            text = if (lang == "en") "Live Interactive Preview" else "معاينة تفاعلية حقيقية (اسحب بإصبعك)",
                            color = colors.textDark,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(colors.primarySoft)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (lang == "en") "Live Sync • $visibleCount/${badges.size}" else "مباشر • $visibleCount/${badges.size}",
                            color = colors.primary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Text(
                    text = if (lang == "en") {
                        "Drag the small icon anywhere around or away from the main circular icon. Position, size & icon design apply immediately to the real app screen."
                    } else {
                        "اسحب الأيقونة الصغيرة بإصبعك لأي مكان حول الأيقونة الرئيسية أو بعيداً عنها — الموضع والحجم والشكل يتطبقان فوراً على الشاشة الرئيسية."
                    },
                    color = colors.textMuted,
                    fontSize = 10.5.sp,
                    lineHeight = 14.sp
                )

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

                // Copy Tabs + Add New Copy + Show/Hide All
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
                            text = if (lang == "en") "Add Icon" else "إضافة أيقونة",
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

        // 2. COPY SELECTOR STRIP (when 1 or more copies exist)
        if (badges.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = colors.bgMain,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.borderSoft, RoundedCornerShape(16.dp))
            ) {
                Text(
                    text = if (lang == "en") {
                        "No floating icons on screen yet. Tap \"Add Icon\" above to create one."
                    } else {
                        "لا توجد أي أيقونات عائمة حالياً. اضغط «إضافة أيقونة» بالأعلى لإضافة واحدة."
                    },
                    color = colors.textMuted,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
        } else if (selectedBadge != null) {
            // Horizontal selector for independent copies (#1, #2, ...)
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
                                width = if (isSelected) 1.6.dp else 1.dp,
                                color = if (isSelected) colors.primary else colors.borderSoft,
                                shape = RoundedCornerShape(999.dp)
                            )
                            .clickable {
                                viewModel.playSound("nav")
                                selectedBadgeId = item.id
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("select_badge_copy_${item.id}")
                    ) {
                        FloatingBadgeVisual(
                            iconId = item.state,
                            sizeDp = 26f,
                            contentDescription = null,
                            modifier = Modifier.alpha(if (item.visible) 1f else 0.4f)
                        )
                        Text(
                            text = if (lang == "en") "Icon #${idx + 1}" else "نسخة #${idx + 1}",
                            color = if (isSelected) colors.primary else colors.textDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            // 3. SELECTED ICON CONTROLS (Size + Fine Position + Duplicate/Hide/Lock/Delete)
            val activeSpec = remember(selectedBadge.state) { FloatingBadgeCatalog.byId(selectedBadge.state) }
            val activeSpecName = if (lang == "en") activeSpec.nameEn else activeSpec.nameAr
            val activeIndex = badges.indexOfFirst { it.id == selectedBadge.id }.coerceAtLeast(0)

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = colors.cardBg,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.borderSoft, RoundedCornerShape(18.dp))
                    .testTag("floating_badge_control_card_${selectedBadge.id}")
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Header row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FloatingBadgeVisual(
                            iconId = selectedBadge.state,
                            sizeDp = 46f,
                            contentDescription = activeSpecName,
                            modifier = Modifier.alpha(if (selectedBadge.visible) 1f else 0.45f)
                        )
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = if (lang == "en") "Icon #${activeIndex + 1} • $activeSpecName" else "الأيقونة #${activeIndex + 1} • $activeSpecName",
                                color = colors.textDark,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (lang == "en") {
                                    "Size: ${selectedBadge.sizeDp.roundToInt()} dp • X: ${(selectedBadge.xFraction * 100).roundToInt()}% • Y: ${(selectedBadge.yFraction * 100).roundToInt()}%"
                                } else {
                                    "الحجم: ${selectedBadge.sizeDp.roundToInt()} • أفقياً ${(selectedBadge.xFraction * 100).roundToInt()}% • عمودياً ${(selectedBadge.yFraction * 100).roundToInt()}%"
                                },
                                color = colors.textMuted,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (selectedBadge.locked) colors.primarySoft else colors.bgMain)
                                .border(1.dp, if (selectedBadge.locked) colors.primary else colors.borderSoft, CircleShape)
                                .clickable { viewModel.toggleFloatingBadgeLock(selectedBadge.id) }
                                .testTag("badge_lock_${selectedBadge.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (selectedBadge.locked) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                                contentDescription = if (lang == "en") "Lock drag" else "قفل السحب",
                                tint = if (selectedBadge.locked) colors.primary else colors.textMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Live Proportional Size Slider (strict 1:1 ratio — zero distortion)
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (lang == "en") "Size (Live on Screen)" else "الحجم (يتغير فوراً على الشاشة)",
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
                                    desc = if (lang == "en") "Decrease size" else "تصغير",
                                    onClick = { viewModel.updateFloatingBadgeSize(selectedBadge.id, (selectedBadge.sizeDp - 6f).coerceAtLeast(26f)) }
                                )
                                Text(
                                    text = "${selectedBadge.sizeDp.roundToInt()} dp",
                                    color = colors.primary,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                SmallStepButton(
                                    icon = Icons.Rounded.Add,
                                    desc = if (lang == "en") "Increase size" else "تكبير",
                                    onClick = { viewModel.updateFloatingBadgeSize(selectedBadge.id, (selectedBadge.sizeDp + 6f).coerceAtMost(180f)) }
                                )
                            }
                        }
                        Slider(
                            value = selectedBadge.sizeDp.coerceIn(26f, 180f),
                            onValueChange = { viewModel.updateFloatingBadgeSize(selectedBadge.id, it) },
                            valueRange = 26f..180f,
                            colors = SliderDefaults.colors(
                                thumbColor = colors.primary,
                                activeTrackColor = colors.primary
                            ),
                            modifier = Modifier.testTag("badge_size_slider_${selectedBadge.id}")
                        )
                    }

                    // Fine Position X & Y Sliders
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (lang == "en") "Horizontal X (${(selectedBadge.xFraction * 100).roundToInt()}%)" else "أفقي X (${(selectedBadge.xFraction * 100).roundToInt()}%)",
                                color = colors.textMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Slider(
                                value = selectedBadge.xFraction.coerceIn(0f, 1f),
                                onValueChange = { viewModel.updateFloatingBadgePosition(selectedBadge.id, it, selectedBadge.yFraction) },
                                valueRange = 0f..1f,
                                colors = SliderDefaults.colors(
                                    thumbColor = colors.primary,
                                    activeTrackColor = colors.primary
                                )
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (lang == "en") "Vertical Y (${(selectedBadge.yFraction * 100).roundToInt()}%)" else "عمودي Y (${(selectedBadge.yFraction * 100).roundToInt()}%)",
                                color = colors.textMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Slider(
                                value = selectedBadge.yFraction.coerceIn(0f, 1f),
                                onValueChange = { viewModel.updateFloatingBadgePosition(selectedBadge.id, selectedBadge.xFraction, it) },
                                valueRange = 0f..1f,
                                colors = SliderDefaults.colors(
                                    thumbColor = colors.primary,
                                    activeTrackColor = colors.primary
                                )
                            )
                        }
                    }

                    // Action Buttons Row: Duplicate | Hide/Show | Delete
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.primarySoft)
                                .border(1.dp, colors.primary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .clickable {
                                    val dupId = viewModel.duplicateFloatingBadge(selectedBadge.id)
                                    if (dupId != null) selectedBadgeId = dupId
                                }
                                .testTag("duplicate_badge_${selectedBadge.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Rounded.ContentCopy, contentDescription = null, tint = colors.primary, modifier = Modifier.size(15.dp))
                                Text(
                                    text = if (lang == "en") "Duplicate" else "تكرار",
                                    color = colors.primary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.bgMain)
                                .border(1.dp, colors.borderSoft, RoundedCornerShape(12.dp))
                                .clickable { viewModel.toggleFloatingBadgeVisibility(selectedBadge.id) }
                                .testTag("toggle_visibility_badge_${selectedBadge.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (selectedBadge.visible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                    contentDescription = null,
                                    tint = colors.textDark,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = if (selectedBadge.visible) {
                                        if (lang == "en") "Hide" else "إخفاء"
                                    } else {
                                        if (lang == "en") "Show" else "إظهار"
                                    },
                                    color = colors.textDark,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(0.9f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFEF2F2))
                                .border(1.dp, Color(0xFFFECACA), RoundedCornerShape(12.dp))
                                .clickable { viewModel.deleteFloatingBadge(selectedBadge.id) }
                                .testTag("delete_badge_${selectedBadge.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Rounded.DeleteOutline, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(15.dp))
                                Text(
                                    text = if (lang == "en") "Delete" else "حذف",
                                    color = Color(0xFFEF4444),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }

            // 4. EMBEDDED 110+ ICON LIBRARY BROWSER (Updates selected icon live!)
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
                            "Choose Icon Design (${FloatingBadgeCatalog.icons.size} Icons & Pop-Out Effects)"
                        } else {
                            "اختر تصميم الأيقونة (${FloatingBadgeCatalog.icons.size} أيقونة مع مؤثرات بارزة)"
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

                    // Category filter pills
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FloatingBadgeCatalog.categories.forEach { cat ->
                            val isSel = selectedCategoryId == cat.id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(if (isSel) colors.primary else colors.bgMain)
                                    .border(
                                        1.dp,
                                        if (isSel) colors.primary else colors.borderSoft,
                                        RoundedCornerShape(999.dp)
                                    )
                                    .clickable { selectedCategoryId = cat.id }
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
                            .testTag("settings_badge_library_grid")
                    ) {
                        items(filteredIcons, key = { it.id }) { spec ->
                            val isActive = selectedBadge.state == spec.id
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
                                        viewModel.updateFloatingBadgeState(selectedBadge.id, spec.id)
                                    }
                                    .padding(top = 10.dp, bottom = 6.dp, start = 3.dp, end = 3.dp)
                                    .testTag("settings_pick_icon_${spec.id}")
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
}

/**
 * Interactive Preview Stage inside Settings showing:
 * - The REAL large main circular icon (with the user's actual photo/theme)
 * - Every visible floating small icon at its exact (xFraction, yFraction) and proportional size
 * - Direct finger dragging on any small icon inside the stage (moves BOTH preview and real screen!)
 */
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
            .height(250.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(colors.bgTopGlow, colors.bgMain, colors.bgMain)
                )
            )
            .border(1.dp, colors.borderSoft, RoundedCornerShape(18.dp))
            .testTag("floating_badge_preview_stage")
    ) {
        val stageWidthPx = with(density) { maxWidth.toPx() }
        val stageHeightPx = with(density) { maxHeight.toPx() }

        // Subtle alignment dot grid so the user sees spatial freedom across the entire stage
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

        // Realistic Mini Main App Banner + Main Circular Icon (Fixed in its original place!)
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
                    .fillMaxWidth(0.92f)
                    .border(1.dp, colors.borderSoft, RoundedCornerShape(20.dp))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // The ACTUAL large main circular icon (stays fixed when small icon moves!)
                    MediaAvatarSlot(
                        imgUri = sampleTask?.imgUri?.ifBlank { prefs.bannerImg } ?: prefs.bannerImg,
                        size = prefs.taskImgDp.coerceIn(50, 74).dp,
                        shapeId = prefs.bannerPhotoShape,
                        useBannerDefaultIfBlank = true,
                        contentDescription = if (lang == "en") "Main circular icon" else "الأيقونة الدائرية الرئيسية"
                    )
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = sampleTask?.title ?: prefs.bannerTitle.ifBlank { "M Notes" },
                            color = colors.textDark,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (lang == "en") "Main icon stays fixed • Drag small icon freely" else "الأيقونة الرئيسية ثابتة • اسحب الصغيرة بحرية",
                            color = colors.textMuted,
                            fontSize = 10.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // All visible independent small floating icons — freely draggable inside the preview!
        badges.filter { it.visible }.forEach { badge ->
            val currentBadge by rememberUpdatedState(badge)
            val previewDiameterDp = (badge.sizeDp * 0.72f).coerceIn(24f, 112f)
            val badgePx = with(density) { previewDiameterDp.dp.toPx() }
            val maxX = (stageWidthPx - badgePx).coerceAtLeast(1f)
            val maxY = (stageHeightPx - badgePx).coerceAtLeast(1f)

            var offsetX by remember(badge.id) { mutableFloatStateOf(badge.xFraction.coerceIn(0f, 1f) * maxX) }
            var offsetY by remember(badge.id) { mutableFloatStateOf(badge.yFraction.coerceIn(0f, 1f) * maxY) }
            var isDragging by remember(badge.id) { mutableStateOf(false) }

            LaunchedEffect(badge.xFraction, badge.yFraction, badge.sizeDp, maxX, maxY, isDragging) {
                if (!isDragging) {
                    offsetX = (badge.xFraction.coerceIn(0f, 1f) * maxX).coerceIn(0f, maxX)
                    offsetY = (badge.yFraction.coerceIn(0f, 1f) * maxY).coerceIn(0f, maxY)
                }
            }

            val isSelected = badge.id == selectedBadgeId
            val dragScale by animateFloatAsState(
                targetValue = if (isDragging) 1.12f else 1f,
                animationSpec = spring(dampingRatio = 0.7f, stiffness = 700f),
                label = "preview_drag_scale"
            )

            Box(
                modifier = Modifier
                    .absoluteOffset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                    .graphicsLayer {
                        scaleX = dragScale
                        scaleY = dragScale
                    }
                    .then(
                        if (isSelected) {
                            Modifier.border(2.dp, colors.primary.copy(alpha = 0.65f), CircleShape)
                        } else {
                            Modifier
                        }
                    )
                    .pointerInput(badge.id, maxX, maxY) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            latestOnSelect(currentBadge.id)
                            var didDrag = false
                            val slopChange = awaitTouchSlopOrCancellation(down.id) { change, over ->
                                change.consume()
                                didDrag = true
                                isDragging = true
                                offsetX = (offsetX + over.x).coerceIn(0f, maxX)
                                offsetY = (offsetY + over.y).coerceIn(0f, maxY)
                                latestOnDrag(
                                    currentBadge.id,
                                    (offsetX / maxX).coerceIn(0f, 1f),
                                    (offsetY / maxY).coerceIn(0f, 1f)
                                )
                            }
                            if (slopChange != null) {
                                drag(slopChange.id) { change ->
                                    val delta = change.positionChange()
                                    if (delta.x != 0f || delta.y != 0f) {
                                        change.consume()
                                        offsetX = (offsetX + delta.x).coerceIn(0f, maxX)
                                        offsetY = (offsetY + delta.y).coerceIn(0f, maxY)
                                        latestOnDrag(
                                            currentBadge.id,
                                            (offsetX / maxX).coerceIn(0f, 1f),
                                            (offsetY / maxY).coerceIn(0f, 1f)
                                        )
                                    }
                                }
                                isDragging = false
                                latestOnDrag(
                                    currentBadge.id,
                                    (offsetX / maxX).coerceIn(0f, 1f),
                                    (offsetY / maxY).coerceIn(0f, 1f)
                                )
                            } else if (!didDrag) {
                                latestOnSelect(currentBadge.id)
                            }
                        }
                    }
                    .testTag("preview_draggable_badge_${badge.id}")
            ) {
                FloatingBadgeVisual(
                    iconId = badge.state,
                    sizeDp = previewDiameterDp,
                    contentDescription = null
                )
            }
        }
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
            .size(30.dp)
            .clip(CircleShape)
            .background(colors.bgMain)
            .border(1.dp, colors.borderSoft, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = desc, tint = colors.textDark, modifier = Modifier.size(15.dp))
    }
}
