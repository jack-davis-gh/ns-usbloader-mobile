package com.blogspot.developersu.ns_usbloader.settings

import android.net.InetAddresses
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>
): ViewModel() {
    companion object {
        val APP_THEME = intPreferencesKey("APP_THEME")
        val NS_IP_KEY = stringPreferencesKey("NS_IP")
        val AUTO_IP_KEY = booleanPreferencesKey("AUTO_IP")
        val PHONE_IP_KEY = stringPreferencesKey("PHONE_IP")
        val PHONE_PORT_KEY = intPreferencesKey("PHONE_PORT")
    }

    var themeSelection by mutableIntStateOf(0)
        private set

    var nsIpString by mutableStateOf("192.168.1.42")
        private set

    var autoIpBool by mutableStateOf(true)
        private set

    var phoneIpString by mutableStateOf("192.168.1.142")
        private set

    var phonePortString by mutableStateOf("6024")
        private set

    init {
        viewModelScope.launch {
            val data = dataStore.data.first()
            data[APP_THEME]?.let { themeSelection = it }
            data[NS_IP_KEY]?.let { nsIpString = it }
            data[AUTO_IP_KEY]?.let { autoIpBool = it }
            data[PHONE_IP_KEY]?.let { phoneIpString = it }
            data[PHONE_PORT_KEY]?.let { phonePortString = it.toString() }
        }
    }

    fun updateThemeSelection(selection: Int) {
        if (selection in 0..2) {
            themeSelection = selection
        }
    }

    fun updateNsIp(ip: String) {
        nsIpString = ip
        if (InetAddresses.isNumericAddress(ip)) {
            viewModelScope.launch {
                dataStore.edit { prefs ->
                    prefs[NS_IP_KEY] = ip
                }
            }
        } else {
            // TODO Error
        }
    }

    fun updateAutoIp(autoIp: Boolean) {
        autoIpBool = autoIp
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[AUTO_IP_KEY] = autoIp
            }
        }
    }

    fun updatePhoneIp(phoneIp: String) {
        phoneIpString = phoneIp

        if (InetAddresses.isNumericAddress(phoneIp)) {
            viewModelScope.launch {
                dataStore.edit { prefs ->
                    prefs[PHONE_IP_KEY] = phoneIp
                }
            }
        } else {
            // TODO Error
        }
    }

    fun updatePort(portStr: String) {
        phonePortString = portStr
        if (portStr.toIntOrNull() != null) {
            val port = portStr.toInt()

            if (port in 1024..65535) {
                viewModelScope.launch {
                    dataStore.edit { prefs ->
                        prefs[PHONE_PORT_KEY] = port
                    }
                }
                return
            }
        }

        // TODO Error
    }
}