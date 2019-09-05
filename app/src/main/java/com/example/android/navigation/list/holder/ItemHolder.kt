package com.example.android.navigation.list.holder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.recyclerview.widget.RecyclerView
import com.example.android.navigation.list.ItemBuilder

import java.text.DateFormat

/*
 * Created by Christian Schabesberger on 12.02.17.
 *
 * Copyright (C) Christian Schabesberger 2016 <chris.schabesberger@mailbox.org>
 * InfoItemHolder.java is part of NewPipe.
 *
 * NewPipe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe.  If not, see <http://www.gnu.org/licenses/>.
 */

abstract class ItemHolder<T>(protected val itemBuilder: ItemBuilder<T>, val layoutId: Int, val parent: ViewGroup,
         val binding : View = LayoutInflater.from(itemBuilder.context).inflate(layoutId, parent, false))
    : RecyclerView.ViewHolder(binding) {

    abstract fun updateFromItem(item: Any?, dateFormat: DateFormat)
}
