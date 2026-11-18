package com.example.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.LibraryPhoto
import com.example.model.TrimmedRingtone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream

private val Context.mediaDataStore by preferencesDataStore(name = "mnotes_local_media")

class LocalMediaRepository(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }
    private val keyPhotos = stringPreferencesKey("library_photos_json")
    private val keyRingtones = stringPreferencesKey("trimmed_ringtones_json")

    val photosFlow: Flow<List<LibraryPhoto>> = context.mediaDataStore.data.map { prefs ->
        val raw = prefs[keyPhotos] ?: "[]"
        runCatching { json.decodeFromString<List<LibraryPhoto>>(raw) }.getOrDefault(emptyList())
    }

    val ringtonesFlow: Flow<List<TrimmedRingtone>> = context.mediaDataStore.data.map { prefs ->
        val raw = prefs[keyRingtones] ?: "[]"
        runCatching { json.decodeFromString<List<TrimmedRingtone>>(raw) }.getOrDefault(emptyList())
    }

    suspend fun saveCroppedBitmap(
        sourceUri: Uri,
        offsetXNorm: Float,
        offsetYNorm: Float,
        scale: Float,
        rotationDeg: Int
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val input = context.contentResolver.openInputStream(sourceUri)
                ?: error("Cannot open image URI")
            val decoded = input.use { BitmapFactory.decodeStream(it) }
                ?: error("Failed to decode image")

            val outSize = 360
            val output = Bitmap.createBitmap(outSize, outSize, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(output)
            canvas.drawColor(android.graphics.Color.WHITE)

            val matrix = Matrix()
            val baseScale = maxOf(
                outSize.toFloat() / decoded.width.toFloat(),
                outSize.toFloat() / decoded.height.toFloat()
            )
            val totalScale = baseScale * scale
            matrix.postTranslate(-decoded.width / 2f, -decoded.height / 2f)
            matrix.postRotate(rotationDeg.toFloat())
            matrix.postScale(totalScale, totalScale)
            matrix.postTranslate(
                outSize / 2f + offsetXNorm * outSize,
                outSize / 2f + offsetYNorm * outSize
            )

            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            canvas.drawBitmap(decoded, matrix, paint)
            decoded.recycle()

            val dir = File(context.filesDir, "mnotes_photos").apply { mkdirs() }
            val outFile = File(dir, "crop_${System.currentTimeMillis()}.png")
            FileOutputStream(outFile).use { fos ->
                output.compress(Bitmap.CompressFormat.PNG, 92, fos)
            }
            output.recycle()
            outFile.absolutePath
        }
    }

    suspend fun addLibraryPhoto(name: String, filePath: String) {
        context.mediaDataStore.edit { prefs ->
            val current = runCatching {
                json.decodeFromString<List<LibraryPhoto>>(prefs[keyPhotos] ?: "[]")
            }.getOrDefault(emptyList())
            val updated = listOf(
                LibraryPhoto(
                    id = "g_${System.currentTimeMillis()}",
                    name = name.ifBlank { "Photo" },
                    filePath = filePath,
                    enabled = true
                )
            ) + current
            prefs[keyPhotos] = json.encodeToString(updated)
        }
    }

    suspend fun toggleLibraryPhoto(id: String, enabled: Boolean) {
        context.mediaDataStore.edit { prefs ->
            val current = runCatching {
                json.decodeFromString<List<LibraryPhoto>>(prefs[keyPhotos] ?: "[]")
            }.getOrDefault(emptyList())
            val updated = current.map { if (it.id == id) it.copy(enabled = enabled) else it }
            prefs[keyPhotos] = json.encodeToString(updated)
        }
    }

    suspend fun removeLibraryPhoto(id: String) {
        context.mediaDataStore.edit { prefs ->
            val current = runCatching {
                json.decodeFromString<List<LibraryPhoto>>(prefs[keyPhotos] ?: "[]")
            }.getOrDefault(emptyList())
            val target = current.firstOrNull { it.id == id }
            if (target != null) {
                runCatching { File(target.filePath).delete() }
            }
            val updated = current.filterNot { it.id == id }
            prefs[keyPhotos] = json.encodeToString(updated)
        }
    }

    suspend fun addTrimmedRingtone(ringtone: TrimmedRingtone) {
        context.mediaDataStore.edit { prefs ->
            val current = runCatching {
                json.decodeFromString<List<TrimmedRingtone>>(prefs[keyRingtones] ?: "[]")
            }.getOrDefault(emptyList())
            val updated = listOf(ringtone) + current
            prefs[keyRingtones] = json.encodeToString(updated)
        }
    }

    suspend fun removeTrimmedRingtone(id: String) {
        context.mediaDataStore.edit { prefs ->
            val current = runCatching {
                json.decodeFromString<List<TrimmedRingtone>>(prefs[keyRingtones] ?: "[]")
            }.getOrDefault(emptyList())
            val target = current.firstOrNull { it.id == id }
            if (target != null) {
                runCatching { File(target.filePath).delete() }
            }
            val updated = current.filterNot { it.id == id }
            prefs[keyRingtones] = json.encodeToString(updated)
        }
    }

    fun newRingtoneOutputFile(): File {
        val dir = File(context.filesDir, "mnotes_ringtones").apply { mkdirs() }
        return File(dir, "ring_${System.currentTimeMillis()}.wav")
    }
}
