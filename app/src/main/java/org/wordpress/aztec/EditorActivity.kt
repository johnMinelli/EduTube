package org.wordpress.aztec

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import org.wordpress.aztec.BuildConfig
import org.wordpress.aztec.glideloader.GlideImageLoader
import org.wordpress.aztec.glideloader.GlideVideoThumbnailLoader
import org.wordpress.aztec.plugins.CssUnderlinePlugin
import org.wordpress.aztec.plugins.IMediaToolbarButton
import org.wordpress.aztec.plugins.shortcodes.AudioShortcodePlugin
import org.wordpress.aztec.plugins.shortcodes.CaptionShortcodePlugin
import org.wordpress.aztec.plugins.shortcodes.VideoShortcodePlugin
import org.wordpress.aztec.plugins.shortcodes.extensions.ATTRIBUTE_VIDEOPRESS_HIDDEN_ID
import org.wordpress.aztec.plugins.shortcodes.extensions.ATTRIBUTE_VIDEOPRESS_HIDDEN_SRC
import org.wordpress.aztec.plugins.shortcodes.extensions.updateVideoPressThumb
import org.wordpress.aztec.plugins.wpcomments.HiddenGutenbergPlugin
import org.wordpress.aztec.plugins.wpcomments.WordPressCommentsPlugin
import org.wordpress.aztec.plugins.wpcomments.toolbar.MoreToolbarButton
import org.wordpress.aztec.plugins.wpcomments.toolbar.PageToolbarButton
import org.wordpress.aztec.source.SourceViewEditText
import org.wordpress.aztec.toolbar.AztecToolbar
import org.wordpress.aztec.toolbar.IAztecToolbarClickListener
import org.wordpress.aztec.util.AztecLog
import org.xml.sax.Attributes
import java.io.File
import java.util.Random
import com.example.android.navigation.R
import com.example.android.navigation.util.Globals
import com.example.android.navigation.util.Constants as C

