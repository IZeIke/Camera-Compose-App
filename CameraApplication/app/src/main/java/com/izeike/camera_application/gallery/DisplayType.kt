package com.izeike.camera_application.gallery

sealed class DisplayType {
    object Grid : DisplayType()
    object StaggeredGrid : DisplayType()
    object List : DisplayType()
}