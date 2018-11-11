package net.ickis.rencode

import net.ickis.rencode.types.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.IOException
import java.math.BigDecimal
import java.math.BigInteger

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RencodeTest {
    private fun assertEncodeDecode(expectedObject: Any?, expectedByteArray: ByteArray) {
        val baos = ByteArrayOutputStream()
        RencodeOutputStream(baos).use {
            it.writeObject(expectedObject)
        }
        val actualByteArray = baos.toByteArray()
        assertArrayEquals(expectedByteArray, actualByteArray)
        val actualObject = RencodeInputStream(ByteArrayInputStream(actualByteArray)).use {
            it.readObject()
        }
        assertEquals(expectedObject, actualObject)
    }

    private fun assertEncodeDecode(expectedObject: Any?, type: IdType<*>) {
        assertEncodeDecode(expectedObject, ByteArray(1) { type.id.toByte() })
    }

    private fun createByteArray(consumer: (DataOutputStream).() -> Unit): ByteArray {
        val baos = ByteArrayOutputStream()
        DataOutputStream(baos).use {
            consumer(it)
        }
        return baos.toByteArray()
    }

    @Test
    fun `Byte Serialization`() {
        val byte = 'b'.toByte()
        assertEncodeDecode(byte, listOf(ByteType.id.toByte(), byte).toByteArray())
    }

    @Test
    fun `Short Serialization`() {
        val short = 128.toShort()
        val array = createByteArray {
            writeByte(ShortType.id)
            writeShort(short.toInt())
        }
        assertEncodeDecode(short, array)
    }

    @Test
    fun `Int Serialization`() {
        val int = 228322
        val array = createByteArray {
            writeByte(IntType.id)
            writeInt(int)
        }
        assertEncodeDecode(int, array)
    }

    @Test
    fun `Double Serialization`() {
        val double = 0.123
        val array = createByteArray {
            writeByte(DoubleType.id)
            writeDouble(double)
        }
        assertEncodeDecode(double, array)
    }

    @Test
    fun `Long Serialization`() {
        val long = 100000000000000
        val array = createByteArray {
            writeByte(LongType.id)
            writeLong(long)
        }
        assertEncodeDecode(long, array)
    }

    @Test
    fun `Float Serialization`() {
        val float = 1.213.toFloat()
        val array = createByteArray {
            writeByte(FloatType.id)
            writeFloat(float)
        }
        assertEncodeDecode(float, array)
    }

    @Test
    fun `BigDecimal Serialization`() {
        val bigDecimal = BigDecimal(123.456)
        val array = createByteArray {
            writeByte(BigNumberType.id)
            writeBytes(bigDecimal.toString())
            writeByte(EndType.id)
        }
        assertEncodeDecode(bigDecimal, array)
    }

    @Test
    fun `BigInteger Serialization`() {
        val bigInt = BigInteger("345734590893457893457893453458934589")
        val array = createByteArray {
            writeByte(BigNumberType.id)
            writeBytes(bigInt.toString())
            writeByte(EndType.id)
        }
        assertEncodeDecode(bigInt, array)
    }

    @Test
    fun `Positive Int Serialization`() {
        val int = 10
        assertEncodeDecode(int, createByteArray { writeByte(int) })
    }

    @Test
    fun `Negative Int Serialization`() {
        val int = -10
        assertEncodeDecode(int, createByteArray { writeByte(79) })
    }

    @Test
    fun `Char to Byte Serialization`() {
        val char = 'c'
        val baos = ByteArrayOutputStream()
        RencodeOutputStream(baos).use {
            it.writeObject(char)
        }
        val actualByteArray = baos.toByteArray()
        val expectedByteArray = createByteArray {
            writeByte(ByteType.id)
            writeByte(char.toInt())
        }
        assertArrayEquals(expectedByteArray, actualByteArray)
        val actualObject = RencodeInputStream(ByteArrayInputStream(actualByteArray)).use {
            it.readByte()
        }
        assertEquals(char.toByte(), actualObject)
    }

    @Test
    fun `Null Serialization`() {
        assertEncodeDecode(null, NullType)
    }

    @Test
    fun `Boolean Serialization`() {
        assertEncodeDecode(true, TrueType)
        assertEncodeDecode(false, FalseType)
    }
    
    @Test
    fun `String Serialization`() {
        val range = 'a'..'z'
        val alphabet = range.asSequence().joinToString("")
        val length = alphabet.length
        val array = createByteArray {
            writeByte(length + StringType.range.start)
            writeBytes(alphabet)
        }
        assertEncodeDecode(alphabet, array)
    }

    @Test
    fun `Large String Serialization`() {
        val range = 'a'..'z'
        val loopedAlphabet = range.asSequence().joinToString("").repeat(10)
        val lengthStr = loopedAlphabet.length.toString()
        val array = createByteArray {
            writeBytes(lengthStr)
            writeByte(':'.toInt())
            writeBytes(loopedAlphabet)
        }
        assertEncodeDecode(loopedAlphabet, array)
    }

    @Test
    fun `List Serialization`() {
        val firstObj = 1
        val secondObj = "Hello"
        val thirdObj = BigDecimal(2.5)
        val list = listOf(firstObj, secondObj, thirdObj)
        val array = createByteArray {
            writeByte(ListType.range.start + list.size)
            writeByte(firstObj)
            writeByte(secondObj.length + StringType.range.start)
            writeBytes(secondObj)
            writeByte(BigNumberType.id)
            writeBytes(thirdObj.toString())
            writeByte(EndType.id)
        }
        assertEncodeDecode(list, array)
    }

    @Test
    fun `Large List Serialization`() {
        val firstObj = 1
        val secondObj = "Hello"
        val thirdObj = BigDecimal(2.5)
        val longList = generateSequence { listOf(firstObj, secondObj, thirdObj) }.take(100).flatten().toList()
        val array = createByteArray {
            writeByte(LargeListType.id)
            repeat(100) {
                writeByte(firstObj)
                writeByte(secondObj.length + StringType.range.start)
                writeBytes(secondObj)
                writeByte(BigNumberType.id)
                writeBytes(thirdObj.toString())
                writeByte(EndType.id)
            }
            writeByte(EndType.id)
        }
        assertEncodeDecode(longList, array)
    }

    @Test
    fun `List of Lists Serialization`() {
        val l1 = listOf(1, 2, 3)
        val l2 = listOf(4, 5, 6, 7)
        val list = listOf(l1, l2)
        val array = createByteArray {
            writeByte(ListType.range.start + list.size)
            writeByte(ListType.range.start + l1.size)
            l1.forEach { writeByte(it) }
            writeByte(ListType.range.start + l2.size)
            l2.forEach { writeByte(it) }
        }
        assertEncodeDecode(list, array)
    }

    @Test
    fun `Array To List Serialization`() {
        val firstObj = 1
        val secondObj = "Hello"
        val thirdObj = BigDecimal(2.5)
        val array = listOf(firstObj, secondObj, thirdObj).toTypedArray()

        val baos = ByteArrayOutputStream()
        RencodeOutputStream(baos).use {
            it.writeObject(array)
        }
        val actualByteArray = baos.toByteArray()
        val actualObject = RencodeInputStream(ByteArrayInputStream(actualByteArray)).use {
            it.readObject()
        }
        assertEquals(array.toList(), actualObject)
    }

    @Test
    fun `All Java Array types are serializable`() {
        val size = 5
        val out = ByteArrayOutputStream()
        val ros = RencodeOutputStream(out)
        ros.writeObject(ByteArray(size))
        ros.writeObject(CharArray(size))
        ros.writeObject(ShortArray(size))
        ros.writeObject(IntArray(size))
        ros.writeObject(LongArray(size))
        ros.writeObject(FloatArray(size))
        ros.writeObject(DoubleArray(size))
        ros.writeObject(BooleanArray(size))

        val ris = RencodeInputStream(ByteArrayInputStream(out.toByteArray()))
        assertEquals(generateSequence { 0.toByte() }.take(5).toList(), ris.readObject())
        assertEquals(generateSequence { 0.toByte() }.take(5).toList(), ris.readObject())
        assertEquals(generateSequence { 0.toShort() }.take(5).toList(), ris.readObject())
        assertEquals(generateSequence { 0 }.take(5).toList(), ris.readObject())
        assertEquals(generateSequence { 0.toLong() }.take(5).toList(), ris.readObject())
        assertEquals(generateSequence { 0.toFloat() }.take(5).toList(), ris.readObject())
        assertEquals(generateSequence { 0.toDouble() }.take(5).toList(), ris.readObject())
        assertEquals(generateSequence { false }.take(5).toList(), ris.readObject())
    }

    @Test
    fun `Map Serialization`() {
        val pair1 = 1 to '1'.toByte()
        val pair2 = 2 to '2'.toByte()
        val map = mapOf(pair1, pair2)
        val array = createByteArray {
            writeByte(MapType.range.start + map.size)
            writeByte(pair1.first)
            writeByte(ByteType.id)
            writeByte(pair1.second.toInt())
            writeByte(pair2.first)
            writeByte(ByteType.id)
            writeByte(pair2.second.toInt())
        }
        assertEncodeDecode(map, array)
    }

    @Test
    fun `Large Map Serialization`() {
        val value = 'c'.toByte()
        val longMap = (0 until 60).map { it.toByte() to value }.toMap()
        val array = createByteArray {
            writeByte(LargeMapType.id)
            repeat(60) {
                writeByte(ByteType.id)
                writeByte(it)
                writeByte(ByteType.id)
                writeByte(value.toInt())
            }
            writeByte(EndType.id)
        }
        assertEncodeDecode(longMap, array)
    }

    @Test
    fun `All registered tokens have unique IDs`() {
        SupportedType.values().forEach { outer ->
            SupportedType.values().filter { outer != it }.forEach { inner ->
                outer.readTokens.forEach { token ->
                    when (token) {
                        is IdType -> assertNull(inner.parseId(token.id), "$outer intersects with $inner on ${token.id}")
                        is RangeType -> token.range.forEach {
                            assertNull(inner.parseId(it), "$outer intersects with $inner on $it")
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `Unknown Type cannot be serialized`() {
        assertThrows(IOException::class.java) {
            RencodeOutputStream(ByteArrayOutputStream()).writeObject(0..1)
        }
    }

    @Test
    fun `Number supports all standard number types`() {
        val byte = 5.toByte()
        val short = 5.toShort()
        val int = 5
        val long = 5L
        val float = 5F
        val double = 5.0
        val bigDecimal = BigDecimal("5.55")
        val bigInteger = BigInteger("555")
        val baos = ByteArrayOutputStream()
        RencodeOutputStream(baos).use {
            it.writeNumber(byte)
            it.writeNumber(short)
            it.writeNumber(int)
            it.writeNumber(long)
            it.writeNumber(float)
            it.writeNumber(double)
            it.writeNumber(bigDecimal)
            it.writeNumber(bigInteger)
        }
        RencodeInputStream(ByteArrayInputStream(baos.toByteArray())).use {
            assertEquals(byte, it.readNumber())
            assertEquals(short, it.readNumber())
            assertEquals(int, it.readNumber())
            assertEquals(long, it.readNumber())
            assertEquals(float, it.readNumber())
            assertEquals(double, it.readNumber())
            assertEquals(bigDecimal, it.readNumber())
            assertEquals(bigInteger, it.readNumber())
        }
    }

    @Test
    fun `Test Type Writers and Readers`() {
        val byte = 5.toByte()
        val short = 5.toShort()
        val int = 5
        val long = 5L
        val float = 5F
        val double = 5.0
        val bool = true
        val str = "5"
        val list = listOf(5, "5")
        val map = mapOf(5 to 5)
        val number = BigDecimal("5.55")
        val baos = ByteArrayOutputStream()
        RencodeOutputStream(baos).use {
            it.writeByte(byte)
            it.writeShort(short)
            it.writeInt(int)
            it.writeLong(long)
            it.writeFloat(float)
            it.writeDouble(double)
            it.writeBoolean(bool)
            it.writeString(str)
            it.writeList(list)
            it.writeMap(map)
            it.writeNumber(number)
        }
        RencodeInputStream(ByteArrayInputStream(baos.toByteArray())).use {
            assertEquals(byte, it.readByte())
            assertEquals(short, it.readShort())
            assertEquals(int, it.readInt())
            assertEquals(long, it.readLong())
            assertEquals(float, it.readFloat())
            assertEquals(double, it.readDouble())
            assertEquals(bool, it.readBoolean())
            assertEquals(str, it.readString())
            assertEquals(list, it.readList())
            assertEquals(map, it.readMap())
            assertEquals(number, it.readNumber())
        }
    }
}
