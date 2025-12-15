package com.eloypedrosa.battleship.data.model

data class Player(
    val id: String = "",
    // Tablero 8x8 inicializado
    val board: List<Cell> = List(36) { i -> Cell(i / 6, i % 6) },
    val ships: List<Ship> = emptyList(),
    var score: Int = 0
)