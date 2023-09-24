package com.izeike.camera_application.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavBackStackEntry

fun (AnimatedContentTransitionScope<NavBackStackEntry>).slideLeftEnterTransition(): EnterTransition {
    return slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Companion.Left,
        animationSpec = tween(500)
    )
}

fun (AnimatedContentTransitionScope<NavBackStackEntry>).slideLeftExitTransition(): ExitTransition {
    return slideOutOfContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Companion.Left,
        animationSpec = tween(500)
    )
}

fun (AnimatedContentTransitionScope<NavBackStackEntry>).slideRightEnterTransition(): EnterTransition {
    return slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Companion.Right,
        animationSpec = tween(500)
    )
}

fun (AnimatedContentTransitionScope<NavBackStackEntry>).slideRightExitTransition(): ExitTransition {
    return slideOutOfContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Companion.Right,
        animationSpec = tween(500)
    )
}

fun (AnimatedContentTransitionScope<NavBackStackEntry>).fadeInTransition(): EnterTransition {
    return fadeIn(
        animationSpec = tween(700)
    )
}

fun (AnimatedContentTransitionScope<NavBackStackEntry>).fadeOutTransition(): ExitTransition {
    return fadeOut(
        animationSpec = tween(700)
    )
}