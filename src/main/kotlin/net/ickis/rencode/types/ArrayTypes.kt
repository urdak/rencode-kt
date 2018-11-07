package net.ickis.rencode.types

import net.ickis.rencode.RencodeInputStream
import net.ickis.rencode.RencodeOutputStream
import java.lang.reflect.Array

private fun Any?.toList(): List<*> {
    val len = Array.getLength(this)
    return (0 until len).map { Array.get(this, it) }
}

/**
 * A write-only type for Arrays. Converts the Array to a List.
 *
 * @see ListType
 */
object ArrayType: RangeType<Any>(ListType.range) {
    override fun write(ros: RencodeOutputStream, obj: Any?) = ListType.write(ros, obj.toList())
    override fun read(ris: RencodeInputStream, token: Int) = throw UnsupportedOperationException()
}

/**
 * A write-only type for long Arrays. Converts the Array to a List.
 *
 * @see LargeListType
 */
object LargeArrayType: IdType<Any>(LargeListType.id) {
    override fun write(ros: RencodeOutputStream, obj: Any?) = LargeListType.write(ros, obj.toList())
    override fun read(ris: RencodeInputStream, token: Int) = throw UnsupportedOperationException()
}
