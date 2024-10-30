package com.github.jack_davis_gh.ns_usbloader.core.database.di

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.github.jack_davis_gh.ns_usbloader.core.database.FileDao
import com.github.jack_davis_gh.ns_usbloader.core.database.NsDatabase
import kotlinx.coroutines.Dispatchers
import me.tatarka.inject.annotations.Provides
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.ContributesTo
import software.amazon.lastmile.kotlin.inject.anvil.MergeComponent
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

@MergeComponent(AppScope::class)
@SingleIn(AppScope::class)
interface DatabaseBuilder {
    val builder: RoomDatabase.Builder<NsDatabase>
}

@SingleIn(AppScope::class)
@MergeComponent(AppScope::class)
interface DatabaseComponent {
//    @Provides
//    expect fun provideNsDatabase(): Room
    //        application,
//        NsDatabase::class.java,
//        "ns-database",
//    ).build()

//    expect fun provideNsDbBuilder(): RoomDatabase.Builder<NsDatabase>

    @Provides
    fun provideNsDatabase(
        builder: DatabaseBuilder
    ) = builder.builder
        .fallbackToDestructiveMigrationOnDowngrade(false)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}

@SingleIn(AppScope::class)
@MergeComponent(AppScope::class)
interface FileDaoComponent {
    @Provides
    fun provideFileDao(database: NsDatabase): FileDao = database.fileDao()
}
