package com.seniordev.neonstack.engine

data class LineClearReward(
    val points: Int,
    val isBackToBack: Boolean,
    val comboCount: Int,
    val message: String
)

class ScoreSystem {
    var score: Long = 0
        private set
    var level: Int = 1
        private set
    var linesCleared: Int = 0
        private set
    var combo: Int = -1
        private set
    var isBackToBackEligible: Boolean = false
        private set

    // Statistics
    var singlesCount: Int = 0
        private set
    var doublesCount: Int = 0
        private set
    var triplesCount: Int = 0
        private set
    var quadsCount: Int = 0
        private set
    var spinsCount: Int = 0
        private set

    fun reset() {
        score = 0
        level = 1
        linesCleared = 0
        combo = -1
        isBackToBackEligible = false
        singlesCount = 0
        doublesCount = 0
        triplesCount = 0
        quadsCount = 0
        spinsCount = 0
    }

    fun addSoftDropPoints(cells: Int) {
        score += cells * 1
    }

    fun addHardDropPoints(cells: Int) {
        score += cells * 2
    }

    fun onPieceLockedWithoutClear(spin: SpinBonus): LineClearReward? {
        combo = -1 // Chain broken
        if (spin != SpinBonus.NONE) {
            val pts = if (spin == SpinBonus.MINI) 150 * level else 500 * level
            score += pts
            spinsCount++
            return LineClearReward(
                points = pts,
                isBackToBack = false,
                comboCount = 0,
                message = if (spin == SpinBonus.MINI) "OVERCLOCK MINI!" else "OVERCLOCK SPIN!"
            )
        }
        return null
    }

    fun onLinesCleared(lines: Int, spin: SpinBonus): LineClearReward {
        linesCleared += lines
        level = (linesCleared / 10) + 1
        combo++

        val isDifficult = lines == 4 || spin != SpinBonus.NONE
        val surgeBonus = isBackToBackEligible && isDifficult

        val basePoints: Int
        val label: String

        when (lines) {
            1 -> {
                singlesCount++
                when (spin) {
                    SpinBonus.NONE -> {
                        basePoints = 150 * level
                        label = "SINGLE"
                    }
                    SpinBonus.MINI -> {
                        basePoints = 300 * level
                        label = "OVERCLOCK MINI!"
                        spinsCount++
                    }
                    SpinBonus.FULL -> {
                        basePoints = 900 * level
                        label = "OVERCLOCK SINGLE!"
                        spinsCount++
                    }
                }
            }
            2 -> {
                doublesCount++
                when (spin) {
                    SpinBonus.NONE -> {
                        basePoints = 400 * level
                        label = "DOUBLE"
                    }
                    SpinBonus.MINI -> {
                        basePoints = 600 * level
                        label = "OVERCLOCK DOUBLE!"
                        spinsCount++
                    }
                    SpinBonus.FULL -> {
                        basePoints = 1400 * level
                        label = "OVERCLOCK DOUBLE!"
                        spinsCount++
                    }
                }
            }
            3 -> {
                triplesCount++
                when (spin) {
                    SpinBonus.NONE -> {
                        basePoints = 750 * level
                        label = "TRIPLE"
                    }
                    else -> {
                        basePoints = 1900 * level
                        label = "OVERCLOCK TRIPLE!"
                        spinsCount++
                    }
                }
            }
            4 -> {
                quadsCount++
                basePoints = 1200 * level
                label = "QUAD CLEAR!"
            }
            else -> {
                basePoints = 0
                label = ""
            }
        }

        var awardPoints = if (surgeBonus) (basePoints * 1.5).toInt() else basePoints
        if (combo > 0) {
            awardPoints += 75 * combo * level
        }

        score += awardPoints

        if (isDifficult) {
            isBackToBackEligible = true
        } else if (lines > 0) {
            isBackToBackEligible = false
        }

        val message = buildString {
            if (surgeBonus) append("SURGE ")
            append(label)
            if (combo > 0) append(" (CHAIN x$combo)")
        }

        return LineClearReward(
            points = awardPoints,
            isBackToBack = surgeBonus,
            comboCount = combo,
            message = message
        )
    }

    /**
     * Gravity drop interval in milliseconds for the current level.
     */
    fun getDropIntervalMs(): Long {
        return when (level) {
            1 -> 800L
            2 -> 720L
            3 -> 630L
            4 -> 550L
            5 -> 470L
            6 -> 380L
            7 -> 300L
            8 -> 220L
            9 -> 140L
            10 -> 100L
            11 -> 80L
            12 -> 65L
            13 -> 50L
            14 -> 40L
            else -> 30L
        }
    }
}
