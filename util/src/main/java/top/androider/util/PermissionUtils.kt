package top.androider.util

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.util.Pair;
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import top.androider.util.Utils.Companion.app
import top.androider.util.constant.PermissionConstants
import top.androider.util.constant.PermissionConstants.getPermissions


/**
 * create by lushengzhu 2020/10/22
 */
/**
 * Return whether the app can draw on top of other apps.
 *
 * @return `true`: yes<br></br>`false`: no
 */
@RequiresApi(api = Build.VERSION_CODES.M)
fun isGrantedDrawOverlays(): Boolean {
    return Settings.canDrawOverlays(app)
}

/**
 * Return whether *you* have been granted the permissions.
 *
 * @param permissions The permissions.
 * @return `true`: yes<br></br>`false`: no
 */
@SuppressLint("WrongConstant")
fun isGranted(@PermissionConstants.Permission vararg permissions: String): Boolean {
    val res = getRequestAndDeniedPermissions(*permissions)
    if (!res?.first.isEmpty()) {
        return false
    }
    res?.second?.forEach {
            permission ->
        if (!isGranted(permission!!)) {
            return false
        }

    }
    return true
}


private fun getRequestAndDeniedPermissions(vararg permissionsParam: String): Pair<List<String?>?, List<String?>?>? {
    val requestPermissions: MutableList<String?> = ArrayList()
    val deniedPermissions: MutableList<String> = ArrayList()
    val appPermissions: List<String?> = getPermissions(app!!.packageName).asList()
    for (param in permissionsParam) {
        var isIncludeInManifest = false
        val permissions = getPermissions(param)
        for (permission in permissions) {
            if (appPermissions.contains(permission)) {
                requestPermissions.add(permission)
                isIncludeInManifest = true
            }
        }
        if (!isIncludeInManifest) {
            deniedPermissions.add(param)
            Log.e("PermissionUtils", "U should add the permission of $param in manifest.")
        }
    }
    return Pair.create(requestPermissions, deniedPermissions)
}


private fun isGranted(permission: String): Boolean {
    return (Build.VERSION.SDK_INT < Build.VERSION_CODES.M
            || PackageManager.PERMISSION_GRANTED
            == ContextCompat.checkSelfPermission(app!!, permission))
}