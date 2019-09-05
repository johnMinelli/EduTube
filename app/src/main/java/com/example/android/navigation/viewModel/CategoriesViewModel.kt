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
import com.example.android.navigation.models.CategoryItem
import com.example.android.navigation.util.Constants
import com.example.android.navigation.util.WrapperChange
import com.google.firebase.firestore.*
import com.google.firebase.messaging.FirebaseMessaging
import java.lang.Exception

class CategoriesViewModel(
        val fire: FirebaseFirestore,
        val pref: SharedPreferences) : ViewModel() {
    //this one will be from DB
    var itemAdded: WrapperChange<CategoryItem>? = null
    var itemReload: CategoryItem? = null
    private var _itemsList = MutableLiveData<ArrayList<CategoryItem>>()
    val itemsList: LiveData<ArrayList<CategoryItem>>
        get() = _itemsList

    private var _readyState = MutableLiveData<Boolean>()
    val readyState: LiveData<Boolean>
        get() = _readyState
    //-------------------------------------------------------------

    private val query by lazy {
        fire.collection("users").document(pref.getString(Constants().UID,"notavaliduid"))
    }
    private val query1 by lazy {
        fire.collection("categories")
    }

    init{
        _readyState.value = false
        _itemsList.value = ArrayList(0)
        initItemsList()
        _readyState.value = true
    }

    fun initItemsList(){
        var r = mutableMapOf<String, String>()
        query.get().addOnSuccessListener {
            it.get("categories")?.let{r = it as MutableMap<String, String>}
        }.addOnSuccessListener {
            query1.get().addOnSuccessListener { querySnapshot ->
                querySnapshot.documents.forEachIndexed { idx, documentSnapshot ->
                    val color = documentSnapshot.getString("color")?:"#FFFFFFFF"
                    val wrap = documentSnapshot.id
                    documentSnapshot.reference.collection("sub")?.get()?.addOnSuccessListener { querySnapshot ->
                        querySnapshot.documents.forEachIndexed { index, documentSnapshot ->
                            try{
                                val a: CategoryItem? = documentSnapshot.toObject(CategoryItem::class.java)
                                a?.let{
                                    if(r[a.title] != null) a.follow = true
                                    a.color = color
                                    a.wrap = wrap
                                    _itemsList.value?.add(a)
                                    val index = _itemsList.value?.indexOf(a)
                                    itemAdded = WrapperChange(index!!,a)
                                    _itemsList.value = _itemsList.value
                                }
                            }catch(e: Exception){
                                Log.d("Tag.CategoriesViewModel","Cast gone bad")
                            }
                        }
                    }?.addOnFailureListener{exception->
                        Log.d("Tag.CategoriesViewModel","Third level exception ${exception}")
                    }
                }
            }.addOnFailureListener{exception->
                Log.d("Tag.CategoriesViewModel","Second level exception ${exception}")
            }
        }.addOnFailureListener{exception->
            Log.d("Tag.CategoriesViewModel","First level exception ${exception}")
        }
    }

    fun doneList(){
        itemAdded = null
        itemReload = null
    }

    fun toggleFollow(selectedItem: CategoryItem){
        query.get().addOnSuccessListener {
            var r = mutableMapOf<String, String>()
            if(!selectedItem.follow){
                //follow
                subscribeForNotifications(selectedItem.title, true)
                if(it.get("categories") != null){
                    val oldCat: MutableMap<String, String> = it.get("categories") as MutableMap<String, String>
                    oldCat[selectedItem.title] = "follow"
                    r = oldCat
                }else{
                    r = mutableMapOf(Pair(selectedItem.title,"follow"))
                }
            }else{
                //unfollow
                subscribeForNotifications(selectedItem.title, false)
                if(it.get("categories") != null){
                    val oldCat: MutableMap<String, String> = it.get("categories") as MutableMap<String, String>
                    oldCat.remove(selectedItem.title)
                    r = oldCat
                }
            }
            query.update("categories",r).addOnSuccessListener {
                selectedItem.follow = !selectedItem.follow
                itemReload = selectedItem
                _itemsList.value = _itemsList.value
            }
        }
    }

    fun subscribeForNotifications(channel: String, status: Boolean){
        if(status){
            FirebaseMessaging.getInstance().subscribeToTopic(channel)
                .addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.d("Tag.CategoriesViewModel","Error notifications following status")
                    }
                }
        }else{
            FirebaseMessaging.getInstance().unsubscribeFromTopic(channel)
                .addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.d("Tag.CategoriesViewModel","Error notifications unfollowing status")
                    }
                }
        }
    }

}