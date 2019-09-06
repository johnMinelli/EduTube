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
import android.preference.PreferenceManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.android.navigation.models.Tab
import com.example.android.navigation.util.Constants
import com.example.android.navigation.util.Globals
import kotlinx.coroutines.withContext

class MainFViewModel(val pref: SharedPreferences) : ViewModel() {

    var tabList = ArrayList<Tab>()
    var creator = false

    init {
        tabList.add(Tab.HotTab())
        tabList.add(Tab.CategoriesTab())
        tabList.add(Tab.BookmarksTab())
        recheckRole()
    }

    fun recheckRole(){
        val a = pref.getBoolean(Constants().KEY_LOGGED, Globals().LOGGED)
        val b = pref.getBoolean(Constants().KEY_ROLE, Globals().ROLE)
        if (a && b && !creator) {
            tabList.add(Tab.CreatorTab())
            creator = true
        }else if((!a || !b) && creator){
            tabList.remove(Tab.CreatorTab())
            creator = false
        }
    }

}