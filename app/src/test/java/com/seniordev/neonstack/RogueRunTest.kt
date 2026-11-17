package com.seniordev.neonstack

import com.seniordev.neonstack.engine.*
import com.seniordev.neonstack.engine.rogue.*
import org.junit.Assert.*
import org.junit.Test

class RogueRunTest {

    @Test
    fun testRogueFloorProgressionAndQuota() {
        val rogue = RogueRunState()
        assertEquals("Starts at Floor 1", 1, rogue.currentFloor)
        assertEquals("Target lines for Floor 1 is 8", 8, rogue.targetLinesForFloor)
        assertEquals("Lines remaining starts at 8", 8, rogue.linesRemainingOnFloor)
        assertFalse("Floor 1 is not a boss floor", rogue.isBossFloor)

        // Clear partial lines
        val floorDone1 = rogue.addLines(3)
        assertFalse("Floor 1 not done after 3 lines", floorDone1)
        assertEquals("Remaining lines should be 5", 5, rogue.linesRemainingOnFloor)

        // Complete Floor 1
        val floorDone2 = rogue.addLines(5)
        assertTrue("Floor 1 should be complete after 8 total lines", floorDone2)

        // Advance to Floor 2
        rogue.advanceToNextFloor()
        assertEquals("Should be on Floor 2", 2, rogue.currentFloor)
        assertEquals("Target lines for Floor 2 is 10 (8 + 2 * (2-1))", 10, rogue.targetLinesForFloor)
        assertEquals("Lines remaining on Floor 2 starts at 10", 10, rogue.linesRemainingOnFloor)

        // Fast forward to Floor 3 (Boss Floor: every 3 floors)
        rogue.addLines(rogue.linesRemainingOnFloor)
        rogue.advanceToNextFloor()
        assertEquals("Should be on Floor 3", 3, rogue.currentFloor)
        assertTrue("Floor 3 must be flagged as a Boss Floor", rogue.isBossFloor)
    }

    @Test
    fun testRelicDraftAndInventory() {
        val rogue = RogueRunState()
        val draft = rogue.rollDraftOptions(3)
        assertEquals("Draft should offer 3 options", 3, draft.size)
        assertEquals("Draft options must all be unique", 3, draft.toSet().size)

        val selected = draft[0]
        rogue.addRelic(selected)
        assertTrue("Rogue state must contain selected relic", rogue.hasRelic(selected.id))
        assertEquals("Active relics count should be 1", 1, rogue.activeRelics.size)

        // Ensure drafted relic is excluded from subsequent rolls
        val draft2 = rogue.rollDraftOptions(3)
        assertFalse("Already possessed relic must not appear in future drafts", draft2.contains(selected))
    }

    @Test
    fun testEngineRogueModeDoubleHoldRelic() {
        val engine = NeonStackEngine()
        engine.mode = GameMode.ROGUE
        engine.start()

        // Standard holds in classic or without relic is 1
        assertEquals("Initially holdsRemaining is 1", 1, engine.holdsRemaining)
        assertTrue(engine.canHold)
        engine.hold()
        assertFalse("Cannot hold a second time without relic", engine.canHold)

        // Draft Dual Hold Chamber
        val dualHold = Relic.ALL_RELICS.first { it.id == RelicId.DOUBLE_HOLD }
        engine.applyRelicDraft(dualHold)

        // After draft, hold count should reset to 2
        assertEquals("With Dual Hold, holdsRemaining must be 2", 2, engine.holdsRemaining)
        assertTrue(engine.canHold)
        engine.hold()
        assertEquals("After first hold, 1 hold should remain", 1, engine.holdsRemaining)
        assertTrue(engine.canHold)
        engine.hold()
        assertEquals("After second hold, 0 holds remain", 0, engine.holdsRemaining)
        assertFalse(engine.canHold)
    }

    @Test
    fun testGoldenIConduitRelic() {
        val engine = NeonStackEngine()
        engine.mode = GameMode.ROGUE
        engine.start()

        assertFalse("Initially extra I conduit is off", engine.randomizer.extraIConduit)
        val goldenI = Relic.ALL_RELICS.first { it.id == RelicId.GOLDEN_I_BAG }
        engine.applyRelicDraft(goldenI)

        assertTrue("Golden I relic must activate extra I piece conduit in randomizer", engine.randomizer.extraIConduit)
    }

    @Test
    fun testRelicCatalogIntegrity() {
        assertEquals("Relic catalog must contain 9 defined relics", 9, Relic.ALL_RELICS.size)
        val ids = Relic.ALL_RELICS.map { it.id }.toSet()
        assertEquals("All 9 RelicIds must be covered", RelicId.values().size, ids.size)

        for (relic in Relic.ALL_RELICS) {
            assertTrue("Relic name cannot be empty", relic.name.isNotBlank())
            assertTrue("Relic description cannot be empty", relic.description.isNotBlank())
            assertTrue("Relic icon cannot be empty", relic.icon.isNotBlank())
            assertNotNull("Relic rarity must be defined", relic.rarity)
        }
    }
}
