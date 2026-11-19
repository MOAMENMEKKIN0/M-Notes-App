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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.VisibilityOff
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FloatingBadgeItem
import com.example.ui.theme.LocalMNotesColors
import kotlin.math.roundToInt

/**
 * Dedicated Icon Selection Library opened ONLY when tapping the independent small
 * circular floating icon on screen (never opens the main photo picker).
 * Preserves the floating icon's exact position and size while letting the user
 * pick from 110+ categorized 3D cartoon icons & pop-out effects or adjust it live.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloatingBadgeLibrarySheet(
    badge: FloatingBadgeItem,
    lang: String,
    onSelectIcon: (String) -> Unit,
    onUpdateSize: (Float) -> Unit,
    onDuplicate: () -> Unit,
    onToggleLock: () -> Unit,
    onHide: () -> Unit,
    onDelete: () -> Unit,
    onOpenFullStudio: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalMNotesColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedCategoryId by remember { mutableStateOf("all") }
    var searchQuery by remember { mutableStateOf("") }

    val activeSpec = remember(badge.state) { FloatingBadgeCatalog.byId(badge.state) }
    val filteredIcons = remember(selectedCategoryId, searchQuery) {
        FloatingBadgeCatalog.filter(selectedCategoryId, searchQuery)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.bgMain,
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Live selected icon preview + Title + Close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(66.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(colors.cardBg)
                        .border(1.2.dp, colors.primary.copy(alpha = 0.35f), RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    FloatingBadgeVisual(
                        iconId = badge.state,
                        sizeDp = 44f,
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
                            "${activeSpec.nameEn} • Size ${badge.sizeDp.roundToInt()}dp • Hold & drag to move"
                        } else {
                            "${activeSpec.nameAr} • الحجم ${badge.sizeDp.roundToInt()} • اضغط مطولاً واسحب للتحريك"
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
                        .clickable(onClick = onDismiss)
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

            // Live Size Control (updates the real icon on screen instantly)
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
                            text = if (lang == "en") "Live Size" else "تغيير الحجم فوراً",
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
                                onClick = { onUpdateSize((badge.sizeDp - 6f).coerceAtLeast(26f)) }
                            )
                            Text(
                                text = "${badge.sizeDp.roundToInt()} dp",
                                color = colors.primary,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            LibraryStepBtn(
                                icon = Icons.Rounded.Add,
                                desc = if (lang == "en") "Larger" else "تكبير",
                                onClick = { onUpdateSize((badge.sizeDp + 6f).coerceAtMost(180f)) }
                            )
                        }
                    }
                    Slider(
                        value = badge.sizeDp.coerceIn(26f, 180f),
                        onValueChange = onUpdateSize,
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

            // Quick Actions Row: Duplicate | Lock | Hide | Full Studio | Delete
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
                    icon = if (badge.locked) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                    label = if (badge.locked) {
                        if (lang == "en") "Locked" else "مقفولة"
                    } else {
                        if (lang == "en") "Free Drag" else "سحب حر"
                    },
                    tint = if (badge.locked) colors.primary else colors.textDark,
                    bg = if (badge.locked) colors.primarySoft else colors.cardBg,
                    onClick = onToggleLock,
                    tag = "library_lock_badge"
                )
                LibraryActionPill(
                    icon = Icons.Rounded.Settings,
                    label = if (lang == "en") "Preview & Settings" else "معاينة وضبط",
                    tint = colors.textDark,
                    bg = colors.cardBg,
                    onClick = {
                        onDismiss()
                        onOpenFullStudio()
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
                        onDismiss()
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
                        onDismiss()
                    },
                    tag = "library_delete_badge"
                )
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                singleLine = true,
                placeholder = {
                    Text(
                        text = if (lang == "en") "Search 110+ icons (Zzz, gym, coffee, walk, food...)" else "ابحث بين 110+ أيقونة (نوم، جيم، قهوة، مشي، أكل...)",
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

            // 4-Column Visual Icon Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(top = 6.dp, bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .testTag("badge_library_grid")
            ) {
                items(filteredIcons, key = { it.id }) { spec ->
                    val isSelected = badge.state == spec.id
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
                                onSelectIcon(spec.id)
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
