package top.androider.util

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.annotation.AnimRes
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import top.androider.util.Utils.Companion.app
import java.lang.ref.WeakReference
import java.util.*

/**
 * <pre>
 * author: Blankj
 * blog  : http://blankj.com
 * time  : 2016/09/23
 * desc  : utils about activity
</pre> *
 */


/**
 * Return the activity by context.
 *
 * @param context The context.
 * @return the activity by context.
 */
fun getActivityByContext(context: Context?): Activity? {
    return getActivityByContextInner(context)?.takeIf { isActivityAlive(it) }?.apply {  }?:null
}

private fun getActivityByContextInner(context: Context?): Activity? {
    var context: Context? = context ?: return null
    val list: MutableList<Context> = ArrayList()
    while (context is ContextWrapper) {
        if (context is Activity) {
            return context
        }
        val activity = getActivityFromDecorContext(context)
        if (activity != null) return activity
        list.add(context)
        context = context.baseContext
        if (context == null) {
            return null
        }
        if (list.contains(context)) {
            // loop context
            return null
        }
    }
    return null
}

private fun getActivityFromDecorContext(context: Context?): Activity? {
    if (context == null) return null
    if (context.javaClass.name == "com.android.internal.policy.DecorContext") {
        try {
            val mActivityContextField = context.javaClass.getDeclaredField("mActivityContext")
            mActivityContextField.isAccessible = true
            return (mActivityContextField[context] as WeakReference<Activity?>).get()
        } catch (ignore: Exception) {
        }
    }
    return null
}

/**
 * Return whether the activity exists.
 *
 * @param pkg The name of the package.
 * @param cls The name of the class.
 * @return `true`: yes<br></br>`false`: no
 */
fun isActivityExists(
    pkg: String,
    cls: String
): Boolean {
    val intent = Intent()
    intent.setClassName(pkg, cls)
    val pm = app!!.packageManager
    return !(pm.resolveActivity(
        intent,
        0
    ) == null || intent.resolveActivity(pm) == null || pm.queryIntentActivities(
        intent,
        0
    ).size == 0)
}

/**
 * Start the activity.
 *
 * @param clz The activity class.
 */
fun startActivity(clz: Class<out Activity?>) {
    val context: Context = topActivityOrApp
    startActivity(context, null, context.packageName, clz.name, null)
}

/**
 * Start the activity.
 *
 * @param clz     The activity class.
 * @param options Additional options for how the Activity should be started.
 */
fun startActivity(
    clz: Class<out Activity?>,
    options: Bundle?
) {
    val context: Context = topActivityOrApp
    startActivity(context, null, context.packageName, clz.name, options)
}

/**
 * Start the activity.
 *
 * @param activity The activity.
 * @param clz      The activity class.
 */
fun startActivity(
    activity: Activity,
    clz: Class<out Activity?>
) {
    startActivity(activity, null, activity.packageName, clz.name, null)
}

/**
 * Start the activity.
 *
 * @param activity The activity.
 * @param clz      The activity class.
 * @param options  Additional options for how the Activity should be started.
 */
fun startActivity(
    activity: Activity,
    clz: Class<out Activity?>,
    options: Bundle?
) {
    startActivity(activity, null, activity.packageName, clz.name, options)
}

/**
 * Start the activity.
 *
 * @param activity       The activity.
 * @param clz            The activity class.
 * @param sharedElements The names of the shared elements to transfer to the called
 * Activity and their associated Views.
 */
fun startActivity(
    activity: Activity,
    clz: Class<out Activity?>,
    sharedElements: Array<out View>
) {
    startActivity(
        activity, null, activity.packageName, clz.name,
        getOptionsBundle(activity, sharedElements)
    )
}

/**
 * Start the activity.
 *
 * @param extras The Bundle of extras to add to this intent.
 * @param clz    The activity class.
 */
fun startActivity(
    extras: Bundle,
    clz: Class<out Activity?>
) {
    val context: Context = topActivityOrApp
    startActivity(context, extras, context.packageName, clz.name, null)
}

/**
 * Start the activity.
 *
 * @param extras  The Bundle of extras to add to this intent.
 * @param clz     The activity class.
 * @param options Additional options for how the Activity should be started.
 */
fun startActivity(
    extras: Bundle,
    clz: Class<out Activity?>,
    options: Bundle?
) {
    val context: Context = topActivityOrApp
    startActivity(context, extras, context.packageName, clz.name, options)
}

/**
 * Start the activity.
 *
 * @param extras   The Bundle of extras to add to this intent.
 * @param activity The activity.
 * @param clz      The activity class.
 */
fun startActivity(
    extras: Bundle,
    activity: Activity,
    clz: Class<out Activity?>
) {
    startActivity(activity, extras, activity.packageName, clz.name, null)
}

/**
 * Start the activity.
 *
 * @param extras   The Bundle of extras to add to this intent.
 * @param activity The activity.
 * @param clz      The activity class.
 * @param options  Additional options for how the Activity should be started.
 */
fun startActivity(
    extras: Bundle,
    activity: Activity,
    clz: Class<out Activity?>,
    options: Bundle?
) {
    startActivity(activity, extras, activity.packageName, clz.name, options)
}

/**
 * Start the activity.
 *
 * @param extras         The Bundle of extras to add to this intent.
 * @param activity       The activity.
 * @param clz            The activity class.
 * @param sharedElements The names of the shared elements to transfer to the called
 * Activity and their associated Views.
 */
fun startActivity(
    extras: Bundle,
    activity: Activity,
    clz: Class<out Activity?>,
    sharedElements: Array<out View>
) {
    startActivity(
        activity, extras, activity.packageName, clz.name,
        getOptionsBundle(activity, sharedElements)
    )
}

/**
 * Start the activity.
 *
 * @param pkg The name of the package.
 * @param cls The name of the class.
 */
