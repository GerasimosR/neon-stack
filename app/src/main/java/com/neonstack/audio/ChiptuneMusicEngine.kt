package com.neonstack.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.tanh

/**
 * Procedural 3-Channel Polyphonic Synthwave Chiptune Engine (10/10 Masterwork Edition).
 * Synthesizes an original "Neon Pulse" cyber-arcade soundtrack in D minor.
 *
 * Audio Engineering Features:
 * 1. Stereo Spatial Imaging (PCM 16-bit 44.1kHz Stereo) with constant-power panning
 *    (ping-pong lead melody, staged hi-hat right, staged snare left, centered punchy kick & bass).
 * 2. State-Adaptive 1-Pole Low-Pass Filter (LPF): Smooth dynamic filter sweep
 *    (680Hz lo-fi muffled warm ambiance during pause/relic draft, sweeping open to 18kHz during gameplay).
 * 3. Analog Tape Soft Clipper (tanh saturation): Eliminates digital square-wave harshness
 *    and produces warm, rich analog harmonics with pristine master headroom.
 * 4. Dynamic Danger State ("Red Zone" Tension): Stack height sensing dynamically accelerates
 *    tempo by +18% and drives 16th-note urgent percussion pulses when blocks reach top rows.
 * 5. Level-based dynamic BPM tempo scaling with smooth glide.
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

        private const val SQRT2_OVER_2 = 0.7071067811865476
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
    private var baseBpm: Double = 120.0
    private var currentBpm: Double = 120.0
    private var targetBpm: Double = 120.0

    // Master volume attenuation (leaves headroom for SFX)
    var masterVolume: Float = 0.35f

    // Dynamic Filter & Danger State
    @Volatile private var isMuffled: Boolean = false
    @Volatile private var isDangerMode: Boolean = false
    private var currentCutoff: Double = 18000.0
    private var targetCutoff: Double = 18000.0

    // Note representation: frequency in Hz, duration in 16th-note steps
    private data class MusicNote(val freq: Double, val steps: Int)

    // Percussion enum
    private enum class DrumType { NONE, KICK, HIHAT, SNARE }

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
     * Level 1: 120 BPM -> Level 15: ~183 BPM.
     */
    fun setLevel(level: Int) {
        val clamped = level.coerceIn(1, 15)
        baseBpm = 120.0 + (clamped - 1) * 4.5
        updateTargetBpm()
    }

    /**
     * Enables or disables dynamic low-pass filter (LPF) muffling.
     * When true (paused, dialogs, relic draft): cutoff smoothly swoops to ~680Hz (warm lo-fi).
     * When false (gameplay active): cutoff smoothly swoops open to 18000Hz (full crisp spectrum).
     */
    fun setFilterMuffled(muffled: Boolean) {
        isMuffled = muffled
        targetCutoff = if (muffled) 680.0 else 18000.0
    }

    /**
     * Activates "Red Zone" danger state tension when blocks reach top 4 rows.
     * Accelerates BPM by +18% and drives intense 16th-note drum groove.
     */
    fun setDangerMode(isDanger: Boolean) {
        if (isDangerMode != isDanger) {
            isDangerMode = isDanger
            updateTargetBpm()
        }
    }

    private fun updateTargetBpm() {
        targetBpm = baseBpm * (if (isDangerMode) 1.18 else 1.0)
    }

    /**
     * Analog Tape Soft Clipper (tanh saturation curve).
     * Compresses signal smoothly to prevent digital clipping while adding warm tape saturation.
     */
    private fun softClip(x: Double): Double = tanh(x)

    private fun audioLoop() {
        val minBufferSize = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val trackBufferSize = max(minBufferSize, BUFFER_SIZE * 4 * 2)

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
                    .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                    .build()
            )
            .setBufferSizeInBytes(trackBufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack.play()

        // Interleaved Stereo buffer: [L0, R0, L1, R1, ...]
        val pcmBuffer = ShortArray(BUFFER_SIZE * 2)

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
        var kickPhase = 0.0
        var noiseSeed = 0x7FFF

        // Filter state memories for L & R channels
        var filterL = 0.0
        var filterR = 0.0

        fun samplesPerStep(bpm: Double): Int {
            return (SAMPLE_RATE * 60.0 / (bpm * 4.0)).toInt()
        }

        leadSamplesInCurrentNote = leadMelody[0].steps * samplesPerStep(currentBpm)
        bassSamplesInCurrentNote = bassMelody[0].steps * samplesPerStep(currentBpm)

        // Staged Drum Panning Constant-Power Gains
        // Snare panned slightly left (-0.25)
        val snareAngle = (-0.25 + 1.0) * (PI / 4.0)
        val snareGainL = cos(snareAngle)
        val snareGainR = sin(snareAngle)

        // HiHat panned slightly right (+0.35)
        val hiHatAngle = (0.35 + 1.0) * (PI / 4.0)
        val hiHatGainL = cos(hiHatAngle)
        val hiHatGainR = sin(hiHatAngle)

        try {
            while (isRunning.get()) {
                if (!isPlaying.get() || !isEnabled) {
                    pcmBuffer.fill(0)
                    audioTrack.write(pcmBuffer, 0, pcmBuffer.size)
                    Thread.sleep(20)
                    continue
                }

                // Smoothly interpolate current BPM towards target BPM
                if (abs(currentBpm - targetBpm) > 0.05) {
                    currentBpm += (targetBpm - currentBpm) * 0.02
                }

                val currentStepSamples = samplesPerStep(currentBpm)

                for (i in 0 until BUFFER_SIZE) {
                    // Smooth low-pass filter cutoff transition (produces organic "whoosh" sweep)
                    if (abs(currentCutoff - targetCutoff) > 1.0) {
                        currentCutoff += (targetCutoff - currentCutoff) * 0.0006
                    }
                    val omega = 2.0 * PI * currentCutoff / SAMPLE_RATE
                    val filterAlpha = (omega / (1.0 + omega)).coerceIn(0.001, 1.0)

                    // --- Channel 1: Lead Pulse Wave (25% Duty Cycle) ---
                    val leadNote = leadMelody[leadIndex]
                    var leadSample = 0.0
                    if (leadNote.freq > 0.0) {
                        leadPhase += leadNote.freq / SAMPLE_RATE
                        if (leadPhase >= 1.0) leadPhase -= 1.0
                        val pulse = if (leadPhase < 0.25) 1.0 else -1.0
                        val noteProgress = leadSampleProgress.toDouble() / leadSamplesInCurrentNote
                        val attack = (noteProgress / 0.04).coerceIn(0.0, 1.0)
                        val release = if (noteProgress > 0.85) (1.0 - noteProgress) / 0.15 else 1.0
                        leadSample = pulse * attack * release
                    }

                    leadSampleProgress++
                    if (leadSampleProgress >= leadSamplesInCurrentNote) {
                        leadSampleProgress = 0
                        leadIndex = (leadIndex + 1) % leadMelody.size
                        leadSamplesInCurrentNote = leadMelody[leadIndex].steps * currentStepSamples
                    }

                    // Dynamic Lead Ping-Pong Panning: subtly dances between L (-0.28) and R (+0.28)
                    val leadPan = if (leadIndex % 2 == 0) -0.28 else 0.28
                    val leadAngle = (leadPan + 1.0) * (PI / 4.0)
                    val leadGainL = cos(leadAngle)
                    val leadGainR = sin(leadAngle)

                    // --- Channel 2: Bass Triangle Wave (Centered) ---
                    val bassNote = bassMelody[bassIndex]
                    var bassSample = 0.0
                    if (bassNote.freq > 0.0) {
                        bassPhase += bassNote.freq / SAMPLE_RATE
                        if (bassPhase >= 1.0) bassPhase -= 1.0
                        val rawTriangle = 2.0 * abs(2.0 * (bassPhase - floor(bassPhase + 0.5))) - 1.0
                        val noteProgress = bassSampleProgress.toDouble() / bassSamplesInCurrentNote
                        val attack = (noteProgress / 0.03).coerceIn(0.0, 1.0)
                        val release = if (noteProgress > 0.90) (1.0 - noteProgress) / 0.10 else 1.0
                        bassSample = rawTriangle * attack * release
                    }

                    bassSampleProgress++
                    if (bassSampleProgress >= bassSamplesInCurrentNote) {
                        bassSampleProgress = 0
                        bassIndex = (bassIndex + 1) % bassMelody.size
                        bassSamplesInCurrentNote = bassMelody[bassIndex].steps * currentStepSamples
                    }

                    // --- Channel 3: Chiptune Percussion ---
                    var drumSample = 0.0
                    var drumGainL = SQRT2_OVER_2
                    var drumGainR = SQRT2_OVER_2

                    drumSampleProgress++
                    if (drumSampleProgress >= currentStepSamples) {
                        drumSampleProgress = 0
                        stepCounter = (stepCounter + 1) % 16

                        currentDrum = if (isDangerMode) {
                            // High-Tension Danger State: Driving beat with extra syncopation
                            when (stepCounter) {
                                0, 8, 10 -> {
                                    kickPhase = 0.0
                                    DrumType.KICK
                                }
                                4, 12, 14 -> DrumType.SNARE
                                else -> DrumType.HIHAT
                            }
                        } else {
                            // Standard Classic Pocket: Beat 1 & 3 Kick, Beat 2 & 4 Snare
                            when (stepCounter) {
                                0, 8 -> {
                                    kickPhase = 0.0
                                    DrumType.KICK
                                }
                                4, 12 -> DrumType.SNARE
                                2, 6, 10, 14 -> DrumType.HIHAT
                                else -> DrumType.NONE
                            }
                        }
                    }

                    if (currentDrum == DrumType.KICK) {
                        // Procedural TR-808 Kick: 145Hz down to 42Hz pitch sweep (Centered)
                        val kickDuration = currentStepSamples * 1.3
                        val progress = (drumSampleProgress.toDouble() / kickDuration).coerceIn(0.0, 1.0)
                        val kickFreq = 42.0 + 103.0 * exp(-progress * 15.0)
                        kickPhase += kickFreq / SAMPLE_RATE
                        if (kickPhase >= 1.0) kickPhase -= 1.0
                        val kickSine = sin(2.0 * PI * kickPhase)
                        val kickEnv = exp(-progress * 6.5)
                        drumSample = kickSine * kickEnv * 0.95
                        drumGainL = SQRT2_OVER_2
                        drumGainR = SQRT2_OVER_2
                    } else if (currentDrum != DrumType.NONE) {
                        // LFSR Noise Generator for Snare / HiHat
                        val bit = ((noiseSeed shr 0) xor (noiseSeed shr 1)) and 1
                        noiseSeed = (noiseSeed shr 1) or (bit shl 14)
                        val rawNoise = (noiseSeed.toDouble() / 16384.0) - 1.0
                        val isSnare = (currentDrum == DrumType.SNARE)
                        val drumDuration = if (isSnare) currentStepSamples else currentStepSamples / 2
                        val progress = (drumSampleProgress.toDouble() / drumDuration).coerceIn(0.0, 1.0)
                        val env = (1.0 - progress)
                        drumSample = rawNoise * env * (if (isSnare) 0.8 else 0.3)
                        drumGainL = if (isSnare) snareGainL else hiHatGainL
                        drumGainR = if (isSnare) snareGainR else hiHatGainR
                    }

                    // --- Stereo Mixdown (Constant Power Spatial Staging) ---
                    // Lead 42%, Bass 34%, Drums 26%
                    val rawL = (leadSample * leadGainL * 0.42 +
                               bassSample * SQRT2_OVER_2 * 0.34 +
                               drumSample * drumGainL * 0.26)

                    val rawR = (leadSample * leadGainR * 0.42 +
                               bassSample * SQRT2_OVER_2 * 0.34 +
                               drumSample * drumGainR * 0.26)

                    // --- Analog Tape Saturation (Warm tanh clipper) ---
                    val satL = softClip(rawL) * masterVolume
                    val satR = softClip(rawR) * masterVolume

                    // --- State-Adaptive Low-Pass Filter (LPF) ---
                    filterL += filterAlpha * (satL - filterL)
                    filterR += filterAlpha * (satR - filterR)

                    // Write 16-bit PCM Interleaved Stereo samples
                    pcmBuffer[i * 2] = (filterL * Short.MAX_VALUE).toInt().coerceIn(-32768, 32767).toShort()
                    pcmBuffer[i * 2 + 1] = (filterR * Short.MAX_VALUE).toInt().coerceIn(-32768, 32767).toShort()
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
