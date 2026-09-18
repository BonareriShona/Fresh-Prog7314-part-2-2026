package com.example.shelastudio.ui.outfit

import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.shelastudio.data.model.ClothingItem
import com.example.shelastudio.data.model.Outfit
import com.example.shelastudio.databinding.ItemOutfitCardBinding
import com.example.shelastudio.util.ImageUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Adapter for the outfit grid.
 *
 * Receives a map of itemId -> ClothingItem so it can resolve
 * each outfit's slots to actual images without extra network calls.
 */
class OutfitAdapter(
    private val itemLookup: Map<String, ClothingItem>,
    private val onItemClick: (Outfit) -> Unit
) : ListAdapter<Outfit, OutfitAdapter.OutfitViewHolder>(DIFF) {

    companion object {
        private const val TAG = "OutfitAdapter"

        private val DIFF = object : DiffUtil.ItemCallback<Outfit>() {
            override fun areItemsTheSame(oldItem: Outfit, newItem: Outfit) =
                oldItem.outfitId == newItem.outfitId

            override fun areContentsTheSame(oldItem: Outfit, newItem: Outfit) =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OutfitViewHolder {
        val binding = ItemOutfitCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return OutfitViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OutfitViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class OutfitViewHolder(
        private val binding: ItemOutfitCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(outfit: Outfit) {
            binding.tvOutfitName.text = outfit.name
            binding.tvOutfitMeta.text = "${outfit.occasion} · ${outfit.itemIds.size} items"

            // Reset images
            binding.ivTop.setImageBitmap(null)
            binding.ivBottom.setImageBitmap(null)
            binding.ivShoes.setImageBitmap(null)

            // Resolve slot images
            val topItem = outfit.slots["Top"]?.let { itemLookup[it] }
            val bottomItem = outfit.slots["Bottom"]?.let { itemLookup[it] }
            val shoesItem = outfit.slots["Shoes"]?.let { itemLookup[it] }

            // If no slots (older outfits), fall back to first 3 items
            val fallbackItems = if (topItem == null && bottomItem == null && shoesItem == null) {
                outfit.itemIds.take(3).mapNotNull { itemLookup[it] }
            } else emptyList()

            loadInto(binding.ivTop, topItem ?: fallbackItems.getOrNull(0))
            loadInto(binding.ivBottom, bottomItem ?: fallbackItems.getOrNull(1))
            loadInto(binding.ivShoes, shoesItem ?: fallbackItems.getOrNull(2))

            val hasAny = topItem != null || bottomItem != null || shoesItem != null ||
                    fallbackItems.isNotEmpty()
            binding.tvNoImage.visibility = if (hasAny) View.GONE else View.VISIBLE
            binding.ivTop.visibility = if (topItem != null || fallbackItems.getOrNull(0) != null) View.VISIBLE else View.GONE
            binding.ivBottom.visibility = if (bottomItem != null || fallbackItems.getOrNull(1) != null) View.VISIBLE else View.GONE
            binding.ivShoes.visibility = if (shoesItem != null || fallbackItems.getOrNull(2) != null) View.VISIBLE else View.GONE

            binding.root.setOnClickListener {
                Log.d(TAG, "Outfit clicked: ${outfit.name}")
                onItemClick(outfit)
            }
        }

        private fun loadInto(imageView: android.widget.ImageView, item: ClothingItem?) {
            if (item == null || item.imageBase64.isEmpty()) return
            val tag = item.itemId
            imageView.tag = tag

            CoroutineScope(Dispatchers.Main).launch {
                val bitmap: Bitmap? = withContext(Dispatchers.IO) {
                    ImageUtils.base64ToBitmap(item.imageBase64)
                }
                if (imageView.tag == tag && bitmap != null) {
                    imageView.setImageBitmap(bitmap)
                }
            }
        }
    }
}