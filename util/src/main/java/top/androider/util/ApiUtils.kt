package top.androider.util

import android.util.Log
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * <pre>
 * author: Blankj
 * blog  : http://blankj.com
 * time  : 2019/07/09
 * desc  : utils about api
</pre> *
 */
private const val TAG = "ApiUtils"
private val mApiMap: MutableMap<Class<*>, BaseApi?> = ConcurrentHashMap()
private val mInjectApiImplMap: MutableMap<Class<*>?, Class<*>> = HashMap()

fun Class<*>.registerApi() {
    mInjectApiImplMap[this.superclass] = this
}

fun <Result> Class<*>.getApi(): Result? {
    var api = mApiMap[this]
    if (api != null) {
        return api as Result
    }
    synchronized(this) {
        api = mApiMap[this]
        if (api != null) {
            return api as Result
        }
        val implClass = mInjectApiImplMap[this]
        return if (implClass != null) {
            try {
                api = implClass.newInstance() as BaseApi
                mApiMap[this] = api
                api as Result?
            } catch (ignore: Exception) {
                Log.e(TAG, "The <$implClass> has no parameterless constructor.")
                null
            }
        } else {
            Log.e(TAG, "The <$this> doesn't implement.")
            null
        }
    }
}

fun toString(): String {
    return "ApiUtils: $mInjectApiImplMap"
}


@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@Retention(RetentionPolicy.CLASS)
annotation class Api(val isMock: Boolean = false)
class BaseApi