package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
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
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
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
 * Independent top-most overlay layer for all floating circular icons on the app screen.
 *
 * Guarantees:
 * 1. Physical LTR screen coordinates regardless of Arabic RTL / English LTR — icons NEVER
 *    invert, flip, or drift across recomposition, language changes, navigation, or restarts.
 * 2. Stable screen reference dimensions — temporary keyboard/IME or sheet height shrinks do NOT
 *    push icons below tasks or underneath banners.
 * 3. Highest Z-order above banners and task lists so icons stay cleanly visible on top.
 * 4. Tap-to-toggle selection:
 *    - 1st tap selects that icon (shows crisp highlight ring + quick actions pill)
 *    - 2nd tap on the SAME icon immediately deselects it
 *    - Tapping another icon switches selection to that icon
 * 5. Direct 1:1 GPU drag moves ONLY the dragged icon freely across the entire screen and saves
 *    its exact independent position immediately on release.
 */
@Composable
fun FloatingCharacterBadgesOverlay(
    badges: List<FloatingBadgeItem>,
    selectedBadgeId: String?,
    lang: String,
    onToggleSelectBadge: (FloatingBadgeItem) -> Unit,
    onOpenBadgeLibrary: (FloatingBadgeItem) -> Unit,
    onDuplicateBadge: (FloatingBadgeItem) -> Unit = {},
    onDeleteBadge: (FloatingBadgeItem) -> Unit = {},
    onUpdatePosition: (badgeId: String, xFraction: Float, yFraction: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val visibleBadges = remember(badges) { badges.filter { it.visible } }
    if (visibleBadges.isEmpty()) return

    val density = LocalDensity.current
    val colors = LocalMNotesColors.current
    val latestOnToggleSelect by rememberUpdatedState(onToggleSelectBadge)
    val latestOnUpdatePosition by rememberUpdatedState(onUpdatePosition)

    // Always position floating icons in physical top-left (LTR) coordinates so Arabic RTL
    // never flips Alignment.TopStart to TopEnd or causes coordinate drift.
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        BoxWithConstraints(
            modifier = modifier
                .fillMaxSize()
                .zIndex(60f)
        ) {
            val measuredWidthPx = with(density) { maxWidth.toPx() }.coerceAtLeast(1f)
            val measuredHeightPx = with(density) { maxHeight.toPx() }.coerceAtLeast(1f)

            // Keep stable full-screen reference height so opening the IME keyboard does not
            // temporarily compress maxY and shift floating icons down/up on their own.
            var stableWidthPx by remember { mutableFloatStateOf(measuredWidthPx) }
            var stableHeightPx by remember { mutableFloatStateOf(measuredHeightPx) }

            LaunchedEffect(measuredWidthPx, measuredHeightPx) {
                val widthChanged = abs(measuredWidthPx - stableWidthPx) > 12f
                if (widthChanged) {
                    // Real orientation / window resize -> update both axes cleanly
                    stableWidthPx = measuredWidthPx
                    stableHeightPx = measuredHeightPx
                } else if (measuredHeightPx >= stableHeightPx * 0.80f) {
                    // Normal bar inset adjustment (not a temporary IME keyboard shrink)
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
                        }
                        .size(boxSizeDp.dp)
                        .pointerInput(badge.id, badge.locked, maxX, maxY) {
                            val dragThresholdPx = 4.dp.toPx()
                            val dragThresholdSq = dragThresholdPx * dragThresholdPx

                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                val pointerId = down.id
                                if (currentBadge.locked) {
                                    var movedFar = false
                                    val tapSlopSq = (dragThresholdPx * 3f) * (dragThresholdPx * 3f)
                                    while (true) {
                                        val event = awaitPointerEvent(PointerEventPass.Main)
                                        val change = event.changes.firstOrNull { it.id == pointerId } ?: break
                                        if (!change.pressed) {
                                            if (!movedFar) {
                                                change.consume()
                                                latestOnToggleSelect(currentBadge)
                                            }
                                            break
                                        }
                                        val d = change.position - down.position
                                        if (d.x * d.x + d.y * d.y > tapSlopSq) movedFar = true
                                    }
                                } else {
                                    down.consume()
                                    var totalDx = 0f
                                    var totalDy = 0f
                                    var dragging = false

                                    while (true) {
                                        val event = awaitPointerEvent(PointerEventPass.Main)
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
                                            } else {
                                                // Pure tap toggles selection ON / OFF immediately
                                                latestOnToggleSelect(currentBadge)
                                            }
                                            break
                                        }

                                        val delta = change.positionChange()
                                        if (delta.x != 0f || delta.y != 0f) {
                                            change.consume()
                                            if (!dragging) {
                                                totalDx += delta.x
                                                totalDy += delta.y
                                                if (totalDx * totalDx + totalDy * totalDy >= dragThresholdSq) {
                                                    dragging = true
                                                    isDragging = true
                                                    offsetX = (offsetX + totalDx).coerceIn(0f, maxX)
                                                    offsetY = (offsetY + totalDy).coerceIn(0f, maxY)
                                                }
                                            } else {
                                                offsetX = (offsetX + delta.x).coerceIn(0f, maxX)
                                                offsetY = (offsetY + delta.y).coerceIn(0f, maxY)
                                            }
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

                // Compact quick-action pill shown ONLY while this floating icon is selected
                // (disappears immediately on second tap of the same icon!)
                if (isSelected && !isDragging) {
                    val toolbarWidthPx = with(density) { 126.dp.toPx() }
                    val toolbarHeightPx = with(density) { 40.dp.toPx() }
                    val gapPx = with(density) { 8.dp.toPx() }
                    val placeAbove = offsetY > toolbarHeightPx + gapPx + with(density) { 36.dp.toPx() }
                    val barY = if (placeAbove) {
                        (offsetY - toolbarHeightPx - gapPx).coerceAtLeast(8f)
                    } else {
                        (offsetY + boxSizePx + gapPx).coerceAtMost((stableHeightPx - toolbarHeightPx - 8f).coerceAtLeast(8f))
                    }
                    val barX = (offsetX + (boxSizePx - toolbarWidthPx) * 0.5f)
                        .coerceIn(8f, (stableWidthPx - toolbarWidthPx - 8f).coerceAtLeast(8f))

                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = colors.cardBg.copy(alpha = 0.96f),
                        shadowElevation = 6.dp,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .zIndex(90f)
                            .graphicsLayer {
                                translationX = barX
                                translationY = barY
                            }
                            .border(1.2.dp, colors.primary.copy(alpha = 0.55f), RoundedCornerShape(999.dp))
                            .testTag("floating_badge_actions_${badge.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
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
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(colors.bgMain)
                                    .clickable { onDuplicateBadge(badge) }
                                    .testTag("floating_badge_dup_${badge.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ContentCopy,
                                    contentDescription = if (lang == "en") "Duplicate icon" else "تكرار الأيقونة",
                                    tint = colors.textDark,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFEE2E2))
                                    .clickable { onDeleteBadge(badge) }
                                    .testTag("floating_badge_del_${badge.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.DeleteOutline,
                                    contentDescription = if (lang == "en") "Delete icon" else "حذف الأيقونة",
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
