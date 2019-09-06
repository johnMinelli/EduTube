package com.example.android.navigation.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.android.navigation.MainFragmentDirections

import com.example.android.navigation.R
import com.example.android.navigation.databinding.FragmentCategoriesBinding
import com.example.android.navigation.databinding.LayoutStreamBinding
import com.example.android.navigation.fragments.base.BaseListFragment
import com.example.android.navigation.list.ItemListAdapter
import com.example.android.navigation.models.CategoryItem
import com.example.android.navigation.models.ContentItem
import com.example.android.navigation.util.Constants
import com.example.android.navigation.viewModel.CategoriesViewModel
import com.example.android.navigation.viewModel.CategoryViewModel
import com.example.android.navigation.viewModel.CategoryViewModelFactory
import com.google.firebase.firestore.FirebaseFirestore

class CategoryFragment : BaseListFragment<ContentItem>() {

    lateinit var binding : LayoutStreamBinding
    private lateinit var viewModel: CategoryViewModel
    private lateinit var viewModelFactory: CategoryViewModelFactory
    private val fire: FirebaseFirestore by lazy { FirebaseFirestore.getInstance()}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate<LayoutStreamBinding>(inflater, R.layout.layout_stream, container, false)
        // init recycler
        listItems = binding.itemsList
        // init adapter
        listAdapter = ItemListAdapter<ContentItem>(Constants().CONTENT_LIST_TYPE, activity as AppCompatActivity)
        // link
        initList()


        val arg = CategoryFragmentArgs.fromBundle(arguments!!).category
        viewModelFactory = CategoryViewModelFactory(fire, arg)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(CategoryViewModel::class.java)
        viewModel.readyState.observe(this, Observer { flag ->
            if(!flag) {
                startLoading(true)
            }else{
                startLoading(false)
            }
        })
        viewModel.itemsList.observe(viewLifecycleOwner, Observer {
            it?.let {
                Log.d("Tag.CreatorFragment", "add " + viewModel.itemAdded.toString())
                if(viewModel.readyState.value!!) {
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
            }
        })

        (activity as AppCompatActivity).supportActionBar!!.title = arg
        return binding.root
    }

    override fun onItemSelected(selectedItem: ContentItem) {
        this.findNavController().navigate(CategoryFragmentDirections.actionCategoryFragmentToStreamFragment(selectedItem))
    }

    override fun onItemHeld(selectedItem: ContentItem) {
        //dialog: creator info - bookmark - play
    }

    override fun onItemDragged(selectedItem: ContentItem, viewHolder: RecyclerView.ViewHolder?) {
        //do nothing
    }

    override fun loadMoreItems() {
        //won't be called
    }

    override fun hasMoreItems(): Boolean {
        return false
    }

}
