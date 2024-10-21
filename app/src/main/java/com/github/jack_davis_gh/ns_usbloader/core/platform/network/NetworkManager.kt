package com.github.jack_davis_gh.ns_usbloader.core.platform.network

import android.net.ConnectivityManager
import java.net.Inet4Address

class NetworkManager(
    private val connectivityManager: ConnectivityManager
) {
    fun getIpAddress(): String? {
        val linkProperties = connectivityManager.getLinkProperties(connectivityManager.activeNetwork)
        val linkAddresses = linkProperties?.linkAddresses ?: return null
        return linkAddresses.first { it.address is Inet4Address }.address.hostAddress
//        return linkAddresses[0]?.address?.hostAddress
    }
}