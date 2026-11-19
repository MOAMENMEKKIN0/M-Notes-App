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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FloatingBadgeItem
import com.example.model.MNotesCatalog
import com.example.ui.MNotesViewModel
import com.example.ui.theme.LocalMNotesColors
import kotlin.math.roundToInt

@Composable
fun FloatingBadgeSettingsFolderContent(
    badges: List<FloatingBadgeItem>,
    lang: String,
    viewModel: MNotesViewModel
) {
    val colors = LocalMNotesColors.current
    var newIconStateId by remember { mutableStateOf("sleeping") }
    val visibleCount = badges.count { it.visible }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Original Character Asset Summary Card
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = colors.bgMain,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, colors.borderSoft, RoundedCornerShape(18.dp))
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OriginalCircularCharacterIcon(
                        stateId = newIconStateId,
                        sizeDp = 58f,
                        contentDescription = if (lang == "en") "Original circular character icon" else "الأيقونة الدائرية الأصلية"
                    )
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = if (lang == "en") "Original Circular Character Icon" else "الأيقونة الدائرية للشخصية (التصميم الأصلي)",
                            color = colors.textDark,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = if (lang == "en") {
                                "Drag any copy freely on screen • Active: $visibleCount / ${badges.size}"
                            } else {
                                "اسحب أي نسخة بحرية على الشاشة • الظاهر: $visibleCount من ${badges.size}"
                            },
                            color = colors.textMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // State variations picker derived from the exact original icon
                Text(
                    text = if (lang == "en") "Choose State for New Copy (Same Original Character):" else "اختر الحالة عند إضافة نسخة جديدة (نفس الشخصية الأصلية):",
                    color = colors.textMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MNotesCatalog.characterBadgeStates.forEach { stateSpec ->
                        val active = newIconStateId == stateSpec.id
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (active) colors.primarySoft else colors.cardBg)
                                .border(
                                    width = if (active) 1.6.dp else 1.dp,
                                    color = if (active) colors.primary else colors.borderSoft,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    viewModel.playSound("nav")
                                    newIconStateId = stateSpec.id
                                }
                                .padding(horizontal = 10.dp, vertical = 7.dp)
                                .testTag("badge_new_state_${stateSpec.id}")
                        ) {
                            OriginalCircularCharacterIcon(
                                stateId = stateSpec.id,
                                sizeDp = 44f,
                                contentDescription = if (lang == "en") stateSpec.nameEn else stateSpec.nameAr
                            )
                            Text(
                                text = if (lang == "en") stateSpec.nameEn else stateSpec.nameAr,
                                color = if (active) colors.primary else colors.textDark,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                // Add New Copy + Show All / Hide All Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.addFloatingBadge(stateId = newIconStateId, sizeDp = 48f) },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                        modifier = Modifier
                            .weight(1.4f)
                            .height(44.dp)
                            .testTag("add_floating_badge_btn")
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (lang == "en") "Add Icon" else "إضافة نسخة",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    if (badges.isNotEmpty()) {
                        val allVisible = badges.all { it.visible }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(colors.cardBg)
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
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Independent Per-Icon Controls List
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
                        "No circular icons on screen yet. Tap \"Add Icon\" above to place a copy."
                    } else {
                        "لا توجد أي نسخ على الشاشة حالياً. اضغط على «إضافة نسخة» بالأعلى لإضافة الأيقونة."
                    },
                    color = colors.textMuted,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
        } else {
            Text(
                text = if (lang == "en") "Active Copies (${badges.size}) — Independent Control" else "النسخ الموجودة (${badges.size}) — تحكم مستقل لكل نسخة",
                color = colors.textMuted,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.ExtraBold
            )

            badges.forEachIndexed { index, badge ->
                FloatingBadgeItemControlCard(
                    index = index,
                    badge = badge,
                    lang = lang,
                    onUpdateState = { st -> viewModel.updateFloatingBadgeState(badge.id, st) },
                    onUpdateSize = { sz -> viewModel.updateFloatingBadgeSize(badge.id, sz) },
                    onUpdatePosition = { x, y -> viewModel.updateFloatingBadgePosition(badge.id, x, y) },
                    onDuplicate = { viewModel.duplicateFloatingBadge(badge.id) },
                    onToggleVisibility = { viewModel.toggleFloatingBadgeVisibility(badge.id) },
                    onToggleLock = { viewModel.toggleFloatingBadgeLock(badge.id) },
                    onDelete = { viewModel.deleteFloatingBadge(badge.id) }
                )
            }
        }
    }
}

