package com.example.shoppingcart

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.shoppingcart.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    private lateinit var mContext: Context

    private companion object{
        private const val TAG = "HOME_TAG"
    }

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(LayoutInflater.from(mContext), container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadCategories()
    }

    private fun loadCategories(){
        val categoryArrayList = ArrayList<ModelCategory>()

        //get categories from utils class and add in categoryArrayList
        for(i in 0 until Utils.categories.size){
            val modelCategory = ModelCategory(Utils.categories[i], Utils.categoryIcons[i])
            categoryArrayList.add(modelCategory)
        }
        //iinit/setup ApaterCategory
        val adapterCategory = AdapterCategory(mContext, categoryArrayList, object : RvListenerCategory{
            override fun onCategoryClick(modelCategory: ModelCategory) {

            }
        })

        binding.categoriesRv.adapter = adapterCategory
    }

}//WE LEFT AT 41:30