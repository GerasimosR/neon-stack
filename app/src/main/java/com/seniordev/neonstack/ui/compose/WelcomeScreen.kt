package com.seniordev.neonstack.ui.compose

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.seniordev.neonstack.data.ControlMode
import com.seniordev.neonstack.engine.BlockShape
import com.seniordev.neonstack.engine.RotationState
import com.seniordev.neonstack.ui.compose.theme.*
import com.seniordev.neonstack.ui.mvi.NeonStackUiState

private val ClassicBadgeText = Color(0xFFB8F8FF)
private val RogueAccent = Color(0xFFE040FB)
private val RogueBadgeText = Color(0xFFF6D2FF)
private val SubtleSurface = Color(0xCC141926)
private val HairlineBorder = Color(0xFF28334E)

@Composable
fun WelcomeScreen(
    state: NeonStackUiState,
    onStartClassic: () -> Unit,
    onStartRogue: () -> Unit,
    onOpenLeaderboard: () -> Unit,
    onOpenCalibration: () -> Unit,
    onToggleSound: (Boolean) -> Unit,
    onToggleMusic: (Boolean) -> Unit,
    onToggleHaptics: (Boolean) -> Unit,
    onToggleCrtFilter: (Boolean) -> Unit,
    onSetControlMode: (ControlMode) -> Unit,
    modifier: Modifier = Modifier,
    initialSettingsOpen: Boolean = false
) {
    var isSettingsOpen by remember { mutableStateOf(initialSettingsOpen) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BgMain)
    ) {
        // Animated Tron Grid & Drifting Translucent Prism Blocks Backdrop with edge vignettes
        WelcomeBackdropCanvas(modifier = Modifier.fillMaxSize())

        // Foreground Content
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            val viewportHeight = maxHeight
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .heightIn(min = viewportHeight)
                    .padding(horizontal = 20.dp)
                    .padding(top = 12.dp, bottom = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 1. Quiet Top Utility Bar (Hall of Fame + Settings)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .height(42.dp)
                            .clip(RoundedCornerShape(21.dp))
                            .background(SubtleSurface)
                            .border(1.dp, HairlineBorder, RoundedCornerShape(21.dp))
                            .clickable(onClick = onOpenLeaderboard)
                            .semantics {
                                contentDescription = "Open Hall of Fame"
                                role = Role.Button
                            }
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TrophyVectorIcon(tint = AccentYellow, size = 18.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "HALL OF FAME",
                            color = TextPrimary.copy(alpha = 0.92f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            maxLines = 1
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(SubtleSurface)
                            .border(1.dp, HairlineBorder, CircleShape)
                            .clickable { isSettingsOpen = true }
                            .semantics {
                                contentDescription = "Open Settings"
                                role = Role.Button
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        SettingsGearVectorIcon(tint = TextPrimary.copy(alpha = 0.9f), size = 19.dp)
                    }
                }

                // 2. Hero Branding Block (Generous Breathing Room, Distinctive Emblem)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(78.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFF1A243B),
                                        Color(0xFF141828)
                                    )
                                )
                            )
                            .border(
                                width = 1.25.dp,
                                brush = Brush.linearGradient(
                                    listOf(
                                        AccentCyan.copy(alpha = 0.75f),
                                        RogueAccent.copy(alpha = 0.65f)
                                    )
                                ),
                                shape = RoundedCornerShape(22.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        HeroBrandEmblemIcon(size = 46.dp)
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "NEON STACK",
                        color = TextPrimary,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 4.5.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Editorial borderless subtitle (clearly non-interactive)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(18.dp)
                                .height(1.dp)
                                .background(AccentCyan.copy(alpha = 0.45f))
                        )
                        Text(
                            text = "ARCADE EDITION",
                            color = AccentCyan.copy(alpha = 0.88f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 3.sp
                        )
                        Box(
                            modifier = Modifier
                                .width(18.dp)
                                .height(1.dp)
                                .background(AccentCyan.copy(alpha = 0.45f))
                        )
                    }
                }

                // 3. Lower Thumb-Zone Stack: Primary Mode Launch Cards + Compact Controls Dock
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "CHOOSE MODE",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.2.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )

                        val rogueStatText = when {
                            state.rogueBestFloor > 0 && state.rogueRuns > 0 ->
                                "BEST FLOOR ${state.rogueBestFloor}  •  ${state.rogueRuns} RUNS"
                            state.rogueBestFloor > 0 ->
                                "BEST FLOOR ${state.rogueBestFloor}"
                            state.rogueRuns > 0 ->
                                "${state.rogueRuns} RUNS  •  9 RELICS"
                            else ->
                                "9 RELICS  •  BOSS EVERY 3RD FLOOR"
                        }

                        ModeLaunchCard(
                            title = "ROGUE SPIRE",
                            badge = "DECKBUILDER",
                            badgeTextColor = RogueBadgeText,
                            subtitle = "Floor Quotas • Relics • Bosses",
                            statText = rogueStatText,
                            statColor = if (state.rogueBestFloor > 0) RogueBadgeText else TextSecondary,
                            accentColor = RogueAccent,
                            icon = { LightningVectorIcon(tint = RogueAccent, size = 24.dp) },
                            contentDescription = "Play Rogue Spire Mode",
                            onClick = onStartRogue
                        )

                        val classicBest = maxOf(state.classicBestScore, state.highScore)
                        val classicStatText = if (classicBest > 0L) {
                            "BEST  %,d".format(classicBest)
                        } else {
                            "9×16 ENDLESS ARCADE"
                        }

                        ModeLaunchCard(
                            title = "ENDLESS",
                            badge = "9×16 ARCADE",
                            badgeTextColor = ClassicBadgeText,
                            subtitle = "12-Bag Polyominoes • Overclock Spins",
                            statText = classicStatText,
                            statColor = if (classicBest > 0L) AccentYellow else TextSecondary,
                            accentColor = AccentCyan,
                            icon = { EndlessBlocksVectorIcon(tint = AccentCyan, size = 26.dp) },
                            contentDescription = "Play Endless Mode",
                            onClick = onStartClassic
                        )
                    }

                    // 4. Compact Bottom Controls Dock (Clean, Font-Scale Safe, Single Accent System)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ControlModeToggleBar(
                            selectedMode = state.controlMode,
                            onSelectMode = onSetControlMode
                        )

                        val controlHint = if (state.controlMode == ControlMode.GESTURES) {
                            "Drag to move • Tap to rotate • Flick down to slam"
                        } else {
                            "Dual-thumb arcade pad • Tap VAULT anytime"
                        }
                        Text(
                            text = controlHint,
                            color = TextSecondary.copy(alpha = 0.78f),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        }

        if (isSettingsOpen) {
            WelcomeSettingsDialog(
                state = state,
                onToggleSound = onToggleSound,
                onToggleMusic = onToggleMusic,
                onToggleHaptics = onToggleHaptics,
                onToggleCrtFilter = onToggleCrtFilter,
                onSetControlMode = onSetControlMode,
                onOpenCalibration = {
                    isSettingsOpen = false
                    onOpenCalibration()
                },
                onDismiss = { isSettingsOpen = false }
            )
        }
    }
}

