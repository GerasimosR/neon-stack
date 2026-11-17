package com.seniordev.neonstack.ui.compose

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seniordev.neonstack.ui.compose.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Modern Sculpted Dual-Thumb "Neo-Arcade" Controller.
 *
 * Ergonomic split layout:
 * - Left thumb: Sculpted D-Pad pod (Left / Right with DAS & ARR, Soft Drop below)
 * - Center bridge: Compact circular HOLD well (reachable by either thumb)
 * - Right thumb: Console ABXY-style circular action cluster (Hero Rotate CW, satellite CCW, elevated SLAM)
 */
@Composable
fun ArcadeGamepad(
    onMoveLeft: () -> Unit,
    onMoveRight: () -> Unit,
    onSoftDrop: () -> Unit,
    onHardDrop: () -> Unit,
    onRotateCw: () -> Unit,
    onRotateCcw: () -> Unit,
    onHold: () -> Unit,
    modifier: Modifier = Modifier,
    dasDelayMs: Long = 150L,
    arrRateMs: Long = 38L,
    softDropRateMs: Long = 40L
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 6.dp, end = 6.dp, top = 4.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. LEFT THUMB ZONE — Sculpted Directional Pod
        Box(
            modifier = Modifier
                .width(138.dp)
                .height(118.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            BgCard.copy(alpha = 0.72f),
                            BgMain.copy(alpha = 0.90f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        listOf(
                            BorderCard.copy(alpha = 0.85f),
                            BorderCard.copy(alpha = 0.25f)
                        )
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(8.dp)
        ) {
            // Left (DAS/ARR)
            SculptedDasPad(
                contentDescription = "Move left",
                onAction = onMoveLeft,
                initialDelayMs = dasDelayMs,
                repeatRateMs = arrRateMs,
                accentColor = AccentCyan,
                shape = CircleShape,
                modifier = Modifier
                    .size(54.dp)
                    .align(Alignment.TopStart)
            ) {
                ChevronLeftVectorIcon(size = 20.dp, tint = TextPrimary)
            }

            // Right (DAS/ARR)
            SculptedDasPad(
                contentDescription = "Move right",
                onAction = onMoveRight,
                initialDelayMs = dasDelayMs,
                repeatRateMs = arrRateMs,
                accentColor = AccentCyan,
                shape = CircleShape,
                modifier = Modifier
                    .size(54.dp)
                    .align(Alignment.TopEnd)
            ) {
                ChevronRightVectorIcon(size = 20.dp, tint = TextPrimary)
            }

            // Soft Drop (Continuous)
            SculptedDasPad(
                contentDescription = "Soft drop",
                onAction = onSoftDrop,
                initialDelayMs = 0L,
                repeatRateMs = softDropRateMs,
                accentColor = AccentYellow,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .width(74.dp)
                    .height(42.dp)
                    .align(Alignment.BottomCenter)
            ) {
                ChevronDownVectorIcon(size = 18.dp, tint = AccentYellow)
            }
        }

        // 2. CENTER WELL — Vault Action
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            SculptedActionPad(
                contentDescription = "Vault piece",
                onClick = onHold,
                accentColor = AccentCyan,
                shape = CircleShape,
                modifier = Modifier.size(46.dp)
            ) {
                HoldCycleVectorIcon(size = 18.dp, tint = TextPrimary)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "VAULT",
                color = TextSecondary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
        }

        // 3. RIGHT THUMB ZONE — Console Action Cluster (SLAM + CCW + Hero CW)
        Box(
            modifier = Modifier
                .width(138.dp)
                .height(118.dp)
        ) {
            // HARD DROP / SLAM (Top-center-right elevated neon sphere)
            SculptedActionPad(
                contentDescription = "Hard drop slam",
                onClick = onHardDrop,
                accentColor = AccentPink,
                topFill = Color(0xFFB732D4),
                bottomFill = Color(0xFF6B1485),
                idleBorderAlpha = 0.85f,
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier
                    .width(74.dp)
                    .height(44.dp)
                    .align(Alignment.TopEnd)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    HardDropSlamVectorIcon(size = 16.dp, tint = AccentYellow)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "SLAM",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp
                    )
                }
            }

            // ROTATE CCW (Bottom-left satellite circle)
            SculptedActionPad(
                contentDescription = "Rotate counter-clockwise",
                onClick = onRotateCcw,
                accentColor = TextPrimary,
                shape = CircleShape,
                modifier = Modifier
                    .size(52.dp)
                    .align(Alignment.BottomStart)
                    .offset(y = (-4).dp)
            ) {
                RotateCcwVectorIcon(size = 20.dp, tint = TextPrimary)
            }

            // ROTATE CW (Bottom-right Hero Primary circle)
            SculptedActionPad(
                contentDescription = "Rotate clockwise",
                onClick = onRotateCw,
                accentColor = AccentCyan,
                topFill = Color(0xFF17364A),
                bottomFill = Color(0xFF111C2D),
                idleBorderAlpha = 0.72f,
                shape = CircleShape,
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.BottomEnd)
            ) {
                RotateCwVectorIcon(size = 24.dp, tint = AccentCyan)
            }
        }
    }
}

