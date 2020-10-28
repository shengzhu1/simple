package top.androider.util

import android.Manifest.permission
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.util.Log
import androidx.annotation.RequiresPermission
import top.androider.util.Utils.Companion.app
import java.io.File

/**
 * <pre>
 * author: Blankj
 * blog  : http://blankj.com
 * time  : 2019/06/29
 * desc  :
</pre> *
 */
object DangerousUtils {

    /**
     * Install the app silently.
     *
     * Without root permission must hold
     * `android:sharedUserId="android.uid.shell"` and
     * `<uses-permission android:name="android.permission.INSTALL_PACKAGES" />`
     *
     * @param filePath The path of file.
     * @param params   The params of installation(e.g.,`-r`, `-s`).
     * @return `true`: success<br></br>`false`: fail
     */
    fun installAppSilent(filePath: String, params: String? = null): Boolean {
        return installAppSilent(getFileByPath(filePath), params)
    }
    /**
     * Install the app silently.
     *
     * Without root permission must hold
     * `android:sharedUserId="android.uid.shell"` and
     * `<uses-permission android:name="android.permission.INSTALL_PACKAGES" />`
     *
     * @param file     The file.
     * @param params   The params of installation(e.g.,`-r`, `-s`).
     * @param isRooted True to use root, false otherwise.
     * @return `true`: success<br></br>`false`: fail
     */
    @JvmOverloads
    fun installAppSilent(
        file: File?,
        params: String? = null,
        isRooted: Boolean = isDeviceRooted
    ): Boolean {
        if (!isFileExists(file)) return false
        val filePath = '"'.toString() + file!!.absolutePath + '"'
        val command = ("LD_LIBRARY_PATH=/vendor/lib*:/system/lib* pm install " +
                (if (params == null) "" else "$params ")
                + filePath)
        val commandResult: CommandResult = command.execCmd(isRooted)
        return if (commandResult.successMsg != null
            && commandResult.successMsg.toLowerCase().contains("success")
        ) {
            true
        } else {
            Log.e(
                "AppUtils", "installAppSilent successMsg: " + commandResult.successMsg +
                        ", errorMsg: " + commandResult.errorMsg
            )
            false
        }
    }
    /**
     * Uninstall the app silently.
     *
     * Without root permission must hold
     * `android:sharedUserId="android.uid.shell"` and
     * `<uses-permission android:name="android.permission.DELETE_PACKAGES" />`
     *
     * @param packageName The name of the package.
     * @param isKeepData  Is keep the data.
     * @param isRooted    True to use root, false otherwise.
     * @return `true`: success<br></br>`false`: fail
     */
    @JvmOverloads
    fun uninstallAppSilent(
        packageName: String,
        isKeepData: Boolean = false,
        isRooted: Boolean = isDeviceRooted
    ): Boolean {
        if (isSpace(packageName)) return false
        val command = ("LD_LIBRARY_PATH=/vendor/lib*:/system/lib* pm uninstall "
                + (if (isKeepData) "-k " else "")
                + packageName)
        val commandResult: CommandResult = command.execCmd(isRooted)
        return if (commandResult.successMsg != null
            && commandResult.successMsg.toLowerCase().contains("success")
        ) {
            true
        } else {
            Log.e(
                "AppUtils", "uninstallAppSilent successMsg: " + commandResult.successMsg +
                        ", errorMsg: " + commandResult.errorMsg
            )
            false
        }
    }

    private fun isFileExists(file: File?): Boolean {
        return file != null && file.exists()
    }

    private fun getFileByPath(filePath: String): File? {
        return if (isSpace(filePath)) null else File(filePath)
    }

