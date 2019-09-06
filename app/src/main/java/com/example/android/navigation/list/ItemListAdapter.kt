package com.example.android.navigation.list

import android.app.Activity
import android.util.Log
import android.view.View
import android.view.ViewGroup


import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.navigation.list.holder.ItemHolder
import com.example.android.navigation.util.OnClickGesture

import java.text.DateFormat
import java.util.*
import com.example.android.navigation.list.holder.content.*
import com.example.android.navigation.list.holder.creator.*
import com.example.android.navigation.util.Constants
import com.example.android.navigation.util.FallbackViewHolder
import java.util.function.Consumer


//(1) fai arrivare il clicklistener inizializzato dal fragment e lo passi durante il binding onBindViewHolder
//allo specifico holder tramite una funzione creata nella classe dell'holder (+bello)
//(2) fai arrivare il clicklistener inizializzato dal fragment e lo passi al costruttore durante la creazione onCreateViewHolder
//dello specifico holder    <-- in questo caso non gli passo solo il clicklistener al costruttore ma l'intero builder specifico per il tipo di ItemList (+versatile/riutilizza l'adapter)

class ItemListAdapter<T>(val type : Int, activity: Activity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val itemBuilder: ItemBuilder<T>
    private val dateFormat: DateFormat
    val itemsList: ArrayList<T>

    private var showFooter = false
    private var useGridVariant = false
    private var header: View? = null
    private var footer: View? = null

    init {
        itemBuilder = ItemBuilder(activity)
        itemsList = ArrayList()
        dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault())
    }

    fun setSelectedListener(listener: OnClickGesture<T>) {
        itemBuilder.onItemSelectedListener = listener
    }

    fun unsetSelectedListener() {
        itemBuilder.onItemSelectedListener = null
    }

    fun addItem(index: Int, data: T){
        if (data != null) {
            when(index){
                0 -> addItemsTop(listOf(data))
                itemsList.size -> addItemsBottom(listOf(data))
                else ->{
                    itemsList.add(index,data)
                    notifyItemMoved(index + if (header != null) 1 else 0, sizeConsideringHeader()+if(footer != null && showFooter) 1 else 0)
                }
            }
        }
    }


    fun addItemsTop(data: List<T>?) {
        if (data != null) {
            //inserimento in testa
            data.forEach {
                itemsList.add(0, it)
            }
            notifyDataSetChanged()
        }
    }

    fun addItemsBottom(data: List<T>?) {
        if (data != null) {
            //inserimento in fondo
            val offsetStart = sizeConsideringHeader()
            itemsList.addAll(data)
            notifyItemRangeInserted(offsetStart, data.size)

            if (footer != null && showFooter) {
                val footerNow = sizeConsideringHeader()
                notifyItemMoved(offsetStart, footerNow)
            }
        }
    }

    fun removeItem(index: Int):Int {
        itemsList.removeAt(index)
        notifyItemRemoved(index + if (header != null) 1 else 0)
        return index
    }

    fun reloadItem(index: Int, data: T) {
        itemsList.removeAt(index)
        itemsList.add(index,data)
        notifyItemChanged(index + if (header != null) 1 else 0)
    }


    fun swapItems(fromAdapterPosition: Int, toAdapterPosition: Int): Boolean {
        val actualFrom = adapterOffsetWithoutHeader(fromAdapterPosition)
        val actualTo = adapterOffsetWithoutHeader(toAdapterPosition)

        if (actualFrom < 0 || actualTo < 0) return false
        if (actualFrom >= itemsList.size || actualTo >= itemsList.size) return false

        itemsList.add(actualTo, itemsList.removeAt(actualFrom))
        notifyItemMoved(fromAdapterPosition, toAdapterPosition)
        return true
    }

    fun clearItemList() {
        if (itemsList.isEmpty()) {
            return
        }
        itemsList.clear()
        notifyDataSetChanged()
    }

    fun setGridItemVariants(useGridVariant: Boolean) {
        this.useGridVariant = useGridVariant
    }

    fun setHeader(header: View) {
        val changed = header !== this.header
        this.header = header
        if (changed) notifyDataSetChanged()
    }

    fun setFooter(view: View) {
        this.footer = view
    }

    fun showFooter(show: Boolean) {
        if (show == showFooter) return
        showFooter = show
        if (show)
            notifyItemInserted(sizeConsideringHeader())
        else
            notifyItemRemoved(sizeConsideringHeader())
    }

    private fun adapterOffsetWithoutHeader(offset: Int): Int {
        return offset - if (header != null) 1 else 0
    }

    private fun sizeConsideringHeader(): Int {
        return itemsList.size + if (header != null) 1 else 0
    }

    override fun getItemCount(): Int {
        var count = itemsList.size
        if (header != null) count++
        if (footer != null && showFooter) count++

        return count
    }

    fun getListItems(): ArrayList<T> {
        return itemsList
    }

    override fun getItemViewType(position: Int): Int {
        var pos = position

        if (header != null && pos == 0) {
            return Constants().HEADER_LIST_TYPE
        } else if (header != null) {
            pos--
        }
        if (footer != null && pos == itemsList.size && showFooter) {
            return Constants().FOOTER_LIST_TYPE
        }
        return type
    }

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): RecyclerView.ViewHolder {
        return when(type){
            Constants().CONTENT_LIST_TYPE -> {if(useGridVariant) ContentGridItemListHolder(itemBuilder, parent)
                                            else ContentItemListHolder(itemBuilder, parent)}
            Constants().CREATOR_LIST_TYPE -> {if(useGridVariant) CreatorGridItemListHolder(itemBuilder, parent) //still TODO
                                            else CreatorItemListHolder(itemBuilder, parent)}
            Constants().COMMENT_LIST_TYPE -> {if(useGridVariant)ContentItemListHolder(itemBuilder, parent)      //still TODO
                                            else ContentItemListHolder(itemBuilder, parent)}
            Constants().CATEGORY_LIST_TYPE -> {CategoryItemListHolder(itemBuilder, parent)}
            else -> {
                Log.e("Tag."+this.toString(), "No view type has been considered for holder: [$type]")
                return FallbackViewHolder(View(parent.context))
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var position = position
        if (holder is ItemHolder<*>) {
            // If header isn't null, offset the items by -1
            if (header != null) position--
            holder.updateFromItem(itemsList[position], dateFormat)
        } else if (position == 0 && header != null){
            //set the holder.view = header
        } else if (position == sizeConsideringHeader() && footer != null && showFooter){
            //set the holder.view = footer
        }
    }

    fun setCustomSpanSizeLookup(spanCount: Int): GridLayoutManager.SpanSizeLookup {
        return object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val type = getItemViewType(position)
                return if (type == Constants().HEADER_LIST_TYPE || type == Constants().FOOTER_LIST_TYPE) spanCount else 1
            }
        }
    }
}
