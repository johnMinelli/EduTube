package com.example.android.navigation.util

import androidx.recyclerview.widget.RecyclerView

abstract class OnClickGesture<T> {

    abstract fun selected(selectedItem: T)

    abstract fun held(selectedItem: T) // Optional gesture

    abstract fun drag(selectedItem: T, viewHolder: RecyclerView.ViewHolder) // Optional gesture
}
