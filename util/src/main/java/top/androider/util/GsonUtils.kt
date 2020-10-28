package top.androider.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.Reader
import java.lang.reflect.Type
import java.util.concurrent.ConcurrentHashMap


private const val KEY_DEFAULT = "defaultGson"
private const val KEY_DELEGATE = "delegateGson"
private const val KEY_LOG_UTILS = "logUtilsGson"

/**
 * Serializes an object into json.
 *
 * @param gson   The gson.
 * @param object The object to serialize.
 * @return object serialized into json.
 */
fun Any?.toJson(gson: Gson = createGson(), typeOfSrc: Type? = null): String {
    return if (null == typeOfSrc){
        gson.toJson(this)
    } else{
        gson.toJson(this, typeOfSrc)
    }
}


fun <T> String?.fromJson(gson: Gson = createGson(), type: Class<T>) = gson.fromJson(this, type)
fun <T> String?.fromJson(gson: Gson = createGson(), type: Type) :T? = if(this == null) null else gson.fromJson(
    this,
    type
)
fun <T> Reader.fromJson(gson: Gson = createGson(), type: Class<T>) = gson.fromJson(this, type)
fun <T> Reader.fromJson(gson: Gson = createGson(), type: Type) :T? =  gson.fromJson(this, type)


/**
 * Return the type of [List] with the `type`.
 *
 * @param type The type.
 * @return the type of [List] with the `type`
 */
fun Type.getListType(): Type {
    return TypeToken.getParameterized(MutableList::class.java, this).type
}

/**
 * Return the type of [Set] with the `type`.
 *
 * @param type The type.
 * @return the type of [Set] with the `type`
 */
fun Type.getSetType(): Type {
    return TypeToken.getParameterized(MutableSet::class.java, this).type
}

/**
 * Return the type of map with the `keyType` and `valueType`.
 *
 * @param valueType The type of value.
 * @return the type of map with the `keyType` and `valueType`
 */
fun Type.getMapType(valueType: Type): Type {
    return TypeToken.getParameterized(MutableMap::class.java, this, valueType).type
}

/**
 * Return the type of array with the `type`.
 *
 * @param type The type.
 * @return the type of map with the `type`
 */
fun Type.getArrayType(): Type {
    return TypeToken.getArray(this).type
}

/**
 * Return the type of `rawType` with the `typeArguments`.
 *
 * @param rawType       The raw type.
 * @param typeArguments The type of arguments.
 * @return the type of map with the `type`
 */
fun Type.getType(vararg typeArguments: Type): Type {
    return TypeToken.getParameterized(this, *typeArguments).type
}


private val GSONS: ConcurrentHashMap<String, Gson> = ConcurrentHashMap()
fun getGson4LogUtils(): Gson? {
    return GSONS[KEY_LOG_UTILS]?:GsonBuilder().setPrettyPrinting().serializeNulls().create().apply {
        GSONS.put(KEY_LOG_UTILS,this)
    }
}
private fun createGson(): Gson {
    return GsonBuilder().serializeNulls().disableHtmlEscaping().create()
}