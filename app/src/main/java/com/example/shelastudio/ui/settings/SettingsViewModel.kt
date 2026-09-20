package com.example.shelastudio.ui.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shelastudio.data.repository.PreferenceRepository
import com.example.shelastudio.di.RepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Manages application preference state for the Settings screens.
 *
 * Preferences are loaded and saved through PreferenceRepository,
 * which uses Firebase as the persistent data source.
 */

class SettingsViewModel(
    private val preferenceRepository: PreferenceRepository =
        RepositoryProvider.preferences
) : ViewModel() {

    companion object {
        private const val TAG = "SettingsViewModel"
    }

    private val _state =
        MutableStateFlow(SettingsState())

    val state: StateFlow<SettingsState> =
        _state.asStateFlow()


    init {
        loadPreferences()
    }


    fun loadPreferences() {

        viewModelScope.launch {

            Log.d(
                TAG,
                "Loading user preferences"
            )

            _state.value =
                _state.value.copy(
                    isLoading = true,
                    error = null
                )

            val result =
                preferenceRepository.getPreferences()


            result.onSuccess { preferences ->

                _state.value =
                    _state.value.copy(
                        preferences = preferences,
                        isLoading = false,
                        error = null
                    )

                Log.i(
                    TAG,
                    "Preferences loaded successfully"
                )
            }


            result.onFailure { exception ->

                _state.value =
                    _state.value.copy(
                        isLoading = false,
                        error = exception.message
                            ?: "Could not load preferences"
                    )

                Log.e(
                    TAG,
                    "Failed to load preferences",
                    exception
                )
            }
        }
    }


    fun updateLanguage(
        language: String
    ) {

        val updated =
            _state.value.preferences.copy(
                language = language
            )

        _state.value =
            _state.value.copy(
                preferences = updated,
                saveSuccessful = false
            )

        Log.d(
            TAG,
            "Language changed to $language"
        )
    }


    fun updateMeasurementUnit(
        unit: String
    ) {

        val updated =
            _state.value.preferences.copy(
                measurementUnit = unit
            )

        _state.value =
            _state.value.copy(
                preferences = updated,
                saveSuccessful = false
            )

        Log.d(
            TAG,
            "Measurement unit changed to $unit"
        )
    }


    fun updateBiometricEnabled(
        enabled: Boolean
    ) {

        val updated =
            _state.value.preferences.copy(
                biometricEnabled = enabled
            )

        _state.value =
            _state.value.copy(
                preferences = updated,
                saveSuccessful = false
            )

        Log.d(
            TAG,
            "Biometric preference changed: $enabled"
        )
    }


    fun updateNotificationsEnabled(
        enabled: Boolean
    ) {

        val updated =
            _state.value.preferences.copy(
                notificationsEnabled = enabled
            )

        _state.value =
            _state.value.copy(
                preferences = updated,
                saveSuccessful = false
            )

        Log.d(
            TAG,
            "Notification preference changed: $enabled"
        )
    }


    fun updateTheme(
        theme: String
    ) {

        val updated =
            _state.value.preferences.copy(
                theme = theme
            )

        _state.value =
            _state.value.copy(
                preferences = updated,
                saveSuccessful = false
            )

        Log.d(
            TAG,
            "Theme changed to $theme"
        )
    }


    fun savePreferences() {

        viewModelScope.launch {

            Log.d(
                TAG,
                "Saving user preferences"
            )

            _state.value =
                _state.value.copy(
                    isSaving = true,
                    saveSuccessful = false,
                    error = null
                )


            val result =
                preferenceRepository.updatePreferences(
                    _state.value.preferences
                )


            result.onSuccess {

                _state.value =
                    _state.value.copy(
                        isSaving = false,
                        saveSuccessful = true,
                        error = null
                    )

                Log.i(
                    TAG,
                    "Preferences saved successfully"
                )
            }


            result.onFailure { exception ->

                _state.value =
                    _state.value.copy(
                        isSaving = false,
                        saveSuccessful = false,
                        error = exception.message
                            ?: "Could not save preferences"
                    )

                Log.e(
                    TAG,
                    "Failed to save preferences",
                    exception
                )
            }
        }
    }


    fun clearSaveMessage() {

        _state.value =
            _state.value.copy(
                saveSuccessful = false
            )
    }
}