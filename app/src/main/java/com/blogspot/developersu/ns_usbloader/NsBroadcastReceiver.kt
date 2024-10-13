package com.blogspot.developersu.ns_usbloader

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class NsBroadcastReceiver : BroadcastReceiver() {
    @Synchronized
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == null) return
        if (intent.action == NsConstants.SERVICE_TRANSFER_TASK_FINISHED_INTENT) {
            val issues = intent.getStringExtra("ISSUES")
            if (issues != null) {
                Toast.makeText(
                    context,
                    context.getString(R.string.transfers_service_stopped) + " " + issues,
                    Toast.LENGTH_LONG
                ).show()
                return
            }
            Toast.makeText(
                context,
                context.getString(R.string.transfers_service_stopped),
                Toast.LENGTH_SHORT
            ).show()
        }
    } // .setContentIntent(PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class).putExtra(UsbManager.EXTRA_DEVICE, usbDevice), 0));
}