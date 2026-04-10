package com.sharease.app.ui.components

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sharease.app.R
import com.sharease.app.data.model.FileItem
import com.sharease.app.data.model.ReceivedItem
import com.sharease.app.databinding.ItemFileBinding
import com.sharease.app.databinding.ItemReceivedBinding

class ReceivedAdapter(
    private val items: List<ReceivedItem>
) : RecyclerView.Adapter<ReceivedAdapter.ReceivedViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReceivedViewHolder {
        val binding = ItemReceivedBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReceivedViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReceivedViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ReceivedViewHolder(
        private val binding: ItemReceivedBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: ReceivedItem) {
            binding.tvFileName.text = item.name
            binding.tvFileSize.text = when (item.type) {
                ReceivedItem.ItemType.TEXT -> "Text message"
                ReceivedItem.ItemType.FILE -> FileItem.formatSize(item.size)
            }
        }
    }
}
