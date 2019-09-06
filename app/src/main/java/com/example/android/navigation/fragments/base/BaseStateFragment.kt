package com.example.android.navigation.fragments.base

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment

import com.example.android.navigation.R
import com.example.android.navigation.util.AnimationUtils.animateView

import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

abstract class BaseStateFragment : Fragment() {

    protected var wasLoading = AtomicBoolean()
    protected var isLoading = AtomicBoolean()

    protected var emptyStateView: View? = null
    protected var loadingProgressBar: ProgressBar? = null

    protected var errorPanelRoot: View? = null
    protected var errorButtonRetry: Button? = null
    protected var errorTextView: TextView? = null

    override fun onViewCreated(rootView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(rootView, savedInstanceState)
        emptyStateView = rootView.findViewById<View>(R.id.empty_state_view)
        loadingProgressBar = rootView.findViewById<ProgressBar>(R.id.loading_progress_bar)

        errorPanelRoot = rootView.findViewById<View>(R.id.error_panel)
        errorButtonRetry = rootView.findViewById<Button>(R.id.error_button_retry)
        errorTextView = rootView.findViewById<TextView>(R.id.error_message_view)
        startLoading(true)
    }

    override fun onPause() {
        super.onPause()
        wasLoading.set(isLoading.get())
    }

    //////////////////////////////////////////////////////////////////////////
    // Init
    //////////////////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////////
    // Contract
    //////////////////////////////////////////////////////////////////////////

    fun startLoading(flag: Boolean) {
        if(flag) {
            showLoading()
            isLoading.set(true)
        }else{
            isLoading.set(false)
            hideLoading()
        }
    }

    open fun showLoading() {
        emptyStateView?.let {
            if (emptyStateView != null) animateView(emptyStateView!!, false, 150)
        }
        loadingProgressBar?.let {
            if (loadingProgressBar != null) animateView(loadingProgressBar!!, true, 400)
        }
        errorPanelRoot?.let{
            animateView(errorPanelRoot!!, false, 150)
        }
    }

    open fun hideLoading() {
//        emptyStateView?.let {
//            if (emptyStateView != null) animateView(emptyStateView!!, false, 150)
//        }
        loadingProgressBar?.let {
            if (loadingProgressBar != null) animateView(loadingProgressBar!!, false, 0)
        }
        errorPanelRoot?.let{
            animateView(errorPanelRoot!!, false, 150)
        }
    }

    open fun showEmptyState() {
        isLoading.set(false)
        emptyStateView?.let {
            if (emptyStateView != null) animateView(emptyStateView!!, true, 200)
        }
        loadingProgressBar?.let {
            if (loadingProgressBar != null) animateView(loadingProgressBar!!, false, 0)
        }
        errorPanelRoot?.let{
            animateView(errorPanelRoot!!, false, 150)
        }
    }

    open fun hideEmptySpace(){
        emptyStateView?.let {
            if (emptyStateView != null) animateView(emptyStateView!!, false, 0)
        }
    }

    open fun showError(message:String, showRetryButton:Boolean) {
        startLoading(false)
        errorTextView?.let {
            errorTextView!!.setText(message)
        }
        errorButtonRetry?.let {
            if (showRetryButton)animateView(errorButtonRetry!!, true, 600) else animateView(errorButtonRetry!!, false, 0)
        }
        errorPanelRoot?.let{
            animateView(errorPanelRoot!!, true, 300)
        }
    }

    //////////////////////////////////////////////////////////////////////////
    // Error handling
    //////////////////////////////////////////////////////////////////////////

    protected fun onError(exception:Throwable):Boolean {
        Log.d("Tag."+this.toString(), "onError() called with: exception = [" + exception + "]")
        startLoading(false)
        if (isDetached() || isRemoving()){
            Log.d("Tag."+this.toString(),"onError() is detached or removing = [" + exception + "]")
            return true
        }else if (exception is IOException){
            showError(getString(R.string.network_error), true)
            return true
        }else{
            Log.d("Tag."+this.toString(),"onError() isInterruptedCaused! = [" + exception + "]")
            return true
        }
        return false
    }

}
