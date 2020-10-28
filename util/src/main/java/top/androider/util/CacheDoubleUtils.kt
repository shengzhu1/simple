package top.androider.util

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Parcelable
import org.json.JSONArray
import org.json.JSONObject
import top.androider.util.constant.CacheConstants
import java.io.Serializable
import java.util.*

/**
 * <pre>
 * author: Blankj
 * blog  : http://blankj.com
 * time  : 2018/06/13
 * desc  : utils about double cache
</pre> *
 */
class CacheDoubleUtils private constructor(
    private val mCacheMemoryUtils: CacheMemoryUtils,
    private val mCacheDiskUtils: CacheDiskUtils
) : CacheConstants {
    /**
     * Put bytes in cache.
     *
     * @param key      The key of cache.
     * @param value    The value of cache.
     * @param saveTime The save time of cache, in seconds.
     */
    @JvmOverloads
    fun put(key: String, value: ByteArray?, saveTime: Int = -1) {
        mCacheMemoryUtils.put(key, value, saveTime)
        mCacheDiskUtils.put(key, value, saveTime)
    }

    /**
     * Return the bytes in cache.
     *
     * @param key          The key of cache.
     * @param defaultValue The default value if the cache doesn't exist.
     * @return the bytes if cache exists or defaultValue otherwise
     */
    fun getBytes(key: String, defaultValue: ByteArray? = null): ByteArray? {
        val obj = mCacheMemoryUtils.get<ByteArray>(key)
        return obj ?: mCacheDiskUtils.getBytes(key, defaultValue)
    }
    /**
     * Put string value in cache.
     *
     * @param key      The key of cache.
     * @param value    The value of cache.
     * @param saveTime The save time of cache, in seconds.
     */
    @JvmOverloads
    fun put(key: String, value: String?, saveTime: Int = -1) {
        mCacheMemoryUtils.put(key, value, saveTime)
        mCacheDiskUtils.put(key, value, saveTime)
    }

    /**
     * Return the string value in cache.
     *
     * @param key          The key of cache.
     * @param defaultValue The default value if the cache doesn't exist.
     * @return the string value if cache exists or defaultValue otherwise
     */
    fun getString(key: String, defaultValue: String? = null): String? {
        val obj = mCacheMemoryUtils.get<String>(key)
        return obj ?: mCacheDiskUtils.getString(key, defaultValue)
    }
    /**
     * Put JSONObject in cache.
     *
     * @param key      The key of cache.
     * @param value    The value of cache.
     * @param saveTime The save time of cache, in seconds.
     */
    @JvmOverloads
    fun put(
        key: String,
        value: JSONObject?,
        saveTime: Int = -1
    ) {
        mCacheMemoryUtils.put(key, value, saveTime)
        mCacheDiskUtils.put(key, value, saveTime)
    }

    /**
     * Return the JSONObject in cache.
     *
     * @param key          The key of cache.
     * @param defaultValue The default value if the cache doesn't exist.
     * @return the JSONObject if cache exists or defaultValue otherwise
     */
    fun getJSONObject(key: String, defaultValue: JSONObject? = null): JSONObject? {
        val obj = mCacheMemoryUtils.get<JSONObject>(key)
        return obj ?: mCacheDiskUtils.getJSONObject(key, defaultValue)
    }

    /**
     * Put JSONArray in cache.
     *
     * @param key      The key of cache.
     * @param value    The value of cache.
     * @param saveTime The save time of cache, in seconds.
     */
    @JvmOverloads
    fun put(key: String, value: JSONArray?, saveTime: Int = -1) {
        mCacheMemoryUtils.put(key, value, saveTime)
        mCacheDiskUtils.put(key, value, saveTime)
    }

    /**
     * Return the JSONArray in cache.
     *
     * @param key          The key of cache.
     * @param defaultValue The default value if the cache doesn't exist.
     * @return the JSONArray if cache exists or defaultValue otherwise
     */
    fun getJSONArray(key: String, defaultValue: JSONArray? = null): JSONArray? {
        val obj = mCacheMemoryUtils.get<JSONArray>(key)
        return obj ?: mCacheDiskUtils.getJSONArray(key, defaultValue)
    }
    /**
     * Put bitmap in cache.
     *
     * @param key      The key of cache.
     * @param value    The value of cache.
     * @param saveTime The save time of cache, in seconds.
     */
    @JvmOverloads
    fun put(key: String, value: Bitmap?, saveTime: Int = -1) {
        mCacheMemoryUtils.put(key, value, saveTime)
        mCacheDiskUtils.put(key, value, saveTime)
    }
    /**
     * Return the bitmap in cache.
     *
     * @param key          The key of cache.
     * @param defaultValue The default value if the cache doesn't exist.
     * @return the bitmap if cache exists or defaultValue otherwise
     */
    fun getBitmap(key: String, defaultValue: Bitmap? = null): Bitmap? {
        val obj = mCacheMemoryUtils.get<Bitmap>(key)
        return obj ?: mCacheDiskUtils.getBitmap(key, defaultValue)
    }
    /**
     * Put drawable in cache.
     *
     * @param key      The key of cache.
     * @param value    The value of cache.
     * @param saveTime The save time of cache, in seconds.
     */
    @JvmOverloads
    fun put(key: String, value: Drawable?, saveTime: Int = -1) {
        mCacheMemoryUtils.put(key, value, saveTime)
        mCacheDiskUtils.put(key, value, saveTime)
    }

    /**
     * Return the drawable in cache.
     *
     * @param key          The key of cache.
     * @param defaultValue The default value if the cache doesn't exist.
     * @return the drawable if cache exists or defaultValue otherwise
     */
    fun getDrawable(key: String, defaultValue: Drawable? = null): Drawable? {
        val obj = mCacheMemoryUtils.get<Drawable>(key)
        return obj ?: mCacheDiskUtils.getDrawable(key, defaultValue)
    }
    /**
     * Put parcelable in cache.
     *
     * @param key      The key of cache.
     * @param value    The value of cache.
     * @param saveTime The save time of cache, in seconds.
     */
    @JvmOverloads
    fun put(key: String, value: Parcelable?, saveTime: Int = -1) {
        mCacheMemoryUtils.put(key, value, saveTime)
        mCacheDiskUtils.put(key, value, saveTime)
    }

    /**
     * Return the parcelable in cache.
     *
     * @param key          The key of cache.
     * @param creator      The creator.
     * @param defaultValue The default value if the cache doesn't exist.
     * @param <T>          The value type.
     * @return the parcelable if cache exists or defaultValue otherwise
    </T> */
    fun <T> getParcelable(
        key: String,
        creator: Parcelable.Creator<T>,
        defaultValue: T? = null
    ): T? {
        val value: T? = mCacheMemoryUtils[key]
        return value ?: mCacheDiskUtils.getParcelable(key, creator, defaultValue)
    }
    /**
     * Put serializable in cache.
     *
     * @param key      The key of cache.
     * @param value    The value of cache.
     * @param saveTime The save time of cache, in seconds.
     */
    @JvmOverloads
    fun put(key: String, value: Serializable?, saveTime: Int = -1) {
        mCacheMemoryUtils.put(key, value, saveTime)
        mCacheDiskUtils.put(key, value, saveTime)
    }

    /**
     * Return the serializable in cache.
     *
     * @param key          The key of cache.
     * @param defaultValue The default value if the cache doesn't exist.
     * @return the bitmap if cache exists or defaultValue otherwise
     */
    fun getSerializable(key: String, defaultValue: Any? = null): Any? {
        val obj = mCacheMemoryUtils.get<Any>(key)
        return obj ?: mCacheDiskUtils.getSerializable(key, defaultValue)
    }

    /**
     * Return the count of cache in memory.
     *
     * @return the count of cache in memory.
     */
    val cacheMemoryCount: Int
        get() = mCacheMemoryUtils.cacheCount

    /**
     * Remove the cache by key.
     *
     * @param key The key of cache.
     */
    fun remove(key: String) {
        mCacheMemoryUtils.remove(key)
        mCacheDiskUtils.remove(key)
    }

    /**
     * Clear all of the cache.
     */
    fun clear() {
        mCacheMemoryUtils.clear()
        mCacheDiskUtils.clear()
    }

    companion object {
        private val CACHE_MAP: MutableMap<String, CacheDoubleUtils> = HashMap()

        /**
         * Return the single [CacheDoubleUtils] instance.
         *
         * @param cacheMemoryUtils The instance of [CacheMemoryUtils].
         * @param cacheDiskUtils   The instance of [CacheDiskUtils].
         * @return the single [CacheDoubleUtils] instance
         */
        fun getInstance(
            cacheMemoryUtils: CacheMemoryUtils = CacheMemoryUtils.getInstance(),
            cacheDiskUtils: CacheDiskUtils = CacheDiskUtils.instance!!
        ): CacheDoubleUtils {
            val cacheKey = cacheDiskUtils.toString() + "_" + cacheMemoryUtils.toString()
            var cache = CACHE_MAP[cacheKey]
            if (cache == null) {
                synchronized(CacheDoubleUtils::class.java) {
                    cache = CACHE_MAP[cacheKey]
                    if (cache == null) {
                        cache = CacheDoubleUtils(cacheMemoryUtils, cacheDiskUtils)
                        CACHE_MAP[cacheKey] = cache!!
                    }
                }
            }
            return cache!!
        }
    }


    /**
     * Return the size of cache in disk.
     *
     * @return the size of cache in disk
     */
    fun getCacheDiskSize(): Long {
        return mCacheDiskUtils.cacheDiskSize
    }

    /**
     * Return the count of cache in disk.
     *
     * @return the count of cache in disk
     */
    fun getCacheDiskCount(): Int {
        return mCacheDiskUtils.cacheDiskCount
    }
}