open class EditorActivity : AppCompatActivity(),
        AztecText.OnImeBackListener,
        AztecText.OnVideoTappedListener,
        AztecText.OnAudioTappedListener,
        IAztecToolbarClickListener,
        IHistoryListener,
        OnRequestPermissionsResultCallback,
        PopupMenu.OnMenuItemClickListener,
        View.OnTouchListener {

    override fun onToolbarFormatButtonClicked(format: ITextFormat, isKeyboardShortcut: Boolean) {
        //do nothing
    }

    companion object {

        fun newIntent(context: Context) = Intent(context, EditorActivity::class.java)
    }

    private val MEDIA_CAMERA_PHOTO_PERMISSION_REQUEST_CODE: Int = 1001
    private val MEDIA_CAMERA_VIDEO_PERMISSION_REQUEST_CODE: Int = 1002
    private val MEDIA_PHOTOS_PERMISSION_REQUEST_CODE: Int = 1003
    private val MEDIA_VIDEOS_PERMISSION_REQUEST_CODE: Int = 1004
    private val REQUEST_MEDIA_CAMERA_PHOTO: Int = 2001
    private val REQUEST_MEDIA_CAMERA_VIDEO: Int = 2002
    private val REQUEST_MEDIA_PHOTO: Int = 2003
    private val REQUEST_MEDIA_VIDEO: Int = 2004

    protected lateinit var aztec: Aztec
    private lateinit var mediaFile: String
    private lateinit var mediaPath: String

    private lateinit var invalidateOptionsHandler: Handler
    private lateinit var invalidateOptionsRunnable: Runnable

    private var mediaUploadDialog: AlertDialog? = null
    private var mediaMenu: PopupMenu? = null

    private var mIsKeyboardOpen = false
    private var mHideActionBarOnSoftKeyboardUp = false

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            var bitmap: Bitmap

            when (requestCode) {
                REQUEST_MEDIA_CAMERA_PHOTO -> {
                    // By default, BitmapFactory.decodeFile sets the bitmap's density to the device default so, we need
                    //  to correctly set the input density to 160 ourselves.
                    val options = BitmapFactory.Options()
                    options.inDensity = DisplayMetrics.DENSITY_DEFAULT
                    bitmap = BitmapFactory.decodeFile(mediaPath, options)
                    insertImageAndSimulateUpload(bitmap, mediaPath)
                }
                REQUEST_MEDIA_PHOTO -> {
                    mediaPath = data?.data.toString()
                    val stream = contentResolver.openInputStream(Uri.parse(mediaPath))
                    // By default, BitmapFactory.decodeFile sets the bitmap's density to the device default so, we need
                    //  to correctly set the input density to 160 ourselves.
                    val options = BitmapFactory.Options()
                    options.inDensity = DisplayMetrics.DENSITY_DEFAULT
                    bitmap = BitmapFactory.decodeStream(stream, null, options)

                    insertImageAndSimulateUpload(bitmap, mediaPath)
                }
                REQUEST_MEDIA_CAMERA_VIDEO -> {
                    mediaPath = data?.data.toString()
                }
                REQUEST_MEDIA_VIDEO -> {
                    mediaPath = data?.data.toString()

                    aztec.visualEditor.videoThumbnailGetter?.loadVideoThumbnail(mediaPath, object : Html.VideoThumbnailGetter.Callbacks {
                        override fun onThumbnailFailed() {
                        }

                        override fun onThumbnailLoaded(drawable: Drawable?) {
                            val conf = Bitmap.Config.ARGB_8888 // see other conf types
                            bitmap = Bitmap.createBitmap(drawable!!.intrinsicWidth, drawable.intrinsicHeight, conf)
                            val canvas = Canvas(bitmap)
                            drawable.setBounds(0, 0, canvas.width, canvas.height)
                            drawable.draw(canvas)

                            insertVideoAndSimulateUpload(bitmap, mediaPath)
                        }

                        override fun onThumbnailLoading(drawable: Drawable?) {
                        }
                    }, this.resources.displayMetrics.widthPixels)
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun insertImageAndSimulateUpload(bitmap: Bitmap?, mediaPath: String) {
//        val bitmapResized = ImageUtils.getScaledBitmapAtLongestSide(bitmap, aztec.visualEditor.maxImagesWidth)
//        val (id, attrs) = generateAttributesForMedia(mediaPath, isVideo = false)
//        aztec.visualEditor.insertImage(BitmapDrawable(resources, bitmapResized), attrs)
//        insertMediaAndSimulateUpload(id, attrs)
//        aztec.toolbar.toggleMediaToolbar()
    }

    fun insertVideoAndSimulateUpload(bitmap: Bitmap?, mediaPath: String) {
//        val bitmapResized = ImageUtils.getScaledBitmapAtLongestSide(bitmap, aztec.visualEditor.maxImagesWidth)
//        val (id, attrs) = generateAttributesForMedia(mediaPath, isVideo = true)
//        aztec.visualEditor.insertVideo(BitmapDrawable(resources, bitmapResized), attrs)
//        insertMediaAndSimulateUpload(id, attrs)
//        aztec.toolbar.toggleMediaToolbar()
    }

    private fun generateAttributesForMedia(mediaPath: String, isVideo: Boolean): Pair<String, AztecAttributes> {
        val id = Random().nextInt(Integer.MAX_VALUE).toString()
        val attrs = AztecAttributes()
        attrs.setValue("src", mediaPath) // Temporary source value.  Replace with URL after uploaded.
        attrs.setValue("id", id)
        attrs.setValue("uploading", "true")

        if (isVideo) {
            attrs.setValue("video", "true")
        }

        return Pair(id, attrs)
    }

    private fun insertMediaAndSimulateUpload(id: String, attrs: AztecAttributes) {
        val predicate = object : AztecText.AttributePredicate {
            override fun matches(attrs: Attributes): Boolean {
                return attrs.getValue("id") == id
            }
        }

        aztec.visualEditor.setOverlay(predicate, 0, ColorDrawable(0x80000000.toInt()), Gravity.FILL)
        aztec.visualEditor.updateElementAttributes(predicate, attrs)

        val progressDrawable = ContextCompat.getDrawable(this, android.R.drawable.progress_horizontal)!!
        // set the height of the progress bar to 2 (it's in dp since the drawable will be adjusted by the span)
        progressDrawable.setBounds(0, 0, 0, 4)

        aztec.visualEditor.setOverlay(predicate, 1, progressDrawable, Gravity.FILL_HORIZONTAL or Gravity.TOP)
        aztec.visualEditor.updateElementAttributes(predicate, attrs)

        var progress = 0

        // simulate an upload delay
        val runnable = Runnable {
            aztec.visualEditor.setOverlayLevel(predicate, 1, progress)
            aztec.visualEditor.updateElementAttributes(predicate, attrs)
            aztec.visualEditor.resetAttributedMediaSpan(predicate)
            progress += 2000

            if (progress >= 10000) {
                attrs.removeAttribute(attrs.getIndex("uploading"))
                aztec.visualEditor.clearOverlays(predicate)

                if (attrs.hasAttribute("video")) {
                    attrs.removeAttribute(attrs.getIndex("video"))
                    aztec.visualEditor.setOverlay(predicate, 0, ContextCompat.getDrawable(this, android.R.drawable.ic_media_play), Gravity.CENTER)
                }

                aztec.visualEditor.updateElementAttributes(predicate, attrs)
            }
        }

        Handler().post(runnable)
        Handler().postDelayed(runnable, 2000)
        Handler().postDelayed(runnable, 4000)
        Handler().postDelayed(runnable, 6000)
        Handler().postDelayed(runnable, 8000)

        aztec.visualEditor.refreshText()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor)

        // Setup hiding the action bar when the soft keyboard is displayed for narrow viewports
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
                && !resources.getBoolean(R.bool.is_large_tablet_landscape)) {
            mHideActionBarOnSoftKeyboardUp = true
        }

        val visualEditor = findViewById<AztecText>(R.id.aztec)
        val sourceEditor = findViewById<SourceViewEditText>(R.id.source)
        val toolbar = findViewById<AztecToolbar>(R.id.formatting_toolbar)

        val galleryButton = MediaToolbarGalleryButton(toolbar)
        galleryButton.setMediaToolbarButtonClickListener(object : IMediaToolbarButton.IMediaToolbarClickListener {
            override fun onClick(view: View) {
                mediaMenu = PopupMenu(this@EditorActivity, view)
                mediaMenu?.setOnMenuItemClickListener(this@EditorActivity)
                mediaMenu?.inflate(R.menu.menu_gallery)
                mediaMenu?.show()
                if (view is ToggleButton) {
                    view.isChecked = false
                }
            }
        })

        val cameraButton = MediaToolbarCameraButton(toolbar)
        cameraButton.setMediaToolbarButtonClickListener(object : IMediaToolbarButton.IMediaToolbarClickListener {
            override fun onClick(view: View) {
                mediaMenu = PopupMenu(this@EditorActivity, view)
                mediaMenu?.setOnMenuItemClickListener(this@EditorActivity)
                mediaMenu?.inflate(R.menu.menu_camera)
                mediaMenu?.show()
                if (view is ToggleButton) {
                    view.isChecked = false
                }
            }
        })

        aztec = Aztec.with(visualEditor, sourceEditor, toolbar, this)
                .setImageGetter(GlideImageLoader(this))
                .setVideoThumbnailGetter(GlideVideoThumbnailLoader(this))
                .setOnImeBackListener(this)
                .setOnTouchListener(this)
                .setHistoryListener(this)
                .setOnVideoTappedListener(this)
                .setOnAudioTappedListener(this)
                .addPlugin(WordPressCommentsPlugin(visualEditor))
                .addPlugin(MoreToolbarButton(visualEditor))
                .addPlugin(PageToolbarButton(visualEditor))
                .addPlugin(CaptionShortcodePlugin(visualEditor))
                .addPlugin(VideoShortcodePlugin())
                .addPlugin(AudioShortcodePlugin())
                .addPlugin(HiddenGutenbergPlugin(visualEditor))
                .addPlugin(galleryButton)
                .addPlugin(cameraButton)


        aztec.visualEditor.setCalypsoMode(false)
        aztec.sourceEditor?.setCalypsoMode(false)
        aztec.addPlugin(CssUnderlinePlugin())

        if (savedInstanceState == null) {
            val intent: Intent = intent
            val editText : String = intent.getStringExtra(C().TASK_REQUEST_EXTRA)
            aztec.visualEditor.fromHtml(editText)
            aztec.sourceEditor?.displayStyledAndFormattedHtml(editText)
            aztec.initSourceEditorHistory()
        }

        invalidateOptionsHandler = Handler()
        invalidateOptionsRunnable = Runnable { invalidateOptionsMenu() }
    }

    override fun onPause() {
        super.onPause()
        mIsKeyboardOpen = false
    }

    override fun onResume() {
        super.onResume()

        showActionBarIfNeeded()
    }

    override fun onDestroy() {
        super.onDestroy()
        aztec.visualEditor.disableCrashLogging()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        // Toggle action bar auto-hiding for the new orientation
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE
                && !resources.getBoolean(R.bool.is_large_tablet_landscape)) {
            mHideActionBarOnSoftKeyboardUp = true
            hideActionBarIfNeeded()
        } else {
            mHideActionBarOnSoftKeyboardUp = false
            showActionBarIfNeeded()
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)

        aztec.initSourceEditorHistory()

        savedInstanceState?.let {
            if (savedInstanceState.getBoolean("isMediaUploadDialogVisible")) {
                showMediaUploadDialog()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        if (mediaUploadDialog != null && mediaUploadDialog!!.isShowing) {
            outState.putBoolean("isMediaUploadDialogVisible", true)
        }
    }

    /**
     * Returns true if a hardware keyboard is detected, otherwise false.
     */
    private fun isHardwareKeyboardPresent(): Boolean {
        val config = resources.configuration
        var returnValue = false
        if (config.keyboard != Configuration.KEYBOARD_NOKEYS) {
            returnValue = true
        }
        return returnValue
    }

    private fun hideActionBarIfNeeded() {

        val actionBar = supportActionBar
        if (actionBar != null
                && !isHardwareKeyboardPresent()
                && mHideActionBarOnSoftKeyboardUp
                && mIsKeyboardOpen
                && actionBar.isShowing) {
            actionBar.hide()
        }
    }

    /**
     * Show the action bar if needed.
     */
    private fun showActionBarIfNeeded() {

        val actionBar = supportActionBar
        if (actionBar != null && !actionBar.isShowing) {
            actionBar.show()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View, event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            // If the WebView or EditText has received a touch event, the keyboard will be displayed and the action bar
            // should hide
            mIsKeyboardOpen = true
            hideActionBarIfNeeded()
        }
        return false
    }

    override fun onSupportNavigateUp(): Boolean {
        setResult(Activity.RESULT_CANCELED)
        finish()
        return false
    }

    override fun onBackPressed() {
        mIsKeyboardOpen = false
        showActionBarIfNeeded()

        return super.onBackPressed()
    }

    /**
     * Intercept back button press while soft keyboard is visible.
     */
    override fun onImeBack() {
        mIsKeyboardOpen = false
        showActionBarIfNeeded()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_editor, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.undo ->
                if (aztec.visualEditor.visibility == View.VISIBLE) {
                    aztec.visualEditor.undo()
                } else {
                    aztec.sourceEditor?.undo()
                }
            R.id.redo ->
                if (aztec.visualEditor.visibility == View.VISIBLE) {
                    aztec.visualEditor.redo()
                } else {
                    aztec.sourceEditor?.redo()
                }
            R.id.save ->{
                val result = Intent()
                val text : String = aztec.visualEditor.toFormattedHtml()
                result.putExtra(C().TASK_REQUEST_EXTRA, text)
                setResult(Activity.RESULT_OK, result)
                //setResult(Activity.RESULT_CANCELED)
                finish()
                return false
            }
            android.R.id.home -> {
                setResult(Activity.RESULT_CANCELED)
                finish()
                return false
            }
            else -> {
            }
        }
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.redo)?.isEnabled = aztec.visualEditor.history.redoValid()
        menu?.findItem(R.id.undo)?.isEnabled = aztec.visualEditor.history.undoValid()
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onRedoEnabled() {
        invalidateOptionsHandler.removeCallbacks(invalidateOptionsRunnable)
        invalidateOptionsHandler.postDelayed(invalidateOptionsRunnable, resources.getInteger(android.R.integer.config_mediumAnimTime).toLong())
    }

    override fun onUndoEnabled() {
        invalidateOptionsHandler.removeCallbacks(invalidateOptionsRunnable)
        invalidateOptionsHandler.postDelayed(invalidateOptionsRunnable, resources.getInteger(android.R.integer.config_mediumAnimTime).toLong())
    }

    override fun onUndo() {}

    override fun onRedo() {}

    //TODO PERMISSION MEDIA
    private fun onCameraPhotoMediaOptionSelected() {
//        if (PermissionUtils.checkAndRequestCameraAndStoragePermissions(this, MEDIA_CAMERA_PHOTO_PERMISSION_REQUEST_CODE)) {
//            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//
//            mediaFile = "wp-" + System.currentTimeMillis() + ".jpg"
//            mediaPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() +
//                    File.separator + "Camera" + File.separator + mediaFile
//            intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(this,
//                    BuildConfig.APPLICATION_ID + ".provider", File(mediaPath)))
//
//            if (intent.resolveActivity(packageManager) != null) {
//                startActivityForResult(intent, REQUEST_MEDIA_CAMERA_PHOTO)
//            }
//        }
    }

    private fun onCameraVideoMediaOptionSelected() {
//        if (PermissionUtils.checkAndRequestCameraAndStoragePermissions(this, MEDIA_CAMERA_PHOTO_PERMISSION_REQUEST_CODE)) {
//            val intent = Intent(MediaStore.INTENT_ACTION_VIDEO_CAMERA)
//
//            if (intent.resolveActivity(packageManager) != null) {
//                startActivityForResult(intent, REQUEST_MEDIA_CAMERA_VIDEO)
//            }
//        }
    }

    private fun onPhotosMediaOptionSelected() {
//        if (PermissionUtils.checkAndRequestStoragePermission(this, MEDIA_PHOTOS_PERMISSION_REQUEST_CODE)) {
//            val intent: Intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                Intent(Intent.ACTION_OPEN_DOCUMENT)
//            } else {
//                Intent.createChooser(Intent(Intent.ACTION_GET_CONTENT), getString(org.wordpress.aztec.demo.R.string.title_select_photo))
//            }
//
//            intent.addCategory(Intent.CATEGORY_OPENABLE)
//            intent.type = "image/*"
//
//            try {
//                startActivityForResult(intent, REQUEST_MEDIA_PHOTO)
//            } catch (exception: ActivityNotFoundException) {
//                AppLog.e(AppLog.T.EDITOR, exception.message)
//                ToastUtils.showToast(this, getString(org.wordpress.aztec.demo.R.string.error_chooser_photo), ToastUtils.Duration.LONG)
//            }
//        }
    }

    private fun onVideosMediaOptionSelected() {
//        if (PermissionUtils.checkAndRequestStoragePermission(this, MEDIA_PHOTOS_PERMISSION_REQUEST_CODE)) {
//            val intent: Intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                Intent(Intent.ACTION_OPEN_DOCUMENT)
//            } else {
//                Intent.createChooser(Intent(Intent.ACTION_GET_CONTENT), getString(org.wordpress.aztec.demo.R.string.title_select_video))
//            }
//
//            intent.addCategory(Intent.CATEGORY_OPENABLE)
//            intent.type = "video/*"
//
//            try {
//                startActivityForResult(intent, REQUEST_MEDIA_VIDEO)
//            } catch (exception: ActivityNotFoundException) {
//                AppLog.e(AppLog.T.EDITOR, exception.message)
//                ToastUtils.showToast(this, getString(org.wordpress.aztec.demo.R.string.error_chooser_video), ToastUtils.Duration.LONG)
//            }
//        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            MEDIA_CAMERA_PHOTO_PERMISSION_REQUEST_CODE,
            MEDIA_CAMERA_VIDEO_PERMISSION_REQUEST_CODE -> {
                var isPermissionDenied = false

                for (i in grantResults.indices) {
                    when (permissions[i]) {
                        Manifest.permission.CAMERA -> {
                            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                                isPermissionDenied = true
                            }
                        }
                        Manifest.permission.WRITE_EXTERNAL_STORAGE -> {
                            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                                isPermissionDenied = true
                            }
                        }
                    }
                }

                if (isPermissionDenied) {
                    Toast.makeText(this, getString(R.string.permission_required_media_camera),Toast.LENGTH_SHORT)
                } else {
                    when (requestCode) {
                        MEDIA_CAMERA_PHOTO_PERMISSION_REQUEST_CODE -> {
                            onCameraPhotoMediaOptionSelected()
                        }
                        MEDIA_CAMERA_VIDEO_PERMISSION_REQUEST_CODE -> {
                            onCameraVideoMediaOptionSelected()
                        }
                    }
                }
            }
            MEDIA_PHOTOS_PERMISSION_REQUEST_CODE,
            MEDIA_VIDEOS_PERMISSION_REQUEST_CODE -> {
                var isPermissionDenied = false

                for (i in grantResults.indices) {
                    when (permissions[i]) {
                        Manifest.permission.WRITE_EXTERNAL_STORAGE -> {
                            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                                isPermissionDenied = true
                            }
                        }
                    }
                }

                when (requestCode) {
                    MEDIA_PHOTOS_PERMISSION_REQUEST_CODE -> {
                        if (isPermissionDenied) {
                            Toast.makeText(this, getString(R.string.permission_required_media_photos),Toast.LENGTH_SHORT)
                        } else {
                            onPhotosMediaOptionSelected()
                        }
                    }
                    MEDIA_VIDEOS_PERMISSION_REQUEST_CODE -> {
                        if (isPermissionDenied) {
                            Toast.makeText(this, getString(R.string.permission_required_media_videos),Toast.LENGTH_SHORT)
                        } else {
                            onVideosMediaOptionSelected()
                        }
                    }
                }
            }
            else -> {
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onToolbarCollapseButtonClicked() {
    }

    override fun onToolbarExpandButtonClicked() {
    }

    override fun onToolbarHeadingButtonClicked() {
    }

    override fun onToolbarHtmlButtonClicked() {
        val uploadingPredicate = object : AztecText.AttributePredicate {
            override fun matches(attrs: Attributes): Boolean {
                return attrs.getIndex("uploading") > -1
            }
        }

        val mediaPending = aztec.visualEditor.getAllElementAttributes(uploadingPredicate).isNotEmpty()

        if (mediaPending) {
            Toast.makeText(this, "Please wait",Toast.LENGTH_SHORT)
        } else {
            aztec.toolbar.toggleEditorMode()
        }
    }

    override fun onToolbarListButtonClicked() {
    }

    override fun onToolbarMediaButtonClicked(): Boolean {
        return false
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        item?.isChecked = (item?.isChecked == false)

        return when (item?.itemId) {
            R.id.take_photo -> {
                onCameraPhotoMediaOptionSelected()
                true
            }
            R.id.take_video -> {
                onCameraVideoMediaOptionSelected()
                true
            }
            R.id.gallery_photo -> {
                onPhotosMediaOptionSelected()
                true
            }
            R.id.gallery_video -> {
                onVideosMediaOptionSelected()
                true
            }
            else -> false
        }
    }

    private fun showMediaUploadDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(getString(R.string.media_upload_dialog_message))
        builder.setPositiveButton(getString(R.string.media_upload_dialog_positive), null)
        mediaUploadDialog = builder.create()
        mediaUploadDialog!!.show()
    }

    override fun onVideoTapped(attrs: AztecAttributes) {
        val url = if (attrs.hasAttribute(ATTRIBUTE_VIDEOPRESS_HIDDEN_SRC)) {
            attrs.getValue(ATTRIBUTE_VIDEOPRESS_HIDDEN_SRC)
        } else {
            attrs.getValue("src")
        }

        url?.let {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                intent.setDataAndType(Uri.parse(url), "video/*")
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                try {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(browserIntent)
                } catch (e: ActivityNotFoundException) {
                }
            }
        }
    }

    override fun onAudioTapped(attrs: AztecAttributes) {
        val url = attrs.getValue("src")
        url?.let {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                intent.setDataAndType(Uri.parse(url), "audio/*")
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                try {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(browserIntent)
                } catch (e: ActivityNotFoundException) {
                }
            }
        }
    }
}
