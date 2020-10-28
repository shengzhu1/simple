package top.androider.util

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Build
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.*
import androidx.core.app.NotificationManagerCompat
import top.androider.util.Utils.Companion.app

/**
 * <pre>
 * author: Blankj
 * blog  : http://blankj.com
 * time  : 2016/09/29
 * desc  : utils about toast
</pre> *
 */
object ToastUtils {
    private const val COLOR_DEFAULT = -0x1000001
    private const val NULL = "null"
    private var iToast: IToast? = null
    var gravity = -1
    var xOffset = -1
    var yOffset = -1
    var bgColor = COLOR_DEFAULT
    var bgResource = -1
    var msgColor = COLOR_DEFAULT
    var msgTextSize = -1

    /**
     * Cancel the toast.
     */
    fun cancel() {
        if (iToast != null) {
            iToast!!.cancel()
        }
    }

    fun show(resId: Int, duration: Int = Toast.LENGTH_SHORT, vararg args: Any) {
        try {
            var text:CharSequence = app!!.resources.getText(resId)
            if (args.isNotEmpty()) {
                text = String.format(text.toString(), *args)
            }
            show(text, duration)
        } catch (ignore: Exception) {
            show(resId.toString(), duration)
        }
    }

    fun show(format: CharSequence = "", duration: Int = Toast.LENGTH_SHORT, vararg args: Any) {
        var text = format
        if (args.isNotEmpty()) {
            text = String.format(format.toString(), *args)
        }
        show(text, duration)
    }
    
    fun show(view: View?, text: CharSequence? = null, duration: Int = Toast.LENGTH_SHORT) {
        ThreadUtils.runOnUiThread {
            cancel()
            iToast = newToast()
            if (view != null) {
                iToast!!.view = view
            } else {
                iToast!!.setMsgView(text)
            }
            iToast!!.setDuration(duration)
            if (gravity != -1 || xOffset != -1 || yOffset != -1) {
                iToast!!.setGravity(gravity, xOffset, yOffset)
            }
            iToast!!.show()
        }
    }

