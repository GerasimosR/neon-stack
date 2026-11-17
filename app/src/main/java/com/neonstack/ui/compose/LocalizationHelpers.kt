package com.neonstack.ui.compose

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.core.os.LocaleListCompat
import com.neonstack.R
import com.neonstack.engine.LineClearReward
import com.neonstack.engine.SpinBonus
import com.neonstack.engine.rogue.Relic
import com.neonstack.engine.rogue.RelicId
import com.neonstack.engine.rogue.RelicRarity

data class AppLanguageOption(
    val tag: String, // "" = system default
    val shortLabel: String
)

val SupportedAppLanguages = listOf(
    AppLanguageOption("", "AUTO"),
    AppLanguageOption("en", "EN"),
    AppLanguageOption("el", "ΕΛ"),
    AppLanguageOption("es", "ES"),
    AppLanguageOption("de", "DE"),
    AppLanguageOption("fr", "FR"),
    AppLanguageOption("ja", "日本語")
)

fun getCurrentAppLanguageTag(): String {
    val locales = AppCompatDelegate.getApplicationLocales()
    if (locales.isEmpty) return ""
    val primary = locales[0]?.language ?: return ""
    return if (SupportedAppLanguages.any { it.tag == primary }) primary else ""
}

fun setAppLanguageTag(tag: String) {
    val localeList = if (tag.isEmpty()) {
        LocaleListCompat.getEmptyLocaleList()
    } else {
        LocaleListCompat.forLanguageTags(tag)
    }
    AppCompatDelegate.setApplicationLocales(localeList)
}

@Composable
fun Relic.localizedName(): String = when (id) {
    RelicId.MIDAS_TOUCH -> stringResource(R.string.relic_midas_name)
    RelicId.OVERHANG_DRILL -> stringResource(R.string.relic_drill_name)
    RelicId.CHRONO_STABILIZER -> stringResource(R.string.relic_chrono_name)
    RelicId.QUAD_SURGE -> stringResource(R.string.relic_surge_name)
    RelicId.DOUBLE_HOLD -> stringResource(R.string.relic_dual_vault_name)
    RelicId.GOLDEN_I_BAG -> stringResource(R.string.relic_conduit_name)
    RelicId.VAPORIZER_BOMB -> stringResource(R.string.relic_bomb_name)
    RelicId.LASER_SWEEP -> stringResource(R.string.relic_laser_name)
    RelicId.GRAVITY_ANCHOR -> stringResource(R.string.relic_anchor_name)
}

@Composable
fun Relic.localizedDescription(): String = when (id) {
    RelicId.MIDAS_TOUCH -> stringResource(R.string.relic_midas_desc)
    RelicId.OVERHANG_DRILL -> stringResource(R.string.relic_drill_desc)
    RelicId.CHRONO_STABILIZER -> stringResource(R.string.relic_chrono_desc)
    RelicId.QUAD_SURGE -> stringResource(R.string.relic_surge_desc)
    RelicId.DOUBLE_HOLD -> stringResource(R.string.relic_dual_vault_desc)
    RelicId.GOLDEN_I_BAG -> stringResource(R.string.relic_conduit_desc)
    RelicId.VAPORIZER_BOMB -> stringResource(R.string.relic_bomb_desc)
    RelicId.LASER_SWEEP -> stringResource(R.string.relic_laser_desc)
    RelicId.GRAVITY_ANCHOR -> stringResource(R.string.relic_anchor_desc)
}

@Composable
fun RelicRarity.localizedLabel(): String = when (this) {
    RelicRarity.COMMON -> stringResource(R.string.rarity_common)
    RelicRarity.RARE -> stringResource(R.string.rarity_rare)
    RelicRarity.LEGENDARY -> stringResource(R.string.rarity_legendary)
}

fun formatLocalizedRewardBanner(context: Context, reward: LineClearReward): String {
    if (reward.isQuadSurge) {
        return context.getString(R.string.banner_quad_surge)
    }
    val base = when (reward.lines) {
        0 -> when (reward.spin) {
            SpinBonus.MINI -> context.getString(R.string.banner_spin_mini)
            SpinBonus.FULL -> context.getString(R.string.banner_spin_full)
            SpinBonus.NONE -> reward.message
        }
        1 -> when (reward.spin) {
            SpinBonus.NONE -> context.getString(R.string.banner_single)
            SpinBonus.MINI -> context.getString(R.string.banner_spin_mini)
            SpinBonus.FULL -> context.getString(R.string.banner_spin_single)
        }
        2 -> when (reward.spin) {
            SpinBonus.NONE -> context.getString(R.string.banner_double)
            SpinBonus.MINI, SpinBonus.FULL -> context.getString(R.string.banner_spin_double)
        }
        3 -> when (reward.spin) {
            SpinBonus.NONE -> context.getString(R.string.banner_triple)
            SpinBonus.MINI, SpinBonus.FULL -> context.getString(R.string.banner_spin_triple)
        }
        4 -> context.getString(R.string.banner_quad)
        else -> reward.message
    }

    val withSurge = if (reward.isBackToBack) {
        context.getString(R.string.banner_surge_prefix, base)
    } else {
        base
    }
    return if (reward.comboCount > 0) {
        context.getString(R.string.banner_chain_suffix, withSurge, reward.comboCount)
    } else {
        withSurge
    }
}
