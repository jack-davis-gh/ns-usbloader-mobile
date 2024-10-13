package com.blogspot.developersu.ns_usbloader.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.ResultReceiver
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.blogspot.developersu.ns_usbloader.MainActivity
import com.blogspot.developersu.ns_usbloader.NsConstants
import com.blogspot.developersu.ns_usbloader.R
import kotlin.concurrent.Volatile

internal abstract class TransferTask(
    private val resultReceiver: ResultReceiver,
    var context: Context
) {
    private var notificationManager: NotificationManager? = null

    private val notificationBuilder: NotificationCompat.Builder

    open var issueDescription: String? = null
    @JvmField
    var status: String = ""

    @Volatile
    var interrupt: Boolean = false

    init {
        this.createNotificationChannel()
        this.notificationBuilder =
            NotificationCompat.Builder(context, NsConstants.NOTIFICATION_FOREGROUND_SERVICE_CHAN_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentTitle(context.getString(R.string.notification_transfer_in_progress))
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .setContentIntent(
                    PendingIntent.getActivity(
                        context, 0, Intent(
                            context,
                            MainActivity::class.java
                        ), FLAG_IMMUTABLE // TODO Some more flags that should be 0, but zero makes it upset
                    )
                )
    }

    fun resetProgressBar() {
        resultReceiver.send(NsConstants.NS_RESULT_PROGRESS_INDETERMINATE, Bundle.EMPTY)
        resetNotificationProgressBar()
    }

    fun updateProgressBar(currentPosition: Int) {
        val bundle = Bundle()
        bundle.putInt("POSITION", currentPosition)
        resultReceiver.send(NsConstants.NS_RESULT_PROGRESS_VALUE, bundle)
        updateNotificationProgressBar(currentPosition)
    }

    /**
     * Main work routine here
     * @return true if issue, false if not
     */
    abstract fun run(): Boolean

    /**
     * What shall we do in case of user interruption
     */
    open fun cancel() {
        interrupt = true
    }

    private fun updateNotificationProgressBar(value: Int) {
        val notify = notificationBuilder.setProgress(100, value, false).setContentText(
            "$value%"
        ).build()
        if (isModernAndroidOs) {
            notificationManager!!.notify(NsConstants.NOTIFICATION_TRANSFER_ID, notify)
            return
        }
        NotificationManagerCompat.from(context).notify(NsConstants.NOTIFICATION_TRANSFER_ID, notify)
    }

    private fun resetNotificationProgressBar() {
        val notify = notificationBuilder.setProgress(0, 0, true).setContentText("").build()

        if (isModernAndroidOs) {
            notificationManager!!.notify(NsConstants.NOTIFICATION_TRANSFER_ID, notify)
            return
        }
        NotificationManagerCompat.from(context).notify(NsConstants.NOTIFICATION_TRANSFER_ID, notify)
    }

    private fun createNotificationChannel() {
        if (isModernAndroidOs) {
            val notificationChanName: CharSequence =
                context.getString(R.string.notification_chan_name_progress)
            val notificationChanDesc = context.getString(R.string.notification_chan_desc_progress)

            val notificationChannel = NotificationChannel(
                NsConstants.NOTIFICATION_FOREGROUND_SERVICE_CHAN_ID,
                notificationChanName,
                NotificationManager.IMPORTANCE_LOW
            )
            notificationChannel.description = notificationChanDesc
            notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager!!.createNotificationChannel(notificationChannel)
        }
    }

    companion object {
        private val isModernAndroidOs = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    }
}
