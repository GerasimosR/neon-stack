package com.seniordev.neonstack.engine

/**
 * Original 9-Shape Prism Roster for Neon Stack's 9x16 playfield.
 * Combines 7 four-cell poly-cores with 2 signature three-cell shapes (ELBOW and PULSE)
 * using a custom Synthwave Prism color palette.
 */
enum class BlockShape(
    val id: Int,
    val colorHex: Long,
    val initialSpawnOffset: Point,
    private val stateMinos: Array<Array<Point>>
) {
    BEAM(
        id = 1,
        colorHex = 0xFF2EFFC5L, // Electric Mint
        initialSpawnOffset = Point(2, 0),
        stateMinos = arrayOf(
            arrayOf(Point(0, 1), Point(1, 1), Point(2, 1), Point(3, 1)),
            arrayOf(Point(2, 0), Point(2, 1), Point(2, 2), Point(2, 3)),
            arrayOf(Point(0, 2), Point(1, 2), Point(2, 2), Point(3, 2)),
            arrayOf(Point(1, 0), Point(1, 1), Point(1, 2), Point(1, 3))
        )
    ),
    HOOK_L(
        id = 2,
        colorHex = 0xFF9D4EDDL, // Cyber Violet
        initialSpawnOffset = Point(3, 0),
        stateMinos = arrayOf(
            arrayOf(Point(0, 0), Point(0, 1), Point(1, 1), Point(2, 1)),
            arrayOf(Point(1, 0), Point(2, 0), Point(1, 1), Point(1, 2)),
            arrayOf(Point(0, 1), Point(1, 1), Point(2, 1), Point(2, 2)),
            arrayOf(Point(1, 0), Point(1, 1), Point(0, 2), Point(1, 2))
        )
    ),
    HOOK_R(
        id = 3,
        colorHex = 0xFFFF5E7EL, // Solar Coral
        initialSpawnOffset = Point(3, 0),
        stateMinos = arrayOf(
            arrayOf(Point(2, 0), Point(0, 1), Point(1, 1), Point(2, 1)),
            arrayOf(Point(1, 0), Point(1, 1), Point(1, 2), Point(2, 2)),
            arrayOf(Point(0, 1), Point(1, 1), Point(2, 1), Point(0, 2)),
            arrayOf(Point(0, 0), Point(1, 0), Point(1, 1), Point(1, 2))
        )
    ),
    CORE(
        id = 4,
        colorHex = 0xFF38BDF8L, // Laser Sky
        initialSpawnOffset = Point(3, 0),
        stateMinos = arrayOf(
            arrayOf(Point(0, 0), Point(1, 0), Point(0, 1), Point(1, 1)),
            arrayOf(Point(0, 0), Point(1, 0), Point(0, 1), Point(1, 1)),
            arrayOf(Point(0, 0), Point(1, 0), Point(0, 1), Point(1, 1)),
            arrayOf(Point(0, 0), Point(1, 0), Point(0, 1), Point(1, 1))
        )
    ),
    ZIG(
        id = 5,
        colorHex = 0xFFFFB703L, // Hyper Amber
        initialSpawnOffset = Point(3, 0),
        stateMinos = arrayOf(
            arrayOf(Point(1, 0), Point(2, 0), Point(0, 1), Point(1, 1)),
            arrayOf(Point(1, 0), Point(1, 1), Point(2, 1), Point(2, 2)),
            arrayOf(Point(1, 1), Point(2, 1), Point(0, 2), Point(1, 2)),
            arrayOf(Point(0, 0), Point(0, 1), Point(1, 1), Point(1, 2))
        )
    ),
    CROWN(
        id = 6,
        colorHex = 0xFFFF2EE6L, // Neon Magenta
        initialSpawnOffset = Point(3, 0),
        stateMinos = arrayOf(
            arrayOf(Point(1, 0), Point(0, 1), Point(1, 1), Point(2, 1)),
            arrayOf(Point(1, 0), Point(1, 1), Point(2, 1), Point(1, 2)),
            arrayOf(Point(0, 1), Point(1, 1), Point(2, 1), Point(1, 2)),
            arrayOf(Point(1, 0), Point(0, 1), Point(1, 1), Point(1, 2))
        )
    ),
    ZAG(
        id = 7,
        colorHex = 0xFFA3FF12L, // Acid Lime
        initialSpawnOffset = Point(3, 0),
        stateMinos = arrayOf(
            arrayOf(Point(0, 0), Point(1, 0), Point(1, 1), Point(2, 1)),
            arrayOf(Point(2, 0), Point(1, 1), Point(2, 1), Point(1, 2)),
            arrayOf(Point(0, 1), Point(1, 1), Point(1, 2), Point(2, 2)),
            arrayOf(Point(1, 0), Point(0, 1), Point(1, 1), Point(0, 2))
        )
    ),
    // Signature 3-Cell Neon Elbow (tactical corner filler & Overclock Spin piece)
    ELBOW(
        id = 8,
        colorHex = 0xFFFF3D8BL, // Electric Rose
        initialSpawnOffset = Point(3, 0),
        stateMinos = arrayOf(
            arrayOf(Point(0, 0), Point(0, 1), Point(1, 1)),
            arrayOf(Point(0, 0), Point(1, 0), Point(0, 1)),
            arrayOf(Point(0, 0), Point(1, 0), Point(1, 1)),
            arrayOf(Point(1, 0), Point(0, 1), Point(1, 1))
        )
    ),
    // Signature 3-Cell Neon Pulse (compact tri-beam tailored for 9-column grids)
    PULSE(
        id = 9,
        colorHex = 0xFFFFD166L, // Radiant Gold
        initialSpawnOffset = Point(3, 0),
        stateMinos = arrayOf(
            arrayOf(Point(0, 1), Point(1, 1), Point(2, 1)),
            arrayOf(Point(1, 0), Point(1, 1), Point(1, 2)),
            arrayOf(Point(0, 1), Point(1, 1), Point(2, 1)),
            arrayOf(Point(1, 0), Point(1, 1), Point(1, 2))
        )
    );

    fun getMinos(state: RotationState): Array<Point> {
        return stateMinos[state.ordinal]
    }

    companion object {
        fun fromId(id: Int): BlockShape? = entries.find { it.id == id }
    }
}
