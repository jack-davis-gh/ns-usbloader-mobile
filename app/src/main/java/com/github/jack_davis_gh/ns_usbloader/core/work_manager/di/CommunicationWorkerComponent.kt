package com.github.jack_davis_gh.ns_usbloader.core.work_manager.di

import android.app.Application
import androidx.work.WorkManager
import me.tatarka.inject.annotations.Provides

interface CommunicationWorkerComponent {
    @Provides
    fun providesWorkManager(application: Application) = WorkManager.getInstance(application)
}