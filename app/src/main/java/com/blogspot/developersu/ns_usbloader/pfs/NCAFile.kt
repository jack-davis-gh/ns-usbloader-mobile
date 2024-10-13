package com.blogspot.developersu.ns_usbloader.pfs

import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Data class to hold NCA, tik, xml etc. meta-information
 */
class NCAFile {
    //public void setNcaNumber(int ncaNumber){ this.ncaNumber = ncaNumber; }
    //public int getNcaNumber() {return this.ncaNumber; }
    //private int ncaNumber;
//    @kotlin.jvm.JvmField
    var ncaFileName: ByteArray = byteArrayOf()
    var ncaOffset: Long = 0
    var ncaSize: Long = 0

    val ncaFileNameLength: ByteArray
        get() = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
            .putInt(ncaFileName.size).array()
}
