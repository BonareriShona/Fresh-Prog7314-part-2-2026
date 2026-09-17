package com.example.shelastudio

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.shelastudio.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "LoginActivity"
    }

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d(TAG, "onCreate: Login screen")

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Login button
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text?.toString()?.trim().orEmpty()
            val password = binding.etPassword.text?.toString().orEmpty()
            Log.d(TAG, "Login clicked: email='$email'")

            if (validateInputs(email, password)) {
                Log.i(TAG, "Login validation passed - navigating to MainActivity")
                // TODO: Replace with real SSO / API call later
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        // Sign Up button
        binding.btnSignUp.setOnClickListener {
            Log.d(TAG, "Sign Up clicked - navigating to SignupActivity")
            startActivity(Intent(this, SignupActivity::class.java))
        }

        // Forgot password
        binding.tvForgotPassword.setOnClickListener {
            Log.d(TAG, "Forgot password clicked")
            Toast.makeText(this, "Password reset coming soon", Toast.LENGTH_SHORT).show()
        }

        // Biometric
        binding.llBiometric.setOnClickListener {
            Log.d(TAG, "Biometric clicked")
            Toast.makeText(this, "Biometric login coming soon", Toast.LENGTH_SHORT).show()
        }

        // Google sign-in
        binding.cvGoogleSignIn.setOnClickListener {
            Log.d(TAG, "Google Sign-In clicked")
            Toast.makeText(this, "Google Sign-In coming soon", Toast.LENGTH_SHORT).show()
        }

        // No account text
        binding.tvNoAccount.setOnClickListener {
            Log.d(TAG, "Don't have an account clicked")
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        if (email.isBlank()) {
            binding.tilEmail.error = "Please enter your email"
            Log.w(TAG, "Validation failed: empty email")
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() && !email.contains("@")) {
            // Allow usernames too - only validate if it looks like an email
            // For prototype, we accept both username and email
        }
        if (password.isBlank()) {
            binding.tilPassword.error = "Please enter your password"
            Log.w(TAG, "Validation failed: empty password")
            return false
        }
        if (password.length < 6) {
            binding.tilPassword.error = "Password must be at least 6 characters"
            Log.w(TAG