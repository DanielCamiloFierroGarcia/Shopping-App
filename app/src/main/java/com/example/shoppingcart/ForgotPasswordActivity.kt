package com.example.shoppingcart

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import com.example.shoppingcart.databinding.ActivityForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityForgotPasswordBinding

    private companion object{
        private const val TAG = "FORGOT_PASSWORD"
    }

    private lateinit var progressDialog: ProgressDialog

    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.toolbarBackBtn.setOnClickListener {
            onBackPressed()
        }

        binding.submitBtn.setOnClickListener {
            validateData()
        }
    }
    private var email = ""

    private fun validateData() {
        email = binding.emailEt.text.toString().trim()
        Log.d(TAG, "validateData: $email")

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.emailEt.error = "Invalid email pattern"
            binding.emailEt.requestFocus()
        }
        else{
            sendPasswordRecoveryInstructions()
        }
    }

    private fun sendPasswordRecoveryInstructions() {
        Log.d(TAG, "sendPasswordRecoveryInstructions: ")
        progressDialog.setMessage("Sending password reset mail to $email")
        progressDialog.show()

        firebaseAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                Log.d(TAG, "sendPasswordRecoveryInstructions: Instructions sent")
                progressDialog.dismiss()
                Utils.toast(this, "Instructions sent")
            }
            .addOnFailureListener {
                Log.d(TAG, "sendPasswordRecoveryInstructions: ", it)
                progressDialog.dismiss()
                Utils.toast(this, "Failed to send due to ${it.message}")
            }
    }
}