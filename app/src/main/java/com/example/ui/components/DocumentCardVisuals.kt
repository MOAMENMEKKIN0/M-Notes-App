package com.example.ui.components

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.AppStrings
import com.example.model.QuickRequest
import com.example.model.UserPreferences
import com.example.ui.theme.LocalMNotesColors
import com.example.ui.theme.LocalMNotesTextScale

data class StampMeta(
    val id: String,
    val labelAr: String,
    val labelEn: String,
    val line1Ar: String,
    val line2Ar: String,
    val tint: Color,
    val bg: Color
)

val requestStamps = listOf(
    StampMeta("none", "بدون ختم", "None", "بدون", "ختم", Color(0xFF64748B), Color(0xFFF1F5F9)),
    StampMeta("paid_thanks", "خالص مع الشكر", "Paid Thanks", "خالص", "مع الشكر", Color(0xFFC62828), Color(0xFFFEE2E2)),
    StampMeta("done", "تـم", "Done", "تـم", "✓", Color(0xFFD32F2F), Color(0xFFFEE2E2)),
    StampMeta("paid_thanks_green", "خالص مع الشكر (أخضر)", "Paid Green", "خالص", "مع الشكر", Color(0xFF15803D), Color(0xFFDCFCE7)),
    StampMeta("paid_full", "مدفوع بالكامل", "Paid Full", "مدفوع", "بالكامل", Color(0xFFB91C1C), Color(0xFFFEE2E2)),
    StampMeta("received", "تم الاستلام", "Received", "تم", "الاستلام", Color(0xFF1D4ED8), Color(0xFFDBEAFE)),
    StampMeta("unpaid", "غير مكتمل", "Incomplete", "غير", "مكتمل", Color(0xFF991B1B), Color(0xFFFEE2E2))
)

fun resolveStampMeta(stampId: String): StampMeta {
    val mappedId = when (stampId) {
        "arrived" -> "received"
        "complete" -> "paid_thanks"
        "ready" -> "done"
        "incomplete" -> "unpaid"
        else -> stampId
    }
    return requestStamps.firstOrNull { it.id == mappedId } ?: requestStamps.first()
}

fun splitRequestTextLines(raw: String, splitMode: String): List<String> {
    val cleaned = raw.trim()
    if (cleaned.isEmpty()) return emptyList()
    return when (splitMode) {
        "word_1" -> cleaned.split(Regex("\\s+")).filter { it.isNotBlank() }
        "word_2" -> cleaned.split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .chunked(2)
            .map { it.joinToString(" ") }
        else -> cleaned.lines().map { it.trim() }.filter { it.isNotBlank() }
    }
}

