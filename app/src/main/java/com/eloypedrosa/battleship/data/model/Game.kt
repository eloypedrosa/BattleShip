package com.eloypedrosa.battleship.data.model

data class Game(
    val gameId: String = "",
    val player1: Player = Player(),
    val player2: Player? = null,
    val currentTurnPlayerId: String = "",
    val winnerId: String? = null,
    val status: String = "SETUP" // WAITING, PLAYING, FINISHED
)

val status: GameStatus = GameStatus.SETUP