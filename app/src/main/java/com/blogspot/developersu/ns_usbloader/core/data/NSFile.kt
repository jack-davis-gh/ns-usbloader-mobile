package com.blogspot.developersu.ns_usbloader.core.data

import com.blogspot.developersu.ns_usbloader.core.database.FileEntity
import com.blogspot.developersu.ns_usbloader.model.NSFile

fun NSFile.asFileEntity() = FileEntity(uri = uriString, name = name, size = size, isSelected = isSelected)