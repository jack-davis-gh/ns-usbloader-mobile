package com.blogspot.developersu.ns_usbloader.core.data

import com.blogspot.developersu.ns_usbloader.core.database.FileDao
import com.blogspot.developersu.ns_usbloader.core.database.FileEntity
import com.blogspot.developersu.ns_usbloader.core.model.NSFile
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FileRepo @Inject constructor(
    private val filesDao: FileDao
) {
    fun getFiles() = filesDao.getFiles().map { it.map(FileEntity::asNsFile) }

    fun getSelectedFile() = filesDao.getSelectedFiles()

    suspend fun upsertFile(file: PlatformFile) {
        filesDao.upsertFile(file.asFileEntity())
    }

    suspend fun upsertFile(file: NSFile) {
        filesDao.upsertFile(file.asFileEntity())
    }
}