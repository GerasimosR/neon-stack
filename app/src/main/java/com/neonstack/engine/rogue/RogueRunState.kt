package com.neonstack.engine.rogue

class RogueRunState {
    var currentFloor: Int = 1
        private set

    var linesClearedOnFloor: Int = 0
        private set

    val activeRelics = mutableListOf<Relic>()

    val targetLinesForFloor: Int
        get() = 8 + (currentFloor - 1) * 2

    val linesRemainingOnFloor: Int
        get() = (targetLinesForFloor - linesClearedOnFloor).coerceAtLeast(0)

    val isBossFloor: Boolean
        get() = currentFloor % 3 == 0

    fun reset() {
        currentFloor = 1
        linesClearedOnFloor = 0
        activeRelics.clear()
    }

    fun hasRelic(id: RelicId): Boolean {
        return activeRelics.any { it.id == id }
    }

    fun addRelic(relic: Relic) {
        if (!hasRelic(relic.id)) {
            activeRelics.add(relic)
        }
    }

    /**
     * Adds cleared lines. Returns true if floor target was reached (triggering relic draft).
     */
    fun addLines(lines: Int): Boolean {
        linesClearedOnFloor += lines
        return linesClearedOnFloor >= targetLinesForFloor
    }

    fun advanceToNextFloor() {
        currentFloor++
        linesClearedOnFloor = 0
    }

    /**
     * Draws 3 random relics from pool prioritizing unowned relics.
     */
    fun rollDraftOptions(count: Int = 3): List<Relic> {
        val unowned = Relic.ALL_RELICS.filter { relic -> !hasRelic(relic.id) }
        val pool = if (unowned.size >= count) unowned else Relic.ALL_RELICS
        return pool.shuffled().take(count)
    }
}
