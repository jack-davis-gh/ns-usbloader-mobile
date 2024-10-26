package com.github.jack_davis_gh.ns_usbloader

import android.app.Application
import com.github.jack_davis_gh.ns_usbloader.core.database.di.DatabaseComponent
import com.github.jack_davis_gh.ns_usbloader.core.datastore.di.SettingsStoreComponent
import com.github.jack_davis_gh.ns_usbloader.core.platform.file.di.FileManagerComponent
import com.github.jack_davis_gh.ns_usbloader.core.platform.network.di.NetworkComponent
import com.github.jack_davis_gh.ns_usbloader.core.platform.usb.di.UsbComponent
import com.github.jack_davis_gh.ns_usbloader.core.work_manager.di.CommunicationWorkerComponent
import com.github.jack_davis_gh.ns_usbloader.home.HomeComponent
import com.github.jack_davis_gh.ns_usbloader.settings.SettingsComponent
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import me.tatarka.inject.annotations.Scope

@Scope
annotation class ApplicationScope

@ApplicationScope
@Component
abstract class ApplicationComponent(
    @get:Provides val application: Application
): CommunicationWorkerComponent, DatabaseComponent, FileManagerComponent, HomeComponent, NetworkComponent,
    SettingsComponent, SettingsStoreComponent, UsbComponent {

    abstract val appViewModel: () -> AppViewModel
    abstract val nsWorkerFactory: NSUsbloaderFactory
}