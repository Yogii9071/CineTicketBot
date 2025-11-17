package com.example.chatbot

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatbot.databinding.ItemTicketBinding

class TicketsAdapter(private val tickets: List<Ticket>) :
    RecyclerView.Adapter<TicketsAdapter.TicketViewHolder>() {

    inner class TicketViewHolder(private val binding: ItemTicketBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(ticket: Ticket) {
            binding.movieText.text = ticket.movie
            binding.cinemaText.text = ticket.cinema
            binding.timeText.text = ticket.time
            binding.seatText.text = "Seats: ${ticket.seats}"
            binding.priceText.text = "₹${ticket.price}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketViewHolder {
        val binding = ItemTicketBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TicketViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TicketViewHolder, position: Int) {
        holder.bind(tickets[position])
    }

    override fun getItemCount() = tickets.size
}
