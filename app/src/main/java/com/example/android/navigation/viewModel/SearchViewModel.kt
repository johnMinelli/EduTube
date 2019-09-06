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

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.android.navigation.models.ContentItem
import com.example.android.navigation.util.Constants
import com.example.android.navigation.util.WrapperChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class SearchViewModel(
        val fire: FirebaseFirestore) : ViewModel() {
    //this one will be from DB
    var itemAdded: WrapperChange<ContentItem>? = null
    private var _itemsList = MutableLiveData<ArrayList<ContentItem>>()
    val itemsList: LiveData<ArrayList<ContentItem>>
        get() = _itemsList

    private var _lock = false
    private var _searchString: String = ""
    private var _contentFilter: String = Constants().FILTER_ALL

    private var _searchingState = MutableLiveData<Boolean>()
    val searchingState: LiveData<Boolean>
        get() = _searchingState

    private var _readyState = MutableLiveData<Boolean>()
    val readyState: LiveData<Boolean>
        get() = _readyState

    //---------------------------------------------------------------

    private val query by lazy {
        fire.collection("contents")
    }

    init{
        _readyState.value = false
        _itemsList.value = ArrayList(0)
        _searchingState.value = false
        _readyState.value = true
    }

    fun search(searchString: String){
        if (searchString.isEmpty()) return
        if(_lock)return else _lock = true
        _itemsList.value = arrayListOf()
        _searchString = searchString
        _searchingState.value = true
        var r = mutableMapOf<String, String>()
        query.get().addOnSuccessListener { querySnapshot ->
            querySnapshot.documents.forEachIndexed { index, documentSnapshot ->
                try{
                    val a: ContentItem? = documentSnapshot.toObject(ContentItem::class.java)
                    a?.let{
                        a.contentId = documentSnapshot.id
                        if(Constants().CONTENT_TYPE == _contentFilter) {
                            if(a.title.contains(searchString,true))add(a)
                        }else if(Constants().CREATOR_TYPE == _contentFilter) {
                            if(a.creator.contains(searchString,true))add(a)
                        }else if(Constants().UPLOADER_TYPE == _contentFilter) {
                            if(a.uploader.contains(searchString,true))add(a)
                        }else{
                            if(searchString.length>3){
                                if(a.title.contains(searchString,true) ||
                                        a.creator.contains(searchString,true) ||
                                        a.uploader.contains(searchString,true))
                                    add(a)
                            }else{
                                if(a.title.contains(searchString,true))add(a)
                            }
                        }

                    }
                }catch(e: Exception){
                    Log.d("Tag.CategoriesViewModel","Cast gone bad")
                }
            }
        }.addOnFailureListener{exception->
            Log.d("Tag.CategoriesViewModel","First level exception ${exception}")
            _searchingState.value = false
        }.addOnCompleteListener{
            if(_searchingState.value!!)_searchingState.value = false
            _lock = false
        }
    }

    fun add(a: ContentItem) {
        _itemsList.value?.add(a)
        val index = _itemsList.value?.indexOf(a)
        itemAdded = WrapperChange(index!!,a)
        _itemsList.value = _itemsList.value
        if(_searchingState.value!!)_searchingState.value = false
    }

    fun applyFilter(contentFilter: String){
        _contentFilter = contentFilter
        search(_searchString)
    }

    fun doneList(){
        itemAdded = null
    }
}