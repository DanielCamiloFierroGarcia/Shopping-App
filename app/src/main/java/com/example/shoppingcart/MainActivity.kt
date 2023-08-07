package com.example.shoppingcart

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.shoppingcart.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        if(firebaseAuth.currentUser == null){
            startActivity(Intent(this, LoginOptionsActivity::class.java))
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
                    showChatsFragment()
                    true
                }
                R.id.menu_my_ads ->{
                    //my ads item click, show my ads fragment
                    showMyAdsFragment()
                    true
                }
                R.id.menu_account ->{
                    //account item click, show account fragment
                    showAccountFragment()
                    true
                }
                else ->{
                    false
                }
            }
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

}