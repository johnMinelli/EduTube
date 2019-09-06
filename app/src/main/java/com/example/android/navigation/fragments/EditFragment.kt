package com.example.android.navigation.fragments

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.preference.Preference
import android.text.Html
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders

import com.example.android.navigation.R
import com.example.android.navigation.databinding.FragmentEditContentBinding
import com.example.android.navigation.models.ContentItem
import com.example.android.navigation.util.Constants
import com.example.android.navigation.viewModel.EditViewModel
import com.example.android.navigation.viewModel.EditViewModelFactory
import org.wordpress.aztec.EditorActivity
import android.widget.Spinner
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.android.navigation.fragments.base.BaseStateFragment
import com.example.android.navigation.list.ItemSpinnerAdapter
import com.example.android.navigation.models.CategoryItem
import com.example.android.navigation.list.ItemSpinnerAdapter.SpinnerItem
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import java.lang.Exception
import android.widget.ArrayAdapter
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.navigation.list.CustomAdapter
import java.lang.RuntimeException


class EditFragment : BaseStateFragment() {

    lateinit var binding : FragmentEditContentBinding
    lateinit var viewModel: EditViewModel
    private lateinit var viewModelFactory: EditViewModelFactory
    private val fire: FirebaseFirestore by lazy { FirebaseFirestore.getInstance()}
    var content : ContentItem? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate<FragmentEditContentBinding>(inflater, R.layout.fragment_edit_content, container, false)
        // Get the argument
        arguments?.let{
            content = EditFragmentArgs.fromBundle(arguments!!).selectedItem
        }
        setHasOptionsMenu(true)

        viewModelFactory = EditViewModelFactory(fire, content)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(EditViewModel::class.java)
        binding.dataEditViewModel = viewModel
        binding.setLifecycleOwner(this)

        viewModel.readyState.observe(this, Observer { flag ->
            if(flag){
                initSpinnerWrap()
                hideLoading()
            }
        })
        viewModel.categoriesList.observe(this, Observer {
            it?.let {
                val spinner_items = ArrayList<SpinnerItem<String>>() //Array di spinnerItem con titolo aka chiave e refPath aka valore
                // fill the 'spinner_items' array with all items to show
                for (i in it.keys) {
                    spinner_items.add(SpinnerItem(i, it[i]!!))
                }
                // to start with any pre-selected, add them to the `selected_items` set
                val headerText = "Select at least one category"
                val adapter = ItemSpinnerAdapter(requireContext(), headerText, spinner_items, viewModel.selectedCategories)
                binding.contentCategory.adapter = adapter
            }
        })
        viewModel.navigateToEditor.observe(viewLifecycleOwner, Observer {
            it?.let {
                viewModel.doneGoToEditor()
                val intent : Intent = EditorActivity.newIntent(requireContext())
                intent.putExtra(Constants().TASK_REQUEST_EXTRA,it)
                startActivityForResult(intent, Constants().TASK_REQUEST)
            }
        })
        viewModel.doneSaveAndNavigateBack.observe(viewLifecycleOwner, Observer {
            it?.let {
                hideLoading()
                viewModel.doneGoBack()
                if(it)
                    this.findNavController().navigateUp()
                else
                    Snackbar.make(binding.editRoot, "Error saving the content: check internet connection", Snackbar.LENGTH_SHORT).show()
            }
        })
        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants().TASK_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                viewModel.returnFromEditor(data?.getStringExtra(Constants().TASK_REQUEST_EXTRA))
            }
        }
    }

    fun validateAndSave(){
        binding.apply {
            val title: String = contentTitle.text.toString()
            val videoUrl = contentUrl.text.toString()
            val text = contentText.text.toString().replace("\\<[^>]*>","").length
            val readTime = contentReadTime.text.toString()
            val cat = if(contentWrap.selectedItem != null) contentWrap.selectedItem.toString() else viewModel.wrap

            if (title.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Set a valid title", Toast.LENGTH_SHORT).show()
                return
            }
            if (videoUrl.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Set a valid url", Toast.LENGTH_SHORT).show()
                return
            }
            if (text<140) {
                Toast.makeText(requireContext(), "Enter a text at least of 140 characters", Toast.LENGTH_SHORT).show()
                return
            }
            try{
                if (readTime.isNullOrEmpty() || Integer.parseInt(readTime) < 1 || Integer.parseInt(readTime) > 500)throw Exception()
            }catch (e: Exception){
                Toast.makeText(requireContext(), "Set a valid time (min)", Toast.LENGTH_SHORT).show()
                return
            }
            if (cat.isNullOrEmpty() || viewModel.selectedCategories.size==0) {
                Toast.makeText(requireContext(), "Set a valid category", Toast.LENGTH_SHORT).show()
                return
            }
            //text and categories aready in the viewModel
            val uid = PreferenceManager.getDefaultSharedPreferences(requireContext()).getString(Constants().UID,"")
            val uname = PreferenceManager.getDefaultSharedPreferences(requireContext()).getString(Constants().UNAME,"")
            if(uid != null && uid != ""){
                try{
                    viewModel.save(title,videoUrl,readTime, uid, uname)
                    showLoading()
                }catch(e: RuntimeException){
                    hideLoading()
                    Toast.makeText(requireContext(), "Set a valid URL", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(requireContext(), "Account info error: try to relog in", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun initSpinnerWrap(){
        val adapter = CustomAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, viewModel.wrapList.keys.toMutableList(),viewModel.selectedItem!!.wrap)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.contentWrap.adapter = adapter
        binding.contentWrap.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(p0: AdapterView<*>?) {
                //nothing to do
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                binding.contentWrap.selectedItem?.let{
                    viewModel.changeWrap(binding.contentWrap.selectedItem.toString())
                }
            }
        }


    }


    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.edit_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if(item?.itemId == R.id.save_edit){
            validateAndSave()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
