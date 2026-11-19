package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Refresh
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChecklistOrderItem
import com.example.model.ChecklistStyleCatalog
import com.example.model.ChecklistStyleConfig
import com.example.ui.theme.LocalMNotesColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

fun parseChecklistHexColor(hex: String, fallback: Color): Color {
    return runCatching {
        Color(android.graphics.Color.parseColor(hex))
    }.getOrDefault(fallback)
}

fun checklistControlShape(shapeId: String): Shape = when (shapeId) {
    "squircle" -> RoundedCornerShape(32)
    "rounded_square" -> RoundedCornerShape(20)
    "organic_blob" -> GenericShape { size, _ ->
        val w = size.width
        val h = size.height
        moveTo(w * 0.50f, h * 0.03f)
        cubicTo(w * 0.86f, h * 0.01f, w * 0.99f, h * 0.22f, w * 0.96f, h * 0.52f)
        cubicTo(w * 0.93f, h * 0.84f, w * 0.78f, h * 0.99f, w * 0.48f, h * 0.97f)
        cubicTo(w * 0.16f, h * 0.95f, h * 0.01f, h * 0.77f, w * 0.04f, h * 0.48f)
        cubicTo(w * 0.07f, h * 0.18f, w * 0.22f, h * 0.05f, w * 0.50f, h * 0.03f)
        close()
    }
    "soft_hex" -> GenericShape { size, _ ->
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = minOf(cx, cy) * 0.96f
        for (i in 0 until 6) {
            val angle = (PI / 3.0 * i) - (PI / 2.0)
            val x = cx + (r * cos(angle)).toFloat()
            val y = cy + (r * sin(angle)).toFloat()
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }
    "diamond" -> GenericShape { size, _ ->
        val w = size.width
        val h = size.height
        moveTo(w * 0.50f, h * 0.04f)
        lineTo(w * 0.96f, h * 0.50f)
        lineTo(w * 0.50f, h * 0.96f)
        lineTo(w * 0.04f, h * 0.50f)
        close()
    }
    else -> CircleShape
}

private fun pixarTileUriFor(styleId: String, status: String): String? {
    return when (styleId) {
        "pixar_3d" -> when (status) {
            ChecklistOrderItem.STATUS_CHECK -> "res://img_pixar_check_x_pack#0"
            ChecklistOrderItem.STATUS_X -> "res://img_pixar_check_x_pack#4"
            else -> "res://img_pixar_check_x_pack#8"
        }
        "pixar_mint" -> when (status) {
            ChecklistOrderItem.STATUS_CHECK -> "res://img_pixar_check_x_pack#1"
            ChecklistOrderItem.STATUS_X -> "res://img_pixar_check_x_pack#5"
            else -> "res://img_pixar_check_x_pack#8"
        }
        "pixar_gold" -> when (status) {
            ChecklistOrderItem.STATUS_CHECK -> "res://img_pixar_check_x_pack#2"
            ChecklistOrderItem.STATUS_X -> "res://img_pixar_check_x_pack#7"
            else -> "res://img_pixar_check_x_pack#8"
        }
        else -> null
    }
}

/**
 * Interactive 3-state (Neutral / Checkmark / X) circular or custom-shaped mark control
 * rendered in the user's chosen style (including 3D Pixar glossy medallions & 7 custom vector styles).
 * Always preserves a >= 48.dp accessible touch target regardless of visual circleSizeDp.
 */
@Composable
fun ChecklistMarkControl(
    status: String,
    style: ChecklistStyleConfig,
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    val visualSize = style.circleSizeDp.coerceIn(22, 52).dp
    val markSize = style.markSizeDp.coerceIn(10, 34).dp
    val shape = remember(style.shapeId) { checklistControlShape(style.shapeId) }

    val checkColor = remember(style.checkColorHex) {
        parseChecklistHexColor(style.checkColorHex, Color(0xFF10B981))
    }
    val xColor = remember(style.xColorHex) {
        parseChecklistHexColor(style.xColorHex, Color(0xFFEF4444))
    }
    val neutralColor = remember(style.neutralColorHex) {
        parseChecklistHexColor(style.neutralColorHex, Color(0xFFCBD5E1))
    }

    val activeColor by animateColorAsState(
        targetValue = when (status) {
            ChecklistOrderItem.STATUS_CHECK -> checkColor
            ChecklistOrderItem.STATUS_X -> xColor
            else -> neutralColor
        },
        label = "checklistMarkColor"
    )
    val popScale by animateFloatAsState(
        targetValue = if (status == ChecklistOrderItem.STATUS_NONE) 0.96f else 1.04f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "checklistMarkScale"
    )

    val pixarUri = remember(style.styleId, status) { pixarTileUriFor(style.styleId, status) }

    Box(
        modifier = modifier
            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            .semantics { this.contentDescription = contentDescription }
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (pixarUri != null) {
            // 3D Pixar Glossy Medallion rendered inside the user's chosen shape & size
            val glowElevation = if (status == ChecklistOrderItem.STATUS_NONE) 2.dp else 6.dp
            Box(
                modifier = Modifier
                    .size(visualSize)
                    .scale(popScale)
                    .shadow(glowElevation, shape, ambientColor = activeColor, spotColor = activeColor)
                    .clip(shape)
                    .background(Color.White)
                    .border(
                        width = if (status == ChecklistOrderItem.STATUS_NONE) 1.5.dp else 2.dp,
                        color = if (status == ChecklistOrderItem.STATUS_NONE) neutralColor else activeColor.copy(alpha = 0.7f),
                        shape = shape
                    ),
                contentAlignment = Alignment.Center
            ) {
                MNotesPhotoContent(
                    imgUri = pixarUri,
                    contentDescription = null,
                    useBannerDefaultIfBlank = false,
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            // Custom vector & canvas styles dynamically reflecting color, shape, circle size, and mark size
            val isNeutral = status == ChecklistOrderItem.STATUS_NONE
            val bgColor = when (style.styleId) {
                "bold_filled" -> if (isNeutral) Color.White else activeColor
                "minimal" -> if (isNeutral) Color.Transparent else activeColor.copy(alpha = 0.14f)
                "soft_pastel" -> if (isNeutral) neutralColor.copy(alpha = 0.25f) else activeColor.copy(alpha = 0.22f)
                "outlined_ring" -> Color.White
                "neon_glow" -> if (isNeutral) Color(0xFFF8FAFC) else activeColor.copy(alpha = 0.16f)
                "stamp_classic" -> if (isNeutral) Color.White else activeColor.copy(alpha = 0.10f)
                "sketch_soft" -> if (isNeutral) Color(0xFFFFFBF5) else activeColor.copy(alpha = 0.15f)
                else -> if (isNeutral) Color.White else activeColor
            }
            val borderWidth = when (style.styleId) {
                "outlined_ring", "stamp_classic" -> 2.4.dp
                "neon_glow" -> 2.2.dp
                "minimal" -> if (isNeutral) 1.5.dp else 1.8.dp
                else -> if (isNeutral) 1.8.dp else 2.dp
            }
            val borderColor = if (isNeutral) neutralColor else activeColor
            val markTint = when (style.styleId) {
                "bold_filled" -> Color.White
                else -> activeColor
            }
            val elevation = when (style.styleId) {
                "neon_glow" -> if (isNeutral) 1.dp else 8.dp
                "bold_filled" -> if (isNeutral) 1.dp else 4.dp
                else -> 0.dp
            }

            Box(
                modifier = Modifier
                    .size(visualSize)
                    .scale(popScale)
                    .then(
                        if (elevation > 0.dp) {
                            Modifier.shadow(elevation, shape, ambientColor = activeColor, spotColor = activeColor)
                        } else {
                            Modifier
                        }
                    )
                    .clip(shape)
                    .background(
                        if (style.styleId == "bold_filled" && !isNeutral) {
                            Brush.linearGradient(listOf(activeColor, activeColor.copy(alpha = 0.82f)))
                        } else {
                            Brush.linearGradient(listOf(bgColor, bgColor))
                        }
                    )
                    .border(borderWidth, borderColor, shape),
                contentAlignment = Alignment.Center
            ) {
                if (style.styleId == "stamp_classic" && !isNeutral) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(3.dp)
                            .border(1.dp, activeColor.copy(alpha = 0.65f), shape)
                    )
                }

                if (!isNeutral) {
                    Canvas(modifier = Modifier.size(markSize)) {
                        val w = size.width
                        val h = size.height
                        val strokePx = when (style.styleId) {
                            "minimal" -> (w * 0.13f).coerceAtLeast(3.5f)
                            "sketch_soft" -> (w * 0.15f).coerceAtLeast(4f)
                            else -> (w * 0.19f).coerceAtLeast(5f)
                        }
                        if (status == ChecklistOrderItem.STATUS_CHECK) {
                            val p = Path().apply {
                                moveTo(w * 0.16f, h * 0.54f)
                                lineTo(w * 0.41f, h * 0.78f)
                                lineTo(w * 0.85f, h * 0.24f)
                            }
                            drawPath(
                                path = p,
                                color = markTint,
                                style = Stroke(
                                    width = strokePx,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                            if (style.styleId == "sketch_soft") {
                                val p2 = Path().apply {
                                    moveTo(w * 0.19f, h * 0.58f)
                                    lineTo(w * 0.43f, h * 0.81f)
                                    lineTo(w * 0.86f, h * 0.28f)
                                }
                                drawPath(
                                    path = p2,
                                    color = markTint.copy(alpha = 0.45f),
                                    style = Stroke(width = strokePx * 0.65f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                                )
                            }
                        } else {
                            drawLine(
                                color = markTint,
                                start = Offset(w * 0.22f, h * 0.22f),
                                end = Offset(w * 0.78f, h * 0.78f),
                                strokeWidth = strokePx,
                                cap = StrokeCap.Round
                            )
                            drawLine(
                                color = markTint,
                                start = Offset(w * 0.78f, h * 0.22f),
                                end = Offset(w * 0.22f, h * 0.78f),
                                strokeWidth = strokePx,
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Interactive Studio Modal Sheet for customizing the Orders Checklist Checkmark & X appearance:
 * 10 styles (including 3D Pixar medallions), 6 shapes, circle size, inner mark size, spacing,
 * and custom Check / X / Neutral colors — persisted across restarts.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChecklistStyleStudioSheet(
    currentStyle: ChecklistStyleConfig,
    lang: String,
    onSave: (ChecklistStyleConfig) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalMNotesColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val safeDismisser = rememberSafeSheetDismisser(sheetState, onDismiss)

    var draft by remember(currentStyle) { mutableStateOf(currentStyle) }
    var previewSampleStatus by remember { mutableStateOf(ChecklistOrderItem.STATUS_CHECK) }

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
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (lang == "en") "Customize Checkmark & X Style" else "تخصيص شكل علامة الصح (✓) والخطأ (✗)",
                        color = colors.textDark,
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = if (lang == "en") "Tap preview marks to test all 3 states" else "اضغط على العلامات في المعاينة لتجربة الـ 3 حالات",
                        color = colors.textMuted,
                        fontSize = 11.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colors.cardBg)
                        .border(1.dp, colors.borderSoft, CircleShape)
                        .clickable { safeDismisser.dismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = null, tint = colors.textDark, modifier = Modifier.size(18.dp))
                }
            }

            // Live Interactive Preview Box
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = colors.cardBg,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.2.dp, colors.primary.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(draft.itemSpacingDp.coerceIn(4, 22).dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            ChecklistMarkControl(
                                status = ChecklistOrderItem.STATUS_NONE,
                                style = draft,
                                onClick = { previewSampleStatus = ChecklistOrderItem.STATUS_NONE },
                                contentDescription = "Neutral preview"
                            )
                            Text(
                                text = if (lang == "en") "Neutral" else "فارغ",
                                color = colors.textMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            ChecklistMarkControl(
                                status = ChecklistOrderItem.STATUS_CHECK,
                                style = draft,
                                onClick = { previewSampleStatus = ChecklistOrderItem.STATUS_CHECK },
                                contentDescription = "Check preview"
                            )
                            Text(
                                text = if (lang == "en") "Done ✓" else "تم ✓",
                                color = parseChecklistHexColor(draft.checkColorHex, Color(0xFF10B981)),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            ChecklistMarkControl(
                                status = ChecklistOrderItem.STATUS_X,
                                style = draft,
                                onClick = { previewSampleStatus = ChecklistOrderItem.STATUS_X },
                                contentDescription = "X preview"
                            )
                            Text(
                                text = if (lang == "en") "Not Done ✗" else "لم يتم ✗",
                                color = parseChecklistHexColor(draft.xColorHex, Color(0xFFEF4444)),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    // Interactive tap-to-cycle preview row
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = colors.bgMain,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, colors.borderSoft, RoundedCornerShape(14.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ChecklistMarkControl(
                                status = previewSampleStatus,
                                style = draft,
                                onClick = {
                                    previewSampleStatus = ChecklistOrderItem.nextCycledStatus(previewSampleStatus)
                                },
                                contentDescription = "Interactive test row"
                            )
                            Text(
                                text = if (lang == "en") "Tap circle to cycle: Neutral → ✓ → ✗" else "جرّب الضغط على الدائرة للتبديل (فارغ ← ✓ ← ✗)",
                                color = colors.textDark,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 1. Mark Design Style Picker (10 styles incl. 3D Pixar glossy)
            Text(
                text = if (lang == "en") "Mark Style (10 Designs incl. 3D Pixar):" else "شكل وتصميم العلامات (10 ستايلات منها 3D Pixar):",
                color = colors.textDark,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ChecklistStyleCatalog.styles.forEach { option ->
                    val selected = draft.styleId == option.id
                    val miniStyle = draft.copy(styleId = option.id, circleSizeDp = 28, markSizeDp = 15)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .width(102.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (selected) colors.primarySoft else colors.cardBg)
                            .border(
                                width = if (selected) 1.8.dp else 1.dp,
                                color = if (selected) colors.primary else colors.borderSoft,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { draft = draft.copy(styleId = option.id) }
                            .padding(horizontal = 6.dp, vertical = 8.dp)
                            .testTag("checklist_style_${option.id}")
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
                            ChecklistMarkControl(
                                status = ChecklistOrderItem.STATUS_CHECK,
                                style = miniStyle,
                                onClick = { draft = draft.copy(styleId = option.id) },
                                contentDescription = option.nameEn
                            )
                            ChecklistMarkControl(
                                status = ChecklistOrderItem.STATUS_X,
                                style = miniStyle,
                                onClick = { draft = draft.copy(styleId = option.id) },
                                contentDescription = option.nameEn
                            )
                        }
                        Text(
                            text = if (lang == "en") option.nameEn else option.nameAr,
                            color = if (selected) colors.primary else colors.textDark,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // 2. Shape Picker (6 shapes)
            Text(
                text = if (lang == "en") "Control Shape:" else "شكل الدائرة / الإطار:",
                color = colors.textDark,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ChecklistStyleCatalog.shapes.forEach { sh ->
                    val sel = draft.shapeId == sh.id
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (sel) colors.primary else colors.cardBg)
                            .border(1.dp, if (sel) colors.primary else colors.borderSoft, RoundedCornerShape(999.dp))
                            .clickable { draft = draft.copy(shapeId = sh.id) }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                            .testTag("checklist_shape_${sh.id}")
                    ) {
                        Text(
                            text = if (lang == "en") sh.nameEn else sh.nameAr,
                            color = if (sel) Color.White else colors.textDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 3. Size & Spacing Sliders
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = colors.cardBg,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.borderSoft, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StyleSliderRow(
                        label = if (lang == "en") "Circle / Badge Size" else "حجم الدائرة",
                        valueText = "${draft.circleSizeDp} dp",
                        value = draft.circleSizeDp.toFloat(),
                        range = 22f..52f,
                        onChange = { draft = draft.copy(circleSizeDp = it.roundToInt()) }
                    )
                    StyleSliderRow(
                        label = if (lang == "en") "Checkmark & X Thickness/Size" else "حجم علامة الصح والخطأ بالداخل",
                        valueText = "${draft.markSizeDp} dp",
                        value = draft.markSizeDp.toFloat(),
                        range = 10f..34f,
                        onChange = { draft = draft.copy(markSizeDp = it.roundToInt()) }
                    )
                    StyleSliderRow(
                        label = if (lang == "en") "Spacing Between Items" else "المسافة بين الطلبات",
                        valueText = "${draft.itemSpacingDp} dp",
                        value = draft.itemSpacingDp.toFloat(),
                        range = 4f..22f,
                        onChange = { draft = draft.copy(itemSpacingDp = it.roundToInt()) }
                    )
                }
            }

            // 4. Custom Color Swatches (Check, X, Neutral)
            ColorSwatchStrip(
                title = if (lang == "en") "Checkmark (✓) Color" else "لون علامة الصح (✓)",
                swatches = ChecklistStyleCatalog.checkColorSwatches,
                selectedHex = draft.checkColorHex,
                onSelect = { draft = draft.copy(checkColorHex = it) }
            )
            ColorSwatchStrip(
                title = if (lang == "en") "X Mark (✗) Color" else "لون علامة الخطأ (✗)",
                swatches = ChecklistStyleCatalog.xColorSwatches,
                selectedHex = draft.xColorHex,
                onSelect = { draft = draft.copy(xColorHex = it) }
            )
            ColorSwatchStrip(
                title = if (lang == "en") "Neutral / Unselected Border Color" else "لون الدائرة الفارغة (المحايدة)",
                swatches = ChecklistStyleCatalog.neutralColorSwatches,
                selectedHex = draft.neutralColorHex,
                onSelect = { draft = draft.copy(neutralColorHex = it) }
            )

            // Save & Reset Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { draft = ChecklistStyleConfig() },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(0.85f)
                        .height(48.dp)
                ) {
                    Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = if (lang == "en") "Default" else "افتراضي",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Button(
                    onClick = {
                        onSave(draft)
                        safeDismisser.dismiss()
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                    modifier = Modifier
                        .weight(1.4f)
                        .height(48.dp)
                        .testTag("checklist_style_save_btn")
                ) {
                    Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (lang == "en") "Save & Apply Style" else "حفظ وتطبيق الشكل",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun StyleSliderRow(
    label: String,
    valueText: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onChange: (Float) -> Unit
) {
    val colors = LocalMNotesColors.current
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, color = colors.textDark, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
            Text(text = valueText, color = colors.primary, fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold)
        }
        Slider(
            value = value.coerceIn(range.start, range.endInclusive),
            onValueChange = onChange,
            valueRange = range,
            colors = SliderDefaults.colors(thumbColor = colors.primary, activeTrackColor = colors.primary),
            modifier = Modifier.height(26.dp)
        )
    }
}

@Composable
private fun ColorSwatchStrip(
    title: String,
    swatches: List<String>,
    selectedHex: String,
    onSelect: (String) -> Unit
) {
    val colors = LocalMNotesColors.current
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = title, color = colors.textDark, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            swatches.forEach { hex ->
                val swatchColor = parseChecklistHexColor(hex, colors.primary)
                val selected = selectedHex.equals(hex, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(swatchColor)
                        .border(
                            width = if (selected) 3.dp else 1.dp,
                            color = if (selected) colors.textDark else Color.White,
                            shape = CircleShape
                        )
                        .clickable { onSelect(hex) },
                    contentAlignment = Alignment.Center
                ) {
                    if (selected) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
