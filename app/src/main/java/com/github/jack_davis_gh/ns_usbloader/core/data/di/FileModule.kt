package com.github.jack_davis_gh.ns_usbloader.core.data.di

import com.github.jack_davis_gh.ns_usbloader.core.data.FileRepo
import com.github.jack_davis_gh.ns_usbloader.core.database.FileDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal object FileModule {
    @Provides
    fun providesFileRepo(
        fileDao: FileDao
    ): FileRepo = FileRepo(fileDao)
}