package com.github.jack_davis_gh.ns_usbloader

class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
    override val version: String
        get() = ""
}

actual fun getPlatform(): Platform = JVMPlatform()