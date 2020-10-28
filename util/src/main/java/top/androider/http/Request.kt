package top.androider.http

import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.UnsupportedEncodingException
import java.net.MalformedURLException
import java.net.URL
import java.nio.charset.Charset
import java.nio.charset.IllegalCharsetNameException
import java.util.*

/**
 * <pre>
 * author: Blankj
 * blog  : http://blankj.com
 * time  : 2019/02/17
</pre> *
 */
class Request private constructor(url: String) {
    @JvmField
    var mURL: URL? = null
    @JvmField
    var mHeader: MutableMap<String, String>? = null
    @JvmField
    var mBody: Body? = null
    fun addHeader(name: String, value: String): Request {
        if (mHeader == null) {
            mHeader = HashMap()
        }
        mHeader!![name] = value
        return this
    }

    fun addHeader(header: Map<String, String>): Request {
        if (mHeader == null) {
            mHeader = HashMap()
        }
        mHeader!!.putAll(header)
        return this
    }

    fun post(body: Body): Request {
        mBody = body
        return this
    }

    class Body {
        @JvmField
        var mediaType: String
        @JvmField
        var bis: BufferedInputStream? = null
        @JvmField
        var length: Long

        private constructor(mediaType: String, body: ByteArray) {
            this.mediaType = mediaType
            bis = BufferedInputStream(ByteArrayInputStream(body))
            length = body.size.toLong()
        }

        private constructor(mediaType: String, body: InputStream) {
            this.mediaType = mediaType
            bis = if (body is BufferedInputStream) {
                body
            } else {
                BufferedInputStream(body)
            }
            length = -1
        }

        companion object {
            private fun getCharsetFromMediaType(mediaType: String): String {
                var mediaType = mediaType
                mediaType = mediaType.toLowerCase().replace(" ", "")
                val index = mediaType.indexOf("charset=")
                if (index == -1) return "utf-8"
                val st = index + 8
                var end = mediaType.length
                require(st < end) { "MediaType is not correct: \"$mediaType\"" }
                for (i in st until end) {
                    val c = mediaType[i]
                    if (c >= 'A' && c <= 'Z') continue
                    if (c >= 'a' && c <= 'z') continue
                    if (c >= '0' && c <= '9') continue
                    if (c == '-' && i != 0) continue
                    if (c == '+' && i != 0) continue
                    if (c == ':' && i != 0) continue
                    if (c == '_' && i != 0) continue
                    if (c == '.' && i != 0) continue
                    end = i
                    break
                }
                val charset = mediaType.substring(st, end)
                return checkCharset(charset)
            }

            fun create(mediaType: String, content: ByteArray): Body {
                return Body(mediaType, content)
            }

            @JvmOverloads
            fun form(form: Map<String?, String?>, charset: String = "utf-8"): Body? {
                val mediaType = "application/x-www-form-urlencoded;charset=" + checkCharset(charset)
                val sb = StringBuilder()
                for (key in form.keys) {
                    if (sb.length > 0) sb.append("&")
                    sb.append(key).append("=").append(form[key])
                }
                return try {
                    Body(mediaType, sb.toString().toByteArray(charset(charset)))
                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace()
                    null
                }
            }

            @JvmOverloads
            fun json(json: String, charset: String = "utf-8"): Body? {
                val mediaType = "application/json;charset=" + checkCharset(charset)
                return try {
                    Body(mediaType, json.toByteArray(charset(charset)))
                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace()
                    null
                }
            } //        public static RequestBody file(String mediaType, final File file) {
            //
            //            return new RequestBody(mediaType, );
            //        }
        }
    }

    companion object {
        fun withUrl(url: String): Request {
            return Request(url)
        }

        private fun checkCharset(charset: String): String {
            if (Charset.isSupported(charset)) return charset
            throw IllegalCharsetNameException(charset)
        }
    }

    init {
        try {
            mURL = URL(url)
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }
    }
}