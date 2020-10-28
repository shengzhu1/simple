package top.androider.util

import android.annotation.SuppressLint
import android.os.Build
import android.os.Environment
import android.text.TextUtils
import java.io.*
import java.util.*


class RomInfo {
    var name: String? = null
    var version: String? = null
    override fun toString(): String {
        return "RomInfo{name=" + name +
                ", version=" + version + "}"
    }
}

private val ROM_HUAWEI = arrayOf("huawei")
private val ROM_VIVO = arrayOf("vivo")
private val ROM_XIAOMI = arrayOf("xiaomi")
private val ROM_OPPO = arrayOf("oppo")
private val ROM_LEECO = arrayOf("leeco", "letv")
private val ROM_360 = arrayOf("360", "qiku")
private val ROM_ZTE = arrayOf("zte")
private val ROM_ONEPLUS = arrayOf("oneplus")
private val ROM_NUBIA = arrayOf("nubia")
private val ROM_COOLPAD = arrayOf("coolpad", "yulong")
private val ROM_LG = arrayOf("lg", "lge")
private val ROM_GOOGLE = arrayOf("google")
private val ROM_SAMSUNG = arrayOf("samsung")
private val ROM_MEIZU = arrayOf("meizu")
private val ROM_LENOVO = arrayOf("lenovo")
private val ROM_SMARTISAN = arrayOf("smartisan")
private val ROM_HTC = arrayOf("htc")
private val ROM_SONY = arrayOf("sony")
private val ROM_GIONEE = arrayOf("gionee", "amigo")
private val ROM_MOTOROLA = arrayOf("motorola")
private const val UNKNOWN = "unknown"

private val romMap = mutableMapOf<String,String>(
    ROM_HUAWEI[0] to "ro.build.version.emui",
    ROM_VIVO[0] to "ro.vivo.os.build.display.id",
    ROM_XIAOMI[0] to "ro.build.version.incremental",
    ROM_OPPO[0] to "ro.build.version.opporom",
    ROM_LEECO[0] to "ro.letv.release.version",
    ROM_360[0] to "ro.build.uiversion",
    ROM_ZTE[0] to "ro.build.MiFavor_version",
    ROM_ONEPLUS[0] to "ro.rom.version",
    ROM_NUBIA[0] to "ro.build.rom.id"
)
private val romInfo by lazy<RomInfo> {
    RomInfo().let { bean :RomInfo->

        val brand = getBrand()
        val manufacturer = getManufacturer()
        arrayListOf<Array<String>>(
            ROM_HUAWEI,ROM_VIVO,ROM_XIAOMI,ROM_OPPO,ROM_LEECO,ROM_360,ROM_ZTE,
            ROM_ONEPLUS,ROM_NUBIA,ROM_COOLPAD,ROM_LG ,ROM_GOOGLE,ROM_SAMSUNG,
            ROM_MEIZU,ROM_LENOVO,ROM_SMARTISAN,ROM_HTC,ROM_SONY,ROM_GIONEE,ROM_MOTOROLA
        ).forEach { names ->
            if (isRightRom(bean,brand,manufacturer,*names)){
                return@let bean
            }
        }
        bean.name = manufacturer
        bean.version = getRomVersion("")
        bean
    }
}

/**
 * Return whether the rom is made by huawei.
 *
 * @return `true`: yes<br></br>`false`: no
 */
val isHuawei: Boolean
    get() = ROM_HUAWEI[0] == romInfo.name

/**
 * Return whether the rom is made by vivo.
 *
 * @return `true`: yes<br></br>`false`: no
 */
val isVivo: Boolean
    get() = ROM_VIVO[0] == romInfo.name

/**
 * Return whether the rom is made by xiaomi.
 *
 * @return `true`: yes<br></br>`false`: no
 */
val isXiaomi: Boolean
    get() = ROM_XIAOMI[0] == romInfo.name

/**
 * Return whether the rom is made by oppo.
 *
 * @return `true`: yes<br></br>`false`: no
 */
val isOppo: Boolean
    get() = ROM_OPPO[0] == romInfo.name

/**
 * Return whether the rom is made by leeco.
 *
 * @return `true`: yes<br></br>`false`: no
 */
val isLeeco: Boolean
    get() = ROM_LEECO[0] == romInfo.name

/**
 * Return whether the rom is made by 360.
 *
 * @return `true`: yes<br></br>`false`: no
 */
fun is360(): Boolean {
    return ROM_360[0] == romInfo.name
}

/**
 * Return whether the rom is made by zte.
 *
 * @return `true`: yes<br></br>`false`: no
 */
val isZte: Boolean
    get() = ROM_ZTE[0] == romInfo.name

/**
 * Return whether the rom is made by oneplus.
 *
 * @return `true`: yes<br></br>`false`: no
 */
val isOneplus: Boolean
    get() = ROM_ONEPLUS[0] == romInfo.name

/**
 * Return whether the rom is made by nubia.
 *
 * @return `true`: yes<br></br>`false`: no
 */
val isNubia: Boolean
    get() = ROM_NUBIA[0] == romInfo.name

/**
 * Return whether the rom is made by coolpad.
 *
 * @return `true`: yes<br></br>`false`: no
 */
val isCoolpad: Boolean
    get() = ROM_COOLPAD[0] == romInfo.name

