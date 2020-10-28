package top.androider.util

import androidx.collection.LruCache
import top.androider.util.constant.CacheConstants
import kotlin.jvm.JvmOverloads
import top.androider.util.CacheMemoryUtils
import java.util.HashMap

/**
 * <pre>
 * author: Blankj
 * blog  : http://blankj.com
 * time  : 2017/05/24
 * desc  : utils about memory cache
</pre> *
 */
class CacheMemoryUtils private constructor(
    private val mCacheKey: String,
    private val mMemoryCache: LruCache<String, CacheValue>
) : CacheConstants {
    override fun toString(): String {
        return mCacheKey + "@" + Integer.toHexString(hashCode())
    }
    /**
     * Put bytes in cache.
     *
     * @param key      The key of cache.
     * @param value    The value of cache.
     * @param saveTime The save time of cache, in seconds.
     */
    @JvmOverloads
    fun put(key: String, value: Any?, saveTime: Int = -1) {
        if (value == null) return
        val dueTime = if (saveTime < 0) -1 else System.currentTimeMillis() + saveTime * 1000
        mMemoryCache.put(key, CacheValue(dueTime, value))
    }

    /**
     * Return the value in cache.
     *
     * @param key          The key of cache.
     * @param defaultValue The default value if the cache doesn't exist.
     * @param <T>          The value type.
     * @return the value if cache exists or defaultValue otherwise
    </T> */
    operator fun <T> get(key: String, defaultValue: T? = null): T? {
        val `val` = mMemoryCache[key] ?: return defaultValue
        if (`val`.dueTime == -1L || `val`.dueTime >= System.currentTimeMillis()) {
            return `val`.value as T?
        }
        mMemoryCache.remove(key)
        return defaultValue
    }

    /**
     * Return the count of cache.
     *
     * @return the count of cache
     */
    val cacheCount: Int
        get() = mMemoryCache.size()

    /**
     * Remove the cache by key.
     *
     * @param key The key of cache.
     * @return `true`: success<br></br>`false`: fail
     */
    fun remove(key: String): Any? {
        val remove = mMemoryCache.remove(key) ?: return null
        return remove.value
    }

    /**
     * Clear all of the cache.
     */
    fun clear() {
        mMemoryCache.evictAll()
    }

    private class CacheValue internal constructor(var dueTime: Long, var value: Any)
    companion object {
        private const val DEFAULT_MAX_COUNT = 256
        private val CACHE_MAP: MutableMap<String, CacheMemoryUtils> = HashMap()

        /**
         * Return the single [CacheMemoryUtils] instance.
         *
         * @param maxCount The max count of cache.
         * @return the single [CacheMemoryUtils] instance
         */
        fun getInstance(maxCount: Int = DEFAULT_MAX_COUNT): CacheMemoryUtils {
            return getInstance(maxCount.toString(), maxCount)
        }

        /**
         * Return the single [CacheMemoryUtils] instance.
         *
         * @param cacheKey The key of cache.
         * @param maxCount The max count of cache.
         * @return the single [CacheMemoryUtils] instance
         */
        fun getInstance(cacheKey: String, maxCount: Int): CacheMemoryUtils {
            var cache = CACHE_MAP[cacheKey]
            if (cache == null) {
                synchronized(CacheMemoryUtils::class.java) {
                    cache = CACHE_MAP[cacheKey]
                    if (cache == null) {
                        cache = CacheMemoryUtils(cacheKey, LruCache(maxCount))
                        CACHE_MAP[cacheKey] = cache!!
                    }
                }
            }
            return cache!!
        }
    }
}