fun startActivity(
    pkg: String,
    cls: String
) {
    startActivity(topActivityOrApp, null, pkg, cls, null)
}

/**
 * Start the activity.
 *
 * @param pkg     The name of the package.
 * @param cls     The name of the class.
 * @param options Additional options for how the Activity should be started.
 */
fun startActivity(
    pkg: String,
    cls: String,
    options: Bundle?
) {
    startActivity(topActivityOrApp, null, pkg, cls, options)
}

/**
 * Start the activity.
 *
 * @param activity The activity.
 * @param pkg      The name of the package.
 * @param cls      The name of the class.
 */
fun startActivity(
    activity: Activity,
    pkg: String,
    cls: String
) {
    startActivity(activity, null, pkg, cls, null)
}

/**
 * Start the activity.
 *
 * @param activity The activity.
 * @param pkg      The name of the package.
 * @param cls      The name of the class.
 * @param options  Additional options for how the Activity should be started.
 */
fun startActivity(
    activity: Activity,
    pkg: String,
    cls: String,
    options: Bundle?
) {
    startActivity(activity, null, pkg, cls, options)
}

/**
 * Start the activity.
 *
 * @param activity       The activity.
 * @param pkg            The name of the package.
 * @param cls            The name of the class.
 * @param sharedElements The names of the shared elements to transfer to the called
 * Activity and their associated Views.
 */
fun startActivity(
    activity: Activity,
    pkg: String,
    cls: String,
    sharedElements: Array<out View>
) {
    startActivity(activity, null, pkg, cls, getOptionsBundle(activity, sharedElements))
}

/**
 * Start the activity.
 *
 * @param extras The Bundle of extras to add to this intent.
 * @param pkg    The name of the package.
 * @param cls    The name of the class.
 */
fun startActivity(
    extras: Bundle,
    pkg: String,
    cls: String
) {
    startActivity(topActivityOrApp, extras, pkg, cls, null)
}

/**
 * Start the activity.
 *
 * @param extras  The Bundle of extras to add to this intent.
 * @param pkg     The name of the package.
 * @param cls     The name of the class.
 * @param options Additional options for how the Activity should be started.
 */
fun startActivity(
    extras: Bundle,
    pkg: String,
    cls: String,
    options: Bundle?
) {
    startActivity(topActivityOrApp, extras, pkg, cls, options)
}

/**
 * Start the activity.
 *
 * @param activity The activity.
 * @param extras   The Bundle of extras to add to this intent.
 * @param pkg      The name of the package.
 * @param cls      The name of the class.
 */
fun startActivity(
    extras: Bundle,
    activity: Activity,
    pkg: String,
    cls: String
) {
    startActivity(activity, extras, pkg, cls, null)
}

/**
 * Start the activity.
 *
 * @param extras   The Bundle of extras to add to this intent.
 * @param activity The activity.
 * @param pkg      The name of the package.
 * @param cls      The name of the class.
 * @param options  Additional options for how the Activity should be started.
 */
fun startActivity(
    extras: Bundle,
    activity: Activity,
    pkg: String,
    cls: String,
    options: Bundle?
) {
    startActivity(activity, extras, pkg, cls, options)
}

/**
 * Start the activity.
 *
 * @param extras         The Bundle of extras to add to this intent.
 * @param activity       The activity.
 * @param pkg            The name of the package.
 * @param cls            The name of the class.
 * @param sharedElements The names of the shared elements to transfer to the called
 * Activity and their associated Views.
 */
fun startActivity(
    extras: Bundle,
    activity: Activity,
    pkg: String,
    cls: String,
    sharedElements: Array<out View>
) {
    startActivity(activity, extras, pkg, cls, getOptionsBundle(activity, sharedElements))
}

/**
 * Start the activity.
 *
 * @param intent The description of the activity to start.
 * @return `true`: success<br></br>`false`: fail
 */
fun startActivity(intent: Intent): Boolean {
    return startActivity(intent, topActivityOrApp, null)
}

/**
 * Start the activity.
 *
 * @param intent  The description of the activity to start.
 * @param options Additional options for how the Activity should be started.
 * @return `true`: success<br></br>`false`: fail
 */
fun startActivity(
    intent: Intent,
    options: Bundle?
): Boolean {
    return startActivity(intent, topActivityOrApp, options)
}

/**
 * Start the activity.
 *
 * @param activity The activity.
 * @param intent   The description of the activity to start.
 */
fun startActivity(
    activity: Activity,
    intent: Intent
) {
    startActivity(intent, activity, null)
}

/**
 * Start the activity.
 *
 * @param activity The activity.
 * @param intent   The description of the activity to start.
 * @param options  Additional options for how the Activity should be started.
 */
fun startActivity(
    activity: Activity,
    intent: Intent,
    options: Bundle?
) {
    startActivity(intent, activity, options)
}

/**
 * Start the activity.
 *
 * @param activity       The activity.
 * @param intent         The description of the activity to start.
 * @param sharedElements The names of the shared elements to transfer to the called
 * Activity and their associated Views.
 */
fun startActivity(
    activity: Activity,
    intent: Intent,
    sharedElements: Array<out View>
) {
    startActivity(intent, activity, getOptionsBundle(activity, sharedElements))
}

/**
 * Start the activity.
 *
 * @param activity    The activity.
 * @param clz         The activity class.
 * @param requestCode if &gt;= 0, this code will be returned in
 * onActivityResult() when the activity exits.
 */
fun startActivityForResult(
    activity: Activity,
    clz: Class<out Activity?>,
    requestCode: Int
) {
    startActivityForResult(
        activity, null, activity.packageName, clz.name,
        requestCode, null
    )
}

/**
 * Start the activity.
 *
 * @param activity    The activity.
 * @param clz         The activity class.
 * @param requestCode if &gt;= 0, this code will be returned in
 * onActivityResult() when the activity exits.
 * @param options     Additional options for how the Activity should be started.
 */
