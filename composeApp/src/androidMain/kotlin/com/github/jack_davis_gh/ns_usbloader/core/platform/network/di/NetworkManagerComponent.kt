package com.github.jack_davis_gh.ns_usbloader.core.platform.network.di

import android.app.Application
import android.net.ConnectivityManager
import me.tatarka.inject.annotations.Provides

interface NetworkComponent {
    @Provides
    fun providesConnectivityManager(application: Application): ConnectivityManager = application.getSystemService(ConnectivityManager::class.java)
}
