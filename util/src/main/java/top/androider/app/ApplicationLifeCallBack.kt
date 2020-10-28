package top.androider.app

import android.content.Context
import android.content.res.Configuration

interface ApplicationLifeCallBack {

    fun onCreate() 

    fun attachBaseContext(base: Context?)

    fun onTerminate()

    fun onConfigurationChanged(newConfig: Configuration)

    fun onLowMemory()
}