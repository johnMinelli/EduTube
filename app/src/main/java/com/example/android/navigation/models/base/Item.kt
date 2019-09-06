package com.example.android.navigation.models.base

/*
 * Created by Christian Schabesberger on 11.02.17.
 *
 * Copyright (C) Christian Schabesberger 2017 <chris.schabesberger@mailbox.org>
 * InfoItem.java is part of NewPipe.
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

import java.io.Serializable

abstract class Item(
        private val contentId: String,
        private val infoType: String) : Serializable {

    fun compare(item: Item): Boolean{
        return (contentId == item.contentId)
    }

    override fun toString(): String {
        return javaClass.simpleName + "{contentId=\"" + contentId + "\",general.infoType=\"" + infoType+ "\"]"
    }


}
