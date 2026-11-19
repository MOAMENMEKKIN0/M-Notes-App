package com.example.ui.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.sizeIn
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.FloatingBadgeItem

/**
 * Preserved alias rendering any floating badge icon via the unified FloatingBadgeVisual
 * (exact original circular character illustration for "sleeping" and all 110+ icons).
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
 * Independent overlay layer for all floating circular icons on the app screen.
 *
 * Dual interaction behavior:
 * 1. TAP (release without dragging past tiny 3.5dp threshold): opens THAT icon's dedicated
 *    FloatingBadgeLibrarySheet (never opens the main photo picker, never moves the icon).
 * 2. DIRECT DRAG: moves THAT icon freely anywhere on screen at 120fps via GPU graphicsLayer
 *    translation with zero recomposition, zero relayout, and zero easing/spring lag.
 */
@Composable
fun FloatingCharacterBadgesOverlay(
    badges: List<FloatingBadgeItem>,
    lang: String,
    onTapBadge: (FloatingBadgeItem) -> Unit,
    onUpdatePosition: (badgeId: String, xFraction: Float, yFraction: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val visibleBadges = remember(badges) { badges.filter { it.visible } }
    if (visibleBadges.isEmpty()) return

    val density = LocalDensity.current
    val latestOnTap by rememberUpdatedState(onTapBadge)
    val latestOnUpdatePosition by rememberUpdatedState(onUpdatePosition)

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val maxWidthPx = with(density) { maxWidth.toPx() }
        val maxHeightPx = with(density) { maxHeight.toPx() }

        visibleBadges.forEach { badge ->
            val currentBadge by rememberUpdatedState(badge)
            val badgeSizePx = with(density) { badge.sizeDp.coerceIn(26f, 180f).dp.toPx() }
            val maxX = (maxWidthPx - badgeSizePx).coerceAtLeast(1f)
            val maxY = (maxHeightPx - badgeSizePx).coerceAtLeast(1f)

            var offsetX by remember(badge.id) { mutableFloatStateOf(badge.xFraction.coerceIn(0f, 1f) * maxX) }
            var offsetY by remember(badge.id) { mutableFloatStateOf(badge.yFraction.coerceIn(0f, 1f) * maxY) }
            var isDragging by remember(badge.id) { mutableStateOf(false) }

            // Real-time sync from Settings sliders, interactive preview drags, or persisted state
            LaunchedEffect(badge.xFraction, badge.yFraction, badge.sizeDp, maxX, maxY) {
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
                    .graphicsLayer {
                        // GPU-only translation: zero recomposition, zero relayout, 1:1 under finger
                        translationX = offsetX
                        translationY = offsetY
                    }
                    .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                    .pointerInput(badge.id, badge.locked, maxX, maxY) {
                        val dragThresholdPx = 3.5.dp.toPx()
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
                                            latestOnTap(currentBadge)
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
                                            latestOnTap(currentBadge)
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
                FloatingBadgeVisual(
                    iconId = badge.state,
                    sizeDp = badge.sizeDp,
                    contentDescription = stateDesc
                )
            }
        }
    }
}
