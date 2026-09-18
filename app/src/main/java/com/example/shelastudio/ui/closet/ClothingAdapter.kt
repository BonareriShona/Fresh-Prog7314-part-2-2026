package com.example.shelastudio.ui.closet

import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.shelastudio.data.model.ClothingItem
import com.example.shelastudio.databinding.ItemClothingCardBinding
import com.example.shelastudio.util.ImageUtils

/**
 * Adapter for the wardrobe grid.
 * Uses ListAdapter + DiffUtil for efficient updates.
 */
class ClothingAdapter(
    private val onItemClick: (ClothingItem) -> Unit
) : ListAdapter<ClothingItem, ClothingAdapter.ItemViewHolder>(DIFF) {

    companion object {
        private const val TAG = "ClothingAdapter"

        private val DIFF = object : DiffUtil.ItemCallback<ClothingItem>() {
            override fun areItemsTheSame(oldItem: ClothingItem, newItem: ClothingItem) =
                oldItem.itemId == newItem.itemId

            override fun areContentsTheSame(oldItem: ClothingItem, newItem: ClothingItem) =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemClothingCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ItemViewHolder(
        private val binding: ItemClothingCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ClothingItem) {
            binding.tvItemName.text = item.name
            binding.tvItemCategory.text = buildString {
                append(item.category)
                if (item.timesWorn > 0) {
                    append(" · Worn ${item.timesWorn}×")
                }
            }

            // Decode Base64 image on a background thread to avoid UI jank
            binding.ivItemImage.setImageBitmap(null)
            binding.ivItemImage.setBackgroundColor(0xFFFADCE0.toInt()) // soft pink while loading

            if (item.imageBase64.isNotEmpty()) {
                // Simple background decode using the itemView's tag to avoid recycling issues
                val tag = item.itemId
                binding.ivItemImage.tag = tag

                android.os.AsyncTask.execute {
                    val bitmap: Bitmap? = ImageUtils.base64ToBitmap(item.imageBase64)
                    binding.ivItemImage.post {
                        if (binding.ivItemImage.tag == tag && bitmap != null) {
                            binding.ivItemImage.setImageBitmap(bitmap)
                            binding.ivItemImage.setBackgroundColor(0x00000000)
                        }
                    }
                }
            }

            binding.root.setOnClickListener {
                Log.d(TAG, "Item clicked: ${item.name}")
                onItemClick(item)
            }
        }
    }
}