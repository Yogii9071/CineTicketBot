package com.example.chatbot

data class ChatMessage(
    val sender: String,
    val message: String,
    val type: Int = TYPE_TEXT,
    val tileItems: List<TileItem> = emptyList(),
    val movieName: String? = null,
    val price: Int? = null,
    val bookingDetails: Map<String, String> = emptyMap()
) {
    companion object {
        const val TYPE_TEXT = 0
        const val TYPE_WITH_TILES = 1
        const val TYPE_SEAT_SELECTION = 2
        const val TYPE_PAYMENT = 3
    }
}

data class TileItem(
    val id: String,
    val title: String,
    val type: TileType,
    val isSelected: Boolean = false,
    val data: Map<String, Any> = emptyMap()
)

enum class TileType {
    MOVIE, CINEMA, SHOWTIME, QUICK_ACTION, PAYMENT, VIEW_TICKET
}