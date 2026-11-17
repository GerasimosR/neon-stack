package com.neonstack.engine

enum class RotationState(val value: Int) {
    SPAWN(0),
    CLOCKWISE(1),
    FLIP(2),
    COUNTER_CLOCKWISE(3);

    fun rotateCw(): RotationState = entries[(ordinal + 1) % entries.size]
    fun rotateCcw(): RotationState = entries[(ordinal + entries.size - 1) % entries.size]

    companion object {
        fun fromInt(value: Int): RotationState = entries[(value % entries.size + entries.size) % entries.size]
    }
}
