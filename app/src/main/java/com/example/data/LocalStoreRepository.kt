package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.localization.AppStrings
import com.example.model.BackupPreferencesDto
import com.example.model.BackupRequestDto
import com.example.model.BackupSnapshotPayload
import com.example.model.BackupTaskDto
import com.example.model.QuickRequest
import com.example.model.RepeatMode
import com.example.model.SmartTask
import com.example.model.UserPreferences
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

private val Context.workspaceDataStore by preferencesDataStore(name = "mnotes_local_workspace")

@Serializable
private data class LocalTaskDto(
    val id: String,
    val title: String,
    val meta: String = "",
    val imgUri: String = "",
    val repeatMode: String = RepeatMode.NONE.wire,
    val cycleSteps: List<String> = emptyList(),
    val cycleIndex: Long = 0L,
    val weekDays: List<Long> = emptyList(),
    val alertEnabled: Boolean = true,
    val alertTime: String = "",
    val nextDueDate: String = "",
    val lastAlertedDate: String = "",
    val sortOrder: Long = 0L
)

@Serializable
private data class LocalRequestDto(
    val id: String,
    val title: String,
    val body: String = "",
    val createdAtMs: Long = 0L
)

@Serializable
private data class LocalWorkspaceState(
    val initialized: Boolean = false,
    val preferences: BackupPreferencesDto = BackupPreferencesDto(),
    val lastBackupLabel: String = "",
    val bannerImg: String = "default",
    val tasks: List<LocalTaskDto> = emptyList(),
    val requests: List<LocalRequestDto> = emptyList()
)

