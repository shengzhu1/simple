package top.androider.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat

private val DF_THREAD_LOCAL: ThreadLocal<DecimalFormat> =
    object : ThreadLocal<DecimalFormat>() {
        override fun initialValue(): DecimalFormat {
            return NumberFormat.getInstance() as DecimalFormat
        }
    }
val safeDecimalFormat: DecimalFormat?
get() = DF_THREAD_LOCAL.get()

/**
 * Format the value.
 *
 * @param value            The value.
 * @param isGrouping       True to set grouping will be used in this format, false otherwise.
 * @param minIntegerDigits The minimum number of digits allowed in the integer portion of value.
 * @param fractionDigits   The number of digits allowed in the fraction portion of value.
 * @param isHalfUp         True to rounded towards the nearest neighbor.
 * @return the format value
 */
fun Float.format(
    isGrouping: Boolean = false,
    minIntegerDigits: Int = 1,
    fractionDigits: Int,
    isHalfUp: Boolean = true
): String {
    return float2Double(this).format(
        isGrouping,
        minIntegerDigits,
        fractionDigits,
        isHalfUp
    )
}

/**
 * Format the value.
 *
 * @param value            The value.
 * @param isGrouping       True to set grouping will be used in this format, false otherwise.
 * @param minIntegerDigits The minimum number of digits allowed in the integer portion of value.
 * @param fractionDigits   The number of digits allowed in the fraction portion of value.
 * @param isHalfUp         True to rounded towards the nearest neighbor.
 * @return the format value
 */
fun Double.format(
    isGrouping: Boolean = false,
    minIntegerDigits: Int = 1,
    fractionDigits: Int,
    isHalfUp: Boolean = true
): String {
    val nf = safeDecimalFormat
    nf!!.isGroupingUsed = isGrouping
    nf.roundingMode =
        if (isHalfUp) RoundingMode.HALF_UP else RoundingMode.DOWN
    nf.minimumIntegerDigits = minIntegerDigits
    nf.minimumFractionDigits = fractionDigits
    nf.maximumFractionDigits = fractionDigits
    return nf.format(this)
}

/**
 * Float to double.
 *
 * @param value The value.
 * @return the number of double
 */
fun float2Double(value: Float): Double {
    return BigDecimal(value.toString()).toDouble()
}