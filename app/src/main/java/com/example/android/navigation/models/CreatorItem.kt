package com.example.android.navigation.models


import com.example.android.navigation.models.base.Item
import com.example.android.navigation.util.Constants

class CreatorItem(
        contentId: String,
        type: String) : Item(contentId, Constants().STREAM_TYPE) {

    constructor(
            contentId: String,
            url: String,
            name: String,
            creator: String,
            uploader: String,
            type: String,
            viewCount: Long,
            viewTime: String,
            readTime: String,
            title: String,
            thumbnailUrl: String?) : this(contentId, name){
        this.creator = creator
        this.uploader = uploader
        this.type = type
        this.viewCount = viewCount
        this.viewTime = viewTime
        this.readTime = readTime
        this.title = title
        this.thumbnailUrl = thumbnailUrl
    }

    var creator: String? = null
    var uploader: String? = null
    var type: String = ""
    var viewCount: Long = -1
    var viewTime: String = ""
    var readTime: String = ""
    var title: String = ""
    var thumbnailUrl: String? = null

}