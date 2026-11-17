package com.seniordev.neonstack.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seniordev.neonstack.R
import com.seniordev.neonstack.engine.rogue.GameMode
import com.seniordev.neonstack.engine.rogue.Relic
import com.seniordev.neonstack.engine.rogue.RelicRarity
import com.seniordev.neonstack.ui.compose.theme.*
import com.seniordev.neonstack.ui.mvi.NeonStackUiState

@Composable
fun TopHeaderBar(
    state: NeonStackUiState,
    onPauseClick: () -> Unit,
    onLeaderboardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cdLeaderboard = stringResource(R.string.cd_open_hall_of_fame)
    val cdPause = stringResource(R.string.cd_pause_game)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val isRogue = state.gameMode == GameMode.ROGUE
        val titleText = if (isRogue) stringResource(R.string.mode_rogue_title) else stringResource(R.string.brand_title)
        val titleColor = if (isRogue) AccentPink else AccentCyan

        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = titleText,
                color = titleColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.4.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.width(8.dp))

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(titleColor.copy(alpha = 0.14f))
                    .border(1.dp, titleColor.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 7.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isRogue) {
                    LightningVectorIcon(tint = AccentPink, size = 10.dp)
                    Spacer(modifier = Modifier.width(3.dp))
                }
                Text(
                    text = if (isRogue) stringResource(R.string.mode_rogue_short_badge) else stringResource(R.string.mode_endless_short_badge),
                    color = titleColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.6.sp,
                    maxLines = 1
                )
            }
        }

        // Leaderboard Trophy Button
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(BtnNormal)
                .border(1.5.dp, BorderCard, RoundedCornerShape(11.dp))
                .semantics {
                    contentDescription = cdLeaderboard
                    role = Role.Button
                }
                .clickable(onClick = onLeaderboardClick),
            contentAlignment = Alignment.Center
        ) {
            TrophyVectorIcon(size = 18.dp)
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Pause Button
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(BtnNormal)
                .border(1.5.dp, BorderCard, RoundedCornerShape(11.dp))
                .semantics {
                    contentDescription = cdPause
                    role = Role.Button
                }
                .clickable(onClick = onPauseClick),
            contentAlignment = Alignment.Center
        ) {
            PauseVectorIcon(size = 15.dp)
        }
    }
}

@Composable
fun ScoreStatsRow(
    state: NeonStackUiState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (state.gameMode == GameMode.ROGUE) {
            val floorTitle = if (state.isBossFloor) stringResource(R.string.boss_label) else stringResource(R.string.floor_label)
            val floorVal = "${state.currentFloor}"
            val floorColor = if (state.isBossFloor) AccentPink else AccentCyan
            val linesClearedOnFloor = state.targetLinesForFloor - state.linesRemainingOnFloor

            StatCard(
                title = floorTitle,
                value = floorVal,
                valueColor = floorColor,
                titleLeadingIcon = if (state.isBossFloor) {
                    { SkullVectorIcon(tint = AccentPink, size = 11.dp) }
                } else null,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = stringResource(R.string.quota_label),
                value = "$linesClearedOnFloor/${state.targetLinesForFloor}",
                valueColor = AccentYellow,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = stringResource(R.string.score_label),
                value = "%,d".format(state.score),
                valueColor = TextPrimary,
                modifier = Modifier.weight(1.2f)
            )
        } else {
            StatCard(
                title = stringResource(R.string.score_label),
                value = "%,d".format(state.score),
                valueColor = TextPrimary,
                modifier = Modifier.weight(1.2f)
            )
            StatCard(
                title = stringResource(R.string.level_label),
                value = state.level.toString(),
                valueColor = AccentCyan,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = stringResource(R.string.lines_label),
                value = state.lines.toString(),
                valueColor = AccentPink,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ActiveRelicsRow(
    relics: List<Relic>,
    modifier: Modifier = Modifier
) {
    if (relics.isEmpty()) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.relics_label),
            color = TextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(end = 2.dp)
        )

        relics.forEach { relic ->
            val (rarityColor, _) = when (relic.rarity) {
                RelicRarity.COMMON -> Pair(AccentCyan, BorderCard)
                RelicRarity.RARE -> Pair(AccentPink, AccentPink)
                RelicRarity.LEGENDARY -> Pair(AccentYellow, AccentYellow)
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(BgCard)
                    .border(1.dp, rarityColor, RoundedCornerShape(8.dp))
                    .padding(horizontal = 6.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RelicVectorIcon(
                    relicId = relic.id,
                    tint = rarityColor,
                    size = 12.dp,
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text(
                    text = relic.localizedName().uppercase(),
                    color = rarityColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    valueColor: Color,
    titleLeadingIcon: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(BgCard)
            .border(1.dp, BorderCard, RoundedCornerShape(10.dp))
            .padding(vertical = 4.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (titleLeadingIcon != null) {
                titleLeadingIcon()
                Spacer(modifier = Modifier.width(3.dp))
            }
            Text(
                text = title,
                color = TextSecondary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.height(1.dp))
        Text(
            text = value,
            color = valueColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            style = TextStyle(fontFeatureSettings = "tnum")
        )
    }
}
