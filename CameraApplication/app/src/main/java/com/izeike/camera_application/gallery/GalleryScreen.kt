package com.izeike.camera_application.gallery

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.izeike.camera_application.R
import com.izeike.camera_application.navigation.Destination.IMAGE_SCREEN
import com.izeike.camera_application.navigation.Destination.VIDEO_PREVIEW_SCREEN

@Composable
fun GalleryScreen(
    navController: NavController,
    viewModel: GalleryViewModel = viewModel()
) {
    val galleryState by viewModel.galleryState.collectAsStateWithLifecycle()
    GalleryScreen(
        galleryState = galleryState,
        displayType = viewModel.displayType,
        onImageClicked = { imageUri ->
            val imageScreenPage = IMAGE_SCREEN.replace("{imageUri}", imageUri)
            navController.navigate(
                imageScreenPage
            )
        },
        onVideoClicked = { uri ->
            val videoPreviewScreen = VIDEO_PREVIEW_SCREEN.replace("{uri}", uri)
            navController.navigate(
                videoPreviewScreen
            )
        },
        onChangeDisplayType = {
            viewModel.changeDisplayType()
        },
        onFetchGalleryData = {
            viewModel.fetchGalleryData()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    galleryState: GalleryState,
    displayType: DisplayType,
    onImageClicked: (imageUri: String) -> Unit,
    onVideoClicked: (uri: String) -> Unit,
    onChangeDisplayType: () -> Unit,
    onFetchGalleryData: suspend () -> Unit
) {
    val context = LocalContext.current

    RequestReadPermission(context) {
        LaunchedEffect(Unit) {
            onFetchGalleryData()
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(text = stringResource(id = R.string.app_name))
                    },
                    colors = TopAppBarDefaults.smallTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = Color.White,
                    ),
                    actions = {
                        IconButton(onClick = onChangeDisplayType) {
                            Icon(
                                Icons.Filled.Refresh,
                                tint = Color.White,
                                contentDescription = "Display Type"
                            )
                        }
                    }
                )
            }
        ) { contentPadding ->
            when (galleryState) {
                is GalleryState.Success -> {
                    if (galleryState.galleries.isEmpty()) {
                        Text(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .wrapContentHeight()
                                .padding(contentPadding),
                            text = "no image",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleLarge
                        )
                    } else {
                        when (displayType) {
                            DisplayType.Grid -> {
                                ImageGrid(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .padding(contentPadding),
                                    images = galleryState.galleries,
                                    onImageClicked = onImageClicked,
                                    onVideoClicked = onVideoClicked
                                )
                            }

                            DisplayType.List -> {
                                ImageList(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .padding(contentPadding),
                                    images = galleryState.galleries,
                                    onImageClicked = onImageClicked,
                                    onVideoClicked = onVideoClicked
                                )
                            }

                            DisplayType.StaggeredGrid -> {
                                ImageStaggeredGrid(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .padding(contentPadding),
                                    images = galleryState.galleries,
                                    onImageClicked = onImageClicked,
                                    onVideoClicked = onVideoClicked
                                )
                            }
                        }
                    }
                }

                GalleryState.Loading -> {
                    Box(
                        modifier = Modifier
                            .padding(contentPadding)
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ImageStaggeredGrid(
    modifier: Modifier,
    images: List<GalleryData>,
    onImageClicked: (uri: String) -> Unit,
    onVideoClicked: (uri: String) -> Unit
) {
    LazyVerticalStaggeredGrid(
        modifier = modifier,
        columns = StaggeredGridCells.Fixed(2),
        verticalItemSpacing = 4.dp,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(
            items = images,
            key = { it.name.orEmpty() }
        ) { galleryData ->
            val isVideo = galleryData is GalleryData.VideoData
            val imagePreview: Any? = if (galleryData is GalleryData.VideoData) {
                galleryData.thumbnail
            } else {
                galleryData.uri
            }
            Box(
                modifier = Modifier
                    .wrapContentSize()
            ) {
                AsyncImage(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .clickable {
                            if (isVideo) {
                                onVideoClicked(Uri.encode(galleryData.uri.toString()))
                            } else {
                                onImageClicked(Uri.encode(galleryData.uri.toString()))
                            }
                        },
                    model = imagePreview,
                    contentScale = ContentScale.Fit,
                    contentDescription = null
                )
                if (galleryData is GalleryData.VideoData) {
                    Text(
                        modifier = Modifier
                            .padding(bottom = 4.dp, end = 4.dp)
                            .align(Alignment.BottomEnd),
                        text = galleryData.duration,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ImageGrid(
    modifier: Modifier,
    images: List<GalleryData>,
    onImageClicked: (uri: String) -> Unit,
    onVideoClicked: (uri: String) -> Unit
) {
    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Adaptive(minSize = 128.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(
            images,
            key = { it.name.orEmpty() }
        ) { galleryData ->
            val isVideo = galleryData is GalleryData.VideoData
            val imagePreview: Any? = if (galleryData is GalleryData.VideoData) {
                galleryData.thumbnail
            } else {
                galleryData.uri
            }
            Box(
                modifier = Modifier
                    .wrapContentSize()
            ) {
                AsyncImage(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clickable {
                            if (isVideo) {
                                onVideoClicked(Uri.encode(galleryData.uri.toString()))
                            } else {
                                onImageClicked(Uri.encode(galleryData.uri.toString()))
                            }
                        },
                    model = imagePreview,
                    contentScale = ContentScale.Crop,
                    contentDescription = null
                )
                if (galleryData is GalleryData.VideoData) {
                    Text(
                        modifier = Modifier
                            .padding(bottom = 4.dp, end = 4.dp)
                            .align(Alignment.BottomEnd),
                        text = galleryData.duration,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ImageList(
    modifier: Modifier,
    images: List<GalleryData>,
    onImageClicked: (uri: String) -> Unit,
    onVideoClicked: (uri: String) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(
            images,
            key = { it.name.orEmpty() }
        ) { galleryData ->
            val isVideo = galleryData is GalleryData.VideoData
            Row(
                modifier = Modifier
                    .height(100.dp)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable {
                        if (isVideo) {
                            onVideoClicked(Uri.encode(galleryData.uri.toString()))
                        } else {
                            onImageClicked(Uri.encode(galleryData.uri.toString()))
                        }
                    }
            ) {
                val imagePreview: Any? = if (galleryData is GalleryData.VideoData) {
                    galleryData.thumbnail
                } else {
                    galleryData.uri
                }
                Box(
                    modifier = Modifier.wrapContentSize()
                ) {
                    AsyncImage(
                        modifier = Modifier
                            .size(100.dp),
                        model = imagePreview,
                        contentScale = ContentScale.Crop,
                        contentDescription = null
                    )
                    if (galleryData is GalleryData.VideoData) {
                        Text(
                            modifier = Modifier
                                .padding(bottom = 4.dp, end = 4.dp)
                                .align(Alignment.BottomEnd),
                            text = galleryData.duration,
                            color = Color.White
                        )
                    }
                }
                Text(
                    modifier = Modifier
                        .padding(start = 20.dp)
                        .fillMaxHeight()
                        .weight(1f)
                        .wrapContentHeight(),
                    text = galleryData.name.orEmpty(),
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestReadPermission(
    context: Context,
    anyComposable: @Composable () -> Unit
) {
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
        anyComposable()
    } else {
        val readPermissionsState = rememberPermissionState(
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        LaunchedEffect(Unit) {
            readPermissionsState.launchPermissionRequest()
        }

        if (readPermissionsState.status.isGranted) {
            anyComposable()
        } else {
            if (readPermissionsState.status.shouldShowRationale) {
                // user denied permission
                Toast.makeText(context, "user denied permission", Toast.LENGTH_LONG).show()
            } else {
                // user doesn't want to be asked again
                Toast.makeText(context, "user doesn't want to be asked again", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }
}