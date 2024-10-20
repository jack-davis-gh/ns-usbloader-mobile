package com.github.jack_davis_gh.ns_usbloader.core.platform.usb.di

import android.content.Context
import android.hardware.usb.UsbManager as AndroidUsbManager
import com.github.jack_davis_gh.ns_usbloader.core.platform.usb.UsbManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal object UsbManagerModule {
    @Provides
    fun provideUsbManager(
        usbManager: AndroidUsbManager
    ): UsbManager = UsbManager(usbManager)

    @Provides
    fun providesAndroidUsbManager(
        @ApplicationContext context: Context
    ): AndroidUsbManager = context.getSystemService(Context.USB_SERVICE) as AndroidUsbManager
}