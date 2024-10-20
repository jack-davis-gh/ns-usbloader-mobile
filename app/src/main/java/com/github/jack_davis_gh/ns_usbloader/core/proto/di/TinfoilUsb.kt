package com.github.jack_davis_gh.ns_usbloader.core.proto.di

import android.content.Context
import android.hardware.usb.UsbManager as AndroidUsbManager
import com.github.jack_davis_gh.ns_usbloader.core.platform.file.FileManager
import com.github.jack_davis_gh.ns_usbloader.core.platform.usb.UsbManager
import com.github.jack_davis_gh.ns_usbloader.core.proto.TinfoilUsb
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal object TinfoilUsbModule {
    @Provides
    fun providesTinfoilUsb(
        @ApplicationContext context: Context,
        usbManager: AndroidUsbManager
    ): TinfoilUsb = TinfoilUsb(
        UsbManager(usbManager),
        FileManager(context.contentResolver)
    )
}