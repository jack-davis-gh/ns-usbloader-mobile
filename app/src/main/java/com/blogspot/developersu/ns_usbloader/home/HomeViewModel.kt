package com.blogspot.developersu.ns_usbloader.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.blogspot.developersu.ns_usbloader.model.Protocol

interface H

class HomeViewModel: ViewModel() {

    var activeProtocol by mutableStateOf(Protocol.Tinfoil.USB)
}