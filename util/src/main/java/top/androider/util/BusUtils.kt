package top.androider.util

import android.util.Log
import top.androider.util.ThreadUtils.cachedPool
import top.androider.util.ThreadUtils.cpuPool
import top.androider.util.ThreadUtils.ioPool
import top.androider.util.ThreadUtils.runOnUiThread
import top.androider.util.ThreadUtils.singlePool
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CopyOnWriteArraySet
/**
 * <pre>
 * author: Blankj
 * blog  : http://blankj.com
 * time  : 2018/10/02
 * desc  : utils about bus
</pre> *
 */
class BusUtils private constructor() {
    private val mTag_BusInfoListMap: MutableMap<String?, MutableList<BusInfo>?> = HashMap()
    private val mClassName_BusesMap: MutableMap<String, MutableSet<Any>> = ConcurrentHashMap()
    private val mClassName_TagsMap: MutableMap<String, MutableList<String?>> = ConcurrentHashMap()
    private val mClassName_Tag_Arg4StickyMap: MutableMap<String, MutableMap<String, Any>> =
        ConcurrentHashMap()

    /**
     * It'll be injected the bus who have [Bus] annotation
     * by function of [BusUtils.registerBus] when execute transform task.
     */
    private fun init() { /*inject*/
    }

    private fun registerBus(
        tag: String,
        className: String, funName: String, paramType: String, paramName: String,
        sticky: Boolean, threadMode: String, priority: Int = 0
    ) {
        var busInfoList = mTag_BusInfoListMap[tag]
        if (busInfoList == null) {
            busInfoList = ArrayList()
            mTag_BusInfoListMap[tag] = busInfoList
        }
        busInfoList.add(
            BusInfo(
                tag,
                className,
                funName,
                paramType,
                paramName,
                sticky,
                threadMode,
                priority
            )
        )
    }

    override fun toString(): String {
        return "BusUtils: $mTag_BusInfoListMap"
    }

    private fun registerInner(bus: Any?) {
        if (bus == null) return
        val aClass: Class<*> = bus.javaClass
        val className = aClass.name
        var isNeedRecordTags = false
        synchronized(mClassName_BusesMap) {
            var buses = mClassName_BusesMap[className]
            if (buses == null) {
                buses = CopyOnWriteArraySet()
                mClassName_BusesMap[className] = buses
                isNeedRecordTags = true
            }
            if (buses.contains(bus)) {
                Log.w(TAG, "The bus of <$bus> already registered.")
                return
            } else {
                buses.add(bus)
            }
        }
        if (isNeedRecordTags) {
            recordTags(aClass, className)
        }
        consumeStickyIfExist(bus)
    }

    private fun recordTags(aClass: Class<*>, className: String) {
        var tags = mClassName_TagsMap[className]
        if (tags == null) {
            synchronized(mClassName_TagsMap) {
                tags = mClassName_TagsMap[className]
                if (tags == null) {
                    tags = CopyOnWriteArrayList()
                    for ((key, value) in mTag_BusInfoListMap) {
                        for (busInfo in value!!) {
                            try {
                                if (Class.forName(busInfo.className).isAssignableFrom(aClass)) {
                                    tags?.add(key)
                                    busInfo.subClassNames.add(className)
                                }
                            } catch (e: ClassNotFoundException) {
                                e.printStackTrace()
                            }
                        }
                    }
                    tags?.let{
                        mClassName_TagsMap[className] = it
                    }
                }
            }
        }
    }

    private fun consumeStickyIfExist(bus: Any) {
        val tagArgMap = mClassName_Tag_Arg4StickyMap[bus.javaClass.name]
            ?: return
        synchronized(mClassName_Tag_Arg4StickyMap) {
            for ((key, value) in tagArgMap) {
                consumeSticky(bus, key, value)
            }
        }
    }

    private fun consumeSticky(bus: Any, tag: String, arg: Any) {
        val busInfoList: List<BusInfo>? = mTag_BusInfoListMap[tag]
        if (busInfoList == null) {
            Log.e(TAG, "The bus of tag <$tag> is not exists.")
            return
        }
        for (busInfo in busInfoList) {
            if (!busInfo.subClassNames.contains(bus.javaClass.name)) {
                continue
            }
            if (!busInfo.sticky) {
                continue
            }
            synchronized(mClassName_Tag_Arg4StickyMap) {
                val tagArgMap: Map<String, Any>? = mClassName_Tag_Arg4StickyMap[busInfo.className]
                tagArgMap?.takeIf { it.containsKey(tag) }?.let {
                    invokeBus(bus, arg, busInfo, true)
                }
            }
        }
    }

