package top.androider.app

import android.content.Context
import android.content.res.Configuration

/**
 * create by lushengzhu 2020/10/7
 */
class AbsApplicationLifeCallBack : ApplicationLifeCallBack {
    override fun onCreate() {

    }

    override fun attachBaseContext(base: Context?) {
    }

    override fun onTerminate() {
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
    }

    override fun onLowMemory() {
    }
}