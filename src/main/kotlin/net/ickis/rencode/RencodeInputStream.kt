package net.ickis.rencode

import net.ickis.rencode.types.*
import java.io.Closeable
import java.io.EOFException
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.charset.Charset

/**
 * An [InputStream] for reading the "rencoded" objects.
 */
class RencodeInputStream @JvmOverloads constructor(
        private val stream: InputStream,
        val charset: Charset = Charset.forName("UTF-8")
): InputStream() {
    fun readObject(): Any? = readObject(readToken())

    fun readByte(): Byte = readType()

    fun readShort(): Short = readType()

    fun readInt(): Int = readType()

    fun readLong(): Long = readType()

    fun readFloat(): Float = readType()

    fun readDouble(): Double = readType()

    fun readBoolean(): Boolean = readType()

    fun readString(): String = readType()

    fun readList(): List<*> = readType()

    fun readMap(): Map<*, *> = readType()

    fun readNumber(): Number = readType()

    override fun read() = stream.read()

    internal fun readToken(): Int {
        return stream.read().takeIf { it != -1 } ?: throw EOFException()
    }

    internal fun readObject(token: Int): Any? {
        return getTokenType(token).read(this, token)
    }

    internal fun readToBuffer(size: Int) = ByteBuffer.wrap(readBytes(size))

    internal fun readBytes(len: Int): ByteArray {
        val array = ByteArray(len)
        if (len < 0)
            throw IndexOutOfBoundsException()
        var n = 0
        while (n < len) {
            val count = stream.read(array, n, len - n)
            if (count < 0)
                throw EOFException()
            n += count
        }
        return array
    }

    private inline fun <reified T, reified C: TokenType<T>> readType(): T {
        val token = readToken()
        return when (val type = getTokenType(token)) {
            is C -> type.read(this, token)!!
            else -> throw IOException()
        }
    }

    private fun getTokenType(token: Int) = SupportedTypes.get(token)

    internal object SupportedTypes {
        internal val idMap: Map<Int, IdType<out Any>> = listOf(
                DoubleType,
                LargeListType,
                LargeMapType,
                BigNumberType,
                ByteType,
                ShortType,
                IntType,
                LongType,
                FloatType,
                TrueType,
                FalseType,
                NullType,
                EndType
        ).associateBy({ k -> k.id }, { k -> k })

        internal val rangeList = listOf(
                PositiveIntType,
                NegativeIntType,
                LargeStringType,
                StringType,
                ListType,
                MapType
        )

        fun get(token: Int): TokenType<out Any> = idMap[token]
                ?: rangeList.find { token in it.range }
                ?: throw IOException("Unsupported token $token")
    }
}
