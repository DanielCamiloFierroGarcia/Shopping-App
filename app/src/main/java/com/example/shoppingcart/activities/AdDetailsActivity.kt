package com.example.shoppingcart.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.shoppingcart.R
import com.example.shoppingcart.Utils
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
            showMarkAsSoldDialog()
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
                        }
                        else{
                            binding.chatBtn.visibility = View.VISIBLE
                            binding.callBtn.visibility = View.VISIBLE
                            binding.smsBtn.visibility = View.VISIBLE
                        }

                        binding.titleTv.text = title
                        binding.descriptionTv.text = description
                        binding.addressTv.text = address
                        binding.coditionTv.text = condition
                        binding.priceTv.text = price
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

    private fun loadSellerDetails() {//LEFT AT 57:28

    }
}