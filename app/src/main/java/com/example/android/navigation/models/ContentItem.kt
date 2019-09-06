package com.example.android.navigation.models


import com.example.android.navigation.models.base.Item
import com.example.android.navigation.util.Constants
import com.google.firebase.firestore.ServerTimestamp
import java.util.*
import kotlin.collections.ArrayList

data class ContentItem(
        var contentId: String = (Math.random()*Math.pow(10.0,8.0)).toInt().toString(),
        var type: String = Constants().STREAM_TYPE,

        var creator: String = "",
        var creatorId: String = "",
        var uploader: String = "",
        var filterall: String = "",
        var viewCount: Int = 0,
        var viewTime: String = "",
        var readTime: String = "",
        @ServerTimestamp var timestamp: Date = Date(),
        var categories: MutableMap<String,String> = mutableMapOf(),
        var wrap: String = "",
        var title: String = "",
        var text: String = "",
        var thumbnailUrl: String = "",
        var videoUrl: String = ""
) : Item(contentId, type)