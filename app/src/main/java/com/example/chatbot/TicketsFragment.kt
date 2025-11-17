package com.example.chatbot

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatbot.databinding.FragmentTicketsBinding
import org.json.JSONObject

class TicketsFragment : Fragment(R.layout.fragment_tickets) {

    private var binding: FragmentTicketsBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentTicketsBinding.bind(view)

        loadTickets()
    }

    private fun loadTickets() {
        val shared = requireContext().getSharedPreferences("tickets_db", 0)
        val json = shared.getString("last_ticket", null)

        if (json == null) {
            showEmptyState()
            return
        }

        val obj = JSONObject(json)
        val ticket = Ticket(
            movie = obj.getString("movie"),
            cinema = obj.getString("cinema"),
            time = obj.getString("time"),
            seats = obj.getString("seats"),
            price = obj.getInt("price")
        )

        showTicket(listOf(ticket))
    }

    private fun showEmptyState() {
        binding?.apply {
            emptyState.visibility = View.VISIBLE
            ticketsRecyclerView.visibility = View.GONE
        }
    }

    private fun showTicket(tickets: List<Ticket>) {
        binding?.apply {
            emptyState.visibility = View.GONE
            ticketsRecyclerView.visibility = View.VISIBLE

            ticketsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            ticketsRecyclerView.adapter = TicketsAdapter(tickets)
        }
    }
}
