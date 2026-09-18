package com.example.shelastudio.ui.outfit

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shelastudio.data.model.ClothingItem
import com.example.shelastudio.databinding.FragmentOutfitDetailBinding
import com.example.shelastudio.di.RepositoryProvider
import kotlinx.coroutines.launch

class OutfitDetailFragment : Fragment() {

    companion object {
        private const val TAG = "OutfitDetailFragment"
    }

    private var _binding: FragmentOutfitDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OutfitViewModel by viewModels()
    private lateinit var adapter: OutfitItemAdapter
    private var outfitId: String? = null
    private var outfitName: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOutfitDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        outfitId = arguments?.getString("outfitId")
        Log.d(TAG, "onViewCreated for outfitId=$outfitId")

        adapter = OutfitItemAdapter()
        binding.rvOutfitItems.layoutManager = LinearLayoutManager(requireContext())
        binding.rvOutfitItems.adapter = adapter

        binding.ivBack.setOnClickListener { findNavController().navigateUp() }
        binding.btnDeleteOutfit.setOnClickListener { confirmDelete() }

        outfitId?.let { loadOutfit(it) }
    }

    private fun loadOutfit(id: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = RepositoryProvider.outfits.getOutfit(id)
            result.onSuccess { outfit ->
                if (outfit == null) {
                    Toast.makeText(requireContext(), "Outfit not found", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                    return@onSuccess
                }

                outfitName = outfit.name
                binding.tvOutfitName.text = outfit.name
                binding.tvOutfitMeta.text = "${outfit.occasion} · ${outfit.season}"

                // Load wardrobe items for slot resolution
                val wardrobeResult = RepositoryProvider.wardrobe.getClothingItems()
                wardrobeResult.onSuccess { items ->
                    val lookup = items.associateBy { it.itemId }
                    val rows = mutableListOf<Pair<String, ClothingItem>>()
                    outfit.slots.forEach { (slot, itemId) ->
                        lookup[itemId]?.let { item -> rows.add(slot to item) }
                    }
                    adapter.setData(rows)
                }
            }.onFailure { e ->
                Log.e(TAG, "Load failed", e)
            }
        }
    }

    private fun confirmDelete() {
        val id = outfitId ?: return
        AlertDialog.Builder(requireContext())
            .setTitle("Delete outfit?")
            .setMessage("Delete \"$outfitName\"? This cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteOutfit(id)
                Toast.makeText(requireContext(), "Deleted", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}