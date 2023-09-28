package com.example.shoppingcart.activities

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.example.shoppingcart.R
import com.example.shoppingcart.Utils
import com.example.shoppingcart.adapters.AdapterChat
import com.example.shoppingcart.databinding.ActivityChatBinding
import com.example.shoppingcart.models.ModelChat
import com.google.android.gms.common.api.Response
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.util.Listener
import com.google.firebase.storage.FirebaseStorage
import org.json.JSONObject
import java.sql.Timestamp

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding

    private companion object{
        private const val TAG = "CHAT_TAG"
    }

    private lateinit var progressDialog: ProgressDialog

    private lateinit var firebaseAuth: FirebaseAuth

    private var receiptUid = ""
    private var receiptFcmToken = ""

    private var myUid = ""
    private var myName = ""
    //WIll generate using UIDs of current user and receipt
    private var chatPath = ""
    //Uri of image picked from camera/gallery
    private var imageUri:Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please Wait")
        progressDialog.setCanceledOnTouchOutside(false)

        receiptUid = intent.getStringExtra("receiptUid").toString()

        myUid = firebaseAuth.uid!!

        Log.d(TAG, "onCreate: myUid $myUid")
        Log.d(TAG, "onCreate: receipt $receiptUid")

        chatPath = Utils.chatPath(receiptUid, myUid)

        loadMyInfo()
        loadReceiptDetails()
        loadMessages()

        binding.toolbarBackBtn.setOnClickListener {
            finish()
        }

        binding.attachFab.setOnClickListener {
            imagePickDialog()
        }

        binding.sendFab.setOnClickListener {
            validateData()
        }
    }

    private fun loadMyInfo(){
        Log.d(TAG, "loadMyInfo: ")

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child("${firebaseAuth.uid}")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    myName = "${snapshot.child("name").value}"
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun loadMessages() {
        Log.d(TAG, "loadMessages: ")

        val messageArrayList = ArrayList<ModelChat>()

        val ref = FirebaseDatabase.getInstance().getReference("Chats")
        ref.child(chatPath)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageArrayList.clear()

                    for(ds: DataSnapshot in snapshot.children){
                        try{
                            val modelChat = ds.getValue(ModelChat::class.java)
                            messageArrayList.add(modelChat!!)
                        }catch (e: Exception){
                            Log.e(TAG, "onDataChange: ", e)
                        }
                    }
                    val adapterChat = AdapterChat(this@ChatActivity, messageArrayList)
                    binding.chatRv.adapter = adapterChat
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun loadReceiptDetails(){
        Log.d(TAG, "loadReceiptDetails: ")

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(receiptUid)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    try{
                        val name = "${snapshot.child("name").value}"
                        val profileImageUrl = "${snapshot.child("profileImageUrl").value}"
                        receiptFcmToken = "${snapshot.child("fcmToken").value}"

                        binding.toolbarTitleTv.text = name

                        try {
                            Glide.with(this@ChatActivity)
                                .load(profileImageUrl)
                                .placeholder(R.drawable.ic_person_white)
                                .into(binding.toolbarProfileIv)
                        }catch (e: Exception){
                            Log.e(TAG, "onDataChange: ", e)
                        }
                    }catch (e: Exception){
                        Log.e(TAG, "onDataChange: ", e)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun imagePickDialog(){
        Log.d(TAG, "imagePickDialog: ")
        //The second parameter is the UI View (attachFab) to above/below we need to show popup
        val popupMenu = PopupMenu(this, binding.attachFab)

        popupMenu.menu.add(Menu.NONE, 1, 1, "Camera")
        popupMenu.menu.add(Menu.NONE, 2, 2, "Gallery")
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { menuItem ->
            val itemId = menuItem.itemId

            if(itemId == 1){
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    requestCameraPermissions.launch(arrayOf(Manifest.permission.CAMERA))
                }
                else{
                    requestCameraPermissions.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                }
            }
            else if(itemId == 2){
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    pickImageGallery()
                }
                else{
                    requestStoragePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }

            true
        }
    }

    private val requestCameraPermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){result ->
        var areAllGranted = true
        for(isGranted in result.values){
            areAllGranted = areAllGranted && isGranted
        }

        if(areAllGranted){
            pickImageCamera()
        }
        else{
            Utils.toast(this, "All or some permissions denied")
        }
    }

    private val requestStoragePermission = registerForActivityResult(ActivityResultContracts.RequestPermission()){isGranted ->
        if(isGranted){
            pickImageGallery()
        }
        else{
            Utils.toast(this, "Permission denied")
        }
    }

    private fun pickImageCamera(){
        Log.d(TAG, "pickImageCamera: ")

        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE, "THE_IMAGE_TITLE")
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "THE_IMAGE_DESCRIPTION")

        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraActivityResultLauncher.launch(intent)
    }

    private val cameraActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result ->
        if(result.resultCode == Activity.RESULT_OK){
            Log.d(TAG, "cameraActivityResultLauncher: imageUri $imageUri")
            //image picked lets upload
            uploadToFirebaseStorage()
        }
        else{
            Utils.toast(this, "Cancelled")
        }
    }

    private fun pickImageGallery(){
        Log.d(TAG, "pickImageGallery: ")

        val intent = Intent(Intent.ACTION_VIEW)
        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)
    }

    private val galleryActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result ->
        if(result.resultCode == RESULT_OK){
            val data = result.data
            imageUri = data!!.data
            //image picked lets upload
            uploadToFirebaseStorage()
        }
        else{
            Utils.toast(this, "Cancelled")
        }
    }

    private fun uploadToFirebaseStorage(){
        Log.d(TAG, "uploadToFirebaseStorage: ")
    
        progressDialog.setMessage("Uploading Image")
        progressDialog.show()
        
        val timestamp = Utils.getTimeStamp()
        val filePathAndName = "ChatImages/$timestamp"
        
        val storageRef = FirebaseStorage.getInstance().getReference(filePathAndName)
        storageRef.putFile(imageUri!!)
            .addOnProgressListener {snapshot ->
                val progress = 100.0 * snapshot.bytesTransferred / snapshot.totalByteCount
                progressDialog.setMessage("Uploading Image: Progress ${progress.toUInt()} %")
            }
            .addOnSuccessListener {taskSnapshot ->
                val uriTask = taskSnapshot.storage.downloadUrl

                while (!uriTask.isSuccessful);

                val uploadedImageUrl = uriTask.result.toString()
                if(uriTask.isSuccessful){
                    sendMessage(Utils.MESSAGE_TYPE_IMAGE, uploadedImageUrl, timestamp)
                }
            }
            .addOnFailureListener{
                progressDialog.dismiss()
                Log.e(TAG, "uploadToFirebaseStorage: ", it)
                Utils.toast(this, "Failed to upload image")
            }
    }

    private fun validateData(){
        Log.d(TAG, "validateData: ")
        //input data
        val message = binding.messageEt.text.toString().trim()
        val timestamp = Utils.getTimeStamp()

        if(message.isEmpty()){
            Utils.toast(this, "Enter message to send.")
        }
        else{
            sendMessage(Utils.MESSAGE_TYPE_TEXT, message, timestamp)
        }
    }

    private fun sendMessage(messageType: String, message: String, timestamp: Long){
        Log.d(TAG, "sendMessage: messageType: $messageType")

        progressDialog.setMessage("Sending message")
        progressDialog.show()
        val refChat = FirebaseDatabase.getInstance().getReference("Chats")

        val keyId = "${refChat.push().key}"
        val hashMap = HashMap<String, Any>()
        hashMap["messageId"] = "$keyId"
        hashMap["messageType"] = "$messageType"
        hashMap["message"] = "$message"
        hashMap["fromUid"] = "$myUid"
        hashMap["toUid"] = "$receiptUid"
        hashMap["timestamp"] = timestamp

        refChat.child(chatPath)
            .child(keyId)
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "sendMessage: Message sent")
                progressDialog.dismiss()
                //after sending message clear message from messageEt
                binding.messageEt.setText("")

                //if message type is text, pass the actual message to show as notification description/body. If message type is Image then pass "Sent an attachment"
                if(messageType == Utils.MESSAGE_TYPE_TEXT){
                    prepareNotification(message)
                }
                else{
                    prepareNotification("Sent and attachment")
                }
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Log.e(TAG, "sendMessage: ", it)
                Utils.toast(this, "Failed to send message")
            }
    }

    private fun prepareNotification(message: String){
        Log.d(TAG, "prepareNotification: ")
        //prepare json what to send and where to send
        val notificationJo = JSONObject()
        val notificationDataJo = JSONObject()
        val notificationNotificationJo = JSONObject()

        try{
            //extra/custom data
            notificationDataJo.put("notificationType", "${Utils.NOTIFICATION_TYPE_NEW_MESSAGE}")
            notificationDataJo.put("senderUid", "${firebaseAuth.uid}")
            //title, description, sound
            notificationNotificationJo.put("title", "$myName")//key "title" is reserved name in FCM API
            notificationNotificationJo.put("body", "$message")//key "body" is reserved name in FCM API
            notificationNotificationJo.put("sound", "default")//key "sound" is reserved name in FCM API
            //combine all data in single JSON object
            notificationJo.put("to", "$receiptFcmToken")//"to" is reserved name in FCM API
            notificationJo.put("notification", notificationNotificationJo)//key "notification" is reserved name in FCM API
            notificationJo.put("data", notificationDataJo)//key "data" is reserved name in FCM API
        }catch (e: Exception){
            Log.e(TAG, "prepareNotification: ", e)
        }

        sendFcmNotification(notificationJo)
    }

    private fun sendFcmNotification(notificationJo: JSONObject){
        //prepare JSON Object request to enqueue
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
            Method.POST,
            "https://fcm.googleapis.com/fcm/send",
            notificationJo,
            com.android.volley.Response.Listener {
                //Notification sent
                Log.d(TAG, "sendFcmNotification: Notification sent $it")
            },
            com.android.volley.Response.ErrorListener {
                //Notification failed to send
                Log.e(TAG, "sendFcmNotification: Notification failed sent $it")
            }
        ){
            override fun getHeaders(): MutableMap<String, String> {
                //put required headers
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                headers["Authorization"] = "key=${Utils.FCN_SERVER_KEY}"

                return headers
            }
        }
        //enqueue the JSON Object Request
        Volley.newRequestQueue(this).add(jsonObjectRequest)
    }
}