package com.example.ui

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localization.AppStrings
import com.example.model.SmartTask
import com.example.notification.TaskAlarmScheduler
import com.example.ui.components.AlertOverlay
import com.example.ui.components.FloatingBadgeLibrarySheet
import com.example.ui.components.FloatingCharacterBadgesOverlay
import com.example.ui.components.HomeQuickOverviewSheet
import com.example.ui.components.MainScreenWallpaperBackdrop
import com.example.ui.components.PhotoCropSheet
import com.example.ui.components.PhotoPickerSheet
import com.example.ui.components.SettingsSheet
import com.example.ui.components.TaskBadgeLibrarySheet
import com.example.ui.components.TaskEditorSheet
import com.example.ui.components.TasksWorkspaceRouter
import com.example.ui.components.toggleFloatingIconSelection
import com.example.ui.theme.LocalMNotesColors
import com.example.ui.theme.LocalMNotesTextScale
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

@Composable
fun MainScreen(viewModel: MNotesViewModel) {
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    val tasksState by viewModel.tasksState.collectAsStateWithLifecycle()
    val libraryPhotos by viewModel.libraryPhotos.collectAsStateWithLifecycle()
    val trimmedRingtones by viewModel.trimmedRingtones.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val activeAlertTask by viewModel.activeAlertTask.collectAsStateWithLifecycle()
    val isBusyBackup by viewModel.isBusyBackup.collectAsStateWithLifecycle()
    val floatingBadges by viewModel.floatingBadges.collectAsStateWithLifecycle()
    val smartIconSettings by viewModel.smartIconSettings.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val windowInfo = LocalWindowInfo.current
    val colors = LocalMNotesColors.current
    val textScale = LocalMNotesTextScale.current
    val lang = prefs.lang
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var notifCheckTick by remember { mutableIntStateOf(0) }
    var notifBannerDismissed by remember { mutableStateOf(false) }

    // Refresh OS notification/alarm/battery status immediately upon returning from Android Settings
    DisposableEffect(lifecycleOwner) {
        val obs = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                notifCheckTick++
            }
        }
        lifecycleOwner.lifecycle.addObserver(obs)
        onDispose { lifecycleOwner.lifecycle.removeObserver(obs) }
    }
    LaunchedEffect(windowInfo.isWindowFocused) {
        if (windowInfo.isWindowFocused) {
            notifCheckTick++
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        notifCheckTick++
        if (!granted) {
            scope.launch {
                val res = snackbarHostState.showSnackbar(
                    message = if (lang == "en") {
                        "Enable notifications in system settings so reminders work when the app is closed."
                    } else {
                        "فعّل إذن الإشعارات من إعدادات الهاتف لكي تصلك التذكيرات عند إغلاق التطبيق وقفل الشاشة."
                    },
                    actionLabel = if (lang == "en") "Open Settings" else "فتح الإعدادات",
                    duration = SnackbarDuration.Long
                )
                if (res == SnackbarResult.ActionPerformed) {
                    TaskAlarmScheduler.openNotificationSettings(context)
                }
            }
        }
    }

    val allTasks = (tasksState as? UiState.Success)?.data ?: emptyList()

    // Navigation & Modal States
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
    var selectedFloatingBadgeId by remember { mutableStateOf<String?>(null) }
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
            // Seamless, continuous full-screen wallpaper covering behind status bar, top bar, body, and nav bar
            // with ZERO visible dividing line or separate top strip.
            MainScreenWallpaperBackdrop(
                wallpaperUri = prefs.mainWallpaperUri,
                dimPercent = prefs.mainWallpaperDimPercent,
                modifier = Modifier.fillMaxSize()
            )

            Scaffold(
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                snackbarHost = { SnackbarHost(snackbarHostState) },
                modifier = Modifier.fillMaxSize()
            ) { _ ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
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
                        // Top Brand Bar (Clean header — HeroBanner opens Settings)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
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
                                        initialSettingsFolder = null
                                        showSettings = true
                                    }
                            )
                        }

                        // Actionable Notification / Exact Alarm readiness strip ONLY when reminders exist & OS permission is missing
                        val hasScheduledReminders = remember(allTasks) {
                            allTasks.any { it.alertEnabled && it.alertTime.isNotBlank() }
                        }
                        val notifDiag = remember(hasScheduledReminders, notifCheckTick, showSettings) {
                            TaskAlarmScheduler.diagnose(context)
                        }
                        val needsOsAlertSetup = hasScheduledReminders && !notifBannerDismissed &&
                            (!notifDiag.hasNotificationPermission || !notifDiag.areNotificationsEnabled || !notifDiag.canScheduleExactAlarms)
                        if (needsOsAlertSetup) {
                            Spacer(Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFFFFFBEB),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                                    .testTag("notification_permission_banner")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.NotificationsActive,
                                        contentDescription = null,
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = when {
                                            !notifDiag.hasNotificationPermission || !notifDiag.areNotificationsEnabled ->
                                                if (lang == "en") "Allow Notifications for reminders" else "فعّل الإشعارات لعمل التذكيرات"
                                            else ->
                                                if (lang == "en") "Allow Exact Alarms for on-time alerts" else "فعّل المنبهات الدقيقة للتذكير بالثانية"
                                        },
                                        color = Color(0xFF92400E),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(999.dp))
                                            .background(Color(0xFFD97706))
                                            .clickable {
                                                when {
                                                    !notifDiag.hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
                                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                                    !notifDiag.areNotificationsEnabled ->
                                                        TaskAlarmScheduler.openNotificationSettings(context)
                                                    else ->
                                                        TaskAlarmScheduler.openExactAlarmSettings(context)
                                                }
                                                notifCheckTick++
                                            }
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                            .testTag("notification_permission_fix_btn")
                                    ) {
                                        Text(
                                            text = if (lang == "en") "Enable" else "تفعيل",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = if (lang == "en") "Dismiss" else "إخفاء",
                                        tint = Color(0xFF92400E),
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clickable { notifBannerDismissed = true }
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Tasks Workspace (Single clean workspace — Orders & Invoices removed)
                        Box(modifier = Modifier.weight(1f)) {
                            TasksWorkspaceRouter(
                                prefs = prefs,
                                allTasks = allTasks,
                                smartIconSettings = smartIconSettings,
                                tasksState = tasksState,
                                searchQuery = searchQuery,
                                onSearchChange = { viewModel.setSearchQuery(it) },
                                onBannerPhotoClick = {
                                    viewModel.playSound("enter")
                                    photoPickTarget = PhotoPickTarget.Banner
                                },
                                onBannerBodyClick = {
                                    viewModel.playSound("enter")
                                    initialSettingsFolder = null
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
                                }
                            )
                        }
                    }
                }
            }

            // Independent Draggable, Resizable, Long-Press/Tap Menu Floating Circular Icon(s)
            FloatingCharacterBadgesOverlay(
                badges = floatingBadges,
                selectedBadgeId = selectedFloatingBadgeId,
                lang = lang,
                onToggleSelectBadge = { tappedBadge ->
                    viewModel.playSound("nav")
                    selectedFloatingBadgeId = toggleFloatingIconSelection(
                        currentSelectedId = selectedFloatingBadgeId,
                        tappedId = tappedBadge.id
                    )
                },
                onSelectBadge = { heldBadge ->
                    viewModel.playSound("nav")
                    selectedFloatingBadgeId = heldBadge.id
                },
                onDismissSelection = {
                    selectedFloatingBadgeId = null
                },
                onOpenBadgeLibrary = { tappedBadge ->
                    viewModel.playSound("enter")
                    selectedFloatingBadgeId = null
                    libraryTargetBadgeId = tappedBadge.id
                },
                onResetBadgePosition = { targetBadge ->
                    viewModel.resetFloatingBadgePosition(targetBadge.id)
                },
                onDuplicateBadge = { tappedBadge ->
                    val dupId = viewModel.duplicateFloatingBadge(tappedBadge.id)
                    selectedFloatingBadgeId = dupId
                },
                onDeleteBadge = { tappedBadge ->
                    viewModel.deleteFloatingBadge(tappedBadge.id)
                    if (selectedFloatingBadgeId == tappedBadge.id) {
                        selectedFloatingBadgeId = null
                    }
                },
                onUpdatePosition = { id, x, y -> viewModel.updateFloatingBadgePosition(id, x, y) }
            )

            // Dedicated 150+ Icon Selection Library Sheet with explicit OK / Save & Apply button
            val activeLibraryBadge = libraryTargetBadgeId?.let { targetId ->
                floatingBadges.firstOrNull { it.id == targetId }
            }
            if (activeLibraryBadge != null) {
                FloatingBadgeLibrarySheet(
                    badge = activeLibraryBadge,
                    lang = lang,
                    onConfirmSave = { iconId, sizeDp, locked ->
                        viewModel.confirmSaveFloatingBadgeChanges(
                            badgeId = activeLibraryBadge.id,
                            stateId = iconId,
                            sizeDp = sizeDp,
                            locked = locked
                        )
                    },
                    onDuplicate = {
                        val dupId = viewModel.duplicateFloatingBadge(activeLibraryBadge.id)
                        if (dupId != null) libraryTargetBadgeId = dupId
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
                        if (alertEnabled && alertTime.isNotBlank()) {
                            val diag = TaskAlarmScheduler.diagnose(context)
                            notifCheckTick++
                            when {
                                !diag.hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                                !diag.areNotificationsEnabled -> {
                                    scope.launch {
                                        val res = snackbarHostState.showSnackbar(
                                            message = if (lang == "en") {
                                                "Notifications are turned off for M Notes."
                                            } else {
                                                "الإشعارات متوقفة للتطبيق في إعدادات الهاتف."
                                            },
                                            actionLabel = if (lang == "en") "Enable" else "تفعيل",
                                            duration = SnackbarDuration.Long
                                        )
                                        if (res == SnackbarResult.ActionPerformed) {
                                            TaskAlarmScheduler.openNotificationSettings(context)
                                        }
                                    }
                                }
                                !diag.canScheduleExactAlarms -> {
                                    scope.launch {
                                        val res = snackbarHostState.showSnackbar(
                                            message = if (lang == "en") {
                                                "Allow Exact Alarms so reminders fire right on time."
                                            } else {
                                                "فعّل إذن المنبهات الدقيقة لكي يعمل التذكير في وقته بالثانية."
                                            },
                                            actionLabel = if (lang == "en") "Allow" else "السماح",
                                            duration = SnackbarDuration.Long
                                        )
                                        if (res == SnackbarResult.ActionPerformed) {
                                            TaskAlarmScheduler.openExactAlarmSettings(context)
                                        }
                                    }
                                }
                            }
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
