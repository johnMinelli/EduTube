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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ahmedabdelmeged.firestore.adapter.FirestoreAdapter
import com.example.android.navigation.models.CategoryItem
import com.example.android.navigation.models.ContentItem
import com.example.android.navigation.util.WrapperChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class CategoryViewModel(
        val fire: FirebaseFirestore,
        val selectedCategory: String) : ViewModel(), FirestoreAdapter {
    //this one will be from DB
    override var itemAdded: WrapperChange<ContentItem>? = null
    override var itemModified: WrapperChange<ContentItem>? = null
    override var itemRemoved: WrapperChange<ContentItem>? = null
    //top contents suggested for you
    override var _itemsList = MutableLiveData<ArrayList<ContentItem>>()

    private var _readyState = MutableLiveData<Boolean>()
    val readyState: LiveData<Boolean>
        get() = _readyState

    //---------------------------------------------------------------
    override var _mRegistration: ListenerRegistration? = null
    override var _mQuery: Query? = null


    private val query by lazy {
        fire.collection("contents").whereGreaterThan("categories."+selectedCategory,"")
    }

    init{
        _readyState.value = false
        _itemsList.value = ArrayList(0)
        setQuery(query)
        _readyState.value = true
    }
}