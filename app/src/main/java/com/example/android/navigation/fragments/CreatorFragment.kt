package com.example.android.navigation.fragments

import android.os.Bundle
import android.util.Log
import android.view.*
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
import com.example.android.navigation.databinding.LayoutStreamBinding
import com.example.android.navigation.fragments.base.BaseListFragment
import com.example.android.navigation.list.ItemListAdapter
import com.example.android.navigation.models.CategoryItem
import com.example.android.navigation.models.ContentItem
import com.example.android.navigation.util.Constants
import com.example.android.navigation.viewModel.*
import com.google.firebase.firestore.FirebaseFirestore

class CreatorFragment : BaseListFragment<ContentItem>() {

    lateinit var binding : LayoutStreamBinding
    private lateinit var viewModel: CreatorViewModel
    private lateinit var viewModelFactory: BaseViewModelFactory
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

        viewModelFactory = BaseViewModelFactory(this, fire, PreferenceManager.getDefaultSharedPreferences(activity))
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(CreatorViewModel::class.java)
        recoverState()

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

        return binding.root
    }

    fun recoverState(){
        if(viewModel.itemsList.value != null && listAdapter.itemsList != viewModel.itemsList.value){
            listAdapter.clearItemList()
            listAdapter.addItemsBottom(viewModel.itemsList.value!!)
            viewModel.doneList()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.creator_fragment_menu, menu)
    }

    override fun onItemSelected(selectedItem: ContentItem) {
        //edit
        this.findNavController().navigate(MainFragmentDirections.actionMainFragmentToEditFragment(selectedItem))
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
