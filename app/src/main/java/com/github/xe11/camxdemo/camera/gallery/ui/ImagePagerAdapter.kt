package com.github.xe11.camxdemo.camera.gallery.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import com.github.xe11.camxdemo.camera.gallery.ui.ImagePagerAdapter.ImageViewHolder
import com.github.xe11.camxdemo.databinding.ItemGalleryImageBinding
import java.io.File

internal class ImagePagerAdapter : ListAdapter<File, ImageViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ImageViewHolder {
        val binding = ItemGalleryImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ImageViewHolder(
        private val binding: ItemGalleryImageBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(file: File) {
            binding.imageView.load(file)
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<File>() {

        override fun areItemsTheSame(oldItem: File, newItem: File): Boolean =
            oldItem == newItem

        override fun areContentsTheSame(oldItem: File, newItem: File): Boolean =
            oldItem.absolutePath == newItem.absolutePath
    }
}
