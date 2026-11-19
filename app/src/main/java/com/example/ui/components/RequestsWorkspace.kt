package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.ChecklistOrderItem
import com.example.model.OrderTextSplitter
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

/**
 * Clean, completely blank customizable Orders / Checklist page.
 * - Free text input with strictly OPTIONAL text splitting (never auto-splits!).
 * - Interactive editable staging preview before saving (edit, add, reorder, delete).
 * - Customizable 3-state circular Checkmark (✓) / X (✗) / Neutral controls with 10 styles
 *   (including 3D Pixar glossy medallions), 6 shapes, sizes, colors & spacing.
 * - Preserves any previously saved legacy invoices/cheques in an optional collapsed drawer
 *   at the bottom so zero existing user data is ever lost.
 */
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

    val checklistOrders by viewModel.checklistOrders.collectAsStateWithLifecycle()
    val checklistStyle by viewModel.checklistStyle.collectAsStateWithLifecycle()

    // Free text composer state — default mode is WHOLE (never auto-splits!)
    var rawInputText by remember { mutableStateOf("") }
    var splitMode by remember { mutableStateOf(OrderTextSplitter.MODE_WHOLE) }
    var customWordsCount by remember { mutableIntStateOf(3) }
    var customDelimiter by remember { mutableStateOf("-") }

    // Editable staging list before saving
    var stagingItems by remember { mutableStateOf<List<String>>(emptyList()) }

    // Style customizer sheet & edit dialog states
    var showStyleStudio by remember { mutableStateOf(false) }
    var editingOrderItem by remember { mutableStateOf<ChecklistOrderItem?>(null) }
    var editOrderDraftText by remember { mutableStateOf("") }
    var confirmClearAll by remember { mutableStateOf(false) }
    var showLegacyDrawer by remember { mutableStateOf(false) }

    val doneCount = remember(checklistOrders) {
        checklistOrders.count { it.status == ChecklistOrderItem.STATUS_CHECK }
    }
    val rejectedCount = remember(checklistOrders) {
        checklistOrders.count { it.status == ChecklistOrderItem.STATUS_X }
    }
    val checkColor = remember(checklistStyle.checkColorHex) {
        parseChecklistHexColor(checklistStyle.checkColorHex, Color(0xFF10B981))
    }
    val xColor = remember(checklistStyle.xColorHex) {
        parseChecklistHexColor(checklistStyle.xColorHex, Color(0xFFEF4444))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Minimal Clean Header + Checkmark/X Customizer Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (lang == "en") "Orders" else "صفحة الطلبات",
                    color = colors.textDark,
                    fontSize = (17f * textScale).sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = if (lang == "en") {
                        "Blank customizable list • optional split • ✓ / ✗ marks"
                    } else {
                        "صفحة فارغة مرنة • تقسيم اختياري • علامات صح (✓) وخطأ (✗)"
                    },
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
                        showStyleStudio = true
                    }
                    .testTag("open_checklist_style_btn")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Palette,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (lang == "en") "Style ✓ / ✗" else "شكل ✓ / ✗",
                        color = colors.primary,
                        fontSize = (11.5f * textScale).sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        // Clean Blank-Page Text Composer with Optional Splitting
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = colors.cardBg.copy(alpha = 0.96f),
            shadowElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, colors.borderSoft, RoundedCornerShape(22.dp))
        ) {
            Column(
                modifier = Modifier.padding((14f * cardScale).dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = rawInputText,
                    onValueChange = { rawInputText = it },
                    placeholder = {
                        Text(
                            text = if (lang == "en") {
                                "Write anything freely... (stays intact unless you choose a split option)"
                            } else {
                                "اكتب طلبك أو ملاحظتك بحرية... (تُضاف كما هي ما لم تختر التقسيم)"
                            },
                            color = colors.textMuted,
                            fontSize = 12.5.sp
                        )
                    },
                    minLines = 2,
                    maxLines = 6,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.borderSoft,
                        focusedContainerColor = colors.bgMain,
                        unfocusedContainerColor = colors.bgMain
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("orders_free_text_input")
                )

                // Strictly Optional Split Controls
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (lang == "en") "Optional Split Mode (only when you want):" else "تقسيم النص (اختياري تماماً حسب رغبتك):",
                        color = colors.textMuted,
                        fontSize = (11f * textScale).sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val splitChoices = listOf(
                            Triple(OrderTextSplitter.MODE_WHOLE, "عنصر واحد (بدون تقسيم)", "Keep as 1 Item"),
                            Triple(OrderTextSplitter.MODE_WORDS, "كل كلمة سطر", "1 Word Each"),
                            Triple(OrderTextSplitter.MODE_PAIRS, "كل كلمتين سطر", "2 Words Each"),
                            Triple(OrderTextSplitter.MODE_COMMA_LINE, "بفاصلة أو سطر", "Comma / Line"),
                            Triple(OrderTextSplitter.MODE_CUSTOM_N, "كل N كلمات", "Custom N Words"),
                            Triple(OrderTextSplitter.MODE_CUSTOM_DELIM, "فاصل مخصص", "Custom Separator")
                        )
                        splitChoices.forEach { (id, arLabel, enLabel) ->
                            val selected = splitMode == id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(if (selected) colors.primary else colors.bgMain)
                                    .border(
                                        1.dp,
                                        if (selected) colors.primary else colors.borderSoft,
                                        RoundedCornerShape(999.dp)
                                    )
                                    .clickable {
                                        viewModel.playSound("nav")
                                        splitMode = id
                                    }
                                    .padding(horizontal = 11.dp, vertical = 7.dp)
                                    .testTag("split_mode_$id")
                            ) {
                                Text(
                                    text = if (lang == "en") enLabel else arLabel,
                                    color = if (selected) Color.White else colors.textDark,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Inline custom options ONLY when chosen
                    if (splitMode == OrderTextSplitter.MODE_CUSTOM_N) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.bgMain)
                                .border(1.dp, colors.borderSoft, RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (lang == "en") "Words per item:" else "عدد الكلمات في كل عنصر:",
                                color = colors.textDark,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                SmallCircleStepButton(
                                    icon = Icons.Rounded.Remove,
                                    desc = "Decrease",
                                    onClick = { customWordsCount = (customWordsCount - 1).coerceAtLeast(1) }
                                )
                                Text(
                                    text = "$customWordsCount",
                                    color = colors.primary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                SmallCircleStepButton(
                                    icon = Icons.Rounded.Add,
                                    desc = "Increase",
                                    onClick = { customWordsCount = (customWordsCount + 1).coerceAtMost(8) }
                                )
                            }
                        }
                    } else if (splitMode == OrderTextSplitter.MODE_CUSTOM_DELIM) {
                        OutlinedTextField(
                            value = customDelimiter,
                            onValueChange = { customDelimiter = it.take(6) },
                            singleLine = true,
                            label = {
                                Text(
                                    text = if (lang == "en") "Custom separator (e.g. - or / or |)" else "رمز الفصل المخصص (مثل - أو / أو |)",
                                    fontSize = 11.sp
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Direct Add OR Review & Edit Staging List
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val pieces = OrderTextSplitter.split(
                                rawText = rawInputText,
                                mode = splitMode,
                                wordsPerGroup = customWordsCount,
                                customDelimiter = customDelimiter
                            )
                            if (pieces.isNotEmpty()) {
                                viewModel.playSound("nav")
                                stagingItems = stagingItems + pieces
                                rawInputText = ""
                            }
                        },
                        enabled = rawInputText.trim().isNotEmpty(),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("orders_prepare_split_btn")
                    ) {
                        Text(
                            text = if (lang == "en") "Preview & Edit" else "معاينة وتعديل",
                            color = colors.primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Button(
                        onClick = {
                            val pieces = OrderTextSplitter.split(
                                rawText = rawInputText,
                                mode = splitMode,
                                wordsPerGroup = customWordsCount,
                                customDelimiter = customDelimiter
                            )
                            if (pieces.isNotEmpty()) {
                                viewModel.addChecklistOrderItems(pieces)
                                rawInputText = ""
                            }
                        },
                        enabled = rawInputText.trim().isNotEmpty(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                        modifier = Modifier
                            .weight(1.25f)
                            .height(48.dp)
                            .testTag("orders_quick_add_btn")
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (splitMode == OrderTextSplitter.MODE_WHOLE) {
                                if (lang == "en") "Add Item" else "إضافة للقائمة"
                            } else {
                                if (lang == "en") "Split & Add" else "تقسيم وإضافة"
                            },
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        // Interactive Editable Staging List (review, edit, add, reorder, delete BEFORE saving)
        if (stagingItems.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = colors.cardBg.copy(alpha = 0.98f),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, colors.primary.copy(alpha = 0.45f), RoundedCornerShape(22.dp))
                    .testTag("orders_staging_card")
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (lang == "en") {
                                "Edit Items Before Saving (${stagingItems.size})"
                            } else {
                                "تعديل وترتيب العناصر قبل الحفظ (${stagingItems.size})"
                            },
                            color = colors.primary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        TextButton(onClick = { stagingItems = stagingItems + "" }) {
                            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(3.dp))
                            Text(if (lang == "en") "Row" else "سطر", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    stagingItems.forEachIndexed { idx, draftText ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            OutlinedTextField(
                                value = draftText,
                                onValueChange = { updated ->
                                    stagingItems = stagingItems.toMutableList().also { it[idx] = updated }
                                },
                                singleLine = true,
                                placeholder = {
                                    Text(if (lang == "en") "Item ${idx + 1}" else "عنصر ${idx + 1}", fontSize = 11.5.sp)
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                SmallMiniIconAction(
                                    icon = Icons.Rounded.ArrowUpward,
                                    desc = "Move up",
                                    tint = if (idx > 0) colors.textDark else colors.textMuted.copy(alpha = 0.35f),
                                    onClick = {
                                        if (idx > 0) {
                                            stagingItems = stagingItems.toMutableList().also { list ->
                                                val item = list.removeAt(idx)
                                                list.add(idx - 1, item)
                                            }
                                        }
                                    }
                                )
                                SmallMiniIconAction(
                                    icon = Icons.Rounded.ArrowDownward,
                                    desc = "Move down",
                                    tint = if (idx < stagingItems.lastIndex) colors.textDark else colors.textMuted.copy(alpha = 0.35f),
                                    onClick = {
                                        if (idx < stagingItems.lastIndex) {
                                            stagingItems = stagingItems.toMutableList().also { list ->
                                                val item = list.removeAt(idx)
                                                list.add(idx + 1, item)
                                            }
                                        }
                                    }
                                )
                            }
                            SmallMiniIconAction(
                                icon = Icons.Rounded.DeleteOutline,
                                desc = "Delete draft",
                                tint = Color(0xFFEF4444),
                                onClick = {
                                    stagingItems = stagingItems.toMutableList().also { it.removeAt(idx) }
                                }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { stagingItems = emptyList() },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(0.8f)
                                .height(46.dp)
                        ) {
                            Text(if (lang == "en") "Cancel" else "إلغاء", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                val cleaned = stagingItems.map { it.trim() }.filter { it.isNotEmpty() }
                                if (cleaned.isNotEmpty()) {
                                    viewModel.addChecklistOrderItems(cleaned)
                                }
                                stagingItems = emptyList()
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                            modifier = Modifier
                                .weight(1.4f)
                                .height(46.dp)
                                .testTag("orders_save_drafts_btn")
                        ) {
                            Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(5.dp))
                            Text(
                                text = if (lang == "en") "Save to Orders" else "حفظ في صفحة الطلبات",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }

        // Saved Orders Checklist (Clean, customizable Checkmark / X per item)
        if (checklistOrders.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = colors.cardBg.copy(alpha = 0.85f),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.borderSoft, RoundedCornerShape(22.dp))
                    .testTag("orders_blank_empty_state")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp, horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ChecklistMarkControl(
                            status = ChecklistOrderItem.STATUS_CHECK,
                            style = checklistStyle.copy(circleSizeDp = 34),
                            onClick = { showStyleStudio = true },
                            contentDescription = "Checkmark preview"
                        )
                        ChecklistMarkControl(
                            status = ChecklistOrderItem.STATUS_X,
                            style = checklistStyle.copy(circleSizeDp = 34),
                            onClick = { showStyleStudio = true },
                            contentDescription = "X mark preview"
                        )
                    }
                    Text(
                        text = if (lang == "en") "Your Orders Page is Clean & Blank" else "صفحة الطلبات فارغة ونظيفة وجاهزة لك",
                        color = colors.textDark,
                        fontSize = (14f * textScale).sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = if (lang == "en") {
                            "Write any text above to add items freely, split only when you choose, and mark each row ✓ or ✗."
                        } else {
                            "اكتب أي نص بالأعلى لإضافته مباشرة أو تقسيمه اختيارياً، وضع علامة صح (✓) أو خطأ (✗) أمام كل عنصر."
                        },
                        color = colors.textMuted,
                        fontSize = (11.5f * textScale).sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Compact Counter Strip + Clear All
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatMiniPill(
                        text = if (lang == "en") "All ${checklistOrders.size}" else "الكل ${checklistOrders.size}",
                        bg = colors.cardBg,
                        fg = colors.textDark
                    )
                    StatMiniPill(
                        text = "✓ $doneCount",
                        bg = checkColor.copy(alpha = 0.14f),
                        fg = checkColor
                    )
                    StatMiniPill(
                        text = "✗ $rejectedCount",
                        bg = xColor.copy(alpha = 0.14f),
                        fg = xColor
                    )
                }
                TextButton(
                    onClick = { confirmClearAll = true },
                    modifier = Modifier.testTag("orders_clear_all_btn")
                ) {
                    Text(
                        text = if (lang == "en") "Clear All" else "مسح الكل",
                        color = Color(0xFFEF4444),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(checklistStyle.itemSpacingDp.coerceIn(4, 22).dp)
            ) {
                checklistOrders.forEachIndexed { index, item ->
                    val rowBg = when (item.status) {
                        ChecklistOrderItem.STATUS_CHECK -> checkColor.copy(alpha = 0.08f)
                        ChecklistOrderItem.STATUS_X -> xColor.copy(alpha = 0.08f)
                        else -> colors.cardBg.copy(alpha = 0.96f)
                    }
                    val rowBorder = when (item.status) {
                        ChecklistOrderItem.STATUS_CHECK -> checkColor.copy(alpha = 0.35f)
                        ChecklistOrderItem.STATUS_X -> xColor.copy(alpha = 0.35f)
                        else -> colors.borderSoft
                    }

                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = rowBg,
                        shadowElevation = 1.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, rowBorder, RoundedCornerShape(18.dp))
                            .testTag("checklist_order_row_${item.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Primary 3-state customizable circular Check/X control
                            ChecklistMarkControl(
                                status = item.status,
                                style = checklistStyle,
                                onClick = { viewModel.cycleChecklistOrderStatus(item.id) },
                                contentDescription = if (lang == "en") "Toggle mark for ${item.text}" else "تبديل العلامة لـ ${item.text}",
                                modifier = Modifier.testTag("checklist_mark_${item.id}")
                            )

                            // Item text (tappable to edit)
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        editingOrderItem = item
                                        editOrderDraftText = item.text
                                    }
                                    .padding(vertical = 4.dp, horizontal = 4.dp)
                            ) {
                                Text(
                                    text = item.text,
                                    color = if (item.status == ChecklistOrderItem.STATUS_NONE) colors.textDark else colors.textDark.copy(alpha = 0.85f),
                                    fontSize = (13.5f * textScale).sp,
                                    fontWeight = FontWeight.Bold,
                                    textDecoration = if (item.status == ChecklistOrderItem.STATUS_X) {
                                        TextDecoration.LineThrough
                                    } else {
                                        TextDecoration.None
                                    }
                                )
                            }

                            // Quick direct ✓ & ✗ buttons + Reorder + Edit + Delete
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                DirectStatusChip(
                                    isActive = item.status == ChecklistOrderItem.STATUS_CHECK,
                                    activeColor = checkColor,
                                    icon = Icons.Rounded.Check,
                                    desc = "Check",
                                    onClick = {
                                        val next = if (item.status == ChecklistOrderItem.STATUS_CHECK) {
                                            ChecklistOrderItem.STATUS_NONE
                                        } else {
                                            ChecklistOrderItem.STATUS_CHECK
                                        }
                                        viewModel.setChecklistOrderStatus(item.id, next)
                                    }
                                )
                                DirectStatusChip(
                                    isActive = item.status == ChecklistOrderItem.STATUS_X,
                                    activeColor = xColor,
                                    icon = Icons.Rounded.Close,
                                    desc = "X",
                                    onClick = {
                                        val next = if (item.status == ChecklistOrderItem.STATUS_X) {
                                            ChecklistOrderItem.STATUS_NONE
                                        } else {
                                            ChecklistOrderItem.STATUS_X
                                        }
                                        viewModel.setChecklistOrderStatus(item.id, next)
                                    }
                                )
                                if (checklistOrders.size > 1) {
                                    SmallMiniIconAction(
                                        icon = Icons.Rounded.ArrowUpward,
                                        desc = "Up",
                                        tint = if (index > 0) colors.textMuted else colors.textMuted.copy(alpha = 0.25f),
                                        onClick = { if (index > 0) viewModel.moveChecklistOrderItem(item.id, -1) }
                                    )
                                    SmallMiniIconAction(
                                        icon = Icons.Rounded.ArrowDownward,
                                        desc = "Down",
                                        tint = if (index < checklistOrders.lastIndex) colors.textMuted else colors.textMuted.copy(alpha = 0.25f),
                                        onClick = { if (index < checklistOrders.lastIndex) viewModel.moveChecklistOrderItem(item.id, 1) }
                                    )
                                }
                                SmallMiniIconAction(
                                    icon = Icons.Rounded.Edit,
                                    desc = "Edit",
                                    tint = colors.primary,
                                    onClick = {
                                        editingOrderItem = item
                                        editOrderDraftText = item.text
                                    }
                                )
                                SmallMiniIconAction(
                                    icon = Icons.Rounded.DeleteOutline,
                                    desc = "Delete",
                                    tint = Color(0xFFEF4444),
                                    onClick = { viewModel.deleteChecklistOrderItem(item.id) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Preserve any previously saved legacy documents cleanly in an optional collapsed drawer
        if (allRequests.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = colors.cardBg.copy(alpha = 0.9f),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.borderSoft, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { showLegacyDrawer = !showLegacyDrawer }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (lang == "en") {
                                "Previous Saved Documents (${allRequests.size})"
                            } else {
                                "الفواتير والشيكات المحفوظة سابقاً (${allRequests.size})"
                            },
                            color = colors.textMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(
                                imageVector = Icons.Rounded.Tune,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable { onOpenRequestsSettings() }
                            )
                            Icon(
                                imageVector = if (showLegacyDrawer) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                contentDescription = null,
                                tint = colors.textMuted
                            )
                        }
                    }

                    if (showLegacyDrawer) {
                        allRequests.forEach { req ->
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
                                            viewModel.showMessage(
                                                if (lang == "en") "Image saved to Pictures/MNotes"
                                                else "تم حفظ الصورة في معرض الصور"
                                            )
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
                                            val title = if (lang == "en") "Send Document as Image" else "إرسال كصورة"
                                            val prepared = DocumentImageExporter.prepareShareIntent(context, bmp, req.id, title)
                                            bmp.recycle()
                                            prepared
                                        }
                                        res.onSuccess { chooser ->
                                            runCatching { context.startActivity(chooser) }
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

        Spacer(Modifier.height(24.dp))
    }

    // Checkmark & X Style Customizer Sheet
    if (showStyleStudio) {
        ChecklistStyleStudioSheet(
            currentStyle = checklistStyle,
            lang = lang,
            onSave = { updatedStyle -> viewModel.saveChecklistStyle(updatedStyle) },
            onDismiss = { showStyleStudio = false }
        )
    }

    // Inline Edit Saved Item Dialog
    val activeEdit = editingOrderItem
    if (activeEdit != null) {
        AlertDialog(
            onDismissRequest = { editingOrderItem = null },
            title = {
                Text(
                    text = if (lang == "en") "Edit Order Item" else "تعديل نص العنصر",
                    fontWeight = FontWeight.ExtraBold
                )
            },
            text = {
                OutlinedTextField(
                    value = editOrderDraftText,
                    onValueChange = { editOrderDraftText = it },
                    singleLine = false,
                    maxLines = 4,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateChecklistOrderText(activeEdit.id, editOrderDraftText)
                        editingOrderItem = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                ) {
                    Text(if (lang == "en") "Save" else "حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingOrderItem = null }) {
                    Text(if (lang == "en") "Cancel" else "إلغاء")
                }
            }
        )
    }

    // Clear All Confirmation Dialog
    if (confirmClearAll) {
        AlertDialog(
            onDismissRequest = { confirmClearAll = false },
            title = {
                Text(
                    text = if (lang == "en") "Clear All Orders?" else "مسح كل عناصر القائمة؟",
                    fontWeight = FontWeight.ExtraBold
                )
            },
            text = {
                Text(
                    text = if (lang == "en") {
                        "This will clear the checklist page back to blank."
                    } else {
                        "سيتم تفريغ صفحة الطلبات لتعود فارغة بالكامل."
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllChecklistOrders()
                        confirmClearAll = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text(if (lang == "en") "Clear" else "مسح الكل")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmClearAll = false }) {
                    Text(if (lang == "en") "Cancel" else "إلغاء")
                }
            }
        )
    }
}

@Composable
private fun StatMiniPill(text: String, bg: Color, fg: Color) {
    val colors = LocalMNotesColors.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .border(1.dp, colors.borderSoft, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text = text, color = fg, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun DirectStatusChip(
    isActive: Boolean,
    activeColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    desc: String,
    onClick: () -> Unit
) {
    val colors = LocalMNotesColors.current
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(if (isActive) activeColor else colors.bgMain)
            .border(1.dp, if (isActive) activeColor else colors.borderSoft, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = desc,
            tint = if (isActive) Color.White else activeColor,
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
private fun SmallCircleStepButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    desc: String,
    onClick: () -> Unit
) {
    val colors = LocalMNotesColors.current
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(colors.cardBg)
            .border(1.dp, colors.borderSoft, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = desc, tint = colors.textDark, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun SmallMiniIconAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    desc: String,
    tint: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = desc, tint = tint, modifier = Modifier.size(15.dp))
    }
}
