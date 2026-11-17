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
        combo = -1 // Combo broken
        if (spin != SpinBonus.NONE) {
            val pts = if (spin == SpinBonus.MINI) 100 * level else 400 * level
            score += pts
            spinsCount++
            return LineClearReward(
                points = pts,
                isBackToBack = false,
                comboCount = 0,
                message = if (spin == SpinBonus.MINI) "PIVOT MINI!" else "PIVOT SPIN!"
            )
        }
        return null
    }

    fun onLinesCleared(lines: Int, spin: SpinBonus): LineClearReward {
        linesCleared += lines
        level = (linesCleared / 10) + 1
        combo++

        val isDifficult = lines == 4 || spin != SpinBonus.NONE
        val b2bBonus = isBackToBackEligible && isDifficult

        val basePoints: Int
        val label: String

        when (lines) {
            1 -> {
                singlesCount++
                when (spin) {
                    SpinBonus.NONE -> {
                        basePoints = 100 * level
                        label = "SINGLE"
                    }
                    SpinBonus.MINI -> {
                        basePoints = 200 * level
                        label = "PIVOT MINI SINGLE!"
                        spinsCount++
                    }
                    SpinBonus.FULL -> {
                        basePoints = 800 * level
                        label = "PIVOT SINGLE!"
                        spinsCount++
                    }
                }
            }
            2 -> {
                doublesCount++
                when (spin) {
                    SpinBonus.NONE -> {
                        basePoints = 300 * level
                        label = "DOUBLE"
                    }
                    SpinBonus.MINI -> {
                        basePoints = 400 * level
                        label = "PIVOT MINI DOUBLE!"
                        spinsCount++
                    }
                    SpinBonus.FULL -> {
                        basePoints = 1200 * level
                        label = "PIVOT DOUBLE!"
                        spinsCount++
                    }
                }
            }
            3 -> {
                triplesCount++
                when (spin) {
                    SpinBonus.NONE -> {
                        basePoints = 500 * level
                        label = "TRIPLE"
                    }
                    else -> {
                        basePoints = 1600 * level
                        label = "PIVOT TRIPLE!"
                        spinsCount++
                    }
                }
            }
            4 -> {
                quadsCount++
                basePoints = 800 * level
                label = "QUAD CLEAR!"
            }
            else -> {
                basePoints = 0
                label = ""
            }
        }

        var awardPoints = if (b2bBonus) (basePoints * 1.5).toInt() else basePoints
        if (combo > 0) {
            awardPoints += 50 * combo * level
        }

        score += awardPoints

        if (isDifficult) {
            isBackToBackEligible = true
        } else if (lines > 0) {
            isBackToBackEligible = false
        }

        val message = buildString {
            if (b2bBonus) append("B2B ")
            append(label)
            if (combo > 0) append(" (COMBO x$combo)")
        }

        return LineClearReward(
            points = awardPoints,
            isBackToBack = b2bBonus,
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
