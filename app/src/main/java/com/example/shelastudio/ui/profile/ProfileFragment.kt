package com.example.shelastudio.ui.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.shelastudio.R
import com.example.shelastudio.databinding.FragmentProfileBinding

/**
 * Displays the user's profile and provides access
 * to account and application settings.
 */
class ProfileFragment : Fragment() {

    companion object {
        private const val TAG = "ProfileFragment"
    }

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        Log.d(TAG, "onCreateView")

        _binding =
            FragmentProfileBinding.inflate(
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

        setupClickListeners()
    }

    /**
     * Handles navigation from the Profile screen.
     */
    private fun setupClickListeners() {

        binding.btnSettings.setOnClickListener {

            Log.d(
                TAG,
                "Navigating from Profile to Settings"
            )

            findNavController().navigate(
                R.id.action_profileFragment_to_settingsFragment
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