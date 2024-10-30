package com.github.jack_davis_gh.ns_usbloader.core.data

import com.github.jack_davis_gh.ns_usbloader.core.database.FileEntity
import com.github.jack_davis_gh.ns_usbloader.core.model.NSFile

fun NSFile.asFileEntity() = FileEntity(uri = uri, name = name, size = size, cuteSize = cuteSize)