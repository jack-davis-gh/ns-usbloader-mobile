package com.github.jack_davis_gh.ns_usbloader.core.transfer_protocol

import com.github.jack_davis_gh.ns_usbloader.core.model.NSFile
import com.github.jack_davis_gh.ns_usbloader.core.platform.FileManager
//import com.github.jack_davis_gh.ns_usbloader.core.platform.file.FileManager
//import com.github.jack_davis_gh.ns_usbloader.core.platform.network.NetworkManager
import com.github.jack_davis_gh.ns_usbloader.core.transfer_protocol.NETPacket.code400
import com.github.jack_davis_gh.ns_usbloader.core.transfer_protocol.NETPacket.code404
import com.github.jack_davis_gh.ns_usbloader.core.transfer_protocol.NETPacket.code416
import com.github.jack_davis_gh.ns_usbloader.core.transfer_protocol.NETPacket.getCode200
import com.github.jack_davis_gh.ns_usbloader.core.transfer_protocol.NETPacket.getCode206
import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.ServerSocket
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.readUTF8Line
import io.ktor.utils.io.writeByteArray
import io.ktor.utils.io.writeInt
import io.ktor.utils.io.writeString
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import me.tatarka.inject.annotations.Inject
import java.io.BufferedInputStream
import java.net.URLEncoder
import java.util.Locale

data class UsbTransferException(override val message: String): Exception(message)

