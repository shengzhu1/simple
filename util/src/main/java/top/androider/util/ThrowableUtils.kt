package top.androider.util

import android.util.Log

/**
 * <pre>
 * author: Blankj
 * blog  : http://blankj.com
 * time  : 2019/02/12
 * desc  : utils about exception
</pre> *
 */
fun Throwable?.getFullStackTrace(): String {
    return Log.getStackTraceString(this)
}