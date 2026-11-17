package com.neonstack.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max

/**
 * Procedural 3-Channel Polyphonic Synthwave Chiptune Engine.
 * Synthesizes an original "Neon Pulse" cyber-arcade soundtrack in D minor
 * with dynamic tempo (BPM) scaling based on player level.
 *
 * Channels:
 * 1. Pulse Wave (25% Duty Cycle) - Syncopated Arcade Lead
 * 2. Triangle Wave - Pumping Octave Synthwave Bass
 * 3. Noise Channel - 15-bit LFSR Chiptune Percussion (Hi-Hat / Snare)
 */
class ChiptuneMusicEngine {

    companion object {
        private const val SAMPLE_RATE = 44100
        private const val BUFFER_SIZE = 2048

        // Musical Frequencies (Hz) — D Minor / Synthwave scale
        private const val REST = 0.0
        private const val D2 = 73.42
        private const val F2 = 87.31
        private const val G2 = 98.00
        private const val A2 = 110.00
        private const val Bb2 = 116.54
        private const val C3 = 130.81
        private const val Cs3 = 138.59
        private const val D3 = 146.83
        private const val E3 = 164.81
        private const val F3 = 174.61
        private const val G3 = 196.00
        private const val A4 = 440.00
        private const val Bb4 = 466.16
        private const val C5 = 523.25
        private const val Cs5 = 554.37
        private const val D5 = 587.33
        private const val E5 = 659.25
        private const val F5 = 698.46
        private const val G5 = 783.99
        private const val A5 = 880.00
        private const val Bb5 = 932.33
    }

    var isEnabled: Boolean = true
        set(value) {
            field = value
            if (!value) {
                pause()
            }
        }

    private val isRunning = AtomicBoolean(false)
    private val isPlaying = AtomicBoolean(false)
    private var audioThread: Thread? = null

    // Dynamic Tempo Parameters
    private var currentBpm: Double = 120.0
    private var targetBpm: Double = 120.0

    // Master volume attenuation (leaves headroom for SFX)
    var masterVolume: Float = 0.35f

    // Note representation: frequency in Hz, duration in 16th-note steps
    private data class MusicNote(val freq: Double, val steps: Int)

    // Percussion enum
    private enum class DrumType { NONE, HIHAT, SNARE }

    // Original "Neon Pulse" Synthwave Lead (16 bars = 256 sixteenth-note steps)
    private val leadMelody = listOf(
        // Section A — Driving Cyber Hook (Dm -> Bb -> C -> A7)
        MusicNote(D5, 2), MusicNote(F5, 2), MusicNote(A5, 4), MusicNote(G5, 2), MusicNote(F5, 2), MusicNote(E5, 2), MusicNote(D5, 2),
        MusicNote(F5, 4), MusicNote(D5, 2), MusicNote(Bb4, 2), MusicNote(D5, 4), MusicNote(F5, 2), MusicNote(G5, 2),
        MusicNote(E5, 2), MusicNote(G5, 2), MusicNote(C5, 4), MusicNote(G5, 2), MusicNote(F5, 2), MusicNote(E5, 2), MusicNote(C5, 2),
        MusicNote(E5, 4), MusicNote(Cs5, 2), MusicNote(A4, 2), MusicNote(Cs5, 4), MusicNote(E5, 4),

        // Section A2 — Escalating Arpeggio
        MusicNote(D5, 2), MusicNote(F5, 2), MusicNote(A5, 2), MusicNote(F5, 2), MusicNote(G5, 2), MusicNote(A5, 2), MusicNote(Bb5, 2), MusicNote(A5, 2),
        MusicNote(G5, 4), MusicNote(F5, 2), MusicNote(D5, 2), MusicNote(F5, 4), MusicNote(G5, 4),
        MusicNote(E5, 2), MusicNote(G5, 2), MusicNote(A5, 2), MusicNote(G5, 2), MusicNote(F5, 2), MusicNote(E5, 2), MusicNote(D5, 2), MusicNote(C5, 2),
        MusicNote(D5, 6), MusicNote(A4, 2), MusicNote(D5, 4), MusicNote(REST, 4),

        // Section B — Soaring Neon Chorus (Gm -> Dm -> Bb -> A7)
        MusicNote(G5, 4), MusicNote(Bb5, 2), MusicNote(A5, 2), MusicNote(G5, 4), MusicNote(D5, 4),
        MusicNote(F5, 4), MusicNote(A5, 2), MusicNote(G5, 2), MusicNote(F5, 4), MusicNote(D5, 4),
        MusicNote(D5, 2), MusicNote(F5, 2), MusicNote(Bb5, 4), MusicNote(A5, 2), MusicNote(G5, 2), MusicNote(F5, 2), MusicNote(D5, 2),
        MusicNote(E5, 6), MusicNote(Cs5, 2), MusicNote(A4, 4), MusicNote(E5, 4),

        // Section B2 — Turnaround Lift (Gm -> Dm -> C -> A7)
        MusicNote(G5, 2), MusicNote(A5, 2), MusicNote(Bb5, 4), MusicNote(A5, 2), MusicNote(G5, 2), MusicNote(F5, 2), MusicNote(G5, 2),
        MusicNote(A5, 4), MusicNote(F5, 2), MusicNote(D5, 2), MusicNote(F5, 4), MusicNote(A5, 4),
        MusicNote(G5, 4), MusicNote(E5, 2), MusicNote(C5, 2), MusicNote(E5, 2), MusicNote(G5, 2), MusicNote(A5, 2), MusicNote(G5, 2),
        MusicNote(A5, 4), MusicNote(G5, 2), MusicNote(F5, 2), MusicNote(E5, 4), MusicNote(Cs5, 2), MusicNote(REST, 2)
    )

