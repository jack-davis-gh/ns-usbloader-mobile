package com.blogspot.developersu.ns_usbloader.home

import android.content.ContentResolver
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
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
import com.blogspot.developersu.ns_usbloader.core.datastore.SettingsStore
import com.blogspot.developersu.ns_usbloader.core.model.NSFile
import com.blogspot.developersu.ns_usbloader.core.model.Protocol
import com.blogspot.developersu.ns_usbloader.core.work.TinfoilUsbWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val usbManager: UsbManager,
    private val workManager: WorkManager,
    private val fileRepo: FileRepo,
    private val contentResolver: ContentResolver,
    settingsStore: SettingsStore
): ViewModel() {
    private var activeFiles = MutableStateFlow(emptyList<String>())

    private val files = fileRepo.getFiles().combine(activeFiles) { files, activeFiles ->
        files.map { file ->
            file.copy(isSelected = file.name in activeFiles)
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    private val activeProtocol = settingsStore.settings
        .map { it.activeProto }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            Protocol.Tinfoil.USB
        )

    val state = combine(files, activeFiles, activeProtocol) { files, activeFiles, activeProtocol ->
        HomeScreenState.Success(activeProtocol = activeProtocol, files = files, selectedFileNum = activeFiles.size)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        HomeScreenState.Loading
    )

    val callbacks = object : HomeScreenCallbacks {
        override fun onFileUriSelected(file: PlatformFile) {
            viewModelScope.launch {
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                contentResolver.takePersistableUriPermission(file.uri, takeFlags)
                fileRepo.upsertFile(file)
            }
        }

        override fun onDeleteSelected() {
            viewModelScope.launch {
                fileRepo.deleteFiles(files.value.filter { it.isSelected })
                activeFiles.value = emptyList()
            }
        }

        override fun onClearSelected() {
            activeFiles.value = emptyList()
        }

        override fun onFileClicked(file: NSFile) {
            if (file.name in activeFiles.value) {
                activeFiles.value -= file.name
            } else {
                activeFiles.value += file.name
            }
        }

        override fun onUploadFileClicked() {
            viewModelScope.launch {
                val usbDevice: UsbDevice? =
                    usbManager.deviceList.values.firstOrNull { device -> device.vendorId == 1406 && device.productId == 12288 }

                if (usbDevice != null && usbManager.hasPermission(usbDevice)) {
                    files.collect { files ->
                        val activeFiles = files.filter { it.name in activeFiles.value }
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
    }
}