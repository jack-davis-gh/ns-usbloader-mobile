package com.blogspot.developersu.ns_usbloader.core.usb

import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import kotlin.concurrent.Volatile

sealed interface UsbTransferStatus {
    data class Open(
        val usbInterface: UsbInterface,
        val epIn: UsbEndpoint,
        val epOut: UsbEndpoint,
        val connection: UsbDeviceConnection
    ): UsbTransferStatus
    data object Closed: UsbTransferStatus
}

class UsbTransfer(
    private val usbManager: UsbManager
) {

    @Volatile
    private var status: UsbTransferStatus = UsbTransferStatus.Closed

    fun open() {
        if (status != UsbTransferStatus.Closed) {
            throw Exception("USB connection is already open.")
        }

        val device = usbManager.deviceList.values
            .filter { device -> device.vendorId == 1406 && device.productId == 12288 }
            .firstOrNull() ?: throw Exception("Unable to find Ns over USB.")

        val usbInterface = device.getInterface(0)
        val epIn = usbInterface.getEndpoint(0) // For bulk read
        val epOut = usbInterface.getEndpoint(1) // For bulk write
        val connection = usbManager.openDevice(device).also {
            if (!it.claimInterface(usbInterface, false)) {
                throw Exception("USB: failed to claim interface")
            }
        }

        status = UsbTransferStatus.Open(usbInterface, epIn, epOut, connection)
    }

    /**
     * Sending any byte array to USB device
     * @return 'false' if no issues
     * 'true' if errors happened
     */
    fun writeUsb(message: ByteArray): Boolean = status.let {
        when (it) {
            UsbTransferStatus.Closed -> throw Exception("USB Connection is not open.")
            is UsbTransferStatus.Open -> {
                while (true) {
                    if (status == UsbTransferStatus.Closed) { throw Exception("USB Connection interrupted") }
                    val bytesWritten = it.connection.bulkTransfer(
                        it.epOut,
                        message,
                        message.size,
                        5050
                    ) // timeout 0 - unlimited
                    if (bytesWritten != 0) return (bytesWritten != message.size)
                }
                return false
            }
        }
    }

    /**
     * Reading what USB device responded.
     * @return byte array if data read successful
     * 'null' if read failed
     */
    fun readUsb(): ByteArray = status.let {
        when(it) {
            UsbTransferStatus.Closed -> throw Exception("USB Connection is not open.")
            is UsbTransferStatus.Open -> {
                val readBuffer = ByteArray(512)
                var readResult: Int = 0
                var i = 0
                while (true) {
                    if (status == UsbTransferStatus.Closed) { throw Exception("USB Connection interrupted") }
                    readResult = it.connection.bulkTransfer(
                        it.epIn,
                        readBuffer,
                        512,
                        1000
                    ) // timeout 0 - unlimited
                    i++
                    if (readResult > 0) break
                }

                readBuffer.copyOf(readResult)
            }
        }
    }

    fun close() = status.let {
        when (it) {
            UsbTransferStatus.Closed -> throw Exception("USB Connection is not open.")
            is UsbTransferStatus.Open -> {
                status = UsbTransferStatus.Closed
                it.connection.releaseInterface(it.usbInterface)
                it.connection.close()
            }
        }
    }
}
