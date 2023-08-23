package com.example.shoppingcart

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.shoppingcart.databinding.RowCategoryBinding
import java.util.Random

/**Adapter category
 * @param context The context of activity from where instance of AdapterCategory class is created
 * @param categoryArrayList list of categories
 * @param rvListenerCategory instance of RvListenerCategory interface*/
class AdapterCategory(
    private val context: Context,
    private val categoryArrayList: ArrayList<ModelCategory>,
    private val rvListenerCategory: RvListenerCategory
) : RecyclerView.Adapter<AdapterCategory.HolderCategory>() {

    private lateinit var binding: RowCategoryBinding

    private companion object{
        private const val TAG = "ADAPTER_CATEGORY_TAG"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderCategory {
        binding = RowCategoryBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderCategory(binding.root)
    }

    override fun getItemCount(): Int {
        return categoryArrayList.size
    }

    override fun onBindViewHolder(holder: HolderCategory, position: Int) {
        val modelCategory = categoryArrayList[position]

        val icon = modelCategory.icon
        val category = modelCategory.category

        val random = Random()
        val color = Color.argb(255, random.nextInt(255), random.nextInt(255), random.nextInt(255))

        holder.categoryIconTv.setImageResource(icon)
        holder.categoryTv.text = category
        holder.categoryIconTv.setBackgroundColor(color)

        holder.itemView.setOnClickListener {
            rvListenerCategory.onCategoryClick(modelCategory)
        }
    }

    inner class HolderCategory(itemView: View): ViewHolder(itemView){
        var categoryIconTv = binding.categoryIconIv
        var categoryTv = binding.categoryTv

    }
}