class LocalStoreRepository(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }
    private val keyWorkspace = stringPreferencesKey("workspace_state_json")

    private fun decodeState(raw: String?): LocalWorkspaceState {
        if (raw.isNullOrBlank()) return LocalWorkspaceState()
        return runCatching { json.decodeFromString<LocalWorkspaceState>(raw) }
            .getOrDefault(LocalWorkspaceState())
    }

    private val stateFlow: Flow<LocalWorkspaceState> = context.workspaceDataStore.data.map { prefs ->
        decodeState(prefs[keyWorkspace])
    }

    val preferencesFlow: Flow<UserPreferences> = stateFlow.map { state ->
        val p = state.preferences
        UserPreferences(
            userId = "local",
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
            bannerImg = state.bannerImg,
            bannerImgSize = p.bannerImgSize,
            bannerPhotoShape = p.bannerPhotoShape,
            taskImgSize = p.taskImgSize,
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
            lastBackupLabel = state.lastBackupLabel
        )
    }

    val tasksFlow: Flow<List<SmartTask>> = stateFlow.map { state ->
        state.tasks
            .sortedByDescending { it.sortOrder }
            .map {
                SmartTask(
                    id = it.id,
                    userId = "local",
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
                    sortOrder = it.sortOrder
                )
            }
    }

    val requestsFlow: Flow<List<QuickRequest>> = stateFlow.map { state ->
        state.requests
            .sortedByDescending { it.createdAtMs }
            .map {
                QuickRequest(
                    id = it.id,
                    userId = "local",
                    title = it.title,
                    body = it.body
                )
            }
    }

    suspend fun ensureInitialized(): Result<Unit> = runWorkspaceEdit { state ->
        if (state.initialized) return@runWorkspaceEdit state
        val today = AppStrings.todayStr()
        val nowOrder = System.currentTimeMillis()
        val starter1 = LocalTaskDto(
            id = UUID.randomUUID().toString(),
            title = "تمرين الجيم",
            meta = "11:00 م",
            imgUri = "",
            repeatMode = RepeatMode.CYCLE.wire,
            cycleSteps = listOf("رجل", "أماميات", "خلفيات"),
            cycleIndex = 0L,
            weekDays = emptyList(),
            alertEnabled = true,
            alertTime = "23:00",
            nextDueDate = today,
            lastAlertedDate = "",
            sortOrder = nowOrder + 1
        )
        val starter2 = LocalTaskDto(
            id = UUID.randomUUID().toString(),
            title = "قراءة وأذكار اليوم",
            meta = "9:00 م",
            imgUri = "",
            repeatMode = RepeatMode.DAILY.wire,
            cycleSteps = emptyList(),
            cycleIndex = 0L,
            weekDays = emptyList(),
            alertEnabled = true,
            alertTime = "21:00",
            nextDueDate = today,
            lastAlertedDate = "",
            sortOrder = nowOrder
        )
        state.copy(
            initialized = true,
            tasks = listOf(starter1, starter2)
        )
    }

    suspend fun updatePreferences(updates: Map<String, Any>): Result<Unit> = runWorkspaceEdit { state ->
        var p = state.preferences
        var bannerImg = state.bannerImg
        var lastBackupLabel = state.lastBackupLabel

        updates.forEach { (k, v) ->
            when (k) {
                "lang" -> p = p.copy(lang = (v as? String)?.takeIf { it == "en" } ?: "ar")
                "themeId" -> (v as? String)?.let { p = p.copy(themeId = it.take(40)) }
                "fontId" -> (v as? String)?.let { p = p.copy(fontId = it.take(40)) }
                "fontWeight" -> (v as? Number)?.toLong()?.let { p = p.copy(fontWeight = it) }
                "topBarStyle" -> (v as? String)?.let { p = p.copy(topBarStyle = it) }
                "alertStyle" -> (v as? String)?.let { p = p.copy(alertStyle = it) }
                "bannerStyle" -> (v as? String)?.let { p = p.copy(bannerStyle = it) }
                "bannerIcon" -> (v as? String)?.let { p = p.copy(bannerIcon = it) }
                "bannerTitle" -> (v as? String)?.let { p = p.copy(bannerTitle = it.take(100)) }
                "bannerSub" -> (v as? String)?.let { p = p.copy(bannerSub = it.take(200)) }
                "bannerImg" -> (v as? String)?.let { bannerImg = it.take(2000) }
                "bannerImgSize" -> (v as? String)?.let { p = p.copy(bannerImgSize = it) }
                "bannerPhotoShape" -> (v as? String)?.let { p = p.copy(bannerPhotoShape = it) }
                "taskImgSize" -> (v as? String)?.let { p = p.copy(taskImgSize = it) }
                "cardShape" -> (v as? String)?.let { p = p.copy(cardShape = it) }
                "showBanner" -> (v as? Boolean)?.let { p = p.copy(showBanner = it) }
                "showTopBarTabs" -> (v as? Boolean)?.let { p = p.copy(showTopBarTabs = it) }
                "showStatsSummary" -> (v as? Boolean)?.let { p = p.copy(showStatsSummary = it) }
                "showWeekStrip" -> (v as? Boolean)?.let { p = p.copy(showWeekStrip = it) }
                "showSpotlightCard" -> (v as? Boolean)?.let { p = p.copy(showSpotlightCard = it) }
                "showFilterChips" -> (v as? Boolean)?.let { p = p.copy(showFilterChips = it) }
                "showSearchBar" -> (v as? Boolean)?.let { p = p.copy(showSearchBar = it) }
                "showQuickDock" -> (v as? Boolean)?.let { p = p.copy(showQuickDock = it) }
                "customPrimaryHex" -> (v as? String)?.let { p = p.copy(customPrimaryHex = it.take(12)) }
                "customBgHex" -> (v as? String)?.let { p = p.copy(customBgHex = it.take(12)) }
                "customAccentHex" -> (v as? String)?.let { p = p.copy(customAccentHex = it.take(12)) }
                "customIconShape" -> (v as? String)?.let { p = p.copy(customIconShape = it.take(20)) }
                "soundNav" -> (v as? Boolean)?.let { p = p.copy(soundNav = it) }
                "soundEnter" -> (v as? Boolean)?.let { p = p.copy(soundEnter = it) }
                "soundExit" -> (v as? Boolean)?.let { p = p.copy(soundExit = it) }
                "soundDone" -> (v as? Boolean)?.let { p = p.copy(soundDone = it) }
                "swipePages" -> (v as? Boolean)?.let { p = p.copy(swipePages = it) }
                "swipeClose" -> (v as? Boolean)?.let { p = p.copy(swipeClose = it) }
                "hapticEnabled" -> (v as? Boolean)?.let { p = p.copy(hapticEnabled = it) }
                "activeRingId" -> (v as? String)?.let { p = p.copy(activeRingId = it.take(100)) }
                "autoBackup" -> (v as? Boolean)?.let { p = p.copy(autoBackup = it) }
                "refreshRateHz" -> (v as? Number)?.toInt()?.takeIf { it in setOf(60, 90, 120) }?.let {
                    p = p.copy(refreshRateHz = it)
                }
                "layoutStyle" -> (v as? String)?.takeIf { it in setOf("L1", "L2", "L3") }?.let {
                    p = p.copy(layoutStyle = it)
                }
                "appIconId" -> (v as? String)?.let { p = p.copy(appIconId = it.take(12)) }
                "lastBackupLabel" -> (v as? String)?.let { lastBackupLabel = it.take(100) }
            }
        }
        state.copy(
            initialized = true,
            preferences = p,
            bannerImg = bannerImg,
            lastBackupLabel = lastBackupLabel
        )
    }

    suspend fun createTask(task: SmartTask): Result<String> {
        val newId = UUID.randomUUID().toString()
        val res = runWorkspaceEdit { state ->
            val dto = LocalTaskDto(
                id = newId,
                title = task.title.trim().take(200),
                meta = task.meta.trim().take(300),
                imgUri = task.imgUri.take(2000),
                repeatMode = task.repeatMode,
                cycleSteps = task.cycleSteps.take(20),
                cycleIndex = task.cycleIndex.coerceIn(0L, 1000L),
                weekDays = task.weekDays.take(7),
                alertEnabled = task.alertEnabled,
                alertTime = task.alertTime.take(10),
                nextDueDate = task.nextDueDate.take(20),
                lastAlertedDate = task.lastAlertedDate.take(20),
                sortOrder = task.sortOrder
            )
            state.copy(initialized = true, tasks = listOf(dto) + state.tasks)
        }
        return if (res.isSuccess) Result.success(newId) else Result.failure(res.exceptionOrNull()!!)
    }

    suspend fun updateTaskFull(taskId: String, task: SmartTask): Result<Unit> = runWorkspaceEdit { state ->
        val cleanTitle = task.title.trim().take(200)
        require(cleanTitle.isNotEmpty()) { "Title must not be empty" }
        val updated = state.tasks.map { existing ->
            if (existing.id == taskId) {
                existing.copy(
                    title = cleanTitle,
                    meta = task.meta.trim().take(300),
                    imgUri = task.imgUri.take(2000),
                    repeatMode = task.repeatMode,
                    cycleSteps = task.cycleSteps.take(20),
                    cycleIndex = task.cycleIndex.coerceIn(0L, 1000L),
                    weekDays = task.weekDays.take(7),
                    alertEnabled = task.alertEnabled,
                    alertTime = task.alertTime.take(10),
                    nextDueDate = task.nextDueDate.take(20),
                    lastAlertedDate = task.lastAlertedDate.take(20)
                )
            } else existing
        }
        state.copy(tasks = updated)
    }

    suspend fun advanceTaskSchedule(
        taskId: String,
        cycleIndex: Long,
        nextDueDate: String,
        lastAlertedDate: String
    ): Result<Unit> = runWorkspaceEdit { state ->
        val updated = state.tasks.map { existing ->
            if (existing.id == taskId) {
                existing.copy(
                    cycleIndex = cycleIndex.coerceIn(0L, 1000L),
                    nextDueDate = nextDueDate.take(20),
                    lastAlertedDate = lastAlertedDate.take(20)
                )
            } else existing
        }
        state.copy(tasks = updated)
    }

    suspend fun updateTaskPhoto(taskId: String, imgUri: String): Result<Unit> = runWorkspaceEdit { state ->
        val updated = state.tasks.map { existing ->
            if (existing.id == taskId) existing.copy(imgUri = imgUri.take(2000)) else existing
        }
        state.copy(tasks = updated)
    }

    suspend fun markTaskAlerted(taskId: String, dateStr: String): Result<Unit> = runWorkspaceEdit { state ->
        val updated = state.tasks.map { existing ->
            if (existing.id == taskId) existing.copy(lastAlertedDate = dateStr.take(20)) else existing
        }
        state.copy(tasks = updated)
    }

    suspend fun deleteTask(taskId: String): Result<Unit> = runWorkspaceEdit { state ->
        state.copy(tasks = state.tasks.filterNot { it.id == taskId })
    }

    suspend fun createRequest(title: String, body: String): Result<Unit> = runWorkspaceEdit { state ->
        val cleanTitle = title.trim().take(200)
        require(cleanTitle.isNotEmpty()) { "Request title must not be empty" }
        val dto = LocalRequestDto(
            id = UUID.randomUUID().toString(),
            title = cleanTitle,
            body = body.trim().take(4000),
            createdAtMs = System.currentTimeMillis()
        )
        state.copy(initialized = true, requests = listOf(dto) + state.requests)
    }

    suspend fun deleteRequest(requestId: String): Result<Unit> = runWorkspaceEdit { state ->
        state.copy(requests = state.requests.filterNot { it.id == requestId })
    }

    suspend fun restoreFromSnapshot(snapshot: BackupSnapshotPayload): Result<Unit> = runWorkspaceEdit { state ->
        val p = snapshot.preferences
        val normalizedPrefs = p.copy(
            lang = if (p.lang == "en") "en" else "ar",
            themeId = p.themeId.take(40),
            fontId = p.fontId.take(40),
            fontWeight = if (p.fontWeight in listOf(400L, 600L, 800L)) p.fontWeight else 600L,
            bannerTitle = p.bannerTitle.take(100),
            bannerSub = p.bannerSub.take(200),
            activeRingId = p.activeRingId.take(100),
            refreshRateHz = if (p.refreshRateHz in setOf(60, 90, 120)) p.refreshRateHz else 120,
            layoutStyle = if (p.layoutStyle in setOf("L1", "L2", "L3")) p.layoutStyle else "L1",
            appIconId = p.appIconId.ifBlank { "I1" }.take(12)
        )
        val restoredTasks = snapshot.tasks.mapIndexed { idx, t: BackupTaskDto ->
            LocalTaskDto(
                id = UUID.randomUUID().toString(),
                title = t.title.trim().take(200).ifEmpty { "Task" },
                meta = t.meta.take(300),
                imgUri = t.imgUri.take(2000),
                repeatMode = RepeatMode.fromWire(t.repeatMode).wire,
                cycleSteps = t.cycleSteps.take(20),
                cycleIndex = t.cycleIndex.coerceIn(0L, 1000L),
                weekDays = t.weekDays.take(7),
                alertEnabled = t.alertEnabled,
                alertTime = t.alertTime.take(10),
                nextDueDate = t.nextDueDate.take(20),
                lastAlertedDate = t.lastAlertedDate.take(20),
                sortOrder = if (t.sortOrder > 0L) t.sortOrder else (System.currentTimeMillis() - idx)
            )
        }
        val baseTime = System.currentTimeMillis()
        val restoredReqs = snapshot.requests.mapIndexed { idx, r: BackupRequestDto ->
            LocalRequestDto(
                id = UUID.randomUUID().toString(),
                title = r.title.trim().take(200).ifEmpty { "Request" },
                body = r.body.take(4000),
                createdAtMs = baseTime - idx
            )
        }
        state.copy(
            initialized = true,
            preferences = normalizedPrefs,
            tasks = restoredTasks,
            requests = restoredReqs
        )
    }

    private suspend fun runWorkspaceEdit(transform: (LocalWorkspaceState) -> LocalWorkspaceState): Result<Unit> {
        return try {
            context.workspaceDataStore.edit { prefs ->
                val current = decodeState(prefs[keyWorkspace])
                val updated = transform(current)
                prefs[keyWorkspace] = json.encodeToString(updated)
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
