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

@Composable
fun MNotesPhotoContent(
    imgUri: String,
    useBannerDefaultIfBlank: Boolean,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    when {
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
