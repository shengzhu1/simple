package top.androider.util

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Parcelable
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import top.androider.util.Utils.Companion.app
import top.androider.util.constant.CacheConstants
import java.io.File
import java.io.Serializable
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * <pre>
 * author: Blankj
 * blog  : http://blankj.com
 * time  : 2017/05/24
 * desc  : utils about disk cache
</pre> *
 */
class CacheDiskUtils private constructor(
    private val mCacheKey: String,
    private val mCacheDir: File,
    private val mMaxSize: Long,
    private val mMaxCount: Int
) : CacheConstants {
    private var mDiskCacheManager: DiskCacheManager? = null
    private val diskCacheManager: DiskCacheManager?
        private get() {
            if (mCacheDir.exists()) {
                if (mDiskCacheManager == null) {
                    mDiskCacheManager = DiskCacheManager(mCacheDir, mMaxSize, mMaxCount)
                }
            } else {
                if (mCacheDir.mkdirs()) {
                    mDiskCacheManager = DiskCacheManager(mCacheDir, mMaxSize, mMaxCount)
                } else {
                    Log.e("CacheDiskUtils", "can't make dirs in " + mCacheDir.absolutePath)
                }
            }
            return mDiskCacheManager
        }

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
    ///////////////////////////////////////////////////////////////////////////
    // about bytes
    ///////////////////////////////////////////////////////////////////////////
    /**
     * Put bytes in cache.
     *
     * @param key   The key of cache.
     * @param value The value of cache.
     */
    @JvmOverloads
    fun put(key: String, value: ByteArray, saveTime: Int = -1) {
        realPutBytes(TYPE_BYTE + key, value, saveTime)
    }

    private fun realPutBytes(key: String, value: ByteArray?, saveTime: Int) {
        var value = value ?: return
        diskCacheManager?.apply {
            if (saveTime >= 0) value = DiskCacheHelper.newByteArrayWithTime(saveTime, value)
            val file = this.getFileBeforePut(key)
            FileIOUtils.writeFileFromBytesByChannel(file, value,true)
            this.updateModify(file)
            this.put(file)
        }
    }

    /**
     * Return the bytes in cache.
     *
     * @param key The key of cache.
     * @return the bytes if cache exists or null otherwise
     */
    fun getBytes(key: String): ByteArray? {
        return getBytes(key, null)
    }

    /**
     * Return the bytes in cache.
     *
     * @param key          The key of cache.
     * @param defaultValue The default value if the cache doesn't exist.
     * @return the bytes if cache exists or defaultValue otherwise
     */
    fun getBytes(key: String, defaultValue: ByteArray?): ByteArray? {
        return realGetBytes(TYPE_BYTE + key, defaultValue)
    }

    private fun realGetBytes(key: String, defaultValue: ByteArray? = null): ByteArray? {
        val diskCacheManager = diskCacheManager ?: return defaultValue
        val file = diskCacheManager.getFileIfExists(key) ?: return defaultValue
        val data: ByteArray? = FileIOUtils.readFile2BytesByChannel(file)
        if (DiskCacheHelper.isDue(data)) {
            diskCacheManager.removeByKey(key)
            return defaultValue
        }
        diskCacheManager.updateModify(file)
        return DiskCacheHelper.getDataWithoutDueTime(data)
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
        realPutBytes(TYPE_STRING + key, ConvertUtils.string2Bytes(value), saveTime)
    }

    /**
     * Return the string value in cache.
     *
     * @param key The key of cache.
     * @return the string value if cache exists or null otherwise
     */
    fun getString(key: String): String? {
        return getString(key, null)
    }

    /**
     * Return the string value in cache.
     *
     * @param key          The key of cache.
     * @param defaultValue The default value if the cache doesn't exist.
     * @return the string value if cache exists or defaultValue otherwise
     */
    fun getString(key: String, defaultValue: String?): String? {
        val bytes = realGetBytes(TYPE_STRING + key) ?: return defaultValue
        return ConvertUtils.bytes2String(bytes)
    }
    /**
     * Put JSONObject in cache.
     *
     * @param key      The key of cache.
     * @param value    The value of cache.
     * @param saveTime The save time of cache, in seconds.
     */
    ///////////////////////////////////////////////////////////////////////////
    // about JSONObject
    ///////////////////////////////////////////////////////////////////////////
    /**
     * Put JSONObject in cache.
     *
     * @param key   The key of cache.
     * @param value The value of cache.
     */
    @JvmOverloads
    fun put(
        key: String,
        value: JSONObject?,
        saveTime: Int = -1
    ) {
        realPutBytes(TYPE_JSON_OBJECT + key, ConvertUtils.jsonObject2Bytes(value), saveTime)
    }

    /**
     * Return the JSONObject in cache.
     *
     * @param key The key of cache.
     * @return the JSONObject if cache exists or null otherwise
     */
    fun getJSONObject(key: String): JSONObject? {
        return getJSONObject(key, null)
    }

    /**
     * Return the JSONObject in cache.
     *
     * @param key          The key of cache.
     * @param defaultValue The default value if the cache doesn't exist.
     * @return the JSONObject if cache exists or defaultValue otherwise
     */
    fun getJSONObject(key: String, defaultValue: JSONObject?): JSONObject? {
        val bytes = realGetBytes(TYPE_JSON_OBJECT + key) ?: return defaultValue
        return ConvertUtils.bytes2JSONObject(bytes)
    }
    /**
     * Put JSONArray in cache.
     *
     * @param key      The key of cache.
     * @param value    The value of cache.
     * @param saveTime The save time of cache, in seconds.
     */
    ///////////////////////////////////////////////////////////////////////////
    // about JSONArray
    ///////////////////////////////////////////////////////////////////////////
    /**
     * Put JSONArray in cache.
     *
     * @param key   The key of cache.
     * @param value The value of cache.
     */
    @JvmOverloads
    fun put(key: String, value: JSONArray?, saveTime: Int = -1) {
        realPutBytes(TYPE_JSON_ARRAY + key, ConvertUtils.jsonArray2Bytes(value), saveTime)
    }

    /**
     * Return the JSONArray in cache.
     *
     * @param key The key of cache.
     * @return the JSONArray if cache exists or null otherwise
     */
    fun getJSONArray(key: String): JSONArray? {
        return getJSONArray(key, null)
    }

    /**
     * Return the JSONArray in cache.
     *
     * @param key          The key of cache.
     * @param defaultValue The default value if the cache doesn't exist.
     * @return the JSONArray if cache exists or defaultValue otherwise
     */
    fun getJSONArray(key: String, defaultValue: JSONArray?): JSONArray? {
        val bytes = realGetBytes(TYPE_JSON_ARRAY + key) ?: return defaultValue
        return ConvertUtils.bytes2JSONArray(bytes)
    }
    /**
     * Put bitmap in cache.
     *
     * @param key      The key of cache.
     * @param value    The value of cache.
     * @param saveTime The save time of cache, in seconds.
     */
    ///////////////////////////////////////////////////////////////////////////
    // about Bitmap
    ///////////////////////////////////////////////////////////////////////////
    /**
     * Put bitmap in cache.
     *
     * @param key   The key of cache.
     * @param value The value of cache.
     */
    @JvmOverloads
    fun put(key: String, value: Bitmap?, saveTime: Int = -1) {
        realPutBytes(TYPE_BITMAP + key, bitmap2Bytes(value), saveTime)
    }

    /**
     * Return the bitmap in cache.
     *
     * @param key The key of cache.
     * @return the bitmap if cache exists or null otherwise
     */
    fun getBitmap(key: String): Bitmap? {
        return getBitmap(key, null)
    }

    /**
     * Return the bitmap in cache.
     *
     * @param key          The key of cache.
     * @param defaultValue The default value if the cache doesn't exist.
     * @return the bitmap if cache exists or defaultValue otherwise
     */
    fun getBitmap(key: String, defaultValue: Bitmap?): Bitmap? {
        val bytes = realGetBytes(TYPE_BITMAP + key) ?: return defaultValue
        return bytes2Bitmap(bytes)
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
        realPutBytes(TYPE_DRAWABLE + key, drawable2Bytes(value), saveTime)
    }

    /**
     * Return the drawable in cache.
     *
     * @param key The key of cache.
     * @return the drawable if cache exists or null otherwise
     */
    fun getDrawable(key: String): Drawable? {
        return getDrawable(key, null)
    }

    /**
     * Return the drawable in cache.
     *
     * @param key          The key of cache.
     * @param defaultValue The default value if the cache doesn't exist.
     * @return the drawable if cache exists or defaultValue otherwise
     */
    fun getDrawable(key: String, defaultValue: Drawable?): Drawable? {
        val bytes = realGetBytes(TYPE_DRAWABLE + key) ?: return defaultValue
        return bytes2Drawable(bytes)
    }
    /**
     * Put parcelable in cache.
     *
     * @param key      The key of cache.
     * @param value    The value of cache.
     * @param saveTime The save time of cache, in seconds.
     */
    ///////////////////////////////////////////////////////////////////////////
    // about Parcelable
    ///////////////////////////////////////////////////////////////////////////
    /**
     * Put parcelable in cache.
     *
     * @param key   The key of cache.
     * @param value The value of cache.
     */
    @JvmOverloads
    fun put(key: String, value: Parcelable?, saveTime: Int = -1) {
        realPutBytes(TYPE_PARCELABLE + key, ConvertUtils.parcelable2Bytes(value), saveTime)
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
        val bytes = realGetBytes(TYPE_PARCELABLE + key) ?: return defaultValue
        return  ConvertUtils.bytes2Parcelable(bytes, creator)
    }
    /**
     * Put serializable in cache.
     *
     * @param key      The key of cache.
     * @param value    The value of cache.
     * @param saveTime The save time of cache, in seconds.
     */
    ///////////////////////////////////////////////////////////////////////////
    // about Serializable
    ///////////////////////////////////////////////////////////////////////////
    /**
     * Put serializable in cache.
     *
     * @param key   The key of cache.
     * @param value The value of cache.
     */
    @JvmOverloads
    fun put(key: String, value: Serializable?, saveTime: Int = -1) {
        realPutBytes(TYPE_SERIALIZABLE + key, ConvertUtils.serializable2Bytes(value), saveTime)
    }

    /**
     * Return the serializable in cache.
     *
     * @param key The key of cache.
     * @return the bitmap if cache exists or null otherwise
     */
    fun getSerializable(key: String): Any? {
        return getSerializable(key, null)
    }

    /**
     * Return the serializable in cache.
     *
     * @param key          The key of cache.
     * @param defaultValue The default value if the cache doesn't exist.
     * @return the bitmap if cache exists or defaultValue otherwise
     */
    fun getSerializable(key: String, defaultValue: Any?): Any? {
        val bytes = realGetBytes(TYPE_SERIALIZABLE + key) ?: return defaultValue
        return ConvertUtils.bytes2Object(bytes)
    }

    /**
 * Return the size of cache in disk.
 *
 * @return the size of cache in disk
 */
    val cacheDiskSize: Long
        get() {
            val diskCacheManager = diskCacheManager ?: return 0
            return diskCacheManager.getCacheSize()
        }


    /**
 * Return the count of cache in disk.
 *
 * @return the count of cache in disk
 *//**
     * Return the count of cache.
     *
     * @return the count of cache
     */
    val cacheDiskCount: Int
        get() {
            return diskCacheManager?.run{ getCacheCount()}?:0
        }

    /**
     * Remove the cache by key.
     *
     * @param key The key of cache.
     * @return `true`: success<br></br>`false`: fail
     */
    fun remove(key: String): Boolean {
        return diskCacheManager?.let {
            it.removeByKey(TYPE_BYTE + key)
                    && it.removeByKey(TYPE_STRING + key)
                    && it.removeByKey(TYPE_JSON_OBJECT + key)
                    && it.removeByKey(TYPE_JSON_ARRAY + key)
                    && it.removeByKey(TYPE_BITMAP + key)
                    && it.removeByKey(TYPE_DRAWABLE + key)
                    && it.removeByKey(TYPE_PARCELABLE + key)
                    && it.removeByKey(TYPE_SERIALIZABLE + key)
        }?:true
    }

    /**
     * Clear all of the cache.
     *
     * @return `true`: success<br></br>`false`: fail
     */
    fun clear(): Boolean {
         return diskCacheManager?.let { clear() }?:true
    }

    internal class DiskCacheManager constructor(
        private val cacheDir: File,
        private val sizeLimit: Long,
        private val countLimit: Int
    ) {
        internal val cacheSize: AtomicLong
        internal val cacheCount: AtomicInteger
        private val lastUsageDates = Collections.synchronizedMap(HashMap<File?, Long?>())
        private val mThread: Thread
        internal fun getCacheSize(): Long {
            wait2InitOk()
            return cacheSize.get()
        }

        internal fun getCacheCount(): Int {
            wait2InitOk()
            return cacheCount.get()
        }

        internal fun getFileBeforePut(key: String): File {
            wait2InitOk()
            val file = File(cacheDir, getCacheNameByKey(key))
            if (file.exists()) {
                cacheCount.addAndGet(-1)
                cacheSize.addAndGet(-file.length())
            }
            return file
        }

        internal fun wait2InitOk() {
            try {
                mThread.join()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }

        internal fun getFileIfExists(key: String): File? {
            val file = File(cacheDir, getCacheNameByKey(key))
            return if (!file.exists()) null else file
        }

        internal fun getCacheNameByKey(key: String): String {
            return CACHE_PREFIX + key.substring(0, 3) + key.substring(3).hashCode()
        }

        internal fun put(file: File) {
            cacheCount.addAndGet(1)
            cacheSize.addAndGet(file.length())
            while (cacheCount.get() > countLimit || cacheSize.get() > sizeLimit) {
                cacheSize.addAndGet(-removeOldest())
                cacheCount.addAndGet(-1)
            }
        }

        internal fun updateModify(file: File) {
            val millis = System.currentTimeMillis()
            file.setLastModified(millis)
            lastUsageDates[file] = millis
        }

        internal fun removeByKey(key: String): Boolean {
            val file = getFileIfExists(key) ?: return true
            if (!file.delete()) return false
            cacheSize.addAndGet(-file.length())
            cacheCount.addAndGet(-1)
            lastUsageDates.remove(file)
            return true
        }

        internal fun clear(): Boolean {
            val files = cacheDir.listFiles { dir, name -> name.startsWith(CACHE_PREFIX) }
            if (files == null || files.size <= 0) return true
            var flag = true
            for (file in files) {
                if (!file.delete()) {
                    flag = false
                    continue
                }
                cacheSize.addAndGet(-file.length())
                cacheCount.addAndGet(-1)
                lastUsageDates.remove(file)
            }
            if (flag) {
                lastUsageDates.clear()
                cacheSize.set(0)
                cacheCount.set(0)
            }
            return flag
        }

        /**
         * Remove the oldest files.
         *
         * @return the size of oldest files, in bytes
         */
        internal fun removeOldest(): Long {
            if (lastUsageDates.isEmpty()) return 0
            var oldestUsage: Long? = Long.MAX_VALUE
            var oldestFile: File? = null
            val entries: Set<Map.Entry<File?, Long?>> = lastUsageDates.entries
            synchronized(lastUsageDates) {
                for ((key, lastValueUsage) in entries) {
                    if (lastValueUsage!! < oldestUsage!!) {
                        oldestUsage = lastValueUsage
                        oldestFile = key
                    }
                }
            }
            if (oldestFile == null) return 0
            val fileSize = oldestFile!!.length()
            if (oldestFile!!.delete()) {
                lastUsageDates.remove(oldestFile)
                return fileSize
            }
            return 0
        }

        init {
            cacheSize = AtomicLong()
            cacheCount = AtomicInteger()
            mThread = Thread {
                var size = 0
                var count = 0
                val cachedFiles = cacheDir.listFiles { dir, name -> name.startsWith(CACHE_PREFIX) }
                if (cachedFiles != null) {
                    for (cachedFile in cachedFiles) {
                        size += cachedFile.length().toInt()
                        count += 1
                        lastUsageDates[cachedFile] = cachedFile.lastModified()
                    }
                    cacheSize.getAndAdd(size.toLong())
                    cacheCount.getAndAdd(count)
                }
            }
            mThread.start()
        }
    }

    internal object DiskCacheHelper {
        const val TIME_INFO_LEN = 14
        fun newByteArrayWithTime(second: Int, data: ByteArray): ByteArray {
            val time = createDueTime(second).toByteArray()
            val content = ByteArray(time.size + data.size)
            System.arraycopy(time, 0, content, 0, time.size)
            System.arraycopy(data, 0, content, time.size, data.size)
            return content
        }

        /**
         * Return the string of due time.
         *
         * @param seconds The seconds.
         * @return the string of due time
         */
        internal fun createDueTime(seconds: Int): String {
            return String.format(
                Locale.getDefault(), "_\$%010d\$_",
                System.currentTimeMillis() / 1000 + seconds
            )
        }

        internal fun isDue(data: ByteArray?): Boolean {
            val millis = getDueTime(data)
            return millis != -1L && System.currentTimeMillis() > millis
        }

        internal fun getDueTime(data: ByteArray?): Long {
            if (hasTimeInfo(data)) {
                val millis = String(copyOfRange(data, 2, 12))
                return try {
                    millis.toLong() * 1000
                } catch (e: NumberFormatException) {
                    -1
                }
            }
            return -1
        }

        internal fun getDataWithoutDueTime(data: ByteArray?): ByteArray? {
            return if (hasTimeInfo(data)) {
                copyOfRange(data!!, TIME_INFO_LEN, data.size)
            } else data
        }

        internal fun copyOfRange(original: ByteArray?, from: Int, to: Int): ByteArray {
            if (null == original) return ByteArray(0)
            val newLength = to - from
            require(newLength >= 0) { "$from > $to" }
            val copy = ByteArray(newLength)
            System.arraycopy(original, from, copy, 0, Math.min(original.size - from, newLength))
            return copy
        }

        internal fun hasTimeInfo(data: ByteArray?): Boolean {
            return data != null && data.size >= TIME_INFO_LEN && data[0].toChar() == '_' && data[1].toChar() == '$' && data[12].toChar() == '$' && data[13].toChar() == '_'
        }
    }

    companion object {
        private const val DEFAULT_MAX_SIZE = Long.MAX_VALUE
        private const val DEFAULT_MAX_COUNT = Int.MAX_VALUE
        private const val CACHE_PREFIX = "cdu_"
        private const val TYPE_BYTE = "by_"
        private const val TYPE_STRING = "st_"
        private const val TYPE_JSON_OBJECT = "jo_"
        private const val TYPE_JSON_ARRAY = "ja_"
        private const val TYPE_BITMAP = "bi_"
        private const val TYPE_DRAWABLE = "dr_"
        private const val TYPE_PARCELABLE = "pa_"
        private const val TYPE_SERIALIZABLE = "se_"
        private val CACHE_MAP: MutableMap<String, CacheDiskUtils> = HashMap()

        /**
         * Return the single [CacheDiskUtils] instance.
         *
         * cache directory: /data/data/package/cache/cacheUtils
         *
         * cache size: unlimited
         *
         * cache count: unlimited
         *
         * @return the single [CacheDiskUtils] instance
         */
        val instance: CacheDiskUtils
            get() = getInstance("", DEFAULT_MAX_SIZE, DEFAULT_MAX_COUNT)

        /**
         * Return the single [CacheDiskUtils] instance.
         *
         * cache directory: /data/data/package/cache/cacheUtils
         *
         * cache size: unlimited
         *
         * cache count: unlimited
         *
         * @param cacheName The name of cache.
         * @return the single [CacheDiskUtils] instance
         */
        fun getInstance(cacheName: String?): CacheDiskUtils? {
            return getInstance(cacheName, DEFAULT_MAX_SIZE, DEFAULT_MAX_COUNT)
        }

        /**
         * Return the single [CacheDiskUtils] instance.
         *
         * cache directory: /data/data/package/cache/cacheUtils
         *
         * @param maxSize  The max size of cache, in bytes.
         * @param maxCount The max count of cache.
         * @return the single [CacheDiskUtils] instance
         */
        fun getInstance(maxSize: Long, maxCount: Int): CacheDiskUtils? {
            return getInstance("", maxSize, maxCount)
        }

        /**
         * Return the single [CacheDiskUtils] instance.
         *
         * cache directory: /data/data/package/cache/cacheName
         *
         * @param cacheName The name of cache.
         * @param maxSize   The max size of cache, in bytes.
         * @param maxCount  The max count of cache.
         * @return the single [CacheDiskUtils] instance
         */
        fun getInstance(cacheName: String?, maxSize: Long, maxCount: Int): CacheDiskUtils {
            var cacheName = cacheName
            if (isSpace(cacheName)) cacheName = "cacheUtils"
            val file = File(app!!.cacheDir, cacheName)
            return getInstance(file, maxSize, maxCount)
        }

        /**
         * Return the single [CacheDiskUtils] instance.
         *
         * cache size: unlimited
         *
         * cache count: unlimited
         *
         * @param cacheDir The directory of cache.
         * @return the single [CacheDiskUtils] instance
         */
        fun getInstance(cacheDir: File): CacheDiskUtils? {
            return getInstance(cacheDir, DEFAULT_MAX_SIZE, DEFAULT_MAX_COUNT)
        }

        /**
         * Return the single [CacheDiskUtils] instance.
         *
         * @param cacheDir The directory of cache.
         * @param maxSize  The max size of cache, in bytes.
         * @param maxCount The max count of cache.
         * @return the single [CacheDiskUtils] instance
         */
        fun getInstance(
            cacheDir: File,
            maxSize: Long,
            maxCount: Int
        ): CacheDiskUtils {
            val cacheKey = cacheDir.absoluteFile.toString() + "_" + maxSize + "_" + maxCount
            var cache = CACHE_MAP[cacheKey]
            if (cache == null) {
                synchronized(CacheDiskUtils::class.java) {
                    cache = CACHE_MAP[cacheKey]
                    if (cache == null) {
                        cache = CacheDiskUtils(cacheKey, cacheDir, maxSize, maxCount)
                        CACHE_MAP[cacheKey] = cache!!
                    }
                }
            }
            return cache!!
        }
    }
}