package com.github.jack_davis_gh.ns_usbloader

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath

interface Platform {
    val name: String
    val version: String
}

expect fun getPlatform(): Platform

fun createDataStore(producePath: () -> String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        produceFile = { producePath().toPath() }
    )

internal val dataStoreFileName = "${getPlatform().name}.preferences_pb"
