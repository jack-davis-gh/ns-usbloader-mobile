package com.blogspot.developersu.ns_usbloader.core.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface FileDao {
    @Query("SELECT * FROM fileentity")
    fun getFiles(): Flow<List<FileEntity>>

    @Upsert
    suspend fun upsertFiles(files: List<FileEntity>)

    @Upsert
    suspend fun upsertFile(file: FileEntity)

    @Delete
    suspend fun deleteFiles(files: List<FileEntity>)
}