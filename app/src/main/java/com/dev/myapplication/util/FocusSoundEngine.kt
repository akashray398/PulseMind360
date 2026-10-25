package com.dev.myapplication.util

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.concurrent.thread
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

object FocusSoundEngine {

    private var audioTrack: AudioTrack? = null
    @Volatile
    private var isPlaying = false

    fun startAmbientSound(soundType: String) {
        stopAmbientSound()

        if (soundType == "OFF") return

        isPlaying = true
        val sampleRate = 22050
        val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(minBufferSize)
            .build()

        audioTrack?.play()

        thread(start = true, isDaemon = true) {
            val buffer = ShortArray(minBufferSize)
            var phase = 0.0

            while (isPlaying) {
                for (i in buffer.indices) {
                    val sample = when (soundType) {
                        "RAIN" -> {
                            val noise = (Random.nextFloat() * 2f - 1f) * 0.15f
                            (noise * Short.MAX_VALUE).toInt().toShort()
                        }
                        "CYBER" -> {
                            phase += 2.0 * PI * 110.0 / sampleRate
                            val tone = sin(phase) * 0.10
                            (tone * Short.MAX_VALUE).toInt().toShort()
                        }
                        else -> 0
                    }
                    buffer[i] = sample
                }
                audioTrack?.write(buffer, 0, buffer.size)
            }
        }
    }

    fun stopAmbientSound() {
        isPlaying = false
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
    }
}
