package com.example.chatbot

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatbot.databinding.FragmentChatBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private lateinit var chatAdapter: ChatAdapter

    // ✅ ViewModel for persistent state
    private val viewModel: ChatBotViewModel by viewModels()
    private var greetingShown = false

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Current booking state
    private var currentMovie: String? = null
    private var currentCinema: String? = null
    private var currentShowtime: String? = null
    private var currentSeats: Int = 0
    private var waitingForSeats: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ❌ REMOVE THIS (you had this incorrectly)
        // chatBotManager = ChatBotManager()

        // ✅ Use persistent ChatBotManager from ViewModel
        val chatBotManager = viewModel.botManager

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        setupChatAdapter()
        setupRecyclerView()

        binding.sendButton.setOnClickListener {
            val userMessage = binding.messageEditText.text.toString().trim()
            if (userMessage.isNotEmpty()) {
                processUserMessage(userMessage)
                binding.messageEditText.text.clear()
            }
        }

        // Initial greeting (only shown once per app session)
        showInitialGreeting()
    }

    private fun setupChatAdapter() {
        chatAdapter = ChatAdapter(
            onTileClick = { tile ->
                when (tile.type) {
                    TileType.MOVIE -> {
                        currentMovie = tile.title
                        addMessageToChat("You", "I want to watch ${tile.title}")
                        requestLocationPermission()
                    }
                    TileType.CINEMA -> {
                        currentCinema = tile.title
                        addMessageToChat("You", tile.title)
                        showShowtimeTiles()
                    }
                    TileType.SHOWTIME -> {
                        currentShowtime = tile.title
                        addMessageToChat("You", tile.title)
                        askForSeats()
                    }
                    TileType.QUICK_ACTION -> handleQuickAction(tile.title)
                    TileType.PAYMENT -> {
                        val price = tile.data["price"] as? Int ?: 0
                        val details = tile.data["details"] as? Map<String, String> ?: emptyMap()
                        showPaymentDialog(price, details)
                    }
                    TileType.VIEW_TICKET -> showTicketPage()
                }
            },
            onPaymentClicked = { price, details ->
                showPaymentDialog(price, details)
            }
        )
    }

    private fun setupRecyclerView() {
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.chatRecyclerView.adapter = chatAdapter
    }

    private fun showInitialGreeting() {

        // 🔥 Prevent duplicate greeting + duplicate tiles
        if (greetingShown) return
        greetingShown = true

        val chatBotManager = viewModel.botManager

        addMessageToChat("Bot", "Hi! I'm your CineTicketBot assistant. How can I help you today?")
        chatAdapter.addMessageWithTiles("Quick actions:", chatBotManager.getQuickActions())
    }


    private fun processUserMessage(userMessage: String) {
        val chatBotManager = viewModel.botManager

        if (waitingForSeats) {
            processSeatInput(userMessage)
            return
        }

        addMessageToChat("You", userMessage)

        lifecycleScope.launch {
            val response = withContext(Dispatchers.IO) {
                chatBotManager.processUserMessage(userMessage)
            }

            addMessageToChat("Bot", response.message)

            when {
                response.showMovieTiles ->
                    chatAdapter.addMessageWithTiles("Available movies:", chatBotManager.getAvailableMovies())

                response.requiresLocation -> {
                    currentMovie = response.movieName
                    requestLocationPermission()
                }

                response.showQuickActions ->
                    chatAdapter.addMessageWithTiles("Quick actions:", chatBotManager.getQuickActions())
            }
        }
    }

    private fun processSeatInput(userMessage: String) {
        val seats = userMessage.toIntOrNull()

        if (seats != null && seats in 1..50) {
            currentSeats = seats
            waitingForSeats = false
            addMessageToChat("You", "$seats seats")
            processSeatSelection(seats)
        } else {
            addMessageToChat("You", userMessage)
            addMessageToChat("Bot", "Please enter a valid number between 1 and 50.")
        }
    }

    private fun askForSeats() {
        waitingForSeats = true
        addMessageToChat("Bot", "How many seats do you want? (1–50)")
    }

    private fun requestLocationPermission() {
        if (
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST
            )
        } else {
            getCurrentLocation()
        }
    }

    private fun getCurrentLocation() {
        if (
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            addMessageToChat("Bot", "Location permission not granted.")
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            val city = if (location != null) getCityFromLocation(location) else "Your City"
            showCinemaTiles(city)
        }.addOnFailureListener {
            addMessageToChat("Bot", "Unable to get location.")
            showCinemaTiles("Your City")
        }
    }

    private fun getCityFromLocation(location: Location): String = "Your City"

    private fun showCinemaTiles(city: String) {
        val chatBotManager = viewModel.botManager
        addMessageToChat("Bot", "Here are the cinemas in $city showing $currentMovie:")
        chatAdapter.addMessageWithTiles("Select cinema:", chatBotManager.getCinemasForCity())
    }

    private fun showShowtimeTiles() {
        val chatBotManager = viewModel.botManager
        chatAdapter.addMessageWithTiles("Choose showtime:", chatBotManager.getShowTimes())
    }

    private fun processSeatSelection(seats: Int) {
        val chatBotManager = viewModel.botManager

        if (chatBotManager.validateSeatsAvailability(seats)) {
            val price = chatBotManager.generateRandomPrice()
            val details = mapOf(
                "movie" to (currentMovie ?: ""),
                "cinema" to (currentCinema ?: ""),
                "time" to (currentShowtime ?: ""),
                "seats" to seats.toString()
            )
            chatAdapter.addPaymentMessage(price, details)
        } else {
            addMessageToChat("Bot", "Sorry, Housefull.")
            chatAdapter.addMessageWithTiles("Quick actions:", chatBotManager.getQuickActions())
        }
    }

    private fun handleQuickAction(action: String) {
        val chatBotManager = viewModel.botManager

        when {
            action.contains("Movie", ignoreCase = true) -> {
                addMessageToChat("You", "Show movies")
                chatAdapter.addMessageWithTiles("Available movies:", chatBotManager.getAvailableMovies())
            }

            action.contains("Ticket", ignoreCase = true) -> {
                addMessageToChat("You", "Show my tickets")
            }

            action.contains("Location", ignoreCase = true) -> {
                addMessageToChat("You", "Change location")
                requestLocationPermission()
            }

            else -> addMessageToChat("You", action)
        }
    }

    private fun showPaymentDialog(price: Int, details: Map<String, String>) {
        simulatePaymentSuccess()
    }

    private fun simulatePaymentSuccess() {
        addMessageToChat("Bot", "Payment successful! ✅")
        chatAdapter.addSuccessMessage()
    }

    private fun showTicketPage() {
        addMessageToChat("Bot", "Opening your ticket…")
    }

    private fun addMessageToChat(sender: String, message: String) {
        chatAdapter.addMessage(ChatMessage(sender, message))
        binding.chatRecyclerView.scrollToPosition(chatAdapter.itemCount - 1)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST -> {
                val granted = grantResults.isNotEmpty() &&
                        (grantResults[0] == PackageManager.PERMISSION_GRANTED ||
                                (grantResults.size > 1 && grantResults[1] == PackageManager.PERMISSION_GRANTED))

                if (granted) getCurrentLocation()
                else {
                    addMessageToChat("Bot", "Location denied.")
                    showCinemaTiles("Your City")
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST = 1001
    }
}
