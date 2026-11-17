package com.seniordev.neonstack.ui

import android.view.Choreographer

/**
 * Hardware VSYNC-driven game loop utilizing Android Choreographer.
 * Adapts to high refresh rates (60Hz, 90Hz, 120Hz LTPO, 144Hz) with microsecond precision.
 */
class VsyncGameLoop(
    private val onTick: (dtMs: Long) -> Unit
) : Choreographer.FrameCallback {

    private val choreographer: Choreographer = Choreographer.getInstance()
    private var isRunning: Boolean = false
    private var lastFrameTimeNanos: Long = 0L

    fun start() {
        if (isRunning) return
        isRunning = true
        lastFrameTimeNanos = 0L
        choreographer.postFrameCallback(this)
    }

    fun stop() {
        isRunning = false
        choreographer.removeFrameCallback(this)
    }

    override fun doFrame(frameTimeNanos: Long) {
        if (!isRunning) return

        if (lastFrameTimeNanos != 0L) {
            val elapsedNanos = frameTimeNanos - lastFrameTimeNanos
            // Convert to ms, capped at 50ms to safeguard against frame spikes during GC
            val dtMs = (elapsedNanos / 1_000_000L).coerceIn(1L, 50L)
            onTick(dtMs)
        }
        lastFrameTimeNanos = frameTimeNanos

        choreographer.postFrameCallback(this)
    }
}
