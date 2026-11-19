package com.example.ui.components

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ScreenDesignConfig
import com.example.model.ScreenDesignPresets
import com.example.ui.theme.LocalMNotesColors
import kotlin.math.roundToInt

private val customColorSwatches = listOf(
    "#FFFFFF", "#FFFDF9", "#F5FBF8", "#EBF6F1", "#EEF7FF", "#E4F2FE",
    "#ECFEFE", "#FDF8F0", "#F6F3FF", "#FFF4EE", "#F2F7F3", "#18C28B",
    "#10B981", "#0EA5E9", "#06B6D4", "#6366F1", "#8B5CF6", "#EC4899",
    "#E67E22", "#F97316", "#F43F5E", "#2E8B57", "#14B8A6", "#334155"
)

@Composable
internal fun StudioLayoutAndOrderTab(
    config: ScreenDesignConfig,
    lang: String,
    onChange: (ScreenDesignConfig) -> Unit
) {
    val colors = LocalMNotesColors.current
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        StudioGroupHeader(if (lang == "en") "Overall Layout Structure" else "هيكل وتخطيط الشاشة")
        StudioChipSelectorRow(
            options = listOf(
                "timeline_list" to if (lang == "en") "Timeline List (A)" else "قائمة تركيز (A)",
                "gallery_2col" to if (lang == "en") "2-Col Gallery (B)" else "شبكة عمودين (B)",
                "spotlight_agenda" to if (lang == "en") "Spotlight Agenda (C)" else "مهمة قادمة بارزة (C)",
                "bento_grouped" to if (lang == "en") "Bento Grouped (D)" else "بينتو ومجموعات (D)",
                "compact_list" to if (lang == "en") "Compact List" else "قائمة مدمجة"
            ),
            selectedId = config.layoutMode,
            onSelect = { mode ->
                val adjusted = when (mode) {
                    "gallery_2col" -> config.copy(layoutMode = mode, taskArrangement = "centered_card")
                    "spotlight_agenda" -> config.copy(layoutMode = mode, showSpotlightUpNext = true, taskArrangement = "row_start")
                    "bento_grouped" -> config.copy(layoutMode = mode, showRestingGroupSeparate = true, showProgressWidget = true)
                    "compact_list" -> config.copy(layoutMode = mode, taskArrangement = "compact_inline")
                    else -> config.copy(layoutMode = mode, taskArrangement = "row_start")
                }
                onChange(adjusted)
            }
        )

        StudioGroupHeader(if (lang == "en") "Task Card Internal Arrangement" else "ترتيب العناصر داخل بطاقة المهمة")
        StudioChipSelectorRow(
            options = listOf(
                "row_start" to if (lang == "en") "Image Start" else "الصورة بالبداية",
                "centered_card" to if (lang == "en") "Centered Portrait" else "صورة بالمنتصف",
                "row_end" to if (lang == "en") "Image End" else "الصورة بالنهاية",
                "compact_inline" to if (lang == "en") "Compact Row" else "صف مدمج"
            ),
            selectedId = config.taskArrangement,
            onSelect = { onChange(config.copy(taskArrangement = it)) }
        )

        StudioGroupHeader(if (lang == "en") "Move & Rearrange Screen Sections" else "تحريك وإعادة ترتيب أقسام الشاشة")
        val order = config.sectionOrder.ifEmpty { ScreenDesignPresets.defaultSectionOrder }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            order.forEachIndexed { idx, sectionId ->
                val label = when (sectionId) {
                    "greeting" -> if (lang == "en") "Greeting & Header" else "التحية والعنوان العلوي"
                    "week_strip" -> if (lang == "en") "Week Days Strip" else "شريط أيام الأسبوع"
                    "progress" -> if (lang == "en") "Progress & Stats Widget" else "مؤشر التقدم والإحصائيات"
                    "spotlight" -> if (lang == "en") "Up Next Spotlight Card" else "بطاقة المهمة القادمة"
                    "filters" -> if (lang == "en") "Category Filter Chips" else "أزرار الفلترة والتصنيف"
                    "search" -> if (lang == "en") "Search Bar" else "شريط البحث"
                    else -> if (lang == "en") "Task Cards List / Grid" else "قائمة / شبكة المهام"
                }
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = colors.bgMain,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, colors.borderSoft, RoundedCornerShape(14.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(colors.primarySoft),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${idx + 1}",
                                    color = colors.primary,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Text(
                                text = label,
                                color = colors.textDark,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row {
                            IconButton(
                                onClick = {
                                    if (idx > 0) {
                                        val mutable = order.toMutableList()
                                        val item = mutable.removeAt(idx)
                                        mutable.add(idx - 1, item)
                                        onChange(config.copy(sectionOrder = mutable))
                                    }
                                },
                                enabled = idx > 0,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ArrowUpward,
                                    contentDescription = if (lang == "en") "Move up" else "تحريك لأعلى",
                                    tint = if (idx > 0) colors.primary else colors.textMuted.copy(alpha = 0.35f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(
                                onClick = {
                                    if (idx < order.lastIndex) {
                                        val mutable = order.toMutableList()
                                        val item = mutable.removeAt(idx)
                                        mutable.add(idx + 1, item)
                                        onChange(config.copy(sectionOrder = mutable))
                                    }
                                },
                                enabled = idx < order.lastIndex,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ArrowDownward,
                                    contentDescription = if (lang == "en") "Move down" else "تحريك لأسفل",
                                    tint = if (idx < order.lastIndex) colors.primary else colors.textMuted.copy(alpha = 0.35f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun StudioShapesAndSizesTab(
    config: ScreenDesignConfig,
    lang: String,
    onChange: (ScreenDesignConfig) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        StudioGroupHeader(if (lang == "en") "Task Card / Container Shape" else "شكل بطاقة وحاوية المهمة")
        StudioChipSelectorRow(
            options = listOf(
                "rounded" to if (lang == "en") "Rounded" else "دائري الحواف",
                "pill" to if (lang == "en") "Soft Pill" else "كبسولة ناعمة",
                "squircle" to if (lang == "en") "Squircle" else "مربع ناعم",
                "organic_blob" to if (lang == "en") "Organic Blob" else "شكل عضوي",
                "cut_corner" to if (lang == "en") "Cut Corner" else "حواف مقطوعة",
                "circle_bubble" to if (lang == "en") "Bubble Circle" else "فقاعة دائرية",
                "frameless" to if (lang == "en") "Frameless" else "بدون إطار"
            ),
            selectedId = config.cardShape,
            onSelect = { onChange(config.copy(cardShape = it)) }
        )

        StudioGroupHeader(if (lang == "en") "Task Icon & Image Shape" else "شكل صورة وأيقونة المهمة")
        StudioChipSelectorRow(
            options = listOf(
                "circle" to if (lang == "en") "Circle" else "دائرة",
                "ring" to if (lang == "en") "Halo Ring" else "حلقة مضيئة",
                "organic_blob" to if (lang == "en") "Organic Blob" else "شكل عضوي",
                "pebble" to if (lang == "en") "Soft Pebble" else "حصوة عضوية",
                "squircle" to if (lang == "en") "Squircle" else "مربع ناعم",
                "soft_hex" to if (lang == "en") "Soft Hexagon" else "سداسي ناعم",
                "ios" to if (lang == "en") "Rounded Box" else "مربع مدوّر"
            ),
            selectedId = config.taskImageShape,
            onSelect = { onChange(config.copy(taskImageShape = it)) }
        )

        StudioSliderItem(
            title = if (lang == "en") "Task Image Size" else "حجم صورة المهمة",
            valueLabel = "${config.taskImageSizeDp}dp",
            value = config.taskImageSizeDp.toFloat(),
            valueRange = 42f..104f,
            onValueChange = { onChange(config.copy(taskImageSizeDp = it.roundToInt())) }
        )
        StudioSliderItem(
            title = if (lang == "en") "Floating Badge Scale" else "حجم الأيقونة العائمة",
            valueLabel = "${config.badgeSizeScalePercent}%",
            value = config.badgeSizeScalePercent.toFloat(),
            valueRange = 70f..160f,
            onValueChange = { onChange(config.copy(badgeSizeScalePercent = it.roundToInt())) }
        )
        StudioSliderItem(
            title = if (lang == "en") "Card Corner Radius" else "استدارة حواف البطاقة",
            valueLabel = "${config.cardCornerRadiusDp}dp",
            value = config.cardCornerRadiusDp.toFloat(),
            valueRange = 8f..36f,
            onValueChange = { onChange(config.copy(cardCornerRadiusDp = it.roundToInt())) }
        )
        StudioSliderItem(
            title = if (lang == "en") "Card Spacing" else "المسافة بين البطاقات",
            valueLabel = "${config.itemSpacingDp}dp",
            value = config.itemSpacingDp.toFloat(),
            valueRange = 4f..22f,
            onValueChange = { onChange(config.copy(itemSpacingDp = it.roundToInt())) }
        )
        StudioSliderItem(
            title = if (lang == "en") "Card Inner Padding" else "الهوامش داخل البطاقة",
            valueLabel = "${config.cardPaddingDp}dp",
            value = config.cardPaddingDp.toFloat(),
            valueRange = 6f..22f,
            onValueChange = { onChange(config.copy(cardPaddingDp = it.roundToInt())) }
        )
        StudioSliderItem(
            title = if (lang == "en") "Card Shadow Elevation" else "عمق ظل البطاقة",
            valueLabel = "${config.cardElevationDp}dp",
            value = config.cardElevationDp.toFloat(),
            valueRange = 0f..16f,
            onValueChange = { onChange(config.copy(cardElevationDp = it.roundToInt())) }
        )
        StudioSliderItem(
            title = if (lang == "en") "Card Border Thickness" else "سُمك إطار البطاقة",
            valueLabel = String.format("%.1fdp", config.cardBorderWidthDp),
            value = config.cardBorderWidthDp,
            valueRange = 0f..3f,
            onValueChange = { onChange(config.copy(cardBorderWidthDp = (it * 10f).roundToInt() / 10f)) }
        )
        StudioSliderItem(
            title = if (lang == "en") "Text Size Scale" else "حجم الخط داخل التصميم",
            valueLabel = "${config.textScalePercent}%",
            value = config.textScalePercent.toFloat(),
            valueRange = 85f..130f,
            onValueChange = { onChange(config.copy(textScalePercent = it.roundToInt())) }
        )
    }
}

@Composable
internal fun StudioColorsAndStyleTab(
    config: ScreenDesignConfig,
    lang: String,
    onChange: (ScreenDesignConfig) -> Unit
) {
    val colors = LocalMNotesColors.current
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        StudioGroupHeader(if (lang == "en") "Color Theme Palettes" else "باليتات الألوان والخلفيات")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ScreenDesignPresets.palettes.forEach { pal ->
                val isSelected = config.paletteId == pal.id
                val bgTop = ScreenDesignPresets.parseHexColor(pal.bgStartHex, colors.bgTopGlow)
                val bgBot = ScreenDesignPresets.parseHexColor(pal.bgEndHex, colors.bgMain)
                val acc = ScreenDesignPresets.parseHexColor(pal.accentHex, colors.primary)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(86.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) colors.primarySoft else colors.bgMain)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) colors.primary else colors.borderSoft,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            onChange(
                                config.copy(
                                    paletteId = pal.id,
                                    bgGradientStartHex = pal.bgStartHex,
                                    bgGradientEndHex = pal.bgEndHex,
                                    cardSurfaceHex = pal.cardHex,
                                    accentHex = pal.accentHex,
                                    secondaryAccentHex = pal.secondaryHex
                                )
                            )
                        }
                        .padding(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(bgTop, bgBot)))
                            .border(2.dp, acc, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(acc)
                        )
                    }
                    Spacer(Modifier.height(5.dp))
                    Text(
                        text = if (lang == "en") pal.nameEn else pal.nameAr,
                        color = colors.textDark,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                        maxLines = 2
                    )
                }
            }
        }

        StudioColorSwatchRow(
            label = if (lang == "en") "Accent Primary Color" else "اللون الأساسي",
            selectedHex = config.accentHex,
            onPick = { onChange(config.copy(paletteId = "custom", accentHex = it)) }
        )
        StudioColorSwatchRow(
            label = if (lang == "en") "Background Top Gradient" else "لون خلفية أعلى الشاشة",
            selectedHex = config.bgGradientStartHex,
            onPick = { onChange(config.copy(paletteId = "custom", bgGradientStartHex = it)) }
        )
        StudioColorSwatchRow(
            label = if (lang == "en") "Background Bottom Gradient" else "لون خلفية أسفل الشاشة",
            selectedHex = config.bgGradientEndHex,
            onPick = { onChange(config.copy(paletteId = "custom", bgGradientEndHex = it)) }
        )
        StudioColorSwatchRow(
            label = if (lang == "en") "Card Surface Color" else "لون خلفية البطاقات",
            selectedHex = config.cardSurfaceHex,
            onPick = { onChange(config.copy(paletteId = "custom", cardSurfaceHex = it)) }
        )

        StudioGroupHeader(if (lang == "en") "Background Decorative Elements" else "العناصر الزخرفية في الخلفية")
        StudioChipSelectorRow(
            options = listOf(
                "none" to if (lang == "en") "Clean" else "بدون زخارف",
                "soft_orbs" to if (lang == "en") "Soft Orbs" else "هالات ناعمة",
                "pastel_waves" to if (lang == "en") "Pastel Waves" else "موجات باستيل",
                "doodle_dots" to if (lang == "en") "Doodle Dots" else "نقاط مزخرفة",
                "floating_sparkles" to if (lang == "en") "Sparkles" else "نجوم لامعة"
            ),
            selectedId = config.decorationStyle,
            onSelect = { onChange(config.copy(decorationStyle = it, showDecorations = it != "none")) }
        )

        StudioGroupHeader(if (lang == "en") "Progress Indicator Style" else "شكل مؤشر التقدم")
        StudioChipSelectorRow(
            options = listOf(
                "ring_card" to if (lang == "en") "Ring Card" else "بطاقة دائرية",
                "bento_tiles" to if (lang == "en") "Bento Tiles" else "مربعات بينتو",
                "linear_bar" to if (lang == "en") "Progress Bar" else "شريط خطي",
                "mini_pills" to if (lang == "en") "Mini Pills" else "كبسولات صغيرة",
                "hidden" to if (lang == "en") "Hidden" else "مخفي"
            ),
            selectedId = config.progressStyle,
            onSelect = { onChange(config.copy(progressStyle = it, showProgressWidget = it != "hidden")) }
        )

        StudioGroupHeader(if (lang == "en") "Filter Bar Style" else "شكل أزرار التصنيف")
        StudioChipSelectorRow(
            options = listOf(
                "pastel_pills" to if (lang == "en") "Pastel Pills" else "كبسولات ملونة",
                "outlined_chips" to if (lang == "en") "Outlined" else "محددة بإطار",
                "segmented" to if (lang == "en") "Segmented" else "شريط مقسّم",
                "minimal_underline" to if (lang == "en") "Underline" else "خط سفلي"
            ),
            selectedId = config.filterStyle,
            onSelect = { onChange(config.copy(filterStyle = it)) }
        )

        StudioGroupHeader(if (lang == "en") "Add Button / Bottom Bar Style" else "شكل زر الإضافة والشريط السفلي")
        StudioChipSelectorRow(
            options = listOf(
                "capsule_bar" to if (lang == "en") "Capsule Bar" else "شريط كبسولة",
                "floating_fab" to if (lang == "en") "Circle FAB" else "زر دائري عائم",
                "extended_pill" to if (lang == "en") "Extended Pill" else "زر ممتد",
                "soft_squircle" to if (lang == "en") "Soft Squircle" else "مربع ناعم",
                "hidden" to if (lang == "en") "Hidden" else "مخفي"
            ),
            selectedId = config.addButtonStyle,
            onSelect = {
                onChange(
                    config.copy(
                        addButtonStyle = it,
                        showBottomNavBar = it == "capsule_bar"
                    )
                )
            }
        )
    }
}

