package com.blogspot.developersu.ns_usbloader.service

import android.app.IntentService
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.ResultReceiver
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.blogspot.developersu.ns_usbloader.MainActivity
import com.blogspot.developersu.ns_usbloader.NsConstants
import com.blogspot.developersu.ns_usbloader.R
import com.blogspot.developersu.ns_usbloader.core.model.NSFile
import java.util.concurrent.atomic.AtomicBoolean

class CommunicationsService : IntentService(SERVICE_TAG) {
//    private var issueDescription: String? = null
//    private var transferTask: TransferTask? = null
//
//    private var nspElements: ArrayList<NSFile>? = null
//    private var status = ""
//
//    var usbDevice: UsbDevice? = null
//
//    var nsIp: String? = null
//    var phoneIp: String? = null
//    var phonePort: Int = 0
//
    override fun onHandleIntent(intent: Intent?) {
//        isActive.set(true)
//
//        val notificationBuilder = NotificationCompat.Builder(
//            baseContext,
//            NsConstants.NOTIFICATION_FOREGROUND_SERVICE_CHAN_ID
//        )
//            .setSmallIcon(R.drawable.ic_notification)
//            .setPriority(NotificationCompat.PRIORITY_LOW)
//            .setContentTitle(getString(R.string.notification_transfer_in_progress))
//            .setProgress(0, 0, true)
//            .setOnlyAlertOnce(true)
//            .setContentIntent(
//                PendingIntent.getActivity(
//                    this, 0, Intent(
//                        this,
//                        MainActivity::class.java
//                    ), FLAG_IMMUTABLE // TODO more flags that should be 0
//                )
//            )
//
//        startForeground(NsConstants.NOTIFICATION_TRANSFER_ID, notificationBuilder.build())
//
//
//        val resultReceiver =
//            intent?.getParcelableExtra<ResultReceiver>(NsConstants.NS_RESULT_RECEIVER)
//        nspElements = intent?.getParcelableArrayListExtra(NsConstants.SERVICE_CONTENT_NSP_LIST)
//        val protocol = intent?.getIntExtra(NsConstants.SERVICE_CONTENT_PROTOCOL, PROTOCOL_UNKNOWN)
//
//        // Clear statuses
//        for (e in nspElements!!) e.status = ""
//
//        try {
//            when (protocol) {
//                NsConstants.PROTO_TF_USB -> {
//                    getDataForUsbTransfer(intent)
//                    transferTask = TinfoilUSB(
//                        resultReceiver!!,
//                        applicationContext, usbDevice!!, getSystemService(USB_SERVICE) as UsbManager,
//                        nspElements!!
//                    )
//                }
//
//                NsConstants.PROTO_GL_USB -> {
//                    getDataForUsbTransfer(intent)
//                    transferTask = GoldLeaf(
//                        resultReceiver!!,
//                        applicationContext, usbDevice!!, getSystemService(USB_SERVICE) as UsbManager,
//                        nspElements!!
//                    )
//                }
//
//                NsConstants.PROTO_TF_NET -> {
//                    getDataForNetTransfer(intent)
//                    transferTask = TinfoilNET(
//                        resultReceiver!!, applicationContext,
//                        nspElements!!, nsIp!!, phoneIp!!, phonePort
//                    )
//                }
//
//                else -> finish()
//            }
//        } catch (e: Exception) {
//            this.issueDescription = e.message
//            status = getString(R.string.status_failed_to_upload)
//            finish()
//            return
//        }
//
//        transferTask!!.run()
//        this.issueDescription = transferTask!!.issueDescription
//        status = transferTask!!.status
//        /*
//        Log.i("LPR", "Status " +status);
//        Log.i("LPR", "issue " +issueDescription);
//        Log.i("LPR", "Interrupt " +transferTask.interrupt.get());
//        Log.i("LPR", "Active " +isActive.get());
//        */
//        finish()
//        stopForeground(true)
//        // Now we have to hide what has to be hidden. This part of code MUST be here right after stopForeground():
//        this.hideNotification(applicationContext)
    }
//
//    private fun getDataForUsbTransfer(intent: Intent) {
//        this.usbDevice = intent.getParcelableExtra(NsConstants.SERVICE_CONTENT_NS_DEVICE)
//    }
//
//    private fun getDataForNetTransfer(intent: Intent) {
//        this.nsIp = intent.getStringExtra(NsConstants.SERVICE_CONTENT_NS_DEVICE_IP)
//        this.phoneIp = intent.getStringExtra(NsConstants.SERVICE_CONTENT_PHONE_IP)
//        this.phonePort = intent.getIntExtra(NsConstants.SERVICE_CONTENT_PHONE_PORT, 6042)
//    }
//
//    private fun finish() {
//        // Set status if not already set
//        for (e in nspElements!!) if (e.status!!.isEmpty()) e.status = status
//        isActive.set(false)
//        val executionFinishIntent = Intent(NsConstants.SERVICE_TRANSFER_TASK_FINISHED_INTENT)
//        executionFinishIntent.putExtra(NsConstants.SERVICE_CONTENT_NSP_LIST, nspElements)
//        if (issueDescription != null) {
//            executionFinishIntent.putExtra("ISSUES", issueDescription)
//        }
//        this.sendBroadcast(executionFinishIntent)
//    }
//
//    fun hideNotification(context: Context) {
//        NotificationManagerCompat.from(context).cancel(NsConstants.NOTIFICATION_TRANSFER_ID)
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        if (transferTask != null) transferTask!!.cancel()
//    }
//
    companion object {
        private const val SERVICE_TAG =
            "com.blogspot.developersu.ns_usbloader.Service.CommunicationsService"
        private const val PROTOCOL_UNKNOWN = -1

        private val isActive = AtomicBoolean(false)

        @JvmStatic
        val isServiceActive: Boolean
            get() = isActive.get()
    }
}