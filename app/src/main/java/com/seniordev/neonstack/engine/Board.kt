package com.seniordev.neonstack.engine

class Board {
    companion object {
        const val WIDTH = 10
        const val BUFFER_ROWS = 2
        const val VISIBLE_ROWS = 18
        const val TOTAL_ROWS = BUFFER_ROWS + VISIBLE_ROWS // 20
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
            // Check horizontal bounds
            if (mino.x !in 0 until WIDTH) {
                return false
            }
            // Check bottom floor bound
            if (mino.y >= TOTAL_ROWS) {
                return false
            }
            // If block is above the ceiling (y < 0), allow it in spawn zone unless x is invalid
            if (mino.y >= 0) {
                if (grid[mino.y][mino.x] != 0) {
                    return false
                }
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
     * Calculates the landing position (hard drop / projection guide) for the given active piece.
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
     * 3-corner Pivot Spin detection for T-shaped pieces.
     * @param piece The active piece (must be T)
     * @param lastActionWasRotate True if the action immediately prior to locking was a rotation
     * @param lastKickIndex 0 for basic rotation, 1..4 for wall kicks (test 5 is index 4)
     */
    fun checkPivotSpin(piece: ActivePiece, lastActionWasRotate: Boolean, lastKickIndex: Int): SpinBonus {
        if (piece.type != BlockShape.T || !lastActionWasRotate) {
            return SpinBonus.NONE
        }

        // Crown piece center is at position + (1, 1)
        val cx = piece.position.x + 1
        val cy = piece.position.y + 1

        fun isOccupied(x: Int, y: Int): Boolean {
            if (x !in 0 until WIDTH || y >= TOTAL_ROWS) return true
            if (y < 0) return false
            return grid[y][x] != 0
        }

        val tl = isOccupied(cx - 1, cy - 1)
        val tr = isOccupied(cx + 1, cy - 1)
        val bl = isOccupied(cx - 1, cy + 1)
        val br = isOccupied(cx + 1, cy + 1)

        val totalOccupied = (if (tl) 1 else 0) + (if (tr) 1 else 0) + (if (bl) 1 else 0) + (if (br) 1 else 0)
        if (totalOccupied < 3) {
            return SpinBonus.NONE
        }

        val (front1, front2) = when (piece.rotation) {
            RotationState.SPAWN -> Pair(tl, tr)
            RotationState.CLOCKWISE -> Pair(tr, br)
            RotationState.FLIP -> Pair(bl, br)
            RotationState.COUNTER_CLOCKWISE -> Pair(tl, bl)
        }

        return if ((front1 && front2) || lastKickIndex == 4) {
            SpinBonus.FULL
        } else {
            SpinBonus.MINI
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
