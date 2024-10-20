package com.github.jack_davis_gh.ns_usbloader.core.transfer_protocol.di

import android.content.Context
import android.net.ConnectivityManager
import com.github.jack_davis_gh.ns_usbloader.core.platform.file.FileManager
import com.github.jack_davis_gh.ns_usbloader.core.platform.network.NetworkManager
import com.github.jack_davis_gh.ns_usbloader.core.transfer_protocol.TinfoilNet
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