@Composable
private fun ModeLaunchCard(
    title: String,
    badge: String,
    badgeTextColor: Color,
    subtitle: String,
    statText: String,
    statColor: Color,
    accentColor: Color,
    icon: @Composable () -> Unit,
    contentDescription: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        BgCard.copy(alpha = 0.96f),
                        accentColor.copy(alpha = 0.13f)
                    )
                )
            )
            .border(
                width = 1.25.dp,
                brush = Brush.linearGradient(
                    listOf(
                        accentColor.copy(alpha = 0.65f),
                        accentColor.copy(alpha = 0.24f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .semantics {
                this.contentDescription = contentDescription
                this.role = Role.Button
            }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Mode Emblem (clean tinted surface without redundant inner border)
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(accentColor.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Mode Info + Embedded Career Progress
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(accentColor.copy(alpha = 0.22f))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badge,
                        color = badgeTextColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.7.sp,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = statText,
                color = statColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // High-Luminance Filled Play CTA
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(accentColor),
            contentAlignment = Alignment.Center
        ) {
            PlayTriangleVectorIcon(
                tint = BgMain,
                size = 16.dp,
                modifier = Modifier.offset(x = 1.dp)
            )
        }
    }
}

@Composable
private fun ControlModeToggleBar(
    selectedMode: ControlMode,
    onSelectMode: (ControlMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SubtleSurface)
            .border(1.dp, HairlineBorder, RoundedCornerShape(14.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ControlModeOption(
            label = "ARCADE PAD",
            selected = selectedMode == ControlMode.ARCADE_PAD,
            onClick = { onSelectMode(ControlMode.ARCADE_PAD) },
            modifier = Modifier.weight(1f)
        )
        ControlModeOption(
            label = "GESTURES",
            selected = selectedMode == ControlMode.GESTURES,
            onClick = { onSelectMode(ControlMode.GESTURES) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ControlModeOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) AccentCyan.copy(alpha = 0.18f) else Color.Transparent)
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = "Select $label controls"
                role = Role.Tab
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) ClassicBadgeText else TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.9.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun WelcomeSettingsDialog(
    state: NeonStackUiState,
    onToggleSound: (Boolean) -> Unit,
    onToggleMusic: (Boolean) -> Unit,
    onToggleHaptics: (Boolean) -> Unit,
    onToggleCrtFilter: (Boolean) -> Unit,
    onSetControlMode: (ControlMode) -> Unit,
    onOpenCalibration: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(BgCard)
                .border(1.5.dp, HairlineBorder, RoundedCornerShape(22.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SettingsGearVectorIcon(tint = AccentCyan, size = 18.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "SETTINGS",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.6.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(BtnNormal)
                        .clickable(onClick = onDismiss)
                        .semantics {
                            contentDescription = "Close Settings"
                            role = Role.Button
                        },
                    contentAlignment = Alignment.Center
                ) {
                    CloseVectorIcon(tint = TextSecondary, size = 16.dp)
                }
            }

            // Audio & Feedback Group
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(BgMain.copy(alpha = 0.65f))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                SettingsToggleRow(
                    title = "Sound FX",
                    subtitle = "Synthesized arcade effects",
                    checked = state.isSoundEnabled,
                    onCheckedChange = onToggleSound
                )
                SettingsDivider()
                SettingsToggleRow(
                    title = "Chiptune Music",
                    subtitle = "Dynamic level-reactive BGM",
                    checked = state.isMusicEnabled,
                    onCheckedChange = onToggleMusic
                )
                SettingsDivider()
                SettingsToggleRow(
                    title = "Haptics",
                    subtitle = "Tactile lock & line-clear feedback",
                    checked = state.isHapticsEnabled,
                    onCheckedChange = onToggleHaptics
                )
                SettingsDivider()
                SettingsToggleRow(
                    title = "Retro CRT Scanlines",
                    subtitle = "Subtle phosphor screen shader",
                    checked = state.isCrtFilterEnabled,
                    onCheckedChange = onToggleCrtFilter
                )
            }

            // Control Mode
            ControlModeToggleBar(
                selectedMode = state.controlMode,
                onSelectMode = onSetControlMode
            )

            // Pro Calibration Studio Entry
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(BtnNormal)
                    .clickable(onClick = onOpenCalibration)
                    .semantics {
                        contentDescription = "Open Calibration Studio"
                        role = Role.Button
                    }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(AccentCyan.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    SlidersVectorIcon(tint = AccentCyan, size = 18.dp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "CALIBRATION STUDIO",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "DAS ${state.dasDelayMs}ms • ARR ${state.arrRateMs}ms • Drop ${state.softDropRateMs}ms",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                ChevronRightVectorIcon(tint = TextSecondary, size = 16.dp)
            }
        }
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = BgMain,
                checkedTrackColor = AccentCyan,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = BtnNormal,
                uncheckedBorderColor = HairlineBorder
            )
        )
    }
}

@Composable
private fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(BorderCard.copy(alpha = 0.45f))
    )
}

