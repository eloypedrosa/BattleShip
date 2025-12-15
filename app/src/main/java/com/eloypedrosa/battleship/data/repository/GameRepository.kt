package com.eloypedrosa.battleship.data.repository

import com.eloypedrosa.battleship.data.model.*
import com.eloypedrosa.battleship.utils.UsernameGenerator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

class GameRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val gamesRef = db.collection("games")
    private val usersRef = db.collection("users")

    private fun getDefaultShips(): List<Ship> {
        return listOf(
            Ship("cruiser", 3),
            Ship("submarine", 3),
            Ship("destroyer", 2)
        )
    }

    suspend fun loginAnonymously(): String? {
        return try {
            val result = auth.signInAnonymously().await()
            result.user?.uid
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // --- GESTIÓN DE USUARIOS ---
    suspend fun createOrFetchUser(uid: String) {
        val docRef = usersRef.document(uid)
        val snapshot = docRef.get().await()

        if (!snapshot.exists()) {
            val newUser = User(
                id = uid,
                username = UsernameGenerator.generate(),
                totalScore = 0,
                gamesWon = 0
            )
            docRef.set(newUser).await()
        }
    }

    fun getLeaderboard(): Flow<List<User>> = callbackFlow {
        val subscription = usersRef
            .orderBy("totalScore", Query.Direction.DESCENDING)
            .limit(20)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val users = snapshot.toObjects(User::class.java)
                    trySend(users)
                }
            }
        awaitClose { subscription.remove() }
    }

    // --- GESTIÓN DE PARTIDA ---
    suspend fun findOrCreateGame(playerId: String): String {
        val snapshot = gamesRef
            .whereEqualTo("status", "WAITING")
            .limit(1)
            .get()
            .await()

        if (!snapshot.isEmpty) {
            val gameDocRef = snapshot.documents.first().reference
            val gameId = gameDocRef.id

            return try {
                db.runTransaction { transaction ->
                    val gameSnapshot = transaction.get(gameDocRef)
                    val game = gameSnapshot.toObject(Game::class.java)

                    if (game != null && game.status == "WAITING" && game.player1.id != playerId && game.player2 == null) {
                        // TABLERO 6x6 (36 celdas)
                        val initialBoard = List(36) { i -> Cell(i / 6, i % 6) }
                        val emptyShips = getDefaultShips()
                        val player2 = Player(id = playerId, board = initialBoard, ships = emptyShips)

                        transaction.update(gameDocRef, mapOf("player2" to player2, "status" to "SETUP"))
                        gameId
                    } else {
                        throw Exception("Retry create")
                    }
                }.await()
            } catch (e: Exception) {
                createGame(playerId)
            }
        } else {
            return createGame(playerId)
        }
    }

    private suspend fun createGame(playerId: String): String {
        val emptyShips = getDefaultShips()
        // TABLERO 6x6 (36 celdas)
        val initialBoard = List(36) { i -> Cell(i / 6, i % 6) }
        val gameId = gamesRef.document().id

        val newGame = Game(
            gameId = gameId,
            player1 = Player(id = playerId, board = initialBoard, ships = emptyShips),
            status = "WAITING"
        )
        gamesRef.document(gameId).set(newGame).await()
        return gameId
    }

    suspend fun savePlayerSetup(gameId: String, playerId: String, finalizedBoard: List<Cell>, placedShips: List<Ship>) {
        val gameDocRef = gamesRef.document(gameId)
        db.runTransaction { transaction ->
            val gameSnapshot = transaction.get(gameDocRef)
            val game = gameSnapshot.toObject(Game::class.java) ?: throw Exception("Game error")

            if (game.status != "SETUP") throw Exception("Not in setup")

            val isPlayer1 = playerId == game.player1.id
            val playerToUpdate = if (isPlayer1) game.player1 else game.player2!!

            val updatedPlayer = Player(id = playerId, board = finalizedBoard, ships = placedShips, score = playerToUpdate.score)
            val playerKey = if (isPlayer1) "player1" else "player2"

            val selfReady = placedShips.all { it.positions.isNotEmpty() }
            val opponentShips = if (isPlayer1) game.player2?.ships else game.player1.ships
            val opponentReady = opponentShips != null && opponentShips.all { it.positions.isNotEmpty() }

            val updateMap = mutableMapOf<String, Any>(playerKey to updatedPlayer)

            if (selfReady && opponentReady && game.player2 != null) {
                val startingPlayerId = if (Random.nextBoolean()) game.player1.id else game.player2.id
                updateMap["status"] = "PLAYING"
                updateMap["currentTurnPlayerId"] = startingPlayerId
            }
            transaction.update(gameDocRef, updateMap as Map<String, Any>)
        }.await()
    }

    fun getGameFlow(gameId: String): Flow<Game?> = callbackFlow {
        val subscription = gamesRef.document(gameId).addSnapshotListener { s, _ -> if (s != null && s.exists()) trySend(s.toObject(Game::class.java)) }
        awaitClose { subscription.remove() }
    }

    suspend fun makeMove(gameId: String, attackerId: String, x: Int, y: Int) {
        val gameDocRef = gamesRef.document(gameId)

        db.runTransaction { transaction ->
            val game = transaction.get(gameDocRef).toObject(Game::class.java) ?: return@runTransaction
            if (game.status != "PLAYING" || game.currentTurnPlayerId != attackerId) return@runTransaction

            val isP1Attacking = attackerId == game.player1.id
            val defender = if (isP1Attacking) game.player2!! else game.player1
            val attacker = if (isP1Attacking) game.player1 else game.player2!!
            val defenderKey = if (isP1Attacking) "player2" else "player1"
            val attackerKey = if (isP1Attacking) "player1" else "player2"

            val targetIdx = defender.board.indexOfFirst { it.x == x && it.y == y }
            if (targetIdx == -1) return@runTransaction
            val targetCell = defender.board[targetIdx]

            if (targetCell.state == CellState.HIT || targetCell.state == CellState.MISS || targetCell.state == CellState.SUNK) return@runTransaction

            var points = 0
            val newDefBoard = defender.board.toMutableList()
            val newDefShips = defender.ships.toMutableList()

            if (targetCell.state == CellState.SHIP) {
                points = 1
                val shipIdx = newDefShips.indexOfFirst { it.id == targetCell.shipId }
                if (shipIdx != -1) {
                    val ship = newDefShips[shipIdx]
                    ship.hits += 1
                    if (ship.isSunk()) {
                        points = 2
                        ship.positions.forEach { c ->
                            val idx = newDefBoard.indexOfFirst { it.x == c.x && it.y == c.y }
                            if (idx != -1) newDefBoard[idx] = newDefBoard[idx].copy(state = CellState.SUNK)
                        }
                    } else {
                        newDefBoard[targetIdx] = newDefBoard[targetIdx].copy(state = CellState.HIT)
                    }
                }
            } else {
                newDefBoard[targetIdx] = newDefBoard[targetIdx].copy(state = CellState.MISS)
            }

            val newDefender = defender.copy(board = newDefBoard, ships = newDefShips)
            val newAttacker = attacker.copy(score = attacker.score + points)

            val allSunk = newDefender.ships.all { it.isSunk() }
            val newStatus = if (allSunk) "FINISHED" else "PLAYING"
            val winnerId = if (allSunk) attackerId else null
            val nextTurn = if (allSunk) "" else defender.id

            // --- ACTUALIZAR PUNTUACIÓN GLOBAL ---
            if (newStatus == "FINISHED") {
                val winnerRef = usersRef.document(attackerId)
                val loserRef = usersRef.document(defender.id)
                // Bonus de 10 puntos por ganar + score partida
                transaction.update(winnerRef, "totalScore", FieldValue.increment((newAttacker.score + 10).toLong()))
                transaction.update(winnerRef, "gamesWon", FieldValue.increment(1))
                // El perdedor solo se lleva sus puntos
                transaction.update(loserRef, "totalScore", FieldValue.increment(newDefender.score.toLong()))
            }

            transaction.update(gameDocRef, mapOf(
                "status" to newStatus,
                "currentTurnPlayerId" to nextTurn,
                "winnerId" to (winnerId ?: ""),
                attackerKey to newAttacker,
                defenderKey to newDefender
            ))
        }.await()
    }

    suspend fun getUser(uid: String): User? {
        return try {
            usersRef.document(uid).get().await().toObject(User::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}