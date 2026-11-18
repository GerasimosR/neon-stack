package com.neonstack.ui.mvi

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.neonstack.audio.AudioFocusHelper
import com.neonstack.audio.ChiptuneMusicEngine
import com.neonstack.audio.SoundEffect
import com.neonstack.data.LeaderboardRepository
import com.neonstack.data.PreferencesManager
import com.neonstack.engine.GameEvent
import com.neonstack.engine.GameStatus
import com.neonstack.engine.NeonStackEngine
import com.neonstack.engine.rogue.GameMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NeonStackViewModel(application: Application) : AndroidViewModel(application) {

    val engine = NeonStackEngine()
    private val prefsManager = PreferencesManager(application)
    private val leaderboardRepo = LeaderboardRepository(application)
    val chiptuneMusicEngine = ChiptuneMusicEngine().apply {
        isEnabled = prefsManager.isMusicEnabled
    }
    private val audioFocusHelper = AudioFocusHelper(
        context = application,
        onFocusLost = {
            chiptuneMusicEngine.pause()
        },
        onFocusGained = {
            if (engine.status == GameStatus.RUNNING && _uiState.value.countdownValue == 0 && prefsManager.isMusicEnabled) {
                chiptuneMusicEngine.resume()
            }
        }
    )

    private var countdownJob: Job? = null

    private val _uiState = MutableStateFlow(
        NeonStackUiState(
            highScore = prefsManager.highScore,
            classicBestScore = prefsManager.classicBestScore,
            rogueBestFloor = prefsManager.rogueBestFloor,
            rogueRuns = prefsManager.rogueRuns,
            gamesPlayed = prefsManager.gamesPlayed,
            isSoundEnabled = prefsManager.isSoundEnabled,
            isMusicEnabled = prefsManager.isMusicEnabled,
            isHapticsEnabled = prefsManager.isHapticsEnabled,
            isCrtFilterEnabled = prefsManager.isCrtFilterEnabled,
            controlMode = prefsManager.controlMode,
            dasDelayMs = prefsManager.dasDelayMs,
            arrRateMs = prefsManager.arrRateMs,
            softDropRateMs = prefsManager.softDropRateMs,
            leaderboardEntries = leaderboardRepo.getTopEntries()
        )
    )
    val uiState: StateFlow<NeonStackUiState> = _uiState.asStateFlow()

    private val _effectChannel = Channel<NeonStackUiEffect>(Channel.BUFFERED)
    val uiEffects: Flow<NeonStackUiEffect> = _effectChannel.receiveAsFlow()

    init {
        setupEngineEvents()
    }

    private fun setupEngineEvents() {
        engine.addEventListener { event ->
            when (event) {
                is GameEvent.PieceMoved -> {
                    val pan = if (event.dx < 0) -0.55f else if (event.dx > 0) 0.55f else 0f
                    sendEffect(NeonStackUiEffect.PlaySound(SoundEffect.MOVE, pan = pan))
                    sendEffect(NeonStackUiEffect.TriggerHaptic(HapticType.TICK))
                }
                is GameEvent.PieceRotated -> {
                    sendEffect(NeonStackUiEffect.PlaySound(SoundEffect.ROTATE))
                    sendEffect(NeonStackUiEffect.TriggerHaptic(HapticType.CLICK))
                }
                is GameEvent.PieceSoftDropped -> {
                    sendEffect(NeonStackUiEffect.PlaySound(SoundEffect.SOFT_DROP))
                    syncState()
                }
                is GameEvent.PieceHardDropped -> {
                    sendEffect(NeonStackUiEffect.PlaySound(SoundEffect.HARD_DROP))
                    sendEffect(NeonStackUiEffect.TriggerHaptic(HapticType.HEAVY_IMPACT))
                    sendEffect(NeonStackUiEffect.ScreenShake(14f))
                    syncState()
                }
                is GameEvent.PieceLocked -> {
                    syncState()
                }
                is GameEvent.HoldUsed -> {
                    sendEffect(NeonStackUiEffect.PlaySound(SoundEffect.HOLD))
                    sendEffect(NeonStackUiEffect.TriggerHaptic(HapticType.CLICK))
                    syncState()
                }
                is GameEvent.LinesCleared -> {
                    val reward = event.reward
                    val isSpin = reward.spin != com.neonstack.engine.SpinBonus.NONE
                    val combo = reward.comboCount
                    if (isSpin) {
                        sendEffect(NeonStackUiEffect.PlaySound(SoundEffect.SPIN_BONUS, combo = combo))
                        sendEffect(NeonStackUiEffect.TriggerHaptic(HapticType.QUAD_CLEAR))
                        sendEffect(NeonStackUiEffect.ScreenShake(14f))
                    } else {
                        when (event.rows.size) {
                            4 -> {
                                sendEffect(NeonStackUiEffect.PlaySound(SoundEffect.QUAD_CLEAR, combo = combo))
                                sendEffect(NeonStackUiEffect.TriggerHaptic(HapticType.QUAD_CLEAR))
                                sendEffect(NeonStackUiEffect.ScreenShake(18f))
                            }
                            3 -> {
                                sendEffect(NeonStackUiEffect.PlaySound(SoundEffect.LINE_CLEAR_3, combo = combo))
                                sendEffect(NeonStackUiEffect.TriggerHaptic(HapticType.LINE_CLEAR))
                                sendEffect(NeonStackUiEffect.ScreenShake(12f))
                            }
                            2 -> {
                                sendEffect(NeonStackUiEffect.PlaySound(SoundEffect.LINE_CLEAR_2, combo = combo))
                                sendEffect(NeonStackUiEffect.TriggerHaptic(HapticType.LINE_CLEAR))
                                sendEffect(NeonStackUiEffect.ScreenShake(8f))
                            }
                            else -> {
                                if (event.rows.isNotEmpty()) {
                                    sendEffect(NeonStackUiEffect.PlaySound(SoundEffect.LINE_CLEAR_1, combo = combo))
                                    sendEffect(NeonStackUiEffect.TriggerHaptic(HapticType.LINE_CLEAR))
                                    sendEffect(NeonStackUiEffect.ScreenShake(5f))
                                }
                            }
                        }
                    }

                    if (event.rows.isNotEmpty()) {
                        sendEffect(NeonStackUiEffect.LineClearAnimation(event.rows, reward.message))
                    }
                    sendEffect(NeonStackUiEffect.FloatingRewardBanner(reward))
                    syncState()
                }
                is GameEvent.LevelUp -> {
                    sendEffect(NeonStackUiEffect.PlaySound(SoundEffect.LEVEL_UP))
                    chiptuneMusicEngine.setLevel(event.newLevel)
                    syncState()
                }
                is GameEvent.FloorCleared -> {
                    chiptuneMusicEngine.setFilterMuffled(true)
                    sendEffect(NeonStackUiEffect.PlaySound(SoundEffect.LEVEL_UP))
                    prefsManager.updateRogueBestFloor(event.floor)
                    _uiState.update {
                        it.copy(
                            isDraftingRelic = true,
                            draftOptions = event.draftOptions,
                            currentFloor = event.floor,
                            rogueBestFloor = prefsManager.rogueBestFloor
                        )
                    }
                }
                is GameEvent.RelicAcquired -> {
                    sendEffect(NeonStackUiEffect.PlaySound(SoundEffect.QUAD_CLEAR))
                    syncState()
                }
                is GameEvent.GameOver -> {
                    chiptuneMusicEngine.pause()
                    chiptuneMusicEngine.setDangerMode(false)
                    chiptuneMusicEngine.setFilterMuffled(false)
                    sendEffect(NeonStackUiEffect.PlaySound(SoundEffect.GAME_OVER))
                    sendEffect(NeonStackUiEffect.TriggerHaptic(HapticType.HEAVY_IMPACT))

                    val isNewHigh = prefsManager.updateHighScore(event.finalScore)
                    if (engine.mode == GameMode.CLASSIC) {
                        prefsManager.updateClassicBestScore(event.finalScore)
                    } else {
                        prefsManager.updateRogueBestFloor(engine.rogueState.currentFloor)
                    }
                    val rank = if (event.finalScore > 0) {
                        leaderboardRepo.recordScore(event.finalScore, event.level, event.lines, "YOU")
                    } else null

                    _uiState.update {
                        it.copy(
                            status = GameStatus.GAME_OVER,
                            isGameOver = true,
                            isNewHighScore = isNewHigh,
                            highScore = prefsManager.highScore,
                            classicBestScore = prefsManager.classicBestScore,
                            rogueBestFloor = prefsManager.rogueBestFloor,
                            rogueRuns = prefsManager.rogueRuns,
                            latestRankAchieved = rank,
                            leaderboardEntries = leaderboardRepo.getTopEntries()
                        )
                    }
                }
                else -> Unit
            }
        }
    }

    private fun sendEffect(effect: NeonStackUiEffect) {
        viewModelScope.launch {
            _effectChannel.send(effect)
        }
    }

    fun handleIntent(intent: NeonStackIntent) {
        when (intent) {
            NeonStackIntent.MoveLeft -> if (_uiState.value.countdownValue == 0) engine.moveLeft()
            NeonStackIntent.MoveRight -> if (_uiState.value.countdownValue == 0) engine.moveRight()
            NeonStackIntent.SoftDrop -> if (_uiState.value.countdownValue == 0) engine.softDrop()
            NeonStackIntent.HardDrop -> if (_uiState.value.countdownValue == 0) engine.hardDrop()
            NeonStackIntent.RotateCw -> if (_uiState.value.countdownValue == 0) engine.rotateCw()
            NeonStackIntent.RotateCcw -> if (_uiState.value.countdownValue == 0) engine.rotateCcw()
            NeonStackIntent.Hold -> if (_uiState.value.countdownValue == 0) engine.hold()
            NeonStackIntent.Pause -> {
                if (engine.status != GameStatus.RUNNING) return
                countdownJob?.cancel()
                engine.pause()
                chiptuneMusicEngine.setFilterMuffled(true)
                _uiState.update { it.copy(isPaused = true, status = GameStatus.PAUSED, countdownValue = 0) }
            }
            NeonStackIntent.Resume -> {
                if (engine.status != GameStatus.PAUSED) return
                engine.resume()
                chiptuneMusicEngine.setFilterMuffled(false)
                if (prefsManager.isMusicEnabled) {
                    audioFocusHelper.requestAudioFocus()
                    chiptuneMusicEngine.play()
                }
                _uiState.update { it.copy(isPaused = false, status = GameStatus.RUNNING) }
            }
            NeonStackIntent.Restart -> startNewGame()
            is NeonStackIntent.StartGame -> {
                engine.mode = intent.mode
                startNewGame()
            }
            NeonStackIntent.ReturnToMenu -> returnToMenu()
            NeonStackIntent.OpenCalibration -> {
                _uiState.update { it.copy(isCalibrationOpen = true) }
            }
            NeonStackIntent.CloseCalibration -> {
                _uiState.update { it.copy(isCalibrationOpen = false) }
            }
            is NeonStackIntent.SaveCalibration -> {
                prefsManager.dasDelayMs = intent.das
                prefsManager.arrRateMs = intent.arr
                prefsManager.softDropRateMs = intent.softDrop
                _uiState.update {
                    it.copy(
                        dasDelayMs = intent.das,
                        arrRateMs = intent.arr,
                        softDropRateMs = intent.softDrop,
                        isCalibrationOpen = false
                    )
                }
            }
            NeonStackIntent.ResetCalibration -> {
                prefsManager.resetCalibration()
                _uiState.update {
                    it.copy(
                        dasDelayMs = 150L,
                        arrRateMs = 38L,
                        softDropRateMs = 40L
                    )
                }
            }
            NeonStackIntent.OpenLeaderboard -> {
                _uiState.update {
                    it.copy(
                        isLeaderboardOpen = true,
                        leaderboardEntries = leaderboardRepo.getTopEntries()
                    )
                }
            }
            NeonStackIntent.CloseLeaderboard -> {
                _uiState.update { it.copy(isLeaderboardOpen = false) }
            }
            is NeonStackIntent.RecordHighScorer -> {
                if (uiState.value.score > 0) {
                    leaderboardRepo.recordScore(uiState.value.score, uiState.value.level, uiState.value.lines, intent.name)
                    _uiState.update { it.copy(leaderboardEntries = leaderboardRepo.getTopEntries()) }
                }
            }
            NeonStackIntent.ResetLeaderboard -> {
                leaderboardRepo.clearLeaderboard()
                _uiState.update { it.copy(leaderboardEntries = leaderboardRepo.getTopEntries()) }
            }
            is NeonStackIntent.SwitchGameMode -> {
                engine.mode = intent.mode
                startNewGame()
            }
            is NeonStackIntent.SelectRelic -> {
                chiptuneMusicEngine.setFilterMuffled(false)
                _uiState.update { it.copy(isDraftingRelic = false) }
                engine.applyRelicDraft(intent.relic)
                syncState()
            }
            is NeonStackIntent.SetSoundEnabled -> {
                prefsManager.isSoundEnabled = intent.enabled
                _uiState.update { it.copy(isSoundEnabled = intent.enabled) }
            }
            is NeonStackIntent.SetMusicEnabled -> {
                prefsManager.isMusicEnabled = intent.enabled
                chiptuneMusicEngine.isEnabled = intent.enabled
                if (intent.enabled && engine.status == GameStatus.RUNNING && _uiState.value.countdownValue == 0) {
                    audioFocusHelper.requestAudioFocus()
                    chiptuneMusicEngine.play()
                }
                _uiState.update { it.copy(isMusicEnabled = intent.enabled) }
            }
            is NeonStackIntent.SetHapticsEnabled -> {
                prefsManager.isHapticsEnabled = intent.enabled
                _uiState.update { it.copy(isHapticsEnabled = intent.enabled) }
            }
            is NeonStackIntent.SetCrtFilterEnabled -> {
                prefsManager.isCrtFilterEnabled = intent.enabled
                _uiState.update { it.copy(isCrtFilterEnabled = intent.enabled) }
            }
            is NeonStackIntent.SetControlMode -> {
                prefsManager.controlMode = intent.mode
                _uiState.update { it.copy(controlMode = intent.mode) }
            }
        }
    }

    fun onGameTick(dtMs: Long) {
        if (engine.status == GameStatus.RUNNING && _uiState.value.countdownValue == 0) {
            engine.update(dtMs)
        }
    }

    fun startNewGame() {
        countdownJob?.cancel()
        prefsManager.incrementGamesPlayed()
        if (engine.mode == GameMode.ROGUE) {
            prefsManager.incrementRogueRuns()
        }
        engine.start()
        chiptuneMusicEngine.pause()
        chiptuneMusicEngine.setLevel(1)
        chiptuneMusicEngine.setFilterMuffled(false)
        chiptuneMusicEngine.setDangerMode(false)

        _uiState.update {
            NeonStackUiState(
                status = GameStatus.RUNNING,
                score = 0,
                highScore = prefsManager.highScore,
                classicBestScore = prefsManager.classicBestScore,
                rogueBestFloor = prefsManager.rogueBestFloor,
                rogueRuns = prefsManager.rogueRuns,
                gamesPlayed = prefsManager.gamesPlayed,
                level = 1,
                lines = 0,
                holdPiece = null,
                canHold = true,
                nextQueue = engine.randomizer.peek(3),
                isSoundEnabled = prefsManager.isSoundEnabled,
                isMusicEnabled = prefsManager.isMusicEnabled,
                isHapticsEnabled = prefsManager.isHapticsEnabled,
                isCrtFilterEnabled = prefsManager.isCrtFilterEnabled,
                controlMode = prefsManager.controlMode,
                dasDelayMs = prefsManager.dasDelayMs,
                arrRateMs = prefsManager.arrRateMs,
                softDropRateMs = prefsManager.softDropRateMs,
                leaderboardEntries = leaderboardRepo.getTopEntries(),
                gameMode = engine.mode,
                currentFloor = engine.rogueState.currentFloor,
                linesRemainingOnFloor = engine.rogueState.linesRemainingOnFloor,
                targetLinesForFloor = engine.rogueState.targetLinesForFloor,
                isBossFloor = engine.rogueState.isBossFloor,
                activeRelics = engine.rogueState.activeRelics.toList(),
                isDraftingRelic = false,
                draftOptions = emptyList(),
                isGameOver = false,
                isPaused = false,
                isNewHighScore = false,
                countdownValue = 3
            )
        }

        countdownJob = viewModelScope.launch {
            for (beat in 3 downTo 1) {
                _uiState.update { it.copy(countdownValue = beat) }
                sendEffect(NeonStackUiEffect.PlaySound(SoundEffect.MOVE))
                sendEffect(NeonStackUiEffect.TriggerHaptic(HapticType.TICK))
                delay(1000L)
            }
            _uiState.update { it.copy(countdownValue = 0) }
            sendEffect(NeonStackUiEffect.PlaySound(SoundEffect.ROTATE))
            sendEffect(NeonStackUiEffect.TriggerHaptic(HapticType.CLICK))
            if (engine.status == GameStatus.RUNNING && prefsManager.isMusicEnabled) {
                audioFocusHelper.requestAudioFocus()
                chiptuneMusicEngine.play()
            }
        }
    }

    fun returnToMenu() {
        countdownJob?.cancel()
        engine.resetToMenu()
        chiptuneMusicEngine.pause()
        chiptuneMusicEngine.setFilterMuffled(false)
        chiptuneMusicEngine.setDangerMode(false)
        audioFocusHelper.abandonAudioFocus()

        _uiState.update {
            NeonStackUiState(
                status = GameStatus.READY,
                highScore = prefsManager.highScore,
                classicBestScore = prefsManager.classicBestScore,
                rogueBestFloor = prefsManager.rogueBestFloor,
                rogueRuns = prefsManager.rogueRuns,
                gamesPlayed = prefsManager.gamesPlayed,
                isSoundEnabled = prefsManager.isSoundEnabled,
                isMusicEnabled = prefsManager.isMusicEnabled,
                isHapticsEnabled = prefsManager.isHapticsEnabled,
                isCrtFilterEnabled = prefsManager.isCrtFilterEnabled,
                controlMode = prefsManager.controlMode,
                dasDelayMs = prefsManager.dasDelayMs,
                arrRateMs = prefsManager.arrRateMs,
                softDropRateMs = prefsManager.softDropRateMs,
                leaderboardEntries = leaderboardRepo.getTopEntries(),
                gameMode = engine.mode
            )
        }
    }

    private fun syncState() {
        if (engine.status == GameStatus.RUNNING) {
            chiptuneMusicEngine.setDangerMode(engine.board.isStackInDanger())
        }
        _uiState.update {
            it.copy(
                status = engine.status,
                score = engine.scoreSystem.score,
                level = engine.scoreSystem.level,
                lines = engine.scoreSystem.linesCleared,
                holdPiece = engine.holdPiece,
                canHold = engine.canHold,
                nextQueue = engine.randomizer.peek(3),
                quadsCount = engine.scoreSystem.quadsCount,
                spinsCount = engine.scoreSystem.spinsCount,
                gameMode = engine.mode,
                currentFloor = engine.rogueState.currentFloor,
                linesRemainingOnFloor = engine.rogueState.linesRemainingOnFloor,
                targetLinesForFloor = engine.rogueState.targetLinesForFloor,
                isBossFloor = engine.rogueState.isBossFloor,
                activeRelics = engine.rogueState.activeRelics.toList()
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioFocusHelper.abandonAudioFocus()
        chiptuneMusicEngine.release()
    }
}
