package com.unimp.utils

import com.facebook.react.bridge.*
import org.json.JSONObject

/**
 * React Native Bridge 扩展函数
 * 提供Kotlin友好的桥接数据处理
 */

/**
 * 将ReadableMap转换为Kotlin Map
 */
fun ReadableMap.toKotlinMap(): Map<String, Any?> {
    val map = mutableMapOf<String, Any?>()
    val iterator = keySetIterator()
    
    while (iterator.hasNextKey()) {
        val key = iterator.nextKey()
        when (getType(key)) {
            ReadableType.Null -> map[key] = null
            ReadableType.Boolean -> map[key] = getBoolean(key)
            ReadableType.Number -> map[key] = getDouble(key)
            ReadableType.String -> map[key] = getString(key)
            ReadableType.Map -> map[key] = getMap(key)?.toKotlinMap()
            ReadableType.Array -> map[key] = getArray(key)?.toKotlinList()
            else -> map[key] = null
        }
    }
    
    return map
}

/**
 * 将ReadableArray转换为Kotlin List
 */
fun ReadableArray.toKotlinList(): List<Any?> {
    val list = mutableListOf<Any?>()
    
    for (i in 0 until size()) {
        when (getType(i)) {
            ReadableType.Null -> list.add(null)
            ReadableType.Boolean -> list.add(getBoolean(i))
            ReadableType.Number -> list.add(getDouble(i))
            ReadableType.String -> list.add(getString(i))
            ReadableType.Map -> list.add(getMap(i)?.toKotlinMap())
            ReadableType.Array -> list.add(getArray(i)?.toKotlinList())
            else -> list.add(null)
        }
    }
    
    return list
}

/**
 * 将Kotlin Map转换为WritableMap
 */
fun Map<String, Any?>.toWritableMap(): WritableMap {
    val map = Arguments.createMap()
    
    forEach { (key, value) ->
        when (value) {
            null -> map.putNull(key)
            is Boolean -> map.putBoolean(key, value)
            is Int -> map.putInt(key, value)
            is Double -> map.putDouble(key, value)
            is String -> map.putString(key, value)
            is Map<*, *> -> map.putMap(key, (value as Map<String, Any?>).toWritableMap())
            is List<*> -> map.putArray(key, value.toWritableArray())
            else -> map.putNull(key)
        }
    }
    
    return map
}

/**
 * 将Kotlin List转换为WritableArray
 */
fun List<Any?>.toWritableArray(): WritableArray {
    val array = Arguments.createArray()
    
    forEach { item ->
        when (item) {
            null -> array.pushNull()
            is Boolean -> array.pushBoolean(item)
            is Int -> array.pushInt(item)
            is Double -> array.pushDouble(item)
            is String -> array.pushString(item)
            is Map<*, *> -> array.pushMap((item as Map<String, Any?>).toWritableMap())
            is List<*> -> array.pushArray(item.toWritableArray())
            else -> array.pushNull()
        }
    }
    
    return array
}

/**
 * 安全地获取ReadableMap中的值
 */
inline fun <T> ReadableMap.getSafe(key: String, defaultValue: T, getter: ReadableMap.(String) -> T): T {
    return if (hasKey(key) && !isNull(key)) {
        getter(key)
    } else {
        defaultValue
    }
}

/**
 * 获取字符串值，如果为null或不存在则返回默认值
 */
fun ReadableMap.getStringSafe(key: String, defaultValue: String = ""): String {
    return getSafe(key, defaultValue) { getString(it) }
}

/**
 * 获取布尔值，如果为null或不存在则返回默认值
 */
fun ReadableMap.getBooleanSafe(key: String, defaultValue: Boolean = false): Boolean {
    return getSafe(key, defaultValue) { getBoolean(it) }
}

/**
 * 获取整数值，如果为null或不存在则返回默认值
 */
fun ReadableMap.getIntSafe(key: String, defaultValue: Int = 0): Int {
    return getSafe(key, defaultValue) { getInt(it) }
}

/**
 * 获取双精度值，如果为null或不存在则返回默认值
 */
fun ReadableMap.getDoubleSafe(key: String, defaultValue: Double = 0.0): Double {
    return getSafe(key, defaultValue) { getDouble(it) }
}

/**
 * 获取ReadableMap，如果为null或不存在则返回空的WritableMap
 */
fun ReadableMap.getMapSafe(key: String): ReadableMap? {
    return if (hasKey(key) && !isNull(key) && getType(key) == ReadableType.Map) {
        getMap(key)
    } else {
        null
    }
}

/**
 * 获取ReadableArray，如果为null或不存在则返回空的WritableArray
 */
fun ReadableMap.getArraySafe(key: String): ReadableArray? {
    return if (hasKey(key) && !isNull(key) && getType(key) == ReadableType.Array) {
        getArray(key)
    } else {
        null
    }
}