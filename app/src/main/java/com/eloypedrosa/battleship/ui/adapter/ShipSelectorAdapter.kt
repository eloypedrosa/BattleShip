package com.eloypedrosa.battleship.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.eloypedrosa.battleship.R // Necesitarás este R para el color
import com.eloypedrosa.battleship.data.model.ShipToPlace
import com.eloypedrosa.battleship.databinding.ItemShipSelectorBinding

class ShipSelectorAdapter(
    private val onShipLongClick: (shipId: String, view: View) -> Boolean,
    private val onShipClick: (shipId: String) -> Unit // NUEVO CLICK
) : RecyclerView.Adapter<ShipSelectorAdapter.ShipViewHolder>() {

    private var ships: List<ShipToPlace> = emptyList()

    fun submitList(newShips: List<ShipToPlace>) {
        ships = newShips.filter { !it.isPlaced }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShipViewHolder {
        val binding = ItemShipSelectorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ShipViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ShipViewHolder, position: Int) {
        holder.bind(ships[position])
    }

    override fun getItemCount() = ships.size

    inner class ShipViewHolder(private val binding: ItemShipSelectorBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(ship: ShipToPlace) {
            binding.tvShipName.text = "${ship.id} (${ship.size})"

            val density = binding.root.context.resources.displayMetrics.density
            val cellDP = 30 * density

            val params = binding.viewShipIndicator.layoutParams

            // Lógica para cambiar la dimensión visual según la orientación
            if (ship.orientation == "horizontal") {
                params.width = (ship.size * cellDP).toInt()
                params.height = cellDP.toInt()
            } else { // vertical
                params.width = cellDP.toInt()
                params.height = (ship.size * cellDP).toInt()
            }

            binding.viewShipIndicator.layoutParams = params
            // Forzar el re-renderizado para asegurar que las dimensiones son correctas antes del drag.
            binding.viewShipIndicator.requestLayout()

            // --- IMPLEMENTACIÓN DEL ONCLICK (Rotación) ---
            binding.root.setOnClickListener {
                onShipClick(ship.id) // Llama al ViewModel para rotar
            }

            // Iniciar arrastre con pulsación larga, usando la vista del indicador
            binding.viewShipIndicator.setOnLongClickListener {
                onShipLongClick(ship.id, binding.viewShipIndicator)
            }

            // Aseguramos que el root no tenga un LongClickListener, solo el indicador
            binding.root.setOnLongClickListener(null)
        }
    }
}