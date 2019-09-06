package com.example.android.navigation


import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.android.navigation.databinding.FragmentMainBinding
import com.google.android.material.tabs.TabLayout
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.ViewModelProviders
import androidx.preference.PreferenceManager
import androidx.viewpager.widget.ViewPager
import com.example.android.navigation.fragments.EmptyFragment
import com.example.android.navigation.util.Constants
import com.example.android.navigation.viewModel.MainFViewModel
import com.example.android.navigation.viewModel.MainFViewModelFactory
import java.lang.Exception




class MainFragment : Fragment(), TabLayout.OnTabSelectedListener{

    private lateinit var viewModel: MainFViewModel
    private lateinit var viewModelFactory: MainFViewModelFactory
    private lateinit var binding: FragmentMainBinding
    private lateinit var viewPager: ViewPager
    private lateinit var pagerAdapter: SelectedTabsPagerAdapter
    private lateinit var tabLayout: TabLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false)
        binding.mainTabLayout
        viewModelFactory = MainFViewModelFactory(PreferenceManager.getDefaultSharedPreferences(activity))
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MainFViewModel::class.java)
        tabInit()

        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        Log.d("MainFragment.Tag", "onCreateOptionsMenu() called with: menu = [$menu], inflater = [$inflater]")
        if(viewModel.tabList[viewPager!!.currentItem].tabId != Constants().CREATOR_TAB_ID){
            inflater.inflate(R.menu.main_fragment_menu, menu)
        }
        inflater.inflate(R.menu.overflow_menu, menu)
//        val supportActionBar = (activity as AppCompatActivity).supportActionBar
//        if (supportActionBar != null) {
//            supportActionBar!!.setDDisplayHomeAsUpEnabled(false)
//        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //segue il behaviour del navigator
        return NavigationUI.onNavDestinationSelected(item,
                view!!.findNavController()) || super.onOptionsItemSelected(item)
    }

    override fun onTabUnselected(p0: TabLayout.Tab?) {}

    override fun onTabSelected(p0: TabLayout.Tab?) { updateCurrentTitle(p0?.position)}

    override fun onTabReselected(p0: TabLayout.Tab?) {}

    fun updateCurrentTitle(p0: Int?) {
        (activity as AppCompatActivity).supportActionBar!!.setTitle(viewModel.tabList.get(p0?:viewPager.currentItem).getTabName(requireContext()))
    }

    private fun tabInit() {
        viewModel.recheckRole()
        tabLayout = binding.mainTabLayout
        viewPager = binding.pager
        /*  Nested fragment, use child fragment here to maintain backstack in view pager. */
        pagerAdapter = SelectedTabsPagerAdapter(childFragmentManager)
        viewPager.adapter = pagerAdapter

        tabLayout.setupWithViewPager(viewPager)
        tabLayout.addOnTabSelectedListener(this)
        for(i in 0 until viewModel.tabList.size) {
            val tabToSet = tabLayout.getTabAt(i)
            val iconId: Int = viewModel.tabList.get(i).getTabIconRes(activity!!)
            tabToSet?.icon = AppCompatResources.getDrawable(requireContext(), iconId)
        }
        updateCurrentTitle(null)
    }


    private inner class SelectedTabsPagerAdapter constructor(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

        override fun getItem(position: Int): Fragment? {
            val tab = viewModel.tabList.get(position)
            var fragment: Fragment? = null
            try {
                fragment = tab.fragment
            } catch (e: Exception) {
                return EmptyFragment()
            }
//            (fragment as BaseFragment).useAsFrontPage(true)
            return fragment
        }

        override fun getItemPosition(`object`: Any): Int {
            return POSITION_NONE
        }

        override fun getCount(): Int {
            return viewModel.tabList.size
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            childFragmentManager
                    .beginTransaction()
                    .remove(`object` as Fragment)
                    .commitNowAllowingStateLoss()
        }

    }


}

