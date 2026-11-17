package com.neonstack.engine

sealed class GameEvent {
    data class PieceMoved(val dx: Int) : GameEvent()
    data class PieceRotated(val type: BlockShape, val kickIndex: Int) : GameEvent()
    object PieceSoftDropped : GameEvent()
    data class PieceHardDropped(val distance: Int) : GameEvent()
    data class PieceLocked(val type: BlockShape) : GameEvent()
    data class HoldUsed(val heldPiece: BlockShape) : GameEvent()
    data class LinesCleared(val reward: LineClearReward, val rows: List<Int>) : GameEvent()
    data class LevelUp(val newLevel: Int) : GameEvent()
    data class FloorCleared(val floor: Int, val draftOptions: List<com.neonstack.engine.rogue.Relic>) : GameEvent()
    data class RelicAcquired(val relic: com.neonstack.engine.rogue.Relic) : GameEvent()
    data class GameOver(val finalScore: Long, val lines: Int, val level: Int) : GameEvent()
    object GamePaused : GameEvent()
    object GameResumed : GameEvent()
    object GameRestarted : GameEvent()
}
