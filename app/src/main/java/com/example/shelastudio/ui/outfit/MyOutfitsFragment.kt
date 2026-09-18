package com.example.shelastudio.ui.outfit

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.shelastudio.R
import com.example.shelastudio.data.model.ClothingItem
import com.example.shelastudio.databinding.FragmentMyOutfitsBinding
import com.example.shelastudio.di.RepositoryProvider
import kotlinx.coroutines.launch

class MyOutfitsFragment : Fragment() {

    companion object {
        private const val TAG = "MyOutfitsFragment"
    }

    private var _binding: FragmentMyOutfitsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OutfitViewModel by viewModels()

    /** Cache of wardrobe items so the adapter can resolve slot IDs to images. */
    private var wardrobeLookup: Map<String, ClothingItem> = emptyMap()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyOutfitsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated")

        binding.ivBack.setOnClickListener { findNavController().navigateUp() }
        binding.btnCreateOutfit.setOnClickListener {
            findNavController().navigate(R.id.action_myOutfitsFragment_to_createOutfitFragment)
        }

        // Load wardrobe first, then outfits
        loadWardrobeThenOutfits()
        observeViewModel()
    }

    private fun loadWardrobeThenOutfits() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Pre-load wardrobe for image resolution
            val result = RepositoryProvider.wardrobe.getClothingItems()
            result.onSuccess { items ->
                wardrobeLookup = items.associateBy { it.itemId }
                Log.i(TAG, "Wardrobe cache ready: ${wardrobeLookup.size} items")
            }
            viewModel.loadOutfits()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is OutfitUiState.Loading -> {
                            binding.pbLoading.visibility = View.VISIBLE
                            binding.rvOutfits.visibility = View.GONE
                            binding.llEmpty.visibility = View.GONE
                        }
                        is OutfitUiState.Success -> {
                            binding.pbLoading.visibility = View.GONE
                            if (state.outfits.isEmpty()) {
                                binding.rvOutfits.visibility = View.GONE
                                binding.llEmpty.visibility = View.VISIBLE
                            } else {
                                binding.rvOutfits.visibility = View.VISIBLE
                                binding.llEmpty.visibility = View.GONE
                                setupAdapter(state.outfits)
                            }
                        }
                        is OutfitUiState.Error -> {
                            binding.pbLoading.visibility = View.GONE
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    private fun setupAdapter(outfits: List<com.example.shelastudio.data.model.Outfit>) {
        val adapter = OutfitAdapter(wardrobeLookup) { outfit ->
            Log.d(TAG, "Navigating to detail: ${outfit.outfitId}")
            findNavController().navigate(
                R.id.action_myOutfitsFragment_to_outfitDetailFragment,
                bundleOf("outfitId" to outfit.outfitId)
            )
        }
        binding.rvOutfits.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvOutfits.adapter = adapter
        adapter.submitList(outfits)
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadOutfits()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}