package com.example.android.navigation.fragments

import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import androidx.databinding.DataBindingUtil

import com.example.android.navigation.R
import com.example.android.navigation.databinding.FragmentStreamBinding
import com.example.android.navigation.models.ContentItem
import com.example.android.navigation.viewModel.StreamViewModel
import com.example.android.navigation.viewModel.StreamViewModelFactory
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import org.wordpress.aztec.Aztec
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.ITextFormat
import org.wordpress.aztec.toolbar.IAztecToolbarClickListener
import com.google.android.material.appbar.AppBarLayout
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import com.example.android.navigation.util.Constants
import com.example.android.navigation.util.FullScreenHelper
import com.example.android.navigation.util.ImageDisplayConstants
import com.example.android.navigation.util.YouTubeDataEndpoint
import com.google.firebase.firestore.FirebaseFirestore
import com.nostra13.universalimageloader.core.ImageLoader
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerFullScreenListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.loadOrCueVideo
import kotlinx.android.synthetic.main.fragment_stream.*
import kotlinx.coroutines.launch
import org.wordpress.aztec.glideloader.GlideImageLoader


class StreamFragment : Fragment(),
        AztecText.OnVideoTappedListener,
        AztecText.OnAudioTappedListener,
        IAztecToolbarClickListener {


    private lateinit var binding: FragmentStreamBinding
    private lateinit var viewModel: StreamViewModel
    private lateinit var viewModelFactory: StreamViewModelFactory
    private val fire: FirebaseFirestore by lazy { FirebaseFirestore.getInstance()}

    private lateinit var aztec: Aztec
    private lateinit var youTubePlayerView: YouTubePlayerView
    private lateinit var fullScreenHelper: FullScreenHelper


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate<FragmentStreamBinding>(inflater,R.layout.fragment_stream,container,false)
        val selectedItem: ContentItem? = arguments?.let{ StreamFragmentArgs.fromBundle(arguments!!).selectedItem}
        if(selectedItem == null){
            //showError("Video not found",false)
            return null
        }

        //set actionbar
        val activ = (activity as AppCompatActivity)
        activ.supportActionBar?.hide()
        val toolbar = binding.toolbar
        activ.setSupportActionBar(toolbar)
        activ.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        activ.supportActionBar!!.title = ""

        //set viewmodel and observer
        viewModelFactory = StreamViewModelFactory(fire, PreferenceManager.getDefaultSharedPreferences(activity), selectedItem)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(StreamViewModel::class.java)
        binding.dataStreamViewModel = viewModel
        binding.lifecycleOwner = this
        viewModel.readyState.observe(viewLifecycleOwner, Observer {
            it?.let{
                if(it==true){
                    hardInflate()
                    initVideoText()
                }
            }
        })
        viewModel.toastMsg.observe(viewLifecycleOwner, Observer {
            it?.let{
                Toast.makeText(requireContext(),it, Toast.LENGTH_LONG).show()
                viewModel.doneToastMsg()
            }
        })
        viewModel.lock.observe(viewLifecycleOwner, Observer {
            it?.let {
                val params = binding.toolbarLayout.layoutParams as AppBarLayout.LayoutParams
                if(it==true)
                    params.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP
                else
                    params.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED
                binding.toolbarLayout.layoutParams = params
            }
        })

        createShare()
        return binding.root
    }


    fun createShare(){
        if (null == getShareIntent().resolveActivity(activity!!.packageManager)) {
            // hide the menu item if it doesn't resolve
            binding.share.visibility = View.GONE
        }else{
            binding.share.setOnClickListener{
                shareContent()
            }
        }
    }

    private fun getShareIntent(): Intent {
        var intent = ShareCompat.IntentBuilder.from(activity as AppCompatActivity).setText("Hey, check this out. I found a great article \"${viewModel.selectedItem.title}\". Downlaod EduTube Italia to read it.").setType("text/plain").intent
        return intent
    }

    private fun shareContent(){
        startActivity(getShareIntent())
    }

    override fun onDestroy() {
        super.onDestroy()
        youTubePlayerView.release()
    }

    private fun initVideoText() {
        youTubePlayerView = binding.youtubePlayerView
        fullScreenHelper = FullScreenHelper(activity)
        lifecycle.addObserver(youTubePlayerView)
        youTubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                super.onReady(youTubePlayer)
                youTubePlayer.loadOrCueVideo(lifecycle, viewModel.selectedItem.videoUrl,0f)
                youTubePlayer.pause()
            }

            override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
                super.onError(youTubePlayer, error)
                youTubePlayer.loadOrCueVideo(lifecycle, viewModel.selectedItem.videoUrl,0f)
                youTubePlayer.pause()
            }

            override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                super.onCurrentSecond(youTubePlayer, second)
                if(viewModel.visualized==false && second>15)viewModel.visualizeIt()
            }
        })
        youTubePlayerView.addFullScreenListener(object : YouTubePlayerFullScreenListener {
            override fun onYouTubePlayerEnterFullScreen() {
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                fullScreenHelper.enterFullScreen()
            }
            override fun onYouTubePlayerExitFullScreen() {
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                fullScreenHelper.exitFullScreen()
            }
        })
        val visualEditor = binding.aztec
        val toolbar = binding.formattingToolbar
        aztec = Aztec.with(visualEditor, toolbar, this)
                .setImageGetter(GlideImageLoader(requireContext()))
                .setOnVideoTappedListener(this)
                .setOnAudioTappedListener(this)
        aztec.visualEditor.fromHtml(viewModel.selectedItem.text)
    }

    private fun hardInflate(){
        var view = binding.detailUploaderThumbnailView
        var url =  viewModel.categoryThumbnail?.first //coppia url size
        val a = PreferenceManager.getDefaultSharedPreferences(context).getString(Constants().KEY_THEME, Constants().LIGHT)
        url?.let{
            val imageLoader = ImageLoader.getInstance()
            imageLoader.displayImage(url, view, ImageDisplayConstants.DISPLAY_CHANNEL_OPTIONS)
            if(!a.equals(Constants().LIGHT)){
                val color = Color.WHITE
                val mode = PorterDuff.Mode.SRC_ATOP
                view.setColorFilter(color, mode)
            }
        }
    }

    override fun onAudioTapped(attrs: AztecAttributes) {
        //play?
    }

    override fun onVideoTapped(attrs: AztecAttributes) {
        //play?
    }
    override fun onToolbarCollapseButtonClicked() {}
    override fun onToolbarExpandButtonClicked() {}
    override fun onToolbarFormatButtonClicked(format: ITextFormat, isKeyboardShortcut: Boolean) {}
    override fun onToolbarHeadingButtonClicked() {}
    override fun onToolbarHtmlButtonClicked() {}
    override fun onToolbarListButtonClicked() {}
    override fun onToolbarMediaButtonClicked(): Boolean {return false}

}
