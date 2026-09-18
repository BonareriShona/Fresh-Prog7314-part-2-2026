package com.example.shelastudio.ui.outfit

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.shelastudio.data.model.ClothingItem
import com.example.shelastudio.databinding.ItemSlotPickerBinding
import com.example.shelastudio.util.ImageUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Horizontal list of wardrobe items filtered for the currently selected slot.
 */
class SlotItemAdapter(
    private val onItemSelected: (ClothingItem?) -> Unit
) : ListAdapter<ClothingItem, SlotItemAdapter.SlotViewHolder>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ClothingItem>() {
            override fun areItemsTheSame(oldItem: ClothingItem, newItem: ClothingItem) =
                oldItem.itemId == newItem.itemId
            override fun areContentsTheSame(oldItem: ClothingItem, newItem: ClothingItem) =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlotViewHolder {
        val binding = ItemSlotPickerBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SlotViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SlotViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SlotViewHolder(
        private val binding: ItemSlotPickerBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ClothingItem) {
            binding.tvSlotItemName.text = item.name

            binding.ivSlotItem.setImageBitmap(null)
            binding.ivSlotItem.setBackgroundColor(0xFFFADCE0.toInt())

            if (item.imageBase64.isNotEmpty()) {
                val tag = item.itemId
                binding.ivSlotItem.tag = tag
                CoroutineScope(Dispatchers.Main).launch {
                    val bitmap: Bitmap? = withContext(Dispatchers.IO) {
                        ImageUtils.base64ToBitmap(item.imageBase64)
                    }
                    if (binding.ivSlotItem.tag == tag && bitmap != null) {
                        binding.ivSlotItem.setImageBitmap(bitmap)
                        binding.ivSlotItem.setBackgroundColor(0x00000000)
                    }
                }
            }

            binding.root.setOnClickListener { onItemSelected(item) }
        }
    }
}