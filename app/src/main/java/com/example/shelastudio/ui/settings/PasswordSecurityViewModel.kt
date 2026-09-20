package com.example.shelastudio.ui.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shelastudio.data.repository.AuthRepository
import com.example.shelastudio.di.RepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Handles password and account-security operations.
 *
 * Password-reset emails are only available for accounts
 * created using email and password authentication.
 */
class PasswordSecurityViewModel(
    private val authRepository: AuthRepository =
        RepositoryProvider.auth
) : ViewModel() {

    companion object {
        private const val TAG = "PasswordSecurityVM"
    }

    private val _state =
        MutableStateFlow(
            PasswordSecurityState()
        )

    val state: StateFlow<PasswordSecurityState> =
        _state.asStateFlow()

    init {
        loadAccountInformation()
    }

    /**
     * Loads the current user's email and sign-in provider.
     */
    private fun loadAccountInformation() {

        viewModelScope.launch {

            Log.d(
                TAG,
                "Loading security information"
            )

            _state.value =
                _state.value.copy(
                    isLoading = true,
                    error = null
                )

            val result =
                authRepository.getUserProfile()

            result.onSuccess { profile ->

                if (profile == null) {

                    _state.value =
                        _state.value.copy(
                            isLoading = false,
                            error = "Account information could not be found."
                        )

                    Log.w(
                        TAG,
                        "No user profile found"
                    )

                    return@onSuccess
                }

                _state.value =
                    _state.value.copy(
                        email = profile.email,
                        signInProvider = profile.signInProvider,
                        isLoading = false,
                        error = null
                    )

                Log.i(
                    TAG,
                    "Security information loaded"
                )
            }

            result.onFailure { exception ->

                _state.value =
                    _state.value.copy(
                        isLoading = false,
                        error =
                            exception.message
                                ?: "Unable to load security information."
                    )

                Log.e(
                    TAG,
                    "Failed to load security information",
                    exception
                )
            }
        }
    }

    /**
     * Sends a password-reset email for email/password accounts.
     */
    fun sendPasswordReset() {

        val currentState =
            _state.value

        if (
            currentState.signInProvider
                .equals(
                    "google",
                    ignoreCase = true
                )
        ) {

            _state.value =
                currentState.copy(
                    error =
                        "This account uses Google Sign-In. Manage your password through Google.",
                    resetSuccessful = false
                )

            Log.w(
                TAG,
                "Password reset blocked for Google account"
            )

            return
        }

        if (currentState.email.isBlank()) {

            _state.value =
                currentState.copy(
                    error = "No email address is available."
                )

            return
        }

        viewModelScope.launch {

            Log.d(
                TAG,
                "Sending password reset email"
            )

            _state.value =
                _state.value.copy(
                    isSendingReset = true,
                    resetSuccessful = false,
                    error = null
                )

            val result =
                authRepository.sendPasswordReset(
                    currentState.email
                )

            result.onSuccess {

                _state.value =
                    _state.value.copy(
                        isSendingReset = false,
                        resetSuccessful = true,
                        error = null
                    )

                Log.i(
                    TAG,
                    "Password reset email sent successfully"
                )
            }

            result.onFailure { exception ->

                _state.value =
                    _state.value.copy(
                        isSendingReset = false,
                        resetSuccessful = false,
                        error =
                            exception.message
                                ?: "Unable to send password reset email."
                    )

                Log.e(
                    TAG,
                    "Password reset failed",
                    exception
                )
            }
        }
    }
}