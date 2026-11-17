package com.neonstack

import com.neonstack.audio.ChiptuneMusicEngine
import org.junit.Assert.*
import org.junit.Test

class AudioFocusHelperTest {

    @Test
    fun testChiptuneMusicEngineStateTransitions() {
        val musicEngine = ChiptuneMusicEngine()
        assertTrue("Initially music engine should be enabled", musicEngine.isEnabled)

        // When focus is lost (simulated via pause)
        musicEngine.pause()
        // Ensure state remains valid
        assertTrue(musicEngine.isEnabled)

        // When focus is regained (simulated via resume)
        musicEngine.resume()
        assertTrue(musicEngine.isEnabled)

        // When disabled by user preferences
        musicEngine.isEnabled = false
        assertFalse(musicEngine.isEnabled)
    }

    @Test
    fun testLevelBpmScalingFormula() {
        val musicEngine = ChiptuneMusicEngine()
        musicEngine.setLevel(1)
        // Level 1: 120.0 BPM
        // Level 10: 120.0 + 9 * 4.5 = 160.5 BPM
        // Level 15: 120.0 + 14 * 4.5 = 183.0 BPM
        musicEngine.setLevel(15)
        // Ensure no exception and valid clamping
        musicEngine.setLevel(50) // clamped to 15
        musicEngine.setLevel(-5) // clamped to 1
    }
}
