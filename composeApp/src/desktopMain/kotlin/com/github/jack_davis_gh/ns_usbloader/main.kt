package com.github.jack_davis_gh.ns_usbloader

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "NSUsbloader",
    ) {
        //App()
    }
}