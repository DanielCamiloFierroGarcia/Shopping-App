package com.example.shoppingcart

import android.content.Context
import android.text.format.DateFormat
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar
import java.util.Locale

object Utils {

    const val AD_STATUS_AVAILABLE = "AVAILABLE"
    const val AD_STATUS_SOLD = "SOLD"

    //Ads categories icons array
    val categories = arrayOf(
        "All",
        "Mobiles",
        "computer/Laptop",
        "Electronics & Home Appliances",
        "Vehicles",
        "Furniture & Home Decor",
        "Fashion & Beauty",
        "Books",
        "Sports",
        "Animals",
        "Businesses",
        "Agriculture"
    )

    //Ads categories icons array
    val categoryIcons = arrayOf(
        R.drawable.ic_category_all,
        R.drawable.ic_category_mobiles,
        R.drawable.ic_category_computer,
        R.drawable.ic_category_electronics,
        R.drawable.ic_category_vehicles,
        R.drawable.ic_category_furniture,
        R.drawable.ic_category_fashion,
        R.drawable.ic_category_books,
        R.drawable.ic_category_sports,
        R.drawable.ic_category_business,
        R.drawable.ic_category_animals,
        R.drawable.ic_category_agriculture
    )

    val conditions = arrayOf(
        "New",
        "Used",
        "Refurbished"
    )

    fun getTimeStamp() : Long{
        return System.currentTimeMillis()
    }

    fun toast(context: Context, message: String){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun formatTimestampDate(timestamp: Long): String{
        val calendar = Calendar.getInstance(Locale.ENGLISH)
        calendar.timeInMillis = timestamp

        return DateFormat.format("dd/MM/yyyy", calendar).toString()
    }

    fun addToFavorite(context: Context, adId: String){
        val firebaseAuth = FirebaseAuth.getInstance()

        if(firebaseAuth.currentUser == null){
            toast(context, "You're not logged-in")
        }
        else{
            val timestamp = getTimeStamp()

            val hashMap = HashMap<String, Any>()
            hashMap["adId"] = adId
            hashMap["timestamp"] = timestamp

            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseAuth.uid!!).child("Favorites").child(adId)
                .setValue(hashMap)
                .addOnSuccessListener {
                    toast(context, "Added to favorites")
                }
                .addOnFailureListener {
                    toast(context, "Failed to add to favorites")
                }
        }
    }

    fun removeFromFavorite(context: Context, adId: String){
        val firebaseAuth = FirebaseAuth.getInstance()
        if(firebaseAuth.currentUser == null){
            toast(context, "You're not logged-in")
        }
        else{
            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseAuth.uid!!).child("Favorites").child(adId)
                .removeValue()
                .addOnSuccessListener {
                    toast(context, "Removed from favorites")
                }
                .addOnFailureListener {
                    toast(context, "Failed to remove from favorites")
                }
        }
    }
}