package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.model.FloatingBadgeItem
import com.example.ui.theme.LocalMNotesColors
import kotlin.math.abs

/**
 * Pure toggle helper for floating icon selection:
 * - First tap selects [tappedId]
 * - Second tap on the SAME icon deselects (returns null)
 * - Tapping a DIFFERENT icon switches selection to [tappedId]
 */
fun toggleFloatingIconSelection(currentSelectedId: String?, tappedId: String): String? =
    if (currentSelectedId == tappedId) null else tappedId

/**
 * Preserved alias rendering any floating badge icon via the unified FloatingBadgeVisual.
 */
@Composable
fun OriginalCircularCharacterIcon(
    stateId: String,
    sizeDp: Float,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    FloatingBadgeVisual(
        iconId = stateId,
        sizeDp = sizeDp,
        contentDescription = contentDescription,
        modifier = modifier
    )
}

/**
 * Shared confirmation dialog shown before deleting any floating icon
 * (used from the on-screen floating icon menu, Settings -> Floating Icons, and Icon Library).
 */
@Composable
fun FloatingBadgeDeleteConfirmDialog(
    badge: FloatingBadgeItem,
    lang: String,
    onConfirmDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalMNotesColors.current
    val spec = remember(badge.state) { FloatingBadgeCatalog.byId(badge.state) }
    val iconName = if (lang == "en") spec.nameEn else spec.nameAr

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.cardBg,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFEE2E2)),
                contentAlignment = Alignment.Center
            ) {
                FloatingBadgeVisual(
                    iconId = badge.state,
                    sizeDp = 40f,
                    contentDescription = iconName
                )
            }
        },
        title = {
            Text(
                text = if (lang == "en") "Delete floating icon?" else "حذف الأيقونة العائمة؟",
                color = colors.textDark,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 17.sp
            )
        },
        text = {
            Text(
                text = if (lang == "en") {
                    "\"$iconName\" will be permanently removed from the screen and stay deleted after reopening the app. You can restore deleted icons anytime in Settings → Floating Icons."
                } else {
                    "سيتم حذف \"$iconName\" من الشاشة وسيستمر الحذف بعد إعادة فتح التطبيق. يمكنك استعادة الأيقونات المحذوفة في أي وقت من الإعدادات ← الأيقونات العائمة."
                },
                color = colors.textMuted,
                fontSize = 13.5.sp,
                lineHeight = 20.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmDelete,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFDC2626),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("floating_badge_delete_confirm")
            ) {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = if (lang == "en") "Delete icon" else "حذف الأيقونة",
                    fontWeight = FontWeight.ExtraBold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("floating_badge_delete_cancel")
            ) {
                Text(
                    text = if (lang == "en") "Cancel" else "إلغاء",
                    color = colors.textDark,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}

/**
 * Independent top-most overlay layer for all floating circular icons on the app screen.
 *
 * Guarantees:
 * 1. Physical LTR screen coordinates inside system-bar-safe insets (statusBarsPadding +
 *    navigationBarsPadding) so icons never get stuck behind system bars or drift on RTL/LTR.
 * 2. Smooth 1:1 GPU drag on mobile touch — moves ONLY the grabbed icon freely across the usable
 *    screen and immediately persists its position on release.
 * 3. Long-press (~420ms with haptic feedback) OR clean tap opens a compact floating menu with:
 *    - "Delete icon" (asks confirmation dialog before deleting)
 *    - "Reset to default position"
 *    - "Cancel"
 *    (plus quick Customize shortcut to the 160+ icon library).
 * 4. Non-icon screen area is 100% pass-through so buttons and content underneath remain unobstructed.
 */
@Composable
fun FloatingCharacterBadgesOverlay(
    badges: List<FloatingBadgeItem>,
    selectedBadgeId: String?,
    lang: String,
    onToggleSelectBadge: (FloatingBadgeItem) -> Unit,
    onSelectBadge: (FloatingBadgeItem) -> Unit = onToggleSelectBadge,
    onDismissSelection: () -> Unit = {},
    onOpenBadgeLibrary: (FloatingBadgeItem) -> Unit,
    onResetBadgePosition: (FloatingBadgeItem) -> Unit = {},
    onDuplicateBadge: (FloatingBadgeItem) -> Unit = {},
    onDeleteBadge: (FloatingBadgeItem) -> Unit = {},
    onUpdatePosition: (badgeId: String, xFraction: Float, yFraction: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val visibleBadges = remember(badges) { badges.filter { it.visible } }
    var pendingDeleteBadge by remember { mutableStateOf<FloatingBadgeItem?>(null) }

    if (pendingDeleteBadge != null) {
        val target = pendingDeleteBadge!!
        FloatingBadgeDeleteConfirmDialog(
            badge = target,
            lang = lang,
            onConfirmDelete = {
                pendingDeleteBadge = null
                onDismissSelection()
                onDeleteBadge(target)
            },
            onDismiss = {
                pendingDeleteBadge = null
            }
        )
    }

    if (visibleBadges.isEmpty()) return

    val density = LocalDensity.current
    val colors = LocalMNotesColors.current
    val haptic = LocalHapticFeedback.current
    val appLayoutDirection = LocalLayoutDirection.current

    val latestOnToggleSelect by rememberUpdatedState(onToggleSelectBadge)
    val latestOnSelect by rememberUpdatedState(onSelectBadge)
    val latestOnDismissSelection by rememberUpdatedState(onDismissSelection)
    val latestOnUpdatePosition by rememberUpdatedState(onUpdatePosition)

    // Always position floating icons in physical top-left (LTR) coordinates so Arabic RTL
    // never flips Alignment.TopStart to TopEnd or causes coordinate drift.
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        BoxWithConstraints(
            modifier = modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .zIndex(60f)
        ) {
            val measuredWidthPx = with(density) { maxWidth.toPx() }.coerceAtLeast(1f)
            val measuredHeightPx = with(density) { maxHeight.toPx() }.coerceAtLeast(1f)

            // Keep stable full-screen reference height so opening the IME keyboard does not
            // temporarily compress maxY and shift floating icons on their own.
            var stableWidthPx by remember { mutableFloatStateOf(measuredWidthPx) }
            var stableHeightPx by remember { mutableFloatStateOf(measuredHeightPx) }

            LaunchedEffect(measuredWidthPx, measuredHeightPx) {
                val widthChanged = abs(measuredWidthPx - stableWidthPx) > 12f
                if (widthChanged) {
                    stableWidthPx = measuredWidthPx
                    stableHeightPx = measuredHeightPx
                } else if (measuredHeightPx >= stableHeightPx * 0.80f) {
                    stableHeightPx = maxOf(stableHeightPx, measuredHeightPx)
                }
            }

            visibleBadges.forEach { badge ->
                val currentBadge by rememberUpdatedState(badge)
                val isSelected = badge.id == selectedBadgeId
                val visualSizeDp = badge.sizeDp.coerceIn(26f, 180f)
                val boxSizeDp = maxOf(visualSizeDp, 48f)
                val boxSizePx = with(density) { boxSizeDp.dp.toPx() }
                val maxX = (stableWidthPx - boxSizePx).coerceAtLeast(1f)
                val maxY = (stableHeightPx - boxSizePx).coerceAtLeast(1f)

                var offsetX by remember(badge.id) {
                    mutableFloatStateOf((badge.xFraction.coerceIn(0f, 1f) * maxX).coerceIn(0f, maxX))
                }
                var offsetY by remember(badge.id) {
                    mutableFloatStateOf((badge.yFraction.coerceIn(0f, 1f) * maxY).coerceIn(0f, maxY))
                }
                var isDragging by remember(badge.id) { mutableStateOf(false) }

                // Sync ONLY from external persisted updates or real window resizes while not dragging
                LaunchedEffect(badge.xFraction, badge.yFraction, boxSizePx, maxX, maxY) {
                    if (!isDragging) {
                        offsetX = (badge.xFraction.coerceIn(0f, 1f) * maxX).coerceIn(0f, maxX)
                        offsetY = (badge.yFraction.coerceIn(0f, 1f) * maxY).coerceIn(0f, maxY)
                    }
                }

                val spec = remember(badge.state) { FloatingBadgeCatalog.byId(badge.state) }
                val stateDesc = if (lang == "en") spec.nameEn else spec.nameAr

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .zIndex(if (isDragging) 99f else if (isSelected) 80f else 60f)
                        .graphicsLayer {
                            translationX = offsetX
                            translationY = offsetY
                            if (isDragging) {
                                scaleX = 1.08f
                                scaleY = 1.08f
                            }
                        }
                        .size(boxSizeDp.dp)
                        .pointerInput(badge.id, badge.locked, maxX, maxY) {
                            val dragThresholdPx = 6.dp.toPx()
                            val dragThresholdSq = dragThresholdPx * dragThresholdPx
                            val longPressTimeoutMs = 420L

                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                val pointerId = down.id
                                down.consume()

                                val downTimeMs = System.currentTimeMillis()
                                var totalDx = 0f
                                var totalDy = 0f
                                var dragging = false
                                var longPressTriggered = false

                                while (true) {
                                    val elapsed = System.currentTimeMillis() - downTimeMs
                                    val event = if (!dragging && !longPressTriggered) {
                                        val remaining = (longPressTimeoutMs - elapsed).coerceAtLeast(1L)
                                        withTimeoutOrNull(remaining) {
                                            awaitPointerEvent(PointerEventPass.Main)
                                        }
                                    } else {
                                        awaitPointerEvent(PointerEventPass.Main)
                                    }

                                    if (event == null) {
                                        // Finger held steady past long-press threshold -> open menu!
                                        longPressTriggered = true
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        latestOnSelect(currentBadge)
                                        continue
                                    }

                                    val change = event.changes.firstOrNull { it.id == pointerId } ?: break
                                    if (!change.pressed) {
                                        change.consume()
                                        if (dragging) {
                                            isDragging = false
                                            latestOnUpdatePosition(
                                                currentBadge.id,
                                                (offsetX / maxX).coerceIn(0f, 1f),
                                                (offsetY / maxY).coerceIn(0f, 1f)
                                            )
                                        } else if (!longPressTriggered) {
                                            // Clean tap toggles menu open / closed
                                            latestOnToggleSelect(currentBadge)
                                        }
                                        break
                                    }

                                    val delta = change.positionChange()
                                    if (delta.x != 0f || delta.y != 0f) {
                                        change.consume()
                                        if (!currentBadge.locked) {
                                            if (!dragging) {
                                                totalDx += delta.x
                                                totalDy += delta.y
                                                if (totalDx * totalDx + totalDy * totalDy >= dragThresholdSq) {
                                                    dragging = true
                                                    isDragging = true
                                                    if (longPressTriggered) {
                                                        latestOnDismissSelection()
                                                    }
                                                    offsetX = (offsetX + totalDx).coerceIn(0f, maxX)
                                                    offsetY = (offsetY + totalDy).coerceIn(0f, maxY)
                                                }
                                            } else {
                                                offsetX = (offsetX + delta.x).coerceIn(0f, maxX)
                                                offsetY = (offsetY + delta.y).coerceIn(0f, maxY)
                                            }
                                        } else {
                                            // Locked icons cannot be dragged; still track slop so slight jitter doesn't break tap
                                            totalDx += delta.x
                                            totalDy += delta.y
                                        }
                                    }
                                }
                            }
                        }
                        .testTag("floating_badge_${badge.id}")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = if (isSelected) {
                            Modifier
                                .clip(CircleShape)
                                .border(2.5.dp, colors.primary, CircleShape)
                                .padding(2.dp)
                        } else {
                            Modifier
                        }
                    ) {
                        FloatingBadgeVisual(
                            iconId = badge.state,
                            sizeDp = visualSizeDp,
                            contentDescription = stateDesc
                        )
                    }
                }

                // Compact context menu shown on long-press or tap of this floating icon
                if (isSelected && !isDragging) {
                    val menuWidthDp = 224.dp
                    val menuWidthPx = with(density) { menuWidthDp.toPx() }
                    val approxMenuHeightPx = with(density) { 196.dp.toPx() }
                    val gapPx = with(density) { 10.dp.toPx() }
                    val edgePadPx = with(density) { 12.dp.toPx() }

                    val placeAbove = offsetY > approxMenuHeightPx + gapPx + edgePadPx
                    val menuY = if (placeAbove) {
                        (offsetY - approxMenuHeightPx - gapPx).coerceAtLeast(edgePadPx)
                    } else {
                        (offsetY + boxSizePx + gapPx)
                            .coerceAtMost((stableHeightPx - approxMenuHeightPx - edgePadPx).coerceAtLeast(edgePadPx))
                    }
                    val menuX = (offsetX + (boxSizePx - menuWidthPx) * 0.5f)
                        .coerceIn(edgePadPx, (stableWidthPx - menuWidthPx - edgePadPx).coerceAtLeast(edgePadPx))

                    CompositionLocalProvider(LocalLayoutDirection provides appLayoutDirection) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = colors.cardBg.copy(alpha = 0.98f),
                            shadowElevation = 10.dp,
                            tonalElevation = 4.dp,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .zIndex(95f)
                                .graphicsLayer {
                                    translationX = menuX
                                    translationY = menuY
                                }
                                .width(menuWidthDp)
                                .border(
                                    1.2.dp,
                                    colors.primary.copy(alpha = 0.45f),
                                    RoundedCornerShape(18.dp)
                                )
                                .testTag("floating_badge_actions_${badge.id}")
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 6.dp)
                            ) {
                                // Header strip: icon name + quick Customize button
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = stateDesc,
                                        color = colors.textMuted,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(colors.primarySoft)
                                            .clickable { onOpenBadgeLibrary(badge) }
                                            .testTag("floating_badge_edit_${badge.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Tune,
                                            contentDescription = if (lang == "en") "Customize icon" else "تخصيص الأيقونة",
                                            tint = colors.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                HorizontalDivider(
                                    color = colors.borderSoft.copy(alpha = 0.8f),
                                    thickness = 0.8.dp
                                )

                                // 1. Reset to default position
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 48.dp)
                                        .clickable {
                                            onDismissSelection()
                                            onResetBadgePosition(badge)
                                        }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                        .testTag("floating_badge_menu_reset_${badge.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.RestartAlt,
                                        contentDescription = null,
                                        tint = colors.primary,
                                        modifier = Modifier.size(19.dp)
                                    )
                                    Text(
                                        text = if (lang == "en") "Reset to default position" else "إعادة للموقع الافتراضي",
                                        color = colors.textDark,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                // 2. Delete icon (opens confirmation dialog)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 48.dp)
                                        .clickable {
                                            pendingDeleteBadge = badge
                                        }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                        .testTag("floating_badge_del_${badge.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.DeleteOutline,
                                        contentDescription = null,
                                        tint = Color(0xFFDC2626),
                                        modifier = Modifier.size(19.dp)
                                    )
                                    Text(
                                        text = if (lang == "en") "Delete icon" else "حذف الأيقونة",
                                        color = Color(0xFFDC2626),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                HorizontalDivider(
                                    color = colors.borderSoft.copy(alpha = 0.7f),
                                    thickness = 0.8.dp
                                )

                                // 3. Cancel
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 46.dp)
                                        .clickable { onDismissSelection() }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                        .testTag("floating_badge_menu_cancel_${badge.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = null,
                                        tint = colors.textMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = if (lang == "en") "Cancel" else "إلغاء",
                                        color = colors.textMuted,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
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
}
