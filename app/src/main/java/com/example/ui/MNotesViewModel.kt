package com.example.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundEngine
import com.example.data.LocalMediaRepository
import com.example.data.LocalStoreRepository
import com.example.data.MNotesRepository
import com.example.localization.AppStrings
import com.example.model.BackupPreferencesDto
import com.example.model.BackupRequestDto
import com.example.model.BackupSnapshotPayload
import com.example.model.BackupTaskDto
import com.example.model.ChecklistOrderItem
import com.example.model.ChecklistStyleConfig
import com.example.model.FloatingBadgeItem
import com.example.model.LibraryPhoto
import com.example.model.QuickRequest
import com.example.model.RepeatMode
import com.example.model.SmartIconSettings
import com.example.model.SmartTask
import com.example.model.TrimmedRingtone
import com.example.model.UserPreferences
import java.util.concurrent.atomic.AtomicInteger
import com.example.notification.TaskAlarmScheduler
import com.example.util.LauncherIconManager
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

class MNotesViewModel(
    private val localStore: LocalStoreRepository,
    private val repository: MNotesRepository,
    private val mediaRepository: LocalMediaRepository,
    val soundEngine: SoundEngine,
    private val appContext: Context? = null
) : ViewModel() {

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }
    private val auth: FirebaseAuth = Firebase.auth

    private val _connectedUser = MutableStateFlow(auth.currentUser)
    val connectedEmail: StateFlow<String?> = _connectedUser
        .map { user -> user?.email?.takeIf { it.isNotBlank() } ?: user?.uid }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = auth.currentUser?.let { it.email?.takeIf { e -> e.isNotBlank() } ?: it.uid }
        )

    private val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        _connectedUser.value = firebaseAuth.currentUser
    }

    val preferences: StateFlow<UserPreferences> = localStore.preferencesFlow
        .catch { error ->
            if (error is CancellationException) throw error
            emit(UserPreferences(userId = "local"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UserPreferences(userId = "local")
        )

    private val _tasksState = MutableStateFlow<UiState<List<SmartTask>>>(UiState.Loading)
    val tasksState: StateFlow<UiState<List<SmartTask>>> = _tasksState.asStateFlow()

    private val inFlightTaskWrites = AtomicInteger(0)

    val requestsState: StateFlow<UiState<List<QuickRequest>>> = localStore.requestsFlow
        .map<List<QuickRequest>, UiState<List<QuickRequest>>> { UiState.Success(it) }
        .catch { error ->
            if (error is CancellationException) throw error
            emit(UiState.Error(error.message ?: "Failed to load requests"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    val libraryPhotos: StateFlow<List<LibraryPhoto>> = mediaRepository.photosFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val trimmedRingtones: StateFlow<List<TrimmedRingtone>> = mediaRepository.ringtonesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private val _floatingBadges = MutableStateFlow<List<FloatingBadgeItem>>(emptyList())
    val floatingBadges: StateFlow<List<FloatingBadgeItem>> = _floatingBadges.asStateFlow()

    private val _smartIconSettings = MutableStateFlow(SmartIconSettings())
    val smartIconSettings: StateFlow<SmartIconSettings> = _smartIconSettings.asStateFlow()

    private val _checklistOrders = MutableStateFlow<List<ChecklistOrderItem>>(emptyList())
    val checklistOrders: StateFlow<List<ChecklistOrderItem>> = _checklistOrders.asStateFlow()

    private val _checklistStyle = MutableStateFlow(ChecklistStyleConfig())
    val checklistStyle: StateFlow<ChecklistStyleConfig> = _checklistStyle.asStateFlow()

    private val inFlightBadgeWrites = AtomicInteger(0)
    private val inFlightChecklistWrites = AtomicInteger(0)
    private var autoBackupDebounceJob: Job? = null
    @Volatile
    private var lastSmartSettingsMutationAtMs: Long = 0L

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _activeAlertTask = MutableStateFlow<SmartTask?>(null)
    val activeAlertTask: StateFlow<SmartTask?> = _activeAlertTask.asStateFlow()

    private val _isAlertPreview = MutableStateFlow(false)
    val isAlertPreview: StateFlow<Boolean> = _isAlertPreview.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val toastMessage = _toastMessage.asSharedFlow()

    private val _isBusyBackup = MutableStateFlow(false)
    val isBusyBackup: StateFlow<Boolean> = _isBusyBackup.asStateFlow()

    private val _lastBackupStatus = MutableStateFlow("idle") // "idle" | "success" | "error"
    val lastBackupStatus: StateFlow<String> = _lastBackupStatus.asStateFlow()

    private val _lastBackupError = MutableStateFlow<String?>(null)
    val lastBackupError: StateFlow<String?> = _lastBackupError.asStateFlow()

    init {
        auth.addAuthStateListener(authListener)
        viewModelScope.launch {
            localStore.ensureInitialized()
        }
        viewModelScope.launch {
            localStore.tasksFlow
                .catch { error ->
                    if (error is CancellationException) throw error
                    _tasksState.value = UiState.Error(error.message ?: "Failed to load tasks")
                }
                .collect { stored ->
                    if (inFlightTaskWrites.get() == 0 || _tasksState.value !is UiState.Success) {
                        _tasksState.value = UiState.Success(stored)
                    }
                    syncAllAlarms(stored)
                }
        }
        viewModelScope.launch {
            localStore.floatingBadgesFlow.collect { stored ->
                if (inFlightBadgeWrites.get() == 0 || _floatingBadges.value.isEmpty()) {
                    _floatingBadges.value = stored
                }
            }
        }
        viewModelScope.launch {
            localStore.smartIconSettingsFlow.collect { stored ->
                val now = System.currentTimeMillis()
                if (now - lastSmartSettingsMutationAtMs > 350L) {
                    _smartIconSettings.value = stored
                }
            }
        }
        viewModelScope.launch {
            localStore.checklistOrdersFlow.collect { stored ->
                if (inFlightChecklistWrites.get() == 0) {
                    _checklistOrders.value = stored
                }
            }
        }
        viewModelScope.launch {
            localStore.checklistStyleFlow.collect { stored ->
                _checklistStyle.value = stored
            }
        }
    }

    private fun syncAllAlarms(tasks: List<SmartTask>) {
        val ctx = appContext ?: return
        TaskAlarmScheduler.syncAllTaskAlarms(ctx, tasks, preferences.value.lang)
    }

    private fun applyOptimisticTasks(transform: (List<SmartTask>) -> List<SmartTask>) {
        val current = (_tasksState.value as? UiState.Success)?.data ?: return
        val updated = transform(current)
        _tasksState.value = UiState.Success(updated)
        syncAllAlarms(updated)
    }

    private fun persistTaskMutation(
        optimisticTransform: (List<SmartTask>) -> List<SmartTask>,
        persistBlock: suspend () -> Result<Unit>,
        onSuccess: (() -> Unit)? = null
    ) {
        applyOptimisticTasks(optimisticTransform)
        inFlightTaskWrites.incrementAndGet()
        viewModelScope.launch {
            try {
                val res = persistBlock()
                if (res.isSuccess) {
                    scheduleDebouncedAutoBackup()
                    onSuccess?.invoke()
                } else {
                    _toastMessage.tryEmit(res.exceptionOrNull()?.localizedMessage ?: "Failed to save changes")
                }
            } finally {
                inFlightTaskWrites.decrementAndGet()
            }
        }
    }

    fun openTaskAlertById(taskId: String) {
        if (taskId.isBlank()) return
        val currentList = (_tasksState.value as? UiState.Success)?.data
        val match = currentList?.firstOrNull { it.id == taskId }
        if (match != null) {
            showAlert(match, previewOnly = false)
            return
        }
        viewModelScope.launch {
            val stored = localStore.getTasksSnapshot().firstOrNull { it.id == taskId }
            if (stored != null) {
                showAlert(stored, previewOnly = false)
            }
        }
    }

    fun scheduleTestLockScreenNotification(context: Context, delaySeconds: Int = 5) {
        playSound("done")
        val lang = preferences.value.lang
        TaskAlarmScheduler.scheduleTestNotificationInSeconds(context, delaySeconds, lang)
        _toastMessage.tryEmit(
            if (lang == "en") {
                "Test notification scheduled in ${delaySeconds}s — lock your screen or close the app to verify!"
            } else {
                "تم جدولة إشعار تجريبي بعد $delaySeconds ثوانٍ — أقفل الشاشة أو أغلق التطبيق للتجربة الآن!"
            }
        )
    }

    private fun scheduleDebouncedAutoBackup() {
        if (!preferences.value.autoBackup || auth.currentUser == null) return
        autoBackupDebounceJob?.cancel()
        autoBackupDebounceJob = viewModelScope.launch {
            delay(1800L)
            runCloudBackup(isAuto = true)
        }
    }

    private fun commitFloatingBadgesOptimistic(
        updated: List<FloatingBadgeItem>,
        toastOnSaved: String? = null
    ) {
        val normalized = updated.map { b ->
            b.copy(
                xFraction = b.xFraction.coerceIn(0f, 1f),
                yFraction = b.yFraction.coerceIn(0f, 1f),
                sizeDp = b.sizeDp.coerceIn(26f, 180f)
            )
        }
        _floatingBadges.value = normalized
        inFlightBadgeWrites.incrementAndGet()
        viewModelScope.launch {
            try {
                val res = localStore.saveFloatingBadges(normalized)
                if (res.isSuccess) {
                    scheduleDebouncedAutoBackup()
                    if (!toastOnSaved.isNullOrBlank()) {
                        _toastMessage.tryEmit(toastOnSaved)
                    }
                } else {
                    _toastMessage.tryEmit(res.exceptionOrNull()?.localizedMessage ?: "Failed to save icon")
                }
            } finally {
                inFlightBadgeWrites.decrementAndGet()
            }
        }
    }

    fun setSearchQuery(q: String) {
        _searchQuery.value = q
    }

    fun playSound(type: String) {
        val p = preferences.value
        soundEngine.playUiSound(
            type = type,
            navEnabled = p.soundNav,
            enterEnabled = p.soundEnter,
            exitEnabled = p.soundExit,
            doneEnabled = p.soundDone,
            hapticEnabled = p.hapticEnabled
        )
    }

    fun showMessage(msg: String) {
        _toastMessage.tryEmit(msg)
    }

    fun updatePreference(updates: Map<String, Any>, triggerAutoBackup: Boolean = false) {
        viewModelScope.launch {
            val res = localStore.updatePreferences(updates)
            if (res.isFailure) {
                _toastMessage.tryEmit(res.exceptionOrNull()?.localizedMessage ?: "Failed to update setting")
            } else if (triggerAutoBackup && preferences.value.autoBackup && auth.currentUser != null) {
                runCloudBackup(isAuto = true)
            }
        }
    }

    fun applyReminderBackground(themeId: String, doodlePackId: String, opacityPercent: Int) {
        playSound("done")
        val clampedOpacity = opacityPercent.coerceIn(6, 30)
        viewModelScope.launch {
            val res = localStore.updatePreferences(
                mapOf(
                    "reminderBgThemeId" to themeId,
                    "reminderDoodlePackId" to doodlePackId,
                    "reminderDoodleOpacity" to clampedOpacity
                )
            )
            if (res.isSuccess) {
                scheduleDebouncedAutoBackup()
                val lang = preferences.value.lang
                _toastMessage.tryEmit(
                    if (lang == "en") "Reminder screen background applied!"
                    else "تم تطبيق خلفية وزخارف شاشة التذكير بنجاح!"
                )
            } else {
                _toastMessage.tryEmit(res.exceptionOrNull()?.localizedMessage ?: "Failed to save reminder background")
            }
        }
    }

    suspend fun importWallpaperFromDevice(uri: Uri): Result<String> =
        mediaRepository.saveWallpaperImage(uri)

    fun applyMainWallpaper(uri: String, dimPercent: Int) {
        playSound("done")
        val clampedDim = dimPercent.coerceIn(0, 45)
        viewModelScope.launch {
            val res = localStore.updatePreferences(
                mapOf(
                    "mainWallpaperUri" to uri,
                    "mainWallpaperDimPercent" to clampedDim
                )
            )
            if (res.isSuccess) {
                scheduleDebouncedAutoBackup()
                val lang = preferences.value.lang
                _toastMessage.tryEmit(
                    if (uri.isBlank()) {
                        if (lang == "en") "Restored default main screen background!"
                        else "تمت استعادة خلفية الشاشة الرئيسية الأصلية!"
                    } else {
                        if (lang == "en") "Main screen wallpaper applied & saved!"
                        else "تم تطبيق وحفظ خلفية الشاشة الرئيسية بنجاح!"
                    }
                )
            } else {
                _toastMessage.tryEmit(res.exceptionOrNull()?.localizedMessage ?: "Failed to save wallpaper")
            }
        }
    }

    fun resetMainWallpaper() {
        applyMainWallpaper(uri = "", dimPercent = 0)
    }

    fun changeLauncherIcon(context: Context, iconId: String) {
        val ok = LauncherIconManager.applyLauncherIcon(context, iconId)
        playSound(if (ok) "done" else "exit")
        updatePreference(mapOf("appIconId" to iconId))
        val msg = if (preferences.value.lang == "en") {
            "Home screen icon updated! Launcher may take a moment to refresh."
        } else {
            "تم تغيير أيقونة التطبيق الخارجية بنجاح! قد يستغرق اللانشر ثوانٍ للتحديث"
        }
        _toastMessage.tryEmit(msg)
    }

    fun createCustomShortcut(
        context: Context,
        shapeId: String,
        symbolId: String,
        bgColorArgb: Int,
        fgColorArgb: Int,
        badgeColorArgb: Int
    ) {
        val ok = LauncherIconManager.pinCustomColoredShortcut(
            context = context,
            shapeId = shapeId,
            symbolId = symbolId,
            bgColorArgb = bgColorArgb,
            fgColorArgb = fgColorArgb,
            badgeColorArgb = badgeColorArgb,
            shortLabel = preferences.value.bannerTitle.ifBlank { "M Notes" }
        )
        playSound(if (ok) "done" else "exit")
        val msg = if (preferences.value.lang == "en") {
            if (ok) "Custom colored icon prompt opened!" else "Launcher does not support pinned shortcuts"
        } else {
            if (ok) "تم تجهيز أيقونتك المخصصة بألوانك لإضافتها على الشاشة الرئيسية" else "مشغل الهاتف الحالي لا يدعم اختصارات الشاشة المخصصة"
        }
        _toastMessage.tryEmit(msg)
    }

    fun rollOverdueWeekdayTasks(tasks: List<SmartTask>) {
        val today = AppStrings.todayStr()
        tasks.filter {
            it.usesWeekDays() && it.nextDueDate.isNotBlank() && it.nextDueDate < today
        }.forEach { task ->
            val rolled = AppStrings.nextWeekDay(task.weekDays, today)
            if (rolled != task.nextDueDate) {
                viewModelScope.launch {
                    localStore.advanceTaskSchedule(
                        taskId = task.id,
                        cycleIndex = task.cycleIndex,
                        nextDueDate = rolled,
                        lastAlertedDate = ""
                    )
                }
            }
        }
    }

    fun checkDueAlerts(tasks: List<SmartTask>) {
        if (_activeAlertTask.value != null) return
        val today = AppStrings.todayStr()
        val hm = SimpleDateFormat("HH:mm", Locale.US).format(Date())
        val dueTask = tasks.firstOrNull { task ->
            task.alertEnabled &&
                task.alertTime == hm &&
                task.isActiveOnDate() &&
                task.lastAlertedDate != today
        } ?: return

        viewModelScope.launch {
            localStore.markTaskAlerted(dueTask.id, today)
            showAlert(dueTask, previewOnly = false)
        }
    }

    fun showAlert(task: SmartTask, previewOnly: Boolean) {
        _isAlertPreview.value = previewOnly
        _activeAlertTask.value = task
        val activeRingId = task.ringId.ifBlank { preferences.value.activeRingId }
        val customPath = trimmedRingtones.value.firstOrNull { it.id == activeRingId }?.filePath
        soundEngine.startAlertRingtoneLoop(activeRingId, customPath)
    }

    fun previewRingtoneId(ringId: String) {
        val targetId = ringId.ifBlank { preferences.value.activeRingId }
        val customClip = trimmedRingtones.value.firstOrNull { it.id == targetId }
        if (customClip != null) {
            soundEngine.playSavedFileOnce(customClip.filePath)
        } else {
            soundEngine.playGymTrackOnce(targetId)
        }
    }

    fun previewAlertStyle(styleId: String) {
        val sample = (tasksState.value as? UiState.Success)?.data?.firstOrNull()
            ?: SmartTask(
                id = "preview",
                userId = "local",
                title = if (preferences.value.lang == "en") "Gym Workout" else "تمرين الجيم",
                meta = "11:00 PM",
                repeatMode = RepeatMode.CYCLE.wire,
                cycleSteps = listOf("Legs", "Push", "Pull"),
                cycleIndex = 0L,
                alertEnabled = true,
                alertTime = "23:00"
            )
        updatePreference(mapOf("alertStyle" to styleId))
        showAlert(sample, previewOnly = true)
    }

    fun dismissAlert() {
        soundEngine.stopRingtone()
        playSound("exit")
        _activeAlertTask.value = null
    }

    fun completeActiveAlert() {
        val task = _activeAlertTask.value ?: return
        val preview = _isAlertPreview.value
        soundEngine.stopRingtone()
        playSound("done")
        _activeAlertTask.value = null

        if (preview) return

        viewModelScope.launch {
            val result = when (task.repeat) {
                RepeatMode.CYCLE -> {
                    val steps = task.cycleSteps
                    val nextIdx = if (steps.isNotEmpty()) ((task.cycleIndex + 1L) % steps.size) else 0L
                    val tomorrow = AppStrings.addDaysStr(1)
                    val nextDate = if (task.usesWeekDays()) AppStrings.nextWeekDay(task.weekDays, tomorrow) else tomorrow
                    localStore.advanceTaskSchedule(task.id, nextIdx, nextDate, AppStrings.todayStr())
                }
                RepeatMode.DAILY -> {
                    localStore.advanceTaskSchedule(
                        task.id,
                        task.cycleIndex,
                        AppStrings.addDaysStr(1),
                        AppStrings.todayStr()
                    )
                }
                RepeatMode.WEEKLY -> {
                    val nextDate = if (task.usesWeekDays()) {
                        AppStrings.nextWeekDay(task.weekDays, AppStrings.addDaysStr(1))
                    } else {
                        AppStrings.addDaysStr(7)
                    }
                    localStore.advanceTaskSchedule(
                        task.id,
                        task.cycleIndex,
                        nextDate,
                        AppStrings.todayStr()
                    )
                }
                RepeatMode.NONE -> {
                    localStore.deleteTask(task.id)
                }
            }
            if (result.isFailure) {
                _toastMessage.tryEmit(result.exceptionOrNull()?.localizedMessage ?: "Failed to update task")
            } else if (preferences.value.autoBackup && auth.currentUser != null) {
                runCloudBackup(isAuto = true)
            }
        }
    }

    fun saveTask(
        editingTask: SmartTask?,
        title: String,
        metaInput: String,
        startDate: String,
        alertTime: String,
        repeatMode: RepeatMode,
        cycleStepsRaw: String,
        weekDays: List<Long>,
        alertEnabled: Boolean,
        imgUri: String,
        ringId: String = "",
        triggerAlertAfterSave: Boolean = false,
        badgeIconId: String? = null,
        badgeVisible: Boolean? = null,
        badgeOffsetX: Float? = null,
        badgeOffsetY: Float? = null,
        badgeSizeDp: Float? = null,
        badgeCategory: String? = null,
        badgeSmartMode: String? = null,
        badgeLocked: Boolean? = null
    ) {
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty()) return
        playSound("done")

        val lang = preferences.value.lang
        val distinctDays = weekDays.distinct().sorted()
        val effectiveRepeat = when {
            distinctDays.isNotEmpty() && repeatMode != RepeatMode.CYCLE -> RepeatMode.WEEKLY
            repeatMode == RepeatMode.NONE && distinctDays.isNotEmpty() -> RepeatMode.WEEKLY
            else -> repeatMode
        }
        val parsedSteps = cycleStepsRaw
            .split(',', '،')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .take(20)
        val finalSteps = if (effectiveRepeat == RepeatMode.CYCLE) {
            parsedSteps.ifEmpty { listOf("Legs", "Push", "Pull") }
        } else {
            emptyList()
        }
        val finalDays = if (effectiveRepeat == RepeatMode.WEEKLY || effectiveRepeat == RepeatMode.CYCLE) {
            distinctDays
        } else {
            emptyList()
        }
        val baseDate = startDate.ifBlank { AppStrings.todayStr() }
        val finalDate = if (finalDays.isNotEmpty()) AppStrings.nextWeekDay(finalDays, baseDate) else baseDate
        val finalMeta = if (alertTime.isNotBlank()) {
            AppStrings.formatTime(alertTime, lang)
        } else {
            metaInput.trim().ifBlank { cleanTitle }
        }

        val resolvedBadgeIconId = badgeIconId ?: (editingTask?.badgeIconId ?: "")
        val resolvedBadgeVisible = badgeVisible ?: (editingTask?.badgeVisible ?: true)

        val payload = SmartTask(
            id = editingTask?.id ?: "",
            userId = "local",
            title = cleanTitle,
            meta = finalMeta,
            imgUri = imgUri,
            repeatMode = effectiveRepeat.wire,
            cycleSteps = finalSteps,
            cycleIndex = editingTask?.cycleIndex ?: 0L,
            weekDays = finalDays,
            alertEnabled = alertEnabled,
            alertTime = alertTime,
            nextDueDate = finalDate,
            lastAlertedDate = "",
            ringId = ringId,
            sortOrder = editingTask?.sortOrder ?: System.currentTimeMillis(),
            badgeIconId = resolvedBadgeIconId,
            badgeVisible = resolvedBadgeVisible,
            badgeOffsetX = badgeOffsetX ?: (editingTask?.badgeOffsetX ?: 20f),
            badgeOffsetY = badgeOffsetY ?: (editingTask?.badgeOffsetY ?: -14f),
            badgeSizeDp = badgeSizeDp ?: (editingTask?.badgeSizeDp ?: 28f),
            badgeCategory = badgeCategory ?: (editingTask?.badgeCategory ?: "auto"),
            badgeSmartMode = badgeSmartMode ?: (editingTask?.badgeSmartMode ?: "manual"),
            badgeLocked = badgeLocked ?: (editingTask?.badgeLocked ?: false)
        )

        if (editingTask != null && editingTask.id.isNotBlank()) {
            val editId = editingTask.id
            applyOptimisticTasks { list ->
                list.map { existing -> if (existing.id == editId) payload.copy(id = editId) else existing }
            }
        }

        viewModelScope.launch {
            val res = if (editingTask != null && editingTask.id.isNotBlank()) {
                localStore.updateTaskFull(editingTask.id, payload).map { editingTask.id }
            } else {
                localStore.createTask(payload)
            }
            if (res.isFailure) {
                _toastMessage.tryEmit(res.exceptionOrNull()?.localizedMessage ?: "Failed to save task")
            } else {
                val savedId = res.getOrNull().orEmpty()
                val savedTask = payload.copy(id = savedId)
                if (editingTask == null || editingTask.id.isBlank()) {
                    applyOptimisticTasks { list ->
                        if (list.any { it.id == savedId }) list else listOf(savedTask) + list
                    }
                }
                if (triggerAlertAfterSave) {
                    showAlert(savedTask, previewOnly = false)
                }
                if (preferences.value.autoBackup && auth.currentUser != null) {
                    runCloudBackup(isAuto = true)
                }
            }
        }
    }

    fun applyTaskBadge(
        taskId: String,
        iconId: String,
        offsetX: Float,
        offsetY: Float,
        sizeDp: Float,
        visible: Boolean = true,
        category: String? = null,
        smartMode: String? = null,
        locked: Boolean? = null,
        showToast: Boolean = true
    ) {
        playSound("done")
        val clampedX = offsetX.coerceIn(-260f, 260f)
        val clampedY = offsetY.coerceIn(-260f, 260f)
        val clampedSize = sizeDp.coerceIn(20f, 96f)
        val resolvedMode = smartMode?.ifBlank { "manual" } ?: "manual"
        persistTaskMutation(
            optimisticTransform = { list ->
                list.map { existing ->
                    if (existing.id == taskId) {
                        existing.copy(
                            badgeIconId = iconId.take(64),
                            badgeVisible = visible,
                            badgeOffsetX = clampedX,
                            badgeOffsetY = clampedY,
                            badgeSizeDp = clampedSize,
                            badgeCategory = category?.ifBlank { "auto" } ?: existing.badgeCategory,
                            badgeSmartMode = resolvedMode,
                            badgeLocked = locked ?: existing.badgeLocked
                        )
                    } else existing
                }
            },
            persistBlock = {
                localStore.updateTaskBadge(
                    taskId = taskId,
                    iconId = iconId,
                    visible = visible,
                    offsetX = clampedX,
                    offsetY = clampedY,
                    sizeDp = clampedSize,
                    category = category,
                    smartMode = resolvedMode,
                    locked = locked
                )
            },
            onSuccess = {
                if (showToast) {
                    val lang = preferences.value.lang
                    _toastMessage.tryEmit(
                        if (lang == "en") "Icon saved & applied to task circle!"
                        else "تم حفظ وتطبيق الأيقونة على دائرة المهمة!"
                    )
                }
            }
        )
    }

    fun applyBadgeToAllTasks(
        iconId: String,
        offsetX: Float,
        offsetY: Float,
        sizeDp: Float,
        visible: Boolean = true,
        smartMode: String? = null
    ) {
        playSound("done")
        val clampedX = offsetX.coerceIn(-260f, 260f)
        val clampedY = offsetY.coerceIn(-260f, 260f)
        val clampedSize = sizeDp.coerceIn(20f, 96f)
        persistTaskMutation(
            optimisticTransform = { list ->
                list.map { existing ->
                    existing.copy(
                        badgeIconId = iconId.take(64),
                        badgeVisible = visible,
                        badgeOffsetX = clampedX,
                        badgeOffsetY = clampedY,
                        badgeSizeDp = clampedSize,
                        badgeSmartMode = smartMode?.ifBlank { "manual" } ?: existing.badgeSmartMode
                    )
                }
            },
            persistBlock = {
                localStore.applyBadgeToAllTasks(
                    iconId = iconId,
                    offsetX = clampedX,
                    offsetY = clampedY,
                    sizeDp = clampedSize,
                    visible = visible,
                    smartMode = smartMode
                )
            },
            onSuccess = {
                val lang = preferences.value.lang
                _toastMessage.tryEmit(
                    if (lang == "en") "Icon saved & applied to all tasks!"
                    else "تم حفظ وتطبيق الأيقونة على جميع المهام!"
                )
            }
        )
    }

    fun updateTaskBadgePosition(taskId: String, offsetX: Float, offsetY: Float) {
        val clampedX = offsetX.coerceIn(-260f, 260f)
        val clampedY = offsetY.coerceIn(-260f, 260f)
        persistTaskMutation(
            optimisticTransform = { list ->
                list.map { existing ->
                    if (existing.id == taskId) existing.copy(badgeOffsetX = clampedX, badgeOffsetY = clampedY)
                    else existing
                }
            },
            persistBlock = {
                localStore.updateTaskBadge(taskId = taskId, offsetX = clampedX, offsetY = clampedY)
            }
        )
    }

    fun updateTaskBadgeSize(taskId: String, sizeDp: Float) {
        val clampedSize = sizeDp.coerceIn(20f, 96f)
        persistTaskMutation(
            optimisticTransform = { list ->
                list.map { existing ->
                    if (existing.id == taskId) existing.copy(badgeSizeDp = clampedSize)
                    else existing
                }
            },
            persistBlock = {
                localStore.updateTaskBadge(taskId = taskId, sizeDp = clampedSize)
            }
        )
    }

    fun updateTaskBadgeIcon(taskId: String, iconId: String) {
        playSound("nav")
        persistTaskMutation(
            optimisticTransform = { list ->
                list.map { existing ->
                    if (existing.id == taskId) {
                        existing.copy(badgeIconId = iconId.take(64), badgeVisible = true, badgeSmartMode = "manual")
                    } else existing
                }
            },
            persistBlock = {
                localStore.updateTaskBadge(taskId = taskId, iconId = iconId, visible = true, smartMode = "manual")
            }
        )
    }

    fun toggleTaskBadgeVisibility(taskId: String) {
        val current = (tasksState.value as? UiState.Success)?.data?.firstOrNull { it.id == taskId } ?: return
        val nextVisible = !current.badgeVisible
        playSound("nav")
        persistTaskMutation(
            optimisticTransform = { list ->
                list.map { existing ->
                    if (existing.id == taskId) existing.copy(badgeVisible = nextVisible) else existing
                }
            },
            persistBlock = {
                localStore.updateTaskBadge(taskId = taskId, visible = nextVisible)
            }
        )
    }

    fun toggleTaskBadgeLock(taskId: String) {
        val current = (tasksState.value as? UiState.Success)?.data?.firstOrNull { it.id == taskId } ?: return
        val nextLocked = !current.badgeLocked
        playSound("nav")
        persistTaskMutation(
            optimisticTransform = { list ->
                list.map { existing ->
                    if (existing.id == taskId) existing.copy(badgeLocked = nextLocked) else existing
                }
            },
            persistBlock = {
                localStore.updateTaskBadge(taskId = taskId, locked = nextLocked)
            }
        )
    }

    fun updateTaskBadgeMode(taskId: String, category: String? = null, smartMode: String? = null) {
        playSound("nav")
        persistTaskMutation(
            optimisticTransform = { list ->
                list.map { existing ->
                    if (existing.id == taskId) {
                        existing.copy(
                            badgeCategory = category?.ifBlank { "auto" } ?: existing.badgeCategory,
                            badgeSmartMode = smartMode?.ifBlank { "manual" } ?: existing.badgeSmartMode
                        )
                    } else existing
                }
            },
            persistBlock = {
                localStore.updateTaskBadge(taskId = taskId, category = category, smartMode = smartMode)
            }
        )
    }

    fun removeTaskBadge(taskId: String) {
        playSound("exit")
        persistTaskMutation(
            optimisticTransform = { list ->
                list.map { existing ->
                    if (existing.id == taskId) {
                        existing.copy(badgeIconId = "", badgeVisible = false, badgeSmartMode = "manual")
                    } else existing
                }
            },
            persistBlock = {
                localStore.updateTaskBadge(
                    taskId = taskId,
                    iconId = "",
                    visible = false,
                    smartMode = "manual"
                )
            }
        )
    }

    fun updateSmartIconSettings(transform: (SmartIconSettings) -> SmartIconSettings) {
        val next = transform(_smartIconSettings.value)
        lastSmartSettingsMutationAtMs = System.currentTimeMillis()
        _smartIconSettings.value = next
        viewModelScope.launch {
            localStore.saveSmartIconSettings(next)
            scheduleDebouncedAutoBackup()
        }
    }

    fun updateTaskPhoto(taskId: String, imgUri: String) {
        playSound("done")
        applyOptimisticTasks { list ->
            list.map { existing -> if (existing.id == taskId) existing.copy(imgUri = imgUri) else existing }
        }
        viewModelScope.launch {
            val res = localStore.updateTaskPhoto(taskId, imgUri)
            if (res.isFailure) {
                _toastMessage.tryEmit(res.exceptionOrNull()?.localizedMessage ?: "Failed to update photo")
            } else {
                scheduleDebouncedAutoBackup()
            }
        }
    }

    fun deleteTask(taskId: String) {
        playSound("exit")
        applyOptimisticTasks { list -> list.filterNot { it.id == taskId } }
        appContext?.let { TaskAlarmScheduler.cancelTaskAlarm(it, taskId) }
        viewModelScope.launch {
            val res = localStore.deleteTask(taskId)
            if (res.isFailure) {
                _toastMessage.tryEmit(res.exceptionOrNull()?.localizedMessage ?: "Failed to delete task")
            } else if (preferences.value.autoBackup && auth.currentUser != null) {
                runCloudBackup(isAuto = true)
            }
        }
    }

    fun simulateTomorrow(task: SmartTask) {
        playSound("nav")
        viewModelScope.launch {
            localStore.advanceTaskSchedule(
                taskId = task.id,
                cycleIndex = task.cycleIndex,
                nextDueDate = AppStrings.todayStr(),
                lastAlertedDate = ""
            )
        }
    }

    fun saveQuickRequest(
        title: String,
        body: String,
        docType: String = preferences.value.reqDefaultDocType,
        customerName: String = "",
        amountText: String = "",
        stampId: String = "none",
        isSigned: Boolean = false,
        signatureName: String = preferences.value.reqSignatureName,
        statusDot: String = "none",
        statusLabel: String = preferences.value.reqDefaultStatusLabel,
        splitMode: String = preferences.value.reqDefaultSplitMode,
        isChecklistOrder: Boolean = false
    ) {
        val t = title.trim()
        val b = body.trim()
        val c = customerName.trim()
        if (t.isEmpty() && b.isEmpty() && c.isEmpty()) return
        playSound("done")
        val finalTitle = t.ifEmpty {
            if (isChecklistOrder) b.lineSequence().firstOrNull().orEmpty().take(40)
            else c.ifEmpty { AppStrings.get(preferences.value.lang, "reqTab") }
        }.ifEmpty { AppStrings.get(preferences.value.lang, "reqTab") }

        val autoCompleteStamps = setOf("paid_thanks", "paid_thanks_green", "done", "paid_full", "received", "arrived", "ready", "complete")
        val effectiveSigned = isSigned || (preferences.value.reqAutoSignOnComplete && stampId in autoCompleteStamps && signatureName.isNotBlank())

        val req = QuickRequest(
            title = finalTitle,
            body = b,
            docType = docType,
            customerName = c,
            amountText = amountText.trim(),
            stampId = stampId,
            isSigned = effectiveSigned,
            signatureName = signatureName.trim(),
            statusDot = statusDot,
            statusLabel = statusLabel.trim(),
            splitMode = splitMode,
            isChecklistOrder = isChecklistOrder
        )
        viewModelScope.launch {
            val res = localStore.createRequest(req)
            if (res.isFailure) {
                _toastMessage.tryEmit(res.exceptionOrNull()?.localizedMessage ?: "Failed to save request")
            } else if (preferences.value.autoBackup && auth.currentUser != null) {
                runCloudBackup(isAuto = true)
            }
        }
    }

    fun toggleRequestDot(requestId: String, targetDot: String) {
        playSound("nav")
        viewModelScope.launch {
            localStore.updateRequest(requestId) { cur ->
                val nextDot = if (cur.statusDot == targetDot) "none" else targetDot
                val nextStatus = when (nextDot) {
                    "green" -> if (cur.statusLabel in setOf("", "غير متاح", "لم يصل", "غير مكتمل")) "وصل / متاح" else cur.statusLabel
                    "red" -> if (cur.statusLabel in setOf("", "متاح", "وصل", "جاهز", "وصل / متاح")) "لم يصل / غير متاح" else cur.statusLabel
                    else -> cur.statusLabel
                }
                val nextStamp = when {
                    nextDot == "green" && cur.stampId in setOf("none", "unpaid", "incomplete") -> "paid_thanks"
                    nextDot == "red" && cur.stampId in setOf("none", "paid_thanks", "done", "arrived", "complete") -> "unpaid"
                    else -> cur.stampId
                }
                val sigName = cur.signatureName.ifBlank { preferences.value.reqSignatureName }
                val completeStamps = setOf("paid_thanks", "paid_thanks_green", "done", "paid_full", "received", "arrived", "ready", "complete")
                val autoSigned = cur.isSigned || (preferences.value.reqAutoSignOnComplete && nextStamp in completeStamps && sigName.isNotBlank())
                cur.copy(
                    statusDot = nextDot,
                    statusLabel = nextStatus,
                    stampId = nextStamp,
                    isSigned = autoSigned,
                    signatureName = sigName
                )
            }
        }
    }

    fun updateRequestStamp(requestId: String, stampId: String) {
        playSound("done")
        viewModelScope.launch {
            localStore.updateRequest(requestId) { cur ->
                val sigName = cur.signatureName.ifBlank { preferences.value.reqSignatureName }
                val completeStamps = setOf("paid_thanks", "paid_thanks_green", "done", "paid_full", "received", "arrived", "ready", "complete")
                val autoSigned = cur.isSigned || (preferences.value.reqAutoSignOnComplete && stampId in completeStamps && sigName.isNotBlank())
                cur.copy(
                    stampId = stampId,
                    isSigned = autoSigned,
                    signatureName = sigName
                )
            }
        }
    }

    fun toggleRequestSignature(requestId: String, customName: String = "") {
        playSound("done")
        viewModelScope.launch {
            localStore.updateRequest(requestId) { cur ->
                val resolvedName = customName.trim().ifBlank {
                    cur.signatureName.ifBlank { preferences.value.reqSignatureName }
                }
                cur.copy(
                    isSigned = !cur.isSigned,
                    signatureName = resolvedName
                )
            }
        }
    }

    fun updateRequestStatusLabel(requestId: String, label: String) {
        playSound("nav")
        viewModelScope.launch {
            localStore.updateRequest(requestId) { cur ->
                cur.copy(statusLabel = label.trim())
            }
        }
    }

    fun deleteQuickRequest(requestId: String) {
        playSound("exit")
        viewModelScope.launch {
            val res = localStore.deleteRequest(requestId)
            if (res.isFailure) {
                _toastMessage.tryEmit(res.exceptionOrNull()?.localizedMessage ?: "Failed to delete request")
            }
        }
    }

    fun addFloatingBadge(stateId: String = "sleeping", sizeDp: Float = 46f): String {
        playSound("done")
        val current = _floatingBadges.value
        val count = current.size
        val newId = "badge_${UUID.randomUUID().toString().take(8)}"
        val x = (0.78f - (count % 4) * 0.14f).coerceIn(0.10f, 0.86f)
        val y = (0.16f + (count / 4) * 0.10f + (count % 3) * 0.05f).coerceIn(0.10f, 0.82f)
        val item = FloatingBadgeItem(
            id = newId,
            state = stateId,
            xFraction = x,
            yFraction = y,
            sizeDp = sizeDp.coerceIn(26f, 180f),
            visible = true,
            locked = false
        )
        commitFloatingBadgesOptimistic(current + item)
        return newId
    }

    fun duplicateFloatingBadge(badgeId: String): String? {
        playSound("done")
        val current = _floatingBadges.value
        val source = current.firstOrNull { it.id == badgeId } ?: return null
        val newId = "badge_${UUID.randomUUID().toString().take(8)}"
        val nextX = if (source.xFraction + 0.09f <= 0.88f) source.xFraction + 0.09f else (source.xFraction - 0.11f).coerceAtLeast(0.06f)
        val nextY = if (source.yFraction + 0.07f <= 0.86f) source.yFraction + 0.07f else (source.yFraction - 0.09f).coerceAtLeast(0.08f)
        val copy = source.copy(
            id = newId,
            xFraction = nextX,
            yFraction = nextY,
            visible = true,
            locked = false
        )
        commitFloatingBadgesOptimistic(current + copy)
        return newId
    }

    fun updateFloatingBadgePosition(badgeId: String, xFraction: Float, yFraction: Float) {
        val updated = _floatingBadges.value.map { cur ->
            if (cur.id == badgeId) cur.copy(xFraction = xFraction, yFraction = yFraction) else cur
        }
        commitFloatingBadgesOptimistic(updated)
    }

    fun updateFloatingBadgeSize(badgeId: String, sizeDp: Float) {
        val updated = _floatingBadges.value.map { cur ->
            if (cur.id == badgeId) cur.copy(sizeDp = sizeDp) else cur
        }
        commitFloatingBadgesOptimistic(updated)
    }

    fun updateFloatingBadgeState(badgeId: String, stateId: String) {
        playSound("nav")
        val updated = _floatingBadges.value.map { cur ->
            if (cur.id == badgeId) cur.copy(state = stateId) else cur
        }
        commitFloatingBadgesOptimistic(updated)
    }

    /**
     * Explicit OK / Save action from the Floating Icon Library Sheet:
     * immediately applies icon, size, visibility & lock state and persists synchronously to DataStore.
     */
    fun confirmSaveFloatingBadgeChanges(
        badgeId: String,
        stateId: String,
        sizeDp: Float,
        locked: Boolean
    ) {
        playSound("done")
        val lang = preferences.value.lang
        val updated = _floatingBadges.value.map { cur ->
            if (cur.id == badgeId) {
                cur.copy(
                    state = stateId,
                    sizeDp = sizeDp.coerceIn(26f, 180f),
                    visible = true,
                    locked = locked
                )
            } else cur
        }
        commitFloatingBadgesOptimistic(
            updated = updated,
            toastOnSaved = if (lang == "en") "Floating icon saved & applied!" else "تم حفظ وتطبيق الأيقونة العائمة بنجاح!"
        )
    }

    fun toggleFloatingBadgeVisibility(badgeId: String) {
        playSound("nav")
        val updated = _floatingBadges.value.map { cur ->
            if (cur.id == badgeId) cur.copy(visible = !cur.visible) else cur
        }
        commitFloatingBadgesOptimistic(updated)
    }

    fun toggleFloatingBadgeLock(badgeId: String) {
        playSound("nav")
        val updated = _floatingBadges.value.map { cur ->
            if (cur.id == badgeId) cur.copy(locked = !cur.locked) else cur
        }
        commitFloatingBadgesOptimistic(updated)
    }

    fun setAllFloatingBadgesVisibility(visible: Boolean) {
        playSound("nav")
        val updated = _floatingBadges.value.map { it.copy(visible = visible) }
        commitFloatingBadgesOptimistic(updated)
    }

    fun deleteFloatingBadge(badgeId: String) {
        playSound("exit")
        val updated = _floatingBadges.value.filterNot { it.id == badgeId }
        commitFloatingBadgesOptimistic(updated)
    }

    // ==================== BLANK CUSTOMIZABLE ORDERS / CHECKLIST ACTIONS ====================

    private fun commitChecklistOrders(updated: List<ChecklistOrderItem>, toastOnSaved: String? = null) {
        val indexed = updated.mapIndexed { idx, item -> item.copy(sortOrder = idx.toLong()) }
        _checklistOrders.value = indexed
        inFlightChecklistWrites.incrementAndGet()
        viewModelScope.launch {
            try {
                val res = localStore.saveChecklistOrders(indexed)
                if (res.isSuccess) {
                    scheduleDebouncedAutoBackup()
                    if (!toastOnSaved.isNullOrBlank()) {
                        _toastMessage.tryEmit(toastOnSaved)
                    }
                } else {
                    _toastMessage.tryEmit(res.exceptionOrNull()?.localizedMessage ?: "Failed to save items")
                }
            } finally {
                inFlightChecklistWrites.decrementAndGet()
            }
        }
    }

    fun addChecklistOrderItems(texts: List<String>) {
        val clean = texts.map { it.trim() }.filter { it.isNotEmpty() }
        if (clean.isEmpty()) return
        playSound("done")
        val base = _checklistOrders.value
        val newItems = clean.mapIndexed { idx, text ->
            ChecklistOrderItem(
                id = "ord_${UUID.randomUUID().toString().take(10)}",
                text = text,
                status = ChecklistOrderItem.STATUS_NONE,
                sortOrder = (base.size + idx).toLong()
            )
        }
        val lang = preferences.value.lang
        commitChecklistOrders(
            updated = base + newItems,
            toastOnSaved = if (lang == "en") "Saved ${newItems.size} item(s)" else "تم حفظ ${newItems.size} عنصر بنجاح"
        )
    }

    fun cycleChecklistOrderStatus(itemId: String) {
        playSound("nav")
        val updated = _checklistOrders.value.map { item ->
            if (item.id == itemId) {
                item.copy(status = ChecklistOrderItem.nextCycledStatus(item.status))
            } else item
        }
        commitChecklistOrders(updated)
    }

    fun setChecklistOrderStatus(itemId: String, status: String) {
        playSound(if (status == ChecklistOrderItem.STATUS_CHECK) "done" else "nav")
        val updated = _checklistOrders.value.map { item ->
            if (item.id == itemId) item.copy(status = status) else item
        }
        commitChecklistOrders(updated)
    }

    fun updateChecklistOrderText(itemId: String, newText: String) {
        val trimmed = newText.trim()
        if (trimmed.isEmpty()) return
        playSound("done")
        val updated = _checklistOrders.value.map { item ->
            if (item.id == itemId) item.copy(text = trimmed) else item
        }
        commitChecklistOrders(updated)
    }

    fun moveChecklistOrderItem(itemId: String, delta: Int) {
        val list = _checklistOrders.value.toMutableList()
        val idx = list.indexOfFirst { it.id == itemId }
        if (idx < 0) return
        val target = (idx + delta).coerceIn(0, list.lastIndex)
        if (target == idx) return
        playSound("nav")
        val moving = list.removeAt(idx)
        list.add(target, moving)
        commitChecklistOrders(list)
    }

    fun deleteChecklistOrderItem(itemId: String) {
        playSound("exit")
        val updated = _checklistOrders.value.filterNot { it.id == itemId }
        commitChecklistOrders(updated)
    }

    fun clearAllChecklistOrders() {
        playSound("exit")
        commitChecklistOrders(emptyList())
    }

    fun saveChecklistStyle(style: ChecklistStyleConfig) {
        playSound("done")
        val normalized = style.copy(
            circleSizeDp = style.circleSizeDp.coerceIn(22, 52),
            markSizeDp = style.markSizeDp.coerceIn(10, 34),
            itemSpacingDp = style.itemSpacingDp.coerceIn(4, 22)
        )
        _checklistStyle.value = normalized
        viewModelScope.launch {
            val res = localStore.saveChecklistStyle(normalized)
            if (res.isSuccess) {
                scheduleDebouncedAutoBackup()
                val lang = preferences.value.lang
                _toastMessage.tryEmit(
                    if (lang == "en") "Checkmark & X style saved!" else "تم حفظ تصميم علامات الصح والغلط!"
                )
            }
        }
    }

    suspend fun cropAndSavePhoto(
        uri: Uri,
        offsetXNorm: Float,
        offsetYNorm: Float,
        scale: Float,
        rotationDeg: Int
    ): Result<String> = mediaRepository.saveCroppedBitmap(uri, offsetXNorm, offsetYNorm, scale, rotationDeg)

    fun addPhotoToLibrary(name: String, filePath: String) {
        viewModelScope.launch {
            mediaRepository.addLibraryPhoto(name, filePath)
        }
    }

    fun toggleLibraryPhoto(id: String, enabled: Boolean) {
        playSound("nav")
        viewModelScope.launch {
            mediaRepository.toggleLibraryPhoto(id, enabled)
        }
    }

    fun removeLibraryPhoto(id: String) {
        playSound("exit")
        viewModelScope.launch {
            mediaRepository.removeLibraryPhoto(id)
        }
    }

    fun playGymTrack(trackId: String) {
        soundEngine.playGymTrackOnce(trackId)
    }

    fun playSavedRingtone(clip: TrimmedRingtone) {
        soundEngine.playSavedFileOnce(clip.filePath)
    }

    fun deleteSavedRingtone(clipId: String) {
        playSound("exit")
        viewModelScope.launch {
            mediaRepository.removeTrimmedRingtone(clipId)
            if (preferences.value.activeRingId == clipId) {
                updatePreference(mapOf("activeRingId" to "G1"))
            }
        }
    }

    fun trimAndSaveRingtone(
        uri: Uri,
        clipName: String,
        startSec: Int,
        durationSec: Int,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val outFile = mediaRepository.newRingtoneOutputFile()
            val result = soundEngine.trimAudioToWav(uri, startSec, durationSec, outFile)
            if (result.isSuccess) {
                val id = "r_${System.currentTimeMillis()}"
                val lang = preferences.value.lang
                val finalName = clipName.trim().ifEmpty { AppStrings.get(lang, "defaultClipName") }
                mediaRepository.addTrimmedRingtone(
                    TrimmedRingtone(
                        id = id,
                        name = finalName,
                        filePath = outFile.absolutePath,
                        durationSec = durationSec
                    )
                )
                updatePreference(mapOf("activeRingId" to id))
                playSound("done")
                onComplete(true)
            } else {
                _toastMessage.tryEmit(AppStrings.get(preferences.value.lang, "audioReadError"))
                onComplete(false)
            }
        }
    }

    fun buildBackupSnapshot(): BackupSnapshotPayload {
        val p = preferences.value
        val tasks = (tasksState.value as? UiState.Success)?.data ?: emptyList()
        val requests = (requestsState.value as? UiState.Success)?.data ?: emptyList()
        val isoNow = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
        return BackupSnapshotPayload(
            version = "1.0",
            savedAt = isoNow,
            preferences = BackupPreferencesDto(
                lang = p.lang,
                themeId = p.themeId,
                fontId = p.fontId,
                fontWeight = p.fontWeight,
                topBarStyle = p.topBarStyle,
                alertStyle = p.alertStyle,
                bannerStyle = p.bannerStyle,
                bannerIcon = p.bannerIcon,
                bannerTitle = p.bannerTitle,
                bannerSub = p.bannerSub,
                bannerImgSize = p.bannerImgSize,
                bannerImgDp = p.bannerImgDp,
                bannerPhotoShape = p.bannerPhotoShape,
                taskImgSize = p.taskImgSize,
                taskImgDp = p.taskImgDp,
                alertImgDp = p.alertImgDp,
                textScalePercent = p.textScalePercent,
                cardScalePercent = p.cardScalePercent,
                cardShape = p.cardShape,
                showBanner = p.showBanner,
                showTopBarTabs = p.showTopBarTabs,
                showStatsSummary = p.showStatsSummary,
                showWeekStrip = p.showWeekStrip,
                showSpotlightCard = p.showSpotlightCard,
                showFilterChips = p.showFilterChips,
                showSearchBar = p.showSearchBar,
                showQuickDock = p.showQuickDock,
                customPrimaryHex = p.customPrimaryHex,
                customBgHex = p.customBgHex,
                customAccentHex = p.customAccentHex,
                customIconShape = p.customIconShape,
                soundNav = p.soundNav,
                soundEnter = p.soundEnter,
                soundExit = p.soundExit,
                soundDone = p.soundDone,
                swipePages = p.swipePages,
                swipeClose = p.swipeClose,
                hapticEnabled = p.hapticEnabled,
                activeRingId = p.activeRingId,
                autoBackup = p.autoBackup,
                refreshRateHz = p.refreshRateHz,
                layoutStyle = p.layoutStyle,
                appIconId = p.appIconId,
                reqSignatureName = p.reqSignatureName,
                reqDefaultDocType = p.reqDefaultDocType,
                reqDefaultSplitMode = p.reqDefaultSplitMode,
                reqDefaultStatusLabel = p.reqDefaultStatusLabel,
                reqCustomStatuses = p.reqCustomStatuses,
                reqAutoSignOnComplete = p.reqAutoSignOnComplete,
                reqShowDocTabs = p.reqShowDocTabs,
                reqShowStamps = p.reqShowStamps,
                reqShowSignature = p.reqShowSignature,
                reqShowSplitControls = p.reqShowSplitControls,
                reqShowOrdersSection = p.reqShowOrdersSection,
                reqShowRedGreenDots = p.reqShowRedGreenDots,
                reqShowStatusBadge = p.reqShowStatusBadge,
                reqShowCustomer = p.reqShowCustomer,
                reqShowAmount = p.reqShowAmount,
                reqShowDate = p.reqShowDate,
                reqShowHeaderBadge = p.reqShowHeaderBadge,
                reqDefaultStampId = p.reqDefaultStampId,
                reminderBgThemeId = p.reminderBgThemeId,
                reminderDoodlePackId = p.reminderDoodlePackId,
                reminderDoodleOpacity = p.reminderDoodleOpacity,
                mainWallpaperUri = p.mainWallpaperUri,
                mainWallpaperDimPercent = p.mainWallpaperDimPercent
            ),
            tasks = tasks.map {
                BackupTaskDto(
                    title = it.title,
                    meta = it.meta,
                    imgUri = it.imgUri,
                    repeatMode = it.repeatMode,
                    cycleSteps = it.cycleSteps,
                    cycleIndex = it.cycleIndex,
                    weekDays = it.weekDays,
                    alertEnabled = it.alertEnabled,
                    alertTime = it.alertTime,
                    nextDueDate = it.nextDueDate,
                    lastAlertedDate = it.lastAlertedDate,
                    ringId = it.ringId,
                    sortOrder = it.sortOrder,
                    badgeIconId = it.badgeIconId,
                    badgeVisible = it.badgeVisible,
                    badgeOffsetX = it.badgeOffsetX,
                    badgeOffsetY = it.badgeOffsetY,
                    badgeSizeDp = it.badgeSizeDp,
                    badgeCategory = it.badgeCategory,
                    badgeSmartMode = it.badgeSmartMode,
                    badgeLocked = it.badgeLocked
                )
            },
            requests = requests.map {
                BackupRequestDto(
                    title = it.title,
                    body = it.body,
                    docType = it.docType,
                    customerName = it.customerName,
                    amountText = it.amountText,
                    stampId = it.stampId,
                    isSigned = it.isSigned,
                    signatureName = it.signatureName,
                    statusDot = it.statusDot,
                    statusLabel = it.statusLabel,
                    splitMode = it.splitMode,
                    isChecklistOrder = it.isChecklistOrder
                )
            },
            floatingBadges = floatingBadges.value,
            smartIconSettings = smartIconSettings.value,
            checklistOrders = checklistOrders.value,
            checklistStyle = checklistStyle.value
        )
    }

    fun serializeBackupJson(): String = json.encodeToString(buildBackupSnapshot())

    fun importBackupJson(rawJson: String) {
        viewModelScope.launch {
            val lang = preferences.value.lang
            val parsed = runCatching { json.decodeFromString<BackupSnapshotPayload>(rawJson) }
            if (parsed.isFailure) {
                _toastMessage.tryEmit(AppStrings.get(lang, "invalidFile"))
                return@launch
            }
            val snapshot = parsed.getOrThrow()
            _isBusyBackup.value = true
            val res = localStore.restoreFromSnapshot(snapshot)
            _isBusyBackup.value = false
            if (res.isSuccess) {
                lastSmartSettingsMutationAtMs = 0L
                _smartIconSettings.value = snapshot.smartIconSettings
                _checklistOrders.value = snapshot.checklistOrders
                _checklistStyle.value = snapshot.checklistStyle
                if (snapshot.floatingBadges.isNotEmpty()) {
                    _floatingBadges.value = snapshot.floatingBadges
                }
                playSound("done")
                _toastMessage.tryEmit(AppStrings.get(lang, "jsonImported"))
            } else {
                _toastMessage.tryEmit(res.exceptionOrNull()?.localizedMessage ?: "Restore failed")
            }
        }
    }

    fun runCloudBackup(isAuto: Boolean = false) {
        val user = auth.currentUser
        val lang = preferences.value.lang
        if (user == null) {
            if (!isAuto) {
                _toastMessage.tryEmit(AppStrings.get(lang, "connectGoogleFirst"))
            }
            return
        }
        viewModelScope.launch {
            if (!isAuto) _isBusyBackup.value = true
            val locale = if (lang == "ar") Locale.forLanguageTag("ar-EG") else Locale.US
            val nowLabel = SimpleDateFormat("yyyy-MM-dd HH:mm", locale).format(Date())
            val snap = buildBackupSnapshot()
            val label = user.email?.takeIf { it.isNotBlank() } ?: user.uid
            val res = repository.saveCloudBackup(
                userId = user.uid,
                accountLabel = label,
                clientTime = nowLabel,
                payload = snap
            )
            if (res.isSuccess) {
                _lastBackupStatus.value = "success"
                _lastBackupError.value = null
                localStore.updatePreferences(mapOf("lastBackupLabel" to nowLabel))
                if (!isAuto) {
                    playSound("done")
                    _toastMessage.tryEmit(AppStrings.get(lang, "backupSaved"))
                }
            } else {
                val errMsg = res.exceptionOrNull()?.localizedMessage ?: "Cloud backup failed"
                _lastBackupStatus.value = "error"
                _lastBackupError.value = errMsg
                if (!isAuto) {
                    _toastMessage.tryEmit(errMsg)
                }
            }
            if (!isAuto) _isBusyBackup.value = false
        }
    }

    fun restoreLatestCloudBackup() {
        val user = auth.currentUser
        val lang = preferences.value.lang
        if (user == null) {
            _toastMessage.tryEmit(AppStrings.get(lang, "connectGoogleFirst"))
            return
        }
        viewModelScope.launch {
            _isBusyBackup.value = true
            val fetched = repository.fetchLatestCloudBackup(user.uid)
            val record = fetched.getOrNull()
            if (fetched.isFailure) {
                val errMsg = fetched.exceptionOrNull()?.localizedMessage ?: "Restore failed"
                _lastBackupStatus.value = "error"
                _lastBackupError.value = errMsg
                _toastMessage.tryEmit(errMsg)
            } else if (record == null || record.snapshotJson.isBlank()) {
                _toastMessage.tryEmit(AppStrings.get(lang, "noBackupFound"))
            } else {
                val parsed = runCatching {
                    json.decodeFromString<BackupSnapshotPayload>(record.snapshotJson)
                }
                if (parsed.isSuccess) {
                    val snapshot = parsed.getOrThrow()
                    val res = localStore.restoreFromSnapshot(snapshot)
                    if (res.isSuccess) {
                        if (snapshot.floatingBadges.isNotEmpty()) {
                            _floatingBadges.value = snapshot.floatingBadges
                        }
                        lastSmartSettingsMutationAtMs = 0L
                        _smartIconSettings.value = snapshot.smartIconSettings
                        _checklistOrders.value = snapshot.checklistOrders
                        _checklistStyle.value = snapshot.checklistStyle
                        _lastBackupStatus.value = "success"
                        _lastBackupError.value = null
                        playSound("done")
                        _toastMessage.tryEmit(AppStrings.get(lang, "backupRestored"))
                    } else {
                        val errMsg = res.exceptionOrNull()?.localizedMessage ?: "Restore failed"
                        _lastBackupStatus.value = "error"
                        _lastBackupError.value = errMsg
                        _toastMessage.tryEmit(errMsg)
                    }
                } else {
                    _lastBackupStatus.value = "error"
                    _lastBackupError.value = AppStrings.get(lang, "invalidFile")
                    _toastMessage.tryEmit(AppStrings.get(lang, "invalidFile"))
                }
            }
            _isBusyBackup.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authListener)
        soundEngine.stopRingtone()
    }
}
