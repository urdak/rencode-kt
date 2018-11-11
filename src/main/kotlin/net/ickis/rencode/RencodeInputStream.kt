package net.ickis.rencode

import net.ickis.rencode.types.SupportedType
import java.io.EOFException
import java.io.FilterInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.charset.Charset

class RencodeInputStream @JvmOverloads constructor(
        stream: InputStream,
        val charset: Charset = Charset.forName("UTF-8")
): FilterInputStream(stream) {
    fun readByte(): Byte = readType(SupportedType.BYTE)

    fun readShort(): Short = readType(SupportedType.SHORT)

    fun readInt(): Int = readType(SupportedType.INT)

    fun readLong(): Long = readType(SupportedType.LONG)

    fun readFloat(): Float = readType(SupportedType.FLOAT)

    fun readDouble(): Double = readType(SupportedType.DOUBLE)

    fun readBoolean(): Boolean = readType(SupportedType.BOOLEAN)

    fun readString(): String = readType(SupportedType.STRING)

    fun readList(): List<*> = readType(SupportedType.COLLECTION)

    fun readMap(): Map<*, *> = readType(SupportedType.MAP)

    fun readNumber(): Number = readType(SupportedType.NUMBER)

    fun readObject(): Any? {
        return readObject(readToken())
    }

    private fun <T: Any> readType(type: SupportedType): T {
        val token = readToken()
        val tokenType = type.parseId(token)
                ?: throw IOException("Unexpected token $token for $type")
        @Suppress("UNCHECKED_CAST")
        return tokenType.read(this, token) as T
    }

    internal fun readToken(): Int {
        return read().takeIf { it != -1 } ?: throw EOFException()
    }

    internal fun readObject(token: Int): Any? {
        val tokenType = SupportedType.findById(token)
                ?: throw IOException("No mapping found for token $token")
        return tokenType.read(this, token)
    }

    internal fun readToBuffer(size: Int) = ByteBuffer.wrap(readBytes(size))

    internal fun readBytes(len: Int): ByteArray {
        val array = ByteArray(len)
        if (len < 0)
            throw IndexOutOfBoundsException()
        var n = 0
        while (n < len) {
            val count = read(array, n, len - n)
            if (count < 0)
                throw EOFException()
            n += count
        }
        return array
    }
}
