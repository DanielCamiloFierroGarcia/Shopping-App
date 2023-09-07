package com.example.shoppingcart

import android.widget.Filter
import com.example.shoppingcart.adapters.AdapterChats
import com.example.shoppingcart.models.ModelChats

class FilterChats : Filter {

    private val adapterChats: AdapterChats

    private val filterList: ArrayList<ModelChats>

    constructor(adapterChats: AdapterChats, filterList: ArrayList<ModelChats>) : super() {
        this.adapterChats = adapterChats
        this.filterList = filterList
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        //perform filter based on what user types
        var const: CharSequence? = constraint
        val results = FilterResults()

        if(!const.isNullOrEmpty()){
            const = constraint.toString().uppercase()

            val filteredModels = ArrayList<ModelChats>()

            for(i in filterList.indices){
                if(filterList[i].name.uppercase().contains(const)){
                    filteredModels.add(filterList[i])
                }
            }
            results.count = filteredModels.size
            results.values = filteredModels
        }
        else{
            results.count = filterList.size
            results.values = filterList
        }

        return results
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
        //publish the filtered result
        adapterChats.chatArrayList = results.values as ArrayList<ModelChats>
        adapterChats.notifyDataSetChanged()
    }
}