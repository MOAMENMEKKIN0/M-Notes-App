package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.QuickRequest
import com.example.model.UserPreferences
import com.example.ui.MNotesViewModel
import com.example.ui.theme.LocalMNotesColors
import kotlinx.coroutines.delay

@Composable
fun RequestsSettingsFolderContent(
    prefs: UserPreferences,
    lang: String,
    viewModel: MNotesViewModel
) {
    val colors = LocalMNotesColors.current

    var signatureDraft by remember(prefs.reqSignatureName) { mutableStateOf(prefs.reqSignatureName) }
    var customStatusesDraft by remember(prefs.reqCustomStatuses) { mutableStateOf(prefs.reqCustomStatuses) }
    var defaultStatusDraft by remember(prefs.reqDefaultStatusLabel) { mutableStateOf(prefs.reqDefaultStatusLabel) }
    var previewSigned by remember { mutableStateOf(true) }
    var previewDot by remember { mutableStateOf("green") }

    LaunchedEffect(signatureDraft) {
        if (signatureDraft != prefs.reqSignatureName) {
            delay(400L)
            viewModel.updatePreference(mapOf("reqSignatureName" to signatureDraft))
        }
    }
    LaunchedEffect(customStatusesDraft) {
        if (customStatusesDraft != prefs.reqCustomStatuses) {
            delay(450L)
            viewModel.updatePreference(mapOf("reqCustomStatuses" to customStatusesDraft))
        }
    }
    LaunchedEffect(defaultStatusDraft) {
        if (defaultStatusDraft != prefs.reqDefaultStatusLabel) {
            delay(450L)
            viewModel.updatePreference(mapOf("reqDefaultStatusLabel" to defaultStatusDraft))
        }
    }

    val previewRequest = remember(
        prefs.reqDefaultDocType,
        prefs.reqDefaultSplitMode,
        prefs.reqDefaultStampId,
        signatureDraft,
        previewSigned,
        previewDot,
        lang
    ) {
        QuickRequest(
            id = "preview_doc",
            title = if (lang == "en") "Sample Order Summary" else "بيان طلبات وتوريد بضاعة",
            body = if (lang == "en") {
                "Olive oil Honey Bread Barley"
            } else {
                "أوردر رقم 104 تسليم فوري عسل نقي خبز شعير"
            },
            docType = prefs.reqDefaultDocType,
            customerName = if (lang == "en") "Ahmed Mahmoud" else "أحمد محمود",
            amountText = if (lang == "en") "1,250 EGP" else "1,250 ج.م",
            stampId = prefs.reqDefaultStampId,
            isSigned = previewSigned,
            signatureName = signatureDraft.ifBlank { if (lang == "en") "Momen" else "مؤمن" },
            statusDot = previewDot,
            splitMode = prefs.reqDefaultSplitMode,
            isChecklistOrder = false
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // LIVE PREVIEW CARD SECTION
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = colors.bgMain,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.2.dp, colors.primary.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (lang == "en") "Live Document Preview" else "معاينة حية لشكل الورقة / الكارت / الشيك",
                        color = colors.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = if (lang == "en") "Updates Live" else "يتحدث فوراً",
                        color = colors.textMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IssuedDocumentCard(
                    req = previewRequest,
                    prefs = prefs,
                    lang = lang,
                    isPreview = true,
                    onToggleDot = { dot ->
                        viewModel.playSound("nav")
                        previewDot = if (previewDot == dot) "none" else dot
                    },
                    onPickStamp = { st ->
                        viewModel.playSound("nav")
                        viewModel.updatePreference(mapOf("reqDefaultStampId" to st))
                    },
                    onToggleSignature = {
                        viewModel.playSound("nav")
                        previewSigned = !previewSigned
                    }
                )

                Text(
                    text = if (lang == "en") {
                        "Tap the round stamp or signature on the preview to test them"
                    } else {
                        "جرّب تشغيل وإيقاف العناصر بالأسفل وشاهد ما يظهر وما يُحذف مباشرة هنا"
                    },
                    color = colors.textMuted,
                    fontSize = 10.5.sp
                )
            }
        }

        Text(
            text = if (lang == "en") "Default Document Format" else "شكل المستند الافتراضي (فاتورة / شيك / كارت)",
            color = colors.textMuted,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                "invoice" to (if (lang == "en") "Invoice" else "فاتورة"),
                "cheque" to (if (lang == "en") "Cheque" else "شيك"),
                "card" to (if (lang == "en") "Card" else "كارت")
            ).forEach { (id, label) ->
                val active = prefs.reqDefaultDocType == id
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (active) colors.primary else colors.bgMain)
                        .border(1.dp, if (active) colors.primary else colors.borderSoft, RoundedCornerShape(999.dp))
                        .clickable {
                            viewModel.playSound("nav")
                            viewModel.updatePreference(mapOf("reqDefaultDocType" to id))
                        }
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (active) Color.White else colors.textDark,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Authentic Round Red/Colored Rubber Stamps Picker
        Text(
            text = if (lang == "en") "Default Round Stamp (Big Ink Seal)" else "الختم المدور الكبير الافتراضي (خالص مع الشكر / تـم)",
            color = colors.textMuted,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            requestStamps.forEach { st ->
                val active = prefs.reqDefaultStampId == st.id
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
                            viewModel.updatePreference(mapOf("reqDefaultStampId" to st.id))
                        }
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                ) {
                    RubberStampSeal(
                        stamp = st,
                        lang = lang,
                        size = 58.dp
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

        Text(
            text = if (lang == "en") "Signature & Stamps" else "الاسم الخاص بالتوقيع",
            color = colors.textMuted,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.ExtraBold
        )

        OutlinedTextField(
            value = signatureDraft,
            onValueChange = { signatureDraft = it },
            label = { Text(if (lang == "en") "Official Signature Name" else "الاسم الذي يظهر كتوقيع على الفواتير والشيكات", fontSize = 11.sp) },
            placeholder = { Text(if (lang == "en") "e.g. Momen / Store Name" else "مثال: مؤمن", fontSize = 12.sp) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.primary,
                unfocusedBorderColor = colors.borderSoft
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = if (lang == "en") "Default Text Split Mode" else "وضع تقسيم الكلام الافتراضي",
            color = colors.textMuted,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                "word_1" to (if (lang == "en") "1 Word" else "كلمة كلمة"),
                "word_2" to (if (lang == "en") "2 Words" else "كلمتين كلمتين"),
                "full" to (if (lang == "en") "Full Sentence" else "جملة كاملة")
            ).forEach { (id, label) ->
                val active = prefs.reqDefaultSplitMode == id
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (active) colors.primary else colors.bgMain)
                        .border(1.dp, if (active) colors.primary else colors.borderSoft, RoundedCornerShape(999.dp))
                        .clickable {
                            viewModel.playSound("nav")
                            viewModel.updatePreference(mapOf("reqDefaultSplitMode" to id))
                        }
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (active) Color.White else colors.textDark,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Text(
            text = if (lang == "en") "What to Show or Remove on the Card" else "إظهار وإخفاء عناصر الورقة / الكارت (ما يُحط وما يُشال)",
            color = colors.textMuted,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.ExtraBold
        )

        val switches = listOf(
            Triple("reqShowHeaderBadge", if (lang == "en") "Show Document Badge (Invoice/Cheque/Card)" else "إظهار شارة نوع الورقة (فاتورة / شيك رسمي / كارت)", prefs.reqShowHeaderBadge),
            Triple("reqShowCustomer", if (lang == "en") "Show Customer Name Field" else "إظهار خانة اسم العميل (يُصرف للسيد)", prefs.reqShowCustomer),
            Triple("reqShowAmount", if (lang == "en") "Show Amount / Price Box" else "إظهار خانة المبلغ / القيمة", prefs.reqShowAmount),
            Triple("reqShowDate", if (lang == "en") "Show Date on Card" else "إظهار التاريخ على الشيك / الكارت", prefs.reqShowDate),
            Triple("reqShowStamps", if (lang == "en") "Show Big Round Ink Stamps" else "إظهار الأختام المدورة الكبيرة (خالص مع الشكر / تـم)", prefs.reqShowStamps),
            Triple("reqShowSignature", if (lang == "en") "Show Signature on Invoices & Cheques" else "إظهار التوقيع باسمك على الشيك والفاتورة", prefs.reqShowSignature),
            Triple("reqAutoSignOnComplete", if (lang == "en") "Auto-Sign When Stamped" else "توقيع تلقائي بمجرد اختيار ختم الاكتمال", prefs.reqAutoSignOnComplete),
            Triple("reqShowDocTabs", if (lang == "en") "Show Invoice / Cheque / Card Switcher" else "إظهار أزرار اختيار (فاتورة / شيك / كارت)", prefs.reqShowDocTabs),
            Triple("reqShowSplitControls", if (lang == "en") "Show Text Split Controls" else "إظهار أزرار تقسيم الكلام (كلمة / كلمتين / جملة)", prefs.reqShowSplitControls),
            Triple("reqShowRedGreenDots", if (lang == "en") "Show Green & Red Status Dots" else "إظهار الدائرتين الخضراء والحمراء على الكارت والطلبات", prefs.reqShowRedGreenDots),
            Triple("reqShowOrdersSection", if (lang == "en") "Show Bottom Orders Checklist" else "إظهار قسم الأوردرات المطلوبة بالأسفل", prefs.reqShowOrdersSection),
            Triple("reqShowStatusBadge", if (lang == "en") "Show Available/Arrived Status Tag" else "إظهار شارة الحالة النصية (متاح / وصل / جاهز...)", prefs.reqShowStatusBadge)
        )

        switches.forEach { (key, title, enabled) ->
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (enabled) colors.primarySoft else colors.bgMain,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, if (enabled) colors.primary else colors.borderSoft, RoundedCornerShape(14.dp))
                    .clickable {
                        viewModel.playSound("nav")
                        viewModel.updatePreference(mapOf(key to !enabled))
                    }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = title,
                        color = colors.textDark,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = enabled,
                        onCheckedChange = {
                            viewModel.playSound("nav")
                            viewModel.updatePreference(mapOf(key to it))
                        },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = colors.primary,
                            checkedThumbColor = Color.White
                        )
                    )
                }
            }
        }

        Text(
            text = if (lang == "en") "Custom Order Status Labels (comma separated)" else "تخصيص حالات الأوردرات (افصل بفاصلة بين كل حالة)",
            color = colors.textMuted,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.ExtraBold
        )

        OutlinedTextField(
            value = customStatusesDraft,
            onValueChange = { customStatusesDraft = it },
            singleLine = false,
            minLines = 2,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.primary,
                unfocusedBorderColor = colors.borderSoft
            ),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = defaultStatusDraft,
            onValueChange = { defaultStatusDraft = it },
            label = { Text(if (lang == "en") "Default New Order Status" else "الحالة الافتراضية للأوردر الجديد", fontSize = 11.sp) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.primary,
                unfocusedBorderColor = colors.borderSoft
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
