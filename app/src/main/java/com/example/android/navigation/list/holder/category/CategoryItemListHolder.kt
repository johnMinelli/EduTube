package com.example.android.navigation.list.holder.content


import android.graphics.Color
import android.graphics.PorterDuff
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.appcompat.content.res.AppCompatResources
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil


import com.example.android.navigation.R
import com.example.android.navigation.databinding.ListStreamItemBinding
import com.example.android.navigation.list.ItemBuilder
import com.example.android.navigation.list.holder.ItemHolder
import com.example.android.navigation.models.CategoryItem
import com.example.android.navigation.models.ContentItem
import com.example.android.navigation.util.Constants
import com.example.android.navigation.util.Globals
import com.example.android.navigation.util.ImageDisplayConstants
import kotlinx.android.synthetic.main.list_category_item.view.*
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

open class CategoryItemListHolder internal constructor(itemBuilder: ItemBuilder<CategoryItem>, layoutId: Int, parent: ViewGroup) : ItemHolder<CategoryItem>(itemBuilder, layoutId, parent) {

    val itemThumbnail: CardView
    val itemThumbnailView: ImageView
    val itemVideoTitleView: TextView
    val itemFlagIconView: ImageView

    constructor(itemBuilder: ItemBuilder<*>, parent: ViewGroup) : this(itemBuilder as ItemBuilder<CategoryItem>, R.layout.list_category_item, parent) {}

    init {
        itemThumbnail = binding.catIcon
        itemThumbnailView = binding.catIconView
        itemVideoTitleView = binding.catTitle
        itemFlagIconView = binding.catFlag
    }

    override fun updateFromItem(localItem: Any?, dateFormat: DateFormat) {
        if (localItem !is CategoryItem) return
        val item = localItem as CategoryItem

        itemVideoTitleView.text = item.title
        val attrId: Int = if(item.follow) R.attr.ic_star else R.attr.ic_star_border
        val iconId: Int = itemBuilder.context.getTheme().obtainStyledAttributes(IntArray(1,{attrId})).getResourceId(0, 0)
        itemFlagIconView.setImageDrawable(AppCompatResources.getDrawable(itemBuilder.context, iconId))

        itemThumbnail.setCardBackgroundColor(Color.parseColor(if(item.color.isEmpty())"#FFFFFFFF" else item.color))
        // Default thumbnail is shown on error, while loading and if the url is empty
        item.thumbnailUrl?.let{
            itemBuilder.displayImage(it!!, itemThumbnailView,ImageDisplayConstants.DISPLAY_THUMBNAIL_OPTIONS)
            if(!PreferenceManager.getDefaultSharedPreferences(itemBuilder.context).getString(Constants().KEY_THEME, Constants().LIGHT).equals(Constants().LIGHT)){
                val color = Color.WHITE
                val mode = PorterDuff.Mode.SRC_ATOP
                itemThumbnailView.setColorFilter(color, mode)
            }
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
