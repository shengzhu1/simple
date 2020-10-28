package top.androider.util

import android.animation.ValueAnimator
import android.app.Activity
import android.app.Application
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.lifecycle.Lifecycle
import top.androider.util.Utils.OnAppStatusChangedListener
import java.lang.reflect.InvocationTargetException
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * <pre>
 * author: blankj
 * blog  : http://blankj.com
 * time  : 2020/03/19
 * desc  :
</pre> *
 */
internal object UtilsActivityLifecycleImpl : Application.ActivityLifecycleCallbacks {
    private val mActivityList = LinkedList<Activity>()
    private val mStatusListeners: MutableList<OnAppStatusChangedListener?> = ArrayList()
    private val mActivityLifecycleCallbacksMap: MutableMap<Activity, MutableList<Utils.ActivityLifecycleCallbacks>> =
        ConcurrentHashMap()
    private var mForegroundCount = 0
    private var mConfigCount = 0
    private var mIsBackground = false
    fun init(app: Application) {
        app.registerActivityLifecycleCallbacks(this)
    }

    fun unInit(app: Application) {
        mActivityList.clear()
        app.unregisterActivityLifecycleCallbacks(this)
    }

    val topActivity: Activity?
        get() {
            val activityList = activityList
            for (activity in activityList) {
                if (!isActivityAlive(activity)) {
                    continue
                }
                return activity
            }
            return null
        }
    val activityList: List<Activity>
        get() {
            if (!mActivityList.isEmpty()) {
                return mActivityList
            }
            val reflectActivities = activitiesByReflect
            mActivityList.addAll(reflectActivities)
            return mActivityList
        }

    fun addOnAppStatusChangedListener(listener: OnAppStatusChangedListener?) {
        mStatusListeners.add(listener)
    }

    fun removeOnAppStatusChangedListener(listener: OnAppStatusChangedListener?) {
        mStatusListeners.remove(listener)
    }

    fun addActivityLifecycleCallbacks(
        activity: Activity?,
        listener: Utils.ActivityLifecycleCallbacks?
    ) {
        if (activity == null || listener == null) return
        ThreadUtils.runOnUiThread(Runnable{ addActivityLifecycleCallbacksInner(activity, listener)})
    }

    val applicationByReflect: Application?
        get() {
            try {
                val activityThreadClass = Class.forName("android.app.ActivityThread")
                val thread = activityThread
                val app =
                    activityThreadClass.getMethod("getApplication").invoke(thread) ?: return null
                return app as Application
            } catch (e: InvocationTargetException) {
                e.printStackTrace()
            } catch (e: NoSuchMethodException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
            }
            return null
        }

    private fun addActivityLifecycleCallbacksInner(
        activity: Activity,
        lifecycleCallbacks: Utils.ActivityLifecycleCallbacks
    ) {
        var callbacks = mActivityLifecycleCallbacksMap[activity]
        if (callbacks == null) {
            callbacks = ArrayList()
            mActivityLifecycleCallbacksMap[activity] = callbacks
        } else {
            if (callbacks.contains(lifecycleCallbacks)) return
        }
        callbacks.add(lifecycleCallbacks)
    }

    fun removeActivityLifecycleCallbacks(activity: Activity?) {
        if (activity == null) return
        ThreadUtils.runOnUiThread(Runnable{ mActivityLifecycleCallbacksMap.remove(activity)})
    }

    fun removeActivityLifecycleCallbacks(
        activity: Activity?,
        callbacks: Utils.ActivityLifecycleCallbacks?
    ) {
        if (activity == null || callbacks == null) return
        ThreadUtils.runOnUiThread(Runnable{ removeActivityLifecycleCallbacksInner(activity, callbacks) })
    }

    private fun removeActivityLifecycleCallbacksInner(
        activity: Activity,
        lifecycleCallbacks: Utils.ActivityLifecycleCallbacks
    ) {
        val callbacks = mActivityLifecycleCallbacksMap[activity]
        if (callbacks != null && !callbacks.isEmpty()) {
            callbacks.remove(lifecycleCallbacks)
        }
    }

