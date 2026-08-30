package com.example.data.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.sin

object ToneAudioEngine {
    private const val SAMPLE_RATE = 44100
    private var currentTrack: AudioTrack? = null

    suspend fun playTone(toneType: String, onFinished: (() -> Unit)? = null) = withContext(Dispatchers.Default) {
        stop()
        val samples = when (toneType) {
            "WESTMINSTER_CHIME" -> generateWestminsterChimes()
            "THREE_TONE_MELODY" -> generateThreeToneMelody()
            "NATIONAL_ANTHEM" -> generateAnthemFanfare()
            "MARS_SEKOLAH" -> generateSchoolMarchTone()
            "ANNOUNCEMENT" -> generateAnnouncementChime()
            "EMERGENCY_SIREN" -> generateEmergencySiren()
            "PRAYER_CALL" -> generateSoftChimeTone()
            else -> generateThreeToneMelody()
        }

        try {
            val bufferSize = samples.size * 2
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            currentTrack = track
            track.write(samples, 0, samples.size)
            track.play()

            // Calculate duration in ms
            val durationMs = (samples.size * 1000L) / SAMPLE_RATE
            kotlinx.coroutines.delay(durationMs + 200)
            onFinished?.invoke()
        } catch (e: Exception) {
            e.printStackTrace()
            onFinished?.invoke()
        }
    }

    fun stop() {
        try {
            currentTrack?.stop()
            currentTrack?.release()
        } catch (e: Exception) {
            // ignore
        }
        currentTrack = null
    }

