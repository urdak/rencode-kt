package net.ickis.rencode.types

import net.ickis.rencode.RencodeInputStream
import net.ickis.rencode.RencodeOutputStream

/**
 * Type [T] that can be written to a [RencodeOutputStream] and read from a [RencodeInputStream].
 */
abstract class TokenType<T> {
    /**
     * Writes an [obj] to the provided [ros].
     */
    abstract fun write(ros: RencodeOutputStream, obj: T?)

    /**
     * Reads an object from [ris]. The token [token] is usually used to determine the type.
     * For some cases, the same byte (the token byte) also represents the value of the type.
     */
    abstract fun read(ris: RencodeInputStream, token: Int): T?
}

/**
 * Token type represented by a single id.
 */
abstract class IdType<T>(val id: Int): TokenType<T>()

/**
 * Token type represented by an id in a range of ids.
 */
abstract class RangeType<T>(val range: IntRange): TokenType<T>() {
    val length = range.count()
}

/**
 * Type representing a boolean value.
 */
abstract class BooleanType(private val value: Boolean, id: Int): IdType<Boolean>(id) {
    override fun write(ros: RencodeOutputStream, obj: Boolean?) = ros.write(id)
    override fun read(ris: RencodeInputStream, token: Int) = value
}

object TrueType: BooleanType(true, 67)

object FalseType: BooleanType(false, 68)

/**
 * Type representing the end of the stream.
 */
object EndType: IdType<Nothing>(127) {
    override fun write(ros: RencodeOutputStream, obj: Nothing?) = ros.write(id)
    override fun read(ris: RencodeInputStream, token: Int): Nothing = throw UnsupportedOperationException()
}

/**
 * Type representing a null.
 */
object NullType: IdType<Unit>(69) {
    override fun write(ros: RencodeOutputStream, obj: Unit?) = ros.write(id)
    override fun read(ris: RencodeInputStream, token: Int): Unit? = null
}
