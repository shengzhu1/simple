package top.androider.util

import android.app.Activity
import android.app.Application
import android.content.Context
import top.androider.util.ThreadUtils.cachedPool
import top.androider.util.Utils.Companion.app

fun preLoad() {
    preLoad(preLoadRunnable)
}


///////////////////////////////////////////////////////////////////////////
// AppUtils
///////////////////////////////////////////////////////////////////////////

val topActivityOrApp: Context
    get() = if (AppUtils.isAppForeground()) {
        val topActivity = UtilsActivityLifecycleImpl.topActivity
        topActivity ?: app!!
    } else {
        app!!
    }

///////////////////////////////////////////////////////////////////////////
// SpUtils
///////////////////////////////////////////////////////////////////////////
val spUtils4Utils: SPUtils
    get() = SPUtils.getInstance("Utils")


///////////////////////////////////////////////////////////////////////////
// ThreadUtils
///////////////////////////////////////////////////////////////////////////
fun <T> doAsync(task: Utils.Task<T>): Utils.Task<T> {
    cachedPool!!.execute(task)
    return task
}

private fun preLoad(vararg runs: Runnable) {
    for (r in runs) {
        cachedPool!!.execute(r)
    }
}