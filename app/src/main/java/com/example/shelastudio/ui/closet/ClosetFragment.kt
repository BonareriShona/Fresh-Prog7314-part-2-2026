package com.example.shelastudio.ui.closet

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
import com.example.shelastudio.databinding.FragmentClosetBinding
import kotlinx.coroutines.launch

class ClosetFragment : Fragment() {

    companion object {
        private const val TAG = "ClosetFragment"
    }

    private var _binding: FragmentClosetBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WardrobeViewModel by viewModels()
    private lateinit var adapter: ClothingAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView")
        _binding = FragmentClosetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated")
        setupRecyclerView()
        setupChips()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = ClothingAdapter { item ->
            Log.d(TAG, "Navigating to detail for ${item.itemId}")
            findNavController().navigate(
                R.id.action_closetFragment_to_itemDetailFragment,
                bundleOf("itemId" to item.itemId)
            )
        }
        binding.rvWardrobe.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.rvWardrobe.adapter = adapter
    }

    private fun setupChips() {
        val chipMap = mapOf(
            binding.chipAll to "All",
            binding.chipTops to "Tops",
            binding.chipBottoms to "Bottoms",
            binding.chipDresses to "Dresses",
            binding.chipShoes to "Shoes",
            binding.chipBags to "Bags",
            binding.chipAccessories to "Accessories"
        )
        chipMap.forEach { (chip, category) ->
            chip.setOnClickListener {
                Log.d(TAG, "Chip selected: $category")
                viewModel.setCategory(category)
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnAddItem.setOnClickListener {
            Log.d(TAG, "Add Item clicked")
            findNavController().navigate(R.id.action_closetFragment_to_addItemFragment)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is WardrobeUiState.Loading -> {
                            binding.pbLoading.visibility = View.VISIBLE
                            binding.rvWardrobe.visibility = View.GONE
                            binding.llEmpty.visibility = View.GONE
                        }
                        is WardrobeUiState.Success -> {
                            binding.pbLoading.visibility = View.GONE
                            if (state.items.isEmpty()) {
                                binding.rvWardrobe.visibility = View.GONE
                                binding.llEmpty.visibility = View.VISIBLE
                            } else {
                                binding.rvWardrobe.visibility = View.VISIBLE
                                binding.llEmpty.visibility = View.GONE
                                adapter.submitList(state.items)
                            }
                        }
                        is WardrobeUiState.Error -> {
                            binding.pbLoading.visibility = View.GONE
                            binding.rvWardrobe.visibility = View.GONE
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume - reloading wardrobe")
        viewModel.loadWardrobe()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView")
        _binding = null
    }
}