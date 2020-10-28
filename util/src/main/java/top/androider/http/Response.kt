package top.androider.http

import com.google.gson.Gson
import top.androider.util.fromJson
import java.io.File
import java.io.InputStream
import java.lang.reflect.Type

/**
 * <pre>
 * author: Blankj
 * blog  : http://blankj.com
 * time  : 2019/02/17
</pre> *
 */
class Response(val headers: Map<String, List<String>>, val body: InputStream) {
    val string: String
        get() = getString("utf-8")

    fun getString(charset: String?): String {
        return HttpUtils.is2String(body, charset)
    }

    fun <T> getJson(type: Type?): T? {
        return getJson(type, "utf-8")
    }

    fun <T> getJson(type: Type?, charset: String?): T? {
        return getString(charset).fromJson(type = type!!)
    }

    fun downloadFile(file: File?): Boolean {
        return HttpUtils.writeFileFromIS(file, body)
    }
}