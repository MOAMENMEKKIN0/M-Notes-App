package com.example.data

import com.example.localization.AppStrings
import com.example.model.BackupSnapshotPayload
import com.example.model.CloudBackupRecord
import com.example.model.QuickRequest
import com.example.model.RepeatMode
import com.example.model.SmartTask
import com.example.model.UserPreferences
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MNotesRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    private val json = Json { ignoreUnknownKeys = true }

    private fun requireActiveUserId(expectedUid: String): String {
        val uid = auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
        check(uid == expectedUid) { "Signed-in user changed; refusing cross-user access." }
        return uid
    }

    fun observePreferences(userId: String): Flow<UserPreferences> =
        db.collection("users").document(userId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObject(
                    UserPreferences::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ) ?: UserPreferences(userId = userId)
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                if (auth.currentUser?.uid == userId) {
                    handleFirestoreError(firestoreError ?: error, OperationType.GET, "users/$userId")
                }
                throw error
            }

    fun observeTasks(userId: String): Flow<List<SmartTask>> =
        db.collection("users").document(userId).collection("tasks")
            .orderBy("sortOrder", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    SmartTask::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                if (auth.currentUser?.uid == userId) {
                    handleFirestoreError(firestoreError ?: error, OperationType.LIST, "users/$userId/tasks")
                }
                throw error
            }

    fun observeRequests(userId: String): Flow<List<QuickRequest>> =
        db.collection("users").document(userId).collection("requests")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    QuickRequest::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                if (auth.currentUser?.uid == userId) {
                    handleFirestoreError(firestoreError ?: error, OperationType.LIST, "users/$userId/requests")
                }
                throw error
            }

    suspend fun ensureInitialized(userId: String): Result<Unit> {
        val prefPath = "users/$userId"
        return try {
            requireActiveUserId(userId)
            val prefRef = db.collection("users").document(userId)
            val snap = prefRef.get().await()
            if (!snap.exists()) {
                val defaults = UserPreferences(userId = userId)
                prefRef.set(preferencesCreateMap(userId, defaults)).await()

                // Seed starter tasks matching the original M Notes template on first login
                val today = AppStrings.todayStr()
                val nowOrder = System.currentTimeMillis()
                val starter1 = SmartTask(
                    userId = userId,
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
                val starter2 = SmartTask(
                    userId = userId,
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
                val tasksCol = prefRef.collection("tasks")
                tasksCol.document().set(taskCreateMap(userId, starter1)).await()
                tasksCol.document().set(taskCreateMap(userId, starter2)).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, prefPath)
            Result.failure(e)
        }
    }

    suspend fun updatePreferences(userId: String, updates: Map<String, Any>): Result<Unit> {
        val path = "users/$userId"
        return try {
            requireActiveUserId(userId)
            val ref = db.collection("users").document(userId)
            val snap = ref.get().await()
            if (!snap.exists()) {
                ref.set(preferencesCreateMap(userId, UserPreferences(userId = userId))).await()
            }
            val payload = updates.toMutableMap().apply {
                this["updatedAt"] = FieldValue.serverTimestamp()
            }
            ref.update(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun createTask(userId: String, task: SmartTask): Result<String> {
        var docPath = "users/$userId/tasks"
        return try {
            requireActiveUserId(userId)
            val docRef = db.collection("users").document(userId).collection("tasks").document()
            docPath = docRef.path
            docRef.set(taskCreateMap(userId, task)).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateTaskFull(userId: String, taskId: String, task: SmartTask): Result<Unit> {
        val docPath = "users/$userId/tasks/$taskId"
        return try {
            requireActiveUserId(userId)
            require(taskId.isNotBlank()) { "Task ID must not be blank" }
            val cleanTitle = task.title.trim().take(200)
            require(cleanTitle.isNotEmpty()) { "Title must not be empty" }
            db.collection("users").document(userId).collection("tasks").document(taskId)
                .update(
                    mapOf(
                        "title" to cleanTitle,
                        "meta" to task.meta.trim().take(300),
                        "imgUri" to task.imgUri.take(2000),
                        "repeatMode" to task.repeatMode,
                        "cycleSteps" to task.cycleSteps.take(20),
                        "cycleIndex" to task.cycleIndex.coerceIn(0L, 1000L),
                        "weekDays" to task.weekDays.take(7),
                        "alertEnabled" to task.alertEnabled,
                        "alertTime" to task.alertTime.take(10),
                        "nextDueDate" to task.nextDueDate.take(20),
                        "lastAlertedDate" to task.lastAlertedDate.take(20),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun advanceTaskSchedule(
        userId: String,
        taskId: String,
        cycleIndex: Long,
        nextDueDate: String,
        lastAlertedDate: String
    ): Result<Unit> {
        val docPath = "users/$userId/tasks/$taskId"
        return try {
            requireActiveUserId(userId)
            require(taskId.isNotBlank()) { "Task ID must not be blank" }
            db.collection("users").document(userId).collection("tasks").document(taskId)
                .update(
                    mapOf(
                        "cycleIndex" to cycleIndex.coerceIn(0L, 1000L),
                        "nextDueDate" to nextDueDate.take(20),
                        "lastAlertedDate" to lastAlertedDate.take(20),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateTaskPhoto(userId: String, taskId: String, imgUri: String): Result<Unit> {
        val docPath = "users/$userId/tasks/$taskId"
        return try {
            requireActiveUserId(userId)
            require(taskId.isNotBlank()) { "Task ID must not be blank" }
            db.collection("users").document(userId).collection("tasks").document(taskId)
                .update(
                    mapOf(
                        "imgUri" to imgUri.take(2000),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun markTaskAlerted(userId: String, taskId: String, dateStr: String): Result<Unit> {
        val docPath = "users/$userId/tasks/$taskId"
        return try {
            requireActiveUserId(userId)
            require(taskId.isNotBlank()) { "Task ID must not be blank" }
            db.collection("users").document(userId).collection("tasks").document(taskId)
                .update(
                    mapOf(
                        "lastAlertedDate" to dateStr.take(20),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteTask(userId: String, taskId: String): Result<Unit> {
        val docPath = "users/$userId/tasks/$taskId"
        return try {
            requireActiveUserId(userId)
            require(taskId.isNotBlank()) { "Task ID must not be blank" }
            db.collection("users").document(userId).collection("tasks").document(taskId)
                .delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    suspend fun createRequest(userId: String, title: String, body: String): Result<Unit> {
        var docPath = "users/$userId/requests"
        return try {
            requireActiveUserId(userId)
            val cleanTitle = title.trim().take(200)
            require(cleanTitle.isNotEmpty()) { "Request title must not be empty" }
            val docRef = db.collection("users").document(userId).collection("requests").document()
            docPath = docRef.path
            docRef.set(
                mapOf(
                    "userId" to userId,
                    "title" to cleanTitle,
                    "body" to body.trim().take(4000),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteRequest(userId: String, requestId: String): Result<Unit> {
        val docPath = "users/$userId/requests/$requestId"
        return try {
            requireActiveUserId(userId)
            require(requestId.isNotBlank()) { "Request ID must not be blank" }
            db.collection("users").document(userId).collection("requests").document(requestId)
                .delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    suspend fun saveCloudBackup(
        userId: String,
        accountLabel: String,
        clientTime: String,
        payload: BackupSnapshotPayload
    ): Result<Unit> {
        val docPath = "users/$userId/backups/latest"
        return try {
            requireActiveUserId(userId)
            val serialized = json.encodeToString(payload).take(500000)
            val docRef = db.collection("users").document(userId).collection("backups").document("latest")
            val existing = docRef.get().await()
            if (existing.exists()) {
                docRef.update(
                    mapOf(
                        "accountLabel" to accountLabel.take(200),
                        "clientTime" to clientTime.take(100),
                        "tasksCount" to payload.tasks.size.toLong(),
                        "requestsCount" to payload.requests.size.toLong(),
                        "snapshotJson" to serialized,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.set(
                    mapOf(
                        "userId" to userId,
                        "accountLabel" to accountLabel.take(200),
                        "clientTime" to clientTime.take(100),
                        "tasksCount" to payload.tasks.size.toLong(),
                        "requestsCount" to payload.requests.size.toLong(),
                        "snapshotJson" to serialized,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    suspend fun fetchLatestCloudBackup(userId: String): Result<CloudBackupRecord?> {
        val docPath = "users/$userId/backups/latest"
        return try {
            requireActiveUserId(userId)
            val snap = db.collection("users").document(userId).collection("backups").document("latest")
                .get().await()
            if (!snap.exists()) {
                Result.success(null)
            } else {
                val record = snap.toObject(
                    CloudBackupRecord::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
                Result.success(record)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docPath)
            Result.failure(e)
        }
    }

    suspend fun restoreFromSnapshot(userId: String, snapshot: BackupSnapshotPayload): Result<Unit> {
        val userPath = "users/$userId"
        return try {
            requireActiveUserId(userId)
            val userRef = db.collection("users").document(userId)
            val p = snapshot.preferences
            updatePreferences(
                userId,
                mapOf(
                    "lang" to (if (p.lang == "en") "en" else "ar"),
                    "themeId" to p.themeId.take(40),
                    "fontId" to p.fontId.take(40),
                    "fontWeight" to (if (p.fontWeight in listOf(400L, 600L, 800L)) p.fontWeight else 600L),
                    "topBarStyle" to p.topBarStyle,
                    "alertStyle" to p.alertStyle,
                    "bannerStyle" to p.bannerStyle,
                    "bannerIcon" to p.bannerIcon,
                    "bannerTitle" to p.bannerTitle.take(100),
                    "bannerSub" to p.bannerSub.take(200),
                    "bannerImgSize" to p.bannerImgSize,
                    "bannerPhotoShape" to p.bannerPhotoShape,
                    "taskImgSize" to p.taskImgSize,
                    "cardShape" to p.cardShape,
                    "showBanner" to p.showBanner,
                    "soundNav" to p.soundNav,
                    "soundEnter" to p.soundEnter,
                    "soundExit" to p.soundExit,
                    "soundDone" to p.soundDone,
                    "swipePages" to p.swipePages,
                    "swipeClose" to p.swipeClose,
                    "hapticEnabled" to p.hapticEnabled,
                    "activeRingId" to p.activeRingId.take(100),
                    "autoBackup" to p.autoBackup
                )
            ).getOrThrow()

            // Replace tasks & requests cleanly
            val existingTasks = userRef.collection("tasks").get().await()
            for (doc in existingTasks.documents) {
                doc.reference.delete().await()
            }
            for (t in snapshot.tasks) {
                val model = SmartTask(
                    userId = userId,
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
                    sortOrder = t.sortOrder
                )
                userRef.collection("tasks").document().set(taskCreateMap(userId, model)).await()
            }

            val existingReqs = userRef.collection("requests").get().await()
            for (doc in existingReqs.documents) {
                doc.reference.delete().await()
            }
            for (r in snapshot.requests) {
                userRef.collection("requests").document().set(
                    mapOf(
                        "userId" to userId,
                        "title" to r.title.trim().take(200).ifEmpty { "Request" },
                        "body" to r.body.take(4000),
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }

            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, userPath)
            Result.failure(e)
        }
    }

    private fun preferencesCreateMap(userId: String, prefs: UserPreferences): Map<String, Any> =
        mapOf(
            "userId" to userId,
            "lang" to prefs.lang,
            "themeId" to prefs.themeId,
            "fontId" to prefs.fontId,
            "fontWeight" to prefs.fontWeight,
            "topBarStyle" to prefs.topBarStyle,
            "alertStyle" to prefs.alertStyle,
            "bannerStyle" to prefs.bannerStyle,
            "bannerIcon" to prefs.bannerIcon,
            "bannerTitle" to prefs.bannerTitle,
            "bannerSub" to prefs.bannerSub,
            "bannerImg" to prefs.bannerImg,
            "bannerImgSize" to prefs.bannerImgSize,
            "bannerPhotoShape" to prefs.bannerPhotoShape,
            "taskImgSize" to prefs.taskImgSize,
            "cardShape" to prefs.cardShape,
            "showBanner" to prefs.showBanner,
            "soundNav" to prefs.soundNav,
            "soundEnter" to prefs.soundEnter,
            "soundExit" to prefs.soundExit,
            "soundDone" to prefs.soundDone,
            "swipePages" to prefs.swipePages,
            "swipeClose" to prefs.swipeClose,
            "hapticEnabled" to prefs.hapticEnabled,
            "activeRingId" to prefs.activeRingId,
            "autoBackup" to prefs.autoBackup,
            "lastBackupLabel" to prefs.lastBackupLabel,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

    private fun taskCreateMap(userId: String, task: SmartTask): Map<String, Any> =
        mapOf(
            "userId" to userId,
            "title" to task.title.trim().take(200),
            "meta" to task.meta.trim().take(300),
            "imgUri" to task.imgUri.take(2000),
            "repeatMode" to task.repeatMode,
            "cycleSteps" to task.cycleSteps.take(20),
            "cycleIndex" to task.cycleIndex.coerceIn(0L, 1000L),
            "weekDays" to task.weekDays.take(7),
            "alertEnabled" to task.alertEnabled,
            "alertTime" to task.alertTime.take(10),
            "nextDueDate" to task.nextDueDate.take(20),
            "lastAlertedDate" to task.lastAlertedDate.take(20),
            "sortOrder" to task.sortOrder,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
}
