package com.example.shelastudio.ui.closet

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
import com.example.shelastudio.databinding.FragmentItemDetailBinding
import com.example.shelastudio.di.RepositoryProvider
import com.example.shelastudio.util.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ItemDetailFragment : Fragment() {

    companion object {
        private const val TAG = "ItemDetailFragment"
    }

    private var _binding: FragmentItemDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WardrobeViewModel by viewModels()
    private var itemId: String? = null
    private var itemName: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentItemDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        itemId = arguments?.getString("itemId")
        Log.d(TAG, "onViewCreated for itemId=$itemId")

        binding.ivBack.setOnClickListener { findNavController().navigateUp() }
        binding.btnDelete.setOnClickListener { confirmDelete() }

        itemId?.let { loadItem(it) }
    }

    private fun loadItem(id: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = RepositoryProvider.wardrobe.getClothingItem(id)
            result.onSuccess { item ->
                if (item == null) {
                    Toast.makeText(requireContext(), "Item not found", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                    return@onSuccess
                }
                itemName = item.name
                binding.tvName.text = item.name
                binding.tvCategory.text = item.category
                binding.tvDetails.text = buildString {
                    if (item.brand.isNotBlank()) append("${item.brand} · ")
                    if (item.purchasePrice > 0) append("R${item.purchasePrice} · ")
                    append(item.season)
                }

                if (item.imageBase64.isNotEmpty()) {
                    val bitmap = withContext(Dispatchers.IO) {
                        ImageUtils.base64ToBitmap(item.imageBase64)
                    }
                    binding.ivImage.setImageBitmap(bitmap)
                }
            }.onFailure { e ->
                Log.e(TAG, "Load failed", e)
                Toast.makeText(requireContext(), "Could not load item", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun confirmDelete() {
        val id = itemId ?: return
        AlertDialog.Builder(requireContext())
            .setTitle("Delete item?")
            .setMessage("Delete \"$itemName\"? This cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteItem(id)
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