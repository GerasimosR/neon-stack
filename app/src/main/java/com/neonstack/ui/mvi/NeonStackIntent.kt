package com.neonstack.ui.mvi

sealed interface NeonStackIntent {
    object MoveLeft : NeonStackIntent
    object MoveRight : NeonStackIntent
    object SoftDrop : NeonStackIntent
    object HardDrop : NeonStackIntent
    object RotateCw : NeonStackIntent
    object RotateCcw : NeonStackIntent
    object Hold : NeonStackIntent
    object Pause : NeonStackIntent
    object Resume : NeonStackIntent
    object Restart : NeonStackIntent
    data class StartGame(val mode: com.neonstack.engine.rogue.GameMode) : NeonStackIntent
    object ReturnToMenu : NeonStackIntent
    object OpenCalibration : NeonStackIntent
    object CloseCalibration : NeonStackIntent
    data class SaveCalibration(val das: Long, val arr: Long, val softDrop: Long) : NeonStackIntent
    object ResetCalibration : NeonStackIntent
    object OpenLeaderboard : NeonStackIntent
    object CloseLeaderboard : NeonStackIntent
    data class RecordHighScorer(val name: String) : NeonStackIntent
    object ResetLeaderboard : NeonStackIntent
    data class SwitchGameMode(val mode: com.neonstack.engine.rogue.GameMode) : NeonStackIntent
    data class SelectRelic(val relic: com.neonstack.engine.rogue.Relic) : NeonStackIntent
    data class SetSoundEnabled(val enabled: Boolean) : NeonStackIntent
    data class SetMusicEnabled(val enabled: Boolean) : NeonStackIntent
    data class SetHapticsEnabled(val enabled: Boolean) : NeonStackIntent
    data class SetCrtFilterEnabled(val enabled: Boolean) : NeonStackIntent
    data class SetControlMode(val mode: com.neonstack.data.ControlMode) : NeonStackIntent
}
