package com.izeike.camera_application.gallery

sealed class GalleryState {
    data class Success(val galleries: List<GalleryData>): GalleryState()
    object Loading: GalleryState()
}