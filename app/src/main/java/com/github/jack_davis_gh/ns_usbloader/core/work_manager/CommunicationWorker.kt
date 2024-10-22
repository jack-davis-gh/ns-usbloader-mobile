package com.github.jack_davis_gh.ns_usbloader.core.work_manager

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.github.jack_davis_gh.ns_usbloader.NsConstants
import com.github.jack_davis_gh.ns_usbloader.NsConstants.json
import com.github.jack_davis_gh.ns_usbloader.core.transfer_protocol.TinfoilUsb
import com.github.jack_davis_gh.ns_usbloader.core.model.NSFile
import com.github.jack_davis_gh.ns_usbloader.core.model.Protocol
import com.github.jack_davis_gh.ns_usbloader.core.transfer_protocol.TinfoilNet
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.CancellationException

@HiltWorker
class CommunicationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    @Assisted private val tinfoilUsb: TinfoilUsb,
    @Assisted private val tinfoilNet: TinfoilNet,
): CoroutineWorker(appContext, workerParams) {
    override suspend fun getForegroundInfo(): ForegroundInfo = createForegroundInfo()

    override suspend fun doWork(): Result = coroutineScope {
        val protoJsonStr = inputData.getString(NsConstants.SERVICE_CONTENT_PROTOCOL) ?: return@coroutineScope Result.failure()
        val proto = json.decodeFromString<Protocol>(protoJsonStr)

        val filesJsonStr = inputData.getString(NsConstants.SERVICE_CONTENT_NSP_LIST) ?: return@coroutineScope Result.failure()
        val files: List<NSFile> = json.decodeFromString(filesJsonStr)

        val nsIp = inputData.getString(NsConstants.SERVICE_CONTENT_NS_DEVICE_IP) ?: return@coroutineScope Result.failure()
        val port = inputData.getInt(NsConstants.SERVICE_CONTENT_PHONE_PORT, 6024)

        setForeground(getForegroundInfo())

        try {
            tinfoilNet.open()
                .getOrThrow()

            tinfoilNet.run(files, nsIp, port)
                .getOrThrow()
        } catch (e: CancellationException) {
            val outData = Data.Builder()
                .putString("CommunicationWorker", e.message ?: "Unknown Exception")
                .build()
            return@coroutineScope Result.success()
        } catch (e: Exception) {
            val outData = Data.Builder()
                .putString("CommunicationWorker", e.message ?: "Unknown Exception")
                .build()
            return@coroutineScope Result.failure(outData)
        } finally {
            tinfoilNet.close()
        }

//        when(proto) {
//            Protocol.TinfoilNET -> tinfoilNet.run(files, nsIp, port)
//            Protocol.TinfoilUSB -> tinfoilUsb.run(files)
//        }


        return@coroutineScope Result.success()
    }
}