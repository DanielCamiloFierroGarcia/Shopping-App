package com.example.shoppingcart.activities

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import com.example.shoppingcart.Utils
import com.example.shoppingcart.databinding.ActivityRegisterEmailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterEmailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterEmailBinding

    private companion object{
        private const val TAG = "REGISTER_TAG"
    }

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.toolbarBackBtn.setOnClickListener {
            onBackPressed()
        }

        binding.haveAccountTv.setOnClickListener {
            onBackPressed()
        }

        binding.registerBtn.setOnClickListener {
            validateData()
        }
    }

    private fun validateData(){
        var email = ""
        var password = ""
        var cPassword = ""

        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()
        cPassword = binding.cPasswordEt.text.toString().trim()

        if(email.isEmpty()){
            binding.emailEt.error = "Please enter an email"
            binding.emailEt.requestFocus()
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.emailEt.error = "Invalid Email format"
            binding.emailEt.requestFocus()
        }
        else if(password.isEmpty()){
            binding.passwordEt.error = "Please enter an password"
            binding.passwordEt.requestFocus()
        }
        else if(cPassword.isEmpty()){
            binding.cPasswordEt.error = "Please enter the confirmation"
            binding.cPasswordEt.requestFocus()
        }
        else if(password != cPassword){
            binding.cPasswordEt.error = "Passwords must be the same"
            binding.cPasswordEt.requestFocus()
        }
        else{
            registerUser(email, password)
        }
    }

    private fun registerUser(email: String, password: String) {
        progressDialog.setMessage("Creating account")
        progressDialog.show()

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                Log.d(TAG, "registerUser: Register success")
                updateUserInfo()
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Log.d(TAG, "registerUser: ", it)
                Utils.toast(this, "Failed to create account due to ${it.message}")
            }
    }

    private fun updateUserInfo() {
        progressDialog.setMessage("Saving user info")

        val timestamp = Utils.getTimeStamp()
        val registeredUserEmail = firebaseAuth.currentUser!!.email
        val registeredUserUid = firebaseAuth.uid

        val hashMap = HashMap<String, Any>()
        hashMap["name"] = ""
        hashMap["phoneCode"] = ""
        hashMap["phoneNumber"] = ""
        hashMap["profileImageUrl"] = ""
        hashMap["dob"] = ""
        hashMap["userType"] = "Email"
        hashMap["typingTo"] = ""
        hashMap["timestamp"] = timestamp
        hashMap["onlineStatus"] = true
        hashMap["email"] = "$registeredUserEmail"
        hashMap["uid"] = "$registeredUserUid"

        val reference = FirebaseDatabase.getInstance().getReference("Users")
        reference.child(registeredUserUid!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                startActivity(Intent(this, MainActivity::class.java))
                Utils.toast(this, "User registered")
                finishAffinity()//finish current and all activities from back stack
            }
            .addOnFailureListener {
                Log.d(TAG, "updateUserInfo: ", it)
                progressDialog.dismiss()
                Utils.toast(this, "Failed to save user info due to ${it.message}")
            }
    }
}