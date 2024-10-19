package com.blogspot.developersu.ns_usbloader.core.common

fun Boolean.asResult(exceptionMsg: String): Result<Unit> {
    return if (this) {
        Result.success(Unit)
    } else {
        Result.failure(Exception(exceptionMsg))
    }
}