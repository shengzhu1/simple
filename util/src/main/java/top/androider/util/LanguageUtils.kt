package top.androider.util

import android.app.Activity
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.text.TextUtils
import android.util.Log
import top.androider.util.Utils.Companion.app
import java.util.*

/**
 * <pre>
 * author: Blankj
 * blog  : http://blankj.com
 * time  : 2019/06/20
 * desc  : utils about language
</pre> *
 */
private const val KEY_LOCALE = "KEY_LOCALE"
private const val VALUE_FOLLOW_SYSTEM = "VALUE_FOLLOW_SYSTEM"

/**
 * Apply the system language.
 * It will not restart Activity. u can put it in ur [Activity.onCreate].
 */
fun applySystemLanguage() {
    if (isAppliedSystemLanguage) return
    applyLanguage(Resources.getSystem().configuration.locale, "", true, false)
}

/**
 * Apply the system language.
 *
 * @param activityClz The class of activity will be started after apply system language.
 */
fun applySystemLanguage(activityClz: Class<out Activity?>?) {
    applyLanguage(Resources.getSystem().configuration.locale, activityClz, true, true)
}

/**
 * Apply the system language.
 *
 * @param activityClassName The full class name of activity will be started after apply system language.
 */
fun applySystemLanguage(activityClassName: String) {
    applyLanguage(Resources.getSystem().configuration.locale, activityClassName, true, true)
}

/**
 * Apply the language.
 * It will not restart Activity. u can put it in ur [Activity.onCreate].
 *
 * @param locale The language of locale.
 */
fun applyLanguage(locale: Locale) {
    if (isAppliedLanguage(locale)) return
    applyLanguage(locale, "", false, false)
}

/**
 * Apply the language.
 *
 * @param locale      The language of locale.
 * @param activityClz The class of activity will be started after apply system language.
 * It will start the launcher activity if the class is null.
 */
fun applyLanguage(
    locale: Locale,
    activityClz: Class<out Activity?>?
) {
    applyLanguage(locale, activityClz, false, true)
}

/**
 * Apply the language.
 *
 * @param locale            The language of locale.
 * @param activityClassName The class of activity will be started after apply system language.
 * It will start the launcher activity if the class name is null.
 */
fun applyLanguage(
    locale: Locale,
    activityClassName: String
) {
    applyLanguage(locale, activityClassName, false, true)
}

private fun applyLanguage(
    locale: Locale,
    activityClz: Class<out Activity?>?,
    isFollowSystem: Boolean,
    isNeedStartActivity: Boolean
) {
    if (activityClz == null) {
        applyLanguage(locale, "", isFollowSystem, isNeedStartActivity)
        return
    }
    applyLanguage(locale, activityClz.name, isFollowSystem, isNeedStartActivity)
}

private fun applyLanguage(
    locale: Locale,
    activityClassName: String,
    isFollowSystem: Boolean,
    isNeedStartActivity: Boolean
) {
    if (isFollowSystem) {
        spUtils4Utils.put(KEY_LOCALE, VALUE_FOLLOW_SYSTEM)
    } else {
        spUtils4Utils.put(KEY_LOCALE, locale2String(locale))
    }
    updateLanguage(app, locale)
    if (isNeedStartActivity) {
        val intent = Intent()
        val realActivityClassName =
            if (TextUtils.isEmpty(activityClassName)) launcherActivity else activityClassName
        intent.component = ComponentName(app!!, realActivityClassName)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        app!!.startActivity(intent)
    }
}

/**
 * Return whether applied the system language by [LanguageUtils].
 *
 * @return `true`: yes<br></br>`false`: no
 */
val isAppliedSystemLanguage: Boolean
get() = VALUE_FOLLOW_SYSTEM == spUtils4Utils.getString(KEY_LOCALE)

/**
 * Return whether applied the language by [LanguageUtils].
 *
 * @return `true`: yes<br></br>`false`: no
 */
val isAppliedLanguage: Boolean
get() = !TextUtils.isEmpty(spUtils4Utils.getString(KEY_LOCALE))

/**
 * Return whether applied the language by [LanguageUtils].
 *
 * @param locale The locale.
 * @return `true`: yes<br></br>`false`: no
 */
fun isAppliedLanguage(locale: Locale): Boolean {
    val spLocale = spUtils4Utils.getString(KEY_LOCALE)
    if (TextUtils.isEmpty(spLocale)) {
        return false
    }
    if (VALUE_FOLLOW_SYSTEM == spLocale) {
        return false
    }
    val settingLocale = string2Locale(spLocale) ?: return false
    return isSameLocale(settingLocale, locale)
}

/**
 * Return the locale.
 *
 * @return the locale
 */
val currentLocale: Locale
get() = app!!.resources.configuration.locale

fun applyLanguage(activity: Activity) {
    val spLocale = spUtils4Utils.getString(KEY_LOCALE)
    if (TextUtils.isEmpty(spLocale)) {
        return
    }
    if (VALUE_FOLLOW_SYSTEM == spLocale) {
        val sysLocale = Resources.getSystem().configuration.locale
        updateLanguage(app, sysLocale)
        updateLanguage(activity, sysLocale)
        return
    }
    val settingLocale = string2Locale(spLocale) ?: return
    updateLanguage(app, settingLocale)
    updateLanguage(activity, settingLocale)
}

private fun locale2String(locale: Locale): String {
    val localLanguage = locale.language
    val localCountry = locale.country
    return "$localLanguage$$localCountry"
}

private fun string2Locale(str: String?): Locale? {
    val language_country = str!!.split("\\$").toTypedArray()
    if (language_country.size != 2) {
        Log.e("LanguageUtils", "The string of $str is not in the correct format.")
        return null
    }
    return Locale(language_country[0], language_country[1])
}

private fun updateLanguage(context: Context?, locale: Locale) {
    val resources = context!!.resources
    val config = resources.configuration
    val contextLocale = config.locale
    if (isSameLocale(contextLocale, locale)) {
        return
    }
    val dm = resources.displayMetrics
    config.setLocale(locale)
    if (context is Application) {
        val newContext = context.createConfigurationContext(config)
        try {
            val mBaseField = ContextWrapper::class.java.getDeclaredField("mBase")
            mBaseField.isAccessible = true
            mBaseField[context] = newContext
        } catch (ignored: Exception) { /**/
        }
    }
    resources.updateConfiguration(config, dm)
}

private fun isSameLocale(l0: Locale, l1: Locale): Boolean {
    return l1.language.equals(l0.language) && l1.country.equals(l0.country)
}