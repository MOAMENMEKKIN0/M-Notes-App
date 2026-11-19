package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Mood
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.model.FloatingBadgeItem
import com.example.model.MNotesCatalog
import com.example.ui.theme.LocalMNotesColors
import kotlin.math.roundToInt

/**
 * Exact original circular character badge preserving its exact colors,
 * soft mint backdrop (#E8F7F2), crisp 1.8dp white ring, 5dp shadow,
 * strict 1:1 circular proportions, and illustration details.
 */
@Composable
fun OriginalCircularCharacterIcon(
    stateId: String,
    sizeDp: Float,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    val spec = MNotesCatalog.characterBadgeStateById(stateId)
    val clampedSize = sizeDp.coerceIn(24f, 180f).dp
    Box(
        modifier = modifier
            .size(clampedSize)
            .shadow(5.dp, CircleShape, clip = false)
            .clip(CircleShape)
            .background(Color(0xFFE8F7F2))
            .border(1.8.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = spec.drawableRes),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
        )
    }
}

@Composable
fun FloatingCharacterBadgesOverlay(
    badges: List<FloatingBadgeItem>,
    lang: String,
    onUpdatePosition: (badgeId: String, xFraction: Float, yFraction: Float) -> Unit,
    onUpdateSize: (badgeId: String, sizeDp: Float) -> Unit,
    onCycleState: (badgeId: String, nextStateId: String) -> Unit,
    onDuplicate: (badgeId: String) -> Unit,
    onHide: (badgeId: String) -> Unit,
    onDelete: (badgeId: String) -> Unit,
    onOpenBadgeSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val visibleBadges = remember(badges) { badges.filter { it.visible } }
    if (visibleBadges.isEmpty()) return

    var selectedBadgeId by remember { mutableStateOf<String?>(null) }

    // Clear selection if selected badge was deleted or hidden
    LaunchedEffect(visibleBadges) {
        if (selectedBadgeId != null && visibleBadges.none { it.id == selectedBadgeId }) {
            selectedBadgeId = null
        }
    }

    val density = LocalDensity.current
    val colors = LocalMNotesColors.current

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val maxWidthPx = with(density) { maxWidth.toPx() }
        val maxHeightPx = with(density) { maxHeight.toPx() }

        visibleBadges.forEach { badge ->
            val badgeSizePx = with(density) { badge.sizeDp.coerceIn(26f, 180f).dp.toPx() }
            val maxX = (maxWidthPx - badgeSizePx).coerceAtLeast(1f)
            val maxY = (maxHeightPx - badgeSizePx).coerceAtLeast(1f)

            var offsetX by remember(badge.id) { mutableFloatStateOf(badge.xFraction.coerceIn(0f, 1f) * maxX) }
            var offsetY by remember(badge.id) { mutableFloatStateOf(badge.yFraction.coerceIn(0f, 1f) * maxY) }
            var isDragging by remember(badge.id) { mutableStateOf(false) }

            // Sync from persisted state whenever not actively dragging
            LaunchedEffect(badge.xFraction, badge.yFraction, maxX, maxY, isDragging) {
                if (!isDragging) {
                    offsetX = (badge.xFraction.coerceIn(0f, 1f) * maxX).coerceIn(0f, maxX)
                    offsetY = (badge.yFraction.coerceIn(0f, 1f) * maxY).coerceIn(0f, maxY)
                }
            }

            val spec = MNotesCatalog.characterBadgeStateById(badge.state)
            val stateDesc = if (lang == "en") spec.nameEn else spec.nameAr

            OriginalCircularCharacterIcon(
                stateId = badge.state,
                sizeDp = badge.sizeDp,
                contentDescription = stateDesc,
                modifier = Modifier
                    .absoluteOffset {
                        IntOffset(offsetX.roundToInt(), offsetY.roundToInt())
                    }
                    .pointerInput(badge.id, badge.locked, maxX, maxY) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            var dragged = false
                            val slopChange = awaitTouchSlopOrCancellation(down.id) { change, over ->
                                if (!badge.locked) {
                                    change.consume()
                                    dragged = true
                                    isDragging = true
                                    offsetX = (offsetX + over.x).coerceIn(0f, maxX)
                                    offsetY = (offsetY + over.y).coerceIn(0f, maxY)
                                }
                            }
                            if (slopChange != null && !badge.locked) {
                                drag(slopChange.id) { change ->
                                    val delta = change.positionChange()
                                    if (delta.x != 0f || delta.y != 0f) {
                                        change.consume()
                                        offsetX = (offsetX + delta.x).coerceIn(0f, maxX)
                                        offsetY = (offsetY + delta.y).coerceIn(0f, maxY)
                                    }
                                }
                                isDragging = false
                                onUpdatePosition(
                                    badge.id,
                                    (offsetX / maxX).coerceIn(0f, 1f),
                                    (offsetY / maxY).coerceIn(0f, 1f)
                                )
                            } else if (!dragged) {
                                selectedBadgeId = if (selectedBadgeId == badge.id) null else badge.id
                            }
                        }
                    }
                    .testTag("floating_badge_${badge.id}")
            )

            // Subtle on-demand quick action pill ONLY when user explicitly taps an icon
            if (selectedBadgeId == badge.id) {
                val barWidthPx = with(density) { 268.dp.toPx() }
                val barHeightPx = with(density) { 44.dp.toPx() }
                val gapPx = with(density) { 8.dp.toPx() }
                val barX = (offsetX + badgeSizePx / 2f - barWidthPx / 2f)
                    .coerceIn(with(density) { 8.dp.toPx() }, (maxWidthPx - barWidthPx - with(density) { 8.dp.toPx() }).coerceAtLeast(8f))
                val placeAbove = offsetY > barHeightPx + gapPx + with(density) { 42.dp.toPx() }
                val barY = if (placeAbove) {
                    (offsetY - barHeightPx - gapPx).coerceAtLeast(with(density) { 36.dp.toPx() })
                } else {
                    (offsetY + badgeSizePx + gapPx).coerceAtMost((maxHeightPx - barHeightPx - with(density) { 12.dp.toPx() }).coerceAtLeast(36f))
                }

                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = colors.cardBg.copy(alpha = 0.96f),
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .absoluteOffset { IntOffset(barX.roundToInt(), barY.roundToInt()) }
                        .border(1.dp, colors.borderSoft, RoundedCornerShape(999.dp))
                        .testTag("floating_badge_quickbar_${badge.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        QuickBadgeActionBtn(
                            icon = Icons.Rounded.Remove,
                            tint = colors.textDark,
                            desc = if (lang == "en") "Smaller" else "تصغير",
                            onClick = { onUpdateSize(badge.id, (badge.sizeDp - 8f).coerceAtLeast(26f)) }
                        )
                        QuickBadgeActionBtn(
                            icon = Icons.Rounded.Add,
                            tint = colors.textDark,
                            desc = if (lang == "en") "Larger" else "تكبير",
                            onClick = { onUpdateSize(badge.id, (badge.sizeDp + 8f).coerceAtMost(180f)) }
                        )
                        QuickBadgeActionBtn(
                            icon = Icons.Rounded.Mood,
                            tint = colors.primary,
                            desc = if (lang == "en") "Next State" else "تغيير الحالة",
                            onClick = {
                                val states = MNotesCatalog.characterBadgeStates
                                val curIdx = states.indexOfFirst { it.id == badge.state }.coerceAtLeast(0)
                                val nextState = states[(curIdx + 1) % states.size].id
                                onCycleState(badge.id, nextState)
                            }
                        )
                        QuickBadgeActionBtn(
                            icon = Icons.Rounded.ContentCopy,
                            tint = colors.primary,
                            desc = if (lang == "en") "Duplicate" else "تكرار",
                            onClick = { onDuplicate(badge.id) }
                        )
                        QuickBadgeActionBtn(
                            icon = Icons.Rounded.VisibilityOff,
                            tint = colors.textMuted,
                            desc = if (lang == "en") "Hide" else "إخفاء",
                            onClick = {
                                selectedBadgeId = null
                                onHide(badge.id)
                            }
                        )
                        QuickBadgeActionBtn(
                            icon = Icons.Rounded.DeleteOutline,
                            tint = Color(0xFFEF4444),
                            desc = if (lang == "en") "Delete" else "حذف",
                            onClick = {
                                selectedBadgeId = null
                                onDelete(badge.id)
                            }
                        )
                        QuickBadgeActionBtn(
                            icon = Icons.Rounded.Settings,
                            tint = colors.textDark,
                            desc = if (lang == "en") "Settings" else "الضبط",
                            onClick = {
                                selectedBadgeId = null
                                onOpenBadgeSettings()
                            }
                        )
                        QuickBadgeActionBtn(
                            icon = Icons.Rounded.Close,
                            tint = colors.textMuted,
                            desc = if (lang == "en") "Close" else "إغلاق",
                            onClick = { selectedBadgeId = null }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickBadgeActionBtn(
    icon: ImageVector,
    tint: Color,
    desc: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = desc,
            tint = tint,
            modifier = Modifier.size(17.dp)
        )
    }
}
