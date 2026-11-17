package com.neonstack

import com.neonstack.ui.mvi.NeonStackUiState
import org.junit.Assert.*
import org.junit.Test

class CalibrationTest {

    @Test
    fun testDefaultCalibrationValues() {
        val defaultState = NeonStackUiState()
        assertEquals(150L, defaultState.dasDelayMs)
        assertEquals(38L, defaultState.arrRateMs)
        assertEquals(40L, defaultState.softDropRateMs)
        assertFalse(defaultState.isCalibrationOpen)
    }

    @Test
    fun testCustomCalibrationState() {
        val customState = NeonStackUiState(
            dasDelayMs = 120L,
            arrRateMs = 16L,
            softDropRateMs = 25L,
            isCalibrationOpen = true
        )
        assertEquals(120L, customState.dasDelayMs)
        assertEquals(16L, customState.arrRateMs)
        assertEquals(25L, customState.softDropRateMs)
        assertTrue(customState.isCalibrationOpen)
    }

    @Test
    fun testCrtFilterToggleState() {
        val defaultState = NeonStackUiState()
        assertFalse("CRT filter should be off by default for crisp OLED rendering", defaultState.isCrtFilterEnabled)

        val crtState = defaultState.copy(isCrtFilterEnabled = true)
        assertTrue("CRT filter state can be enabled", crtState.isCrtFilterEnabled)
    }
}
