package com.github.jack_davis_gh.ns_usbloader

import kotlinx.serialization.json.Json

object NsConstants {
    const val NS_RESULT_RECEIVER: String = "RECEIVER"

    // Request permissions to access NS USB device
    const val REQUEST_NS_ACCESS_INTENT: String =
        "com.github.jack_davis_gh.ns_usbloader.ACTION_USB_PERMISSION"

    // Get in BroadcastReceiver and MainActivity's broadcastReceiver information regarding finished process
    const val SERVICE_TRANSFER_TASK_FINISHED_INTENT: String =
        "com.github.jack_davis_gh.ns_usbloader.SERVICE_TRANSFER_TASK_FINISHED"

    // To get data inside IntentService
    const val SERVICE_CONTENT_NSP_LIST: String = "NSP_LIST"
    const val SERVICE_CONTENT_PROTOCOL: String = "PROTOCOL"
    const val SERVICE_CONTENT_NS_DEVICE: String = "DEVICE"
    const val SERVICE_CONTENT_NS_DEVICE_IP: String = "DEVICE_IP"
    const val SERVICE_CONTENT_PHONE_IP: String = "PHONE_IP"
    const val SERVICE_CONTENT_PHONE_PORT: String = "PHONE_PORT"

    // Result Reciever possible codes
    const val NS_RESULT_PROGRESS_INDETERMINATE: Int = -1 // upper limit would be 0; value would be 0
    const val NS_RESULT_PROGRESS_VALUE: Int = 0

    // Declare TF/GL names
    const val PROTO_TF_USB: Int = 10
    const val PROTO_TF_NET: Int = 20
    const val PROTO_GL_USB: Int = 30

    const val NOTIFICATION_FOREGROUND_SERVICE_CHAN_ID: String =
        "com.github.jack_davis_gh.ns_usbloader.CHAN_ID_FOREGROUND_SERVICE"
    const val NOTIFICATION_TRANSFER_ID: Int = 1
    val json: Json = Json.Default
}
