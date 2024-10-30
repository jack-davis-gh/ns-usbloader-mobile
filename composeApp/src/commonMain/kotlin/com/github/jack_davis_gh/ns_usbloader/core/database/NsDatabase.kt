package com.github.jack_davis_gh.ns_usbloader.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [FileEntity::class], version = 1)
abstract class NsDatabase: RoomDatabase() {
    abstract fun fileDao(): FileDao
}