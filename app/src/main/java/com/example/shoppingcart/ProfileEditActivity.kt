package com.example.shoppingcart

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
import com.bumptech.glide.Glide
import com.example.shoppingcart.databinding.ActivityProfileEditBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileEditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileEditBinding

    private companion object{
        private const val TAG = "PROFILE_EDIT_TAG"
    }

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    private var myUserType = ""

    private var imageUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()
        loadMyInfo()

        binding.toolbarBackBtn.setOnClickListener {
            onBackPressed()
        }

        binding.profileImagePickFab.setOnClickListener {
            imagePickDialog()
        }
    }

    private fun loadMyInfo() {
        Log.d(TAG, "loadMyInfo: ")
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child("${firebaseAuth.uid}")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val dob = "${snapshot.child("dob").value}"
                    val email = "${snapshot.child("email").value}"
                    val name = "${snapshot.child("name").value}"
                    val phoneCode = "${snapshot.child("phoneCode").value}"
                    val phoneNumber = "${snapshot.child("phoneNumber").value}"
                    val profileImageUrl = "${snapshot.child("profileImageUrl").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    myUserType = "${snapshot.child("userType").value}"
                    val phone = phoneCode+phoneNumber

                    if(myUserType.equals("Email", true) || myUserType.equals("Google", true)){
                        binding.emailTil.isEnabled = false
                        binding.emailEt.isEnabled = false
                    }
                    else{
                        binding.phoneNumberTil.isEnabled = false
                        binding.phoneNumberEt.isEnabled = false
                        binding.countryCodePicker.isEnabled = false
                    }
                    binding.emailEt.setText(email)
                    binding.dobEt.setText(dob)
                    binding.nameEt.setText(name)
                    binding.phoneNumberEt.setText(phoneNumber)

                    try{
                        val phoneCodeInt = phoneCode.replace("+","").toInt()
                        binding.countryCodePicker.setCountryForPhoneCode(phoneCodeInt)
                    }catch (e: Exception){
                        Log.d(TAG, "onDataChange: ", e)
                    }

                    try {
                        Glide.with(this@ProfileEditActivity)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.ic_person_white)
                            .into(binding.profileIv)
                    }catch (e: Exception){
                        Log.d(TAG, "onDataChange: ", e)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun imagePickDialog(){
        val popupMenu = PopupMenu(this, binding.profileImagePickFab)

        popupMenu.menu.add(Menu.NONE, 1, 1, "Camera")
        popupMenu.menu.add(Menu.NONE, 1, 1, "Gallery")

        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { item ->
            val itemId = item.itemId

            if(itemId == 1){
                Log.d(TAG, "imagePickDialog: Camera clicked, check if camera permission granted")
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    requestCameraPermissions.launch(arrayOf(android.Manifest.permission.CAMERA))
                }
                else{
                    requestCameraPermissions.launch(arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE))
                }
            }
            else if(itemId == 2){
                Log.d(TAG, "imagePickDialog: Gallery clicked, check if storage permission granted")
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    pickImageGallery()
                }
                else{
                    requestStoragePermission.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }
            return@setOnMenuItemClickListener true
        }
    }

    private val requestCameraPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()){result ->
        Log.d(TAG, "requestCameraPermissions: $result")
        var areAllGranted = true
        for(isGranted  in result.values){
            areAllGranted = areAllGranted && isGranted
        }
        if(areAllGranted){
            Log.d(TAG, "requestCameraPermissions: All granted")
            pickImageCamera()
        }
        else{
            Log.d(TAG, "requestCameraPermissions: Not al granted granted")
            Utils.toast(this, "Camera or storage permission denied")
        }
    }

    private val requestStoragePermission = registerForActivityResult(ActivityResultContracts.RequestPermission()){isGranted ->
        Log.d(TAG, "requestStoragePermission: is granted $isGranted")
        if(isGranted){
            pickImageGallery()
        }
        else{
            Utils.toast(this, "Storage permission denied")
        }
    }

    private fun pickImageCamera() {
        Log.d(TAG, "pickImageCamera: ")
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_image_title")
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp_image_description")

        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
    }

    private fun pickImageGallery() {

    }

    //WE LEFT AT 1 HOUR
}