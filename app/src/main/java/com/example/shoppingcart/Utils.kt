package com.example.shoppingcart

import android.content.Context
import android.text.format.DateFormat
import android.widget.Toast
import java.util.Calendar
import java.util.Locale

object Utils {

    const val AD_STATUS_AVAILABLE = "AVAILABLE"
    const val AD_STATUS_SOLD = "SOLD"

    val categories = arrayOf(
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
}