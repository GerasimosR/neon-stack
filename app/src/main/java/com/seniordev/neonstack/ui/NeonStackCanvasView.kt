package com.seniordev.neonstack.ui

import android.content.Context
import android.graphics.*
import android.os.SystemClock
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import com.seniordev.neonstack.engine.*
import com.seniordev.neonstack.engine.Point
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

    // Paints (pre-allocated for zero GC churn during 120Hz rendering)
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#0C0E14")
    }
    private val boardAmbientPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val gridLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#182034")
        strokeWidth = 1.6f
        style = Paint.Style.STROKE
    }
    private val boardBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#35476D")
        strokeWidth = 3.5f
        style = Paint.Style.STROKE
    }

    // Neon Glass Circuit Module Paints
    private val moduleCorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#0E1422")
        style = Paint.Style.FILL
    }
    private val moduleTintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val moduleGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val moduleRimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val moduleNodePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val moduleSparkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(215, 255, 255, 255)
        style = Paint.Style.FILL
    }
    private val bridgeConduitPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    // Laser Drop-Rail Paints
    private val laserBeamPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val laserRailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.6f
    }
    private val impactBarGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val impactBarCorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(225, 255, 255, 255)
        style = Paint.Style.FILL
    }

    // Top Command Dock Paints
    private val panelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#131928")
        style = Paint.Style.FILL
    }
    private val panelBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#25304A")
        style = Paint.Style.STROKE
        strokeWidth = 2.5f
    }
    private val emptySlotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#283450")
        style = Paint.Style.STROKE
        strokeWidth = 2.2f
        pathEffect = DashPathEffect(floatArrayOf(9f, 7f), 0f)
    }
    private val textHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#7E91B8")
        textSize = 24f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        letterSpacing = 0.14f
    }
    private val floatingTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFD166")
        textSize = 42f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        setShadowLayer(10f, 0f, 0f, Color.parseColor("#FF2EE6"))
    }
    private val particlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val lineFlashPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    // Reusable drawing geometry to avoid allocations in onDraw
    private val tempRect = RectF()
    private val tempNodePath = Path()

    // Dynamic Layout Measurements (Unified Top Command Dock: VAULT + QUEUE above 9x16 matrix)
    private val boardRect = RectF()
    private val vaultRect = RectF()
    private val queueRect = RectF()
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
        val durationMs: Long = 900L
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
                    gestureListener?.onHardDrop()
                    invalidate()
                } else if (!hasDragged &&
                    abs(totalDx) >= quickSwipeSlop &&
                    abs(totalDx) > abs(totalDy) * 1.1f
                ) {
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
                    gestureListener?.onSwipeDown()
                    invalidate()
                } else if (!hasDragged && hypot(totalDx.toDouble(), totalDy.toDouble()) <= tapSlop) {
                    val vaultTouchSlop = 12f * density
                    val hitVault = x >= (vaultRect.left - vaultTouchSlop) &&
                        x <= (vaultRect.right + vaultTouchSlop) &&
                        y >= (vaultRect.top - vaultTouchSlop) &&
                        y <= (vaultRect.bottom + vaultTouchSlop)

                    if (hitVault) {
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
        val horizontalPadding = 8f
        val verticalPadding = 6f
        val dockGap = 10f

        // Unified Top Command Dock (VAULT + QUEUE) above a large centered 9x16 matrix
        val dockFraction = if (isFullScreenGestureMode) 0.11f else 0.135f
        val topDockHeight = (viewHeight * dockFraction).coerceIn(96f, 152f)
        val dockBottom = verticalPadding + topDockHeight
        val boardAreaTop = dockBottom + dockGap
        val boardAreaH = (viewHeight - boardAreaTop - verticalPadding).coerceAtLeast(100f)

        val maxCellW = (viewWidth - horizontalPadding * 2) / Board.WIDTH.toFloat()
        val maxCellH = boardAreaH / Board.VISIBLE_ROWS.toFloat()
        cellSize = minOf(maxCellW, maxCellH)
        textHeaderPaint.textSize = (cellSize * 0.34f).coerceIn(19f, 26f)

        val boardW = cellSize * Board.WIDTH
        val boardH = cellSize * Board.VISIBLE_ROWS
        val boardLeft = (viewWidth - boardW) / 2f
        val boardTop = boardAreaTop + (boardAreaH - boardH) / 2f
        boardRect.set(boardLeft, boardTop, boardLeft + boardW, boardTop + boardH)

        // Dock spans at least board width (up to full padded width for generous breathing room)
        val dockWidth = minOf(viewWidth - horizontalPadding * 2, maxOf(boardW, viewWidth * 0.90f))
        val dockLeft = (viewWidth - dockWidth) / 2f
        val vaultW = dockWidth * 0.28f
        vaultRect.set(dockLeft, verticalPadding, dockLeft + vaultW, dockBottom)
        queueRect.set(vaultRect.right + dockGap, verticalPadding, dockLeft + dockWidth, dockBottom)

        // Radial ambient cyber glow behind playfield
        val cx = boardRect.centerX()
        val cy = boardRect.centerY()
        val radius = maxOf(boardRect.width(), boardRect.height()) * 0.72f
        boardAmbientPaint.shader = RadialGradient(
            cx, cy, radius,
            intArrayOf(
                Color.parseColor("#18243C"),
                Color.parseColor("#111726"),
                Color.parseColor("#0B0E16")
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

        if (engine == null) return
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
                    Color.parseColor("#FF3D8B"),
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

        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)

        // Top Command Dock (VAULT + QUEUE)
        drawVaultPanel(canvas, eng)
        drawQueuePanel(canvas, eng)

        // 9x16 Playfield Matrix
        drawPlayfield(canvas, eng, now)

        // Visual FX Overlays
        drawFloatingBanners(canvas, now)
        drawParticles(canvas)

        canvas.restore()

        if (eng.status == GameStatus.RUNNING || particles.isNotEmpty() || floatingBanners.isNotEmpty()) {
            postInvalidateOnAnimation()
        }
    }

    private fun drawPlayfield(canvas: Canvas, eng: NeonStackEngine, now: Long) {
        canvas.drawRoundRect(boardRect, 12f, 12f, boardAmbientPaint)

        // Subtle cyber grid lines
        for (col in 1 until Board.WIDTH) {
            val x = boardRect.left + col * cellSize
            canvas.drawLine(x, boardRect.top, x, boardRect.bottom, gridLinePaint)
        }
        for (row in 1 until Board.VISIBLE_ROWS) {
            val y = boardRect.top + row * cellSize
            canvas.drawLine(boardRect.left, y, boardRect.right, y, gridLinePaint)
        }

        // Draw locked Neon Glass Circuit Modules
        val grid = eng.board.grid
        for (y in Board.BUFFER_ROWS until Board.TOTAL_ROWS) {
            val screenRow = y - Board.BUFFER_ROWS
            for (x in 0 until Board.WIDTH) {
                val blockId = grid[y][x]
                if (blockId > 0) {
                    val blockShape = BlockShape.fromId(blockId)
                    val color = blockShape?.colorHex?.toInt() ?: Color.GRAY
                    drawNeonModule(
                        canvas = canvas,
                        x = boardRect.left + x * cellSize,
                        y = boardRect.top + screenRow * cellSize,
                        size = cellSize,
                        color = color,
                        isActive = false
                    )
                }
            }
        }

        // Draw Laser Drop-Rails + Active Fused Neon Circuit Piece
        val currentPiece = eng.currentPiece
        if (currentPiece != null && eng.status == GameStatus.RUNNING) {
            val dropPos = eng.getGhostPiecePosition()
            val pieceColor = currentPiece.type.colorHex.toInt()

            if (dropPos != null && dropPos.y > currentPiece.position.y) {
                drawLaserDropRails(canvas, currentPiece, dropPos, pieceColor)
            }

            drawConnectedActivePiece(canvas, currentPiece.getWorldMinos(), pieceColor)
        }

        // Line Clear Flash Animation
        val lineAnim = activeLineClearAnim
        if (lineAnim != null) {
            val elapsed = now - lineAnim.startTime
            if (elapsed < lineAnim.durationMs) {
                val progress = elapsed.toFloat() / lineAnim.durationMs
                val alpha = ((1f - progress) * 230).toInt()
                lineFlashPaint.color = Color.argb(alpha, 255, 255, 255)
                for (row in lineAnim.rows) {
                    val screenRow = row - Board.BUFFER_ROWS
                    if (screenRow in 0 until Board.VISIBLE_ROWS) {
                        val rowTop = boardRect.top + screenRow * cellSize
                        canvas.drawRect(boardRect.left, rowTop, boardRect.right, rowTop + cellSize, lineFlashPaint)
                    }
                }
            } else {
                activeLineClearAnim = null
            }
        }

        // Outer neon playfield frame
        canvas.drawRoundRect(boardRect, 12f, 12f, boardBorderPaint)
    }

    /**
     * Draws Neon Stack's signature Laser Drop-Rails: vertical translucent projection rails
     * framing the active piece's horizontal column span down to glowing surface impact bars.
     */
    private fun drawLaserDropRails(
        canvas: Canvas,
        activePiece: ActivePiece,
        dropPos: Point,
        color: Int
    ) {
        val activeMinos = activePiece.getWorldMinos()
        val dropMinos = activePiece.getWorldMinos(dropPos, activePiece.rotation)

        val minCol = dropMinos.minOf { it.x }
        val maxCol = dropMinos.maxOf { it.x }

        val topScreenRow = (activeMinos.maxOf { it.y } - Board.BUFFER_ROWS + 1).coerceIn(0, Board.VISIBLE_ROWS)
        val bottomScreenRow = (dropMinos.maxOf { it.y } - Board.BUFFER_ROWS + 1).coerceIn(0, Board.VISIBLE_ROWS)
        if (bottomScreenRow <= topScreenRow) return

        val beamLeft = boardRect.left + minCol * cellSize + 2f
        val beamRight = boardRect.left + (maxCol + 1) * cellSize - 2f
        val beamTop = boardRect.top + topScreenRow * cellSize
        val beamBottom = boardRect.top + bottomScreenRow * cellSize

        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)

        // Subtle translucent projection corridor
        laserBeamPaint.shader = LinearGradient(
            0f, beamTop, 0f, beamBottom,
            Color.argb(8, r, g, b),
            Color.argb(34, r, g, b),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(beamLeft, beamTop, beamRight, beamBottom, laserBeamPaint)
        laserBeamPaint.shader = null

        // Left & right crisp vertical laser guide rails
        laserRailPaint.shader = LinearGradient(
            0f, beamTop, 0f, beamBottom,
            Color.argb(30, r, g, b),
            Color.argb(175, r, g, b),
            Shader.TileMode.CLAMP
        )
        canvas.drawLine(beamLeft, beamTop, beamLeft, beamBottom, laserRailPaint)
        canvas.drawLine(beamRight, beamTop, beamRight, beamBottom, laserRailPaint)
        laserRailPaint.shader = null

        // Glowing surface impact pads directly beneath the lowest landing block in each column
        val lowestByCol = mutableMapOf<Int, Int>()
        for (m in dropMinos) {
            val prev = lowestByCol[m.x]
            if (prev == null || m.y > prev) {
                lowestByCol[m.x] = m.y
            }
        }

        val padHeight = (cellSize * 0.14f).coerceIn(4f, 8f)
        for ((col, worldY) in lowestByCol) {
            val screenRow = worldY - Board.BUFFER_ROWS
            if (screenRow !in 0 until Board.VISIBLE_ROWS) continue
            val cellLeft = boardRect.left + col * cellSize + 3f
            val cellRight = boardRect.left + (col + 1) * cellSize - 3f
            val padBottom = boardRect.top + (screenRow + 1) * cellSize - 1.5f
            val padTop = padBottom - padHeight

            impactBarGlowPaint.color = Color.argb(170, r, g, b)
            tempRect.set(cellLeft, padTop - 2f, cellRight, padBottom + 1f)
            canvas.drawRoundRect(tempRect, padHeight, padHeight, impactBarGlowPaint)

            tempRect.set(cellLeft + 3f, padTop + padHeight * 0.28f, cellRight - 3f, padBottom - padHeight * 0.28f)
            canvas.drawRoundRect(tempRect, padHeight, padHeight, impactBarCorePaint)
        }
    }

    /**
     * Renders the active piece as a fused Neon Glass Circuit assembly with luminous energy
     * conduits bridging adjacent cells.
     */
    private fun drawConnectedActivePiece(canvas: Canvas, worldMinos: List<Point>, color: Int) {
        val minoSet = worldMinos.toSet()

        // 1. Draw each neon glass module
        for (mino in worldMinos) {
            val screenRow = mino.y - Board.BUFFER_ROWS
            if (screenRow in 0 until Board.VISIBLE_ROWS) {
                drawNeonModule(
                    canvas = canvas,
                    x = boardRect.left + mino.x * cellSize,
                    y = boardRect.top + screenRow * cellSize,
                    size = cellSize,
                    color = color,
                    isActive = true
                )
            }
        }

        // 2. Fuse adjacent cells with luminous energy bridges across shared seams
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        bridgeConduitPaint.color = Color.argb(215, r, g, b)
        bridgeConduitPaint.strokeWidth = (cellSize * 0.13f).coerceAtLeast(3f)

        for (mino in worldMinos) {
            val sr = mino.y - Board.BUFFER_ROWS
            if (sr !in 0 until Board.VISIBLE_ROWS) continue
            val cx = boardRect.left + mino.x * cellSize + cellSize * 0.5f
            val cy = boardRect.top + sr * cellSize + cellSize * 0.5f

            // Right neighbor bridge
            val rightNeighbor = Point(mino.x + 1, mino.y)
            if (minoSet.contains(rightNeighbor)) {
                val nx = boardRect.left + rightNeighbor.x * cellSize + cellSize * 0.5f
                canvas.drawLine(cx + cellSize * 0.16f, cy, nx - cellSize * 0.16f, cy, bridgeConduitPaint)
            }
            // Bottom neighbor bridge
            val bottomNeighbor = Point(mino.x, mino.y + 1)
            val bsr = bottomNeighbor.y - Board.BUFFER_ROWS
            if (minoSet.contains(bottomNeighbor) && bsr in 0 until Board.VISIBLE_ROWS) {
                val ny = boardRect.top + bsr * cellSize + cellSize * 0.5f
                canvas.drawLine(cx, cy + cellSize * 0.16f, cx, ny - cellSize * 0.16f, bridgeConduitPaint)
            }
        }
    }

    /**
     * Signature Neon Glass Circuit Module renderer (dark obsidian translucent core,
     * outer neon halo, luminous circuit rim, and glowing diamond center node).
     */
    private fun drawNeonModule(
        canvas: Canvas,
        x: Float,
        y: Float,
        size: Float,
        color: Int,
        isActive: Boolean
    ) {
        val gap = (size * 0.08f).coerceIn(1.5f, 3.2f)
        val left = x + gap
        val top = y + gap
        val right = x + size - gap
        val bottom = y + size - gap
        val radius = size * 0.24f

        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)

        // 1. Dark obsidian cyber-glass core
        tempRect.set(left, top, right, bottom)
        canvas.drawRoundRect(tempRect, radius, radius, moduleCorePaint)

        // 2. Translucent neon tint wash inside glass body
        moduleTintPaint.color = Color.argb(if (isActive) 86 else 64, r, g, b)
        canvas.drawRoundRect(tempRect, radius, radius, moduleTintPaint)

        // 3. Soft neon rim glow halo
        val rimWidth = (size * 0.085f).coerceIn(1.6f, 3.4f)
        moduleGlowPaint.color = Color.argb(if (isActive) 95 else 65, r, g, b)
        moduleGlowPaint.strokeWidth = rimWidth * 2.1f
        canvas.drawRoundRect(tempRect, radius, radius, moduleGlowPaint)

        // 4. Crisp luminous neon outer border
        moduleRimPaint.color = Color.argb(if (isActive) 255 else 225, r, g, b)
        moduleRimPaint.strokeWidth = rimWidth
        canvas.drawRoundRect(tempRect, radius, radius, moduleRimPaint)

        // 5. Glowing center micro-circuit diamond node + specular core
        val cx = (left + right) * 0.5f
        val cy = (top + bottom) * 0.5f
        val nodeR = size * (if (isActive) 0.145f else 0.115f)
        tempNodePath.reset()
        tempNodePath.moveTo(cx, cy - nodeR)
        tempNodePath.lineTo(cx + nodeR, cy)
        tempNodePath.lineTo(cx, cy + nodeR)
        tempNodePath.lineTo(cx - nodeR, cy)
        tempNodePath.close()

        moduleNodePaint.color = Color.argb(if (isActive) 240 else 190, r, g, b)
        canvas.drawPath(tempNodePath, moduleNodePaint)

        if (size >= 14f) {
            canvas.drawCircle(cx, cy, nodeR * 0.36f, moduleSparkPaint)
        }
    }

    private fun drawVaultPanel(canvas: Canvas, eng: NeonStackEngine) {
        canvas.drawRoundRect(vaultRect, 14f, 14f, panelPaint)
        canvas.drawRoundRect(vaultRect, 14f, 14f, panelBorderPaint)

        val headerY = vaultRect.top + textHeaderPaint.textSize + 6f
        canvas.drawText("VAULT", vaultRect.centerX(), headerY, textHeaderPaint)

        val vaulted = eng.holdPiece
        if (vaulted != null) {
            val color = if (eng.canHold) vaulted.colorHex.toInt() else Color.parseColor("#5A6275")
            val contentTop = headerY + 4f
            val contentH = (vaultRect.bottom - contentTop - 8f).coerceAtLeast(16f)
            val contentW = (vaultRect.width() - 16f).coerceAtLeast(16f)
            val miniCell = minOf(cellSize * 0.44f, contentW / 4.2f, contentH / 2.6f)
            drawMiniShape(canvas, vaulted, vaultRect.centerX(), contentTop + contentH / 2f, miniCell, color)
        } else {
            val slotInsetX = vaultRect.width() * 0.18f
            val slotTop = headerY + 8f
            val slotBottom = vaultRect.bottom - 10f
            if (slotBottom > slotTop + 10f) {
                tempRect.set(
                    vaultRect.left + slotInsetX,
                    slotTop,
                    vaultRect.right - slotInsetX,
                    slotBottom
                )
                canvas.drawRoundRect(tempRect, 10f, 10f, emptySlotPaint)
            }
        }
    }

    private fun drawQueuePanel(canvas: Canvas, eng: NeonStackEngine) {
        canvas.drawRoundRect(queueRect, 14f, 14f, panelPaint)
        canvas.drawRoundRect(queueRect, 14f, 14f, panelBorderPaint)

        val headerY = queueRect.top + textHeaderPaint.textSize + 6f
        canvas.drawText("QUEUE", queueRect.centerX(), headerY, textHeaderPaint)

        val nextPieces = eng.randomizer.peek(3)
        val contentTop = headerY + 4f
        val contentH = (queueRect.bottom - contentTop - 8f).coerceAtLeast(16f)
        val slotW = queueRect.width() / 3f
        val miniCell = minOf(cellSize * 0.42f, (slotW - 14f) / 4.2f, contentH / 2.6f)

        for (i in nextPieces.indices) {
            val shape = nextPieces[i]
            val centerX = queueRect.left + i * slotW + slotW / 2f
            val centerY = contentTop + contentH / 2f
            drawMiniShape(canvas, shape, centerX, centerY, miniCell, shape.colorHex.toInt())
        }
    }

    private fun drawMiniShape(
        canvas: Canvas,
        shape: BlockShape,
        centerX: Float,
        centerY: Float,
        miniCell: Float,
        color: Int
    ) {
        val minos = shape.getMinos(RotationState.SPAWN)
        val minX = minos.minOf { it.x }
        val maxX = minos.maxOf { it.x }
        val minY = minos.minOf { it.y }
        val maxY = minos.maxOf { it.y }
        val pieceW = (maxX - minX + 1) * miniCell
        val pieceH = (maxY - minY + 1) * miniCell

        val startX = centerX - pieceW / 2f - minX * miniCell
        val startY = centerY - pieceH / 2f - minY * miniCell

        for (m in minos) {
            drawNeonModule(
                canvas = canvas,
                x = startX + m.x * miniCell,
                y = startY + m.y * miniCell,
                size = miniCell,
                color = color,
                isActive = true
            )
        }
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

    private fun drawParticles(canvas: Canvas) {
        val dt = 0.016f
        val iter = particles.iterator()
        while (iter.hasNext()) {
            val p = iter.next()
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.vy += 280f * dt
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
