package com.example.shoppingcart.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import androidx.appcompat.widget.PopupMenu
import com.bumptech.glide.Glide
import com.example.shoppingcart.R
import com.example.shoppingcart.Utils
import com.example.shoppingcart.adapters.AdapterImageSlider
import com.example.shoppingcart.databinding.ActivityAdDetailsBinding
import com.example.shoppingcart.models.ModelAd
import com.example.shoppingcart.models.ModelImageSlider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdDetailsBinding

    private companion object{
        private const val TAG = "AD_DETAILS_TAG"
    }

    private lateinit var firebaseAuth: FirebaseAuth

    private var adId = ""

    private var adLatitude = 0.0
    private var adLongitude = 0.0

    private var sellerUid = ""
    private var sellerPhone = ""
    private var favorite = false

    private lateinit var imageSliderArray: ArrayList<ModelImageSlider>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //hide some UI views in start. We will show edit, delete option if user is ad owner. we will show call, chat, sms option if user isnt ad owner
        binding.toolbarEditBtn.visibility = View.GONE
        binding.toolbarDeleteBtn.visibility = View.GONE
        binding.chatBtn.visibility = View.GONE
        binding.callBtn.visibility = View.GONE
        binding.smsBtn.visibility = View.GONE

        firebaseAuth = FirebaseAuth.getInstance()

        adId = intent.getStringExtra("adId").toString()

        if(firebaseAuth.currentUser != null){
            checkIsFavorite()
        }

        loadAdDetails()
        loadAdImages()

        binding.toolbarBackBtn.setOnClickListener {
            onBackPressed()
        }

        binding.toolbarDeleteBtn.setOnClickListener {
            val materialAlertDialogBuilder = MaterialAlertDialogBuilder(this)
            materialAlertDialogBuilder.setTitle("Delete Ad")
                .setMessage("Are you sure you want to delete this ad?")
                .setPositiveButton("DELETE"){dialog, which->
                    Log.d(TAG, "onCreate: Delete clicked")
                    deleteAd()
                }
                .setNegativeButton("CANCEL"){dialog, which->
                    Log.d(TAG, "onCreate: Cancel clicked")
                    dialog.dismiss()
                }.show()
        }

        binding.toolbarEditBtn.setOnClickListener {
            editOptionsDialog()
        }

        binding.toolbarFavBtn.setOnClickListener {
            //if fav remove from favs and if not fav add to favs
            if(favorite){
                Utils.removeFromFavorite(this, adId)
            }
            else{
                Utils.addToFavorite(this, adId)
            }
        }

        binding.sellerProfileCv.setOnClickListener {
            val intent = Intent(this, AdSellerProfileActivity::class.java)
            intent.putExtra("sellerUid", sellerUid)
            startActivity(intent)
        }

        binding.chatBtn.setOnClickListener {

        }

        binding.callBtn.setOnClickListener {
            Utils.callIntent(this, sellerPhone)
        }

        binding.smsBtn.setOnClickListener {
            Utils.smsIntent(this, sellerPhone)
        }

        binding.mapBtn.setOnClickListener {
            Utils.mapIntent(this, adLatitude, adLongitude)
        }
    }

    private fun editOptionsDialog() {
        Log.d(TAG, "editOptionsDialog: ")
        val popupMenu = PopupMenu(this, binding.toolbarEditBtn)
        popupMenu.menu.add(Menu.NONE, 0, 0, "Edit")
        popupMenu.menu.add(Menu.NONE, 1, 1, "Mark As Sold")

        popupMenu.show()
        popupMenu.setOnMenuItemClickListener { menuItem ->
            val itemId = menuItem.itemId

            if(itemId == 0){
                val intent = Intent(this, AdCreateActivity::class.java)
                intent.putExtra("isEditMode", true)
                intent.putExtra("adId", adId)
                startActivity(intent)
            }
            else if(itemId == 1){
                showMarkAsSoldDialog()
            }
            return@setOnMenuItemClickListener true
        }
    }

    private fun showMarkAsSoldDialog() {
        Log.d(TAG, "showMarkAsSoldDialog: ")

        val alertDialog = MaterialAlertDialogBuilder(this)
        alertDialog.setTitle("Mark as sold")
            .setMessage("Are you sure you want to mark this Ad as sold?")
            .setPositiveButton("SOLD"){dialog, which ->
                Log.d(TAG, "showMarkAsSoldDialog: Sold clicked")
                val hashMap = HashMap<String, Any>()
                hashMap["status"] = "${Utils.AD_STATUS_SOLD}"

                val ref = FirebaseDatabase.getInstance().getReference("Ads")
                ref.child(adId)
                    .updateChildren(hashMap)
                    .addOnSuccessListener {
                        Log.d(TAG, "showMarkAsSoldDialog: MArked as sold")
                    }
                    .addOnFailureListener {
                        Log.d(TAG, "showMarkAsSoldDialog:", it)
                        Utils.toast(this, "Failed to mark as sold")
                    }
            }
            .setNegativeButton("CANCEL"){dialog, which ->
                Log.d(TAG, "showMarkAsSoldDialog: Cancel clicked")
                dialog.dismiss()
            }
            .show()
    }

    private fun loadAdDetails(){
        Log.d(TAG, "loadAdDetails: ")
        val ref = FirebaseDatabase.getInstance().getReference("Ads")
        ref.child(adId)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    try{
                        val modelAd = snapshot.getValue(ModelAd::class.java)
                        sellerUid = "${modelAd!!.uid}"
                        val title = modelAd.title
                        val description = modelAd.description
                        val address = modelAd.address
                        val condition = modelAd.condition
                        val category = modelAd.category
                        val price = modelAd.price
                        adLatitude = modelAd.latitude
                        adLongitude = modelAd.longitude
                        val timestamp = modelAd.timestamp

                        val formattedDate = Utils.formatTimestampDate(timestamp)

                        //check if ad is by currently signed in user
                        if(sellerUid == firebaseAuth.uid){
                            //ad is created by current user so
                            //1) should be able to edit and delete ad
                            binding.toolbarEditBtn.visibility = View.VISIBLE
                            binding.toolbarDeleteBtn.visibility = View.VISIBLE

                            binding.sellerProfileCv.visibility = View.GONE
                            binding.sellerProfileLabelTv.visibility = View.GONE
                        }
                        else{
                            binding.chatBtn.visibility = View.VISIBLE
                            binding.callBtn.visibility = View.VISIBLE
                            binding.smsBtn.visibility = View.VISIBLE

                            binding.sellerProfileCv.visibility = View.VISIBLE
                            binding.sellerProfileLabelTv.visibility = View.VISIBLE
                        }

                        binding.titleTv.text = title
                        binding.descriptionTv.text = description
                        binding.addressTv.text = address
                        binding.coditionTv.text = condition
                        binding.priceTv.text = price
                        binding.categoryTv.text = category
                        binding.dateTv.text = formattedDate

                        loadSellerDetails()
                    }catch (e: Exception){
                        Log.e(TAG, "onDataChange: ", e)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun loadSellerDetails() {
        Log.d(TAG, "loadSellerDetails: ")
        //Db path to seller info
        val ref =  FirebaseDatabase.getInstance().getReference("Users")
        ref.child(sellerUid)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = "${snapshot.child("name").value}"
                    val phoneCode = "${snapshot.child("phoneCode").value}"
                    val phoneNumber = "${snapshot.child("phoneNumber").value}"
                    val profileImageUrl = "${snapshot.child("profileImageUrl").value}"
                    val timestamp = snapshot.child("timestamp").value as Long

                    val formattedDate = Utils.formatTimestampDate(timestamp)

                    sellerPhone = "$phoneCode$phoneNumber"

                    binding.sellerNameTV.text = name
                    binding.memberSinceTv.text = formattedDate

                    try{
                        Glide.with(this@AdDetailsActivity)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.ic_person_white)
                            .into(binding.sellerProfileIv)
                    }catch (e: Exception){
                        Log.e(TAG, "onDataChange: ", e)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun checkIsFavorite(){
        Log.d(TAG, "checkIsFavorite: ")
        //DB path to ckeck if ad is in favorite of current user
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child("${firebaseAuth.uid}").child("Favorites").child(adId)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    favorite = snapshot.exists()

                    if(favorite){
                        binding.toolbarFavBtn.setImageResource(R.drawable.ic_fav_yes)
                    }
                    else{
                        binding.toolbarFavBtn.setImageResource(R.drawable.ic_fav_no)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun loadAdImages(){
        Log.d(TAG, "loadAdImages: ")

        imageSliderArray = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Ads")
        ref.child(adId).child("Images")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    imageSliderArray.clear()
                    for(ds in snapshot.children){

                        try{
                            val modelImagesSlider = ds.getValue(ModelImageSlider::class.java)
                            imageSliderArray.add(modelImagesSlider!!)
                        }catch (e: Exception){
                            Log.e(TAG, "onDataChange: ", e)
                        }

                        val adapterImageSlider = AdapterImageSlider(this@AdDetailsActivity, imageSliderArray)
                        binding.imageSliderVp.adapter = adapterImageSlider
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun deleteAd(){
        Log.d(TAG, "deleteAd: ")

        val ref = FirebaseDatabase.getInstance().getReference("Ads")
        ref.child(adId)
            .removeValue()
            .addOnSuccessListener {
                Log.d(TAG, "deleteAd: Deleted")
                Utils.toast(this, "Deleted")
                finish()
            }
            .addOnFailureListener {
                Log.e(TAG, "deleteAd: ", it)
                Utils.toast(this, "Failed to delete")
            }
    }
}