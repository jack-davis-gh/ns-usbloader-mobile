package com.github.jack_davis_gh.ns_usbloader.core.work_manager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.github.jack_davis_gh.ns_usbloader.NsConstants
import com.github.jack_davis_gh.ns_usbloader.NsConstants.json
import com.github.jack_davis_gh.ns_usbloader.core.transfer_protocol.TinfoilUsb
import com.github.jack_davis_gh.ns_usbloader.core.model.NSFile
import com.github.jack_davis_gh.ns_usbloader.core.model.Protocol
import com.github.jack_davis_gh.ns_usbloader.core.transfer_protocol.TinfoilNet
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

class CommunicationWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val tinfoilUsb: TinfoilUsb,
    private val tinfoilNet: TinfoilNet,
): CoroutineWorker(appContext, workerParams) {
    override suspend fun getForegroundInfo(): ForegroundInfo = createForegroundInfo()

    override suspend fun doWork(): Result = coroutineScope {
        val protoJsonStr = inputData.getString(NsConstants.SERVICE_CONTENT_PROTOCOL) ?: return@coroutineScope Result.failure()
        val proto = json.decodeFromString<Protocol>(protoJsonStr)

        val filesJsonStr = inputData.getString(NsConstants.SERVICE_CONTENT_NSP_LIST) ?: return@coroutineScope Result.failure()
        val files: List<NSFile> = json.decodeFromString(filesJsonStr)

        val nsIp = inputData.getString(NsConstants.SERVICE_CONTENT_NS_DEVICE_IP) ?: return@coroutineScope Result.failure()
        val port = inputData.getInt(NsConstants.SERVICE_CONTENT_PHONE_PORT, 6024)

        setForeground(createForegroundInfo())

        val channel = Channel<Int>()
        val scope = CoroutineScope(Dispatchers.IO)
        try {
            scope.launch {
                when(proto) {
                    Protocol.Network -> tinfoilNet.run(files, nsIp, port)
                    Protocol.USB -> tinfoilUsb.run(files, channel)
                }
            }

            for (progress in channel) {
                setProgress(workDataOf("Progress" to progress))
                setForegroundAsync(createForegroundInfo(progress))
            }
            channel.close()
        } catch (e: CancellationException) {
            return@coroutineScope Result.success()
        } catch (e: Exception) {
            val outData = Data.Builder()
                .putString("CommunicationWorker", e.message ?: "Unknown Exception")
                .build()
            return@coroutineScope Result.failure(outData)
        } finally {
            channel.close()
        }

        return@coroutineScope Result.success()
    }
}