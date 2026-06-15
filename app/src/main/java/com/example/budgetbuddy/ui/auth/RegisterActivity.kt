package com.example.budgetbuddy.ui.auth

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.budgetbuddy.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup click listener for the registration button
        binding.btnRegister.setOnClickListener {
            val user = binding.editUsername.text.toString().trim()
            val pass = binding.editPassword.text.toString().trim()
            val confirm = binding.editConfirmPassword.text.toString().trim()

            // Basic validation to ensure all fields are filled
            if (user.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Password confirmation check
            if (pass != confirm) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Hand off registration to the ViewModel
            viewModel.register(user, pass) { success ->
                if (success) {
                    Toast.makeText(this, "Registration successful! Please login.", Toast.LENGTH_SHORT).show()
                    finish() // Close the activity and return to the login screen
                } else {
                    Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Link back to login screen if the user already has an account
        binding.textLoginLink.setOnClickListener {
            finish()
        }
    }
}