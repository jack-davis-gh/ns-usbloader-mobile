package com.github.jack_davis_gh.ns_usbloader.core.platform.usb.di

import android.app.Application
import android.content.Context
import me.tatarka.inject.annotations.Provides
import android.hardware.usb.UsbManager as AndroidUsbManager

interface UsbComponent {
    @Provides
    fun providesUsbManager(application: Application) = application.getSystemService(Context.USB_SERVICE) as AndroidUsbManager
}
