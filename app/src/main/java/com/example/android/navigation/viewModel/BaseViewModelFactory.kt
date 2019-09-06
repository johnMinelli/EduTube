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

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.android.navigation.fragments.*
import com.example.android.navigation.models.ContentItem
import com.google.firebase.firestore.FirebaseFirestore

/**
 * This is pretty much boiler plate code for a ViewModel Factory.
 *
 * Provides the SleepDatabaseDao and context to the ViewModel.
 */
class BaseViewModelFactory(
        private val vm: Any,
        private val dataSource: FirebaseFirestore,
        private val pref: SharedPreferences) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        when(vm){
            is CategoriesFragment ->
                if(modelClass.isAssignableFrom(CategoriesViewModel::class.java)) {
                    return CategoriesViewModel(dataSource, pref) as T
                }
            is HotFragment ->
                if(modelClass.isAssignableFrom(HotViewModel::class.java)) {
                    return HotViewModel(dataSource, pref) as T
                }
            is BookmarksFragment ->
                if(modelClass.isAssignableFrom(BookmarksViewModel::class.java)) {
                    return BookmarksViewModel(dataSource, pref) as T
                }
            is HistoryFragment ->
                if(modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
                    return HistoryViewModel(dataSource, pref) as T
                }
            is CreatorFragment ->
                if(modelClass.isAssignableFrom(CreatorViewModel::class.java)) {
                    return CreatorViewModel(dataSource, pref) as T
                }
            is AdminFragment ->
                if(modelClass.isAssignableFrom(AdminViewModel::class.java)) {
                    return AdminViewModel(dataSource, pref) as T
                }
            else -> {}
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