/**
 * Tactile glass-beveled momentary action button with spring compression and specular top rim.
 */
@Composable
private fun SculptedActionPad(
    contentDescription: String,
    onClick: () -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier,
    topFill: Color = Color(0xFF242E46),
    bottomFill: Color = Color(0xFF131928),
    idleBorderAlpha: Float = 0.42f,
    shape: Shape = CircleShape,
    content: @Composable BoxScope.() -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val currentOnClick by rememberUpdatedState(onClick)

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.91f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "pad_scale"
    )

    val bgBrush = if (isPressed) {
        Brush.verticalGradient(listOf(accentColor.copy(alpha = 0.35f), bottomFill))
    } else {
        Brush.verticalGradient(listOf(topFill, bottomFill))
    }

    val borderBrush = if (isPressed) {
        Brush.verticalGradient(listOf(accentColor, accentColor.copy(alpha = 0.6f)))
    } else {
        Brush.verticalGradient(
            listOf(
                accentColor.copy(alpha = idleBorderAlpha),
                BorderCard.copy(alpha = 0.45f)
            )
        )
    }

    Box(
        modifier = modifier
            .scale(scale)
            .clip(shape)
            .background(bgBrush)
            .border(width = if (isPressed) 2.dp else 1.4.dp, brush = borderBrush, shape = shape)
            .semantics {
                this.contentDescription = contentDescription
                this.role = Role.Button
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown()
                    isPressed = true
                    currentOnClick()
                    try {
                        waitForUpOrCancellation()
                    } finally {
                        isPressed = false
                    }
                }
            },
        contentAlignment = Alignment.Center,
        content = content
    )
}

/**
 * Tactile glass-beveled DAS / ARR directional pad with spring compression.
 */
@Composable
private fun SculptedDasPad(
    contentDescription: String,
    onAction: () -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier,
    initialDelayMs: Long = 150L,
    repeatRateMs: Long = 38L,
    shape: Shape = CircleShape,
    content: @Composable BoxScope.() -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val currentOnAction by rememberUpdatedState(onAction)
    val currentDas by rememberUpdatedState(initialDelayMs)
    val currentArr by rememberUpdatedState(repeatRateMs)
    val coroutineScope = rememberCoroutineScope()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.91f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "das_pad_scale"
    )

    val bgBrush = if (isPressed) {
        Brush.verticalGradient(listOf(accentColor.copy(alpha = 0.30f), Color(0xFF141B2B)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFF253048), Color(0xFF151B2A)))
    }

    val borderBrush = if (isPressed) {
        Brush.verticalGradient(listOf(accentColor, accentColor.copy(alpha = 0.5f)))
    } else {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.22f),
                BorderCard.copy(alpha = 0.45f)
            )
        )
    }

    Box(
        modifier = modifier
            .scale(scale)
            .clip(shape)
            .background(bgBrush)
            .border(width = if (isPressed) 2.dp else 1.2.dp, brush = borderBrush, shape = shape)
            .semantics {
                this.contentDescription = contentDescription
                this.role = Role.Button
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown()
                    isPressed = true
                    currentOnAction()

                    val repeatJob = coroutineScope.launch {
                        if (currentDas > 0) {
                            delay(currentDas)
                        }
                        while (isActive) {
                            currentOnAction()
                            val stepDelay = if (currentArr <= 0) 10L else currentArr
                            delay(stepDelay)
                        }
                    }

                    try {
                        waitForUpOrCancellation()
                    } finally {
                        repeatJob.cancel()
                        isPressed = false
                    }
                }
            },
        contentAlignment = Alignment.Center,
        content = content
    )
}
