package top.androider.util.constant

import android.Manifest.permission
import android.os.Build
import androidx.annotation.StringDef
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 * <pre>
 * author: Blankj
 * blog  : http://blankj.com
 * time  : 2017/12/29
 * desc  : constants of permission
</pre> *
 */
object PermissionConstants {
    const val CALENDAR = "CALENDAR"
    const val CAMERA = "CAMERA"
    const val CONTACTS = "CONTACTS"
    const val LOCATION = "LOCATION"
    const val MICROPHONE = "MICROPHONE"
    const val PHONE = "PHONE"
    const val SENSORS = "SENSORS"
    const val SMS = "SMS"
    const val STORAGE = "STORAGE"
    private val GROUP_CALENDAR = arrayOf<String?>(
        permission.READ_CALENDAR, permission.WRITE_CALENDAR
    )
    private val GROUP_CAMERA = arrayOf<String?>(
        permission.CAMERA
    )
    private val GROUP_CONTACTS = arrayOf<String?>(
        permission.READ_CONTACTS, permission.WRITE_CONTACTS, permission.GET_ACCOUNTS
    )
    private val GROUP_LOCATION = arrayOf<String?>(
        permission.ACCESS_FINE_LOCATION, permission.ACCESS_COARSE_LOCATION
    )
    private val GROUP_MICROPHONE = arrayOf<String?>(
        permission.RECORD_AUDIO
    )
    private val GROUP_PHONE = arrayOf<String?>(
        permission.READ_PHONE_STATE, permission.READ_PHONE_NUMBERS, permission.CALL_PHONE,
        permission.READ_CALL_LOG, permission.WRITE_CALL_LOG, permission.ADD_VOICEMAIL,
        permission.USE_SIP, permission.PROCESS_OUTGOING_CALLS, permission.ANSWER_PHONE_CALLS
    )
    private val GROUP_PHONE_BELOW_O = arrayOf<String?>(
        permission.READ_PHONE_STATE, permission.READ_PHONE_NUMBERS, permission.CALL_PHONE,
        permission.READ_CALL_LOG, permission.WRITE_CALL_LOG, permission.ADD_VOICEMAIL,
        permission.USE_SIP, permission.PROCESS_OUTGOING_CALLS
    )
    private val GROUP_SENSORS = arrayOf<String?>(
        permission.BODY_SENSORS
    )
    private val GROUP_SMS = arrayOf<String?>(
        permission.SEND_SMS, permission.RECEIVE_SMS, permission.READ_SMS,
        permission.RECEIVE_WAP_PUSH, permission.RECEIVE_MMS
    )
    private val GROUP_STORAGE = arrayOf<String?>(
        permission.READ_EXTERNAL_STORAGE, permission.WRITE_EXTERNAL_STORAGE
    )

    @JvmStatic
    fun getPermissions(@Permission permission: String?): Array<String?> {
        if (permission == null) return arrayOfNulls(0)
        return when (permission) {
            CALENDAR -> GROUP_CALENDAR
            CAMERA -> GROUP_CAMERA
            CONTACTS -> GROUP_CONTACTS
            LOCATION -> GROUP_LOCATION
            MICROPHONE -> GROUP_MICROPHONE
            PHONE -> if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                GROUP_PHONE_BELOW_O
            } else {
                GROUP_PHONE
            }
            SENSORS -> GROUP_SENSORS
            SMS -> GROUP_SMS
            STORAGE -> GROUP_STORAGE
            else ->  arrayOf(permission)
        }

    }

    @StringDef(CALENDAR, CAMERA, CONTACTS, LOCATION, MICROPHONE, PHONE, SENSORS, SMS, STORAGE)
    @Retention(RetentionPolicy.SOURCE)
    annotation class Permission
}