    // Pumping Octave Synthwave Bassline (16 bars = 256 sixteenth-note steps)
    private val bassMelody = listOf(
        // Section A
        MusicNote(D2, 2), MusicNote(D3, 2), MusicNote(D2, 2), MusicNote(D3, 2), MusicNote(D2, 2), MusicNote(D3, 2), MusicNote(F2, 2), MusicNote(A2, 2),
        MusicNote(Bb2, 2), MusicNote(F3, 2), MusicNote(Bb2, 2), MusicNote(F3, 2), MusicNote(Bb2, 2), MusicNote(F3, 2), MusicNote(D3, 2), MusicNote(Bb2, 2),
        MusicNote(C3, 2), MusicNote(G3, 2), MusicNote(C3, 2), MusicNote(G3, 2), MusicNote(C3, 2), MusicNote(G3, 2), MusicNote(E3, 2), MusicNote(C3, 2),
        MusicNote(A2, 2), MusicNote(E3, 2), MusicNote(A2, 2), MusicNote(E3, 2), MusicNote(A2, 2), MusicNote(Cs3, 2), MusicNote(E3, 2), MusicNote(G3, 2),

        // Section A2
        MusicNote(D2, 2), MusicNote(D3, 2), MusicNote(D2, 2), MusicNote(D3, 2), MusicNote(D2, 2), MusicNote(D3, 2), MusicNote(F2, 2), MusicNote(A2, 2),
        MusicNote(Bb2, 2), MusicNote(F3, 2), MusicNote(Bb2, 2), MusicNote(F3, 2), MusicNote(Bb2, 2), MusicNote(F3, 2), MusicNote(D3, 2), MusicNote(Bb2, 2),
        MusicNote(C3, 2), MusicNote(G3, 2), MusicNote(C3, 2), MusicNote(G3, 2), MusicNote(C3, 2), MusicNote(G3, 2), MusicNote(E3, 2), MusicNote(C3, 2),
        MusicNote(D2, 4), MusicNote(A2, 4), MusicNote(D3, 4), MusicNote(REST, 4),

        // Section B
        MusicNote(G2, 2), MusicNote(G3, 2), MusicNote(G2, 2), MusicNote(G3, 2), MusicNote(G2, 2), MusicNote(Bb2, 2), MusicNote(D3, 2), MusicNote(G3, 2),
        MusicNote(D2, 2), MusicNote(D3, 2), MusicNote(D2, 2), MusicNote(D3, 2), MusicNote(D2, 2), MusicNote(F2, 2), MusicNote(A2, 2), MusicNote(D3, 2),
        MusicNote(Bb2, 2), MusicNote(F3, 2), MusicNote(Bb2, 2), MusicNote(F3, 2), MusicNote(Bb2, 2), MusicNote(D3, 2), MusicNote(F3, 2), MusicNote(D3, 2),
        MusicNote(A2, 2), MusicNote(E3, 2), MusicNote(A2, 2), MusicNote(E3, 2), MusicNote(A2, 2), MusicNote(Cs3, 2), MusicNote(E3, 2), MusicNote(A2, 2),

        // Section B2
        MusicNote(G2, 2), MusicNote(G3, 2), MusicNote(G2, 2), MusicNote(G3, 2), MusicNote(G2, 2), MusicNote(Bb2, 2), MusicNote(D3, 2), MusicNote(G3, 2),
        MusicNote(D2, 2), MusicNote(D3, 2), MusicNote(D2, 2), MusicNote(D3, 2), MusicNote(D2, 2), MusicNote(F2, 2), MusicNote(A2, 2), MusicNote(D3, 2),
        MusicNote(C3, 2), MusicNote(G3, 2), MusicNote(C3, 2), MusicNote(G3, 2), MusicNote(C3, 2), MusicNote(E3, 2), MusicNote(G3, 2), MusicNote(C3, 2),
        MusicNote(A2, 4), MusicNote(Cs3, 4), MusicNote(E3, 4), MusicNote(A2, 4)
    )