@Composable
internal fun StudioElementsVisibilityTab(
    config: ScreenDesignConfig,
    lang: String,
    onChange: (ScreenDesignConfig) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        StudioGroupHeader(if (lang == "en") "Show or Hide Any Screen Element" else "إظهار أو إخفاء أي عنصر بالشاشة")
        StudioToggleItem(
            title = if (lang == "en") "Greeting & Summary Header" else "العنوان العلوي وملخص اليوم",
            checked = config.showGreetingHeader,
            onCheckedChange = { onChange(config.copy(showGreetingHeader = it)) }
        )
        StudioToggleItem(
            title = if (lang == "en") "Week Days Strip" else "شريط أيام الأسبوع",
            checked = config.showWeekStrip,
            onCheckedChange = { onChange(config.copy(showWeekStrip = it)) }
        )
        StudioToggleItem(
            title = if (lang == "en") "Progress & Streak Indicator" else "مؤشر التقدم والاستمرارية",
            checked = config.showProgressWidget,
            onCheckedChange = { onChange(config.copy(showProgressWidget = it)) }
        )
        StudioToggleItem(
            title = if (lang == "en") "Hero \"Up Next\" Spotlight Card" else "بطاقة المهمة القادمة البارزة",
            checked = config.showSpotlightUpNext,
            onCheckedChange = { onChange(config.copy(showSpotlightUpNext = it)) }
        )
        StudioToggleItem(
            title = if (lang == "en") "Category Filter Chips" else "أزرار الفلترة (الكل/اليوم/الجيم)",
            checked = config.showFilterChips,
            onCheckedChange = { onChange(config.copy(showFilterChips = it)) }
        )
        StudioToggleItem(
            title = if (lang == "en") "Search Bar" else "شريط البحث السريع",
            checked = config.showSearchBar,
            onCheckedChange = { onChange(config.copy(showSearchBar = it)) }
        )
        StudioToggleItem(
            title = if (lang == "en") "Task Floating Mini Icons" else "الأيقونات العائمة الصغيرة على المهام",
            checked = config.showTaskFloatingBadges,
            onCheckedChange = { onChange(config.copy(showTaskFloatingBadges = it)) }
        )
        StudioToggleItem(
            title = if (lang == "en") "Time & Schedule Pill" else "شارة الوقت والموعد",
            checked = config.showTaskTimePill,
            onCheckedChange = { onChange(config.copy(showTaskTimePill = it)) }
        )
        StudioToggleItem(
            title = if (lang == "en") "Cycle & Status Badges (Zzz)" else "شارات الحالة والتكرار (Zzz)",
            checked = config.showTaskStatusBadges,
            onCheckedChange = { onChange(config.copy(showTaskStatusBadges = it)) }
        )
        StudioToggleItem(
            title = if (lang == "en") "Separate Active Today vs Resting Groups" else "فصل المهام النشطة عن النائمة بمجموعتين",
            checked = config.showRestingGroupSeparate,
            onCheckedChange = { onChange(config.copy(showRestingGroupSeparate = it)) }
        )
        StudioToggleItem(
            title = if (lang == "en") "Quick Action Buttons on Cards" else "أزرار التنبيه والحذف على البطاقة",
            checked = config.showQuickActionsOnCard,
            onCheckedChange = { onChange(config.copy(showQuickActionsOnCard = it)) }
        )
        StudioToggleItem(
            title = if (lang == "en") "Bottom Capsule Navigation Bar" else "شريط التنقل السفلي العائم",
            checked = config.showBottomNavBar,
            onCheckedChange = {
                onChange(
                    config.copy(
                        showBottomNavBar = it,
                        addButtonStyle = if (it) "capsule_bar" else if (config.addButtonStyle == "capsule_bar") "floating_fab" else config.addButtonStyle
                    )
                )
            }
        )
        StudioToggleItem(
            title = if (lang == "en") "Background Decorative Art" else "زخارف وهالات الخلفية",
            checked = config.showDecorations,
            onCheckedChange = { onChange(config.copy(showDecorations = it)) }
        )
    }
}

