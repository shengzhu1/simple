package top.androider.http

/**
 * <pre>
 * author: Blankj
 * blog  : http://blankj.com
 * time  : 2019/02/17
</pre> *
 */
abstract class ResponseCallback {
    abstract fun onResponse(response: Response?)
    abstract fun onFailed(e: Exception?)
}