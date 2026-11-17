package com.neonstack.data

import android.content.Context
import android.content.SharedPreferences

enum class ControlMode {
    ARCADE_PAD,
    GESTURES
}

class PreferencesManager(context: Context) {
    companion object {
        private const val PREFS_NAME = "neon_stack_prefs"
        private const val KEY_HIGH_SCORE = "high_score"
        private const val KEY_CLASSIC_BEST_SCORE = "classic_best_score"
        private const val KEY_ROGUE_BEST_FLOOR = "rogue_best_floor"
        private const val KEY_ROGUE_RUNS = "rogue_runs"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val KEY_MUSIC_ENABLED = "music_enabled"
        private const val KEY_HAPTICS_ENABLED = "haptics_enabled"
        private const val KEY_CRT_FILTER_ENABLED = "crt_filter_enabled"
        private const val KEY_GAMES_PLAYED = "games_played"
        private const val KEY_DAS_DELAY_MS = "das_delay_ms"
        private const val KEY_ARR_RATE_MS = "arr_rate_ms"
        private const val KEY_SOFT_DROP_RATE_MS = "soft_drop_rate_ms"
        private const val KEY_CONTROL_MODE = "control_mode"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var highScore: Long
        get() = prefs.getLong(KEY_HIGH_SCORE, 0L)
        set(value) = prefs.edit().putLong(KEY_HIGH_SCORE, value).apply()

    var classicBestScore: Long
        get() {
            val saved = prefs.getLong(KEY_CLASSIC_BEST_SCORE, -1L)
            return if (saved >= 0L) saved else highScore
        }
        set(value) = prefs.edit().putLong(KEY_CLASSIC_BEST_SCORE, value).apply()

    var rogueBestFloor: Int
        get() = prefs.getInt(KEY_ROGUE_BEST_FLOOR, 0)
        set(value) = prefs.edit().putInt(KEY_ROGUE_BEST_FLOOR, value).apply()

    var rogueRuns: Int
        get() = prefs.getInt(KEY_ROGUE_RUNS, 0)
        set(value) = prefs.edit().putInt(KEY_ROGUE_RUNS, value).apply()

    var isSoundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_SOUND_ENABLED, value).apply()

    var isMusicEnabled: Boolean
        get() = prefs.getBoolean(KEY_MUSIC_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_MUSIC_ENABLED, value).apply()

    var isHapticsEnabled: Boolean
        get() = prefs.getBoolean(KEY_HAPTICS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_HAPTICS_ENABLED, value).apply()

    var isCrtFilterEnabled: Boolean
        get() = prefs.getBoolean(KEY_CRT_FILTER_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_CRT_FILTER_ENABLED, value).apply()

    var gamesPlayed: Int
        get() = prefs.getInt(KEY_GAMES_PLAYED, 0)
        set(value) = prefs.edit().putInt(KEY_GAMES_PLAYED, value).apply()

    var dasDelayMs: Long
        get() = prefs.getLong(KEY_DAS_DELAY_MS, 150L)
        set(value) = prefs.edit().putLong(KEY_DAS_DELAY_MS, value).apply()

    var arrRateMs: Long
        get() = prefs.getLong(KEY_ARR_RATE_MS, 38L)
        set(value) = prefs.edit().putLong(KEY_ARR_RATE_MS, value).apply()

    var softDropRateMs: Long
        get() = prefs.getLong(KEY_SOFT_DROP_RATE_MS, 40L)
        set(value) = prefs.edit().putLong(KEY_SOFT_DROP_RATE_MS, value).apply()

    var controlMode: ControlMode
        get() {
            val raw = prefs.getString(KEY_CONTROL_MODE, ControlMode.ARCADE_PAD.name)
            return runCatching { ControlMode.valueOf(raw ?: ControlMode.ARCADE_PAD.name) }
                .getOrDefault(ControlMode.ARCADE_PAD)
        }
        set(value) = prefs.edit().putString(KEY_CONTROL_MODE, value.name).apply()

    fun resetCalibration() {
        dasDelayMs = 150L
        arrRateMs = 38L
        softDropRateMs = 40L
    }

    fun updateHighScore(currentScore: Long): Boolean {
        if (currentScore > highScore) {
            highScore = currentScore
            return true
        }
        return false
    }

    fun updateClassicBestScore(currentScore: Long): Boolean {
        if (currentScore > classicBestScore) {
            classicBestScore = currentScore
            return true
        }
        return false
    }

    fun updateRogueBestFloor(floorReached: Int): Boolean {
        if (floorReached > rogueBestFloor) {
            rogueBestFloor = floorReached
            return true
        }
        return false
    }

    fun incrementGamesPlayed() {
        gamesPlayed += 1
    }

    fun incrementRogueRuns() {
        rogueRuns += 1
    }
}
