package top.androider.util

import android.Manifest.permission
import android.content.Context
import android.os.Vibrator
import androidx.annotation.RequiresPermission
import top.androider.util.Utils.Companion.app

/**
 * <pre>
 * author: Blankj
 * blog  : http://blankj.com
 * time  : 2016/09/29
 * desc  : utils about vibrate
</pre> *
 */
object VibrateUtils {
    private val vibrator by lazy<Vibrator> { app!!.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator }

    /**
     * Vibrate.
     *app!!.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
     * Must hold `<uses-permission android:name="android.permission.VIBRATE" />`
     *
     * @param milliseconds The number of milliseconds to vibrate.
     */
    @RequiresPermission(permission.VIBRATE)
    fun vibrate(milliseconds: Long) {
        vibrator.vibrate(milliseconds)
    }

    /**
     * Vibrate.
     *
     * Must hold `<uses-permission android:name="android.permission.VIBRATE" />`
     *
     * @param pattern An array of longs of times for which to turn the vibrator on or off.
     * @param repeat  The index into pattern at which to repeat, or -1 if you don't want to repeat.
     */
    @RequiresPermission(permission.VIBRATE)
    fun vibrate(pattern: LongArray?, repeat: Int) {
        vibrator.vibrate(pattern, repeat)
    }

    /**
     * Cancel vibrate.
     *
     * Must hold `<uses-permission android:name="android.permission.VIBRATE" />`
     */
    @RequiresPermission(permission.VIBRATE)
    fun cancel() {
        vibrator.cancel()
    }
}