/**
 * Return whether the rom is made by lg.
 *
 * @return `true`: yes<br></br>`false`: no
 */
val isLg: Boolean
    get() = ROM_LG[0] == romInfo.name

/**
 * Return whether the rom is made by google.
 *
 * @return `true`: yes<br></br>`false`: no
 */
val isGoogle: Boolean
    get() = ROM_GOOGLE[0] == romInfo.name

/**
 * Return whether the rom is made by samsung.
 *
 * @return `true`: yes<br></br>`false`: no
 */
val isSamsung: Boolean
    get() = ROM_SAMSUNG[0] == romInfo.name

/**
 * Return whether the rom is made by meizu.
 *
 * @return `true`: yes<br></br>`false`: no
 */
val isMeizu: Boolean
    get() = ROM_MEIZU[0] == romInfo.name

/**
 * Return whether the rom is made by lenovo.
 *
 * @return `true`: yes<br></br>`false`: no
 */
val isLenovo: Boolean
    get() = ROM_LENOVO[0] == romInfo.name

/**
 * Return whether the rom is made by smartisan.
 *
 * @return `true`: yes<br></br>`false`: no
 */
val isSmartisan: Boolean
    get() = ROM_SMARTISAN[0] == romInfo.name

/**
 * Return whether the rom is made by htc.
 *
 * @return `true`: yes<br></br>`false`: no
 */
val isHtc: Boolean
    get() = ROM_HTC[0] == romInfo.name

/**
 * Return whether the rom is made by sony.
 *
 * @return `true`: yes<br></br>`false`: no
 */
val isSony: Boolean
    get() = ROM_SONY[0] == romInfo.name

/**
 * Return whether the rom is made by gionee.
 *
 * @return `true`: yes<br></br>`false`: no
 */
val isGionee: Boolean
    get() = ROM_GIONEE[0] == romInfo.name

/**
 * Return whether the rom is made by motorola.
 *
 * @return `true`: yes<br></br>`false`: no
 */
val isMotorola: Boolean
    get() = ROM_MOTOROLA[0] == romInfo.name

private fun isRightRom(brand: String, manufacturer: String, vararg names: String): Boolean {
    for (name in names) {
        if (brand.contains(name) || manufacturer.contains(name)) {
            return true
        }
    }
    return false
}

private fun isRightRom(bean:RomInfo,brand: String, manufacturer: String, vararg names: String): Boolean {
    if (isRightRom(brand, manufacturer, *names)){
        bean.name = names[0]
        val versionProp = romMap[names[0]]?:""
        val version = getRomVersion(versionProp)
        val temp = version.split("_").toTypedArray()
        if (temp.size > 1) {
            bean.version = temp[1]
        } else {
            bean.version = version
        }
        return true
    }
    return false
}

/**/
private fun getManufacturer() = run {
    try {
        Build.MANUFACTURER?.takeIf { it.isNotEmpty() }?.toLowerCase()?: UNKNOWN
    } catch (ignore: Throwable) { /**/
    }
    UNKNOWN
}

/**/
private fun getBrand() = run {
    try {
        Build.BRAND?.takeIf { it.isNotEmpty() }?.toLowerCase()?: UNKNOWN
    } catch (ignore: Throwable) { /**/
    }
    UNKNOWN
}

private fun getRomVersion(propertyName: String): String {
    var ret = ""
    if (!TextUtils.isEmpty(propertyName)) {
        ret = getSystemProperty(propertyName)
    }
    if (TextUtils.isEmpty(ret) || ret == UNKNOWN) {
        try {
            val display = Build.DISPLAY
            if (!TextUtils.isEmpty(display)) {
                ret = display.toLowerCase()
            }
        } catch (ignore: Throwable) { /**/
        }
    }
    return if (TextUtils.isEmpty(ret)) {
        UNKNOWN
    } else ret
}

private fun getSystemProperty(name: String): String {
    var prop = getSystemPropertyByShell(name)
    if (!TextUtils.isEmpty(prop)) return prop
    prop = getSystemPropertyByStream(name)
    if (!TextUtils.isEmpty(prop)) return prop
    return if (Build.VERSION.SDK_INT < 28) {
        getSystemPropertyByReflect(name)
    } else prop
}

private fun getSystemPropertyByShell(propName: String): String {
    var input: BufferedReader? = null
    try {
        val p = Runtime.getRuntime().exec("getprop $propName")
        input = BufferedReader(InputStreamReader(p.inputStream), 1024)
        val ret = input.readLine()
        if (ret != null) {
            return ret
        }
    } catch (ignore: IOException) {
    } finally {
        try {
            input?.close()
        } catch (ignore: IOException) { /**/
        }
    }
    return ""
}

private fun getSystemPropertyByStream(key: String): String {
    try {
        val prop = Properties()
        val `is` = FileInputStream(
            File(Environment.getRootDirectory(), "build.prop")
        )
        prop.load(`is`)
        return prop.getProperty(key, "")
    } catch (ignore: Exception) { /**/
    }
    return ""
}

private fun getSystemPropertyByReflect(key: String): String {
    try {
        @SuppressLint("PrivateApi") val clz = Class.forName("android.os.SystemProperties")
        val getMethod = clz.getMethod("get", String::class.java, String::class.java)
        return getMethod.invoke(clz, key, "") as String
    } catch (e: Exception) { /**/
    }
    return ""
}