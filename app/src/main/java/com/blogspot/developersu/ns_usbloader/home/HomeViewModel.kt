package com.blogspot.developersu.ns_usbloader.home

import android.content.ContentResolver
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import com.blogspot.developersu.ns_usbloader.NsConstants
import com.blogspot.developersu.ns_usbloader.NsConstants.json
import com.blogspot.developersu.ns_usbloader.core.data.FileRepo
import com.blogspot.developersu.ns_usbloader.core.model.NSFile
import com.blogspot.developersu.ns_usbloader.core.model.Protocol
import com.blogspot.developersu.ns_usbloader.core.work.TinfoilUsbWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val usbManager: UsbManager,
    private val workManager: WorkManager,
    private val fileRepo: FileRepo,
    private val contentResolver: ContentResolver
): ViewModel() {

    val files = fileRepo.getFiles()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )

    var activeProtocol by mutableStateOf<Protocol>(Protocol.Tinfoil.USB)
        private set

    fun updateActiveProtocol(protocol: Protocol) {
        activeProtocol = protocol
    }

    fun onFileUriSelected(file: PlatformFile) = viewModelScope.launch {
        val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        contentResolver.takePersistableUriPermission(file.uri, takeFlags)
        fileRepo.upsertFile(file)
    }

    fun onClickFile(file: NSFile) = viewModelScope.launch { fileRepo.upsertFile(file.copy(isSelected = !file.isSelected)) }

    fun uploadFile() = viewModelScope.launch {
        val usbDevice: UsbDevice? =
            usbManager.deviceList.values.firstOrNull { device -> device.vendorId == 1406 && device.productId == 12288 }

        if (usbDevice != null && usbManager.hasPermission(usbDevice)) {
            fileRepo.getSelectedFile().collect { activeFiles ->
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
}