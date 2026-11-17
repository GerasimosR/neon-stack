package com.seniordev.neonstack.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.seniordev.neonstack.R
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
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.game_paused),
                color = AccentCyan,
                fontSize = 21.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.8.sp,
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
                    text = stringResource(R.string.sound),
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
                    text = stringResource(R.string.music),
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
                    text = stringResource(R.string.haptics),
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
                    text = stringResource(R.string.crt_filter),
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
                    text = stringResource(R.string.controls_label),
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
                            text = stringResource(R.string.control_pad_short),
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
                            text = stringResource(R.string.control_touch_short),
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
                    text = stringResource(R.string.arcade_leaderboard),
                    color = AccentYellow,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
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
                    text = stringResource(R.string.pro_calibration_studio),
                    color = AccentPink,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
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
                    text = stringResource(R.string.resume),
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
                    text = stringResource(R.string.restart),
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
                    text = stringResource(R.string.main_menu),
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
                text = stringResource(R.string.game_over),
                color = AccentRed,
                fontSize = 23.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.8.sp
            )

            if (state.latestRankAchieved != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    StarVectorIcon(tint = AccentYellow, size = 13.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.rank_achieved, state.latestRankAchieved),
                        color = AccentYellow,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
                        text = stringResource(R.string.new_high_score),
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
                    StatRow(stringResource(R.string.mode_colon), stringResource(R.string.mode_rogue_title), AccentPink)
                    StatRow(stringResource(R.string.floors_cleared_colon), "${state.currentFloor - 1}", AccentYellow)
                    StatRow(stringResource(R.string.relics_drafted_colon), "${state.activeRelics.size}", AccentCyan)
                }
                StatRow(stringResource(R.string.final_score), "%,d".format(state.score), TextPrimary)
                StatRow(stringResource(R.string.best_score_colon), "%,d".format(state.highScore), AccentYellow)
                StatRow(stringResource(R.string.lines_cleared_colon), state.lines.toString(), AccentPink)
                StatRow(stringResource(R.string.level_reached_colon), state.level.toString(), AccentCyan)
                StatRow(stringResource(R.string.quads_spins_colon), "${state.quadsCount} / ${state.spinsCount}", TextPrimary)
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
                    text = stringResource(R.string.view_leaderboard),
                    color = AccentYellow,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
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
                    text = stringResource(R.string.play_again),
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
                    text = stringResource(R.string.main_menu),
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
    var das by remember { mutableFloatStateOf(state.dasDelayMs.toFloat()) }
    var arr by remember { mutableFloatStateOf(state.arrRateMs.toFloat()) }
    var softDrop by remember { mutableFloatStateOf(state.softDropRateMs.toFloat()) }

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
                    text = stringResource(R.string.calibration_studio),
                    color = AccentCyan,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Slider 1: DAS
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = stringResource(R.string.das_label), color = TextSecondary, fontSize = 13.sp)
                    Text(text = stringResource(R.string.ms_value, das.toInt()), color = AccentYellow, fontSize = 13.sp, fontWeight = FontWeight.Bold)
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

            // Slider 2: ARR
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = stringResource(R.string.arr_label), color = TextSecondary, fontSize = 13.sp)
                    val label = if (arr.toInt() == 0) {
                        stringResource(R.string.arr_instant)
                    } else {
                        stringResource(R.string.ms_value, arr.toInt())
                    }
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
                    Text(text = stringResource(R.string.soft_drop_label), color = TextSecondary, fontSize = 13.sp)
                    Text(text = stringResource(R.string.ms_value, softDrop.toInt()), color = AccentPink, fontSize = 13.sp, fontWeight = FontWeight.Bold)
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
                    Text(
                        text = stringResource(R.string.reset),
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Button(
                    onClick = {
                        onSave(das.toLong(), arr.toLong(), softDrop.toLong())
                    },
                    modifier = Modifier.weight(1.3f).height(46.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BtnHardDrop)
                ) {
                    Text(
                        text = stringResource(R.string.apply),
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
