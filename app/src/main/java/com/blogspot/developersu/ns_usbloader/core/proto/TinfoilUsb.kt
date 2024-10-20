package com.blogspot.developersu.ns_usbloader.core.proto

import com.blogspot.developersu.ns_usbloader.core.model.NSFile
import com.blogspot.developersu.ns_usbloader.core.platform.file.FileManager
import com.blogspot.developersu.ns_usbloader.core.platform.usb.UsbManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.coroutines.coroutineContext

class TinfoilUsb(
    private val transfer: UsbManager,
    private val fileManager: FileManager
) {
    private val magicPacket = byteArrayOf(0x54, 0x55, 0x43, 0x30) // eq. 'TUC0' @ UTF-8 (actually ASCII lol, u know what I mean)

    suspend fun run(files: List<NSFile>): Result<Unit> {
        try {
            transfer.open()
                .onFailure { return Result.failure(it) }
            sendFiles(files)
                .onFailure { return Result.failure(it) }
        } finally {
            transfer.close()
        }
        return Result.success(Unit)
    }

    private suspend fun sendFiles(files: List<NSFile>): Result<Unit> {
//            status =
//                context.resources.getString(R.string.status_uploaded) // Don't change status that is already set to FAILED TODO: FIX

        // Send what NSP will be transferred
        //Collect file names
        val nspListNames = files.map(NSFile::name).joinToString(separator = "\n").toByteArray() // android's .getBytes() default == UTF8
        val nspListSize = ByteBuffer.allocate(4)
            .order(ByteOrder.LITTLE_ENDIAN) // integer = 4 bytes; BTW Java is stored in big-endian format
            .putInt(nspListNames.size) // This way we obtain length in int converted to byte array in correct Big-endian order. Trust me.
            .array()

        // Send list of NSP files:
        // Proceed "TUL0"
        transfer.writeUsb(byteArrayOf(0x54, 0x55, 0x4c, 0x30))
            .onFailure { return Result.failure(Exception("TF Send list of files: handshake failure")) }

        // Sending NSP list
         // size of the list we're going to transfer goes... + 8 zero bytes goes (Padding)...
        transfer.writeUsb(nspListSize)
            .onFailure { return Result.failure(Exception("TF Send list of files: [send list length]")) }

        transfer.writeUsb(ByteArray(8))
            .onFailure { return Result.failure(Exception("TF Send list of files: [padding]")) }

        transfer.writeUsb(nspListNames) // list of the names goes...
            .onFailure { return Result.failure(Exception("TF Send list of files: [send list itself]")) }

    // After we sent commands to NS, this chain starts
        while (coroutineContext.isActive) {
            val receivedArray = transfer.readUsb()
                .getOrElse { return Result.failure(it) }

            // Bytes from 0 to 3 should contain 'magic' TUC0, so must be verified like this
            if (!receivedArray.copyOfRange(0, 4).contentEquals(magicPacket))
                continue

            // 8th to 12th(explicits) bytes in returned data stands for command ID as unsigned integer (Little-endian). Actually, we have to compare arrays here, but in real world it can't be greater then 0/1/2, thus:
            // BTW also protocol specifies 4th byte to be 0x00 kinda indicating that that this command is valid. But, as you may see, never happens other situation when it's not = 0.
            when (receivedArray[8].toInt()) {
                0x00 -> return Result.success(Unit) // 0x00 - exit - All interaction with USB device should be ended (expected);
                0x01, 0x02 -> { //0x01 - file range; 0x02 unknown bug on backend side (dirty hack).
                    fileRangeCmd(files)
                        .onFailure { return Result.failure(it) }
                }
            }
        }
        return Result.success(Unit)
    }

    /**
     * This is what returns requested file (files)
     * Executes multiple times
     * @return 'true' if everything is ok
     * 'false' is error/exception occurs
     */
    private suspend fun fileRangeCmd(files: List<NSFile>): Result<Unit> {
        // Here we take information of what other side wants
        val receivedArray = transfer.readUsb()
            .getOrElse { return Result.failure(Exception("Unable to read requested file from ns.")) }

        // range_offset of the requested file. In the beginning it will be 0x10.
        val receivedRangeSize = ByteBuffer
            .wrap(receivedArray.copyOfRange(0, 8))
            .order(ByteOrder.LITTLE_ENDIAN)
            .getLong()

        val receivedRangeSizeRAW = receivedArray.copyOfRange(0, 8)
        val receivedRangeOffset = ByteBuffer.wrap(receivedArray.copyOfRange(8, 16))
            .order(ByteOrder.LITTLE_ENDIAN).getLong()

        // Requesting UTF-8 file name required:
        val receivedArray2 = transfer.readUsb().getOrElse {
            return Result.failure(Exception("Unable to read file name from usb")) }
        val receivedRequestedNSP: String = receivedArray2.toString(Charsets.UTF_8)

        // Sending response header
        val replyArray = magicPacket + byteArrayOf(
            0x01, 0x00, 0x00, 0x00,  // CMD_TYPE_RESPONSE = 1
            0x01, 0x00, 0x00, 0x00
        ) + receivedRangeSizeRAW + ByteArray(12)

        transfer.writeUsb(replyArray)
            .onFailure { return Result.failure(
                Exception("Failed to send response header")) }

        // Get receivedRangeSize in 'RAW' format exactly as it has been received. It's simply.

        try {
            val inputStream = files
                .firstOrNull { it.name == receivedRequestedNSP }
                ?.let { fileManager.openInputStream(it).getOrNull() }
                ?: return Result.failure(Exception("Unable to open file."))

            val bufferedInStream = BufferedInputStream(inputStream)

            var readBuf: ByteArray //= new byte[1048576];        // eq. Allocate 1mb

            if (withContext(Dispatchers.IO) {
                    bufferedInStream.skip(receivedRangeOffset)
                } != receivedRangeOffset) {
                return Result.failure(Exception("TF Requested skip is out of file size. Nothing to transmit."))
            }

            var readFrom: Long = 0
            // 'End Offset' equal to receivedRangeSize.
            var readPice = 16384 // 8388608 = 8Mb
//            var updateProgressPeriods = 0

            while (coroutineContext.isActive && readFrom < receivedRangeSize) {
                if ((readFrom + readPice) >= receivedRangeSize) readPice =
                    (receivedRangeSize - readFrom).toInt() // TODO: Troubles could raise here


                readBuf = ByteArray(readPice) // TODO: not perfect moment, consider refactoring.

                if (withContext(Dispatchers.IO) {
                        bufferedInStream.read(readBuf)
                    } != readPice) {
                    return Result.failure(Exception("TF Reading of stream suddenly ended"))
                }
                //write to USB
                transfer.writeUsb(readBuf)
                    .onFailure { return Result.failure(
                        Exception("TF Failure during NSP transmission.")) }

                readFrom += readPice.toLong()

//                if (updateProgressPeriods++ % 1024 == 0)  // Update progress bar after every 16mb goes to NS
//                    updateProgressBar(((readFrom + 1) / (receivedRangeSize / 100 + 1)).toInt()) // This shit takes too much time

//                Log.i("LPR", "CO: "+readFrom+"RRS: "+receivedRangeSize+"RES: "+(readFrom+1/(receivedRangeSize/100+1)));
            }

            withContext(Dispatchers.IO) {
                bufferedInStream.close()
            }
//            resetProgressBar()
        } catch (ioe: IOException) {
            return Result.failure(Exception("TF IOException: " + ioe.message))
        }
        return Result.success(Unit)
    }
}