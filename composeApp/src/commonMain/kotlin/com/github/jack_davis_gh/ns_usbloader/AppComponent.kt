package com.github.jack_davis_gh.ns_usbloader

import com.github.jack_davis_gh.ns_usbloader.home.HomeViewModel
import com.github.jack_davis_gh.ns_usbloader.settings.SettingsViewModel
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.MergeComponent
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

@MergeComponent(AppScope::class)
@SingleIn(AppScope::class)
interface AppComponent {
    val appViewModel: () -> AppViewModel
    val homeViewModel: () -> HomeViewModel
    val settingsViewModel: () -> SettingsViewModel
}

//@ApplicationScope
//@Component
//abstract class ApplicationComponent: DatabaseComponent, FileManagerComponent, HomeComponent, NetworkComponent,
//    SettingsComponent, SettingsStoreComponent, UsbComponent {
//

//
//}