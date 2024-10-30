package com.github.jack_davis_gh.ns_usbloader.core.platform

import com.github.jack_davis_gh.ns_usbloader.core.model.NSFile
import me.tatarka.inject.annotations.Inject
import java.io.InputStream

interface FileManager {
    fun openInputStream(file: NSFile): Result<InputStream?>
}