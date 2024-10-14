package com.blogspot.developersu.ns_usbloader.model

sealed interface Protocol {
    sealed interface Tinfoil: Protocol {
        data object USB: Tinfoil
        data object NET: Tinfoil
    }
    data object GoldLeafUSB: Protocol
}