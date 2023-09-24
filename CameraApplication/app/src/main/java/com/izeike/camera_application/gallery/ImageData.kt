package com.izeike.camera_application.gallery

import android.graphics.Bitmap
import android.net.Uri

sealed class GalleryData(open val uri: Uri?, open val name: String?, open val dateAdded: Long?) {
    data class ImageData(
        override val uri: Uri,
        override val name: String,
        override val dateAdded: Long
    ) : GalleryData(uri, name, dateAdded)

    data class VideoData(
        override val uri: Uri,
        override val name: String,
        override val dateAdded: Long,
        val duration: String,
        val thumbnail: Bitmap? = null
    ) : GalleryData(uri, name, dateAdded)

    object InitialData: GalleryData(null,null,null)
}