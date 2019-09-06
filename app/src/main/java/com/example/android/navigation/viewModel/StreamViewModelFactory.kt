package com.example.android.navigation.viewModel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.android.navigation.models.ContentItem
import com.google.firebase.firestore.FirebaseFirestore

class StreamViewModelFactory(
        private val dataSource: FirebaseFirestore,
        private val pref: SharedPreferences,
        private val selectedItem: ContentItem) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StreamViewModel::class.java)) {
            return StreamViewModel(dataSource, pref, selectedItem) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}