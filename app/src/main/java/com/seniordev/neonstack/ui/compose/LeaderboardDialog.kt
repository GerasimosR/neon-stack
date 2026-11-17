package com.seniordev.neonstack.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.seniordev.neonstack.R
import com.seniordev.neonstack.data.LeaderboardEntry
import com.seniordev.neonstack.ui.compose.theme.*
import com.seniordev.neonstack.ui.mvi.NeonStackUiState

@Composable
fun LeaderboardDialog(
    state: NeonStackUiState,
    onSaveName: (String) -> Unit,
    onResetLeaderboard: () -> Unit,
    onDismiss: () -> Unit
) {
    var customTag by remember { mutableStateOf("YOU") }

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
                horizontalArrangement = Arrangement.Center
            ) {
                TrophyVectorIcon(tint = AccentYellow, size = 20.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.hall_of_fame),
                    color = AccentYellow,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.6.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = stringResource(R.string.top_legends),
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Optional Arcade Name Entry (if player just scored into the Top 10)
            if (state.latestRankAchieved != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(BgMain)
                        .border(1.5.dp, AccentCyan, RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.rank_prefix, state.latestRankAchieved),
                        color = AccentCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = customTag,
                        onValueChange = { if (it.length <= 6) customTag = it.uppercase() },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        modifier = Modifier.weight(1f).height(48.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { onSaveName(customTag) })
                    )
                    Button(
                        onClick = { onSaveName(customTag) },
                        colors = ButtonDefaults.buttonColors(containerColor = BtnHardDrop),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(stringResource(R.string.tag_button), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Top 10 List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .heightIn(max = 340.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(state.leaderboardEntries) { entry ->
                    LeaderboardRow(entry = entry, isCurrentPlayer = entry.rank == state.latestRankAchieved)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons (Reset & Close)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onResetLeaderboard,
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BtnNormal)
                ) {
                    Text(
                        text = stringResource(R.string.reset),
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1.4f).height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BtnNormal),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, AccentCyan)
                ) {
                    Text(
                        text = stringResource(R.string.close),
                        color = AccentCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun LeaderboardRow(entry: LeaderboardEntry, isCurrentPlayer: Boolean) {
    val rankColor = when (entry.rank) {
        1 -> AccentYellow
        2 -> TextPrimary
        3 -> AccentPink
        else -> TextSecondary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isCurrentPlayer) BtnPressed else BgMain)
            .border(
                width = if (isCurrentPlayer) 1.5.dp else 1.dp,
                color = if (isCurrentPlayer) AccentYellow else BorderCard,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.width(32.dp),
            contentAlignment = Alignment.Center
        ) {
            if (entry.rank <= 3) {
                MedalVectorIcon(rank = entry.rank, size = 20.dp)
            } else {
                Text(
                    text = "#${entry.rank}",
                    color = rankColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }

        Text(
            text = entry.playerName,
            color = if (isCurrentPlayer) AccentCyan else TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp,
            modifier = Modifier.width(64.dp)
        )

        Text(
            text = "%,d".format(entry.score),
            color = AccentYellow,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = stringResource(R.string.leaderboard_meta, entry.level, entry.lines),
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal
        )
    }
}
