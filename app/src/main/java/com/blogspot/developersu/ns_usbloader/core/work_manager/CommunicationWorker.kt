package com.blogspot.developersu.ns_usbloader.core.work_manager

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.blogspot.developersu.ns_usbloader.NsConstants
import com.blogspot.developersu.ns_usbloader.NsConstants.json
import com.blogspot.developersu.ns_usbloader.core.proto.TinfoilUsb
import com.blogspot.developersu.ns_usbloader.core.model.NSFile
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.coroutineScope

@HiltWorker
class CommunicationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    @Assisted private val tinfoilUsb: TinfoilUsb
): CoroutineWorker(appContext, workerParams) {
    override suspend fun getForegroundInfo(): ForegroundInfo = createForegroundInfo()

    override suspend fun doWork(): Result = coroutineScope {
        val jsonStr = inputData.getString(NsConstants.SERVICE_CONTENT_NSP_LIST) ?: return@coroutineScope Result.failure()
        val files: List<NSFile> = json.decodeFromString(jsonStr)
        setForeground(getForegroundInfo())

        tinfoilUsb.run(files)
            .onFailure {
                val outData = Data.Builder()
                    .putString("TinfoilUsbWorker", it.message ?: "Unknown Exception")
                    .build()
                return@coroutineScope Result.failure(outData)
            }

        return@coroutineScope Result.success()
    }
}