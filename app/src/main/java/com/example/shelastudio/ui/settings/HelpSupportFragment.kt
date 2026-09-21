package com.example.shelastudio.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.shelastudio.databinding.FragmentHelpSupportBinding

/**
 * Displays frequently asked questions and allows the
 * user to contact support using an email application.
 */
class HelpSupportFragment : Fragment() {

    companion object {
        private const val TAG = "HelpSupportFragment"
    }

    private var _binding: FragmentHelpSupportBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding =
            FragmentHelpSupportBinding.inflate(
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

        binding.btnContactSupport
            .setOnClickListener {

                Log.d(
                    TAG,
                    "Contact support selected"
                )

                openEmailApplication()
            }
    }

    /**
     * Opens the user's preferred email application.
     */
    private fun openEmailApplication() {

        val intent =
            Intent(
                Intent.ACTION_SENDTO
            ).apply {

                data =
                    Uri.parse(
                        "mailto:"
                    )

                putExtra(
                    Intent.EXTRA_SUBJECT,
                    "Shela Studio Support"
                )
            }

        try {

            startActivity(intent)

            Log.i(
                TAG,
                "Email application opened"
            )

        } catch (exception: Exception) {

            Log.e(
                TAG,
                "Unable to open email application",
                exception
            )
        }
    }

    override fun onDestroyView() {

        super.onDestroyView()

        _binding = null
    }
}