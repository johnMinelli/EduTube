package com.example.android.navigation.list.holder.creator


import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil


import com.example.android.navigation.R
import com.example.android.navigation.databinding.ListStreamItemBinding
import com.example.android.navigation.list.ItemBuilder
import com.example.android.navigation.list.holder.ItemHolder
import com.example.android.navigation.models.CreatorItem
import com.example.android.navigation.util.ImageDisplayConstants
import kotlinx.android.synthetic.main.list_stream_item.view.*

import java.text.DateFormat

/*
 * Created by Christian Schabesberger on 01.08.16.
 * <p>
 * Copyright (C) Christian Schabesberger 2016 <chris.schabesberger@mailbox.org>
 * StreamInfoItemHolder.java is part of NewPipe.
 * <p>
 * NewPipe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * NewPipe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with NewPipe.  If not, see <http://www.gnu.org/licenses/>.
 */

open class CreatorItemListHolder internal constructor(itemBuilder: ItemBuilder<CreatorItem>, layoutId: Int, parent: ViewGroup) : ItemHolder<CreatorItem>(itemBuilder, layoutId, parent) {

    val itemThumbnailView: ImageView
    val itemVideoTitleView: TextView
    val itemUploaderView: TextView
    val itemDurationView: TextView
    @Nullable
    val itemAdditionalDetails: TextView?

    constructor(itemBuilder: ItemBuilder<*>, parent: ViewGroup) : this(itemBuilder as ItemBuilder<CreatorItem>, R.layout.list_stream_item, parent) {}

    init {
        val binding = DataBindingUtil.findBinding<ListStreamItemBinding>(itemView)!!
        itemThumbnailView = binding.itemThumbnailView
        itemVideoTitleView = binding.itemVideoTitleView
        itemUploaderView = binding.itemUploaderView
        itemDurationView = binding.itemDurationView
        itemAdditionalDetails = binding.itemAdditionalDetails
    }

    override fun updateFromItem(localItem: Any?, dateFormat: DateFormat) {
        if (localItem !is CreatorItem) return
        val item = localItem as CreatorItem

        itemVideoTitleView.setText(item.title)
        itemUploaderView.setText(item.uploader)

        if (!item.viewTime.equals("") && item.viewTime != null) {
            itemDurationView.setText(item.viewTime)
            itemDurationView.setBackgroundColor(ContextCompat.getColor(itemBuilder.context,
                    R.color.duration_background_color))
            itemDurationView.visibility = View.VISIBLE
        } else {
            itemDurationView.visibility = View.GONE
        }

        if (itemAdditionalDetails != null) {
            itemAdditionalDetails.text = item.uploader+" "+item.readTime
        }

        // Default thumbnail is shown on error, while loading and if the url is empty
        item.thumbnailUrl?.let{
            itemBuilder.displayImage(it!!, itemThumbnailView,ImageDisplayConstants.DISPLAY_THUMBNAIL_OPTIONS)
        }

        itemView.setOnClickListener { view ->
            if (itemBuilder.onItemSelectedListener != null) {
                itemBuilder.onItemSelectedListener!!.selected(item)
            }
        }

        itemView.isLongClickable = true
        itemView.setOnLongClickListener { view ->
            if (itemBuilder.onItemSelectedListener != null) {
                itemBuilder.onItemSelectedListener!!.held(item)
            }
            true
        }
    }
}
