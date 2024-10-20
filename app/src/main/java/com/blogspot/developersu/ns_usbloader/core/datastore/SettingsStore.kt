package com.blogspot.developersu.ns_usbloader.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.blogspot.developersu.ns_usbloader.NsConstants.json
import com.blogspot.developersu.ns_usbloader.core.model.Protocol
import com.blogspot.developersu.ns_usbloader.core.model.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import javax.inject.Inject

class SettingsStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val APP_THEME = intPreferencesKey("APP_THEME")
        val PROTO = stringPreferencesKey("PROTO")
        val NS_IP_KEY = stringPreferencesKey("NS_IP")
        val AUTO_IP_KEY = booleanPreferencesKey("AUTO_IP")
        val PHONE_IP_KEY = stringPreferencesKey("PHONE_IP")
        val PHONE_PORT_KEY = intPreferencesKey("PHONE_PORT")
    }

    val settings: Flow<Settings> = dataStore.data.map { prefs ->
        Settings(
            appTheme = prefs[APP_THEME] ?: 0,
            activeProto = prefs[PROTO]?.let { json.decodeFromString<Protocol>(it) } ?: Protocol.TinfoilUSB,
            nsIp = prefs[NS_IP_KEY] ?: "192.168.1.42",
            autoIp = prefs[AUTO_IP_KEY] ?: true,
            phoneIp = prefs[PHONE_IP_KEY] ?: "192.168.1.142",
            phonePort = prefs[PHONE_PORT_KEY] ?: 6024
        )
    }

    suspend fun update(
        appTheme: Int? = null,
        activeProto: Protocol? = null,
        nsIp: String? = null,
        autoIp: Boolean? = null,
        phoneIp: String? = null,
        phonePort: Int? = null
    ) {
        dataStore.edit { prefs ->
            if (appTheme != null)
                prefs[APP_THEME] = appTheme
            if (activeProto != null)
                prefs[PROTO] = json.encodeToString(activeProto)
            if (nsIp != null)
                prefs[NS_IP_KEY] = nsIp
            if (autoIp != null)
                prefs[AUTO_IP_KEY] = autoIp
            if (phoneIp != null)
                prefs[PHONE_IP_KEY] = phoneIp
            if (phonePort != null)
                prefs[PHONE_PORT_KEY] = phonePort
        }
    }
}