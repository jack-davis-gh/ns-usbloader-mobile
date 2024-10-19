package com.blogspot.developersu.ns_usbloader.core.model

import kotlinx.serialization.Serializable
import java.util.Locale

@Serializable
data class NSFile(
    val uri: String,
    val name: String,
    val size: Long,
    val isSelected: Boolean = false,
    val status: String = ""
) {
    val cuteSize: String
        get() {
            val (size: Double, unit: String) = if (size > 1_000_000_000) {
                Pair(size / 1_000_000_000.0, "GB")
            } else if (size > 1_000_000.0) {
                Pair(size / 1_000_000.0, "MB")
            } else {
                Pair(size / 1_000.0, "KB")
            }
            return "${String.format(Locale.getDefault(), "%.2f", size)} $unit"
        }
}
