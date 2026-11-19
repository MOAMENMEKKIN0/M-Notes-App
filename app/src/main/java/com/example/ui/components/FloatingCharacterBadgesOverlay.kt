package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.model.FloatingBadgeItem
import kotlin.math.roundToInt

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
 * 1. TAP (release without dragging past touch slop): opens THAT icon's dedicated
 *    FloatingBadgeLibrarySheet (never opens the main photo picker, never moves the icon).
 * 2. TOUCH, HOLD & DRAG: moves THAT icon freely anywhere on screen (left, right, up,
 *    down, even far away from the main icon) using physical absoluteOffset so RTL and
 *    LTR dragging always match the user's finger 1:1. Moving never triggers tap.
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
            LaunchedEffect(badge.xFraction, badge.yFraction, badge.sizeDp, maxX, maxY, isDragging) {
                if (!isDragging) {
                    offsetX = (badge.xFraction.coerceIn(0f, 1f) * maxX).coerceIn(0f, maxX)
                    offsetY = (badge.yFraction.coerceIn(0f, 1f) * maxY).coerceIn(0f, maxY)
                }
            }

            val dragScale by animateFloatAsState(
                targetValue = if (isDragging) 1.09f else 1f,
                animationSpec = spring(dampingRatio = 0.72f, stiffness = 700f),
                label = "badge_drag_scale"
            )

            val spec = remember(badge.state) { FloatingBadgeCatalog.byId(badge.state) }
            val stateDesc = if (lang == "en") spec.nameEn else spec.nameAr

            Box(
                modifier = Modifier
                    .absoluteOffset {
                        IntOffset(offsetX.roundToInt(), offsetY.roundToInt())
                    }
                    .graphicsLayer {
                        scaleX = dragScale
                        scaleY = dragScale
                    }
                    .pointerInput(badge.id, badge.locked, maxX, maxY) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            var didDrag = false
                            val slopChange = awaitTouchSlopOrCancellation(down.id) { change, over ->
                                if (!currentBadge.locked) {
                                    change.consume()
                                    didDrag = true
                                    isDragging = true
                                    offsetX = (offsetX + over.x).coerceIn(0f, maxX)
                                    offsetY = (offsetY + over.y).coerceIn(0f, maxY)
                                }
                            }
                            if (slopChange != null && !currentBadge.locked) {
                                drag(slopChange.id) { change ->
                                    val delta = change.positionChange()
                                    if (delta.x != 0f || delta.y != 0f) {
                                        change.consume()
                                        offsetX = (offsetX + delta.x).coerceIn(0f, maxX)
                                        offsetY = (offsetY + delta.y).coerceIn(0f, maxY)
                                    }
                                }
                                isDragging = false
                                latestOnUpdatePosition(
                                    currentBadge.id,
                                    (offsetX / maxX).coerceIn(0f, 1f),
                                    (offsetY / maxY).coerceIn(0f, 1f)
                                )
                            } else if (!didDrag) {
                                // Clean tap: open this floating icon's own Icon Library
                                latestOnTap(currentBadge)
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
