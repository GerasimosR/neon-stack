package com.seniordev.neonstack.engine

import com.seniordev.neonstack.engine.rogue.GameMode
import com.seniordev.neonstack.engine.rogue.Relic
import com.seniordev.neonstack.engine.rogue.RelicId
import com.seniordev.neonstack.engine.rogue.RogueRunState

enum class GameStatus {
    READY,
    RUNNING,
    PAUSED,
    GAME_OVER
}

class NeonStackEngine(
    val board: Board = Board(),
    val randomizer: BagRandomizer = BagRandomizer(),
    val scoreSystem: ScoreSystem = ScoreSystem()
) {
    companion object {
        const val LOCK_DELAY_MS = 500L
        const val MAX_LOCK_RESETS = 15
    }

    var mode: GameMode = GameMode.CLASSIC
    val rogueState: RogueRunState = RogueRunState()

    var status: GameStatus = GameStatus.READY
        private set

    var currentPiece: ActivePiece? = null
        private set

    var holdPiece: BlockShape? = null
        private set

    var holdsRemaining: Int = 1
        private set

    val canHold: Boolean
        get() = holdsRemaining > 0

    // Lock delay tracking
    private var isGrounded: Boolean = false
    private var lockDelayAccumulator: Long = 0L
    private var lockResetCount: Int = 0
    private var lowestYReached: Int = 0

    // Last action tracking for Pivot Spin
    private var lastActionWasRotate: Boolean = false
    private var lastKickIndex: Int = 0

    // Gravity timing
    private var gravityAccumulator: Long = 0L

    private val eventListeners = mutableListOf<(GameEvent) -> Unit>()

    fun addEventListener(listener: (GameEvent) -> Unit) {
        eventListeners.add(listener)
    }

    fun removeEventListener(listener: (GameEvent) -> Unit) {
        eventListeners.remove(listener)
    }

    private fun emitEvent(event: GameEvent) {
        for (i in eventListeners.indices) {
            eventListeners[i].invoke(event)
        }
    }

    fun start() {
        board.clear()
        randomizer.reset()
        scoreSystem.reset()
        holdPiece = null
        if (mode == GameMode.ROGUE) {
            rogueState.reset()
            randomizer.extraIConduit = false
        }
        resetHolds()
        status = GameStatus.RUNNING
        spawnNext()
        emitEvent(GameEvent.GameRestarted)
    }

    fun resetToMenu() {
        board.clear()
        randomizer.reset()
        scoreSystem.reset()
        rogueState.reset()
        randomizer.extraIConduit = false
        currentPiece = null
        holdPiece = null
        holdsRemaining = 1
        isGrounded = false
        lockDelayAccumulator = 0L
        lockResetCount = 0
        gravityAccumulator = 0L
        status = GameStatus.READY
    }

    private fun resetHolds() {
        val maxHolds = if (mode == GameMode.ROGUE && rogueState.hasRelic(RelicId.DOUBLE_HOLD)) 2 else 1
        holdsRemaining = maxHolds
    }

    fun pause() {
        if (status == GameStatus.RUNNING) {
            status = GameStatus.PAUSED
            emitEvent(GameEvent.GamePaused)
        }
    }

    fun resume() {
        if (status == GameStatus.PAUSED) {
            status = GameStatus.RUNNING
            emitEvent(GameEvent.GameResumed)
        }
    }

    fun togglePause() {
        if (status == GameStatus.RUNNING) pause()
        else if (status == GameStatus.PAUSED) resume()
    }

    /**
     * Fixed or variable delta update loop in milliseconds.
     */
    fun update(dtMs: Long) {
        if (status != GameStatus.RUNNING) return
        val piece = currentPiece ?: return

        val effectiveLockDelay = if (mode == GameMode.ROGUE && rogueState.hasRelic(RelicId.CHRONO_STABILIZER)) {
            850L
        } else {
            LOCK_DELAY_MS
        }

        // Check if piece is touching surface
        isGrounded = !board.isValidPosition(piece, Point(piece.position.x, piece.position.y + 1), piece.rotation)

        if (isGrounded) {
            lockDelayAccumulator += dtMs
            if (lockDelayAccumulator >= effectiveLockDelay) {
                lockCurrentPiece()
                return
            }
        } else {
            lockDelayAccumulator = 0L
        }

        // Apply natural gravity
        gravityAccumulator += dtMs
        val dropInterval = scoreSystem.getDropIntervalMs()
        while (gravityAccumulator >= dropInterval) {
            gravityAccumulator -= dropInterval
            if (status != GameStatus.RUNNING) break
            stepDown(isManual = false)
        }
    }

    fun moveLeft(): Boolean {
        if (status != GameStatus.RUNNING) return false
        val piece = currentPiece ?: return false
        val newPos = Point(piece.position.x - 1, piece.position.y)
        if (board.isValidPosition(piece, newPos, piece.rotation)) {
            piece.position = newPos
            lastActionWasRotate = false
            onMoveOrRotate()
            emitEvent(GameEvent.PieceMoved(-1))
            return true
        }
        return false
    }

    fun moveRight(): Boolean {
        if (status != GameStatus.RUNNING) return false
        val piece = currentPiece ?: return false
        val newPos = Point(piece.position.x + 1, piece.position.y)
        if (board.isValidPosition(piece, newPos, piece.rotation)) {
            piece.position = newPos
            lastActionWasRotate = false
            onMoveOrRotate()
            emitEvent(GameEvent.PieceMoved(1))
            return true
        }
        return false
    }

    fun rotateCw(): Boolean {
        return rotate(isCw = true)
    }

    fun rotateCcw(): Boolean {
        return rotate(isCw = false)
    }

    private fun rotate(isCw: Boolean): Boolean {
        if (status != GameStatus.RUNNING) return false
        val piece = currentPiece ?: return false

        val targetState = if (isCw) piece.rotation.rotateCw() else piece.rotation.rotateCcw()
        val kicks = WallKickTable.getKicks(piece.type, piece.rotation, targetState)

        for (index in kicks.indices) {
            val kick = kicks[index]
            val testPos = piece.position + kick
            if (board.isValidPosition(piece, testPos, targetState)) {
                piece.position = testPos
                piece.rotation = targetState
                lastActionWasRotate = true
                lastKickIndex = index
                onMoveOrRotate()
                emitEvent(GameEvent.PieceRotated(piece.type, index))
                return true
            }
        }
        return false
    }

    fun softDrop(): Boolean {
        if (status != GameStatus.RUNNING) return false
        val stepped = stepDown(isManual = true)
        if (stepped) {
            val pts = if (mode == GameMode.ROGUE && rogueState.hasRelic(RelicId.GRAVITY_ANCHOR)) 3 else 1
            scoreSystem.addSoftDropPoints(pts)
            emitEvent(GameEvent.PieceSoftDropped)
        }
        return stepped
    }

    fun hardDrop(): Int {
        if (status != GameStatus.RUNNING) return 0
        val piece = currentPiece ?: return 0
        var targetPos = board.getDropPosition(piece)

        // Relic: Overhang Drill (punch through 1 block directly beneath)
        if (mode == GameMode.ROGUE && rogueState.hasRelic(RelicId.OVERHANG_DRILL)) {
            val beneathY = targetPos.y + 1
            if (beneathY in 0 until Board.TOTAL_ROWS) {
                for (mino in piece.getWorldMinos(targetPos)) {
                    val checkY = mino.y + 1
                    if (checkY in 0 until Board.TOTAL_ROWS && board.grid[checkY][mino.x] != 0) {
                        board.grid[checkY][mino.x] = 0 // Drill through obstruction!
                    }
                }
                targetPos = board.getDropPosition(piece)
            }
        }

        val distance = targetPos.y - piece.position.y
        piece.position = targetPos
        scoreSystem.addHardDropPoints(distance)
        emitEvent(GameEvent.PieceHardDropped(distance))

        lockCurrentPiece()
        return distance
    }

    fun hold(): Boolean {
        if (status != GameStatus.RUNNING || !canHold) return false
        val current = currentPiece ?: return false

        val pieceToHold = current.type
        val held = holdPiece

        if (held == null) {
            holdPiece = pieceToHold
            spawnNext()
        } else {
            holdPiece = pieceToHold
            spawnPiece(held)
        }

        holdsRemaining--
        emitEvent(GameEvent.HoldUsed(pieceToHold))
        return true
    }

    fun getGhostPiecePosition(): Point? {
        val piece = currentPiece ?: return null
        return board.getDropPosition(piece)
    }

    private fun stepDown(isManual: Boolean): Boolean {
        val piece = currentPiece ?: return false
        val newPos = Point(piece.position.x, piece.position.y + 1)
        if (board.isValidPosition(piece, newPos, piece.rotation)) {
            piece.position = newPos
            lastActionWasRotate = false
            if (piece.position.y > lowestYReached) {
                lowestYReached = piece.position.y
                lockResetCount = 0
                lockDelayAccumulator = 0L
            }
            return true
        }
        return false
    }

    private fun onMoveOrRotate() {
        val piece = currentPiece ?: return
        isGrounded = !board.isValidPosition(piece, Point(piece.position.x, piece.position.y + 1), piece.rotation)

        val maxResets = if (mode == GameMode.ROGUE && rogueState.hasRelic(RelicId.CHRONO_STABILIZER)) {
            25
        } else {
            MAX_LOCK_RESETS
        }

        if (isGrounded) {
            if (piece.position.y > lowestYReached) {
                lowestYReached = piece.position.y
                lockResetCount = 0
                lockDelayAccumulator = 0L
            } else if (lockResetCount < maxResets) {
                lockResetCount++
                lockDelayAccumulator = 0L
            }
        }
    }

    private fun lockCurrentPiece() {
        val piece = currentPiece ?: return
        board.lock(piece)
        emitEvent(GameEvent.PieceLocked(piece.type))

        // Check Overclock Spin
        val spin = board.checkOverclockSpin(piece, lastActionWasRotate, lastKickIndex)

        // Check line clears
        val completedRows = board.findCompletedRows()
        if (completedRows.isNotEmpty()) {
            board.clearRows(completedRows)
            val prevLevel = scoreSystem.level
            var reward = scoreSystem.onLinesCleared(completedRows.size, spin)

            // Relic: Quad Surge (3x score & vaporize bottom row on 4 lines)
            if (mode == GameMode.ROGUE && rogueState.hasRelic(RelicId.QUAD_SURGE) && completedRows.size == 4) {
                scoreSystem.addHardDropPoints(reward.points * 2)
                board.clearRows(listOf(Board.TOTAL_ROWS - 1))
                reward = reward.copy(message = "QUAD SURGE 3X!")
            }

            emitEvent(GameEvent.LinesCleared(reward, completedRows))
            if (scoreSystem.level > prevLevel) {
                emitEvent(GameEvent.LevelUp(scoreSystem.level))
            }

            // Rogue Spire Floor Check
            if (mode == GameMode.ROGUE) {
                val floorComplete = rogueState.addLines(completedRows.size)
                if (floorComplete) {
                    if (rogueState.hasRelic(RelicId.LASER_SWEEP)) {
                        board.clearRows(listOf(Board.TOTAL_ROWS - 1))
                    }
                    status = GameStatus.PAUSED
                    val draftOptions = rogueState.rollDraftOptions(3)
                    emitEvent(GameEvent.FloorCleared(rogueState.currentFloor, draftOptions))
                    return
                }
            }
        } else {
            val reward = scoreSystem.onPieceLockedWithoutClear(spin)
            if (reward != null) {
                emitEvent(GameEvent.LinesCleared(reward, emptyList()))
            }
        }

        // Check Lock Out: triggers game over only if all 4 blocks locked above the visible ceiling
        if (board.isLockedOut(piece)) {
            triggerGameOver()
            return
        }

        resetHolds()
        spawnNext()
    }

    fun applyRelicDraft(relic: Relic) {
        rogueState.addRelic(relic)
        if (relic.id == RelicId.GOLDEN_I_BAG) {
            randomizer.extraIConduit = true
        }
        rogueState.advanceToNextFloor()
        emitEvent(GameEvent.RelicAcquired(relic))
        resetHolds()
        resume()
        spawnNext()
    }

    private fun spawnNext() {
        val nextType = randomizer.nextPiece()
        spawnPiece(nextType)
    }

    private fun spawnPiece(type: BlockShape) {
        val newPiece = ActivePiece.spawn(type)
        if (!board.isValidPosition(newPiece)) {
            // Block-out game over!
            currentPiece = newPiece
            triggerGameOver()
            return
        }

        currentPiece = newPiece
        isGrounded = false
        lockDelayAccumulator = 0L
        lockResetCount = 0
        lowestYReached = newPiece.position.y
        lastActionWasRotate = false
        lastKickIndex = 0
        gravityAccumulator = 0L
    }

    private fun triggerGameOver() {
        status = GameStatus.GAME_OVER
        emitEvent(GameEvent.GameOver(scoreSystem.score, scoreSystem.linesCleared, scoreSystem.level))
    }
}
