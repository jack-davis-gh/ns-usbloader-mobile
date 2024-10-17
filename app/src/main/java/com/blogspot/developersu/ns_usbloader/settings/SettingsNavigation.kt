package com.blogspot.developersu.ns_usbloader.settings

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import kotlinx.serialization.Serializable

@Serializable data object SettingsUi

fun NavController.navigateToSettings(navOptions: NavOptions? = null) = navigate(route = SettingsUi, navOptions)