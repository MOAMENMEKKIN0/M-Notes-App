package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.min
import kotlin.math.sin

class SoundEngine(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var ringtoneJob: Job? = null
    private var mediaPlayer: MediaPlayer? = null
    @Volatile
    private var activeAudioTrack: AudioTrack? = null

    private enum class Wave { SINE, TRIANGLE, SAW, SQUARE }
    private data class ToneEvent(
        val freq: Double,
        val startSec: Double,
        val durSec: Double,
        val wave: Wave = Wave.SINE,
        val vol: Double = 0.22,
        val slideTo: Double? = null
    )

    fun vibrate(hapticEnabled: Boolean, isDone: Boolean = false) {
        if (!hapticEnabled) return
        runCatching {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vm.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            if (!vibrator.hasVibrator()) return
            if (isDone) {
                vibrator.vibrate(
                    VibrationEffect.createWaveform(longArrayOf(0, 25, 30, 35), -1)
                )
            } else {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(14, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            }
        }
    }

    fun playUiSound(
        type: String,
        navEnabled: Boolean,
        enterEnabled: Boolean,
        exitEnabled: Boolean,
        doneEnabled: Boolean,
        hapticEnabled: Boolean
    ) {
        vibrate(hapticEnabled, isDone = (type == "done"))
        val events = when (type) {
            "nav" -> if (navEnabled) listOf(ToneEvent(620.0, 0.0, 0.06, Wave.SINE, 0.16, 320.0)) else emptyList()
            "enter" -> if (enterEnabled) listOf(
                ToneEvent(440.0, 0.0, 0.15, Wave.TRIANGLE, 0.14),
                ToneEvent(587.3, 0.055, 0.16, Wave.TRIANGLE, 0.14)
            ) else emptyList()
            "exit" -> if (exitEnabled) listOf(
                ToneEvent(523.2, 0.0, 0.12, Wave.SINE, 0.13),
                ToneEvent(349.2, 0.045, 0.13, Wave.SINE, 0.13)
            ) else emptyList()
            "done" -> if (doneEnabled) listOf(
                ToneEvent(523.2, 0.0, 0.25, Wave.SINE, 0.16),
                ToneEvent(659.2, 0.07, 0.25, Wave.SINE, 0.16),
                ToneEvent(784.0, 0.14, 0.28, Wave.SINE, 0.18)
            ) else emptyList()
            else -> emptyList()
        }
        if (events.isNotEmpty()) {
            scope.launch { playEvents(events) }
        }
    }

    fun playGymTrackOnce(trackId: String) {
        stopRingtone()
        ringtoneJob = scope.launch {
            playEvents(eventsForGymTrack(trackId))
        }
    }

    fun startAlertRingtoneLoop(activeRingId: String, customFilePath: String?) {
        stopRingtone()
        if (!customFilePath.isNullOrBlank() && File(customFilePath).exists()) {
            runCatching {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(customFilePath)
                    isLooping = true
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    prepare()
                    start()
                }
                return
            }.onFailure {
                Log.w("SoundEngine", "Failed to play custom ringtone, falling back to gym track", it)
            }
        }
        ringtoneJob = scope.launch {
            while (isActive) {
                playEvents(eventsForGymTrack(activeRingId))
                delay(350)
            }
        }
    }

    fun previewAudioFile(uri: Uri, startSec: Int, durationSec: Int) {
        stopRingtone()
        runCatching {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, uri)
                prepare()
                seekTo(startSec * 1000)
                start()
            }
            ringtoneJob = scope.launch {
                delay(durationSec * 1000L)
                withContext(Dispatchers.Main) {
                    stopRingtone()
                }
            }
        }.onFailure {
            Log.e("SoundEngine", "Failed to preview audio uri", it)
        }
    }

    fun playSavedFileOnce(filePath: String) {
        stopRingtone()
        if (!File(filePath).exists()) return
        runCatching {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                isLooping = false
                prepare()
                start()
                setOnCompletionListener { stopRingtone() }
            }
        }.onFailure {
            Log.e("SoundEngine", "Failed to play saved clip", it)
        }
    }

    fun stopRingtone() {
        ringtoneJob?.cancel()
        ringtoneJob = null
        runCatching {
            activeAudioTrack?.pause()
            activeAudioTrack?.flush()
            activeAudioTrack?.stop()
        }
        runCatching {
            activeAudioTrack?.release()
        }
        activeAudioTrack = null
        runCatching {
            mediaPlayer?.stop()
        }
        runCatching {
            mediaPlayer?.release()
        }
        mediaPlayer = null
    }

    fun getAudioDurationSeconds(uri: Uri): Int {
        return runCatching {
            val mp = MediaPlayer()
            mp.setDataSource(context, uri)
            mp.prepare()
            val sec = (mp.duration / 1000).coerceAtLeast(10)
            mp.release()
            sec
        }.getOrDefault(60)
    }

    suspend fun trimAudioToWav(
        uri: Uri,
        startSec: Int,
        durationSec: Int,
        outputFile: File
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val extractor = MediaExtractor()
            extractor.setDataSource(context, uri, null)
            var audioTrackIndex = -1
            var trackFormat: MediaFormat? = null
            for (i in 0 until extractor.trackCount) {
                val fmt = extractor.getTrackFormat(i)
                val mime = fmt.getString(MediaFormat.KEY_MIME) ?: ""
                if (mime.startsWith("audio/")) {
                    audioTrackIndex = i
                    trackFormat = fmt
                    break
                }
            }
            require(audioTrackIndex >= 0 && trackFormat != null) { "No audio track found" }

            extractor.selectTrack(audioTrackIndex)
            val mime = trackFormat.getString(MediaFormat.KEY_MIME)!!
            val startUs = startSec * 1_000_000L
            val endUs = (startSec + durationSec) * 1_000_000L
            extractor.seekTo(startUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC)

            val codec = MediaCodec.createDecoderByType(mime)
            codec.configure(trackFormat, null, null, 0)
            codec.start()

            val pcmOut = ByteArrayOutputStream()
            val bufferInfo = MediaCodec.BufferInfo()
            var sampleRate = if (trackFormat.containsKey(MediaFormat.KEY_SAMPLE_RATE)) {
                trackFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
            } else 44100
            var channelCount = if (trackFormat.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) {
                trackFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
            } else 2

            var sawInputEOS = false
            var sawOutputEOS = false
            while (!sawOutputEOS) {
                if (!sawInputEOS) {
                    val inIdx = codec.dequeueInputBuffer(10_000)
                    if (inIdx >= 0) {
                        val inBuf = codec.getInputBuffer(inIdx)!!
                        val sampleSize = extractor.readSampleData(inBuf, 0)
                        val sampleTime = extractor.sampleTime
                        if (sampleSize < 0 || sampleTime > endUs) {
                            codec.queueInputBuffer(inIdx, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            sawInputEOS = true
                        } else {
                            codec.queueInputBuffer(inIdx, 0, sampleSize, sampleTime, 0)
                            extractor.advance()
                        }
                    }
                }

                val outIdx = codec.dequeueOutputBuffer(bufferInfo, 10_000)
                if (outIdx >= 0) {
                    val outBuf = codec.getOutputBuffer(outIdx)!!
                    if (bufferInfo.size > 0 && bufferInfo.presentationTimeUs >= startUs) {
                        val chunk = ByteArray(bufferInfo.size)
                        outBuf.position(bufferInfo.offset)
                        outBuf.get(chunk)
                        pcmOut.write(chunk)
                    }
                    codec.releaseOutputBuffer(outIdx, false)
                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        sawOutputEOS = true
                    }
                } else if (outIdx == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    val newFmt = codec.outputFormat
                    if (newFmt.containsKey(MediaFormat.KEY_SAMPLE_RATE)) {
                        sampleRate = newFmt.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                    }
                    if (newFmt.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) {
                        channelCount = newFmt.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                    }
                }
            }

            codec.stop()
            codec.release()
            extractor.release()

            val pcmBytes = pcmOut.toByteArray()
            require(pcmBytes.isNotEmpty()) { "Trimmed audio is empty" }
            outputFile.parentFile?.mkdirs()
            writeWavFile(outputFile, pcmBytes, sampleRate, channelCount.coerceIn(1, 2))
            outputFile
        }
    }

    private fun writeWavFile(file: File, pcmData: ByteArray, sampleRate: Int, channels: Int) {
        val byteRate = sampleRate * channels * 2
        val totalAudioLen = pcmData.size
        val totalDataLen = totalAudioLen + 36
        val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN).apply {
            put("RIFF".toByteArray())
            putInt(totalDataLen)
            put("WAVE".toByteArray())
            put("fmt ".toByteArray())
            putInt(16)
            putShort(1.toShort())
            putShort(channels.toShort())
            putInt(sampleRate)
            putInt(byteRate)
            putShort((channels * 2).toShort())
            putShort(16.toShort())
            put("data".toByteArray())
            putInt(totalAudioLen)
        }
        FileOutputStream(file).use { out ->
            out.write(header.array())
            out.write(pcmData)
        }
    }

    private fun buildTenSecondPattern(
        bpm: Double,
        bassNotes: DoubleArray,
        leadNotes: DoubleArray,
        leadWave: Wave,
        kickFreq: Double = 135.0,
        leadVol: Double = 0.16
    ): List<ToneEvent> {
        val beatSec = 60.0 / bpm
        val eighth = beatSec / 2.0
        val events = ArrayList<ToneEvent>()
        var t = 0.0
        var step = 0
        while (t < 9.85) {
            if (step % 2 == 0) {
                events.add(ToneEvent(kickFreq, t, 0.16, Wave.SINE, 0.34, 42.0))
            } else {
                events.add(ToneEvent(bassNotes[(step / 2) % bassNotes.size], t, eighth * 0.85, Wave.TRIANGLE, 0.20))
            }
            if (step % 4 == 2) {
                events.add(ToneEvent(1150.0, t, 0.07, Wave.SQUARE, 0.06, 420.0))
            }
            val leadFreq = leadNotes[step % leadNotes.size]
            if (leadFreq > 0.0) {
                events.add(ToneEvent(leadFreq, t, eighth * 0.92, leadWave, leadVol))
            }
            t += eighth
            step++
        }
        return events
    }

    private fun eventsForGymTrack(id: String): List<ToneEvent> = when (id) {
        "G1" -> buildTenSecondPattern(
            bpm = 130.0,
            bassNotes = doubleArrayOf(98.0, 98.0, 110.0, 130.8),
            leadNotes = doubleArrayOf(392.0, 0.0, 440.0, 493.9, 523.3, 0.0, 493.9, 440.0),
            leadWave = Wave.SAW,
            kickFreq = 140.0
        )
        "G2" -> buildTenSecondPattern(
            bpm = 126.0,
            bassNotes = doubleArrayOf(130.8, 146.8, 164.8, 196.0),
            leadNotes = doubleArrayOf(523.3, 659.3, 784.0, 659.3, 880.0, 784.0, 659.3, 587.3),
            leadWave = Wave.TRIANGLE,
            kickFreq = 128.0,
            leadVol = 0.20
        )
        "G3" -> buildTenSecondPattern(
            bpm = 136.0,
            bassNotes = doubleArrayOf(110.0, 110.0, 146.8, 130.8),
            leadNotes = doubleArrayOf(980.0, 0.0, 659.3, 0.0, 980.0, 784.0, 659.3, 523.3),
            leadWave = Wave.SINE,
            kickFreq = 150.0,
            leadVol = 0.22
        )
        "G4" -> buildTenSecondPattern(
            bpm = 124.0,
            bassNotes = doubleArrayOf(82.4, 98.0, 110.0, 98.0),
            leadNotes = doubleArrayOf(329.6, 329.6, 440.0, 392.0, 329.6, 493.9, 440.0, 392.0),
            leadWave = Wave.SAW,
            kickFreq = 132.0,
            leadVol = 0.15
        )
        "G5" -> buildTenSecondPattern(
            bpm = 132.0,
            bassNotes = doubleArrayOf(110.0, 123.5, 130.8, 146.8),
            leadNotes = doubleArrayOf(440.0, 0.0, 440.0, 0.0, 587.3, 659.3, 880.0, 784.0),
            leadWave = Wave.TRIANGLE,
            kickFreq = 145.0,
            leadVol = 0.21
        )
        "G6" -> buildTenSecondPattern(
            bpm = 128.0,
            bassNotes = doubleArrayOf(130.8, 164.8, 196.0, 164.8),
            leadNotes = doubleArrayOf(523.3, 659.3, 784.0, 1046.5, 784.0, 659.3, 587.3, 523.3),
            leadWave = Wave.TRIANGLE,
            kickFreq = 130.0,
            leadVol = 0.21
        )
        "G7" -> buildTenSecondPattern(
            bpm = 140.0,
            bassNotes = doubleArrayOf(116.5, 130.8, 146.8, 130.8),
            leadNotes = doubleArrayOf(587.3, 440.0, 587.3, 698.5, 587.3, 523.3, 440.0, 523.3),
            leadWave = Wave.SQUARE,
            kickFreq = 148.0,
            leadVol = 0.12
        )
        "G8" -> buildTenSecondPattern(
            bpm = 122.0,
            bassNotes = doubleArrayOf(146.8, 164.8, 196.0, 220.0),
            leadNotes = doubleArrayOf(587.3, 740.0, 880.0, 740.0, 659.3, 587.3, 659.3, 880.0),
            leadWave = Wave.SINE,
            kickFreq = 125.0,
            leadVol = 0.22
        )
        "G9" -> buildTenSecondPattern(
            bpm = 134.0,
            bassNotes = doubleArrayOf(73.4, 73.4, 87.3, 98.0),
            leadNotes = doubleArrayOf(293.7, 349.2, 440.0, 392.0, 293.7, 440.0, 523.3, 440.0),
            leadWave = Wave.SAW,
            kickFreq = 155.0,
            leadVol = 0.15
        )
        else -> buildTenSecondPattern(
            bpm = 138.0,
            bassNotes = doubleArrayOf(130.8, 146.8, 164.8, 196.0),
            leadNotes = doubleArrayOf(659.3, 784.0, 880.0, 1046.5, 880.0, 784.0, 659.3, 784.0),
            leadWave = Wave.TRIANGLE,
            kickFreq = 142.0,
            leadVol = 0.20
        )
    }

    private fun playEvents(events: List<ToneEvent>) {
        if (events.isEmpty()) return
        runCatching {
            val sampleRate = 22050
            val maxSec = events.maxOf { it.startSec + it.durSec } + 0.05
            val totalSamples = (maxSec * sampleRate).toInt().coerceAtLeast(1)
            val mix = DoubleArray(totalSamples)

            for (ev in events) {
                val s0 = (ev.startSec * sampleRate).toInt().coerceAtLeast(0)
                val len = (ev.durSec * sampleRate).toInt().coerceAtLeast(1)
                val s1 = min(totalSamples, s0 + len)
                var phase = 0.0
                for (i in s0 until s1) {
                    val progress = (i - s0).toDouble() / len
                    val freq = if (ev.slideTo != null) {
                        ev.freq * Math.pow(ev.slideTo / ev.freq, progress)
                    } else ev.freq
                    phase += 2.0 * PI * freq / sampleRate
                    val sampleVal = when (ev.wave) {
                        Wave.SINE -> sin(phase)
                        Wave.TRIANGLE -> 2.0 * kotlin.math.abs(2.0 * ((phase / (2.0 * PI)) % 1.0) - 1.0) - 1.0
                        Wave.SAW -> 2.0 * ((phase / (2.0 * PI)) % 1.0) - 1.0
                        Wave.SQUARE -> if (sin(phase) >= 0) 1.0 else -1.0
                    }
                    val env = exp(-3.5 * progress)
                    mix[i] += sampleVal * ev.vol * env
                }
            }

            val pcm = ShortArray(totalSamples) { i ->
                (mix[i].coerceIn(-0.98, 0.98) * Short.MAX_VALUE).toInt().toShort()
            }

            val minBuf = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(maxOf(minBuf, pcm.size * 2))
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            activeAudioTrack = track
            track.write(pcm, 0, pcm.size)
            track.play()
            var elapsed = 0L
            val targetMs = (maxSec * 1000).toLong() + 40L
            while (elapsed < targetMs && activeAudioTrack === track) {
                Thread.sleep(60L)
                elapsed += 60L
            }
            if (activeAudioTrack === track) {
                activeAudioTrack = null
            }
            runCatching { track.stop() }
            runCatching { track.release() }
        }
    }
}
