package com.github.jack_davis_gh.ns_usbloader.core.platform.network

import android.net.ConnectivityManager

class NetworkManager(
    private val connectivityManager: ConnectivityManager
) {
    fun getIpAddress(): String? {
        val linkProperties = connectivityManager.getLinkProperties(connectivityManager.activeNetwork)
        return linkProperties?.linkAddresses?.get(0)?.address?.hostAddress
    }
}