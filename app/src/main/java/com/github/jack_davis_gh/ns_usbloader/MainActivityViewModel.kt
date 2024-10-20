package com.github.jack_davis_gh.ns_usbloader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.jack_davis_gh.ns_usbloader.core.common.stateIn
import com.github.jack_davis_gh.ns_usbloader.core.datastore.SettingsStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    settingsStore: SettingsStore
): ViewModel() {
    val theme = settingsStore.settings
        .map { it.appTheme }
        .stateIn(viewModelScope, 0)
}