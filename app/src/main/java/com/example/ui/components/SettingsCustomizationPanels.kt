package com.example.ui.components

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.rounded.AddToHomeScreen
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LauncherIconSpec
import com.example.model.MNotesCatalog
import com.example.model.UserPreferences
import com.example.ui.MNotesViewModel
import com.example.ui.theme.LocalMNotesColors
import com.example.util.LauncherIconManager

private data class ColorSwatchOption(
    val hex: String,
    val labelAr: String,
    val labelEn: String,
    val color: Color
)

@Composable
fun HomeScreenControlsContent(
    prefs: UserPreferences,
    lang: String,
    viewModel: MNotesViewModel
) {
    val colors = LocalMNotesColors.current

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = if (lang == "en") "Choose Main Layout (3 Styles)" else "اختيار شكل الواجهة الرئيسية (٣ تصاميم)",
            color = colors.textMuted,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            MNotesCatalog.layoutOptions.forEach { opt ->
                val active = prefs.layoutStyle == opt.id
                StudioSwitchRow(
                    title = if (lang == "en") opt.nameEn else opt.nameAr,
                    checked = active,
                    onCheckedChange = {
                        viewModel.playSound("nav")
                        viewModel.updatePreference(mapOf("layoutStyle" to opt.id))
                    }
                )
            }
        }

        Spacer(Modifier.height(4.dp))
        Text(
            text = if (lang == "en") "Show or Hide Home Screen Sections" else "إظهار أو حذف أي جزء من الواجهة الرئيسية",
            color = colors.textMuted,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.ExtraBold
        )

        val toggles = listOf(
            Triple("showTopBarTabs", if (lang == "en") "Top Tabs Bar (Tasks / Requests)" else "شريط التبويبات العلوي (المهام / الطلبات)", prefs.showTopBarTabs),
            Triple("showBanner", if (lang == "en") "Header / Hero Banner" else "بطاقة الترويسة والبانر العلوي", prefs.showBanner),
            Triple("showStatsSummary", if (lang == "en") "Progress Ring & Summary Cards" else "بطاقات الملخص والإحصائيات والعد التنازلي", prefs.showStatsSummary),
            Triple("showWeekStrip", if (lang == "en") "Weekly Days Strip" else "شريط أيام الأسبوع", prefs.showWeekStrip),
            Triple("showSpotlightCard", if (lang == "en") "Up-Next Spotlight Card" else "بطاقة المهمة القادمة الآن", prefs.showSpotlightCard),
            Triple("showFilterChips", if (lang == "en") "Quick Filter Chips" else "أزرار الفرز السريعة (الكل / الجيم / يومي)", prefs.showFilterChips),
            Triple("showSearchBar", if (lang == "en") "Search Input Bar" else "شريط البحث في المهام", prefs.showSearchBar),
            Triple("showQuickDock", if (lang == "en") "Quick Action Buttons" else "أزرار الإضافة والمعاينة السريعة", prefs.showQuickDock)
        )

        toggles.forEach { (key, title, enabled) ->
            StudioSwitchRow(
                title = title,
                checked = enabled,
                onCheckedChange = { checked ->
                    viewModel.playSound("nav")
                    viewModel.updatePreference(mapOf(key to checked))
                }
            )
        }
    }
}

