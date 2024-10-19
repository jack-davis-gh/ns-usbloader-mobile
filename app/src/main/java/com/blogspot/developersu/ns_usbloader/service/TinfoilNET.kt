package com.blogspot.developersu.ns_usbloader.service

import android.content.Context
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.ResultReceiver
import com.blogspot.developersu.ns_usbloader.R
import com.blogspot.developersu.ns_usbloader.service.NETPacket.code400
import com.blogspot.developersu.ns_usbloader.service.NETPacket.code404
import com.blogspot.developersu.ns_usbloader.service.NETPacket.code416
import com.blogspot.developersu.ns_usbloader.service.NETPacket.getCode200
import com.blogspot.developersu.ns_usbloader.service.NETPacket.getCode206
import com.blogspot.developersu.ns_usbloader.core.model.NSFile
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.URLEncoder
import java.nio.ByteBuffer
import java.util.LinkedList
import java.util.Locale

internal class TinfoilNET(
    resultReceiver: ResultReceiver,
    context: Context,
    nspElements: ArrayList<NSFile>,
    private val nsIp: String,
    private var phoneIp: String,
    private val phonePort: Int
) : TransferTask(context) {
    private val files = HashMap<String, NSFile>()

    private var handShakeSocket: Socket? = null
    private var serverSocket: ServerSocket? = null

    private var currSockOS: OutputStream? = null
    private var currSockPW: PrintWriter? = null

    private var jobInProgress = true

    override fun cancel() {
        super.cancel()

        try {
            handShakeSocket!!.close()
            serverSocket!!.close()
        } catch (ignored: IOException) {
        } catch (ignored: NullPointerException) {
        }
    }

    /**
     * Simple constructor that everybody uses
     */
    init {
        // Collect and encode NSP files list
        for (nspElem in nspElements) files[URLEncoder.encode(nspElem.name, "UTF-8")
            .replace("\\+".toRegex(), "%20")] =
            nspElem // replace + to %20


        // Resolve IP
        if (phoneIp.isEmpty()) resolvePhoneIp()
        // Open Server Socket on port
        try {
            serverSocket = ServerSocket(phonePort)
        } catch (ioe: IOException) {
            throw Exception("NET: Can't open socket using port: " + phonePort + ". Returned: " + ioe.message)
        }
    }

    @Throws(Exception::class)
    private fun resolvePhoneIp() {
        val wm =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                ?: throw Exception("NET: Unable to auto-resolve IP address.")

        val intIp = wm.connectionInfo.ipAddress
        phoneIp = String.format(
            Locale.US, "%d.%d.%d.%d",
            (intIp and 0xff),
            (intIp shr 8 and 0xff),
            (intIp shr 16 and 0xff),
            (intIp shr 24 and 0xff)
        )
        if (phoneIp == "0.0.0.0") throw Exception("NET: Unable to auto-resolve IP address (0.0.0.0)")
    }

    override fun run(): Boolean {
        try {
            if (interrupt) return false

            val handshakeCommand =
                buildHandshakeContent().toByteArray() // android's .getBytes() default == UTF8  // Follow the
            val handshakeCommandSize = ByteBuffer.allocate(4).putInt(handshakeCommand.size)
                .array() // defining order ; Integer size = 4 bytes

            sendHandshake(handshakeCommandSize, handshakeCommand)

            serveRequestsLoop()
        } catch (e: Exception) {
            close(true)
            issueDescription = "NET: Unable to connect to NS and send files list: " + e.message
            return true
        }
        close(false)
        return true
    }

    private fun buildHandshakeContent(): String {
        val myStrBuilder = StringBuilder()
        for (fileNameEncoded in files.keys) {
            myStrBuilder.append(phoneIp)
            myStrBuilder.append(':')
            myStrBuilder.append(phonePort)
            myStrBuilder.append('/')
            myStrBuilder.append(fileNameEncoded)
            myStrBuilder.append('\n')
        }
        return myStrBuilder.toString()
    }

    @Throws(Exception::class)
    private fun sendHandshake(handshakeCommandSize: ByteArray, handshakeCommand: ByteArray) {
        try {
            handShakeSocket = Socket()
            handShakeSocket!!.connect(
                InetSocketAddress(InetAddress.getByName(nsIp), 2000),
                1000
            ) // e.g. 1sec
            val os = handShakeSocket!!.getOutputStream()
            os.write(handshakeCommandSize)
            os.write(handshakeCommand)
            os.flush()

            handShakeSocket!!.close()
        } catch (uhe: IOException) {
            throw Exception("NET: Unable to send files list: " + uhe.message)
        }
    }

    @Throws(Exception::class)
    private fun serveRequestsLoop() {
        while (jobInProgress) {
            val clientSocket = serverSocket!!.accept()

            val br = BufferedReader(
                InputStreamReader(clientSocket.getInputStream())
            )

            currSockOS = clientSocket.getOutputStream()
            currSockPW = PrintWriter(OutputStreamWriter(currSockOS))

            var line: String
            val tcpPacket = LinkedList<String>()

            while ((br.readLine().also { line = it }) != null) {
                if (line.trim { it <= ' ' }.isEmpty()) {          // If TCP packet is ended
                    handleRequest(tcpPacket) // Proceed required things
                    tcpPacket.clear() // Clear data and wait for next TCP packet
                } else tcpPacket.add(line) // Otherwise collect data
            }
            clientSocket.close()
        }
    }
    // 200 206 400 (inv range) 404 416 (Range Not Satisfiable )
    /**
     * Handle requests
     */
    @Throws(Exception::class)
    private fun handleRequest(packet: LinkedList<String>) {
        if (packet[0].startsWith("DROP")) {
            jobInProgress = false
            return
        }

        val reqFileName = packet[0].replace("(^[A-z\\s]+/)|(\\s+?.*$)".toRegex(), "")

        if (!files.containsKey(reqFileName)) {
            writeToSocket(code404)
            return
        }
        val requestedElement = files[reqFileName]

        val reqFileSize = requestedElement!!.size

        if (reqFileSize == 0L) {   // well.. tell 404 if file exists with 0 length is against standard, but saves time
            writeToSocket(code404)
//            requestedElement.status =
//                context.resources.getString(R.string.status_failed_to_upload)
            return
        }
        if (packet[0].startsWith("HEAD")) {
            writeToSocket(getCode200(reqFileSize))
            return
        }
        if (packet[0].startsWith("GET")) {
            for (line in packet) {
                if (line.lowercase(Locale.getDefault()).startsWith("range")) {
                    parseGETrange(requestedElement, reqFileSize, line)
                    return
                }
            }
        }
    }

    @Throws(Exception::class)
    private fun parseGETrange(
        requestedElement: NSFile,
        fileSize: Long,
        rangeDirective: String
    ) {
        try {
            val rangeStr = rangeDirective.lowercase(Locale.getDefault())
                .replace("^range:\\s+?bytes=".toRegex(), "").split("-".toRegex(), limit = 2)
                .toTypedArray()

            if (!rangeStr[0].isEmpty()) {
                if (rangeStr[1].isEmpty()) {
                    writeToSocket(requestedElement, rangeStr[0].toLong(), fileSize)
                    return
                }

                val fromRange = rangeStr[0].toLong()
                val toRange = rangeStr[1].toLong()
                if (fromRange > toRange) { // If start bytes greater then end bytes
                    writeToSocket(code400)
//                    requestedElement.status =
//                        context.resources.getString(R.string.status_failed_to_upload)
                    return
                }
                writeToSocket(requestedElement, fromRange, toRange)
                return
            }

            if (rangeStr[1].isEmpty()) {
                writeToSocket(code400) // If Range not defined: like "Range: bytes=-"
//                requestedElement.status =
//                    context.resources.getString(R.string.status_failed_to_upload)
                return
            }

            if (fileSize > 500) {
                writeToSocket(requestedElement, fileSize - 500, fileSize)
                return
            }
            // If file smaller than 500 bytes
            writeToSocket(code416)
//            requestedElement.status = context.resources.getString(R.string.status_failed_to_upload)
        } catch (nfe: NumberFormatException) {
            writeToSocket(code400)
//            requestedElement.status = context.resources.getString(R.string.status_failed_to_upload)
            throw Exception("NET: Requested range for " + requestedElement.name + " has incorrect format. Returning 400\n\t" + nfe.message)
        }
    }

    /** Send commands  */
    private fun writeToSocket(string: String) {
        currSockPW!!.write(string)
        currSockPW!!.flush()
    }

    /** Send files  */
    @Throws(Exception::class)
    private fun writeToSocket(nspElem: NSFile, start: Long, end: Long) {
        if (interrupt) {
            throw Exception("Interrupted by user")
        }
        writeToSocket(getCode206(nspElem.size, start, end))
        try {
            val count = end - start + 1 // Meeh. Somehow it works

            val elementInputStream = context.contentResolver.openInputStream(Uri.parse(nspElem.uri))
                ?: throw Exception("NET Unable to obtain input stream")

            val bis = BufferedInputStream(elementInputStream)

            var readPice = 4194304 //8388608;// = 8Mb (1024 is slow)
            var byteBuf: ByteArray

            if (bis.skip(start) != start) {
//                nspElem.status = context.resources.getString(R.string.status_failed_to_upload)
                throw Exception("NET: Unable to skip requested range")
            }
            var currentOffset: Long = 0
            while (currentOffset < count) {
                if (interrupt) throw Exception("Interrupted by user")
                if ((currentOffset + readPice) >= count) {
                    readPice = (count - currentOffset).toInt()
                }
                byteBuf = ByteArray(readPice)

                if (bis.read(byteBuf) != readPice) {
                    throw Exception("NET: Reading from file stream suddenly ended")
                }
                currSockOS!!.write(byteBuf)

                currentOffset += readPice.toLong()

                updateProgressBar((currentOffset.toDouble() / (count.toDouble() / 100.0)).toInt())
            }
            currSockOS!!.flush() // TODO: check if this really needed.
            bis.close()
            resetProgressBar()
        } catch (ioe: IOException) {
//            nspElem.status =
//                context.resources.getString(R.string.status_failed_to_upload) // TODO: REDUNDANT?
            throw Exception("NET: File transmission failed. Returned: " + ioe.message)
        }
    }

    /**
     * Close when done
     */
    private fun close(isFailed: Boolean) {
        status = if (isFailed) context.resources.getString(R.string.status_failed_to_upload)
        else context.resources.getString(R.string.status_unkown)
        try {
            serverSocket?.close() // Closing server socket.
        } catch (ignored: IOException) {
        } catch (ignored: NullPointerException) {
        }
    }

    override var issueDescription: String? = null
}
