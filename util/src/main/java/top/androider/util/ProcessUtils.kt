package top.androider.util

import android.Manifest.permission
import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import androidx.annotation.RequiresPermission
import top.androider.util.Utils.Companion.app
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.*


fun getActivityManager() = app!!.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
/**
 * Return the foreground process name.
 *
 * Target APIs greater than 21 must hold
 * `<uses-permission android:name="android.permission.PACKAGE_USAGE_STATS" />`
 *
 * @return the foreground process name
 */
fun getForegroundProcessName(): String? {
        getActivityManager().runningAppProcesses?.forEach {
            aInfo ->
            if (aInfo.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return@getForegroundProcessName aInfo.processName
            }
        }
        val pm = app!!.packageManager
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        val list = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        Log.i("ProcessUtils", list.toString())
        if (list.size <= 0) {
            Log.i("ProcessUtils","getForegroundProcessName: noun of access to usage information.")
            return ""
        }
        try { // Access to usage information.
            val info = pm.getApplicationInfo(app!!.packageName, 0)
            val aom = app!!.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            if (aom.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    info.uid,
                    info.packageName
                ) != AppOpsManager.MODE_ALLOWED
            ) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                app!!.startActivity(intent)
            }
            if (aom.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    info.uid,
                    info.packageName
                ) != AppOpsManager.MODE_ALLOWED
            ) {
                Log.i(
                    "ProcessUtils",
                    "getForegroundProcessName: refuse to device usage stats."
                )
                return ""
            }
            val usageStatsManager = app!!.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            var usageStatsList: List<UsageStats>? = null
            if (usageStatsManager != null) {
                val endTime = System.currentTimeMillis()
                val beginTime = endTime - 86400000 * 7
                usageStatsList = usageStatsManager
                    .queryUsageStats(
                        UsageStatsManager.INTERVAL_BEST,
                        beginTime, endTime
                    )
            }
            if (usageStatsList == null || usageStatsList.isEmpty()) return ""
            var recentStats: UsageStats? = null
            for (usageStats in usageStatsList) {
                if (recentStats == null
                    || usageStats.lastTimeUsed > recentStats.lastTimeUsed
                ) {
                    recentStats = usageStats
                }
            }
            return recentStats?.packageName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return ""
    }

/**
 * Return all background processes.
 *
 * Must hold `<uses-permission android:name="android.permission.KILL_BACKGROUND_PROCESSES" />`
 *
 * @return all background processes
 */
@get:RequiresPermission(permission.KILL_BACKGROUND_PROCESSES)
val allBackgroundProcesses: Set<String>
    get() {
        val am = app!!.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val set = hashSetOf<String>()
        am.runningAppProcesses?.forEach {
            aInfo ->
            set.addAll(aInfo.pkgList)
        }
        return set
    }

/**
 * Kill all background processes.
 *
 * Must hold `<uses-permission android:name="android.permission.KILL_BACKGROUND_PROCESSES" />`
 *
 * @return background processes were killed
 */
@RequiresPermission(permission.KILL_BACKGROUND_PROCESSES)
fun killAllBackgroundProcesses(): Set<String> {
    val am = app!!.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    var info = am.runningAppProcesses
    val set: MutableSet<String> = HashSet()
    am.runningAppProcesses?.forEach {
            aInfo ->
        aInfo.pkgList?.forEach {
            pkg ->
            am.killBackgroundProcesses(pkg)
            set.add(pkg)
        }
    }
    am.runningAppProcesses?.forEach {
            aInfo ->
        aInfo.pkgList?.forEach {
                pkg ->
            set.remove(pkg)
        }
    }
    return set
}

/**
 * Kill background processes.
 *
 * Must hold `<uses-permission android:name="android.permission.KILL_BACKGROUND_PROCESSES" />`
 *
 * @param packageName The name of the package.
 * @return `true`: success<br></br>`false`: fail
 */
@RequiresPermission(permission.KILL_BACKGROUND_PROCESSES)
fun killBackgroundProcesses(packageName: String): Boolean {
    val am = app!!.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    am.runningAppProcesses?.forEach {
            aInfo ->
        aInfo.pkgList?.forEach {
                pkg ->
            if (pkg == packageName)
                am.killBackgroundProcesses(pkg)
        }
    }
    am.runningAppProcesses?.forEach {
            aInfo ->
        aInfo.pkgList?.forEach {
                pkg ->
            if (pkg == packageName) return@killBackgroundProcesses false
        }
    }
    return true
}

/**
 * Return whether app running in the main process.
 *
 * @return `true`: yes<br></br>`false`: no
 */
val isMainProcess = app!!.packageName == currentProcessName

/**
 * Return the name of current process.
 *
 * @return the name of current process
 */

val currentProcessName: String
    get() = currentProcessNameByFile.takeIf { it.isNotEmpty() }?:currentProcessNameByAms.takeIf { it.isNotEmpty() }?:currentProcessNameByReflect

private val currentProcessNameByFile: String
    private get() = try {
        val file = File("/proc/" + Process.myPid() + "/" + "cmdline")
        val mBufferedReader = BufferedReader(FileReader(file))
        val processName = mBufferedReader.readLine().trim { it <= ' ' }
        mBufferedReader.close()
        processName
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }

private val currentProcessNameByAms: String
    private get() {
        return try {
            val am = app!!.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                ?: return ""
            val pid = Process.myPid()
            am.runningAppProcesses?.forEach {
                    aInfo ->
                if (aInfo.pid == pid) {
                    if (aInfo.processName != null) {
                        return aInfo.processName
                    }
                }
            }
            ""
        } catch (e: Exception) {
             ""
        }
    }
private val currentProcessNameByReflect: String
    private get() {
        return try {
            val loadedApkField = app!!.javaClass.getField("mLoadedApk")
            loadedApkField.isAccessible = true
            val loadedApk = loadedApkField[app]
            val activityThreadField =
                loadedApk.javaClass.getDeclaredField("mActivityThread")
            activityThreadField.isAccessible = true
            val activityThread = activityThreadField[loadedApk]
            val getProcessName =
                activityThread.javaClass.getDeclaredMethod("getProcessName")
            getProcessName.invoke(activityThread) as String
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }