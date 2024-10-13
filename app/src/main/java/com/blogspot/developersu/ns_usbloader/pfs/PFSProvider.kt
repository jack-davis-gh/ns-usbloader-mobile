package com.blogspot.developersu.ns_usbloader.pfs

import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Arrays
import java.util.Locale

/**
 * Used in GoldLeaf USB protocol
 */
class PFSProvider(inputStream: InputStream?, nspFileName: String?) {
    private val bufferedInStream: BufferedInputStream?
    private val nspFileName: String?
    private var ncaFiles: Array<NCAFile?> = arrayOf()

    /**
     * Return bodySize
     */
    var bodySize: Long = 0
        private set

    /**
     * Return special NCA file: ticket
     * (sugar)
     */
    var ncaTicketID: Int = -1
        private set

    init {
        if (inputStream != null && nspFileName != null) {
            this.bufferedInStream = BufferedInputStream(inputStream) // TODO: refactor?
            this.nspFileName = nspFileName
        } else {
            this.bufferedInStream = null
            this.nspFileName = null
        }
    }

    fun init(): Boolean {
        if (nspFileName == null || bufferedInStream == null) return false

        val filesCount: Int
        val stringTableSize: Int

        try {
            val fileStartingBytes: ByteArray = ByteArray(12)
            // Read PFS0, files count, stringTableSize, padding (4 zero bytes)
            if (bufferedInStream.read(fileStartingBytes) != 12) {
                bufferedInStream.close()
                return false
            }
            // Check PFS0
            if (!PFS0.contentEquals(Arrays.copyOfRange(fileStartingBytes, 0, 4))) {
                bufferedInStream.close()
                return false
            }
            // Get files count
            filesCount = ByteBuffer.wrap(Arrays.copyOfRange(fileStartingBytes, 4, 8)).order(
                ByteOrder.LITTLE_ENDIAN
            ).getInt()
            if (filesCount <= 0) {
                bufferedInStream.close()
                return false
            }
            // Get stringTableSize
            stringTableSize = ByteBuffer.wrap(Arrays.copyOfRange(fileStartingBytes, 8, 12)).order(
                ByteOrder.LITTLE_ENDIAN
            ).getInt()
            if (stringTableSize <= 0) {
                bufferedInStream.close()
                return false
            }
            //*********************************************************************************************
            // Create NCA set
            this.ncaFiles = arrayOfNulls(filesCount)
            // Collect files from NSP
            val ncaInfoArr: ByteArray =
                ByteArray(24) // should be unsigned long, but.. java.. u know my pain man

            val ncaNameOffsets: HashMap<Int, Long> = LinkedHashMap()

            var nca_offset: Long
            var nca_size: Long
            var nca_name_offset: Long

            for (i in 0 until filesCount) {
                if (bufferedInStream.read(ncaInfoArr) != 24) {
                    bufferedInStream.close()
                    return false
                }

                nca_offset = ByteBuffer.wrap(Arrays.copyOfRange(ncaInfoArr, 4, 12))
                    .order(ByteOrder.LITTLE_ENDIAN).getLong()
                nca_size = ByteBuffer.wrap(Arrays.copyOfRange(ncaInfoArr, 12, 20))
                    .order(ByteOrder.LITTLE_ENDIAN).getLong()
                nca_name_offset = ByteBuffer.wrap(Arrays.copyOfRange(ncaInfoArr, 20, 24)).order(
                    ByteOrder.LITTLE_ENDIAN
                ).getInt().toLong() // yes, cast from int to long.

                val ncaFile: NCAFile = NCAFile()
                ncaFile.ncaOffset = nca_offset
                ncaFile.ncaSize = nca_size
                ncaFiles[i] = ncaFile

                ncaNameOffsets[i] = nca_name_offset
            }
            // Final offset
            val bufForInt: ByteArray = ByteArray(4)
            if ((bufferedInStream.read(bufForInt) != 4)) return false

            // Calculate position including stringTableSize for body size offset
            //bodySize = bufferedInStream.getFilePointer()+stringTableSize;
            bodySize = (filesCount * 24 + 16 + stringTableSize).toLong()
            //*********************************************************************************************
            bufferedInStream.mark(stringTableSize)
            // Collect file names from NCAs
            var ncaFN: MutableList<Byte> // Temporary
            val b: ByteArray = ByteArray(1) // Temporary
            for (i in 0 until filesCount) {
                ncaFN = ArrayList()
                if (bufferedInStream.skip(ncaNameOffsets.get(i)!!) != ncaNameOffsets.get(i))  // Files cont * 24(bit for each meta-data) + 4 bytes goes after all of them  + 12 bit what were in the beginning
                    return false
                while ((bufferedInStream.read(b)) != -1) {
                    if (b.get(0).toInt() == 0x00) break
                    else ncaFN.add(b.get(0))
                }

                // TODO: CHANGE TO ncaFN.toArray();
                val exchangeTempArray: ByteArray = ByteArray(ncaFN.size)
                for (j in ncaFN.indices) exchangeTempArray[j] = ncaFN[j]
                // Find and store ticket (.tik)
                if (String(exchangeTempArray, charset("UTF-8")).lowercase(Locale.getDefault())
                        .endsWith(".tik")
                ) this.ncaTicketID = i
                ncaFiles.get(i)!!.ncaFileName = exchangeTempArray.copyOf(exchangeTempArray.size)

                bufferedInStream.reset()
            }
            bufferedInStream.close()
        } catch (ioe: IOException) {
            return false
        }
        return true
    }

    val bytesNspFileName: ByteArray
        /**
         * Return file name as byte array
         */
        get() = nspFileName!!.toByteArray()

    /**
     * Return file name as String
     */
    /*  Legacy code; leave for now
    public String getStringNspFileName(){
        return nspFileName;
    }
    */
    val bytesNspFileNameLength: ByteArray
        /**
         * Return file name length as byte array
         */
        get() = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
            .putInt(bytesNspFileName.size).array()
    val bytesCountOfNca: ByteArray
        /**
         * Return NCA count inside of file as byte array
         */
        get() {
            return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
                .putInt(ncaFiles.size).array()
        }
    val intCountOfNca: Int
        /**
         * Return NCA count inside of file as int
         */
        get() {
            return ncaFiles.size
        }

    /**
     * Return requested-by-number NCA file inside of file
     */
    fun getNca(ncaNumber: Int): NCAFile? {
        return ncaFiles.get(ncaNumber)
    }

    companion object {
        private val PFS0: ByteArray = byteArrayOf(
            0x50.toByte(),
            0x46.toByte(),
            0x53.toByte(),
            0x30.toByte()
        ) // PFS0, and what did you think?
    }
}
