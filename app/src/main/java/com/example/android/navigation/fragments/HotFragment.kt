package com.example.android.navigation.fragments

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.example.android.navigation.fragments.base.BaseListFragment
import com.example.android.navigation.list.ItemListAdapter
import com.example.android.navigation.models.ContentItem
import com.example.android.navigation.util.Constants
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.example.android.navigation.MainFragmentDirections
import com.example.android.navigation.R
import com.example.android.navigation.databinding.FragmentHotBinding
import com.example.android.navigation.util.Globals
import com.example.android.navigation.util.ImageDisplayConstants
import com.example.android.navigation.viewModel.BaseViewModelFactory
import com.example.android.navigation.viewModel.HotViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader


class HotFragment : BaseListFragment<ContentItem>() {

    lateinit var binding : FragmentHotBinding
    private lateinit var viewModel: HotViewModel
    private lateinit var viewModelFactory: BaseViewModelFactory
    private val fire: FirebaseFirestore by lazy { FirebaseFirestore.getInstance()}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate<FragmentHotBinding>(inflater, R.layout.fragment_hot, container, false)
        // init recycler
        listItems = binding.itemsList
        // init adapter
        listAdapter = ItemListAdapter<ContentItem>(Constants().CONTENT_LIST_TYPE, activity as AppCompatActivity)
        // link
        initList()
        listItems.setNestedScrollingEnabled(false)

        startLoading(true)
        viewModelFactory = BaseViewModelFactory(this, fire,PreferenceManager.getDefaultSharedPreferences(activity))
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(HotViewModel::class.java)
        binding.dataHotViewModel = viewModel
        recoverState()

        viewModel.readyState.observe(this, Observer { flag ->
            if(!flag) {
                startLoading(true)
            }else{
                try {
                    hardInflate()
                    startLoading(false)
                }catch(e: Exception){
                    showError("Error",false)
                }
            }
        })
        viewModel.itemsList.observe(viewLifecycleOwner, Observer {
            it?.let {
                Log.d("Tag.HotFragment", "add " + viewModel.itemAdded.toString())
                if (viewModel.itemAdded != null) {
                    listAdapter.addItem(viewModel.itemAdded!!.index, viewModel.itemAdded!!.data)
                    hideEmptySpace()
                }else if (viewModel.itemModified != null)
                    listAdapter.reloadItem(viewModel.itemModified!!.index, viewModel.itemModified!!.data)
                else if (viewModel.itemRemoved != null)
                    listAdapter.removeItem(viewModel.itemRemoved!!.index)
                else {
                    listAdapter.clearItemList()
                    listAdapter.addItemsBottom(it)
                }
                viewModel.doneList()
            }
            if (it.size == 0) showEmptyState()
        })
        viewModel.navigateToCategory.observe(viewLifecycleOwner, Observer {
            it?.let {
                viewModel.doneGoToCategory()
                this.findNavController().navigate(MainFragmentDirections.actionMainFragmentToCategoryFragment(it.title))
            }
        })
        viewModel.navigateToContent.observe(viewLifecycleOwner, Observer {
            it?.let {
                viewModel.doneGoToContent()
                this.findNavController().navigate(MainFragmentDirections.actionMainFragmentToStreamFragment(it))
            }
        })

        return binding.root
    }

    fun hardInflate(){
        inflateImage(binding.itemThumbnailView1, viewModel.catList[0].thumbnailUrl, true)
        inflateImage(binding.itemThumbnailView2, viewModel.catList[1].thumbnailUrl, true)
        inflateImage(binding.itemThumbnailView3, viewModel.catList[2].thumbnailUrl, true)
        inflateImage(binding.itemThumbnailView4, viewModel.catList[3].thumbnailUrl, true)
        inflateImage(binding.channelBannerImage, viewModel.topItem.thumbnailUrl)
        binding.additionalDetailsTopContent.text = viewModel.topItem.creator+" • "+viewModel.topItem.uploader
        binding.titleTopContent.text = viewModel.topItem.title
        binding.itemTitleView1.text = viewModel.catList[0].title
        binding.itemAdditionalDetails1.text = viewModel.catList[0].wrap
        binding.itemTitleView2.text = viewModel.catList[1].title
        binding.itemAdditionalDetails2.text = viewModel.catList[1].wrap
        binding.itemTitleView3.text = viewModel.catList[2].title
        binding.itemAdditionalDetails3.text = viewModel.catList[2].wrap
        binding.itemTitleView4.text = viewModel.catList[3].title
        binding.itemAdditionalDetails4.text = viewModel.catList[3].wrap
    }

    fun inflateImage(view: ImageView, url: String?,needRepaint: Boolean = false){
        url?.let{
            val imageLoader = ImageLoader.getInstance()
            imageLoader.displayImage(url, view, ImageDisplayConstants.DISPLAY_BANNER_OPTIONS)
            val a = PreferenceManager.getDefaultSharedPreferences(context).getString(Constants().KEY_THEME, Constants().LIGHT)
            if(needRepaint && !a.equals(Constants().LIGHT)){
                val color = Color.WHITE
                val mode = PorterDuff.Mode.SRC_ATOP
                view.setColorFilter(color, mode)
            }
        }
    }

    fun recoverState(){
        if(viewModel.itemsList.value != null && listAdapter.itemsList != viewModel.itemsList.value){
            listAdapter.clearItemList()
            listAdapter.addItemsBottom(viewModel.itemsList.value!!)
            viewModel.doneList()
        }
    }

    override fun onItemSelected(selectedItem: ContentItem) {
        viewModel.goToContent(selectedItem)
    }

    override fun onItemHeld(selectedItem: ContentItem) {
        //dialog play, info, bookmark
    }

    override fun onItemDragged(selectedItem: ContentItem, viewHolder: RecyclerView.ViewHolder?) {
        //nothing to do
    }

    override fun loadMoreItems() {
        //never called
    }

    override fun hasMoreItems(): Boolean {
        //nothing to do
        return false
    }

}
