package com.blogspot.developersu.ns_usbloader.service

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.ResultReceiver
import com.blogspot.developersu.ns_usbloader.core.usb.UsbTransfer
import com.blogspot.developersu.ns_usbloader.model.NSFile

internal class GoldLeaf(
    resultReceiver: ResultReceiver,
    context: Context,
    usbDevice: UsbDevice,
    usbManager: UsbManager,
    private val nspElements: ArrayList<NSFile>
) {
//    :
//    UsbTransfer(usbDevice, usbManager) {
//    private val pfsElement: PFSProvider
//
//    //                     CMD                                G     L     U     C
//    private val CMD_GLUC = byteArrayOf(0x47, 0x4c, 0x55, 0x43)
//    private val CMD_ConnectionRequest = byteArrayOf(0x00, 0x00, 0x00, 0x00) // Write-only command
//    private val CMD_NSPName = byteArrayOf(0x02, 0x00, 0x00, 0x00) // Write-only command
//    private val CMD_NSPData = byteArrayOf(0x04, 0x00, 0x00, 0x00) // Write-only command
//
//    private val CMD_ConnectionResponse = byteArrayOf(0x01, 0x00, 0x00, 0x00)
//    private val CMD_Start = byteArrayOf(0x03, 0x00, 0x00, 0x00)
//    private val CMD_NSPContent = byteArrayOf(0x05, 0x00, 0x00, 0x00)
//    private val CMD_NSPTicket = byteArrayOf(0x06, 0x00, 0x00, 0x00)
//    private val CMD_Finish = byteArrayOf(0x07, 0x00, 0x00, 0x00)
//
//
//    init {
//        val fileInputStream =
//            context.contentResolver.openInputStream(nspElements[0].uri)
//        val fileName = nspElements[0].name
//        pfsElement = PFSProvider(fileInputStream, fileName)
//        if (!pfsElement.init()) throw Exception("GL File provided have incorrect structure and won't be uploaded.")
//    }
//
//    override fun run(): Boolean {
//        if (initGoldLeafProtocol(pfsElement)) status =
//            context.resources.getString(R.string.status_uploaded) // else - no change status that is already set to FAILED
//
//
//        finish()
//        return false
//    }
//
//    private fun initGoldLeafProtocol(pfsElement: PFSProvider): Boolean {
//        // Go parse commands
//        var readByte: ByteArray?
//
//        // Go connect to GoldLeaf
//        if (writeUsb(CMD_GLUC)) {
//            issueDescription = "GL Initiating GoldLeaf connection: 1/2"
//            return false
//        }
//
//        if (writeUsb(CMD_ConnectionRequest)) {
//            issueDescription = "GL Initiating GoldLeaf connection: 2/2"
//            return false
//        }
//
//        while (true) {
//            readByte = readUsb()
//            if (readByte == null) return false
//            if (readByte.contentEquals(CMD_GLUC)) {
//                readByte = readUsb()
//                if (readByte == null) return false
//                if (readByte.contentEquals(CMD_ConnectionResponse)) {
//                    if (!handleConnectionResponse(pfsElement)) return false
//                    continue
//                }
//                if (readByte.contentEquals(CMD_Start)) {
//                    if (!handleStart(pfsElement)) return false
//                    continue
//                }
//                if (readByte.contentEquals(CMD_NSPContent)) {
//                    if (!handleNSPContent(pfsElement, true)) return false
//                    continue
//                }
//                if (readByte.contentEquals(CMD_NSPTicket)) {
//                    if (!handleNSPContent(pfsElement, false)) return false
//                    continue
//                }
//                if (readByte.contentEquals(CMD_Finish)) {  // All good
//                    break
//                }
//            }
//        }
//        return true
//    }
//
//    /**
//     * ConnectionResponse command handler
//     */
//    private fun handleConnectionResponse(pfsElement: PFSProvider): Boolean {
//        if (writeUsb(CMD_GLUC)) {
//            issueDescription = "GL 'ConnectionResponse' command: INFO: [1/4]"
//            return false
//        }
//
//        if (writeUsb(CMD_NSPName)) {
//            issueDescription = "GL 'ConnectionResponse' command: INFO: [2/4]"
//            return false
//        }
//
//        if (writeUsb(pfsElement.bytesNspFileNameLength)) {
//            issueDescription = "GL 'ConnectionResponse' command: INFO: [3/4]"
//            return false
//        }
//
//        if (writeUsb(pfsElement.bytesNspFileName)) {
//            issueDescription = "GL 'ConnectionResponse' command: INFO: [4/4]"
//            return false
//        }
//
//        return true
//    }
//
//    /**
//     * Start command handler
//     */
//    private fun handleStart(pfsElement: PFSProvider): Boolean {
//        if (writeUsb(CMD_GLUC)) {
//            issueDescription = "GL Handle 'Start' command: [Send command prepare]"
//            return false
//        }
//
//        if (writeUsb(CMD_NSPData)) {
//            issueDescription = "GL Handle 'Start' command: [Send command]"
//            return false
//        }
//
//        if (writeUsb(pfsElement.bytesCountOfNca)) {
//            issueDescription = "GL Handle 'Start' command: [Send length]"
//            return false
//        }
//
//        val ncaCount = pfsElement.intCountOfNca
//
//        for (i in 0 until ncaCount) {
//            if (writeUsb(pfsElement.getNca(i)!!.ncaFileNameLength)) {
//                issueDescription = "GL Handle 'Start' command: File # $i/$ncaCount step: [1/4]"
//                return false
//            }
//
//            if (writeUsb(pfsElement.getNca(i)!!.ncaFileName)) {
//                issueDescription = "GL Handle 'Start' command: File # $i/$ncaCount step: [2/4]"
//                return false
//            }
//
//            if (writeUsb(
//                    ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(
//                        pfsElement.bodySize + pfsElement.getNca(
//                            i
//                        )!!.ncaOffset
//                    ).array()
//                )
//            ) {   // offset. real.
//                issueDescription = "GL Handle 'Start' command: File # $i/$ncaCount step: [3/4]"
//                return false
//            }
//
//            if (writeUsb(
//                    ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(
//                        pfsElement.getNca(i)!!.ncaSize
//                    ).array()
//                )
//            ) {  // size
//                issueDescription = "GL Handle 'Start' command: File # $i/$ncaCount step: [4/4]"
//                return false
//            }
//        }
//        return true
//    }
//
//    /**
//     * NSPContent command handler
//     * isItRawRequest - if True, just ask NS what's needed
//     * - if False, send ticket
//     */
//    private fun handleNSPContent(pfsElement: PFSProvider, isItRawRequest: Boolean): Boolean {
//        val requestedNcaID: Int
//
//        if (isItRawRequest) {
//            val readByte = readUsb()
//            if (readByte == null || readByte.size != 4) {
//                issueDescription = "GL Handle 'Content' command: [Read requested ID]"
//                return false
//            }
//            requestedNcaID = ByteBuffer.wrap(readByte).order(ByteOrder.LITTLE_ENDIAN).getInt()
//        } else {
//            requestedNcaID = pfsElement.ncaTicketID
//        }
//
//        val realNcaOffset = pfsElement.getNca(requestedNcaID)!!.ncaOffset + pfsElement.bodySize
//        val realNcaSize = pfsElement.getNca(requestedNcaID)!!.ncaSize
//
//        var readFrom: Long = 0
//
//        var readPice = 16384 // 8mb NOTE: consider switching to 1mb 1048576
//        var readBuf: ByteArray
//
//        try {
//            val bufferedInStream = BufferedInputStream(
//                context.contentResolver.openInputStream(
//                    nspElements[0].uri
//                )
//            ) // TODO: refactor?
//            if (bufferedInStream.skip(realNcaOffset) != realNcaOffset) {
//                issueDescription = "GL Failed to skip NCA offset"
//                return false
//            }
//            var updateProgressPeriods = 0
//            while (readFrom < realNcaSize) {
//                if (readPice > (realNcaSize - readFrom)) readPice =
//                    (realNcaSize - readFrom).toInt() // TODO: Troubles could raise here
//
//                readBuf = ByteArray(readPice)
//                if (bufferedInStream.read(readBuf) != readPice) {
//                    issueDescription = "GL Failed to read data from file."
//                    return false
//                }
//
//                if (writeUsb(readBuf)) {
//                    issueDescription = "GL Failed to write data into NS."
//                    return false
//                }
//
//                readFrom += readPice.toLong()
//                if (updateProgressPeriods++ % 1024 == 0)  // Update progress bar after every 16mb goes to NS
//                    updateProgressBar(((readFrom + 1) / (realNcaSize / 100 + 1)).toInt())
//            }
//            bufferedInStream.close()
//
//            resetProgressBar()
//        } catch (ioe: IOException) {
//            issueDescription =
//                "GL Failed to read NCA ID " + requestedNcaID + ". IO Exception: " + ioe.message
//            return false
//        }
//        return true
//    }
}
