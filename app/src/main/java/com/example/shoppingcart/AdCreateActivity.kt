package com.example.shoppingcart

import android.app.ProgressDialog
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import com.example.shoppingcart.databinding.ActivityAdCreateBinding
import com.google.firebase.auth.FirebaseAuth

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
        binding.conditionAct.setAdapter(adapterConditions)//LEFT AT 40:00

        binding.toolbarBackBtn.setOnClickListener {
            onBackPressed()
        }
    }
}