package top.androider.util

import android.content.res.Resources.NotFoundException
import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import top.androider.util.Utils.Companion.app

fun CharSequence?.equals(s2: CharSequence?) = this == s2 || s2?.let { this?.equals(it)?:false }?:false

/**
 * Return whether string1 is equals to string2, ignoring case considerations..
 *
 * @param s1 The first string.
 * @param s2 The second string.
 * @return `true`: yes<br></br>`false`: no
 */
fun equalsIgnoreCase(s1: String?, s2: String?): Boolean {
    return s1?.equals(s2, ignoreCase = true) ?: (s2 == null)
}

/**
 * Return the length of string.
 *
 * @param s The string.
 * @return the length of string
 */
fun CharSequence?.length( )= this?.length ?: 0

/**
 * Set the first letter of string upper.
 *
 * @param s The string.
 * @return the string with first letter upper.
 */
fun upperFirstLetter(s: String?): String {
    if (s.isNullOrBlank()) return ""
    if (!s[0].isLowerCase()) return s

    return s[0].toUpperCase().toString() + s.substring(1)
}

/**
 * Set the first letter of string lower.
 *
 * @param s The string.
 * @return the string with first letter lower.
 */
fun lowerFirstLetter(s: String?): String {
    if (s.isNullOrBlank()) return ""
    if (!s[0].isUpperCase()) return s
    return s[0].toLowerCase().toString() + s.substring(1)
}

/**
 * Reverse the string.
 *
 * @param s The string.
 * @return the reverse string.
 */
fun reverse(s: String?): String {
    if (s == null) return ""
    val len = s.length
    if (len <= 1) return s
    val mid = len shr 1
    val chars = s.toCharArray()
    var c: Char
    for (i in 0 until mid) {
        c = chars[i]
        chars[i] = chars[len - i - 1]
        chars[len - i - 1] = c
    }
    return String(chars)
}

/**
 * Convert string to DBC.
 *
 * @param s The string.
 * @return the DBC string
 */
fun toDBC(s: String?): String {
    if (s.isNullOrBlank()) return ""
    val chars = s.toCharArray()
    var i = 0
    val len = chars.size
    while (i < len) {
        val ch = chars[i].toInt()
        if (ch == 12288) {
            chars[i] = ' '
        } else if (65281 <= ch && ch <= 65374) {
            chars[i] = (chars[i] - 65248)
        } else {
            chars[i] = chars[i]
        }
        i++
    }
    return String(chars)
}

/**
 * Convert string to SBC.
 *
 * @param s The string.
 * @return the SBC string
 */
fun toSBC(s: String?): String {
    if (s.isNullOrBlank()) return ""
    val chars = s.toCharArray()
    var i = 0
    val len = chars.size
    while (i < len) {
        val ch = chars[i].toInt()
        if (chars[i] == ' ') {
            chars[i] = 12288.toChar()
        } else if (33 <= ch && ch <= 126) {
            chars[i] = (chars[i] + 65248)
        } else {
            chars[i] = chars[i]
        }
        i++
    }
    return String(chars)
}


fun isSpace(s: String?): Boolean {
    if (s == null) return true
    var i = 0
    val len = s.length
    while (i < len) {
        if (!Character.isWhitespace(s[i])) {
            return false
        }
        ++i
    }
    return true
}