package com.blogspot.developersu.ns_usbloader.core.common

@Suppress("NOTHING_TO_INLINE")
inline fun <T> T?.mapToResult(
    exception: Exception = Exception("NPE mapToResult")
) = if (this == null) {
    Result.failure(exception)
} else {
    Result.success(this)
}