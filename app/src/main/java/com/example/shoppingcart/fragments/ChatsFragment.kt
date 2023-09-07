package com.example.shoppingcart.fragments

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.shoppingcart.R
import com.example.shoppingcart.adapters.AdapterChats
import com.example.shoppingcart.databinding.FragmentChatsBinding
import com.example.shoppingcart.models.ModelChats
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class ChatsFragment : Fragment() {

    private lateinit var binding: FragmentChatsBinding

    private companion object{
        private const val TAG = "CHATS_TAG"
    }

    private lateinit var firebaseAuth: FirebaseAuth

    private var myUid = ""

    private lateinit var mContext: Context

    private lateinit var chatsArrayList: ArrayList<ModelChats>

    private lateinit var adapterChats: AdapterChats

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentChatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()

        myUid = "${firebaseAuth.uid}"

        loadChats()
        //add text change listener to searchEt to search chats using filter applied in AdapterChats class
        binding.searchEt.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try{
                    val query = s.toString()
                    Log.d(TAG, "onTextChanged: query: $query")

                    adapterChats.filter.filter(query)
                }catch (e: Exception){
                    Log.e(TAG, "onTextChanged: ", e)
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
    }

    private fun loadChats() {
        chatsArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Chats")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                chatsArrayList.clear()
                
                for(ds in snapshot.children){
                    val chatKey = "${ds.key}"
                    Log.d(TAG, "onDataChange: chatKey $chatKey")

                    //if chat key uid1_uid2 contains uid of currently logged user will be considered as chat of currently logged user
                    if(chatKey.contains(myUid)){
                        Log.d(TAG, "onDataChange: contains, add to list")
                        //Create instance of ModelChats in chatsArrayList
                        val modelChats = ModelChats()
                        modelChats.chatKey = chatKey
                        //add instance in array
                        chatsArrayList.add(modelChats)
                    }
                    else{
                        Log.d(TAG, "onDataChange: Not contains, skip")
                    }
                }
                //init/setup the adapter class and set recycler
                adapterChats = AdapterChats(mContext, chatsArrayList)
                binding.chatsRv.adapter = adapterChats
                //after loading data in list we will sort the list usnig timestamp of each last message of chat, to show the newest chat first
                sort()
            }

            override fun onCancelled(error: DatabaseError) {
                
            }
        })
    }

    private fun sort(){
        //Delay 1 sec before sorting list
        Handler().postDelayed({
            chatsArrayList.sortWith{model1: ModelChats, model2: ModelChats ->
                model2.timestamp.compareTo(model1.timestamp)
            }

            adapterChats.notifyDataSetChanged()
        }, 1000)
    }
}