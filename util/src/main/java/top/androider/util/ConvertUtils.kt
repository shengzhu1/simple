package top.androider.util

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import android.view.View
import org.json.JSONArray
import org.json.JSONObject
import top.androider.util.constant.MemoryConstants
import top.androider.util.constant.TimeConstants
import java.io.*
import java.nio.charset.Charset
import java.util.*

/**
 * <pre>
 * author: Blankj
 * blog  : http://blankj.com
 * time  : 2016/08/13
 * desc  : utils about convert
</pre> *
 */
class ConvertUtils {
    companion object {
        private const val BUFFER_SIZE = 8192
        private val HEX_DIGITS_UPPER = charArrayOf('0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F')
        private val HEX_DIGITS_LOWER = charArrayOf('0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f')

        /**
         * Int to hex string.
         *
         * @param num The int number.
         * @return the hex string
         */
        fun int2HexString(num: Int): String {
            return Integer.toHexString(num)
        }

        /**
         * Hex string to int.
         *
         * @param hexString The hex string.
         * @return the int
         */
        fun hexString2Int(hexString: String): Int {
            return hexString.toInt(16)
        }

        /**
         * Bytes to bits.
         *
         * @param bytes The bytes.
         * @return bits
         */
        fun bytes2Bits(bytes: ByteArray?): String? {
            if (bytes == null || bytes.size == 0) return ""
            val sb = StringBuilder()
            for (aByte in bytes) {
                for (j in 7 downTo 0) {
                    sb.append(if (aByte shr j and 0x01 == 0.toByte()) '0' else '1')
                }
            }
            return sb.toString()
        }

        /**
         * Bits to bytes.
         *
         * @param bits The bits.
         * @return bytes
         */
        fun bits2Bytes(bits: String): ByteArray {
            var bits = bits
            val lenMod = bits.length % 8
            var byteLen = bits.length / 8
            // add "0" until length to 8 times
            if (lenMod != 0) {
                for (i in lenMod..7) {
                    bits = "0$bits"
                }
                byteLen++
            }
            val bytes = ByteArray(byteLen)
            for (i in 0 until byteLen) {
                for (j in 0..7) {
                    bytes[i] = bytes[i] shl 1
                    bytes[i] = bytes[i] or (bits[i * 8 + j] - '0').toByte()
                }
            }
            return bytes
        }

        /**
         * Bytes to chars.
         *
         * @param bytes The bytes.
         * @return chars
         */
        fun bytes2Chars(bytes: ByteArray?): CharArray? {
            if (bytes == null) return null
            val len = bytes.size
            if (len <= 0) return null
            val chars = CharArray(len)
            for (i in 0 until len) {
                chars[i] = (bytes[i] and 0xff.toByte()).toChar()
            }
            return chars
        }

        /**
         * Chars to bytes.
         *
         * @param chars The chars.
         * @return bytes
         */
        fun chars2Bytes(chars: CharArray?): ByteArray? {
            if (chars == null || chars.size <= 0) return null
            val len = chars.size
            val bytes = ByteArray(len)
            for (i in 0 until len) {
                bytes[i] = chars[i].toByte()
            }
            return bytes
        }
        /**
         * Bytes to hex string.
         *
         * e.g. bytes2HexString(new byte[] { 0, (byte) 0xa8 }, true) returns "00A8"
         *
         * @param bytes       The bytes.
         * @param isUpperCase True to use upper case, false otherwise.
         * @return hex string
         */
        /**
         * Bytes to hex string.
         *
         * e.g. bytes2HexString(new byte[] { 0, (byte) 0xa8 }) returns "00A8"
         *
         * @param bytes The bytes.
         * @return hex string
         */
        @JvmOverloads
        fun bytes2HexString(bytes: ByteArray?, isUpperCase: Boolean = true): String {
            if (bytes == null) return ""
            val hexDigits = if (isUpperCase) HEX_DIGITS_UPPER else HEX_DIGITS_LOWER
            val len = bytes.size
            if (len <= 0) return ""
            val ret = CharArray(len shl 1)
            var i = 0
            var j = 0
            while (i < len) {
                ret[j++] = hexDigits[(bytes[i] shr 4 and 0x0f).toInt()]
                ret[j++] = hexDigits[(bytes[i] and 0x0f).toInt()]
                i++
            }
            return String(ret)
        }

        /**
         * Hex string to bytes.
         *
         * e.g. hexString2Bytes("00A8") returns { 0, (byte) 0xA8 }
         *
         * @param hexString The hex string.
         * @return the bytes
         */
        fun hexString2Bytes(hexString: String?): ByteArray {
            var hexString = hexString?:""
            if (isSpace(hexString)) return ByteArray(0)
            var len = hexString.length
            if (len % 2 != 0) {
                hexString = "0$hexString"
                len = len + 1
            }
            val hexBytes = hexString.toUpperCase().toCharArray()
            val ret = ByteArray(len shr 1)
            var i = 0
            while (i < len) {
                ret[i shr 1] = (hex2Dec(hexBytes[i]) shl 4 or hex2Dec(
                    hexBytes[i + 1]
                )).toByte()
                i += 2
            }
            return ret
        }

        private fun hex2Dec(hexChar: Char): Int {
            return if (hexChar >= '0' && hexChar <= '9') {
                hexChar - '0'
            } else if (hexChar >= 'A' && hexChar <= 'F') {
                hexChar - 'A' + 10
            } else {
                throw IllegalArgumentException()
            }
        }
        /**
         * Bytes to string.
         */
        /**
         * Bytes to string.
         */
        @JvmOverloads
        fun bytes2String(bytes: ByteArray?, charsetName: String = ""): String? {
            return if (bytes == null) null else try {
                String(bytes, charset(getSafeCharset(charsetName)))
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
                String(bytes)
            }
        }
        /**
         * String to bytes.
         */
        /**
         * String to bytes.
         */
        @JvmOverloads
        fun string2Bytes(string: String?, charsetName: String = ""): ByteArray? {
            return if (string == null) null else try {
                string.toByteArray(charset(getSafeCharset(charsetName)))
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
                string.toByteArray()
            }
        }

        /**
         * Bytes to JSONObject.
         */
        fun bytes2JSONObject(bytes: ByteArray?): JSONObject? {
            return if (bytes == null) null else try {
                JSONObject(String(bytes))
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        /**
         * JSONObject to bytes.
         */
        fun jsonObject2Bytes(jsonObject: JSONObject?): ByteArray? {
            return jsonObject?.toString()?.toByteArray()
        }

        /**
         * Bytes to JSONArray.
         */
        fun bytes2JSONArray(bytes: ByteArray?): JSONArray? {
            return if (bytes == null) null else try {
                JSONArray(String(bytes))
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        /**
         * JSONArray to bytes.
         */
        fun jsonArray2Bytes(jsonArray: JSONArray?): ByteArray? {
            return jsonArray?.toString()?.toByteArray()
        }

        /**
         * Bytes to Parcelable
         */
        fun <T> bytes2Parcelable(
            bytes: ByteArray?,
            creator: Parcelable.Creator<T>
        ): T? {
            if (bytes == null) return null
            val parcel = Parcel.obtain()
            parcel.unmarshall(bytes, 0, bytes.size)
            parcel.setDataPosition(0)
            val result = creator.createFromParcel(parcel)
            parcel.recycle()
            return result
        }

        /**
         * Parcelable to bytes.
         */
        fun parcelable2Bytes(parcelable: Parcelable?): ByteArray? {
            if (parcelable == null) return null
            val parcel = Parcel.obtain()
            parcelable.writeToParcel(parcel, 0)
            val bytes = parcel.marshall()
            parcel.recycle()
            return bytes
        }

        /**
         * Bytes to Serializable.
         */
        fun bytes2Object(bytes: ByteArray?): Any? {
            if (bytes == null) return null
            var ois: ObjectInputStream? = null
            return try {
                ois = ObjectInputStream(ByteArrayInputStream(bytes))
                ois.readObject()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            } finally {
                try {
                    ois?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        /**
         * Serializable to bytes.
         */
        fun serializable2Bytes(serializable: Serializable?): ByteArray? {
            if (serializable == null) return null
            var baos: ByteArrayOutputStream
            var oos: ObjectOutputStream? = null
            return try {
                oos = ObjectOutputStream(ByteArrayOutputStream().also { baos = it })
                oos.writeObject(serializable)
                baos.toByteArray()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            } finally {
                try {
                    oos?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        /**
         * Size of memory in unit to size of byte.
         *
         * @param memorySize Size of memory.
         * @param unit       The unit of memory size.
         *
         *  * [MemoryConstants.BYTE]
         *  * [MemoryConstants.KB]
         *  * [MemoryConstants.MB]
         *  * [MemoryConstants.GB]
         *
         * @return size of byte
         */
        fun memorySize2Byte(
            memorySize: Long,
            @MemoryConstants.Unit unit: Int
        ): Long {
            return if (memorySize < 0) -1 else memorySize * unit
        }

        /**
         * Size of byte to size of memory in unit.
         *
         * @param byteSize Size of byte.
         * @param unit     The unit of memory size.
         *
         *  * [MemoryConstants.BYTE]
         *  * [MemoryConstants.KB]
         *  * [MemoryConstants.MB]
         *  * [MemoryConstants.GB]
         *
         * @return size of memory in unit
         */
        fun byte2MemorySize(
            byteSize: Long,
            @MemoryConstants.Unit unit: Int
        ): Double {
            return if (byteSize < 0) (-1).toDouble() else byteSize.toDouble() / unit
        }

        /**
         * Size of byte to fit size of memory.
         *
         * to three decimal places
         *
         * @param byteSize Size of byte.
         * @return fit size of memory
         */
        @SuppressLint("DefaultLocale")
        fun byte2FitMemorySize(byteSize: Long): String {
            return byte2FitMemorySize(byteSize, 3)
        }

        /**
         * Size of byte to fit size of memory.
         *
         * to three decimal places
         *
         * @param byteSize  Size of byte.
         * @param precision The precision
         * @return fit size of memory
         */
        @SuppressLint("DefaultLocale")
        fun byte2FitMemorySize(byteSize: Long, precision: Int): String {
            require(precision >= 0) { "precision shouldn't be less than zero!" }
            return if (byteSize < 0) {
                throw IllegalArgumentException("byteSize shouldn't be less than zero!")
            } else if (byteSize < MemoryConstants.KB) {
                String.format("%." + precision + "fB", byteSize.toDouble())
            } else if (byteSize < MemoryConstants.MB) {
                String.format("%." + precision + "fKB", byteSize.toDouble() / MemoryConstants.KB)
            } else if (byteSize < MemoryConstants.GB) {
                String.format("%." + precision + "fMB", byteSize.toDouble() / MemoryConstants.MB)
            } else {
                String.format("%." + precision + "fGB", byteSize.toDouble() / MemoryConstants.GB)
            }
        }


        /**
         * Input stream to output stream.
         */
        fun input2OutputStream(`is`: InputStream?): ByteArrayOutputStream? {
            return if (`is` == null) null else try {
                val os = ByteArrayOutputStream()
                val b = ByteArray(BUFFER_SIZE)
                var len: Int
                while (`is`.read(b, 0, BUFFER_SIZE).also { len = it } != -1) {
                    os.write(b, 0, len)
                }
                os
            } catch (e: IOException) {
                e.printStackTrace()
                null
            } finally {
                try {
                    `is`.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        /**
         * Output stream to input stream.
         */
        fun output2InputStream(out: OutputStream?): ByteArrayInputStream? {
            return if (out == null) null else ByteArrayInputStream((out as ByteArrayOutputStream).toByteArray())
        }

        /**
         * Input stream to bytes.
         */
        fun inputStream2Bytes(`is`: InputStream?): ByteArray? {
            return if (`is` == null) null else input2OutputStream(`is`)!!
                .toByteArray()
        }

        /**
         * Bytes to input stream.
         */
        fun bytes2InputStream(bytes: ByteArray?): InputStream? {
            return if (bytes == null || bytes.size <= 0) null else ByteArrayInputStream(bytes)
        }

        /**
         * Output stream to bytes.
         */
        fun outputStream2Bytes(out: OutputStream?): ByteArray? {
            return if (out == null) null else (out as ByteArrayOutputStream).toByteArray()
        }

        /**
         * Bytes to output stream.
         */
        fun bytes2OutputStream(bytes: ByteArray?): OutputStream? {
            if (bytes == null || bytes.size <= 0) return null
            var os: ByteArrayOutputStream? = null
            return try {
                os = ByteArrayOutputStream()
                os.write(bytes)
                os
            } catch (e: IOException) {
                e.printStackTrace()
                null
            } finally {
                try {
                    os?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        /**
         * Input stream to string.
         */
        fun inputStream2String(`is`: InputStream?, charsetName: String): String {
            return if (`is` == null) "" else try {
                val baos = input2OutputStream(`is`) ?: return ""
                baos.toString(getSafeCharset(charsetName))
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
                ""
            }
        }

        /**
         * String to input stream.
         */
        fun string2InputStream(string: String?, charsetName: String): InputStream? {
            return if (string == null) null else try {
                ByteArrayInputStream(string.toByteArray(charset(getSafeCharset(charsetName))))
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
                null
            }
        }

        /**
         * Output stream to string.
         */
        fun outputStream2String(out: OutputStream?, charsetName: String): String {
            return if (out == null) "" else try {
                String(outputStream2Bytes(out)!!, charset(getSafeCharset(charsetName)))
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
                ""
            }
        }

        /**
         * String to output stream.
         */
        fun string2OutputStream(string: String?, charsetName: String): OutputStream? {
            return if (string == null) null else try {
                bytes2OutputStream(string.toByteArray(charset(getSafeCharset(charsetName))))
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
                null
            }
        }

        @JvmOverloads
        fun inputStream2Lines(
            `is`: InputStream?,
            charsetName: String = ""
        ): List<String>? {
            var reader: BufferedReader? = null
            return try {
                val list: MutableList<String> = ArrayList()
                reader = BufferedReader(InputStreamReader(`is`, getSafeCharset(charsetName)))
                var line: String
                while (reader.readLine().also { line = it } != null) {
                    list.add(line)
                }
                list
            } catch (e: IOException) {
                e.printStackTrace()
                null
            } finally {
                try {
                    reader?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        private fun getSafeCharset(charsetName: String): String {
            var cn = charsetName
            if (isSpace(charsetName) || !Charset.isSupported(charsetName)) {
                cn = "UTF-8"
            }
            return cn
        }
    }
}