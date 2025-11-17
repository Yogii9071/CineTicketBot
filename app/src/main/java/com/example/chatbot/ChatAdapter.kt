package com.example.chatbot

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatbot.databinding.*

class ChatAdapter(
    private val onTileClick: (TileItem) -> Unit,
    private val onPaymentClicked: (Int, Map<String, String>) -> Unit = { _, _ -> }
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val messages = mutableListOf<ChatMessage>()
    private val tilesAdapter = TilesAdapter { tile ->
        onTileClick(tile)
    }

    companion object {
        private const val TYPE_USER = 0
        private const val TYPE_BOT = 1
        private const val TYPE_WITH_TILES = 2
        private const val TYPE_PAYMENT = 3
    }

    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    fun addMessageWithTiles(message: String, tiles: List<TileItem>) {
        messages.add(ChatMessage("Bot", message, type = TYPE_WITH_TILES, tileItems = tiles))
        notifyItemInserted(messages.size - 1)
    }

    fun addPaymentMessage(price: Int, details: Map<String, String>) {
        val paymentTile = TileItem(
            id = "payment_${System.currentTimeMillis()}",
            title = "💳 Make Payment ₹$price",
            type = TileType.PAYMENT,
            data = mapOf("price" to price, "details" to details)
        )
        messages.add(ChatMessage("Bot", "Please confirm to proceed with payment:", type = TYPE_PAYMENT, price = price, bookingDetails = details))
        notifyItemInserted(messages.size - 1)
    }

    fun addSuccessMessage() {
        val viewTicketTile = TileItem(
            id = "ticket_${System.currentTimeMillis()}",
            title = "🎟️ View Ticket",
            type = TileType.VIEW_TICKET
        )
        messages.add(ChatMessage("Bot", "Thank you for your purchase! Your ticket is ready 🎟️", type = TYPE_WITH_TILES, tileItems = listOf(viewTicketTile)))
        notifyItemInserted(messages.size - 1)
    }

    override fun getItemViewType(position: Int): Int {
        return messages[position].type
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_USER -> {
                val binding = ItemMessageUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                UserViewHolder(binding)
            }
            TYPE_WITH_TILES -> {
                val binding = ItemTilesContainerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                TilesViewHolder(binding)
            }
            TYPE_PAYMENT -> {
                val binding = ItemPaymentTileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                PaymentViewHolder(binding)
            }
            else -> {
                val binding = ItemMessageBotBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                BotViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when (holder) {
            is UserViewHolder -> holder.bind(message)
            is BotViewHolder -> holder.bind(message)
            is TilesViewHolder -> holder.bind(message.tileItems)
            is PaymentViewHolder -> holder.bind(message)
        }
    }

    override fun getItemCount() = messages.size

    inner class UserViewHolder(private val binding: ItemMessageUserBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessage) {
            binding.userMessage.text = message.message
        }
    }

    inner class BotViewHolder(private val binding: ItemMessageBotBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessage) {
            binding.botMessage.text = message.message
        }
    }

    inner class TilesViewHolder(private val binding: ItemTilesContainerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(tileItems: List<TileItem>) {
            binding.tilesRecyclerView.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = tilesAdapter
            }
            tilesAdapter.updateTiles(tileItems)
        }
    }

    inner class PaymentViewHolder(private val binding: ItemPaymentTileBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: ChatMessage) {
            val details = message.bookingDetails ?: emptyMap()

            binding.bookingDetails.text = buildString {
                appendLine("Movie: ${details["movie"]}")
                appendLine("Cinema: ${details["cinema"]}")
                appendLine("Time: ${details["time"]}")
                appendLine("Seats: ${details["seats"]}")
                append("Price: ₹${message.price}")
            }

            binding.paymentButton.text = "💳 Make Payment ₹${message.price ?: 0}"

            binding.paymentButton.setOnClickListener {
                val price = message.price
                if (price != null) {
                    onPaymentClicked(price, details)
                }
            }
        }
    }
}