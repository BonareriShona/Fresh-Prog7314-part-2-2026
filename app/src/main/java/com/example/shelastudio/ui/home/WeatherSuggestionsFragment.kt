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
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.shelastudio.data.model.ClothingItem
import com.example.shelastudio.databinding.FragmentWeatherSuggestionsBinding
import com.example.shelastudio.databinding.ItemWeatherSuggestionBinding
import com.example.shelastudio.di.RepositoryProvider
import com.example.shelastudio.util.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WeatherSuggestionsFragment : Fragment() {

    companion object {
        private const val TAG = "WeatherSuggestions"

        /** DiffUtil at outer class level so the inner adapter can reference it. */
        private val SUGGESTION_DIFF = object : DiffUtil.ItemCallback<UISuggestion>() {
            override fun areItemsTheSame(a: UISuggestion, b: UISuggestion) =
                a.name == b.name && a.itemIds == b.itemIds

            override fun areContentsTheSame(a: UISuggestion, b: UISuggestion) =
                a == b
        }
    }

    private var _binding: FragmentWeatherSuggestionsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WeatherViewModel by viewModels()
    private lateinit var adapter: SuggestionAdapter

    private var wardrobeLookup: Map<String, ClothingItem> = emptyMap()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView")
        _binding = FragmentWeatherSuggestionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated")

        binding.ivBack.setOnClickListener { findNavController().navigateUp() }

        adapter = SuggestionAdapter { s ->
            Log.d(TAG, "Suggestion tapped: ${s.name}")
            Toast.makeText(requireContext(), "Suggested: ${s.name}", Toast.LENGTH_SHORT).show()
        }
        binding.rvSuggestions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSuggestions.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            RepositoryProvider.wardrobe.getClothingItems().onSuccess {
                wardrobeLookup = it.associateBy { item -> item.itemId }
                Log.d(TAG, "Wardrobe cache ready: ${wardrobeLookup.size}")
                adapter.updateLookup(wardrobeLookup)
            }
        }

        observeState()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        is WeatherUiState.Loading -> {
                            binding.pbLoading.visibility = View.VISIBLE
                            binding.rvSuggestions.visibility = View.GONE
                            binding.llEmpty.visibility = View.GONE
                        }
                        is WeatherUiState.Success -> {
                            binding.pbLoading.visibility = View.GONE
                            binding.tvBannerLocation.text = state.weather.locationName
                            binding.tvBannerTemp.text = state.weather.formattedTemp()
                            binding.tvBannerDesc.text = state.weather.description

                            if (state.suggestions.isEmpty()) {
                                binding.rvSuggestions.visibility = View.GONE
                                binding.llEmpty.visibility = View.VISIBLE
                            } else {
                                binding.rvSuggestions.visibility = View.VISIBLE
                                binding.llEmpty.visibility = View.GONE
                                adapter.submitList(state.suggestions)
                            }
                        }
                        is WeatherUiState.Error -> {
                            binding.pbLoading.visibility = View.GONE
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView")
        _binding = null
    }

    // ============================================================
    //  ADAPTER
    // ============================================================

    inner class SuggestionAdapter(
        private val onView: (UISuggestion) -> Unit
    ) : ListAdapter<UISuggestion, SuggestionAdapter.VH>(SUGGESTION_DIFF) {

        private var lookup: Map<String, ClothingItem> = emptyMap()

        fun updateLookup(newLookup: Map<String, ClothingItem>) {
            lookup = newLookup
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val b = ItemWeatherSuggestionBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return VH(b)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.bind(getItem(position))
        }

        inner class VH(private val b: ItemWeatherSuggestionBinding) :
            RecyclerView.ViewHolder(b.root) {

            fun bind(s: UISuggestion) {
                b.tvName.text = s.name
                b.tvOccasion.text = s.occasion
                b.tvReason.text = s.reason

                val first = s.itemIds.firstOrNull()?.let { lookup[it] }
                b.ivImage.setImageBitmap(null)
                b.ivImage.setBackgroundColor(0xFFFADCE0.toInt())

                if (first != null && first.imageBase64.isNotEmpty()) {
                    val tag = first.itemId
                    b.ivImage.tag = tag
                    lifecycleScope.launch {
                        val bmp: Bitmap? = withContext(Dispatchers.IO) {
                            ImageUtils.base64ToBitmap(first.imageBase64)
                        }
                        if (b.ivImage.tag == tag && bmp != null) {
                            b.ivImage.setImageBitmap(bmp)
                            b.ivImage.setBackgroundColor(0x00000000)
                        }
                    }
                }

                b.btnViewOutfit.setOnClickListener { onView(s) }
            }
        }
    }
}