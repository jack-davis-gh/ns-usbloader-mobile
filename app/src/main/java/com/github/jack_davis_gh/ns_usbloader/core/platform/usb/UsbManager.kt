package com.github.jack_davis_gh.ns_usbloader.core.platform.usb

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager as AndroidUsbManager
import com.github.jack_davis_gh.ns_usbloader.core.common.asResult
import com.github.jack_davis_gh.ns_usbloader.core.common.mapToResult
import kotlinx.coroutines.isActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.coroutineContext

class UsbManager(
    private val usbManager: AndroidUsbManager
) {
    private val mutex = Mutex()
    private var usbInterface: UsbInterface? = null
    private var epIn: UsbEndpoint? = null
    private var epOut: UsbEndpoint? = null
    private var connection: UsbDeviceConnection? = null

    fun getNs(): Result<UsbDevice> = usbManager.deviceList.values
            .firstOrNull { device -> device.vendorId == 1406 && device.productId == 12288 }
            .let { device ->
                if (device != null && usbManager.hasPermission(device)) {
                    device
                } else null
            }
            .mapToResult(exception = Exception("Couldn't find ns"))

    suspend fun open(): Result<Unit> {
        mutex.withLock {
            val device = getNs().getOrElse { return Result.failure(it) }
            usbInterface = device.getInterface(0)
            epIn = usbInterface?.getEndpoint(0) // For bulk read
            epOut = usbInterface?.getEndpoint(1) // For bulk write
            connection = usbManager.openDevice(device).also {
                if (!it.claimInterface(usbInterface, true)) {
                    return Result.failure(Exception("USB: failed to claim interface"))
                }
            }
        }
        return Result.success(Unit)
    }

    /**
     * Sending any byte array to USB device
     * @return 'false' if no issues
     * 'true' if errors happened
     */
    suspend fun writeUsb(message: ByteArray): Result<Unit> {
        val exp = Exception("Write ByteArray to usb failed.\nByteArray = $message")

        mutex.withLock {
            val connection = this.connection ?: return Result.failure(Exception("Usb connection is not open"))
            while (coroutineContext.isActive) {
                val bytesWritten = connection.bulkTransfer(
                    epOut,
                    message,
                    message.size,
                    5050
                ) // timeout 0 - unlimited
                if (bytesWritten != 0)
                    return (bytesWritten == message.size).asResult(exp)
            }
        }

        return Result.failure(exp)
    }

    /**
     * Reading what USB device responded.
     * @return byte array if data read successful
     * 'null' if read failed
     */
    suspend fun readUsb(): Result<ByteArray> {
        val readBuffer = ByteArray(512)

        mutex.withLock {
            val connection = this.connection ?: return Result.failure(Exception("Usb connection is not open"))
            while (coroutineContext.isActive) {
                val readResult = connection.bulkTransfer(
                    epIn,
                    readBuffer,
                    512,
                    1000
                ) // timeout 0 - unlimited
                if (readResult > 0) return Result.success(readBuffer.copyOf(readResult))
            }
        }

        return Result.failure(Exception("Read ByteArray from usb failed."))
    }

    suspend fun close(): Result<Unit> {
        mutex.withLock {
            val connection = this.connection ?: return Result.failure(Exception("Usb connection is not open"))
            connection.releaseInterface(usbInterface)
            connection.close()
        }
        return Result.success(Unit)
    }
}
