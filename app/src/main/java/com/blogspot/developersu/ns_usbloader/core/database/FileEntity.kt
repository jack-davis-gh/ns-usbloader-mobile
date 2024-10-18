package com.blogspot.developersu.ns_usbloader.core.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.blogspot.developersu.ns_usbloader.model.NSFile
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class FileEntity(
    @PrimaryKey val uri: String,
    val name: String,
    val size: Long,
    @ColumnInfo(name = "is_selected") val isSelected: Boolean = false
) {
    fun asNsFile() = NSFile(uriString = uri, name = name, size = size, isSelected = isSelected)
}