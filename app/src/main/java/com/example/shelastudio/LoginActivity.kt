package com.example.shelastudio

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.shelastudio.databinding.ActivityLoginBinding
import com.example.shelastudio.di.RepositoryProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "LoginActivity"
        private const val MIN_PASSWORD_LENGTH = 6
    }

    private lateinit var binding: ActivityLoginBinding
    private lateinit var googleSignInClient: GoogleSignInClient

    private val authRepository = RepositoryProvider.auth

    /**
     * Receives the result of the Google account picker. The picker returns a
     * Google ID token, which is then exchanged for a Firebase session.
     */
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d(TAG, "Google sign-in activity returned resultCode=${result.resultCode}")
        try {
            val account = GoogleSignIn
                .getSignedInAccountFromIntent(result.data)
                .getResult(ApiException::class.java)

            val idToken = account?.idToken
            if (idToken == null) {
                Log.e(TAG, "Google account returned without an ID token")
                showError("Google sign-in failed. Please try again.")
                setLoading(false)
                return@registerForActivityResult
            }

            Log.i(TAG, "Google account selected: ${account.email}")
            authenticateWithGoogle(idToken)

        } catch (e: ApiException) {
            // statusCode 12501 means the user simply cancelled the picker.
            Log.w(TAG, "Google sign-in cancelled or failed. code=${e.statusCode}", e)
            if (e.statusCode != 12501) {
                showError("Google sign-in failed (code ${e.statusCode})")
            }
            setLoading(false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d(TAG, "onCreate: Login screen displayed")

        configureGoogleSignIn()
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
     * Builds the Google Sign-In client. requestIdToken() is essential —
     * without it Google returns an account but no token, and Firebase
     * has nothing to verify.
     */
    private fun configureGoogleSignIn() {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, options)
        Log.d(TAG, "Google Sign-In client configured")
    }

    private fun setupClickListeners() {

        // ---- Log In button (email + password) ----
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text?.toString()?.trim().orEmpty()
            val password = binding.etPassword.text?.toString().orEmpty()
            Log.d(TAG, "Login clicked: email='$email'")

            if (validateInputs(email, password)) {
                signInWithEmail(email, password)
            }
        }

        // ---- Sign Up button ----
        binding.btnSignUp.setOnClickListener {
            Log.d(TAG, "Sign Up clicked - navigating to SignupActivity")
            startActivity(Intent(this, SignupActivity::class.java))
        }

        // ---- Forgot password ----
        binding.tvForgotPassword.setOnClickListener {
            val email = binding.etEmail.text?.toString()?.trim().orEmpty()
            Log.d(TAG, "Forgot password clicked for '$email'")

            if (email.isBlank()) {
                binding.tilEmail.error = "Enter your email first"
                return@setOnClickListener
            }

            lifecycleScope.launch {
                authRepository.sendPasswordReset(email)
                    .onSuccess {
                        Toast.makeText(
                            this@LoginActivity,
                            "Password reset link sent to $email",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    .onFailure { showError(it.message ?: "Could not send reset email") }
            }
        }

        // ---- Biometric (implemented in the final POE) ----
        binding.llBiometric.setOnClickListener {
            Log.d(TAG, "Biometric clicked")
            Toast.makeText(this, "Biometric login coming soon", Toast.LENGTH_SHORT).show()
        }

        // ---- Google Sign-In ----
        binding.cvGoogleSignIn.setOnClickListener {
            Log.i(TAG, "Google Sign-In clicked - launching account picker")
            setLoading(true)
            googleSignInLauncher.launch(googleSignInClient.signInIntent)
        }

        // ---- "Don't have an account?" ----
        binding.tvNoAccount.setOnClickListener {
            Log.d(TAG, "No account clicked - navigating to SignupActivity")
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun signInWithEmail(email: String, password: String) {
        setLoading(true)
        lifecycleScope.launch {
            authRepository.signInWithEmail(email, password)
                .onSuccess { uid ->
                    Log.i(TAG, "Email sign-in succeeded. UID=$uid")
                    navigateToMain()
                }
                .onFailure { e ->
                    Log.w(TAG, "Email sign-in rejected: ${e.message}")
                    setLoading(false)
                    showError(friendlyAuthError(e))
                }
        }
    }

    private fun authenticateWithGoogle(idToken: String) {
        lifecycleScope.launch {
            authRepository.signInWithGoogle(idToken)
                .onSuccess { uid ->
                    Log.i(TAG, "Firebase session established via Google. UID=$uid")
                    navigateToMain()
                }
                .onFailure { e ->
                    Log.e(TAG, "Firebase rejected the Google credential", e)
                    setLoading(false)
                    showError("Could not complete Google sign-in")
                }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun setLoading(loading: Boolean) {
        binding.btnLogin.isEnabled = !loading
        binding.cvGoogleSignIn.isEnabled = !loading
        binding.btnLogin.text = if (loading) "Signing in…" else getString(R.string.log_in)
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    /** Turns Firebase exception text into something a user can act on. */
    private fun friendlyAuthError(e: Throwable): String {
        val raw = e.message.orEmpty()
        return when {
            raw.contains("no user record", true) -> "No account found with that email"
            raw.contains("password is invalid", true) -> "Incorrect password"
            raw.contains("badly formatted", true) -> "That email address isn't valid"
            raw.contains("network", true) -> "No internet connection"
            else -> "Sign-in failed. Please check your details."
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        binding.tilEmail.error = null
        binding.tilPassword.error = null

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

        if (password.isBlank()) {
            binding.tilPassword.error = "Please enter your password"
            Log.w(TAG, "Validation failed: empty password")
            return false
        }

        if (password.length < MIN_PASSWORD_LENGTH) {
            binding.tilPassword.error =
                "Password must be at least $MIN_PASSWORD_LENGTH characters"
            Log.w(TAG, "Validation failed: password too short")
            return false
        }

        return true
    }
}