    private fun unregisterInner(bus: Any?) {
        if (bus == null) return
        val className = bus.javaClass.name
        synchronized(mClassName_BusesMap) {
            val buses = mClassName_BusesMap[className]
            if (buses == null || !buses.contains(bus)) {
                Log.e(TAG, "The bus of <$bus> was not registered before.")
                return
            }
            buses.remove(bus)
        }
    }

    private fun postInner(tag: String, arg: Any, sticky: Boolean = false) {
        val busInfoList: List<BusInfo>? = mTag_BusInfoListMap[tag]
        if (busInfoList == null) {
            Log.e(TAG, "The bus of tag <$tag> is not exists.")
            if (mTag_BusInfoListMap.isEmpty()) {
                Log.e(TAG, "Please check whether the bus plugin is applied.")
            }
            return
        }
        for (busInfo in busInfoList) {
            invokeBus(arg, busInfo, sticky)
        }
    }

    private fun invokeBus(arg: Any, busInfo: BusInfo, sticky: Boolean) {
        invokeBus(null, arg, busInfo, sticky)
    }

    private fun invokeBus(bus: Any?, arg: Any, busInfo: BusInfo, sticky: Boolean) {
        if (busInfo.method == null) {
            val method = getMethodByBusInfo(busInfo) ?: return
            busInfo.method = method
        }
        invokeMethod(bus, arg, busInfo, sticky)
    }

    private fun getMethodByBusInfo(busInfo: BusInfo): Method? {
        try {
            return if ("" == busInfo.paramType) {
                Class.forName(busInfo.className).getDeclaredMethod(busInfo.funName)
            } else {
                Class.forName(busInfo.className)
                    .getDeclaredMethod(busInfo.funName, getClassName(busInfo.paramType))
            }
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        }
        return null
    }

    @Throws(ClassNotFoundException::class)
    private fun getClassName(paramType: String): Class<*>? {
        return when (paramType) {
            "boolean" -> Boolean::class.javaPrimitiveType
            "int" -> Int::class.javaPrimitiveType
            "long" -> Long::class.javaPrimitiveType
            "short" -> Short::class.javaPrimitiveType
            "byte" -> Byte::class.javaPrimitiveType
            "double" -> Double::class.javaPrimitiveType
            "float" -> Float::class.javaPrimitiveType
            "char" -> Char::class.javaPrimitiveType
            else -> Class.forName(paramType)
        }
    }

    private fun invokeMethod(arg: Any, busInfo: BusInfo, sticky: Boolean) {
        invokeMethod(null, arg, busInfo, sticky)
    }

    private fun invokeMethod(bus: Any?, arg: Any, busInfo: BusInfo, sticky: Boolean) {
        val runnable = Runnable { realInvokeMethod(bus, arg, busInfo, sticky) }
        when (busInfo.threadMode) {
            "MAIN" -> {
                runOnUiThread(runnable)
                return
            }
            "IO" -> {
                ioPool!!.execute(runnable)
                return
            }
            "CPU" -> {
                cpuPool!!.execute(runnable)
                return
            }
            "CACHED" -> {
                cachedPool!!.execute(runnable)
                return
            }
            "SINGLE" -> {
                singlePool!!.execute(runnable)
                return
            }
            else -> runnable.run()
        }
    }

    private fun realInvokeMethod(bus: Any?, arg: Any, busInfo: BusInfo, sticky: Boolean) {
        val buses: MutableSet<Any> = HashSet()
        if (bus == null) {
            for (subClassName in busInfo.subClassNames) {
                val subBuses: Set<Any>? = mClassName_BusesMap[subClassName]
                if (subBuses != null && !subBuses.isEmpty()) {
                    buses.addAll(subBuses)
                }
            }
            if (buses.size == 0) {
                if (!sticky) {
                    Log.e(TAG, "The $busInfo was not registered before.")
                }
                return
            }
        } else {
            buses.add(bus)
        }
        invokeBuses(arg, busInfo, buses)
    }

