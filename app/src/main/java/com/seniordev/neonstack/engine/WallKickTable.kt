package com.seniordev.neonstack.engine

object WallKickTable {
    // Standard 3x3 piece kick offsets (Y+ is downwards)
    private val STANDARD_KICKS = mapOf(
        Pair(RotationState.SPAWN, RotationState.CLOCKWISE) to arrayOf(
            Point(0, 0), Point(-1, 0), Point(-1, -1), Point(0, 2), Point(-1, 2)
        ),
        Pair(RotationState.CLOCKWISE, RotationState.SPAWN) to arrayOf(
            Point(0, 0), Point(1, 0), Point(1, 1), Point(0, -2), Point(1, -2)
        ),
        Pair(RotationState.CLOCKWISE, RotationState.FLIP) to arrayOf(
            Point(0, 0), Point(1, 0), Point(1, 1), Point(0, -2), Point(1, -2)
        ),
        Pair(RotationState.FLIP, RotationState.CLOCKWISE) to arrayOf(
            Point(0, 0), Point(-1, 0), Point(-1, -1), Point(0, 2), Point(-1, 2)
        ),
        Pair(RotationState.FLIP, RotationState.COUNTER_CLOCKWISE) to arrayOf(
            Point(0, 0), Point(1, 0), Point(1, -1), Point(0, 2), Point(1, 2)
        ),
        Pair(RotationState.COUNTER_CLOCKWISE, RotationState.FLIP) to arrayOf(
            Point(0, 0), Point(-1, 0), Point(-1, 1), Point(0, -2), Point(-1, -2)
        ),
        Pair(RotationState.COUNTER_CLOCKWISE, RotationState.SPAWN) to arrayOf(
            Point(0, 0), Point(-1, 0), Point(-1, 1), Point(0, -2), Point(-1, -2)
        ),
        Pair(RotationState.SPAWN, RotationState.COUNTER_CLOCKWISE) to arrayOf(
            Point(0, 0), Point(1, 0), Point(1, -1), Point(0, 2), Point(1, 2)
        )
    )

    // Beam (I) kick offsets (Y+ is downwards)
    private val BEAM_KICKS = mapOf(
        Pair(RotationState.SPAWN, RotationState.CLOCKWISE) to arrayOf(
            Point(0, 0), Point(-2, 0), Point(1, 0), Point(-2, 1), Point(1, -2)
        ),
        Pair(RotationState.CLOCKWISE, RotationState.SPAWN) to arrayOf(
            Point(0, 0), Point(2, 0), Point(-1, 0), Point(2, -1), Point(-1, 2)
        ),
        Pair(RotationState.CLOCKWISE, RotationState.FLIP) to arrayOf(
            Point(0, 0), Point(-1, 0), Point(2, 0), Point(-1, -2), Point(2, 1)
        ),
        Pair(RotationState.FLIP, RotationState.CLOCKWISE) to arrayOf(
            Point(0, 0), Point(1, 0), Point(-2, 0), Point(1, 2), Point(-2, -1)
        ),
        Pair(RotationState.FLIP, RotationState.COUNTER_CLOCKWISE) to arrayOf(
            Point(0, 0), Point(2, 0), Point(-1, 0), Point(2, -1), Point(-1, 2)
        ),
        Pair(RotationState.COUNTER_CLOCKWISE, RotationState.FLIP) to arrayOf(
            Point(0, 0), Point(-2, 0), Point(1, 0), Point(-2, 1), Point(1, -2)
        ),
        Pair(RotationState.COUNTER_CLOCKWISE, RotationState.SPAWN) to arrayOf(
            Point(0, 0), Point(1, 0), Point(-2, 0), Point(1, 2), Point(-2, -1)
        ),
        Pair(RotationState.SPAWN, RotationState.COUNTER_CLOCKWISE) to arrayOf(
            Point(0, 0), Point(-1, 0), Point(2, 0), Point(-1, -2), Point(2, 1)
        )
    )

    private val ZERO_KICK = arrayOf(Point(0, 0))

    fun getKicks(type: BlockShape, from: RotationState, to: RotationState): Array<Point> {
        if (type == BlockShape.O || from == to) {
            return ZERO_KICK
        }
        val key = Pair(from, to)
        return if (type == BlockShape.I) {
            BEAM_KICKS[key] ?: ZERO_KICK
        } else {
            STANDARD_KICKS[key] ?: ZERO_KICK
        }
    }
}