@Composable
fun InAppCustomColorStudio(
    prefs: UserPreferences,
    lang: String,
    viewModel: MNotesViewModel
) {
    val colors = LocalMNotesColors.current

    val primarySwatches = listOf(
        ColorSwatchOption("", "تلقائي", "Default", MNotesCatalog.themeById(prefs.themeId).primary),
        ColorSwatchOption("#18C28B", "مينت", "Mint", Color(0xFF18C28B)),
        ColorSwatchOption("#10B981", "زمردي", "Emerald", Color(0xFF10B981)),
        ColorSwatchOption("#38BDF8", "لبني", "Baby Blue", Color(0xFF38BDF8)),
        ColorSwatchOption("#9A5B25", "بيج كراميل", "Beige", Color(0xFF9A5B25)),
        ColorSwatchOption("#0F172A", "داكن أنيق", "Slate", Color(0xFF0F172A))
    )

    val bgSwatches = listOf(
        ColorSwatchOption("", "تلقائي", "Default", MNotesCatalog.themeById(prefs.themeId).bgMain),
        ColorSwatchOption("#FFFFFF", "أبيض نقي", "White", Color(0xFFFFFFFF)),
        ColorSwatchOption("#F4FBF8", "مينت في أبيض", "Mint White", Color(0xFFF4FBF8)),
        ColorSwatchOption("#E6FBF3", "مينت ناعم", "Soft Mint", Color(0xFFE6FBF3)),
        ColorSwatchOption("#F5EBDD", "بيج", "Beige", Color(0xFFF5EBDD)),
        ColorSwatchOption("#EFF8FF", "لبني", "Baby Blue", Color(0xFFEFF8FF))
    )

    val accentSwatches = listOf(
        ColorSwatchOption("", "تلقائي", "Default", MNotesCatalog.themeById(prefs.themeId).accent),
        ColorSwatchOption("#D5B895", "بيج", "Beige", Color(0xFFD5B895)),
        ColorSwatchOption("#18C28B", "مينت", "Mint", Color(0xFF18C28B)),
        ColorSwatchOption("#38BDF8", "لبني", "Baby Blue", Color(0xFF38BDF8)),
        ColorSwatchOption("#EA580C", "دافئ", "Warm", Color(0xFFEA580C)),
        ColorSwatchOption("#FFFFFF", "أبيض", "White", Color(0xFFFFFFFF))
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colors.bgMain,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colors.borderSoft, RoundedCornerShape(16.dp))
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Rounded.Palette, contentDescription = null, tint = colors.primary, modifier = Modifier.size(16.dp))
                    Text(
                        text = if (lang == "en") "Custom Color Builder" else "تركيب وتخصيص الألوان يدوياً",
                        color = colors.textDark,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                if (prefs.customPrimaryHex.isNotEmpty() || prefs.customBgHex.isNotEmpty() || prefs.customAccentHex.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(colors.cardBg)
                            .border(1.dp, colors.borderSoft, RoundedCornerShape(999.dp))
                            .clickable {
                                viewModel.playSound("nav")
                                viewModel.updatePreference(
                                    mapOf(
                                        "customPrimaryHex" to "",
                                        "customBgHex" to "",
                                        "customAccentHex" to ""
                                    )
                                )
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Rounded.Refresh, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(12.dp))
                        Text(
                            text = if (lang == "en") "Reset" else "الافتراضي",
                            color = colors.textMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            ColorSwatchStrip(
                title = if (lang == "en") "Icons & Primary Accent" else "لون الأيقونات والأزرار",
                lang = lang,
                swatches = primarySwatches,
                selectedHex = prefs.customPrimaryHex,
                onSelect = { hex ->
                    viewModel.playSound("nav")
                    viewModel.updatePreference(mapOf("customPrimaryHex" to hex))
                }
            )

            ColorSwatchStrip(
                title = if (lang == "en") "App Background Color" else "لون خلفية الواجهة",
                lang = lang,
                swatches = bgSwatches,
                selectedHex = prefs.customBgHex,
                onSelect = { hex ->
                    viewModel.playSound("nav")
                    viewModel.updatePreference(mapOf("customBgHex" to hex))
                }
            )

            ColorSwatchStrip(
                title = if (lang == "en") "Secondary Highlights" else "لون اللمسات والرموز",
                lang = lang,
                swatches = accentSwatches,
                selectedHex = prefs.customAccentHex,
                onSelect = { hex ->
                    viewModel.playSound("nav")
                    viewModel.updatePreference(mapOf("customAccentHex" to hex))
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExternalAppIconFolderContent(
    prefs: UserPreferences,
    lang: String,
    viewModel: MNotesViewModel
) {
    val colors = LocalMNotesColors.current
    val context = LocalContext.current

    // Filter state: All / Circle / Organic / Mint / Beige / Baby Blue / White
    var iconFilter by remember { mutableStateOf("all") }
    val filters = listOf(
        "all" to (if (lang == "en") "All (12)" else "الكل (١٢)"),
        "circle" to (if (lang == "en") "Circle" else "مدورة"),
        "organic" to (if (lang == "en") "Organic" else "شكل عضوي"),
        "beige" to (if (lang == "en") "Beige" else "بيج"),
        "mint" to (if (lang == "en") "Mint" else "مينت"),
        "blue" to (if (lang == "en") "Baby Blue" else "لبني"),
        "white" to (if (lang == "en") "White" else "أبيض")
    )

    val filteredIcons = remember(iconFilter) {
        MNotesCatalog.launcherIcons.filter { spec ->
            when (iconFilter) {
                "circle" -> spec.id in setOf("I2", "I4", "I6", "I9", "I11")
                "organic" -> spec.id in setOf("I3", "I5", "I7", "I10", "I12")
                "beige" -> spec.id in setOf("I2", "I7", "I11")
                "mint" -> spec.id in setOf("I1", "I3", "I4", "I6", "I8", "I10", "I12")
                "blue" -> spec.id in setOf("I5", "I9", "I12")
                "white" -> spec.id in setOf("I3", "I6", "I8", "I9", "I11")
                else -> true
            }
        }
    }

    // Custom Icon Designer State
    var studioShape by remember { mutableStateOf("blob") }
    var studioSymbol by remember { mutableStateOf("bell_m") }
    var studioBgHex by remember { mutableStateOf("#E6FBF3") }
    var studioFgHex by remember { mutableStateOf("#18C28B") }
    var studioBadgeHex by remember { mutableStateOf("#38BDF8") }

    val studioBgSwatches = listOf(
        ColorSwatchOption("#F5EBDD", "بيج", "Beige", Color(0xFFF5EBDD)),
        ColorSwatchOption("#FFFFFF", "أبيض", "White", Color(0xFFFFFFFF)),
        ColorSwatchOption("#18C28B", "مينت", "Mint", Color(0xFF18C28B)),
        ColorSwatchOption("#E6FBF3", "مينت في أبيض", "Mint Ice", Color(0xFFE6FBF3)),
        ColorSwatchOption("#38BDF8", "لبني", "Baby Blue", Color(0xFF38BDF8)),
        ColorSwatchOption("#EFF8FF", "لبني فاتح", "Sky Ice", Color(0xFFEFF8FF))
    )

    val studioFgSwatches = listOf(
        ColorSwatchOption("#FFFFFF", "أبيض", "White", Color(0xFFFFFFFF)),
        ColorSwatchOption("#18C28B", "مينت", "Mint", Color(0xFF18C28B)),
        ColorSwatchOption("#8B4F21", "بيج غامق", "Caramel", Color(0xFF8B4F21)),
        ColorSwatchOption("#0EA5E9", "لبني", "Sky", Color(0xFF0EA5E9)),
        ColorSwatchOption("#0F172A", "داكن", "Slate", Color(0xFF0F172A))
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = if (lang == "en") {
                "Choose a rounded, organic, reminder & letter design for your phone home screen:"
            } else {
                "اختر أي تصميم مدور أو عضوي أو بألوان البيج والأبيض والمينت واللبني لتغييره كأيقونة للتطبيق:"
            },
            color = colors.textMuted,
            fontSize = 11.5.sp
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            filters.forEach { (id, label) ->
                val selected = iconFilter == id
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (selected) colors.primary else colors.bgMain)
                        .border(1.dp, if (selected) colors.primary else colors.borderSoft, RoundedCornerShape(999.dp))
                        .clickable {
                            viewModel.playSound("nav")
                            iconFilter = id
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = label,
                        color = if (selected) Color.White else colors.textMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        filteredIcons.chunked(2).forEach { rowIcons ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowIcons.forEach { spec: LauncherIconSpec ->
                    val active = prefs.appIconId == spec.id
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (active) colors.primarySoft else colors.bgMain,
                        modifier = Modifier
                            .weight(1f)
                            .border(
                                1.5.dp,
                                if (active) colors.primary else colors.borderSoft,
                                RoundedCornerShape(16.dp)
                            )
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                viewModel.changeLauncherIcon(context, spec.id)
                            }
                            .testTag("launcher_icon_${spec.id}")
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(64.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(58.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(
                                            Brush.linearGradient(listOf(spec.bgStart, spec.bgEnd))
                                        )
                                        .border(1.dp, colors.borderSoft, RoundedCornerShape(18.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = spec.fgDrawableRes),
                                        contentDescription = if (lang == "en") spec.nameEn else spec.nameAr,
                                        contentScale = if (spec.isBitmapArt) ContentScale.Crop else ContentScale.Fit,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .then(if (spec.isBitmapArt) Modifier else Modifier.padding(4.dp))
                                    )
                                }
                                if (active) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = null,
                                        tint = colors.primary,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .align(Alignment.TopEnd)
                                            .background(Color.White, CircleShape)
                                    )
                                }
                            }

                            Text(
                                text = if (lang == "en") spec.nameEn else spec.nameAr,
                                color = colors.textDark,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(if (active) colors.primary else colors.cardBg)
                                    .border(
                                        1.dp,
                                        if (active) colors.primary else colors.borderSoft,
                                        RoundedCornerShape(999.dp)
                                    )
                                    .padding(vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (active) {
                                        if (lang == "en") "Active ✓" else "مفعلة ✓"
                                    } else {
                                        if (lang == "en") "Select" else "تفعيل"
                                    },
                                    color = if (active) Color.White else colors.textMuted,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                if (rowIcons.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }

        // Custom Icon & Color Designer Studio
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = colors.bgMain,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, colors.borderSoft, RoundedCornerShape(18.dp))
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (lang == "en") "Custom Icon & Color Studio" else "استوديو تلوين وتصميم الأيقونة من جوا",
                    color = colors.textDark,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                val previewBitmap = remember(studioShape, studioSymbol, studioBgHex, studioFgHex, studioBadgeHex) {
                    LauncherIconManager.renderCustomIconBitmap(
                        sizePx = 180,
                        shapeId = studioShape,
                        symbolId = studioSymbol,
                        bgColorArgb = android.graphics.Color.parseColor(studioBgHex),
                        fgColorArgb = android.graphics.Color.parseColor(studioFgHex),
                        badgeColorArgb = android.graphics.Color.parseColor(studioBadgeHex)
                    ).asImageBitmap()
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(colors.cardBg)
                            .border(1.dp, colors.borderSoft, RoundedCornerShape(18.dp))
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = previewBitmap,
                            contentDescription = "Custom Icon Preview",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            listOf(
                                "circle" to (if (lang == "en") "Circle" else "مدور"),
                                "blob" to (if (lang == "en") "Organic" else "عضوي"),
                                "ios" to (if (lang == "en") "Rounded" else "مربع ناعم")
                            ).forEach { (id, lbl) ->
                                MiniStudioPill(
                                    label = lbl,
                                    selected = studioShape == id,
                                    onClick = {
                                        viewModel.playSound("nav")
                                        studioShape = id
                                    }
                                )
                            }
                        }

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            listOf(
                                "bell_m" to (if (lang == "en") "Bell + M" else "تذكير + حرف"),
                                "clock_m" to (if (lang == "en") "Clock + M" else "ساعة + حرف"),
                                "gym_m" to (if (lang == "en") "Gym + M" else "جيم + حرف"),
                                "m_only" to (if (lang == "en") "Letter M" else "حرف + نقطة")
                            ).forEach { (id, lbl) ->
                                MiniStudioPill(
                                    label = lbl,
                                    selected = studioSymbol == id,
                                    onClick = {
                                        viewModel.playSound("nav")
                                        studioSymbol = id
                                    }
                                )
                            }
                        }
                    }
                }

                ColorSwatchStrip(
                    title = if (lang == "en") "Icon Background Color" else "لون خلفية الأيقونة",
                    lang = lang,
                    swatches = studioBgSwatches,
                    selectedHex = studioBgHex,
                    onSelect = {
                        viewModel.playSound("nav")
                        studioBgHex = it
                    }
                )

                ColorSwatchStrip(
                    title = if (lang == "en") "Letter & Reminder Color" else "لون الحرف وعلامة التذكير",
                    lang = lang,
                    swatches = studioFgSwatches,
                    selectedHex = studioFgHex,
                    onSelect = {
                        viewModel.playSound("nav")
                        studioFgHex = it
                    }
                )

                ColorSwatchStrip(
                    title = if (lang == "en") "Accent Detail Color" else "لون اللمسة الجمالية",
                    lang = lang,
                    swatches = studioFgSwatches,
                    selectedHex = studioBadgeHex,
                    onSelect = {
                        viewModel.playSound("nav")
                        studioBadgeHex = it
                    }
                )

                Button(
                    onClick = {
                        viewModel.createCustomShortcut(
                            context = context,
                            shapeId = studioShape,
                            symbolId = studioSymbol,
                            bgColorArgb = android.graphics.Color.parseColor(studioBgHex),
                            fgColorArgb = android.graphics.Color.parseColor(studioFgHex),
                            badgeColorArgb = android.graphics.Color.parseColor(studioBadgeHex)
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Rounded.AddToHomeScreen, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (lang == "en") "Add Custom Colored Icon to Home Screen" else "إضافة هذه الأيقونة بألوانها للشاشة الرئيسية",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = {
                        viewModel.playSound("done")
                        val primaryChoice = if (studioFgHex == "#FFFFFF") studioBgHex else studioFgHex
                        viewModel.updatePreference(
                            mapOf(
                                "customPrimaryHex" to primaryChoice,
                                "customBgHex" to studioBgHex,
                                "customAccentHex" to studioBadgeHex,
                                "bannerPhotoShape" to studioShape,
                                "cardShape" to if (studioShape == "circle") "circle" else studioShape
                            )
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (lang == "en") "Apply Colors & Shape Inside App Too" else "تطبيق نفس الشكل والألوان داخل التطبيق أيضاً",
                        color = colors.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorSwatchStrip(
    title: String,
    lang: String,
    swatches: List<ColorSwatchOption>,
    selectedHex: String,
    onSelect: (String) -> Unit
) {
    val colors = LocalMNotesColors.current
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(
            text = title,
            color = colors.textMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            swatches.forEach { sw ->
                val active = selectedHex.equals(sw.hex, ignoreCase = true)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (active) colors.primarySoft else colors.cardBg)
                        .border(
                            if (active) 1.5.dp else 1.dp,
                            if (active) colors.primary else colors.borderSoft,
                            RoundedCornerShape(999.dp)
                        )
                        .clickable { onSelect(sw.hex) }
                        .padding(horizontal = 9.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(sw.color)
                            .border(0.8.dp, Color(0x440F172A), CircleShape)
                    )
                    Text(
                        text = if (lang == "en") sw.labelEn else sw.labelAr,
                        color = if (active) colors.primary else colors.textDark,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniStudioPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalMNotesColors.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) colors.primary else colors.cardBg)
            .border(1.dp, if (selected) colors.primary else colors.borderSoft, RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 9.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else colors.textMuted,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun StudioSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = LocalMNotesColors.current
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (checked) colors.primarySoft else colors.bgMain,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (checked) colors.primary else colors.borderSoft, RoundedCornerShape(14.dp))
            .clickable { onCheckedChange(!checked) }
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
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = colors.primary,
                    checkedThumbColor = Color.White
                )
            )
        }
    }
}