@Inject
class TinfoilNet(
    private val fileManager: FileManager,
//    private val networkManager: NetworkManager
) {
    private val selectorManager = SelectorManager(Dispatchers.IO)
    private var files: Map<String, NSFile> = emptyMap()

    private val mutex = Mutex()
    private var serverSocket: ServerSocket? = null
    private var receiveChannel: ByteReadChannel? = null
    private var writerChannel: ByteWriteChannel? = null

    suspend fun run(nsFiles: List<NSFile>, nsIp: String, phonePort: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            open(phonePort)

            files = nsFiles.associateBy { URLEncoder.encode(it.name, "UTF-8")
                .replace("\\+".toRegex(), "%20") }

            sendFileList(nsIp, phonePort)

            serveRequestsLoop()
        } catch (e: Exception) {
            return@withContext Result.failure(Exception("NET: Unable to connect to NS and send files list: " + e.message))
        } finally {
            close() // Closing server socket.
        }

        return@withContext Result.success(Unit)
    }

    /**
     * Simple constructor that everybody uses
     */
    private fun open(phonePort: Int) = runBlocking {
        // Open Server Socket on port
        try {
            mutex.withLock {
                serverSocket = aSocket(selectorManager).tcp().bind(port = phonePort)
            }
        } catch (e: Exception) {
            throw UsbTransferException("NET: Can't open socket using port: " + phonePort + ". Returned: " + e.message)
        }
    }

    private fun sendFileList(nsIp: String, phonePort: Int) = runBlocking {
        val phoneIp = TODO() //networkManager.getIpAddress() ?: throw UsbTransferException("Can't get IP Address")
        // Collect and encode NSP files list
        val handshakeCommandStr = buildString {
            files.keys.forEach { encodedName ->
                append("$phoneIp:$phonePort/")
                append("$encodedName\n")
            }
        }

        val socket = withTimeout(1_000) {
            aSocket(selectorManager).tcp().connect(nsIp, 2000)
        }

        socket.openWriteChannel(autoFlush = true).apply {
            writeInt(handshakeCommandStr.length)
            writeString(handshakeCommandStr)
        }

        socket.close()
    }

    private suspend fun serveRequestsLoop() = withContext(Dispatchers.IO) {
        while (coroutineContext.isActive) {
            val clientSocket = serverSocket?.accept()
                ?: throw UsbTransferException("ServerSocket is not open, unable to accept")

            clientSocket.use {
                mutex.withLock {
                    receiveChannel = clientSocket.openReadChannel()
                    writerChannel = clientSocket.openWriteChannel(autoFlush = false)
                }

                val tcpPacket = mutableListOf<String>()
                while (coroutineContext.isActive) {
                    val line = receiveChannel?.readUTF8Line() ?: break

                    if (line.trim { it <= ' ' }.isEmpty()) {          // If TCP packet is ended
                        handleRequest(tcpPacket) // Proceed required things

                        tcpPacket.clear() // Clear data and wait for next TCP packet
                    } else tcpPacket.add(line) // Otherwise collect data
                }
            }
        }
    }
    // 200 206 400 (inv range) 404 416 (Range Not Satisfiable )
    /**
     * Handle requests
     */
    private suspend fun handleRequest(packet: List<String>) {
        val header = packet.first()
        if (header.startsWith("DROP")) {
            throw CancellationException("Drop command received")
        }

        val reqFileName = header.replace("(^[A-z\\s]+/)|(\\s+?.*$)".toRegex(), "")

        if (!files.containsKey(reqFileName)) {
            writeToSocket(code404)
            throw UsbTransferException("Failed to find file.")
        }
        val requestedElement = files[reqFileName] ?: throw UsbTransferException("NPE when trying to get file by name.")

        val reqFileSize = requestedElement.size

        if (reqFileSize == 0L) {   // well.. tell 404 if file exists with 0 length is against standard, but saves time
            writeToSocket(code404)
            throw UsbTransferException("File size is empty.")
        }
        if (header.startsWith("HEAD")) {
            writeToSocket(getCode200(reqFileSize))
            return
        }
        if (header.startsWith("GET")) {
            packet.forEach { line ->
                if (line.lowercase(Locale.getDefault()).startsWith("range")) {
                    parseGETrange(requestedElement, reqFileSize, line)
                    return
                }
            }
        }
    }

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
    private suspend fun writeToSocket(string: String) {
        writerChannel?.writeString(string)
        writerChannel?.flush()
    }

    /** Send files  */
    private suspend fun writeToSocket(nspElem: NSFile, start: Long, end: Long) = withContext(Dispatchers.IO) {
        writeToSocket(getCode206(nspElem.size, start, end))
        try {
            val count = end - start + 1 // Meeh. Somehow it works

            val elementInputStream = fileManager.openInputStream(nspElem)
                .getOrElse { throw UsbTransferException("NET Unable to obtain input stream") }

            val bis = BufferedInputStream(elementInputStream)

            var readPice = 4194304 //8388608;// = 8Mb (1024 is slow)
            var byteBuf: ByteArray

            if (bis.skip(start) != start) {
                throw UsbTransferException("NET: Unable to skip requested range")
            }
            var currentOffset: Long = 0
            while (coroutineContext.isActive && currentOffset < count) {
                if ((currentOffset + readPice) >= count) {
                    readPice = (count - currentOffset).toInt()
                }
                byteBuf = ByteArray(readPice)

                if (bis.read(byteBuf) != readPice) {
                    return@withContext Result.failure(Exception("NET: Reading from file stream suddenly ended"))
                }
                writerChannel?.writeByteArray(byteBuf) ?: break

                currentOffset += readPice.toLong()

//                updateProgressBar((currentOffset.toDouble() / (count.toDouble() / 100.0)).toInt())
            }

            writerChannel?.flush() // TODO: check if this really needed.
            bis.close()
//            resetProgressBar()
        } catch (e: Exception) {
            return@withContext Result.failure(Exception("NET: File transmission failed. Returned: " + e.message))
        }
        return@withContext Result.success(Unit)
    }

    /**
     * Close when done
     */
    private fun close(): Result<Unit> = runBlocking {
//        status = if (isFailed) context.resources.getString(R.string.status_failed_to_upload)
//        else context.resources.getString(R.string.status_unkown)
        mutex.withLock {
            serverSocket?.close() ?: return@runBlocking Result.failure(Exception("Socket already closed"))
            serverSocket = null
        }
        return@runBlocking Result.success(Unit)
    }
}
