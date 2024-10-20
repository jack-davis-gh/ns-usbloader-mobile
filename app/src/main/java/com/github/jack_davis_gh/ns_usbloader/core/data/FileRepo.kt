package com.github.jack_davis_gh.ns_usbloader.core.data

import com.github.jack_davis_gh.ns_usbloader.core.database.FileDao
import com.github.jack_davis_gh.ns_usbloader.core.database.FileEntity
import com.github.jack_davis_gh.ns_usbloader.core.model.NSFile
import com.github.jack_davis_gh.ns_usbloader.core.model.asFileEntity
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FileRepo @Inject constructor(
    private val filesDao: FileDao
) {
    fun getFiles() = filesDao.getFiles().map { it.map(FileEntity::asNsFile) }

    suspend fun upsertFile(file: PlatformFile) {
        filesDao.upsertFile(file.asFileEntity())
    }

    suspend fun deleteFiles(files: List<NSFile>) {
        filesDao.deleteFiles(files.map { it.asFileEntity() })
    }
}