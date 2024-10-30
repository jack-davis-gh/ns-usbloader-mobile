package com.github.jack_davis_gh.ns_usbloader.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.jack_davis_gh.ns_usbloader.core.datastore.SettingsStore
import com.github.jack_davis_gh.ns_usbloader.core.model.Protocol
import com.github.jack_davis_gh.ns_usbloader.core.model.AppSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

sealed interface SettingsUiState {
    data class Success(val appSettings: AppSettings): SettingsUiState
    data object Loading: SettingsUiState
}

interface SettingsPrefsUpdateCallback {
    companion object {
        val Empty = object : SettingsPrefsUpdateCallback {}
    }
    fun updateThemeSelection(selection: AppSettings.Theme) {}
    fun updateProtocol(proto: Protocol) {}
    fun updateNsIp(ip: String) {}
    fun updateAutoIp(autoIp: Boolean) {}
    fun updatePhoneIp(phoneIp: String) {}
    fun updatePhonePort(port: Int) {}
}

@Inject
class SettingsViewModel(
    private val settingsStore: SettingsStore
): ViewModel() {
    val state: StateFlow<SettingsUiState> = settingsStore.appSettings
        .map { SettingsUiState.Success(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsUiState.Loading
        )

    fun updateSettings(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch { block() }
    }

    val updateStore = object : SettingsPrefsUpdateCallback {
        override fun updateThemeSelection(selection: AppSettings.Theme) = updateSettings {
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