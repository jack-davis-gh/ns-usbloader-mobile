package com.blogspot.developersu.ns_usbloader.about

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import kotlinx.serialization.Serializable

@Serializable data object AboutUi

fun NavController.navigateToAbout(navOptions: NavOptions? = null) = navigate(route = AboutUi, navOptions)
