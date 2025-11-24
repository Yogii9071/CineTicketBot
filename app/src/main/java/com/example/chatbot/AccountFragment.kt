package com.example.chatbot

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

class AccountFragment : Fragment(R.layout.fragment_account) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val emailTextView = view.findViewById<TextView>(R.id.userEmail)
        val logoutButton = view.findViewById<MaterialButton>(R.id.logoutButton)

        val sharedPref = requireActivity().getSharedPreferences("USER_DATA", Context.MODE_PRIVATE)
        val email = sharedPref.getString("EMAIL", "No Email")


        emailTextView.text = email


        logoutButton.setOnClickListener {
            sharedPref.edit().clear().apply()

            FirebaseAuth.getInstance().signOut()
            val intent = Intent(requireContext(), CineTicketBotLogin::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}
