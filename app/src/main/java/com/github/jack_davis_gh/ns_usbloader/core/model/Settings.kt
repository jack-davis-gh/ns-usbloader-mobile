package com.github.jack_davis_gh.ns_usbloader.core.model

data class Settings(
    val appTheme: Int, // TODO fix this, this should be representable via an Object or something, an int is not useful to look at
    val activeProto: Protocol,
    val nsIp: String,
    val autoIp: Boolean,
    val phoneIp: String,
    val phonePort: Int
)