package net.ickis.rencode

import net.ickis.rencode.types.*
import java.io.IOException
import java.io.OutputStream
import java.math.BigDecimal
import java.math.BigInteger
import java.nio.charset.Charset

/**
 * An [OutputStream] for "rencoding" objects.
 */
class RencodeOutputStream @JvmOverloads constructor(
        private val stream: OutputStream, 
        val charset: Charset = Charset.forName("UTF-8")
): OutputStream() {
    fun writeObject(obj: Any?) {
        val tokenType: TokenType<Any?> = getTokenType(obj)
        tokenType.write(this, obj)
    }

    fun writeByte(v: Byte) = writeObject(v)

    fun writeShort(v: Short) = writeObject(v)

    fun writeInt(v: Int) = writeObject(v)

    fun writeLong(v: Long) = writeObject(v)

    fun writeFloat(v: Float) = writeObject(v)

    fun writeDouble(v: Double) = writeObject(v)

    fun writeBoolean(v: Boolean) = writeObject(v)

    fun writeString(v: String) = writeObject(v)

    fun writeList(v: List<*>) = writeObject(v)

    fun writeMap(v: Map<*, *>) = writeObject(v)

    fun writeNumber(v: Number) = writeObject(v)

    override fun write(byte: Int) = stream.write(byte)

    override fun flush() = stream.flush()

    override fun close() = stream.close()

    @Suppress("UNCHECKED_CAST")
    private fun <T> getTokenType(obj: T?): TokenType<in T> = when (obj) {
        null -> NullType
        true -> TrueType
        false -> FalseType
        is BigDecimal, is BigInteger -> BigNumberType
        is Byte -> ByteType
        is Char -> CharType
        is Short -> ShortType
        is Long -> LongType
        is Float -> FloatType
        is Double -> DoubleType
        is Int -> when (obj) {
            in PositiveIntType.range -> PositiveIntType
            in NegativeIntType.values -> NegativeIntType
            else -> IntType
        }
        is String -> if (obj.length < StringType.length) StringType else LargeStringType
        is Collection<*> -> if (obj.size < ListType.length) ListType else LargeListType
        is Array<*>,
        is ByteArray,
        is CharArray,
        is ShortArray,
        is IntArray,
        is LongArray,
        is FloatArray,
        is DoubleArray,
        is BooleanArray -> if (java.lang.reflect.Array.getLength(obj) < ArrayType.length) ArrayType else LargeArrayType
        is Map<*, *> -> if (obj.size < MapType.length) MapType else LargeMapType
        else -> {
            throw IOException("Unsupported type $obj")
        }
    } as TokenType<in T>
}
