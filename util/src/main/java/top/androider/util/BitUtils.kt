package top.androider.util

import android.util.Log


/**
 * create by lushengzhu 2020/10/11
 */
/** Shifts this value left by the [bitCount] number of bits. */
infix fun Byte.shl(bitCount: Int): Byte = (this.toInt() shl  bitCount).toByte()
/** Shifts this value right by the [bitCount] number of bits, filling the leftmost bits with copies of the sign bit. */
infix fun Byte.shr(bitCount: Int): Byte = (this.toInt() shr  bitCount).toByte()
/** Shifts this value right by the [bitCount] number of bits, filling the leftmost bits with zeros. */
infix fun Byte.ushr(bitCount: Int): Byte = (this.toInt() ushr  bitCount).toByte()
/** Performs a bitwise AND operation between the two values. */
infix fun Byte.and(other: Byte): Byte = (this.toInt() and  other.toInt()).toByte()
/** Performs a bitwise OR operation between the two values. */
infix fun Byte.or(other: Byte): Byte = (this.toInt() or  other.toInt()).toByte()
/** Performs a bitwise XOR operation between the two values. */
infix fun Byte.xor(other: Byte): Byte = (this.toInt() xor  other.toInt()).toByte()

infix fun Byte.equals(other: Int): Boolean = (this.toInt() == other)

/** Inverts the bits in this value. */
fun Byte.inv(): Byte = (this.toInt().inv()).toByte()


/**
 * create by lushengzhu 2020/10/11
 */
/** Shifts this value left by the [bitCount] number of bits. */
infix fun Short.shl(bitCount: Int): Short = (this.toInt() shl  bitCount).toShort()
/** Shifts this value right by the [bitCount] number of bits, filling the leftmost bits with copies of the sign bit. */
infix fun Short.shr(bitCount: Int): Short = (this.toInt() shr  bitCount).toShort()
/** Shifts this value right by the [bitCount] number of bits, filling the leftmost bits with zeros. */
infix fun Short.ushr(bitCount: Int): Short = (this.toInt() ushr  bitCount).toShort()
/** Performs a bitwise AND operation between the two values. */
infix fun Short.and(other: Short): Short = (this.toInt() and  other.toInt()).toShort()
/** Performs a bitwise OR operation between the two values. */
infix fun Short.or(other: Short): Short = (this.toInt() or  other.toInt()).toShort()
/** Performs a bitwise XOR operation between the two values. */
infix fun Short.xor(other: Short): Short = (this.toInt() xor  other.toInt()).toShort()

infix fun Short.equals(other: Int): Boolean = (this.toInt() == other)

/** Inverts the bits in this value. */
fun Short.inv(): Short = (this.toInt().inv()).toShort()
/**
 * 获取运算数指定位置的值<br></br>
 * 例如： 0000 1011 获取其第 0 位的值为 1, 第 2 位 的值为 0<br></br>
 *
 * @param source 需要运算的数
 * @param pos    指定位置 (0...7)
 * @return 指定位置的值(0 or 1)
 */
fun getBitValue(source: Byte, pos: Int): Byte {
    return source shr pos and 1
}

/**
 * 将运算数指定位置的值置为指定值<br></br>
 * 例: 0000 1011 需要更新为 0000 1111, 即第 2 位的值需要置为 1<br></br>
 *
 * @param source 需要运算的数
 * @param pos    指定位置 (0<=pos<=7)
 * @param value  只能取值为 0, 或 1, 所有大于0的值作为1处理, 所有小于0的值作为0处理
 * @return 运算后的结果数
 */
fun setBitValue(source: Byte, pos: Int, value: Byte): Byte {
    var source = source
    val mask = (1 shl pos).toByte()
    if (value > 0) {
        source = source or mask
    } else {
        source = source and mask.inv()
    }
    return source
}

/**
 * 将运算数指定位置取反值<br></br>
 * 例： 0000 1011 指定第 3 位取反, 结果为 0000 0011; 指定第2位取反, 结果为 0000 1111<br></br>
 *
 * @param source
 * @param pos    指定位置 (0<=pos<=7)
 * @return 运算后的结果数
 */
fun reverseBitValue(source: Byte, pos: Int): Byte {
    val mask = (1 shl pos).toByte()
    return (source xor mask) as Byte
}

/**
 * 检查运算数的指定位置是否为1<br></br>
 *
 * @param source 需要运算的数
 * @param pos    指定位置 (0<=pos<=7)
 * @return true 表示指定位置值为1, false 表示指定位置值为 0
 */
fun checkBitValue(source: Byte, pos: Int): Boolean {
    var source = source
    source = (source ushr pos) as Byte
    return (source and 1).toInt() == 1
}

/**
 * 入口函数做测试<br></br>
 *
 * @param args
 */
fun main(args: Array<String>) {
    // 取十进制 11 (二级制 0000 1011) 为例子
    val source: Byte = 11
    // 取第2位值并输出, 结果应为 0000 1011
    for (i in 7 downTo 0) {
        Log.d("BitUtils", getBitValue(source, i).toString() + "")
    }
    // 将第6位置为1并输出 , 结果为 75 (0100 1011)
    Log.d("BitUtils", setBitValue(source, 6, 1.toByte()).toString() + "")

    // 将第6位取反并输出, 结果应为75(0100 1011)
    Log.d("BitUtils", reverseBitValue(source, 6).toString() + "")

    // 检查第6位是否为1，结果应为false
    Log.d("BitUtils", checkBitValue(source, 6).toString() + "")

    // 输出为1的位, 结果应为 0 1 3
    for (i in 0..7) {
        if (checkBitValue(source, i)) {
            Log.d("BitUtils", i.toString() + "")
        }
    }
}
