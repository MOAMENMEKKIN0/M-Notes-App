package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Draw
import androidx.compose.material.icons.rounded.PlaylistAddCheck
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.AppStrings
import com.example.model.QuickRequest
import com.example.model.UserPreferences
import com.example.ui.MNotesViewModel
import com.example.ui.UiState
import com.example.ui.theme.LocalMNotesCardScale
import com.example.ui.theme.LocalMNotesColors
import com.example.ui.theme.LocalMNotesTextScale
import com.example.util.DocumentImageExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RequestsWorkspace(
    prefs: UserPreferences,
    allRequests: List<QuickRequest>,
    requestsState: UiState<List<QuickRequest>>,
    viewModel: MNotesViewModel,
    onOpenRequestsSettings: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val colors = LocalMNotesColors.current
    val textScale = LocalMNotesTextScale.current
    val cardScale = LocalMNotesCardScale.current
    val lang = prefs.lang

    // Top Document Composer State (Invoice / Cheque / Card)
    var docType by remember(prefs.reqDefaultDocType) { mutableStateOf(prefs.reqDefaultDocType) }
    var splitMode by remember(prefs.reqDefaultSplitMode) { mutableStateOf(prefs.reqDefaultSplitMode) }
    var customerName by remember { mutableStateOf("") }
    var docTitle by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var docBody by remember { mutableStateOf("") }
    var selectedStamp by remember(prefs.reqDefaultStampId) { mutableStateOf(prefs.reqDefaultStampId) }
    var signNow by remember(prefs.reqAutoSignOnComplete) { mutableStateOf(false) }

    // Bottom Orders Checklist Composer State
    val availableStatuses = remember(prefs.reqCustomStatuses) {
        prefs.reqCustomStatuses.split(',', '،')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .ifEmpty { listOf("متاح", "غير متاح", "وصل", "لم يصل", "جاهز") }
    }
    var orderText by remember { mutableStateOf("") }
    var orderSplitMode by remember(prefs.reqDefaultSplitMode) { mutableStateOf(prefs.reqDefaultSplitMode) }
    var orderStatusLabel by remember(prefs.reqDefaultStatusLabel) {
        mutableStateOf(prefs.reqDefaultStatusLabel.ifBlank { availableStatuses.first() })
    }
    var orderInitialDot by remember { mutableStateOf("none") }
    var splitEachLineAsSeparateOrder by remember { mutableStateOf(false) }

    val documentRequests = remember(allRequests) { allRequests.filter { !it.isChecklistOrder } }
    val checklistOrders = remember(allRequests) { allRequests.filter { it.isChecklistOrder } }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header strip with direct jump to Requests Settings Folder (with Live Card Preview)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (lang == "en") "Orders, Invoices & Cheques" else "الطلبات والفواتير والشيكات",
                    color = colors.textDark,
                    fontSize = (15.5f * textScale).sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = if (lang == "en") "Round stamps, signature & image export" else "فواتير، شيكات، أختام مدورة وإرسال كصورة",
                    color = colors.textMuted,
                    fontSize = (11f * textScale).sp
                )
            }
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = colors.primarySoft,
                modifier = Modifier
                    .border(1.dp, colors.primary.copy(alpha = 0.35f), RoundedCornerShape(999.dp))
                    .clip(RoundedCornerShape(999.dp))
                    .clickable {
                        viewModel.playSound("enter")
                        onOpenRequestsSettings()
                    }
                    .testTag("open_requests_settings_btn")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(Icons.Rounded.Tune, contentDescription = null, tint = colors.primary, modifier = Modifier.size(15.dp))
                    Text(
                        text = if (lang == "en") "Preview & Setup" else "معاينة وضبط",
                        color = colors.primary,
                        fontSize = (11f * textScale).sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        // Document Composer Card (Invoice / Cheque / Card)
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = colors.cardBg,
            shadowElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, colors.borderSoft, RoundedCornerShape(22.dp))
        ) {
            Column(
                modifier = Modifier.padding((14f * cardScale).dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (prefs.reqShowDocTabs) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            Triple("invoice", if (lang == "en") "Invoice" else "فاتورة", Icons.Rounded.ReceiptLong),
                            Triple("cheque", if (lang == "en") "Cheque" else "شيك", Icons.Rounded.Description),
                            Triple("card", if (lang == "en") "Card" else "كارت", Icons.Rounded.CreditCard)
                        ).forEach { (id, label, icon) ->
                            val selected = docType == id
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (selected) colors.primary else colors.bgMain)
                                    .border(1.dp, if (selected) colors.primary else colors.borderSoft, RoundedCornerShape(14.dp))
                                    .clickable {
                                        viewModel.playSound("nav")
                                        docType = id
                                    }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (selected) Color.White else colors.textMuted,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    text = label,
                                    color = if (selected) Color.White else colors.textDark,
                                    fontSize = (12f * textScale).sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }

                if (prefs.reqShowCustomer || prefs.reqShowAmount) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (prefs.reqShowCustomer) {
                            OutlinedTextField(
                                value = customerName,
                                onValueChange = { customerName = it },
                                placeholder = {
                                    Text(if (lang == "en") "Customer name" else "اسم صاحب الطلب", color = colors.textMuted, fontSize = 12.sp)
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colors.primary,
                                    unfocusedBorderColor = colors.borderSoft,
                                    focusedContainerColor = colors.bgMain,
                                    unfocusedContainerColor = colors.bgMain
                                ),
                                modifier = Modifier.weight(1.3f)
                            )
                        }

                        if (prefs.reqShowAmount) {
                            OutlinedTextField(
                                value = amountText,
                                onValueChange = { amountText = it },
                                placeholder = {
                                    Text(if (lang == "en") "Amount" else "القيمة / السعر", color = colors.textMuted, fontSize = 12.sp)
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colors.primary,
                                    unfocusedBorderColor = colors.borderSoft,
                                    focusedContainerColor = colors.bgMain,
                                    unfocusedContainerColor = colors.bgMain
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = docTitle,
                    onValueChange = { docTitle = it },
                    placeholder = {
                        Text(AppStrings.get(lang, "reqTitlePh"), color = colors.textMuted, fontSize = 13.sp)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.borderSoft,
                        focusedContainerColor = colors.bgMain,
                        unfocusedContainerColor = colors.bgMain
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("request_title_input")
                )

                if (prefs.reqShowSplitControls) {
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(
                            text = if (lang == "en") "Auto Text Line Splitting:" else "تقسيم الكلام تلقائياً في السطور:",
                            color = colors.textMuted,
                            fontSize = (11f * textScale).sp,
                            fontWeight = FontWeight.Bold
                        )
                        SplitModeRow(
                            selectedMode = splitMode,
                            lang = lang,
                            onSelect = {
                                viewModel.playSound("nav")
                                splitMode = it
                            }
                        )
                    }
                }

                OutlinedTextField(
                    value = docBody,
                    onValueChange = { docBody = it },
                    placeholder = {
                        Text(
                            if (lang == "en") "Type continuously — auto splits into lines..." else "اكتب الطلبات ورا بعضها (هتتقسم تلقائياً لسطور حسب اختيارك)...",
                            color = colors.textMuted,
                            fontSize = 12.5.sp
                        )
                    },
                    minLines = 3,
                    maxLines = 6,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.borderSoft,
                        focusedContainerColor = colors.bgMain,
                        unfocusedContainerColor = colors.bgMain
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("request_body_input")
                )

                // Live preview of split lines when typing
                val previewLines = remember(docBody, splitMode) { splitRequestTextLines(docBody, splitMode) }
                if (previewLines.isNotEmpty() && splitMode != "full") {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.bgMain,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, colors.borderSoft, RoundedCornerShape(12.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = if (lang == "en") "Preview (${previewLines.size} lines):" else "معاينة التقسيم (${previewLines.size} سطور):",
                                color = colors.primary,
                                fontSize = (10.5f * textScale).sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            previewLines.take(8).forEachIndexed { idx, line ->
                                Text(
                                    text = "${idx + 1}. $line",
                                    color = colors.textDark,
                                    fontSize = (11.5f * textScale).sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            if (previewLines.size > 8) {
                                Text(
                                    text = "+${previewLines.size - 8} ...",
                                    color = colors.textMuted,
                                    fontSize = 10.5.sp
                                )
                            }
                        }
                    }
                }

                if (prefs.reqShowStamps) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = if (lang == "en") "Choose Round Ink Stamp:" else "اختر الختم المدور على الشيك / الفاتورة:",
                            color = colors.textMuted,
                            fontSize = (11f * textScale).sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            requestStamps.forEach { st ->
                                val active = selectedStamp == st.id
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (active) st.bg else colors.bgMain)
                                        .border(
                                            width = if (active) 1.8.dp else 1.dp,
                                            color = if (active) st.tint else colors.borderSoft,
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .clickable {
                                            viewModel.playSound("nav")
                                            selectedStamp = st.id
                                        }
                                        .padding(horizontal = 10.dp, vertical = 7.dp)
                                ) {
                                    RubberStampSeal(
                                        stamp = st,
                                        lang = lang,
                                        size = 54.dp
                                    )
                                    Text(
                                        text = if (lang == "en") st.labelEn else st.labelAr,
                                        color = if (active) st.tint else colors.textDark,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }
                }

                if (prefs.reqShowSignature) {
                    val sigDisplay = prefs.reqSignatureName.ifBlank {
                        if (lang == "en") "Set in Settings" else "اكتب اسمك من الضبط"
                    }
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (signNow) colors.primarySoft else colors.bgMain,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, if (signNow) colors.primary else colors.borderSoft, RoundedCornerShape(14.dp))
                            .clickable {
                                viewModel.playSound("nav")
                                signNow = !signNow
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Rounded.Draw, contentDescription = null, tint = colors.primary, modifier = Modifier.size(18.dp))
                                Column {
                                    Text(
                                        text = if (lang == "en") "Include Signature" else "إضافة التوقيع الرسمي على الشيك/الفاتورة",
                                        color = colors.textDark,
                                        fontSize = (11.5f * textScale).sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = sigDisplay,
                                        color = colors.primary,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Serif,
                                        fontStyle = FontStyle.Italic,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Switch(
                                checked = signNow,
                                onCheckedChange = { signNow = it },
                                colors = SwitchDefaults.colors(
                                    checkedTrackColor = colors.primary,
                                    checkedThumbColor = Color.White
                                )
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        if (docTitle.trim().isNotEmpty() || docBody.trim().isNotEmpty() || customerName.trim().isNotEmpty()) {
                            viewModel.saveQuickRequest(
                                title = docTitle,
                                body = docBody,
                                docType = docType,
                                customerName = customerName,
                                amountText = amountText,
                                stampId = selectedStamp,
                                isSigned = signNow,
                                signatureName = prefs.reqSignatureName,
                                splitMode = splitMode,
                                isChecklistOrder = false
                            )
                            docTitle = ""
                            docBody = ""
                            customerName = ""
                            amountText = ""
                            selectedStamp = prefs.reqDefaultStampId
                            signNow = false
                        }
                    },
                    enabled = docTitle.trim().isNotEmpty() || docBody.trim().isNotEmpty() || customerName.trim().isNotEmpty(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_request_button")
                ) {
                    Text(
                        text = when (docType) {
                            "cheque" -> if (lang == "en") "Save Cheque" else "حفظ وإصدار الشيك"
                            "card" -> if (lang == "en") "Save Order Card" else "حفظ وإصدار الكارت"
                            else -> if (lang == "en") "Save Invoice" else "حفظ وإصدار الفاتورة"
                        },
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = (13.5f * textScale).sp
                    )
                }
            }
        }

        // Saved Invoices / Cheques / Cards Display
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = colors.cardBg,
            shadowElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, colors.borderSoft, RoundedCornerShape(22.dp))
        ) {
            Column(
                modifier = Modifier.padding((14f * cardScale).dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (lang == "en") "Issued Invoices, Cheques & Cards" else "الفواتير والشيكات والكروت المحفوظة",
                        color = colors.textDark,
                        fontSize = (14f * textScale).sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(colors.primarySoft)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${documentRequests.size}",
                            color = colors.primary,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                when (val rState = requestsState) {
                    is UiState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = colors.primary)
                        }
                    }
                    is UiState.Error -> {
                        Text(text = rState.message, color = Color(0xFFEF4444), fontSize = 12.5.sp)
                    }
                    is UiState.Success -> {
                        if (documentRequests.isEmpty()) {
                            Text(
                                text = AppStrings.get(lang, "noReq"),
                                color = colors.textMuted,
                                fontSize = (12f * textScale).sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 14.dp)
                            )
                        } else {
                            documentRequests.forEach { req ->
                                IssuedDocumentCard(
                                    req = req,
                                    prefs = prefs,
                                    lang = lang,
                                    isPreview = false,
                                    onToggleDot = { dot -> viewModel.toggleRequestDot(req.id, dot) },
                                    onPickStamp = { st -> viewModel.updateRequestStamp(req.id, st) },
                                    onToggleSignature = { viewModel.toggleRequestSignature(req.id) },
                                    onDownloadImage = {
                                        viewModel.playSound("enter")
                                        scope.launch {
                                            val result = withContext(Dispatchers.IO) {
                                                val bmp = DocumentImageExporter.renderDocumentBitmap(req, prefs, colors)
                                                val saved = DocumentImageExporter.saveDocumentToGallery(context, bmp, req.id)
                                                bmp.recycle()
                                                saved
                                            }
                                            if (result.isSuccess) {
                                                viewModel.playSound("done")
                                                val msg = if (lang == "en") {
                                                    "Image saved to Pictures/MNotes"
                                                } else {
                                                    "تم حفظ صورة الكارت/الشيك في معرض الصور بنجاح"
                                                }
                                                viewModel.showMessage(msg)
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    result.exceptionOrNull()?.localizedMessage ?: "Export error",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    },
                                    onShareImage = {
                                        viewModel.playSound("enter")
                                        scope.launch {
                                            val res = withContext(Dispatchers.IO) {
                                                val bmp = DocumentImageExporter.renderDocumentBitmap(req, prefs, colors)
                                                val title = if (lang == "en") "Send Document as Image" else "إرسال الشيك أو الفاتورة كصورة"
                                                val prepared = DocumentImageExporter.prepareShareIntent(context, bmp, req.id, title)
                                                bmp.recycle()
                                                prepared
                                            }
                                            res.onSuccess { chooser ->
                                                runCatching { context.startActivity(chooser) }
                                                    .onFailure { err ->
                                                        viewModel.showMessage(err.localizedMessage ?: "Failed to share image")
                                                    }
                                            }.onFailure { err ->
                                                viewModel.showMessage(err.localizedMessage ?: "Failed to share image")
                                            }
                                        }
                                    },
                                    onDelete = { viewModel.deleteQuickRequest(req.id) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bottom Section: Requested Orders Quick Tracker with Red/Green Dots + Selectable Status
        if (prefs.reqShowOrdersSection) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = colors.cardBg,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.borderSoft, RoundedCornerShape(22.dp))
            ) {
                Column(
                    modifier = Modifier.padding((14f * cardScale).dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Rounded.PlaylistAddCheck, contentDescription = null, tint = colors.primary, modifier = Modifier.size(20.dp))
                            Text(
                                text = if (lang == "en") "Requested Orders Checklist" else "الأوردرات المطلوبة وحالة الوصول",
                                color = colors.textDark,
                                fontSize = (14.5f * textScale).sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(colors.primarySoft)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${checklistOrders.size}",
                                color = colors.primary,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    if (prefs.reqShowSplitControls) {
                        SplitModeRow(
                            selectedMode = orderSplitMode,
                            lang = lang,
                            onSelect = {
                                viewModel.playSound("nav")
                                orderSplitMode = it
                            }
                        )
                    }

                    OutlinedTextField(
                        value = orderText,
                        onValueChange = { orderText = it },
                        placeholder = {
                            Text(
                                if (lang == "en") "Write order items here..." else "اكتب الأوردر المطلوب هنا (أو عدة أوردرات ورا بعضها)...",
                                color = colors.textMuted,
                                fontSize = 12.5.sp
                            )
                        },
                        minLines = 2,
                        maxLines = 5,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.borderSoft,
                            focusedContainerColor = colors.bgMain,
                            unfocusedContainerColor = colors.bgMain
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("order_checklist_input")
                    )

                    if (orderSplitMode != "full") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.bgMain)
                                .border(1.dp, colors.borderSoft, RoundedCornerShape(12.dp))
                                .clickable { splitEachLineAsSeparateOrder = !splitEachLineAsSeparateOrder }
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (lang == "en") "Save each split line as its own order" else "انزل كل سطر كطلب مستقل بالدوائر بتاعته",
                                color = colors.textDark,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = splitEachLineAsSeparateOrder,
                                onCheckedChange = { splitEachLineAsSeparateOrder = it },
                                colors = SwitchDefaults.colors(
                                    checkedTrackColor = colors.primary,
                                    checkedThumbColor = Color.White
                                )
                            )
                        }
                    }

                    if (prefs.reqShowStatusBadge) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            availableStatuses.forEach { stLabel ->
                                val selected = orderStatusLabel == stLabel
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(if (selected) colors.primary else colors.bgMain)
                                        .border(1.dp, if (selected) colors.primary else colors.borderSoft, RoundedCornerShape(999.dp))
                                        .clickable {
                                            viewModel.playSound("nav")
                                            orderStatusLabel = stLabel
                                        }
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = stLabel,
                                        color = if (selected) Color.White else colors.textDark,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (prefs.reqShowRedGreenDots) {
                            RedGreenDotsPair(
                                activeDot = orderInitialDot,
                                onToggle = { dot ->
                                    viewModel.playSound("nav")
                                    orderInitialDot = if (orderInitialDot == dot) "none" else dot
                                }
                            )
                        }

                        Button(
                            onClick = {
                                val cleaned = orderText.trim()
                                if (cleaned.isNotEmpty()) {
                                    val splitLines = splitRequestTextLines(cleaned, orderSplitMode)
                                    if (splitEachLineAsSeparateOrder && splitLines.size > 1) {
                                        splitLines.forEach { line ->
                                            viewModel.saveQuickRequest(
                                                title = line,
                                                body = "",
                                                statusDot = orderInitialDot,
                                                statusLabel = orderStatusLabel,
                                                splitMode = orderSplitMode,
                                                isChecklistOrder = true
                                            )
                                        }
                                    } else {
                                        val joined = splitLines.joinToString("\n").ifEmpty { cleaned }
                                        viewModel.saveQuickRequest(
                                            title = splitLines.firstOrNull().orEmpty().take(50),
                                            body = joined,
                                            statusDot = orderInitialDot,
                                            statusLabel = orderStatusLabel,
                                            splitMode = orderSplitMode,
                                            isChecklistOrder = true
                                        )
                                    }
                                    orderText = ""
                                    orderInitialDot = "none"
                                }
                            },
                            enabled = orderText.trim().isNotEmpty(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("add_checklist_order_btn")
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = if (lang == "en") "Add Order" else "إضافة للأوردرات",
                                fontSize = (12.5f * textScale).sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    if (checklistOrders.isNotEmpty()) {
                        HorizontalDivider(color = colors.borderSoft)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            checklistOrders.forEach { item ->
                                ChecklistOrderRow(
                                    item = item,
                                    prefs = prefs,
                                    availableStatuses = availableStatuses,
                                    lang = lang,
                                    onToggleDot = { dot -> viewModel.toggleRequestDot(item.id, dot) },
                                    onPickStatus = { lbl -> viewModel.updateRequestStatusLabel(item.id, lbl) },
                                    onDelete = { viewModel.deleteQuickRequest(item.id) }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChecklistOrderRow(
    item: QuickRequest,
    prefs: UserPreferences,
    availableStatuses: List<String>,
    lang: String,
    onToggleDot: (String) -> Unit,
    onPickStatus: (String) -> Unit,
    onDelete: () -> Unit
) {
    val colors = LocalMNotesColors.current
    val textScale = LocalMNotesTextScale.current
    var showStatusMenu by remember { mutableStateOf(false) }

    val rowTint = when (item.statusDot) {
        "green" -> Color(0xFFECFDF5)
        "red" -> Color(0xFFFEF2F2)
        else -> colors.bgMain
    }
    val rowBorder = when (item.statusDot) {
        "green" -> Color(0xFF10B981)
        "red" -> Color(0xFFEF4444)
        else -> colors.borderSoft
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = rowTint,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, rowBorder, RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (prefs.reqShowRedGreenDots) {
                    RedGreenDotsPair(
                        activeDot = item.statusDot,
                        onToggle = onToggleDot
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    val displayBody = item.body.trim()
                    Text(
                        text = if (displayBody.isNotBlank()) displayBody else item.title,
                        color = colors.textDark,
                        fontSize = (12.5f * textScale).sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (prefs.reqShowStatusBadge) {
                    val badgeText = item.statusLabel.ifBlank {
                        if (lang == "en") "Status" else "الحالة"
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(colors.primarySoft)
                            .border(1.dp, colors.primary.copy(alpha = 0.4f), RoundedCornerShape(999.dp))
                            .clickable { showStatusMenu = !showStatusMenu }
                            .padding(horizontal = 9.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = badgeText,
                            color = colors.primary,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onDelete),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                }
            }

            if (showStatusMenu && prefs.reqShowStatusBadge) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    availableStatuses.forEach { stLabel ->
                        val active = item.statusLabel == stLabel
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(if (active) colors.primary else colors.cardBg)
                                .border(1.dp, if (active) colors.primary else colors.borderSoft, RoundedCornerShape(999.dp))
                                .clickable {
                                    onPickStatus(stLabel)
                                    showStatusMenu = false
                                }
                                .padding(horizontal = 9.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = stLabel,
                                color = if (active) Color.White else colors.textDark,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SplitModeRow(
    selectedMode: String,
    lang: String,
    onSelect: (String) -> Unit
) {
    val colors = LocalMNotesColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        listOf(
            "word_1" to (if (lang == "en") "1 Word/Line" else "كلمة كلمة"),
            "word_2" to (if (lang == "en") "2 Words/Line" else "كلمتين كلمتين"),
            "full" to (if (lang == "en") "Full Text" else "الجملة كاملة")
        ).forEach { (id, label) ->
            val selected = selectedMode == id
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (selected) colors.primary else colors.bgMain)
                    .border(1.dp, if (selected) colors.primary else colors.borderSoft, RoundedCornerShape(999.dp))
                    .clickable { onSelect(id) }
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (selected) Color.White else colors.textMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}
