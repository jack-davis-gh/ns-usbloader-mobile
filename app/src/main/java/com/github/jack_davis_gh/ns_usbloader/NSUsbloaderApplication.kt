package com.github.jack_davis_gh.ns_usbloader

import android.app.Application
import android.content.Context
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.github.jack_davis_gh.ns_usbloader.core.proto.TinfoilNet
import com.github.jack_davis_gh.ns_usbloader.core.proto.TinfoilUsb
import com.github.jack_davis_gh.ns_usbloader.core.work_manager.CommunicationWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class NSUsbloaderApplication: Application(), Configuration.Provider {
    @Inject lateinit var workerFactory: NSUsbloaderFactory

    override val workManagerConfiguration by lazy {
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()
    }
}

class NSUsbloaderFactory @Inject constructor(
    private val tinfoilUsb: TinfoilUsb,
    private val tinfoilNet: TinfoilNet,
): WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker = CommunicationWorker(appContext, workerParameters, tinfoilUsb, tinfoilNet)
}