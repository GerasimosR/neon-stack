package com.seniordev.neonstack.engine

import java.util.LinkedList
import java.util.Random

class BagRandomizer(private val random: Random = Random()) {
    private val queue = LinkedList<BlockShape>()
    var extraIConduit: Boolean = false

    init {
        refillBag()
        refillBag()
    }

    private fun refillBag() {
        val bag = BlockShape.entries.toMutableList()
        if (extraIConduit) {
            bag.add(BlockShape.I)
        }
        // Fisher-Yates shuffle
        for (i in bag.indices.reversed()) {
            val j = random.nextInt(i + 1)
            val temp = bag[i]
            bag[i] = bag[j]
            bag[j] = temp
        }
        queue.addAll(bag)
    }

    fun nextPiece(): BlockShape {
        if (queue.size < 7) {
            refillBag()
        }
        return queue.poll() ?: BlockShape.T
    }

    fun peek(count: Int = 3): List<BlockShape> {
        while (queue.size < count) {
            refillBag()
        }
        return queue.take(count)
    }

    fun reset() {
        queue.clear()
        refillBag()
        refillBag()
    }
}
