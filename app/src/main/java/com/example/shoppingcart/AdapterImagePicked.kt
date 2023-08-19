package com.example.shoppingcart

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.shoppingcart.databinding.RowImagePickedBinding

class AdapterImagePicked(private val context: Context, private val imagePickedArrayList: ArrayList<ModelImagePicked>) : Adapter<AdapterImagePicked.HolderImagePicked>(){

    private lateinit var binding: RowImagePickedBinding

    private companion object{
        private const val TAG = "IMAGES TAG"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderImagePicked {
        //inflate the row_images picked xml
        binding = RowImagePickedBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderImagePicked(binding.root)
    }

    override fun getItemCount(): Int {
        //return size of list
        return imagePickedArrayList.size
    }

    override fun onBindViewHolder(holder: HolderImagePicked, position: Int) {
        //get data from particular position of list and set UI views of row... and handle clicks
        val model = imagePickedArrayList[position]

        val imageUri = model.imageUri
        Log.d(TAG, "onBindViewHolder: imageUri $imageUri")

        try{
            Glide.with(context)
                .load(imageUri)
                .placeholder(R.drawable.ic_image_gray)
                .into(holder.imageIv)
        }catch (e: Exception){
            Log.d(TAG, "onBindViewHolder: ", e)
        }

        holder.closeBtn.setOnClickListener {
            imagePickedArrayList.remove(model)
            notifyDataSetChanged()
        }
    }

    inner class HolderImagePicked(itemView: View) : ViewHolder(itemView){
        var imageIv = binding.imageIv
        var closeBtn = binding.closeBtn
    }
}