package com.github.jack_davis_gh.ns_usbloader.core.platform.usb.di

import android.app.Application
import android.content.Context
import com.github.jack_davis_gh.ns_usbloader.core.platform.UsbManager
import com.github.jack_davis_gh.ns_usbloader.core.platform.di.UsbManagerProvider
import com.github.jack_davis_gh.ns_usbloader.core.platform.usb.AndroidUsbManager
import me.tatarka.inject.annotations.Inject
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.ContributesBinding
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn
import android.hardware.usb.UsbManager as AndroidInternalUsbManager

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class UsbComponent(application: Application): UsbManagerProvider {
    override val usbManager: UsbManager = AndroidUsbManager(
        application.getSystemService(Context.USB_SERVICE) as AndroidInternalUsbManager)
}
