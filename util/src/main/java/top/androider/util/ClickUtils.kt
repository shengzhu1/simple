package top.androider.util

import android.R
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import android.os.Build
import android.os.SystemClock
import android.util.Log
import android.util.StateSet
import android.view.MotionEvent
import android.view.TouchDelegate
import android.view.View
import android.view.View.OnTouchListener
import androidx.annotation.IntRange
import androidx.core.view.ViewCompat


interface Back2HomeFriendlyListener {
    fun show(text: CharSequence?, duration: Long)
    fun dismiss()

    companion object {
        val DEFAULT: Back2HomeFriendlyListener = object : Back2HomeFriendlyListener {
            override fun show(text: CharSequence?, duration: Long) {
                text?.let {
                    ToastUtils.show(it)
                }
            }

            override fun dismiss() {
                ToastUtils.cancel()
            }
        }
    }
}

abstract class OnDebouncingClickListener @JvmOverloads constructor(
    private val mIsGlobal: Boolean = true,
    private val mDuration: Long = DEBOUNCING_DEFAULT_VALUE
) : View.OnClickListener {
    constructor(duration: Long) : this(true, duration) {}

    abstract fun onDebouncingClick(v: View?)
    override fun onClick(v: View) {
        if (mIsGlobal) {
            if (mEnabled) {
                mEnabled = false
                v.postDelayed(ENABLE_AGAIN, mDuration)
                onDebouncingClick(v)
            }
        } else {
            if (isValid(v, mDuration)) {
                onDebouncingClick(v)
            }
        }
    }

    companion object {
        private var mEnabled = true
        private val ENABLE_AGAIN = Runnable { mEnabled = true }
    }
}

abstract class OnMultiClickListener @JvmOverloads constructor(
    private val mTriggerClickCount: Int,
    private val mClickInterval: Long = INTERVAL_DEFAULT_VALUE
) : View.OnClickListener {
    private var mLastClickTime: Long = 0
    private var mClickCount = 0
    abstract fun onTriggerClick(v: View?)
    abstract fun onBeforeTriggerClick(v: View?, count: Int)
    override fun onClick(v: View) {
        if (mTriggerClickCount <= 1) {
            onTriggerClick(v)
            return
        }
        val curTime = System.currentTimeMillis()
        if (curTime - mLastClickTime < mClickInterval) {
            mClickCount++
            if (mClickCount == mTriggerClickCount) {
                onTriggerClick(v)
            } else if (mClickCount < mTriggerClickCount) {
                onBeforeTriggerClick(v, mClickCount)
            } else {
                mClickCount = 1
                onBeforeTriggerClick(v, mClickCount)
            }
        } else {
            mClickCount = 1
            onBeforeTriggerClick(v, mClickCount)
        }
        mLastClickTime = curTime
    }

    companion object {
        private const val INTERVAL_DEFAULT_VALUE: Long = 666
    }
}

private class OnUtilsTouchListener private constructor() : OnTouchListener {
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        val action = event.action
        if (action == MotionEvent.ACTION_DOWN) {
            processScale(v, true)
            processAlpha(v, true)
        } else if (action == MotionEvent.ACTION_UP
            || action == MotionEvent.ACTION_CANCEL
        ) {
            processScale(v, false)
            processAlpha(v, false)
        }
        return false
    }

    private fun processScale(view: View, isDown: Boolean) {
        val tag = view.getTag(PRESSED_VIEW_SCALE_TAG) as? Float ?: return
        val value: Float = if (isDown) 1 + tag else 1F
        view.animate()
            .scaleX(value)
            .scaleY(value)
            .setDuration(200)
            .start()
    }

    private fun processAlpha(view: View, isDown: Boolean) {
        val tag =
            view.getTag(if (isDown) PRESSED_VIEW_ALPHA_TAG else PRESSED_VIEW_ALPHA_SRC_TAG) as? Float
                ?: return
        view.alpha = tag
    }

    companion object {
        val instance =  OnUtilsTouchListener()
    }
}

internal class ClickDrawableWrapper(drawable: Drawable?) :
    ShadowUtils.DrawableWrapper(drawable) {
    private var mBitmapDrawable: BitmapDrawable? = null

    // 低版本ColorDrawable.setColorFilter无效，这里直接用画笔画上
    private val mColorPaint by lazy<Paint?>{
        if (drawable is ColorDrawable) {
            Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG).apply { setColor(drawable.color) }
        } else{
            null
        }
    }

    override fun draw(canvas: Canvas) {
        if (mBitmapDrawable == null) {
            val bitmap =
                Bitmap.createBitmap(bounds.width(), bounds.height(), Bitmap.Config.ARGB_8888)
            val myCanvas = Canvas(bitmap)
            mColorPaint?.let {
                myCanvas.drawRect(bounds, it)
            }?:super.draw(myCanvas)
            mBitmapDrawable = BitmapDrawable(Resources.getSystem(), bitmap)
            mBitmapDrawable!!.bounds = bounds
        }
        mBitmapDrawable!!.draw(canvas)
    }
}

