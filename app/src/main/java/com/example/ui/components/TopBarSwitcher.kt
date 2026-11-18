package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.AppStrings
import com.example.ui.theme.LocalMNotesColors

@Composable
fun TopBarSwitcher(
    styleId: String,
    currentPage: Int,
    lang: String,
    onSelectPage: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalMNotesColors.current
    val tab0Label = AppStrings.get(lang, "tasksTab")
    val tab1Label = AppStrings.get(lang, "reqTab")

    when (styleId) {
        "T1" -> {
            BoxWithConstraints(
                modifier = modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(999.dp), clip = false)
                    .clip(RoundedCornerShape(999.dp))
                    .background(colors.cardBg)
                    .border(1.dp, colors.borderSoft, RoundedCornerShape(999.dp))
                    .padding(4.dp)
            ) {
                val halfWidth = maxWidth / 2
                val indicatorOffset by animateDpAsState(
                    targetValue = if (currentPage == 0) 0.dp else halfWidth,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "t1_glider"
                )
                Box(
                    modifier = Modifier
                        .offset(x = indicatorOffset)
                        .width(halfWidth)
                        .height(38.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(colors.primary)
                )
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf(tab0Label, tab1Label).forEachIndexed { idx, label ->
                        val active = currentPage == idx
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .clickable { onSelectPage(idx) }
                                .testTag("topbar_tab_$idx"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (active) Color.White else colors.textMuted,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        "T13" -> {
            Column(modifier = modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    listOf(tab0Label, tab1Label).forEachIndexed { idx, label ->
                        val active = currentPage == idx
                        val lineScale by animateFloatAsState(
                            targetValue = if (active) 1f else 0f,
                            label = "t13_line"
                        )
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSelectPage(idx) }
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .testTag("topbar_tab_$idx"),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = label,
                                color = if (active) colors.textDark else colors.textMuted,
                                fontWeight = if (active) FontWeight.ExtraBold else FontWeight.Bold,
                                fontSize = 13.5.sp
                            )
                            Spacer(Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .width((72 * lineScale).dp)
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(colors.primary)
                            )
                        }
                    }
                }
                HorizontalDivider(color = colors.borderSoft)
            }
        }

        "T18" -> {
            Box(
                modifier = modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = colors.cardBg,
                    shadowElevation = 2.dp,
                    modifier = Modifier.border(1.dp, colors.borderSoft, RoundedCornerShape(999.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(tab0Label, tab1Label).forEachIndexed { idx, label ->
                            val active = currentPage == idx
                            val bg by animateColorAsState(
                                if (active) colors.primary else Color.Transparent,
                                label = "t18_bg"
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(bg)
                                    .clickable { onSelectPage(idx) }
                                    .padding(horizontal = 18.dp, vertical = 8.dp)
                                    .testTag("topbar_tab_$idx"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (active) Color.White else colors.textMuted,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        "T20" -> {
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(tab0Label, tab1Label).forEachIndexed { idx, label ->
                    val active = currentPage == idx
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = colors.cardBg,
                        modifier = Modifier
                            .weight(1f)
                            .border(
                                width = 1.dp,
                                color = if (active) colors.textDark else colors.borderSoft,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onSelectPage(idx) }
                            .testTag("topbar_tab_$idx")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(if (active) 10.dp else 8.dp)
                                    .clip(CircleShape)
                                    .background(if (active) colors.primary else Color(0xFFCBD5E1))
                            )
                            Spacer(Modifier.width(7.dp))
                            Text(
                                text = label,
                                color = if (active) colors.textDark else colors.textMuted,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        else -> {
            // T11: Elevated iOS Pill (default)
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x2994A3B8))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(tab0Label, tab1Label).forEachIndexed { idx, label ->
                    val active = currentPage == idx
                    Surface(
                        shape = RoundedCornerShape(11.dp),
                        color = if (active) colors.cardBg else Color.Transparent,
                        shadowElevation = if (active) 2.dp else 0.dp,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(11.dp))
                            .clickable { onSelectPage(idx) }
                            .testTag("topbar_tab_$idx")
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 9.dp, horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (active) colors.textDark else colors.textMuted,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
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
