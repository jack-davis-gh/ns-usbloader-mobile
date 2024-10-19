package com.blogspot.developersu.ns_usbloader.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.blogspot.developersu.ns_usbloader.core.model.NSFile
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class FileEntity(
    @PrimaryKey val uri: String,
    val name: String,
    val size: Long,
    val cuteSize: String
) {
    fun asNsFile() = NSFile(uri, name, size, cuteSize, false)
}