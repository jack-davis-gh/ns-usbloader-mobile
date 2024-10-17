package com.blogspot.developersu.ns_usbloader.core.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.blogspot.developersu.ns_usbloader.NsConstants
import com.blogspot.developersu.ns_usbloader.NsConstants.json
import com.blogspot.developersu.ns_usbloader.model.NSFile
import com.blogspot.developersu.ns_usbloader.core.usb.UsbTransfer
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.BufferedInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

@HiltWorker
class TinfoilUsbWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    @Assisted private val usbTransfer: UsbTransfer,
): CoroutineWorker(appContext, workerParams) {
    private val foregroundInfo = createForegroundInfo()
    private val magicPacket = byteArrayOf(0x54, 0x55, 0x43, 0x30) // eq. 'TUC0' @ UTF-8 (actually ASCII lol, u know what I mean)

    override suspend fun getForegroundInfo(): ForegroundInfo = foregroundInfo

    override suspend fun doWork(): Result {
        val jsonStr = inputData.getString(NsConstants.SERVICE_CONTENT_NSP_LIST) ?: return Result.failure()
        val files: List<NSFile> = json.decodeFromString(jsonStr)
        setForeground(foregroundInfo)

        try {
            usbTransfer.open()

            sendListOfNSP(files)

//        if (
            proceedCommands(files)
            //            )  // REPORT SUCCESS
//            status =
//                context.resources.getString(R.string.status_uploaded) // Don't change status that is already set to FAILED TODO: FIX
        } catch (e: Exception) {
//            Log.error("TinfoilUsbWorker", e.message ?: "Unknown Exception")
            val outData = Data.Builder()
                .putString("WORKER_EXCEPTION", e.message ?: "Unknown Exception")
                .build()
            return Result.failure(outData)
        } finally {
            usbTransfer.close()
        }

        return Result.success()
    }

    // Send what NSP will be transferred
    private fun sendListOfNSP(files: List<NSFile>) {
        //Collect file names
        val nspListNames = files.map(NSFile::name).joinToString(separator = "\n").toByteArray() // android's .getBytes() default == UTF8
        val nspListSize = ByteBuffer.allocate(4)
            .order(ByteOrder.LITTLE_ENDIAN) // integer = 4 bytes; BTW Java is stored in big-endian format
            .putInt(nspListNames.size) // This way we obtain length in int converted to byte array in correct Big-endian order. Trust me.
            .array()

        // Send list of NSP files:
        // Proceed "TUL0"
        if (usbTransfer.writeUsb(byteArrayOf(0x54, 0x55, 0x4c, 0x30))) {
            throw Exception("TF Send list of files: handshake failure")
        }

        // Sending NSP list
        val nspListPacket = nspListSize + ByteArray(8) // size of the list we're going to transfer goes... + 8 zero bytes goes (Padding)...
        if (usbTransfer.writeUsb(nspListPacket)) {
            throw Exception("TF Send list of files: [send list length]")
        }

        if (usbTransfer.writeUsb(nspListNames)) { // list of the names goes...
            throw Exception("TF Send list of files: [send list itself]")
        }
    }

    // After we sent commands to NS, this chain starts
    private fun proceedCommands(files: List<NSFile>): Boolean {
        while (true) {
            val receivedArray = usbTransfer.readUsb()

            // Bytes from 0 to 3 should contain 'magic' TUC0, so must be verified like this
            if (!receivedArray.copyOfRange(0, 4).contentEquals(magicPacket))
                continue

            // 8th to 12th(explicits) bytes in returned data stands for command ID as unsigned integer (Little-endian). Actually, we have to compare arrays here, but in real world it can't be greater then 0/1/2, thus:
            // BTW also protocol specifies 4th byte to be 0x00 kinda indicating that that this command is valid. But, as you may see, never happens other situation when it's not = 0.
            when (receivedArray[8].toInt()) {
                0x00 -> return true // 0x00 - exit - All interaction with USB device should be ended (expected);
                0x01, 0x02 -> { //0x01 - file range; 0x02 unknown bug on backend side (dirty hack).
                    if (!fileRangeCmd(files))  // issueDescription inside
                        return false
                }
            }
        }
    }

    /**
     * This is what returns requested file (files)
     * Executes multiple times
     * @return 'true' if everything is ok
     * 'false' is error/exception occurs
     */
    private fun fileRangeCmd(files: List<NSFile>): Boolean {
        // Here we take information of what other side wants
        val receivedArray = usbTransfer.readUsb()

        // range_offset of the requested file. In the begining it will be 0x10.
        val receivedRangeSize = ByteBuffer.wrap(receivedArray.copyOfRange(0, 8)).order(
            ByteOrder.LITTLE_ENDIAN
        ).getLong()
        val receivedRangeSizeRAW = receivedArray.copyOfRange(0, 8)
        val receivedRangeOffset = ByteBuffer.wrap(receivedArray.copyOfRange(8, 16))
            .order(ByteOrder.LITTLE_ENDIAN).getLong()

        // Requesting UTF-8 file name required:
        val receivedArray2 = usbTransfer.readUsb()
        val receivedRequestedNSP: String = receivedArray2.toString(Charsets.UTF_8)

        // Sending response header
        val replyArray = magicPacket + byteArrayOf(
            0x01, 0x00, 0x00, 0x00,  // CMD_TYPE_RESPONSE = 1
            0x01, 0x00, 0x00, 0x00
        ) + receivedRangeSizeRAW + ByteArray(12)
        usbTransfer.writeUsb(replyArray)
        // Get receivedRangeSize in 'RAW' format exactly as it has been received. It's simply.

        try {
            val bufferedInStream = files
                .filter { it.name == receivedRequestedNSP }
                .firstOrNull()?.let {
                    BufferedInputStream(applicationContext.contentResolver.openInputStream(it.uri))
                } ?: throw Exception("Unable to open file.")


            var readBuf: ByteArray //= new byte[1048576];        // eq. Allocate 1mb

            if (bufferedInStream.skip(receivedRangeOffset) != receivedRangeOffset) {
//                issueDescription = "TF Requested skip is out of file size. Nothing to transmit."
                return false
            }

            var readFrom: Long = 0
            // 'End Offset' equal to receivedRangeSize.
            var readPice = 16384 // 8388608 = 8Mb
//            var updateProgressPeriods = 0

            while (readFrom < receivedRangeSize) {
                if ((readFrom + readPice) >= receivedRangeSize) readPice =
                    (receivedRangeSize - readFrom).toInt() // TODO: Troubles could raise here


                readBuf = ByteArray(readPice) // TODO: not perfect moment, consider refactoring.

                if (bufferedInStream.read(readBuf) != readPice) {
//                    issueDescription = "TF Reading of stream suddenly ended"
                    return false
                }
                //write to USB
                usbTransfer.writeUsb(readBuf) // Otherwise "TF Failure during NSP transmission."
                readFrom += readPice.toLong()

//                if (updateProgressPeriods++ % 1024 == 0)  // Update progress bar after every 16mb goes to NS
//                    updateProgressBar(((readFrom + 1) / (receivedRangeSize / 100 + 1)).toInt()) // This shit takes too much time

//                Log.i("LPR", "CO: "+readFrom+"RRS: "+receivedRangeSize+"RES: "+(readFrom+1/(receivedRangeSize/100+1)));
            }
            bufferedInStream.close()

//            resetProgressBar()
        } catch (ioe: IOException) {
//            issueDescription = "TF IOException: " + ioe.message
            return false
        }
        return true
    }
}