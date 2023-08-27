package com.example.shoppingcart.activities

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import com.example.shoppingcart.R
import com.example.shoppingcart.Utils
import com.example.shoppingcart.databinding.ActivityLoginOptionsBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase

class LoginOptionsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginOptionsBinding

    private companion object{
        private const val TAG = "LOGIN_OPTIONS_TAG"
    }

    private lateinit var progressDialog: ProgressDialog

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginOptionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please Wait")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.closeBtn.setOnClickListener {
            onBackPressed()
        }

        binding.loginEmailBtn.setOnClickListener {
            startActivity(Intent(this, LoginEmailActivity::class.java))
        }

        binding.loginGoogleBtn.setOnClickListener {
            beginGoogleLogin()
        }

        binding.loginPhoneBtb.setOnClickListener {
            startActivity(Intent(this, LoginPhoneActivity::class.java))
        }
    }
    private fun beginGoogleLogin(){
        Log.d(TAG, "beginGoogleLogin: ")
        //intent to launch ggogle sign in options dialog
        val googleSignInIntent = mGoogleSignInClient.signInIntent
        googleSignInARL.launch(googleSignInIntent)
    }

    private val googleSignInARL = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){result ->
        if(result.resultCode == RESULT_OK){
            //user has chosen some option to login from intent, get data/intent from result param get logged in user info, used to login to fb auth
            val data = result.data
            //task to get the googleSignInAccount from intent
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try{
                //get the GoogleSIgnInAccount from intent
                val account = task.getResult(ApiException::class.java)
                Log.d(TAG, "googleSignInARL: Account ID ${account.id}")
                firebaseAuthWithGoogleAccount(account.idToken)
            }catch (e: Exception){
                Log.d(TAG, "googleSignInARL: ", e)
                Utils.toast(this, "${e.message}")
            }
        }
        else{
            //user cancelled
            Utils.toast(this, "Cancelled...")
        }
    }

    private fun firebaseAuthWithGoogleAccount(idToken: String?) {
        Log.d(TAG, "firebaseAuthWithGoogleAccount: idToken: $idToken")
        //setup google credential to sign in with firebase auth
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        //sign in to firebase auth using google credentials
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener {authResult ->
                if(authResult.additionalUserInfo!!.isNewUser){
                    Log.d(TAG, "firebaseAuthWithGoogleAccount: New User, Account created")
                    updateUserInfoDb()
                }
                else{
                    Log.d(TAG, "firebaseAuthWithGoogleAccount: Existing User, loggend in")
                    startActivity(Intent(this, MainActivity::class.java))
                    finishAffinity()
                }
            }
            .addOnFailureListener {
                Log.d(TAG, "firebaseAuthWithGoogleAccount: ", it)
                Utils.toast(this, "${it.message}")
            }
    }

    private fun updateUserInfoDb(){
        Log.d(TAG, "updateUserInfoDb: ")
        progressDialog.setMessage("Saving user info")
        progressDialog.show()

        val timestamp = Utils.getTimeStamp()
        val registeredUserEmail = firebaseAuth.currentUser?.email
        val registeredUserUid = firebaseAuth.uid
        val name = firebaseAuth.currentUser?.displayName

        val hashMap = HashMap<String, Any?>()
        hashMap["name"] = "$name"
        hashMap["phoneCode"] = ""
        hashMap["phoneNumber"] = ""
        hashMap["profileImageUrl"] = ""
        hashMap["dob"] = ""
        hashMap["userType"] = "Google"
        hashMap["typingTo"] = ""
        hashMap["timestamp"] = timestamp
        hashMap["onlineStatus"] = true
        hashMap["email"] = "$registeredUserEmail"
        hashMap["uid"] = "$registeredUserUid"

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(registeredUserUid!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Log.d(TAG, "updateUserInfoDb: User info saved")
                Utils.toast(this, "User info saved")
                startActivity(Intent(this, MainActivity::class.java))
                finishAffinity()
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Log.d(TAG, "updateUserInfoDb: ", it)
                Utils.toast(this, "Failed to save user info due to ${it.message}")
            }
    }
}