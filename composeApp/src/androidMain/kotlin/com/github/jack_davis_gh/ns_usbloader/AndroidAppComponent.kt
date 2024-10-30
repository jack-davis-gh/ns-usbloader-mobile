package com.github.jack_davis_gh.ns_usbloader

import android.app.Application
import com.github.jack_davis_gh.ns_usbloader.core.datastore.di.SettingsStoreComponent
import com.github.jack_davis_gh.ns_usbloader.core.platform.usb.di.UsbComponent
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Inject
import me.tatarka.inject.annotations.Provides
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.ContributesBinding
import software.amazon.lastmile.kotlin.inject.anvil.ContributesTo
import software.amazon.lastmile.kotlin.inject.anvil.MergeComponent
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
abstract class AndroidAppComponent(
    @get:Provides val application: Application
): AppComponent {
    abstract val nsWorkerFactory: NSUsbloaderFactory
}