package com.example.shoppingcart

import com.example.shoppingcart.models.ModelCategory

interface RvListenerCategory {
    fun onCategoryClick(modelCategory: ModelCategory)
}