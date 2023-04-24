package com.example.boardgamestats.models

data class PlayerStats(
    val name: String,
    val highestScore: Int,
    val playCount: Int,
    val winCount: Int,
)