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

    companion object {
        private const val TYPE_USER = ChatMessage.TYPE_USER
        private const val TYPE_BOT = ChatMessage.TYPE_BOT
        private const val TYPE_WITH_TILES = ChatMessage.TYPE_WITH_TILES
        private const val TYPE_PAYMENT = ChatMessage.TYPE_PAYMENT
    }

    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    fun addMessageWithTiles(message: String, tiles: List<TileItem>) {
        messages.add(
            ChatMessage(
                sender = "Bot",
                message = message,
                type = TYPE_WITH_TILES,
                tileItems = tiles
            )
        )
        notifyItemInserted(messages.size - 1)
    }

    fun addPaymentMessage(price: Int, details: Map<String, String>) {
        messages.add(
            ChatMessage(
                sender = "Bot",
                message = "Please confirm to proceed with payment:",
                type = TYPE_PAYMENT,
                price = price,
                bookingDetails = details
            )
        )
        notifyItemInserted(messages.size - 1)
    }

    fun addSuccessMessage() {
        val ticketTile = TileItem(
            id = "ticket_${System.currentTimeMillis()}",
            title = "🎟️ View Ticket",
            type = TileType.VIEW_TICKET
        )

        messages.add(
            ChatMessage(
                sender = "Bot",
                message = "Thank you for your purchase! Your ticket is ready 🎟️",
                type = TYPE_WITH_TILES,
                tileItems = listOf(ticketTile)
            )
        )
        notifyItemInserted(messages.size - 1)
    }

    override fun getItemViewType(position: Int): Int {
        return messages[position].type
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {

            TYPE_USER -> UserViewHolder(
                ItemMessageUserBinding.inflate(inflater, parent, false)
            )

            TYPE_BOT -> BotViewHolder(
                ItemMessageBotBinding.inflate(inflater, parent, false)
            )

            TYPE_WITH_TILES -> TilesViewHolder(
                ItemTilesContainerBinding.inflate(inflater, parent, false)
            )

            TYPE_PAYMENT -> PaymentViewHolder(
                ItemPaymentTileBinding.inflate(inflater, parent, false)
            )

            else -> BotViewHolder(
                ItemMessageBotBinding.inflate(inflater, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = messages[position]

        when (holder) {
            is UserViewHolder -> holder.bind(msg)
            is BotViewHolder -> holder.bind(msg)
            is TilesViewHolder -> holder.bind(msg.tileItems)
            is PaymentViewHolder -> holder.bind(msg)
        }
    }

    override fun getItemCount() = messages.size


    // ------------------- ViewHolders ----------------------

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
            val adapter = TilesAdapter { tile -> onTileClick(tile) }

            binding.tilesRecyclerView.apply {
                layoutManager = LinearLayoutManager(
                    binding.root.context,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
                this.adapter = adapter
            }

            adapter.updateTiles(tileItems)
        }
    }

    inner class PaymentViewHolder(private val binding: ItemPaymentTileBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: ChatMessage) {
            val details = message.bookingDetails

            binding.bookingDetails.text = """
                Movie: ${details["movie"]}
                Cinema: ${details["cinema"]}
                Time: ${details["time"]}
                Seats: ${details["seats"]}
                Price: ₹${message.price}
            """.trimIndent()

            binding.paymentButton.text = "💳 Make Payment ₹${message.price}"

            binding.paymentButton.setOnClickListener {
                onPaymentClicked(message.price!!, details)
            }
        }
    }
}
