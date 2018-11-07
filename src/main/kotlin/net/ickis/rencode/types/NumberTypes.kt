package net.ickis.rencode.types

import net.ickis.rencode.RencodeInputStream
import net.ickis.rencode.RencodeOutputStream
import java.io.IOException
import java.math.BigDecimal
import java.math.BigInteger
import java.nio.ByteBuffer

/**
 * Generic Number type.
 */
abstract class NumberType<T: Number>(
        id: Int,
        private val size: Int,
        private val bufToNum: (ByteBuffer) -> T,
        private val numToBuf: (ByteBuffer, T) -> ByteBuffer
): IdType<T>(id) {
    override fun read(ris: RencodeInputStream, token: Int) = bufToNum.invoke(ris.readToBuffer(size))

    override fun write(ros: RencodeOutputStream, obj: T?) {
        ros.write(id)
        val buffer = ByteBuffer.allocate(size)
        numToBuf(buffer, obj!!)
        ros.write(buffer.array())
    }
}

object ByteType: NumberType<Byte>(62, 1, ByteBuffer::get, ByteBuffer::put)

object ShortType: NumberType<Short>(63, 2, ByteBuffer::getShort, ByteBuffer::putShort)

object IntType: NumberType<Int>(64, 4, ByteBuffer::getInt, ByteBuffer::putInt)

object DoubleType: NumberType<Double>(44, 8, ByteBuffer::getDouble, ByteBuffer::putDouble)

object LongType: NumberType<Long>(65, 8, ByteBuffer::getLong, ByteBuffer::putLong)

object FloatType: NumberType<Float>(66, 4, ByteBuffer::getFloat, ByteBuffer::putFloat)

/**
 * Type representing a [BigInteger] or a [BigDecimal].
 */
object BigNumberType: IdType<Number>(61) {
    override fun write(ros: RencodeOutputStream, obj: Number?) {
        ros.write(id)
        ros.write(obj!!.toString().toByteArray(ros.charset))
        ros.write(EndType.id)
    }

    override fun read(ris: RencodeInputStream, token: Int): Number? {
        val sb = StringBuilder()
        var decimal = false

        while (true) {
            val tmp = ris.readToken()
            if (tmp == EndType.id) break
            val char = tmp.toChar()
            if (char == '.') decimal = true
            sb.append(char)
        }

        val str = sb.toString()

        try {
            return if (decimal) BigDecimal(str) else BigInteger(str)
        } catch (ex: NumberFormatException) {
            throw IOException(ex)
        }
    }
}

/**
 * A write-only type for Chars. Converts the Char to a Byte.
 */
object CharType: IdType<Char>(ByteType.id) {
    override fun write(ros: RencodeOutputStream, obj: Char?) = ByteType.write(ros, obj!!.toByte())
    override fun read(ris: RencodeInputStream, token: Int)
            = throw UnsupportedOperationException("Read Byte instead.")
}

/**
 * Type representing an integer value between 0 and 43.
 */
object PositiveIntType: RangeType<Int>(0 until 44) {
    override fun write(ros: RencodeOutputStream, obj: Int?) = ros.write(obj!!)
    override fun read(ris: RencodeInputStream, token: Int) = token
}

/**
 * Type representing an integer value between -32 to -1.
 */
object NegativeIntType: RangeType<Int>(70 until 102) {
    val values = (range.first - range.last) until 0
    override fun write(ros: RencodeOutputStream, obj: Int?) = ros.write(range.start - 1 - obj!!)
    override fun read(ris: RencodeInputStream, token: Int): Int = range.first - 1 - token
}
