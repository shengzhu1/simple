package top.androider.http

import android.accounts.NetworkErrorException
import top.androider.util.isSpace
import java.io.*
import java.net.HttpURLConnection
import java.util.*
import javax.net.ssl.HttpsURLConnection

/**
 * <pre>
 * author: Blankj
 * blog  : http://blankj.com
 * time  : 2019/02/08
 * desc  : utils about http
</pre> *
 */
class HttpUtils private constructor(private val mConfig: Config) {
    class Config {
        val sslConfig = SSLConfig.DEFAULT_SSL_CONFIG
        val connectTimeout = CONNECT_TIMEOUT_TIME
        val readTimeout = READ_TIMEOUT_TIME
    }

    internal class Call(private val request: Request, private val callback: ResponseCallback) :
        Runnable {
        override fun run() {
            var conn: HttpURLConnection? = null
            try {
                conn = getConnection(request)
                val responseCode = conn.responseCode
                if (responseCode == 200) {
                    val `is` = conn.inputStream
                    callback.onResponse(Response(conn.headerFields, `is`))
                    `is`.close()
                } else if (responseCode == 301 || responseCode == 302) {
                    val location = conn.getHeaderField("Location")
                    call(request, callback)
                } else {
                    var errorMsg: String? = null
                    val es = conn.errorStream
                    if (es != null) {
                        errorMsg = is2String(es, "utf-8")
                    }
                    callback.onFailed(
                        NetworkErrorException(
                            "error code: " + responseCode +
                                    if (isSpace(errorMsg)) "" else "\nerror message: $errorMsg"
                        )
                    )
                }
            } catch (e: IOException) {
                callback.onFailed(e)
            } finally {
                conn?.disconnect()
            }
        }
    }

    companion object {
        private val BOUNDARY = UUID.randomUUID().toString()
        private const val TWO_HYPHENS = "--"
        private const val CONNECT_TIMEOUT_TIME = 15000
        private const val READ_TIMEOUT_TIME = 20000
        private const val BUFFER_SIZE = 8192
        private val CONFIG = Config()
        private var sHttpUtils: HttpUtils? = null
        fun getInstance(config: Config): HttpUtils? {
            if (sHttpUtils == null) {
                synchronized(HttpUtils::class.java) { sHttpUtils = HttpUtils(config) }
            }
            return sHttpUtils
        }

        fun call(request: Request, callback: ResponseCallback) {
            Call(request, callback).run()
        }

        @Throws(IOException::class)
        private fun getConnection(request: Request): HttpURLConnection {
            val conn = request.mURL!!.openConnection() as HttpURLConnection
            if (conn is HttpsURLConnection) {
                conn.sslSocketFactory = CONFIG.sslConfig.mSSLSocketFactory
                conn.hostnameVerifier = CONFIG.sslConfig.mHostnameVerifier
            }
            println(conn.getHeaderField("USE"))
            addHeader(conn, request.mHeader)
            addBody(conn, request.mBody)
            conn.connectTimeout = CONFIG.connectTimeout
            conn.readTimeout = CONFIG.readTimeout
            return conn
        }

        @Throws(IOException::class)
        private fun addBody(conn: HttpURLConnection, body: Request.Body?) {
            if (body == null) {
                conn.requestMethod = "GET"
            } else {
                conn.requestMethod = "POST"
                conn.useCaches = false
                conn.doOutput = true
                conn.setRequestProperty("content-type", body.mediaType)
                if (body.length > 0) {
                    conn.setRequestProperty("content-length", body.length.toString())
                }
                val bos = BufferedOutputStream(conn.outputStream, 10240)
                if (body.bis != null) {
                    val buffer = ByteArray(10240)
                    var len: Int
                    while (body.bis!!.read(buffer).also { len = it } != -1) {
                        bos.write(buffer, 0, len)
                    }
                    bos.close()
                    body.bis!!.close()
                }
            }
        }

        private fun addHeader(conn: HttpURLConnection, headerMap: Map<String, String>?) {
            if (headerMap != null) {
                for (key in headerMap.keys) {
                    conn.setRequestProperty(key, headerMap[key])
                }
            }
        }

        fun is2String(`is`: InputStream, charset: String?): String {
            val result = ByteArrayOutputStream()
            val buffer = ByteArray(8192)
            return try {
                var len: Int
                while (`is`.read(buffer).also { len = it } != -1) {
                    result.write(buffer, 0, len)
                }
                result.toString(charset)
            } catch (e: Exception) {
                e.printStackTrace()
                ""
            } finally {
                try {
                    `is`.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        fun writeFileFromIS(
            file: File?,
            `is`: InputStream?
        ): Boolean {
            if (!createOrExistsFile(file) || `is` == null) return false
            var os: OutputStream? = null
            return try {
                os = BufferedOutputStream(FileOutputStream(file))
                val data = ByteArray(8192)
                var len: Int
                while (`is`.read(data).also { len = it } != -1) {
                    os.write(data, 0, len)
                }
                true
            } catch (e: IOException) {
                e.printStackTrace()
                false
            } finally {
                try {
                    `is`.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                try {
                    os?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        private fun createOrExistsFile(file: File?): Boolean {
            if (file == null) return false
            if (file.exists()) return file.isFile
            return if (!createOrExistsDir(file.parentFile)) false else try {
                file.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
                false
            }
        }

        private fun createOrExistsDir(file: File?): Boolean {
            return file != null && if (file.exists()) file.isDirectory else file.mkdirs()
        }
    }
}