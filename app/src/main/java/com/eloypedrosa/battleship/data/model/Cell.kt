package com.eloypedrosa.battleship.data.model

data class Cell(
    val x: Int = 0,
    val y: Int = 0,
    var state: CellState = CellState.WATER,
    var shipId: String? = null // Null si és aigua
)