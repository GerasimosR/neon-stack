package com.neonstack.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

enum class SoundEffect {
    MOVE,
    ROTATE,
    SOFT_DROP,
    HARD_DROP,
    HOLD,
    LINE_CLEAR,
    LINE_CLEAR_1,
    LINE_CLEAR_2,
    LINE_CLEAR_3,
    QUAD_CLEAR,
    SPIN_BONUS,
    LEVEL_UP,
    GAME_OVER
}

class SoundSynthesizer {
    companion object {
        private const val SAMPLE_RATE = 44100
    }

    var isEnabled: Boolean = true

    private val soundBuffers = mutableMapOf<SoundEffect, ShortArray>()
    private val preloadedTracks = mutableMapOf<SoundEffect, AudioTrack>()

    init {
        generateAllSounds()
        initAudioTracks()
    }

    private fun generateAllSounds() {
        soundBuffers[SoundEffect.MOVE] = generateMoveSound()
        soundBuffers[SoundEffect.ROTATE] = generateRotateSound()
        soundBuffers[SoundEffect.SOFT_DROP] = generateSoftDropSound()
        soundBuffers[SoundEffect.HARD_DROP] = generateHardDropSound()
        soundBuffers[SoundEffect.HOLD] = generateHoldSound()
        soundBuffers[SoundEffect.LINE_CLEAR] = generateLineClear1Sound()
        soundBuffers[SoundEffect.LINE_CLEAR_1] = generateLineClear1Sound()
        soundBuffers[SoundEffect.LINE_CLEAR_2] = generateLineClear2Sound()
        soundBuffers[SoundEffect.LINE_CLEAR_3] = generateLineClear3Sound()
        soundBuffers[SoundEffect.QUAD_CLEAR] = generateQuadClearSound()
        soundBuffers[SoundEffect.SPIN_BONUS] = generateSpinBonusSound()
        soundBuffers[SoundEffect.LEVEL_UP] = generateLevelUpSound()
        soundBuffers[SoundEffect.GAME_OVER] = generateGameOverSound()
    }

    private fun initAudioTracks() {
        for ((effect, buffer) in soundBuffers) {
            try {
                val minBufferSize = AudioTrack.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                val bufferSize = maxOf(minBufferSize, buffer.size * 2)

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
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

                audioTrack.write(buffer, 0, buffer.size)
                preloadedTracks[effect] = audioTrack
            } catch (_: Exception) {
                // Silently skip if device audio resources are limited
            }
        }
    }

    /**
     * Replays preloaded static AudioTrack with sub-millisecond hardware latency
     * and zero allocation overhead.
     */
    fun play(effect: SoundEffect) {
        if (!isEnabled) return
        val track = preloadedTracks[effect] ?: return

        try {
            track.pause()
            track.playbackHeadPosition = 0
            track.play()
        } catch (_: Exception) {
            // Silently ignore if audio subsystem is interrupted
        }
    }

    fun release() {
        for (track in preloadedTracks.values) {
            try {
                track.stop()
                track.release()
            } catch (_: Exception) {}
        }
        preloadedTracks.clear()
    }

    // --- Procedural Waveform Generators ---

