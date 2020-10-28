package top.androider.util

import android.app.Activity
import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Process
import android.util.Log
import top.androider.util.Utils.Companion.app
import top.androider.util.Utils.OnAppStatusChangedListener
import java.io.File
import java.util.*

/**
 * <pre>
 * author: Blankj
 * blog  : http://blankj.com
 * time  : 2016/08/02
 * desc  : utils about app
</pre> *
 */
object AppUtils {

    /**
     * Install the app.
     *
     * Target APIs greater than 25 must hold
     * `<uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />`
     *
     * @param filePath The path of file.
     */
    fun installApp(filePath: String?) {
        installApp(FileUtils.getFileByPath(filePath))
    }

    /**
     * Install the app.
     *
     * Target APIs greater than 25 must hold
     * `<uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />`
     *
     * @param file The file.
     */
    fun installApp(file: File?) {
        val installAppIntent: Intent = IntentUtils.getInstallAppIntent(file) ?: return
        app!!.startActivity(installAppIntent)
    }

    /**
     * Install the app.
     *
     * Target APIs greater than 25 must hold
     * `<uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />`
     *
     * @param uri The uri.
     */
    fun installApp(uri: Uri?) {
        val installAppIntent: Intent = IntentUtils.getInstallAppIntent(uri) ?: return
        app!!.startActivity(installAppIntent)
    }

    /**
     * Uninstall the app.
     *
     * Target APIs greater than 25 must hold
     * Must hold `<uses-permission android:name="android.permission.REQUEST_DELETE_PACKAGES" />`
     *
     * @param packageName The name of the package.
     */
    fun uninstallApp(packageName: String?) {
        if (isSpace(packageName)) return
        app!!.startActivity(IntentUtils.getUninstallAppIntent(packageName!!))
    }

