package com.neonstack.ui.compose

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.neonstack.R
import com.neonstack.audio.SoundSynthesizer
import com.neonstack.data.ControlMode
import com.neonstack.engine.GameStatus
import com.neonstack.engine.rogue.GameMode
import com.neonstack.haptics.HapticFeedbackManager
import com.neonstack.ui.NeonStackCanvasView
import com.neonstack.ui.VsyncGameLoop
import com.neonstack.ui.compose.theme.*
import com.neonstack.ui.mvi.*

@Composable
fun NeonStackScreen(
    viewModel: NeonStackViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val soundSynthesizer = remember {
        SoundSynthesizer().apply { isEnabled = uiState.isSoundEnabled }
    }
    val hapticManager = remember {
        HapticFeedbackManager(context).apply { isEnabled = uiState.isHapticsEnabled }
    }

    var canvasViewRef by remember { mutableStateOf<NeonStackCanvasView?>(null) }

    // Synchronize audio/haptic toggles
    LaunchedEffect(uiState.isSoundEnabled) {
        soundSynthesizer.isEnabled = uiState.isSoundEnabled
    }
    LaunchedEffect(uiState.isHapticsEnabled) {
        hapticManager.isEnabled = uiState.isHapticsEnabled
    }

    // Process One-off MVI UI Effects (Sound, Haptics, Screen Shake, Floating Banners)
    LaunchedEffect(Unit) {
        viewModel.uiEffects.collect { effect ->
            when (effect) {
                is NeonStackUiEffect.PlaySound -> soundSynthesizer.play(effect.sound, effect.combo, effect.pan)
                is NeonStackUiEffect.TriggerHaptic -> {
                    when (effect.type) {
                        HapticType.TICK -> hapticManager.tick()
                        HapticType.CLICK -> hapticManager.click()
                        HapticType.HEAVY_IMPACT -> hapticManager.heavyImpact()
                        HapticType.LINE_CLEAR -> hapticManager.lineClear()
                        HapticType.QUAD_CLEAR -> hapticManager.quadClear()
                    }
                }
                is NeonStackUiEffect.ScreenShake -> canvasViewRef?.triggerScreenShake(effect.magnitude)
                is NeonStackUiEffect.LineClearAnimation -> canvasViewRef?.triggerLineClearAnimation(effect.rows)
                is NeonStackUiEffect.FloatingRewardBanner -> {
                    canvasViewRef?.showFloatingBanner(formatLocalizedRewardBanner(context, effect.reward))
                }
            }
        }
    }

    // Lifecycle-Aware Hardware VSYNC Choreographer Game Loop (60Hz / 90Hz / 120Hz LTPO)
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val vsyncLoop = VsyncGameLoop { dtMs ->
            viewModel.onGameTick(dtMs)
            canvasViewRef?.invalidate()
        }

        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            when (event) {
                androidx.lifecycle.Lifecycle.Event.ON_START, androidx.lifecycle.Lifecycle.Event.ON_RESUME -> {
                    vsyncLoop.start()
                }
                androidx.lifecycle.Lifecycle.Event.ON_PAUSE -> {
                    viewModel.handleIntent(NeonStackIntent.Pause)
                }
                androidx.lifecycle.Lifecycle.Event.ON_STOP -> {
                    vsyncLoop.stop()
                }
                androidx.lifecycle.Lifecycle.Event.ON_DESTROY -> {
                    vsyncLoop.stop()
                    soundSynthesizer.release()
                }
                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            vsyncLoop.stop()
            soundSynthesizer.release()
        }
    }

    // Handle Android Back button cleanly (close dialogs, pause active run, or resume from pause)
    val shouldInterceptBack = uiState.isLeaderboardOpen ||
        uiState.isCalibrationOpen ||
        uiState.status == GameStatus.RUNNING ||
        uiState.isPaused

    BackHandler(enabled = shouldInterceptBack) {
        when {
            uiState.isLeaderboardOpen -> viewModel.handleIntent(NeonStackIntent.CloseLeaderboard)
            uiState.isCalibrationOpen -> viewModel.handleIntent(NeonStackIntent.CloseCalibration)
            uiState.isPaused -> viewModel.handleIntent(NeonStackIntent.Resume)
            uiState.status == GameStatus.RUNNING -> viewModel.handleIntent(NeonStackIntent.Pause)
        }
    }

    NeonStackTheme {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(BgMain)
        ) {
            AnimatedContent(
                targetState = uiState.status == GameStatus.READY,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "welcome_to_game_transition"
            ) { isWelcome ->
                if (isWelcome) {
                    WelcomeScreen(
                        state = uiState,
                        onStartClassic = { viewModel.handleIntent(NeonStackIntent.StartGame(GameMode.CLASSIC)) },
                        onStartRogue = { viewModel.handleIntent(NeonStackIntent.StartGame(GameMode.ROGUE)) },
                        onOpenLeaderboard = { viewModel.handleIntent(NeonStackIntent.OpenLeaderboard) },
                        onOpenCalibration = { viewModel.handleIntent(NeonStackIntent.OpenCalibration) },
                        onToggleSound = { viewModel.handleIntent(NeonStackIntent.SetSoundEnabled(it)) },
                        onToggleMusic = { viewModel.handleIntent(NeonStackIntent.SetMusicEnabled(it)) },
                        onToggleHaptics = { viewModel.handleIntent(NeonStackIntent.SetHapticsEnabled(it)) },
                        onToggleCrtFilter = { viewModel.handleIntent(NeonStackIntent.SetCrtFilterEnabled(it)) },
                        onSetControlMode = { viewModel.handleIntent(NeonStackIntent.SetControlMode(it)) }
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .systemBarsPadding()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        // Top Header (Title, Mode Badge, Trophy, Pause)
                        TopHeaderBar(
                            state = uiState,
                            onPauseClick = { viewModel.handleIntent(NeonStackIntent.Pause) },
                            onLeaderboardClick = { viewModel.handleIntent(NeonStackIntent.OpenLeaderboard) }
                        )

                        // Stats Row (Score, Level, Lines or Floor/Quota)
                        ScoreStatsRow(
                            state = uiState,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        // Active Relics (Rogue Spire Mode)
                        ActiveRelicsRow(
                            relics = uiState.activeRelics,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )

                        // Main Playfield Canvas (Embedded with Hardware Acceleration)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            AndroidView(
                                factory = { ctx ->
                                    NeonStackCanvasView(ctx).apply {
                                        engine = viewModel.engine
                                        gestureListener = object : NeonStackCanvasView.GameGestureListener {
                                            override fun onSwipeLeft() {
                                                viewModel.handleIntent(NeonStackIntent.MoveLeft)
                                            }
                                            override fun onSwipeRight() {
                                                viewModel.handleIntent(NeonStackIntent.MoveRight)
                                            }
                                            override fun onSwipeDown() {
                                                viewModel.handleIntent(NeonStackIntent.SoftDrop)
                                            }
                                            override fun onHardDrop() {
                                                viewModel.handleIntent(NeonStackIntent.HardDrop)
                                            }
                                            override fun onTapRotateCw() {
                                                viewModel.handleIntent(NeonStackIntent.RotateCw)
                                            }
                                            override fun onDoubleTapRotateCcw() {
                                                viewModel.handleIntent(NeonStackIntent.RotateCcw)
                                            }
                                            override fun onHold() {
                                                viewModel.handleIntent(NeonStackIntent.Hold)
                                            }
                                        }
                                    }.also { canvasViewRef = it }
                                },
                                update = { view ->
                                    view.engine = viewModel.engine
                                    view.isCrtFilterEnabled = uiState.isCrtFilterEnabled
                                    view.isFullScreenGestureMode = (uiState.controlMode == ControlMode.GESTURES)
                                    view.invalidate()
                                },
                                modifier = Modifier.fillMaxSize()
                            )

                            // Crisp 3-2-1 Ready Countdown Overlay before gravity kicks in
                            if (uiState.countdownValue > 0) {
                                CountdownOverlay(count = uiState.countdownValue, mode = uiState.gameMode)
                            }
                        }

                        // Bottom Controls: Modern Dual-Thumb Neo-Arcade Pad OR Whisper-Thin Gesture Hint
                        if (uiState.controlMode == ControlMode.ARCADE_PAD) {
                            ArcadeGamepad(
                                onMoveLeft = { viewModel.handleIntent(NeonStackIntent.MoveLeft) },
                                onMoveRight = { viewModel.handleIntent(NeonStackIntent.MoveRight) },
                                onSoftDrop = { viewModel.handleIntent(NeonStackIntent.SoftDrop) },
                                onHardDrop = { viewModel.handleIntent(NeonStackIntent.HardDrop) },
                                onRotateCw = { viewModel.handleIntent(NeonStackIntent.RotateCw) },
                                onRotateCcw = { viewModel.handleIntent(NeonStackIntent.RotateCcw) },
                                onHold = { viewModel.handleIntent(NeonStackIntent.Hold) },
                                dasDelayMs = uiState.dasDelayMs,
                                arrRateMs = uiState.arrRateMs,
                                softDropRateMs = uiState.softDropRateMs
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 2.dp, bottom = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.gesture_hud_hint),
                                    color = TextSecondary.copy(alpha = 0.55f),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // Pause Dialog
            if (uiState.isPaused) {
                PauseDialog(
                    state = uiState,
                    onResume = { viewModel.handleIntent(NeonStackIntent.Resume) },
                    onRestart = { viewModel.handleIntent(NeonStackIntent.Restart) },
                    onMainMenu = { viewModel.handleIntent(NeonStackIntent.ReturnToMenu) },
                    onToggleSound = { viewModel.handleIntent(NeonStackIntent.SetSoundEnabled(it)) },
                    onToggleMusic = { viewModel.handleIntent(NeonStackIntent.SetMusicEnabled(it)) },
                    onToggleHaptics = { viewModel.handleIntent(NeonStackIntent.SetHapticsEnabled(it)) },
                    onToggleCrtFilter = { viewModel.handleIntent(NeonStackIntent.SetCrtFilterEnabled(it)) },
                    onSetControlMode = { viewModel.handleIntent(NeonStackIntent.SetControlMode(it)) },
                    onOpenCalibration = { viewModel.handleIntent(NeonStackIntent.OpenCalibration) },
                    onOpenLeaderboard = { viewModel.handleIntent(NeonStackIntent.OpenLeaderboard) }
                )
            }

            // Leaderboard Dialog
            if (uiState.isLeaderboardOpen) {
                LeaderboardDialog(
                    state = uiState,
                    onSaveName = { name ->
                        viewModel.handleIntent(NeonStackIntent.RecordHighScorer(name))
                    },
                    onResetLeaderboard = {
                        viewModel.handleIntent(NeonStackIntent.ResetLeaderboard)
                    },
                    onDismiss = {
                        viewModel.handleIntent(NeonStackIntent.CloseLeaderboard)
                    }
                )
            }

            // Calibration Studio Dialog
            if (uiState.isCalibrationOpen) {
                CalibrationDialog(
                    state = uiState,
                    onSave = { das, arr, softDrop ->
                        viewModel.handleIntent(NeonStackIntent.SaveCalibration(das, arr, softDrop))
                    },
                    onReset = {
                        viewModel.handleIntent(NeonStackIntent.ResetCalibration)
                    },
                    onDismiss = {
                        viewModel.handleIntent(NeonStackIntent.CloseCalibration)
                    }
                )
            }

            // Relic Draft Dialog (Rogue Spire Mode)
            if (uiState.isDraftingRelic) {
                RelicDraftDialog(
                    currentFloor = uiState.currentFloor,
                    options = uiState.draftOptions,
                    onSelectRelic = { relic ->
                        viewModel.handleIntent(NeonStackIntent.SelectRelic(relic))
                    }
                )
            }

            // Game Over Dialog
            if (uiState.isGameOver) {
                GameOverDialog(
                    state = uiState,
                    onPlayAgain = { viewModel.handleIntent(NeonStackIntent.Restart) },
                    onViewLeaderboard = { viewModel.handleIntent(NeonStackIntent.OpenLeaderboard) },
                    onMainMenu = { viewModel.handleIntent(NeonStackIntent.ReturnToMenu) }
                )
            }
        }
    }
}

@Composable
private fun CountdownOverlay(
    count: Int,
    mode: GameMode
) {
    val accent = if (mode == GameMode.ROGUE) AccentPink else AccentCyan
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(BgCard.copy(alpha = 0.88f))
            .border(2.dp, accent, RoundedCornerShape(24.dp))
            .padding(horizontal = 36.dp, vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.get_ready),
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            AnimatedContent(
                targetState = count,
                transitionSpec = {
                    (fadeIn() + scaleIn(initialScale = 1.4f)) togetherWith
                        (fadeOut() + scaleOut(targetScale = 0.7f))
                },
                label = "countdown_number"
            ) { value ->
                Text(
                    text = value.toString(),
                    color = accent,
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}
