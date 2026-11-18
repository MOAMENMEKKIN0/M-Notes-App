package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.AppStrings
import com.example.model.UserPreferences
import com.example.ui.theme.LocalMNotesColors

fun bannerIconVector(iconId: String): ImageVector? = when (iconId) {
    "clock" -> Icons.Rounded.AccessTime
    "award" -> Icons.Rounded.EmojiEvents
    "star" -> Icons.Rounded.Star
    "target" -> Icons.Rounded.MyLocation
    "gym" -> Icons.Rounded.FitnessCenter
    "flame" -> Icons.Rounded.LocalFireDepartment
    "heart" -> Icons.Rounded.Favorite
    "check" -> Icons.Rounded.Verified
    else -> null
}

@Composable
private fun BannerTitleRow(
    title: String,
    iconId: String,
    titleColor: Color,
    iconChipBg: Color,
    iconTint: Color,
    fontSizeSp: Float = 15f
) {
    val icon = bannerIconVector(iconId)
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(iconChipBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text = title,
            color = titleColor,
            fontSize = fontSizeSp.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun HeroBanner(
    prefs: UserPreferences,
    dueTodayCount: Int,
    isDemo: Boolean = false,
    onBannerPhotoClick: () -> Unit = {}
) {
    val colors = LocalMNotesColors.current
    val lang = prefs.lang
    if (!prefs.showBanner) {
        if (isDemo) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = colors.cardBg,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${AppStrings.get(lang, "banShow")} (OFF)",
                    color = colors.textMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
        return
    }

    val title = prefs.bannerTitle.ifBlank { "M Notes" }
    val sub = prefs.bannerSub.ifBlank { AppStrings.get(lang, "banSubDef") }
    val photoDp = bannerImageDp(prefs.bannerImgSize)
    val photoClick: (() -> Unit)? = if (isDemo) null else onBannerPhotoClick

    when (prefs.bannerStyle) {
        "B2" -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(104.dp)
                    .shadow(3.dp, RoundedCornerShape(20.dp), clip = false)
                    .clip(RoundedCornerShape(20.dp))
                    .then(if (photoClick != null) Modifier.clickable(onClick = photoClick) else Modifier)
            ) {
                MNotesPhotoContent(
                    imgUri = prefs.bannerImg,
                    useBannerDefaultIfBlank = true,
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xC80F172A), Color(0x300F172A))
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(14.dp)
                ) {
                    BannerTitleRow(
                        title = title,
                        iconId = prefs.bannerIcon,
                        titleColor = Color.White,
                        iconChipBg = Color.White.copy(alpha = 0.22f),
                        iconTint = Color.White
                    )
                    Text(
                        text = sub,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        "B3" -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(88.dp)
                    .shadow(2.dp, RoundedCornerShape(20.dp), clip = false)
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.cardBg)
                    .border(1.dp, colors.borderSoft, RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxHeight()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    BannerTitleRow(
                        title = title,
                        iconId = prefs.bannerIcon,
                        titleColor = colors.textDark,
                        iconChipBg = colors.primarySoft,
                        iconTint = colors.primary,
                        fontSizeSp = 14.5f
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = sub,
                        color = colors.textMuted,
                        fontSize = 11.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .then(if (photoClick != null) Modifier.clickable(onClick = photoClick) else Modifier)
                ) {
                    MNotesPhotoContent(
                        imgUri = prefs.bannerImg,
                        useBannerDefaultIfBlank = true,
                        contentDescription = title,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        "B4" -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(3.dp, RoundedCornerShape(999.dp), clip = false)
                    .clip(RoundedCornerShape(999.dp))
                    .background(colors.primary)
                    .padding(start = 12.dp, end = 14.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    MediaAvatarSlot(
                        imgUri = prefs.bannerImg,
                        size = photoDp,
                        shapeId = prefs.bannerPhotoShape,
                        useBannerDefaultIfBlank = true,
                        contentDescription = title,
                        onClick = photoClick
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = title,
                            color = Color.White,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = sub,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 10.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color.White.copy(alpha = 0.22f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$dueTodayCount ${AppStrings.get(lang, "forToday")}",
                        color = Color.White,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        "B5" -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(20.dp), clip = false)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.linearGradient(listOf(colors.primarySoft, colors.bgTopGlow)))
                    .border(1.dp, colors.borderSoft, RoundedCornerShape(20.dp))
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.85f))
                        .padding(horizontal = 11.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MediaAvatarSlot(
                        imgUri = prefs.bannerImg,
                        size = photoDp,
                        shapeId = prefs.bannerPhotoShape,
                        useBannerDefaultIfBlank = true,
                        contentDescription = title,
                        onClick = photoClick
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        BannerTitleRow(
                            title = title,
                            iconId = prefs.bannerIcon,
                            titleColor = colors.textDark,
                            iconChipBg = colors.primarySoft,
                            iconTint = colors.primary,
                            fontSizeSp = 14f
                        )
                        Text(
                            text = sub,
                            color = colors.textMuted,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        "B7" -> {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = colors.cardBg,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.borderSoft, RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MediaAvatarSlot(
                            imgUri = prefs.bannerImg,
                            size = photoDp,
                            shapeId = prefs.bannerPhotoShape,
                            useBannerDefaultIfBlank = true,
                            contentDescription = title,
                            onClick = photoClick
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            BannerTitleRow(
                                title = title,
                                iconId = prefs.bannerIcon,
                                titleColor = colors.textDark,
                                iconChipBg = colors.primarySoft,
                                iconTint = colors.primary,
                                fontSizeSp = 14f
                            )
                            Text(
                                text = sub,
                                color = colors.textMuted,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(colors.borderSoft)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(999.dp))
                                .background(Brush.horizontalGradient(listOf(colors.primary, colors.accent)))
                        )
                    }
                }
            }
        }

        "B8" -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.primarySoft)
                )
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = colors.cardBg,
                    shadowElevation = 3.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 5.dp)
                        .border(1.dp, colors.borderSoft, RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MediaAvatarSlot(
                            imgUri = prefs.bannerImg,
                            size = photoDp,
                            shapeId = prefs.bannerPhotoShape,
                            useBannerDefaultIfBlank = true,
                            contentDescription = title,
                            onClick = photoClick
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            BannerTitleRow(
                                title = title,
                                iconId = prefs.bannerIcon,
                                titleColor = colors.textDark,
                                iconChipBg = colors.primarySoft,
                                iconTint = colors.primary,
                                fontSizeSp = 14f
                            )
                            Text(
                                text = sub,
                                color = colors.textMuted,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        else -> {
            // B1: Soft Glow (default)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(20.dp), clip = false)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.linearGradient(listOf(colors.bgTopGlow, colors.cardBg)))
                    .border(1.dp, colors.borderSoft, RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    BannerTitleRow(
                        title = title,
                        iconId = prefs.bannerIcon,
                        titleColor = colors.textDark,
                        iconChipBg = colors.primarySoft,
                        iconTint = colors.primary
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = sub,
                        color = colors.textMuted,
                        fontSize = 11.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.width(10.dp))
                MediaAvatarSlot(
                    imgUri = prefs.bannerImg,
                    size = photoDp,
                    shapeId = prefs.bannerPhotoShape,
                    useBannerDefaultIfBlank = true,
                    contentDescription = title,
                    onClick = photoClick
                )
            }
        }
    }
}