fun startActivityForResult(
    activity: Activity,
    clz: Class<out Activity?>,
    requestCode: Int,
    options: Bundle?
) {
    startActivityForResult(
        activity, null, activity.packageName, clz.name,
        requestCode, options
    )
}

/**
 * Start the activity.
 *
 * @param activity       The activity.
 * @param clz            The activity class.
 * @param requestCode    if &gt;= 0, this code will be returned in
 * onActivityResult() when the activity exits.
 * @param sharedElements The names of the shared elements to transfer to the called
 * Activity and their associated Views.
 */
fun startActivityForResult(
    activity: Activity,
    clz: Class<out Activity?>,
    requestCode: Int,
    sharedElements: Array<out View>
) {
    startActivityForResult(
        activity, null, activity.packageName, clz.name,
        requestCode, getOptionsBundle(activity, sharedElements)
    )
}

/**
 * Start the activity.
 *
 * @param extras      The Bundle of extras to add to this intent.
 * @param activity    The activity.
 * @param clz         The activity class.
 * @param requestCode if &gt;= 0, this code will be returned in
 * onActivityResult() when the activity exits.
 */
fun startActivityForResult(
    extras: Bundle,
    activity: Activity,
    clz: Class<out Activity?>,
    requestCode: Int
) {
    startActivityForResult(
        activity, extras, activity.packageName, clz.name,
        requestCode, null
    )
}

/**
 * Start the activity.
 *
 * @param extras      The Bundle of extras to add to this intent.
 * @param activity    The activity.
 * @param clz         The activity class.
 * @param requestCode if &gt;= 0, this code will be returned in
 * onActivityResult() when the activity exits.
 * @param options     Additional options for how the Activity should be started.
 */
fun startActivityForResult(
    extras: Bundle,
    activity: Activity,
    clz: Class<out Activity?>,
    requestCode: Int,
    options: Bundle?
) {
    startActivityForResult(
        activity, extras, activity.packageName, clz.name,
        requestCode, options
    )
}

/**
 * Start the activity.
 *
 * @param extras         The Bundle of extras to add to this intent.
 * @param activity       The activity.
 * @param clz            The activity class.
 * @param requestCode    if &gt;= 0, this code will be returned in
 * onActivityResult() when the activity exits.
 * @param sharedElements The names of the shared elements to transfer to the called
 * Activity and their associated Views.
 */
fun startActivityForResult(
    extras: Bundle,
    activity: Activity,
    clz: Class<out Activity?>,
    requestCode: Int,
    sharedElements: Array<out View>
) {
    startActivityForResult(
        activity, extras, activity.packageName, clz.name,
        requestCode, getOptionsBundle(activity, sharedElements)
    )
}

/**
 * Start the activity for result.
 *
 * @param activity    The activity.
 * @param extras      The Bundle of extras to add to this intent.
 * @param pkg         The name of the package.
 * @param cls         The name of the class.
 * @param requestCode if &gt;= 0, this code will be returned in
 * onActivityResult() when the activity exits.
 */
fun startActivityForResult(
    extras: Bundle,
    activity: Activity,
    pkg: String,
    cls: String,
    requestCode: Int
) {
    startActivityForResult(activity, extras, pkg, cls, requestCode, null)
}

/**
 * Start the activity for result.
 *
 * @param extras      The Bundle of extras to add to this intent.
 * @param activity    The activity.
 * @param pkg         The name of the package.
 * @param cls         The name of the class.
 * @param requestCode if &gt;= 0, this code will be returned in
 * onActivityResult() when the activity exits.
 * @param options     Additional options for how the Activity should be started.
 */
fun startActivityForResult(
    extras: Bundle,
    activity: Activity,
    pkg: String,
    cls: String,
    requestCode: Int,
    options: Bundle?
) {
    startActivityForResult(activity, extras, pkg, cls, requestCode, options)
}

/**
 * Start the activity for result.
 *
 * @param extras         The Bundle of extras to add to this intent.
 * @param activity       The activity.
 * @param pkg            The name of the package.
 * @param cls            The name of the class.
 * @param requestCode    if &gt;= 0, this code will be returned in
 * onActivityResult() when the activity exits.
 * @param sharedElements The names of the shared elements to transfer to the called
 * Activity and their associated Views.
 */
fun startActivityForResult(
    extras: Bundle,
    activity: Activity,
    pkg: String,
    cls: String,
    requestCode: Int,
    sharedElements: Array<out View>
) {
    startActivityForResult(
        activity, extras, pkg, cls,
        requestCode, getOptionsBundle(activity, sharedElements)
    )
}

/**
 * Start the activity for result.
 *
 * @param activity    The activity.
 * @param intent      The description of the activity to start.
 * @param requestCode if &gt;= 0, this code will be returned in
 * onActivityResult() when the activity exits.
 */
fun startActivityForResult(
    activity: Activity,
    intent: Intent,
    requestCode: Int
) {
    startActivityForResult(intent, activity, requestCode, null)
}

/**
 * Start the activity for result.
 *
 * @param activity    The activity.
 * @param intent      The description of the activity to start.
 * @param requestCode if &gt;= 0, this code will be returned in
 * onActivityResult() when the activity exits.
 * @param options     Additional options for how the Activity should be started.
 */
fun startActivityForResult(
    activity: Activity,
    intent: Intent,
    requestCode: Int,
    options: Bundle?
) {
    startActivityForResult(intent, activity, requestCode, options)
}

/**
 * Start the activity for result.
 *
 * @param activity       The activity.
 * @param intent         The description of the activity to start.
 * @param requestCode    if &gt;= 0, this code will be returned in
 * onActivityResult() when the activity exits.
 * @param sharedElements The names of the shared elements to transfer to the called
 * Activity and their associated Views.
 */