private data class BackdropPiece(
    val type: BlockShape,
    val rotation: RotationState,
    val xFraction: Float,
    val phaseOffset: Float,
    val speedMultiplier: Float,
    val cellDp: Float,
    val alpha: Float
)

@Composable
private fun WelcomeBackdropCanvas(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "welcome_backdrop")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "block_drift"
    )

    val pieces = remember {
        listOf(
            BackdropPiece(BlockShape.ARCH, RotationState.SPAWN, 0.08f, 0.10f, 1.0f, 18f, 0.11f),
            BackdropPiece(BlockShape.THUMB, RotationState.CLOCKWISE, 0.28f, 0.45f, 2.0f, 19f, 0.10f),
            BackdropPiece(BlockShape.ELBOW, RotationState.FLIP, 0.54f, 0.75f, 1.0f, 18f, 0.10f),
            BackdropPiece(BlockShape.PULSE, RotationState.CLOCKWISE, 0.78f, 0.25f, 2.0f, 19f, 0.10f),
            BackdropPiece(BlockShape.SPARK, RotationState.SPAWN, 0.88f, 0.65f, 1.0f, 17f, 0.11f),
            BackdropPiece(BlockShape.CROWN, RotationState.SPAWN, 0.15f, 0.82f, 2.0f, 17f, 0.08f),
            BackdropPiece(BlockShape.BEAM, RotationState.COUNTER_CLOCKWISE, 0.68f, 0.05f, 1.0f, 18f, 0.09f)
        )
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Ambient cybernetic radial glow
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    AccentCyan.copy(alpha = 0.11f),
                    Color.Transparent
                ),
                center = Offset(w * 0.5f, h * 0.25f),
                radius = w * 0.85f
            )
        )
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    RogueAccent.copy(alpha = 0.09f),
                    Color.Transparent
                ),
                center = Offset(w * 0.8f, h * 0.68f),
                radius = w * 0.75f
            )
        )

        // Subtle Tron grid lines
        val gridStep = 34.dp.toPx()
        val gridColor = BorderCard.copy(alpha = 0.24f)
        var x = 0f
        while (x <= w) {
            drawLine(gridColor, Offset(x, 0f), Offset(x, h), strokeWidth = 1f)
            x += gridStep
        }
        var y = 0f
        while (y <= h) {
            drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
            y += gridStep
        }

        // Drifting translucent jewel-faceted Prism Blocks
        pieces.forEach { piece ->
            val cellPx = piece.cellDp.dp.toPx()
            val gap = cellPx * 0.12f
            val travelHeight = h + cellPx * 6f
            val cycle = (progress * piece.speedMultiplier + piece.phaseOffset) % 1f
            val baseX = w * piece.xFraction
            val baseY = cycle * travelHeight - cellPx * 4f
            val color = Color(piece.type.colorHex).copy(alpha = piece.alpha)
            val corner = CornerRadius(cellPx * 0.18f, cellPx * 0.18f)

            piece.type.getMinos(piece.rotation).forEach { pt ->
                val mx = baseX + pt.x * (cellPx + gap)
                val my = baseY + pt.y * (cellPx + gap)
                drawRoundRect(
                    color = color,
                    topLeft = Offset(mx, my),
                    size = Size(cellPx, cellPx),
                    cornerRadius = corner
                )
            }
        }

        // Top & Bottom subtle scrim vignettes so status bar & footer text remain crisp
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(BgMain.copy(alpha = 0.88f), Color.Transparent),
                startY = 0f,
                endY = h * 0.14f
            )
        )
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Transparent, BgMain.copy(alpha = 0.94f)),
                startY = h * 0.80f,
                endY = h
            )
        )
    }
}

