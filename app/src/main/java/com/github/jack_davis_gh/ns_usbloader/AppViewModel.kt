package com.github.jack_davis_gh.ns_usbloader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.jack_davis_gh.ns_usbloader.core.common.stateIn
import com.github.jack_davis_gh.ns_usbloader.core.datastore.SettingsStore
import kotlinx.coroutines.flow.map
import me.tatarka.inject.annotations.Inject

@Inject
class AppViewModel(
    settingsStore: SettingsStore
): ViewModel() {
    val theme = settingsStore.appSettings
        .map { it.theme }
        .stateIn(viewModelScope, 0)
}