fun startActivityForResult(
    activity: Activity,
    intent: Intent,
    requestCode: Int,
    sharedElements: Array<out View>
) {
    startActivityForResult(
        intent, activity,
        requestCode, getOptionsBundle(activity, sharedElements)
    )
}

/**
 * Start the activity.
 *
 * @param fragment    The fragment.
 * @param clz         The activity class.
 * @param requestCode if &gt;= 0, this code will be returned in
 * onActivityResult() when the activity exits.
 */
fun startActivityForResult(
    fragment: Fragment,
    clz: Class<out Activity?>,
    requestCode: Int
) {
    startActivityForResult(
        fragment, null, app!!.packageName, clz.name,
        requestCode, null
    )
}

/**
 * Start the activity.
 *
 * @param fragment    The fragment.
 * @param clz         The activity class.
 * @param requestCode if &gt;= 0, this code will be returned in
 * onActivityResult() when the activity exits.
 * @param options     Additional options for how the Activity should be started.
 */
fun startActivityForResult(
    fragment: Fragment,
    clz: Class<out Activity?>,
    requestCode: Int,
    options: Bundle?
) {
    startActivityForResult(
        fragment, null, app!!.packageName, clz.name,
        requestCode, options
    )
}

/**
 * Start the activity.
 *
 * @param fragment       The fragment.
 * @param clz            The activity class.
 * @param requestCode    if &gt;= 0, this code will be returned in
 * onActivityResult() when the activity exits.
 * @param sharedElements The names of the shared elements to transfer to the called
 * Activity and their associated Views.
 */
fun startActivityForResult(
    fragment: Fragment,
    clz: Class<out Activity?>,
    requestCode: Int,
    sharedElements: Array<out View>
) {
    startActivityForResult(
        fragment, null, app!!.packageName, clz.name,
        requestCode, getOptionsBundle(fragment, sharedElements)
    )
}

/**
 * Start the activity.
 *
 * @param fragment    The fragment.
 * @param clz         The activity class.
 * @param requestCode if &gt;= 0, this code will be returned in
 * onActivityResult() when the activity exits.
 * @param enterAnim   A resource ID of the animation resource to use for the
 * incoming activity.
 * @param exitAnim    A resource ID of the animation resource to use for the
 * outgoing activity.
 */
fun startActivityForResult(
    fragment: Fragment,
    clz: Class<out Activity?>,
    requestCode: Int,
    @AnimRes enterAnim: Int,
    @AnimRes exitAnim: Int
) {
    startActivityForResult(
        fragment, null, app!!.packageName, clz.name,
        requestCode, getOptionsBundle(fragment, enterAnim, exitAnim)
    )
}

/**
 * Start the activity.
 *
 * @param extras      The Bundle of extras to add to this intent.
 * @param fragment    The fragment.
 * @param clz         The activity class.
 * @param requestCode if &gt;= 0, this code will be returned in
 * onActivityResult() when the activity exits.
 */
fun startActivityForResult(
    extras: Bundle,
    fragment: Fragment,
    clz: Class<out Activity?>,
    requestCode: Int
) {
    startActivityForResult(
        fragment, extras, app!!.packageName, clz.name,
        requestCode, null
    )
}

/**
 * Start the activity.
 *
 * @param extras      The Bundle of extras to add to this intent.
 * @param fragment    The fragment.
 * @param clz         The activity class.
 * @param requestCode if &gt;= 0, this code will be returned in
 * onActivityResult() when the activity exits.
 * @param options     Additional options for how the Activity should be started.
 */
fun startActivityForResult(
    extras: Bundle,
    fragment: Fragment,
    clz: Class<out Activity?>,
    requestCode: Int,
    options: Bundle?
) {
    startActivityForResult(
        fragment, extras, app!!.packageName, clz.name,
        requestCode, options
    )
}

/**
 * Start the activity.
 *
 * @param extras         The Bundle of extras to add to this intent.
 * @param fragment       The fragment.
 * @param clz            The activity class.
 * @param requestCode    if &gt;= 0, this code will be returned in
 * onActivityResult() when the activity exits.
 * @param sharedElements The names of the shared elements to transfer to the called
 * Activity and their associated Views.
 */
fun startActivityForResult(
    extras: Bundle,
    fragment: Fragment,
    clz: Class<out Activity?>,
    requestCode: Int,
    sharedElements: Array<out View>
) {
    startActivityForResult(
        fragment, extras, app!!.packageName, clz.name,
        requestCode, getOptionsBundle(fragment, sharedElements)
    )
}

/**
 * Start the activity for result.
 *
 * @param fragment    The fragment.
 * @param extras      The Bundle of extras to add to this intent.
 * @param pkg         The name of the package.
 * @param cls         The name of the class.
 * @param requestCode if &gt;= 0, this code will be returned in
 * onActivityResult() when the activity exits.
 */
fun startActivityForResult(
    extras: Bundle,
    fragment: Fragment,
    pkg: String,
    cls: String,
    requestCode: Int
) {
    startActivityForResult(fragment, extras, pkg, cls, requestCode, null)
}

/**
 * Start the activity for result.
 *
 * @param extras      The Bundle of extras to add to this intent.
 * @param fragment    The fragment.
 * @param pkg         The name of the package.
 * @param cls         The name of the class.
 * @param requestCode if &gt;= 0, this code will be returned in
 * onActivityResult() when the activity exits.
 * @param options     Additional options for how the Activity should be started.
 */
fun startActivityForResult(
    extras: Bundle,
    fragment: Fragment,
    pkg: String,
    cls: String,
    requestCode: Int,
    options: Bundle?
) {
    startActivityForResult(fragment, extras, pkg, cls, requestCode, options)
}

/**
 * Start the activity for result.
 *
 * @param extras         The Bundle of extras to add to this intent.
 * @param fragment       The fragment.
 * @param pkg            The name of the package.
 * @param cls            The name of the class.
 * @param requestCode    if &gt;= 0, this code will be returned in
 * onActivityResult() when the activity exits.
 * @param sharedElements The names of the shared elements to transfer to the called
 * Activity and their associated Views.
 */
