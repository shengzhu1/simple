package top.androider.util

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.lifecycle.Lifecycle
import top.androider.util.ThreadUtils.SimpleTask

/**
 * <pre>
 * author:
 * ___           ___           ___         ___
 * _____                       /  /\         /__/\         /__/|       /  /\
 * /  /::\                     /  /::\        \  \:\       |  |:|      /  /:/
 * /  /:/\:\    ___     ___    /  /:/\:\        \  \:\      |  |:|     /__/::\
 * /  /:/~/::\  /__/\   /  /\  /  /:/~/::\   _____\__\:\   __|  |:|     \__\/\:\
 * /__/:/ /:/\:| \  \:\ /  /:/ /__/:/ /:/\:\ /__/::::::::\ /__/\_|:|____    \  \:\
 * \  \:\/:/~/:/  \  \:\  /:/  \  \:\/:/__\/ \  \:\~~\~~\/ \  \:\/:::::/     \__\:\
 * \  \::/ /:/    \  \:\/:/    \  \::/       \  \:\  ~~~   \  \::/~~~~      /  /:/
 * \  \:\/:/      \  \::/      \  \:\        \  \:\        \  \:\         /__/:/
 * \  \::/        \__\/        \  \:\        \  \:\        \  \:\        \__\/
 * \__\/                       \__\/         \__\/         \__\/
 * blog  : http://blankj.com
 * time  : 16/12/08
 * desc  : utils about initialization
</pre> *
 */
class Utils {
    ///////////////////////////////////////////////////////////////////////////
    // interface
    ///////////////////////////////////////////////////////////////////////////
    abstract class Task<Result>(private val mConsumer: Consumer<Result>?) : SimpleTask<Result>() {
        override fun onSuccess(result: Result) {
            mConsumer?.accept(result)
        }
    }

    interface OnAppStatusChangedListener {
        fun onForeground(activity: Activity?)
        fun onBackground(activity: Activity?)
    }

    open class ActivityLifecycleCallbacks {
        fun onActivityCreated(activity: Activity) { /**/
        }

        fun onActivityStarted(activity: Activity) { /**/
        }

        fun onActivityResumed(activity: Activity) { /**/
        }

        fun onActivityPaused(activity: Activity) { /**/
        }

        fun onActivityStopped(activity: Activity) { /**/
        }

        open fun onActivityDestroyed(activity: Activity) { /**/
        }

        fun onLifecycleChanged(activity: Activity, event: Lifecycle.Event?) { /**/
        }
    }

    interface Consumer<T> {
        fun accept(t: T)
    }

    interface Supplier<T> {
        fun get(): T
    }

    interface Func1<Ret, Par> {
        fun call(param: Par): Ret
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var sApp: Application? = null

        /**
         * Init utils.
         *
         * Init it in the class of UtilsFileProvider.
         *
         * @param app application
         */
        @JvmStatic
        fun init(app: Application?) {
            if (app == null) {
                Log.e("Utils", "app is null.")
                return
            }
            if (sApp == null) {
                sApp = app
                UtilsActivityLifecycleImpl.init(sApp!!)
                preLoad()
                return
            }
            if (sApp == app) return
            UtilsActivityLifecycleImpl.unInit(sApp!!)
            sApp = app
            UtilsActivityLifecycleImpl.init(sApp!!)
        }

        /**
         * Return the Application object.
         *
         * Main process get app by UtilsFileProvider,
         * and other process get app by reflect.
         *
         * @return the Application object
         */
        @JvmStatic
        val app: Application?
            get() {
                if (sApp != null) return sApp
                init(UtilsActivityLifecycleImpl.applicationByReflect)
                if (sApp == null) throw NullPointerException("reflect failed.")
                Log.i("Utils", currentProcessName + " reflect app success.")
                return sApp
            }
    }
}