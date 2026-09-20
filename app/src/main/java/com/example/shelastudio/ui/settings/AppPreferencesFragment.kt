package com.example.shelastudio.ui.settings

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle

import com.example.shelastudio.databinding.FragmentAppPreferencesBinding

import kotlinx.coroutines.launch


/**
 * Allows the user to view and update application preferences.
 *
 * Preferences are stored through PreferenceRepository and persisted
 * in Firebase for the currently authenticated user.
 */
class AppPreferencesFragment : Fragment() {

    companion object {
        private const val TAG = "AppPreferencesFragment"
    }


    private var _binding:
            FragmentAppPreferencesBinding? = null

    private val binding
        get() = _binding!!


    private val viewModel:
            SettingsViewModel by viewModels()


    private var isLoadingPreferences = true


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        Log.d(
            TAG,
            "Creating App Preferences screen"
        )

        _binding =
            FragmentAppPreferencesBinding.inflate(
                inflater,
                container,
                false
            )

        return binding.root
    }


    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {

        super.onViewCreated(
            view,
            savedInstanceState
        )

        setupSpinners()

        setupListeners()

        observeState()
    }


    /**
     * Configures the available values for each preference.
     */
    private fun setupSpinners() {

        val languages =
            listOf(
                "English",
                "isiZulu",
                "Afrikaans"
            )

        val measurementUnits =
            listOf(
                "cm",
                "inches"
            )

        val themes =
            listOf(
                "light",
                "dark"
            )


        binding.spinnerLanguage.adapter =
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                languages
            )


        binding.spinnerMeasurement.adapter =
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                measurementUnits
            )


        binding.spinnerTheme.adapter =
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                themes
            )
    }


    /**
     * Handles preference changes made by the user.
     */
    private fun setupListeners() {

        binding.spinnerLanguage.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {

                    if (!isLoadingPreferences) {

                        val language =
                            parent
                                ?.getItemAtPosition(position)
                                .toString()

                        viewModel.updateLanguage(
                            language
                        )
                    }
                }


                override fun onNothingSelected(
                    parent: AdapterView<*>?
                ) {
                    // No action required.
                }
            }


        binding.spinnerMeasurement.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {

                    if (!isLoadingPreferences) {

                        val unit =
                            parent
                                ?.getItemAtPosition(position)
                                .toString()

                        viewModel.updateMeasurementUnit(
                            unit
                        )
                    }
                }


                override fun onNothingSelected(
                    parent: AdapterView<*>?
                ) {
                    // No action required.
                }
            }


        binding.spinnerTheme.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {

                    if (!isLoadingPreferences) {

                        val theme =
                            parent
                                ?.getItemAtPosition(position)
                                .toString()

                        viewModel.updateTheme(
                            theme
                        )
                    }
                }


                override fun onNothingSelected(
                    parent: AdapterView<*>?
                ) {
                    // No action required.
                }
            }


        binding.switchNotifications
            .setOnCheckedChangeListener { _, checked ->

                if (!isLoadingPreferences) {

                    viewModel
                        .updateNotificationsEnabled(
                            checked
                        )
                }
            }


        binding.switchBiometric
            .setOnCheckedChangeListener { _, checked ->

                if (!isLoadingPreferences) {

                    viewModel
                        .updateBiometricEnabled(
                            checked
                        )
                }
            }


        binding.btnSavePreferences
            .setOnClickListener {

                Log.d(
                    TAG,
                    "Save Preferences selected"
                )

                viewModel.savePreferences()
            }
    }


    /**
     * Observes preference state from the ViewModel
     * while this Fragment is visible.
     */
    private fun observeState() {

        viewLifecycleOwner.lifecycleScope.launch {

            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {

                viewModel.state.collect { state ->

                    binding.progressPreferences.visibility =
                        if (
                            state.isLoading ||
                            state.isSaving
                        ) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }


                    binding.btnSavePreferences.isEnabled =
                        !state.isSaving


                    if (!state.isLoading) {

                        displayPreferences(
                            state
                        )

                        isLoadingPreferences =
                            false
                    }


                    when {

                        state.saveSuccessful -> {

                            binding.txtPreferenceStatus.visibility =
                                View.VISIBLE

                            binding.txtPreferenceStatus.text =
                                "Preferences saved successfully."
                        }


                        state.error != null -> {

                            binding.txtPreferenceStatus.visibility =
                                View.VISIBLE

                            binding.txtPreferenceStatus.text =
                                state.error
                        }


                        else -> {

                            binding.txtPreferenceStatus.visibility =
                                View.GONE
                        }
                    }
                }
            }
        }
    }


    /**
     * Displays preferences previously loaded from Firebase.
     */
    private fun displayPreferences(
        state: SettingsState
    ) {

        val preferences =
            state.preferences


        setSpinnerValue(
            binding.spinnerLanguage,
            preferences.language
        )


        setSpinnerValue(
            binding.spinnerMeasurement,
            preferences.measurementUnit
        )


        setSpinnerValue(
            binding.spinnerTheme,
            preferences.theme
        )


        binding.switchNotifications.isChecked =
            preferences.notificationsEnabled


        binding.switchBiometric.isChecked =
            preferences.biometricEnabled
    }


    /**
     * Selects the spinner item matching the value
     * retrieved from saved preferences.
     */
    private fun setSpinnerValue(
        spinner: android.widget.Spinner,
        value: String
    ) {

        val adapter =
            spinner.adapter

        for (
        position in 0 until adapter.count
        ) {

            if (
                adapter
                    .getItem(position)
                    .toString()
                    .equals(
                        value,
                        ignoreCase = true
                    )
            ) {

                spinner.setSelection(
                    position
                )

                break
            }
        }
    }


    override fun onDestroyView() {

        Log.d(
            TAG,
            "Destroying App Preferences view"
        )

        super.onDestroyView()

        _binding = null
    }
}