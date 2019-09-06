package com.example.android.navigation.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.navigation.MainFragmentDirections

import com.example.android.navigation.R
import com.example.android.navigation.databinding.FragmentAdminBinding
import com.example.android.navigation.databinding.FragmentCategoriesBinding
import com.example.android.navigation.databinding.LayoutStreamBinding
import com.example.android.navigation.fragments.base.BaseListFragment
import com.example.android.navigation.list.ItemListAdapter
import com.example.android.navigation.models.CategoryItem
import com.example.android.navigation.models.ContentItem
import com.example.android.navigation.util.Constants
import com.example.android.navigation.viewModel.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId

class AdminFragment : Fragment() {

    lateinit var binding : FragmentAdminBinding
    private lateinit var viewModel: AdminViewModel
    private lateinit var viewModelFactory: BaseViewModelFactory
    private val fire: FirebaseFirestore by lazy { FirebaseFirestore.getInstance()}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate<FragmentAdminBinding>(inflater, R.layout.fragment_admin, container, false)

        viewModelFactory = BaseViewModelFactory(this, fire, PreferenceManager.getDefaultSharedPreferences(activity))
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(AdminViewModel::class.java)

        binding.notButton.setOnClickListener{
            val title = binding.notTitle.text.toString()
            val text = binding.notText.text.toString()
            if(!title.isNullOrEmpty() && !text.isNullOrEmpty()){
                viewModel.send(title, text)
            }
        }

        binding.tokButton.setOnClickListener{
            FirebaseInstanceId.getInstance().instanceId
                    .addOnCompleteListener {
                        if (!it.isSuccessful) {
                            Log.w("Tag.AdminFragment", "getInstanceId failed", it.exception)
                            return@addOnCompleteListener
                        }
                        val token = it.result?.token
                        binding.tokText.text = token
                        Log.w("Tag.AdminFragment", "tokenFcm "+token)
                    }
        }

        viewModel.readyState.observe(this, Observer { flag ->
            if(!flag) {
//
            }else{
//
            }
        })
        return binding.root
    }


}
