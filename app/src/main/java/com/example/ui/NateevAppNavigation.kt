package com.example.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

sealed class Screen(val route: String) {
    object Camera : Screen("camera_screen")
    object Gallery : Screen("gallery_screen")
    object Settings : Screen("settings_screen")
}

@Composable
fun NateevAppNavigation() {
    val navController = rememberNavController()
    val cameraViewModel: CameraViewModel = viewModel()
    val preferences by cameraViewModel.appPreferences.collectAsState()
    val hardwareProfile by cameraViewModel.hardwareProfile.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.Camera.route
    ) {
        composable(Screen.Camera.route) {
            CameraScreen(
                viewModel = cameraViewModel,
                onNavigateToGallery = { navController.navigate(Screen.Gallery.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.Gallery.route) {
            GalleryGridScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                currentPreferences = preferences,
                hardwareProfile = hardwareProfile,
                onPreferenceChanged = { cameraViewModel.updatePreferences(it) },
                onBackClicked = { navController.popBackStack() }
            )
        }
    }
}
