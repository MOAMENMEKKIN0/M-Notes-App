package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Search
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.AppStrings
import com.example.model.SmartIconSettings
import com.example.model.SmartTaskBadgeResolver
import com.example.ui.MNotesViewModel
import com.example.ui.theme.LocalMNotesColors

/**
 * Smart Daily & Recurring Task Icon Preferences Panel.
 *
 * Lets the user:
 * - Enable or disable smart automatic icons (manual selections are ALWAYS preserved when off).
 * - Choose default smart mode: Manual, Category Preferred, Rotate My Favorites, or Weekday Schedule.
 * - Toggle Rest-Day icon on recurring task off-days & Today is Holiday override.
 * - Curate their OWN favorite icons per activity category (Gym, Rest Days, Holiday, Work, Study,
 *   Sleep, Food, Coffee, Walk, Relax). The app ONLY uses icons the user explicitly picks.
 * - Assign custom icons to specific days of the week (Sun..Sat).
 */
@Composable
fun SmartIconRulesPanel(
    settings: SmartIconSettings,
    lang: String,
    viewModel: MNotesViewModel
) {
    val colors = LocalMNotesColors.current
    var editingCategoryId by remember { mutableStateOf<String?>(null) }
    var editingWeekdayIdx by remember { mutableStateOf<Int?>(null) }
    var pickerCatFilter by remember { mutableStateOf("all") }
    var pickerQuery by remember { mutableStateOf("") }

    val filteredPickerIcons = remember(pickerCatFilter, pickerQuery) {
        FloatingBadgeCatalog.filter(pickerCatFilter, pickerQuery)
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // 1. Master Smart Auto-Icon Controls
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = colors.cardBg,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.2.dp, colors.primary.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = if (lang == "en") "Smart Automatic Icons" else "تغيير الأيقونات الذكي التلقائي",
                                color = colors.textDark,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = if (lang == "en") {
                                    "Only uses icons you choose below. Manual task icons stay unchanged when off."
                                } else {
                                    "يستخدم فقط الأيقونات التي تختارها بالأسفل. عند الإيقاف يظل اختيارك اليدوي ثابتاً."
                                },
                                color = colors.textMuted,
                                fontSize = 10.sp,
                                lineHeight = 13.5.sp
                            )
                        }
                    }
                    Switch(
                        checked = settings.enableSmartAutoIcons,
                        onCheckedChange = { enabled ->
                            viewModel.playSound("nav")
                            viewModel.updateSmartIconSettings { it.copy(enableSmartAutoIcons = enabled) }
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = colors.primary),
                        modifier = Modifier.testTag("smart_auto_icons_switch")
                    )
                }

                // Default Smart Mode Selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (lang == "en") "Default Automatic Mode" else "الوضع التلقائي الافتراضي",
                        color = colors.textDark,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SmartTaskBadgeResolver.smartModeOptions.forEach { mode ->
                            val sel = settings.smartDefaultMode == mode.id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(if (sel) colors.primary else colors.bgMain)
                                    .border(
                                        1.dp,
                                        if (sel) colors.primary else colors.borderSoft,
                                        RoundedCornerShape(999.dp)
                                    )
                                    .clickable {
                                        viewModel.playSound("nav")
                                        viewModel.updateSmartIconSettings { it.copy(smartDefaultMode = mode.id) }
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("smart_default_mode_${mode.id}")
                            ) {
                                Text(
                                    text = if (lang == "en") mode.nameEn else mode.nameAr,
                                    color = if (sel) Color.White else colors.textDark,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Rest-Day Off-Day Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (lang == "en") "Rest-Day Icon on Off-Days" else "أيقونة الراحة في الأيام غير النشطة",
                            color = colors.textDark,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (lang == "en") {
                                "Shows your chosen Rest Days icon when a recurring task is resting today"
                            } else {
                                "يعرض أيقونة أيام الراحة المختارة عندما تكون المهمة في راحة اليوم"
                            },
                            color = colors.textMuted,
                            fontSize = 10.sp
                        )
                    }
                    Switch(
                        checked = settings.useRestDayIconOnOffDays,
                        onCheckedChange = { checked ->
                            viewModel.playSound("nav")
                            viewModel.updateSmartIconSettings { it.copy(useRestDayIconOnOffDays = checked) }
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = colors.primary),
                        modifier = Modifier.testTag("smart_rest_day_switch")
                    )
                }

                // Today is Holiday / Day Off Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (lang == "en") "Today is a Holiday / Day Off" else "اليوم إجازة / يوم عطلة",
                            color = colors.textDark,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (lang == "en") {
                                "Switches smart tasks to your chosen Holiday icon(s) today"
                            } else {
                                "يعرض أيقونة الإجازة المختارة للمهام الذكية اليوم"
                            },
                            color = colors.textMuted,
                            fontSize = 10.sp
                        )
                    }
                    Switch(
                        checked = settings.todayIsHoliday,
                        onCheckedChange = { checked ->
                            viewModel.playSound("nav")
                            viewModel.updateSmartIconSettings { it.copy(todayIsHoliday = checked) }
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = colors.primary),
                        modifier = Modifier.testTag("smart_holiday_today_switch")
                    )
                }
            }
        }

        // 2. MY FAVORITE ICONS BY ACTIVITY CATEGORY (multi-select for Category Preferred & Rotation)
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = colors.cardBg,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, colors.borderSoft, RoundedCornerShape(18.dp))
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (lang == "en") "My Favorite Icons per Category" else "أيقوناتي المفضلة لكل نشاط",
                    color = colors.textDark,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = if (lang == "en") {
                        "Tap a category to pick 1 or more favorite icons. Smart mode only uses icons you select here."
                    } else {
                        "اضغط على أي نشاط لاختيار أيقونة أو أكثر من مكتبتك. النظام الذكي لا يختار إلا من مفضلاتك فقط."
                    },
                    color = colors.textMuted,
                    fontSize = 10.sp,
                    lineHeight = 13.5.sp
                )

                SmartTaskBadgeResolver.smartCategories.forEach { cat ->
                    val favIds = settings.categoryFavorites[cat.id].orEmpty()
                    val isOpen = editingCategoryId == cat.id
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isOpen) colors.primarySoft.copy(alpha = 0.45f) else colors.bgMain)
                            .border(
                                1.dp,
                                if (isOpen) colors.primary else colors.borderSoft,
                                RoundedCornerShape(14.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.playSound("nav")
                                    editingWeekdayIdx = null
                                    editingCategoryId = if (isOpen) null else cat.id
                                    pickerCatFilter = "all"
                                    pickerQuery = ""
                                }
                                .testTag("smart_fav_cat_${cat.id}"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (lang == "en") cat.nameEn else cat.nameAr,
                                    color = colors.textDark,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = if (lang == "en") "${favIds.size} selected" else "${favIds.size} مختارة",
                                    color = colors.textMuted,
                                    fontSize = 10.sp
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                favIds.take(4).forEach { id ->
                                    FloatingBadgeVisual(iconId = id, sizeDp = 24f, contentDescription = null)
                                }
                                Icon(
                                    imageVector = if (isOpen) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        if (isOpen) {
                            InlineIconMultiPicker(
                                selectedIds = favIds.toSet(),
                                lang = lang,
                                filterCat = pickerCatFilter,
                                onFilterCatChange = { pickerCatFilter = it },
                                query = pickerQuery,
                                onQueryChange = { pickerQuery = it },
                                filteredIcons = filteredPickerIcons,
                                onToggleIcon = { iconId ->
                                    viewModel.playSound("nav")
                                    viewModel.updateSmartIconSettings { cur ->
                                        val existing = cur.categoryFavorites[cat.id].orEmpty()
                                        val nextList = if (existing.contains(iconId)) {
                                            existing.filterNot { it == iconId }
                                        } else {
                                            existing + iconId
                                        }
                                        val nextMap = cur.categoryFavorites.toMutableMap()
                                        nextMap[cat.id] = nextList
                                        cur.copy(categoryFavorites = nextMap)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // 3. WEEKDAY SCHEDULE ICONS (0=Sun .. 6=Sat)
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = colors.cardBg,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, colors.borderSoft, RoundedCornerShape(18.dp))
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (lang == "en") "Weekday Schedule Icons" else "أيقونات أيام الأسبوع",
                    color = colors.textDark,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = if (lang == "en") {
                        "Tap any day circle to assign an icon for \"By Weekday Schedule\" mode."
                    } else {
                        "اضغط على أي يوم لتعيين أيقونة مخصصة لوضع «حسب يوم الأسبوع»."
                    },
                    color = colors.textMuted,
                    fontSize = 10.sp
                )

                val dayLabels = AppStrings.dayNames(lang)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    dayLabels.forEachIndexed { dayIdx, label ->
                        val assignedIcon = settings.dayOfWeekIcons[dayIdx]
                        val isEditing = editingWeekdayIdx == dayIdx
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isEditing) colors.primarySoft else colors.bgMain)
                                .border(
                                    1.2.dp,
                                    if (isEditing) colors.primary else colors.borderSoft,
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    viewModel.playSound("nav")
                                    editingCategoryId = null
                                    editingWeekdayIdx = if (isEditing) null else dayIdx
                                    pickerCatFilter = "all"
                                    pickerQuery = ""
                                }
                                .padding(vertical = 6.dp, horizontal = 2.dp)
                                .testTag("smart_weekday_$dayIdx")
                        ) {
                            Text(
                                text = label,
                                color = if (isEditing) colors.primary else colors.textDark,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            if (!assignedIcon.isNullOrBlank()) {
                                FloatingBadgeVisual(iconId = assignedIcon, sizeDp = 24f, contentDescription = null)
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(colors.cardBg)
                                        .border(1.dp, colors.borderSoft, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("—", color = colors.textMuted, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }

                val targetDay = editingWeekdayIdx
                if (targetDay != null) {
                    val assigned = settings.dayOfWeekIcons[targetDay].orEmpty()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (lang == "en") {
                                "Choose icon for ${dayLabels.getOrElse(targetDay) { "" }}"
                            } else {
                                "اختر أيقونة يوم ${dayLabels.getOrElse(targetDay) { "" }}"
                            },
                            color = colors.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        if (assigned.isNotBlank()) {
                            Text(
                                text = if (lang == "en") "Clear" else "مسح",
                                color = Color(0xFFEF4444),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        viewModel.updateSmartIconSettings { cur ->
                                            val next = cur.dayOfWeekIcons.toMutableMap()
                                            next.remove(targetDay)
                                            cur.copy(dayOfWeekIcons = next)
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    InlineIconMultiPicker(
                        selectedIds = if (assigned.isNotBlank()) setOf(assigned) else emptySet(),
                        lang = lang,
                        filterCat = pickerCatFilter,
                        onFilterCatChange = { pickerCatFilter = it },
                        query = pickerQuery,
                        onQueryChange = { pickerQuery = it },
                        filteredIcons = filteredPickerIcons,
                        onToggleIcon = { iconId ->
                            viewModel.playSound("done")
                            viewModel.updateSmartIconSettings { cur ->
                                val next = cur.dayOfWeekIcons.toMutableMap()
                                next[targetDay] = iconId
                                cur.copy(dayOfWeekIcons = next)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun InlineIconMultiPicker(
    selectedIds: Set<String>,
    lang: String,
    filterCat: String,
    onFilterCatChange: (String) -> Unit,
    query: String,
    onQueryChange: (String) -> Unit,
    filteredIcons: List<FloatingBadgeIconSpec>,
    onToggleIcon: (String) -> Unit
) {
    val colors = LocalMNotesColors.current
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            placeholder = {
                Text(
                    text = if (lang == "en") "Search icons..." else "ابحث عن أيقونة...",
                    fontSize = 11.sp
                )
            },
            leadingIcon = {
                Icon(Icons.Rounded.Search, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(16.dp))
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = null,
                        tint = colors.textMuted,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { onQueryChange("") }
                    )
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.primary,
                unfocusedBorderColor = colors.borderSoft,
                focusedContainerColor = colors.cardBg,
                unfocusedContainerColor = colors.cardBg
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            FloatingBadgeCatalog.categories.forEach { cat ->
                val sel = filterCat == cat.id
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (sel) colors.primary else colors.cardBg)
                        .border(1.dp, if (sel) colors.primary else colors.borderSoft, RoundedCornerShape(999.dp))
                        .clickable { onFilterCatChange(cat.id) }
                        .padding(horizontal = 9.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = if (lang == "en") cat.nameEn else cat.nameAr,
                        color = if (sel) Color.White else colors.textDark,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            contentPadding = PaddingValues(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
        ) {
            items(filteredIcons, key = { it.id }) { spec ->
                val isSelected = selectedIds.contains(spec.id)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) colors.primarySoft else colors.cardBg)
                        .border(
                            if (isSelected) 1.5.dp else 1.dp,
                            if (isSelected) colors.primary else colors.borderSoft,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onToggleIcon(spec.id) }
                        .padding(top = 8.dp, bottom = 5.dp, start = 2.dp, end = 2.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        FloatingBadgeVisual(iconId = spec.id, sizeDp = 32f, contentDescription = null)
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(colors.primary)
                                    .border(1.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(9.dp))
                            }
                        }
                    }
                    Text(
                        text = if (lang == "en") spec.nameEn else spec.nameAr,
                        color = if (isSelected) colors.primary else colors.textDark,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        Spacer(Modifier.height(2.dp))
    }
}