    private val isDeviceRooted: Boolean
        private get() {
            val su = "su"
            val locations = arrayOf(
                "/system/bin/", "/system/xbin/", "/sbin/", "/system/sd/xbin/",
                "/system/bin/failsafe/", "/data/local/xbin/", "/data/local/bin/", "/data/local/",
                "/system/sbin/", "/usr/bin/", "/vendor/bin/"
            )
            for (location in locations) {
                if (File(location + su).exists()) {
                    return true
                }
            }
            return false
        }
    ///////////////////////////////////////////////////////////////////////////
    // DeviceUtils
    ///////////////////////////////////////////////////////////////////////////
    /**
     * Shutdown the device
     *
     * Requires root permission
     * or hold `android:sharedUserId="android.uid.system"`,
     * `<uses-permission android:name="android.permission.SHUTDOWN" />`
     * in manifest.
     *
     * @return `true`: success<br></br>`false`: fail
     */
    fun shutdown(): Boolean {
        return try {
            val result: CommandResult = "reboot -p".execCmd( true)
            if (result.result === 0) return true
            app!!.startActivity(IntentUtils.shutdownIntent)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Reboot the device.
     *
     * Requires root permission
     * or hold `android:sharedUserId="android.uid.system"` in manifest.
     *
     * @return `true`: success<br></br>`false`: fail
     */
    fun reboot(): Boolean {
        return try {
            val result: CommandResult = "reboot".execCmd( true)
            if (result.result === 0) return true
            val intent = Intent(Intent.ACTION_REBOOT)
            intent.putExtra("nowait", 1)
            intent.putExtra("interval", 1)
            intent.putExtra("window", 0)
            app!!.sendBroadcast(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Reboot the device.
     *
     * Requires root permission
     * or hold `android:sharedUserId="android.uid.system"`,
     * `<uses-permission android:name="android.permission.REBOOT" />`
     *
     * @param reason code to pass to the kernel (e.g., "recovery") to
     * request special boot modes, or null.
     * @return `true`: success<br></br>`false`: fail
     */
    fun reboot(reason: String?): Boolean {
        return try {
            val pm = app!!.getSystemService(Context.POWER_SERVICE) as PowerManager
            pm.reboot(reason)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Reboot the device to recovery.
     *
     * Requires root permission.
     *
     * @return `true`: success<br></br>`false`: fail
     */
    fun reboot2Recovery(): Boolean {
        val result: CommandResult = "reboot recovery".execCmd( true)
        return result.result === 0
    }

    /**
     * Reboot the device to bootloader.
     *
     * Requires root permission.
     *
     * @return `true`: success<br></br>`false`: fail
     */
    fun reboot2Bootloader(): Boolean {
        val result: CommandResult = "reboot bootloader".execCmd(true)
        return result.result === 0
    }

    /**
     * Enable or disable mobile data.
     *
     * Must hold `android:sharedUserId="android.uid.system"`,
     * `<uses-permission android:name="android.permission.MODIFY_PHONE_STATE" />`
     *
     * @param enabled True to enabled, false otherwise.
     * @return `true`: success<br></br>`false`: fail
     */
    @RequiresPermission(permission.MODIFY_PHONE_STATE)
    fun setMobileDataEnabled(enabled: Boolean): Boolean {
        try {
            val tm = app!!.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                ?: return false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                tm.isDataEnabled = enabled
                return true
            }
            val setDataEnabledMethod =
                tm.javaClass.getDeclaredMethod("setDataEnabled", Boolean::class.javaPrimitiveType)
            setDataEnabledMethod.invoke(tm, enabled)
            return true
        } catch (e: Exception) {
            Log.e("NetworkUtils", "setMobileDataEnabled: ", e)
        }
        return false
    }

    /**
     * Send sms silently.
     *
     * Must hold `<uses-permission android:name="android.permission.SEND_SMS" />`
     *
     * @param phoneNumber The phone number.
     * @param content     The content.
     */
    @RequiresPermission(permission.SEND_SMS)
    fun sendSmsSilent(phoneNumber: String?, content: String) {
        if (TextUtils.isEmpty(content)) return
        val sentIntent = PendingIntent.getBroadcast(app, 0, Intent("send"), 0)
        val smsManager = SmsManager.getDefault()
        if (content.length >= 70) {
            val ms: List<String> = smsManager.divideMessage(content)
            for (str in ms) {
                smsManager.sendTextMessage(phoneNumber, null, str, sentIntent, null)
            }
        } else {
            smsManager.sendTextMessage(phoneNumber, null, content, sentIntent, null)
        }
    }
}