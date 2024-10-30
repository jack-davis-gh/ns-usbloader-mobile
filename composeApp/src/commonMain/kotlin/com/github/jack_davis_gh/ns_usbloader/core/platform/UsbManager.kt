package com.github.jack_davis_gh.ns_usbloader.core.platform

data class UsbManagerException(override val message: String): Exception(message)

interface UsbManager {
    fun open()

    suspend fun writeUsb(message: ByteArray, exceptionMsg: String = "Write ByteArray to usb failed.\nByteArray = $message")

    suspend fun readUsb(): ByteArray

    fun close()
}
