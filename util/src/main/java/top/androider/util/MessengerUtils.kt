package top.androider.util

import android.annotation.SuppressLint
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.text.TextUtils
import android.util.Log
import top.androider.util.Utils.Companion.app
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * <pre>
 * author: Blankj
 * blog  : http://blankj.com
 * time  : 2019/07/10
 * desc  : utils about messenger
</pre> *
 */
object MessengerUtils {
    private val subscribers = ConcurrentHashMap<String, MessageCallback>()
    private val sClientMap: MutableMap<String, Client?> = HashMap()
    private var sLocalClient: Client? = null
    private const val WHAT_SUBSCRIBE = 0x00
    private const val WHAT_UNSUBSCRIBE = 0x01
    private const val WHAT_SEND = 0x02
    private const val KEY_STRING = "MESSENGER_UTILS"
    fun register() {
        if (isMainProcess) {
            if (isServiceRunning(ServerService::class.java.name)) {
                Log.i("MessengerUtils", "Server service is running.")
                return
            }
            val intent = Intent(app, ServerService::class.java)
            app!!.startService(intent)
            return
        }
        if (sLocalClient == null) {
            val client = Client(null)
            if (client.bind()) {
                sLocalClient = client
            } else {
                Log.e("MessengerUtils", "Bind service failed.")
            }
        } else {
            Log.i("MessengerUtils", "The client have been bind.")
        }
    }

    fun unregister() {
        if (isMainProcess) {
            if (!isServiceRunning(ServerService::class.java.name)) {
                Log.i("MessengerUtils", "Server service isn't running.")
                return
            }
            val intent = Intent(app, ServerService::class.java)
            app!!.stopService(intent)
        }
        if (sLocalClient != null) {
            sLocalClient!!.unbind()
        }
    }

    fun register(pkgName: String) {
        if (sClientMap.containsKey(pkgName)) {
            Log.i("MessengerUtils", "register: client registered: $pkgName")
            return
        }
        val client = Client(pkgName)
        if (client.bind()) {
            sClientMap[pkgName] = client
        } else {
            Log.e("MessengerUtils", "register: client bind failed: $pkgName")
        }
    }

    fun unregister(pkgName: String) {
        if (!sClientMap.containsKey(pkgName)) {
            Log.i("MessengerUtils", "unregister: client didn't register: $pkgName")
            return
        }
        val client = sClientMap[pkgName]
        sClientMap.remove(pkgName)
        client?.unbind()
    }

    fun subscribe(key: String, callback: MessageCallback) {
        subscribers[key] = callback
    }

    fun unsubscribe(key: String) {
        subscribers.remove(key)
    }

    fun post(key: String, data: Bundle) {
        data.putString(KEY_STRING, key)
        if (sLocalClient != null) {
            sLocalClient!!.sendMsg2Server(data)
        } else {
            val intent = Intent(app, ServerService::class.java)
            intent.putExtras(data)
            app!!.startService(intent)
        }
        for (client in sClientMap.values) {
            client!!.sendMsg2Server(data)
        }
    }

    internal class Client(var mPkgName: String?) {
        var mServer: Messenger? = null
        var mCached = LinkedList<Bundle>()

        @SuppressLint("HandlerLeak")
        var mReceiveServeMsgHandler: Handler = object : Handler() {
            override fun handleMessage(msg: Message) {
                val data = msg.data
                data.classLoader = MessengerUtils::class.java.classLoader
                data.getString(KEY_STRING)?.apply {
                    val callback = subscribers[this]
                    callback?.messageCall(data)
                }
            }
        }
        var mClient = Messenger(mReceiveServeMsgHandler)
        var mConn: ServiceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                Log.d("MessengerUtils", "client service connected $name")
                mServer = Messenger(service)
                val key = currentProcessName.hashCode()
                val msg = Message.obtain(mReceiveServeMsgHandler, WHAT_SUBSCRIBE, key, 0)
                msg.data.classLoader = MessengerUtils::class.java.classLoader
                msg.replyTo = mClient
                try {
                    mServer!!.send(msg)
                } catch (e: RemoteException) {
                    Log.e("MessengerUtils", "onServiceConnected: ", e)
                }
                sendCachedMsg2Server()
            }

