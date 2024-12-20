package com.github.jack_davis_gh.ns_usbloader.home

import android.content.ContentResolver
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import com.github.jack_davis_gh.ns_usbloader.NsConstants
import com.github.jack_davis_gh.ns_usbloader.NsConstants.json
import com.github.jack_davis_gh.ns_usbloader.core.common.stateIn
import com.github.jack_davis_gh.ns_usbloader.core.data.FileRepo
import com.github.jack_davis_gh.ns_usbloader.core.datastore.SettingsStore
import com.github.jack_davis_gh.ns_usbloader.core.model.NSFile
import com.github.jack_davis_gh.ns_usbloader.core.model.Protocol
import com.github.jack_davis_gh.ns_usbloader.core.platform.usb.UsbManager
import com.github.jack_davis_gh.ns_usbloader.core.work_manager.CommunicationWorker
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import me.tatarka.inject.annotations.Inject

@Inject
class HomeViewModel(
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
    }.stateIn(viewModelScope, emptyList())

    private val activeProtocol = settingsStore.appSettings
        .map { it.activeProto }
        .stateIn(viewModelScope, Protocol.USB)

    val state = combine(files, activeFiles, activeProtocol) { files, activeFiles, activeProtocol ->
        HomeScreenState.Success(
            activeProtocol = activeProtocol,
            files = files,
            selectedFileNum = activeFiles.size
        )
    }.stateIn(viewModelScope, HomeScreenState.Loading)

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
                val files = files.firstOrNull() ?: return@launch // TODO Error State
                val settings = settingsStore.appSettings.firstOrNull() ?: return@launch

                val activeNsFiles = files.filter { it.name in activeFiles.value }

                val inputData = Data.Builder()
                    .putString(
                        NsConstants.SERVICE_CONTENT_NSP_LIST,
                        json.encodeToString(activeNsFiles)
                    )
                    .putString(
                        NsConstants.SERVICE_CONTENT_PROTOCOL,
                        json.encodeToString(settings.activeProto)
                    )
                    .putString(NsConstants.SERVICE_CONTENT_NS_DEVICE_IP, settings.nsIp)
                    .putInt(NsConstants.SERVICE_CONTENT_PHONE_PORT, settings.phonePort)
                    .build()

                val request = when (settings.activeProto) {
                    Protocol.Network -> {
                        val constraints = Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.UNMETERED)
                            .build()

                        OneTimeWorkRequestBuilder<CommunicationWorker>()
                            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                            .setConstraints(constraints)
                    }

                    Protocol.USB -> OneTimeWorkRequestBuilder<CommunicationWorker>()
                        .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)

                }.setInputData(inputData).build()

                if (settings.activeProto == Protocol.Network || (settings.activeProto == Protocol.USB && usbManager.getNs() != null)) {
                    workManager.cancelAllWork()
                    workManager.enqueueUniqueWork(
                        "Ns Transfer",
                        ExistingWorkPolicy.KEEP,
                        request
                    )
                }
            }
        }
    }
}