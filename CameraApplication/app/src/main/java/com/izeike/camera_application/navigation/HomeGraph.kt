package com.izeike.camera_application.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.izeike.camera_application.camera.CameraScreen
import com.izeike.camera_application.gallery.GalleryScreen
import com.izeike.camera_application.image.ImageScreen
import com.izeike.camera_application.navigation.Destination.CAMERA_SCREEN
import com.izeike.camera_application.navigation.Destination.GALLERY_SCREEN
import com.izeike.camera_application.navigation.Destination.HOME
import com.izeike.camera_application.navigation.Destination.IMAGE_SCREEN
import com.izeike.camera_application.navigation.Destination.VIDEO_PREVIEW_SCREEN
import com.izeike.camera_application.navigation.Destination.VIDEO_SCREEN
import com.izeike.camera_application.video.VideoScreen
import com.izeike.camera_application.videopreview.VideoPreviewScreen

fun NavGraphBuilder.homeGraph(navController: NavController) {
    navigation(startDestination = CAMERA_SCREEN, route = HOME) {
        composable(
            route = CAMERA_SCREEN,
            enterTransition = { slideLeftEnterTransition() },
            exitTransition = { slideLeftExitTransition() },
            popEnterTransition = { slideRightEnterTransition() },
            popExitTransition = { slideRightExitTransition() }
        ) {
            CameraScreen(navController = navController)
        }
        composable(
            route = GALLERY_SCREEN,
            enterTransition = { slideLeftEnterTransition() },
            exitTransition = { slideLeftExitTransition() },
            popEnterTransition = { slideRightEnterTransition() },
            popExitTransition = { slideRightExitTransition() }
        ) {
            GalleryScreen(navController)
        }
        composable(
            route = IMAGE_SCREEN,
            enterTransition = { slideLeftEnterTransition() },
            exitTransition = { slideLeftExitTransition() },
            popEnterTransition = { slideRightEnterTransition() },
            popExitTransition = { slideRightExitTransition() }
        ) { backStackEntry ->
            ImageScreen(backStackEntry.arguments?.getString("imageUri") ?: "")
        }
        composable(
            route = VIDEO_SCREEN,
            enterTransition = { slideLeftEnterTransition() },
            exitTransition = { slideLeftExitTransition() },
            popEnterTransition = { slideRightEnterTransition() },
            popExitTransition = { slideRightExitTransition() }
        ) {
            VideoScreen(navController)
        }
        composable(
            route = VIDEO_PREVIEW_SCREEN,
            enterTransition = { slideLeftEnterTransition() },
            exitTransition = { slideLeftExitTransition() },
            popEnterTransition = { slideRightEnterTransition() },
            popExitTransition = { slideRightExitTransition() }
        ) { backStackEntry ->
            VideoPreviewScreen(backStackEntry.arguments?.getString("uri") ?: "")
        }
    }
}