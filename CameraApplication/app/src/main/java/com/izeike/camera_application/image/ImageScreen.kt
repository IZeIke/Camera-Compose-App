package com.izeike.camera_application.image

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

@Composable
fun ImageScreen(imageUri: String) {
    Box(
        modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)
    ) {
        AsyncImage(
            modifier = Modifier
                .fillMaxSize(),
            model = Uri.parse(imageUri),
            contentScale = ContentScale.Fit,
            contentDescription = null
        )
    }
}