    // 4-chime Westminster Bell
    private fun generateWestminsterChimes(): ShortArray {
        // Notes: G#4 (415 Hz), F#4 (370 Hz), E4 (329.6 Hz), B3 (246.9 Hz)
        val freqs = doubleArrayOf(415.3, 369.99, 329.63, 246.94)
        val noteDuration = 0.55 // seconds
        val totalSamples = (SAMPLE_RATE * noteDuration * freqs.size).toInt()
        val buffer = ShortArray(totalSamples)
        var offset = 0

        for (freq in freqs) {
            val count = (SAMPLE_RATE * noteDuration).toInt()
            for (i in 0 until count) {
                val t = i.toDouble() / SAMPLE_RATE
                val envelope = kotlin.math.exp(-3.5 * (t / noteDuration)) // bell decay
                val harmonic = sin(2.0 * Math.PI * freq * t) +
                        0.4 * sin(2.0 * Math.PI * (freq * 2.0) * t) +
                        0.2 * sin(2.0 * Math.PI * (freq * 3.0) * t)
                val sample = (harmonic * envelope * 0.7 * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                buffer[offset++] = sample.toShort()
            }
        }
        return buffer
    }

    // Classic 3-Tone School Bell (Do - Mi - Sol)
    private fun generateThreeToneMelody(): ShortArray {
        val freqs = doubleArrayOf(523.25, 659.25, 783.99) // C5, E5, G5
        val noteDuration = 0.45
        val totalSamples = (SAMPLE_RATE * noteDuration * freqs.size).toInt()
        val buffer = ShortArray(totalSamples)
        var offset = 0

        for (freq in freqs) {
            val count = (SAMPLE_RATE * noteDuration).toInt()
            for (i in 0 until count) {
                val t = i.toDouble() / noteDuration
                val env = 1.0 - t // linear fade
                val raw = sin(2.0 * Math.PI * freq * (i.toDouble() / SAMPLE_RATE))
                buffer[offset++] = (raw * env * 0.8 * Short.MAX_VALUE).toInt().toShort()
            }
        }
        return buffer
    }

    // Two-tone Announcement ding-dong
    private fun generateAnnouncementChime(): ShortArray {
        val freqs = doubleArrayOf(880.0, 587.33) // A5 -> D5
        val noteDuration = 0.65
        val totalSamples = (SAMPLE_RATE * noteDuration * freqs.size).toInt()
        val buffer = ShortArray(totalSamples)
        var offset = 0

        for (freq in freqs) {
            val count = (SAMPLE_RATE * noteDuration).toInt()
            for (i in 0 until count) {
                val t = i.toDouble() / noteDuration
                val env = kotlin.math.exp(-2.8 * t)
                val raw = sin(2.0 * Math.PI * freq * (i.toDouble() / SAMPLE_RATE)) +
                        0.3 * sin(2.0 * Math.PI * freq * 2.0 * (i.toDouble() / SAMPLE_RATE))
                buffer[offset++] = (raw * env * 0.75 * Short.MAX_VALUE).toInt().toShort()
            }
        }
        return buffer
    }

    // Anthem Fanfare Tone
    private fun generateAnthemFanfare(): ShortArray {
        val freqs = doubleArrayOf(392.0, 523.25, 659.25, 783.99, 1046.5) // G4, C5, E5, G5, C6
        val durations = doubleArrayOf(0.3, 0.3, 0.3, 0.4, 0.7)
        val totalSamples = (durations.sum() * SAMPLE_RATE).toInt()
        val buffer = ShortArray(totalSamples)
        var offset = 0

        for (idx in freqs.indices) {
            val count = (durations[idx] * SAMPLE_RATE).toInt()
            val freq = freqs[idx]
            for (i in 0 until count) {
                val t = i.toDouble() / count
                val env = if (t < 0.1) t / 0.1 else (1.0 - t * 0.5)
                val raw = sin(2.0 * Math.PI * freq * (i.toDouble() / SAMPLE_RATE))
                buffer[offset++] = (raw * env * 0.75 * Short.MAX_VALUE).toInt().toShort()
            }
        }
        return buffer
    }

    // Mars Sekolah Marching tempo
    private fun generateSchoolMarchTone(): ShortArray {
        val freqs = doubleArrayOf(440.0, 440.0, 554.37, 659.25, 880.0)
        val durations = doubleArrayOf(0.2, 0.2, 0.25, 0.25, 0.6)
        val totalSamples = (durations.sum() * SAMPLE_RATE).toInt()
        val buffer = ShortArray(totalSamples)
        var offset = 0

        for (idx in freqs.indices) {
            val count = (durations[idx] * SAMPLE_RATE).toInt()
            val freq = freqs[idx]
            for (i in 0 until count) {
                val t = i.toDouble() / count
                val env = 1.0 - t * 0.4
                val raw = sin(2.0 * Math.PI * freq * (i.toDouble() / SAMPLE_RATE))
                buffer[offset++] = (raw * env * 0.7 * Short.MAX_VALUE).toInt().toShort()
            }
        }
        return buffer
    }

    // Soft chime for prayer/quiet call
    private fun generateSoftChimeTone(): ShortArray {
        val freqs = doubleArrayOf(349.23, 440.0, 523.25) // F4, A4, C5
        val noteDuration = 0.7
        val totalSamples = (SAMPLE_RATE * noteDuration * freqs.size).toInt()
        val buffer = ShortArray(totalSamples)
        var offset = 0

        for (freq in freqs) {
            val count = (SAMPLE_RATE * noteDuration).toInt()
            for (i in 0 until count) {
                val t = i.toDouble() / noteDuration
                val env = kotlin.math.exp(-2.2 * t)
                val raw = sin(2.0 * Math.PI * freq * (i.toDouble() / SAMPLE_RATE))
                buffer[offset++] = (raw * env * 0.65 * Short.MAX_VALUE).toInt().toShort()
            }
        }
        return buffer
    }

    // Emergency Evacuation / Siren Tone
    private fun generateEmergencySiren(): ShortArray {
        val duration = 2.0 // 2 seconds sweep
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val buffer = ShortArray(totalSamples)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            // 440 Hz to 900 Hz oscillating sweep
            val sweepFreq = 440.0 + 440.0 * (0.5 * (1.0 + sin(2.0 * Math.PI * 2.0 * t)))
            val phase = 2.0 * Math.PI * sweepFreq * t
            val raw = sin(phase)
            buffer[i] = (raw * 0.85 * Short.MAX_VALUE).toInt().toShort()
        }
        return buffer
    }
}
