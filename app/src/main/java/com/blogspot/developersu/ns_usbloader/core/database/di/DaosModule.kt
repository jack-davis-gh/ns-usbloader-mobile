package com.blogspot.developersu.ns_usbloader.core.database.di

import com.blogspot.developersu.ns_usbloader.core.database.FileDao
import com.blogspot.developersu.ns_usbloader.core.database.NsDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal object DaosModule {
    @Provides
    fun providesFileDao(
        database: NsDatabase,
    ): FileDao = database.fileDao()
}