@Composable
fun RubberStampSeal(
    stamp: StampMeta,
    lang: String,
    size: Dp = 84.dp,
    onClick: (() -> Unit)? = null
) {
    val ink = stamp.tint
    Box(
        modifier = Modifier
            .size(size)
            .rotate(-14f)
            .clip(CircleShape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val outerRadius = this.size.minDimension / 2f - 3.dp.toPx()
            val innerRadius = outerRadius * 0.78f
            val tinyRadius = outerRadius * 0.68f
            drawCircle(
                color = ink.copy(alpha = 0.08f),
                radius = outerRadius
            )
            drawCircle(
                color = ink.copy(alpha = 0.90f),
                radius = outerRadius,
                style = Stroke(width = 3.2.dp.toPx())
            )
            drawCircle(
                color = ink.copy(alpha = 0.78f),
                radius = innerRadius,
                style = Stroke(width = 1.4.dp.toPx())
            )
            drawCircle(
                color = ink.copy(alpha = 0.50f),
                radius = tinyRadius,
                style = Stroke(
                    width = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
                )
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(6.dp)
        ) {
            Text(
                text = "★ ★ ★",
                color = ink.copy(alpha = 0.85f),
                fontSize = (size.value * 0.085f).sp,
                lineHeight = (size.value * 0.095f).sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (lang == "en") stamp.labelEn else stamp.line1Ar,
                color = ink,
                fontSize = (if (stamp.id == "done") size.value * 0.22f else size.value * 0.165f).sp,
                lineHeight = (if (stamp.id == "done") size.value * 0.24f else size.value * 0.18f).sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            if (lang != "en" && stamp.line2Ar.isNotBlank()) {
                Text(
                    text = stamp.line2Ar,
                    color = ink,
                    fontSize = (if (stamp.id == "done") size.value * 0.16f else size.value * 0.128f).sp,
                    lineHeight = (size.value * 0.145f).sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
            Text(
                text = "★ ★ ★",
                color = ink.copy(alpha = 0.85f),
                fontSize = (size.value * 0.085f).sp,
                lineHeight = (size.value * 0.095f).sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun IssuedDocumentCard(
    req: QuickRequest,
    prefs: UserPreferences,
    lang: String,
    isPreview: Boolean = false,
    onToggleDot: (String) -> Unit = {},
    onPickStamp: (String) -> Unit = {},
    onToggleSignature: () -> Unit = {},
    onDownloadImage: () -> Unit = {},
    onShareImage: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val colors = LocalMNotesColors.current
    val textScale = LocalMNotesTextScale.current
    val stamp = resolveStampMeta(req.stampId)
    val lines = remember(req.body, req.splitMode) { splitRequestTextLines(req.body, req.splitMode) }

    val cardBg = when (req.docType) {
        "cheque" -> Brush.linearGradient(listOf(Color(0xFFFAF6EE), Color(0xFFF3ECE0)))
        "card" -> Brush.linearGradient(listOf(colors.primarySoft, colors.cardBg))
        else -> Brush.linearGradient(listOf(Color(0xFFFFFFFF), colors.bgMain))
    }
    val borderColor = when (req.docType) {
        "cheque" -> Color(0xFFC4A47C)
        "card" -> colors.primary.copy(alpha = 0.55f)
        else -> colors.borderSoft
    }
    val docBadgeLabel = when (req.docType) {
        "cheque" -> if (lang == "en") "OFFICIAL CHEQUE" else "شيك رسمي"
        "card" -> if (lang == "en") "ORDER CARD" else "كارت أوردر"
        else -> if (lang == "en") "INVOICE" else "فاتورة طلب"
    }

    var expandStampPicker by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(cardBg)
            .border(if (req.docType == "cheque") 1.8.dp else 1.dp, borderColor, RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Top Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (prefs.reqShowHeaderBadge) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(colors.primary.copy(alpha = 0.14f))
                                .padding(horizontal = 9.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = docBadgeLabel,
                                color = colors.primary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                    if (prefs.reqShowAmount && req.amountText.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.88f))
                                .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = req.amountText,
                                color = colors.textDark,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                    if (prefs.reqShowDate) {
                        Text(
                            text = AppStrings.todayStr(),
                            color = colors.textMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (prefs.reqShowRedGreenDots) {
                        RedGreenDotsPair(
                            activeDot = req.statusDot,
                            onToggle = onToggleDot
                        )
                    }
                    if (!isPreview) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.85f))
                                .border(1.dp, Color(0xFFFEE2E2), CircleShape)
                                .clickable(onClick = onDelete),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Customer & Title
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                if (prefs.reqShowCustomer && req.customerName.isNotBlank()) {
                    Text(
                        text = if (req.docType == "cheque") "يُصرف / للسيد: ${req.customerName}" else "العميل: ${req.customerName}",
                        color = colors.primary,
                        fontSize = (11.5f * textScale).sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                if (req.title.isNotBlank()) {
                    Text(
                        text = req.title,
                        color = colors.textDark,
                        fontSize = (14f * textScale).sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // Receipt Dashed Divider or Cheque Line
            if (req.docType == "invoice" || req.docType == "cheque") {
                Canvas(modifier = Modifier.fillMaxWidth().height(2.dp)) {
                    drawLine(
                        color = borderColor,
                        start = Offset(0f, 1f),
                        end = Offset(size.width, 1f),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                    )
                }
            }

            // Body lines
            if (lines.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    lines.forEachIndexed { idx, line ->
                        Text(
                            text = if (req.splitMode != "full" && lines.size > 1) "${idx + 1}. $line" else line,
                            color = colors.textDark.copy(alpha = 0.88f),
                            fontSize = (12f * textScale).sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Bottom Big Round Stamp + Signature Line
            if (prefs.reqShowStamps || prefs.reqShowSignature) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (prefs.reqShowStamps) {
                        if (stamp.id != "none") {
                            RubberStampSeal(
                                stamp = stamp,
                                lang = lang,
                                size = 82.dp,
                                onClick = { expandStampPicker = !expandStampPicker }
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(stamp.bg)
                                    .border(1.4.dp, stamp.tint.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                    .clickable { expandStampPicker = !expandStampPicker }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Rounded.Verified, contentDescription = null, tint = stamp.tint, modifier = Modifier.size(14.dp))
                                    Text(
                                        text = if (lang == "en") "Add Stamp" else "إضافة ختم مدور",
                                        color = stamp.tint,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    } else {
                        Spacer(Modifier.width(1.dp))
                    }

                    if (prefs.reqShowSignature) {
                        val sigText = req.signatureName.ifBlank { prefs.reqSignatureName }
                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = onToggleSignature)
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            if (req.isSigned && sigText.isNotBlank()) {
                                Text(
                                    text = sigText,
                                    color = Color(0xFF1E3A8A),
                                    fontSize = (15f * textScale).sp,
                                    fontFamily = FontFamily.Serif,
                                    fontStyle = FontStyle.Italic,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(2.dp))
                                Box(
                                    modifier = Modifier
                                        .width(96.dp)
                                        .height(1.5.dp)
                                        .background(Color(0xFF1E3A8A).copy(alpha = 0.6f))
                                )
                            } else {
                                Text(
                                    text = if (lang == "en") "Tap to sign" else "اضغط للتوقيع",
                                    color = colors.textMuted,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            if (expandStampPicker && prefs.reqShowStamps) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    requestStamps.forEach { st ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(if (stamp.id == st.id) st.tint else st.bg)
                                .border(1.dp, st.tint.copy(alpha = 0.6f), RoundedCornerShape(999.dp))
                                .clickable {
                                    onPickStamp(st.id)
                                    expandStampPicker = false
                                }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = if (lang == "en") st.labelEn else st.labelAr,
                                color = if (stamp.id == st.id) Color.White else st.tint,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (!isPreview) {
                Spacer(Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.cardBg.copy(alpha = 0.92f))
                            .border(1.dp, colors.borderSoft, RoundedCornerShape(12.dp))
                            .clickable(onClick = onDownloadImage)
                            .padding(vertical = 8.dp)
                            .testTag("download_doc_image_${req.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            Icon(Icons.Rounded.Download, contentDescription = null, tint = colors.primary, modifier = Modifier.size(16.dp))
                            Text(
                                text = if (lang == "en") "Save Image" else "حفظ كصورة",
                                color = colors.textDark,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.primary)
                            .clickable(onClick = onShareImage)
                            .padding(vertical = 8.dp)
                            .testTag("share_doc_image_${req.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            Icon(Icons.Rounded.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                            Text(
                                text = if (lang == "en") "Send as Image" else "إرسال كصورة",
                                color = Color.White,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RedGreenDotsPair(
    activeDot: String,
    onToggle: (String) -> Unit
) {
    val greenSelected = activeDot == "green"
    val redSelected = activeDot == "red"
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (greenSelected) Color(0xFF16A34A) else Color(0xFFDCFCE7))
                .border(1.6.dp, Color(0xFF16A34A), CircleShape)
                .clickable { onToggle("green") }
                .testTag("green_status_dot"),
            contentAlignment = Alignment.Center
        ) {
            if (greenSelected) {
                Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }

        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (redSelected) Color(0xFFDC2626) else Color(0xFFFEE2E2))
                .border(1.6.dp, Color(0xFFDC2626), CircleShape)
                .clickable { onToggle("red") }
                .testTag("red_status_dot"),
            contentAlignment = Alignment.Center
        ) {
            if (redSelected) {
                Icon(Icons.Rounded.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
    }
}
