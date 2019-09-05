package com.example.android.navigation.list.holder.creator

import android.view.ViewGroup

import com.example.android.navigation.R
import com.example.android.navigation.list.ItemBuilder
import com.example.android.navigation.models.CreatorItem


class CreatorGridItemListHolder(infoItemBuilder: ItemBuilder<*>,
                                parent: ViewGroup) : CreatorItemListHolder(
        infoItemBuilder as ItemBuilder<CreatorItem>, R.layout.list_stream_grid_item, parent)
