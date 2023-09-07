package com.example.shoppingcart.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.shoppingcart.FilterChats
import com.example.shoppingcart.R
import com.example.shoppingcart.Utils
import com.example.shoppingcart.activities.ChatActivity
import com.example.shoppingcart.databinding.RowChatsBinding
import com.example.shoppingcart.models.ModelChats
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdapterChats : RecyclerView.Adapter<AdapterChats.HolderChats>, Filterable{
    private var context: Context

    var chatArrayList: ArrayList<ModelChats>

    private var filterList: ArrayList<ModelChats>

    private var filter: FilterChats? = null

    private lateinit var binding: RowChatsBinding

    private companion object{
        private const val TAG = "ADAPTER_CHATS_TAG"
    }

    private var firebaseAuth: FirebaseAuth

    private var myUid = ""

    constructor(context: Context, chatArrayList: ArrayList<ModelChats>) {
        this.context = context
        this.chatArrayList = chatArrayList
        firebaseAuth = FirebaseAuth.getInstance()
        myUid = "${firebaseAuth.uid}"
        this.filterList = chatArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderChats {
        binding = RowChatsBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderChats(binding.root)
    }

    override fun getItemCount(): Int {
        return chatArrayList.size
    }

    override fun onBindViewHolder(holder: HolderChats, position: Int) {
        val modelChats = chatArrayList[position]

        loadLastMessage(modelChats, holder)

        //handle chat item click, open ChatActivity
        holder.itemView.setOnClickListener {
            val receiptUid = modelChats.receiptUid

            if(receiptUid != null){
                val intent = Intent(context, ChatActivity::class.java)
                intent.putExtra("receiptUid", receiptUid)
                context.startActivity(intent)
            }
        }
    }

    private fun loadLastMessage(modelChats: ModelChats, holder: AdapterChats.HolderChats) {
        Log.d(TAG, "loadLastMessage: ")

        val chatKey = modelChats.chatKey

        val ref = FirebaseDatabase.getInstance().getReference("Chats")
        ref.child(chatKey).limitToLast(1)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(ds in snapshot.children){
                        val fromUid = "${ds.child("fromUid").value}"
                        val message = "${ds.child("message").value}"
                        val messageId = "${ds.child("messageId").value}"
                        val messageType = "${ds.child("messageType").value}"
                        val timestamp = ds.child("timestamp").value as Long ?: 0
                        val toUid = "${ds.child("toUid").value}"

                        val formattedDate = Utils.formatTimestampDateTime(timestamp = timestamp)

                        modelChats.message = message
                        modelChats.messageId = messageId
                        modelChats.messageType = messageType
                        modelChats.fromUid = fromUid
                        modelChats.timestamp = timestamp
                        modelChats.toUid = toUid

                        holder.dateTimeTv.text = "$formattedDate"

                        if(messageType == Utils.MESSAGE_TYPE_TEXT){
                            holder.lastMessageTv.text = message
                        }
                        else{
                            holder.lastMessageTv.text = "Sends Attachment"
                        }
                    }
                    loadReceiptUserInfo(modelChats, holder)
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun loadReceiptUserInfo(modelChats: ModelChats, holder: AdapterChats.HolderChats) {
        Log.d(TAG, "loadReceiptUserInfo: ")

        val fromUid = modelChats.fromUid
        val toUid = modelChats.toUid

        var receiptUid = ""
        if(fromUid == myUid){
            receiptUid = toUid
        }
        else{
            receiptUid = fromUid
        }

        modelChats.receiptUid = receiptUid

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(receiptUid)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = "${snapshot.child("name").value}"
                    val profileImageUrl = "${snapshot.child("profileImageUrl").value}"

                    modelChats.name = name
                    modelChats.profileImageUrl = profileImageUrl

                    holder.nameTv.text = name
                    try{
                        Glide.with(context)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.ic_person_white)
                            .into(holder.profileIv)
                    }catch (e: Exception){
                        Log.e(TAG, "onDataChange: ", e)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    override fun getFilter(): Filter {
        //init the filter obj only if its null
        if(filter == null){
            filter = FilterChats(this, filterList)
        }
        return filter!!
    }

    inner class HolderChats(itemView: View) : RecyclerView.ViewHolder(itemView){
        var profileIv = binding.profileIv
        var nameTv = binding.nameTv
        var lastMessageTv = binding.lastMessageTv
        var dateTimeTv = binding.dateTimeTv
    }
}