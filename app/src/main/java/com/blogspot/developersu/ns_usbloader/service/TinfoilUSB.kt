package com.blogspot.developersu.ns_usbloader.service

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.ResultReceiver
import com.blogspot.developersu.ns_usbloader.R
import com.blogspot.developersu.ns_usbloader.view.NSPElement
import java.io.BufferedInputStream
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Arrays

internal class TinfoilUSB(
    resultReceiver: ResultReceiver,
    context: Context,
    usbDevice: UsbDevice,
    usbManager: UsbManager?,
    private val nspElements: ArrayList<NSPElement>
) : UsbTransfer(resultReceiver, context, usbDevice, usbManager) {
    private val replyConstArray = byteArrayOf(
        0x54.toByte(), 0x55.toByte(), 0x43.toByte(), 0x30.toByte(),  // 'TUC0'
        0x01.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),  // CMD_TYPE_RESPONSE = 1
        0x01.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte()
    )

    override fun run(): Boolean {
        if (!sendListOfNSP()) {
            finish()
            return true
        }

        if (proceedCommands())  // REPORT SUCCESS
            status =
                context.resources.getString(R.string.status_uploaded) // Don't change status that is already set to FAILED TODO: FIX

        finish()

        return false
    }

    // Send what NSP will be transferred
    private fun sendListOfNSP(): Boolean {
        // Send list of NSP files:
        // Proceed "TUL0"
        if (writeUsb("TUL0".toByteArray())) {  // new byte[]{(byte) 0x54, (byte) 0x55, (byte) 0x76, (byte) 0x30} //"US-ASCII"?
            issueDescription = "TF Send list of files: handshake failure"
            return false
        }
        //Collect file names
        val nspListNamesBuilder = StringBuilder() // Add every title to one stringBuilder
        for (element in nspElements) {
            nspListNamesBuilder.append(element.filename) // And here we come with java string default encoding (UTF-16)
            nspListNamesBuilder.append('\n')
        }

        val nspListNames =
            nspListNamesBuilder.toString().toByteArray() // android's .getBytes() default == UTF8
        val byteBuffer = ByteBuffer.allocate(4)
            .order(ByteOrder.LITTLE_ENDIAN) // integer = 4 bytes; BTW Java is stored in big-endian format
        byteBuffer.putInt(nspListNames.size) // This way we obtain length in int converted to byte array in correct Big-endian order. Trust me.
        val nspListSize = byteBuffer.array()

        // Sending NSP list
        if (writeUsb(nspListSize)) {                                           // size of the list we're going to transfer goes...
            issueDescription = "TF Send list of files: [send list length]"
            return false
        }

        if (writeUsb(ByteArray(8))) {                                           // 8 zero bytes goes...
            issueDescription = "TF Send list of files: [send padding]"
            return false
        }

        if (writeUsb(nspListNames)) {                                           // list of the names goes...
            issueDescription = "TF Send list of files: [send list itself]"
            return false
        }

        return true
    }

    // After we sent commands to NS, this chain starts
    private fun proceedCommands(): Boolean {
        val magic = byteArrayOf(
            0x54.toByte(),
            0x55.toByte(),
            0x43.toByte(),
            0x30.toByte()
        ) // eq. 'TUC0' @ UTF-8 (actually ASCII lol, u know what I mean)

        var receivedArray: ByteArray?

        while (true) {
            receivedArray = readUsb()
            if (receivedArray == null) return false // catches exception


            if (!Arrays.copyOfRange(receivedArray, 0, 4)
                    .contentEquals(magic)
            )  // Bytes from 0 to 3 should contain 'magic' TUC0, so must be verified like this
                continue

            // 8th to 12th(explicits) bytes in returned data stands for command ID as unsigned integer (Little-endian). Actually, we have to compare arrays here, but in real world it can't be greater then 0/1/2, thus:
            // BTW also protocol specifies 4th byte to be 0x00 kinda indicating that that this command is valid. But, as you may see, never happens other situation when it's not = 0.
            if (receivedArray[8].toInt() == 0x00) {                           //0x00 - exit
                return true // All interaction with USB device should be ended (expected);
            } else if ((receivedArray[8].toInt() == 0x01) || (receivedArray[8].toInt() == 0x02)) {           //0x01 - file range; 0x02 unknown bug on backend side (dirty hack).
                if (!fileRangeCmd())  // issueDescription inside
                    return false
            }
        }
    }

    /**
     * This is what returns requested file (files)
     * Executes multiple times
     * @return 'true' if everything is ok
     * 'false' is error/exception occurs
     */
    private fun fileRangeCmd(): Boolean {
        // Here we take information of what other side wants
        var receivedArray = readUsb()
        if (receivedArray == null) {
            issueDescription = "TF Unable to get meta information @fileRangeCmd()"
            return false
        }

        // range_offset of the requested file. In the begining it will be 0x10.
        val receivedRangeSize = ByteBuffer.wrap(Arrays.copyOfRange(receivedArray, 0, 8)).order(
            ByteOrder.LITTLE_ENDIAN
        ).getLong()
        val receivedRangeSizeRAW = Arrays.copyOfRange(receivedArray, 0, 8)
        val receivedRangeOffset = ByteBuffer.wrap(Arrays.copyOfRange(receivedArray, 8, 16)).order(
            ByteOrder.LITTLE_ENDIAN
        ).getLong()

        // Requesting UTF-8 file name required:
        receivedArray = readUsb()
        if (receivedArray == null) {
            issueDescription = "TF Unable to get file name @fileRangeCmd()"
            return false
        }
        val receivedRequestedNSP: String
        try {
            receivedRequestedNSP = String(receivedArray, charset("UTF-8")) //TODO:FIX
        } catch (uee: UnsupportedEncodingException) {
            issueDescription = "TF UnsupportedEncodingException @fileRangeCmd()"
            return false
        }

        // Sending response header
        if (sendResponse(receivedRangeSizeRAW))  // Get receivedRangeSize in 'RAW' format exactly as it has been received. It's simply.
            return false // issueDescription handled by method


        try {
            var bufferedInStream: BufferedInputStream? = null

            for (e in nspElements) {
                if (e.filename == receivedRequestedNSP) {
                    val elementIS = context.contentResolver.openInputStream(e.uri)
                    if (elementIS == null) {
                        issueDescription = "TF Unable to obtain InputStream"
                        return false
                    }
                    bufferedInStream = BufferedInputStream(elementIS) // TODO: refactor?
                    break
                }
            }

            if (bufferedInStream == null) {
                issueDescription = "TF Unable to create BufferedInputStream"
                return false
            }

            var readBuf: ByteArray //= new byte[1048576];        // eq. Allocate 1mb

            if (bufferedInStream.skip(receivedRangeOffset) != receivedRangeOffset) {
                issueDescription = "TF Requested skip is out of file size. Nothing to transmit."
                return false
            }

            var readFrom: Long = 0
            // 'End Offset' equal to receivedRangeSize.
            var readPice = 16384 // 8388608 = 8Mb
            var updateProgressPeriods = 0

            while (readFrom < receivedRangeSize) {
                if ((readFrom + readPice) >= receivedRangeSize) readPice =
                    (receivedRangeSize - readFrom).toInt() // TODO: Troubles could raise here


                readBuf = ByteArray(readPice) // TODO: not perfect moment, consider refactoring.

                if (bufferedInStream.read(readBuf) != readPice) {
                    issueDescription = "TF Reading of stream suddenly ended"
                    return false
                }
                //write to USB
                if (writeUsb(readBuf)) {
                    issueDescription = "TF Failure during NSP transmission."
                    return false
                }
                readFrom += readPice.toLong()

                if (updateProgressPeriods++ % 1024 == 0)  // Update progress bar after every 16mb goes to NS
                    updateProgressBar(((readFrom + 1) / (receivedRangeSize / 100 + 1)).toInt()) // This shit takes too much time

                //Log.i("LPR", "CO: "+readFrom+"RRS: "+receivedRangeSize+"RES: "+(readFrom+1/(receivedRangeSize/100+1)));
            }
            bufferedInStream.close()

            resetProgressBar()
        } catch (ioe: IOException) {
            issueDescription = "TF IOException: " + ioe.message
            return false
        }
        return true
    }

    /**
     * Send response header.
     * @return false if everything OK
     * true if failed
     */
    private fun sendResponse(rangeSize: ByteArray): Boolean {
        if (writeUsb(replyConstArray)) {
            issueDescription = "TF Response: [1/3]"
            return true
        }

        if (writeUsb(rangeSize)) {                                                          // Send EXACTLY what has been received
            issueDescription = "TF Response: [2/3]"
            return true
        }

        if (writeUsb(ByteArray(12))) {                                                       // kinda another one padding
            issueDescription = "TF Response: [3/3]"
            return true
        }
        return false
    }
}
