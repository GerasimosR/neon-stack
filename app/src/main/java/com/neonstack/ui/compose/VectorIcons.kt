package com.neonstack.ui.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.neonstack.engine.rogue.RelicId
import com.neonstack.ui.compose.theme.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TrophyVectorIcon(
    tint: Color = AccentYellow,
    size: Dp = 16.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val strokeW = w * 0.095f
        val glowStroke = Stroke(width = strokeW * 2.1f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val coreStroke = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)

        // Sleek geometric chalice body
        val cupPath = Path().apply {
            moveTo(w * 0.27f, h * 0.15f)
            lineTo(w * 0.73f, h * 0.15f)
            lineTo(w * 0.72f, h * 0.40f)
            cubicTo(w * 0.72f, h * 0.57f, w * 0.62f, h * 0.66f, w * 0.50f, h * 0.66f)
            cubicTo(w * 0.38f, h * 0.66f, w * 0.28f, h * 0.57f, w * 0.28f, h * 0.40f)
            close()
        }

        // Symmetrical side handles hugging the cup walls
        val leftHandle = Path().apply {
            moveTo(w * 0.27f, h * 0.23f)
            lineTo(w * 0.14f, h * 0.23f)
            cubicTo(w * 0.08f, h * 0.23f, w * 0.09f, h * 0.46f, w * 0.29f, h * 0.48f)
        }
        val rightHandle = Path().apply {
            moveTo(w * 0.73f, h * 0.23f)
            lineTo(w * 0.86f, h * 0.23f)
            cubicTo(w * 0.92f, h * 0.23f, w * 0.91f, h * 0.46f, w * 0.71f, h * 0.48f)
        }

        // Stem & pedestal base
        val basePath = Path().apply {
            moveTo(w * 0.50f, h * 0.66f)
            lineTo(w * 0.50f, h * 0.86f)
            moveTo(w * 0.30f, h * 0.86f)
            lineTo(w * 0.70f, h * 0.86f)
        }

        // 1. Translucent neon glass fill inside cup
        drawPath(cupPath, tint.copy(alpha = 0.20f))

        // 2. Soft outer neon tube halo
        val glowColor = tint.copy(alpha = 0.26f)
        drawPath(leftHandle, glowColor, style = glowStroke)
        drawPath(rightHandle, glowColor, style = glowStroke)
        drawPath(cupPath, glowColor, style = glowStroke)
        drawPath(basePath, glowColor, style = glowStroke)

        // 3. Crisp neon outline
        drawPath(leftHandle, tint, style = coreStroke)
        drawPath(rightHandle, tint, style = coreStroke)
        drawPath(cupPath, tint, style = coreStroke)
        drawPath(basePath, tint, style = coreStroke)

        // 4. Inner neon diamond gleam
        val gleam = Path().apply {
            moveTo(w * 0.50f, h * 0.29f)
            lineTo(w * 0.57f, h * 0.38f)
            lineTo(w * 0.50f, h * 0.47f)
            lineTo(w * 0.43f, h * 0.38f)
            close()
        }
        drawPath(gleam, Color.White.copy(alpha = 0.75f))
    }
}

@Composable
fun PauseVectorIcon(
    tint: Color = TextPrimary,
    size: Dp = 15.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val barW = w * 0.26f
        val radius = CornerRadius(barW * 0.4f, barW * 0.4f)

        // Left vertical pill
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.16f, h * 0.15f),
            size = Size(barW, h * 0.70f),
            cornerRadius = radius
        )
        // Right vertical pill
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.58f, h * 0.15f),
            size = Size(barW, h * 0.70f),
            cornerRadius = radius
        )
    }
}

