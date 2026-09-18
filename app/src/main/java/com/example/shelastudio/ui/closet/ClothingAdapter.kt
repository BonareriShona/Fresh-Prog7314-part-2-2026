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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

            // Reset image state
            binding.ivItemImage.setImageBitmap(null)
            binding.ivItemImage.setBackgroundColor(0xFFFADCE0.toInt())

            if (item.imageBase64.isNotEmpty()) {
                val tag = item.itemId
                binding.ivItemImage.tag = tag

                // Decode on a background thread; post back if this view still belongs to the same item
                CoroutineScope(Dispatchers.Main).launch {
                    val bitmap: Bitmap? = withContext(Dispatchers.IO) {
                        ImageUtils.base64ToBitmap(item.imageBase64)
                    }
                    if (binding.ivItemImage.tag == tag && bitmap != null) {
                        binding.ivItemImage.setImageBitmap(bitmap)
                        binding.ivItemImage.setBackgroundColor(0x00000000)
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