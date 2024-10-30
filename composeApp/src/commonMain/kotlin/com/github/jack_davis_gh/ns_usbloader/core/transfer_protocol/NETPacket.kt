package com.github.jack_davis_gh.ns_usbloader.core.transfer_protocol

import com.github.jack_davis_gh.ns_usbloader.getPlatform
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

object NETPacket {
    val platform = getPlatform().toString()
    private val CODE_200 = "HTTP/1.0 200 OK\r\n" +
            "Server: NS-USBloader-M-v." + platform + "\r\n" +
            "Date: %s\r\n" +
            "Content-type: application/octet-stream\r\n" +
            "Accept-Ranges: bytes\r\n" +
            "Content-Range: bytes 0-%d/%d\r\n" +
            "Content-Length: %d\r\n" +
            "Last-Modified: Thu, 01 Jan 1970 00:00:00 GMT\r\n\r\n"
    private val CODE_206 = "HTTP/1.0 206 Partial Content\r\n" +
            "Server: NS-USBloader-M-v." + platform + "\r\n" +
            "Date: %s\r\n" +
            "Content-type: application/octet-stream\r\n" +
            "Accept-Ranges: bytes\r\n" +
            "Content-Range: bytes %d-%d/%d\r\n" +
            "Content-Length: %d\r\n" +
            "Last-Modified: Thu, 01 Jan 1970 00:00:00 GMT\r\n\r\n"
    private val CODE_400 = "HTTP/1.0 400 invalid range\r\n" +
            "Server: NS-USBloader-M-v." + platform + "\r\n" +
            "Date: %s\r\n" +
            "Connection: close\r\n" +
            "Content-Type: text/html;charset=utf-8\r\n" +
            "Content-Length: 0\r\n\r\n"
    private val CODE_404 = "HTTP/1.0 404 Not Found\r\n" +
            "Server: NS-USBloader-M-v." + platform + "\r\n" +
            "Date: %s\r\n" +
            "Connection: close\r\n" +
            "Content-Type: text/html;charset=utf-8\r\n" +
            "Content-Length: 0\r\n\r\n"
    private val CODE_416 = "HTTP/1.0 416 Requested Range Not Satisfiable\r\n" +
            "Server: NS-USBloader-M-v." + platform + "\r\n" +
            "Date: %s\r\n" +
            "Connection: close\r\n" +
            "Content-Type: text/html;charset=utf-8\r\n" +
            "Content-Length: 0\r\n\r\n"

    fun getCode200(nspFileSize: Long): String {
        return String.format(
            Locale.US, CODE_200,
            time, nspFileSize - 1, nspFileSize, nspFileSize
        )
    }

    fun getCode206(nspFileSize: Long, startPos: Long, endPos: Long): String {
        return String.format(
            Locale.US, CODE_206,
            time, startPos, endPos, nspFileSize, endPos - startPos + 1
        )
    }


    val code404: String
        get() = String.format(Locale.US, CODE_404, time)

    val code416: String
        get() = String.format(Locale.US, CODE_416, time)

    val code400: String
        get() = String.format(Locale.US, CODE_400, time)

    private val time: String
        get() {
            val sdf =
                SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            return sdf.format(Calendar.getInstance().time)
        }
}
