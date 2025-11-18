package com.example.chatbot


import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChatBotManager {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    private val moviesNowShowing = listOf(
        "Avengers ", "Deadpool", "Jawan", "War 2", "Thamma", "The Taj Story"
    )

    private var greeted = false

    suspend fun processUserMessage(userMessage: String): BotResponse {
        return try {
            val msg = userMessage.lowercase().trim()

            if (!greeted) {
                greeted = true
                return BotResponse(
                    message = "Hi! I'm your CineTicketBot assistant. How can I help you today?",
                    showQuickActions = true
                )
            }

            val movie = detectMovieName(msg)

            if (movie != null) {
                return BotResponse(
                    message = "Great choice! To check availability for \"$movie\", please allow location access.",
                    movieName = movie,
                    requiresLocation = true
                )

            }

            // ---- USER TALKS ABOUT MOVIES BUT NOT VALID ----
            if (isMovieRelatedMessage(msg)) {
                return BotResponse(
                    message = "Sorry, that movie is not currently available.\nHere are the movies now showing:",
                    showMovieTiles = true
                )
            }

            // ---- NORMAL AI CONVERSATION ----
            val prompt = """
                You are a friendly movie ticket chatbot.
                Give short helpful answers (1–2 lines max).
                
                If user asks about movies, tell them to choose from:
                ${moviesNowShowing.joinToString(", ")}

                User: $userMessage
                Bot:
            """.trimIndent()

            val response = withContext(Dispatchers.IO) {
                generativeModel.generateContent(prompt)
            }

            BotResponse(
                message = response.text ?: "How can I assist you?",
                showQuickActions = true
            )

        } catch (e: Exception) {
            BotResponse(
                message = "Sorry, I’m having trouble responding right now."
            )
        }
    }

    private fun detectMovieName(message: String): String? {
        return moviesNowShowing.firstOrNull { movie ->
            message.contains(movie.lowercase())
        }
    }

    private fun isMovieRelatedMessage(message: String): Boolean {
        val keywords = listOf("movie", "watch", "book", "ticket", "show", "cinema")
        return keywords.any { message.contains(it) }
    }

    fun getAvailableMovies(): List<TileItem> =
        moviesNowShowing.map {
            TileItem("movie_${it.hashCode()}", it, TileType.MOVIE)
        }

    fun getCinemasForCity(): List<TileItem> =
        listOf("PVR", "INOX", "Cinepolis", "CityMall").map {
            TileItem("cinema_${it.hashCode()}", it, TileType.CINEMA)
        }

    fun getShowTimes(): List<TileItem> =
        listOf("10:00 AM", "1:30 PM", "4:45 PM", "9:15 PM").map {
            TileItem("st_${it.hashCode()}", it, TileType.SHOWTIME)
        }

    fun getQuickActions(): List<TileItem> =
        listOf("🎬 Browse Movies", "🎫 My Tickets", "ℹ️ Help").map {
            TileItem("qa_${it.hashCode()}", it, TileType.QUICK_ACTION)
        }

    fun generateRandomPrice(): Int {
        return listOf(100,110,120,130,140,150,160,170,180,190,200).random()
    }

    fun validateSeatsAvailability(requestedSeats: Int, availableSeats: Int = 50): Boolean {
        return requestedSeats in 1..50 && requestedSeats <= availableSeats
    }
    data class BotResponse(
        val message: String,
        val movieName: String? = null,
        val requiresLocation: Boolean = false,
        val showMovieTiles: Boolean = false,
        val showCinemas: Boolean = false,
        val showShowtimes: Boolean = false,
        val showSeatSelection: Boolean = false,
        val showPayment: Boolean = false,
        val showQuickActions: Boolean = false,
        val price: Int? = null,
        val bookingDetails: Map<String, String> = emptyMap()
    )
}