@Composable
internal fun StudioGroupHeader(text: String) {
    val colors = LocalMNotesColors.current
    Text(
        text = text,
        color = colors.textDark,
        fontSize = 12.sp,
        fontWeight = FontWeight.ExtraBold
    )
}

@Composable
internal fun StudioChipSelectorRow(
    options: List<Pair<String, String>>,
    selectedId: String,
    onSelect: (String) -> Unit
) {
    val colors = LocalMNotesColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        options.forEach { (id, label) ->
            val isSelected = id == selectedId
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (isSelected) colors.primary else colors.bgMain)
                    .border(
                        width = if (isSelected) 0.dp else 1.dp,
                        color = if (isSelected) Color.Transparent else colors.borderSoft,
                        shape = RoundedCornerShape(999.dp)
                    )
                    .clickable { onSelect(id) }
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            ) {
                Text(
                    text = label,
                    color = if (isSelected) Color.White else colors.textDark,
                    fontSize = 11.5.sp,
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun StudioSliderItem(
    title: String,
    valueLabel: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    val colors = LocalMNotesColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.bgMain)
            .border(1.dp, colors.borderSoft, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, color = colors.textDark, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
            Text(text = valueLabel, color = colors.primary, fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold)
        }
        Slider(
            value = value.coerceIn(valueRange.start, valueRange.endInclusive),
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = colors.primary,
                activeTrackColor = colors.primary,
                inactiveTrackColor = colors.borderSoft
            ),
            modifier = Modifier.height(30.dp)
        )
    }
}

@Composable
private fun StudioColorSwatchRow(
    label: String,
    selectedHex: String,
    onPick: (String) -> Unit
) {
    val colors = LocalMNotesColors.current
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = label, color = colors.textMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            customColorSwatches.forEach { hex ->
                val swatchColor = ScreenDesignPresets.parseHexColor(hex, Color.White)
                val isSelected = selectedHex.equals(hex, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(swatchColor)
                        .border(
                            width = if (isSelected) 2.5.dp else 1.dp,
                            color = if (isSelected) colors.primary else colors.borderSoft,
                            shape = CircleShape
                        )
                        .clickable { onPick(hex) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        val isLight = (swatchColor.red * 0.299f + swatchColor.green * 0.587f + swatchColor.blue * 0.114f) > 0.72f
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = if (isLight) Color(0xFF0F172A) else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StudioToggleItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = LocalMNotesColors.current
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = colors.bgMain,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colors.borderSoft, RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCheckedChange(!checked) }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                color = colors.textDark,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = colors.primary
                )
            )
        }
    }
}
