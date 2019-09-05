package org.wordpress.aztec

import org.wordpress.aztec.toolbar.IToolbarAction
import org.wordpress.aztec.toolbar.ToolbarActionType
import com.example.android.navigation.R

enum class MediaToolbarAction constructor(override val buttonId: Int, override val actionType: ToolbarActionType,
                                          override val textFormats: Set<ITextFormat> = setOf()) : IToolbarAction {
    GALLERY(R.id.media_bar_button_gallery, ToolbarActionType.OTHER, setOf(AztecTextFormat.FORMAT_NONE)),
    CAMERA(R.id.media_bar_button_camera, ToolbarActionType.OTHER, setOf(AztecTextFormat.FORMAT_NONE))
}
