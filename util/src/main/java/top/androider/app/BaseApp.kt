package top.androider.app

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import top.androider.util.Utils

open class BaseApp : Application(){

    companion object{
        lateinit var app: BaseApp

        private var callBacks = mutableListOf<ApplicationLifeCallBack>()

        fun addCallBack(callBack: ApplicationLifeCallBack){
            callBacks.add(callBack)
            app.baseContext?.apply {
                callBack.attachBaseContext(this)
                callBack.onCreate()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        app = this
        Utils.init(this)
        callBacks.forEach {
            it.onCreate()
        }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        callBacks.forEach {
            it.attachBaseContext(base)
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        callBacks.forEach {
            it.onTerminate()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        callBacks.forEach {
            it.onConfigurationChanged(newConfig)
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        callBacks.forEach {
            it.onLowMemory()
        }
    }

}