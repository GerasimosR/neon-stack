package com.neonstack

import com.neonstack.engine.*
import org.junit.Assert.*
import org.junit.Test

class NeonStackEngineTest {

    @Test
    fun test12BagPolyominoDistributionAndGeometry() {
        val randomizer = BagRandomizer()
        val first12 = (1..12).map { randomizer.nextPiece() }.toSet()
        assertEquals("First 12 pieces must contain all 12 unique polyomino shapes", 12, first12.size)
        assertTrue("Roster must include 2-cell SPARK domino", first12.contains(BlockShape.SPARK))
        assertTrue("Roster must include 3-cell ELBOW", first12.contains(BlockShape.ELBOW))
        assertTrue("Roster must include 3-cell PULSE", first12.contains(BlockShape.PULSE))
        assertTrue("Roster must include 5-cell Ubongo THUMB (P)", first12.contains(BlockShape.THUMB))
        assertTrue("Roster must include 5-cell Ubongo ARCH (U)", first12.contains(BlockShape.ARCH))

        val next12 = (1..12).map { randomizer.nextPiece() }.toSet()
        assertEquals("Next 12 pieces must also contain all 12 unique polyomino shapes", 12, next12.size)

        // Verify exact cell counts across all 4 rotation states for new polyominoes
        for (state in RotationState.entries) {
            assertEquals("SPARK must have 2 unique cells", 2, BlockShape.SPARK.getMinos(state).toSet().size)
            assertEquals("ELBOW must have 3 unique cells", 3, BlockShape.ELBOW.getMinos(state).toSet().size)
            assertEquals("PULSE must have 3 unique cells", 3, BlockShape.PULSE.getMinos(state).toSet().size)
            assertEquals("THUMB must have 5 unique cells", 5, BlockShape.THUMB.getMinos(state).toSet().size)
            assertEquals("ARCH must have 5 unique cells", 5, BlockShape.ARCH.getMinos(state).toSet().size)
        }
    }

    @Test
    fun testBoardDimensionsAndBoundaryCollisions() {
        assertEquals("Playfield width is 9", 9, Board.WIDTH)
        assertEquals("Visible playfield height is 16", 16, Board.VISIBLE_ROWS)
        assertEquals("Total rows including spawn buffer is 18", 18, Board.TOTAL_ROWS)

        val board = Board()
        val piece = ActivePiece(BlockShape.CORE, Point(0, 8))

        assertTrue("Valid position on board", board.isValidPosition(piece))

        val leftOutOfBounds = ActivePiece(BlockShape.CORE, Point(-1, 8))
        assertFalse("Cannot place left of boundary", board.isValidPosition(leftOutOfBounds))

        val rightOutOfBounds = ActivePiece(BlockShape.CORE, Point(8, 8))
        assertFalse("CORE width is 2, so x=8 goes out of right bound 9", board.isValidPosition(rightOutOfBounds))

        val floorCollision = ActivePiece(BlockShape.CORE, Point(0, Board.TOTAL_ROWS - 1))
        assertFalse("Cannot place past bottom floor", board.isValidPosition(floorCollision))
    }

    @Test
    fun testWallKicks() {
        val board = Board()
        val piece = ActivePiece(BlockShape.CROWN, Point(0, 8), RotationState.SPAWN)
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
            board.grid[Board.TOTAL_ROWS - 1][x] = BlockShape.BEAM.id
        }
        assertEquals(0, board.findCompletedRows().size)

        board.grid[Board.TOTAL_ROWS - 1][0] = BlockShape.BEAM.id
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
    fun testOverclockSpinDetectionCrownAndElbow() {
        val board = Board()
        val floorRow = Board.TOTAL_ROWS - 1
        val topRow = floorRow - 2
        board.grid[floorRow][3] = 1 // bl
        board.grid[floorRow][5] = 1 // br
        board.grid[topRow][5] = 1 // tr

        val crownPiece = ActivePiece(BlockShape.CROWN, Point(3, topRow), RotationState.FLIP)
        val crownResult = board.checkOverclockSpin(crownPiece, lastActionWasRotate = true, lastKickIndex = 0)
        assertNotEquals("Should detect Overclock Spin on CROWN", SpinBonus.NONE, crownResult)

        // Test signature 3-cell ELBOW tucked under an overhang
        board.clear()
        board.grid[floorRow - 2][2] = 1 // Overhang directly above ELBOW at (2, floorRow - 1)
        val elbowPiece = ActivePiece(BlockShape.ELBOW, Point(2, floorRow - 1), RotationState.SPAWN)
        val elbowResult = board.checkOverclockSpin(elbowPiece, lastActionWasRotate = true, lastKickIndex = 1)
        assertEquals("Tucked ELBOW with kick should detect FULL Overclock Spin", SpinBonus.FULL, elbowResult)
    }

    @Test
    fun testScoringAndSurgeBonus() {
        val scoreSystem = ScoreSystem()

        // First Quad Clear: 1200 pts
        val reward1 = scoreSystem.onLinesCleared(4, SpinBonus.NONE)
        assertEquals(1200, reward1.points)
        assertFalse("First quad clear is not Surge yet", reward1.isBackToBack)
        assertTrue("Surge eligibility should now be active", scoreSystem.isBackToBackEligible)

        // Second consecutive Quad Clear: 1200 * 1.5 = 1800 + chain pts (75 * 1 * 1) = 1875
        val reward2 = scoreSystem.onLinesCleared(4, SpinBonus.NONE)
        assertTrue("Second quad clear should have Surge bonus", reward2.isBackToBack)
        assertEquals(1800 + 75, reward2.points)

        // A single clear breaks Surge
        val reward3 = scoreSystem.onLinesCleared(1, SpinBonus.NONE)
        assertFalse(reward3.isBackToBack)
        assertFalse("Single clear broke Surge eligibility", scoreSystem.isBackToBackEligible)
    }

    @Test
    fun testVaultMechanism() {
        val engine = NeonStackEngine()
        engine.start()

        val initialPiece = engine.currentPiece!!.type
        assertTrue("Can vault initially", engine.canHold)

        val vaulted = engine.hold()
        assertTrue(vaulted)
        assertEquals("Vault slot should hold initial piece", initialPiece, engine.holdPiece)
        assertFalse("Cannot vault twice before locking a piece", engine.canHold)

        val secondAttempt = engine.hold()
        assertFalse("Second vault attempt must fail", secondAttempt)
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
        val partialPiece = ActivePiece(BlockShape.BEAM, Point(2, 0), RotationState.CLOCKWISE)
        assertFalse("Piece with blocks inside visible matrix must NOT trigger lock-out", board.isLockedOut(partialPiece))

        val allBufferPiece = ActivePiece(BlockShape.BEAM, Point(2, -1), RotationState.SPAWN)
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
