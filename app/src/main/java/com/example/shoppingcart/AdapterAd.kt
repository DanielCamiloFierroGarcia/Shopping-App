package com.example.shoppingcart

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.shoppingcart.databinding.RowAdBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdapterAd : RecyclerView.Adapter<AdapterAd.HolderAd>, Filterable{

    private lateinit var binding: RowAdBinding

    var adArrayList: ArrayList<ModelAd>

    private var filterList: ArrayList<ModelAd>

    private var filter: FilterAd? = null

    private var context: Context

    private var firebaseAuth: FirebaseAuth

    constructor(adArrayList: ArrayList<ModelAd>, context: Context) {
        this.adArrayList = adArrayList
        this.context = context
        this.filterList = adArrayList
        firebaseAuth = FirebaseAuth.getInstance()
    }

    private companion object{
        private const val TAG = "ADAPTER_AD_TAG"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderAd {
        binding = RowAdBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderAd(binding.root)
    }

    override fun getItemCount(): Int {
        return adArrayList.size
    }

    override fun getFilter(): Filter {
        //init the filter obj only if its not null
        if(filter == null){
            filter = FilterAd(this, filterList)
        }
        return filter as FilterAd
    }

    override fun onBindViewHolder(holder: HolderAd, position: Int) {
        val modelAd = adArrayList[position]

        val title = modelAd.title
        val description = modelAd.description
        val address = modelAd.description
        val condition = modelAd.condition
        val price = modelAd.price
        val timestamp = modelAd.timestamp
        val formattedDate = Utils.formatTimestampDate(timestamp)

        loadAdFirstImage(modelAd, holder)
        //if user is logged then check if the ad is in favs of current user
        if(firebaseAuth.currentUser != null){
            checkIsFavorite(modelAd, holder)
        }

        holder.titleTv.text = title
        holder.descriptionTv.text = description
        holder.addressTv.text = address
        holder.conditionTv.text = condition
        holder.priceTv.text = price
        holder.dateTv.text = formattedDate

        holder.favBtn.setOnClickListener {
            val favorite = modelAd.favorite
            if(favorite){
                Utils.removeFromFavorite(context, modelAd.id)
            }
            else{
                Utils.addToFavorite(context, modelAd.id)
            }
        }
    }

    private fun checkIsFavorite(modelAd: ModelAd, holder: AdapterAd.HolderAd) {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(modelAd.id)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val favorite = snapshot.exists()
                    modelAd.favorite = favorite

                    if(favorite){
                        holder.favBtn.setImageResource(R.drawable.ic_fav_yes)
                    }
                    else{
                        holder.favBtn.setImageResource(R.drawable.ic_fav_no)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun loadAdFirstImage(modelAd: ModelAd, holder: HolderAd) {
        //load first image from available images of ad. ex: if there ar 5 images of ad load first
        val adId = modelAd.id
        Log.d(TAG, "loadAdFirstImage: adId $adId")

        val ref = FirebaseDatabase.getInstance().getReference("Ads")
        ref.child(adId).child("Images").limitToFirst(1)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(ds in snapshot.children){
                        val imageUrl = "${ds.child("imageUrl").value}"
                        //set image to IV
                        try{
                            Glide.with(context)
                                .load(imageUrl)
                                .placeholder(R.drawable.ic_image_gray)
                                .into(holder.imageIv)
                        }catch (e: Exception){
                            Log.e(TAG, "onDataChange: ", e)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }


    inner class HolderAd(itemView: View) : RecyclerView.ViewHolder(itemView){
        var imageIv = binding.imageIv
        var titleTv = binding.titleTv
        var descriptionTv = binding.descriptionTV
        var favBtn = binding.favBtn
        var addressTv = binding.addressTv
        var conditionTv = binding.conditionTv
        var priceTv = binding.priceTv
        var dateTv = binding.dateTv
    }
}