private const val PRESSED_VIEW_SCALE_TAG = -1
private const val PRESSED_VIEW_SCALE_DEFAULT_VALUE = -0.06f
private const val PRESSED_VIEW_ALPHA_TAG = -2
private const val PRESSED_VIEW_ALPHA_SRC_TAG = -3
private const val PRESSED_VIEW_ALPHA_DEFAULT_VALUE = 0.8f
private const val PRESSED_BG_ALPHA_STYLE = 4
private const val PRESSED_BG_ALPHA_DEFAULT_VALUE = 0.9f
private const val PRESSED_BG_DARK_STYLE = 5
private const val PRESSED_BG_DARK_DEFAULT_VALUE = 0.9f
private const val DEBOUNCING_DEFAULT_VALUE: Long = 1000


/**
 * Apply scale animation for the views' click.
 *
 * @param views        The views.
 * @param scaleFactors The factors of scale for the views.
 */
fun Array<View>?.applyPressedViewScale(scaleFactors: FloatArray? = null ) {
    this?.forEach {
        it.applyPressedViewScale(screenDensity)
    }
}

/**
 * Apply scale animation for the views' click.
 *
 * @param view        The view.
 * @param scaleFactor The factor of scale for the view.
 */
fun View?.applyPressedViewScale(scaleFactor: Float? = null) {
    this?.apply {
        val scale = scaleFactor ?: PRESSED_VIEW_SCALE_DEFAULT_VALUE
        setTag(PRESSED_VIEW_SCALE_TAG, scaleFactor)
        isClickable = true
        setOnTouchListener(OnUtilsTouchListener.instance)
    }
}

/**
 * Apply alpha for the views' click.
 *
 * @param views The views.
 */
fun applyPressedViewAlpha(vararg views: View?) {
    views.forEach {
        it.applyPressedViewScale(null)
    }
}

/**
 * Apply alpha for the views' click.
 *
 * @param views  The views.
 * @param alphas The alphas for the views.
 */
fun Array<View?>?.applyPressedViewAlpha(alphas: FloatArray?) {
    this?.forEachIndexed { i, view ->
        if (alphas == null || i >= alphas.size) {
            view.applyPressedViewAlpha(PRESSED_VIEW_ALPHA_DEFAULT_VALUE)
        } else {
            view.applyPressedViewAlpha(alphas[i])
        }
    }
}

/**
 * Apply scale animation for the views' click.
 *
 * @param view  The view.
 * @param alpha The alpha for the view.
 */
fun View?.applyPressedViewAlpha(alpha: Float) {
    this?.apply {
        setTag(PRESSED_VIEW_ALPHA_TAG, alpha)
        setTag(PRESSED_VIEW_ALPHA_SRC_TAG, this.alpha)
        isClickable = true
        setOnTouchListener(OnUtilsTouchListener.instance)
    }
}
/**
 * Apply alpha for the view's background.
 *
 * @param view  The views.
 * @param alpha The alpha.
 */
@JvmOverloads
fun View?.applyPressedBgAlpha(alpha: Float = PRESSED_BG_ALPHA_DEFAULT_VALUE) {
    this.applyPressedBgStyle(PRESSED_BG_ALPHA_STYLE, alpha)
}

private fun View?.applyPressedBgStyle(style: Int = PRESSED_BG_DARK_STYLE, value: Float = PRESSED_BG_DARK_DEFAULT_VALUE) {
    this?.let {
        view ->
        var background = view.background
        val tag = view.getTag(-style)
        if (tag is Drawable) {
            ViewCompat.setBackground(view, tag)
        } else {
            background = createStyleDrawable(background, style, value)
            ViewCompat.setBackground(view, background)
            view.setTag(-style, background)
        }
    }
}

private fun createStyleDrawable(src: Drawable, style: Int, value: Float): Drawable {
    val srcCopy = src?:ColorDrawable(0)
    return srcCopy.constantState?.let {
        var pressed = it.newDrawable().mutate()
        if (style == PRESSED_BG_ALPHA_STYLE) {
            pressed = createAlphaDrawable(pressed, value)
        } else if (style == PRESSED_BG_DARK_STYLE) {
            pressed = createDarkDrawable(pressed, value)
        }
        var disable = it.newDrawable().mutate()
        disable = createAlphaDrawable(disable, 0.5f)
        return@let StateListDrawable().apply {
            addState(intArrayOf(R.attr.state_pressed), pressed)
            addState(intArrayOf(-R.attr.state_enabled), disable)
            addState(StateSet.WILD_CARD, src)
        }
    }?:srcCopy
}

