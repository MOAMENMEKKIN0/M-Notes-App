package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.theme.LocalMNotesColors
import java.io.File

val OrganicBlobShape: Shape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    moveTo(w * 0.58f, 0f)
    cubicTo(w * 0.92f, 0f, w, h * 0.25f, w, h * 0.52f)
    cubicTo(w, h * 0.85f, w * 0.72f, h, w * 0.45f, h)
    cubicTo(w * 0.14f, h, 0f, h * 0.78f, 0f, h * 0.46f)
    cubicTo(0f, h * 0.14f, w * 0.24f, 0f, w * 0.58f, 0f)
    close()
}

fun shapeForPhoto(shapeId: String): Shape = when (shapeId) {
    "ios" -> RoundedCornerShape(14.dp)
    "blob" -> OrganicBlobShape
    "strip" -> RectangleShape
    else -> CircleShape
}

fun bannerImageDp(sizeId: String): Dp = when (sizeId) {
    "small" -> 42.dp
    "large" -> 66.dp
    else -> 52.dp
}

fun taskImageDp(sizeId: String): Dp = when (sizeId) {
    "small" -> 38.dp
    "large" -> 58.dp
    else -> 46.dp
}

private fun builtInDrawableForUri(uri: String): Int? = when (uri) {
    "res://img_3d_elder_man" -> R.drawable.img_3d_elder_man_1795021150883
    "res://img_3d_elder_beige" -> R.drawable.img_3d_elder_beige_1795021564309
    "res://img_3d_bald_man" -> R.drawable.img_3d_bald_man_1795021150430
    "res://img_3d_bald_brown" -> R.drawable.img_3d_bald_brown_1795021591352
    "res://img_3d_haired_man" -> R.drawable.img_3d_haired_man_1795021151702
    "res://img_3d_haired_mint" -> R.drawable.img_3d_haired_mint_1795021612096
    "res://img_3d_child" -> R.drawable.img_3d_child_1795021152915
    "res://img_3d_child_cream" -> R.drawable.img_3d_child_cream_1795021589727
    "res://img_3d_woman" -> R.drawable.img_3d_woman_1795021154226
    "res://img_3d_woman_beige" -> R.drawable.img_3d_woman_beige_1795021590044
    "res://img_3d_medieval_bald" -> R.drawable.img_3d_medieval_bald_1795022190347
    "res://img_3d_jahiliyyah" -> R.drawable.img_3d_jahiliyyah_1795022191921
    "res://img_3d_ancient_fruits" -> R.drawable.img_3d_ancient_fruits_1795022223609
    "res://img_3d_ancient_bread_grain" -> R.drawable.img_3d_ancient_bread_grain_1795022221497
    "res://img_3d_gym_workout" -> R.drawable.img_3d_gym_workout_1795021155953
    "res://img_3d_gym_chest" -> R.drawable.img_3d_gym_chest_1795021564566
    "res://img_3d_gym_back" -> R.drawable.img_3d_gym_back_1795021563371
    "res://img_3d_gym_legs" -> R.drawable.img_3d_gym_legs_1795021566532
    "res://img_3d_gym_legpress" -> R.drawable.img_3d_gym_legpress_1795022190947
    "res://img_3d_gym_lunges" -> R.drawable.img_3d_gym_lunges_1795022188475
    "res://img_3d_gym_arms" -> R.drawable.img_3d_gym_arms_1795021588933
    "res://img_3d_gym_shoulders" -> R.drawable.img_3d_gym_shoulders_1795021611562
    else -> null
}

@Composable
fun MNotesPhotoContent(
    imgUri: String,
    useBannerDefaultIfBlank: Boolean,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    val builtInRes = builtInDrawableForUri(imgUri)
    when {
        builtInRes != null -> {
            Image(
                painter = painterResource(id = builtInRes),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = modifier
            )
        }
        imgUri == "default" || (imgUri.isBlank() && useBannerDefaultIfBlank) -> {
            Image(
                painter = painterResource(id = R.drawable.img_banner_default),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = modifier
            )
        }
        imgUri.isNotBlank() && File(imgUri).exists() -> {
            AsyncImage(
                model = File(imgUri),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = modifier
            )
        }
        imgUri.isNotBlank() -> {
            AsyncImage(
                model = imgUri,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = modifier
            )
        }
        else -> {
            val colors = LocalMNotesColors.current
            Box(
                modifier = modifier.background(colors.primarySoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Image,
                    contentDescription = contentDescription,
                    tint = colors.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun MediaAvatarSlot(
    imgUri: String,
    size: Dp,
    shapeId: String,
    useBannerDefaultIfBlank: Boolean = false,
    contentDescription: String? = null,
    onClick: (() -> Unit)? = null
) {
    val colors = LocalMNotesColors.current
    val outerShape = shapeForPhoto(shapeId)
    val clickMod = if (onClick != null) {
        Modifier
            .minimumInteractiveComponentSize()
            .clickable(onClick = onClick)
    } else Modifier

    if (shapeId == "ring") {
        Box(
            modifier = Modifier
                .size(size)
                .shadow(4.dp, CircleShape, clip = false)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(colors.primary, colors.accent)))
                .then(clickMod)
                .padding(2.5.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .border(2.dp, Color.White, CircleShape)
                    .background(colors.primarySoft),
                contentAlignment = Alignment.Center
            ) {
                MNotesPhotoContent(
                    imgUri = imgUri,
                    useBannerDefaultIfBlank = useBannerDefaultIfBlank,
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    } else if (shapeId == "strip") {
        Box(
            modifier = Modifier
                .size(size + 10.dp)
                .background(colors.primarySoft)
                .then(clickMod),
            contentAlignment = Alignment.Center
        ) {
            MNotesPhotoContent(
                imgUri = imgUri,
                useBannerDefaultIfBlank = useBannerDefaultIfBlank,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize()
            )
        }
    } else {
        Box(
            modifier = Modifier
                .size(size)
                .shadow(4.dp, outerShape, clip = false)
                .clip(outerShape)
                .border(2.dp, Color.White, outerShape)
                .background(colors.primarySoft)
                .then(clickMod),
            contentAlignment = Alignment.Center
        ) {
            MNotesPhotoContent(
                imgUri = imgUri,
                useBannerDefaultIfBlank = useBannerDefaultIfBlank,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
