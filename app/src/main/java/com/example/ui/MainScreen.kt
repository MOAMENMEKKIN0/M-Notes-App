package com.example.ui

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localization.AppStrings
import com.example.model.ScreenDesignPresets
import com.example.model.SmartTask
import com.example.notification.TaskAlarmScheduler
import com.example.ui.components.AlertOverlay
import com.example.ui.components.CustomScreenThemeWorkspace
import com.example.ui.components.FloatingBadgeLibrarySheet
import com.example.ui.components.FloatingCharacterBadgesOverlay
import com.example.ui.components.HomeQuickOverviewSheet
import com.example.ui.components.PhotoCropSheet
import com.example.ui.components.PhotoPickerSheet
import com.example.ui.components.RequestsWorkspace
import com.example.ui.components.ScreenDesignBackdrop
import com.example.ui.components.SettingsSheet
import com.example.ui.components.TaskBadgeLibrarySheet
import com.example.ui.components.TaskEditorSheet
import com.example.ui.components.TasksWorkspaceRouter
import com.example.ui.components.TopBarSwitcher
import com.example.ui.theme.LocalMNotesColors
import com.example.ui.theme.LocalMNotesTextScale
import com.example.ui.theme.motionStiffnessForHz
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
    val floatingBadges by viewModel.floatingBadges.collectAsStateWithLifecycle()
    val smartIconSettings by viewModel.smartIconSettings.collectAsStateWithLifecycle()
    val screenDesignState by viewModel.screenDesignState.collectAsStateWithLifecycle()

    val activeScreenDesignConfig = remember(screenDesignState) {
        if (screenDesignState.activeDesignId == ScreenDesignPresets.ID_CLASSIC) {
            null
        } else {
            screenDesignState.activeConfig
        }
    }

    val context = LocalContext.current
    val colors = LocalMNotesColors.current
    val textScale = LocalMNotesTextScale.current
    val lang = prefs.lang
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            viewModel.showMessage(
                if (lang == "en") {
                    "Enable notifications in Settings so reminders work when the app is closed."
                } else {
                    "فعّل إذن الإشعارات من الإعدادات لكي تصلك التذكيرات عند إغلاق التطبيق وقفل الشاشة."
                }
            )
        }
    }

    val allTasks = (tasksState as? UiState.Success)?.data ?: emptyList()
    val allRequests = (requestsState as? UiState.Success)?.data ?: emptyList()

    // Navigation & Modal States
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })
    var showSettings by remember { mutableStateOf(false) }
    var initialSettingsFolder by remember { mutableStateOf<String?>(null) }
    var showHomeOverview by remember { mutableStateOf(false) }
    var overviewDayFilter by remember { mutableStateOf<Int?>(null) }
    var showTaskEditor by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<SmartTask?>(null) }
    var editorDraftImgUri by remember { mutableStateOf("") }

    var photoPickTarget by remember { mutableStateOf<PhotoPickTarget?>(null) }
    var cropSourceUri by remember { mutableStateOf<Uri?>(null) }
    var cropDestination by remember { mutableStateOf<CropDestination?>(null) }
    var libraryTargetBadgeId by remember { mutableStateOf<String?>(null) }
    var taskBadgeLibraryTaskId by remember { mutableStateOf<String?>(null) }

    // Request Android 13+ notification permission once if user has active reminders
    var promptedNotifPermission by remember { mutableStateOf(false) }
    LaunchedEffect(allTasks.size) {
        if (!promptedNotifPermission &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            allTasks.any { it.alertEnabled && it.alertTime.isNotBlank() } &&
            !TaskAlarmScheduler.hasNotificationPermission(context)
        ) {
            promptedNotifPermission = true
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

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
            if (activeScreenDesignConfig != null) {
                ScreenDesignBackdrop(
                    config = activeScreenDesignConfig,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Scaffold(
                containerColor = Color.Transparent,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (activeScreenDesignConfig == null) {
                            Modifier.background(
                                Brush.verticalGradient(
                                    colors = listOf(colors.bgTopGlow, colors.bgMain, colors.bgMain)
                                )
                            )
                        } else {
                            Modifier
                        }
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
                                fontSize = (21f * textScale).sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        viewModel.playSound("enter")
                                        initialSettingsFolder = "homeLayout"
                                        showSettings = true
                                    }
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
                                            initialSettingsFolder = null
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

                        if (prefs.showTopBarTabs) {
                            Spacer(Modifier.height(10.dp))

                            // Top Bar Switcher (T1, T11, T13, T18, T20)
                            TopBarSwitcher(
                                styleId = prefs.topBarStyle,
                                currentPage = pagerState.currentPage,
                                lang = lang,
                                onSelectPage = { idx ->
                                    viewModel.playSound("nav")
                                    scope.launch {
                                        pagerState.animateScrollToPage(
                                            page = idx,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioNoBouncy,
                                                stiffness = motionStiffnessForHz(prefs.refreshRateHz)
                                            )
                                        )
                                    }
                                }
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        // Two-page Swipeable Workspace
                        HorizontalPager(
                            state = pagerState,
                            userScrollEnabled = prefs.swipePages,
                            modifier = Modifier.weight(1f)
                        ) { page ->
                            if (page == 0) {
                                if (activeScreenDesignConfig != null) {
                                    CustomScreenThemeWorkspace(
                                        config = activeScreenDesignConfig,
                                        allTasks = allTasks,
                                        smartIconSettings = smartIconSettings,
                                        requestsCount = allRequests.size,
                                        lang = lang,
                                        searchQuery = searchQuery,
                                        onSearchChange = { viewModel.setSearchQuery(it) },
                                        onAddTaskClick = {
                                            viewModel.playSound("enter")
                                            editingTask = null
                                            editorDraftImgUri = ""
                                            showTaskEditor = true
                                        },
                                        onTaskPhotoClick = { taskId ->
                                            viewModel.playSound("enter")
                                            photoPickTarget = PhotoPickTarget.Task(taskId)
                                        },
                                        onTaskEditClick = { task ->
                                            viewModel.playSound("enter")
                                            editingTask = task
                                            editorDraftImgUri = task.imgUri
                                            showTaskEditor = true
                                        },
                                        onTaskDeleteClick = { taskId ->
                                            viewModel.deleteTask(taskId)
                                        },
                                        onTaskBadgeTap = { tappedTask ->
                                            viewModel.playSound("enter")
                                            taskBadgeLibraryTaskId = tappedTask.id
                                        },
                                        onTaskBadgeDrag = { taskId, offsetX, offsetY ->
                                            viewModel.updateTaskBadgePosition(taskId, offsetX, offsetY)
                                        },
                                        onPreviewTaskAlert = { task ->
                                            viewModel.showAlert(task, previewOnly = true)
                                        },
                                        onOpenOverview = { dayIdx ->
                                            viewModel.playSound("enter")
                                            overviewDayFilter = dayIdx
                                            showHomeOverview = true
                                        },
                                        onOpenRequestsPage = {
                                            viewModel.playSound("nav")
                                            scope.launch {
                                                pagerState.animateScrollToPage(
                                                    page = 1,
                                                    animationSpec = spring(
                                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                                        stiffness = motionStiffnessForHz(prefs.refreshRateHz)
                                                    )
                                                )
                                            }
                                        },
                                        onOpenSettings = {
                                            viewModel.playSound("enter")
                                            initialSettingsFolder = "screenDesigns"
                                            showSettings = true
                                        },
                                        isPreview = false,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    TasksWorkspaceRouter(
                                        prefs = prefs,
                                        allTasks = allTasks,
                                        smartIconSettings = smartIconSettings,
                                        requestsCount = allRequests.size,
                                        tasksState = tasksState,
                                        searchQuery = searchQuery,
                                        onSearchChange = { viewModel.setSearchQuery(it) },
                                        onBannerPhotoClick = {
                                            viewModel.playSound("enter")
                                            photoPickTarget = PhotoPickTarget.Banner
                                        },
                                        onBannerBodyClick = {
                                            viewModel.playSound("enter")
                                            initialSettingsFolder = "homeLayout"
                                            showSettings = true
                                        },
                                        onAddTaskClick = {
                                            viewModel.playSound("enter")
                                            editingTask = null
                                            editorDraftImgUri = ""
                                            showTaskEditor = true
                                        },
                                        onTaskPhotoClick = { taskId ->
                                            viewModel.playSound("enter")
                                            photoPickTarget = PhotoPickTarget.Task(taskId)
                                        },
                                        onTaskEditClick = { task ->
                                            viewModel.playSound("enter")
                                            editingTask = task
                                            editorDraftImgUri = task.imgUri
                                            showTaskEditor = true
                                        },
                                        onTaskDeleteClick = { taskId ->
                                            viewModel.deleteTask(taskId)
                                        },
                                        onTaskBadgeTap = { tappedTask ->
                                            viewModel.playSound("enter")
                                            taskBadgeLibraryTaskId = tappedTask.id
                                        },
                                        onTaskBadgeDrag = { taskId, offsetX, offsetY ->
                                            viewModel.updateTaskBadgePosition(taskId, offsetX, offsetY)
                                        },
                                        onPreviewTaskAlert = { task ->
                                            viewModel.showAlert(task, previewOnly = true)
                                        },
                                        onOpenOverview = { dayIdx ->
                                            viewModel.playSound("enter")
                                            overviewDayFilter = dayIdx
                                            showHomeOverview = true
                                        },
                                        onOpenRequestsPage = {
                                            viewModel.playSound("nav")
                                            scope.launch {
                                                pagerState.animateScrollToPage(
                                                    page = 1,
                                                    animationSpec = spring(
                                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                                        stiffness = motionStiffnessForHz(prefs.refreshRateHz)
                                                    )
                                                )
                                            }
                                        }
                                    )
                                }
                            } else {
                                // Page 1: Rich Requests, Invoices, Cheques, Cards & Orders
                                RequestsWorkspace(
                                    prefs = prefs,
                                    allRequests = allRequests,
                                    requestsState = requestsState,
                                    viewModel = viewModel,
                                    onOpenRequestsSettings = {
                                        initialSettingsFolder = "requests"
                                        showSettings = true
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Independent Draggable, Resizable, Tappable Small Floating Circular Icon(s)
            FloatingCharacterBadgesOverlay(
                badges = floatingBadges,
                lang = lang,
                onTapBadge = { tappedBadge ->
                    viewModel.playSound("enter")
                    libraryTargetBadgeId = tappedBadge.id
                },
                onUpdatePosition = { id, x, y -> viewModel.updateFloatingBadgePosition(id, x, y) }
            )

            // Dedicated 110+ Icon Selection Library Sheet (opened ONLY by tapping a small floating icon)
            val activeLibraryBadge = libraryTargetBadgeId?.let { targetId ->
                floatingBadges.firstOrNull { it.id == targetId }
            }
            if (activeLibraryBadge != null) {
                FloatingBadgeLibrarySheet(
                    badge = activeLibraryBadge,
                    lang = lang,
                    onSelectIcon = { iconId ->
                        viewModel.updateFloatingBadgeState(activeLibraryBadge.id, iconId)
                    },
                    onUpdateSize = { newSize ->
                        viewModel.updateFloatingBadgeSize(activeLibraryBadge.id, newSize)
                    },
                    onDuplicate = {
                        val dupId = viewModel.duplicateFloatingBadge(activeLibraryBadge.id)
                        if (dupId != null) libraryTargetBadgeId = dupId
                    },
                    onToggleLock = {
                        viewModel.toggleFloatingBadgeLock(activeLibraryBadge.id)
                    },
                    onHide = {
                        viewModel.toggleFloatingBadgeVisibility(activeLibraryBadge.id)
                    },
                    onDelete = {
                        viewModel.deleteFloatingBadge(activeLibraryBadge.id)
                    },
                    onOpenFullStudio = {
                        viewModel.playSound("enter")
                        initialSettingsFolder = "characterBadge"
                        showSettings = true
                    },
                    onDismiss = {
                        viewModel.playSound("exit")
                        libraryTargetBadgeId = null
                    }
                )
            }

            // Dedicated Task Circle Floating Icon Library Sheet (opened when tapping a task circle's small icon)
            val activeTaskBadgeTarget = taskBadgeLibraryTaskId?.let { targetId ->
                allTasks.firstOrNull { it.id == targetId }
            }
            if (activeTaskBadgeTarget != null) {
                TaskBadgeLibrarySheet(
                    task = activeTaskBadgeTarget,
                    smartIconSettings = smartIconSettings,
                    lang = lang,
                    onApply = { iconId, sizeDp, offsetX, offsetY, category, smartMode, locked ->
                        viewModel.applyTaskBadge(
                            taskId = activeTaskBadgeTarget.id,
                            iconId = iconId,
                            offsetX = offsetX,
                            offsetY = offsetY,
                            sizeDp = sizeDp,
                            visible = true,
                            category = category,
                            smartMode = smartMode,
                            locked = locked
                        )
                        taskBadgeLibraryTaskId = null
                    },
                    onHide = {
                        viewModel.toggleTaskBadgeVisibility(activeTaskBadgeTarget.id)
                    },
                    onRemove = {
                        viewModel.removeTaskBadge(activeTaskBadgeTarget.id)
                    },
                    onOpenFullStudio = {
                        viewModel.playSound("enter")
                        initialSettingsFolder = "characterBadge"
                        showSettings = true
                    },
                    onDismiss = {
                        viewModel.playSound("exit")
                        taskBadgeLibraryTaskId = null
                    }
                )
            }

            // Task Editor Modal Sheet
            if (showTaskEditor) {
                TaskEditorSheet(
                    editingTask = editingTask,
                    draftImgUri = editorDraftImgUri,
                    lang = lang,
                    libraryPhotos = libraryPhotos,
                    trimmedRingtones = trimmedRingtones,
                    smartIconSettings = smartIconSettings,
                    onPreviewRingtone = { ringId -> viewModel.previewRingtoneId(ringId) },
                    onPickAndCropPhoto = { uri ->
                        cropDestination = CropDestination.EditorDraft
                        cropSourceUri = uri
                    },
                    onSelectDraftImg = { path ->
                        viewModel.playSound("nav")
                        editorDraftImgUri = path
                    },
                    onSave = { title, meta, startDate, alertTime, repeatMode, cycleRaw, weekDays, alertEnabled, imgUri, ringId, testAlert, badgeIconId, badgeVisible, badgeSizeDp, badgeCategory, badgeSmartMode ->
                        if (alertEnabled && alertTime.isNotBlank() &&
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            !TaskAlarmScheduler.hasNotificationPermission(context)
                        ) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
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
                            ringId = ringId,
                            triggerAlertAfterSave = testAlert,
                            badgeIconId = badgeIconId,
                            badgeVisible = badgeVisible,
                            badgeSizeDp = badgeSizeDp,
                            badgeCategory = badgeCategory,
                            badgeSmartMode = badgeSmartMode
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
                val activeTargetImgUri = when (currentPhotoTarget) {
                    is PhotoPickTarget.Banner -> prefs.bannerImg
                    is PhotoPickTarget.Task -> allTasks.firstOrNull { it.id == currentPhotoTarget.taskId }?.imgUri.orEmpty()
                }
                PhotoPickerSheet(
                    title = AppStrings.get(
                        lang,
                        if (currentPhotoTarget is PhotoPickTarget.Banner) "pickImgBan" else "pickImgTask"
                    ),
                    lang = lang,
                    libraryPhotos = libraryPhotos,
                    currentImgUri = activeTargetImgUri,
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

            // Quick Home Overview Sheet (opened by Progress Circle, Day Strip, Countdown, etc.)
            if (showHomeOverview) {
                val dueTodayCount = allTasks.count { it.isActiveOnDate() }
                HomeQuickOverviewSheet(
                    lang = lang,
                    dueTodayCount = dueTodayCount,
                    allTasks = allTasks,
                    selectedDayFilter = overviewDayFilter,
                    requestsCount = allRequests.size,
                    onEditTask = { task ->
                        editingTask = task
                        editorDraftImgUri = task.imgUri
                        showTaskEditor = true
                    },
                    onPreviewAlert = { task ->
                        viewModel.showAlert(task, previewOnly = true)
                    },
                    onAddTask = {
                        editingTask = null
                        editorDraftImgUri = ""
                        showTaskEditor = true
                    },
                    onOpenRequestsPage = {
                        viewModel.playSound("nav")
                        scope.launch {
                            pagerState.animateScrollToPage(
                                page = 1,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = motionStiffnessForHz(prefs.refreshRateHz)
                                )
                            )
                        }
                    },
                    onOpenHomeSettings = {
                        initialSettingsFolder = "homeLayout"
                        showSettings = true
                    },
                    onDismiss = {
                        viewModel.playSound("exit")
                        showHomeOverview = false
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
                    initialFolder = initialSettingsFolder,
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
                    alertImgDp = prefs.alertImgDp,
                    lang = lang,
                    reminderBgThemeId = prefs.reminderBgThemeId,
                    reminderDoodlePackId = prefs.reminderDoodlePackId,
                    reminderDoodleOpacity = prefs.reminderDoodleOpacity,
                    onComplete = { viewModel.completeActiveAlert() },
                    onDismiss = { viewModel.dismissAlert() }
                )
            }
        }
    }
}
