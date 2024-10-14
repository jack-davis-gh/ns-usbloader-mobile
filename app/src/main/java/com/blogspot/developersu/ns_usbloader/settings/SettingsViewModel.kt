package com.blogspot.developersu.ns_usbloader.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SettingsUiState {
    data class Success(
        val appTheme: Int, // TODO fix this, this should be representable via an Object or something, an int is not useful to look at
        val nsIp: String,
        val autoIp: Boolean,
        val phoneIp: String,
        val phonePort: Int,
    ): SettingsUiState
    data object Loading: SettingsUiState
}

interface SettingsPrefsUpdateCallback {
    companion object {
        val Empty = object : SettingsPrefsUpdateCallback {}
    }
    fun updateThemeSelection(selection: Int) {}
    fun updateNsIp(ip: String) {}
    fun updateAutoIp(autoIp: Boolean) {}
    fun updatePhoneIp(phoneIp: String) {}
    fun updatePhonePort(phonePort: Int) {}
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>
): ViewModel(), SettingsPrefsUpdateCallback {
    companion object {
        val APP_THEME = intPreferencesKey("APP_THEME")
        val NS_IP_KEY = stringPreferencesKey("NS_IP")
        val AUTO_IP_KEY = booleanPreferencesKey("AUTO_IP")
        val PHONE_IP_KEY = stringPreferencesKey("PHONE_IP")
        val PHONE_PORT_KEY = intPreferencesKey("PHONE_PORT")
    }

    val state: StateFlow<SettingsUiState> = dataStore.data.map { prefs ->
        SettingsUiState.Success(
            appTheme = prefs[APP_THEME] ?: 0,
            nsIp = prefs[NS_IP_KEY] ?: "192.168.1.42",
            autoIp = prefs[AUTO_IP_KEY] ?: true,
            phoneIp = prefs[PHONE_IP_KEY] ?: "192.168.1.142",
            phonePort = prefs[PHONE_PORT_KEY] ?: 6024
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState.Loading
    )

    val updateCallbacks = object : SettingsPrefsUpdateCallback {
        override fun updateThemeSelection(selection: Int) {
            viewModelScope.launch {
                dataStore.edit { prefs ->
                    prefs[APP_THEME] = selection
                }
            }
        }

        override fun updateNsIp(ip: String) {
            viewModelScope.launch {
                dataStore.edit { prefs ->
                    prefs[NS_IP_KEY] = ip
                }
            }
        }

        override fun updateAutoIp(autoIp: Boolean) {
            viewModelScope.launch {
                dataStore.edit { prefs ->
                    prefs[AUTO_IP_KEY] = autoIp
                }
            }
        }

        override fun updatePhoneIp(phoneIp: String) {
            viewModelScope.launch {
                dataStore.edit { prefs ->
                    prefs[PHONE_IP_KEY] = phoneIp
                }
            }
        }

        override fun updatePhonePort(port: Int) {
            viewModelScope.launch {
                dataStore.edit { prefs ->
                    prefs[PHONE_PORT_KEY] = port
                }
            }
        }
    }
}