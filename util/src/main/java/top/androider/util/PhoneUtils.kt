package top.androider.util

import android.Manifest.permission
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.telephony.TelephonyManager
import android.text.TextUtils
import androidx.annotation.RequiresPermission
import top.androider.util.Utils.Companion.app
import java.lang.reflect.InvocationTargetException

private val telephonyManager= app!!.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager


/**
 * Returns the current phone type.
 *
 * @return the current phone type
 *
 *  * [TelephonyManager.PHONE_TYPE_NONE]
 *  * [TelephonyManager.PHONE_TYPE_GSM]
 *  * [TelephonyManager.PHONE_TYPE_CDMA]
 *  * [TelephonyManager.PHONE_TYPE_SIP]
 *
 */
val phoneType = telephonyManager.phoneType
/**
 * Return whether the device is phone.
 *
 * @return `true`: yes<br></br>`false`: no
 */
val isPhone = phoneType != TelephonyManager.PHONE_TYPE_NONE

/**
 * Return the unique device id.
 *
 * If the version of SDK is greater than 28, it will return an empty string.
 *
 * Must hold `<uses-permission android:name="android.permission.READ_PHONE_STATE" />`
 *
 * @return the unique device id
 */
@get:RequiresPermission(permission.READ_PHONE_STATE)
@get:SuppressLint("HardwareIds")
val deviceId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
    ""
} else{
    val deviceId = telephonyManager.deviceId
    if (!TextUtils.isEmpty(deviceId)) {
        deviceId
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
        telephonyManager.imei?:""
    } else ""

}

/**
 * Return the serial of device.
 *
 * @return the serial of device
 */
@get:RequiresPermission(permission.READ_PHONE_STATE)
@get:SuppressLint("HardwareIds")
val serial: String
    get() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return try {
                Build.getSerial()
            } catch (e: SecurityException) {
                e.printStackTrace()
                ""
            }
        }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Build.getSerial() else Build.SERIAL
    }

/**
 * Return the IMEI.
 *
 * If the version of SDK is greater than 28, it will return an empty string.
 *
 * Must hold `<uses-permission android:name="android.permission.READ_PHONE_STATE" />`
 *
 * @return the IMEI
 */
@get:RequiresPermission(permission.READ_PHONE_STATE)
val iMEI: String? = getImeiOrMeid(true)

/**
 * Return the MEID.
 *
 * If the version of SDK is greater than 28, it will return an empty string.
 *
 * Must hold `<uses-permission android:name="android.permission.READ_PHONE_STATE" />`
 *
 * @return the MEID
 */
@get:RequiresPermission(permission.READ_PHONE_STATE)
val mEID: String? = getImeiOrMeid(false)

@SuppressLint("HardwareIds")
@RequiresPermission(permission.READ_PHONE_STATE)
fun getImeiOrMeid(isImei: Boolean): String? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        return ""
    }
    val tm = telephonyManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        return if (isImei) {
            getMinOne(tm.getImei(0), tm.getImei(1))
        } else {
            getMinOne(tm.getMeid(0), tm.getMeid(1))
        }
    } else {
        val ids = getSystemPropertyByReflect(if (isImei) "ril.gsm.imei" else "ril.cdma.meid")
        if (!TextUtils.isEmpty(ids)) {
            val idArr = ids.split(",").toTypedArray()
            return if (idArr.size == 2) {
                getMinOne(idArr[0], idArr[1])
            } else {
                idArr[0]
            }
        }
        var id0 = tm.deviceId
        var id1 = ""
        try {
            val method = tm.javaClass.getMethod("getDeviceId", Int::class.javaPrimitiveType)
            id1 = method.invoke(
                tm,
                if (isImei) TelephonyManager.PHONE_TYPE_GSM else TelephonyManager.PHONE_TYPE_CDMA
            ) as String
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
        if (isImei) {
            if (id0 != null && id0.length < 15) {
                id0 = ""
            }
            if (id1 != null && id1.length < 15) {
                id1 = ""
            }
        } else {

            if (id0?.length?:0 == 14) {
                id0 = ""
            }
            if (id1?.length?:0 == 14) {
                id1 = ""
            }
        }
        return getMinOne(id0, id1)
    }
    return ""
}

private fun getMinOne(s0: String?, s1: String?): String? {
    val empty0 = TextUtils.isEmpty(s0)
    val empty1 = TextUtils.isEmpty(s1)
    if (empty0 && empty1) return ""
    if (!empty0 && !empty1) {
        return if (s0!!.compareTo(s1!!) <= 0) {
            s0
        } else {
            s1
        }
    }
    return if (!empty0) s0 else s1
}

private fun getSystemPropertyByReflect(key: String): String {
    try {
        @SuppressLint("PrivateApi") val clz = Class.forName("android.os.SystemProperties")
        val getMethod = clz.getMethod("get", String::class.java, String::class.java)
        return getMethod.invoke(clz, key, "") as String
    } catch (e: Exception) { /**/
    }
    return ""
}

/**
 * Return the IMSI.
 *
 * Must hold `<uses-permission android:name="android.permission.READ_PHONE_STATE" />`
 *
 * @return the IMSI
 */
@get:RequiresPermission(permission.READ_PHONE_STATE)
@get:SuppressLint("HardwareIds")
val iMSI: String
    get() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                telephonyManager.subscriberId
            } catch (e: SecurityException) {
                e.printStackTrace()
                return ""
            }
        }
        return telephonyManager.subscriberId
    }


/**
 * Return whether sim card state is ready.
 *
 * @return `true`: yes<br></br>`false`: no
 */
val isSimCardReady = telephonyManager.simState == TelephonyManager.SIM_STATE_READY

/**
 * Return the sim operator name.
 *
 * @return the sim operator name
 */
val simOperatorName = telephonyManager.simOperatorName

/**
 * Return the sim operator using mnc.
 *
 * @return the sim operator
 */
val simOperatorByMnc: String
    get() {
        val tm = telephonyManager
        val operator = tm.simOperator ?: return ""
        return when (operator) {
            "46000", "46002", "46007", "46020" -> "中国移动"
            "46001", "46006", "46009" -> "中国联通"
            "46003", "46005", "46011" -> "中国电信"
            else -> operator
        }
    }

/**
 * Skip to dial.
 *
 * @param phoneNumber The phone number.
 */
fun dial(phoneNumber: String) {
    app!!.startActivity(IntentUtils.getDialIntent(phoneNumber))
}

/**
 * Make a phone call.
 *
 * Must hold `<uses-permission android:name="android.permission.CALL_PHONE" />`
 *
 * @param phoneNumber The phone number.
 */
@RequiresPermission(permission.CALL_PHONE)
fun call(phoneNumber: String) {
    app!!.startActivity(IntentUtils.getCallIntent(phoneNumber))
}

/**
 * Send sms.
 *
 * @param phoneNumber The phone number.
 * @param content     The content.
 */
fun sendSms(phoneNumber: String, content: String?) {
    app!!.startActivity(IntentUtils.getSendSmsIntent(phoneNumber, content))
}

