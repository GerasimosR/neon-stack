package com.neonstack.engine

data class ActivePiece(
    val type: BlockShape,
    var position: Point,
    var rotation: RotationState = RotationState.SPAWN
) {
    /**
     * Get world coordinates of all 4 blocks of this piece.
     */
    fun getWorldMinos(offset: Point = position, state: RotationState = rotation): List<Point> {
        val minos = type.getMinos(state)
        return minos.map { Point(it.x + offset.x, it.y + offset.y) }
    }

    fun clone(): ActivePiece {
        return ActivePiece(type, position, rotation)
    }

    companion object {
        fun spawn(type: BlockShape): ActivePiece {
            return ActivePiece(type, type.initialSpawnOffset, RotationState.SPAWN)
        }
    }
}
