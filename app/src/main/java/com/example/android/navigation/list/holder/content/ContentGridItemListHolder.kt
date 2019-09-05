package com.example.android.navigation.list.holder.content

import android.view.ViewGroup

import com.example.android.navigation.R
import com.example.android.navigation.list.ItemBuilder
import com.example.android.navigation.models.ContentItem


class ContentGridItemListHolder(infoItemBuilder: ItemBuilder<*>,
                                parent: ViewGroup) : ContentItemListHolder(
        infoItemBuilder as ItemBuilder<ContentItem>, R.layout.list_stream_grid_item, parent)
