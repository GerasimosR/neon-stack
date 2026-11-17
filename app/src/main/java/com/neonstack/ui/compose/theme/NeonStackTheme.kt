package com.neonstack.ui.compose.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val BgMain = Color(0xFF0C0E14)
val BgCard = Color(0xFF141926)
val BorderCard = Color(0xFF252E46)
val AccentCyan = Color(0xFF00E5FF)
val AccentYellow = Color(0xFFFFD700)
val AccentPink = Color(0xFFD500F9)
val AccentRed = Color(0xFFFF1744)
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFF8A9BB8)
val BtnNormal = Color(0xFF1D2436)
val BtnPressed = Color(0xFF2B3652)
val BtnHardDrop = Color(0xFF9C27B0)

private val DarkColorScheme = darkColorScheme(
    primary = AccentCyan,
    secondary = AccentYellow,
    tertiary = AccentPink,
    background = BgMain,
    surface = BgCard,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun NeonStackTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
