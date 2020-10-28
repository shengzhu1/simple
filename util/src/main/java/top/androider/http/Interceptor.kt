package top.androider.http

import java.io.IOException

interface Interceptor {
    @Throws(IOException::class)
    fun intercept(chain: Chain?): Response?
}