package com.blogspot.developersu.ns_usbloader.core.data

import com.blogspot.developersu.ns_usbloader.core.database.FileEntity
import io.github.vinceglb.filekit.core.PlatformFile

fun PlatformFile.asFileEntity() = FileEntity(
    uri = uri.toString(),
    name = name,
    size = getSize() ?: 0L
)