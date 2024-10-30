package com.github.jack_davis_gh.ns_usbloader.core.common

fun Boolean.asResult(exception: Exception): Result<Unit> {
    return if (this) {
        Result.success(Unit)
    } else {
        Result.failure(exception)
    }
}