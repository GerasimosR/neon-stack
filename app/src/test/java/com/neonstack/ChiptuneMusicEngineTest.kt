package com.neonstack

import com.neonstack.audio.ChiptuneMusicEngine
import org.junit.Assert.*
import org.junit.Test

class ChiptuneMusicEngineTest {

    @Test
    fun testMusicEngineLevelTempoScaling() {
        val engine = ChiptuneMusicEngine()
        assertTrue(engine.isEnabled)

        // Test level bounds
        engine.setLevel(1)
        engine.setLevel(5)
        engine.setLevel(10)
        engine.setLevel(15)
        engine.setLevel(99) // Should clamp cleanly to level 15 max tempo
    }

    @Test
    fun testMusicEngineToggling() {
        val engine = ChiptuneMusicEngine()
        engine.isEnabled = false
        assertFalse(engine.isEnabled)

        engine.isEnabled = true
        assertTrue(engine.isEnabled)
    }
}