    fun start() {
        if (isRunning.get()) return
        isRunning.set(true)
        isPlaying.set(true)

        audioThread = Thread({ audioLoop() }, "NeonStackMusicThread").apply {
            priority = Thread.NORM_PRIORITY - 1
            start()
        }
    }

    fun play() {
        if (!isEnabled) return
        if (!isRunning.get()) {
            start()
        }
        isPlaying.set(true)
    }

    fun pause() {
        isPlaying.set(false)
    }

    fun resume() {
        if (isEnabled) {
            isPlaying.set(true)
        }
    }

    fun stop() {
        isPlaying.set(false)
    }

    fun release() {
        isRunning.set(false)
        isPlaying.set(false)
        audioThread?.interrupt()
        audioThread = null
    }

    /**
     * Dynamically scales tempo (BPM) based on player level.
     * Level 1: 120 BPM -> Level 15: ~183 BPM!
     */
    fun setLevel(level: Int) {
        val clamped = level.coerceIn(1, 15)
        targetBpm = 120.0 + (clamped - 1) * 4.5
    }

    private fun audioLoop() {
        val minBufferSize = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val trackBufferSize = max(minBufferSize, BUFFER_SIZE * 4)

        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(trackBufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack.play()

        val pcmBuffer = ShortArray(BUFFER_SIZE)

        // Song timeline cursors
        var leadIndex = 0
        var leadSampleProgress = 0
        var leadSamplesInCurrentNote: Int

        var bassIndex = 0
        var bassSampleProgress = 0
        var bassSamplesInCurrentNote: Int

        var stepCounter = 0
        var drumSampleProgress = 0
        var currentDrum = DrumType.NONE

        var leadPhase = 0.0
        var bassPhase = 0.0
        var noiseSeed = 0x7FFF

        fun samplesPerStep(bpm: Double): Int {
            return (SAMPLE_RATE * 60.0 / (bpm * 4.0)).toInt()
        }

        leadSamplesInCurrentNote = leadMelody[0].steps * samplesPerStep(currentBpm)
        bassSamplesInCurrentNote = bassMelody[0].steps * samplesPerStep(currentBpm)

        try {
            while (isRunning.get()) {
                if (!isPlaying.get() || !isEnabled) {
                    pcmBuffer.fill(0)
                    audioTrack.write(pcmBuffer, 0, pcmBuffer.size)
                    Thread.sleep(20)
                    continue
                }

                // Smoothly interpolate current BPM towards target BPM
                if (abs(currentBpm - targetBpm) > 0.1) {
                    currentBpm += (targetBpm - currentBpm) * 0.02
                }

                val currentStepSamples = samplesPerStep(currentBpm)

                for (i in 0 until BUFFER_SIZE) {
                    // --- Channel 1: Lead Pulse Wave (25% Duty Cycle) ---
                    val leadNote = leadMelody[leadIndex]
                    var leadSample = 0.0
                    if (leadNote.freq > 0.0) {
                        leadPhase += leadNote.freq / SAMPLE_RATE
                        if (leadPhase >= 1.0) leadPhase -= 1.0
                        val pulse = if (leadPhase < 0.25) 1.0 else -1.0
                        val noteProgress = leadSampleProgress.toDouble() / leadSamplesInCurrentNote
                        val envelope = if (noteProgress > 0.85) (1.0 - noteProgress) / 0.15 else 1.0
                        leadSample = pulse * envelope
                    }

                    leadSampleProgress++
                    if (leadSampleProgress >= leadSamplesInCurrentNote) {
                        leadSampleProgress = 0
                        leadIndex = (leadIndex + 1) % leadMelody.size
                        leadSamplesInCurrentNote = leadMelody[leadIndex].steps * currentStepSamples
                    }

                    // --- Channel 2: Bass Triangle Wave ---
                    val bassNote = bassMelody[bassIndex]
                    var bassSample = 0.0
                    if (bassNote.freq > 0.0) {
                        bassPhase += bassNote.freq / SAMPLE_RATE
                        if (bassPhase >= 1.0) bassPhase -= 1.0
                        bassSample = 2.0 * abs(2.0 * (bassPhase - floor(bassPhase + 0.5))) - 1.0
                    }

                    bassSampleProgress++
                    if (bassSampleProgress >= bassSamplesInCurrentNote) {
                        bassSampleProgress = 0
                        bassIndex = (bassIndex + 1) % bassMelody.size
                        bassSamplesInCurrentNote = bassMelody[bassIndex].steps * currentStepSamples
                    }

                    // --- Channel 3: Chiptune Drums (LFSR Noise) ---
                    var drumSample = 0.0
                    drumSampleProgress++
                    if (drumSampleProgress >= currentStepSamples) {
                        drumSampleProgress = 0
                        stepCounter = (stepCounter + 1) % 16
                        currentDrum = when {
                            stepCounter == 4 || stepCounter == 12 -> DrumType.SNARE
                            stepCounter % 2 == 0 -> DrumType.HIHAT
                            else -> DrumType.NONE
                        }
                    }

                    if (currentDrum != DrumType.NONE) {
                        val bit = ((noiseSeed shr 0) xor (noiseSeed shr 1)) and 1
                        noiseSeed = (noiseSeed shr 1) or (bit shl 14)
                        val rawNoise = (noiseSeed.toDouble() / 16384.0) - 1.0
                        val drumDuration = if (currentDrum == DrumType.SNARE) currentStepSamples else currentStepSamples / 2
                        val progress = (drumSampleProgress.toDouble() / drumDuration).coerceIn(0.0, 1.0)
                        val env = (1.0 - progress)
                        drumSample = rawNoise * env * (if (currentDrum == DrumType.SNARE) 0.8 else 0.3)
                    }

                    // Mix Channels (Lead 45%, Bass 35%, Drum 20%)
                    val mixed = (leadSample * 0.45 + bassSample * 0.35 + drumSample * 0.20) * masterVolume
                    pcmBuffer[i] = (mixed * Short.MAX_VALUE).toInt().coerceIn(-32768, 32767).toShort()
                }

                audioTrack.write(pcmBuffer, 0, pcmBuffer.size)
            }
        } catch (_: InterruptedException) {
            // Audio thread stopped cleanly
        } finally {
            try {
                audioTrack.stop()
                audioTrack.release()
            } catch (_: Exception) {}
        }
    }
}
