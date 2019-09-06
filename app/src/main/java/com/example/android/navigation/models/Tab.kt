package com.example.android.navigation.models

import android.content.Context
import androidx.fragment.app.Fragment
import com.example.android.navigation.R
import com.example.android.navigation.fragments.*
import com.example.android.navigation.util.Constants

abstract class Tab {

    abstract val tabId: Int

    /**
     * Return a instance of the fragment that this tab represent.
     */
    abstract val fragment: Fragment

    abstract fun getTabName(context: Context): String
    abstract fun getTabIconRes(context: Context): Int
    override fun equals(obj: Any?): Boolean {
        return (obj is Tab && obj.javaClass == this.javaClass
                && obj.tabId == this.tabId)
    }

    //////////////////////////////////////////////////////////////////////////
    // Implementations
    //////////////////////////////////////////////////////////////////////////



    class EmptyTab:Tab() {

        override val tabId:Int
            get() = ID

        override val fragment: EmptyFragment
            get() = EmptyFragment()

        override fun getTabName(context:Context):String {
            return "NewFrag" //context.getString(R.string.blank_page_summary);
        }

        override fun getTabIconRes(context:Context):Int {
            return context.getTheme().obtainStyledAttributes(IntArray(1,{R.attr.ic_blank_page})).getResourceId(0, 0)
        }

        companion object {
            val ID = 0
        }
    }

    class CategoriesTab:Tab() {

        override val tabId:Int
            get() = ID

        override val fragment:CategoriesFragment
            get() = CategoriesFragment()

        override fun getTabName(context:Context):String {
            return context.getString(R.string.tab_subscriptions)
        }

        override fun getTabIconRes(context:Context):Int {
            return context.getTheme().obtainStyledAttributes(IntArray(1,{R.attr.ic_grid})).getResourceId(0, 0)
        }

        companion object {
            val ID = 1
        }

    }

    class HotTab:Tab() {

        override val tabId:Int
            get() = ID

        override val fragment:HotFragment
            get() = HotFragment()

        override fun getTabName(context:Context):String {
            return context.getString(R.string.fragment_whats_new)
        }

        override fun getTabIconRes(context:Context):Int {
            return context.getTheme().obtainStyledAttributes(IntArray(1,{R.attr.rss})).getResourceId(0, 0)
        }

        companion object {
            val ID = 2
        }
    }

    class BookmarksTab:Tab() {

        override val tabId:Int
            get() = ID

        override val fragment: BookmarksFragment
            get() = BookmarksFragment()

        override fun getTabName(context:Context):String {
            return context.getString(R.string.tab_bookmarks)
        }

        override fun getTabIconRes(context:Context):Int {
            return context.getTheme().obtainStyledAttributes(IntArray(1,{R.attr.ic_bookmark})).getResourceId(0, 0)
        }

        companion object {
            val ID = 3
        }
    }

    class CreatorTab:Tab() {

        override val tabId:Int
            get() = ID

        override val fragment: CreatorFragment
            get() = CreatorFragment()

        override fun getTabName(context:Context):String {
            return context.getString(R.string.tab_edit)
        }

        override fun getTabIconRes(context:Context):Int {
            return context.getTheme().obtainStyledAttributes(IntArray(1,{R.attr.ic_edit})).getResourceId(0, 0)
        }

        companion object {
            val ID = Constants().CREATOR_TAB_ID
        }
    }
}
