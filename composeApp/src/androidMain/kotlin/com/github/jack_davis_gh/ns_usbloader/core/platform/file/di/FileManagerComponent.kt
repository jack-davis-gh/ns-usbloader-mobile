package com.github.jack_davis_gh.ns_usbloader.core.platform.file.di

import android.app.Application
import android.content.ContentResolver
import me.tatarka.inject.annotations.Provides

interface FileManagerComponent {
    @Provides
    fun providesContentResolver(application: Application): ContentResolver = application.contentResolver
}