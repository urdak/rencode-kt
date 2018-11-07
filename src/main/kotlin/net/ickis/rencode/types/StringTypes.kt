package net.ickis.rencode.types

import net.ickis.rencode.RencodeInputStream
import net.ickis.rencode.RencodeOutputStream

/**
 * Type representing a String.
 */
object StringType: RangeType<String>(128 until 192) {
    override fun write(ros: RencodeOutputStream, obj: String?) {
        val str = obj!!
        val length = str.length
        ros.write(range.start + length)
        ros.write(str.toByteArray(ros.charset))
    }

    override fun read(ris: RencodeInputStream, token: Int): String {
        val length = token - range.first
        return String(ris.readBytes(length), ris.charset)
    }
}

/**
 * Type representing a String, whose size is greater than the length of the range specified by the [StringType].
 */
object LargeStringType: RangeType<String>(('0'.toInt()..'9'.toInt())) {
    private const val LENGTH_TOKEN = ':'

    override fun write(ros: RencodeOutputStream, obj: String?) {
        val str = obj!!
        ros.write(str.length.toString().toByteArray(ros.charset))
        ros.write(LENGTH_TOKEN.toInt())
        ros.write(str.toByteArray(ros.charset))
    }

    override fun read(ris: RencodeInputStream, token: Int): String {
        val size = readLength(ris, token)
        val byteArray = ris.readBytes(size)
        return String(byteArray, ris.charset)
    }

    private fun readLength(ris: RencodeInputStream, token: Int): Int {
        val sb = StringBuilder()
        sb.append(token.toChar())
        while (true) {
            val tmp = ris.readToken().toChar()
            if (tmp == LENGTH_TOKEN) break
            sb.append(tmp)
        }
        return Integer.parseInt(sb.toString())
    }
}
