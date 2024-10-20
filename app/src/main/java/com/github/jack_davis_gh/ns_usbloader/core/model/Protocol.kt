package com.github.jack_davis_gh.ns_usbloader.core.model

import kotlinx.serialization.Serializable

@Serializable
sealed interface Protocol {
    @Serializable data object TinfoilUSB: Protocol
    @Serializable data object TinfoilNET: Protocol
//    data object GoldLeafUSB: Protocol TODO Removing this for now, idk whats changed from 0.5 to 1.0 of goldleaf
//     and its already a lot just trying to figure out the new tinfoil stuff
}