package com.example.chatbot

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.chatbot.databinding.ActivityCineTicketBotSignupBinding
import com.google.firebase.auth.FirebaseAuth

class CineTicketBotSignup : AppCompatActivity() {

    private lateinit var binding: ActivityCineTicketBotSignupBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Correct binding class
        binding = ActivityCineTicketBotSignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Signup Button Action
        binding.signupButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()
            val confirmPassword = binding.confirmEditText.text.toString().trim()

            when {
                email.isEmpty() -> {
                    showToast("Enter email")
                }
                password.isEmpty() -> {
                    showToast("Enter password")
                }
                confirmPassword.isEmpty() -> {
                    showToast("Confirm your password")
                }
                password != confirmPassword -> {
                    showToast("Passwords do not match")
                }
                else -> {
                    createAccount(email, password)
                }
            }
        }

        // Login redirect
        binding.loginText.setOnClickListener {
            startActivity(Intent(this, CineTicketBotLogin::class.java))
            finish()
        }
    }

    // Create Firebase Account
    private fun createAccount(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showToast("Account Created Successfully!")

                    val intent = Intent(this, CineTicketBotLogin::class.java)
                    startActivity(intent)
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                } else {
                    showToast(task.exception?.message ?: "Registration failed")
                }
            }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