fun startActivityForResult(
    extras: Bundle,
    fragment: Fragment,
    pkg: String,
    cls: String,
    requestCode: Int,
    sharedElements: Array<out View>
) {
    startActivityForResult(
        fragment, extras, pkg, cls,
        requestCode, getOptionsBundle(fragment, sharedElements)
    )
}

/**
 * Start the activity for result.
 *
 * @param extras      The Bundle of extras to add to this intent.
 * @param pkg         The name of the package.
 * @param cls         The name of the class.
 * @param requestCode if &gt;= 0, this code will be returned in
 * onActivityResult() when the activity exits.
 * @param enterAnim   A resource ID of the animation resource to use for the
 * incoming activity.
 * @param exitAnim    A resource ID of the animation resource to use for the
 * outgoing activity.
 */
fun startActivityForResult(
    extras: Bundle,
    fragment: Fragment,
    pkg: String,
    cls: String,
    requestCode: Int,
    @AnimRes enterAnim: Int,
    @AnimRes exitAnim: Int
) {
    startActivityForResult(
        fragment, extras, pkg, cls,
        requestCode, getOptionsBundle(fragment, enterAnim, exitAnim)
    )
}

/**
 * Start the activity for result.
 *
 * @param fragment    The fragment.
 * @param intent      The description of the activity to start.
 * @param requestCode if &gt;= 0, this code will be returned in
 * onActivityResult() when the activity exits.
 */
fun startActivityForResult(
    fragment: Fragment,
    intent: Intent,
    requestCode: Int
) {
    startActivityForResult(intent, fragment, requestCode, null)
}

/**
 * Start the activity for result.
 *
 * @param fragment    The fragment.
 * @param intent      The description of the activity to start.
 * @param requestCode if &gt;= 0, this code will be returned in
 * onActivityResult() when the activity exits.
 * @param options     Additional options for how the Activity should be started.
 */
fun startActivityForResult(
    fragment: Fragment,
    intent: Intent,
    requestCode: Int,
    options: Bundle?
) {
    startActivityForResult(intent, fragment, requestCode, options)
}

/**
 * Start the activity for result.
 *
 * @param fragment       The fragment.
 * @param intent         The description of the activity to start.
 * @param requestCode    if &gt;= 0, this code will be returned in
 * onActivityResult() when the activity exits.
 * @param sharedElements The names of the shared elements to transfer to the called
 * Activity and their associated Views.
 */
fun startActivityForResult(
    fragment: Fragment,
    intent: Intent,
    requestCode: Int,
    sharedElements: Array<out View>
) {
    startActivityForResult(
        intent, fragment,
        requestCode, getOptionsBundle(fragment, sharedElements)
    )
}

/**
 * Start the activity for result.
 *
 * @param fragment    The fragment.
 * @param intent      The description of the activity to start.
 * @param requestCode if &gt;= 0, this code will be returned in
 * onActivityResult() when the activity exits.
 * @param enterAnim   A resource ID of the animation resource to use for the
 * incoming activity.
 * @param exitAnim    A resource ID of the animation resource to use for the
 * outgoing activity.
 */
fun startActivityForResult(
    fragment: Fragment,
    intent: Intent,
    requestCode: Int,
    @AnimRes enterAnim: Int,
    @AnimRes exitAnim: Int
) {
    startActivityForResult(
        intent, fragment,
        requestCode, getOptionsBundle(fragment, enterAnim, exitAnim)
    )
}

/**
 * Start activities.
 *
 * @param intents The descriptions of the activities to start.
 */
fun startActivities(intents: Array<Intent>) {
    startActivities(intents, topActivityOrApp, null)
}

/**
 * Start activities.
 *
 * @param intents The descriptions of the activities to start.
 * @param options Additional options for how the Activity should be started.
 */
fun startActivities(
    intents: Array<Intent>,
    options: Bundle?
) {
    startActivities(intents, topActivityOrApp, options)
}

/**
 * Start activities.
 *
 * @param activity The activity.
 * @param intents  The descriptions of the activities to start.
 */
fun startActivities(
    activity: Activity,
    intents: Array<Intent>
) {
    startActivities(intents, activity, null)
}

/**
 * Start activities.
 *
 * @param activity The activity.
 * @param intents  The descriptions of the activities to start.
 * @param options  Additional options for how the Activity should be started.
 */
fun startActivities(
    activity: Activity,
    intents: Array<Intent>,
    options: Bundle?
) {
    startActivities(intents, activity, options)
}

/**
 * Start activities.
 *
 * @param activity  The activity.
 * @param intents   The descriptions of the activities to start.
 * @param enterAnim A resource ID of the animation resource to use for the
 * incoming activity.
 * @param exitAnim  A resource ID of the animation resource to use for the
 * outgoing activity.
 */
fun startActivities(
    activity: Activity,
    intents: Array<Intent>,
    @AnimRes enterAnim: Int,
    @AnimRes exitAnim: Int
) {
    startActivities(intents, activity, getOptionsBundle(activity, enterAnim, exitAnim))
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
        activity.overridePendingTransition(enterAnim, exitAnim)
    }
}

/**
 * Start home activity.
 */
fun startHomeActivity() {
    val homeIntent = Intent(Intent.ACTION_MAIN)
    homeIntent.addCategory(Intent.CATEGORY_HOME)
    homeIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    startActivity(homeIntent)
}
/**
 * Start the launcher activity.
 *
 * @param pkg The name of the package.
 */
/**
 * Start the launcher activity.
 */
@JvmOverloads
fun startLauncherActivity(pkg: String = app!!.packageName) {
    val launcherActivity = getLauncherActivity(pkg)
    if (TextUtils.isEmpty(launcherActivity)) return
    startActivity(pkg, launcherActivity)
}

