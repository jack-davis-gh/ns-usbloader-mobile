package com.github.jack_davis_gh.ns_usbloader.core.platform.di

import com.github.jack_davis_gh.ns_usbloader.core.platform.UsbManager
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.ContributesTo
import software.amazon.lastmile.kotlin.inject.anvil.MergeComponent

interface UsbManagerProvider {
    val usbManager: UsbManager
}

@MergeComponent(AppScope::class)
interface UsbManagerProviderComponent {
    val usbManager: UsbManagerProvider
}