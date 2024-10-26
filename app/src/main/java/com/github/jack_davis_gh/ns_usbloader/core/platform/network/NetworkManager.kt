package com.github.jack_davis_gh.ns_usbloader.core.platform.network

import android.net.ConnectivityManager
import me.tatarka.inject.annotations.Inject
import java.net.Inet4Address

@Inject
class NetworkManager(
    private val connectivityManager: ConnectivityManager
) {
    fun getIpAddress(): String? {
        val linkProperties = connectivityManager.getLinkProperties(connectivityManager.activeNetwork)
        val linkAddresses = linkProperties?.linkAddresses ?: return null
        return linkAddresses.first { it.address is Inet4Address }.address.hostAddress
    }
}