    private fun consumeActivityLifecycleCallbacks(activity: Activity, event: Lifecycle.Event) {
        val listeners: List<Utils.ActivityLifecycleCallbacks>? =
            mActivityLifecycleCallbacksMap[activity]
        if (listeners != null) {
            for (listener in listeners) {
                listener.onLifecycleChanged(activity, event)
                if (event == Lifecycle.Event.ON_CREATE) {
                    listener.onActivityCreated(activity)
                } else if (event == Lifecycle.Event.ON_START) {
                    listener.onActivityStarted(activity)
                } else if (event == Lifecycle.Event.ON_RESUME) {
                    listener.onActivityResumed(activity)
                } else if (event == Lifecycle.Event.ON_PAUSE) {
                    listener.onActivityPaused(activity)
                } else if (event == Lifecycle.Event.ON_STOP) {
                    listener.onActivityStopped(activity)
                } else if (event == Lifecycle.Event.ON_DESTROY) {
                    listener.onActivityDestroyed(activity)
                }
            }
            if (event == Lifecycle.Event.ON_DESTROY) {
                mActivityLifecycleCallbacksMap.remove(activity)
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // lifecycle start
    ///////////////////////////////////////////////////////////////////////////
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle) {
        applyLanguage(activity)
        setAnimatorsEnabled()
        setTopActivity(activity)
        consumeActivityLifecycleCallbacks(activity, Lifecycle.Event.ON_CREATE)
    }

    override fun onActivityStarted(activity: Activity) {
        if (!mIsBackground) {
            setTopActivity(activity)
        }
        if (mConfigCount < 0) {
            ++mConfigCount
        } else {
            ++mForegroundCount
        }
        consumeActivityLifecycleCallbacks(activity, Lifecycle.Event.ON_START)
    }

    override fun onActivityResumed(activity: Activity) {
        setTopActivity(activity)
        if (mIsBackground) {
            mIsBackground = false
            postStatus(activity, true)
        }
        processHideSoftInputOnActivityDestroy(activity, false)
        consumeActivityLifecycleCallbacks(activity, Lifecycle.Event.ON_RESUME)
    }

    override fun onActivityPaused(activity: Activity) {
        consumeActivityLifecycleCallbacks(activity, Lifecycle.Event.ON_PAUSE)
    }

    override fun onActivityStopped(activity: Activity) {
        if (activity.isChangingConfigurations) {
            --mConfigCount
        } else {
            --mForegroundCount
            if (mForegroundCount <= 0) {
                mIsBackground = true
                postStatus(activity, false)
            }
        }
        processHideSoftInputOnActivityDestroy(activity, true)
        consumeActivityLifecycleCallbacks(activity, Lifecycle.Event.ON_STOP)
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) { /**/
    }

    override fun onActivityDestroyed(activity: Activity) {
        mActivityList.remove(activity)
        KeyboardUtils.fixSoftInputLeaks(activity)
        consumeActivityLifecycleCallbacks(activity, Lifecycle.Event.ON_DESTROY)
    }
    ///////////////////////////////////////////////////////////////////////////
    // lifecycle end
    ///////////////////////////////////////////////////////////////////////////
    /**
     * To solve close keyboard when activity onDestroy.
     * The preActivity set windowSoftInputMode will prevent
     * the keyboard from closing when curActivity onDestroy.
     */
    private fun processHideSoftInputOnActivityDestroy(activity: Activity, isSave: Boolean) {
        if (isSave) {
            val attrs = activity.window.attributes
            val softInputMode = attrs.softInputMode
            activity.window.decorView.setTag(-123, softInputMode)
            activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        } else {
            val tag = activity.window.decorView.getTag(-123) as? Int ?: return
            ThreadUtils.runOnUiThreadDelayed(Runnable{
                val window = activity.window
                window?.setSoftInputMode(tag)
            }, 100)
        }
    }

    private fun postStatus(activity: Activity, isForeground: Boolean) {
        if (mStatusListeners.isEmpty()) return
        for (statusListener in mStatusListeners) {
            if (isForeground) {
                statusListener!!.onForeground(activity)
            } else {
                statusListener!!.onBackground(activity)
            }
        }
    }

    private fun setTopActivity(activity: Activity) {
        if (mActivityList.contains(activity)) {
            if (mActivityList.first != activity) {
                mActivityList.remove(activity)
                mActivityList.addFirst(activity)
            }
        } else {
            mActivityList.addFirst(activity)
        }
    }

    /**
     * @return the activities which topActivity is first position
     */
    private val activitiesByReflect: List<Activity>
        private get() {
            val list = LinkedList<Activity>()
            var topActivity: Activity? = null
            try {
                val mActivitiesField = activityThread!!.javaClass.getDeclaredField("mActivities")
                mActivitiesField.isAccessible = true
                val mActivities = mActivitiesField[activityThread] as? Map<*, *> ?: return list
                val binder_activityClientRecord_map = mActivities as Map<Any, Any>
                for (activityRecord in binder_activityClientRecord_map.values) {
                    val activityClientRecordClass: Class<*> = activityRecord.javaClass
                    val activityField = activityClientRecordClass.getDeclaredField("activity")
                    activityField.isAccessible = true
                    val activity = activityField[activityRecord] as Activity
                    if (topActivity == null) {
                        val pausedField = activityClientRecordClass.getDeclaredField("paused")
                        pausedField.isAccessible = true
                        if (!pausedField.getBoolean(activityRecord)) {
                            topActivity = activity
                        } else {
                            list.add(activity)
                        }
                    } else {
                        list.add(activity)
                    }
                }
            } catch (e: Exception) {
                Log.e("UtilsActivityLifecycle", "getActivitiesByReflect: " + e.message)
            }
            if (topActivity != null) {
                list.addFirst(topActivity)
            }
            return list
        }
    private val activityThread: Any?
        private get() {
            val activityThread = activityThreadInActivityThreadStaticField
            return activityThread ?: activityThreadInActivityThreadStaticMethod
        }
    private val activityThreadInActivityThreadStaticField: Any?
        private get() = try {
            val activityThreadClass = Class.forName("android.app.ActivityThread")
            val sCurrentActivityThreadField =
                activityThreadClass.getDeclaredField("sCurrentActivityThread")
            sCurrentActivityThreadField.isAccessible = true
            sCurrentActivityThreadField[null]
        } catch (e: Exception) {
            Log.e(
                "UtilsActivityLifecycle",
                "getActivityThreadInActivityThreadStaticField: " + e.message
            )
            null
        }
    private val activityThreadInActivityThreadStaticMethod: Any?
        private get() = try {
            val activityThreadClass = Class.forName("android.app.ActivityThread")
            activityThreadClass.getMethod("currentActivityThread").invoke(null)
        } catch (e: Exception) {
            Log.e(
                "UtilsActivityLifecycle",
                "getActivityThreadInActivityThreadStaticMethod: " + e.message
            )
            null
        }


    /**
     * Set animators enabled.
     */
    private fun setAnimatorsEnabled() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && ValueAnimator.areAnimatorsEnabled()) {
            return
        }
        try {
            val sDurationScaleField =
                ValueAnimator::class.java.getDeclaredField("sDurationScale")
            sDurationScaleField.isAccessible = true
            val sDurationScale = sDurationScaleField[null] as Float
            if (sDurationScale == 0f) {
                sDurationScaleField[null] = 1f
                Log.i(
                    "UtilsActivityLifecycle",
                    "setAnimatorsEnabled: Animators are enabled now!"
                )
            }
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
    }
}