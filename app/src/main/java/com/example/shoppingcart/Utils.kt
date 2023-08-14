package com.example.shoppingcart

import android.content.Context
import android.text.format.DateFormat
import android.widget.Toast
import java.util.Calendar
import java.util.Locale

object Utils {

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