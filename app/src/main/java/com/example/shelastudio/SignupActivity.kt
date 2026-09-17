package com.example.shelastudio

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.shelastudio.databinding.ActivitySignupBinding
import java.util.Calendar

class SignupActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "SignupActivity"
        private const val MIN_PASSWORD_LENGTH = 6
    }

    private lateinit var binding: ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d(TAG, "onCreate: Signup screen displayed")

        setupDatePicker()
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

    /**
     * Makes the DOB field open a DatePickerDialog when tapped.
     */
    private fun setupDatePicker() {
        binding.etDob.setOnClickListener {
            Log.d(TAG, "Date of birth field tapped")
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val formatted = String.format(
                        "%04d-%02d-%02d",
                        selectedYear,
                        selectedMonth + 1,
                        selectedDay
                    )
                    binding.etDob.setText(formatted)
                    Log.d(TAG, "DOB selected: $formatted")
                },
                year - 18, // Default to 18 years ago
                month,
                day
            )
            datePicker.datePicker.maxDate = System.currentTimeMillis()
            datePicker.show()
        }
    }

    private fun setupClickListeners() {

        // ---- Sign Up button ----
        binding.btnSignUp.setOnClickListener {
            val fullName = binding.etFullName.text?.toString()?.trim().orEmpty()
            val email = binding.etEmail.text?.toString()?.trim().orEmpty()
            val mobile = binding.etMobile.text?.toString()?.trim().orEmpty()
            val dob = binding.etDob.text?.toString()?.trim().orEmpty()
            val password = binding.etPassword.text?.toString().orEmpty()
            val confirmPassword = binding.etConfirmPassword.text?.toString().orEmpty()

            Log.d(TAG, "Sign Up clicked: name='$fullName', email='$email'")

            if (validateInputs(fullName, email, mobile, dob, password, confirmPassword)) {
                Log.i(TAG, "Signup validation passed")
                Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                // Navigate to MainActivity after signup
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }

        // ---- "Already have an account?" ----
        binding.tvAlreadyHaveAccount.setOnClickListener {
            Log.d(TAG, "Already have account clicked - returning to Login")
            finish() // Returns to LoginActivity
        }
    }

    /**
     * Validates all signup inputs.
     */
    private fun validateInputs(
        fullName: String,
        email: String,
        mobile: String,
        dob: String,
        password: String,
        confirmPassword: String
    ): Boolean {

        // Clear previous errors
        binding.tilFullName.error = null
        binding.tilEmail.error = null
        binding.tilMobile.error = null
        binding.tilDob.error = null
        binding.tilPassword.error = null
        binding.tilConfirmPassword.error = null

        // -- Full name --
        if (fullName.isBlank()) {
            binding.tilFullName.error = "Please enter your full name"
            Log.w(TAG, "Validation failed: empty full name")
            return false
        }

        // -- Email --
        if (email.isBlank()) {
            binding.tilEmail.error = "Please enter your email"
            Log.w(TAG, "Validation failed: empty email")
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Please enter a valid email address"
            Log.w(TAG, "Validation failed: invalid email format")
            return false
        }

        // -- Mobile --
        if (mobile.isBlank()) {
            binding.tilMobile.error = "Please enter your mobile number"
            Log.w(TAG, "Validation failed: empty mobile")
            return false
        }
        if (mobile.length < 10) {
            binding.tilMobile.error = "Please enter a valid mobile number"
            Log.w(TAG, "Validation failed: mobile too short")
            return false
        }

        // -- DOB --
        if (dob.isBlank()) {
            binding.tilDob.error = "Please select your date of birth"
            Log.w(TAG, "Validation failed: empty DOB")
            return false
        }

        // -- Password --
        if (password.isBlank()) {
            binding.tilPassword.error = "Please enter a password"
            Log.w(TAG, "Validation failed: empty password")
            return false
        }
        if (password.length < MIN_PASSWORD_LENGTH) {
            binding.tilPassword.error = "Password must be at least $MIN_PASSWORD_LENGTH characters"
            Log.w(TAG, "Validation failed: password too short")
            return false
        }

        // -- Confirm password --
        if (confirmPassword.isBlank()) {
            binding.tilConfirmPassword.error = "Please confirm your password"
            Log.w(TAG, "Validation failed: empty confirm password")
            return false
        }
        if (password != confirmPassword) {
            binding.tilConfirmPassword.error = "Passwords do not match"
            Log.w(TAG, "Validation failed: passwords do not match")
            return false
        }

        return true
    }
}