@Composable
private fun FloatingBadgeItemControlCard(
    index: Int,
    badge: FloatingBadgeItem,
    lang: String,
    onUpdateState: (String) -> Unit,
    onUpdateSize: (Float) -> Unit,
    onUpdatePosition: (Float, Float) -> Unit,
    onDuplicate: () -> Unit,
    onToggleVisibility: () -> Unit,
    onToggleLock: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = LocalMNotesColors.current
    val stateSpec = MNotesCatalog.characterBadgeStateById(badge.state)
    val stateName = if (lang == "en") stateSpec.nameEn else stateSpec.nameAr

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (badge.visible) colors.cardBg else colors.bgMain,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (badge.visible) 1.2.dp else 1.dp,
                color = if (badge.visible) colors.primary.copy(alpha = 0.45f) else colors.borderSoft,
                shape = RoundedCornerShape(18.dp)
            )
            .testTag("floating_badge_control_card_${badge.id}")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card Header: Live circular preview + metadata + lock button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OriginalCircularCharacterIcon(
                    stateId = badge.state,
                    sizeDp = 46f,
                    contentDescription = stateName,
                    modifier = Modifier.alpha(if (badge.visible) 1f else 0.45f)
                )

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (lang == "en") "Icon #${index + 1} • $stateName" else "الأيقونة #${index + 1} • $stateName",
                            color = colors.textDark,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        if (!badge.visible) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(Color(0xFFF1F5F9))
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (lang == "en") "Hidden" else "مخفية",
                                    color = Color(0xFF64748B),
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Text(
                        text = if (lang == "en") {
                            "Size: ${badge.sizeDp.roundToInt()} dp  •  X: ${(badge.xFraction * 100).roundToInt()}%  Y: ${(badge.yFraction * 100).roundToInt()}%"
                        } else {
                            "الحجم: ${badge.sizeDp.roundToInt()}  •  الموضع: أفقياً ${(badge.xFraction * 100).roundToInt()}% • عمودياً ${(badge.yFraction * 100).roundToInt()}%"
                        },
                        color = colors.textMuted,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Lock / Unlock Drag on Screen
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (badge.locked) colors.primarySoft else colors.bgMain)
                        .border(1.dp, if (badge.locked) colors.primary else colors.borderSoft, CircleShape)
                        .clickable(onClick = onToggleLock)
                        .testTag("badge_lock_${badge.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (badge.locked) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                        contentDescription = if (lang == "en") "Lock drag" else "قفل السحب",
                        tint = if (badge.locked) colors.primary else colors.textMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // State Variations Selector for THIS specific copy
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MNotesCatalog.characterBadgeStates.forEach { st ->
                    val selected = badge.state == st.id
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selected) colors.primarySoft else colors.bgMain)
                            .border(
                                width = if (selected) 1.5.dp else 1.dp,
                                color = if (selected) colors.primary else colors.borderSoft,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onUpdateState(st.id) }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                            .testTag("badge_${badge.id}_state_${st.id}")
                    ) {
                        OriginalCircularCharacterIcon(
                            stateId = st.id,
                            sizeDp = 34f,
                            contentDescription = if (lang == "en") st.nameEn else st.nameAr
                        )
                        Text(
                            text = if (lang == "en") st.nameEn.substringBefore(" ") else st.nameAr.substringBefore(" "),
                            color = if (selected) colors.primary else colors.textMuted,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Proportional Size Control (Strict 1:1 Ratio — Zero Distortion)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (lang == "en") "Size (Proportional 1:1)" else "الحجم (بنفس الأبعاد الأصلية بدون تشويه)",
                        color = colors.textDark,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SmallStepButton(
                            icon = Icons.Rounded.Remove,
                            desc = if (lang == "en") "Decrease size" else "تصغير",
                            onClick = { onUpdateSize((badge.sizeDp - 6f).coerceAtLeast(26f)) }
                        )
                        Text(
                            text = "${badge.sizeDp.roundToInt()} dp",
                            color = colors.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        SmallStepButton(
                            icon = Icons.Rounded.Add,
                            desc = if (lang == "en") "Increase size" else "تكبير",
                            onClick = { onUpdateSize((badge.sizeDp + 6f).coerceAtMost(180f)) }
                        )
                    }
                }
                Slider(
                    value = badge.sizeDp.coerceIn(26f, 180f),
                    onValueChange = { onUpdateSize(it) },
                    valueRange = 26f..180f,
                    colors = SliderDefaults.colors(
                        thumbColor = colors.primary,
                        activeTrackColor = colors.primary
                    ),
                    modifier = Modifier.testTag("badge_size_slider_${badge.id}")
                )
            }

            // Fine Position Controls (in addition to free dragging directly on screen)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (lang == "en") "Horizontal X (${(badge.xFraction * 100).roundToInt()}%)" else "أفقي X (${(badge.xFraction * 100).roundToInt()}%)",
                        color = colors.textMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = badge.xFraction.coerceIn(0f, 1f),
                        onValueChange = { onUpdatePosition(it, badge.yFraction) },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = colors.primary,
                            activeTrackColor = colors.primary
                        )
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (lang == "en") "Vertical Y (${(badge.yFraction * 100).roundToInt()}%)" else "عمودي Y (${(badge.yFraction * 100).roundToInt()}%)",
                        color = colors.textMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = badge.yFraction.coerceIn(0f, 1f),
                        onValueChange = { onUpdatePosition(badge.xFraction, it) },
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
                // Duplicate
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.primarySoft)
                        .border(1.dp, colors.primary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .clickable(onClick = onDuplicate)
                        .testTag("duplicate_badge_${badge.id}"),
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

                // Hide / Show
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.bgMain)
                        .border(1.dp, colors.borderSoft, RoundedCornerShape(12.dp))
                        .clickable(onClick = onToggleVisibility)
                        .testTag("toggle_visibility_badge_${badge.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (badge.visible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                            contentDescription = null,
                            tint = colors.textDark,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = if (badge.visible) {
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

                // Delete
                Box(
                    modifier = Modifier
                        .weight(0.9f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFEF2F2))
                        .border(1.dp, Color(0xFFFECACA), RoundedCornerShape(12.dp))
                        .clickable(onClick = onDelete)
                        .testTag("delete_badge_${badge.id}"),
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
}

@Composable
private fun SmallStepButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
