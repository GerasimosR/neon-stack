package com.seniordev.neonstack

import com.seniordev.neonstack.data.LeaderboardEntry
import org.junit.Assert.*
import org.junit.Test

class LeaderboardEntryTest {

    @Test
    fun testLeaderboardEntryCreation() {
        val entry = LeaderboardEntry(
            rank = 1,
            playerName = "ACE",
            score = 150_000L,
            level = 12,
            lines = 115,
            dateString = "17 Nov 2026"
        )
        assertEquals(1, entry.rank)
        assertEquals("ACE", entry.playerName)
        assertEquals(150_000L, entry.score)
        assertEquals(12, entry.level)
        assertEquals(115, entry.lines)
    }

    @Test
    fun testLeaderboardSortingLogic() {
        val entries = mutableListOf(
            LeaderboardEntry(0, "PLAYER_C", 50_000, 5, 45, "Date"),
            LeaderboardEntry(0, "PLAYER_A", 120_000, 10, 95, "Date"),
            LeaderboardEntry(0, "PLAYER_B", 85_000, 8, 72, "Date")
        )

        entries.sortWith(compareByDescending<LeaderboardEntry> { it.score }.thenByDescending { it.lines })
        val ranked = entries.mapIndexed { idx, e -> e.copy(rank = idx + 1) }

        assertEquals("PLAYER_A", ranked[0].playerName)
        assertEquals(1, ranked[0].rank)
        assertEquals("PLAYER_B", ranked[1].playerName)
        assertEquals(2, ranked[1].rank)
        assertEquals("PLAYER_C", ranked[2].playerName)
        assertEquals(3, ranked[2].rank)
    }
}
