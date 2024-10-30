package com.github.jack_davis_gh.ns_usbloader.core.model

import com.github.jack_davis_gh.ns_usbloader.core.database.FileEntity
import io.github.vinceglb.filekit.core.PlatformFile
import java.util.Locale

private fun Long.asCuteSize(): String {
    val (size: Double, unit: String) = if (this > 1_000_000_000) {
        Pair(this / 1_000_000_000.0, "GB")
    } else if (this > 1_000_000.0) {
        Pair(this / 1_000_000.0, "MB")
    } else {
        Pair(this / 1_000.0, "KB")
    }
    return "${String.format(Locale.getDefault(), "%.2f", size)} $unit"
}

fun PlatformFile.asFileEntity() = FileEntity(
    uri = "", //uri.toString(),
    name = name,
    size = getSize() ?: 0L,
    cuteSize = getSize()?.asCuteSize() ?: "0 B"
)