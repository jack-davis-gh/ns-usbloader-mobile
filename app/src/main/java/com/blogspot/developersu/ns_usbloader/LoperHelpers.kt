package com.blogspot.developersu.ns_usbloader

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

object LoperHelpers {
    @JvmStatic
    fun getFileNameFromUri(item: Uri, context: Context): String? {
        var result: String? = null

        val cursor = context.contentResolver.query(item, null, null, null, null)

        if (cursor != null && cursor.moveToFirst()) {
            result = cursor.getString(
                cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            )
            cursor.close()
        }

        return result
    }

    @JvmStatic
    fun getFileSizeFromUri(item: Uri, context: Context): Long {
        var result: Long = -1
        val cursor = context.contentResolver.query(item, null, null, null, null)

        if (cursor != null && cursor.moveToFirst()) {
            result = cursor.getLong(
                cursor.getColumnIndex(OpenableColumns.SIZE)
            )
            cursor.close()
        }

        return result
    }
}