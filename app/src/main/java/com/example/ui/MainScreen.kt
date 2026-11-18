package com.example.ui

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localization.AppStrings
import com.example.model.QuickRequest
import com.example.model.SmartTask
import com.example.ui.components.AlertOverlay
import com.example.ui.components.HeroBanner
import com.example.ui.components.PhotoCropSheet
import com.example.ui.components.PhotoPickerSheet
import com.example.ui.components.SettingsSheet
import com.example.ui.components.TaskCard
import com.example.ui.components.TaskEditorSheet
import com.example.ui.components.TopBarSwitcher
import com.example.ui.theme.LocalMNotesColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private sealed interface PhotoPickTarget {
    data object Banner : PhotoPickTarget
    data class Task(val taskId: String) : PhotoPickTarget
}

private sealed interface CropDestination {
    data object Banner : CropDestination
    data class TaskRow(val taskId: String) : CropDestination
    data object EditorDraft : CropDestination
    data class Library(val name: String) : CropDestination
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen(viewModel: MNotesViewModel) {
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    val tasksState by viewModel.tasksState.collectAsStateWithLifecycle()
    val requestsState by viewModel.requestsState.collectAsStateWithLifecycle()
    val libraryPhotos by viewModel.libraryPhotos.collectAsStateWithLifecycle()
    val trimmedRingtones by viewModel.trimmedRingtones.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val activeAlertTask by viewModel.activeAlertTask.collectAsStateWithLifecycle()
    val isBusyBackup by viewModel.isBusyBackup.collectAsStateWithLifecycle()

    val colors = LocalMNotesColors.current
    val lang = prefs.lang
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val allTasks = (tasksState as? UiState.Success)?.data ?: emptyList()
    val allRequests = (requestsState as? UiState.Success)?.data ?: emptyList()

    // Navigation & Modal States
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })
    var showSettings by remember { mutableStateOf(false) }
    var showTaskEditor by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<SmartTask?>(null) }
    var editorDraftImgUri by remember { mutableStateOf("") }

    var photoPickTarget by remember { mutableStateOf<PhotoPickTarget?>(null) }
    var cropSourceUri by remember { mutableStateOf<Uri?>(null) }
    var cropDestination by remember { mutableStateOf<CropDestination?>(null) }

    // Requests Tab Input States
    var reqTitle by remember { mutableStateOf("") }
    var reqBody by remember { mutableStateOf("") }

    // Observe transient toast messages
    LaunchedEffect(Unit) {
        viewModel.toastMessage.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    // Periodic schedule rollover & due-time alert check
    LaunchedEffect(allTasks) {
        viewModel.rollOverdueWeekdayTasks(allTasks)
        viewModel.checkDueAlerts(allTasks)
        while (isActive) {
            delay(4000L)
            viewModel.rollOverdueWeekdayTasks(allTasks)
            viewModel.checkDueAlerts(allTasks)
        }
    }

    val layoutDir = if (lang == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr
    CompositionLocalProvider(LocalLayoutDirection provides layoutDir) {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                containerColor = Color.Transparent,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(colors.bgTopGlow, colors.bgMain, colors.bgMain)
                        )
                    )
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .statusBarsPadding()
                        .navigationBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 600.dp)
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        // Top Brand Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = AppStrings.get(lang, "appName"),
                                color = colors.textDark,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.ExtraBold
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = colors.cardBg,
                                    shadowElevation = 2.dp,
                                    modifier = Modifier
                                        .size(42.dp)
                                        .border(1.dp, colors.borderSoft, CircleShape)
                                        .clip(CircleShape)
                                        .clickable {
                                            viewModel.playSound("enter")
                                            showSettings = true
                                        }
                                        .testTag("settings_button")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Rounded.Settings,
                                            contentDescription = AppStrings.get(lang, "settings"),
                                            tint = colors.textDark,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        viewModel.playSound("enter")
                                        editingTask = null
                                        editorDraftImgUri = ""
                                        showTaskEditor = true
                                    },
                                    shape = RoundedCornerShape(999.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colors.primary,
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .height(42.dp)
                                        .testTag("add_task_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = AppStrings.get(lang, "add"),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        // Top Bar Switcher (T1, T11, T13, T18, T20)
                        TopBarSwitcher(
                            styleId = prefs.topBarStyle,
                            currentPage = pagerState.currentPage,
                            lang = lang,
                            onSelectPage = { idx ->
                                viewModel.playSound("nav")
                                scope.launch { pagerState.animateScrollToPage(idx) }
                            }
                        )

                        Spacer(Modifier.height(12.dp))

                        // Two-page Swipeable Workspace
                        HorizontalPager(
                            state = pagerState,
                            userScrollEnabled = prefs.swipePages,
                            modifier = Modifier.weight(1f)
                        ) { page ->
                            if (page == 0) {
                                val today = AppStrings.todayStr()
                                val q = searchQuery.trim().lowercase()
                                val dueTasks = allTasks.filter { t ->
                                    (t.nextDueDate.isBlank() || t.nextDueDate <= today) &&
                                        (q.isEmpty() ||
                                            t.title.lowercase().contains(q) ||
                                            t.meta.lowercase().contains(q))
                                }
                                val dueTodayTotal = allTasks.count {
                                    it.nextDueDate.isBlank() || it.nextDueDate <= today
                                }

                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    HeroBanner(
                                        prefs = prefs,
                                        dueTodayCount = dueTodayTotal,
                                        onBannerPhotoClick = {
                                            viewModel.playSound("enter")
                                            photoPickTarget = PhotoPickTarget.Banner
                                        }
                                    )

                                    // Search Pill Bar
                                    OutlinedTextField(
                                        value = searchQuery,
                                        onValueChange = { viewModel.setSearchQuery(it) },
                                        placeholder = {
                                            Text(
                                                text = AppStrings.get(lang, "search"),
                                                color = colors.textMuted,
                                                fontSize = 13.sp
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Rounded.Search,
                                                contentDescription = null,
                                                tint = colors.textMuted
                                            )
                                        },
                                        trailingIcon = {
                                            if (searchQuery.isNotEmpty()) {
                                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Close,
                                                        contentDescription = AppStrings.get(lang, "close"),
                                                        tint = colors.textMuted
                                                    )
                                                }
                                            }
                                        },
                                        singleLine = true,
                                        shape = RoundedCornerShape(999.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = colors.primary,
                                            unfocusedBorderColor = colors.borderSoft,
                                            focusedContainerColor = colors.cardBg,
                                            unfocusedContainerColor = colors.cardBg
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("task_search_input")
                                    )

                                    // Today's Tasks Card
                                    Surface(
                                        shape = RoundedCornerShape(22.dp),
                                        color = colors.cardBg,
                                        shadowElevation = 2.dp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, colors.borderSoft, RoundedCornerShape(22.dp))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(14.dp),
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = AppStrings.get(lang, "curTasks"),
                                                    color = colors.textDark,
                                                    fontSize = 14.5.sp,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(999.dp))
                                                        .background(colors.primarySoft)
                                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                                ) {
                                                    Text(
                                                        text = "${dueTasks.size} ${AppStrings.get(lang, "forToday")}",
                                                        color = colors.primary,
                                                        fontSize = 11.5.sp,
                                                        fontWeight = FontWeight.ExtraBold
                                                    )
                                                }
                                            }

                                            when (val state = tasksState) {
                                                is UiState.Loading -> {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(24.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        CircularProgressIndicator(color = colors.primary)
                                                    }
                                                }
                                                is UiState.Error -> {
                                                    Text(
                                                        text = state.message,
                                                        color = Color(0xFFEF4444),
                                                        fontSize = 12.5.sp,
                                                        textAlign = TextAlign.Center,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(12.dp)
                                                    )
                                                }
                                                is UiState.Success -> {
                                                    if (dueTasks.isEmpty()) {
                                                        Text(
                                                            text = AppStrings.get(lang, "allDone"),
                                                            color = colors.textMuted,
                                                            fontSize = 12.5.sp,
                                                            textAlign = TextAlign.Center,
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(vertical = 22.dp)
                                                        )
                                                    } else {
                                                        dueTasks.forEach { task ->
                                                            TaskCard(
                                                                task = task,
                                                                lang = lang,
                                                                cardShape = prefs.cardShape,
                                                                taskImgSize = prefs.taskImgSize,
                                                                onPhotoClick = {
                                                                    viewModel.playSound("enter")
                                                                    photoPickTarget = PhotoPickTarget.Task(task.id)
                                                                },
                                                                onEditClick = {
                                                                    viewModel.playSound("enter")
                                                                    editingTask = task
                                                                    editorDraftImgUri = task.imgUri
                                                                    showTaskEditor = true
                                                                },
                                                                onDeleteClick = {
                                                                    viewModel.deleteTask(task.id)
                                                                }
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    Spacer(Modifier.height(14.dp))
                                }
                            } else {
                                // Page 1: Requests
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Create Request Card
                                    Surface(
                                        shape = RoundedCornerShape(22.dp),
                                        color = colors.cardBg,
                                        shadowElevation = 2.dp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, colors.borderSoft, RoundedCornerShape(22.dp))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(14.dp),
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = AppStrings.get(lang, "reqHead"),
                                                    color = colors.textDark,
                                                    fontSize = 14.5.sp,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(999.dp))
                                                        .background(colors.primarySoft)
                                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                                ) {
                                                    Text(
                                                        text = "${allRequests.size} ${AppStrings.get(lang, "reqSaved")}",
                                                        color = colors.primary,
                                                        fontSize = 11.5.sp,
                                                        fontWeight = FontWeight.ExtraBold
                                                    )
                                                }
                                            }

                                            OutlinedTextField(
                                                value = reqTitle,
                                                onValueChange = { reqTitle = it },
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

                                            OutlinedTextField(
                                                value = reqBody,
                                                onValueChange = { reqBody = it },
                                                placeholder = {
                                                    Text(AppStrings.get(lang, "reqBodyPh"), color = colors.textMuted, fontSize = 13.sp)
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

                                            Button(
                                                onClick = {
                                                    if (reqTitle.trim().isNotEmpty() || reqBody.trim().isNotEmpty()) {
                                                        viewModel.saveQuickRequest(reqTitle, reqBody)
                                                        reqTitle = ""
                                                        reqBody = ""
                                                    }
                                                },
                                                enabled = reqTitle.trim().isNotEmpty() || reqBody.trim().isNotEmpty(),
                                                shape = RoundedCornerShape(14.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(48.dp)
                                                    .testTag("save_request_button")
                                            ) {
                                                Text(
                                                    text = AppStrings.get(lang, "saveReq"),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.5.sp
                                                )
                                            }
                                        }
                                    }

                                    // Saved Requests Card
                                    Surface(
                                        shape = RoundedCornerShape(22.dp),
                                        color = colors.cardBg,
                                        shadowElevation = 2.dp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, colors.borderSoft, RoundedCornerShape(22.dp))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(14.dp),
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = AppStrings.get(lang, "reqTab"),
                                                    color = colors.textDark,
                                                    fontSize = 14.5.sp,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                                Text(
                                                    text = "${allRequests.size}",
                                                    color = colors.textMuted,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
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
                                                    Text(
                                                        text = rState.message,
                                                        color = Color(0xFFEF4444),
                                                        fontSize = 12.5.sp
                                                    )
                                                }
                                                is UiState.Success -> {
                                                    if (allRequests.isEmpty()) {
                                                        Text(
                                                            text = AppStrings.get(lang, "noReq"),
                                                            color = colors.textMuted,
                                                            fontSize = 12.5.sp,
                                                            textAlign = TextAlign.Center,
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(vertical = 20.dp)
                                                        )
                                                    } else {
                                                        allRequests.forEach { req: QuickRequest ->
                                                            Surface(
                                                                shape = RoundedCornerShape(16.dp),
                                                                color = colors.bgMain,
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .border(1.dp, colors.borderSoft, RoundedCornerShape(16.dp))
                                                            ) {
                                                                Row(
                                                                    modifier = Modifier
                                                                        .fillMaxWidth()
                                                                        .padding(12.dp),
                                                                    verticalAlignment = Alignment.CenterVertically
                                                                ) {
                                                                    Column(modifier = Modifier.weight(1f)) {
                                                                        Text(
                                                                            text = req.title,
                                                                            color = colors.textDark,
                                                                            fontSize = 13.5.sp,
                                                                            fontWeight = FontWeight.ExtraBold
                                                                        )
                                                                        if (req.body.isNotBlank()) {
                                                                            Spacer(Modifier.height(2.dp))
                                                                            Text(
                                                                                text = req.body,
                                                                                color = colors.textMuted,
                                                                                fontSize = 12.sp
                                                                            )
                                                                        }
                                                                    }
                                                                    Spacer(Modifier.width(8.dp))
                                                                    Box(
                                                                        modifier = Modifier
                                                                            .size(34.dp)
                                                                            .clip(CircleShape)
                                                                            .background(colors.cardBg)
                                                                            .border(1.dp, Color(0xFFFEE2E2), CircleShape)
                                                                            .clickable {
                                                                                viewModel.deleteQuickRequest(req.id)
                                                                            }
                                                                            .testTag("delete_request_${req.id}"),
                                                                        contentAlignment = Alignment.Center
                                                                    ) {
                                                                        Icon(
                                                                            imageVector = Icons.Rounded.DeleteOutline,
                                                                            contentDescription = "Delete",
                                                                            tint = Color(0xFFEF4444),
                                                                            modifier = Modifier.size(16.dp)
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    Spacer(Modifier.height(14.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Task Editor Modal Sheet
            if (showTaskEditor) {
                TaskEditorSheet(
                    editingTask = editingTask,
                    draftImgUri = editorDraftImgUri,
                    lang = lang,
                    libraryPhotos = libraryPhotos,
                    onPickAndCropPhoto = { uri ->
                        cropDestination = CropDestination.EditorDraft
                        cropSourceUri = uri
                    },
                    onSelectDraftImg = { path ->
                        viewModel.playSound("nav")
                        editorDraftImgUri = path
                    },
                    onSave = { title, meta, startDate, alertTime, repeatMode, cycleRaw, weekDays, alertEnabled, imgUri, testAlert ->
                        viewModel.saveTask(
                            editingTask = editingTask,
                            title = title,
                            metaInput = meta,
                            startDate = startDate,
                            alertTime = alertTime,
                            repeatMode = repeatMode,
                            cycleStepsRaw = cycleRaw,
                            weekDays = weekDays,
                            alertEnabled = alertEnabled,
                            imgUri = imgUri,
                            triggerAlertAfterSave = testAlert
                        )
                        showTaskEditor = false
                    },
                    onDismiss = {
                        viewModel.playSound("exit")
                        showTaskEditor = false
                    }
                )
            }

            // Photo Picker Sheet
            val currentPhotoTarget = photoPickTarget
            if (currentPhotoTarget != null) {
                PhotoPickerSheet(
                    title = AppStrings.get(
                        lang,
                        if (currentPhotoTarget is PhotoPickTarget.Banner) "pickImgBan" else "pickImgTask"
                    ),
                    lang = lang,
                    libraryPhotos = libraryPhotos,
                    onPickFromDevice = { uri ->
                        cropDestination = when (currentPhotoTarget) {
                            is PhotoPickTarget.Banner -> CropDestination.Banner
                            is PhotoPickTarget.Task -> CropDestination.TaskRow(currentPhotoTarget.taskId)
                        }
                        photoPickTarget = null
                        cropSourceUri = uri
                    },
                    onSelectFromLibrary = { path ->
                        when (currentPhotoTarget) {
                            is PhotoPickTarget.Banner -> {
                                viewModel.playSound("done")
                                viewModel.updatePreference(mapOf("bannerImg" to path))
                            }
                            is PhotoPickTarget.Task -> {
                                viewModel.updateTaskPhoto(currentPhotoTarget.taskId, path)
                            }
                        }
                        photoPickTarget = null
                    },
                    onDismiss = {
                        viewModel.playSound("exit")
                        photoPickTarget = null
                    }
                )
            }

            // Interactive Photo Crop Sheet
            val currentCropUri = cropSourceUri
            val currentCropDest = cropDestination
            if (currentCropUri != null && currentCropDest != null) {
                PhotoCropSheet(
                    sourceUri = currentCropUri,
                    lang = lang,
                    onCropConfirmed = { offsetXNorm, offsetYNorm, scale, rotationDeg ->
                        val res = viewModel.cropAndSavePhoto(
                            uri = currentCropUri,
                            offsetXNorm = offsetXNorm,
                            offsetYNorm = offsetYNorm,
                            scale = scale,
                            rotationDeg = rotationDeg
                        )
                        val savedPath = res.getOrNull()
                        if (savedPath != null) {
                            viewModel.playSound("done")
                            when (currentCropDest) {
                                is CropDestination.Banner -> {
                                    viewModel.updatePreference(mapOf("bannerImg" to savedPath))
                                }
                                is CropDestination.TaskRow -> {
                                    viewModel.updateTaskPhoto(currentCropDest.taskId, savedPath)
                                }
                                is CropDestination.EditorDraft -> {
                                    editorDraftImgUri = savedPath
                                    viewModel.addPhotoToLibrary(
                                        if (lang == "en") "Task Photo" else "صورة مهمة",
                                        savedPath
                                    )
                                }
                                is CropDestination.Library -> {
                                    viewModel.addPhotoToLibrary(currentCropDest.name, savedPath)
                                }
                            }
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar(AppStrings.get(lang, "invalidFile"))
                            }
                        }
                        cropSourceUri = null
                        cropDestination = null
                    },
                    onDismiss = {
                        viewModel.playSound("exit")
                        cropSourceUri = null
                        cropDestination = null
                    }
                )
            }

            // Settings Sheet
            if (showSettings) {
                SettingsSheet(
                    prefs = prefs,
                    tasks = allTasks,
                    libraryPhotos = libraryPhotos,
                    trimmedRingtones = trimmedRingtones,
                    isBusyBackup = isBusyBackup,
                    viewModel = viewModel,
                    onPickBannerPhoto = {
                        photoPickTarget = PhotoPickTarget.Banner
                    },
                    onAddLibraryPhotoUri = { uri, name ->
                        cropDestination = CropDestination.Library(name)
                        cropSourceUri = uri
                    },
                    onShowToast = { msg ->
                        scope.launch { snackbarHostState.showSnackbar(msg) }
                    },
                    onDismiss = {
                        viewModel.playSound("exit")
                        showSettings = false
                    }
                )
            }

            // Highest Z-Order: Due-Time Task Alert Overlay (A1..A6)
            val alertTask = activeAlertTask
            if (alertTask != null) {
                AlertOverlay(
                    task = alertTask,
                    alertStyle = prefs.alertStyle,
                    lang = lang,
                    onComplete = { viewModel.completeActiveAlert() },
                    onDismiss = { viewModel.dismissAlert() }
                )
            }
        }
    }
}
