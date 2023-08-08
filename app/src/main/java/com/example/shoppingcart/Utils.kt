package com.example.shoppingcart

import android.content.Context
import android.widget.Toast

object Utils {

    fun getTimeStamp() : Long{
        return System.currentTimeMillis()
    }

    fun toast(context: Context, message: String){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}