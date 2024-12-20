package com.github.jack_davis_gh.ns_usbloader.core.work_manager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import com.github.jack_davis_gh.ns_usbloader.MainActivity
import com.github.jack_davis_gh.ns_usbloader.NsConstants
import com.github.jack_davis_gh.ns_usbloader.R

fun CoroutineWorker.createForegroundInfo(downloadProgress: Int? = null): ForegroundInfo {
    val notificationManager = applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    // This PendingIntent can be used to cancel the worker
    val intent = WorkManager.getInstance(applicationContext)
        .createCancelPendingIntent(id)

    // Create a Notification channel
    val channel = NotificationChannel(
        NsConstants.NOTIFICATION_FOREGROUND_SERVICE_CHAN_ID,
        applicationContext.getString(R.string.notification_chan_name_usb),
        NotificationManager.IMPORTANCE_DEFAULT
    ).apply {
        description = applicationContext.getString(R.string.notification_chan_desc_usb)
        enableVibration(false)
    }
    // Register the channel with the system. You can't change the importance
    // or other notification behaviors after this.
    notificationManager.createNotificationChannel(channel)

    val notification = NotificationCompat.Builder(applicationContext, NsConstants.NOTIFICATION_FOREGROUND_SERVICE_CHAN_ID)
        .setContentTitle(applicationContext.getString(R.string.notification_transfer_in_progress))
//            .setContentText(progress)
        .setSmallIcon(R.drawable.ic_notification)
        .setOngoing(true)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setVibrate(null)
        // Add the cancel action to the notification which can
        // be used to cancel the worker
        .addAction(android.R.drawable.ic_delete, "Cancel", intent)
        .setContentIntent(
            PendingIntent.getActivity(
                applicationContext, 0, Intent(
                    applicationContext,
                    MainActivity::class.java
                ), FLAG_IMMUTABLE
            )
        )

    if (downloadProgress != null) {
        notification.setProgress(100, downloadProgress, false)
    }

    return ForegroundInfo(
        NsConstants.NOTIFICATION_TRANSFER_ID, notification.build(), FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
    )
}