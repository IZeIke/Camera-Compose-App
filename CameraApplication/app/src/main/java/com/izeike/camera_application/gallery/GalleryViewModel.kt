package com.izeike.camera_application.gallery

import android.app.Application
import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.izeike.camera_application.constant.Path
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.withContext

class GalleryViewModel(app: Application) : AndroidViewModel(app) {

    private val context: Context
        get() = getApplication<Application>().applicationContext

    var displayType by mutableStateOf<DisplayType>(DisplayType.Grid)
        private set

    private val _images = MutableStateFlow<List<GalleryData>>(listOf(GalleryData.InitialData))
    private val _videos = MutableStateFlow<List<GalleryData>>(listOf(GalleryData.InitialData))

    val galleryState: StateFlow<GalleryState> = _images.zip(_videos) { images, videos ->
        images + videos
    }.map { galleries ->
        if (galleries.contains(GalleryData.InitialData)) {
            GalleryState.Loading
        } else {
            GalleryState.Success(galleries.sortedByDescending { it.dateAdded })
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000L),
        GalleryState.Loading
    )

    fun changeDisplayType() {
        displayType = when (displayType) {
            DisplayType.Grid -> DisplayType.StaggeredGrid
            DisplayType.StaggeredGrid -> DisplayType.List
            DisplayType.List -> DisplayType.Grid
        }
    }

    suspend fun fetchGalleryData() {
        withContext(Dispatchers.IO) {
            fetchImages()
            fetchVideos()
        }
    }

    private fun fetchImages() {
        val images = mutableListOf<GalleryData.ImageData>()

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED
        )

        val selection = "${MediaStore.Images.Media.DATA} like ? "
        val selectionArgs = arrayOf("%/${Path.APP_IMAGES_PATH}/%")

        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        val cursor = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )

        cursor?.use {
            val columnIndexId = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val columnIndexDisplayName =
                it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val columnIndexDateAdded = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)

            while (it.moveToNext()) {
                val imageUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    it.getLong(columnIndexId)
                )
                val imageData = GalleryData.ImageData(
                    uri = imageUri,
                    name = it.getString(columnIndexDisplayName),
                    dateAdded = it.getLong(columnIndexDateAdded)
                )
                images.add(imageData)
            }
        }

        _images.value = images
    }

    private fun fetchVideos() {
        val videos = mutableListOf<GalleryData.VideoData>()

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DURATION
        )

        val selection = "${MediaStore.Video.Media.DATA} like ? "
        val selectionArgs = arrayOf("%/${Path.APP_VIDEOS_PATH}/%")

        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

        val cursor = context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )

        cursor?.use {
            val columnIndexId = it.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val columnIndexDateAdded = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
            val columnIndexDuration = it.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val columnIndexDisplayName =
                it.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)

            while (it.moveToNext()) {
                val videoUri = ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    it.getLong(columnIndexId)
                )
                val videoData = GalleryData.VideoData(
                    uri = videoUri,
                    name = it.getString(columnIndexDisplayName),
                    dateAdded = it.getLong(columnIndexDateAdded),
                    duration = millisToTime(it.getLong(columnIndexDuration)),
                    thumbnail = createVideoThumb(videoUri)
                )
                videos.add(videoData)
            }
        }

        _videos.value = videos
    }

    private fun createVideoThumb(uri: Uri): Bitmap? {
        try {
            val mediaMetadataRetriever = MediaMetadataRetriever()
            mediaMetadataRetriever.setDataSource(context, uri)
            return mediaMetadataRetriever.frameAtTime
        } catch (ex: Exception) {
            // Error retrieving bitmap
        }
        return null
    }

    private fun millisToTime(millis: Long): String {
        val seconds = millis / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60

        return String.format("%02d:%02d", minutes, remainingSeconds)
    }
}