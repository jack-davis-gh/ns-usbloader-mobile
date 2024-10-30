package com.github.jack_davis_gh.ns_usbloader

import android.app.Application
import android.content.Context
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.github.jack_davis_gh.ns_usbloader.core.transfer_protocol.TinfoilNet
import com.github.jack_davis_gh.ns_usbloader.core.transfer_protocol.TinfoilUsb
import com.github.jack_davis_gh.ns_usbloader.core.work_manager.CommunicationWorker
import me.tatarka.inject.annotations.Inject

class NSUsbloaderApplication: Application(), Configuration.Provider {
    val component: AndroidAppComponent = AndroidAppComponentMerged::class.create(this@NSUsbloaderApplication)

    private val workerFactory: NSUsbloaderFactory by lazy(component::nsWorkerFactory)

    override val workManagerConfiguration by lazy {
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()
    }
}

@Inject
class NSUsbloaderFactory(
    private val tinfoilUsb: TinfoilUsb,
    private val tinfoilNet: TinfoilNet,
): WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker = CommunicationWorker(appContext, workerParameters, tinfoilUsb, tinfoilNet)
}