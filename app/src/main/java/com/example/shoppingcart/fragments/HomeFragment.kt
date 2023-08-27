package com.example.shoppingcart.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import com.example.shoppingcart.activities.LocationPickerActivity
import com.example.shoppingcart.RvListenerCategory
import com.example.shoppingcart.Utils
import com.example.shoppingcart.adapters.AdapterAd
import com.example.shoppingcart.adapters.AdapterCategory
import com.example.shoppingcart.databinding.FragmentHomeBinding
import com.example.shoppingcart.models.ModelAd
import com.example.shoppingcart.models.ModelCategory
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    private lateinit var mContext: Context
    //to hol ads to show in RecyclerView
    private lateinit var adArrayList: ArrayList<ModelAd>
    //AdapterAd class instance to set to RV to show ads list
    private lateinit var adapterAd: AdapterAd
    //SP to store selected location from map to load ads nearby
    private lateinit var locationSp: SharedPreferences

    private var currentLatitude = 0.0
    private var currentLongitude = 0.0
    private var currentAddress = ""

    private companion object{
        private const val TAG = "HOME_TAG"
        //max distnace in KM to show ads under that distance
        private const val MAX_DISTANCE_TO_LOAD_ADS_KM = 10
    }

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(LayoutInflater.from(mContext), container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locationSp = mContext.getSharedPreferences("LOCATION_SP", Context.MODE_PRIVATE)
        //this will be obtain mas abajo
        currentLatitude = locationSp.getFloat("CURRENT_LATITUDE", 0.0f).toDouble()
        currentLongitude = locationSp.getFloat("CURRENT_LONGITUDE", 0.0f).toDouble()
        currentAddress = locationSp.getString("CURRENT_ADDRESS", "")!!

        if(currentLatitude != 0.0 && currentLongitude != 0.0){
            binding.locationTv.text = currentAddress
        }

        loadCategories()
        //fun to load all ads
        loadAds("All")
        //add text change listener to seacrhEt to search ads based on query typed in searchEt
        binding.searchEt.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d(TAG, "onTextChanged: Query: $s")

                try{
                    val query = s.toString()
                    adapterAd.filter.filter(query)
                }catch (e:Exception){
                    Log.e(TAG, "onTextChanged: ", e)
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        binding.locationCv.setOnClickListener {
            val intent = Intent(mContext, LocationPickerActivity::class.java)
            locationPickerActivityResultLauncher.launch(intent)
        }
    }

    private val locationPickerActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result ->
        if(result.resultCode == Activity.RESULT_OK){
            Log.d(TAG, "locationPickerActivityResultLauncher: ")

            val data = result.data
            //check if location is picked from map
            if(data != null){
                Log.d(TAG, "locationPickerActivityResultLauncher: Location Picked")
                //get location from intent
                currentLongitude = data.getDoubleExtra("longitude", 0.0)
                currentLatitude = data.getDoubleExtra("latitude", 0.0)
                currentAddress = data.getStringExtra("address").toString()
                //save location info to SP so when app is launched next time no need to pick again
                locationSp.edit()
                    .putFloat("CURRENT_LATITUDE", currentLatitude.toFloat())
                    .putFloat("CURRENT_LONGITUDE", currentLongitude.toFloat())
                    .putString("CURRENT_ADDRESS", currentAddress)
                    .apply()

                //set picked address
                binding.locationTv.text = currentAddress

                //after picking addres reload all ads again based on newly picked location
                loadAds("All")
            }
        }
        else{
            Utils.toast(mContext, "Cancelled")
        }
    }

    private fun loadAds(category: String) {
        Log.d(TAG, "loadAds: Category: $category")

        adArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Ads")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                adArrayList.clear()

                for(ds in snapshot.children){
                    try{
                        val modelAd = ds.getValue(ModelAd::class.java)
                        val distance = calculateDistanceKm(modelAd?.latitude ?: 0.0, modelAd?.longitude ?: 0.0)
                        val lat = modelAd?.latitude ?: 0.0
                        val long = modelAd?.longitude ?: 0.0
                        //Log.d(TAG, "prueba: LOng y lat ${modelAd?.latitude} and ${modelAd?.longitude}")
                        //filter, add the ad to list only if category is matched and is under specific distance
                        if(category == "All"){
                            //category all is selected, now check distance
                            if(distance <= MAX_DISTANCE_TO_LOAD_ADS_KM || (lat ==0.0 && long == 0.0)){
                                adArrayList.add(modelAd!!)
                            }
                        }else{
                            //some category is selected so lets match if selected category matches with ads category
                            if(modelAd!!.category.equals(category)){
                                if(distance <= MAX_DISTANCE_TO_LOAD_ADS_KM){
                                    adArrayList.add(modelAd)
                                }
                            }
                        }
                    }catch (e: Exception){
                        Log.e(TAG, "onDataChange: ", e)
                    }
                }
                //setup adapter and set to RV
                adapterAd = AdapterAd(adArrayList, mContext)
                binding.adsRv.adapter = adapterAd
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun calculateDistanceKm(adLatitude: Double, adLongitude: Double): Double {
        Log.d(TAG, "calculateDistanceKm: ")
        //source location ex: users current location
        val startPoint = Location(LocationManager.NETWORK_PROVIDER)
        startPoint.latitude = currentLatitude
        startPoint.longitude = currentLongitude
        //destination location ex: ads location
        val endPoint = Location(LocationManager.NETWORK_PROVIDER)
        endPoint.latitude = adLatitude
        endPoint.longitude = adLongitude
        //calculate distance in meters
        val distanceInMeters = startPoint.distanceTo(endPoint).toDouble()

        return distanceInMeters/1000
    }

    private fun loadCategories(){
        val categoryArrayList = ArrayList<ModelCategory>()

        //get categories from utils class and add in categoryArrayList
        for(i in 0 until Utils.categories.size){
            val modelCategory = ModelCategory(Utils.categories[i], Utils.categoryIcons[i])
            categoryArrayList.add(modelCategory)
        }
        //iinit/setup ApaterCategory
        val adapterCategory = AdapterCategory(mContext, categoryArrayList, object :
            RvListenerCategory {
            override fun onCategoryClick(modelCategory: ModelCategory) {
                //get selected cat
                val selectedCategory = modelCategory.category
                //load ads base on selected category
                loadAds(selectedCategory)
            }
        })

        binding.categoriesRv.adapter = adapterCategory
    }

}