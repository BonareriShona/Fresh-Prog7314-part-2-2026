package com.example.shelastudio.ui.settings

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.shelastudio.databinding.FragmentAboutBinding

/**
 * Displays application information and the currently
 * installed version of Shela Studio.
 */
class AboutFragment : Fragment() {

    companion object {
        private const val TAG = "AboutFragment"
    }

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding =
            FragmentAboutBinding.inflate(
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

        displayAppVersion()
    }

    /**
     * Reads the installed application version from
     * the Android package information.
     */
    private fun displayAppVersion() {

        try {

            val packageInfo =
                requireContext()
                    .packageManager
                    .getPackageInfo(
                        requireContext().packageName,
                        0
                    )

            val versionName =
                packageInfo.versionName
                    ?: "Unknown"

            binding.txtAppVersion.text =
                "Version $versionName"

            Log.d(
                TAG,
                "Application version displayed: $versionName"
            )

        } catch (exception: Exception) {

            binding.txtAppVersion.text =
                "Version unavailable"

            Log.e(
                TAG,
                "Unable to read application version",
                exception
            )
        }
    }

    override fun onDestroyView() {

        super.onDestroyView()

        _binding = null
    }
}