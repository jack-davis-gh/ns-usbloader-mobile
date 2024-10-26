package com.github.jack_davis_gh.ns_usbloader.core.database.di

import android.app.Application
import androidx.room.Room
import com.github.jack_davis_gh.ns_usbloader.core.database.FileDao
import com.github.jack_davis_gh.ns_usbloader.core.database.NsDatabase
import me.tatarka.inject.annotations.Provides

interface DatabaseComponent {
    @Provides
    fun provideNsDatabase(application: Application) = Room.databaseBuilder(
        application,
        NsDatabase::class.java,
        "ns-database",
    ).build()

    @Provides
    fun provideFileDao(database: NsDatabase): FileDao = database.fileDao()
}
