package net.ickis.rencode.types

import net.ickis.rencode.RencodeInputStream
import net.ickis.rencode.RencodeOutputStream

/**
 * Type representing a list of any objects.
 */
object ListType: RangeType<Collection<Any?>>(192 until 256) {
    override fun write(ros: RencodeOutputStream, obj: Collection<Any?>?) {
        val list = obj!!
        ros.write(range.start + list.size)
        list.forEach { ros.writeObject(it) }
    }

    override fun read(ris: RencodeInputStream, token: Int): Collection<Any?> {
        val list = ArrayList<Any?>()
        val length = token - range.start

        repeat(length) {
            list.add(ris.readObject())
        }

        return list
    }
}

/**
 * Type representing a list of any objects, whose size is greater than the length of the
 * range specified by the [ListType].
 */
object LargeListType: IdType<Collection<Any?>>(59) {
    override fun write(ros: RencodeOutputStream, obj: Collection<Any?>?) {
        ros.write(id)
        obj!!.forEach { ros.writeObject(it) }
        ros.write(EndType.id)
    }

    override fun read(ris: RencodeInputStream, token: Int): Collection<Any?> {
        val list = ArrayList<Any?>()

        while (true) {
            val tmp = ris.readToken()
            if (tmp == EndType.id) break
            list.add(ris.readObject(tmp))
        }

        return list
    }
}

private fun MutableMap<Any?, Any?>.readItem(ris: RencodeInputStream, token: Int) {
    val key = ris.readObject(token)
    val value = ris.readObject()
    this[key] = value
}

private fun Map<*, *>.writeItems(ros: RencodeOutputStream) {
    forEach { k, v ->
        ros.writeObject(k)
        ros.writeObject(v)
    }
}

/**
 * Type representing a map of any objects to any objects.
 */
object MapType: RangeType<Map<*, *>>(102 until 127) {
    override fun write(ros: RencodeOutputStream, obj: Map<*, *>?) {
        val map = obj!!
        ros.write(range.start + map.size)
        map.writeItems(ros)
    }

    override fun read(ris: RencodeInputStream, token: Int): Map<*, *> {
        val map = HashMap<Any?, Any?>()
        val length = token - range.start

        repeat(length) {
            map.readItem(ris, ris.readToken())
        }

        return map
    }
}

/**
 * Type representing a map of any objects to any objects, whose size is greater than the length of the
 * range specified by the [MapType].
 */
object LargeMapType: IdType<Map<*, *>>(60) {
    override fun write(ros: RencodeOutputStream, obj: Map<*, *>?) {
        ros.write(id)
        obj!!.writeItems(ros)
        ros.write(EndType.id)
    }

    override fun read(ris: RencodeInputStream, token: Int): Map<*, *> {
        val map = HashMap<Any?, Any?>()

        while (true) {
            val tmp = ris.readToken()
            if (tmp == EndType.id) break
            map.readItem(ris, tmp)
        }

        return map
    }
}
