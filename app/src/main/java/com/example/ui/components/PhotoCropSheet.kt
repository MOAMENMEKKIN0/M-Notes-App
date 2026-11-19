package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.RotateRight
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.AppStrings
import com.example.model.LibraryPhoto
import com.example.model.MNotesCatalog
import com.example.ui.theme.LocalMNotesColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoPickerSheet(
    title: String,
    lang: String,
    libraryPhotos: List<LibraryPhoto>,
    currentImgUri: String = "",
    onPickFromDevice: (Uri) -> Unit,
    onSelectFromLibrary: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalMNotesColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val safeDismisser = rememberSafeSheetDismisser(sheetState, onDismiss)
    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            safeDismisser.dismissWith { onPickFromDevice(uri) }
        }
    }

    ModalBottomSheet(
        onDismissRequest = { safeDismisser.onSystemDismiss() },
        sheetState = sheetState,
        containerColor = colors.bgMain,
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = colors.textDark,
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Button(
                    onClick = { safeDismisser.dismiss() },
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                ) {
                    Text(AppStrings.get(lang, "close"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = colors.cardBg,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.borderSoft, RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.primarySoft)
                            .border(1.5.dp, colors.primary, RoundedCornerShape(14.dp))
                            .clickable {
                                photoLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            .padding(vertical = 12.dp)
                            .testTag("pick_photo_from_device"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.AddPhotoAlternate,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "+ ${AppStrings.get(lang, "pickPhoto")}",
                                color = colors.primary,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = if (lang == "en") {
                            "Exercise & Photo Library"
                        } else {
                            "مكتبة صور التمارين والمجلدات"
                        },
                        color = colors.primary,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(Modifier.height(8.dp))

                    CategorizedBuiltInPhotosBrowser(
                        lang = lang,
                        selectedUri = currentImgUri,
                        columns = 3,
                        defaultCategory = "chest",
                        showLabels = true,
                        onSelectUri = { uri ->
                            onSelectFromLibrary(uri)
                            safeDismisser.dismiss()
                        }
                    )

                    val enabledPhotos = libraryPhotos.filter { it.enabled }
                    if (enabledPhotos.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = if (lang == "en") "My Uploaded Photos" else "مجلد صوري المرفوعة",
                            color = colors.textMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(Modifier.height(8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            enabledPhotos.chunked(4).forEach { rowItems ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    rowItems.forEach { item ->
                                        val isCur = currentImgUri == item.filePath
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                                .clip(CircleShape)
                                                .border(
                                                    if (isCur) 2.5.dp else 1.5.dp,
                                                    if (isCur) colors.primary else colors.borderSoft,
                                                    CircleShape
                                                )
                                                .clickable {
                                                    onSelectFromLibrary(item.filePath)
                                                    safeDismisser.dismiss()
                                                }
                                                .padding(2.dp)
                                        ) {
                                            MNotesPhotoContent(
                                                imgUri = item.filePath,
                                                useBannerDefaultIfBlank = false,
                                                contentDescription = item.name,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(CircleShape)
                                            )
                                        }
                                    }
                                    repeat(4 - rowItems.size) {
                                        Spacer(Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoCropSheet(
    sourceUri: Uri,
    lang: String,
    onCropConfirmed: suspend (offsetXNorm: Float, offsetYNorm: Float, scale: Float, rotationDeg: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalMNotesColors.current
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val safeDismisser = rememberSafeSheetDismisser(sheetState, onDismiss)

    var previewBitmap by remember(sourceUri) { mutableStateOf<Bitmap?>(null) }
    var scale by remember { mutableFloatStateOf(1f) }
    var rotationDeg by remember { mutableIntStateOf(0) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var stagePx by remember { mutableFloatStateOf(240f) }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(sourceUri) {
        previewBitmap = withContext(Dispatchers.IO) {
            runCatching {
                context.contentResolver.openInputStream(sourceUri)?.use { stream ->
                    val opts = BitmapFactory.Options().apply { inSampleSize = 2 }
                    BitmapFactory.decodeStream(stream, null, opts)
                }
            }.getOrNull()
        }
    }

    ModalBottomSheet(
        onDismissRequest = { safeDismisser.onSystemDismiss() },
        sheetState = sheetState,
        containerColor = colors.bgMain,
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = AppStrings.get(lang, "cropTitle"),
                    color = colors.textDark,
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                OutlinedButton(
                    onClick = { safeDismisser.dismiss() },
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(AppStrings.get(lang, "cancel"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = colors.cardBg,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.borderSoft, RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = AppStrings.get(lang, "cropHint"),
                        color = colors.textMuted,
                        fontSize = 11.5.sp
                    )
                    Spacer(Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .size(230.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0F172A))
                            .border(3.dp, colors.primary, CircleShape)
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    offsetX += dragAmount.x
                                    offsetY += dragAmount.y
                                }
                            }
                    ) {
                        val bmp = previewBitmap
                        if (bmp != null) {
                            val imageBmp = remember(bmp) { bmp.asImageBitmap() }
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                stagePx = size.width
                                val baseScale = maxOf(
                                    size.width / imageBmp.width.toFloat(),
                                    size.height / imageBmp.height.toFloat()
                                )
                                val totalScale = baseScale * scale
                                val drawW = imageBmp.width * totalScale
                                val drawH = imageBmp.height * totalScale
                                withTransform({
                                    translate(
                                        left = size.width / 2f + offsetX,
                                        top = size.height / 2f + offsetY
                                    )
                                    rotate(degrees = rotationDeg.toFloat(), pivot = androidx.compose.ui.geometry.Offset.Zero)
                                }) {
                                    drawImage(
                                        image = imageBmp,
                                        dstOffset = IntOffset((-drawW / 2f).roundToInt(), (-drawH / 2f).roundToInt()),
                                        dstSize = IntSize(drawW.roundToInt(), drawH.roundToInt())
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = AppStrings.get(lang, "zoom"),
                            color = colors.textDark,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedButton(
                            onClick = { rotationDeg = (rotationDeg + 90) % 360 },
                            shape = RoundedCornerShape(999.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.RotateRight, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(AppStrings.get(lang, "rot"), fontSize = 11.5.sp)
                        }
                    }

                    Slider(
                        value = scale,
                        onValueChange = { scale = it },
                        valueRange = 1f..3f,
                        colors = SliderDefaults.colors(
                            thumbColor = colors.primary,
                            activeTrackColor = colors.primary
                        )
                    )

                    Spacer(Modifier.height(6.dp))

                    Button(
                        onClick = {
                            if (isSaving) return@Button
                            isSaving = true
                            val normX = if (stagePx > 0f) offsetX / stagePx else 0f
                            val normY = if (stagePx > 0f) offsetY / stagePx else 0f
                            safeDismisser.dismissWithSuspend(
                                action = { onCropConfirmed(normX, normY, scale, rotationDeg) },
                                onFinally = { isSaving = false }
                            )
                        },
                        enabled = !isSaving,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("crop_confirm_button")
                    ) {
                        Text(
                            text = AppStrings.get(lang, "cropConfirm"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