/**
 * Return the list of activity.
 *
 * @return the list of activity
 */
val activityList: List<Activity>
    get() = UtilsActivityLifecycleImpl.activityList

/**
 * Return the name of launcher activity.
 *
 * @return the name of launcher activity
 */
val launcherActivity: String
    get() = getLauncherActivity(app!!.packageName)

/**
 * Return the name of launcher activity.
 *
 * @param pkg The name of the package.
 * @return the name of launcher activity
 */
fun getLauncherActivity(pkg: String?): String {
    if (pkg.isNullOrBlank()) return ""
    val intent = Intent(Intent.ACTION_MAIN, null)
    intent.addCategory(Intent.CATEGORY_LAUNCHER)
    intent.setPackage(pkg)
    val pm = app!!.packageManager
    val info = pm.queryIntentActivities(intent, 0)
    return if (info == null || info.size == 0) {
        ""
    } else info[0].activityInfo.name
}

/**
 * Return the list of main activities.
 *
 * @return the list of main activities
 */
val mainActivities: List<String>
    get() = getMainActivities(app!!.packageName)

/**
 * Return the list of main activities.
 *
 * @param pkg The name of the package.
 * @return the list of main activities
 */
fun getMainActivities(pkg: String): List<String> {
    val ret: MutableList<String> = ArrayList()
    val intent = Intent(Intent.ACTION_MAIN, null)
    intent.setPackage(pkg)
    val pm = app!!.packageManager
    val info = pm.queryIntentActivities(intent, 0)
    val size = info.size
    if (size == 0) return ret
    for (i in 0 until size) {
        val ri = info[i]
        if (ri.activityInfo.processName == pkg) {
            ret.add(ri.activityInfo.name)
        }
    }
    return ret
}

/**
 * Return whether the activity is alive.
 *
 * @param context The context.
 * @return `true`: yes<br></br>`false`: no
 */
fun isActivityAlive(context: Context?): Boolean {
    return isActivityAlive(getActivityByContext(context))
}

/**
 * Return whether the activity exists in activity's stack.
 *
 * @param activity The activity.
 * @return `true`: yes<br></br>`false`: no
 */
fun isActivityExistsInStack(activity: Activity): Boolean {
    val activities: List<Activity> = UtilsActivityLifecycleImpl.activityList
    for (aActivity in activities) {
        if (aActivity == activity) {
            return true
        }
    }
    return false
}

/**
 * Return whether the activity exists in activity's stack.
 *
 * @param clz The activity class.
 * @return `true`: yes<br></br>`false`: no
 */
fun isActivityExistsInStack(clz: Class<out Activity?>): Boolean {
    val activities: List<Activity> = UtilsActivityLifecycleImpl.activityList
    for (aActivity in activities) {
        if (aActivity.javaClass == clz) {
            return true
        }
    }
    return false
}
/**
 * Finish the activity.
 *
 * @param activity   The activity.
 * @param isLoadAnim True to use animation for the outgoing activity, false otherwise.
 */
/**
 * Finish the activity.
 *
 * @param activity The activity.
 */
@JvmOverloads
fun finishActivity(activity: Activity, isLoadAnim: Boolean = false) {
    activity.finish()
    if (!isLoadAnim) {
        activity.overridePendingTransition(0, 0)
    }
}

/**
 * Finish the activity.
 *
 * @param activity  The activity.
 * @param enterAnim A resource ID of the animation resource to use for the
 * incoming activity.
 * @param exitAnim  A resource ID of the animation resource to use for the
 * outgoing activity.
 */
fun finishActivity(
    activity: Activity,
    @AnimRes enterAnim: Int,
    @AnimRes exitAnim: Int
) {
    activity.finish()
    activity.overridePendingTransition(enterAnim, exitAnim)
}
/**
 * Finish the activity.
 *
 * @param clz        The activity class.
 * @param isLoadAnim True to use animation for the outgoing activity, false otherwise.
 */
/**
 * Finish the activity.
 *
 * @param clz The activity class.
 */
@JvmOverloads
fun finishActivity(
    clz: Class<out Activity?>,
    isLoadAnim: Boolean = false
) {
    val activities: List<Activity> = UtilsActivityLifecycleImpl.activityList
    for (activity in activities) {
        if (activity.javaClass == clz) {
            activity.finish()
            if (!isLoadAnim) {
                activity.overridePendingTransition(0, 0)
            }
        }
    }
}

/**
 * Finish the activity.
 *
 * @param clz       The activity class.
 * @param enterAnim A resource ID of the animation resource to use for the
 * incoming activity.
 * @param exitAnim  A resource ID of the animation resource to use for the
 * outgoing activity.
 */
fun finishActivity(
    clz: Class<out Activity?>,
    @AnimRes enterAnim: Int,
    @AnimRes exitAnim: Int
) {
    val activities: List<Activity> = UtilsActivityLifecycleImpl.activityList
    for (activity in activities) {
        if (activity.javaClass == clz) {
            activity.finish()
            activity.overridePendingTransition(enterAnim, exitAnim)
        }
    }
}
/**
 * Finish to the activity.
 *
 * @param activity      The activity.
 * @param isIncludeSelf True to include the activity, false otherwise.
 * @param isLoadAnim    True to use animation for the outgoing activity, false otherwise.
 */
/**
 * Finish to the activity.
 *
 * @param activity      The activity.
 * @param isIncludeSelf True to include the activity, false otherwise.
 */
@JvmOverloads
fun finishToActivity(
    activity: Activity,
    isIncludeSelf: Boolean,
    isLoadAnim: Boolean = false
): Boolean {
    val activities: List<Activity> = UtilsActivityLifecycleImpl.activityList
    for (act in activities) {
        if (act == activity) {
            if (isIncludeSelf) {
                finishActivity(act, isLoadAnim)
            }
            return true
        }
        finishActivity(act, isLoadAnim)
    }
    return false
}

