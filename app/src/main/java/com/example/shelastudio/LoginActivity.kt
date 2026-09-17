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
        private const val MIN_PASSWORD_LENGTH = 6
    }

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d(TAG, "onCreate: Login screen displayed")

        setupClickListeners()
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }

    /**
     * Wires up all clickable UI elements on the Login screen.
     */
    private fun setupClickListeners() {

        // ---- Log In button ----
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text?.toString()?.trim().orEmpty()
            val password = binding.etPassword.text?.toString().orEmpty()
            Log.d(TAG, "Login clicked: email='$email', password length=${password.length}")

            if (validateInputs(email, password)) {
                Log.i(TAG, "Login validation passed - navigating to MainActivity")
                // TODO: Replace this with real SSO / API call later
                // For now, navigate directly to MainActivity to demonstrate the flow
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }

        // ---- Sign Up button ----
        binding.btnSignUp.setOnClickListener {
            Log.d(TAG, "Sign Up clicked - navigating to SignupActivity")
            startActivity(Intent(this, SignupActivity::class.java))
        }

        // ---- Forgot password ----
        binding.tvForgotPassword.setOnClickListener {
            Log.d(TAG, "Forgot password clicked")
            Toast.makeText(this, "Password reset coming soon", Toast.LENGTH_SHORT).show()
        }

        // ---- Biometric ----
        binding.llBiometric.setOnClickListener {
            Log.d(TAG, "Biometric clicked")
            Toast.makeText(this, "Biometric login coming soon", Toast.LENGTH_SHORT).show()
        }

        // ---- Google Sign-In ----
        binding.cvGoogleSignIn.setOnClickListener {
            Log.d(TAG, "Google Sign-In clicked")
            Toast.makeText(this, "Google Sign-In coming soon", Toast.LENGTH_SHORT).show()
        }

        // ---- "Don't have an account?" text ----
        binding.tvNoAccount.setOnClickListener {
            Log.d(TAG, "Don't have an account clicked - navigating to SignupActivity")
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    /**
     * Validates the login inputs.
     *
     * @param email The email or username entered by the user.
     * @param password The password entered by the user.
     * @return true if validation passes, false otherwise.
     */
    private fun validateInputs(email: String, password: String): Boolean {

        // Reset any previous errors
        binding.tilEmail.error = null
        binding.tilPassword.error = null

        // -- Email validation --
        if (email.isBlank()) {
            binding.tilEmail.error = "Please enter your email"
            Log.w(TAG, "Validation failed: empty email")
            return false
        }

        // If it looks like an email (contains @), validate format
        if (email.contains("@")) {
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.tilEmail.error = "Please enter a valid email address"
                Log.w(TAG, "Validation failed: invalid email format")
                return false
            }
        }

        // -- Password validation --
        if (password.isBlank()) {
            binding.tilPassword.error = "Please enter your password"
            Log.w(TAG, "Validation failed: empty password")
            return false
        }

        if (password.length < MIN_PASSWORD_LENGTH) {
            binding.tilPassword.error = "Password must be at least $MIN_PASSWORD_LENGTH characters"
            Log.w(TAG, "Validation failed: password too short (${password.length})")
            return false
        }

        return true
    }
}