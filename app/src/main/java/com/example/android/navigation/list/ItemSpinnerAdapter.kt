package com.example.android.navigation.list

import android.content.Context
import android.content.res.Resources
import android.text.Layout
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.NonNull
import com.example.android.navigation.R
import kotlinx.android.synthetic.main.list_checkable_spinner_item.view.*
import androidx.recyclerview.widget.RecyclerView
import com.google.common.io.Resources.getResource


class ItemSpinnerAdapter<T> internal constructor(private val context: Context,
                                                 private val headerText: String,
                                                 private val all_items: List<SpinnerItem<T>>,
                                                 private val selected_items: MutableMap<String,T>) : BaseAdapter() {

    override fun getCount():Int{
        return all_items.size + 1
    }

    internal class SpinnerItem<T>(val txt: String, val item: T)

    override fun getItem(position: Int): Any? {
        return if (position < 1) {
            null
        } else {
            all_items[position - 1]
        }
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    @Suppress("unchecked_cast")
    @NonNull
    override fun getView(position: Int, convertView: View?, @NonNull parent: ViewGroup): View {
        var binding = convertView
        var holder: ViewHolder
        val colorText = context.getResources().getColor(context.getTheme().obtainStyledAttributes(IntArray(1,{R.attr.colorText})).getResourceId(0, 0))

        if (binding == null) {
            binding = LayoutInflater.from(context).inflate(R.layout.list_checkable_spinner_item, parent, false)
            holder = ViewHolder()
            holder.mTextView = binding.text_spinner
            holder.mCheckBox = binding.checkbox_spinner
            binding!!.setTag(holder)
        }else{
            holder = binding!!.tag as ItemSpinnerAdapter<T>.ViewHolder
        }

        if (position < 1) {
            holder.mCheckBox!!.setVisibility(View.GONE)
            holder.mTextView!!.setText(headerText)
            holder.mTextView!!.setTextColor(colorText)
        } else {
            val listPos = position - 1
            holder.mCheckBox!!.setVisibility(View.VISIBLE)
            holder.mTextView!!.setText(all_items[listPos].txt)
            holder.mTextView!!.setTextColor(colorText)

            val key = all_items[listPos].txt
            val item = all_items[listPos].item
            val isSel = selected_items.containsValue(item)

            holder.mCheckBox!!.setOnCheckedChangeListener(null)
            holder.mCheckBox!!.setChecked(isSel)

            holder.mCheckBox!!.setOnCheckedChangeListener {buttonView, isChecked ->
                if (isChecked) {
                    selected_items.put(key,item)
                } else {
                    selected_items.remove(key)
                }
            }
            holder.mTextView!!.setOnClickListener{view ->
                holder.mCheckBox!!.toggle()
            }
        }
        return binding
    }

    private inner class ViewHolder {
        var mTextView: TextView? = null
        var mCheckBox: CheckBox? = null
    }
}


class CustomAdapter<String>(context: Context, textViewResourceId: Int, val objects: MutableList<String>, val label: String) : ArrayAdapter<String>(context, textViewResourceId, objects) {


    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        var v: View? = null
        if (position < 1) {
            val tv = TextView(context)
            tv.visibility = View.GONE
            v = tv
        } else {
            v = super.getDropDownView(position, null, parent)
        }
        return v!!
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var v: View? = null
        if (position < 1) {
            val tv = TextView(context)
            tv.text = label as CharSequence
            tv.textSize = 18f
            tv.gravity = Gravity.CENTER_VERTICAL
            v = tv
        } else {
            v = super.getView(position, convertView, parent)
        }
        return v!!
    }

    override fun getCount():Int{
        return objects.size + 1
    }

    override fun getItem(position: Int): String? {
        return if (position < 1) {
            null
        } else {
            objects[position - 1]
        }
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

}