package com.eloypedrosa.battleship.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eloypedrosa.battleship.data.model.*
import com.eloypedrosa.battleship.data.repository.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GameViewModel : ViewModel() {
    private val repository = GameRepository()
    private val _gameState = MutableStateFlow<Game?>(null)
    val gameState: StateFlow<Game?> = _gameState
    private var myPlayerId: String = ""

    private val _setupBoard = MutableStateFlow<List<Cell>>(emptyList())
    val setupBoard: StateFlow<List<Cell>> = _setupBoard
    private val _shipsToPlace = MutableStateFlow<List<ShipToPlace>>(getDefaultShips())
    val shipsToPlace: StateFlow<List<ShipToPlace>> = _shipsToPlace
    var currentLocalShips: MutableList<Ship> = mutableListOf()

    fun initGame(gameId: String, playerId: String) {
        myPlayerId = playerId
        viewModelScope.launch {
            repository.getGameFlow(gameId).collect { game ->
                _gameState.value = game
                if (game?.status == "SETUP") {
                    val myPlayer = if (game.player1.id == myPlayerId) game.player1 else game.player2
                    myPlayer?.let {
                        _setupBoard.value = it.board
                        if (currentLocalShips.isEmpty()) currentLocalShips.addAll(it.ships)
                        _shipsToPlace.value = getDefaultShips().map { shipToPlace ->
                            val placed = it.ships.find { s -> s.id == shipToPlace.id }?.positions?.isNotEmpty() ?: false
                            shipToPlace.copy(isPlaced = placed)
                        }
                    }
                }
            }
        }
    }

    fun placeShip(shipId: String, startX: Int, startY: Int, orientation: String): Boolean {
        val shipToPlace = _shipsToPlace.value.find { it.id == shipId } ?: return false
        val board = _setupBoard.value.toMutableList()
        val shipSize = shipToPlace.size
        val newPositions = mutableListOf<Coordinate>()

        for (i in 0 until shipSize) {
            val x = if (orientation == "vertical") startX + i else startX
            val y = if (orientation == "horizontal") startY + i else startY

            // LÍMITES 6x6
            if (x < 0 || x >= 6 || y < 0 || y >= 6) return false

            val cellIndex = board.indexOfFirst { it.x == x && it.y == y }
            if (cellIndex == -1) return false
            if (board[cellIndex].state == CellState.SHIP && board[cellIndex].shipId != shipId) return false
            newPositions.add(Coordinate(x, y))
        }

        val existingShip = currentLocalShips.find { it.id == shipId }
        existingShip?.positions?.forEach { coord ->
            val index = board.indexOfFirst { it.x == coord.x && it.y == coord.y }
            if (index != -1) board[index] = board[index].copy(state = CellState.WATER, shipId = null)
        }

        newPositions.forEach { coord ->
            val index = board.indexOfFirst { it.x == coord.x && it.y == coord.y }
            if (index != -1) board[index] = board[index].copy(state = CellState.SHIP, shipId = shipId)
        }

        val updatedShip = existingShip?.copy(positions = newPositions) ?: Ship(shipId, shipSize, newPositions)
        currentLocalShips.removeAll { it.id == shipId }
        currentLocalShips.add(updatedShip)

        _setupBoard.value = board.toList()
        _shipsToPlace.value = _shipsToPlace.value.map { if (it.id == shipId) it.copy(isPlaced = true) else it }
        return true
    }

    fun finalizeSetup() {
        if (currentLocalShips.all { it.positions.isNotEmpty() }) {
            viewModelScope.launch {
                repository.savePlayerSetup(_gameState.value!!.gameId, myPlayerId, _setupBoard.value, currentLocalShips.toList())
            }
        }
    }

    fun toggleShipOrientation(shipId: String) {
        _shipsToPlace.value = _shipsToPlace.value.map { if (it.id == shipId) it.toggleOrientation() else it }
    }
    fun getShipOrientation(shipId: String) = _shipsToPlace.value.find { it.id == shipId }?.orientation ?: "horizontal"

    fun onCellClicked(x: Int, y: Int) {
        val game = _gameState.value ?: return
        if (game.status == "PLAYING" && game.currentTurnPlayerId == myPlayerId) {
            viewModelScope.launch { repository.makeMove(game.gameId, myPlayerId, x, y) }
        }
    }
}