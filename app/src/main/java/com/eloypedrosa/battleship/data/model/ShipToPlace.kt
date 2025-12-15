package com.eloypedrosa.battleship.data.model

data class ShipToPlace(
    val id: String,
    val size: Int,
    val orientation: String = "horizontal", // 'horizontal' o 'vertical'
    var isPlaced: Boolean = false

){
    fun toggleOrientation(): ShipToPlace {
        val newOrientation = if (orientation == "horizontal") "vertical" else "horizontal"
        return this.copy(orientation = newOrientation)
    }
}

fun getDefaultShips(): List<ShipToPlace> {
    return listOf(
        ShipToPlace("cruiser", 3),
        ShipToPlace("submarine", 3),
        ShipToPlace("destroyer", 2)
    )
}