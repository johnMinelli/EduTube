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
import com.ahmedabdelmeged.firestore.adapter.FirestoreDocAdapter
import com.example.android.navigation.models.CategoryItem
import com.example.android.navigation.models.ContentItem
import com.example.android.navigation.util.Constants
import com.example.android.navigation.util.WrapperChange
import com.google.firebase.firestore.*
import java.lang.Exception

class HistoryViewModel(
        val fire: FirebaseFirestore,
        val pref: SharedPreferences) : ViewModel(), FirestoreDocAdapter {

    var itemAdded: WrapperChange<ContentItem>? = null
    private var _itemsList = MutableLiveData<ArrayList<ContentItem>>()
    val itemsList: LiveData<ArrayList<ContentItem>>
        get() = _itemsList

    private var _readyState = MutableLiveData<Boolean>()
    val readyState: LiveData<Boolean>
        get() = _readyState

    private var bookmarks: MutableMap<String,String> = mutableMapOf()


    //---------------------------------------------------------------
    override var _mRegistration: ListenerRegistration? = null
    override var _mQueryDoc: DocumentReference? = null


    val uid = pref.getString(Constants().UID,"notavaliduid")
    private val query by lazy {
        fire.collection("users").document(uid)
    }
    private val query2 by lazy {
        fire.collection("contents")
    }
    //will be great to implement also an offline managment of bookmarks

    init{
        _readyState.value = false
        _itemsList.value = ArrayList(0)
        setQuery(query)
        _readyState.value = true
    }

    override fun onDocumentModified(change: DocumentSnapshot) {
        try{
            change.get("history")?.let{
                val newbk: MutableMap<String,String> = it as MutableMap<String,String>
                if(newbk != bookmarks){
                    bookmarks = newbk
                    bookmarks.keys.forEach {
                        query2.document(it).get().addOnSuccessListener { documentSnapshot ->
                            try {
                                val a: ContentItem? = documentSnapshot.toObject(ContentItem::class.java)
                                a?.let {
                                    a.contentId = documentSnapshot.id
                                    _itemsList.value?.add(a)
                                    val index = _itemsList.value?.indexOf(a)
                                    itemAdded = WrapperChange(index!!, a)
                                    _itemsList.value = _itemsList.value
                                }
                            } catch (e: Exception) {
                                Log.d("Tag.HistoryViewModel", "Cast gone bad")
                            }
                        }.addOnFailureListener { exception ->
                            Log.d("Tag.HistoryViewModel", "First level exception ${exception}")
                            //probabily a deleted content
                            bookmarks.remove(it)
                            query.update("history",bookmarks)
                        }
                    }
                }
            }
        }catch(e: Exception){
            Log.d("Tag.HistoryViewModel","Cast mutable history gone bad")
        }
    }

    fun doneList(){
        itemAdded = null
    }
}