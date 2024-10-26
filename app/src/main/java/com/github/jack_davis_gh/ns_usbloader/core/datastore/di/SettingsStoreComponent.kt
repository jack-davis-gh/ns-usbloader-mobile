package com.github.jack_davis_gh.ns_usbloader.core.datastore.di

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.github.jack_davis_gh.ns_usbloader.ApplicationScope
import me.tatarka.inject.annotations.Provides

private val Context.prefsDataStore: DataStore<Preferences> by preferencesDataStore(name = "NS_USB_Loader")

interface SettingsStoreComponent {
    @ApplicationScope
    @Provides
    fun provideDataStore(application: Application): DataStore<Preferences> = application.prefsDataStore
}
