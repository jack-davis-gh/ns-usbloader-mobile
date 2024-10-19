package com.blogspot.developersu.ns_usbloader.core.data

import com.blogspot.developersu.ns_usbloader.core.database.FileEntity
import com.blogspot.developersu.ns_usbloader.core.model.NSFile

fun NSFile.asFileEntity() = FileEntity(uri = uri, name = name, size = size, cuteSize = cuteSize)