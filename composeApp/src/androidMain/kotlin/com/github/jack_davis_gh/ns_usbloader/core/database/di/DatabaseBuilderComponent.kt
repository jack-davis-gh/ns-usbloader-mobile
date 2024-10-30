package com.github.jack_davis_gh.ns_usbloader.core.database.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.github.jack_davis_gh.ns_usbloader.core.database.NsDatabase
import me.tatarka.inject.annotations.Inject
import me.tatarka.inject.annotations.Provides
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.ContributesBinding
import software.amazon.lastmile.kotlin.inject.anvil.ContributesTo
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

@Inject
@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
abstract class DatabaseBuilderComponent(
    private val application: Application
): DatabaseBuilder {
    override val builder: RoomDatabase.Builder<NsDatabase> by lazy {
        val dbFile = application.getDatabasePath("ns-usbloader.db")
        Room.databaseBuilder<NsDatabase>(
            context = application,
            name = dbFile.absolutePath
        )
    }
}
