package com.example.engine

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.*
import kotlin.math.log10
import kotlin.math.sqrt

class AudioRecorderPipeline {

    private var audioRecord: AudioRecord? = null
    private var isMetering = false
    private var meteringJob: Job? = null

    private val sampleRate = 44100
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    @SuppressLint("MissingPermission")
    fun startMetering(onDecibelUpdate: (Float) -> Unit, scope: CoroutineScope) {
        if (isMetering) return

        try {
            val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
            if (minBufferSize <= 0) return

            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                minBufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                audioRecord?.release()
                audioRecord = null
                return
            }

            audioRecord?.startRecording()
            isMetering = true

            meteringJob = scope.launch(Dispatchers.IO) {
                val buffer = ShortArray(minBufferSize)
                while (isActive && isMetering) {
                    val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (read > 0) {
                        var sum = 0.0
                        for (i in 0 until read) {
                            sum += buffer[i] * buffer[i]
                        }
                        val rms = sqrt(sum / read)
                        val db = if (rms > 0) 20 * log10(rms / 32767.0) else -80.0
                        withContext(Dispatchers.Main) {
                            onDecibelUpdate(db.toFloat().coerceIn(-80f, 0f))
                        }
                    }
                    delay(80)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            stopMetering()
        }
    }

    fun stopMetering() {
        isMetering = false
        meteringJob?.cancel()
        meteringJob = null
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        audioRecord = null
    }
}
