package com.github.jack_davis_gh.ns_usbloader

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.jack_davis_gh.ns_usbloader.home.HomeScreen
import com.github.jack_davis_gh.ns_usbloader.home.HomeUi
import com.github.jack_davis_gh.ns_usbloader.settings.SettingsScreen
import com.github.jack_davis_gh.ns_usbloader.settings.SettingsUi
import com.github.jack_davis_gh.ns_usbloader.settings.navigateToSettings
import com.github.jack_davis_gh.ns_usbloader.ui.theme.AppTheme
import me.tatarka.inject.annotations.Inject

@Inject
@Composable
fun App(mainViewModel: () -> AppViewModel) {
    val viewModel = viewModel { mainViewModel() }
    val theme by viewModel.theme.collectAsStateWithLifecycle()
    val isDarkTheme = when (theme) {
        0 -> isSystemInDarkTheme()
        1 -> false
        else -> true
    }

    val appComp = (LocalContext.current.applicationContext as NSUsbloaderApplication).component
    AppTheme(isDarkTheme) {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = HomeUi) {
            composable<HomeUi> { HomeScreen(homeViewModel = appComp.homeViewModel, onSettingsClicked = navController::navigateToSettings) }
            composable<SettingsUi> { SettingsScreen(settingsViewModel = appComp.settingsViewModel, onBackPressed = { navController.popBackStack() }) }
        }
    }
}

