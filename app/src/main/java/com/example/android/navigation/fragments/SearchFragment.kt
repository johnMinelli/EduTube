package com.example.android.navigation.fragments

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.TooltipCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.android.navigation.databinding.FragmentSearchBinding
import com.example.android.navigation.fragments.base.BaseListFragment
import com.example.android.navigation.list.ItemListAdapter
import com.example.android.navigation.models.ContentItem
import com.example.android.navigation.util.Constants
import com.example.android.navigation.viewModel.SearchViewModel
import com.example.android.navigation.viewModel.SearchViewModelFactory
import androidx.lifecycle.Observer
import com.example.android.navigation.MainFragmentDirections
import com.example.android.navigation.R
import com.google.firebase.firestore.FirebaseFirestore


class SearchFragment : BaseListFragment<ContentItem>() {

    private lateinit var searchToolbarContainer: View
    private lateinit var searchEditText: EditText
    private lateinit var searchClear: View
    lateinit var binding : FragmentSearchBinding
    private lateinit var viewModel: SearchViewModel
    private lateinit var viewModelFactory: SearchViewModelFactory
    private val fire: FirebaseFirestore by lazy { FirebaseFirestore.getInstance()}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {


        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate<FragmentSearchBinding>(inflater, R.layout.fragment_search, container, false)
        // init recycler
        listItems = binding.itemsList
        // init adapter
        listAdapter = ItemListAdapter(Constants().CONTENT_LIST_TYPE, activity as AppCompatActivity)
        // link
        initList()

        searchToolbarContainer = activity!!.findViewById(R.id.toolbar_search_container)
        searchEditText = searchToolbarContainer.findViewById(R.id.toolbar_search_edit_text)
        searchClear = searchToolbarContainer.findViewById(R.id.toolbar_search_clear)
        initSearch()

        viewModelFactory = SearchViewModelFactory(fire)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(SearchViewModel::class.java)

        viewModel.searchingState.observe(this, Observer { flag ->
            if(flag) {
                listAdapter.clearItemList()
                startLoading(true)
                hideKeyboardSearch()
            }else{
                startLoading(false)
                if(viewModel.itemsList.value?.size == 0) showEmptyState()
            }
        })
        viewModel.itemsList.observe(viewLifecycleOwner, Observer {
            it?.let {
                if(viewModel.readyState.value?:false){
                    Log.d("Tag.CategoriesFragment","add "+viewModel.itemAdded.toString())
                    if (viewModel.itemAdded != null) {
                        listAdapter.addItem(viewModel.itemAdded!!.index,viewModel.itemAdded!!.data)
                        viewModel.doneList()
                        hideEmptySpace()
                    }
                    if(it.size == 0) showEmptyState()
                }
            }
        })
        return binding.root
    }

    private fun initSearch() {
        //ANIMATIONS
        searchEditText.setText("")
        searchToolbarContainer.setTranslationX(100f)
        searchToolbarContainer.setAlpha(0f)
        searchToolbarContainer.setVisibility(View.VISIBLE)
        searchToolbarContainer.animate()
                .translationX(0f)
                .alpha(1f)
                .setDuration(200)
                .setInterpolator(DecelerateInterpolator()).start()
        //LISTENERS
        searchClear.setOnClickListener { v ->
            if (TextUtils.isEmpty(searchEditText.text)) {
                hideKeyboardSearch()
                this.findNavController().navigate(SearchFragmentDirections.actionSearchFragmentToMainFragment())
            }
            searchEditText.setText("")
            showKeyboardSearch()
        }

        TooltipCompat.setTooltipText(searchClear, getString(R.string.clear))

        searchEditText.setOnClickListener { v -> showKeyboardSearch() }

        searchEditText.setOnFocusChangeListener { v: View, hasFocus: Boolean ->
            if (!hasFocus) {
                hideKeyboardSearch()
            }
        }
        searchEditText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_PREVIOUS) {
                hideKeyboardSearch()
            } else if ((event?.getKeyCode() == KeyEvent.KEYCODE_ENTER || event?.getAction() == EditorInfo.IME_ACTION_SEARCH
                            || actionId == EditorInfo.IME_ACTION_SEARCH)) {
                viewModel.search(searchEditText.text.toString())
                return@setOnEditorActionListener true
            }
            false
        }
    }

    private fun showKeyboardSearch() {
        if (searchEditText?.requestFocus()) {
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(searchEditText, InputMethodManager.SHOW_FORCED)
        }
    }

    private fun hideKeyboardSearch() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchEditText.windowToken, InputMethodManager.RESULT_UNCHANGED_SHOWN)
        searchEditText?.clearFocus()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        Log.d("MainFragment.Tag", "onCreateOptionsMenu() called with: menu = [$menu], inflater = [$inflater]")
        inflater.inflate(R.menu.search_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        item?.let{
            item.setChecked(true)
            (when (item.itemId) {
                R.id.menu_filter_all -> Constants().FILTER_ALL
                R.id.menu_filter_content -> Constants().FILTER_CONTENT
                R.id.menu_filter_creator -> Constants().FILTER_CREATOR
                R.id.menu_filter_uploader -> Constants().FILTER_UPLOADER
                else -> null
            })?.let{ viewModel.applyFilter(it) }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onItemSelected(selectedItem: ContentItem) {
        Log.d("Tag.SearchFragment",selectedItem.title)
        this.findNavController().navigate(SearchFragmentDirections.actionSearchFragmentToStreamFragment(selectedItem))
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
