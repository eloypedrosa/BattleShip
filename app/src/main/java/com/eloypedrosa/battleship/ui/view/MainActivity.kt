package com.eloypedrosa.battleship.ui.view

import android.content.ClipData
import android.content.ClipDescription
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.DragEvent
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eloypedrosa.battleship.databinding.ActivityMainBinding
import com.eloypedrosa.battleship.data.model.CellState
import com.eloypedrosa.battleship.ui.adapter.BoardAdapter
import com.eloypedrosa.battleship.ui.adapter.ShipSelectorAdapter
import com.eloypedrosa.battleship.ui.viewmodel.GameViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: GameViewModel by viewModels()
    private lateinit var enemyBoardAdapter: BoardAdapter
    private lateinit var myBoardAdapter: BoardAdapter
    private lateinit var shipSelectorAdapter: ShipSelectorAdapter
    private var myPlayerId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        val gameId = intent.getStringExtra("GAME_ID")
        myPlayerId = intent.getStringExtra("PLAYER_ID")

        if (gameId != null && myPlayerId != null) {
            setupRecyclerViews()
            viewModel.initGame(gameId, myPlayerId!!)
            observeViewModel()
        } else {
            finish()
        }
    }

    private fun setupRecyclerViews() {
        // CORRECCIÓN: Tableros de 6 columnas
        enemyBoardAdapter = BoardAdapter(onCellClick = { x, y -> viewModel.onCellClicked(x, y) })
        binding.rvEnemyBoard.layoutManager = GridLayoutManager(this, 6)
        binding.rvEnemyBoard.adapter = enemyBoardAdapter

        myBoardAdapter = BoardAdapter(
            onCellClick = { _, _ -> },
            onCellLongClick = { shipId, cellView -> startShipDragFromBoard(shipId, cellView) }
        )
        binding.rvMyBoardSetup.layoutManager = GridLayoutManager(this, 6)
        binding.rvMyBoardSetup.adapter = myBoardAdapter

        shipSelectorAdapter = ShipSelectorAdapter(
            onShipLongClick = { shipId, view -> startShipDrag(shipId, view) },
            onShipClick = { shipId -> viewModel.toggleShipOrientation(shipId) }
        )
        binding.rvShipSelector.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvShipSelector.adapter = shipSelectorAdapter
        binding.rvMyBoardSetup.setOnDragListener(MyBoardDragListener())
    }

    private fun createShipDragView(shipId: String): View {
        val shipsToPlace = viewModel.shipsToPlace.value
        val ship = shipsToPlace.find { it.id == shipId }

        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val boardWidth = screenWidth - (32 * displayMetrics.density) // Approx padding
        val cellSizePixel = (boardWidth / 6).toInt() // CORRECCIÓN: Celda dinámica

        val shipSize = ship?.size ?: viewModel.currentLocalShips.find { it.id == shipId }?.size ?: 1
        val orientation = ship?.orientation ?: viewModel.getShipOrientation(shipId)

        val width = if (orientation == "horizontal") cellSizePixel * shipSize else cellSizePixel
        val height = if (orientation == "horizontal") cellSizePixel else cellSizePixel * shipSize

        val dragView = View(this).apply {
            layoutParams = ViewGroup.LayoutParams(width, height)
            setBackgroundColor(Color.parseColor("#4CAF50"))
        }
        dragView.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
        )
        dragView.layout(0, 0, width, height)
        return dragView
    }

    private fun startShipDrag(shipId: String, view: View): Boolean {
        val dragView = createShipDragView(shipId)
        val item = ClipData.Item(shipId as? CharSequence)
        val dragData = ClipData(shipId, arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN), item)

        // CAMBIO AQUÍ: Usamos nuestra clase personalizada
        val shadowBuilder = ShipDragShadowBuilder(dragView)

        return view.startDragAndDrop(dragData, shadowBuilder, null, 0)
    }

    private fun startShipDragFromBoard(shipId: String, cellView: View): Boolean {
        // Reutilizamos la misma lógica
        return startShipDrag(shipId, cellView)
    }

    private inner class MyBoardDragListener : View.OnDragListener {
        override fun onDrag(v: View, event: DragEvent): Boolean {
            val view = v as? RecyclerView ?: return false
            when (event.action) {
                DragEvent.ACTION_DROP -> {
                    val shipId = event.clipData.getItemAt(0).text.toString()
                    val targetView = view.findChildViewUnder(event.x, event.y)
                    val position = targetView?.let { view.getChildAdapterPosition(it) } ?: return false

                    // CORRECCIÓN: Cálculo 6 columnas
                    val startX = position / 6
                    val startY = position % 6
                    viewModel.placeShip(shipId, startX, startY, viewModel.getShipOrientation(shipId))
                    return true
                }
                DragEvent.ACTION_DRAG_ENTERED -> v.setBackgroundColor(Color.LTGRAY)
                DragEvent.ACTION_DRAG_EXITED, DragEvent.ACTION_DRAG_ENDED -> v.setBackgroundColor(Color.TRANSPARENT)
            }
            return true
        }
    }

    private class ShipDragShadowBuilder(view: View) : View.DragShadowBuilder(view) {
        override fun onProvideShadowMetrics(outShadowSize: android.graphics.Point, outShadowTouchPoint: android.graphics.Point) {

            outShadowSize.set(view.width, view.height)
            val cellSize = if (view.width < view.height) view.width else view.height
            outShadowTouchPoint.set(cellSize / 2, cellSize / 2)
        }
    }
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.gameState.collect { game ->
                if (game != null) {
                    binding.gameStatus = game.status
                    val isPlayer1 = game.player1.id == myPlayerId
                    val myPlayer = if (isPlayer1) game.player1 else game.player2
                    val opponent = if (isPlayer1) game.player2 else game.player1

                    if (game.status == "SETUP") {
                        val isMeReady = myPlayer?.ships?.all { it.positions.isNotEmpty() } == true
                        val isOpReady = opponent?.ships?.all { it.positions.isNotEmpty() } == true
                        val count = (if (isMeReady) 1 else 0) + (if (isOpReady) 1 else 0)
                        binding.tvSetupStatus.text = "Jugadores listos: $count/2"

                        launch { viewModel.setupBoard.collect { myBoardAdapter.submitList(it) } }
                        launch { viewModel.shipsToPlace.collect { shipSelectorAdapter.submitList(it) } }
                    } else if (game.status == "PLAYING" || game.status == "FINISHED") {
                        binding.isMyTurn = game.currentTurnPlayerId == myPlayerId
                        opponent?.let {
                            val maskedBoard = it.board.map { cell ->
                                if (cell.state == CellState.SHIP) cell.copy(state = CellState.WATER) else cell
                            }
                            enemyBoardAdapter.submitList(maskedBoard)
                            binding.tvScore.text = "Puntos: ${myPlayer?.score ?: 0}"
                        }
                        if (game.status == "FINISHED") {
                            val dest = if (game.winnerId == myPlayerId) WinnerActivity::class.java else LoserActivity::class.java
                            if (!isFinishing) {
                                startActivity(Intent(this@MainActivity, dest).putExtra("FINAL_SCORE", myPlayer?.score ?: 0))
                                finish()
                            }
                        }
                    }
                }
            }
        }
    }
}