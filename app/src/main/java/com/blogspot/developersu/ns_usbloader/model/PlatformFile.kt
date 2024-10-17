package com.blogspot.developersu.ns_usbloader.model

import io.github.vinceglb.filekit.core.PlatformFile

fun PlatformFile.asNSFile() = NSFile(uriString = uri.toString(), name = name, size = getSize() ?: 0L)