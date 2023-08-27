package com.example.shoppingcart.fragments

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.shoppingcart.activities.ChangePasswordActivity
import com.example.shoppingcart.activities.DeleteAccountActivity
import com.example.shoppingcart.activities.MainActivity
import com.example.shoppingcart.activities.ProfileEditActivity
import com.example.shoppingcart.R
import com.example.shoppingcart.Utils
import com.example.shoppingcart.databinding.FragmentAccountBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AccountFragment : Fragment() {

    private lateinit var binding: FragmentAccountBinding

    private companion object{
        private const val TAG = "ACCOUNT_TAG"
    }

    private lateinit var mContext: Context

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentAccountBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressDialog = ProgressDialog(mContext)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()

        loadMyInfo()

        binding.logoutCv.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(mContext, MainActivity::class.java))
            activity?.finishAffinity()
        }

        binding.editProfileCv.setOnClickListener {
            startActivity(Intent(mContext, ProfileEditActivity::class.java))
        }

        binding.changePasswordCv.setOnClickListener {
            startActivity(Intent(mContext, ChangePasswordActivity::class.java))
        }

        binding.verifyAccountCv.setOnClickListener {
            verifyAccount()
        }

        binding.deleteAccountCv.setOnClickListener {
            startActivity(Intent(mContext, DeleteAccountActivity::class.java))
        }
    }

    private fun loadMyInfo() {
        //Log.d(TAG, "loadMyInfo eef3: ${firebaseAuth.currentUser!!.email} and ${firebaseAuth.currentUser!!.isEmailVerified}")
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child("${firebaseAuth.uid}")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val dob = "${snapshot.child("dob").value}"
                    val email = "${snapshot.child("email").value}"
                    val name = "${snapshot.child("name").value}"
                    val phoneCode = "${snapshot.child("phoneCode").value}"
                    val phoneNumber = "${snapshot.child("phoneNumber").value}"
                    val profileImageUrl = "${snapshot.child("profileImageUrl").value}"
                    var timestamp = "${snapshot.child("timestamp").value}"
                    val usertype = "${snapshot.child("userType").value}"

                    val phone = phoneCode+phoneNumber

                    if(timestamp == "null"){
                        timestamp = "0"
                    }

                    val formattedDate = Utils.formatTimestampDate(timestamp = timestamp.toLong())

                    binding.emailTv.text = email
                    binding.nameTv.text = name
                    binding.dobTv.text = dob
                    binding.phoneTv.text = phone
                    binding.memberSinceTv.text = formattedDate

                    if(usertype == "Email"){
                        val isVerified = firebaseAuth.currentUser!!.isEmailVerified
                        Log.d(TAG, "onDataChange: aaaa $isVerified")
                        if(isVerified){
                            binding.verifyAccountCv.visibility = View.GONE
                            binding.verificationTv.text = "Verified"
                        }
                        else{
                            binding.verifyAccountCv.visibility = View.VISIBLE
                            binding.verificationTv.text = "Not Verified"
                        }
                    }
                    else{
                        binding.verifyAccountCv.visibility = View.GONE
                        binding.verificationTv.text = "Verified"
                    }

                    try{
                        Glide.with(mContext)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.ic_person_white)
                            .into(binding.profileIv)
                    }catch (e: Exception){
                        Log.d(TAG, "onDataChange: ", e)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun verifyAccount(){
        Log.d(TAG, "verifyAccount: ")

        progressDialog.setMessage("Sending account verification to email")
        progressDialog.show()

        firebaseAuth.currentUser!!.sendEmailVerification()
            .addOnSuccessListener {
                Log.d(TAG, "verifyAccount: Succesfully sent")
                progressDialog.dismiss()
                Utils.toast(mContext, "Verification sent to email")
            }
            .addOnFailureListener {
                Log.d(TAG, "verifyAccount: ", it)
                progressDialog.dismiss()
                Utils.toast(mContext, "Failed to send due to ${it.message}")
            }
    }
}