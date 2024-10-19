package com.blogspot.developersu.ns_usbloader.core.work

import android.content.Context
import android.net.Uri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.blogspot.developersu.ns_usbloader.NsConstants
import com.blogspot.developersu.ns_usbloader.NsConstants.json
import com.blogspot.developersu.ns_usbloader.core.usb.TinfoilUsb
import com.blogspot.developersu.ns_usbloader.core.model.NSFile
import com.blogspot.developersu.ns_usbloader.core.usb.UsbTransfer
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.coroutineScope

@HiltWorker
class TinfoilUsbWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    @Assisted private val transfer: UsbTransfer
): CoroutineWorker(appContext, workerParams) {
    override suspend fun getForegroundInfo(): ForegroundInfo = createForegroundInfo()

    override suspend fun doWork(): Result = coroutineScope {

        val jsonStr = inputData.getString(NsConstants.SERVICE_CONTENT_NSP_LIST) ?: return@coroutineScope Result.failure()
        val files: List<NSFile> = json.decodeFromString(jsonStr)
        setForeground(getForegroundInfo())

        try {
            transfer.open()

            val tinfoil = TinfoilUsb(transfer) { file ->
                val inputStream = applicationContext.contentResolver.openInputStream(Uri.parse(file.uri))
                if (inputStream != null)
                    kotlin.Result.success(inputStream)
                else
                    kotlin.Result.failure(Exception("Unable to open file, name = ${file.name} uri = ${file.uri}"))
            }

            tinfoil.sendFiles(files)
        } catch (e: Exception) {
            val outData = Data.Builder()
                .putString("TinfoilUsbWorker", e.message ?: "Unknown Exception")
                .build()
            return@coroutineScope Result.failure(outData)
        } finally {
            transfer.close()
        }

        return@coroutineScope Result.success()
    }


}