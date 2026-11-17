package com.seniordev.neonstack.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class LeaderboardRepository(context: Context) {

    companion object {
        private const val PREFS_NAME = "neon_stack_leaderboard_prefs"
        private const val KEY_LEADERBOARD_JSON = "leaderboard_json"
        private const val MAX_ENTRIES = 10
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    init {
        if (!prefs.contains(KEY_LEADERBOARD_JSON)) {
            seedDefaultArcadeRecords()
        }
    }

    private fun seedDefaultArcadeRecords() {
        val defaultList = listOf(
            LeaderboardEntry(1, "MAX", 120_000, 10, 95, "Retro Hall"),
            LeaderboardEntry(2, "CYB", 85_000, 8, 72, "Retro Hall"),
            LeaderboardEntry(3, "NEO", 64_000, 6, 58, "Retro Hall"),
            LeaderboardEntry(4, "ACE", 42_000, 5, 41, "Retro Hall"),
            LeaderboardEntry(5, "PRO", 25_000, 3, 24, "Retro Hall")
        )
        saveEntries(defaultList)
    }

    fun getTopEntries(): List<LeaderboardEntry> {
        val jsonString = prefs.getString(KEY_LEADERBOARD_JSON, null) ?: return emptyList()
        val list = mutableListOf<LeaderboardEntry>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    LeaderboardEntry(
                        rank = i + 1,
                        playerName = obj.getString("playerName"),
                        score = obj.getLong("score"),
                        level = obj.getInt("level"),
                        lines = obj.getInt("lines"),
                        dateString = obj.getString("dateString")
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    /**
     * Checks if a score qualifies for the Top 10 leaderboard.
     */
    fun isQualifyingScore(score: Long): Boolean {
        if (score <= 0) return false
        val current = getTopEntries()
        if (current.size < MAX_ENTRIES) return true
        return score > current.last().score
    }

    /**
     * Inserts a new score, sorts descending, trims to top 10 and returns rank (1-based) or null.
     */
    fun recordScore(
        score: Long,
        level: Int,
        lines: Int,
        playerName: String = "YOU"
    ): Int? {
        if (!isQualifyingScore(score)) return null

        val current = getTopEntries().toMutableList()
        val dateStr = dateFormat.format(Date())

        val newEntry = LeaderboardEntry(
            rank = 0,
            playerName = playerName.take(8).uppercase(),
            score = score,
            level = level,
            lines = lines,
            dateString = dateStr
        )
        current.add(newEntry)

        // Sort descending by score, breaking ties by lines cleared
        current.sortWith(compareByDescending<LeaderboardEntry> { it.score }.thenByDescending { it.lines })

        // Trim to MAX_ENTRIES
        val trimmed = current.take(MAX_ENTRIES)
        val rankedList = trimmed.mapIndexed { index, entry ->
            entry.copy(rank = index + 1)
        }

        saveEntries(rankedList)

        // Find rank of the new entry
        val rankAchieved = rankedList.indexOfFirst {
            it.score == score && it.level == level && it.lines == lines && it.playerName == newEntry.playerName
        }
        return if (rankAchieved != -1) rankAchieved + 1 else null
    }

    private fun saveEntries(entries: List<LeaderboardEntry>) {
        val jsonArray = JSONArray()
        for (entry in entries) {
            val obj = JSONObject().apply {
                put("playerName", entry.playerName)
                put("score", entry.score)
                put("level", entry.level)
                put("lines", entry.lines)
                put("dateString", entry.dateString)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_LEADERBOARD_JSON, jsonArray.toString()).apply()
    }

    fun clearLeaderboard() {
        prefs.edit().remove(KEY_LEADERBOARD_JSON).apply()
        seedDefaultArcadeRecords()
    }
}
