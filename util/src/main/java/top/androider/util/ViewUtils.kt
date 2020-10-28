package top.androider.util

import android.os.Build
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import top.androider.util.Utils.Companion.app
import java.util.*

/**
 * <pre>
 * author: Blankj
 * blog  : http://blankj.com
 * time  : 2019/06/18
 * desc  : utils about view
</pre> *
 */
object ViewUtils {
    /**
     * Set the enabled state of this view.
     *
     * @param view    The view.
     * @param enabled True to enabled, false otherwise.
     */
    fun setViewEnabled(view: View?, enabled: Boolean) {
        setViewEnabled(view, enabled, (null as View?)!!)
    }

    /**
     * Set the enabled state of this view.
     *
     * @param view     The view.
     * @param enabled  True to enabled, false otherwise.
     * @param excludes The excludes.
     */
    fun setViewEnabled(view: View?, enabled: Boolean, vararg excludes: View) {
        if (view == null) return
        if (excludes != null) {
            for (exclude in excludes) {
                if (view === exclude) return
            }
        }
        if (view is ViewGroup) {
            val viewGroup = view
            val childCount = viewGroup.childCount
            for (i in 0 until childCount) {
                setViewEnabled(viewGroup.getChildAt(i), enabled, *excludes)
            }
        }
        view.isEnabled = enabled
    }

    /**
     * Return whether horizontal layout direction of views are from Right to Left.
     *
     * @return `true`: yes<br></br>`false`: no
     */
    val isLayoutRtl: Boolean
        get() {
            val primaryLocale: Locale
            primaryLocale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                app!!.resources.configuration.locales[0]
            } else {
                app!!.resources.configuration.locale
            }
            return TextUtils.getLayoutDirectionFromLocale(primaryLocale) == View.LAYOUT_DIRECTION_RTL
        }

    /**
     * Fix the problem of topping the ScrollView nested ListView/GridView/WebView/RecyclerView.
     *
     * @param view The root view inner of ScrollView.
     */
    fun fixScrollViewTopping(view: View) {
        view.isFocusable = false
        var viewGroup: ViewGroup? = null
        if (view is ViewGroup) {
            viewGroup = view
        }
        if (viewGroup == null) {
            return
        }
        var i = 0
        val n = viewGroup.childCount
        while (i < n) {
            val childAt = viewGroup.getChildAt(i)
            childAt.isFocusable = false
            if (childAt is ViewGroup) {
                fixScrollViewTopping(childAt)
            }
            i++
        }
    }
}