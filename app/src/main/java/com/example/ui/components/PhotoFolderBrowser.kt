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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessibilityNew
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.SportsGymnastics
import androidx.compose.material.icons.rounded.SportsMartialArts
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MNotesCatalog
import com.example.ui.theme.LocalMNotesColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private fun iconForCategory(categoryId: String): ImageVector = when (categoryId) {
    "chest" -> Icons.Rounded.FitnessCenter
    "back" -> Icons.Rounded.AccessibilityNew
    "legs" -> Icons.Rounded.DirectionsWalk
    "cardio" -> Icons.Rounded.DirectionsRun
    "abs" -> Icons.Rounded.SportsGymnastics
    "shoulders_arms" -> Icons.Rounded.SportsMartialArts
    "recovery" -> Icons.Rounded.SelfImprovement
    "gym" -> Icons.Rounded.FitnessCenter
    "fruits" -> Icons.Rounded.Spa
    "vegetables" -> Icons.Rounded.Eco
    "heritage" -> Icons.Rounded.Face
    else -> Icons.Rounded.FolderOpen
}

@Composable
fun CategorizedBuiltInPhotosBrowser(
    lang: String,
    selectedUri: String,
    columns: Int = 3,
    defaultCategory: String = "chest",
    showLabels: Boolean = true,
    onSelectUri: (String) -> Unit
) {
    val context = LocalContext.current
    val colors = LocalMNotesColors.current
    var activeCategory by remember { mutableStateOf(defaultCategory) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredPhotos = remember(activeCategory, searchQuery) {
        val q = searchQuery.trim().lowercase()
        val base = if (q.isNotEmpty()) {
            MNotesCatalog.pixel3dPhotos
        } else {
            MNotesCatalog.pixel3dPhotos.filter { it.matchesCategory(activeCategory) }
        }
        if (q.isEmpty()) base
        else base.filter {
            it.nameAr.lowercase().contains(q) ||
                it.nameEn.lowercase().contains(q) ||
                it.category.lowercase().contains(q)
        }
    }

    // Background prewarm of active category's 3x3 sheets so tiles render instantaneously with zero lag
    LaunchedEffect(activeCategory) {
        val uris = MNotesCatalog.pixel3dPhotos
            .filter { it.matchesCategory(activeCategory) }
            .map { it.uri }
            .distinctBy { it.substringBefore('#') }
        withContext(Dispatchers.IO) {
            uris.forEach { uri -> GridTileBitmapCache.prewarmUri(context, uri) }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Quick Exercise & Photo Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            singleLine = true,
            placeholder = {
                Text(
                    text = if (lang == "en") {
                        "Search exercises (bench, squat, plank, cardio...)"
                    } else {
                        "ابحث عن تمرين (صدر، سكوات، بلانك، جري، ظهر...)"
                    },
                    fontSize = 11.5.sp,
                    color = colors.textMuted
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = if (lang == "en") "Clear search" else "مسح البحث",
                        tint = colors.textMuted,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { searchQuery = "" }
                    )
                }
            } else null,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.primary,
                unfocusedBorderColor = colors.borderSoft,
                focusedContainerColor = colors.bgMain,
                unfocusedContainerColor = colors.bgMain
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("photo_library_search_input")
        )

        // Category Folder Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            MNotesCatalog.photoCategories.forEach { cat ->
                val active = searchQuery.isBlank() && activeCategory == cat.id
                val count = remember(cat.id) {
                    MNotesCatalog.pixel3dPhotos.count { it.matchesCategory(cat.id) }
                }
                val title = if (lang == "en") cat.nameEn else cat.nameAr
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (active) colors.primary else colors.bgMain,
                    modifier = Modifier
                        .border(
                            1.dp,
                            if (active) colors.primary else colors.borderSoft,
                            RoundedCornerShape(14.dp)
                        )
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            searchQuery = ""
                            activeCategory = cat.id
                        }
                        .testTag("photo_cat_${cat.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = iconForCategory(cat.id),
                            contentDescription = null,
                            tint = if (active) Color.White else colors.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = title,
                            color = if (active) Color.White else colors.textDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1
                        )
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(
                                    if (active) Color.White.copy(alpha = 0.24f) else colors.primarySoft
                                )
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "$count",
                                color = if (active) Color.White else colors.primary,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }

        if (filteredPhotos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (lang == "en") "No matching exercises found" else "لا توجد صور مطابقة للبحث",
                    color = colors.textMuted,
                    fontSize = 12.sp
                )
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                filteredPhotos.chunked(columns.coerceIn(2, 4)).forEach { rowItems ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        rowItems.forEach { item ->
                            val selected = selectedUri == item.uri
                            val label = if (lang == "en") item.nameEn else item.nameAr
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (selected) colors.primarySoft else colors.bgMain,
                                modifier = Modifier
                                    .weight(1f)
                                    .border(
                                        width = if (selected) 2.2.dp else 1.dp,
                                        color = if (selected) colors.primary else colors.borderSoft,
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { onSelectUri(item.uri) }
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.86f)
                                            .aspectRatio(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape)
                                                .border(
                                                    1.5.dp,
                                                    if (selected) colors.primary else colors.borderSoft,
                                                    CircleShape
                                                )
                                                .padding(2.dp)
                                        ) {
                                            MNotesPhotoContent(
                                                imgUri = item.uri,
                                                useBannerDefaultIfBlank = false,
                                                contentDescription = label,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(CircleShape)
                                            )
                                        }
                                        if (selected) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .size(20.dp)
                                                    .clip(CircleShape)
                                                    .background(colors.primary)
                                                    .border(1.5.dp, Color.White, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Check,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                    }
                                    if (showLabels) {
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = label,
                                            color = if (selected) colors.primary else colors.textDark,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                        repeat((columns.coerceIn(2, 4)) - rowItems.size) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