/**
 * Finish to the activity.
 *
 * @param activity      The activity.
 * @param isIncludeSelf True to include the activity, false otherwise.
 * @param enterAnim     A resource ID of the animation resource to use for the
 * incoming activity.
 * @param exitAnim      A resource ID of the animation resource to use for the
 * outgoing activity.
 */
fun finishToActivity(
    activity: Activity,
    isIncludeSelf: Boolean,
    @AnimRes enterAnim: Int,
    @AnimRes exitAnim: Int
): Boolean {
    val activities: List<Activity> = UtilsActivityLifecycleImpl.activityList
    for (act in activities) {
        if (act == activity) {
            if (isIncludeSelf) {
                finishActivity(act, enterAnim, exitAnim)
            }
            return true
        }
        finishActivity(act, enterAnim, exitAnim)
    }
    return false
}
/**
 * Finish to the activity.
 *
 * @param clz           The activity class.
 * @param isIncludeSelf True to include the activity, false otherwise.
 * @param isLoadAnim    True to use animation for the outgoing activity, false otherwise.
 */
/**
 * Finish to the activity.
 *
 * @param clz           The activity class.
 * @param isIncludeSelf True to include the activity, false otherwise.
 */
@JvmOverloads
fun finishToActivity(
    clz: Class<out Activity?>,
    isIncludeSelf: Boolean,
    isLoadAnim: Boolean = false
): Boolean {
    val activities: List<Activity> = UtilsActivityLifecycleImpl.activityList
    for (act in activities) {
        if (act.javaClass == clz) {
            if (isIncludeSelf) {
                finishActivity(act, isLoadAnim)
            }
            return true
        }
        finishActivity(act, isLoadAnim)
    }
    return false
}

/**
 * Finish to the activity.
 *
 * @param clz           The activity class.
 * @param isIncludeSelf True to include the activity, false otherwise.
 * @param enterAnim     A resource ID of the animation resource to use for the
 * incoming activity.
 * @param exitAnim      A resource ID of the animation resource to use for the
 * outgoing activity.
 */
fun finishToActivity(
    clz: Class<out Activity?>,
    isIncludeSelf: Boolean,
    @AnimRes enterAnim: Int,
    @AnimRes exitAnim: Int
): Boolean {
    val activities: List<Activity> = UtilsActivityLifecycleImpl.activityList
    for (act in activities) {
        if (act.javaClass == clz) {
            if (isIncludeSelf) {
                finishActivity(act, enterAnim, exitAnim)
            }
            return true
        }
        finishActivity(act, enterAnim, exitAnim)
    }
    return false
}
/**
 * Finish the activities whose type not equals the activity class.
 *
 * @param clz        The activity class.
 * @param isLoadAnim True to use animation for the outgoing activity, false otherwise.
 */
/**
 * Finish the activities whose type not equals the activity class.
 *
 * @param clz The activity class.
 */
@JvmOverloads
fun finishOtherActivities(
    clz: Class<out Activity?>,
    isLoadAnim: Boolean = false
) {
    val activities: List<Activity> = UtilsActivityLifecycleImpl.activityList
    for (act in activities) {
        if (act.javaClass != clz) {
            finishActivity(act, isLoadAnim)
        }
    }
}

/**
 * Finish the activities whose type not equals the activity class.
 *
 * @param clz       The activity class.
 * @param enterAnim A resource ID of the animation resource to use for the
 * incoming activity.
 * @param exitAnim  A resource ID of the animation resource to use for the
 * outgoing activity.
 */
fun finishOtherActivities(
    clz: Class<out Activity?>,
    @AnimRes enterAnim: Int,
    @AnimRes exitAnim: Int
) {
    val activities: List<Activity> = UtilsActivityLifecycleImpl.activityList
    for (act in activities) {
        if (act.javaClass != clz) {
            finishActivity(act, enterAnim, exitAnim)
        }
    }
}
/**
 * Finish all of activities.
 *
 * @param isLoadAnim True to use animation for the outgoing activity, false otherwise.
 */
/**
 * Finish all of activities.
 */
@JvmOverloads
fun finishAllActivities(isLoadAnim: Boolean = false) {
    val activityList: List<Activity> = UtilsActivityLifecycleImpl.activityList
    for (act in activityList) {
        // sActivityList remove the index activity at onActivityDestroyed
        act.finish()
        if (!isLoadAnim) {
            act.overridePendingTransition(0, 0)
        }
    }
}

/**
 * Finish all of activities.
 *
 * @param enterAnim A resource ID of the animation resource to use for the
 * incoming activity.
 * @param exitAnim  A resource ID of the animation resource to use for the
 * outgoing activity.
 */
fun finishAllActivities(
    @AnimRes enterAnim: Int,
    @AnimRes exitAnim: Int
) {
    val activityList: List<Activity> = UtilsActivityLifecycleImpl.activityList
    for (act in activityList) {
        // sActivityList remove the index activity at onActivityDestroyed
        act.finish()
        act.overridePendingTransition(enterAnim, exitAnim)
    }
}
/**
 * Finish all of activities except the newest activity.
 *
 * @param isLoadAnim True to use animation for the outgoing activity, false otherwise.
 */
/**
 * Finish all of activities except the newest activity.
 */
@JvmOverloads
fun finishAllActivitiesExceptNewest(isLoadAnim: Boolean = false) {
    val activities: List<Activity> = UtilsActivityLifecycleImpl.activityList
    for (i in 1 until activities.size) {
        finishActivity(activities[i], isLoadAnim)
    }
}

/**
 * Finish all of activities except the newest activity.
 *
 * @param enterAnim A resource ID of the animation resource to use for the
 * incoming activity.
 * @param exitAnim  A resource ID of the animation resource to use for the
 * outgoing activity.
 */
