package com.github.jack_davis_gh.ns_usbloader.core.platform.file

import android.content.ContentResolver
import android.net.Uri
import com.github.jack_davis_gh.ns_usbloader.core.model.NSFile
import java.io.InputStream
import javax.inject.Inject

class FileManager @Inject constructor(
    private val contentResolver: ContentResolver
) {
    fun openInputStream(file: NSFile): Result<InputStream?> {
        val inputStream = contentResolver.openInputStream(Uri.parse(file.uri))
        return if (inputStream != null) {
            Result.success(inputStream)
        } else {
            Result.failure(Exception("Unable to open file, name = ${file.name} uri = ${file.uri}"))
        }
    }
}