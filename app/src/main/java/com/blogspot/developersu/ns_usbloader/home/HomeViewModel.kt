package com.blogspot.developersu.ns_usbloader.home

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import com.blogspot.developersu.ns_usbloader.NsConstants
import com.blogspot.developersu.ns_usbloader.NsConstants.json
import com.blogspot.developersu.ns_usbloader.model.NSFile
import com.blogspot.developersu.ns_usbloader.model.Protocol
import com.blogspot.developersu.ns_usbloader.core.work.TinfoilUsbWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.serialization.encodeToString
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val usbManager: UsbManager,
    private val workManager: WorkManager,
): ViewModel() {

    var files by mutableStateOf(emptyList<NSFile>())
        private set

    private val activeFiles: List<NSFile>
        get() = files.filter(NSFile::isSelected)

    var activeProtocol by mutableStateOf<Protocol>(Protocol.Tinfoil.USB)
        private set

    fun updateActiveProtocol(protocol: Protocol) {
        activeProtocol = protocol
    }

    fun selectFileUri(file: NSFile) {
        if (file !in files) {
            files += file
        }
    }

    fun onClickFile(file: NSFile) {
        val oldFiles = files.filter { it.name != file.name }
        files = oldFiles + file.copy(isSelected = !file.isSelected)
    }

    fun uploadFile() {
        val usbDevice: UsbDevice? = usbManager.deviceList.values
            .filter { device -> device.vendorId == 1406 && device.productId == 12288 }
            .firstOrNull()

        if (usbDevice != null && usbManager.hasPermission(usbDevice)) {
            val inputData = Data.Builder()
                .putString(NsConstants.SERVICE_CONTENT_NSP_LIST,
                    json.encodeToString(activeFiles))
                .build()

            val request = OneTimeWorkRequestBuilder<TinfoilUsbWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setInputData(inputData)
                .build()

            workManager.cancelAllWork()
            workManager.enqueueUniqueWork(
                "USB Transfer",
                ExistingWorkPolicy.KEEP,
                request
            )
        }
    }
}