package top.androider.util

import java.io.Closeable
import java.io.IOException

/**
 * <pre>
 * author: Blankj
 * blog  : http://blankj.com
 * time  : 2016/10/09
 * desc  : utils about close
</pre> *
 */
/**
 * Close the io stream.
 *
 * @param closeables The closeables.
 */
fun closeIO(quietly: Boolean = false,vararg closeables: Closeable?) {
    if (closeables == null) return
    for (closeable in closeables) {
        try {
            closeable?.close()
        } catch (e: IOException) {
            if (quietly){
                e.printStackTrace()
            }
        }
    }
}