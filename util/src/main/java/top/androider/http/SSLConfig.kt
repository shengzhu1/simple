package top.androider.http

import android.annotation.SuppressLint
import android.os.Build
import java.io.IOException
import java.net.InetAddress
import java.net.Socket
import java.security.GeneralSecurityException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.*

/**
 * <pre>
 * author: Blankj
 * blog  : http://blankj.com
 * time  : 2019/02/20
</pre> *
 */
class SSLConfig(var mSSLSocketFactory: SSLSocketFactory, var mHostnameVerifier: HostnameVerifier) {
    private class DefaultSSLSocketFactory internal constructor() : SSLSocketFactory() {
        companion object {
            private val PROTOCOL_ARRAY: Array<String>
            private val DEFAULT_TRUST_MANAGERS: Array<TrustManager>

            init {
                PROTOCOL_ARRAY = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    arrayOf("TLSv1", "TLSv1.1", "TLSv1.2")
                } else {
                    arrayOf("SSLv3", "TLSv1", "TLSv1.1", "TLSv1.2")
                }
                DEFAULT_TRUST_MANAGERS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                     arrayOf(
                        object : X509ExtendedTrustManager() {
                            @SuppressLint("TrustAllX509TrustManager")
                            override fun checkClientTrusted(
                                chain: Array<X509Certificate>,
                                authType: String
                            ) { /**/
                            }

                            @SuppressLint("TrustAllX509TrustManager")
                            override fun checkServerTrusted(
                                chain: Array<X509Certificate>,
                                authType: String
                            ) { /**/
                            }

                            override fun getAcceptedIssuers(): Array<X509Certificate> {
                                return arrayOf()
                            }

                            @SuppressLint("TrustAllX509TrustManager")
                            override fun checkClientTrusted(
                                chain: Array<X509Certificate>,
                                authType: String,
                                socket: Socket
                            ) { /**/
                            }

                            @SuppressLint("TrustAllX509TrustManager")
                            override fun checkServerTrusted(
                                chain: Array<X509Certificate>,
                                authType: String,
                                socket: Socket
                            ) { /**/
                            }

                            @SuppressLint("TrustAllX509TrustManager")
                            override fun checkClientTrusted(
                                chain: Array<X509Certificate>,
                                authType: String,
                                engine: SSLEngine
                            ) { /**/
                            }

                            @SuppressLint("TrustAllX509TrustManager")
                            override fun checkServerTrusted(
                                chain: Array<X509Certificate>,
                                authType: String,
                                engine: SSLEngine
                            ) { /**/
                            }
                        }
                    )
                } else {
                    arrayOf(
                        object : X509TrustManager {
                            @SuppressLint("TrustAllX509TrustManager")
                            override fun checkClientTrusted(
                                chain: Array<X509Certificate>,
                                authType: String
                            ) { /**/
                            }

                            @SuppressLint("TrustAllX509TrustManager")
                            override fun checkServerTrusted(
                                chain: Array<X509Certificate>,
                                authType: String
                            ) { /**/
                            }

                            override fun getAcceptedIssuers(): Array<X509Certificate> {
                                return arrayOf()
                            }
                        }
                    )
                }
            }
        }

        private var mFactory: SSLSocketFactory? = null
        override fun getDefaultCipherSuites(): Array<String> {
            return mFactory!!.defaultCipherSuites
        }

        override fun getSupportedCipherSuites(): Array<String> {
            return mFactory!!.supportedCipherSuites
        }

        @Throws(IOException::class)
        override fun createSocket(s: Socket, host: String, port: Int, autoClose: Boolean): Socket {
            val ssl = mFactory!!.createSocket(s, host, port, autoClose)
            setSupportProtocolAndCipherSuites(ssl)
            return ssl
        }

        @Throws(IOException::class)
        override fun createSocket(host: String, port: Int): Socket {
            val ssl = mFactory!!.createSocket(host, port)
            setSupportProtocolAndCipherSuites(ssl)
            return ssl
        }

        @Throws(IOException::class)
        override fun createSocket(
            host: String,
            port: Int,
            localHost: InetAddress,
            localPort: Int
        ): Socket {
            val ssl = mFactory!!.createSocket(host, port, localHost, localPort)
            setSupportProtocolAndCipherSuites(ssl)
            return ssl
        }

        @Throws(IOException::class)
        override fun createSocket(host: InetAddress, port: Int): Socket {
            val ssl = mFactory!!.createSocket(host, port)
            setSupportProtocolAndCipherSuites(ssl)
            return ssl
        }

        @Throws(IOException::class)
        override fun createSocket(
            address: InetAddress,
            port: Int,
            localAddress: InetAddress,
            localPort: Int
        ): Socket {
            val ssl = mFactory!!.createSocket(address, port, localAddress, localPort)
            setSupportProtocolAndCipherSuites(ssl)
            return ssl
        }

        @Throws(IOException::class)
        override fun createSocket(): Socket {
            val ssl = mFactory!!.createSocket()
            setSupportProtocolAndCipherSuites(ssl)
            return ssl
        }

        private fun setSupportProtocolAndCipherSuites(socket: Socket) {
            if (socket is SSLSocket) {
                socket.enabledProtocols =
                    PROTOCOL_ARRAY
            }
        }

        init {
            mFactory = try {
                val sslContext = SSLContext.getInstance("TLS")
                sslContext.init(null, DEFAULT_TRUST_MANAGERS, SecureRandom())
                sslContext.socketFactory
            } catch (e: GeneralSecurityException) {
                throw AssertionError()
            }
        }
    }

    companion object {
        val DEFAULT_VERIFIER = HostnameVerifier { hostname, session -> true }
        val DEFAULT_SSL_SOCKET_FACTORY: SSLSocketFactory = DefaultSSLSocketFactory()
        @JvmField
        val DEFAULT_SSL_CONFIG = SSLConfig(DEFAULT_SSL_SOCKET_FACTORY, DEFAULT_VERIFIER)
    }
}