package com.blogspot.developersu.ns_usbloader.core.proto.di

import android.content.Context
import android.net.ConnectivityManager
import com.blogspot.developersu.ns_usbloader.core.platform.file.FileManager
import com.blogspot.developersu.ns_usbloader.core.platform.network.NetworkManager
import com.blogspot.developersu.ns_usbloader.core.proto.TinfoilNet
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal object TinfoilNetModule {
    @Provides
    fun providesTinfoilNet(
        @ApplicationContext context: Context
    ): TinfoilNet = TinfoilNet(
        FileManager(context.contentResolver),
        NetworkManager(context.getSystemService(ConnectivityManager::class.java))
    )
}