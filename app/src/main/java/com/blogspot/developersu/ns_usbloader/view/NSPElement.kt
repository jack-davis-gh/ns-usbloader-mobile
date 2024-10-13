package com.blogspot.developersu.ns_usbloader.view

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable

class NSPElement : Parcelable {
    var uri: Uri
        private set
    var filename: String?
        private set
    var size: Long
        private set
    @JvmField
    var status: String?
    var isSelected: Boolean

    constructor(uri: Uri, filename: String?, size: Long) {
        this.uri = uri
        this.filename = filename
        this.size = size
        this.status = ""
        this.isSelected = false
    }

    /*-----------------------
    / Parcelable shit next
    /-----------------------*/
    private constructor(parcel: Parcel) {
        uri = Uri.parse(parcel.readString())
        filename = parcel.readString()
        size = parcel.readLong()
        status = parcel.readString()
        isSelected = parcel.readByte().toInt() == 0x1
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(outParcel: Parcel, flags: Int) {
        outParcel.writeString(uri.toString())
        outParcel.writeString(filename)
        outParcel.writeLong(size)
        outParcel.writeString(status)
        outParcel.writeByte((if (isSelected) 0x1 else 0x0).toByte())
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<NSPElement?> {
            override fun createFromParcel(`in`: Parcel): NSPElement? {
                return NSPElement(`in`)
            }

            override fun newArray(size: Int): Array<NSPElement?> {
                return arrayOfNulls(size)
            }
        }
    }
}