    private fun generateMoveSound(): ShortArray {
        val durationMs = 20
        val numSamples = (SAMPLE_RATE * durationMs / 1000)
        val samples = ShortArray(numSamples)
        val freq = 600.0

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val envelope = exp(-t * 120.0)
            val wave = sin(2.0 * PI * freq * t)
            samples[i] = (wave * envelope * Short.MAX_VALUE * 0.35).toInt().coerceIn(-32768, 32767).toShort()
        }
        return samples
    }

    private fun generateRotateSound(): ShortArray {
        val durationMs = 45
        val numSamples = (SAMPLE_RATE * durationMs / 1000)
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val t = i.toDouble() / SAMPLE_RATE
            val freq = 450.0 + progress * 400.0 // Pitch sweep up 450 -> 850 Hz
            val envelope = 1.0 - progress
            val wave = sin(2.0 * PI * freq * t)
            samples[i] = (wave * envelope * Short.MAX_VALUE * 0.4).toInt().coerceIn(-32768, 32767).toShort()
        }
        return samples
    }

    private fun generateSoftDropSound(): ShortArray {
        // Fast, non-fatiguing 20ms sine pulse with 2ms attack to avoid clicking on rapid repeat
        val durationMs = 20
        val numSamples = (SAMPLE_RATE * durationMs / 1000)
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val t = i.toDouble() / SAMPLE_RATE
            val attack = (progress / 0.10).coerceIn(0.0, 1.0)
            val envelope = attack * exp(-progress * 5.0)
            val wave = sin(2.0 * PI * 240.0 * t)
            samples[i] = (wave * envelope * Short.MAX_VALUE * 0.28).toInt().coerceIn(-32768, 32767).toShort()
        }
        return samples
    }

    private fun generateHardDropSound(): ShortArray {
        val durationMs = 120
        val numSamples = (SAMPLE_RATE * durationMs / 1000)
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val t = i.toDouble() / SAMPLE_RATE
            val freq = 180.0 * (1.0 - progress * 0.7) // Pitch drops 180 -> 54 Hz
            val envelope = exp(-progress * 6.0)
            val wave = sin(2.0 * PI * freq * t) + 0.3 * (Math.random() * 2.0 - 1.0)
            samples[i] = (wave * envelope * Short.MAX_VALUE * 0.6).toInt().coerceIn(-32768, 32767).toShort()
        }
        return samples
    }

    private fun generateHoldSound(): ShortArray {
        val durationMs = 80
        val numSamples = (SAMPLE_RATE * durationMs / 1000)
        val samples = ShortArray(numSamples)

        val half = numSamples / 2
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val freq = if (i < half) 523.25 else 783.99 // C5 then G5
            val localProgress = if (i < half) i.toDouble() / half else (i - half).toDouble() / half
            val envelope = 1.0 - localProgress
            val wave = sin(2.0 * PI * freq * t)
            samples[i] = (wave * envelope * Short.MAX_VALUE * 0.45).toInt().coerceIn(-32768, 32767).toShort()
        }
        return samples
    }

    private fun generateLineClear1Sound(): ShortArray {
        // Tier 1: Crisp single clear chime (C5 with subtle C6 shimmer)
        val durationMs = 130
        val numSamples = (SAMPLE_RATE * durationMs / 1000)
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val t = i.toDouble() / SAMPLE_RATE
            val attack = (progress / 0.05).coerceIn(0.0, 1.0)
            val envelope = attack * exp(-progress * 4.5)
            val wave = 0.8 * sin(2.0 * PI * 523.25 * t) + 0.2 * sin(2.0 * PI * 1046.50 * t)
            samples[i] = (wave * envelope * Short.MAX_VALUE * 0.48).toInt().coerceIn(-32768, 32767).toShort()
        }
        return samples
    }

    private fun generateLineClear2Sound(): ShortArray {
        // Tier 2: Dual rising chime chord (C5 -> G5)
        val durationMs = 210
        val numSamples = (SAMPLE_RATE * durationMs / 1000)
        val samples = ShortArray(numSamples)

        val notes = doubleArrayOf(523.25, 783.99)
        val seg = numSamples / notes.size

        for (i in 0 until numSamples) {
            val noteIdx = (i / seg).coerceIn(0, notes.size - 1)
            val freq = notes[noteIdx]
            val t = i.toDouble() / SAMPLE_RATE
            val segProgress = (i % seg).toDouble() / seg
            val attack = (segProgress / 0.06).coerceIn(0.0, 1.0)
            val envelope = attack * exp(-segProgress * 3.5)
            val wave = sin(2.0 * PI * freq * t)
            samples[i] = (wave * envelope * Short.MAX_VALUE * 0.50).toInt().coerceIn(-32768, 32767).toShort()
        }
        return samples
    }

    private fun generateLineClear3Sound(): ShortArray {
        // Tier 3: Triple harmonic ascending arpeggio (C5 -> E5 -> G5 -> Bb5 dominant 7th lift)
        val durationMs = 300
        val numSamples = (SAMPLE_RATE * durationMs / 1000)
        val samples = ShortArray(numSamples)

        val notes = doubleArrayOf(523.25, 659.25, 783.99, 932.33)
        val seg = numSamples / notes.size

        for (i in 0 until numSamples) {
            val noteIdx = (i / seg).coerceIn(0, notes.size - 1)
            val freq = notes[noteIdx]
            val t = i.toDouble() / SAMPLE_RATE
            val segProgress = (i % seg).toDouble() / seg
            val attack = (segProgress / 0.06).coerceIn(0.0, 1.0)
            val envelope = attack * exp(-segProgress * 2.8)
            val wave = 0.85 * sin(2.0 * PI * freq * t) + 0.15 * (if (sin(2.0 * PI * freq * t) >= 0) 1.0 else -1.0)
            samples[i] = (wave * envelope * Short.MAX_VALUE * 0.52).toInt().coerceIn(-32768, 32767).toShort()
        }
        return samples
    }

    private fun generateSpinBonusSound(): ShortArray {
        // Signature Sci-Fi Resonant Warp (Overclock Spin):
        // Dual-sine frequency modulation sweeping from 320Hz to 780Hz with sub-bass punch
        val durationMs = 230
        val numSamples = (SAMPLE_RATE * durationMs / 1000)
        val samples = ShortArray(numSamples)

        var phase = 0.0
        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val t = i.toDouble() / SAMPLE_RATE
            val carrierFreq = 320.0 + 460.0 * sin(PI * progress) // Up and down frequency arc
            val mod = 40.0 * sin(2.0 * PI * 65.0 * t) // 65Hz vibrato modulation
            phase += (carrierFreq + mod) / SAMPLE_RATE
            if (phase >= 1.0) phase -= 1.0

            val attack = (progress / 0.08).coerceIn(0.0, 1.0)
            val envelope = attack * (1.0 - progress * 0.85)
            val wave = sin(2.0 * PI * phase)
            samples[i] = (wave * envelope * Short.MAX_VALUE * 0.55).toInt().coerceIn(-32768, 32767).toShort()
        }
        return samples
    }

    private fun generateQuadClearSound(): ShortArray {
        val durationMs = 420
        val numSamples = (SAMPLE_RATE * durationMs / 1000)
        val samples = ShortArray(numSamples)

        val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.50) // C5, E5, G5, C6 fanfare
        val seg = numSamples / notes.size

        for (i in 0 until numSamples) {
            val noteIdx = (i / seg).coerceIn(0, notes.size - 1)
            val freq = notes[noteIdx]
            val t = i.toDouble() / SAMPLE_RATE
            val segProgress = (i % seg).toDouble() / seg
            val envelope = exp(-segProgress * 2.0)
            // Square wave blended with sine for retro fanfare punch
            val sine = sin(2.0 * PI * freq * t)
            val square = if (sine >= 0) 1.0 else -1.0
            val wave = 0.7 * sine + 0.3 * square
            samples[i] = (wave * envelope * Short.MAX_VALUE * 0.5).toInt().coerceIn(-32768, 32767).toShort()
        }
        return samples
    }

    private fun generateLevelUpSound(): ShortArray {
        val durationMs = 350
        val numSamples = (SAMPLE_RATE * durationMs / 1000)
        val samples = ShortArray(numSamples)

        val notes = doubleArrayOf(440.0, 554.37, 659.25, 880.0) // A4, C#5, E5, A5
        val seg = numSamples / notes.size

        for (i in 0 until numSamples) {
            val noteIdx = (i / seg).coerceIn(0, notes.size - 1)
            val freq = notes[noteIdx]
            val t = i.toDouble() / SAMPLE_RATE
            val segProgress = (i % seg).toDouble() / seg
            val envelope = exp(-segProgress * 2.0)
            val wave = sin(2.0 * PI * freq * t)
            samples[i] = (wave * envelope * Short.MAX_VALUE * 0.5).toInt().coerceIn(-32768, 32767).toShort()
        }
        return samples
    }

    private fun generateGameOverSound(): ShortArray {
        val durationMs = 600
        val numSamples = (SAMPLE_RATE * durationMs / 1000)
        val samples = ShortArray(numSamples)

        val notes = doubleArrayOf(311.13, 261.63, 196.00) // Eb4, C4, G3
        val seg = numSamples / notes.size

        for (i in 0 until numSamples) {
            val noteIdx = (i / seg).coerceIn(0, notes.size - 1)
            val freq = notes[noteIdx]
            val t = i.toDouble() / SAMPLE_RATE
            val segProgress = (i % seg).toDouble() / seg
            val envelope = exp(-segProgress * 1.8)
            val wave = sin(2.0 * PI * freq * t)
            samples[i] = (wave * envelope * Short.MAX_VALUE * 0.5).toInt().coerceIn(-32768, 32767).toShort()
        }
        return samples
    }
}
