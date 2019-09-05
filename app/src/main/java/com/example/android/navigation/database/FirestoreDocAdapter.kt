package com.ahmedabdelmeged.firestore.adapter


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.android.navigation.models.ContentItem
import com.example.android.navigation.util.WrapperChange
import com.google.firebase.firestore.*
import java.util.ArrayList


interface FirestoreDocAdapter: EventListener<DocumentSnapshot>{
    var _mRegistration: ListenerRegistration?
    var _mQueryDoc: DocumentReference?

    override fun onEvent(p0: DocumentSnapshot?, e: FirebaseFirestoreException?) {
        if (e != null) {
            Log.d("Tag.FirestoreAdapter", "onEvent:error", e)
            onError(e)
            return
        }
        try {
            onDocumentModified(p0!!)
        }catch(e: Exception){
            Log.d("Tag.FirestoreDocAdapter","Not a change")
        }
    }

    fun startListening() {
        if (_mQueryDoc != null && _mRegistration == null) {
            _mRegistration = _mQueryDoc!!.addSnapshotListener(this)
        }
    }

    fun stopListening() {
        if (_mRegistration != null) {
            _mRegistration!!.remove()
            _mRegistration = null
        }
    }

    fun setQuery(query: DocumentReference) {
        // Stop listening
        stopListening()
        // Listen to new query
        _mQueryDoc = query
        startListening()
    }

    fun onDocumentModified(change: DocumentSnapshot)

    fun onError(e: FirebaseFirestoreException) {}

}
