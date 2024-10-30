package com.github.jack_davis_gh.ns_usbloader

import android.content.Context
import android.os.Build
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import me.tatarka.inject.annotations.KmpComponentCreate

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
    override val version: String = BuildConfig.VERSION_NAME
}

actual fun getPlatform(): Platform = AndroidPlatform()

//internal lateinit var application: NSUsbloaderApplication
//
//actual val appComponent: ApplicationComponent by lazy {
//    AndroidApplicationComponent(application)
//}
//
//fun createDataStore(context: Context): DataStore<Preferences> = createDataStore(
//    producePath = { context.filesDir.resolve(dataStoreFileName).absolutePath }
//)