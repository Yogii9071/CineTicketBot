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
    private lateinit var chatBotManager: ChatBotManager
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

        chatBotManager = ChatBotManager()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        setupChatAdapter()
        setupRecyclerView()

        // Send message on button click
        binding.sendButton.setOnClickListener {
            val userMessage = binding.messageEditText.text.toString().trim()
            if (userMessage.isNotEmpty()) {
                processUserMessage(userMessage)
                binding.messageEditText.text.clear()
            }
        }

        // Initial greeting
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
                    TileType.QUICK_ACTION -> {
                        handleQuickAction(tile.title)
                    }
                    TileType.PAYMENT -> {
                        val price = tile.data["price"] as? Int ?: 0
                        val details = tile.data["details"] as? Map<String, String> ?: emptyMap()
                        showPaymentDialog(price, details)
                    }
                    TileType.VIEW_TICKET -> {
                        showTicketPage()
                    }
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
        addMessageToChat("Bot", "Hi! I'm your CineTicketBot assistant. How can I help you today?")
        chatAdapter.addMessageWithTiles("Quick actions:", chatBotManager.getQuickActions())
        chatAdapter.addMessageWithTiles("Movies now showing:", chatBotManager.getAvailableMovies())
    }

    private fun processUserMessage(userMessage: String) {
        // If we're waiting for seat input, process it as seats
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
                response.showMovieTiles -> {
                    chatAdapter.addMessageWithTiles("Available movies:", chatBotManager.getAvailableMovies())
                }
                response.requiresLocation -> {
                    currentMovie = response.movieName
                    requestLocationPermission()
                }
                response.showQuickActions -> {
                    chatAdapter.addMessageWithTiles("Quick actions:", chatBotManager.getQuickActions())
                }
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
            // Stay in waiting for seats mode
        }
    }

    private fun askForSeats() {
        waitingForSeats = true
        addMessageToChat("Bot", "How many seats do you want? (1-50)\n\nPlease type the number in the chat.")
    }

    private fun requestLocationPermission() {
        // Check if we have location permission
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request both permissions
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST
            )
        } else {
            // We already have permission
            getCurrentLocation()
        }
    }

    private fun getCurrentLocation() {
        // Check permission again before accessing location
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            addMessageToChat("Bot", "Location permission not granted. Please enable location to see cinemas.")
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                // Use Geocoder to get city name (simplified for now)
                val city = getCityFromLocation(location)
                showCinemaTiles(city)
            } else {
                // Location is null, use default city
                showCinemaTiles("Your City")
            }
        }.addOnFailureListener { exception ->
            // Handle location failure
            addMessageToChat("Bot", "Unable to get location. Using default city.")
            showCinemaTiles("Your City")
        }
    }

    private fun getCityFromLocation(location: Location): String {
        // For now, return a placeholder
        // In real implementation, use Geocoder to get actual city name
        return "Your City"

        /* Real implementation would be:
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            return addresses?.firstOrNull()?.locality ?: "Your City"
        } catch (e: Exception) {
            return "Your City"
        }
        */
    }

    private fun showCinemaTiles(city: String) {
        addMessageToChat("Bot", "Here are the cinemas in $city showing $currentMovie:")
        chatAdapter.addMessageWithTiles("Select cinema:", chatBotManager.getCinemasForCity())
    }

    private fun showShowtimeTiles() {
        chatAdapter.addMessageWithTiles("Choose showtime:", chatBotManager.getShowTimes())
    }

    private fun processSeatSelection(seats: Int) {
        val isAvailable = chatBotManager.validateSeatsAvailability(seats)

        if (isAvailable) {
            val price = chatBotManager.generateRandomPrice()
            val bookingDetails = mapOf(
                "movie" to (currentMovie ?: ""),
                "cinema" to (currentCinema ?: ""),
                "time" to (currentShowtime ?: ""),
                "seats" to seats.toString()
            )
            chatAdapter.addPaymentMessage(price, bookingDetails)
        } else {
            addMessageToChat("Bot", "Sorry, Housefull. Only limited seats available.")
            chatAdapter.addMessageWithTiles("Quick actions:", chatBotManager.getQuickActions())
        }
    }

    private fun handleQuickAction(action: String) {
        when {
            action.contains("Movie", ignoreCase = true) -> {
                addMessageToChat("You", "Show movies")
                chatAdapter.addMessageWithTiles("Available movies:", chatBotManager.getAvailableMovies())
            }
            action.contains("Ticket", ignoreCase = true) -> {
                addMessageToChat("You", "Show my tickets")
                // Navigate to tickets fragment
            }
            action.contains("Location", ignoreCase = true) -> {
                addMessageToChat("You", "Change location")
                requestLocationPermission()
            }
            else -> {
                addMessageToChat("You", action)
            }
        }
    }

    private fun showPaymentDialog(price: Int, details: Map<String, String>) {
        // Simulate payment success for now
        simulatePaymentSuccess()
    }

    private fun simulatePaymentSuccess() {
        addMessageToChat("Bot", "Payment successful! ✅")
        chatAdapter.addSuccessMessage()
    }

    private fun showTicketPage() {
        // Navigate to TicketPageActivity
        addMessageToChat("Bot", "Opening your ticket...")
        // val intent = Intent(requireContext(), TicketPageActivity::class.java)
        // startActivity(intent)
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
                // Check if any location permission was granted
                val locationPermissionGranted = grantResults.isNotEmpty() &&
                        (grantResults[0] == PackageManager.PERMISSION_GRANTED ||
                                (grantResults.size > 1 && grantResults[1] == PackageManager.PERMISSION_GRANTED))

                if (locationPermissionGranted) {
                    getCurrentLocation()
                } else {
                    addMessageToChat("Bot", "Location permission denied. Please enable location to see cinemas.")
                    // Show cinemas with default city anyway
                    showCinemaTiles("Your City")
                }
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST = 1001
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}