            override fun onServiceDisconnected(name: ComponentName) {
                Log.w("MessengerUtils", "client service disconnected:$name")
                mServer = null
                if (!bind()) {
                    Log.e("MessengerUtils", "client service rebind failed: $name")
                }
            }
        }

        fun bind(): Boolean {
            if (TextUtils.isEmpty(mPkgName)) {
                val intent = Intent(app, ServerService::class.java)
                return app!!.bindService(intent, mConn, Context.BIND_AUTO_CREATE)
            }
            return if (AppUtils.isAppInstalled(mPkgName)) {
                if (AppUtils.isAppRunning(mPkgName!!)) {
                    val intent = Intent("$mPkgName.messenger")
                    intent.setPackage(mPkgName)
                    app!!.bindService(
                        intent,
                        mConn,
                        Context.BIND_AUTO_CREATE
                    )
                } else {
                    Log.e("MessengerUtils", "bind: the app is not running -> $mPkgName")
                    false
                }
            } else {
                Log.e("MessengerUtils", "bind: the app is not installed -> $mPkgName")
                false
            }
        }

        fun unbind() {
            val key = currentProcessName.hashCode()
            val msg = Message.obtain(mReceiveServeMsgHandler, WHAT_UNSUBSCRIBE, key, 0)
            msg.replyTo = mClient
            try {
                mServer!!.send(msg)
            } catch (e: RemoteException) {
                Log.e("MessengerUtils", "unbind: ", e)
            }
            try {
                app!!.unbindService(mConn)
            } catch (ignore: Exception) { /*ignore*/
            }
        }

        fun sendMsg2Server(bundle: Bundle) {
            if (mServer == null) {
                mCached.addFirst(bundle)
                Log.i("MessengerUtils", "save the bundle $bundle")
            } else {
                sendCachedMsg2Server()
                if (!send2Server(bundle)) {
                    mCached.addFirst(bundle)
                }
            }
        }

        private fun sendCachedMsg2Server() {
            if (mCached.isEmpty()) return
            for (i in mCached.indices.reversed()) {
                if (send2Server(mCached[i])) {
                    mCached.removeAt(i)
                }
            }
        }

        private fun send2Server(bundle: Bundle): Boolean {
            val msg = Message.obtain(mReceiveServeMsgHandler, WHAT_SEND)
            bundle.classLoader = MessengerUtils::class.java.classLoader
            msg.data = bundle
            msg.replyTo = mClient
            return try {
                mServer!!.send(msg)
                true
            } catch (e: RemoteException) {
                Log.e("MessengerUtils", "send2Server: ", e)
                false
            }
        }
    }

    class ServerService : Service() {
        private val mClientMap = ConcurrentHashMap<Int, Messenger>()

        @SuppressLint("HandlerLeak")
        private val mReceiveClientMsgHandler: Handler = object : Handler() {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    WHAT_SUBSCRIBE -> mClientMap[msg.arg1] = msg.replyTo
                    WHAT_SEND -> {
                        sendMsg2Client(msg)
                        consumeServerProcessCallback(msg)
                    }
                    WHAT_UNSUBSCRIBE -> mClientMap.remove(msg.arg1)
                    else -> super.handleMessage(msg)
                }
            }
        }
        private val messenger = Messenger(mReceiveClientMsgHandler)
        override fun onBind(intent: Intent): IBinder? {
            return messenger.binder
        }

        override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
            if (intent != null) {
                val extras = intent.extras
                if (extras != null) {
                    val msg = Message.obtain(mReceiveClientMsgHandler, WHAT_SEND)
                    msg.replyTo = messenger
                    msg.data = extras
                    sendMsg2Client(msg)
                    consumeServerProcessCallback(msg)
                }
            }
            return START_NOT_STICKY
        }

        private fun sendMsg2Client(msg: Message) {
            for (client in mClientMap.values) {
                try {
                    client?.send(msg)
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }
            }
        }

        private fun consumeServerProcessCallback(msg: Message) {
            val data = msg.data
            if (data != null) {
                val key = data.getString(KEY_STRING)
                if (key != null) {
                    val callback = subscribers[key]
                    callback?.messageCall(data)
                }
            }
        }
    }

    interface MessageCallback {
        fun messageCall(data: Bundle?)
    }
}