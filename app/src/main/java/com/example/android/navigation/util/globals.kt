package com.example.android.navigation.util

import android.app.Application

class Constants{
    val KEY_THEME = "theme"
    val KEY_VIEW_MODE = "list_view_mode"
    val KEY_ROLE = "role"
    val KEY_NOTIFICATION = "notification"
    val KEY_LOGGED = "logged"

    val LIGHT = "light_theme"
    val DARK = "dark_theme"

    val USER = false
    val CREATOR = true

    val UID = "user_id"
    val UNAME = "user_name"
    val UIMAGE = "user_image"

    val VIEW_MODE_AUTO = "auto"
    val VIEW_MODE_LIST = "list"
    val VIEW_MODE_GRID = "grid"

    val HEADER_TYPE = "header"
    val FOOTER_TYPE = "footer"
    val STREAM_TYPE = "stream"
    val CONTENT_TYPE = "content"
    val CREATOR_TYPE = "creator"
    val UPLOADER_TYPE = "uploader"
    val COMMENT_TYPE = "comment"
    val CATEGORY_TYPE = "category"

    val HEADER_LIST_TYPE = 1
    val FOOTER_LIST_TYPE = 2
    val CONTENT_LIST_TYPE = 3
    val CREATOR_LIST_TYPE = 4
    val COMMENT_LIST_TYPE = 5
    val CATEGORY_LIST_TYPE = 6

    val FILTER_ALL = "all"
    val FILTER_CONTENT = "content"
    val FILTER_CREATOR = "creator"
    val FILTER_UPLOADER = "uploader"
    val CREATOR_TAB_ID = 4

    val TASK_REQUEST = 1
    val TASK_REQUEST_EXTRA = "task_request_extra"
    val NOTIFICATION_CHANNEL = "notification"
    val FCM_TOKEN = "token"
}

class Globals : Application() {
    var THEME = Constants().LIGHT
    var ROLE = Constants().USER
    var NOTIFICATION = false
    var LOGGED = false
    var VIEW_MODE = Constants().VIEW_MODE_LIST
    val YT_API_KEY = "AIzaSyBY9v3agBVPQb-76WIw-4kjUxq_LD3krKY"
    val FCM_API_KEY = "AIzaSyDmTkir8HghKmIJY58aCl244heiUqZ7lbw"
    val APP_NAME = "Edutube"

}