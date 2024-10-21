package com.github.jack_davis_gh.ns_usbloader.core.transfer_protocol

import com.github.jack_davis_gh.ns_usbloader.core.model.NSFile
import com.github.jack_davis_gh.ns_usbloader.core.platform.file.FileManager
import com.github.jack_davis_gh.ns_usbloader.core.platform.network.NetworkManager
import com.github.jack_davis_gh.ns_usbloader.core.transfer_protocol.NETPacket.code400
import com.github.jack_davis_gh.ns_usbloader.core.transfer_protocol.NETPacket.code404
import com.github.jack_davis_gh.ns_usbloader.core.transfer_protocol.NETPacket.code416
import com.github.jack_davis_gh.ns_usbloader.core.transfer_protocol.NETPacket.getCode200
import com.github.jack_davis_gh.ns_usbloader.core.transfer_protocol.NETPacket.getCode206
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
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
import javax.inject.Inject
import kotlin.coroutines.coroutineContext

class TinfoilNet @Inject constructor(
    private val fileManager: FileManager,
    private val networkManager: NetworkManager
) {
    private var files: Map<String, NSFile> = emptyMap()

    private var serverSocket: ServerSocket? = null

    private var currSockOS: OutputStream? = null
    private var currSockPW: PrintWriter? = null

    suspend fun run(nsFiles: List<NSFile>, nsIp: String, phonePort: Int): Result<Unit> {
        try {
            open(phonePort)
                .onFailure { return Result.failure(it) }

            files = nsFiles.associateBy { URLEncoder.encode(it.name, "UTF-8")
                .replace("\\+".toRegex(), "%20") }

            val phoneIp = networkManager.getIpAddress() ?: return Result.failure(Exception("Can't get IP Address"))
            // Collect and encode NSP files list
            val handshakeCommandStr = buildString {
                files.keys.forEach { encodedName ->
                    append("$phoneIp:$phonePort/")
                    append("$encodedName\n")
                }
            }

            val handshakeCommand = handshakeCommandStr.toByteArray() // android's .getBytes() default == UTF8  // Follow the

            val handshakeCommandSize = ByteBuffer.allocate(4)
                .putInt(handshakeCommand.size)
                .array() // defining order ; Integer size = 4 bytes

            sendHandshake(nsIp, handshakeCommandSize, handshakeCommand)
                .onFailure { return Result.failure(it) }

            serveRequestsLoop()
                .onFailure {
                    return Result.failure(it)
                }
        } catch (e: Exception) {
            return Result.failure(Exception("NET: Unable to connect to NS and send files list: " + e.message))
        } finally {
            close() // Closing server socket.
        }

        return Result.success(Unit)
    }

    /**
     * Simple constructor that everybody uses
     */
    private fun open(phonePort: Int): Result<Unit> {
        // Open Server Socket on port
        try {
            serverSocket = ServerSocket(phonePort)
        } catch (ioe: IOException) {
            return Result.failure(Exception("NET: Can't open socket using port: " + phonePort + ". Returned: " + ioe.message))
        }
        return Result.success(Unit)
    }

    private fun sendHandshake(nsIp: String, handshakeCommandSize: ByteArray, handshakeCommand: ByteArray): Result<Unit> {
        try {
            val handShakeSocket = Socket()
            handShakeSocket.connect(
                InetSocketAddress(InetAddress.getByName(nsIp), 2000),
                1000
            ) // e.g. 1sec
            val os = handShakeSocket.getOutputStream()
            os.write(handshakeCommandSize)
            os.write(handshakeCommand)
            os.flush()

            handShakeSocket.close()
        } catch (uhe: IOException) {
            return Result.failure(Exception("NET: Unable to send files list: " + uhe.message))
        }
        return Result.success(Unit)
    }

    private suspend fun serveRequestsLoop(): Result<Unit> {
        while (coroutineContext.isActive) {
            val clientSocket = withContext(Dispatchers.IO) {
                    serverSocket?.accept()
                } ?: return Result.failure(Exception("ServerSocket is not open, unable to accept"))

            val br = withContext(Dispatchers.IO) {
                BufferedReader(
                    InputStreamReader(clientSocket.getInputStream())
                )
            }

            currSockOS = withContext(Dispatchers.IO) { clientSocket.getOutputStream() }
            currSockPW = PrintWriter(OutputStreamWriter(currSockOS))

            var line = ""
            val tcpPacket = LinkedList<String>()

            while (coroutineContext.isActive && withContext(Dispatchers.IO) { br.readLine() }.also { line = it } != null) {
                if (line.trim { it <= ' ' }.isEmpty()) {          // If TCP packet is ended
                    handleRequest(tcpPacket) // Proceed required things
                    tcpPacket.clear() // Clear data and wait for next TCP packet
                } else tcpPacket.add(line) // Otherwise collect data
            }
            withContext(Dispatchers.IO) { clientSocket.close() }
        }
        return Result.success(Unit)
    }
    // 200 206 400 (inv range) 404 416 (Range Not Satisfiable )
    /**
     * Handle requests
     */
    private suspend fun handleRequest(packet: LinkedList<String>): Result<Unit> {
        if (packet[0].startsWith("DROP")) {
//            jobInProgress = false TODO Probably pass as a result and bubble up
            return Result.success(Unit)
        }

        val reqFileName = packet[0].replace("(^[A-z\\s]+/)|(\\s+?.*$)".toRegex(), "")

        if (!files.containsKey(reqFileName)) {
            writeToSocket(code404)
            return Result.success(Unit)
        }
        val requestedElement = files[reqFileName] ?: return Result.failure(Exception("NPE when trying to get file by name."))

        val reqFileSize = requestedElement.size

        if (reqFileSize == 0L) {   // well.. tell 404 if file exists with 0 length is against standard, but saves time
            writeToSocket(code404)
//            requestedElement.status =
//                context.resources.getString(R.string.status_failed_to_upload)
            return Result.success(Unit)
        }
        if (packet[0].startsWith("HEAD")) {
            writeToSocket(getCode200(reqFileSize))
            return Result.success(Unit)
        }
        if (packet[0].startsWith("GET")) {
            for (line in packet) {
                if (line.lowercase(Locale.getDefault()).startsWith("range")) {
                    parseGETrange(requestedElement, reqFileSize, line)
                    return Result.success(Unit)
                }
            }
        }
        return Result.success(Unit)
    }

    @Throws(Exception::class)
    private suspend fun parseGETrange(
        requestedElement: NSFile,
        fileSize: Long,
        rangeDirective: String
    ) {
        try {
            val rangeStr = rangeDirective.lowercase(Locale.getDefault())
                .replace("^range:\\s+?bytes=".toRegex(), "").split("-".toRegex(), limit = 2)
                .toTypedArray()

            if (rangeStr[0].isNotEmpty()) {
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
        currSockPW?.write(string)
        currSockPW?.flush()
    }

    /** Send files  */
    @Throws(Exception::class)
    private suspend fun writeToSocket(nspElem: NSFile, start: Long, end: Long): Result<Unit> {
        writeToSocket(getCode206(nspElem.size, start, end))
        try {
            val count = end - start + 1 // Meeh. Somehow it works

            val elementInputStream = fileManager.openInputStream(nspElem)
                .getOrElse { return Result.failure(Exception("NET Unable to obtain input stream")) }

            val bis = BufferedInputStream(elementInputStream)

            var readPice = 4194304 //8388608;// = 8Mb (1024 is slow)
            var byteBuf: ByteArray

            if (withContext(Dispatchers.IO) { bis.skip(start) } != start) {
//                nspElem.status = context.resources.getString(R.string.status_failed_to_upload)
                return Result.failure(Exception("NET: Unable to skip requested range"))
            }
            var currentOffset: Long = 0
            while (coroutineContext.isActive && currentOffset < count) {
                if ((currentOffset + readPice) >= count) {
                    readPice = (count - currentOffset).toInt()
                }
                byteBuf = ByteArray(readPice)

                if (withContext(Dispatchers.IO) { bis.read(byteBuf) } != readPice) {
                    return Result.failure(Exception("NET: Reading from file stream suddenly ended"))
                }
                withContext(Dispatchers.IO) { currSockOS?.write(byteBuf) }

                currentOffset += readPice.toLong()

//                updateProgressBar((currentOffset.toDouble() / (count.toDouble() / 100.0)).toInt())
            }
            withContext(Dispatchers.IO) {
                currSockOS?.flush() // TODO: check if this really needed.
                bis.close()
            }
//            resetProgressBar()
        } catch (ioe: IOException) {
//            nspElem.status =
//                context.resources.getString(R.string.status_failed_to_upload) // TODO: REDUNDANT?
            return Result.failure(Exception("NET: File transmission failed. Returned: " + ioe.message))
        }
        return Result.success(Unit)
    }

    /**
     * Close when done
     */
    private fun close() {
//        status = if (isFailed) context.resources.getString(R.string.status_failed_to_upload)
//        else context.resources.getString(R.string.status_unkown)
        serverSocket?.close()
        serverSocket = null
    }
}
