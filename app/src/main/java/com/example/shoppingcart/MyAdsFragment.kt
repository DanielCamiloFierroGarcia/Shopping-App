package com.example.shoppingcart

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.example.shoppingcart.databinding.FragmentMyAdsBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener


class MyAdsFragment : Fragment() {

    private lateinit var binding: FragmentMyAdsBinding

    companion object{
        private const val TAG = "MY_ADS_TAG"
    }

    private lateinit var mContext: Context

    private lateinit var myTabsViewPagerAdapter: MyTabsViewPagerAdapter

    override fun onAttach(context: Context) {
        //this will init context for this fragment class
        this.mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = FragmentMyAdsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //add the tabs to tabLayout
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Ads"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Favorites"))
        //Fragment manage, initializing using getChildFragmentManager() cause we are using tabs in fragment not activity (in there should be getFragmentManager())
        val fragmentManager = childFragmentManager
        myTabsViewPagerAdapter = MyTabsViewPagerAdapter(fragmentManager, lifecycle)
        binding.viewPager.adapter = myTabsViewPagerAdapter
        //tab selected listener to set current item on view page
        binding.tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab) {
                //set current item on view page
                Log.d(TAG, "onTabSelected: tab: ${tab.position}")
                binding.viewPager.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
        })

        //change tab when swiping
        binding.viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.tabLayout.selectTab(binding.tabLayout.getTabAt(position))
            }
        })
    }

    class MyTabsViewPagerAdapter(fragmentManager: FragmentManager, lifeCycle: Lifecycle) :  FragmentStateAdapter(fragmentManager, lifeCycle){
        override fun getItemCount(): Int {
            //return list od items/tabs
            return 2//setting static size 2 cause we have two tabs/fragments
        }

        override fun createFragment(position: Int): Fragment {
            //tab position starts from 0. if 0 set/show MyAdsFragment otherwise it is 1 so show myAdsFavFragment
            if(position == 0){
                return MyAdsAdsFragment()
            }
            else{
                return MyAdsFavFragment()
            }
        }
    }

}