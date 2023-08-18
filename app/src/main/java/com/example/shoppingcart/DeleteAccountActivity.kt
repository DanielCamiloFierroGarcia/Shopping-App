package com.example.shoppingcart

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.shoppingcart.databinding.ActivityDeleteAccountBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DeleteAccountActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDeleteAccountBinding

    companion object{
        private const val TAG = "DELETE_ACCOUNT_TAG"
    }

    private lateinit var progressDialog: ProgressDialog

    private lateinit var firebaseAuth: FirebaseAuth

    private var firebaseUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeleteAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()

        firebaseUser = firebaseAuth.currentUser

        binding.toolbarBackBtn.setOnClickListener {
            onBackPressed()
        }

        binding.submitBtn.setOnClickListener {
            deleteAccount()
        }
    }

    private fun deleteAccount() {
        Log.d(TAG, "deleteAccount: ")
        progressDialog.setMessage("Deleting account")
        progressDialog.show()

        val myUid = firebaseAuth.uid

        //step 1: delete account from auth
        firebaseUser!!.delete()
            .addOnSuccessListener {
                Log.d(TAG, "deleteAccount: Account deleted")
                progressDialog.setMessage("Deleting user Ads")
                //step 2: remove user ads, currently there are no ads, ads will be saved in DB > Ads > AdId. each ad will countain id of owner
                val refUserAds = FirebaseDatabase.getInstance().getReference("Ads")
                refUserAds.orderByChild("uid").equalTo(myUid)
                    .addValueEventListener(object : ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for(ds in snapshot.children){
                                ds.ref.removeValue()
                            }
                            progressDialog.setMessage("Deleting user Data")

                            //step 3 rempve user data DB > Users > userId
                            val refUsers = FirebaseDatabase.getInstance().getReference("Users")
                            refUsers.child(myUid!!).removeValue()
                                .addOnSuccessListener {
                                    Log.d(TAG, "onDataChange: User data deleted")
                                    progressDialog.dismiss()
                                    startMainActivity()
                                }
                                .addOnFailureListener {
                                    Log.d(TAG, "onDataChange: ", it)
                                    progressDialog.dismiss()
                                    Utils.toast(this@DeleteAccountActivity, "Failed to delete user due to ${it.message}")
                                    startMainActivity()
                                }
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }
                    })
            }
            .addOnFailureListener {
                Log.d(TAG, "deleteAccount: ", it)
                progressDialog.dismiss()
                Utils.toast(this, "Failed to delete due to ${it.message}")
            }
    }

    private fun startMainActivity(){
        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
    }

    override fun onBackPressed() {
        startMainActivity()
    }
}