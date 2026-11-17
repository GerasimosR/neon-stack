package com.seniordev.neonstack.ui

import android.content.Context
import android.graphics.*
import android.os.SystemClock
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import com.seniordev.neonstack.engine.*
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

class NeonStackCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var engine: NeonStackEngine? = null

    var isFullScreenGestureMode: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                if (width > 0 && height > 0) {
                    calculateLayout(width, height)
                    invalidate()
                }
            }
        }

    // Touch gesture callback interface
    interface GameGestureListener {
        fun onSwipeLeft()
        fun onSwipeRight()
        fun onSwipeDown()
        fun onHardDrop()
        fun onTapRotateCw()
        fun onDoubleTapRotateCcw()
        fun onHold()
    }

    var gestureListener: GameGestureListener? = null

    // Paints
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#0C0E14")
    }
    private val boardBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#121622")
    }
    private val boardAmbientPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val gridLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1B2236")
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }
    private val boardBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#38486E")
        strokeWidth = 4f
        style = Paint.Style.STROKE
    }
    private val blockPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val blockHighlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(80, 255, 255, 255)
        style = Paint.Style.FILL
    }
    private val blockShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(80, 0, 0, 0)
        style = Paint.Style.FILL
    }
    private val blockInnerFacetPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val ghostPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }
    private val ghostFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val panelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#141926")
        style = Paint.Style.FILL
    }
    private val panelBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#252E46")
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }
    private val emptySlotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#252E46")
        style = Paint.Style.STROKE
        strokeWidth = 2.5f
        pathEffect = DashPathEffect(floatArrayOf(10f, 8f), 0f)
    }
    private val textHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#7A8BAF")
        textSize = 28f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    private val floatingTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFD700")
        textSize = 42f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        setShadowLayer(8f, 0f, 0f, Color.parseColor("#FFA000"))
    }
    private val particlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    // Dynamic Layout Measurements
    private val boardRect = RectF()
    private val holdRect = RectF()
    private val nextRect = RectF()
    private var cellSize = 0f

    // Next-Gen AGSL Runtime Shaders (Android 13+)
    private val shaderManager = com.seniordev.neonstack.ui.shaders.AgslShaderManager(this)

    var isCrtFilterEnabled: Boolean
        get() = shaderManager.isCrtEnabled
        set(value) {
            shaderManager.isCrtEnabled = value
        }

    // Visual FX: Screen Shake
    private var shakeMagnitude = 0f
    private var shakeDecayTime = 0L

    // Visual FX: Floating Banner Texts
    private data class FloatingBanner(
        val text: String,
        val startTime: Long,
        val durationMs: Long = 900L,
        var yOffset: Float = 0f
    )
    private val floatingBanners = mutableListOf<FloatingBanner>()

    // Visual FX: Line clear flashes
    private data class LineClearAnim(val rows: List<Int>, val startTime: Long, val durationMs: Long = 200L)
    private var activeLineClearAnim: LineClearAnim? = null

    // Visual FX: Particle System
    private data class SparkParticle(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        val color: Int,
        var alpha: Float = 1f,
        val size: Float
    )
    private val particles = mutableListOf<SparkParticle>()

    // Live 1:1 Touch & Gesture Tracking State
    private var velocityTracker: VelocityTracker? = null
    private var downX = 0f
    private var downY = 0f
    private var dragAnchorX = 0f
    private var dragAnchorY = 0f
    private var hasDraggedHorizontal = false
    private var hasDraggedVertical = false
    private var hardDroppedInGesture = false

    init {
        isClickable = true
        isFocusable = true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val density = resources.displayMetrics.density
        val x = event.x
        val y = event.y

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                parent?.requestDisallowInterceptTouchEvent(true)
                velocityTracker?.recycle()
                velocityTracker = VelocityTracker.obtain().apply { addMovement(event) }
                downX = x
                downY = y
                dragAnchorX = x
                dragAnchorY = y
                hasDraggedHorizontal = false
                hasDraggedVertical = false
                hardDroppedInGesture = false
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (hardDroppedInGesture) return true
                velocityTracker?.addMovement(event)
                velocityTracker?.computeCurrentVelocity(1000)
                val vy = velocityTracker?.yVelocity ?: 0f

                val baseCell = if (cellSize > 0f) cellSize else 28f * density
                val stepX = (baseCell * 0.55f).coerceIn(14f * density, 24f * density)
                val stepY = (baseCell * 0.65f).coerceIn(16f * density, 26f * density)

                // 1:1 horizontal finger-tracking slide
                var movedHorizontallyNow = false
                while (abs(x - dragAnchorX) >= stepX) {
                    if (x > dragAnchorX) {
                        gestureListener?.onSwipeRight()
                        dragAnchorX += stepX
                    } else {
                        gestureListener?.onSwipeLeft()
                        dragAnchorX -= stepX
                    }
                    hasDraggedHorizontal = true
                    movedHorizontallyNow = true
                    dragAnchorY = y
                }
                if (movedHorizontallyNow) {
                    invalidate()
                }

                // Vertical soft-drop drag & high-velocity downward flick slam
                val totalDy = y - downY
                val totalDx = x - downX
                if (!movedHorizontallyNow && y - dragAnchorY >= stepY && totalDy > abs(totalDx) * 0.9f) {
                    if (!hasDraggedHorizontal && vy > 1700f * density && totalDy > baseCell * 1.3f) {
                        gestureListener?.onHardDrop()
                        hardDroppedInGesture = true
                        hasDraggedVertical = true
                        invalidate()
                    } else {
                        while (y - dragAnchorY >= stepY) {
                            gestureListener?.onSwipeDown()
                            dragAnchorY += stepY
                            hasDraggedVertical = true
                        }
                        invalidate()
                    }
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                parent?.requestDisallowInterceptTouchEvent(false)
                velocityTracker?.addMovement(event)
                velocityTracker?.computeCurrentVelocity(1000)
                val vy = velocityTracker?.yVelocity ?: 0f
                val totalDy = y - downY
                val totalDx = x - downX
                val hasDragged = hasDraggedHorizontal || hasDraggedVertical
                val tapSlop = 11f * density
                val quickSwipeSlop = 12f * density
                val baseCell = if (cellSize > 0f) cellSize else 28f * density

                if (!hardDroppedInGesture &&
                    !hasDraggedHorizontal &&
                    totalDy > baseCell * 0.85f &&
                    vy > 1150f * density &&
                    totalDy > abs(totalDx) * 1.25f
                ) {
                    // Quick downward flick = Hard Drop
                    gestureListener?.onHardDrop()
                    invalidate()
                } else if (!hasDragged &&
                    abs(totalDx) >= quickSwipeSlop &&
                    abs(totalDx) > abs(totalDy) * 1.1f
                ) {
                    // Quick horizontal flick left/right (short thumb swipe)
                    if (totalDx > 0f) {
                        gestureListener?.onSwipeRight()
                    } else {
                        gestureListener?.onSwipeLeft()
                    }
                    invalidate()
                } else if (!hasDragged &&
                    totalDy >= quickSwipeSlop &&
                    totalDy > abs(totalDx) * 1.2f
                ) {
                    // Short downward swipe = Soft Drop
                    gestureListener?.onSwipeDown()
                    invalidate()
                } else if (!hasDragged && hypot(totalDx.toDouble(), totalDy.toDouble()) <= tapSlop) {
                    // Instant 0ms tap detection
                    val holdTouchSlop = 12f * density
                    val hitHold = x >= (holdRect.left - holdTouchSlop) &&
                        x <= (holdRect.right + holdTouchSlop) &&
                        y >= (holdRect.top - holdTouchSlop) &&
                        y <= (holdRect.bottom + holdTouchSlop)

                    if (hitHold) {
                        gestureListener?.onHold()
                    } else if (x < width * 0.35f) {
                        gestureListener?.onDoubleTapRotateCcw()
                    } else {
                        gestureListener?.onTapRotateCw()
                    }
                    invalidate()
                }

                velocityTracker?.recycle()
                velocityTracker = null
                return true
            }

            MotionEvent.ACTION_CANCEL -> {
                parent?.requestDisallowInterceptTouchEvent(false)
                velocityTracker?.recycle()
                velocityTracker = null
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        calculateLayout(w, h)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q && w > 0 && h > 0) {
            val edgeWidth = (36f * resources.displayMetrics.density).toInt()
            val maxExclusionHeight = (190f * resources.displayMetrics.density).toInt().coerceAtMost(h)
            val top = ((h - maxExclusionHeight) / 2).coerceAtLeast(0)
            systemGestureExclusionRects = listOf(
                Rect(0, top, edgeWidth, top + maxExclusionHeight),
                Rect((w - edgeWidth).coerceAtLeast(0), top, w, top + maxExclusionHeight)
            )
        }
    }

    private fun calculateLayout(viewWidth: Int, viewHeight: Int) {
        val horizontalPadding = 6f
        val verticalPadding = 8f
        val panelGap = 10f

        if (isFullScreenGestureMode) {
            // Full-Screen Gesture Layout:
            // Compact horizontal HOLD + NEXT tray at top, massive 10x18 matrix below!
            val topTrayHeight = (viewHeight * 0.10f).coerceIn(112f, 165f)
            val trayBottom = verticalPadding + topTrayHeight
            val boardAreaTop = trayBottom + panelGap
            val boardAreaH = (viewHeight - boardAreaTop - verticalPadding).coerceAtLeast(100f)

            val maxCellW = (viewWidth - horizontalPadding * 2) / Board.WIDTH.toFloat()
            val maxCellH = boardAreaH / Board.VISIBLE_ROWS.toFloat()
            cellSize = minOf(maxCellW, maxCellH)
            textHeaderPaint.textSize = (cellSize * 0.34f).coerceIn(20f, 28f)

            val boardW = cellSize * Board.WIDTH
            val boardH = cellSize * Board.VISIBLE_ROWS
            val boardLeft = (viewWidth - boardW) / 2f
            val boardTop = boardAreaTop + (boardAreaH - boardH) / 2f
            boardRect.set(boardLeft, boardTop, boardLeft + boardW, boardTop + boardH)

            // Align top tray neatly to board width
            val trayWidth = maxOf(boardW, viewWidth - horizontalPadding * 2 - 8f)
            val trayLeft = (viewWidth - trayWidth) / 2f
            val holdW = trayWidth * 0.28f
            holdRect.set(trayLeft, verticalPadding, trayLeft + holdW, trayBottom)
            nextRect.set(holdRect.right + panelGap, verticalPadding, trayLeft + trayWidth, trayBottom)
        } else {
            // Symmetrical Neo-Arcade Pad Layout: HOLD | BOARD | NEXT
            val targetWidthUnits = 15.6f
            val targetHeightUnits = Board.VISIBLE_ROWS + 0.3f

            val maxCellW = (viewWidth - horizontalPadding * 2 - panelGap * 2) / targetWidthUnits
            val maxCellH = (viewHeight - verticalPadding * 2) / targetHeightUnits

            cellSize = minOf(maxCellW, maxCellH)
            textHeaderPaint.textSize = (cellSize * 0.42f).coerceIn(22f, 32f)

            val boardW = cellSize * Board.WIDTH
            val boardH = cellSize * Board.VISIBLE_ROWS

            val boardLeft = (viewWidth - boardW) / 2f
            val boardTop = maxOf(verticalPadding, (viewHeight - boardH) / 2f)
            boardRect.set(boardLeft, boardTop, boardLeft + boardW, boardTop + boardH)

            val availableSideW = boardLeft - horizontalPadding - panelGap
            val sidePanelW = minOf(cellSize * 2.65f, availableSideW).coerceAtLeast(cellSize * 2.1f)
            val sidePanelH = cellSize * 3.1f

            val holdLeft = maxOf(horizontalPadding, boardLeft - sidePanelW - panelGap)
            holdRect.set(holdLeft, boardTop, holdLeft + sidePanelW, boardTop + sidePanelH)

            val nextLeft = minOf(viewWidth - horizontalPadding - sidePanelW, boardRect.right + panelGap)
            val nextH = minOf(cellSize * 8.4f, boardH)
            nextRect.set(nextLeft, boardTop, nextLeft + sidePanelW, boardTop + nextH)
        }

        // Radial ambient cyber glow behind playfield
        val cx = boardRect.centerX()
        val cy = boardRect.centerY()
        val radius = maxOf(boardRect.width(), boardRect.height()) * 0.72f
        boardAmbientPaint.shader = RadialGradient(
            cx, cy, radius,
            intArrayOf(
                Color.parseColor("#18243C"),
                Color.parseColor("#121826"),
                Color.parseColor("#0C0E14")
            ),
            floatArrayOf(0.0f, 0.55f, 1.0f),
            Shader.TileMode.CLAMP
        )
    }

    // Triggers
    fun triggerScreenShake(magnitude: Float = 12f) {
        shakeMagnitude = magnitude
        shakeDecayTime = SystemClock.uptimeMillis() + 150
        if (magnitude >= 12f) {
            shaderManager.triggerChromaticAberration()
        }
        invalidate()
    }

    fun showFloatingBanner(text: String) {
        floatingBanners.add(FloatingBanner(text, SystemClock.uptimeMillis()))
        invalidate()
    }

    fun triggerLineClearAnimation(rows: List<Int>) {
        activeLineClearAnim = LineClearAnim(rows, SystemClock.uptimeMillis())

        if (rows.size >= 4) {
            shaderManager.triggerShockwave(boardRect.centerX(), boardRect.centerY())
        }

        // Spawn particles along cleared rows
        val eng = engine ?: return
        for (row in rows) {
            val visibleRow = row - Board.BUFFER_ROWS
            if (visibleRow < 0) continue
            val centerY = boardRect.top + visibleRow * cellSize + cellSize / 2f

            for (i in 0 until 18) {
                val cx = boardRect.left + (i.toFloat() / 18f) * boardRect.width()
                val angle = Random.nextDouble(0.0, Math.PI * 2)
                val speed = Random.nextFloat() * 220f + 80f
                val vx = (cos(angle) * speed).toFloat()
                val vy = (sin(angle) * speed).toFloat()
                val colors = intArrayOf(
                    Color.parseColor("#2EFFC5"),
                    Color.parseColor("#FF2EE6"),
                    Color.parseColor("#FFB703"),
                    Color.parseColor("#38BDF8"),
                    Color.parseColor("#FFFFFF")
                )
                val c = colors[Random.nextInt(colors.size)]
                particles.add(SparkParticle(cx, centerY, vx, vy, c, 1f, Random.nextFloat() * 6f + 3f))
            }
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        shaderManager.update()
        val eng = engine ?: return
        val now = SystemClock.uptimeMillis()

        // Handle screen shake
        var shakeOffsetX = 0f
        var shakeOffsetY = 0f
        if (now < shakeDecayTime && shakeMagnitude > 0f) {
            val factor = (shakeDecayTime - now).toFloat() / 150f
            shakeOffsetX = (Random.nextFloat() * 2f - 1f) * shakeMagnitude * factor
            shakeOffsetY = (Random.nextFloat() * 2f - 1f) * shakeMagnitude * factor
            invalidate()
        }

        canvas.save()
        canvas.translate(shakeOffsetX, shakeOffsetY)

        // Draw Canvas Background
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)

        // Draw Hold Box
        drawHoldPanel(canvas, eng)

        // Draw Next Queue Box
        drawNextPanel(canvas, eng)

        // Draw Playfield Matrix
        drawPlayfield(canvas, eng, now)

        // Draw Floating Banners
        drawFloatingBanners(canvas, now)

        // Draw Particles
        drawParticles(canvas, now)

        canvas.restore()

        // Game loop auto-refresh
        if (eng.status == GameStatus.RUNNING || particles.isNotEmpty() || floatingBanners.isNotEmpty()) {
            postInvalidateOnAnimation()
        }
    }

    private fun drawPlayfield(canvas: Canvas, eng: NeonStackEngine, now: Long) {
        // Draw board background with radial ambient cyber glow
        canvas.drawRoundRect(boardRect, 8f, 8f, boardAmbientPaint)
        canvas.drawRoundRect(boardRect, 8f, 8f, boardBorderPaint)

        // Draw grid lines
        for (col in 1 until Board.WIDTH) {
            val x = boardRect.left + col * cellSize
            canvas.drawLine(x, boardRect.top, x, boardRect.bottom, gridLinePaint)
        }
        for (row in 1 until Board.VISIBLE_ROWS) {
            val y = boardRect.top + row * cellSize
            canvas.drawLine(boardRect.left, y, boardRect.right, y, gridLinePaint)
        }

        // Draw locked blocks
        val grid = eng.board.grid
        for (y in Board.BUFFER_ROWS until Board.TOTAL_ROWS) {
            val screenRow = y - Board.BUFFER_ROWS
            for (x in 0 until Board.WIDTH) {
                val blockId = grid[y][x]
                if (blockId > 0) {
                    val blockShape = BlockShape.fromId(blockId)
                    val color = blockShape?.colorHex?.toInt() ?: Color.GRAY
                    drawMino(canvas, boardRect.left + x * cellSize, boardRect.top + screenRow * cellSize, cellSize, color)
                }
            }
        }

        // Draw Ghost Piece
        val currentPiece = eng.currentPiece
        if (currentPiece != null && eng.status == GameStatus.RUNNING) {
            val dropPos = eng.getGhostPiecePosition()
            if (dropPos != null && dropPos.y != currentPiece.position.y) {
                val ghostMinos = currentPiece.getWorldMinos(dropPos, currentPiece.rotation)
                val ghostColor = currentPiece.type.colorHex.toInt()
                for (mino in ghostMinos) {
                    val screenRow = mino.y - Board.BUFFER_ROWS
                    if (screenRow in 0 until Board.VISIBLE_ROWS) {
                        drawGhostMino(
                            canvas,
                            boardRect.left + mino.x * cellSize,
                            boardRect.top + screenRow * cellSize,
                            cellSize,
                            ghostColor
                        )
                    }
                }
            }

            // Draw Active Piece
            val activeMinos = currentPiece.getWorldMinos()
            val pieceColor = currentPiece.type.colorHex.toInt()
            for (mino in activeMinos) {
                val screenRow = mino.y - Board.BUFFER_ROWS
                if (screenRow in 0 until Board.VISIBLE_ROWS) {
                    drawMino(
                        canvas,
                        boardRect.left + mino.x * cellSize,
                        boardRect.top + screenRow * cellSize,
                        cellSize,
                        pieceColor
                    )
                }
            }
        }

        // Draw Line Clear Flash Animation
        val lineAnim = activeLineClearAnim
        if (lineAnim != null) {
            val elapsed = now - lineAnim.startTime
            if (elapsed < lineAnim.durationMs) {
                val progress = elapsed.toFloat() / lineAnim.durationMs
                val alpha = ((1f - progress) * 220).toInt()
                val flashPaint = Paint().apply {
                    color = Color.argb(alpha, 255, 255, 255)
                    style = Paint.Style.FILL
                }
                for (row in lineAnim.rows) {
                    val screenRow = row - Board.BUFFER_ROWS
                    if (screenRow in 0 until Board.VISIBLE_ROWS) {
                        val rowTop = boardRect.top + screenRow * cellSize
                        canvas.drawRect(boardRect.left, rowTop, boardRect.right, rowTop + cellSize, flashPaint)
                    }
                }
            } else {
                activeLineClearAnim = null
            }
        }

        // Draw outer board border
        canvas.drawRoundRect(boardRect, 8f, 8f, boardBorderPaint)
    }

    private fun drawHoldPanel(canvas: Canvas, eng: NeonStackEngine) {
        canvas.drawRoundRect(holdRect, 12f, 12f, panelPaint)
        canvas.drawRoundRect(holdRect, 12f, 12f, panelBorderPaint)

        val headerY = holdRect.top + textHeaderPaint.textSize + 8f
        canvas.drawText("HOLD", holdRect.centerX(), headerY, textHeaderPaint)

        val held = eng.holdPiece
        if (held != null) {
            val color = if (eng.canHold) held.colorHex.toInt() else Color.parseColor("#5A6275")
            val contentTop = headerY + 6f
            val contentH = (holdRect.bottom - contentTop - 8f).coerceAtLeast(16f)
            val contentW = (holdRect.width() - 16f).coerceAtLeast(16f)
            val miniCell = minOf(cellSize * 0.50f, contentW / 4.2f, contentH / 2.6f)
            val minos = held.getMinos(RotationState.SPAWN)

            val minX = minos.minOf { it.x }
            val maxX = minos.maxOf { it.x }
            val minY = minos.minOf { it.y }
            val maxY = minos.maxOf { it.y }
            val pieceW = (maxX - minX + 1) * miniCell
            val pieceH = (maxY - minY + 1) * miniCell

            val startX = holdRect.centerX() - pieceW / 2f - minX * miniCell
            val startY = contentTop + contentH / 2f - pieceH / 2f - minY * miniCell

            for (m in minos) {
                drawMino(canvas, startX + m.x * miniCell, startY + m.y * miniCell, miniCell, color)
            }
        } else {
            // Subtle dashed empty slot outline to balance visually with NEXT panel
            val slotInsetX = holdRect.width() * 0.16f
            val slotTop = headerY + 10f
            val slotBottom = holdRect.bottom - 12f
            if (slotBottom > slotTop + 12f) {
                val slotRect = RectF(
                    holdRect.left + slotInsetX,
                    slotTop,
                    holdRect.right - slotInsetX,
                    slotBottom
                )
                canvas.drawRoundRect(slotRect, 10f, 10f, emptySlotPaint)
            }
        }
    }

    private fun drawNextPanel(canvas: Canvas, eng: NeonStackEngine) {
        canvas.drawRoundRect(nextRect, 12f, 12f, panelPaint)
        canvas.drawRoundRect(nextRect, 12f, 12f, panelBorderPaint)

        val headerY = nextRect.top + textHeaderPaint.textSize + 8f
        canvas.drawText("NEXT", nextRect.centerX(), headerY, textHeaderPaint)

        val nextPieces = eng.randomizer.peek(3)
        if (isFullScreenGestureMode) {
            // Horizontal 3-piece queue in full-screen top tray
            val contentTop = headerY + 6f
            val contentH = (nextRect.bottom - contentTop - 8f).coerceAtLeast(16f)
            val slotW = nextRect.width() / 3f
            val miniCell = minOf(cellSize * 0.42f, (slotW - 14f) / 4.2f, contentH / 2.6f)

            for (i in nextPieces.indices) {
                val type = nextPieces[i]
                val minos = type.getMinos(RotationState.SPAWN)
                val minX = minos.minOf { it.x }
                val maxX = minos.maxOf { it.x }
                val minY = minos.minOf { it.y }
                val maxY = minos.maxOf { it.y }
                val pieceW = (maxX - minX + 1) * miniCell
                val pieceH = (maxY - minY + 1) * miniCell

                val centerX = nextRect.left + i * slotW + slotW / 2f
                val centerY = contentTop + contentH / 2f
                val startX = centerX - pieceW / 2f - minX * miniCell
                val startY = centerY - pieceH / 2f - minY * miniCell

                for (m in minos) {
                    drawMino(canvas, startX + m.x * miniCell, startY + m.y * miniCell, miniCell, type.colorHex.toInt())
                }
            }
        } else {
            // Classic vertical 3-piece queue beside board
            val miniCell = cellSize * 0.50f
            val contentTop = headerY + 10f
            val itemHeight = (nextRect.bottom - contentTop - 8f) / 3f

            for (i in nextPieces.indices) {
                val type = nextPieces[i]
                val minos = type.getMinos(RotationState.SPAWN)
                val minX = minos.minOf { it.x }
                val maxX = minos.maxOf { it.x }
                val minY = minos.minOf { it.y }
                val maxY = minos.maxOf { it.y }
                val pieceW = (maxX - minX + 1) * miniCell
                val pieceH = (maxY - minY + 1) * miniCell

                val centerY = contentTop + i * itemHeight + itemHeight / 2f
                val startX = nextRect.centerX() - pieceW / 2f - minX * miniCell
                val startY = centerY - pieceH / 2f - minY * miniCell

                for (m in minos) {
                    drawMino(canvas, startX + m.x * miniCell, startY + m.y * miniCell, miniCell, type.colorHex.toInt())
                }
            }
        }
    }

    private fun drawMino(canvas: Canvas, x: Float, y: Float, size: Float, color: Int) {
        val gap = 1.5f
        val rect = RectF(x + gap, y + gap, x + size - gap, y + size - gap)
        val radius = size * 0.15f

        // Base fill
        blockPaint.color = color
        canvas.drawRoundRect(rect, radius, radius, blockPaint)

        // Top-left highlight bevel
        val hlPath = Path().apply {
            moveTo(rect.left, rect.bottom)
            lineTo(rect.left, rect.top + radius)
            quadTo(rect.left, rect.top, rect.left + radius, rect.top)
            lineTo(rect.right, rect.top)
            lineTo(rect.right - size * 0.22f, rect.top + size * 0.22f)
            lineTo(rect.left + size * 0.22f, rect.top + size * 0.22f)
            lineTo(rect.left + size * 0.22f, rect.bottom - size * 0.22f)
            close()
        }
        canvas.drawPath(hlPath, blockHighlightPaint)

        // Bottom-right shadow bevel
        val shPath = Path().apply {
            moveTo(rect.right, rect.top)
            lineTo(rect.right, rect.bottom - radius)
            quadTo(rect.right, rect.bottom, rect.right - radius, rect.bottom)
            lineTo(rect.left, rect.bottom)
            lineTo(rect.left + size * 0.22f, rect.bottom - size * 0.22f)
            lineTo(rect.right - size * 0.22f, rect.bottom - size * 0.22f)
            lineTo(rect.right - size * 0.22f, rect.top + size * 0.22f)
            close()
        }
        canvas.drawPath(shPath, blockShadowPaint)

        // Center jewel facet reflection
        val innerMargin = size * 0.28f
        val innerRect = RectF(rect.left + innerMargin, rect.top + innerMargin, rect.right - innerMargin, rect.bottom - innerMargin)
        blockInnerFacetPaint.color = Color.argb(40, 255, 255, 255)
        canvas.drawRoundRect(innerRect, radius * 0.5f, radius * 0.5f, blockInnerFacetPaint)
    }

    private fun drawGhostMino(canvas: Canvas, x: Float, y: Float, size: Float, color: Int) {
        val gap = 2f
        val rect = RectF(x + gap, y + gap, x + size - gap, y + size - gap)
        val radius = size * 0.15f

        ghostFillPaint.color = Color.argb(35, Color.red(color), Color.green(color), Color.blue(color))
        canvas.drawRoundRect(rect, radius, radius, ghostFillPaint)

        ghostPaint.color = Color.argb(140, Color.red(color), Color.green(color), Color.blue(color))
        canvas.drawRoundRect(rect, radius, radius, ghostPaint)
    }

    private fun drawFloatingBanners(canvas: Canvas, now: Long) {
        val iter = floatingBanners.iterator()
        while (iter.hasNext()) {
            val banner = iter.next()
            val elapsed = now - banner.startTime
            if (elapsed > banner.durationMs) {
                iter.remove()
                continue
            }
            val progress = elapsed.toFloat() / banner.durationMs
            val alpha = ((1f - progress) * 255).toInt().coerceIn(0, 255)
            val currentY = boardRect.centerY() - (progress * 120f)

            floatingTextPaint.alpha = alpha
            canvas.drawText(banner.text, boardRect.centerX(), currentY, floatingTextPaint)
        }
    }

    private fun drawParticles(canvas: Canvas, now: Long) {
        val dt = 0.016f // ~60fps
        val iter = particles.iterator()
        while (iter.hasNext()) {
            val p = iter.next()
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.vy += 280f * dt // Gravity
            p.alpha -= 1.8f * dt

            if (p.alpha <= 0f) {
                iter.remove()
                continue
            }

            particlePaint.color = p.color
            particlePaint.alpha = (p.alpha * 255).toInt().coerceIn(0, 255)
            canvas.drawCircle(p.x, p.y, p.size, particlePaint)
        }
    }
}
