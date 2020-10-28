package top.androider.util

import java.lang.reflect.*
import java.util.*

/**
 * <pre>
 * author: Blankj
 * blog  : http://blankj.com
 * time  : 2017/12/15
 * desc  : utils about reflect
</pre> *
 */
class ReflectUtils private constructor(
    private val type: Class<*>,
    private val `object`: Any? = type
) {
    ///////////////////////////////////////////////////////////////////////////
    // newInstance
    ///////////////////////////////////////////////////////////////////////////
    /**
     * Create and initialize a new instance.
     *
     * @return the single [ReflectUtils] instance
     */
    fun newInstance(): ReflectUtils {
        return newInstance(*arrayOfNulls<Any>(0))
    }

    /**
     * Create and initialize a new instance.
     *
     * @param args The args.
     * @return the single [ReflectUtils] instance
     */
    fun newInstance(vararg args: Any?): ReflectUtils {
        val types = getArgsType(*args)
        return try {
            val constructor = type().getDeclaredConstructor(*types)
            newInstance(constructor, *args)
        } catch (e: NoSuchMethodException) {
            val list: MutableList<Constructor<*>> = ArrayList()
            for (constructor in type().declaredConstructors) {
                if (match(constructor.parameterTypes, types)) {
                    list.add(constructor)
                }
            }
            if (list.isEmpty()) {
                throw ReflectException(e)
            } else {
                sortConstructors(list)
                newInstance(list[0], *args)
            }
        }
    }

    private fun getArgsType(vararg args: Any?): Array<Class<*>> {
        return  args?.let {
            val result: MutableList<Class<*>> = mutableListOf()
            it.forEach { value ->
                result.add(value?.javaClass ?: NULL::class.java)
            }
            result.toTypedArray()
        }?:arrayOf<Class<*>>()
    }

    private fun sortConstructors(list: List<Constructor<*>>) {
        Collections.sort(list, Comparator<Constructor<*>> { o1, o2 ->
            val types1 = o1.parameterTypes
            val types2 = o2.parameterTypes
            val len = types1.size
            for (i in 0 until len) {
                if (types1[i] != types2[i]) {
                    return@Comparator if (wrapper(types1[i])!!.isAssignableFrom(wrapper(types2[i])!!)) {
                        1
                    } else {
                        -1
                    }
                }
            }
            0
        })
    }

    private fun newInstance(constructor: Constructor<*>?, vararg args: Any): ReflectUtils {
        return try {
            ReflectUtils(
                constructor!!.declaringClass,
                accessible(constructor)!!.newInstance(*args)
            )
        } catch (e: Exception) {
            throw ReflectException(e)
        }
    }
    ///////////////////////////////////////////////////////////////////////////
    // field
    ///////////////////////////////////////////////////////////////////////////
    /**
     * Get the field.
     *
     * @param name The name of field.
     * @return the single [ReflectUtils] instance
     */
    fun field(name: String): ReflectUtils {
        return try {
            val field = getField(name)
            ReflectUtils(field.type, field[`object`])
        } catch (e: IllegalAccessException) {
            throw ReflectException(e)
        }
    }

    /**
     * Set the field.
     *
     * @param name  The name of field.
     * @param value The value.
     * @return the single [ReflectUtils] instance
     */
    fun field(name: String, value: Any): ReflectUtils {
        return try {
            val field = getField(name)
            field[`object`] = unwrap(value)
            this
        } catch (e: Exception) {
            throw ReflectException(e)
        }
    }

    @Throws(IllegalAccessException::class)
    private fun getField(name: String): Field {
        val field = getAccessibleField(name)
        if (field.modifiers and Modifier.FINAL == Modifier.FINAL) {
            try {
                val modifiersField = Field::class.java.getDeclaredField("modifiers")
                modifiersField.isAccessible = true
                modifiersField.setInt(field, field.modifiers and Modifier.FINAL.inv())
            } catch (ignore: NoSuchFieldException) {
                // runs in android will happen
                field.isAccessible = true
            }
        }
        return field
    }

    private fun getAccessibleField(name: String): Field {
        var type: Class<*>? = type()
        return try {
            accessible(type!!.getField(name))!!
        } catch (e: NoSuchFieldException) {
            do {
                try {
                    return accessible(type!!.getDeclaredField(name))!!
                } catch (ignore: NoSuchFieldException) {
                }
                type = type!!.superclass
            } while (type != null)
            throw ReflectException(e)
        }
    }

    private fun unwrap(`object`: Any): Any {
        return if (`object` is ReflectUtils) {
            `object`.get<Any>()!!
        } else `object`
    }
    ///////////////////////////////////////////////////////////////////////////
    // method
    ///////////////////////////////////////////////////////////////////////////
    /**
     * Invoke the method.
     *
     * @param name The name of method.
     * @return the single [ReflectUtils] instance
     * @throws ReflectException if reflect unsuccessfully
     */
    @Throws(ReflectException::class)
    fun method(name: String): ReflectUtils {
        return method(name, *arrayOfNulls<Any>(0))
    }

    /**
     * Invoke the method.
     *
     * @param name The name of method.
     * @param args The args.
     * @return the single [ReflectUtils] instance
     * @throws ReflectException if reflect unsuccessfully
     */
    @Throws(ReflectException::class)
    fun method(name: String, vararg args: Any?): ReflectUtils {
        val types = getArgsType(*args)
        return try {
            val method : Method? = exactMethod(name, types)
            method(method, `object`, *args)
        } catch (e: NoSuchMethodException) {
            try {
                val method = similarMethod(name, types)
                method(method, `object`, *args)
            } catch (e1: NoSuchMethodException) {
                throw ReflectException(e1)
            }
        }
    }

    private fun method(method: Method?, obj: Any?, vararg args: Any?): ReflectUtils {
        return try {
            accessible<Method?>(method)
            if (method!!.returnType == Void.TYPE) {
                method.invoke(obj, *args)
                reflect(obj)
            } else {
                reflect(method.invoke(obj, *args))
            }
        } catch (e: Exception) {
            throw ReflectException(e)
        }
    }

    @Throws(NoSuchMethodException::class)
    private fun exactMethod(name: String, types: Array<Class<*>>): Method {
        var type: Class<*>? = type()
        return try {
            type!!.getMethod(name, *types)
        } catch (e: NoSuchMethodException) {
            do {
                try {
                    return type!!.getDeclaredMethod(name, *types)
                } catch (ignore: NoSuchMethodException) {
                }
                type = type!!.superclass
            } while (type != null)
            throw NoSuchMethodException()
        }
    }

    @Throws(NoSuchMethodException::class)
    private fun similarMethod(name: String, types: Array<Class<*>>): Method? {
        var type: Class<*>? = type()
        val methods: MutableList<Method> = ArrayList()
        for (method in type!!.methods) {
            if (isSimilarSignature(method, name, types)) {
                methods.add(method)
            }
        }
        if (!methods.isEmpty()) {
            sortMethods(methods)
            return methods[0]
        }
        do {
            for (method in type!!.declaredMethods) {
                if (isSimilarSignature(method, name, types)) {
                    methods.add(method)
                }
            }
            if (!methods.isEmpty()) {
                sortMethods(methods)
                return methods[0]
            }
            type = type.superclass
        } while (type != null)
        throw NoSuchMethodException(
            "No similar method " + name + " with params "
                    + Arrays.toString(types) + " could be found on type " + type() + "."
        )
    }

    private fun sortMethods(methods: List<Method>) {
        Collections.sort(methods, Comparator<Method> { o1, o2 ->
            val types1 = o1.parameterTypes
            val types2 = o2.parameterTypes
            val len = types1.size
            for (i in 0 until len) {
                if (types1[i] != types2[i]) {
                    return@Comparator if (wrapper(types1[i])!!.isAssignableFrom(wrapper(types2[i])!!)) {
                        1
                    } else {
                        -1
                    }
                }
            }
            0
        })
    }

    private fun isSimilarSignature(
        possiblyMatchingMethod: Method,
        desiredMethodName: String,
        desiredParamTypes: Array<Class<*>>
    ): Boolean {
        return possiblyMatchingMethod.name == desiredMethodName && match(
            possiblyMatchingMethod.parameterTypes,
            desiredParamTypes
        )
    }

    private fun match(declaredTypes: Array<Class<*>>, actualTypes: Array<Class<*>>): Boolean {
        return if (declaredTypes.size == actualTypes.size) {
            for (i in actualTypes.indices) {
                if (actualTypes[i] == NULL::class.java
                    || wrapper(declaredTypes[i])!!.isAssignableFrom(wrapper(actualTypes[i])!!)
                ) {
                    continue
                }
                return false
            }
            true
        } else {
            false
        }
    }

    private fun <T : AccessibleObject?> accessible(accessible: T?): T? {
        if (accessible == null) return null
        if (accessible is Member) {
            val member = accessible as Member
            if (Modifier.isPublic(member.modifiers)
                && Modifier.isPublic(member.declaringClass.modifiers)
            ) {
                return accessible
            }
        }
        if (!accessible.isAccessible) accessible.isAccessible = true
        return accessible
    }
    ///////////////////////////////////////////////////////////////////////////
    // proxy
    ///////////////////////////////////////////////////////////////////////////
    /**
     * Create a proxy for the wrapped object allowing to typesafely invoke
     * methods on it using a custom interface.
     *
     * @param proxyType The interface type that is implemented by the proxy.
     * @return a proxy for the wrapped object
     */
    fun <P> proxy(proxyType: Class<P>): P {
        val isMap = `object` is Map<*, *>
        val handler = InvocationHandler { proxy, method, args ->
            val name = method.name
            try {
                return@InvocationHandler reflect(`object`).method(name, *args).get<Any>()!!
            } catch (e: ReflectException) {
                if (isMap) {
                    val map = `object` as MutableMap<String, Any>?
                    val length = args?.size ?: 0
                    if (length == 0 && name.startsWith("get")) {
                        return@InvocationHandler map!![property(name.substring(3))]
                    } else if (length == 0 && name.startsWith("is")) {
                        return@InvocationHandler map!![property(name.substring(2))]
                    } else if (length == 1 && name.startsWith("set")) {
                        map!![property(name.substring(3))] = args[0]
                        return@InvocationHandler null
                    }
                }
                throw e
            }
        }
        return Proxy.newProxyInstance(
            proxyType.classLoader, arrayOf<Class<*>>(proxyType),
            handler
        ) as P
    }

    private fun type(): Class<*> {
        return type
    }

    private fun wrapper(type: Class<*>?): Class<*>? {
        if (type == null) {
            return null
        } else if (type.isPrimitive) {
            if (Boolean::class.javaPrimitiveType == type) {
                return Boolean::class.java
            } else if (Int::class.javaPrimitiveType == type) {
                return Int::class.java
            } else if (Long::class.javaPrimitiveType == type) {
                return Long::class.java
            } else if (Short::class.javaPrimitiveType == type) {
                return Short::class.java
            } else if (Byte::class.javaPrimitiveType == type) {
                return Byte::class.java
            } else if (Double::class.javaPrimitiveType == type) {
                return Double::class.java
            } else if (Float::class.javaPrimitiveType == type) {
                return Float::class.java
            } else if (Char::class.javaPrimitiveType == type) {
                return Char::class.java
            } else if (Void.TYPE == type) {
                return Void::class.java
            }
        }
        return type
    }

    /**
     * Get the result.
     *
     * @param <T> The value type.
     * @return the result
    </T> */
    fun <T> get(): T? {
        return `object` as T?
    }

    override fun hashCode(): Int {
        return `object`.hashCode()
    }

    override fun equals(obj: Any?): Boolean {
        return obj is ReflectUtils && `object` == obj.get<Any>()
    }

    override fun toString(): String {
        return `object`.toString()!!
    }
    private class NULL
    class ReflectException : RuntimeException {
        constructor(message: String?) : super(message) {}
        constructor(message: String?, cause: Throwable?) : super(message, cause) {}
        constructor(cause: Throwable?) : super(cause) {}

        companion object {
            private const val serialVersionUID = 858774075258496016L
        }
    }

    companion object {
        ///////////////////////////////////////////////////////////////////////////
        // reflect
        ///////////////////////////////////////////////////////////////////////////
        /**
         * Reflect the class.
         *
         * @param className The name of class.
         * @return the single [ReflectUtils] instance
         * @throws ReflectException if reflect unsuccessfully
         */
        @Throws(ReflectException::class)
        fun reflect(className: String): ReflectUtils {
            return reflect(forName(className))
        }

        /**
         * Reflect the class.
         *
         * @param className   The name of class.
         * @param classLoader The loader of class.
         * @return the single [ReflectUtils] instance
         * @throws ReflectException if reflect unsuccessfully
         */
        @Throws(ReflectException::class)
        fun reflect(className: String, classLoader: ClassLoader): ReflectUtils {
            return reflect(forName(className, classLoader))
        }

        /**
         * Reflect the class.
         *
         * @param clazz The class.
         * @return the single [ReflectUtils] instance
         * @throws ReflectException if reflect unsuccessfully
         */
        @Throws(ReflectException::class)
        fun reflect(clazz: Class<*>): ReflectUtils {
            return ReflectUtils(clazz)
        }

        /**
         * Reflect the class.
         *
         * @param object The object.
         * @return the single [ReflectUtils] instance
         * @throws ReflectException if reflect unsuccessfully
         */
        @Throws(ReflectException::class)
        fun reflect(`object`: Any?): ReflectUtils {
            return ReflectUtils(`object`?.javaClass ?: Any::class.java, `object`)
        }

        private fun forName(className: String): Class<*> {
            return try {
                Class.forName(className)
            } catch (e: ClassNotFoundException) {
                throw ReflectException(e)
            }
        }

        private fun forName(name: String, classLoader: ClassLoader): Class<*> {
            return try {
                Class.forName(name, true, classLoader)
            } catch (e: ClassNotFoundException) {
                throw ReflectException(e)
            }
        }

        /**
         * Get the POJO property name of an getter/setter
         */
        private fun property(string: String): String {
            val length = string.length
            return if (length == 0) {
                ""
            } else if (length == 1) {
                string.toLowerCase()
            } else {
                string.substring(0, 1).toLowerCase() + string.substring(1)
            }
        }
    }
}