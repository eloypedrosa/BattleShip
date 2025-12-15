package com.eloypedrosa.battleship.data.model

data class Ship(
    val id: String = "",
    val size: Int = 0,
    // ¡CAMBIO CLAVE AQUÍ! Usar Coordinate en lugar de Pair.
    val positions: List<Coordinate> = emptyList(),
    var hits: Int = 0
) {
    fun isSunk() = hits >= size
}