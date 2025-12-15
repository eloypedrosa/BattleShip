package com.eloypedrosa.battleship.data.model

enum class CellState {
    WATER,  // Agua sin descubrir
    SHIP,   // Barco (visible solo para el dueño, invisible para enemigo hasta HIT)
    MISS,   // Agua descubierta (disparo fallido)
    HIT,    // Barco tocado
    SUNK    // Barco hundido
}