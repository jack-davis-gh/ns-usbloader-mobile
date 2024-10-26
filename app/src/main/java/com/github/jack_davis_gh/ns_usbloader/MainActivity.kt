package com.github.jack_davis_gh.ns_usbloader

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import io.github.vinceglb.filekit.core.FileKit

class MainActivity : AppCompatActivity() {
    private val appComponent by lazy {
        (application as NSUsbloaderApplication).component
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FileKit.init(this)
        enableEdgeToEdge()

        setContent {
            App(mainViewModel = appComponent.appViewModel)
        }
    }
}
