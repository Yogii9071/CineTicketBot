package com.example.chatbot

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.chatbot.databinding.ItemTileBinding

class TilesAdapter(
    private val onTileClick: (TileItem) -> Unit
) : RecyclerView.Adapter<TilesAdapter.TileViewHolder>() {

    private val tiles = mutableListOf<TileItem>()

    fun updateTiles(newTiles: List<TileItem>) {
        tiles.clear()
        tiles.addAll(newTiles)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TileViewHolder {
        val binding = ItemTileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TileViewHolder, position: Int) {
        holder.bind(tiles[position])
    }

    override fun getItemCount() = tiles.size

    inner class TileViewHolder(private val binding: ItemTileBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(tile: TileItem) {
            binding.tileText.text = tile.title
            binding.tileCard.isSelected = tile.isSelected

            // Set different styles based on type
            when (tile.type) {
                TileType.MOVIE -> {
                    binding.tileCard.setCardBackgroundColor(
                        ContextCompat.getColor(binding.root.context, R.color.movie_tile_bg)
                    )
                }
                TileType.CINEMA -> {
                    binding.tileCard.setCardBackgroundColor(
                        ContextCompat.getColor(binding.root.context, R.color.cinema_tile_bg)
                    )
                }
                TileType.SHOWTIME -> {
                    binding.tileCard.setCardBackgroundColor(
                        ContextCompat.getColor(binding.root.context, R.color.showtime_tile_bg)
                    )
                }
                TileType.PAYMENT -> {
                    binding.tileCard.setCardBackgroundColor(
                        ContextCompat.getColor(binding.root.context, R.color.payment_tile_bg)
                    )
                    // No icons - emoji is already in the title "💳 Make Payment"
                }
                TileType.VIEW_TICKET -> {
                    binding.tileCard.setCardBackgroundColor(
                        ContextCompat.getColor(binding.root.context, R.color.success_tile_bg)
                    )
                    binding.tileText.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_ticket, 0, 0, 0
                    )
                }
                else -> {
                    binding.tileCard.setCardBackgroundColor(
                        ContextCompat.getColor(binding.root.context, R.color.default_tile_bg)
                    )
                }
            }

            binding.tileCard.setOnClickListener {
                onTileClick(tile)
            }
        }
    }
}