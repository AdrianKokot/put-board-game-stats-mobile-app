package com.example.boardgamestats.models

data class BoardGameStats(
    val highestScore: Int,
    val avgScore: Int,
    val lowestScore: Int,
    val lastPlay: Long,
    val avgPlaytime: Long,
    val playsCount: Int
)