    private fun invokeBuses(arg: Any, busInfo: BusInfo, buses: Set<Any>) {
        try {
            if (arg === NULL) {
                for (bus in buses) {
                    busInfo.method!!.invoke(bus)
                }
            } else {
                for (bus in buses) {
                    busInfo.method!!.invoke(bus, arg)
                }
            }
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
    }

    private fun postStickyInner(tag: String, arg: Any) {
        val busInfoList: List<BusInfo>? = mTag_BusInfoListMap[tag]
        if (busInfoList == null) {
            Log.e(TAG, "The bus of tag <$tag> is not exists.")
            return
        }
        // 获取多对象，然后消费各个 busInfoList
        for (busInfo in busInfoList) {
            if (!busInfo.sticky) { // not sticky bus will post directly.
                invokeBus(arg, busInfo, false)
                continue
            }
            synchronized(mClassName_Tag_Arg4StickyMap) {
                var tagArgMap = mClassName_Tag_Arg4StickyMap[busInfo.className]
                if (tagArgMap == null) {
                    tagArgMap = HashMap()
                    mClassName_Tag_Arg4StickyMap[busInfo.className] = tagArgMap
                }
                tagArgMap.put(tag, arg)
            }
            invokeBus(arg, busInfo, true)
        }
    }

    private fun removeStickyInner(tag: String) {
        val busInfoList: List<BusInfo>? = mTag_BusInfoListMap[tag]
        if (busInfoList == null) {
            Log.e(TAG, "The bus of tag <$tag> is not exists.")
            return
        }
        for (busInfo in busInfoList) {
            if (!busInfo.sticky) {
                continue
            }
            synchronized(mClassName_Tag_Arg4StickyMap) {
                val tagArgMap = mClassName_Tag_Arg4StickyMap[busInfo.className]
                if (tagArgMap == null || !tagArgMap.containsKey(tag)) {
                    return
                }
                tagArgMap.remove(tag)
            }
        }
    }

    private class BusInfo internal constructor(
        var tag: String,
        var className: String,
        var funName: String,
        var paramType: String,
        var paramName: String,
        var sticky: Boolean,
        var threadMode: String,
        var priority: Int
    ) {
        var method: Method? = null
        var subClassNames: MutableList<String>
        override fun toString(): String {
            return "BusInfo { tag : " + tag +
                    ", desc: " + desc +
                    ", sticky: " + sticky +
                    ", threadMode: " + threadMode +
                    ", method: " + method +
                    ", priority: " + priority +
                    " }"
        }

        private val desc: String
            private get() = className + "#" + funName +
                    if ("" == paramType) "()" else "($paramType $paramName)"

        init {
            subClassNames = CopyOnWriteArrayList()
        }
    }

    enum class ThreadMode {
        MAIN, IO, CPU, CACHED, SINGLE, POSTING
    }

    @Target(
        AnnotationTarget.FUNCTION,
        AnnotationTarget.PROPERTY_GETTER,
        AnnotationTarget.PROPERTY_SETTER
    )
    @Retention(
        RetentionPolicy.CLASS
    )
    annotation class Bus(
        val tag: String,
        val sticky: Boolean = false,
        val threadMode: ThreadMode = ThreadMode.POSTING,
        val priority: Int = 0
    )

    companion object {
        private val instance = BusUtils()
        private val NULL: Any = "nULl"
        private const val TAG = "BusUtils"
        fun register(bus: Any?) {
            instance.registerInner(bus)
        }

        fun unregister(bus: Any?) {
            instance.unregisterInner(bus)
        }

        @JvmOverloads
        fun post(tag: String, arg: Any = NULL) {
            instance.postInner(tag, arg)
        }

        @JvmOverloads
        fun postSticky(tag: String, arg: Any = NULL) {
            instance.postStickyInner(tag, arg)
        }

        fun removeSticky(tag: String) {
            instance.removeStickyInner(tag)
        }

        fun toString_(): String? {
            return instance.toString()
        }

        fun registerBus4Test(
            tag: String,
            className: String, funName: String, paramType: String, paramName: String,
            sticky: Boolean, threadMode: String, priority: Int
        ) {
            instance.registerBus(
                tag,
                className,
                funName,
                paramType,
                paramName,
                sticky,
                threadMode,
                priority
            )
        }
    }

    init {
        init()
    }
}