package com.seniordev.neonstack.ui.mvi

import com.seniordev.neonstack.audio.SoundEffect

enum class HapticType {
    TICK,
    CLICK,
    HEAVY_IMPACT,
    LINE_CLEAR,
    QUAD_CLEAR
}

sealed interface NeonStackUiEffect {
    data class PlaySound(val sound: SoundEffect) : NeonStackUiEffect
    data class TriggerHaptic(val type: HapticType) : NeonStackUiEffect
    data class ScreenShake(val magnitude: Float) : NeonStackUiEffect
    data class LineClearAnimation(val rows: List<Int>, val message: String) : NeonStackUiEffect
    data class FloatingMessage(val text: String) : NeonStackUiEffect
}
