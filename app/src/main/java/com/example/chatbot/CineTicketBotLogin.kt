package com.example.chatbot
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.chatbot.databinding.ActivityCineTicketBotLoginBinding
import com.google.firebase.auth.FirebaseAuth

class CineTicketBotLogin : AppCompatActivity() {

    private lateinit var binding: ActivityCineTicketBotLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCineTicketBotLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // -----------------------------------------
        // ✅ AUTO LOGIN (no need to login everytime)
        // -----------------------------------------
        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }
        // -----------------------------------------

        setupClickListeners()
    }

    private fun setupClickListeners() {

        // LOGIN
        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        //Email saving
                        val sharedPref = getSharedPreferences("USER_DATA", MODE_PRIVATE)
                        sharedPref.edit()
                            .putString("EMAIL", email)
                            .apply()

                        Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                    }
                }
        }

        // NAVIGATE TO SIGNUP
        binding.signUpText.setOnClickListener {
            startActivity(Intent(this, CineTicketBotSignup::class.java))
        }

        // FORGOT PASSWORD
        binding.forgotPasswordText.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            if (email.isEmpty()) {
                Toast.makeText(this, "Enter email to reset password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    Toast.makeText(this, "Password reset link sent!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                }
        }
    }
}
