package com.example.shoppingcart.activities

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.shoppingcart.Utils
import com.example.shoppingcart.databinding.ActivityLoginPhoneBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import com.google.firebase.database.FirebaseDatabase
import java.util.concurrent.TimeUnit

class LoginPhoneActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginPhoneBinding

    private companion object{
        private const val TAG = "PHONE_LOGIN_TAG"
    }

    private lateinit var progressDialog: ProgressDialog

    private lateinit var firebaseAuth: FirebaseAuth

    private var forceRefreshingToken: ForceResendingToken? = null

    private lateinit var mCallbacks: OnVerificationStateChangedCallbacks

    private var mVerificationId: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginPhoneBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.phoneInputRl.visibility = View.VISIBLE
        binding.otpInputRl.visibility = View.GONE

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please Wait")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()
        //login for phone login callbacks. Hint: you may put here instead of creating a fun
        phoneLoginCallBacks()
        
        binding.toolbarBackBtn.setOnClickListener { 
            onBackPressed()
        }
        
        binding.sendOtpBtn.setOnClickListener { 
            validateData()
        }
        
        binding.resendOtpTv.setOnClickListener { 
            resendVerificationCode(forceRefreshingToken)
        }
        
        binding.verifyOtpBtn.setOnClickListener { 
            val otp = binding.otpEt.text.toString().trim()
            if(otp.isEmpty()){
                binding.otpEt.error = "Enter OTP"
                binding.otpEt.requestFocus()
            }
            else if(otp.length < 6){
                binding.otpEt.error = "OTP length must be 6 characters long"
                binding.otpEt.requestFocus()
            }
            else{
                verifyPhoneNumberWithCode(mVerificationId, otp)
            }
        }
    }

    private var phoneCode = ""
    private var phoneNumber = ""
    private var phoneNumberWithCode = ""

    private fun validateData(){
        phoneCode = binding.phoneCodeTil.selectedCountryCodeWithPlus
        phoneNumber = binding.phoneNumberEt.text.toString().trim()
        phoneNumberWithCode = phoneCode + phoneNumber

        if(phoneNumber.isEmpty()){
            binding.phoneNumberEt.error = "Enter phone number"
            binding.phoneNumberEt.requestFocus()
        }
        else{
            startPhoneNumberVerification()
        }
    }

    private fun startPhoneNumberVerification() {
        Log.d(TAG, "startPhoneNumberVerification: ")
        progressDialog.setMessage("Sending OTP to $phoneNumberWithCode")
        progressDialog.show()

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumberWithCode)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(mCallbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun phoneLoginCallBacks() {
        Log.d(TAG, "phoneLoginCallBacks: ")
        mCallbacks = object : OnVerificationStateChangedCallbacks(){
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                //this callback will be invoked in 2 situations:
                //1: instant verification: in some cases the phone number can be instantly verified without needing to send a verficiation code
                //2: Auto retrieval: on some devices Google play services can automatically detect the incoing sms and perform verify without user action
                Log.d(TAG, "onVerificationCompleted: ")
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                //this callback is invoked in an invalid request for verify is made, for instance if the phone number format is not valid
                Log.d(TAG, "onVerificationFailed: ", e)

                progressDialog.dismiss()

                Utils.toast(this@LoginPhoneActivity, "${e.message}")
            }

            override fun onCodeSent(verificationId: String, token: ForceResendingToken) {
                //the sms verification code has been sent to the provided phone number we now need to ask the user to enter the code and then constuct a credential by conbining the ccode with a verification id
                Log.d(TAG, "onCodeSent: $verificationId")
                mVerificationId = verificationId
                forceRefreshingToken = token
                //otp is sent to hide progress for now
                progressDialog.dismiss()
                //otp is sent to hide progress for now
                binding.phoneInputRl.visibility = View.GONE
                binding.otpInputRl.visibility = View.VISIBLE

                Utils.toast(this@LoginPhoneActivity, "OTP is sent to $phoneNumberWithCode")

                binding.loginLabelTv.text = "Please type the verification code sent to $phoneNumberWithCode"
            }

            override fun onCodeAutoRetrievalTimeOut(p0: String) {
                super.onCodeAutoRetrievalTimeOut(p0)
            }
        }
    }

    private fun resendVerificationCode(token: ForceResendingToken?){
        Log.d(TAG, "resendVerificationCode: ")
        progressDialog.setMessage("Resending OTP to $phoneNumberWithCode")
        progressDialog.show()

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumberWithCode)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(mCallbacks)
            .setForceResendingToken(token!!)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        progressDialog.setMessage("Loggin in")
        progressDialog.show()

        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener {authResult ->
                Log.d(TAG, "signInWithPhoneAuthCredential: Success")
                if(authResult.additionalUserInfo!!.isNewUser){
                    Log.d(TAG, "signInWithPhoneAuthCredential: New User, Account Created")
                    updateUserInfoDb()
                }
                else{
                    Log.d(TAG, "signInWithPhoneAuthCredential: Existing user logged in")
                    startActivity(Intent(this, MainActivity::class.java))
                    finishAffinity()
                }
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Log.d(TAG, "signInWithPhoneAuthCredential: ", it)
                Utils.toast(this, "Failed to login due to ${it.message}")
            }
    }

    private fun updateUserInfoDb() {
        Log.d(TAG, "updateUserInfoDb: ")
        progressDialog.setMessage("Saving user info")
        progressDialog.show()

        val timestamp = Utils.getTimeStamp()
        val registeredUserUid = firebaseAuth.uid

        val hashMap = HashMap<String, Any?>()
        hashMap["name"] = ""
        hashMap["phoneCode"] = "$phoneCode"
        hashMap["phoneNumber"] = "$phoneNumber"
        hashMap["profileImageUrl"] = ""
        hashMap["dob"] = ""
        hashMap["userType"] = "Phone"
        hashMap["typingTo"] = ""
        hashMap["timestamp"] = timestamp
        hashMap["onlineStatus"] = true
        hashMap["email"] = ""
        hashMap["uid"] = "$registeredUserUid"

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(registeredUserUid!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "updateUserInfoDb: User info saved")
                progressDialog.dismiss()
                startActivity(Intent(this, MainActivity::class.java))
                finishAffinity()
            }
            .addOnFailureListener {
                Log.d(TAG, "updateUserInfoDb: ", it)
                progressDialog.dismiss()
                Utils.toast(this, "Failed to save user info fue to ${it.message}")
            }
    }

    private fun verifyPhoneNumberWithCode(verificationId: String?, otp: String) {
        progressDialog.setMessage("Verifying OTP")
        progressDialog.show()

        val credential = PhoneAuthProvider.getCredential(verificationId!!, otp)
        signInWithPhoneAuthCredential(credential)
    }
}