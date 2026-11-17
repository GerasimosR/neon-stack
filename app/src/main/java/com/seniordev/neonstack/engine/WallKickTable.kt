package com.seniordev.neonstack.engine

/**
 * Custom Neon Stack Omni-Kick Table tuned for mixed 3-cell and 4-cell prism shapes
 * on a 9x16 playfield.
 */
object WallKickTable {
    private val ZERO_KICK = arrayOf(Point(0, 0))

    // Clockwise pivot offsets: prioritize natural nudge, opposite nudge, tuck down, then lift
    private val STANDARD_CW_KICKS = arrayOf(
        Point(0, 0),
        Point(-1, 0),
        Point(1, 0),
        Point(0, 1),
        Point(-1, 1),
        Point(1, 1),
        Point(0, -1)
    )

    // Counter-clockwise pivot offsets (mirrored horizontal priority)
    private val STANDARD_CCW_KICKS = arrayOf(
        Point(0, 0),
        Point(1, 0),
        Point(-1, 0),
        Point(0, 1),
        Point(1, 1),
        Point(-1, 1),
        Point(0, -1)
    )

    // Extended 4-cell Beam kick offsets (handles 2-cell wall clearance cleanly)
    private val BEAM_KICKS = arrayOf(
        Point(0, 0),
        Point(-1, 0),
        Point(1, 0),
        Point(-2, 0),
        Point(2, 0),
        Point(0, 1),
        Point(0, -1),
        Point(0, -2)
    )

    fun getKicks(type: BlockShape, from: RotationState, to: RotationState): Array<Point> {
        if (type == BlockShape.CORE || from == to) {
            return ZERO_KICK
        }
        if (type == BlockShape.BEAM) {
            return BEAM_KICKS
        }
        val isCw = (from.rotateCw() == to)
        return if (isCw) STANDARD_CW_KICKS else STANDARD_CCW_KICKS
    }
}