@Preview(name = "Welcome Screen", showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun WelcomeScreenPreview() {
    NeonStackTheme {
        WelcomeScreen(
            state = NeonStackUiState(
                highScore = 244L,
                classicBestScore = 244L,
                rogueBestFloor = 4,
                rogueRuns = 12,
                gamesPlayed = 21,
                isSoundEnabled = true,
                isMusicEnabled = true,
                isHapticsEnabled = true,
                isCrtFilterEnabled = true,
                controlMode = ControlMode.ARCADE_PAD
            ),
            onStartClassic = {},
            onStartRogue = {},
            onOpenLeaderboard = {},
            onOpenCalibration = {},
            onToggleSound = {},
            onToggleMusic = {},
            onToggleHaptics = {},
            onToggleCrtFilter = {},
            onSetControlMode = {}
        )
    }
}

@Preview(name = "Welcome Settings", showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun WelcomeSettingsPreview() {
    NeonStackTheme {
        WelcomeScreen(
            state = NeonStackUiState(
                highScore = 244L,
                classicBestScore = 244L,
                rogueBestFloor = 4,
                rogueRuns = 12,
                gamesPlayed = 21,
                isSoundEnabled = true,
                isMusicEnabled = true,
                isHapticsEnabled = true,
                isCrtFilterEnabled = true,
                controlMode = ControlMode.ARCADE_PAD
            ),
            onStartClassic = {},
            onStartRogue = {},
            onOpenLeaderboard = {},
            onOpenCalibration = {},
            onToggleSound = {},
            onToggleMusic = {},
            onToggleHaptics = {},
            onToggleCrtFilter = {},
            onSetControlMode = {},
            initialSettingsOpen = true
        )
    }
}
