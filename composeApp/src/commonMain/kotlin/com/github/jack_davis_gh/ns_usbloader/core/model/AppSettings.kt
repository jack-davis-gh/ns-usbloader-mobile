package com.github.jack_davis_gh.ns_usbloader.core.model

data class AppSettings(
    val theme: Theme,
    val activeProto: Protocol,
    val nsIp: String,
    val autoIp: Boolean,
    val phoneIp: String,
    val phonePort: Int
) {
    companion object {
        val Default = AppSettings(
            Theme.FollowSystem,
            Protocol.USB,
            "192.168.1.42",
            true,
            "192.168.1.142",
            6024,
        )
    }

    enum class Theme {
        FollowSystem,
        Day,
        Night
    }
}