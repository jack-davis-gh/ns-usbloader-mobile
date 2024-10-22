package com.github.jack_davis_gh.ns_usbloader.core.transfer_protocol

interface TransferProto {
    fun open(): Result<Unit>
    fun close(): Result<Unit>
}