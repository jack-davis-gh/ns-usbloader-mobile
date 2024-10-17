package com.blogspot.developersu.ns_usbloader.home

import android.content.Context
import android.hardware.usb.UsbManager
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal object HomeModule {
    @Provides
    fun provideUsbManager(
        @ApplicationContext context: Context
    ): UsbManager = context.getUsbManager()

    @Provides
    fun provideWorkManager(
        @ApplicationContext context: Context
    ): WorkManager = WorkManager.getInstance(context)
}

fun Context.getUsbManager() = getSystemService(Context.USB_SERVICE) as UsbManager