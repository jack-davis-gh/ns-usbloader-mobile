package com.blogspot.developersu.ns_usbloader

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface

object NsNotificationPopUp {
    fun getAlertWindow(context: Context?, title: String?, message: String?) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton(
                "OK"
            ) { dialogInterface: DialogInterface, i: Int -> dialogInterface.dismiss() }
        builder.create().show()
    }
}