fun finishAllActivitiesExceptNewest(
    @AnimRes enterAnim: Int,
    @AnimRes exitAnim: Int
) {
    val activities: List<Activity> = UtilsActivityLifecycleImpl.activityList
    for (i in 1 until activities.size) {
        finishActivity(activities[i], enterAnim, exitAnim)
    }
}

/**
 * Return the icon of activity.
 *
 * @param activity The activity.
 * @return the icon of activity
 */
fun getActivityIcon(activity: Activity): Drawable? {
    return getActivityIcon(activity.componentName)
}

/**
 * Return the icon of activity.
 *
 * @param clz The activity class.
 * @return the icon of activity
 */
fun getActivityIcon(clz: Class<out Activity?>): Drawable? {
    return getActivityIcon(ComponentName(app!!, clz))
}

/**
 * Return the icon of activity.
 *
 * @param activityName The name of activity.
 * @return the icon of activity
 */
fun getActivityIcon(activityName: ComponentName): Drawable? {
    val pm = app!!.packageManager
    return try {
        pm.getActivityIcon(activityName)
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        null
    }
}

/**
 * Return the logo of activity.
 *
 * @param activity The activity.
 * @return the logo of activity
 */
fun getActivityLogo(activity: Activity): Drawable? {
    return getActivityLogo(activity.componentName)
}

/**
 * Return the logo of activity.
 *
 * @param clz The activity class.
 * @return the logo of activity
 */
fun getActivityLogo(clz: Class<out Activity?>): Drawable? {
    return getActivityLogo(ComponentName(app!!, clz))
}

/**
 * Return the logo of activity.
 *
 * @param activityName The name of activity.
 * @return the logo of activity
 */
fun getActivityLogo(activityName: ComponentName): Drawable? {
    val pm = app!!.packageManager
    return try {
        pm.getActivityLogo(activityName)
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        null
    }
}

private fun startActivity(
    context: Context,
    extras: Bundle?,
    pkg: String,
    cls: String,
    options: Bundle?
) {
    val intent = Intent()
    if (extras != null) intent.putExtras(extras)
    intent.component = ComponentName(pkg, cls)
    startActivity(intent, context, options)
}

private fun startActivity(
    intent: Intent,
    context: Context,
    options: Bundle?
): Boolean {
    if (!isIntentAvailable(intent)) {
        Log.e("ActivityUtils", "intent is unavailable")
        return false
    }
    if (context !is Activity) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    if (options != null) {
        context.startActivity(intent, options)
    } else {
        context.startActivity(intent)
    }
    return true
}

private fun isIntentAvailable(intent: Intent): Boolean {
    return app!!.getPackageManager()
        .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        .size > 0
}

private fun startActivityForResult(
    activity: Activity,
    extras: Bundle?,
    pkg: String,
    cls: String,
    requestCode: Int,
    options: Bundle?
): Boolean {
    val intent = Intent()
    if (extras != null) intent.putExtras(extras)
    intent.component = ComponentName(pkg, cls)
    return startActivityForResult(intent, activity, requestCode, options)
}

private fun startActivityForResult(
    intent: Intent,
    activity: Activity,
    requestCode: Int,
    options: Bundle?
): Boolean {
    if (!isIntentAvailable(intent)) {
        Log.e("ActivityUtils", "intent is unavailable")
        return false
    }
    if (options != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
        activity.startActivityForResult(intent, requestCode, options)
    } else {
        activity.startActivityForResult(intent, requestCode)
    }
    return true
}

private fun startActivities(
    intents: Array<Intent>,
    context: Context,
    options: Bundle?
) {
    if (context !is Activity) {
        for (intent in intents) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
    if (options != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
        context.startActivities(intents, options)
    } else {
        context.startActivities(intents)
    }
}

private fun startActivityForResult(
    fragment: Fragment,
    extras: Bundle?,
    pkg: String,
    cls: String,
    requestCode: Int,
    options: Bundle?
): Boolean {
    val intent = Intent()
    if (extras != null) intent.putExtras(extras)
    intent.component = ComponentName(pkg, cls)
    return startActivityForResult(intent, fragment, requestCode, options)
}

private fun startActivityForResult(
    intent: Intent,
    fragment: Fragment,
    requestCode: Int,
    options: Bundle?
): Boolean {
    if (!isIntentAvailable(intent)) {
        Log.e("ActivityUtils", "intent is unavailable")
        return false
    }
    if (fragment.activity == null) {
        Log.e("ActivityUtils", "Fragment $fragment not attached to Activity")
        return false
    }
    if (options != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
        fragment.startActivityForResult(intent, requestCode, options)
    } else {
        fragment.startActivityForResult(intent, requestCode)
    }
    return true
}

private fun getOptionsBundle(
    fragment: Fragment,
    enterAnim: Int,
    exitAnim: Int
): Bundle? {
    val activity = fragment.activity ?: return null
    return ActivityOptionsCompat.makeCustomAnimation(activity, enterAnim, exitAnim).toBundle()
}

private fun getOptionsBundle(
    context: Context,
    enterAnim: Int,
    exitAnim: Int
): Bundle? {
    return ActivityOptionsCompat.makeCustomAnimation(context, enterAnim, exitAnim).toBundle()
}

private fun getOptionsBundle(
    fragment: Fragment,
    sharedElements: Array<out View>
): Bundle? {
    return fragment.activity?.let { it:FragmentActivity -> getOptionsBundle(it as Activity, sharedElements) }?:null
}

fun getOptionsBundle(
    activity: Activity,
    sharedElements: Array<out View>
): Bundle? {
    val len = sharedElements.size
    if (len <= 0) return null
    val pairs: Array<Pair<View, String>?> = arrayOfNulls(len)
    for (i in 0 until len) {
        pairs[i] = Pair.create(
            sharedElements[i], sharedElements[i].transitionName
        )
    }
    return ActivityOptionsCompat.makeSceneTransitionAnimation(activity, *pairs).toBundle()
}