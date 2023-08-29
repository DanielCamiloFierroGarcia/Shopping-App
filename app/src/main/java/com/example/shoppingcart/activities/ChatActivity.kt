package com.example.shoppingcart.activities

import android.app.ProgressDialog
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.bumptech.glide.Glide
import com.example.shoppingcart.R
import com.example.shoppingcart.Utils
import com.example.shoppingcart.databinding.ActivityChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding

    private companion object{
        private const val TAG = "CHAT_TAG"
    }

    private lateinit var progressDialog: ProgressDialog

    private lateinit var firebaseAuth: FirebaseAuth

    private var receiptUid = ""

    private var myUid = ""
    //WIll generate using UIDs of current user and receipt
    private var chatPath = ""
    //Uri of image picked from camera/gallery
    private var imageUri:Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please Wait")
        progressDialog.setCanceledOnTouchOutside(false)

        receiptUid = intent.getStringExtra("receiptUid").toString()

        myUid = firebaseAuth.uid!!

        chatPath = Utils.chatPath(receiptUid, myUid)
    }

    private fun loadReceiptDetails(){
        Log.d(TAG, "loadReceiptDetails: ")

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(myUid)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    try{
                        val name = "${snapshot.child("name").value}"
                        val profileImageUrl = "${snapshot.child("profileImageUrl").value}"

                        binding.toolbarTitleTv.text = name

                        try {
                            Glide.with(this@ChatActivity)
                                .load(profileImageUrl)
                                .placeholder(R.drawable.ic_person_white)
                                .into(binding.toolbarProfileIv)//LEFT AT 29:54
                        }catch (e: Exception){
                            Log.e(TAG, "onDataChange: ", e)
                        }
                    }catch (e: Exception){
                        Log.e(TAG, "onDataChange: ", e)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}