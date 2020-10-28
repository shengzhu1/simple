package top.androider.util

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import top.androider.util.Utils.Companion.app
import java.io.Serializable
import java.util.*

/**
 * <pre>
 * author: blankj
 * blog  : http://blankj.com
 * time  : 2020/03/19
 * desc  :
</pre> *
 */
open class UtilsTransActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(0, 0)
        val extra = intent.getSerializableExtra(EXTRA_DELEGATE)
        if (extra !is TransActivityDelegate) {
            super.onCreate(savedInstanceState)
            finish()
            return
        }
        val delegate = extra
        CALLBACK_MAP[this] = delegate
        delegate.onCreateBefore(this, savedInstanceState)
        super.onCreate(savedInstanceState)
        delegate.onCreated(this, savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        CALLBACK_MAP[this]?.onStarted(this)
    }

    override fun onResume() {
        super.onResume()
        CALLBACK_MAP[this]?.onResumed(this)
    }

    override fun onPause() {
        overridePendingTransition(0, 0)
        super.onPause()
        CALLBACK_MAP[this]?.onPaused(this)
    }

    override fun onStop() {
        super.onStop()
        
        CALLBACK_MAP[this]?.onStopped(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        CALLBACK_MAP[this]?.onSaveInstanceState(this, outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        CALLBACK_MAP[this]?.onDestroy(this)
        CALLBACK_MAP.remove(this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        CALLBACK_MAP[this]?.onRequestPermissionsResult(this, requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        CALLBACK_MAP[this]?.onActivityResult(this, requestCode, resultCode, data)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val callback = CALLBACK_MAP[this]
            ?: return super.dispatchTouchEvent(ev)
        return if (CALLBACK_MAP[this]?.dispatchTouchEvent(this, ev)?:false) {
            true
        } else super.dispatchTouchEvent(ev)
    }

    abstract class TransActivityDelegate : Serializable {
        fun onCreateBefore(activity: UtilsTransActivity, savedInstanceState: Bundle?) { /**/
        }

        fun onCreated(activity: UtilsTransActivity, savedInstanceState: Bundle?) { /**/
        }

        fun onStarted(activity: UtilsTransActivity) { /**/
        }

        fun onDestroy(activity: UtilsTransActivity) { /**/
        }

        fun onResumed(activity: UtilsTransActivity) { /**/
        }

        fun onPaused(activity: UtilsTransActivity) { /**/
        }

        fun onStopped(activity: UtilsTransActivity) { /**/
        }

        fun onSaveInstanceState(activity: UtilsTransActivity, outState: Bundle?) { /**/
        }

        fun onRequestPermissionsResult(
            activity: UtilsTransActivity,
            requestCode: Int,
            permissions: Array<String>?,
            grantResults: IntArray?
        ) { /**/
        }

        fun onActivityResult(
            activity: UtilsTransActivity,
            requestCode: Int,
            resultCode: Int,
            data: Intent?
        ) { /**/
        }

        fun dispatchTouchEvent(activity: UtilsTransActivity, ev: MotionEvent?): Boolean {
            return false
        }
    }

    companion object {
        private val CALLBACK_MAP: MutableMap<UtilsTransActivity, TransActivityDelegate> = HashMap()
        protected const val EXTRA_DELEGATE = "extra_delegate"
        fun start(delegate: TransActivityDelegate?) {
            start(null, null, delegate, UtilsTransActivity::class.java)
        }

        fun start(
            consumer: Utils.Consumer<Intent?>?,
            delegate: TransActivityDelegate?
        ) {
            start(null, consumer, delegate, UtilsTransActivity::class.java)
        }

        fun start(
            activity: Activity?,
            delegate: TransActivityDelegate?
        ) {
            start(activity, null, delegate, UtilsTransActivity::class.java)
        }

        fun start(
            activity: Activity?,
            consumer: Utils.Consumer<Intent?>?,
            delegate: TransActivityDelegate?
        ) {
            start(activity, consumer, delegate, UtilsTransActivity::class.java)
        }

        @JvmStatic
        protected fun start(
            activity: Activity?,
            consumer: Utils.Consumer<Intent?>?,
            delegate: TransActivityDelegate?,
            cls: Class<*>?
        ) {
            if (delegate == null) return
            val starter = Intent(app, cls)
            starter.putExtra(EXTRA_DELEGATE, delegate)
            consumer?.accept(starter)
            if (activity == null) {
                starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                app!!.startActivity(starter)
            } else {
                activity.startActivity(starter)
            }
        }
    }
}