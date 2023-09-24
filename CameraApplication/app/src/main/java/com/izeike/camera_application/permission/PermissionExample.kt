package com.izeike.camera_application.permission

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@Composable
fun PermissionExample1() {

    val context = LocalContext.current

    var isPermissionGranted by remember {
        mutableStateOf(false)
    }

    val permission = Manifest.permission.CAMERA

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        isPermissionGranted = isGranted
    }

    LaunchedEffect(Unit) {
        val permissionCheckResult = ContextCompat.checkSelfPermission(context, permission)
        if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
            isPermissionGranted = true
        } else {
            launcher.launch(permission)
        }
    }

    if (isPermissionGranted) {
        // show camera
    } else {
        // handle no permission
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionExample2() {
    val cameraPermissionsState = rememberPermissionState(
        Manifest.permission.CAMERA
    )

    LaunchedEffect(Unit) {
        cameraPermissionsState.launchPermissionRequest()
    }

    if (cameraPermissionsState.status.isGranted) {
        // show camera
    } else if (cameraPermissionsState.status.shouldShowRationale) {
        // user denied permission
    } else {
        // user doesn't want to be asked again
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionExample3() {
    val cameraPermissionsState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    )

    LaunchedEffect(Unit) {
        cameraPermissionsState.launchMultiplePermissionRequest()
    }

    val allPermissionsRevoked =
        cameraPermissionsState.permissions.size ==
                cameraPermissionsState.revokedPermissions.size

    if(cameraPermissionsState.allPermissionsGranted) {
        // show camera
    } else if (!allPermissionsRevoked) {
        // user denied some permission
    } else if (cameraPermissionsState.shouldShowRationale) {
        // user denied permission
    } else {
        // user doesn't want to be asked again
    }
}