package com.example.shoppingcart.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.shoppingcart.R
import com.example.shoppingcart.Utils
import com.example.shoppingcart.databinding.ActivityLocationPickerBinding
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener

class LocationPickerActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityLocationPickerBinding

    private companion object{
        private const val TAG = "LOCATION_PICKER_TAG"

        private const val DEFAULT_ZOOM = 15
    }

    private var mMap: GoogleMap? = null

    private var mPlaceClient: PlacesClient? = null

    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    //gepgraphical location where the device is currently located. This is last known location retrieved by fused Location Provider
    private var mLastKnownLocation: Location? = null
    private var selectedLatitude:Double? = null
    private var selectedLongitude:Double? = null
    private var selectedAddress = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //hide the done LL for now, it will be show when user select location
        binding.doneLl.visibility = View.GONE
        //Obtain the supportMapFragment and get notified when the map is ready to be used
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
        //initialize places client
        Places.initialize(this, getString(R.string.my_google_map_api_key))
        //create a new places client
        mPlaceClient = Places.createClient(this)
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        //Initialize the AutoCompleteSupportFragment to search place on map
        val autoCompleteSupportMapFragment = supportFragmentManager.findFragmentById(R.id.autoComplete_fragment) as AutocompleteSupportFragment
        //list of location fields we need in search result ex: PLace, Field Id, PLace:field. name
        val placesList = arrayOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)
        //listen for place selections
        autoCompleteSupportMapFragment.setPlaceFields(listOf(*placesList))

        autoCompleteSupportMapFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener{
            override fun onError(status: Status) {

            }

            override fun onPlaceSelected(place: Place) {
                Log.d(TAG, "onPlaceSelected: ")
                //Place selected, Tha param place contain all fields that we seat as list
                val id = place.id
                val name = place.name
                val latLng = place.latLng

                selectedLatitude = latLng?.latitude
                selectedLongitude = latLng?.longitude
                selectedAddress = place.address ?: ""

                addMarker(latLng, name, selectedAddress)
            }
        })

        binding.toolbarBackBtn.setOnClickListener {
            onBackPressed()
        }

        binding.toolbarGpsBtn.setOnClickListener { 
            if(isGPSEnabled()){
                requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            else{
                Utils.toast(this, "Location is not on! Turn it on to show location")
            }
        }

        binding.doneBtn.setOnClickListener {
            val intent = Intent()
            intent.putExtra("latitude", selectedLatitude)
            intent.putExtra("longitude", selectedLongitude)
            intent.putExtra("address", selectedAddress)
            setResult(Activity.RESULT_OK, intent)

            finish()
        }
    }

    @SuppressLint("MissingPermission")
    private val requestLocationPermission: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()){isGranted ->
            Log.d(TAG, "requestLocationPermission: ")
            if(isGranted){
                //enable google maps gps button to set current location on map
                mMap!!.isMyLocationEnabled = true
                pickCurrentPlace()
            }
            else{
                Utils.toast(this, "Permission denied")
            }
        }

    /**
     * this fun will only be called if location permission is granted
     * We will only check if map object is not null the proceed to show location in map
     * **/
    private fun pickCurrentPlace() {
        Log.d(TAG, "pickCurrentPlace: ")
        if(mMap == null){
            return
        }

        detectAndShowDeviceLocationMap()
    }

    //Get current location of the device and position the maps camera
    @SuppressLint("MissingPermission")
    private fun detectAndShowDeviceLocationMap(){
        //get the best and most recent location of the device, which may be null in rare case when location is not available
        try{
            val locationResult = mFusedLocationProviderClient!!.lastLocation

            locationResult.addOnSuccessListener {location ->
                if(location != null){
                    //location got, save that location in MlastKnownLoca
                    mLastKnownLocation = location
                    selectedLongitude = location.longitude
                    selectedLatitude = location.latitude
                    //setup latlng from selected latitude and other
                    val latLng =LatLng(selectedLatitude!!, selectedLongitude!!)
                    mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM.toFloat()))
                    mMap!!.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM.toFloat()))
                    //fun call to retrieve the addres form latlong
                    addressFromLatLng(latLng)
                }
            }.addOnFailureListener {
                Log.d(TAG, "detectAndShowDeviceLocationMap: ", it)
            }
        }catch (e: Exception){
            Log.d(TAG, "detectAndShowDeviceLocationMap: ", e)
        }
    }

    private fun isGPSEnabled(): Boolean {
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var gpsEnabled = false
        var networkEnabled = false

        try{
            gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        }catch (e: Exception){
            Log.d(TAG, "isGPSEnabled: ", e)
        }

        try{
            networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }catch (e: Exception){
            Log.d(TAG, "isGPSEnabled: ", e)
        }
        return !(!gpsEnabled && !networkEnabled)
    }

    private fun addMarker(latLng: LatLng, title: String, selectedAddress: String) {
        Log.d(TAG, "addMarker: ")
        //clear map before adding a new marker. As we only need one marker if there is one already clear
        mMap!!.clear()

        try{
            //setup marker options with latlng, address title and complete address
            val markerOptions = MarkerOptions()
            markerOptions.position(latLng)
            markerOptions.title("$title")
            markerOptions.snippet("$selectedAddress")
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            //add marker to map and move camera to newly added marker
            mMap!!.addMarker(markerOptions)
            mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM.toFloat()))
            //show done Ll so user can go back
            binding.doneLl.visibility = View.VISIBLE
            binding.selectedPlaceTv.text = selectedAddress
        }catch (e: Exception){
            Log.d(TAG, "addMarker: ", e)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Log.d(TAG, "onMapReady: ")
        mMap = googleMap
        //prompt user for permission
        requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        //get lat and lon from param latlong
        mMap!!.setOnMapClickListener { latLng ->
            selectedLatitude = latLng.latitude
            selectedLongitude = latLng.longitude
            //fun call to get the address details from latlong
            addressFromLatLng(latLng)
        }
    }

    private fun addressFromLatLng(latLng: LatLng) {
        Log.d(TAG, "addressFromLatLng: ")
        //init geocoder class to get address details from latlng
        val geocoder = Geocoder(this)
        try{
            //get max one result (address) from list available address list of addresses on basis pf lat an long passed
            val addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            //get address object from the list addresslist of type List<Address>
            val address = addressList!![0]
            //get address details
            val addressLine = address.getAddressLine(0)
            val subLocality = address.subLocality
            //save address in selectedAddress var
            selectedAddress = "$addressLine"
            //add marker to map
            addMarker(latLng, "$subLocality", "$addressLine")
        }catch (e: Exception){
            Log.d(TAG, "addressFromLatLng: ", e)
        }
    }

}