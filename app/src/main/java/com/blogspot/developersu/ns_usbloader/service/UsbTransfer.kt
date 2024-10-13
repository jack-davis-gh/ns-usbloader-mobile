package com.blogspot.developersu.ns_usbloader.service

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import android.os.ResultReceiver

internal abstract class UsbTransfer(
    resultReceiver: ResultReceiver,
    context: Context,
    usbDevice: UsbDevice,
    usbManager: UsbManager?
) :
    TransferTask(resultReceiver, context) {
    private val deviceConnection: UsbDeviceConnection
    private val usbInterface: UsbInterface
    private val epIn: UsbEndpoint
    private val epOut: UsbEndpoint

    init {
        if (usbManager == null) {
            finish()
            throw Exception("This table must go")
        }

        usbInterface = usbDevice.getInterface(0)
        epIn = usbInterface.getEndpoint(0) // For bulk read
        epOut = usbInterface.getEndpoint(1) // For bulk write

        deviceConnection = usbManager!!.openDevice(usbDevice)

        if (!deviceConnection.claimInterface(usbInterface, false)) {
            issueDescription = "USB: failed to claim interface"
            throw Exception("USB: failed to claim interface")
        }
    }

    /**
     * Sending any byte array to USB device
     * @return 'false' if no issues
     * 'true' if errors happened
     */
    fun writeUsb(message: ByteArray): Boolean {
        var bytesWritten: Int
        while (!interrupt) {
            bytesWritten = deviceConnection.bulkTransfer(
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
    fun readUsb(): ByteArray? {
        val readBuffer = ByteArray(512)
        var readResult: Int
        while (!interrupt) {
            readResult =
                deviceConnection.bulkTransfer(epIn, readBuffer, 512, 1000) // timeout 0 - unlimited
            if (readResult > 0) return readBuffer.copyOf(readResult)
        }
        return null
    }

    fun finish() {
        deviceConnection.releaseInterface(usbInterface)
        deviceConnection.close()
    }
}
