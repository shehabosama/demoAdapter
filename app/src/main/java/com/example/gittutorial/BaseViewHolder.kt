package com.example.gittutorial

import android.util.Log
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.gittutorial.databinding.ItemLoadingBinding
import com.example.gittutorial.databinding.ItemPhotoBinding
import com.plcoding.androidstorage.SharedStoragePhoto
import com.squareup.picasso.Picasso

sealed class BaseViewHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {

    class ImageViewHolder(private val binding: ItemPhotoBinding) : BaseViewHolder(binding) {
        fun bind(item: SharedStoragePhoto, onPhotoClick: (SharedStoragePhoto) -> Unit) {

            // binding.ivPhoto.setImageURI(item.contentUri)
            Picasso.get()
                .load(item.contentUri)
                .placeholder(R.drawable.ic_launcher_background)
                .resize(200, 200)
                .centerCrop()
                .into(binding.ivPhoto);

            val aspectRatio = item.width.toFloat() / item.height.toFloat()
            ConstraintSet().apply {
                clone(binding.root)
                setDimensionRatio(binding.ivPhoto.id, aspectRatio.toString())
                applyTo(binding.root)
            }


            binding.ivPhoto.setOnLongClickListener {
                onPhotoClick(item)
                true
            }

//            val aspectRatio = binding.ivPhoto.width.toFloat() / binding.ivPhoto.height.toFloat()
//
//            ConstraintSet().apply {
//                clone(binding.root)
//                setDimensionRatio(binding.ivPhoto.id, aspectRatio.toString())
//                applyTo(binding.root)
//            }

            binding.ivPhoto.setOnLongClickListener {
                onPhotoClick(item)
                true
            }

            Log.i("BaseViewHolder", "bind: ")
        }
    }

    class ProgressViewHolder(binding: ItemLoadingBinding) : BaseViewHolder(binding){}
}