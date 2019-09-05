package com.example.android.navigation.fragments.base

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.annotation.NonNull
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.navigation.R
import com.example.android.navigation.list.ItemListAdapter
import com.example.android.navigation.util.AnimationUtils.animateView
import com.example.android.navigation.util.Constants
import com.example.android.navigation.util.Globals
import com.example.android.navigation.util.OnClickGesture
import com.example.android.navigation.util.OnScrollBelowItemsListener

import java.util.Queue

abstract class BaseListFragment<T>: BaseStateFragment() {
    //////////////////////////////////////////////////////////////////////////
    // Views
    //////////////////////////////////////////////////////////////////////////

    protected lateinit var listAdapter: ItemListAdapter<T>
    protected lateinit var listItems : RecyclerView
    private var updateFlags = 0

    //////////////////////////////////////////////////////////////////////////
    // Init
    //////////////////////////////////////////////////////////////////////////

    protected val listLayoutManager:RecyclerView.LayoutManager
        get() {
            return LinearLayoutManager(activity)
        }

    protected val gridLayoutManager:RecyclerView.LayoutManager
        get() {
            val resources = activity!!.getResources()
            var width = resources.getDimensionPixelSize(R.dimen.video_item_grid_thumbnail_image_width)
            width += (24 * resources.getDisplayMetrics().density).toInt()
            val spanCount = Math.floor(resources.getDisplayMetrics().widthPixels / width.toDouble()).toInt()
            val lm = GridLayoutManager(activity, spanCount)
            lm.setSpanSizeLookup(listAdapter.setCustomSpanSizeLookup(spanCount))
            return lm
        }

    protected val isGridLayout:Boolean
        get() {
            val list_mode = PreferenceManager.getDefaultSharedPreferences(activity).getString(Constants().KEY_VIEW_MODE, Globals().VIEW_MODE)
            if ("auto" == list_mode){
                val configuration = getResources().getConfiguration()
                return (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE && configuration.isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_LARGE))
            }else{
                return "grid" == list_mode
            }
        }

    //////////////////////////////////////////////////////////////////////////
    // LifeCycle
    //////////////////////////////////////////////////////////////////////////

    public override fun onAttach(context:Context) {
        super.onAttach(context)
    }

    public override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    public override fun onDestroy() {
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()

        if (updateFlags != 0)
        {
            if ((updateFlags and LIST_MODE_UPDATE_FLAG) != 0)
            {
                val useGrid = isGridLayout
                listItems.setLayoutManager(if (useGrid) gridLayoutManager else listLayoutManager)
                listAdapter.setGridItemVariants(useGrid)
                listAdapter.notifyDataSetChanged()
            }
            updateFlags = 0
        }
    }

    fun generateSuffix():String {
        // Naive solution, but it's good for now (the items don't change)
        return "." + listAdapter.getListItems().size + ".list"
    }

    fun writeTo(objectsToSave:Queue<Any>) {
        objectsToSave.add(listAdapter.getListItems())
    }

    @Throws(Exception::class)
    fun readFrom(@NonNull savedObjects:Queue<Any>) {
//        listAdapter!!.getListItems().clear()
//        listAdapter!!.getListItems().addAll(savedObjects.poll() as List<InfoItem>)
    }

    protected open fun initList() {
        val useGrid = isGridLayout
        //listItems = rootView.findViewById<RecyclerView>(R.id.items_list) <-- WRONG init nel fragment specifico
        listItems.setLayoutManager(if (useGrid) gridLayoutManager else listLayoutManager)
        //listAdapter = ItemListAdapter(1,activity as AppCompatActivity) <-- WRONG init nel fragment specifico
        listAdapter.setGridItemVariants(useGrid)
        listItems.setAdapter(listAdapter)
        initListeners()
    }

    abstract fun onItemSelected(selectedItem: T)
    abstract fun onItemHeld(selectedItem: T)
    abstract fun onItemDragged(selectedItem: T, viewHolder: RecyclerView.ViewHolder?)

    protected fun initListeners() {
        //this handle all types comments, channel, playlist, results
        listAdapter.setSelectedListener(object: OnClickGesture<T>() {
            override fun selected(selectedItem:T) {
                Log.d("Tag."+this.toString(), "listner click called with: selectedItem = [" + selectedItem + "]")
                onItemSelected(selectedItem)
            }
            override fun held(selectedItem:T) {
                onItemHeld(selectedItem)
                //showStreamDialog(selectedItem)
            }
            override fun drag(selectedItem: T, viewHolder: RecyclerView.ViewHolder) {
                onItemDragged(selectedItem,viewHolder)
            }
        })

        listItems.clearOnScrollListeners()
        listItems.addOnScrollListener(object: OnScrollBelowItemsListener() {
            override fun onScrolledDown(recyclerView: RecyclerView) {
                onScrollToBottom()
            }
        })
//        no need of this
//        ItemTouchHelper(object : ItemTouchHelper.Callback() {
//            override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
//                return getSuggestionMovementFlags(recyclerView, viewHolder)
//            }
//
//            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
//                                viewHolder1: RecyclerView.ViewHolder): Boolean {
//                return false
//            }
//
//            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, i: Int) {
//                onSuggestionItemSwiped(viewHolder, i)
//            }
//        }).attachToRecyclerView(listItems)
    }

    protected fun onScrollToBottom() {
        if (hasMoreItems() && !isLoading.get()){
            loadMoreItems()
        }
    }

    //////////////////////////////////////////////////////////////////////////
    // Load and handle
    //////////////////////////////////////////////////////////////////////////

    protected abstract fun loadMoreItems()

    protected abstract fun hasMoreItems():Boolean

    //////////////////////////////////////////////////////////////////////////
    // Contract
    //////////////////////////////////////////////////////////////////////////

    public override fun showLoading() {
        super.showLoading()
        animateView(listItems, false, 400)
    }

    public override fun hideLoading() {
        super.hideLoading()
        animateView(listItems, true, 300)
    }

    public override fun showError(message:String, showRetryButton:Boolean) {
        super.showError(message, showRetryButton)
        animateView(listItems, false, 200)
    }

    public override fun showEmptyState() {
        super.showEmptyState()
    }

    companion object {
        private val LIST_MODE_UPDATE_FLAG = 0x32
    }
}
