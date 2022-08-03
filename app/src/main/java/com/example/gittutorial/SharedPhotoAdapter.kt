package com.example.gittutorial

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gittutorial.databinding.ItemPhotoBinding
import com.plcoding.androidstorage.SharedStoragePhoto
import com.squareup.picasso.Picasso
import java.io.File

class SharedPhotoAdapter(
    private val onPhotoClick: (SharedStoragePhoto) -> Unit
) : ListAdapter<SharedStoragePhoto, SharedPhotoAdapter.PhotoViewHolder>(Companion) {

    inner class PhotoViewHolder(val binding: ItemPhotoBinding): RecyclerView.ViewHolder(binding.root)

    companion object : DiffUtil.ItemCallback<SharedStoragePhoto>() {
        override fun areItemsTheSame(oldItem: SharedStoragePhoto, newItem: SharedStoragePhoto): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SharedStoragePhoto, newItem: SharedStoragePhoto): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        return PhotoViewHolder(
            ItemPhotoBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photo = currentList[position]
        holder.binding.apply {
           // ivPhoto.setImageURI(photo.contentUri)
             Picasso.get()
                .load(photo.contentUri)
                .placeholder(R.drawable.ic_launcher_background)
                .fit()
                .centerCrop()
                .into(ivPhoto);

            val aspectRatio = photo.width.toFloat() / photo.height.toFloat()
            ConstraintSet().apply {
                clone(root)
                setDimensionRatio(ivPhoto.id, aspectRatio.toString())
                applyTo(root)
            }


            ivPhoto.setOnLongClickListener {
                onPhotoClick(photo)
                true
            }
        }
    }
}