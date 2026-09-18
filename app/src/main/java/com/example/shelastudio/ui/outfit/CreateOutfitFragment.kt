package com.example.shelastudio.ui.outfit

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shelastudio.data.model.ClothingItem
import com.example.shelastudio.databinding.FragmentCreateOutfitBinding
import com.example.shelastudio.util.ImageUtils
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreateOutfitFragment : Fragment() {

    companion object {
        private const val TAG = "CreateOutfitFragment"
    }

    private var _binding: FragmentCreateOutfitBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OutfitViewModel by viewModels()
    private lateinit var slotAdapter: SlotItemAdapter

    /** Current slot the user is picking items for. */
    private var currentSlot: String = "Top"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateOutfitBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated")

        viewModel.clearBuilder()
        viewModel.loadWardrobeForBuilder()

        setupTabs()
        setupOccasionDropdown()
        setupSlotAdapter()
        setupClickListeners()
        observeBuilder()
    }

    private fun setupTabs() {
        OutfitViewModel.SLOTS.forEach { slot ->
            binding.tabSlots.addTab(binding.tabSlots.newTab().setText(slot))
        }
        binding.tabSlots.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                currentSlot = tab.text.toString()
                Log.d(TAG, "Slot tab selected: $currentSlot")
                refreshSlotAdapter()
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun setupOccasionDropdown() {
        val occasions = listOf("Casual", "Work", "Evening", "Sport", "Formal", "Weekend")
        binding.actOccasion.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, occasions)
        )
    }

    private fun setupSlotAdapter() {
        slotAdapter = SlotItemAdapter { item ->
            if (item != null) {
                viewModel.setSlot(currentSlot, item)
                Log.d(TAG, "Assigned ${item.name} to $currentSlot")
                Toast.makeText(requireContext(), "${item.name} → $currentSlot", Toast.LENGTH_SHORT).show()
            }
        }
        binding.rvItemsForSlot.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvItemsForSlot.adapter = slotAdapter
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener { findNavController().navigateUp() }
        binding.btnSaveOutfit.setOnClickListener { saveOutfit() }
    }

    private fun observeBuilder() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.builderState.collect { state ->
                    refreshSlotAdapter()
                    refreshPreview(state.selectedSlots)
                }
            }
        }
    }

    private fun refreshSlotAdapter() {
        val state = viewModel.builderState.value
        val filtered = filterForSlot(state.wardrobe, currentSlot)
        Log.d(TAG, "Slot '$currentSlot' has ${filtered.size} matching items")
        slotAdapter.submitList(filtered)
    }

    /**
     * Filters wardrobe items by the slot the user is currently picking for.
     */
    private fun filterForSlot(items: List<ClothingItem>, slot: String): List<ClothingItem> {
        return when (slot) {
            "Top" -> items.filter { it.category == "Tops" }
            "Bottom" -> items.filter { it.category == "Bottoms" }
            "Outerwear" -> items.filter { it.category == "Outerwear" }
            "Shoes" -> items.filter { it.category == "Shoes" }
            "Accessory" -> items.filter { it.category == "Bags" || it.category == "Accessories" }
            else -> items
        }
    }

    private fun refreshPreview(slots: Map<String, ClothingItem>) {
        loadPreview(binding.ivSlotTop, slots["Top"])
        loadPreview(binding.ivSlotBottom, slots["Bottom"])
        loadPreview(binding.ivSlotShoes, slots["Shoes"])
    }

    private fun loadPreview(imageView: android.widget.ImageView, item: ClothingItem?) {
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

    private fun saveOutfit() {
        val name = binding.etOutfitName.text?.toString()?.trim().orEmpty()
        val occasion = binding.actOccasion.text?.toString()?.trim().orEmpty().ifBlank { "Casual" }

        binding.tilOutfitName.error = null

        if (name.isBlank()) {
            binding.tilOutfitName.error = "Please enter a name for this outfit"
            return
        }

        if (viewModel.builderState.value.selectedSlots.isEmpty()) {
            Toast.makeText(requireContext(), "Add at least one item", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.saveOutfit(name, occasion, "All-Season") { success ->
            if (success) {
                Toast.makeText(requireContext(), "Outfit saved!", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            } else {
                Toast.makeText(requireContext(), "Save failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}