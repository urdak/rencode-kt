package net.ickis.rencode

import net.ickis.rencode.types.SupportedType
import net.ickis.rencode.types.TokenType
import java.io.FilterOutputStream
import java.io.IOException
import java.io.OutputStream
import java.nio.charset.Charset

class RencodeOutputStream @JvmOverloads constructor(
        stream: OutputStream,
        val charset: Charset = Charset.forName("UTF-8")
): FilterOutputStream(stream) {
    fun writeByte(v: Byte) = write(v, SupportedType.BYTE)

    fun writeShort(v: Short) = write(v, SupportedType.SHORT)

    fun writeInt(v: Int) = write(v, SupportedType.INT)

    fun writeLong(v: Long) = write(v, SupportedType.LONG)

    fun writeFloat(v: Float) = write(v, SupportedType.FLOAT)

    fun writeDouble(v: Double) = write(v, SupportedType.DOUBLE)

    fun writeBoolean(v: Boolean) = write(v, SupportedType.BOOLEAN)

    fun writeString(v: String) = write(v, SupportedType.STRING)

    fun writeList(v: List<*>) = write(v, SupportedType.COLLECTION)

    fun writeMap(v: Map<*, *>) = write(v, SupportedType.MAP)

    fun writeNumber(v: Number) = write(v, SupportedType.NUMBER)

    fun writeObject(obj: Any?) {
        val tokenType = SupportedType.findByValue(obj)
                ?: throw IOException("Unsupported type ${obj?.javaClass}")
        writeToken(obj, tokenType)
    }

    private fun <T: Any> write(obj: T, type: SupportedType) {
        val tokenType = type.parseValue(obj)
                ?: throw IOException("Unexpected type $obj")
        writeToken(obj, tokenType)
    }

    private fun <T: Any?> writeToken(obj: T, tokenType: TokenType<*>) {
        @Suppress("UNCHECKED_CAST")
        tokenType as TokenType<T>
        tokenType.write(this, obj)
    }
}
