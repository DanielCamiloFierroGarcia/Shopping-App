package com.example.shoppingcart.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.shoppingcart.R
import com.example.shoppingcart.Utils
import com.example.shoppingcart.databinding.RowImagePickedBinding
import com.example.shoppingcart.models.ModelImagePicked
import com.google.firebase.database.FirebaseDatabase

class AdapterImagePicked(private val context: Context, private val imagePickedArrayList: ArrayList<ModelImagePicked>, private val adId: String) : Adapter<AdapterImagePicked.HolderImagePicked>(){

    private lateinit var binding: RowImagePickedBinding

    private companion object{
        private const val TAG = "IMAGES_TAG"
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

        if(model.fromInternet){
            //image is from internet/FB DB. Get image url of the image to set
            try{
                val imageUrl = model.imageUrl

                Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_image_gray)
                    .into(holder.imageIv)
            }catch (e: Exception){
                Log.e(TAG, "onBindViewHolder: ", e)
            }
        }
        else{
            //Image picked from gallery/camera Get image Uri of image
            val imageUri = model.imageUri
            try{
                Glide.with(context)
                    .load(imageUri)
                    .placeholder(R.drawable.ic_image_gray)
                    .into(holder.imageIv)
            }catch (e: Exception){
                Log.d(TAG, "onBindViewHolder: ", e)
            }
        }

        holder.closeBtn.setOnClickListener {
            //check if image is from device or FB
            if(model.fromInternet){
                deleteImageFirebase(model, holder, position)
            }
            else{
                imagePickedArrayList.remove(model)
                notifyDataSetChanged()
            }
        }
    }

    private fun deleteImageFirebase(model: ModelImagePicked, holder: AdapterImagePicked.HolderImagePicked, position: Int) {
        Log.d(TAG, "deleteImageFirebase: adId: $adId")
        val imageId = model.id

        Log.d(TAG, "deleteImageFirebase: imageId $imageId")
        Log.d(TAG, "deleteImageFirebase: adId $adId")

        val ref = FirebaseDatabase.getInstance().getReference("Ads")
        ref.child(adId).child("Images").child(imageId)
            .removeValue()
            .addOnSuccessListener {
                Log.d(TAG, "deleteImageFirebase: Image $imageId deleted")
                Utils.toast(context, "Image deleted")
                try{
                    imagePickedArrayList.remove(model)
                    notifyItemRemoved(position)
                }catch (e: Exception){
                    Log.e(TAG, "deleteImageFirebase1: ", e)
                }
            }
            .addOnFailureListener {
                Log.e(TAG, "deleteImageFirebase1: ", it)
                Utils.toast(context, "Failed to delete image")
            }
    }

    inner class HolderImagePicked(itemView: View) : ViewHolder(itemView){
        var imageIv = binding.imageIv
        var closeBtn = binding.closeBtn
    }
}