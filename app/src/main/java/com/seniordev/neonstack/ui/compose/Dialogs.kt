package com.seniordev.neonstack.ui.compose

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.seniordev.neonstack.data.ControlMode
import com.seniordev.neonstack.engine.rogue.GameMode
import com.seniordev.neonstack.ui.compose.theme.*
import com.seniordev.neonstack.ui.mvi.NeonStackUiState

@Composable
fun PauseDialog(
    state: NeonStackUiState,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onMainMenu: () -> Unit,
    onToggleSound: (Boolean) -> Unit,
    onToggleMusic: (Boolean) -> Unit,
    onToggleHaptics: (Boolean) -> Unit,
    onToggleCrtFilter: (Boolean) -> Unit,
    onSetControlMode: (ControlMode) -> Unit,
    onOpenCalibration: () -> Unit,
    onOpenLeaderboard: () -> Unit
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(BgCard)
                .border(2.dp, BorderCard, RoundedCornerShape(20.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "GAME PAUSED",
                color = AccentCyan,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Sound Toggle Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sound FX",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = state.isSoundEnabled,
                    onCheckedChange = onToggleSound,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AccentCyan,
                        checkedTrackColor = BorderCard
                    )
                )
            }

            // Music BGM Toggle Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Music BGM (Chiptune)",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = state.isMusicEnabled,
                    onCheckedChange = onToggleMusic,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AccentCyan,
                        checkedTrackColor = BorderCard
                    )
                )
            }

            // Haptics Toggle Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Vibration",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = state.isHapticsEnabled,
                    onCheckedChange = onToggleHaptics,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AccentCyan,
                        checkedTrackColor = BorderCard
                    )
                )
            }

            // Retro CRT Scanlines Shader Toggle Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Retro CRT Scanlines",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = state.isCrtFilterEnabled,
                    onCheckedChange = onToggleCrtFilter,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AccentPink,
                        checkedTrackColor = BorderCard
                    )
                )
            }

            // Control Mode Switcher (Arcade Pad vs Gestures)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Controls",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1f)
                )
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(BgMain)
                        .border(1.dp, BorderCard, RoundedCornerShape(10.dp))
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    val isPad = state.controlMode == ControlMode.ARCADE_PAD
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isPad) AccentCyan.copy(alpha = 0.22f) else Color.Transparent)
                            .clickable { onSetControlMode(ControlMode.ARCADE_PAD) }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "PAD",
                            color = if (isPad) AccentCyan else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!isPad) AccentPink.copy(alpha = 0.22f) else Color.Transparent)
                            .clickable { onSetControlMode(ControlMode.GESTURES) }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "TOUCH",
                            color = if (!isPad) AccentPink else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Leaderboard Button
            Button(
                onClick = onOpenLeaderboard,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BtnNormal),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, AccentYellow)
            ) {
                TrophyVectorIcon(tint = AccentYellow, size = 16.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ARCADE LEADERBOARD",
                    color = AccentYellow,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calibration Studio Button
            Button(
                onClick = onOpenCalibration,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BtnNormal),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, AccentPink)
            ) {
                SlidersVectorIcon(tint = AccentPink, size = 16.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PRO CALIBRATION STUDIO",
                    color = AccentPink,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Resume Button
            Button(
                onClick = onResume,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BtnNormal)
            ) {
                Text(
                    text = "RESUME",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Restart Button
            Button(
                onClick = onRestart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BtnNormal)
            ) {
                Text(
                    text = "RESTART",
                    color = AccentYellow,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Menu Button
            OutlinedButton(
                onClick = onMainMenu,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderCard)
            ) {
                Text(
                    text = "MAIN MENU",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun GameOverDialog(
    state: NeonStackUiState,
    onPlayAgain: () -> Unit,
    onViewLeaderboard: () -> Unit,
    onMainMenu: () -> Unit
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(BgCard)
                .border(2.dp, BorderCard, RoundedCornerShape(20.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "GAME OVER",
                color = AccentRed,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
            )

            if (state.latestRankAchieved != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    StarVectorIcon(tint = AccentYellow, size = 13.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "RANK #${state.latestRankAchieved} ON LEADERBOARD!",
                        color = AccentYellow,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    StarVectorIcon(tint = AccentYellow, size = 13.dp)
                }
            } else if (state.isNewHighScore && state.score > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    StarVectorIcon(tint = AccentYellow, size = 13.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "NEW HIGH SCORE!",
                        color = AccentYellow,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    StarVectorIcon(tint = AccentYellow, size = 13.dp)
                }
            }

            // Stats Breakdown Table
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(BgMain)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (state.gameMode == GameMode.ROGUE) {
                    StatRow("Mode:", "ROGUE SPIRE", AccentPink)
                    StatRow("Floors Cleared:", "${state.currentFloor - 1}", AccentYellow)
                    StatRow("Relics Drafted:", "${state.activeRelics.size}", AccentCyan)
                }
                StatRow("Final Score:", "%,d".format(state.score), TextPrimary)
                StatRow("Best Score:", "%,d".format(state.highScore), AccentYellow)
                StatRow("Lines Cleared:", state.lines.toString(), AccentPink)
                StatRow("Level Reached:", state.level.toString(), AccentCyan)
                StatRow("Quads / Spins:", "${state.quadsCount} / ${state.spinsCount}", TextPrimary)
            }

            // View Leaderboard Button
            Button(
                onClick = onViewLeaderboard,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BtnNormal),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, AccentYellow)
            ) {
                TrophyVectorIcon(tint = AccentYellow, size = 15.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "VIEW LEADERBOARD",
                    color = AccentYellow,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Play Again Button
            Button(
                onClick = onPlayAgain,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BtnHardDrop)
            ) {
                Text(
                    text = "PLAY AGAIN",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Menu Button
            OutlinedButton(
                onClick = onMainMenu,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderCard)
            ) {
                Text(
                    text = "MAIN MENU",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextSecondary, fontSize = 14.sp)
        Text(text = value, color = valueColor, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun CalibrationDialog(
    state: NeonStackUiState,
    onSave: (das: Long, arr: Long, softDrop: Long) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    var das by androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(state.dasDelayMs.toFloat()) }
    var arr by androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(state.arrRateMs.toFloat()) }
    var softDrop by androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(state.softDropRateMs.toFloat()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(BgCard)
                .border(2.dp, BorderCard, RoundedCornerShape(20.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                SlidersVectorIcon(tint = AccentCyan, size = 18.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "CALIBRATION STUDIO",
                    color = AccentCyan,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }

            // Slider 1: DAS (Delayed Auto Shift)
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "DAS (Shift Delay):", color = TextSecondary, fontSize = 13.sp)
                    Text(text = "${das.toInt()} ms", color = AccentYellow, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = das,
                    onValueChange = { das = it },
                    valueRange = 80f..260f,
                    steps = 17,
                    colors = SliderDefaults.colors(
                        thumbColor = AccentYellow,
                        activeTrackColor = AccentYellow,
                        inactiveTrackColor = BorderCard
                    )
                )
            }

            // Slider 2: ARR (Auto Repeat Rate)
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "ARR (Repeat Rate):", color = TextSecondary, fontSize = 13.sp)
                    val label = if (arr.toInt() == 0) "0 ms (INSTANT)" else "${arr.toInt()} ms"
                    Text(text = label, color = AccentCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = arr,
                    onValueChange = { arr = it },
                    valueRange = 0f..65f,
                    steps = 12,
                    colors = SliderDefaults.colors(
                        thumbColor = AccentCyan,
                        activeTrackColor = AccentCyan,
                        inactiveTrackColor = BorderCard
                    )
                )
            }

            // Slider 3: Soft Drop Speed
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Soft Drop Repeat:", color = TextSecondary, fontSize = 13.sp)
                    Text(text = "${softDrop.toInt()} ms", color = AccentPink, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = softDrop,
                    onValueChange = { softDrop = it },
                    valueRange = 15f..80f,
                    steps = 12,
                    colors = SliderDefaults.colors(
                        thumbColor = AccentPink,
                        activeTrackColor = AccentPink,
                        inactiveTrackColor = BorderCard
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        das = 150f
                        arr = 38f
                        softDrop = 40f
                        onReset()
                    },
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BtnNormal)
                ) {
                    Text(text = "RESET", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        onSave(das.toLong(), arr.toLong(), softDrop.toLong())
                    },
                    modifier = Modifier.weight(1.3f).height(46.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BtnHardDrop)
                ) {
                    Text(text = "APPLY", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
