package com.example.shoppingcart

import android.widget.Filter
import java.util.Locale

class FilterAd(
    private val adapter: AdapterAd,
    private val filterList: ArrayList<ModelAd>
) : Filter(){
    override fun performFiltering(constraint: CharSequence?): FilterResults {
        //this function is to perfor the filter based on what is typed
        var const = constraint
        var results = FilterResults()
        //check if query is not empty nor null
        if(!constraint.isNullOrEmpty()){
            const = constraint.toString().uppercase(Locale.getDefault())

            //to hold list of filtered ads based on query
            val filteredModels = ArrayList<ModelAd>()
            for(i in filterList.indices){
                //apply filter if query matches to any fo bran,.... and add it to filteredModels
                if(filterList[i].brand.uppercase(Locale.getDefault()).contains(const) ||
                    filterList[i].category.uppercase(Locale.getDefault()).contains(const) ||
                    filterList[i].condition.uppercase(Locale.getDefault()).contains(const) ||
                    filterList[i].title.uppercase(Locale.getDefault()).contains(const)
                ){
                    //query matches to any of brand....
                    filteredModels.add(filterList[i])
                }
            }
            results.count = filteredModels.size
            results.values = filteredModels
        }
        else{
            //query is either  null or empty so return the original list
            results.count = filterList.size
            results.values = filterList
        }
        return results
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
        adapter.adArrayList = results.values as ArrayList<ModelAd>

        adapter.notifyDataSetChanged()
    }
}