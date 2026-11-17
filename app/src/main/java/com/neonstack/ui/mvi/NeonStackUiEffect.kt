package com.neonstack.ui.mvi

import com.neonstack.audio.SoundEffect
import com.neonstack.engine.LineClearReward

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
    data class FloatingRewardBanner(val reward: LineClearReward) : NeonStackUiEffect
}
