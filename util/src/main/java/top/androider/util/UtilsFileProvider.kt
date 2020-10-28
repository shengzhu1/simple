package top.androider.util

import android.app.Application
import androidx.core.content.FileProvider
import top.androider.util.Utils.Companion.app
import top.androider.util.Utils.Companion.init

/**
 * <pre>
 * author: blankj
 * blog  : http://blankj.com
 * time  : 2020/03/19
 * desc  :
</pre> *
 */
class UtilsFileProvider : FileProvider() {
    override fun onCreate(): Boolean {
        init(context!!.applicationContext as Application)
        return true
    }

    companion object {
        @JvmStatic
        val fileAuthority: String
            get() = app!!.packageName + ".provider"
    }
}