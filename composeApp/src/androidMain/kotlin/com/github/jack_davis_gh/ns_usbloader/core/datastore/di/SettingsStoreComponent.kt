package com.github.jack_davis_gh.ns_usbloader.core.datastore.di

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import me.tatarka.inject.annotations.Provides
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.MergeComponent
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

private val Context.prefsDataStore: DataStore<Preferences> by preferencesDataStore(name = "NS_USB_Loader")

@MergeComponent(AppScope::class)
@SingleIn(AppScope::class)
interface SettingsStoreComponent {
    @Provides
    fun provideDataStore(application: Application): DataStore<Preferences> = application.prefsDataStore
}