    private fun newToast(): IToast {
        if (NotificationManagerCompat.from(app!!).areNotificationsEnabled()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!isGrantedDrawOverlays()) {
                    return SystemToast(Toast(app))
                }
            }
        }
        return ToastWithoutNotification(Toast(app))
    }

    private fun getView(@LayoutRes layoutId: Int): View {
        val inflate = app!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        return inflate.inflate(layoutId, null)
    }

    internal class SystemToast(toast: Toast?) : AbsToast(toast) {
        override fun show() {
            mToast!!.show()
        }

        override fun cancel() {
            mToast!!.cancel()
            super.cancel()
        }

        internal class SafeHandler(private val impl: Handler) : Handler() {
            override fun handleMessage(msg: Message) {
                impl.handleMessage(msg)
            }

            override fun dispatchMessage(msg: Message) {
                try {
                    impl.dispatchMessage(msg)
                } catch (e: Exception) {
                    Log.e("ToastUtils", e.toString())
                }
            }
        }

        init {
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) {
                try {
                    val mTNField = Toast::class.java.getDeclaredField("mTN")
                    mTNField.isAccessible = true
                    val mTN = mTNField[toast]
                    val mTNmHandlerField = mTNField.type.getDeclaredField("mHandler")
                    mTNmHandlerField.isAccessible = true
                    val tnHandler = mTNmHandlerField[mTN] as Handler
                    mTNmHandlerField[mTN] = SafeHandler(tnHandler)
                } catch (ignored: Exception) { /**/
                }
            }
        }
    }

    internal class ToastWithoutNotification(toast: Toast?) : AbsToast(toast) {
        private var mWM: WindowManager? = null
        private val mParams = WindowManager.LayoutParams()
        override fun show() {
            if (mToast == null) return
            var isActivityContext = false
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) {
                mWM = app!!.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                mParams.type = WindowManager.LayoutParams.TYPE_TOAST
            } else if (isGrantedDrawOverlays()) {
                mWM = app!!.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    mParams.type = WindowManager.LayoutParams.TYPE_PHONE
                }
            } else {
                val topActivityOrApp = topActivityOrApp
                if (topActivityOrApp !is Activity) {
                    Log.w("ToastUtils", "Couldn't get top Activity.")
                    // try to use system toast
                    SystemToast(mToast).show()
                    return
                }
                val topActivity = topActivityOrApp
                if (topActivity.isFinishing || topActivity.isDestroyed) {
                    Log.w("ToastUtils", "$topActivity is useless")
                    // try to use system toast
                    SystemToast(mToast).show()
                    return
                }
                isActivityContext = true
                mWM = topActivity.windowManager
                mParams.type = WindowManager.LayoutParams.LAST_APPLICATION_WINDOW
                UtilsActivityLifecycleImpl.addActivityLifecycleCallbacks(topActivity, activityLifecycleCallbacks)
            }
            setToastParams()
            val duration = if (mToast!!.duration == Toast.LENGTH_SHORT) 2000 else 3500.toLong()
            if (isActivityContext) {
                ThreadUtils.runOnUiThreadDelayed({ setToast(duration) }, 300)
            } else {
                setToast(duration)
            }
        }

        private fun setToastParams() {
            mParams.height = WindowManager.LayoutParams.WRAP_CONTENT
            mParams.width = WindowManager.LayoutParams.WRAP_CONTENT
            mParams.format = PixelFormat.TRANSLUCENT
            mParams.windowAnimations = android.R.style.Animation_Toast
            mParams.title = "ToastWithoutNotification"
            mParams.flags = (WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            mParams.packageName = app!!.packageName
            mParams.gravity = mToast!!.gravity
            if (mParams.gravity and Gravity.HORIZONTAL_GRAVITY_MASK == Gravity.FILL_HORIZONTAL) {
                mParams.horizontalWeight = 1.0f
            }
            if (mParams.gravity and Gravity.VERTICAL_GRAVITY_MASK == Gravity.FILL_VERTICAL) {
                mParams.verticalWeight = 1.0f
            }
            mParams.x = mToast!!.xOffset
            mParams.y = mToast!!.yOffset
            mParams.horizontalMargin = mToast!!.horizontalMargin
            mParams.verticalMargin = mToast!!.verticalMargin
        }

        private fun setToast(duration: Long) {
            try {
                if (mWM != null) {
                    mWM!!.addView(mToastView, mParams)
                }
            } catch (ignored: Exception) { /**/
            }
            ThreadUtils.runOnUiThreadDelayed({ cancel() }, duration)
        }

        private val activityLifecycleCallbacks: Utils.ActivityLifecycleCallbacks
            private get() = object : Utils.ActivityLifecycleCallbacks() {
                override fun onActivityDestroyed(activity: Activity) {
                    if (iToast == null) return
                    activity.window.decorView.visibility = View.GONE
                    iToast!!.cancel()
                }
            }

        override fun cancel() {
            try {
                if (mWM != null) {
                    mWM!!.removeViewImmediate(mToastView)
                }
            } catch (ignored: Exception) { /**/
            }
            mWM = null
            super.cancel()
        }
    }

    internal abstract class AbsToast(protected var mToast: Toast?) : IToast {
        protected var mToastView: View? = null
        override fun setMsgView(text: CharSequence?) {
            mToastView = mToast!!.view
            if (mToastView == null || mToastView!!.findViewById<View?>(android.R.id.message) == null) {
                mToastView = getView(R.layout.toast_layout)
                mToast!!.view = mToastView
            }
            val tvMessage = mToastView!!.findViewById<TextView>(android.R.id.message)
            tvMessage.text = text
            if (msgColor != COLOR_DEFAULT) {
                tvMessage.setTextColor(msgColor)
            }
            if (msgTextSize != -1) {
                tvMessage.textSize = msgTextSize.toFloat()
            }
            setBg(tvMessage)
        }

        private fun setBg(tvMsg: TextView) {
            if (bgResource != -1) {
                mToastView!!.setBackgroundResource(bgResource)
                tvMsg.setBackgroundColor(Color.TRANSPARENT)
            } else if (bgColor != COLOR_DEFAULT) {
                val tvBg = mToastView!!.background
                val msgBg = tvMsg.background
                if (tvBg != null && msgBg != null) {
                    tvBg.colorFilter = PorterDuffColorFilter(bgColor, PorterDuff.Mode.SRC_IN)
                    tvMsg.setBackgroundColor(Color.TRANSPARENT)
                } else if (tvBg != null) {
                    tvBg.colorFilter = PorterDuffColorFilter(bgColor, PorterDuff.Mode.SRC_IN)
                } else if (msgBg != null) {
                    msgBg.colorFilter = PorterDuffColorFilter(bgColor, PorterDuff.Mode.SRC_IN)
                } else {
                    mToastView!!.setBackgroundColor(bgColor)
                }
            }
        }

        override var view: View?
            get() = mToastView
            set(view) {
                mToastView = view
                mToast!!.view = mToastView
            }

        override fun setDuration(duration: Int) {
            mToast!!.duration = duration
        }

        override fun setGravity(gravity: Int, xOffset: Int, yOffset: Int) {
            mToast!!.setGravity(gravity, xOffset, yOffset)
        }

        override fun setText(resId: Int) {
            mToast!!.setText(resId)
        }

        override fun setText(s: CharSequence?) {
            mToast!!.setText(s)
        }

        @CallSuper
        override fun cancel() {
            mToast = null
            mToastView = null
        }
    }

    internal interface IToast {
        fun show()
        fun cancel()
        fun setMsgView(text: CharSequence?)
        var view: View?
        fun setDuration(duration: Int)
        fun setGravity(gravity: Int, xOffset: Int, yOffset: Int)
        fun setText(@StringRes resId: Int)
        fun setText(s: CharSequence?)
    }
}