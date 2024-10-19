package com.blogspot.developersu.ns_usbloader.core.model

sealed interface Protocol {
    sealed interface Tinfoil: Protocol {
        data object USB: Tinfoil
        data object NET: Tinfoil
    }
    data object GoldLeafUSB: Protocol

    fun asInt() = when(this) {
        Tinfoil.USB -> 0
        Tinfoil.NET -> 1
        GoldLeafUSB -> 2
    }
}

fun Int.asProto() = when(this) {
    0 -> Protocol.Tinfoil.USB
    1 -> Protocol.Tinfoil.NET
    else -> Protocol.GoldLeafUSB
}