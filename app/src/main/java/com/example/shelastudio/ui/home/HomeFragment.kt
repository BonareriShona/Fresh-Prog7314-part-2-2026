package com.example.shelastudio.ui.home

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.shelastudio.R
import com.example.shelastudio.data.model.ClothingItem
import com.example.shelastudio.databinding.FragmentHomeBinding
import com.example.shelastudio.util.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    companion object {
        private const val TAG = "HomeFragment"
    }

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView")
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated")

        setupClickListeners()
        observeState()
    }

    private fun setupClickListeners() {
        // Tap the weather card → Weather Suggestions screen
        binding.cvWeather.setOnClickListener {
            Log.d(TAG, "Weather card tapped → navigating to Weather Suggestions")
            findNavController().navigate(R.id.action_homeFragment_to_weatherSuggestionsFragment)
        }

        // Quick actions
        binding.qaAddItem.setOnClickListener {
            Log.d(TAG, "Quick action: Add Item")
            findNavController().navigate(R.id.action_homeFragment_to_addItemFragment)
        }

        binding.qaCreateOutfit.setOnClickListener {
            Log.d(TAG, "Quick action: Create Outfit")
            findNavController().navigate(R.id.action_homeFragment_to_createOutfitFragment)
        }

        binding.qaLogOutfit.setOnClickListener {
            Log.d(TAG, "Quick action: Log Outfit (not implemented yet)")
            Toast.makeText(requireContext(), "Log Outfit coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.qaTripPlanner.setOnClickListener {
            Log.d(TAG, "Quick action: Trip Planner (not implemented yet)")
            Toast.makeText(requireContext(), "Trip Planner coming soon", Toast.LENGTH_SHORT).show()
        }

        // View the recommended outfit's details
        binding.btnViewOutfit.setOnClickListener {
            val outfitId = viewModel.state.value.recommendedOutfit?.outfitId
            if (outfitId != null) {
                Log.d(TAG, "View Outfit tapped: $outfitId")
                val args = Bundle().apply { putString("outfitId", outfitId) }
                findNavController().navigate(R.id.action_homeFragment_to_outfitDetailFragment, args)
            } else {
                Toast.makeText(requireContext(), "Create an outfit to see it here", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    renderWeather(state)
                    renderRecommended(state)
                    renderStats(state)
                }
            }
        }
    }

    private fun renderWeather(state: HomeState) {
        val w = state.weather ?: run {
            binding.tvWeatherTemp.text = "--°C"
            binding.tvWeatherDesc.text = "Weather unavailable"
            return
        }
        binding.tvWeatherLocation.text = "${w.locationName} · Today"
        binding.tvWeatherTemp.text = w.formattedTemp()
        binding.tvWeatherDesc.text = w.description
    }

    private fun renderRecommended(state: HomeState) {
        val outfit = state.recommendedOutfit
        val items = state.recommendedItems

        if (outfit == null || items.isEmpty()) {
            binding.tvRecommendedName.text = "No outfit yet"
            binding.tvRecommendedMeta.text = "Create your first outfit"
            binding.tvNoOutfit.visibility = View.VISIBLE
            binding.ivRecommendedTop.setImageBitmap(null)
            binding.ivRecommendedBottom.setImageBitmap(null)
            binding.ivRecommendedShoes.setImageBitmap(null)
            return
        }

        binding.tvNoOutfit.visibility = View.GONE
        binding.tvRecommendedName.text = outfit.name
        binding.tvRecommendedMeta.text = "${outfit.occasion} · ${items.size} items"

        loadImage(binding.ivRecommendedTop, items.getOrNull(0))
        loadImage(binding.ivRecommendedBottom, items.getOrNull(1))
        loadImage(binding.ivRecommendedShoes, items.getOrNull(2))
    }

    private fun loadImage(imageView: android.widget.ImageView, item: ClothingItem?) {
        if (item == null || item.imageBase64.isEmpty()) {
            imageView.setImageBitmap(null)
            return
        }
        viewLifecycleOwner.lifecycleScope.launch {
            val bitmap: Bitmap? = withContext(Dispatchers.IO) {
                ImageUtils.base64ToBitmap(item.imageBase64)
            }
            imageView.setImageBitmap(bitmap)
        }
    }

    private fun renderStats(state: HomeState) {
        binding.tvStatItems.text = state.itemCount.toString()
        binding.tvStatOutfits.text = state.outfitCount.toString()
        binding.tvStatTimesWorn.text = state.timesWorn.toString()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume — refreshing dashboard")
        viewModel.loadDashboard()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView")
        _binding = null
    }
}