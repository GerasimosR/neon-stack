package com.seniordev.neonstack.engine.rogue

enum class RelicRarity {
    COMMON,
    RARE,
    LEGENDARY
}

enum class RelicId {
    MIDAS_TOUCH,
    VAPORIZER_BOMB,
    OVERHANG_DRILL,
    CHRONO_STABILIZER,
    QUAD_SURGE,
    DOUBLE_HOLD,
    GOLDEN_I_BAG,
    LASER_SWEEP,
    GRAVITY_ANCHOR
}

data class Relic(
    val id: RelicId,
    val name: String,
    val description: String,
    val icon: String,
    val rarity: RelicRarity
) {
    companion object {
        val ALL_RELICS = listOf(
            Relic(
                id = RelicId.MIDAS_TOUCH,
                name = "Midas Touch",
                description = "25% of spawned blocks turn Gold. Clearing them awards +400 bonus points each.",
                icon = "sparkle",
                rarity = RelicRarity.COMMON
            ),
            Relic(
                id = RelicId.OVERHANG_DRILL,
                name = "Overhang Drill",
                description = "Hard drops drill through 1 obstructed overhang block directly beneath the landing site.",
                icon = "drill",
                rarity = RelicRarity.RARE
            ),
            Relic(
                id = RelicId.CHRONO_STABILIZER,
                name = "Chrono Buffer",
                description = "Expands Lock Delay to 850ms and permits up to 25 rotation/movement resets.",
                icon = "hourglass",
                rarity = RelicRarity.COMMON
            ),
            Relic(
                id = RelicId.QUAD_SURGE,
                name = "Quad Surge",
                description = "4-line clears award 3x points and instantly vaporize the bottom-most row.",
                icon = "bolt",
                rarity = RelicRarity.LEGENDARY
            ),
            Relic(
                id = RelicId.DOUBLE_HOLD,
                name = "Dual Hold Chamber",
                description = "Enables holding pieces twice before requiring a lock.",
                icon = "dual_hold",
                rarity = RelicRarity.RARE
            ),
            Relic(
                id = RelicId.GOLDEN_I_BAG,
                name = "Beam Conduit",
                description = "Injects an additional straight Beam block into every 7-piece bag cycle.",
                icon = "diamond",
                rarity = RelicRarity.LEGENDARY
            ),
            Relic(
                id = RelicId.VAPORIZER_BOMB,
                name = "Kinetic Core",
                description = "Hard dropping from 10+ rows height triggers a mini shockwave clearing adjacent blocks.",
                icon = "bomb",
                rarity = RelicRarity.RARE
            ),
            Relic(
                id = RelicId.LASER_SWEEP,
                name = "Laser Cleaner",
                description = "Vaporizes the bottom-most row automatically after completing each floor.",
                icon = "laser",
                rarity = RelicRarity.COMMON
            ),
            Relic(
                id = RelicId.GRAVITY_ANCHOR,
                name = "Gravity Anchor",
                description = "Soft drop moves at terminal hyper-speed and scores 3 points per cell.",
                icon = "anchor",
                rarity = RelicRarity.COMMON
            )
        )
    }
}
