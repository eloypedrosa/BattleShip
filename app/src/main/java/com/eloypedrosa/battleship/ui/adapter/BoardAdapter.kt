package com.eloypedrosa.battleship.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.eloypedrosa.battleship.databinding.ItemCellBinding
import com.eloypedrosa.battleship.data.model.Cell
import com.eloypedrosa.battleship.data.model.CellState

class BoardAdapter(
    private val onCellClick: (Int, Int) -> Unit,
    private val onCellLongClick: ((String, View) -> Boolean)? = null
) : RecyclerView.Adapter<BoardAdapter.CellViewHolder>() {

    private var cells: List<Cell> = emptyList()

    fun submitList(newCells: List<Cell>) {
        cells = newCells
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CellViewHolder {
        val binding = ItemCellBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CellViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CellViewHolder, position: Int) {
        holder.bind(cells[position])
    }

    override fun getItemCount() = cells.size

    inner class CellViewHolder(private val binding: ItemCellBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(cell: Cell) {
            val color = when (cell.state) {
                CellState.WATER -> Color.parseColor("#BBDEFB")
                CellState.MISS -> Color.BLUE
                CellState.HIT -> Color.RED
                CellState.SUNK -> Color.DKGRAY
                CellState.SHIP -> Color.parseColor("#4CAF50")
            }

            binding.root.setBackgroundColor(color)
            binding.root.setOnClickListener { onCellClick(cell.x, cell.y) }

            if (onCellLongClick != null && cell.state == CellState.SHIP && cell.shipId != null) {
                binding.root.setOnLongClickListener {
                    onCellLongClick.invoke(cell.shipId!!, binding.root)
                }
            } else {
                binding.root.setOnLongClickListener(null)
            }
        }
    }
}
