package com.blogspot.developersu.ns_usbloader.core.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface FileDao {
    @Query("SELECT * FROM fileentity")
    fun getFiles(): Flow<List<FileEntity>>

    @Query("SELECT * FROM fileentity WHERE is_selected = 1")
    fun getSelectedFiles(): Flow<List<FileEntity>>

    @Upsert
    suspend fun upsertFiles(files: List<FileEntity>)

    @Upsert
    suspend fun upsertFile(file: FileEntity)
}