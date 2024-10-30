package com.github.jack_davis_gh.ns_usbloader.core.platform.usb

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import com.github.jack_davis_gh.ns_usbloader.core.platform.UsbManager
import android.hardware.usb.UsbManager as AndroidInternalUsbManager
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.tatarka.inject.annotations.Inject
import kotlin.coroutines.coroutineContext

data class UsbManagerException(override val message: String): Exception(message)

@Inject
class AndroidUsbManager(
    private val usbManager: AndroidInternalUsbManager
): UsbManager {
    private val mutex = Mutex()
    private var usbInterface: UsbInterface? = null
    private var epIn: UsbEndpoint? = null
    private var epOut: UsbEndpoint? = null
    private var connection: UsbDeviceConnection? = null

    fun getNs(): UsbDevice? = usbManager.deviceList.values
            .firstOrNull { device -> device.vendorId == 1406 && device.productId == 12288 }
            .let { device ->
                if (device != null && usbManager.hasPermission(device)) {
                    device
                } else null
            }

    override fun open() = runBlocking {
        val device = getNs() ?: throw UsbManagerException("Couldn't find ns")

        mutex.withLock {
            usbInterface = device.getInterface(0)
            epIn = usbInterface?.getEndpoint(0) // For bulk read
            epOut = usbInterface?.getEndpoint(1) // For bulk write
            connection = usbManager.openDevice(device)
        }

        if (connection?.claimInterface(usbInterface, true) == false) {
            throw UsbManagerException("USB: failed to claim interface")
        }
    }

    /**
     * Sending any byte array to USB device
     * @return 'false' if no issues
     * 'true' if errors happened
     */
    override suspend fun writeUsb(message: ByteArray, exceptionMsg: String) {
        val connection = this.connection ?: throw UsbManagerException("Usb connection is not open")
        while (coroutineContext.isActive) {
            val bytesWritten = connection.bulkTransfer(
                epOut,
                message,
                message.size,
                5050
            ) // timeout 0 - unlimited
            if (bytesWritten != 0) {
                if (bytesWritten != message.size) {
                    throw UsbManagerException(exceptionMsg)
                } else break
            }
        }
    }

    /**
     * Reading what USB device responded.
     * @return byte array if data read successful
     * 'null' if read failed
     */
    override suspend fun readUsb(): ByteArray {
        val readBuffer = ByteArray(512)

        val connection = connection ?: throw UsbManagerException("Usb connection is not open")
        while (coroutineContext.isActive) {
            val readResult = connection.bulkTransfer(
                epIn,
                readBuffer,
                512,
                1000
            ) // timeout 0 - unlimited
            if (readResult > 0) return readBuffer.copyOf(readResult)
        }

        throw UsbManagerException("Read ByteArray from usb failed.")
    }

    override fun close() = runBlocking {
        mutex.withLock {
            connection?.releaseInterface(usbInterface) ?: throw UsbManagerException("Usb connection is not open")
            connection?.close()
            usbInterface = null
            epIn = null
            epOut = null
            connection = null
        }
    }
}
