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
import com.example.android.navigation.util.Globals
import com.example.android.navigation.util.WrapperChange
import com.google.firebase.firestore.*
import com.squareup.okhttp.*
import kotlinx.coroutines.*
import org.json.JSONObject


class AdminViewModel(
        val fire: FirebaseFirestore,
        val pref: SharedPreferences) : ViewModel() {

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private val client: OkHttpClient = OkHttpClient()
    private val type: MediaType = MediaType.parse("application/json; charset=utf-8")

    private var _readyState = MutableLiveData<Boolean>()
    val readyState: LiveData<Boolean>
        get() = _readyState

    val uid = pref.getString(Constants().UID,"notavaliduid")
    private val query by lazy {
        fire.collection("constants")
    }

    init{
        _readyState.value = false
        _readyState.value = true
    }


    fun send(title: String, text: String){
        query.document("admin").get().addOnSuccessListener {
            if(it.get("uid") == uid){
                uiScope.launch {
                    val url = "https://fcm.googleapis.com/fcm/send"
                    val json = JSONObject()
                    val not = JSONObject()
                    json.put("to", "/topics/notification")
                    not.put("title", title)
                    not.put("body", text)
                    json.put("notification", not)
                    Log.d("Tag.post",post(url, json.toString()))
                }
            }else{
                Log.d("Tag.AdminViewModel","You are not an admin... still you are here... how")
            }
        }.addOnFailureListener{
            Log.w("Tag.AdminViewModel", "Get admin table failed", it)

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

}