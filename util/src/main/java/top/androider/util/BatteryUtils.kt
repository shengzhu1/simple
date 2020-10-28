package top.androider.util

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.annotation.IntDef
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.util.*

/**
 * Register the status of battery changed listener.
 *
 * @param listener The status of battery changed listener.
 */
fun OnBatteryStatusChangedListener?.register() {
    BatteryChangedReceiver.instance.registerListener(this)
}

/**
 * Return whether the status of battery changed listener has been registered.
 *
 * @return true to registered, false otherwise.
 */
fun OnBatteryStatusChangedListener?.isRegistered(): Boolean {
    return BatteryChangedReceiver.instance.isRegistered(this)
}

/**
 * Unregister the status of battery changed listener.
 *
 */
fun OnBatteryStatusChangedListener?.unregister() {
    BatteryChangedReceiver.instance.unregisterListener(this)
}



interface OnBatteryStatusChangedListener {
    fun onBatteryStatusChanged(status: Status?)
}

class Status internal constructor(
    var level: Int,
    @field:BatteryStatus @get:BatteryStatus var status: Int
) {

    override fun toString(): String {
        return batteryStatus2String(status) + ": " + level + "%"
    }

    companion object {
        fun batteryStatus2String(@BatteryStatus status: Int) =
            when(status){
                BatteryStatus.DISCHARGING -> "discharging"
                BatteryStatus.CHARGING -> "charging"
                BatteryStatus.NOT_CHARGING -> "not_charging"
                BatteryStatus.FULL ->"full"
                else -> "unknown"
            }
    }
}
/**
 * <pre>
 * author: blankj
 * blog  : http://blankj.com
 * time  : 2020/03/31
 * desc  :
</pre> *
 */


@IntDef(
    BatteryStatus.UNKNOWN,
    BatteryStatus.DISCHARGING,
    BatteryStatus.CHARGING,
    BatteryStatus.NOT_CHARGING,
    BatteryStatus.FULL
)
@Retention(RetentionPolicy.SOURCE)
annotation class BatteryStatus {
    companion object{
        const val UNKNOWN = BatteryManager.BATTERY_STATUS_UNKNOWN
        const val DISCHARGING = BatteryManager.BATTERY_STATUS_DISCHARGING
        const val CHARGING = BatteryManager.BATTERY_STATUS_CHARGING
        const val NOT_CHARGING = BatteryManager.BATTERY_STATUS_NOT_CHARGING
        const val FULL = BatteryManager.BATTERY_STATUS_FULL
    }
}
class BatteryChangedReceiver : BroadcastReceiver() {
    private val mListeners: MutableSet<OnBatteryStatusChangedListener> = HashSet()
    fun registerListener(listener: OnBatteryStatusChangedListener?) {
        if (listener == null) return
        ThreadUtils.runOnUiThread {
            val preSize = mListeners.size
            mListeners.add(listener)
            if (preSize == 0 && mListeners.size == 1) {
                val intentFilter = IntentFilter()
                intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED)
                Utils.app!!.registerReceiver(Companion.instance, intentFilter)
            }
        }
    }

    fun isRegistered(listener: OnBatteryStatusChangedListener?): Boolean {
        return if (listener == null) false else mListeners.contains(listener)
    }

    fun unregisterListener(listener: OnBatteryStatusChangedListener?) {
        if (listener == null) return
        ThreadUtils.runOnUiThread {
            val preSize = mListeners.size
            mListeners.remove(listener)
            if (preSize == 1 && mListeners.size == 0) {
                Utils.app!!.unregisterReceiver(Companion.instance)
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BATTERY_CHANGED == intent.action) {
            ThreadUtils.runOnUiThread {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val status =
                    intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryStatus.UNKNOWN)
                for (listener in mListeners) {
                    listener.onBatteryStatusChanged(Status(level, status))
                }
            }
        }
    }

    companion object {
        @JvmStatic
        val instance = BatteryChangedReceiver()
    }
}