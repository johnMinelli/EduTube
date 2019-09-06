package com.ahmedabdelmeged.firestore.adapter


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.android.navigation.models.ContentItem
import com.example.android.navigation.util.WrapperChange
import com.google.firebase.firestore.*
import java.util.ArrayList


interface FirestoreAdapter: EventListener<QuerySnapshot>{
    var _mRegistration: ListenerRegistration?
    var _mQuery: Query?

    //this one will be from DB
    var itemAdded: WrapperChange<ContentItem>?
    var itemModified: WrapperChange<ContentItem>?
    var itemRemoved: WrapperChange<ContentItem>?

    var _itemsList: MutableLiveData<ArrayList<ContentItem>>
    val itemsList: LiveData<ArrayList<ContentItem>>
        get() = _itemsList

    override fun onEvent(documentSnapshots: QuerySnapshot?, e: FirebaseFirestoreException?) {
        if (e != null) {
            Log.d("Tag.FirestoreAdapter", "onEvent:error", e)
            onError(e)
            return
        }

        // Dispatch the event
        Log.d("Tag.FirestoreAdapter", "onEvent:numChanges:" + documentSnapshots!!.documentChanges.size)
        for (change in documentSnapshots.documentChanges) {
            when (change.type) {
                DocumentChange.Type.ADDED -> onDocumentAdded(change)
                DocumentChange.Type.MODIFIED -> onDocumentModified(change)
                DocumentChange.Type.REMOVED -> onDocumentRemoved(change)
            }
        }
        onDataChanged()
    }

    fun startListening() {
        if (_mQuery != null && _mRegistration == null) {
            _mRegistration = _mQuery!!.addSnapshotListener(this)
        }
    }

    fun stopListening() {
        if (_mRegistration != null) {
            _mRegistration!!.remove()
            _mRegistration = null
        }
    }

    fun setQuery(query: Query) {
        // Stop listening
        stopListening()
        // Clear existing data
        _itemsList.value = arrayListOf()
        // Listen to new query
        _mQuery = query
        startListening()
    }

//    fun onDocumentAdded(change: DocumentChange)
//    fun onDocumentModified(change: DocumentChange)
//    fun onDocumentRemoved(change: DocumentChange)

    fun onDocumentAdded(change: DocumentChange) {
        try {
            _itemsList.value?.let{
                val a = change.document.toObject(ContentItem::class.java)
                a.contentId = change.document.id
                _itemsList.value!!.add(change.newIndex,a)
                val index = _itemsList.value?.indexOf(a)
                itemAdded = WrapperChange(index!!,a)
                _itemsList.value = _itemsList.value
            }
        }catch (e: java.lang.Exception){
            Log.d("Tag.Err","Casting gone bad")
        }
    }

    fun onDocumentModified(change: DocumentChange) {
        try {
            _itemsList.value?.let{
                val a = change.document.toObject(ContentItem::class.java)
                a.contentId = change.document.id
                if (change.oldIndex == change.newIndex) {
                    // Item changed but remained in same position
                    _itemsList.value!![change.newIndex] = a
                    itemModified = WrapperChange(change.newIndex,a)
                    _itemsList.value = _itemsList.value
                } else {
                    // Item changed and changed position
                    onDocumentRemoved(change) //to remove at the old index position
                    onDocumentAdded(change) //to add at the new index position
                }
            }
        }catch (e: java.lang.Exception){
            Log.d("Tag.Err","Casting gone bad")
        }
    }

    fun onDocumentRemoved(change: DocumentChange) {
        try {
            _itemsList.value?.let{
                _itemsList.value!!.removeAt(change.oldIndex)
                itemRemoved = WrapperChange(change.oldIndex,change.document.toObject(ContentItem::class.java))
                _itemsList.value = _itemsList.value
            }
        }catch (e: java.lang.Exception){
            Log.d("Tag.Err","Casting gone bad")
        }
    }

    fun doneList(){
        itemAdded = null
        itemModified = null
        itemRemoved = null
    }

    fun onError(e: FirebaseFirestoreException) {}
    fun onDataChanged() {}

}
