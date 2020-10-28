package top.androider.util

/**
 * <pre>
 * author: Blankj
 * blog  : http://blankj.com
 * time  : 2019/01/04
 * desc  : utils about memory cache
</pre> *
 */
object CacheMemoryStaticUtils {
    private var sDefaultCacheMemoryUtils: CacheMemoryUtils? = null

    /**
     * Return the count of cache.
     *
     * @return the count of cache
     */
    val cacheCount: Int
        get() = getCacheCount(defaultCacheMemoryUtils)

    /**
     * Put bytes in cache.
     *
     * @param key              The key of cache.
     * @param value            The value of cache.
     * @param saveTime         The save time of cache, in seconds.
     * @param cacheMemoryUtils The instance of [CacheMemoryUtils].
     */
    @JvmOverloads
    fun put(
        key: String,
        value: Any?,
        saveTime: Int = -1,
        cacheMemoryUtils: CacheMemoryUtils = defaultCacheMemoryUtils
    ) {
        cacheMemoryUtils.put(key, value, saveTime)
    }

    /**
     * Return the value in cache.
     *
     * @param key              The key of cache.
     * @param cacheMemoryUtils The instance of [CacheMemoryUtils].
     * @param <T>              The value type.
     * @return the value if cache exists or null otherwise
    </T> */
    operator fun <T> get(key: String, cacheMemoryUtils: CacheMemoryUtils = defaultCacheMemoryUtils): T? {
        return cacheMemoryUtils[key]
    }

    /**
     * Return the value in cache.
     *
     * @param key              The key of cache.
     * @param defaultValue     The default value if the cache doesn't exist.
     * @param cacheMemoryUtils The instance of [CacheMemoryUtils].
     * @param <T>              The value type.
     * @return the value if cache exists or defaultValue otherwise
    </T> */
    operator fun <T> get(
        key: String,
        defaultValue: T? = null,
        cacheMemoryUtils: CacheMemoryUtils = defaultCacheMemoryUtils
    ): T? {
        return cacheMemoryUtils[key, defaultValue]
    }

    /**
     * Return the count of cache.
     *
     * @param cacheMemoryUtils The instance of [CacheMemoryUtils].
     * @return the count of cache
     */
    fun getCacheCount(cacheMemoryUtils: CacheMemoryUtils): Int {
        return cacheMemoryUtils.cacheCount
    }
    /**
     * Remove the cache by key.
     *
     * @param key              The key of cache.
     * @param cacheMemoryUtils The instance of [CacheMemoryUtils].
     * @return `true`: success<br></br>`false`: fail
     */
    /**
     * Remove the cache by key.
     *
     * @param key The key of cache.
     * @return `true`: success<br></br>`false`: fail
     */
    @JvmOverloads
    fun remove(key: String, cacheMemoryUtils: CacheMemoryUtils = defaultCacheMemoryUtils): Any? {
        return cacheMemoryUtils.remove(key)
    }
    /**
     * Clear all of the cache.
     *
     * @param cacheMemoryUtils The instance of [CacheMemoryUtils].
     */
    @JvmOverloads
    fun clear(cacheMemoryUtils: CacheMemoryUtils = defaultCacheMemoryUtils) {
        cacheMemoryUtils.clear()
    }

    /**
     * Set the default instance of [CacheMemoryUtils].
     *
     * @param cacheMemoryUtils The default instance of [CacheMemoryUtils].
     */
    private var defaultCacheMemoryUtils: CacheMemoryUtils
        private get() = sDefaultCacheMemoryUtils?: CacheMemoryUtils.getInstance()
        set(cacheMemoryUtils) {
            sDefaultCacheMemoryUtils = cacheMemoryUtils
        }
}