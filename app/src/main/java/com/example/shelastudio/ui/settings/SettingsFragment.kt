package com.example.shelastudio.ui.settings

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.shelastudio.R
import com.example.shelastudio.databinding.FragmentSettingsBinding

import kotlinx.coroutines.launch


class SettingsFragment : Fragment() {

    companion object {
        private const val TAG =
            "SettingsFragment"
    }


    private var _binding:
            FragmentSettingsBinding? = null

    private val binding
        get() = _binding!!


    private val viewModel:
            SettingsViewModel
            by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        Log.d(
            TAG,
            "onCreateView"
        )


        _binding =
            FragmentSettingsBinding.inflate(
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


        Log.d(
            TAG,
            "Settings screen opened"
        )


        observeState()

        setupClickListeners()
    }


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

                                Log.d(
                                    TAG,
                                    "Current language: ${state.language}"
                                )
                            }
                    }
            }
    }


    private fun setupClickListeners() {

        binding.btnPersonalInfo
            .setOnClickListener {

                Log.d(
                    TAG,
                    "Personal Information selected"
                )

                /*
                Navigation will be added next.
                */
            }


        binding.btnPassword
            .setOnClickListener {

                Log.d(
                    TAG,
                    "Password & Security selected"
                )
            }


        binding.btnStylePreferences
            .setOnClickListener {

                Log.d(
                    TAG,
                    "Style Preferences selected"
                )
            }


        binding.btnAppPreferences
            .setOnClickListener {

                Log.d(
                    TAG,
                    "Navigating to App Preferences"
                )

                findNavController().navigate(
                    R.id.action_settingsFragment_to_appPreferencesFragment
                )
            }


        binding.btnHelpSupport
            .setOnClickListener {

                Log.d(
                    TAG,
                    "Help & Support selected"
                )
            }


        binding.btnAbout
            .setOnClickListener {

                Log.d(
                    TAG,
                    "About selected"
                )
            }


        binding.btnLogout
            .setOnClickListener {

                Log.d(
                    TAG,
                    "Logout selected"
                )
            }
    }


    override fun onDestroyView() {

        Log.d(
            TAG,
            "onDestroyView"
        )

        super.onDestroyView()

        _binding = null
    }
}