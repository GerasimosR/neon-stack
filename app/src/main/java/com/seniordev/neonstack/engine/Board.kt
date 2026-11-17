package com.seniordev.neonstack.engine

class Board {
    companion object {
        const val WIDTH = 9
        const val BUFFER_ROWS = 2
        const val VISIBLE_ROWS = 16
        const val TOTAL_ROWS = BUFFER_ROWS + VISIBLE_ROWS // 18
    }

    // grid[y][x]: 0 = empty, > 0 = BlockShape.id
    val grid = Array(TOTAL_ROWS) { IntArray(WIDTH) { 0 } }

    fun clear() {
        for (y in 0 until TOTAL_ROWS) {
            for (x in 0 until WIDTH) {
                grid[y][x] = 0
            }
        }
    }

    /**
     * Check if a piece with given offset and rotation can be placed without collision.
     */
    fun isValidPosition(piece: ActivePiece, offset: Point = piece.position, state: RotationState = piece.rotation): Boolean {
        val minos = piece.getWorldMinos(offset, state)
        for (mino in minos) {
            if (mino.x !in 0 until WIDTH) {
                return false
            }
            if (mino.y >= TOTAL_ROWS) {
                return false
            }
            if (mino.y >= 0 && grid[mino.y][mino.x] != 0) {
                return false
            }
        }
        return true
    }

    /**
     * Lock the active piece into the playfield matrix.
     */
    fun lock(piece: ActivePiece) {
        val minos = piece.getWorldMinos()
        for (mino in minos) {
            if (mino.y in 0 until TOTAL_ROWS && mino.x in 0 until WIDTH) {
                grid[mino.y][mino.x] = piece.type.id
            }
        }
    }

    /**
     * Calculates the landing position (hard drop / laser rail impact) for the given active piece.
     */
    fun getDropPosition(piece: ActivePiece): Point {
        var dropY = piece.position.y
        while (isValidPosition(piece, Point(piece.position.x, dropY + 1), piece.rotation)) {
            dropY++
        }
        return Point(piece.position.x, dropY)
    }

    /**
     * Identify rows that are completely filled.
     */
    fun findCompletedRows(): List<Int> {
        val completed = mutableListOf<Int>()
        for (y in 0 until TOTAL_ROWS) {
            var full = true
            for (x in 0 until WIDTH) {
                if (grid[y][x] == 0) {
                    full = false
                    break
                }
            }
            if (full) {
                completed.add(y)
            }
        }
        return completed
    }

    /**
     * Clears specified rows and cascades down upper rows.
     */
    fun clearRows(rows: List<Int>) {
        if (rows.isEmpty()) return

        val rowsToClear = rows.toSet()
        val newGrid = Array(TOTAL_ROWS) { IntArray(WIDTH) { 0 } }
        var targetY = TOTAL_ROWS - 1

        for (sourceY in (TOTAL_ROWS - 1) downTo 0) {
            if (!rowsToClear.contains(sourceY)) {
                for (x in 0 until WIDTH) {
                    newGrid[targetY][x] = grid[sourceY][x]
                }
                targetY--
            }
        }

        for (y in 0 until TOTAL_ROWS) {
            for (x in 0 until WIDTH) {
                grid[y][x] = newGrid[y][x]
            }
        }
    }

    /**
     * Neon Stack Overclock Spin detection: rewards tight rotational tucks
     * for both CROWN (4-cell) and ELBOW (3-cell) pieces.
     */
    fun checkOverclockSpin(piece: ActivePiece, lastActionWasRotate: Boolean, lastKickIndex: Int): SpinBonus {
        if (!lastActionWasRotate) return SpinBonus.NONE

        fun isOccupied(x: Int, y: Int): Boolean {
            if (x !in 0 until WIDTH || y >= TOTAL_ROWS) return true
            if (y < 0) return false
            return grid[y][x] != 0
        }

        return when (piece.type) {
            BlockShape.CROWN -> {
                val cx = piece.position.x + 1
                val cy = piece.position.y + 1
                val tl = isOccupied(cx - 1, cy - 1)
                val tr = isOccupied(cx + 1, cy - 1)
                val bl = isOccupied(cx - 1, cy + 1)
                val br = isOccupied(cx + 1, cy + 1)
                val occupiedCorners = (if (tl) 1 else 0) + (if (tr) 1 else 0) + (if (bl) 1 else 0) + (if (br) 1 else 0)
                if (occupiedCorners < 3) return SpinBonus.NONE

                val (front1, front2) = when (piece.rotation) {
                    RotationState.SPAWN -> Pair(tl, tr)
                    RotationState.CLOCKWISE -> Pair(tr, br)
                    RotationState.FLIP -> Pair(bl, br)
                    RotationState.COUNTER_CLOCKWISE -> Pair(tl, bl)
                }
                if ((front1 && front2) || lastKickIndex >= 3) SpinBonus.FULL else SpinBonus.MINI
            }
            BlockShape.ELBOW -> {
                // Check if the 3-cell elbow was tucked under an overhang (occupied block directly above any cell)
                val minos = piece.getWorldMinos()
                val minoSet = minos.toSet()
                val hasOverhang = minos.any { m ->
                    val above = Point(m.x, m.y - 1)
                    !minoSet.contains(above) && isOccupied(above.x, above.y)
                }
                if (!hasOverhang) return SpinBonus.NONE
                if (lastKickIndex > 0) SpinBonus.FULL else SpinBonus.MINI
            }
            else -> SpinBonus.NONE
        }
    }

    /**
     * Ceiling Lock-Out check: returns true if and only if all blocks of the locked piece
     * are completely in the hidden buffer zone (y < BUFFER_ROWS).
     */
    fun isLockedOut(piece: ActivePiece): Boolean {
        val minos = piece.getWorldMinos()
        return minos.all { it.y < BUFFER_ROWS }
    }
}

enum class SpinBonus {
    NONE,
    MINI,
    FULL
}
