package com.eloypedrosa.battleship.data.model

data class User(
    val id: String = "",
    val username: String = "",
    val totalScore: Int = 0,
    val gamesWon: Int = 0
)