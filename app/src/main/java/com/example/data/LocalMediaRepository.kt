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

    /**
     * Copies a user-picked wallpaper image into private app storage (downscaled cleanly to <= 1920px)
     * so it persists permanently across app restarts without relying on temporary content URI grants.
     *
     * Reads the content URI ONCE into a temporary cache file (with ContentResolver stream,
     * AssetFileDescriptor, ParcelFileDescriptor, and file:// fallbacks) so PickVisualMedia,
     * SAF documents, and cloud gallery providers never fail on double-open or inJustDecodeBounds.
     */
    suspend fun saveWallpaperImage(sourceUri: Uri): Result<String> = withContext(Dispatchers.IO) {
        var tempFile: File? = null
        runCatching {
            val cacheCopy = File(context.cacheDir, "wp_import_${System.currentTimeMillis()}.tmp")
            tempFile = cacheCopy

            val copiedBytes = copyUriToTempFile(sourceUri, cacheCopy)
            if (copiedBytes <= 0L || !cacheCopy.exists() || cacheCopy.length() <= 0L) {
                error("Cannot read selected wallpaper image")
            }

            val decoded = decodeDownscaledBitmap(cacheCopy, maxDimension = 1920)
                ?: error("Unsupported or corrupted wallpaper image")

            val dir = File(context.filesDir, "mnotes_wallpapers").apply { mkdirs() }
            val outFile = File(dir, "wallpaper_${System.currentTimeMillis()}.jpg")
            FileOutputStream(outFile).use { fos ->
                decoded.compress(Bitmap.CompressFormat.JPEG, 92, fos)
            }
            decoded.recycle()
            outFile.absolutePath
        }.also {
            runCatching { tempFile?.delete() }
        }
    }

    private fun copyUriToTempFile(sourceUri: Uri, destFile: File): Long {
        val resolver = context.contentResolver
        // 1. Primary: ContentResolver.openInputStream
        val fromStream = runCatching {
            resolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output -> input.copyTo(output) }
            }
        }.getOrNull()
        if (fromStream != null && fromStream > 0L) return fromStream

        // 2. Fallback: openAssetFileDescriptor (works with certain Photo Picker / MediaStore URIs)
        val fromAssetFd = runCatching {
            resolver.openAssetFileDescriptor(sourceUri, "r")?.use { afd ->
                afd.createInputStream().use { input ->
                    FileOutputStream(destFile).use { output -> input.copyTo(output) }
                }
            }
        }.getOrNull()
        if (fromAssetFd != null && fromAssetFd > 0L) return fromAssetFd

        // 3. Fallback: openFileDescriptor
        val fromPfd = runCatching {
            resolver.openFileDescriptor(sourceUri, "r")?.use { pfd ->
                java.io.FileInputStream(pfd.fileDescriptor).use { input ->
                    FileOutputStream(destFile).use { output -> input.copyTo(output) }
                }
            }
        }.getOrNull()
        if (fromPfd != null && fromPfd > 0L) return fromPfd

        // 4. Fallback: direct file:// URI
        if (sourceUri.scheme.equals("file", ignoreCase = true) && !sourceUri.path.isNullOrBlank()) {
            val src = File(sourceUri.path!!)
            if (src.exists() && src.canRead()) {
                return src.inputStream().use { input ->
                    FileOutputStream(destFile).use { output -> input.copyTo(output) }
                }
            }
        }
        return 0L
    }

    private fun decodeDownscaledBitmap(file: File, maxDimension: Int): Bitmap? {
        // On Android 9+, ImageDecoder handles EXIF orientation, HEIF/HEIC, WebP, PNG, JPEG cleanly.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            val fromDecoder = runCatching {
                val source = android.graphics.ImageDecoder.createSource(file)
                android.graphics.ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                    decoder.allocator = android.graphics.ImageDecoder.ALLOCATOR_SOFTWARE
                    val w = info.size.width.coerceAtLeast(1)
                    val h = info.size.height.coerceAtLeast(1)
                    val maxSide = maxOf(w, h)
                    if (maxSide > maxDimension) {
                        val scale = maxDimension.toFloat() / maxSide.toFloat()
                        decoder.setTargetSize(
                            (w * scale).toInt().coerceAtLeast(1),
                            (h * scale).toInt().coerceAtLeast(1)
                        )
                    }
                }
            }.getOrNull()
            if (fromDecoder != null) return fromDecoder
        }

        // Reliable universal fallback (also used by Robolectric JVM tests)
        val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, boundsOpts)
        val rawW = boundsOpts.outWidth
        val rawH = boundsOpts.outHeight
        var sampleSize = 1
        if (rawW > 0 && rawH > 0) {
            val maxDim = maxOf(rawW, rawH)
            while (maxDim / sampleSize > maxDimension) {
                sampleSize *= 2
            }
        }
        val decodeOpts = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        return BitmapFactory.decodeFile(file.absolutePath, decodeOpts)
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
