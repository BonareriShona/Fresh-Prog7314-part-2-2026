package com.example.shelastudio.ui.settings

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle

import com.example.shelastudio.databinding.FragmentPersonalInfoBinding

import kotlinx.coroutines.launch

import java.util.Calendar
import java.util.Locale

/**
 * Allows the authenticated user to view and update
 * their personal profile information.
 */
class PersonalInfoFragment : Fragment() {

    companion object {
        private const val TAG =
            "PersonalInfoFragment"
    }

    private var _binding:
            FragmentPersonalInfoBinding? = null

    private val binding
        get() = _binding!!

    private val viewModel:
            PersonalInfoViewModel by viewModels()

    private var profileDisplayed =
        false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        Log.d(
            TAG,
            "Creating Personal Information screen"
        )

        _binding =
            FragmentPersonalInfoBinding.inflate(
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

        setupListeners()

        observeState()
    }

    /**
     * Handles user interactions on the Personal Information screen.
     */
    private fun setupListeners() {

        binding.etDateOfBirth
            .setOnClickListener {

                showDatePicker()
            }

        binding.btnSavePersonalInfo
            .setOnClickListener {

                Log.d(
                    TAG,
                    "Save personal information selected"
                )

                viewModel.saveProfile(
                    fullName =
                        binding.etFullName
                            .text
                            .toString(),

                    mobileNumber =
                        binding.etMobile
                            .text
                            .toString(),

                    dateOfBirth =
                        binding.etDateOfBirth
                            .text
                            .toString()
                )
            }
    }

    /**
     * Displays a date picker instead of requiring
     * the user to manually type a date.
     */
    private fun showDatePicker() {

        val calendar =
            Calendar.getInstance()

        val dialog =
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->

                    val selectedDate =
                        String.format(
                            Locale.getDefault(),
                            "%04d-%02d-%02d",
                            year,
                            month + 1,
                            day
                        )

                    binding.etDateOfBirth
                        .setText(
                            selectedDate
                        )
                },
                calendar.get(
                    Calendar.YEAR
                ),
                calendar.get(
                    Calendar.MONTH
                ),
                calendar.get(
                    Calendar.DAY_OF_MONTH
                )
            )

        // Prevent future dates from being selected.
        dialog.datePicker.maxDate =
            System.currentTimeMillis()

        dialog.show()
    }

    /**
     * Observes Firebase profile loading and saving state.
     */
    private fun observeState() {

        viewLifecycleOwner
            .lifecycleScope
            .launch {

                viewLifecycleOwner
                    .repeatOnLifecycle(
                        Lifecycle.State.STARTED
                    ) {

                        viewModel.state
                            .collect { state ->

                                binding.progressPersonalInfo.visibility =
                                    if (
                                        state.isLoading ||
                                        state.isSaving
                                    ) {
                                        View.VISIBLE
                                    } else {
                                        View.GONE
                                    }

                                binding.btnSavePersonalInfo.isEnabled =
                                    !state.isSaving

                                /*
                                 * Only populate the form once.
                                 * This avoids overwriting text while
                                 * the user is editing the fields.
                                 */
                                if (
                                    !profileDisplayed &&
                                    !state.isLoading &&
                                    state.profile != null
                                ) {

                                    displayProfile(
                                        state.profile
                                    )

                                    profileDisplayed =
                                        true
                                }

                                when {

                                    state.saveSuccessful -> {

                                        binding
                                            .txtPersonalInfoStatus
                                            .visibility =
                                            View.VISIBLE

                                        binding
                                            .txtPersonalInfoStatus
                                            .text =
                                            "Personal information saved successfully."
                                    }

                                    state.error != null -> {

                                        binding
                                            .txtPersonalInfoStatus
                                            .visibility =
                                            View.VISIBLE

                                        binding
                                            .txtPersonalInfoStatus
                                            .text =
                                            state.error
                                    }

                                    else -> {

                                        binding
                                            .txtPersonalInfoStatus
                                            .visibility =
                                            View.GONE
                                    }
                                }
                            }
                    }
            }
    }

    /**
     * Places the user's current profile information into the form.
     */
    private fun displayProfile(
        profile:
        com.example.shelastudio.data.model.UserProfile
    ) {

        binding.etFullName.setText(
            profile.fullName
        )

        binding.etEmail.setText(
            profile.email
        )

        binding.etMobile.setText(
            profile.mobileNumber
        )

        binding.etDateOfBirth.setText(
            profile.dateOfBirth
        )

        Log.d(
            TAG,
            "Profile displayed"
        )
    }

    override fun onDestroyView() {

        Log.d(
            TAG,
            "Destroying Personal Information view"
        )

        super.onDestroyView()

        _binding = null
    }
}