package com.example.shoppingcart

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.format.DateFormat
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.Arrays
import java.util.Calendar
import java.util.Locale

object Utils {

    const val MESSAGE_TYPE_TEXT = "TEXT"
    const val MESSAGE_TYPE_IMAGE = "IMAGE"

    const val AD_STATUS_AVAILABLE = "AVAILABLE"
    const val AD_STATUS_SOLD = "SOLD"

    const val NOTIFICATION_TYPE_NEW_MESSAGE = "NOTIFICATION_TYPE_NEW_MESSAGE"
    const val FCN_SERVER_KEY = "AAAAwXu_vWE:APA91bFwZ5m-QyDiBKzMSZzu2c9hHOTWb4DJoNYF3xc3dRndWVTVet-OnnAjzF6e5AU8fOI-gRr6B2daEqhe-SFQP18MH6S4dLjC-TiuvRDT9l4Xh2NqeShZtodL3O2O6LWzapJrV6QP"

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
    //to format with time, for chats
    fun formatTimestampDateTime(timestamp: Long): String{
        val calendar = Calendar.getInstance(Locale.ENGLISH)
        calendar.timeInMillis = timestamp

        return DateFormat.format("dd/MM/yyyy hh:mm:a", calendar).toString()
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

    //Launch Call intent with phone number
    fun callIntent(context: Context, phone: String){
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("tel:${Uri.encode(phone)}"))
        context.startActivity(intent)
    }

    //Launch SMS intent with phone number
    fun smsIntent(context: Context, phone: String){
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:${Uri.encode(phone)}"))
        context.startActivity(intent)
    }

    //Launch Google Map intent with phone number
    fun mapIntent(context: Context, latitude: Double, longitude: Double){
        val gmmIntentUri = Uri.parse("http://maps.google.com/maps?daddr=$latitude,$longitude")

        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")

        if(mapIntent.resolveActivity(context.packageManager) != null){
            context.startActivity(mapIntent)
        }
        else{
            toast(context, "Google Map not installed")
        }
    }

    /**
     * Generate Chat Path
     * THis will generate chat path by sorting these UIDs and concatenate sorted array of UIDs having _ in between
     * All messages of these 2 users will be saved in this path
     *
     * @param receiptUid The UID of the receipt
     * @param uourUid The UID of current logged user**/
    fun chatPath(receiptUid: String, yourUid: String): String{
        //Array of UIDs
        val arrayUids = arrayOf(receiptUid, yourUid)
        //Sort Array
        Arrays.sort(arrayUids)
        //Concatenate both UIDs (after sorting) having _ between
        //return chat path eg if id1 = 1234 and id2 = 5678 path will be 1234_5678
        return "${arrayUids[0]}_${arrayUids[1]}"
    }
}