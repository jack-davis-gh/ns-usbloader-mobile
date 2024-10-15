package com.blogspot.developersu.ns_usbloader.home

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.blogspot.developersu.ns_usbloader.model.Protocol

class HomeViewModel: ViewModel() {

    var fileUris by mutableStateOf(emptyList<Uri>())
        private set

    var activeProtocol by mutableStateOf<Protocol>(Protocol.Tinfoil.USB)
        private set

    fun updateActiveProtocol(protocol: Protocol) {
        activeProtocol = protocol
    }

    fun addFileUri(fileUri: Uri) {
        if (fileUri !in fileUris) {
            fileUris += fileUri
        }
    }
}