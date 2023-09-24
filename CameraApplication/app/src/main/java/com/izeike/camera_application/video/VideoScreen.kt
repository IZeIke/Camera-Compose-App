package com.izeike.camera_application.video

import android.Manifest
import android.content.Context
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.izeike.camera_application.camera.ActionButton

@Composable
fun VideoScreen(
    navController: NavController,
    viewModel: VideoViewModel = viewModel()
) {
    VideoScreen(
        cameraSelector = viewModel.cameraSelector,
        isStartRecord = viewModel.isStartRecord,
        isPauseRecord = viewModel.isPauseRecord,
        setUpVideoCapture = { previewView, lifecycleOwner, cameraSelector ->
            viewModel.setUpVideoCapture(
                previewView,
                lifecycleOwner,
                cameraSelector
            )
        },
        onChangeCameraClicked = {
            viewModel.changeCamera()
        },
        onRecordVideoClicked = {
            viewModel.startRecording()
        },
        onPauseRecordVideo = {
            viewModel.pauseRecording()
        },
        goToCameraScreen = {
            navController.popBackStack()
        }
    )
}

@Composable
fun VideoScreen(
    cameraSelector: CameraSelector,
    isStartRecord: Boolean,
    isPauseRecord: Boolean,
    setUpVideoCapture: suspend (previewView: PreviewView, lifecycleOwner: LifecycleOwner, cameraSelector: CameraSelector) -> Unit,
    onChangeCameraClicked: () -> Unit,
    onRecordVideoClicked: () -> Unit,
    onPauseRecordVideo: () -> Unit,
    goToCameraScreen: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView: PreviewView = remember { PreviewView(context) }

    RequestAudioPermission(
        context = context,
        permissionsNotGrantedContent = {
            Text(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .wrapContentHeight(),
                text = "Audio not Granted",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
        }
    ) {

        LaunchedEffect(cameraSelector) {
            setUpVideoCapture(
                previewView,
                lifecycleOwner,
                cameraSelector
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {

            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = {
                    previewView
                }
            )

            ActionButton(
                modifier = Modifier
                    .padding(start = 20.dp, top = 20.dp)
                    .wrapContentSize()
                    .padding(5.dp)
                    .align(Alignment.TopStart),
                text = "Camera"
            ) {
                goToCameraScreen()
            }

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
                text = if(!isStartRecord) "Record Video" else "Stop Record Video",
                color = if(!isStartRecord) MaterialTheme.colorScheme.primary else Color.Red,
                onClick = {
                    onRecordVideoClicked()
                }
            )

            if(isStartRecord) {
                ActionButton(
                    modifier = Modifier
                        .padding(bottom = 20.dp, end = 20.dp)
                        .size(100.dp, 100.dp)
                        .align(Alignment.BottomEnd),
                    text = if(!isPauseRecord) "Pause" else "Resume",
                    onClick = {
                        onPauseRecordVideo()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestAudioPermission(
    context: Context,
    permissionsNotGrantedContent: @Composable () -> Unit,
    permissionsGrantedContent: @Composable () -> Unit
) {
    val audioPermissionsState = rememberPermissionState(
        Manifest.permission.RECORD_AUDIO
    )

    LaunchedEffect(Unit) {
        audioPermissionsState.launchPermissionRequest()
    }

    if (audioPermissionsState.status.isGranted) {
        permissionsGrantedContent()
    } else {
        if (audioPermissionsState.status.shouldShowRationale) {
            // user denied permission
            Toast.makeText(context, "user denied permission", Toast.LENGTH_LONG).show()
        } else {
            // user doesn't want to be asked again
            Toast.makeText(context, "user doesn't want to be asked again", Toast.LENGTH_LONG)
                .show()
        }
        permissionsNotGrantedContent()
    }
}