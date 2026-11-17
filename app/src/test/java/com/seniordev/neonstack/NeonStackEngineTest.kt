package com.seniordev.neonstack

import com.seniordev.neonstack.engine.*
import org.junit.Assert.*
import org.junit.Test

class NeonStackEngineTest {

    @Test
    fun test7BagRandomizerDistribution() {
        val randomizer = BagRandomizer()
        val first7 = (1..7).map { randomizer.nextPiece() }.toSet()
        assertEquals("First 7 pieces must contain all 7 unique block shapes", 7, first7.size)

        val next7 = (1..7).map { randomizer.nextPiece() }.toSet()
        assertEquals("Next 7 pieces must also contain all 7 unique block shapes", 7, next7.size)
    }

    @Test
    fun testBoardDimensionsAndBoundaryCollisions() {
        assertEquals("Playfield width is 10", 10, Board.WIDTH)
        assertEquals("Visible playfield height is 18", 18, Board.VISIBLE_ROWS)
        assertEquals("Total rows including spawn buffer is 20", 20, Board.TOTAL_ROWS)

        val board = Board()
        val piece = ActivePiece(BlockShape.O, Point(0, 10))

        assertTrue("Valid position on board", board.isValidPosition(piece))

        val leftOutOfBounds = ActivePiece(BlockShape.O, Point(-1, 10))
        assertFalse("Cannot place left of boundary", board.isValidPosition(leftOutOfBounds))

        val rightOutOfBounds = ActivePiece(BlockShape.O, Point(9, 10))
        assertFalse("O piece width is 2, so x=9 goes out of right bound 10", board.isValidPosition(rightOutOfBounds))

        val floorCollision = ActivePiece(BlockShape.O, Point(0, Board.TOTAL_ROWS - 1))
        assertFalse("Cannot place past bottom floor", board.isValidPosition(floorCollision))
    }

    @Test
    fun testWallKicks() {
        val board = Board()
        val piece = ActivePiece(BlockShape.T, Point(0, 10), RotationState.SPAWN)
        assertTrue(board.isValidPosition(piece))

        val targetState = piece.rotation.rotateCcw()
        val kicks = WallKickTable.getKicks(piece.type, piece.rotation, targetState)

        var kickSucceeded = false
        for (kick in kicks) {
            val testPos = piece.position + kick
            if (board.isValidPosition(piece, testPos, targetState)) {
                kickSucceeded = true
                assertTrue("Kick must keep piece inside board (x >= 0)", testPos.x >= 0)
                break
            }
        }
        assertTrue("Wall kick must succeed near left wall", kickSucceeded)
    }

    @Test
    fun testLineClearAndCascade() {
        val board = Board()
        for (x in 1 until Board.WIDTH) {
            board.grid[Board.TOTAL_ROWS - 1][x] = BlockShape.I.id
        }
        assertEquals(0, board.findCompletedRows().size)

        board.grid[Board.TOTAL_ROWS - 1][0] = BlockShape.I.id
        val completed = board.findCompletedRows()
        assertEquals(1, completed.size)
        assertEquals(Board.TOTAL_ROWS - 1, completed[0])

        board.clearRows(completed)
        assertEquals(0, board.findCompletedRows().size)
        for (x in 0 until Board.WIDTH) {
            assertEquals("Bottom row should now be empty after cascade", 0, board.grid[Board.TOTAL_ROWS - 1][x])
        }
    }

    @Test
    fun testPivotSpinDetection() {
        val board = Board()
        val floorRow = Board.TOTAL_ROWS - 1
        val topRow = floorRow - 2
        board.grid[floorRow][4] = 1 // bl
        board.grid[floorRow][6] = 1 // br
        board.grid[topRow][6] = 1 // tr

        val crownPiece = ActivePiece(BlockShape.T, Point(4, topRow), RotationState.FLIP)
        val result = board.checkPivotSpin(crownPiece, lastActionWasRotate = true, lastKickIndex = 0)
        assertNotEquals("Should detect Pivot Spin", SpinBonus.NONE, result)
    }

    @Test
    fun testScoringAndBackToBack() {
        val scoreSystem = ScoreSystem()

        // First Quad Clear: 800 pts
        val reward1 = scoreSystem.onLinesCleared(4, SpinBonus.NONE)
        assertEquals(800, reward1.points)
        assertFalse("First quad clear is not B2B yet", reward1.isBackToBack)
        assertTrue("B2B eligible should now be active", scoreSystem.isBackToBackEligible)

        // Second consecutive Quad Clear: 800 * 1.5 = 1200 + combo pts (50 * 1 * 1) = 1250
        val reward2 = scoreSystem.onLinesCleared(4, SpinBonus.NONE)
        assertTrue("Second quad clear should have Back-to-Back bonus", reward2.isBackToBack)
        assertEquals(1200 + 50, reward2.points)

        // A single clear breaks B2B
        val reward3 = scoreSystem.onLinesCleared(1, SpinBonus.NONE)
        assertFalse(reward3.isBackToBack)
        assertFalse("Single clear broke B2B eligibility", scoreSystem.isBackToBackEligible)
    }

    @Test
    fun testHoldMechanism() {
        val engine = NeonStackEngine()
        engine.start()

        val initialPiece = engine.currentPiece!!.type
        assertTrue("Can hold initially", engine.canHold)

        val heldSuccess = engine.hold()
        assertTrue(heldSuccess)
        assertEquals("Hold slot should hold initial piece", initialPiece, engine.holdPiece)
        assertFalse("Cannot hold twice before locking a piece", engine.canHold)

        val secondHoldAttempt = engine.hold()
        assertFalse("Second hold attempt must fail", secondHoldAttempt)
    }

    @Test
    fun testHardDrop() {
        val engine = NeonStackEngine()
        engine.start()

        val distance = engine.hardDrop()
        assertTrue("Distance dropped must be positive", distance > 0)
        assertTrue("Score should increase by 2 * distance", engine.scoreSystem.score >= distance * 2)
    }

    @Test
    fun testCeilingLockOutRule() {
        val board = Board()
        val partialPiece = ActivePiece(BlockShape.I, Point(3, 0), RotationState.CLOCKWISE)
        assertFalse("Piece with blocks inside visible matrix must NOT trigger lock-out", board.isLockedOut(partialPiece))

        val allBufferPiece = ActivePiece(BlockShape.I, Point(3, -1), RotationState.SPAWN)
        assertTrue("Piece locked completely above ceiling must trigger lock-out", board.isLockedOut(allBufferPiece))
    }

    @Test
    fun testResetToMenuReturnsReadyState() {
        val engine = NeonStackEngine()
        assertEquals(GameStatus.READY, engine.status)

        engine.start()
        assertEquals(GameStatus.RUNNING, engine.status)
        assertNotNull(engine.currentPiece)
        engine.hardDrop()
        assertTrue(engine.scoreSystem.score > 0)

        engine.resetToMenu()
        assertEquals(GameStatus.READY, engine.status)
        assertNull(engine.currentPiece)
        assertNull(engine.holdPiece)
        assertEquals(0L, engine.scoreSystem.score)
    }
}
