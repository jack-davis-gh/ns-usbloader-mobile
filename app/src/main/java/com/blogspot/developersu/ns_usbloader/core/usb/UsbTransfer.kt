package com.blogspot.developersu.ns_usbloader.core.usb

import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import kotlin.concurrent.Volatile

class UsbTransfer(
    private val usbManager: UsbManager
) {
    private val device by lazy {
        usbManager.deviceList.values
            .filter { device -> device.vendorId == 1406 && device.productId == 12288 }
            .firstOrNull() ?: throw Exception("Unable to find Ns over USB.")
    }

    @Volatile private var isStopped: Boolean = true
    private lateinit var usbInterface: UsbInterface
    private lateinit var epIn: UsbEndpoint
    private lateinit var epOut: UsbEndpoint
    private lateinit var connection: UsbDeviceConnection

    fun open() {
        usbInterface = device.getInterface(0)
        epIn = usbInterface.getEndpoint(0) // For bulk read
        epOut = usbInterface.getEndpoint(1) // For bulk write
        connection = usbManager.openDevice(device).also {
            if (!it.claimInterface(usbInterface, false)) {
                throw Exception("USB: failed to claim interface")
            }
        }
        isStopped = false
    }


    /**
     * Sending any byte array to USB device
     * @return 'false' if no issues
     * 'true' if errors happened
     */
    fun writeUsb(message: ByteArray): Boolean {
        while (!isStopped) {
            val bytesWritten = connection.bulkTransfer(
                epOut,
                message,
                message.size,
                5050
            ) // timeout 0 - unlimited
            if (bytesWritten != 0) return (bytesWritten != message.size)
        }
        return false
    }

    /**
     * Reading what USB device responded.
     * @return byte array if data read successful
     * 'null' if read failed
     */
    fun readUsb(): ByteArray {
        if (isStopped) throw Exception("How?")
        val readBuffer = ByteArray(512)
        var readResult: Int = 0
        while (!isStopped) {
            readResult = connection.bulkTransfer(
                epIn,
                readBuffer,
                512,
                1000
            ) // timeout 0 - unlimited
            if (readResult > 0) break
        }

        return readBuffer.copyOf(readResult)
    }

    fun close() {
        isStopped = true
        connection.releaseInterface(usbInterface)
        connection.close()
    }
}