private fun createAlphaDrawable(drawable: Drawable, alpha: Float): Drawable {
    val drawableWrapper = ClickDrawableWrapper(drawable)
    drawableWrapper.alpha = (alpha * 255).toInt()
    return drawableWrapper
}

private fun createDarkDrawable(drawable: Drawable, alpha: Float): Drawable {
    val drawableWrapper = ClickDrawableWrapper(drawable)
    drawableWrapper.colorFilter = getDarkColorFilter(alpha)
    return drawableWrapper
}

private fun getDarkColorFilter(darkAlpha: Float): ColorMatrixColorFilter {
    return ColorMatrixColorFilter(
        ColorMatrix(
            floatArrayOf(
                darkAlpha,
                0f,
                0f,
                0f,
                0f,
                0f,
                darkAlpha,
                0f,
                0f,
                0f,
                0f,
                0f,
                darkAlpha,
                0f,
                0f,
                0f,
                0f,
                0f,
                2f,
                0f
            )
        )
    )
}

/**
 * Apply single debouncing for the view's click.
 *
 * @param view     The view.
 * @param listener The listener.
 */
fun View.applySingleDebouncing(listener: View.OnClickListener?) {
    arrayOf(this).applySingleDebouncing(listener = listener)
}

/**
 * Apply single debouncing for the view's click.
 *
 * @param view     The view.
 * @param duration The duration of debouncing.
 * @param listener The listener.
 */
fun View.applySingleDebouncing(@IntRange(from = 0) duration: Long,
    listener: View.OnClickListener?
) {
    arrayOf(this).applySingleDebouncing(duration, listener)
}

/**
 * Apply single debouncing for the views' click.
 *
 * @param views    The views.
 * @param duration The duration of debouncing.
 * @param listener The listener.
 */
fun Array<View>?.applySingleDebouncing(
    @IntRange(from = 0) duration: Long = DEBOUNCING_DEFAULT_VALUE,
    listener: View.OnClickListener?
) {
    this.applyDebouncing(false, duration, listener)
}

/**
 * Apply global debouncing for the view's click.
 *
 * @param view     The view.
 * @param listener The listener.
 */
fun View.applyGlobalDebouncing(listener: View.OnClickListener?) {
    arrayOf(this).applyGlobalDebouncing(listener = listener)
}

/**
 * Apply global debouncing for the view's click.
 *
 * @param view     The view.
 * @param duration The duration of debouncing.
 * @param listener The listener.
 */
fun View.applyGlobalDebouncing( @IntRange(from = 0) duration: Long,
    listener: View.OnClickListener?
) {
    arrayOf(this).applyGlobalDebouncing( duration, listener)
}

/**
 * Apply global debouncing for the views' click.
 *
 * @param views    The views.
 * @param duration The duration of debouncing.
 * @param listener The listener.
 */
fun Array<View>?.applyGlobalDebouncing(
    @IntRange(from = 0) duration: Long = DEBOUNCING_DEFAULT_VALUE,
    listener: View.OnClickListener?
) {
    this.applyDebouncing(true, duration, listener)
}

private fun Array<View>?.applyDebouncing(
    isGlobal: Boolean = false,
    @IntRange(from = 0) duration: Long,
    listener: View.OnClickListener?
) {
    this?.takeIf { listener != null }?.forEach {
        view ->
        view.setOnClickListener(object : OnDebouncingClickListener(isGlobal, duration) {
            override fun onDebouncingClick(v: View?) {
                listener?.onClick(v)
            }
        })
    }
}

fun View.expandClickArea(expandSize: Int) {
    this.expandClickArea(expandSize, expandSize, expandSize, expandSize)
}

fun View.expandClickArea(
    expandSizeTop: Int,
    expandSizeLeft: Int,
    expandSizeRight: Int,
    expandSizeBottom: Int
) {
    val parentView = parent as View?
    parentView?.post {
        val rect = Rect()
        getHitRect(rect)
        rect.top -= expandSizeTop
        rect.bottom += expandSizeBottom
        rect.left -= expandSizeLeft
        rect.right += expandSizeRight
        parentView.touchDelegate = TouchDelegate(rect, this)
    }
}

private const val TIP_DURATION = 2000L
private var sLastClickMillis: Long = 0
private var sClickCount = 0

fun back2HomeFriendly(
    tip: CharSequence,
    duration: Long = TIP_DURATION,
    listener: Back2HomeFriendlyListener =  Back2HomeFriendlyListener.DEFAULT
) {
    val nowMillis = SystemClock.elapsedRealtime()
    if (Math.abs(nowMillis - sLastClickMillis) < duration) {
        sClickCount++
        if (sClickCount == 2) {
            startHomeActivity()
            listener.dismiss()
            sLastClickMillis = 0
        }
    } else {
        sClickCount = 1
        listener.show(tip, duration)
        sLastClickMillis = nowMillis
    }
}