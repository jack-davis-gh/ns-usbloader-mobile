package com.blogspot.developersu.ns_usbloader.service

import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.hardware.usb.UsbManager
import com.blogspot.developersu.ns_usbloader.core.usb.UsbTransfer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal object ServiceModule {
    @Provides
    fun providesUsbTransfer(
        @ApplicationContext context: Context
    ): UsbTransfer = UsbTransfer(
        context.getSystemService(Context.USB_SERVICE) as UsbManager
    )

    @Provides
    fun provideNotificationManager(
        @ApplicationContext context: Context
    ): NotificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
}