package com.blogspot.developersu.ns_usbloader.core.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.blogspot.developersu.ns_usbloader.core.datastore.SettingsStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal object SettingsStoreModule {
    private val Context.prefsDataStore: DataStore<Preferences> by preferencesDataStore(name = "NS_USB_Loader")

    @Provides
    fun provideSettingsStore(
        @ApplicationContext context: Context
    ): SettingsStore = SettingsStore(context.prefsDataStore)
}