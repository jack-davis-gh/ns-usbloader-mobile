package com.github.jack_davis_gh.ns_usbloader.core.model

import kotlinx.serialization.Serializable

@Serializable
data class NSFile(
    val uri: String,
    val name: String,
    val size: Long,
    val cuteSize: String,
    val isSelected: Boolean
)
