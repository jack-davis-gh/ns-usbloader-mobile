package com.blogspot.developersu.ns_usbloader.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blogspot.developersu.ns_usbloader.core.datastore.SettingsStore
import com.blogspot.developersu.ns_usbloader.core.model.Protocol
import com.blogspot.developersu.ns_usbloader.core.model.Settings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SettingsUiState {
    data class Success(val settings: Settings): SettingsUiState
    data object Loading: SettingsUiState
}

interface SettingsPrefsUpdateCallback {
    companion object {
        val Empty = object : SettingsPrefsUpdateCallback {}
    }
    fun updateThemeSelection(selection: Int) {}
    fun updateProtocol(proto: Protocol) {}
    fun updateNsIp(ip: String) {}
    fun updateAutoIp(autoIp: Boolean) {}
    fun updatePhoneIp(phoneIp: String) {}
    fun updatePhonePort(port: Int) {}
}



@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsStore: SettingsStore
): ViewModel(), SettingsPrefsUpdateCallback {
    val state: StateFlow<SettingsUiState> = settingsStore.settings
        .map { SettingsUiState.Success(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsUiState.Loading
        )

    fun updateSettings(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch { block() }
    }

    val updateCallbacks = object : SettingsPrefsUpdateCallback {
        override fun updateThemeSelection(selection: Int) = updateSettings {
            settingsStore.update(appTheme = selection)
        }

        override fun updateProtocol(proto: Protocol) = updateSettings {
            settingsStore.update(activeProto = proto)
        }

        override fun updateNsIp(ip: String) = updateSettings {
            settingsStore.update(nsIp = ip)
        }

        override fun updateAutoIp(autoIp: Boolean) = updateSettings {
            settingsStore.update(autoIp = autoIp)
        }

        override fun updatePhoneIp(phoneIp: String) = updateSettings {
            settingsStore.update(phoneIp = phoneIp)
        }

        override fun updatePhonePort(port: Int) = updateSettings {
            settingsStore.update(phonePort = port)
        }
    }
}