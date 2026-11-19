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
import com.example.model.FloatingBadgeItem
import com.example.model.LibraryPhoto
import com.example.model.QuickRequest
import com.example.model.RepeatMode
import com.example.model.SmartTask
import com.example.model.TrimmedRingtone
import com.example.model.UserPreferences
import com.example.util.LauncherIconManager
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.CancellationException
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

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

class MNotesViewModel(
    private val localStore: LocalStoreRepository,
    private val repository: MNotesRepository,
    private val mediaRepository: LocalMediaRepository,
    val soundEngine: SoundEngine
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

    val tasksState: StateFlow<UiState<List<SmartTask>>> = localStore.tasksFlow
        .map<List<SmartTask>, UiState<List<SmartTask>>> { UiState.Success(it) }
        .catch { error ->
            if (error is CancellationException) throw error
            emit(UiState.Error(error.message ?: "Failed to load tasks"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

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

    val floatingBadges: StateFlow<List<FloatingBadgeItem>> = localStore.floatingBadgesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

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

    init {
        auth.addAuthStateListener(authListener)
        viewModelScope.launch {
            localStore.ensureInitialized()
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
        triggerAlertAfterSave: Boolean = false
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
            sortOrder = editingTask?.sortOrder ?: System.currentTimeMillis()
        )

        viewModelScope.launch {
            val res = if (editingTask != null && editingTask.id.isNotBlank()) {
                localStore.updateTaskFull(editingTask.id, payload).map { editingTask.id }
            } else {
                localStore.createTask(payload)
            }
            if (res.isFailure) {
                _toastMessage.tryEmit(res.exceptionOrNull()?.localizedMessage ?: "Failed to save task")
            } else {
                if (triggerAlertAfterSave) {
                    val savedId = res.getOrNull().orEmpty()
                    showAlert(payload.copy(id = savedId), previewOnly = false)
                }
                if (preferences.value.autoBackup && auth.currentUser != null) {
                    runCloudBackup(isAuto = true)
                }
            }
        }
    }

    fun updateTaskPhoto(taskId: String, imgUri: String) {
        playSound("done")
        viewModelScope.launch {
            val res = localStore.updateTaskPhoto(taskId, imgUri)
            if (res.isFailure) {
                _toastMessage.tryEmit(res.exceptionOrNull()?.localizedMessage ?: "Failed to update photo")
            }
        }
    }

    fun deleteTask(taskId: String) {
        playSound("exit")
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

    fun addFloatingBadge(stateId: String = "sleeping", sizeDp: Float = 46f) {
        playSound("done")
        viewModelScope.launch {
            localStore.addFloatingBadge(stateId = stateId, sizeDp = sizeDp)
        }
    }

    fun duplicateFloatingBadge(badgeId: String) {
        playSound("done")
        viewModelScope.launch {
            localStore.duplicateFloatingBadge(badgeId)
        }
    }

    fun updateFloatingBadgePosition(badgeId: String, xFraction: Float, yFraction: Float) {
        viewModelScope.launch {
            localStore.updateFloatingBadge(badgeId) { cur ->
                cur.copy(xFraction = xFraction, yFraction = yFraction)
            }
        }
    }

    fun updateFloatingBadgeSize(badgeId: String, sizeDp: Float) {
        viewModelScope.launch {
            localStore.updateFloatingBadge(badgeId) { cur ->
                cur.copy(sizeDp = sizeDp)
            }
        }
    }

    fun updateFloatingBadgeState(badgeId: String, stateId: String) {
        playSound("nav")
        viewModelScope.launch {
            localStore.updateFloatingBadge(badgeId) { cur ->
                cur.copy(state = stateId)
            }
        }
    }

    fun toggleFloatingBadgeVisibility(badgeId: String) {
        playSound("nav")
        viewModelScope.launch {
            localStore.updateFloatingBadge(badgeId) { cur ->
                cur.copy(visible = !cur.visible)
            }
        }
    }

    fun toggleFloatingBadgeLock(badgeId: String) {
        playSound("nav")
        viewModelScope.launch {
            localStore.updateFloatingBadge(badgeId) { cur ->
                cur.copy(locked = !cur.locked)
            }
        }
    }

    fun setAllFloatingBadgesVisibility(visible: Boolean) {
        playSound("nav")
        viewModelScope.launch {
            localStore.setAllFloatingBadgesVisibility(visible)
        }
    }

    fun deleteFloatingBadge(badgeId: String) {
        playSound("exit")
        viewModelScope.launch {
            localStore.deleteFloatingBadge(badgeId)
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
                reqDefaultStampId = p.reqDefaultStampId
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
                    sortOrder = it.sortOrder
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
            floatingBadges = floatingBadges.value
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
            _isBusyBackup.value = true
            val res = localStore.restoreFromSnapshot(parsed.getOrThrow())
            _isBusyBackup.value = false
            if (res.isSuccess) {
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
                localStore.updatePreferences(mapOf("lastBackupLabel" to nowLabel))
                if (!isAuto) {
                    playSound("done")
                    _toastMessage.tryEmit(AppStrings.get(lang, "backupSaved"))
                }
            } else if (!isAuto) {
                _toastMessage.tryEmit(res.exceptionOrNull()?.localizedMessage ?: "Cloud backup failed")
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
                _toastMessage.tryEmit(fetched.exceptionOrNull()?.localizedMessage ?: "Restore failed")
            } else if (record == null || record.snapshotJson.isBlank()) {
                _toastMessage.tryEmit(AppStrings.get(lang, "noBackupFound"))
            } else {
                val parsed = runCatching {
                    json.decodeFromString<BackupSnapshotPayload>(record.snapshotJson)
                }
                if (parsed.isSuccess) {
                    val res = localStore.restoreFromSnapshot(parsed.getOrThrow())
                    if (res.isSuccess) {
                        playSound("done")
                        _toastMessage.tryEmit(AppStrings.get(lang, "backupRestored"))
                    } else {
                        _toastMessage.tryEmit(res.exceptionOrNull()?.localizedMessage ?: "Restore failed")
                    }
                } else {
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
