package com.neonstack.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.neonstack.R
import com.neonstack.engine.rogue.Relic
import com.neonstack.engine.rogue.RelicRarity
import com.neonstack.ui.compose.theme.*

@Composable
fun RelicDraftDialog(
    currentFloor: Int,
    options: List<Relic>,
    onSelectRelic: (Relic) -> Unit
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
                .border(2.dp, AccentPink, RoundedCornerShape(20.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.floor_cleared_title, currentFloor),
                color = AccentYellow,
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.2.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.draft_relic_subtitle),
                color = AccentCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
            )

            // 3 Relic Cards
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                options.forEach { relic ->
                    RelicCard(relic = relic, onClick = { onSelectRelic(relic) })
                }
            }
        }
    }
}

@Composable
private fun RelicCard(
    relic: Relic,
    onClick: () -> Unit
) {
    val (rarityColor, rarityBorder) = when (relic.rarity) {
        RelicRarity.COMMON -> Pair(AccentCyan, BorderCard)
        RelicRarity.RARE -> Pair(AccentPink, AccentPink)
        RelicRarity.LEGENDARY -> Pair(AccentYellow, AccentYellow)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BgMain)
            .border(1.5.dp, rarityBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(BtnNormal)
                .border(1.dp, rarityColor, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            RelicVectorIcon(
                relicId = relic.id,
                tint = rarityColor,
                size = 24.dp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = relic.localizedName(),
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = relic.rarity.localizedLabel(),
                    color = rarityColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp
                )
            }
            Text(
                text = relic.localizedDescription(),
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 15.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
