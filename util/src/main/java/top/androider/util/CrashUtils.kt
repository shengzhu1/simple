package top.androider.util

import android.annotation.SuppressLint
import android.os.Build
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

///////////////////////////////////////////////////////////////////////////
// interface
///////////////////////////////////////////////////////////////////////////
interface OnCrashListener {
    fun onCrash(crashInfo: String?, e: Throwable?)
}

/**
 * <pre>
 * author: Blankj
 * blog  : http://blankj.com
 * time  : 2016/09/27
 * desc  : utils about crash
</pre> *
 */
class CrashUtils private constructor() {


    companion object {
        private val FILE_SEP = System.getProperty("file.separator")
        private val DEFAULT_UNCAUGHT_EXCEPTION_HANDLER = Thread.getDefaultUncaughtExceptionHandler()

        /**
         * Initialization.
         */
        @SuppressLint("MissingPermission")
        fun init() {
            init("")
        }

        /**
         * Initialization
         *
         * @param crashDir The directory of saving crash information.
         */
        fun init(crashDir: File) {
            init(crashDir.absolutePath, null)
        }

        /**
         * Initialization
         *
         * @param onCrashListener The crash listener.
         */
        fun init(onCrashListener: OnCrashListener?) {
            init("", onCrashListener)
        }

        /**
         * Initialization
         *
         * @param crashDir        The directory of saving crash information.
         * @param onCrashListener The crash listener.
         */
        fun init(crashDir: File, onCrashListener: OnCrashListener?) {
            init(crashDir.absolutePath, onCrashListener)
        }
        /**
         * Initialization
         *
         * @param crashDirPath    The directory's path of saving crash information.
         * @param onCrashListener The crash listener.
         */
        /**
         * Initialization
         *
         * @param crashDirPath The directory's path of saving crash information.
         */
        @JvmOverloads
        fun init(crashDirPath: String, onCrashListener: OnCrashListener? = null) {
            val dirPath: String
            dirPath = if (isSpace(crashDirPath)) {
                if (isSDCardEnableByEnvironment
                    && Utils.app!!.getExternalFilesDir(null) != null
                ) {
                    Utils.app!!.getExternalFilesDir(null)
                        .toString() + FILE_SEP + "crash" + FILE_SEP
                } else {
                    Utils.app!!.filesDir.toString() + FILE_SEP + "crash" + FILE_SEP
                }
            } else {
                if (crashDirPath.endsWith(FILE_SEP!!)) crashDirPath else crashDirPath + FILE_SEP
            }
            Thread.setDefaultUncaughtExceptionHandler(
                getUncaughtExceptionHandler(
                    dirPath,
                    onCrashListener
                )
            )
        }

        private fun getUncaughtExceptionHandler(
            dirPath: String,
            onCrashListener: OnCrashListener?
        ): Thread.UncaughtExceptionHandler {
            return Thread.UncaughtExceptionHandler { t, e ->
                val time = SimpleDateFormat("yyyy_MM_dd-HH_mm_ss").format(Date())
                val sb = StringBuilder()
                val head = """
                    ************* Log Head ****************
                    Time Of Crash      : $time
                    Device Manufacturer: ${Build.MANUFACTURER}
                    Device Model       : ${Build.MODEL}
                    Android Version    : ${Build.VERSION.RELEASE}
                    Android SDK        : ${Build.VERSION.SDK_INT}
                    App VersionName    : ${AppUtils.appVersionName}
                    App VersionCode    : ${AppUtils.appVersionCode}
                    ************* Log Head ****************


                    """.trimIndent()
                sb.append(head).append(e.getFullStackTrace())
                val crashInfo = sb.toString()
                val crashFile = "$dirPath$time.txt"
                FileIOUtils.writeFileFromString(crashFile, crashInfo, true)
                onCrashListener?.onCrash(crashInfo, e)
                if (DEFAULT_UNCAUGHT_EXCEPTION_HANDLER != null) {
                    DEFAULT_UNCAUGHT_EXCEPTION_HANDLER.uncaughtException(t, e)
                }
            }
        }
    }

    init {
        throw UnsupportedOperationException("u can't instantiate me...")
    }
}