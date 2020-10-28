package top.androider.util

import java.lang.reflect.Type

/**
 * <pre>
 * author: Blankj
 * blog  : http://blankj.com
 * time  : 2018/01/30
 * desc  : utils about clone
</pre> *
 */

fun <T> Any.deepClone(type: Type): T? {
    return try {
        this.toJson().fromJson(type = type)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}