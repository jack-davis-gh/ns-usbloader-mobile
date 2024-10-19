package com.blogspot.developersu.ns_usbloader.core.model

import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver

class NsResultReciever(handler: Handler?) : ResultReceiver(handler) {
    interface Receiver {
        fun onReceiveResults(code: Int, bundle: Bundle?)
    }

    private var mReceiver: Receiver? = null

    fun setReceiver(receiver: Receiver?) {
        this.mReceiver = receiver
    }

    override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
        mReceiver?.onReceiveResults(resultCode, resultData)
    }
}
