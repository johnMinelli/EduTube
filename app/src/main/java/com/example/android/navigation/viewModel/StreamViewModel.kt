package com.example.android.navigation.viewModel

import android.content.SharedPreferences
import android.util.Log
import android.widget.ScrollView
import androidx.annotation.InspectableProperty
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.android.navigation.R
import com.example.android.navigation.models.ContentItem
import com.example.android.navigation.util.Constants
import com.example.android.navigation.util.VideoInfo
import com.example.android.navigation.util.YouTubeDataEndpoint
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.min

class StreamViewModel(
        val fire: FirebaseFirestore,
        val pref: SharedPreferences,
        val selectedItem: ContentItem) : ViewModel() {

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var _readyState = MutableLiveData<Boolean>()
    val readyState: LiveData<Boolean>
        get() = _readyState

    private var _toggle = MutableLiveData<Boolean?>()
    val toggle = Transformations.map(_toggle){
        it==true
    }

    private var _toastMsg = MutableLiveData<String?>()
    val toastMsg: LiveData<String?>
        get() = _toastMsg

    private var _lock = MutableLiveData<Boolean?>()
    val lock = Transformations.map(_lock){
        it==true
    }
    val scroll = 0x00000020
    var visualized = false
    var categoryThumbnail: Pair<String,Int>? = null

    private val query by lazy {
        fire.collection("users").document(pref.getString(Constants().UID,"notavaliduid")!!)
    }
    private val query1 by lazy {
        fire.collection("contents").document(selectedItem.contentId)
    }

    init{
        _readyState.value = false
        _toggle.value = false
        _lock.value = false
        _toastMsg.value = null
        try {
            getCategoryThumbnail()
            retrieveVideoInfo()
        }catch(e: Exception){
            Log.d("Tag.StreamViewModel","Error (probably missing internet connection) ${e}")
        }
    }

    fun getCategoryThumbnail(){
        fire.document(selectedItem.categories.values.elementAt(0)).get().addOnSuccessListener {
            it.get("thumbnailUrl")?.let{
                categoryThumbnail = Pair(it as String,selectedItem.categories.values.size)
            }
        }.addOnCompleteListener{
            _readyState.value = true
        }
    }

    fun retrieveVideoInfo(){
        uiScope.launch {
            try {
                val v: VideoInfo? = YouTubeDataEndpoint.getVideoInfoFromYouTubeDataAPIs(selectedItem.videoUrl)
                v?.let {
                    if (selectedItem.uploader != v.channelTitle) query1.update("uploader", v.channelTitle)
                    if (selectedItem.thumbnailUrl != v.thumbnail) query1.update("thumbnailUrl", v.thumbnail)
                }
            }catch(e: RuntimeException){
                Log.d("Tag.StreamViewModel","Video link in valid, let it fail ${e}")
            }
        }
    }

    fun toggleDetail(){
        _toggle.value = !_toggle.value!!
    }

    fun toggleLock(){
        _lock.value = !_lock.value!!
    }

    fun visualizeIt(){
        visualized = true
        query.get().addOnSuccessListener {
            if(it.exists()){
                var oldHis = mutableMapOf<String, Timestamp>()
                var oldInt = mutableMapOf<String, Long>()
                var inc = 1
                it.get("history")?.let{
                    oldHis = it as MutableMap<String, Timestamp>
                    oldHis[selectedItem.contentId]?.let{inc=0}
                    oldHis[selectedItem.contentId] = Timestamp.now()
                }
                it.get("interests")?.let{
                    oldInt = it as MutableMap<String, Long>
                    selectedItem.categories.keys.forEach{
                        oldInt[it] = min((oldInt[it]?:0)+inc,20)
                    }
                }
                query.update("history",oldHis,"interests",oldInt)
            }
        }
        fire.runTransaction { transaction ->
            try{
                val video = transaction.get(query1).toObject<ContentItem>(ContentItem::class.java)
                video?.let{
                    it.viewCount = it.viewCount.plus(1)
                    // Commit to Firestore
                    transaction.set(query1, video)
                }
            }catch(e: Exception){
                Log.d("Tag.StreamViewModel","Casting gone bad")
            }
            null
        }
    }

    fun toggleBookmarks(){
        query.get().addOnSuccessListener {
            if(it.exists()){
                var oldBook = mutableMapOf<String, Timestamp>()
                var msg = ""
                it.get("bookmarks")?.let{
                    oldBook = it as MutableMap<String, Timestamp>
                    if(oldBook[selectedItem.contentId] != null){
                        oldBook.remove(selectedItem.contentId)
                        msg = "Content removed from Bookmarmks"
                    }else{
                        oldBook[selectedItem.contentId] = Timestamp.now()
                        msg = "Content added to Bookmarks"
                    }
                }
                query.update("bookmarks",oldBook).addOnSuccessListener {
                    _toastMsg.value = msg
                }.addOnFailureListener{
                    _toastMsg.value = "Error updating Bookmarks"

                }
            }else{
                _toastMsg.value = "Please log in first"
            }
        }
    }

    fun doneToastMsg(){
        _toastMsg.value = null
    }
}
