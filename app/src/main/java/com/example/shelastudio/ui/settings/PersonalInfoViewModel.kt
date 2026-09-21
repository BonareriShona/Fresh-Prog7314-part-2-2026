package com.example.shelastudio.ui.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shelastudio.data.model.UserProfile
import com.example.shelastudio.data.repository.AuthRepository
import com.example.shelastudio.di.RepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Loads and updates the authenticated user's profile information.
 *
 * Profile data is stored in Firestore under users/{userId}.
 */
class PersonalInfoViewModel(
    private val authRepository: AuthRepository =
        RepositoryProvider.auth
) : ViewModel() {

    companion object {
        private const val TAG = "PersonalInfoViewModel"
    }

    private val _state =
        MutableStateFlow(PersonalInfoState())

    val state: StateFlow<PersonalInfoState> =
        _state.asStateFlow()

    init {
        loadProfile()
    }

    /**
     * Loads the profile belonging to the current authenticated user.
     */
    fun loadProfile() {

        viewModelScope.launch {

            Log.d(TAG, "Loading user profile")

            _state.value =
                _state.value.copy(
                    isLoading = true,
                    error = null
                )

            val result =
                authRepository.getUserProfile()

            result.onSuccess { profile ->

                _state.value =
                    _state.value.copy(
                        profile = profile,
                        isLoading = false,
                        error = null
                    )

                Log.i(TAG, "User profile loaded")
            }

            result.onFailure { exception ->

                _state.value =
                    _state.value.copy(
                        isLoading = false,
                        error = exception.message
                            ?: "Unable to load profile"
                    )

                Log.e(
                    TAG,
                    "Failed to load user profile",
                    exception
                )
            }
        }
    }

    /**
     * Validates and saves editable profile information.
     */
    fun saveProfile(
        fullName: String,
        mobileNumber: String,
        dateOfBirth: String
    ) {

        val validationError =
            validateInput(
                fullName,
                mobileNumber,
                dateOfBirth
            )

        if (validationError != null) {

            _state.value =
                _state.value.copy(
                    error = validationError,
                    saveSuccessful = false
                )

            Log.w(
                TAG,
                "Profile validation failed: $validationError"
            )

            return
        }

        val existingProfile =
            _state.value.profile
                ?: UserProfile(
                    userId =
                        authRepository
                            .currentUserId()
                            .orEmpty()
                )

        /*
         * copy() preserves fields such as the user's email,
         * profile photo, sign-in provider and creation date.
         */
        val updatedProfile =
            existingProfile.copy(
                fullName = fullName.trim(),
                mobileNumber = mobileNumber.trim(),
                dateOfBirth = dateOfBirth.trim()
            )

        viewModelScope.launch {

            Log.d(TAG, "Saving personal information")

            _state.value =
                _state.value.copy(
                    isSaving = true,
                    error = null,
                    saveSuccessful = false
                )

            val result =
                authRepository.updateUserProfile(
                    updatedProfile
                )

            result.onSuccess {

                _state.value =
                    _state.value.copy(
                        profile = updatedProfile,
                        isSaving = false,
                        saveSuccessful = true,
                        error = null
                    )

                Log.i(
                    TAG,
                    "Personal information updated successfully"
                )
            }

            result.onFailure { exception ->

                _state.value =
                    _state.value.copy(
                        isSaving = false,
                        saveSuccessful = false,
                        error = exception.message
                            ?: "Unable to update profile"
                    )

                Log.e(
                    TAG,
                    "Failed to update personal information",
                    exception
                )
            }
        }
    }

    /**
     * Checks user input before any Firebase update occurs.
     */
    private fun validateInput(
        fullName: String,
        mobileNumber: String,
        dateOfBirth: String
    ): String? {

        if (fullName.trim().length < 2) {
            return "Please enter your full name."
        }

        val phonePattern =
            Regex(
                "^[+]?[0-9 ]{7,15}$"
            )

        if (
            mobileNumber.isNotBlank() &&
            !phonePattern.matches(
                mobileNumber.trim()
            )
        ) {
            return "Please enter a valid mobile number."
        }

        if (dateOfBirth.isBlank()) {
            return "Please select your date of birth."
        }

        return null
    }
}