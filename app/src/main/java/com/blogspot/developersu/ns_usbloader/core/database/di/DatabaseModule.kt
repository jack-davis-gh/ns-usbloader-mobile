package com.blogspot.developersu.ns_usbloader.core.database.di

import android.content.Context
import androidx.room.Room
import com.blogspot.developersu.ns_usbloader.core.database.NsDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {
    @Provides
    @Singleton
    fun providesNsDatabase(
        @ApplicationContext context: Context,
    ): NsDatabase = Room.databaseBuilder(
        context,
        NsDatabase::class.java,
        "ns-database",
    ).build()
}
