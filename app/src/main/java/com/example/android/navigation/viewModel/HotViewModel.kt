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

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ahmedabdelmeged.firestore.adapter.FirestoreAdapter
import com.example.android.navigation.models.CategoryItem
import com.example.android.navigation.models.ContentItem
import com.example.android.navigation.util.Constants
import com.example.android.navigation.util.WrapperChange
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class HotViewModel(
        val fire: FirebaseFirestore,
        val pref: SharedPreferences) : ViewModel(), FirestoreAdapter {

    //this one will be from DB
    override var itemAdded: WrapperChange<ContentItem>? = null
    override var itemModified: WrapperChange<ContentItem>? = null
    override var itemRemoved: WrapperChange<ContentItem>? = null
    //top content suggested (hard inflated)
    var topItem: ContentItem = ContentItem()
    //array di 4 elementi con le categorie preferite (hard inflated)
    var catList: ArrayList<CategoryItem> = ArrayList(0)
    //top contents suggested for you
    override var _itemsList = MutableLiveData<ArrayList<ContentItem>>()

    private var _readyState = MutableLiveData<Boolean>()
    val readyState: LiveData<Boolean>
        get() = _readyState

    private val _navigateToCategory = MutableLiveData<CategoryItem?>()
    val navigateToCategory: LiveData<CategoryItem?>
        get() = _navigateToCategory

    private val _navigateToContent = MutableLiveData<ContentItem?>()
    val navigateToContent: LiveData<ContentItem?>
        get() = _navigateToContent

    //---------------------------------------------------------------
    override var _mRegistration: ListenerRegistration? = null
    override var _mQuery: Query? = null


    private val query by lazy {
        fire.collection("contents").orderBy("viewCount").limit(1)
    }
    private val query2 by lazy {
        fire.collection("categories").document("Matematica").collection("sub")
    }
    private val query3 by lazy {
        fire.collection("contents").orderBy("viewCount").limit(5)
    }

    init {
        _readyState.value = false
        _navigateToCategory.value = null
        _itemsList.value = ArrayList(0)
        //wrap requst and on done set ready
        setQuery(query3)
        initTopAndCategories()
    }

    fun initTopAndCategories(){
        query.get().addOnSuccessListener { querySnapshot ->
            try{
                val a: ContentItem? = querySnapshot.documents.get(0).toObject(ContentItem::class.java)
                topItem = a!!
            }catch(e: Exception){
                Log.d("Tag.HotViewModel","Cast gone bad")
            }
        }.addOnFailureListener{exception->
            Log.d("Tag.HotViewModel","First level first exception ${exception}")
        }.addOnCompleteListener{
            query2.get().addOnSuccessListener { querySnapshot ->
                querySnapshot.documents.forEachIndexed { idx, documentSnapshot ->
                    try{
                        val a: CategoryItem? = documentSnapshot.toObject(CategoryItem::class.java)
                        a?.let{
                            catList.add(a)
                        }
                    }catch(e: Exception){
                        Log.d("Tag.HotViewModel","Cast gone bad")
                    }
                }
                _readyState.value = true
            }.addOnFailureListener{exception->
                Log.d("Tag.HotViewModel","First level second exception ${exception}")
            }
        }
    }

    fun goToCategory(c: CategoryItem) {
        _navigateToCategory.value = c
    }

    fun doneGoToCategory() {
        _navigateToCategory.value = null
    }

    fun goToContent(c: ContentItem) {
        _navigateToContent.value = c
    }

    fun doneGoToContent() {
        _navigateToContent.value = null
    }
}