package com.sharease.app.ui.components

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sharease.app.R
import com.sharease.app.data.model.FileItem
import com.sharease.app.databinding.ItemFileBinding

class FileAdapter(
    private val files: List<FileItem>,
    private val onRemove: (Int) -> Unit
) : RecyclerView.Adapter<FileAdapter.FileViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val binding = ItemFileBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(files[position])
    }

    override fun getItemCount(): Int = files.size

    inner class FileViewHolder(
        private val binding: ItemFileBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(file: FileItem) {
            binding.tvFileName.text = file.name
            binding.tvFileSize.text = file.sizeFormatted
            
            val iconRes = when {
                file.mimeType?.startsWith("image/") == true -> R.drawable.ic_file
                file.mimeType?.startsWith("video/") == true -> R.drawable.ic_file
                file.mimeType?.startsWith("audio/") == true -> R.drawable.ic_file
                file.mimeType?.contains("pdf") == true -> R.drawable.ic_file
                else -> R.drawable.ic_file
            }
            binding.ivIcon.setImageResource(iconRes)
            
            binding.btnRemove.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onRemove(position)
                }
            }
        }
    }
}
