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
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.navigation.MainFragmentDirections

import com.example.android.navigation.R
import com.example.android.navigation.databinding.FragmentCategoriesBinding
import com.example.android.navigation.fragments.base.BaseListFragment
import com.example.android.navigation.list.ItemListAdapter
import com.example.android.navigation.models.CategoryItem
import com.example.android.navigation.util.Constants
import com.example.android.navigation.viewModel.BaseViewModelFactory
import com.example.android.navigation.viewModel.CategoriesViewModel
import com.google.firebase.firestore.FirebaseFirestore

class CategoriesFragment : BaseListFragment<CategoryItem>() {

    lateinit var binding : FragmentCategoriesBinding
    private lateinit var viewModel: CategoriesViewModel
    private lateinit var viewModelFactory: BaseViewModelFactory
    private val fire: FirebaseFirestore by lazy { FirebaseFirestore.getInstance()}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate<FragmentCategoriesBinding>(inflater, R.layout.fragment_categories, container, false)
        // init recycler
        listItems = binding.itemsList
        // init adapter
        listAdapter = ItemListAdapter<CategoryItem>(Constants().CATEGORY_LIST_TYPE, activity as AppCompatActivity)
        // link
        initList()

        binding.lifecycleOwner = this
        viewModelFactory = BaseViewModelFactory(this, fire, PreferenceManager.getDefaultSharedPreferences(activity))
        viewModel = ViewModelProviders.of(this,viewModelFactory).get(CategoriesViewModel::class.java)
        recoverState()

        viewModel.readyState.observe(this, Observer { flag ->
            if(!flag) {
                startLoading(true)
            }else{
                startLoading(false)
            }
        })
        viewModel.itemsList.observe(this, Observer {
            it?.let {
                Log.d("Tag.CategoriesFragment","add "+viewModel.itemAdded.toString())
                if(viewModel.readyState.value!!) {
                    if (viewModel.itemReload != null) {
                        listAdapter.reloadItem(listAdapter.itemsList.indexOf(viewModel.itemReload!!),viewModel.itemReload!!)
                        viewModel.doneList()
                    }else if (viewModel.itemAdded != null) {
                        listAdapter.addItem(viewModel.itemAdded!!.index,viewModel.itemAdded!!.data)
                        viewModel.doneList()
                        hideEmptySpace()
                    }
                }
                if(it.size == 0) showEmptyState()
            }
        })
        return binding.root
    }

    fun recoverState(){
        if(viewModel.itemsList.value != null && listAdapter.itemsList != viewModel.itemsList.value){
            listAdapter.clearItemList()
            listAdapter.addItemsBottom(viewModel.itemsList.value!!)
            viewModel.doneList()
        }
    }

    override fun onItemSelected(selectedItem: CategoryItem) {
        //navigate
        this.findNavController().navigate(MainFragmentDirections.actionMainFragmentToCategoryFragment(selectedItem.title))
    }

    override fun onItemHeld(selectedItem: CategoryItem) {
        viewModel.toggleFollow(selectedItem)
    }

    override fun onItemDragged(selectedItem: CategoryItem, viewHolder: RecyclerView.ViewHolder?) {
        //do nothing
    }

    override fun loadMoreItems() {
        //won't be called
    }

    override fun hasMoreItems(): Boolean {
        return false
    }

}