    /**
     * Return whether the app is installed.
     *
     * @param pkgName The name of the package.
     * @return `true`: yes<br></br>`false`: no
     */
    fun isAppInstalled(pkgName: String?): Boolean {
        if (isSpace(pkgName)) return false
        val pm = app!!.packageManager
        return try {
            pm.getApplicationInfo(pkgName, 0).enabled
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    /**
     * Return whether the application with root permission.
     *
     * @return `true`: yes<br></br>`false`: no
     */
    val isAppRoot: Boolean
        get() {
            val result: CommandResult = "echo root".execCmd(true)
            return result.result === 0
        }

    /**
     * Return whether it is a debug application.
     *
     * @return `true`: yes<br></br>`false`: no
     */
    val isAppDebug: Boolean
        get() = isAppDebug(app!!.packageName)

    /**
     * Return whether it is a debug application.
     *
     * @param packageName The name of the package.
     * @return `true`: yes<br></br>`false`: no
     */
    fun isAppDebug(packageName: String? = app!!.packageName): Boolean {
        if (isSpace(packageName)) return false
        val ai = app!!.applicationInfo
        return ai != null && ai.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
    }

    /**
     * Return whether it is a system application.
     *
     * @return `true`: yes<br></br>`false`: no
     */
    val isAppSystem: Boolean
        get() = isAppSystem(app!!.packageName)

    /**
     * Return whether it is a system application.
     *
     * @param packageName The name of the package.
     * @return `true`: yes<br></br>`false`: no
     */
    fun isAppSystem(packageName: String?): Boolean {
        return if (isSpace(packageName)) false else try {
            val pm = app!!.packageManager
            val ai = pm.getApplicationInfo(packageName, 0)
            ai != null && ai.flags and ApplicationInfo.FLAG_SYSTEM != 0
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Return whether application is foreground.
     *
     * @return `true`: yes<br></br>`false`: no
     */
    val isAppForeground: Boolean
        get() {
            val am = app!!.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                ?: return false
            val info = am.runningAppProcesses
            if (info == null || info.size == 0) return false
            for (aInfo in info) {
                if (aInfo.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    if (aInfo.processName == app!!.packageName) {
                        return true
                    }
                }
            }
            return false
        }

    /**
     * Return whether application is foreground.
     *
     * Target APIs greater than 21 must hold
     * `<uses-permission android:name="android.permission.PACKAGE_USAGE_STATS" />`
     *
     * @param pkgName The name of the package.
     * @return `true`: yes<br></br>`false`: no
     */
    fun isAppForeground(pkgName: String = app!!.packageName): Boolean {
        return !isSpace(pkgName) && pkgName == getForegroundProcessName()
    }

    /**
     * Return whether application is running.
     *
     * @param pkgName The name of the package.
     * @return `true`: yes<br></br>`false`: no
     */
    fun isAppRunning(pkgName: String): Boolean {
        if (isSpace(pkgName)) return false
        val ai = app!!.applicationInfo
        val uid = ai.uid
        val am = app!!.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        if (am != null) {
            val taskInfo = am.getRunningTasks(Int.MAX_VALUE)
            if (taskInfo != null && taskInfo.size > 0) {
                for (aInfo in taskInfo) {
                    if (aInfo.baseActivity != null) {
                        if (pkgName == aInfo.baseActivity!!.packageName) {
                            return true
                        }
                    }
                }
            }
            val serviceInfo = am.getRunningServices(Int.MAX_VALUE)
            if (serviceInfo != null && serviceInfo.size > 0) {
                for (aInfo in serviceInfo) {
                    if (uid == aInfo.uid) {
                        return true
                    }
                }
            }
        }
        return false
    }

    /**
     * Launch the application.
     *
     * @param packageName The name of the package.
     */
    fun launchApp(packageName: String?) {
        if (isSpace(packageName)) return
        val launchAppIntent: Intent? = IntentUtils.getLaunchAppIntent(packageName)
        if (launchAppIntent == null) {
            Log.e("AppUtils", "Didn't exist launcher activity.")
            return
        }
        app!!.startActivity(launchAppIntent)
    }
    /**
     * Relaunch the application.
     *
     * @param isKillProcess True to kill the process, false otherwise.
     */
    @JvmOverloads
    fun relaunchApp(isKillProcess: Boolean = false) {
        val intent: Intent? = IntentUtils.getLaunchAppIntent(app!!.packageName)
        if (intent == null) {
            Log.e("AppUtils", "Didn't exist launcher activity.")
            return
        }
        intent.addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK
                    or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
        )
        app!!.startActivity(intent)
        if (!isKillProcess) return
        Process.killProcess(Process.myPid())
        System.exit(0)
    }
    /**
     * Launch the application's details settings.
     *
     * @param pkgName The name of the package.
     */
    /**
     * Launch the application's details settings.
     */
    @JvmOverloads
    fun launchAppDetailsSettings(pkgName: String = app!!.packageName) {
        if (isSpace(pkgName)) return
        val intent: Intent = IntentUtils.getLaunchAppDetailsSettingsIntent(pkgName, true)
        if (!IntentUtils.isIntentAvailable(intent)) return
        app!!.startActivity(intent)
    }
    /**
     * Launch the application's details settings.
     *
     * @param activity    The activity.
     * @param requestCode The requestCode.
     * @param pkgName     The name of the package.
     */
    @JvmOverloads
    fun launchAppDetailsSettings(
        activity: Activity?,
        requestCode: Int,
        pkgName: String? = app!!.packageName
    ) {
        if (activity == null || isSpace(pkgName)) return
        val intent: Intent = IntentUtils.getLaunchAppDetailsSettingsIntent(pkgName!!)
        if (!IntentUtils.isIntentAvailable(intent)) return
        activity.startActivityForResult(intent, requestCode)
    }

    /**
     * Exit the application.
     */
    fun exitApp() {
        finishAllActivities()
        System.exit(0)
    }

    /**
     * Return the application's icon.
     *
     * @return the application's icon
     */
    val appIcon: Drawable?
        get() = getAppIcon(app!!.packageName)

    /**
     * Return the application's icon.
     *
     * @param packageName The name of the package.
     * @return the application's icon
     */
    fun getAppIcon(packageName: String?): Drawable? {
        return if (isSpace(packageName)) null else try {
            val pm = app!!.packageManager
            val pi = pm.getPackageInfo(packageName, 0)
            pi?.applicationInfo?.loadIcon(pm)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Return the application's icon resource identifier.
     *
     * @return the application's icon resource identifier
     */
    val appIconId: Int
        get() = getAppIconId(app!!.packageName)

    /**
     * Return the application's icon resource identifier.
     *
     * @param packageName The name of the package.
     * @return the application's icon resource identifier
     */
    fun getAppIconId(packageName: String?): Int {
        return if (isSpace(packageName)) 0 else try {
            val pm = app!!.packageManager
            val pi = pm.getPackageInfo(packageName, 0)
            pi?.applicationInfo?.icon ?: 0
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            0
        }
    }

    /**
     * Return the application's package name.
     *
     * @return the application's package name
     */
    val appPackageName: String
        get() = app!!.packageName

    /**
     * Return the application's name.
     *
     * @return the application's name
     */
    val appName: String?
        get() = getAppName(app!!.packageName)

    /**
     * Return the application's name.
     *
     * @param packageName The name of the package.
     * @return the application's name
     */
    fun getAppName(packageName: String?): String? {
        return if (isSpace(packageName)) "" else try {
            val pm = app!!.packageManager
            val pi = pm.getPackageInfo(packageName, 0)
            pi?.applicationInfo?.loadLabel(pm)?.toString()
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            ""
        }
    }

    /**
     * Return the application's path.
     *
     * @return the application's path
     */
    val appPath: String?
        get() = getAppPath(app!!.packageName)

    /**
     * Return the application's path.
     *
     * @param packageName The name of the package.
     * @return the application's path
     */
    fun getAppPath(packageName: String?): String? {
        return if (isSpace(packageName)) "" else try {
            val pm = app!!.packageManager
            val pi = pm.getPackageInfo(packageName, 0)
            pi?.applicationInfo?.sourceDir
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            ""
        }
    }

    /**
     * Return the application's version name.
     *
     * @return the application's version name
     */
    val appVersionName: String?
        get() = getAppVersionName(app!!.packageName)

    /**
     * Return the application's version name.
     *
     * @param packageName The name of the package.
     * @return the application's version name
     */
    fun getAppVersionName(packageName: String?): String? {
        return if (isSpace(packageName)) "" else try {
            val pm = app!!.packageManager
            val pi = pm.getPackageInfo(packageName, 0)
            pi?.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            ""
        }
    }

    /**
     * Return the application's version code.
     *
     * @return the application's version code
     */
    val appVersionCode: Int
        get() = getAppVersionCode(app!!.packageName)

    /**
     * Return the application's version code.
     *
     * @param packageName The name of the package.
     * @return the application's version code
     */
    fun getAppVersionCode(packageName: String?): Int {
        return if (isSpace(packageName)) -1 else try {
            val pm = app!!.packageManager
            val pi = pm.getPackageInfo(packageName, 0)
            pi?.versionCode ?: -1
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            -1
        }
    }

    /**
     * Return the application's signature.
     *
     * @return the application's signature
     */
    val appSignature: Array<Signature>?
        get() = getAppSignature(app!!.packageName)

    /**
     * Return the application's signature.
     *
     * @param packageName The name of the package.
     * @return the application's signature
     */
    fun getAppSignature(packageName: String?): Array<Signature>? {
        return if (isSpace(packageName)) null else try {
            val pm = app!!.packageManager
            val pi: PackageInfo?
            pi = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pm.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            } else {
                pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            }
            pi?.signatures
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Return the application's signature.
     *
     * @param file The file.
     * @return the application's signature
     */
    fun getAppSignature(file: File?): Array<Signature>? {
        if (file == null) return null
        val pm = app!!.packageManager
        val pi: PackageInfo?
        pi = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            pm.getPackageArchiveInfo(file.absolutePath, PackageManager.GET_SIGNING_CERTIFICATES)
        } else {
            pm.getPackageArchiveInfo(file.absolutePath, PackageManager.GET_SIGNATURES)
        }
        return pi?.signatures
    }

    /**
     * Return the application's signature for SHA1 value.
     *
     * @return the application's signature for SHA1 value
     */
    val appSignatureSHA1: String
        get() = getAppSignatureSHA1(app!!.packageName)

    /**
     * Return the application's signature for SHA1 value.
     *
     * @param packageName The name of the package.
     * @return the application's signature for SHA1 value
     */
    fun getAppSignatureSHA1(packageName: String): String {
        return getAppSignatureHash(packageName, "SHA1")
    }

    /**
     * Return the application's signature for SHA256 value.
     *
     * @return the application's signature for SHA256 value
     */
    val appSignatureSHA256: String
        get() = getAppSignatureSHA256(app!!.packageName)

    /**
     * Return the application's signature for SHA256 value.
     *
     * @param packageName The name of the package.
     * @return the application's signature for SHA256 value
     */
    fun getAppSignatureSHA256(packageName: String): String {
        return getAppSignatureHash(packageName, "SHA256")
    }

    /**
     * Return the application's signature for MD5 value.
     *
     * @return the application's signature for MD5 value
     */
    val appSignatureMD5: String
        get() = getAppSignatureMD5(app!!.packageName)

    /**
     * Return the application's signature for MD5 value.
     *
     * @param packageName The name of the package.
     * @return the application's signature for MD5 value
     */
    fun getAppSignatureMD5(packageName: String): String {
        return getAppSignatureHash(packageName, "MD5")
    }

    /**
     * Return the application's user-ID.
     *
     * @return the application's signature for MD5 value
     */
    val appUid: Int
        get() = getAppUid(app!!.packageName)

    /**
     * Return the application's user-ID.
     *
     * @param pkgName The name of the package.
     * @return the application's signature for MD5 value
     */
    fun getAppUid(pkgName: String?): Int {
        try {
            return app!!.packageManager.getApplicationInfo(pkgName, 0).uid
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return -1
    }

    private fun getAppSignatureHash(packageName: String, algorithm: String): String {
        if (isSpace(packageName)) return ""
        val signature = getAppSignature(packageName)
        val res =  if (signature == null || signature.size <= 0) "" else ConvertUtils.bytes2HexString(
            EncryptUtils.hashTemplate(signature[0].toByteArray(), algorithm)
        )
        return (res as java.lang.String).replaceAll("(?<=[0-9A-F]{2})[0-9A-F]{2}", ":$0")
    }

    /**
     * Return the application's information.
     *
     *  * name of package
     *  * icon
     *  * name
     *  * path of package
     *  * version name
     *  * version code
     *  * is system
     *
     *
     * @return the application's information
     */
    val appInfo: AppInfo?
        get() = getAppInfo(app!!.packageName)

    /**
     * Return the application's information.
     *
     *  * name of package
     *  * icon
     *  * name
     *  * path of package
     *  * version name
     *  * version code
     *  * is system
     *
     *
     * @param packageName The name of the package.
     * @return the application's information
     */
    fun getAppInfo(packageName: String?): AppInfo? {
        return try {
            val pm = app!!.packageManager ?: return null
            getBean(pm, pm.getPackageInfo(packageName, 0))
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Return the applications' information.
     *
     * @return the applications' information
     */
    val appsInfo: List<AppInfo>
        get() {
            val list: MutableList<AppInfo> = ArrayList()
            val pm = app!!.packageManager ?: return list
            val installedPackages = pm.getInstalledPackages(0)
            for (pi in installedPackages) {
                val ai = getBean(pm, pi) ?: continue
                list.add(ai)
            }
            return list
        }

    /**
     * Return the application's package information.
     *
     * @return the application's package information
     */
    fun getApkInfo(apkFile: File?): AppInfo? {
        return if (apkFile == null || !apkFile.isFile || !apkFile.exists()) null else getApkInfo(
            apkFile.absolutePath
        )
    }

    /**
     * Return the application's package information.
     *
     * @return the application's package information
     */
    fun getApkInfo(apkFilePath: String?): AppInfo? {
        if (isSpace(apkFilePath)) return null
        val pm = app!!.packageManager ?: return null
        val pi = pm.getPackageArchiveInfo(apkFilePath, 0) ?: return null
        val appInfo = pi.applicationInfo
        appInfo.sourceDir = apkFilePath
        appInfo.publicSourceDir = apkFilePath
        return getBean(pm, pi)
    }

    private fun getBean(pm: PackageManager, pi: PackageInfo?): AppInfo? {
        if (pi == null) return null
        val ai = pi.applicationInfo
        val packageName = pi.packageName
        val name = ai.loadLabel(pm).toString()
        val icon = ai.loadIcon(pm)
        val packagePath = ai.sourceDir
        val versionName = pi.versionName
        val versionCode = pi.versionCode
        val isSystem = ApplicationInfo.FLAG_SYSTEM and ai.flags != 0
        return AppInfo(packageName, name, icon, packagePath, versionName, versionCode, isSystem)
    }

    /**
     * The application's information.
     */
    class AppInfo(
        packageName: String?, name: String?, icon: Drawable?, packagePath: String?,
        versionName: String?, versionCode: Int, isSystem: Boolean
    ) {
        var packageName: String? = null
        var name: String? = null
        var icon: Drawable? = null
        var packagePath: String? = null
        var versionName: String? = null
        var versionCode = 0
        var isSystem = false
        override fun toString(): String {
            return """{
    pkg name: $packageName
    app icon: $icon
    app name: $name
    app path: $packagePath
    app v name: $versionName
    app v code: $versionCode
    is system: $isSystem
}"""
        }

        init {
            this.name = name
            this.icon = icon
            this.packageName = packageName
            this.packagePath = packagePath
            this.versionName = versionName
            this.versionCode = versionCode
            this.isSystem = isSystem
        }
    }
}