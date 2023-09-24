package com.izeike.camera_application.camera

import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.izeike.camera_application.navigation.Destination

@Composable
fun CameraScreen(
    navController: NavController,
    viewModel: CameraViewModel = viewModel()
) {
    CameraScreen(
        cameraController = viewModel.cameraController,
        cameraSelector = viewModel.cameraSelector,
        onTakePhotoClicked = {
            viewModel.takePhoto()
        },
        onChangeCameraClicked = {
            viewModel.changeCamera()
        },
        goToGalleryScreen = {
            navController.navigate(Destination.GALLERY_SCREEN)
        },
        goToVideoScreen = {
            navController.navigate(Destination.VIDEO_SCREEN)
        }
    )
}

@Composable
fun CameraScreen(
    cameraController: LifecycleCameraController? = null,
    cameraSelector: CameraSelector,
    onTakePhotoClicked: () -> Unit,
    onChangeCameraClicked: () -> Unit,
    goToGalleryScreen: () -> Unit,
    goToVideoScreen: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    RequestCameraPermission(
        context = context,
        permissionsNotGrantedContent = {
            Text(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .wrapContentHeight(),
                text = "Camera not Granted",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {

            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    PreviewView(context).apply {
                        cameraController?.unbind()
                        cameraController?.bindToLifecycle(lifecycleOwner)
                        controller = cameraController
                    }
                },
                update = { previewView ->
                    previewView.controller?.cameraSelector = cameraSelector
                }
            )

            ActionButton(
                modifier = Modifier
                    .padding(end = 20.dp, top = 20.dp)
                    .wrapContentSize()
                    .padding(5.dp)
                    .align(Alignment.TopEnd),
                text = "Video",
                onClick = {
                    goToVideoScreen()
                }
            )

            ActionButton(
                modifier = Modifier
                    .padding(start = 20.dp, bottom = 20.dp)
                    .size(100.dp, 100.dp)
                    .align(Alignment.BottomStart),
                text = "Change Camera",
                onClick = {
                    onChangeCameraClicked()
                }
            )

            ActionButton(
                modifier = Modifier
                    .padding(bottom = 20.dp)
                    .size(100.dp, 100.dp)
                    .align(Alignment.BottomCenter),
                text = "Take a photo",
                onClick = {
                    onTakePhotoClicked()
                }
            )

            ActionButton(
                modifier = Modifier
                    .padding(bottom = 20.dp, end = 20.dp)
                    .size(100.dp, 100.dp)
                    .align(Alignment.BottomEnd),
                text = "Go to Gallery",
                onClick = {
                    goToGalleryScreen()
                }
            )
        }
    }
}

@Composable
fun ActionButton(
    modifier: Modifier,
    text: String,
    color: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(color)
    ) {
        Text(
            modifier = Modifier
                .align(Alignment.CenterVertically),
            text = text,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestCameraPermission(
    context: Context,
    permissionsNotGrantedContent: @Composable () -> Unit,
    permissionsGrantedContent: @Composable () -> Unit
) {
    val cameraPermissionsState = rememberMultiplePermissionsState(
        mutableListOf(
            android.Manifest.permission.CAMERA,
        ).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    )

    LaunchedEffect(Unit) {
        cameraPermissionsState.launchMultiplePermissionRequest()
    }

    if (cameraPermissionsState.allPermissionsGranted) {
        permissionsGrantedContent()
    } else {
        val allPermissionsRevoked =
            cameraPermissionsState.permissions.size ==
                    cameraPermissionsState.revokedPermissions.size

        if (!allPermissionsRevoked) {
            // user denied some permission
            Toast.makeText(context, "user denied some permission", Toast.LENGTH_LONG).show()
        } else if (cameraPermissionsState.shouldShowRationale) {
            // user denied permission
            Toast.makeText(context, "user denied permission", Toast.LENGTH_LONG).show()
        } else {
            // user doesn't want to be asked again
            Toast.makeText(context, "user doesn't want to be asked again", Toast.LENGTH_LONG).show()
        }

        permissionsNotGrantedContent()
    }
}