@Composable
fun LightningVectorIcon(
    tint: Color = AccentPink,
    size: Dp = 14.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val strokeW = w * 0.09f
        val glowStroke = Stroke(width = strokeW * 2.1f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val coreStroke = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)

        val bolt = Path().apply {
            moveTo(w * 0.58f, h * 0.08f)
            lineTo(w * 0.20f, h * 0.54f)
            lineTo(w * 0.48f, h * 0.54f)
            lineTo(w * 0.42f, h * 0.92f)
            lineTo(w * 0.80f, h * 0.44f)
            lineTo(w * 0.52f, h * 0.44f)
            close()
        }

        // 1. Translucent neon fill
        drawPath(bolt, tint.copy(alpha = 0.24f))
        // 2. Soft outer neon glow halo
        drawPath(bolt, tint.copy(alpha = 0.28f), style = glowStroke)
        // 3. Crisp neon outline
        drawPath(bolt, tint, style = coreStroke)
        // 4. White-hot inner energy spine
        val coreFlash = Path().apply {
            moveTo(w * 0.53f, h * 0.28f)
            lineTo(w * 0.42f, h * 0.49f)
            lineTo(w * 0.56f, h * 0.49f)
            lineTo(w * 0.47f, h * 0.70f)
        }
        drawPath(
            path = coreFlash,
            color = Color.White.copy(alpha = 0.72f),
            style = Stroke(width = w * 0.055f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun HardDropSlamVectorIcon(
    tint: Color = AccentYellow,
    size: Dp = 16.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val strokeW = w * 0.13f
        val glowStroke = Stroke(width = strokeW * 2.0f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val coreStroke = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)

        // Upper trailing chevron (motion blur feel)
        val topChevron = Path().apply {
            moveTo(w * 0.28f, h * 0.15f)
            lineTo(w * 0.50f, h * 0.37f)
            lineTo(w * 0.72f, h * 0.15f)
        }

        // Primary downward slam chevron
        val bottomChevron = Path().apply {
            moveTo(w * 0.22f, h * 0.41f)
            lineTo(w * 0.50f, h * 0.67f)
            lineTo(w * 0.78f, h * 0.41f)
        }

        // Impact floor bar
        val impactStart = Offset(w * 0.20f, h * 0.85f)
        val impactEnd = Offset(w * 0.80f, h * 0.85f)

        // Soft outer neon glow
        val glowColor = tint.copy(alpha = 0.30f)
        drawPath(bottomChevron, glowColor, style = glowStroke)
        drawLine(glowColor, impactStart, impactEnd, strokeWidth = strokeW * 2.0f, cap = StrokeCap.Round)

        // Crisp neon vector strokes
        drawPath(topChevron, tint.copy(alpha = 0.70f), style = coreStroke)
        drawPath(bottomChevron, tint, style = coreStroke)
        drawLine(tint, impactStart, impactEnd, strokeWidth = strokeW, cap = StrokeCap.Round)

        // Subtle bright core highlight on impact bar
        drawLine(
            color = Color.White.copy(alpha = 0.65f),
            start = Offset(w * 0.32f, h * 0.85f),
            end = Offset(w * 0.68f, h * 0.85f),
            strokeWidth = strokeW * 0.45f,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun RotateCwVectorIcon(
    tint: Color = AccentCyan,
    size: Dp = 16.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val strokeW = w * 0.14f

        // Circular arc (270 degrees)
        drawArc(
            color = tint,
            startAngle = -60f,
            sweepAngle = 270f,
            useCenter = false,
            topLeft = Offset(w * 0.15f, h * 0.15f),
            size = Size(w * 0.70f, h * 0.70f),
            style = Stroke(width = strokeW, cap = StrokeCap.Round)
        )

        // Arrow head pointing clockwise
        val arrow = Path().apply {
            moveTo(w * 0.65f, h * 0.02f)
            lineTo(w * 0.88f, h * 0.22f)
            lineTo(w * 0.60f, h * 0.32f)
        }
        drawPath(arrow, tint, style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
fun RotateCcwVectorIcon(
    tint: Color = TextPrimary,
    size: Dp = 16.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val strokeW = w * 0.14f

        // Circular arc counter-clockwise
        drawArc(
            color = tint,
            startAngle = 240f,
            sweepAngle = -270f,
            useCenter = false,
            topLeft = Offset(w * 0.15f, h * 0.15f),
            size = Size(w * 0.70f, h * 0.70f),
            style = Stroke(width = strokeW, cap = StrokeCap.Round)
        )

        // Arrow head pointing counter-clockwise
        val arrow = Path().apply {
            moveTo(w * 0.35f, h * 0.02f)
            lineTo(w * 0.12f, h * 0.22f)
            lineTo(w * 0.40f, h * 0.32f)
        }
        drawPath(arrow, tint, style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
fun HoldCycleVectorIcon(
    tint: Color = TextPrimary,
    size: Dp = 14.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val strokeW = w * 0.13f

        // Top arc
        drawArc(
            color = tint,
            startAngle = 180f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(w * 0.15f, h * 0.15f),
            size = Size(w * 0.70f, h * 0.70f),
            style = Stroke(width = strokeW, cap = StrokeCap.Round)
        )
        // Bottom arc
        drawArc(
            color = tint,
            startAngle = 0f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(w * 0.15f, h * 0.15f),
            size = Size(w * 0.70f, h * 0.70f),
            style = Stroke(width = strokeW, cap = StrokeCap.Round)
        )

        // Top arrow head
        val arrow1 = Path().apply {
            moveTo(w * 0.78f, h * 0.14f)
            lineTo(w * 0.90f, h * 0.34f)
            lineTo(w * 0.66f, h * 0.34f)
        }
        drawPath(arrow1, tint, style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round))

        // Bottom arrow head
        val arrow2 = Path().apply {
            moveTo(w * 0.22f, h * 0.86f)
            lineTo(w * 0.10f, h * 0.66f)
            lineTo(w * 0.34f, h * 0.66f)
        }
        drawPath(arrow2, tint, style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
fun ChevronLeftVectorIcon(
    tint: Color = TextPrimary,
    size: Dp = 18.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val path = Path().apply {
            moveTo(w * 0.65f, h * 0.20f)
            lineTo(w * 0.32f, h * 0.50f)
            lineTo(w * 0.65f, h * 0.80f)
        }
        drawPath(
            path = path,
            color = tint,
            style = Stroke(width = w * 0.16f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun ChevronRightVectorIcon(
    tint: Color = TextPrimary,
    size: Dp = 18.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val path = Path().apply {
            moveTo(w * 0.35f, h * 0.20f)
            lineTo(w * 0.68f, h * 0.50f)
            lineTo(w * 0.35f, h * 0.80f)
        }
        drawPath(
            path = path,
            color = tint,
            style = Stroke(width = w * 0.16f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun ChevronDownVectorIcon(
    tint: Color = AccentYellow,
    size: Dp = 16.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val path = Path().apply {
            moveTo(w * 0.20f, h * 0.35f)
            lineTo(w * 0.50f, h * 0.68f)
            lineTo(w * 0.80f, h * 0.35f)
        }
        drawPath(
            path = path,
            color = tint,
            style = Stroke(width = w * 0.16f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun EndlessBlocksVectorIcon(
    tint: Color = AccentCyan,
    size: Dp = 22.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val cell = this.size.width * 0.28f
        val gap = this.size.width * 0.05f
        val r = CornerRadius(cell * 0.22f, cell * 0.22f)
        val startX = (this.size.width - (cell * 3 + gap * 2)) / 2f
        val startY = (this.size.height - (cell * 2 + gap)) / 2f

        // Stacked crown blocks: top-center + bottom 3
        val cells = listOf(
            Offset(startX + cell + gap, startY),
            Offset(startX, startY + cell + gap),
            Offset(startX + cell + gap, startY + cell + gap),
            Offset(startX + (cell + gap) * 2, startY + cell + gap)
        )
        cells.forEach { pos ->
            drawRoundRect(
                color = tint,
                topLeft = pos,
                size = Size(cell, cell),
                cornerRadius = r
            )
        }
    }
}

@Composable
fun SlidersVectorIcon(
    tint: Color = AccentPink,
    size: Dp = 18.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = w * 0.11f
        val knobR = w * 0.11f

        val rows = listOf(
            Pair(h * 0.22f, w * 0.35f),
            Pair(h * 0.50f, w * 0.68f),
            Pair(h * 0.78f, w * 0.42f)
        )
        rows.forEach { (y, knobX) ->
            drawLine(
                color = tint.copy(alpha = 0.55f),
                start = Offset(w * 0.10f, y),
                end = Offset(w * 0.90f, y),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
            drawCircle(
                color = tint,
                radius = knobR,
                center = Offset(knobX, y)
            )
        }
    }
}

@Composable
fun PlayTriangleVectorIcon(
    tint: Color = TextPrimary,
    size: Dp = 14.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val path = Path().apply {
            moveTo(w * 0.22f, h * 0.14f)
            lineTo(w * 0.86f, h * 0.50f)
            lineTo(w * 0.22f, h * 0.86f)
            close()
        }
        drawPath(path = path, color = tint)
    }
}

@Composable
fun StarVectorIcon(
    tint: Color = AccentYellow,
    size: Dp = 14.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val cx = this.size.width / 2f
        val cy = this.size.height / 2f
        val outerR = this.size.minDimension * 0.48f
        val innerR = outerR * 0.42f
        val path = Path()
        for (i in 0 until 10) {
            val angle = (-90.0 + i * 36.0) * PI / 180.0
            val r = if (i % 2 == 0) outerR else innerR
            val x = cx + (r * cos(angle)).toFloat()
            val y = cy + (r * sin(angle)).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        drawPath(path = path, color = tint)
    }
}

@Composable
fun MedalVectorIcon(
    rank: Int,
    size: Dp = 20.dp,
    modifier: Modifier = Modifier
) {
    val medalColor = when (rank) {
        1 -> AccentYellow
        2 -> Color(0xFFC8D6E5)
        else -> Color(0xFFE08A4E)
    }
    val ribbonColor = when (rank) {
        1 -> AccentRed
        2 -> AccentCyan
        else -> AccentPink
    }

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Left & right ribbon straps
        val leftRibbon = Path().apply {
            moveTo(w * 0.24f, h * 0.06f)
            lineTo(w * 0.46f, h * 0.06f)
            lineTo(w * 0.50f, h * 0.44f)
            lineTo(w * 0.34f, h * 0.44f)
            close()
        }
        val rightRibbon = Path().apply {
            moveTo(w * 0.54f, h * 0.06f)
            lineTo(w * 0.76f, h * 0.06f)
            lineTo(w * 0.66f, h * 0.44f)
            lineTo(w * 0.50f, h * 0.44f)
            close()
        }
        drawPath(leftRibbon, ribbonColor.copy(alpha = 0.85f))
        drawPath(rightRibbon, ribbonColor)

        // Medal disc
        val center = Offset(w * 0.5f, h * 0.65f)
        val radius = w * 0.29f
        drawCircle(color = medalColor, radius = radius, center = center)
        drawCircle(
            color = Color.White.copy(alpha = 0.35f),
            radius = radius * 0.72f,
            center = center,
            style = Stroke(width = w * 0.06f)
        )
    }
}

@Composable
fun SkullVectorIcon(
    tint: Color = AccentPink,
    size: Dp = 13.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Cranium
        drawOval(
            color = tint,
            topLeft = Offset(w * 0.14f, h * 0.08f),
            size = Size(w * 0.72f, h * 0.62f)
        )
        // Jaw
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.28f, h * 0.58f),
            size = Size(w * 0.44f, h * 0.32f),
            cornerRadius = CornerRadius(w * 0.08f, w * 0.08f)
        )
        // Eye sockets
        drawCircle(color = BgCard, radius = w * 0.10f, center = Offset(w * 0.36f, h * 0.40f))
        drawCircle(color = BgCard, radius = w * 0.10f, center = Offset(w * 0.64f, h * 0.40f))
        // Teeth slits
        val stroke = w * 0.07f
        drawLine(BgCard, Offset(w * 0.42f, h * 0.72f), Offset(w * 0.42f, h * 0.90f), strokeWidth = stroke)
        drawLine(BgCard, Offset(w * 0.58f, h * 0.72f), Offset(w * 0.58f, h * 0.90f), strokeWidth = stroke)
    }
}

@Composable
fun RelicVectorIcon(
    relicId: RelicId,
    tint: Color = AccentCyan,
    size: Dp = 20.dp,
    modifier: Modifier = Modifier
) {
    when (relicId) {
        RelicId.QUAD_SURGE -> LightningVectorIcon(tint = tint, size = size, modifier = modifier)
        RelicId.DOUBLE_HOLD -> HoldCycleVectorIcon(tint = tint, size = size, modifier = modifier)
        RelicId.MIDAS_TOUCH -> SparkleVectorIcon(tint = tint, size = size, modifier = modifier)
        RelicId.OVERHANG_DRILL -> DrillVectorIcon(tint = tint, size = size, modifier = modifier)
        RelicId.CHRONO_STABILIZER -> HourglassVectorIcon(tint = tint, size = size, modifier = modifier)
        RelicId.GOLDEN_I_BAG -> DiamondVectorIcon(tint = tint, size = size, modifier = modifier)
        RelicId.VAPORIZER_BOMB -> BombVectorIcon(tint = tint, size = size, modifier = modifier)
        RelicId.LASER_SWEEP -> LaserVectorIcon(tint = tint, size = size, modifier = modifier)
        RelicId.GRAVITY_ANCHOR -> AnchorVectorIcon(tint = tint, size = size, modifier = modifier)
    }
}

@Composable
private fun SparkleVectorIcon(tint: Color, size: Dp, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val mainStar = Path().apply {
            moveTo(w * 0.50f, h * 0.06f)
            quadraticTo(w * 0.54f, h * 0.46f, w * 0.94f, h * 0.50f)
            quadraticTo(w * 0.54f, h * 0.54f, w * 0.50f, h * 0.94f)
            quadraticTo(w * 0.46f, h * 0.54f, w * 0.06f, h * 0.50f)
            quadraticTo(w * 0.46f, h * 0.46f, w * 0.50f, h * 0.06f)
            close()
        }
        drawPath(mainStar, tint)
        drawCircle(tint.copy(alpha = 0.7f), radius = w * 0.07f, center = Offset(w * 0.22f, h * 0.22f))
        drawCircle(tint.copy(alpha = 0.7f), radius = w * 0.055f, center = Offset(w * 0.80f, h * 0.78f))
    }
}

@Composable
private fun DrillVectorIcon(tint: Color, size: Dp, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        // Top shank
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.30f, h * 0.08f),
            size = Size(w * 0.40f, h * 0.20f),
            cornerRadius = CornerRadius(w * 0.06f, w * 0.06f)
        )
        // Cone bit
        val cone = Path().apply {
            moveTo(w * 0.24f, h * 0.32f)
            lineTo(w * 0.76f, h * 0.32f)
            lineTo(w * 0.50f, h * 0.94f)
            close()
        }
        drawPath(cone, tint)
        // Spiral flutes
        val stroke = w * 0.08f
        drawLine(BgMain, Offset(w * 0.30f, h * 0.46f), Offset(w * 0.68f, h * 0.38f), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(BgMain, Offset(w * 0.37f, h * 0.66f), Offset(w * 0.62f, h * 0.58f), strokeWidth = stroke, cap = StrokeCap.Round)
    }
}

@Composable
private fun HourglassVectorIcon(tint: Color, size: Dp, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = w * 0.11f
        // Top and bottom caps
        drawLine(tint, Offset(w * 0.20f, h * 0.12f), Offset(w * 0.80f, h * 0.12f), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(tint, Offset(w * 0.20f, h * 0.88f), Offset(w * 0.80f, h * 0.88f), strokeWidth = stroke, cap = StrokeCap.Round)
        // Glass silhouette
        val glass = Path().apply {
            moveTo(w * 0.26f, h * 0.14f)
            lineTo(w * 0.74f, h * 0.14f)
            lineTo(w * 0.54f, h * 0.50f)
            lineTo(w * 0.74f, h * 0.86f)
            lineTo(w * 0.26f, h * 0.86f)
            lineTo(w * 0.46f, h * 0.50f)
            close()
        }
        drawPath(glass, tint.copy(alpha = 0.25f))
        drawPath(glass, tint, style = Stroke(width = w * 0.09f, join = StrokeJoin.Round))
    }
}

@Composable
private fun DiamondVectorIcon(tint: Color, size: Dp, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val gem = Path().apply {
            moveTo(w * 0.26f, h * 0.16f)
            lineTo(w * 0.74f, h * 0.16f)
            lineTo(w * 0.92f, h * 0.42f)
            lineTo(w * 0.50f, h * 0.90f)
            lineTo(w * 0.08f, h * 0.42f)
            close()
        }
        drawPath(gem, tint)
        // Facet lines
        val facetColor = Color.White.copy(alpha = 0.45f)
        val stroke = w * 0.06f
        drawLine(facetColor, Offset(w * 0.08f, h * 0.42f), Offset(w * 0.92f, h * 0.42f), strokeWidth = stroke)
        drawLine(facetColor, Offset(w * 0.34f, h * 0.42f), Offset(w * 0.50f, h * 0.90f), strokeWidth = stroke)
        drawLine(facetColor, Offset(w * 0.66f, h * 0.42f), Offset(w * 0.50f, h * 0.90f), strokeWidth = stroke)
    }
}

@Composable
private fun BombVectorIcon(tint: Color, size: Dp, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        // Bomb sphere
        drawCircle(color = tint, radius = w * 0.32f, center = Offset(w * 0.45f, h * 0.60f))
        // Specular highlight
        drawCircle(color = Color.White.copy(alpha = 0.35f), radius = w * 0.09f, center = Offset(w * 0.34f, h * 0.50f))
        // Fuse stem
        val fuse = Path().apply {
            moveTo(w * 0.60f, h * 0.34f)
            quadraticTo(w * 0.72f, h * 0.16f, w * 0.84f, h * 0.22f)
        }
        drawPath(fuse, tint, style = Stroke(width = w * 0.09f, cap = StrokeCap.Round))
        // Spark tip
        drawCircle(color = AccentYellow, radius = w * 0.08f, center = Offset(w * 0.86f, h * 0.22f))
    }
}

@Composable
private fun LaserVectorIcon(tint: Color, size: Dp, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        // Emitter core
        drawCircle(color = tint, radius = w * 0.16f, center = Offset(w * 0.22f, h * 0.50f))
        // Horizontal laser beam
        drawLine(
            color = tint,
            start = Offset(w * 0.32f, h * 0.50f),
            end = Offset(w * 0.92f, h * 0.50f),
            strokeWidth = w * 0.13f,
            cap = StrokeCap.Round
        )
        // Energy flare lines
        val stroke = w * 0.08f
        drawLine(tint.copy(alpha = 0.65f), Offset(w * 0.56f, h * 0.26f), Offset(w * 0.86f, h * 0.18f), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(tint.copy(alpha = 0.65f), Offset(w * 0.56f, h * 0.74f), Offset(w * 0.86f, h * 0.82f), strokeWidth = stroke, cap = StrokeCap.Round)
    }
}

@Composable
private fun AnchorVectorIcon(tint: Color, size: Dp, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = w * 0.11f
        // Top ring
        drawCircle(
            color = tint,
            radius = w * 0.12f,
            center = Offset(w * 0.50f, h * 0.20f),
            style = Stroke(width = stroke)
        )
        // Vertical shank
        drawLine(tint, Offset(w * 0.50f, h * 0.32f), Offset(w * 0.50f, h * 0.84f), strokeWidth = stroke, cap = StrokeCap.Round)
        // Crossbar
        drawLine(tint, Offset(w * 0.30f, h * 0.44f), Offset(w * 0.70f, h * 0.44f), strokeWidth = stroke, cap = StrokeCap.Round)
        // Curved flukes
        drawArc(
            color = tint,
            startAngle = 15f,
            sweepAngle = 150f,
            useCenter = false,
            topLeft = Offset(w * 0.16f, h * 0.36f),
            size = Size(w * 0.68f, h * 0.50f),
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun SettingsGearVectorIcon(
    tint: Color = TextPrimary,
    size: Dp = 20.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val cx = this.size.width / 2f
        val cy = this.size.height / 2f
        val outerR = this.size.minDimension * 0.44f
        val innerR = this.size.minDimension * 0.30f
        val strokeW = this.size.minDimension * 0.11f

        // 8 radial gear teeth
        for (i in 0 until 8) {
            val angle = (i * 45.0) * PI / 180.0
            val cosA = cos(angle).toFloat()
            val sinA = sin(angle).toFloat()
            drawLine(
                color = tint,
                start = Offset(cx + cosA * innerR, cy + sinA * innerR),
                end = Offset(cx + cosA * outerR, cy + sinA * outerR),
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )
        }
        // Gear ring body
        drawCircle(
            color = tint,
            radius = this.size.minDimension * 0.28f,
            center = Offset(cx, cy),
            style = Stroke(width = strokeW)
        )
        // Center axle dot
        drawCircle(
            color = tint.copy(alpha = 0.7f),
            radius = this.size.minDimension * 0.08f,
            center = Offset(cx, cy)
        )
    }
}

@Composable
fun CloseVectorIcon(
    tint: Color = TextSecondary,
    size: Dp = 18.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val strokeW = w * 0.14f
        drawLine(
            color = tint,
            start = Offset(w * 0.22f, h * 0.22f),
            end = Offset(w * 0.78f, h * 0.78f),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = Offset(w * 0.78f, h * 0.22f),
            end = Offset(w * 0.22f, h * 0.78f),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun HeroBrandEmblemIcon(
    size: Dp = 44.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cell = w * 0.26f
        val gap = w * 0.05f
        val total = cell * 3 + gap * 2
        val startX = (w - total) / 2f
        val startY = (h - total) / 2f
        val r = CornerRadius(cell * 0.22f, cell * 0.22f)

        // 1. Holographic Puzzle Reticle at Slot (col 2, row 1)
        val slotX = startX + 2 * (cell + gap)
        val slotY = startY + 1 * (cell + gap)
        val bracketLen = cell * 0.28f
        val reticleStroke = Stroke(width = cell * 0.09f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val reticleColor = AccentCyan.copy(alpha = 0.45f)

        val tl = Path().apply {
            moveTo(slotX, slotY + bracketLen)
            lineTo(slotX, slotY)
            lineTo(slotX + bracketLen, slotY)
        }
        drawPath(tl, reticleColor, style = reticleStroke)

        val tr = Path().apply {
            moveTo(slotX + cell - bracketLen, slotY)
            lineTo(slotX + cell, slotY)
            lineTo(slotX + cell, slotY + bracketLen)
        }
        drawPath(tr, reticleColor, style = reticleStroke)

        val bl = Path().apply {
            moveTo(slotX, slotY + cell - bracketLen)
            lineTo(slotX, slotY + cell)
            lineTo(slotX + bracketLen, slotY + cell)
        }
        drawPath(bl, reticleColor, style = reticleStroke)

        val br = Path().apply {
            moveTo(slotX + cell - bracketLen, slotY + cell)
            lineTo(slotX + cell, slotY + cell)
            lineTo(slotX + cell, slotY + cell - bracketLen)
        }
        drawPath(br, reticleColor, style = reticleStroke)

        val centerSlotX = slotX + cell * 0.5f
        val centerSlotY = slotY + cell * 0.5f
        drawLine(
            color = reticleColor.copy(alpha = 0.35f),
            start = Offset(centerSlotX, centerSlotY - cell * 0.15f),
            end = Offset(centerSlotX, centerSlotY + cell * 0.15f),
            strokeWidth = cell * 0.08f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = reticleColor.copy(alpha = 0.35f),
            start = Offset(centerSlotX - cell * 0.15f, centerSlotY),
            end = Offset(centerSlotX + cell * 0.15f, centerSlotY),
            strokeWidth = cell * 0.08f,
            cap = StrokeCap.Round
        )

        // Polyomino cell coordinates
        val cyanCells = listOf(Pair(0, 0), Pair(0, 1), Pair(0, 2), Pair(1, 2))
        val magCells = listOf(Pair(1, 0), Pair(2, 0), Pair(1, 1))
        val goldCells = listOf(Pair(2, 2))

        // 2. Diffuse Ambient Outer Bloom (Stroke)
        val outerBloomStroke = Stroke(width = cell * 0.35f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        cyanCells.forEach { (col, row) ->
            val x = startX + col * (cell + gap)
            val y = startY + row * (cell + gap)
            drawRoundRect(color = AccentCyan.copy(alpha = 0.18f), topLeft = Offset(x, y), size = Size(cell, cell), cornerRadius = r, style = outerBloomStroke)
        }
        magCells.forEach { (col, row) ->
            val x = startX + col * (cell + gap)
            val y = startY + row * (cell + gap)
            drawRoundRect(color = Color(0xFFE040FB).copy(alpha = 0.18f), topLeft = Offset(x, y), size = Size(cell, cell), cornerRadius = r, style = outerBloomStroke)
        }
        goldCells.forEach { (col, row) ->
            val x = startX + col * (cell + gap)
            val y = startY + row * (cell + gap)
            drawRoundRect(color = AccentYellow.copy(alpha = 0.22f), topLeft = Offset(x, y), size = Size(cell, cell), cornerRadius = r, style = outerBloomStroke)
        }

        // 3. Concentrated Mid Bloom (Stroke)
        val midBloomStroke = Stroke(width = cell * 0.16f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        cyanCells.forEach { (col, row) ->
            val x = startX + col * (cell + gap)
            val y = startY + row * (cell + gap)
            drawRoundRect(color = AccentCyan.copy(alpha = 0.38f), topLeft = Offset(x, y), size = Size(cell, cell), cornerRadius = r, style = midBloomStroke)
        }
        magCells.forEach { (col, row) ->
            val x = startX + col * (cell + gap)
            val y = startY + row * (cell + gap)
            drawRoundRect(color = Color(0xFFE040FB).copy(alpha = 0.38f), topLeft = Offset(x, y), size = Size(cell, cell), cornerRadius = r, style = midBloomStroke)
        }
        goldCells.forEach { (col, row) ->
            val x = startX + col * (cell + gap)
            val y = startY + row * (cell + gap)
            drawRoundRect(color = AccentYellow.copy(alpha = 0.42f), topLeft = Offset(x, y), size = Size(cell, cell), cornerRadius = r, style = midBloomStroke)
        }

        // 4. Vibrant Gradient Cores
        cyanCells.forEach { (col, row) ->
            val x = startX + col * (cell + gap)
            val y = startY + row * (cell + gap)
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF22EEFF), Color(0xFF008CAE)),
                    start = Offset(x, y),
                    end = Offset(x + cell, y + cell)
                ),
                topLeft = Offset(x, y),
                size = Size(cell, cell),
                cornerRadius = r
            )
        }
        magCells.forEach { (col, row) ->
            val x = startX + col * (cell + gap)
            val y = startY + row * (cell + gap)
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFF355FF), Color(0xFF9C00B8)),
                    start = Offset(x, y),
                    end = Offset(x + cell, y + cell)
                ),
                topLeft = Offset(x, y),
                size = Size(cell, cell),
                cornerRadius = r
            )
        }
        goldCells.forEach { (col, row) ->
            val x = startX + col * (cell + gap)
            val y = startY + row * (cell + gap)
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFFFF066), Color(0xFFC99A00)),
                    start = Offset(x, y),
                    end = Offset(x + cell, y + cell)
                ),
                topLeft = Offset(x, y),
                size = Size(cell, cell),
                cornerRadius = r
            )
        }

        // 5. Crystal Facet Inset Rim
        val facetStroke = Stroke(width = cell * 0.06f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val allCells = cyanCells + magCells + goldCells
        allCells.forEach { (col, row) ->
            val x = startX + col * (cell + gap)
            val y = startY + row * (cell + gap)
            val inset = cell * 0.08f
            drawRoundRect(
                color = Color.White.copy(alpha = 0.22f),
                topLeft = Offset(x + inset, y + inset),
                size = Size(cell - inset * 2, cell - inset * 2),
                cornerRadius = CornerRadius(r.x * 0.8f, r.y * 0.8f),
                style = facetStroke
            )
        }

        // 6. White-Hot Phosphor Gas Filaments
        allCells.forEach { (col, row) ->
            val x = startX + col * (cell + gap)
            val y = startY + row * (cell + gap)
            drawRoundRect(
                color = Color.White.copy(alpha = 0.80f),
                topLeft = Offset(x + cell * 0.18f, y + cell * 0.16f),
                size = Size(cell * 0.50f, cell * 0.16f),
                cornerRadius = CornerRadius(cell * 0.08f, cell * 0.08f)
            )
        }

        // 7. Midas Keystone Diamond Sparkle at (col 2, row 2)
        val goldX = startX + 2 * (cell + gap) + cell * 0.5f
        val goldY = startY + 2 * (cell + gap) + cell * 0.5f
        val sparkH = cell * 0.24f
        val sparkW = cell * 0.09f
        val diamond = Path().apply {
            moveTo(goldX, goldY - sparkH)
            lineTo(goldX + sparkW, goldY)
            lineTo(goldX, goldY + sparkH)
            lineTo(goldX - sparkW, goldY)
            close()
        }
        val diamondCross = Path().apply {
            moveTo(goldX - sparkH, goldY)
            lineTo(goldX, goldY + sparkW)
            lineTo(goldX + sparkH, goldY)
            lineTo(goldX, goldY - sparkW)
            close()
        }
        drawPath(diamond, Color.White.copy(alpha = 0.95f))
        drawPath(diamondCross, Color.White.copy(alpha = 0.80f))
        drawCircle(Color.White, radius = cell * 0.04f, center = Offset(goldX, goldY))
    }
}
