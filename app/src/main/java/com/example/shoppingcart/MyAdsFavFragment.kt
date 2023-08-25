package com.example.shoppingcart

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
import androidx.core.widget.addTextChangedListener
import com.example.shoppingcart.databinding.FragmentMyAdsFavBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MyAdsFavFragment : Fragment() {

    private lateinit var binding: FragmentMyAdsFavBinding

    companion object{
        private const val TAG = "FAV_ADS_TAG"
    }

    private lateinit var mContext: Context

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var adArrayList: ArrayList<ModelAd>

    private lateinit var adapterAd: AdapterAd

    override fun onAttach(context: Context) {
        this.mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentMyAdsFavBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()

        loadAds()

        binding.searchEt.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try{
                    val query = s.toString()
                    adapterAd.filter.filter(query)
                }catch (e: Exception){
                    Log.e(TAG, "onTextChanged: ", e)
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
    }

    private fun loadAds() {
        Log.d(TAG, "loadAds: ")

        adArrayList = ArrayList()

        val favRef = FirebaseDatabase.getInstance().getReference("Users")
        favRef.child(firebaseAuth.uid!!).child("Favorites")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    adArrayList.clear()
                    for(ds in snapshot.children){
                        val adId = "${ds.child("adId").value}"

                        val adRef = FirebaseDatabase.getInstance().getReference("Ads")
                        adRef.child(adId)
                            .addListenerForSingleValueEvent(object :ValueEventListener{
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    try{
                                        val modelAd = snapshot.getValue(ModelAd::class.java)
                                        adArrayList.add(modelAd!!)
                                    }catch (e: Exception){
                                        Log.e(TAG, "onDataChange: ", e)
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {

                                }
                            })
                    }
                    //sometimes fav ads were not loading due to nested db listeners because we are getting data using 2 paths so added some delay
                    Handler().postDelayed({
                        adapterAd = AdapterAd(adArrayList, mContext)
                        binding.adsRv.adapter = adapterAd
                    }, 500)
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

}