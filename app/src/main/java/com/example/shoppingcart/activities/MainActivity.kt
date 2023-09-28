package com.example.shoppingcart.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.shoppingcart.fragments.MyAdsFragment
import com.example.shoppingcart.R
import com.example.shoppingcart.Utils
import com.example.shoppingcart.databinding.ActivityMainBinding
import com.example.shoppingcart.fragments.AccountFragment
import com.example.shoppingcart.fragments.ChatsFragment
import com.example.shoppingcart.fragments.HomeFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    
    companion object{
        private const val TAG = "MAIN_TAG"
    }

    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        if(firebaseAuth.currentUser == null){
            //user is not logged in. move to Login
            startActivity(Intent(this, LoginOptionsActivity::class.java))
        }
        else{
            //user logged in, ask notification permission and update FCN token
            updateFcmToken()
            askNotificationPermission()
        }

        showHomeFragment()

        binding.bottomNv.setOnItemSelectedListener {item ->
            when(item.itemId){
                R.id.menu_home ->{
                    //home item click, show home fragment
                    showHomeFragment()
                    true
                }
                R.id.menu_chats ->{
                    //chats item click, show chats fragment
                    if(firebaseAuth.currentUser == null){
                        startActivity(Intent(this, LoginOptionsActivity::class.java))
                        Utils.toast(this, "Login Required")
                        false
                    }
                    else{
                        showChatsFragment()
                        true
                    }
                }
                R.id.menu_my_ads ->{
                    //my ads item click, show my ads fragment
                    if(firebaseAuth.currentUser == null){
                        startActivity(Intent(this, LoginOptionsActivity::class.java))//the startLoginOptions method is this
                        Utils.toast(this, "Login Required")
                        false
                    }
                    else{
                        showMyAdsFragment()
                        true
                    }
                }
                R.id.menu_account ->{
                    //account item click, show account fragment
                    if(firebaseAuth.currentUser == null){
                        startActivity(Intent(this, LoginOptionsActivity::class.java))
                        Utils.toast(this, "Login Required")
                        false
                    }
                    else{
                        showAccountFragment()
                        true
                    }
                }
                else ->{
                    false
                }
            }
        }

        binding.sellFab.setOnClickListener {
            val intent = Intent(this, AdCreateActivity::class.java)
            intent.putExtra("isEditMode", false)
            startActivity(intent)
        }
    }
    private fun showHomeFragment(){
        binding.toolbarTitleTv.text = "Home"
        //show fragment
        val fragment = HomeFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentsFl.id, fragment, "HomeFragment")
        fragmentTransaction.commit()
    }

    private fun showChatsFragment(){
        binding.toolbarTitleTv.text = "Chats"
        //show fragment
        val fragment = ChatsFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentsFl.id, fragment, "ChatsFragment")
        fragmentTransaction.commit()
    }

    private fun showMyAdsFragment(){
        binding.toolbarTitleTv.text = "My Ads"
        //show fragment
        val fragment = MyAdsFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentsFl.id, fragment, "MyAdsFragment")
        fragmentTransaction.commit()
    }

    private fun showAccountFragment(){
        binding.toolbarTitleTv.text = "Account"
        //show fragment
        val fragment = AccountFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentsFl.id, fragment, "AccountFragment")
        fragmentTransaction.commit()
    }

    private fun updateFcmToken(){
        val myUid = "${firebaseAuth.uid}"
        Log.d(TAG, "updateFcmToken: ")
        //get FCN token
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener {token ->
                Log.d(TAG, "updateFcmToken: token $token")
                val hashMap = HashMap<String, Any>()
                hashMap["fcmToken"] = token
                
                val ref = FirebaseDatabase.getInstance().getReference("Users")
                ref.child(myUid)
                    .updateChildren(hashMap)
                    .addOnSuccessListener {
                        Log.d(TAG, "updateFcmToken: Fcn token updated to db")
                    }
                    .addOnFailureListener {
                        Log.e(TAG, "updateFcmToken: ", it)
                    }
            }
            .addOnFailureListener {
                Log.e(TAG, "updateFcmToken: ", it)
            }
    }

    private fun askNotificationPermission(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_DENIED){ //Permission not granted yet, Request
                requestNotificationsPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private val requestNotificationsPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()){isGranted ->
        Log.d(TAG, "requestNotificationsPermission: isGranted $isGranted")

    }
}