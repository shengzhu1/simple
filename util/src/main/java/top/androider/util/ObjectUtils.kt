package top.androider.util

import android.os.Build
import android.util.SparseArray
import android.util.SparseBooleanArray
import android.util.SparseIntArray
import android.util.SparseLongArray
import androidx.annotation.RequiresApi
import androidx.collection.LongSparseArray
import androidx.collection.SimpleArrayMap
import java.lang.reflect.Array
import java.util.*

/**
 * Return whether object is empty.
 *
 * @param obj The object.
 * @return `true`: yes<br></br>`false`: no
 */
fun Any?.isEmpty() = this == null

fun CharSequence?.isEmpty() = this == null || this.toString().length == 0
fun Collection<*>?.isEmpty() = this == null || this.isEmpty()
fun Map<*, *>?.isEmpty() = this == null || this.isEmpty()
fun SimpleArrayMap<*, *>?.isEmpty()=  this?.isEmpty?:true
fun SparseArray<*>?.isEmpty() = this?.size()?:0 == 0
fun SparseBooleanArray?.isEmpty() = this?.size()?:0 == 0
fun SparseIntArray?.isEmpty()= this?.size()?:0 == 0
fun LongSparseArray<*>?.isEmpty()= this?.isEmpty?:true
fun SparseLongArray?.isEmpty()= this?.size()?:0 == 0
fun android.util.LongSparseArray<*>?.isEmpty() = this?.size()?:0 == 0


fun Any?.isNotEmpty()= !this.isEmpty()
fun CharSequence?.isNotEmpty()= !this.isEmpty()
fun Collection<*>?.isNotEmpty()= !this.isEmpty()
fun Map<*, *>?.isNotEmpty()= !this.isEmpty()

fun SimpleArrayMap<*, *>?.isNotEmpty()= !this.isEmpty()
fun SparseArray<*>?.isNotEmpty()= !this.isEmpty()
fun SparseBooleanArray?.isNotEmpty()= !this.isEmpty()
fun SparseIntArray?.isNotEmpty()= !this.isEmpty()
fun LongSparseArray<*>?.isNotEmpty()= !this.isEmpty()
fun SparseLongArray?.isNotEmpty()= !this.isEmpty()
fun android.util.LongSparseArray<*>?.isNotEmpty()= !this.isEmpty()

fun <T> T.compare(b: T, c: (T,T)->Int) = if (this === b) 0 else c(this, b)
fun Any?.equals(o2: Any)= this === o2 || this != null && this == o2
fun Any?.toString(nullDefault: String? = null) = this?.toString() ?: nullDefault
fun Any?.hashCode() = this?.hashCode() ?: 0
fun hashCodes(vararg values: Any?) = Arrays.hashCode(values)

