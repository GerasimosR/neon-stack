package com.seniordev.neonstack.data

data class LeaderboardEntry(
    val rank: Int,
    val playerName: String,
    val score: Long,
    val level: Int,
    val lines: Int,
    val dateString: String
)
