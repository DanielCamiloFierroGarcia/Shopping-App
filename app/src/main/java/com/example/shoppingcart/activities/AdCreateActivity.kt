package com.example.shoppingcart.activities

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.ColorSpace.Model
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import com.example.shoppingcart.R
import com.example.shoppingcart.Utils
import com.example.shoppingcart.adapters.AdapterImagePicked
import com.example.shoppingcart.databinding.ActivityAdCreateBinding
import com.example.shoppingcart.models.ModelImagePicked
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class AdCreateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdCreateBinding

    private companion object{
        private const val TAG = "AD_CREATE_TAG"
    }

    private lateinit var progressDialog: ProgressDialog

    private lateinit var firebaseAuth: FirebaseAuth

    private var imageUri: Uri? = null

    private lateinit var imagePickedArrayList: ArrayList<ModelImagePicked>

    private lateinit var adapterImagePicked: AdapterImagePicked

    private var isEditMode = false
    private var adIdForEditing = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()

        val adapterCategories = ArrayAdapter(this, R.layout.row_category_act, Utils.categories)
        binding.categoryAct.setAdapter(adapterCategories)

        val adapterConditions = ArrayAdapter(this, R.layout.row_condition_act, Utils.conditions)
        binding.conditionAct.setAdapter(adapterConditions)

        isEditMode = intent.getBooleanExtra("isEditMode", false)

        if(isEditMode){
            adIdForEditing = intent.getStringExtra("adId") ?: ""
            loadAdDetails()
            binding.toolbarTitleTv.text = "Update Ad"
            binding.postAdBtn.text = "Update Ad"
        }
        else{
            binding.toolbarTitleTv.text = "Create Ad"
            binding.postAdBtn.text = "Post Ad"
        }

        imagePickedArrayList = ArrayList()
        loadImages()

        binding.toolbarBackBtn.setOnClickListener {
            onBackPressed()
        }

        binding.toolbarAdImageBtn.setOnClickListener {
            showImagesPickOptions()
        }

        binding.postAdBtn.setOnClickListener {
            validateData()
        }
        //click to launch Location... activity to pick location from map
        binding.locationAct.setOnClickListener {
            val intent = Intent(this, LocationPickerActivity::class.java)
            locationPickerActivityResultLauncher.launch(intent)
        }
    }

    private val locationPickerActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result ->
        Log.d(TAG, "locationPickerActivityResultLauncher: ")
        //this will get the result from Location... activity
        if(result.resultCode == Activity.RESULT_OK){
            val data = result.data
            if(data != null){
                latitude = data.getDoubleExtra("latitude", 0.0)
                longitude = data.getDoubleExtra("longitude", 0.0)
                address = data.getStringExtra("address") ?: ""

                binding.locationAct.setText(address)
            }
        }
        else{
            Log.d(TAG, "locationPickerActivityResultLauncher: cancelled")
            Utils.toast(this, "Cancelled")
        }
    }

    private fun loadImages() {
        Log.d(TAG, "loadImages: ")

        adapterImagePicked = AdapterImagePicked(this, imagePickedArrayList, adIdForEditing)

        binding.imagesRv.adapter = adapterImagePicked
    }

    private fun showImagesPickOptions() {
        Log.d(TAG, "showImagesPickOptions: ")

        val popupMenu = PopupMenu(this, binding.toolbarAdImageBtn)
        popupMenu.menu.add(Menu.NONE, 1, 1, "Camera")
        popupMenu.menu.add(Menu.NONE, 2, 2, "Gallery")

        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { item->
            val itemId = item.itemId
            //check which item id is clicked from popup menu
            if(itemId == 1){
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    val cameraPermissions = arrayOf(Manifest.permission.CAMERA)
                    requestCameraPermission.launch(cameraPermissions)
                }
                else{
                    val cameraPermissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    requestCameraPermission.launch(cameraPermissions)
                }
            }
            else if(itemId == 2){
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    pickImageGallery()
                }
                else{
                    val storagePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE
                    requestStoragePermission.launch(storagePermission)
                }
            }
            true
        }
    }

    private val requestStoragePermission = registerForActivityResult(ActivityResultContracts.RequestPermission()){isGranted ->
        Log.d(TAG, "requestStoragePermission: isGranted $isGranted")

        if(isGranted){
            pickImageGallery()
        }
        else{
            Utils.toast(this, "Storage permission denied")
        }
    }

    private val requestCameraPermission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){result ->
        Log.d(TAG, "requestCameraPermission: result $result")

        var areAllGranted = true
        for(isGranted in result.values){
            areAllGranted = areAllGranted && isGranted
        }

        if(areAllGranted){
            pickImageCamera()
        }
        else{
            Utils.toast(this, "Camera or storage denied")
        }
    }

    private fun pickImageGallery(){
        Log.d(TAG, "pickImageGallery: ")
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)
    }

    private fun pickImageCamera(){
        Log.d(TAG, "pickImageCamera: ")

        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE, "TEMP_IMAGE_TITLE")
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "TEMP_IMAGE_DESCRIPTION")

        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraActivityResultLauncher.launch(intent)
    }

    private val galleryActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result ->
        if(result.resultCode == Activity.RESULT_OK){
            val data = result.data
            imageUri = data!!.data
            Log.d(TAG, "galleryActivityResultLauncher: imageUri $imageUri")
            val timestamp = "${Utils.getTimeStamp()}"

            val modelImagePicked = ModelImagePicked(timestamp, imageUri, null, false)
            //add model to the array
            imagePickedArrayList.add(modelImagePicked)
            //to reload the images
            loadImages()
        }
        else{
            Utils.toast(this, "Cancelled")
        }
    }

    private val cameraActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result ->
        if(result.resultCode == Activity.RESULT_OK){
            Log.d(TAG, "cameraActivityResultLauncher: imageUri $imageUri")
            val timestamp = "${Utils.getTimeStamp()}"

            val modelImagePicked = ModelImagePicked(timestamp, imageUri, null, false)
            imagePickedArrayList.add(modelImagePicked)
            loadImages()
        }
        else{
            Utils.toast(this, "Cancelled")
        }
    }

    private var brand = ""
    private var category = ""
    private var condition = ""
    private var price = ""
    private var address = ""
    private var title = ""
    private var description = ""
    private var latitude = 0.0
    private var longitude = 0.0

    private fun validateData() {
        Log.d(TAG, "validateData: ")
        //input data
        brand = binding.brandEt.text.toString().trim()
        category = binding.categoryAct.text.toString().trim()
        condition = binding.conditionAct.text.toString().trim()
        address = binding.locationAct.text.toString().trim()
        price = binding.priceEt.text.toString().trim()
        title = binding.titleEt.text.toString().trim()
        description = binding.descriptionEt.text.toString().trim()
        //validate
        if(brand.isEmpty()){
            binding.brandEt.error = "Enter Brand"
            binding.brandEt.requestFocus()
        }
        else if(category.isEmpty()){
            binding.categoryAct.error = "Choose Category"
            binding.categoryAct.requestFocus()
        }
        else if(condition.isEmpty()){
            binding.conditionAct.error = "Choose Condition"
            binding.conditionAct.requestFocus()
        }
        else if(title.isEmpty()){
            binding.titleEt.error = "Enter title"
            binding.titleEt.requestFocus()
        }
        else if(description.isEmpty()){
            binding.descriptionEt.error = "Enter Description"
            binding.descriptionEt.requestFocus()
        }
        else{
            //all data is validated we can proceed further now
            if(isEditMode){
                updateAd()
            }
            else{
                postAd()
            }
        }
    }

    private fun updateAd() {
        Log.d(TAG, "updateAd: ")

        progressDialog.setMessage("Updating Ad")
        progressDialog.show()

        val hashMap = HashMap<String, Any>()
        hashMap["brand"] = "$brand"
        hashMap["category"] = "$category"
        hashMap["condition"] = "$condition"
        hashMap["address"] = "$address"
        hashMap["price"] = "$price"
        hashMap["title"] = "$title"
        hashMap["description"] = "$description"
        hashMap["latitude"] = latitude
        hashMap["longitude"] = longitude

        val ref = FirebaseDatabase.getInstance().getReference("Ads")
        ref.child(adIdForEditing)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "updateAd: Ad updated")
                //start uploading images picked for ad
                uploadImagesStorage(adIdForEditing)
                progressDialog.dismiss()
            }
            .addOnFailureListener {
                Log.d(TAG, "updateAd: ", it)
                progressDialog.dismiss()
                Utils.toast(this, "Failed due to ${it.message}")
            }
    }

    private fun postAd() {
        Log.d(TAG, "postAd: ")

        progressDialog.setMessage("Publishing ad")
        progressDialog.show()

        val timestamp = Utils.getTimeStamp()

        val refAds = FirebaseDatabase.getInstance().getReference("Ads")
        //key id from the reference to use as Ad id
        val keyId = refAds.push().key

        val hashMap = HashMap<String, Any>()
        hashMap["id"] = "$keyId"
        hashMap["uid"] = "${firebaseAuth.uid}"
        hashMap["brand"] = "$brand"
        hashMap["category"] = "$category"
        hashMap["condition"] = "$condition"
        hashMap["address"] = "$address"
        hashMap["price"] = "$price"
        hashMap["title"] = "$title"
        hashMap["description"] = "$description"
        hashMap["status"] = "${Utils.AD_STATUS_AVAILABLE}"
        hashMap["timestamp"] = timestamp
        hashMap["latitude"] = latitude
        hashMap["longitude"] = longitude

        refAds.child(keyId!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "postAd: Ad published")
                uploadImagesStorage(keyId)
                progressDialog.dismiss()
            }
            .addOnFailureListener {
                Log.d(TAG, "postAd: ", it)
                progressDialog.dismiss()
                Utils.toast(this, "Failed due to ${it.message}")
            }
    }

    private fun uploadImagesStorage(adId: String){
        for(i in imagePickedArrayList.indices){
            val modelImagePicked = imagePickedArrayList[i]

            //upload image only if picked from gallery/camera
            if(!modelImagePicked.fromInternet){
                val imageName = modelImagePicked.id
                val filePathAndName = "Ads/$imageName"
                val imageIndexForProgress = i +1
                val storageRef = FirebaseStorage.getInstance().getReference(filePathAndName)
                storageRef.putFile(modelImagePicked.imageUri!!)
                    .addOnProgressListener {snapshot ->
                        val progress = 100.0 * snapshot.bytesTransferred / snapshot.totalByteCount
                        Log.d(TAG, "uploadImagesStorage: progress: $progress")
                        val message = "Uploading $imageIndexForProgress of ${imagePickedArrayList.size} images.. Progress ${progress.toInt()}"
                        progressDialog.setMessage(message)
                        progressDialog.show()
                    }
                    .addOnSuccessListener {
                        Log.d(TAG, "uploadImagesStorage: onSuccess")
                        val uriTask = it.storage.downloadUrl
                        while (!uriTask.isSuccessful);
                        val uploadedImageUrl = uriTask.result

                        if(uriTask.isSuccessful){
                            val hashMap = HashMap<String, Any>()
                            hashMap["id"] = "${modelImagePicked.id}"
                            hashMap["imageUrl"] = "$uploadedImageUrl"
                            //add in FB db. Ads > AdId > Images > ImageId > ImageData
                            val ref = FirebaseDatabase.getInstance().getReference("Ads")
                            ref.child(adId).child("Images")
                                .child(imageName)
                                .updateChildren(hashMap)
                        }
                        progressDialog.dismiss()
                    }
                    .addOnFailureListener{
                        Log.d(TAG, "uploadImagesStorage: ", it)
                        progressDialog.dismiss()
                    }
            }
        }
    }

    private fun loadAdDetails() {
        Log.d(TAG, "loadAdDetails: ")

        val ref = FirebaseDatabase.getInstance().getReference("Ads")
        ref.child(adIdForEditing)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val brand = "${snapshot.child("brand").value}"
                    val category = "${snapshot.child("category").value}"
                    val condition = "${snapshot.child("condition").value}"
                    latitude = (snapshot.child("latitude").value as Double) ?: 0.0
                    longitude = (snapshot.child("longitude").value as Double)?: 0.0
                    address = "${snapshot.child("address").value}"
                    val price = "${snapshot.child("price").value}"
                    val title = "${snapshot.child("title").value}"
                    val description = "${snapshot.child("description").value}"

                    binding.brandEt.setText(brand)
                    binding.categoryAct.setText(category)
                    binding.conditionAct.setText(condition)
                    binding.locationAct.setText(address)
                    binding.priceEt.setText(price)
                    binding.titleEt.setText(title)
                    binding.descriptionEt.setText(description)

                    //load the ad images Ads > AdId > images
                    val refImages = snapshot.child("Images").ref
                    refImages.addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (ds in snapshot.children){
                                val id = "${ds.child("id").value}"
                                val imageUrl = "${ds.child("imageUrl").value}"

                                val modelImagePicked = ModelImagePicked(id, null, imageUrl, true)
                                imagePickedArrayList.add(modelImagePicked)
                            }
                            loadImages()
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }
                    })
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}