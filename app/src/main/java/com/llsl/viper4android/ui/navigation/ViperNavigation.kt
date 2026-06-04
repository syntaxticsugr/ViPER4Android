package com.llsl.viper4android.ui.navigation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.llsl.viper4android.ui.screens.main.MainScreen
import com.llsl.viper4android.ui.screens.main.MainViewModel
import com.llsl.viper4android.ui.screens.settings.SettingsScreen

/** Duration of the slide between Main and Settings. */
private const val NAV_ANIM_MS = 300

@Composable
fun ViperNavigation() {
    val navController = rememberNavController()
    // Hoist the ViewModel here so Main and Settings share one instance and stay in sync.
    val viewModel: MainViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = "main",
        enterTransition = {
            slideInHorizontally(
                animationSpec = tween(NAV_ANIM_MS, easing = LinearEasing),
                initialOffsetX = { it },
            )
        },
        exitTransition = {
            // Hold the outgoing page in place beneath the incoming one for the duration.
            slideOutHorizontally(
                animationSpec = tween(NAV_ANIM_MS, easing = LinearEasing),
                targetOffsetX = { 0 },
            )
        },
        popEnterTransition = {
            // The revealed page stays put beneath the one sliding away.
            slideInHorizontally(
                animationSpec = tween(NAV_ANIM_MS, easing = LinearEasing),
                initialOffsetX = { 0 },
            )
        },
        popExitTransition = {
            slideOutHorizontally(
                animationSpec = tween(NAV_ANIM_MS, easing = LinearEasing),
                targetOffsetX = { it },
            )
        },
    ) {
        composable("main") {
            MainScreen(
                viewModel = viewModel,
                onOpenSettings = { navController.navigate("settings") },
            )
        }
        composable("settings") {
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
