package top.androider.util

/**
 * <pre>
 * author: Blankj
 * blog  : http://blankj.com
 * time  : 2019/01/04
 * desc  : utils about shared preference
</pre> *
 */
object SPStaticUtils {
    private val defaultSPUtils by lazy<SPUtils> { SPUtils.getInstance() }

    /**
     * Return all values in sp.
     *
     * @return all values in sp
     */
    val all: Map<String, *>
        get() = getAll(defaultSPUtils)

    /**
     * Put the string value in sp.
     *
     * @param key      The key of sp.
     * @param value    The value of sp.
     * @param isCommit True to use [SharedPreferences.Editor.commit],
     * false to use [SharedPreferences.Editor.apply]
     */
    @JvmOverloads
    fun put(
        key: String,
        value: String?,
        isCommit: Boolean = false,
        spUtils: SPUtils = defaultSPUtils
    ) {
        spUtils.put(key, value, isCommit)
    }

    /**
     * Return the string value in sp.
     *
     * @param key          The key of sp.
     * @param defaultValue The default value if the sp doesn't exist.
     * @param spUtils      The instance of [SPUtils].
     * @return the string value if sp exists or `defaultValue` otherwise
     */
    fun getString(
        key: String,
        defaultValue: String? = "",
        spUtils: SPUtils = defaultSPUtils
    ): String? {
        return spUtils.getString(key, defaultValue)
    }
    /**
     * Put the int value in sp.
     *
     * @param key      The key of sp.
     * @param value    The value of sp.
     * @param isCommit True to use [SharedPreferences.Editor.commit],
     * false to use [SharedPreferences.Editor.apply]
     * @param spUtils  The instance of [SPUtils].
     */
    @JvmOverloads
    fun put(
        key: String,
        value: Int,
        isCommit: Boolean = false,
        spUtils: SPUtils = defaultSPUtils
    ) {
        spUtils.put(key, value, isCommit)
    }

    /**
     * Return the int value in sp.
     *
     * @param key          The key of sp.
     * @param defaultValue The default value if the sp doesn't exist.
     * @param spUtils      The instance of [SPUtils].
     * @return the int value if sp exists or `defaultValue` otherwise
     */
    fun getInt(key: String, defaultValue: Int = -1, spUtils: SPUtils = defaultSPUtils): Int {
        return spUtils.getInt(key, defaultValue)
    }

    /**
     * Put the long value in sp.
     *
     * @param key      The key of sp.
     * @param value    The value of sp.
     * @param isCommit True to use [SharedPreferences.Editor.commit],
     * false to use [SharedPreferences.Editor.apply]
     * @param spUtils  The instance of [SPUtils].
     */
    @JvmOverloads
    fun put(
        key: String,
        value: Long,
        isCommit: Boolean = false,
        spUtils: SPUtils = defaultSPUtils
    ) {
        spUtils.put(key, value, isCommit)
    }

    /**
     * Return the long value in sp.
     *
     * @param key     The key of sp.
     * @param spUtils The instance of [SPUtils].
     * @return the long value if sp exists or `-1` otherwise
     */
    fun getLong(key: String, defaultValue: Long = -1L, spUtils: SPUtils = defaultSPUtils): Long {
        return spUtils.getLong(key, defaultValue)
    }
    /**
     * Put the float value in sp.
     *
     * @param key      The key of sp.
     * @param value    The value of sp.
     * @param isCommit True to use [SharedPreferences.Editor.commit],
     * false to use [SharedPreferences.Editor.apply]
     * @param spUtils  The instance of [SPUtils].
     */
    @JvmOverloads
    fun put(
        key: String,
        value: Float,
        isCommit: Boolean = false,
        spUtils: SPUtils = defaultSPUtils
    ) {
        spUtils.put(key, value, isCommit)
    }

    /**
     * Return the float value in sp.
     *
     * @param key          The key of sp.
     * @param defaultValue The default value if the sp doesn't exist.
     * @param spUtils      The instance of [SPUtils].
     * @return the float value if sp exists or `defaultValue` otherwise
     */
    fun getFloat(key: String, defaultValue: Float = -1f, spUtils: SPUtils): Float {
        return spUtils.getFloat(key, defaultValue)
    }

    /**
     * Put the boolean value in sp.
     *
     * @param key      The key of sp.
     * @param value    The value of sp.
     * @param isCommit True to use [SharedPreferences.Editor.commit],
     * false to use [SharedPreferences.Editor.apply]
     * @param spUtils  The instance of [SPUtils].
     */
    @JvmOverloads
    fun put(
        key: String,
        value: Boolean,
        isCommit: Boolean = false,
        spUtils: SPUtils = defaultSPUtils
    ) {
        spUtils.put(key, value, isCommit)
    }

    /**
     * Return the boolean value in sp.
     *
     * @param key          The key of sp.
     * @param defaultValue The default value if the sp doesn't exist.
     * @param spUtils      The instance of [SPUtils].
     * @return the boolean value if sp exists or `defaultValue` otherwise
     */
    fun getBoolean(
        key: String,
        defaultValue: Boolean = false,
        spUtils: SPUtils
    ): Boolean {
        return spUtils.getBoolean(key, defaultValue)
    }
    /**
     * Put the set of string value in sp.
     *
     * @param key      The key of sp.
     * @param value    The value of sp.
     * @param isCommit True to use [SharedPreferences.Editor.commit],
     * false to use [SharedPreferences.Editor.apply]
     * @param spUtils  The instance of [SPUtils].
     */
    @JvmOverloads
    fun put(
        key: String,
        value: Set<String?>?,
        isCommit: Boolean = false,
        spUtils: SPUtils = defaultSPUtils
    ) {
        spUtils.put(key, value, isCommit)
    }

    /**
     * Return the set of string value in sp.
     *
     * @param key          The key of sp.
     * @param defaultValue The default value if the sp doesn't exist.
     * @param spUtils      The instance of [SPUtils].
     * @return the set of string value if sp exists or `defaultValue` otherwise
     */
    fun getStringSet(
        key: String,
        defaultValue: Set<String?>? = emptySet(),
        spUtils: SPUtils
    ): Set<String>? {
        return spUtils.getStringSet(key, defaultValue)
    }

    /**
     * Return all values in sp.
     *
     * @param spUtils The instance of [SPUtils].
     * @return all values in sp
     */
    fun getAll(spUtils: SPUtils): Map<String, *> {
        return spUtils.all
    }

    /**
     * Return whether the sp contains the preference.
     *
     * @param key     The key of sp.
     * @param spUtils The instance of [SPUtils].
     * @return `true`: yes<br></br>`false`: no
     */
    @JvmOverloads
    fun contains(key: String, spUtils: SPUtils = defaultSPUtils): Boolean {
        return spUtils.contains(key)
    }

    /**
     * Remove the preference in sp.
     *
     * @param key      The key of sp.
     * @param isCommit True to use [SharedPreferences.Editor.commit],
     * false to use [SharedPreferences.Editor.apply]
     * @param spUtils  The instance of [SPUtils].
     */
    @JvmOverloads
    fun remove(key: String, isCommit: Boolean = false, spUtils: SPUtils = defaultSPUtils) {
        spUtils.remove(key, isCommit)
    }

    /**
     * Remove all preferences in sp.
     *
     * @param isCommit True to use [SharedPreferences.Editor.commit],
     * false to use [SharedPreferences.Editor.apply]
     * @param spUtils  The instance of [SPUtils].
     */
    @JvmOverloads
    fun clear(isCommit: Boolean = false, spUtils: SPUtils = defaultSPUtils) {
        spUtils.clear(isCommit)
    }

}