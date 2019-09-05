package com.example.android.navigation.models

import com.example.android.navigation.models.base.Item
import com.example.android.navigation.util.Constants

data class CategoryItem(
            var contentId: String = (Math.random()*Math.pow(10.0,8.0)).toInt().toString(),
            var type: String = Constants().CATEGORY_TYPE,

            var title: String = "",
            var wrap: String = "",
            var follow: Boolean = false,
            var thumbnailUrl: String? = "",
            var color: String = "#FFFFFFFF"

) : Item(contentId, type)