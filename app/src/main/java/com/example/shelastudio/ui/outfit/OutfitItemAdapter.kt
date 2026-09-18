package com.example.shelastudio.ui.outfit

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.shelastudio.data.model.ClothingItem
import com.example.shelastudio.databinding.ItemOutfitRowBinding
import com.example.shelastudio.util.ImageUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Row item for showing clothing items inside an outfit detail view.
 */
class OutfitItemAdapter(
    private var items: List<Pair<String, ClothingItem>> = emptyList()
) : RecyclerView.Adapter<OutfitItemAdapter.RowHolder>() {

    fun setData(newItems: List<Pair<String, ClothingItem>>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RowHolder {
        val binding = ItemOutfitRowBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return RowHolder(binding)
    }

    override fun onBindViewHolder(holder: RowHolder, position: Int) {
        val (slot, item) = items[position]
        holder.bind(slot, item)
    }

    override fun getItemCount() = items.size

    inner class RowHolder(
        private val binding: ItemOutfitRowBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(slot: String, item: ClothingItem) {
            binding.tvSlotLabel.text = slot
            binding.tvItemName.text = item.name
            binding.tvItemCategory.text = item.category

            binding.ivItem.setImageBitmap(null)
            if (item.imageBase64.isNotEmpty()) {
                val tag = item.itemId
                binding.ivItem.tag = tag
                CoroutineScope(Dispatchers.Main).launch {
                    val bitmap: Bitmap? = withContext(Dispatchers.IO) {
                        ImageUtils.base64ToBitmap(item.imageBase64)
                    }
                    if (binding.ivItem.tag == tag && bitmap != null) {
                        binding.ivItem.setImageBitmap(bitmap)
                    }
                }
            }
        }
    }
}