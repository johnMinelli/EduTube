/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.navigation.viewModel

import android.os.Build
import android.text.Html
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.android.navigation.R
import com.example.android.navigation.models.CategoryItem
import com.example.android.navigation.models.ContentItem
import com.example.android.navigation.util.Constants
import com.example.android.navigation.util.Globals
import com.example.android.navigation.util.VideoInfo
import com.example.android.navigation.util.YouTubeDataEndpoint
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.okhttp.*
import kotlinx.coroutines.*
import org.json.JSONObject
import java.lang.RuntimeException

class EditViewModel(
        val fire: FirebaseFirestore,
        var selectedItem: ContentItem?) : ViewModel() {

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private val client: OkHttpClient = OkHttpClient()
    private val type: MediaType = MediaType.parse("application/json; charset=utf-8")

    var wrapList: MutableMap<String, String>
    private var _categoriesList = MutableLiveData<MutableMap<String, String>>() //coppia titolo, refpath
    val categoriesList: LiveData<MutableMap<String, String>>
        get() = _categoriesList
    var selectedCategories: MutableMap<String,String> = mutableMapOf()
    var wrap: String = ""

    //only the text of the content is mutable
    private var text = MutableLiveData<String>()

    val textTransformed = Transformations.map(text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            Html.fromHtml(it, Html.FROM_HTML_MODE_COMPACT)
        else
            Html.fromHtml(it)
    }

    private var _readyState = MutableLiveData<Boolean>()
    val readyState: LiveData<Boolean>
        get() = _readyState

    private val _navigateToEditor = MutableLiveData<String?>()
    val navigateToEditor: LiveData<String?>
        get() = _navigateToEditor

    private val _doneSaveAndNavigateBack = MutableLiveData<Boolean?>()
    val doneSaveAndNavigateBack: LiveData<Boolean?>
        get() = _doneSaveAndNavigateBack

    private val query by lazy {
        fire.collection("categories")
    }
    private val query2 by lazy {
        fire.collection("contents")
    }

    init {
        _readyState.value = false
        if (selectedItem == null) {
            selectedItem = ContentItem()
            wrap = selectedItem!!.wrap
            wrapList = mutableMapOf()
            initWrapList().addOnCompleteListener{
                _readyState.value = true
            }
        }else{
            wrap = selectedItem!!.wrap
            wrapList = mutableMapOf()
            initWrapList().addOnCompleteListener{
                selectedCategories = selectedItem!!.categories
                changeWrap(selectedItem!!.wrap,true)
                _readyState.value = true
            }
        }
        text.value = selectedItem!!.text
        _navigateToEditor.value = null
    }

    fun initWrapList(): Task<*> {
        return query.get().addOnSuccessListener { querySnapshot ->
            querySnapshot.documents.forEachIndexed { idx, documentSnapshot ->
                wrapList.put(documentSnapshot.id, documentSnapshot.reference.id) //ciò che è scritto nel db aka chiave (a), path relativa per la categoria aka valore
            }
            //contents -> [idcontent] -> {..., wrap: (a), }
            //categories -> (b) -> documenti
        }.addOnFailureListener { exception ->
            Log.d("Tag.EditViewModel", "initWrapList ${exception}")
        }
    }

    fun changeWrap(key: String = "", initForSelected: Boolean = false){
        val ref = wrapList.get(key)?:return
        wrap = key
        _categoriesList.value = mutableMapOf()
        if(!initForSelected)selectedCategories = mutableMapOf()
        query.document(ref).collection("sub").get().addOnSuccessListener { querySnapshot ->
            querySnapshot.documents.forEachIndexed { idx, documentSnapshot ->
                try{
                    val a: CategoryItem? = documentSnapshot.toObject(CategoryItem::class.java)
                    _categoriesList.value!!.put(a!!.title,documentSnapshot.reference.path)
                }catch(e: Exception){
                    Log.d("Tag.EditViewModel","Cast gone bad")
                }
            }
        }.addOnFailureListener { exception ->
            Log.d("Tag.EditViewModel", "changeWrap ${exception}")
        }.addOnCompleteListener {
            _categoriesList.value = _categoriesList.value
        }
    }


    @Throws(RuntimeException::class)
    fun save(title: String, videoUrl: String, readTime: String, uid: String, uname: String){
        uiScope.launch {
            var cleanUrl = videoUrl
            if(videoUrl.contains("www.",true) || videoUrl.contains("youtube",false))
                cleanUrl = videoUrl.removeRange(0,videoUrl.indexOf("?v=",0, true)+3)
            val v: VideoInfo? = YouTubeDataEndpoint.getVideoInfoFromYouTubeDataAPIs(cleanUrl)
            if (v != null) {
                val a = selectedItem!!
                a.creator = uname
                a.creatorId = uid
                a.uploader = v.channelTitle
                a.thumbnailUrl = v.thumbnail
                a.videoUrl = cleanUrl
                a.readTime = readTime
                a.categories = selectedCategories
                a.wrap = wrap
                a.title = title
                a.text = text.value!!
                a.filterall = a.title + " " + a.creator + " " + a.uploader
                try {
                    Integer.parseInt(a.contentId)
                    query2.add(a).addOnFailureListener {
                        _doneSaveAndNavigateBack.value = false
                    }.addOnSuccessListener {
                        _doneSaveAndNavigateBack.value = true
                        query2.document(it.id).update("contentId", it.id)
                        selectedCategories.keys.forEach{
                            notifyToFollowers(it,"Hey, new content in this section for you")
                        }
                    }
                } catch (e: Exception) {
                    query2.document(a.contentId).set(a).addOnFailureListener {
                        _doneSaveAndNavigateBack.value = false
                    }.addOnSuccessListener {
                        _doneSaveAndNavigateBack.value = true
                    }
                }
            } else {
                _doneSaveAndNavigateBack.value = false
            }
        }
    }

    fun notifyToFollowers(category: String, text: String){
        uiScope.launch {
            val url = "https://fcm.googleapis.com/fcm/send"
            val json = JSONObject()
            val not = JSONObject()
            json.put("to", "/topics/"+category)
            not.put("title", fire.app.applicationContext.getString(R.string.app_name)+" • "+category)
            not.put("body", text)
            json.put("notification", not)
            Log.d("Tag.post",post(url, json.toString()))
        }
    }

    suspend fun post(url: String, json: String): String{
        return withContext(Dispatchers.IO) {
            val body: RequestBody = RequestBody.create(type, json)
            val request: Request = Request.Builder().url(url).post(body)
                    .addHeader("Authorization", "key="+ Globals().FCM_API_KEY).build()
            val response: Response = client.newCall(request).execute()
            response.body().string()
        }
    }


    fun doneGoBack() {
        _doneSaveAndNavigateBack.value = null
    }

    fun goToEditor() {
        _navigateToEditor.value = text.value
    }

    fun doneGoToEditor() {
        _navigateToEditor.value = null
    }

    fun returnFromEditor(newText: String?) {
        newText?.let{
            selectedItem?.text = newText
            text.value = newText
        }
    }
}