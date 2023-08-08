package com.example.shoppingcart

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import com.example.shoppingcart.databinding.ActivityLoginEmailBinding
import com.google.firebase.auth.FirebaseAuth

class LoginEmailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginEmailBinding

    private companion object{
        private const val TAG = "LOGIN_TAG"
    }

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.toolbarBackBtn.setOnClickListener {
            onBackPressed()
        }

        binding.noAccountTv.setOnClickListener {
            startActivity(Intent(this, RegisterEmailActivity::class.java))
        }

        binding.loginBtn.setOnClickListener {
            validateData()
        }
    }

    private fun validateData(){
        var email = ""
        var password = ""
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()

        if(email.isEmpty()){
            binding.emailEt.error = "Please enter an email"
            binding.emailEt.requestFocus()
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.emailEt.error = "Invalid Email format"
            binding.emailEt.requestFocus()
        }
        else if(email.isEmpty()){
            binding.passwordEt.error = "Please enter an password"
            binding.passwordEt.requestFocus()
        }
        else{
            loginUser(email, password)
        }
    }

    private fun loginUser(email: String, password: String){
        progressDialog.setMessage("Loggin In")
        progressDialog.show()

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                Log.d(TAG, "loginUser: Logged in")
                progressDialog.dismiss()
                startActivity(Intent(this, MainActivity::class.java))
                finishAffinity()
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Log.d(TAG, "loginUser: ", it)
                Utils.toast(this, "Unable to login due to ${it.message}")
            }
    }
}