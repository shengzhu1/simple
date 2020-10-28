package top.androider.util

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.text.TextUtils
import android.util.Log

/**
 * <pre>
 * author: Blankj
 * blog  : http://blankj.com
 * time  : 2019/05/20
 * desc  : utils about app store
</pre> *
 */
private const val TAG = "AppStoreUtils"
private const val GOOGLE_PLAY_APP_STORE_PACKAGE_NAME = "com.android.vending"

/**
 * 获取跳转到应用商店的 Intent
 *
 * 优先跳转到手机自带的应用市场
 *
 * @param packageName              包名
 * @param isIncludeGooglePlayStore 是否包括 Google Play 商店
 * @return 跳转到应用商店的 Intent
 */
fun getAppStoreIntent(packageName: String = Utils.app!!.packageName, isIncludeGooglePlayStore: Boolean = false): Intent? {
    val intent = Intent().apply{
        data = Uri.parse("market://details?id=$packageName")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    val resolveInfos = Utils.app!!.packageManager
        .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
    if (resolveInfos.isEmpty()) {
        Log.e(TAG, "No app store!")
        return null
    }
    var googleIntent: Intent? = null
    for (resolveInfo in resolveInfos) {
        val pkgName = resolveInfo.activityInfo.packageName
        if (GOOGLE_PLAY_APP_STORE_PACKAGE_NAME != pkgName) {
            if (isAppSystem(pkgName)) {
                intent.setPackage(pkgName)
                return intent
            }
        } else {
            intent.setPackage(GOOGLE_PLAY_APP_STORE_PACKAGE_NAME)
            googleIntent = intent
        }
    }
    if (isIncludeGooglePlayStore && googleIntent != null) {
        return googleIntent
    }
    intent.setPackage(resolveInfos[0].activityInfo.packageName)
    return intent
}

private fun go2NormalAppStore(packageName: String): Boolean {
    val intent = getNormalAppStoreIntent(packageName) ?: return false
    intent.data = Uri.parse("market://details?id=$packageName")
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    Utils.app!!.startActivity(intent)
    return true
}

private fun getNormalAppStoreIntent(packageName: String = Utils.app!!.packageName): Intent?{
    return Intent().apply{
        data = Uri.parse("market://details?id=$packageName")
    }.takeIf {
        getAvailableIntentSize(it) > 0
    }

}

private fun getAvailableIntentSize(intent: Intent): Int {
    return Utils.app!!.packageManager
        .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        .size
}

private fun isAppSystem(packageName: String?): Boolean {
    return if (TextUtils.isEmpty(packageName)) false else try {
        val flag = Utils.app!!.packageManager?.getApplicationInfo(packageName,0)?.flags?:0
        flag and ApplicationInfo.FLAG_SYSTEM != 0
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        false
    }
}