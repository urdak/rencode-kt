package net.ickis.rencode.types

import net.ickis.rencode.types.SupportedType.Companion
import java.math.BigDecimal
import java.math.BigInteger
import java.lang.reflect.Array as ReflectArray

/**
 * Aggregates all supported rencode types. Provides helped methods to determine the type of an
 * object based on its token id or its type.
 * @see [Companion]
 */
enum class SupportedType(
        internal vararg val readTokens: TokenType<*>
) {
    NULL(NullType) {
        override fun parseValue(value: Any?) = if (value == null) NullType else null
    },
    BOOLEAN(TrueType, FalseType) {
        override fun parseValue(value: Any?) = when (value) {
            true -> TrueType
            false -> FalseType
            else -> null
        }
    },
    BYTE(ByteType) {
        override fun parseValue(value: Any?) = (value as? Byte)?.let { ByteType }
    },
    CHAR(/* write-only type */) {
        override fun parseValue(value: Any?) = (value as? Char)?.let { CharType }
    },
    SHORT(ShortType) {
        override fun parseValue(value: Any?) = (value as? Short)?.let { ShortType }
    },
    LONG(LongType) {
        override fun parseValue(value: Any?) = (value as? Long)?.let { LongType }
    },
    FLOAT(FloatType) {
        override fun parseValue(value: Any?) = (value as? Float)?.let { FloatType }
    },
    DOUBLE(DoubleType) {
        override fun parseValue(value: Any?) = (value as? Double)?.let { DoubleType }
    },
    INT(PositiveIntType, NegativeIntType, IntType) {
        override fun parseValue(value: Any?): TokenType<Int>? {
            if (value !is Int) return null
            return when (value) {
                in PositiveIntType.range -> PositiveIntType
                in NegativeIntType.values -> NegativeIntType
                else -> IntType
            }
        }
    },
    NUMBER(BigNumberType, *BYTE.readTokens, *SHORT.readTokens, *LONG.readTokens, *FLOAT.readTokens,
           *DOUBLE.readTokens, *INT.readTokens) {
        override fun parseValue(value: Any?) = when (value) {
            is BigDecimal, is BigInteger -> BigNumberType
            is Byte -> BYTE.parseValue(value)
            is Short -> SHORT.parseValue(value)
            is Long -> LONG.parseValue(value)
            is Float -> FLOAT.parseValue(value)
            is Double -> DOUBLE.parseValue(value)
            is Int -> INT.parseValue(value)
            else -> null
        }
    },
    STRING(StringType, LargeStringType) {
        override fun parseValue(value: Any?): TokenType<String>? {
            if (value !is String) return null
            return if (value.length < StringType.length) StringType else LargeStringType
        }
    },
    COLLECTION(ListType, LargeListType) {
        override fun parseValue(value: Any?) = when (value) {
            is Collection<*> -> if (value.size < ListType.length) ListType else LargeListType
            is Array<*>,
            is ByteArray,
            is CharArray,
            is ShortArray,
            is IntArray,
            is LongArray,
            is FloatArray,
            is DoubleArray,
            is BooleanArray -> if (ReflectArray.getLength(value) < ArrayType.length)
                ArrayType else LargeArrayType
            else -> null
        }
    },
    MAP(MapType, LargeMapType) {
        override fun parseValue(value: Any?) = when (value) {
            is Map<*, *> -> if (value.size < MapType.length) MapType else LargeMapType
            else -> null
        }
    };

    abstract fun parseValue(value: Any?): TokenType<*>?

    fun parseId(id: Int) = readTokens.firstOrNull {
        when (it) {
            is IdType -> it.id == id
            is RangeType -> id in it.range
        }
    }

    companion object {
        /**
         * Retrieves the type that can be used to write the [value] or null if [value] class is not
         * supported.
         */
        fun findByValue(value: Any?) = firstOrNull { it.parseValue(value) }

        /**
         * Retrieves the type that has the specified [id] or null if no type matches the [id].
         */
        fun findById(id: Int) = firstOrNull { it.parseId(id) }

        /**
         * Retrieves the type that returns a non-null value for [function].
         */
        private inline fun firstOrNull(function: (SupportedType) -> TokenType<*>?): TokenType<*>? {
            for (value in values()) {
                val token = function(value)
                if (token != null) return token
            }
            return null
        }
    }
}
